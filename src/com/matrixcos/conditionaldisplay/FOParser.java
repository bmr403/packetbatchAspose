package com.matrixcos.conditionaldisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class FOParser {

	final static Logger logger = Logger.getLogger(FOParser.class);

	public static final String TEMPLATE_DELIMITER = "::"; // using triple pipe
															// symbol as
															// delimiter
	public static final String DB_VARAIBLE_DELIMITER = ","; // using db
															// variables
															// delimiter

	public static String fo_ConditionBuilder(String fileContent,
			long intakeNumber, long clientCodeValue, String documentIdValue,
			Connection connection) {

		boolean isDbConnected = false;
		FOProcess foProcess = new FOProcess();
		ArrayList<String> conditionVarList = new ArrayList<String>();
		String[] conditionArrray = new String[] {};
		String tempFileConetent = null;
		String hr_Detail_Query = null;
		String tempFileConetent_LineItem = null;
		String dbVariables = null;
		String conditionString = null; // This denotes the string that is going
										// to get extracted from file string
		String searchString = null;
		boolean isMatched = false;
		String rollbackFileContent = null;

		isDbConnected = connection != null ? true : false; // Why always If else
															// , use ternary
															// operator.

		tempFileConetent = fileContent;
		tempFileConetent_LineItem = fileContent;
		rollbackFileContent = fileContent;

		// getting attributes form conditional attribute query
		List<FOBean> foBeanQueryList = new ArrayList();
		try {

			ResultSet query_ResultSet = buildQueryDynamically(connection,
					String.valueOf(clientCodeValue), documentIdValue, "", "",
					"EC_PKT_FO_CONDITION_ATTR_QUERY");
			while (query_ResultSet.next()) {
				dbVariables = query_ResultSet.getString("DB_VARIABLES");
				hr_Detail_Query = query_ResultSet
						.getString("QUERY_DESCRIPTION");

				logger.info("hr_Detail_Query is :" + hr_Detail_Query);
				// Fire query into HR_DATA_DETAIL database and get data from it.
				foBeanQueryList = fetchDataFromQuery(hr_Detail_Query,
						documentIdValue, intakeNumber, connection,
						String.valueOf(clientCodeValue)); // fetching list of fo
															// bean objects
				logger.info(foBeanQueryList.size());
			}
		} catch (Exception ex) {
			logger.info("Exception while getting attributes of document from Attributes query:"+ex.getStackTrace());
		}
		
		do { // Get all conditions from FO template and storing in list.
			conditionString = foProcess.getFOCondition(tempFileConetent);
			if (conditionString != null) {
				logger.info("Condition String is :" + conditionString);
				String temp = "<fo:block color=\"white\" text-align=\"center\">char=";
				String temp2 = "=char</fo:block>";
				searchString = temp + conditionString + temp2; // searching in
																// temp fo file
																// for condition
																// string and
																// remove them
																// only in temp
																// fo content
																// file.
				logger.info("Search String is :" + searchString);
				
				tempFileConetent = tempFileConetent.replaceFirst(
						Pattern.quote(searchString), "");
				tempFileConetent = tempFileConetent.replaceFirst(
						Pattern.quote(searchString), "");
				conditionString = conditionString.replaceFirst("<", "");
				conditionString = conditionString.replaceFirst(">", "");
				// conditionVarList.add(conditionString);

				// Split condition using delimiter.
				conditionArrray = conditionString.split(TEMPLATE_DELIMITER); // It
																				// will
																				// returns
																				// only
																				// two
																				// string
																				// arrays
																				// separated
																				// by
																				// '|||'
																				// symbol
																				// as
																				// delimiter,
																				// it
																				// depends
																				// on
																				// dynamic
																				// variable
																				// in
																				// fo
																				// template.
				String clientCode = conditionArrray[1];
				String documentId = conditionArrray[2];
				String conditionId = conditionArrray[3];
				logger.info(clientCode + " == " + documentId + " == "
						+ conditionId);
				if (isDbConnected) {
					// logger.info("Db connected");
					// Get query from database table.

					try {
						if (foBeanQueryList.size() != 0) {
							// Get Conditions from database table based on
							// client details.
							ResultSet conditionsResultSet = buildQueryDynamically(
									connection, clientCode, documentId,
									conditionId, "", "EC_PKT_FO_CONDITION"); // Get
																				// condition
																				// based
																				// on
																				// condition
																				// ID
							String condition = null;
							List<String> conditionsList = null;
							String primaryConditionString = null;
							String finalCondition = null;
							String eachConditionBlock = null;
							while (conditionsResultSet.next()) {
								// looping condition check based on result set
								// size.
								logger.info("CONDITION is : "
										+ conditionsResultSet
												.getString("CONDITION"));
								condition = conditionsResultSet
										.getString("CONDITION");
								// Get sub conditions from main condition and
								// maintain them in a list.
								conditionsList = foProcess
										.getSubConditions(condition);
								logger.info("Before removing condition in lIst :"
										+ conditionsList);
								primaryConditionString = conditionsList.get(0);
								conditionsList.remove(0); // removing first
															// index value i.e,
															// Full condition
															// from list.
								logger.info("After removing condition in lIst :"
										+ conditionsList);
								// Split each condition from list object and
								// return the Final condition which needs to
								// evaluate.
								finalCondition = foProcess.splitConditions(
										conditionsList, primaryConditionString,
										foBeanQueryList);
								// We will get here Final which needs to be
								// evaluated as T or F.
								if (finalCondition
										.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")) {
									// If the flag indicates roll back process
									// then return the original file content.
									// throw new
									// Exception("Condition does not have valid database variable(s).");
									return rollbackFileContent;

								}
								logger.info("O/P - condition string with bool values -----"
										+ finalCondition);

								// evaluate the condition and returns either
								// true or false

								isMatched = evaluateCondition(finalCondition
										.replaceAll("\\s", ""));
								logger.info("O/P - is Matched -----"
										+ isMatched);
								logger.info(" :: " + conditionsList
										+ " :: Size is :"
										+ conditionsList.size());
								if (isMatched) {
									// Remove the data from string based on
									// condition
									// logger.info(tempFileConetent);
									logger.info(tempFileConetent_LineItem
											.indexOf(searchString));

									logger.info("Search Str  is :"
											+ searchString);
									int lineItemBeginIndex = tempFileConetent_LineItem
											.indexOf(searchString);
									tempFileConetent_LineItem = tempFileConetent_LineItem
											.replaceFirst(
													Pattern.quote(searchString),
													"");
									int lineItemEndIndex = tempFileConetent_LineItem
											.lastIndexOf(searchString);
									tempFileConetent_LineItem = tempFileConetent_LineItem
											.replaceFirst(
													Pattern.quote(searchString),
													"");
									logger.info(lineItemBeginIndex + " :"
											+ lineItemEndIndex);
									eachConditionBlock = tempFileConetent_LineItem
											.substring(lineItemBeginIndex,
													lineItemEndIndex);

									// logger.info("each Condition Block is :"+eachConditionBlock);
									// logger.info(tempFileConetent.substring(lineItemBeginIndex,lineItemEndIndex));

									// logger.info("tempFile Conetent_LineItem is :: "+tempFileConetent_LineItem);

									String prefix = "FO_LineItem=";
									String suffix = "=FO_LineItem";
									String lineItemString = null;
									try {
										do {

											int beginIndex, endIndex;
											beginIndex = eachConditionBlock
													.indexOf(prefix);
											endIndex = eachConditionBlock
													.indexOf(suffix);

											logger.info(beginIndex + " ::"
													+ endIndex);

											if (beginIndex != -1
													|| endIndex != -1) {
												// get FO block condition

												lineItemString = eachConditionBlock
														.substring(beginIndex,
																endIndex + 12);
												logger.info("LineItem Search String is :"
														+ lineItemString);
												String LineItemSearch = lineItemString;
												// if(lineItemSearchString !=
												// null){
												eachConditionBlock = eachConditionBlock
														.replaceAll(
																Pattern.quote(lineItemString),
																"");
												lineItemString = lineItemString
														.replaceFirst(prefix,
																"");
												lineItemString = lineItemString
														.replaceFirst(suffix,
																"");

												logger.info(clientCode + " :"
														+ documentId + " : "
														+ conditionId);
												// fetchConditionLineItems(connection,
												// clientCode, documentId,
												// conditionId,
												// lineItemSearchString);
												ResultSet lineItemResultSet = buildQueryDynamically(
														connection, clientCode,
														documentId,
														conditionId,
														lineItemString,
														"EC_PKT_FO_CONDTN_DISPY_CONTENT");
												logger.info("Line Item Size is :"
														+ lineItemResultSet
																.getFetchSize());
												while (lineItemResultSet.next()) {
													String displayContent = lineItemResultSet
															.getString("DISPLAY_CONTENT");
													logger.info("display Content is == "
															+ displayContent);

													logger.info(LineItemSearch);
													// tempFileConetent =
													// tempFileConetent.replaceAll(LineItemSearch,
													// displayContent);
													if(displayContent != null){
														fileContent = fileContent.replaceAll(LineItemSearch,displayContent);
													} else {
														fileContent = fileContent.replaceAll(LineItemSearch,"");
													}
													// logger.info("Final Display Content is == "+fileContent);
													// logger.info("tempFileConetent Content is == "+tempFileConetent);
													// logger.info("TempFile Conetent LineItem is == "+tempFileConetent_LineItem);
													logger.info("each Condition Block in While is :"
															+ eachConditionBlock);

													break;
												}

											} else {
												logger.info("After Null in Else");
												break;
											}
										} while (lineItemString != null);

										logger.info(clientCode + " :"
												+ documentId + " : "
												+ conditionId);
										fileContent = foProcess
												.removeDataFromString(
														isMatched, fileContent,
														searchString);

									} catch (Exception ex) {
										ex.printStackTrace();
									}
								} else {
									// Remove conditions and display data blocks
									logger.info("Removing conditions in Template ");
									fileContent = foProcess
											.removeDataFromString(isMatched,
													fileContent, searchString);
									// fileContent = tempFileConetent;

								}

							}
						} // foBeanQueryList.size()
						else
						{
							logger.info("Removing condition in Template as conditional attribute qry returned 0 rows. Please Check Query or Data not available in Table");
							isMatched = false;
							fileContent = foProcess
									.removeDataFromString(isMatched,
											fileContent, searchString);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				logger.info("conditionString is : " + conditionString);
			}

		} while (conditionString != null);

		return fileContent;

	}

	public static ResultSet buildQueryDynamically(Connection connection,
			String clientCode, String documentId, String conditionId,
			String lineTemId, String quertType) {
		String query = null;
		if (quertType.equalsIgnoreCase("EC_PKT_FO_CONDITION_ATTR_QUERY")
				&& (clientCode != null && documentId != null)) {
			query = "select QUERY_DESCRIPTION,DB_VARIABLES from EC_PKT_FO_CONDITION_ATTR_QUERY where CLI_CODE='"
					+ clientCode + "' and DOCUMENT_ID='" + documentId + "' ";
			logger.info("EC_PKT_FO_CONDITION_ATTR_QUERY query is :" + query);
		} else if (quertType.equalsIgnoreCase("EC_PKT_FO_CONDITION")
				&& (clientCode != null && documentId != null && conditionId != null)) {
			query = "select CONDITION from EC_PKT_FO_CONDITION where CLI_CODE = '"
					+ clientCode
					+ "' and DOCUMENT_ID = '"
					+ documentId
					+ "' and CONDITION_ID = '" + conditionId + "' ";
			logger.info("EC_PKT_FO_CONDITION query is :" + query);
		} else if (quertType.equalsIgnoreCase("EC_PKT_FO_CONDTN_DISPY_CONTENT")
				&& (clientCode != null && documentId != null
						&& conditionId != null && lineTemId != null)) {
			query = "select DISPLAY_CONTENT from EC_PKT_FO_CONDTN_DISPY_CONTENT where CONDITIONLINEITEM_ID= "
					+ lineTemId
					+ " AND CONDITION_ID = '"
					+ conditionId
					+ "' AND DOCUMENT_ID = '"
					+ documentId
					+ "' AND CLI_CODE = '" + clientCode + "' ";
			logger.info("EC_PKT_FO_CONDTN_DISPY_CONTENT query is :" + query);
		}

		ResultSet query_resultSet = null;

		try {
			PreparedStatement ps_query = null;
			if (query != null) {
				ps_query = connection.prepareStatement(query);
				query_resultSet = ps_query.executeQuery();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				// con.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return query_resultSet;

	}

	public ResultSet fetchHRDetailData(String query, Connection con,
			long intakeNumber) {
		// Connection con = null;
		ResultSet hrDetail_ResultSet = null;
		PreparedStatement hrDetail_PStmt = null;
		try {
			if (query != null) {
				hrDetail_PStmt = con.prepareStatement(query);
				hrDetail_PStmt.setLong(1, intakeNumber); // Query input
				hrDetail_ResultSet = hrDetail_PStmt.executeQuery();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				// con.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return hrDetail_ResultSet;

	}

	public static List<FOBean> fetchDataFromQuery(String query,
			String documentId, long intakeNumber, Connection con,
			String clientCode) {
		// Connection con = null;

		List<FOBean> foBeanList = new ArrayList<FOBean>();

		try {
			logger.info("FO Parser ::::::" + intakeNumber + "---" + documentId
					+ "----" + clientCode);

			// PreparedStatement ps =
			// con.prepareStatement(FOConstants.RESULT_SET_QUERY);
			PreparedStatement ps = con.prepareStatement(query);
			ps.setLong(1, intakeNumber); // Query input
			ps.setString(2, documentId); // Query input
			ps.setString(3, clientCode); // Query input
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				logger.info("FO Parser Inside resultset::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				// Finding and Setting the column name and its data type.
				ResultSetMetaData rsMetaData = rs.getMetaData();

				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					logger.info("FO Parser::::::: FO_"
							+ rsMetaData.getColumnName(i).toUpperCase());
					logger.info("FO Parser value:::::::"
							+ rs.getString(rsMetaData.getColumnName(i)));
					FOBean foBean = new FOBean();
					foBean.setAttributeKey("FO_"
							+ rsMetaData.getColumnName(i).toUpperCase()); // Append
																			// "FO_"
																			// for
																			// differentiate
																			// b/w
																			// Merged
																			// and
																			// Condition
																			// variables.
					foBean.setAttributeType(rsMetaData.getColumnTypeName(i)
							.toUpperCase());
					foBean.setAttributeValue(rs.getString(rsMetaData
							.getColumnName(i)));
					foBeanList.add(foBean);
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				// con.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return foBeanList;

	}

	// Evaluate and return boolean value from final boolean condition string
	public static boolean evaluateCondition(String finalCondition) {

		int firstIndex, lastIndex;

		try {
			do {
				firstIndex = finalCondition.lastIndexOf("(");
				lastIndex = finalCondition.indexOf(")", firstIndex);

				if (firstIndex != -1 || lastIndex != -1) {
					String firstString = finalCondition.substring(firstIndex,
							lastIndex + 1);

					if (firstString.contains("&&")) {

						String cString[] = firstString.split(
								Pattern.quote("&&"), 2);

						if (cString.length > 1) {
							boolean opnd1 = Boolean.parseBoolean(cString[0]
									.replace("(", "").trim());
							boolean opnd2 = Boolean.parseBoolean(cString[1]
									.replace(")", "").trim());

							if (opnd1 && opnd2) {
								finalCondition = finalCondition.replace(
										firstString, "TRUE");
							} else {
								finalCondition = finalCondition.replace(
										firstString, "FALSE");
							}

						}

					} else if (firstString.contains("||")) {
						String cString[] = firstString.split(
								Pattern.quote("||"), 2);

						if (cString.length > 1) {
							boolean opnd1 = Boolean.parseBoolean(cString[0]
									.replace("(", "").trim());
							boolean opnd2 = Boolean.parseBoolean(cString[1]
									.replace(")", "").trim());

							if (opnd1 || opnd2) {
								finalCondition = finalCondition.replace(
										firstString, "TRUE");
							} else {
								finalCondition = finalCondition.replace(
										firstString, "FALSE");
							}

						}
					}

					else if (firstString.contains("(TRUE)")) {
						finalCondition = finalCondition.replace(firstString,
								"TRUE");
					}

					else if (firstString.contains("(FALSE)")) {
						finalCondition = finalCondition.replace(firstString,
								"FALSE");
					}

				}
			} while (firstIndex != -1 || lastIndex != -1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Boolean.parseBoolean(finalCondition);

	}

}
