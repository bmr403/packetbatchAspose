package com.matrixcos.conditionaldisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

public class FOConditionConfig {

	final static Logger logger = Logger.getLogger(FOConditionConfig.class);

	public static boolean checkFOConditionConfig(long clientCode,
			long intakeNumber, String fileContent, Connection connection) {
		ResultSet condition_Config_ResultSet = null;
		String query = null;
		boolean conditionDisplayFlag = false;
		if (connection != null ? true : false) {
			try {
				PreparedStatement ps_query = null;
				logger.info("In side checkFOConditionConfig Method & connection Success");
				if (new Long(clientCode) != null ? true : false) {
					query = "select ENABLE_CONDITION_DISPLAY_FLAG from EC_PKT_FO_CONDITION_CONFIG where CLI_CODE = '"+ clientCode + "' ";
					ps_query = connection.prepareStatement(query);
					condition_Config_ResultSet = ps_query.executeQuery();
					while (condition_Config_ResultSet.next()) {
						conditionDisplayFlag = condition_Config_ResultSet.getString("ENABLE_CONDITION_DISPLAY_FLAG").equalsIgnoreCase("Y") ? true : false;
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage()+ " got exception in checkFOConditionConfig method ");
				ex.printStackTrace();
			} finally {
				try {
					// con.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return conditionDisplayFlag;
	}

}
