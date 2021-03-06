/*
 * DBUtil.java
 *
 * Created on September 17, 2007, 8:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.matrix.intake.ecomm.packet.Main;
import com.matrix.intake.ecomm.packet.PacketsBatchGeneration;
import com.matrix.intake.ecomm.packet.objects.DocumentInfo;
import com.matrix.intake.ecomm.packet.objects.EcommVO;
import com.matrix.intake.ecomm.packet.objects.MajorControlFileInfo;
import com.matrix.intake.ecomm.packet.objects.MinorControlFileInfo;

/**
 * 
 * @author Venkat, Kalyan Bobba(changes)
 */
public class DBUtil {

	private static final String EMPTY = "";

	private static Logger log = Logger.getLogger(DBUtil.class);

	// ResourceUtils resourceUtils;
	private static Properties props;

	Connection conn = null;

	/**
	 * Creates a new instance of DBUtil
	 * 
	 * @param props
	 */
	public DBUtil(Properties appProps) {
		DBUtil.props = appProps;
		initConnection();
	}

	private void initConnection() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(props
					.getProperty("database.url"), props
					.getProperty("workdesk.user"), props
					.getProperty("workdesk.pass"));
		} catch (SQLException e) {
			log.error("initConnection", e);
		}

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("finalize. Error closing connection.", e);
			}
		}
	}

	public int isValidPacketNumberAvailable() {
		int theReturnValue = -1;
		Statement stmnt = null;
		ResultSet rs = null;
		try {

			stmnt = conn.createStatement();
			rs = stmnt
					.executeQuery("select PACKET_NUMBER from WD_ECOMM_PACKET_HEADER "
							+ " where PACKET_STATUS = 'O' ORDER BY PACKET_NUMBER DESC");
			// technically there should be only one returned
			// meaning at any point of time there can be only one packet number
			// in open status
			if (null != rs) {
				while (rs.next()) {
					theReturnValue = rs.getInt(1);
					break;
				}
			}
		} catch (SQLException e) {
			log.error("isValidPacketNumberAvailable()", e);
		} finally {
			closeResultSet(rs);
			closeStatement(stmnt);
		}
		return theReturnValue;
	}

	public List<DocumentInfo> findDocs(long ecomm) {
		List<DocumentInfo> docList = new ArrayList<DocumentInfo>(); // the
		
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();

			String sql = " SELECT a.DOCUMENT_ID, REPLACE (a.DESCRIPTION, '&', '&amp;'),"
					+ " a.DOCUMENT_TYPE, a.TEMPLATE_NAME,b.DOCUMENT_DISPLAY_SEQUENCE"
					+ " FROM ECONFIG.EC_DOCUMENT_REPOSITORY a, "
					+ "      WORKDESK.WD_ECOMM_DOCUMENTS b "
					+ " WHERE a.DOCUMENT_ID = b.DOCUMENT_ID "
					+ " AND b.ECOMM_NUMBER = "
					+ ecomm
					+ " ORDER BY b.DOCUMENT_DISPLAY_SEQUENCE ASC";

			log.debug(sql);

			rs = stmt.executeQuery(sql);

			log.info("STATUS : DocumentList for eComm = " + ecomm);
			if (null != rs) {
				// successfully executed
				int index = 1;
				while (rs.next()) {
					DocumentInfo docInfo = new DocumentInfo(ecomm, rs
							.getString(1), rs.getString(2), rs.getString(3), rs
							.getString(4));
					docList.add(docInfo);
					log.info(index++ + ". " + docInfo.getDocumentId()); // FIXME
				}
			}
		} catch (SQLException e) {
			log.error("DBUtil.findAllDcoumentIDsForEcomm() ", e);
		} finally {
			closeResultSet(rs);
			closeStatement(stmt);
		}
		log.info("DocumentListSize=" + docList.size());
		return docList;
	}

	/**
	 * Finds the list of eComms from WD_ECOMM_PACKET_DETAILS who's status is 'N'
	 * 
	 * @param packet
	 * @return
	 */
	public List<EcommVO> findNewEcomms(int packet) {
		List<EcommVO> ecommList = new ArrayList<EcommVO>();
		Statement stmnt = null;
		ResultSet rs = null;
		try {
			stmnt = conn.createStatement();
			String sql = "SELECT   PD.ECOMM_NUMBER, EH.CLI_CODE, EH.CREATED_DATETIME, IH.INTAKE_NUMBER, IH.MAILING_ADDRESS_FLAG, IH.CREATED_DATE "
					+ " FROM   WORKDESK.WD_ECOMM_PACKET_DETAILS PD, "
					+ "      WORKDESK.WD_ECOMM_HEADER EH, EFILING.EF_INTAKE_HEADER IH"
					+ " WHERE PD.ECOMM_PACKET_STATUS = 'T' AND PD.PACKET_NUMBER = " ////Changed to 'T' from 'N' For tesaing by Nirmal
					+ packet
					+ "    AND PD.ECOMM_NUMBER  = EH.ECOMM_NUMBER"
					+ "    AND EH.INTAKE_NUMBER = IH.INTAKE_NUMBER "
					+ " ORDER BY   PD.ECOMM_NUMBER ASC";

			rs = stmnt.executeQuery(sql);

			// technically there should be only one returned
			// meaning at any point of time there can be only one packet number
			// in open status
			if (null != rs) {
				// successfully executed
				log.info("Ecomms found in PACKET DETAIL TABLE");
				while (rs.next()) {
					long ecomm = rs.getLong(1);
					String mailingAddressFlag = rs.getString(5);
					log.info("ECOMM NUMBER -> " + ecomm + ", "
							+ mailingAddressFlag);
					ecommList.add(new EcommVO(ecomm, rs.getLong(2), rs
							.getString(3), rs.getLong(4), mailingAddressFlag,
							rs.getString(6)));
				}
			}

		} catch (SQLException e) {
			log.error("DBUtil.findAllNewEcommsFromPacketDetail() ", e);
		} finally {
			closeResultSet(rs);
			closeStatement(stmnt);
		}
		return ecommList;
	}

	public MajorControlFileInfo getMajorControlFileInfo(long ecomm) {
		MajorControlFileInfo mcfInfo = new MajorControlFileInfo();
		ResultSet rs = null;
		Statement stmnt = null;
		try {
			stmnt = conn.createStatement();
			String query = "SELECT "
					+ " b.CLIENT_NAME, b.CLI_CODE, c.FIRST_NAME, c.LAST_NAME "
					+ " FROM WD_ECOMM_HEADER a, EC_CLIENT b, EF_INTAKE_HEADER c "
					+ " WHERE " + " a.INTAKE_NUMBER = c.INTAKE_NUMBER "
					+ " AND a.CLI_CODE = b.CLI_CODE " + " AND a.ECOMM_NUMBER ="
					+ ecomm;

			rs = stmnt.executeQuery(query);

			// technically there should be only one returned
			// meaning at any point of time there can be only one packet number
			// in open status
			if (null != rs) {
				// successfully executed
				while (rs.next()) {
					mcfInfo.setTheClientName(rs.getString(1));
					mcfInfo.setTheClientCode(String.valueOf(rs.getInt(2)));
					mcfInfo.setTheFirstName(rs.getString(3));
					mcfInfo.setTheLastName(rs.getString(4));
				}
			}

		} catch (Exception e) {
			log.error("DBUtil.getMajorControlFileInfo ", e);
		} finally {
			closeResultSet(rs);
			closeStatement(stmnt);
		}
		return mcfInfo;
	}

	/**
	 * Minor Control File Info.
	 * 
	 * @param ecomm
	 * @param packetBatch
	 * @return
	 * @throws Exception
	 */
	public MinorControlFileInfo getMinorControlFileInfo(long ecomm) {

		log.info("START getMinorControlFileInfo");

		MinorControlFileInfo micfInfo = new MinorControlFileInfo();
		try {

			Statement stmt = conn.createStatement();
			String query = "SELECT "
					+ "replace(b.CLIENT_NAME,'&', '&amp;'), b.CLI_CODE, "
					+ "c.FIRST_NAME, c.LAST_NAME, c.MIDDLE_NAME, "
					+ "REPLACE(fm.MEMBER_FIRST_NAME, '&', '&amp;'),fm.MEMBER_MIDDLE_NAME,fm.MEMBER_LAST_NAME,"
					+ "fm.MEMBER_RELATIONSHIP,"
					+ "DECODE(UPPER(fm.MEMBER_RELATIONSHIP),'CHILD',fm.MEMBER_DATE_OF_BIRTH),"
					+ "c.DATE_OF_BIRTH, c.FIRST_DATE_UNABLE_TO_PERFORM,c.FIRST_LEAVE_DATE, "
					+ "a.CREATED_DATETIME, a.TECHNICIAN_NAME, "
					+ "a.TECHNICIAN_CODE, 'XXX-XX-' || substr(c.SSN,6),c.RETURN_TO_WORK_DATE, "
					+"(select DECODE(TRIM(TO_CHAR(TRUNC(sysdate)+15,'Day')), "
                    +   " 'Saturday', TRUNC(Sysdate+15)+2, "
                    +    "'Sunday', TRUNC(Sysdate+15)+1, "
                    +    "TRUNC(Sysdate)+15) from dual), "
                    +	"c.INTAKE_NUMBER, c.FORM_NAME, "  
                    +	"CASE WHEN   TRIM(a.claim_number) IS NULL  THEN ' ' "
                    +	"WHEN  (NVL(TRIM(b.PACS_CLIENT),'N') = 'Y' AND  TRIM(a.claim_number) IS NOT NULL) "
                    +	"THEN   NVL((select claimnumber "
                    +	"from pacs_claim where businessobjectnum = a.claim_number),' ') "
                    +	"ELSE  TO_CHAR(a.claim_number)       END AS  claim_number,  "
                    +	"( Select TRIM(PROVIDER_FIRST_NAME)||' '||PROVIDER_LAST_NAME " 
                    +   "from VW_INTAKE_DOCTORS_PHONE_FAX  where Intake_number = c.intake_number " 
                    +	 "and rownum = 1) Phy_name"
					+ " FROM " + "WD_ECOMM_HEADER a, " + "EC_CLIENT b, "
					+ "EF_INTAKE_HEADER c " + ",EF_FMLA_FAMILY_MEMBER fm "
					+ " WHERE " + " a.INTAKE_NUMBER = c.INTAKE_NUMBER "
					+ " AND a.CLI_CODE = b.CLI_CODE "
					+ " AND c.INTAKE_NUMBER = fm.INTAKE_NUMBER(+)"
					+ " AND a.ECOMM_NUMBER =" + ecomm;

			log.info(query);

			ResultSet rs = stmt.executeQuery(query);

			String technicianCode = null;
			if (null != rs) {
				// successfully executed
				while (rs.next()) {
					if (null != rs.getString(1))
						micfInfo.setClientName(rs.getString(1));
					if (null != rs.getString(2))
						micfInfo.setClientCode(Integer.toString(rs.getInt(2)));
					if (null != rs.getString(3))
						micfInfo.setClaimantFirstName(rs.getString(3));
					if (null != rs.getString(4))
						micfInfo.setClaimantLastName(rs.getString(4));
					if (null != rs.getString(5))
						micfInfo.setClaimantMiddleName(rs.getString(5));

					if (null != rs.getString(6))
						micfInfo.setMemberFirstName(rs.getString(6));
					if (null != rs.getString(7))
						micfInfo.setMemberMiddleName(rs.getString(7));
					if (null != rs.getString(8))
						micfInfo.setMemberLastName(rs.getString(8));
					if (null != rs.getString(9))
						micfInfo.setMemberRelationship(rs.getString(9));
					if (null != rs.getDate(10))
						micfInfo.setMemberDateOfBirth(StringUtil.formatDate(rs
								.getDate(10)));

					if (null != rs.getDate(11)) {
						java.sql.Date sqlDate = (java.sql.Date) rs.getDate(11);
						java.util.Date date = new java.util.Date(sqlDate
								.getTime());
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						int year = calendar.get(Calendar.YEAR);
						int month = calendar.get(Calendar.MONTH);
						String processedMonth = formatMonthValueForDisplay(month);
						String intakeDOB = processedMonth + "-"
								+ calendar.get(Calendar.DATE) + "-" + year;
						micfInfo.setIntakeDOB(intakeDOB);
					}
					if (null != rs.getDate(12))
						micfInfo.setIntakeFirstDayUnableToWork(StringUtil.formatDate(rs.getDate(12)));
					//new value for above merge if used for LOA
					if (null != rs.getDate(13) && null == rs.getDate(12))
						micfInfo.setIntakeFirstDayUnableToWork(StringUtil.formatDate(rs.getDate(13)));						
					if (null != rs.getDate(14))
						micfInfo.setMatrixEventCreateDateTime(StringUtil.formatDate(rs.getDate(14)));
					if (null != rs.getString(15))
						micfInfo.setTechnicianName(rs.getString(15));
					technicianCode = rs.getString(16);
					// TODO
					micfInfo.setClaimantSSN(rs.getString(17));
				//Two newer variables from DB
					micfInfo.setRTWDateTime(StringUtil.formatDateU(rs.getDate(18)));
					micfInfo.setClaimantCurrDt15(StringUtil.formatDate(rs.getDate(19)));

				//AtoFax Cert requirements
				    micfInfo.setIntakeNumber(Integer.toString(rs.getInt(20)));
					if (null != rs.getString(21))
				    micfInfo.setFormName((String)IntakeFormConstants.getFormDescriptionsHashtable().get(rs.getString(21)));
					if (null != rs.getString(22))
					micfInfo.setClaimNumber(rs.getString(22));
					if (null != rs.getString(23))					
		            micfInfo.setPhyName(rs.getString(23));					
				
				}

			}
			rs.close();

			query = "SELECT "
					+ "a.office_address_1, a.office_address_2, "
					+ "a.phone, a.phone_800, a.fax, "
					+ "b.tech_fname, b.tech_lname, b.tech_email_id, "
					+ "a.office_city, a.office_state, a.office_zip "
					+ " FROM "
					+ " ec_matrix_offices a, dis_technician b, wd_ecomm_header d "
					+ " WHERE " + "    a.LOCATION = b.SITE "
					+ " AND d.TECHNICIAN_CODE = b.TECH_CODE "
					+ " AND b.TECH_CODE =" + technicianCode
					+ " AND d.ecomm_number > 2000000 " + " AND d.ecomm_number="
					+ ecomm;

			log.info(query);

			// "THE TECHNICAL/MATRIX-Address QUERY EXECUTED IS -> "
			rs = stmt.executeQuery(query);
			if (null != rs) {
				boolean foundData = false;

				while (rs.next()) {

					foundData = true;

					if (null != rs.getString(1)) {
						micfInfo.setMatrixAddress1(rs.getString(1));
						micfInfo.setReturnAddress1(rs.getString(1));
					}

					if (null != rs.getString(2)) {
						micfInfo.setMatrixAddress2(rs.getString(2));
						micfInfo.setReturnAddress2(rs.getString(1));
					}

					if (null != rs.getString(3)) {
						micfInfo.setMatrixPhoneNumber(rs.getString(3));
					}

					if (null != rs.getString(4)) {
						micfInfo.setMatrixTollFree(rs.getString(4));
					}

					if (null != rs.getString(5)) {
						micfInfo.setMatrixFaxNumber(rs.getString(5));
					}

					String technicianName = EMPTY;

					if (null != rs.getString(6)) {
						technicianName = technicianName + rs.getString(6) + " ";
					}

					if (null != rs.getString(7)) {
						technicianName = technicianName + rs.getString(7);
					}

					micfInfo.setTechnicianName(technicianName);

					if (null != rs.getString(8)) {
						micfInfo.setTechnicianMail(rs.getString(8));
					}

					if (null != rs.getString(9)) {
						micfInfo.setMatrixCity(rs.getString(9));
					}

					if (null != rs.getString(10)) {
						micfInfo.setMatrixState(rs.getString(10));
						micfInfo.setReturnState(rs.getString(10));
					}

					if (null != rs.getString(11)) {
						micfInfo.setMatrixZip(rs.getString(11));
						micfInfo.setReturnZip(rs.getString(11));
					}

				}// while
				rs.close();

				if (!foundData) {
					String logStr = "Could not find Technician/Matrix-Office Info for "
							+ "eComm="
							+ ecomm
							+ " technicianCode="
							+ technicianCode;
					log.error(logStr);
					MailUtil.sendMail(ecomm, "Technician", logStr);
				}
			}

			// now check for the mailing address flag to determine whether the
			// address
			// is H or M
			// if H use home address, if M use mailing address
			// this is handled in the query
			query = "SELECT decode(a.mailing_address_flag, 'H', REPLACE (a.home_address_1, '&', '&amp;') ,"
					+ "                               'E', REPLACE (a.home_address_1, '&', '&amp;'),"
					+ "                               'O', REPLACE (a.home_address_1, '&', '&amp;'),"
					+ "                               'M', REPLACE (a.mailing_Address_1, '&', '&amp;'),"
					+ "                               'I', REPLACE (A.INTERNATIONAL_ADDRESS, '&', '&amp;')),"
					+ " decode(a.mailing_address_flag, 'H', REPLACE (a.home_address_2, '&', '&amp;'),"
					+ "                               'E', REPLACE (a.home_address_2, '&', '&amp;'),"
					+ "                               'O', REPLACE (a.home_address_2, '&', '&amp;'),"
					+ "                               'M', REPLACE (a.mailing_Address_2, '&', '&amp;'),"
					+ "                               'I', ''),"
					+ " decode(a.mailing_address_flag, 'H', a.home_city,"
					+ "                               'E', a.home_city,"
					+ "                               'O', a.home_city,"
					+ "                               'M', a.mailing_city,"
					+ "                               'I', ''),"
					+ " decode(a.mailing_address_flag, 'H', a.home_state,"
					+ "                               'E', a.home_state,"
					+ "                               'O', a.home_state,"
					+ "                               'M', a.mailing_state,"
					+ "                               'I', ''),"
					+ " decode(a.mailing_address_flag, 'H', a.home_zip,"
					+ "                               'E', a.home_zip,"
					+ "                               'O', a.home_zip,"
					+ "                               'M', a.mailing_zip,"
					+ "                               'I', ''),"
					+ " decode(CS.COUNTRY_CODE, 'USA',NULL,'CAN','CANADA', decode(a.mailing_address_flag, 'I', A.INTERNATIONAL_COUNTRY) )"
					+ " FROM efiling.EF_INTAKE_HEADER a, workdesk.WD_ECOMM_HEADER b, ECONFIG.EC_COUNTRY_STATE_DETAILS cs"
					+ " WHERE b.intake_number = a.intake_number AND b.ecomm_number= "
					+ ecomm
					+ " AND cs.state_code(+) = decode(a.mailing_address_flag,"
					+ "                           'H', a.home_state,"
					+ "                           'E', a.home_state,"
					+ "                           'O', a.home_state,"
					+ "                           'M', a.mailing_state,"
					+ "                           'I', NULL ) ORDER BY a.intake_number desc";

			log.debug(query);

			rs = stmt.executeQuery(query);
			if (null != rs) {
				// successfully executed
				while (rs.next()) {
					if (null != rs.getString(1))
						micfInfo.setIntakeHomeAddress1(rs.getString(1));
					if (null != rs.getString(2))
						micfInfo.setIntakeHomeAddress2(rs.getString(2));
					if (null != rs.getString(3))
						micfInfo.setIntakeHomeCity(rs.getString(3));
					if (null != rs.getString(4))
						micfInfo.setIntakeHomeState(rs.getString(4));
					if (null != rs.getString(5))
						micfInfo.setIntakeHomeZip(rs.getString(5));

					if (null != rs.getString(6)) {
						micfInfo.setIntakeHomeCountry(rs.getString(6));
						micfInfo.setMatrixCountry(PacketConstants.COUNTRY.USA
								.toString());
					} else {
						micfInfo.setIntakeHomeCountry(EMPTY);
						micfInfo.setMatrixCountry(EMPTY);
					}
				}

			}

			// close all
			rs.close();
			stmt.close();

		} catch (Exception e) {
			log.error("DBUtil.getMinorControlFileInfo ", e);
		}
		log.info("END getMinorControlFileInfo");
		return micfInfo;
	}

	public String formatMonthValueForDisplay(int theDefaultMonthValue) {
		String theMonthValueToReturn = EMPTY;
		theDefaultMonthValue = theDefaultMonthValue + 1;
		if (theDefaultMonthValue < 10)
			theMonthValueToReturn = "0" + theDefaultMonthValue;
		else
			theMonthValueToReturn = new Integer(theDefaultMonthValue)
					.toString();

		return theMonthValueToReturn;
	}

	public boolean updateWDPacketDetails(String status, long ecomm,
			int packetBatch) {
		try {
			Statement stmnt = conn.createStatement();
			String sql = "UPDATE WD_ECOMM_PACKET_DETAILS SET ECOMM_PACKET_STATUS='"
					+ status
					+ "', UPDATED_BY='PACKET', UPDATED_DATE=sysdate "
					+ " WHERE PACKET_NUMBER="
					+ packetBatch
					+ " AND ECOMM_NUMBER=" + ecomm;

			int result = stmnt.executeUpdate(sql);
			stmnt.close();
			conn.commit();

			if (result != 1) {
				log.error("Update WD_ECOMM_PACKET_DETAILS  ecomm=" + ecomm
						+ " packetBatch=" + packetBatch);
				return false;
			} else {
				log
						.info("STATUS : DBUtil.updateWDPacketDetails() ROW UPDATED SUCCESSFULLY");
			}

		} catch (Exception e) {
			log.error("DBUtil.updateWDPacketDetails() ", e);
		}
		return true;
	}

	public boolean updateDocFileName(List<DocumentInfo> docList, long ecomm) {


		if (null == docList || docList.isEmpty()) {
			log.warn("No documents to update. docList is null or empty");
			return false;
		}
		Iterator<DocumentInfo> docIter = docList.iterator();
		Statement stmnt = null;
		try {
			while (docIter.hasNext()) {
				DocumentInfo docInfo = docIter.next();
				String fileName = docInfo.getFileName();
				if (null != fileName && !EMPTY.equals(fileName)
						&& fileName.length() > 0) {
					String sql = " UPDATE WD_ECOMM_DOCUMENTS "
							+ " SET DOCUMENT_OUTPUT_LOCATION='" + fileName
							+ "', UPDATED_BY='PACKET', UPDATED_DATE=sysdate "
							+ " WHERE ECOMM_NUMBER=" + ecomm
							+ " AND DOCUMENT_ID='" + docInfo.getDocumentId()
							+ "'";

					stmnt = conn.createStatement();
					int result = stmnt.executeUpdate(sql);
					if (result != 1) {
						log.error("Update WD_ECOMM_DOCUMENTS Ecomm=" + ecomm
								+ " DOCUMENT_ID=" + docInfo.getDocumentId()
								+ " result=" + result);
						return false;
					} else {// Success.
						if (log.isDebugEnabled()) {
							log.debug("Update WD_ECOMM_DOCUMENTS Ecomm="
									+ ecomm + " DOCUMENT_ID="
									+ docInfo.getDocumentId() + " FileName="
									+ fileName + " result=" + result);
						}
					}
				}
			}// while
			conn.commit();

		} catch (Exception e) {
			log.error("DBUtil.updateDocLocation() for eComm=" + ecomm, e);
			return false;
		} finally {
			closeStatement(stmnt);
		}
		return true;
	}

	public void updateWDEcommHeader(String status, long ecomm) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = "UPDATE WD_ECOMM_HEADER SET ECOMM_STATUS='" + status
					+ "', UPDATED_BY='PACKET', UPDATED_DATE=sysdate "
					+ " WHERE ECOMM_NUMBER=" + ecomm;
			int result = stmt.executeUpdate(query);
			// technically there should be only one returned
			// meaning at any point of time there can be only one packet number
			// in open status
			if (result != 0) {
				// successfully executed
				log
						.info("STATUS : DBUtil.updateWDEcommHeader updated status to '"
								+ status + "' for eComm " + ecomm);
			}

			conn.commit();
		} catch (Exception e) {
			log.error("updateWDEcommHeader for eComm=" + ecomm, e);
		} finally {
			closeStatement(stmt);
		}
	}// updateWDEcommHeader

	private void closeResultSet(ResultSet rs) {
		try {
			if (null != rs)
				rs.close();
		} catch (SQLException e) {
			log.error("Excpetion while closing resulset", e);
		}
	}

	private void closeStatement(Statement stmnt) {
		try {
			if (null != stmnt)
				stmnt.close();
		} catch (SQLException e) {
			log.error("Excpetion while closing statement", e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Main.initLog("log4j.xml");
		PacketsBatchGeneration.loadProperties();
		DBUtil dbUtil = new DBUtil(PacketsBatchGeneration.getProps());
		// long eComm = 730501L;
		// eComm = 5023385L;

		int packetBatch = dbUtil.isValidPacketNumberAvailable();
		log
				.info("Valid Packet Number "
						+ dbUtil.isValidPacketNumberAvailable());

		List<EcommVO> eCommList = dbUtil.findNewEcomms(packetBatch);
		log.info(" NewEcommsList size = " + eCommList.size());

		Iterator<EcommVO> eCommIter = eCommList.iterator();

		EcommVO evo = null;

		while (eCommIter.hasNext()) {
			evo = eCommIter.next();
			log.info("ECOMM " + evo.getEcomm() + " Intake" + evo.getIntake()
					+ " IntakeYearMonthPath" + evo.getIntakeYearMonthPath());
		}

		// MinorControlFileInfo micfInfo =
		dbUtil.getMinorControlFileInfo(evo.getEcomm());
		// log.info(micfInfo.getClientName());
		// log.info(micfInfo.getMemberFirstName());
		// log.info(micfInfo.getClaimantFirstName());
		// log.info(micfInfo.getTechnicianName());
		// log.info(micfInfo.getMatrixAddress1());
		// log.info(micfInfo.getMatrixAddress2());

		List<DocumentInfo> docList = dbUtil.findDocs(evo.getEcomm());
		for (DocumentInfo docInfo : docList) {
			docInfo.setFileName("TestTestTestTestTestTestTestTest");
		}
		dbUtil.getMajorControlFileInfo(evo.getEcomm());
		// dbUtil.updateDocFileName(docList, evo.getEcomm());
	}// main

}// class
