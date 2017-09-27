package com.matrixcos.conditionaldisplay;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;

import org.apache.log4j.Logger;

public class FoJavaUtil {

	final static Logger logger = Logger.getLogger(FoJavaUtil.class);
	
	public String processFO(String inputFoFileString, long intakeNumber, long clientCode, String documentId, String dbUrl, String dbUser, String dbPassword)
	{
		String processedFoString = "";
		Connection connection = null;
		Writer out = null;
		try {
			
			// Get database connection object
			connection = DBConnection.connection(dbUrl, dbUser, dbPassword);
			if (FOConditionConfig.checkFOConditionConfig(clientCode,intakeNumber, inputFoFileString, connection)) {
				logger.info("FO Condition Config Enable is TRUE & connection is Fine proceeding with evaluation of conditional code");
				processedFoString = FOParser.fo_ConditionBuilder(inputFoFileString,intakeNumber,clientCode,documentId, connection);
			} else {
				logger.error("FO Condition Config Enable is FALSE or database connection is FAILED. Skiping the process ");
				processedFoString = inputFoFileString;
				// When database connection fails or FO Conditional display flag is FALSE.
				// File content won't changes so ROLLBACK input data.
			}
			//logger.info(processedFoString);
			

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception occured returning the original fo string to generate PDF file");
			processedFoString = inputFoFileString;
		} finally {
			try {
				connection.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception occured returning the original fo string to generate PDF file");
				processedFoString = inputFoFileString;
			}

		}
		
		
		return processedFoString;
	}
	
	private static String generateStringFromFOFile(String filePath) {

		StringBuffer fileStringBuf = new StringBuffer();
		BufferedReader br = null;
		try {
			String line = null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					filePath), "UTF-8"));

			while ((line = br.readLine()) != null) {
				fileStringBuf.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return fileStringBuf.toString();
	}
	
}
