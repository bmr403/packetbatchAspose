package com.matrix.intake.ecomm.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;

import org.apache.log4j.Logger;

import com.matrixcos.conditionaldisplay.DBConnection;
import com.matrixcos.conditionaldisplay.FOConditionConfig;
import com.matrixcos.conditionaldisplay.FOParser;
import com.matrixcos.conditionaldisplay.AsposeWordToPDF;

public class MainForTest {

	final static Logger logger = Logger.getLogger(MainForTest.class);

	public static void main(String[] args) {
		
		
		/*DB Details-----------------------------------------
		// jdbc:oracle:thin:@localhost:1521:xe
		//String dbURL = "jdbc:oracle:thin:@es-stgdb1.eservices.local:1522:ESSTG";
		 */
		String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=matabsuat-scan.matrix.local)(PORT=1521))(CONNECT_DATA=(SERVER=dedicated)(SERVICE_NAME=ESUAT.es-uat.eservices.local)))";
		String userName = "ECONFIG";
		String password = "ECONFIG";
		//-------------------------------------------------------
		
		//------ File Path --------------------------------------
		
		
		String inputFile ="D://Packet Project//Matrix_Aspose//Files//HDPacket_N12.docx";
		String OutputDocFile = "D://Packet Project//Matrix_Aspose//Files//HDPacket_N12_out.docx";
		String pdfFilePath = "D://Packet Project//Matrix_Aspose//Files//HDPacket_N12.pdf";
		//------------------------------------------------------
		
		long eCommnumber=10396514;
		
		
		
		Writer out = null;
		long intakeNumber = 5063731;
		String filePath = "C:\\Users\\Manjunath\\Desktop\\HDIntroPacketGeneric.fo";
		Connection connection = null;
		long clientCode = 648; // setting statically for testing conditions.
		AsposeWordToPDF WORDoBJ= new AsposeWordToPDF();
		
		
		try {
			
			// Get database connection object
			connection = DBConnection.connection(dbURL, userName, password);
//			WORDoBJ.wordToPDF(inputFile, OutputDocFile, pdfFilePath);
//			WORDoBJ.getAttributes(eCommnumber,connection);
//			
				}
		catch (Exception e) {
			System.out.println("Expetion");
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				
			} catch (Exception e) {
				System.out.println("Expetion");
				e.printStackTrace();
			}

		} 
		
		
		
		
		
		
	}


}
