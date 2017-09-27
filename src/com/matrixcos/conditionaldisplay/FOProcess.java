package com.matrixcos.conditionaldisplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class FOProcess {

	final static Logger logger = Logger.getLogger(FOProcess.class);
	
	
	//Extract FO-Condition from file-string.
	public String getFOCondition(String fileString) {

		String beginSearchString = "char=";
		String endSearchString   = "=char";
		String conditionString   = null;

		try {
			if (fileString != null) {
				int beginIndex, endIndex;
				beginIndex = fileString.indexOf(beginSearchString);
				endIndex = fileString.indexOf(endSearchString);

				if (beginIndex != -1 || endIndex != -1) {
					// get FO block condition
					conditionString = fileString.substring(beginIndex + 5,
							endIndex);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return conditionString;

	}

	
	//Extract sub conditions from main condition.
	public List<String> getSubConditions(String conditionString) {
		List<String> conditionsList = new ArrayList<String>();
		String foblockConditionString = null;
		
		try {
			
			// Replace condition name with java conditions for future use
			conditionString = conditionString.replaceAll(" AND ", "&&");
			conditionString = conditionString.replaceAll(" OR ", "||");
			conditionString = conditionString.replaceAll("\"", "");
			conditionString = conditionString.replaceAll("\'", "");
			conditionString = conditionString.replaceAll("IF ", "");
			conditionString = conditionString.replaceAll(" THEN", "");

			// Assign for future use.
			foblockConditionString = conditionString;
			conditionsList.add(foblockConditionString);

			conditionString = conditionString.replace("(", ""); 
			conditionString = conditionString.replace(")", "");
			
			StringTokenizer st = new StringTokenizer(conditionString.trim(),
					"&&||");

			while (st.hasMoreTokens()) {
				conditionsList.add(st.nextToken().trim());
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conditionsList;

	}

	//Match the condition value with Resultset value which is coming from database.
	private String verifyData(String valueType, String conditionKey,
			String conditionValue, String testValue,
			String primaryConditionString, String subCondition, String op) {
		
		try {
			//logger.info(conditionKey+ " ::"+conditionValue);
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			
			if (valueType.equals("DATE")) {
				Date conditionDate = format.parse(conditionValue);
				Date resultSetDate = format.parse(testValue);
				
				if (op.equals(">")) {
					if (resultSetDate.after(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}

				if (op.equals("<")) {
					if (resultSetDate.before(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}

				if (op.equals("<=")) {

					if (resultSetDate.before(conditionDate)
							|| resultSetDate.equals(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}

				if (op.equals(">=")) {

					if (resultSetDate.after(conditionDate)
							|| resultSetDate.equals(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}
				
				if (op.equals("==")) {

					if (resultSetDate.equals(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}
				
				if (op.equals("!=")) {

					if (!resultSetDate.equals(conditionDate)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}

			} else if (valueType.equals("NUMBER")) {
				
				 
				 String regDoublePattern = "^[\\+\\-]{0,1}[0-9]+[\\.\\,]{1}[0-9]+$"; 
				 String regIntPattern    = "^\\d+$";
				 
				 try
				 {
					 double conditionParsedValue = 0;
					 double testPrasedValue = 0;
					 
					 if(Pattern.matches(regDoublePattern, conditionValue))
					 {
						 conditionParsedValue = Double.parseDouble(conditionValue);
						 testPrasedValue      = Double.parseDouble(testValue); 
					 }
					 else if(Pattern.matches(regIntPattern, conditionValue))
					 {
						 conditionParsedValue = Integer.parseInt(conditionValue);
						 testPrasedValue      = Integer.parseInt(testValue);
					 }
					 
					 
					 if (op.equals(">")) {
						 
						 	logger.info("---" +conditionParsedValue);
						 	logger.info("---" +testPrasedValue);
						 	
							if (testPrasedValue > conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}

						}

						if (op.equals("<")) {

							if (testPrasedValue < conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}

						}

						if (op.equals("<=")) {
							if (testPrasedValue < conditionParsedValue
									|| testPrasedValue == conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}

						}

						if (op.equals(">=")) {
							if (testPrasedValue > conditionParsedValue
									|| testPrasedValue == conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}
						}
						
						if (op.equals("==")) {
							if (testPrasedValue == conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}
						}
						
						if (op.equals("!=")) {
							if (testPrasedValue != conditionParsedValue) {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "TRUE");
							} else {
								primaryConditionString = primaryConditionString
										.replace(subCondition, "FALSE");
							}
						}
					 
					 
				 }
				 catch(Exception ex)
				 {
					 ex.printStackTrace();
				 }
				
			}
			
			else if(valueType.equals("INTEGER"))
			{
				 
				 try
				 {
					int conditionParsedValue = Integer.parseInt(conditionValue);
					int testPrasedValue      = Integer.parseInt(testValue);
					
					if (op.equals(">")) {
						if (testPrasedValue > conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals("<")) {

						if (testPrasedValue < conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals("<=")) {
						if (testPrasedValue < conditionParsedValue
								|| testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals(">=")) {
						if (testPrasedValue > conditionParsedValue
								|| testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					
					if (op.equals("==")) {
						if (testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					
					if (op.equals("!=")) {
						if (testPrasedValue != conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					 
				 }
				 catch(Exception ex)
				 {
					 ex.printStackTrace();
				 }
				 
			}
			
			else if(valueType.equals("FLOAT"))
			{
				 
				 try
				 {
					float conditionParsedValue = Float.parseFloat(conditionValue);
					float testPrasedValue      = Float.parseFloat(testValue);
					
					
					if (op.equals(">")) {
						if (testPrasedValue > conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals("<")) {

						if (testPrasedValue < conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals("<=")) {
						if (testPrasedValue < conditionParsedValue
								|| testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}

					}

					if (op.equals(">=")) {
						if (testPrasedValue > conditionParsedValue
								|| testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					
					if (op.equals("==")) {
						if (testPrasedValue == conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					
					if (op.equals("!=")) {
						if (testPrasedValue != conditionParsedValue) {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "TRUE");
						} else {
							primaryConditionString = primaryConditionString
									.replace(subCondition, "FALSE");
						}
					}
					 
				 }
				 catch(Exception ex)
				 {
					 ex.printStackTrace();
				 }
				 
			}

			else if (valueType.equals("VARCHAR2") || valueType.equals("CHAR")) {
				
				if (op.equals("==")) {
					if (testValue.equalsIgnoreCase(conditionValue)) {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}

				}
				if (op.equals("!=")) {

					if (!testValue.equalsIgnoreCase(conditionValue)) {
						logger.info(testValue + " != "+conditionValue);
						primaryConditionString = primaryConditionString
								.replace(subCondition, "TRUE");
					} else {
						primaryConditionString = primaryConditionString
								.replace(subCondition, "FALSE");
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return primaryConditionString;

	}

	private String evaluateSubCond(List<FOBean> foTestData, String primaryConditionString, String subCondition, String operator)
	 {

			String conditionData[] = subCondition.split(operator);
			logger.info("Condition Data Lenth is  :" + conditionData.length);
			if (conditionData.length == 2) {
				logger.info("Condition Key And Value before Trim:"+conditionData[0]+":"+conditionData[1]);	
				String conditionKey = conditionData[0];
				String conditionValue = conditionData[1];
				conditionKey = conditionKey.toString().trim();
				conditionValue = conditionValue.toString().trim();
				logger.info("Condition Key And Value After Trim:"+conditionKey+":"+conditionValue);
				String testValue = null;
				String valueType = null;
				String dbAttributeKey = null;

				if (conditionValue.equals(FOConstants.FO_CURRENT_DATE)) {
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat sdf = new SimpleDateFormat(pattern);

					Date today = Calendar.getInstance().getTime();
					conditionValue = sdf.format(today);

					// logger.info("condition value ----" +conditionValue);
				}

				// get the value from database
				for (int j = 0; j < foTestData.size(); j++) {
					// logger.info(foTestData.get(j).getAttributeKey()
					// +" == "+conditionKey+" :: "+foTestData.get(j).getAttributeValue());
					// logger.info(foTestData.get(j).getAttributeKey().length()+" :: "+conditionKey.length());
					if (foTestData.get(j).getAttributeKey()
							.equalsIgnoreCase(conditionKey)) {
						testValue = foTestData.get(j).getAttributeValue();
						valueType = foTestData.get(j).getAttributeType();
						dbAttributeKey = foTestData.get(j).getAttributeKey();
						break;
					}
				}

				if (dbAttributeKey != null
						&& (dbAttributeKey.equalsIgnoreCase(conditionKey))
						&& testValue != null) {
					// when database variables and condition key got matches along
					// with test value have data then go for evaluating to return
					// FNAL CONDITION.
					logger.info("Going to call verifyData method when variables got matches");
					primaryConditionString = verifyData(valueType, conditionKey,
							conditionValue, testValue, primaryConditionString,
							subCondition, operator);

				} else if (dbAttributeKey != null
						&& (dbAttributeKey.equalsIgnoreCase(conditionKey))
						&& testValue == null) {
					// Replacement value (L.H.S value ) in condition is NULL, then
					// remove condition from FO File and return with database text
					// for Line items
					logger.info(" Going to remove conditions from template when testValue is NULL. Please check your field Value in db table.");
					primaryConditionString = primaryConditionString.replace(
							subCondition, "FALSE");

				} else if (dbAttributeKey != null
						&& (dbAttributeKey.equalsIgnoreCase(conditionKey))
						&& conditionValue == null) {
					// Replacement value (L.H.S value ) in condition is NULL, then
					// remove condition from FO File and return with database text
					// for Line items
					logger.info(" Going to remove conditions from template when conditionValue is NULL, Please check your Condition.");
					primaryConditionString = primaryConditionString.replace(
							subCondition, "FALSE");

				} else if (dbAttributeKey == null && testValue == null) {
					// Database variables mismatches and also replacement value
					// (L.H.S) value is NULL then ROLLBACK the process and return
					// same file which passed earlier.
					try {
						logger.error(conditionKey
								+ " is not a valid database variable.");
						return "ROLLBACK_FO_TEMPLATE";

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				logger.info(" Going to remove conditions from template , There is problem with your Condition in table.");
				primaryConditionString = primaryConditionString.replace(
						subCondition, "FALSE");
			}
			return primaryConditionString;

		}
	
	public String splitConditions(List<String> subConditionsList,
			String primaryConditionString, List<FOBean> foTestData) 
	{

		try 
		{
			for (int i = 0; i < subConditionsList.size(); i++) 
			{
				if (subConditionsList.get(i).contains("!=")) 
				{
					String operator = "!=";
					//logger.info("subConditionsList.get(i) is :"+subConditionsList.get(i));
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}
				}

				else if (subConditionsList.get(i).contains("==")) 
				{
					String operator = "==";
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}
				}
				
				else if (subConditionsList.get(i).contains("<=")) 
				{
					String operator = "<=";
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}

				}
				
				else if (subConditionsList.get(i).contains(">=")) 
				{
					String operator = ">=";
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}
				}
				
				else if (subConditionsList.get(i).contains("<")) 
				{
					String operator = "<";
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}
				}
				
				else if (subConditionsList.get(i).contains(">")) 
				{
					String operator = ">";
					primaryConditionString = evaluateSubCond(foTestData, primaryConditionString, subConditionsList.get(i), operator);
					if(primaryConditionString.equalsIgnoreCase("ROLLBACK_FO_TEMPLATE")){
						return primaryConditionString;
					}
				}

			}
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}

		return primaryConditionString;

	}

	//Evaluate and return boolean value from final boolean condition string
	public boolean evaluateCondition(String finalCondition) {

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

	//Remove the data from string.
	public String removeDataFromString(boolean isMatched, String fileString, String searchString) 
	{
		try 
		{
			logger.info("searchString is :"+searchString);
			if (isMatched) 
			{	
				// This will only remove start and end foblock and keep the data in the document
				logger.info("searchString in If is :"+searchString);
				fileString = fileString.replaceFirst(Pattern.quote(searchString), "");
				fileString = fileString.replaceFirst(Pattern.quote(searchString), "");
			} 
			else 
			{
				// This will remove whole foblock along with data.
				int beginIndex = fileString.indexOf(searchString);
				int endIndex = fileString.indexOf(searchString, beginIndex+1);
				
				if (fileString != null) 
				{

					if (beginIndex != -1 && endIndex != -1) 
					{
						String temp2 = fileString.substring(beginIndex, endIndex+searchString.length());
						fileString = fileString.replaceFirst(Pattern.quote(temp2), "");
					}
				}
			}
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return fileString;

	}
	public void parseFile(String fileString,List<FOBean> foInTakeData )
	{
		FOProcess foProcess = new FOProcess();
		String conditionString = null;
		String temp = "<fo:block color=\"white\" text-align=\"center\">char=";
		String temp2 = "=char</fo:block>";
		boolean evaluatedExpressionResult = true; // based on this variable we decide whether to remove data block between fo condition block along with conditions.
		
		do {

			// Step-2 : Read condition one by one from fo-file string
			conditionString = foProcess.getFOCondition(fileString);
			logger.info("O/P From Step - 2 conditionString -----------"+ conditionString);
			String searchString = temp + conditionString + temp2;

			// Step -3: Get list of sub conditions in the selected condition
			List<String> subConditionsList = foProcess.getSubConditions(conditionString.toUpperCase());
			String primaryConditionString = subConditionsList.get(0);
			subConditionsList.remove(0);
			logger.info("O/P From Step - 3 - list of sub conditons -----"+ subConditionsList);

				
			if (foInTakeData.size() > 0) // Incase of intake data available then evaluating condition
			{
				// Step-4 : Split the conditions and return final conditional string with boolean value.
				String finalCondition = foProcess.splitConditions(subConditionsList, primaryConditionString, foInTakeData);
				logger.info("O/P From Step - 4 - condition string with bool values -----"+ finalCondition);

				// Step-5 : evaluate the condition and returns either true or false
				evaluatedExpressionResult = foProcess.evaluateCondition(finalCondition);
				logger.info("O/P From Step - 5 - is Matched -----"+ evaluatedExpressionResult);
			}
			fileString = foProcess.removeDataFromString(evaluatedExpressionResult, fileString, searchString);
		}
		while (conditionString != null);
		
	}

}
