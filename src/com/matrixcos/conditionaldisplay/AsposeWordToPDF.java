package com.matrixcos.conditionaldisplay;

import java.nio.charset.Charset;
import java.util.HashMap;


import com.aspose.words.Document;
import com.aspose.words.FileFormatInfo;
import com.aspose.words.FileFormatUtil;
import com.aspose.words.MailMergeCleanupOptions;
import com.aspose.words.PdfSaveOptions;


import org.apache.log4j.Logger;

import com.matrix.intake.ecomm.packet.objects.MinorControlFileInfo;

public class AsposeWordToPDF {

	private static Logger log = Logger.getLogger(AsposeWordToPDF.class);

	private HashMap<String, Object> getMergeAttributes(MinorControlFileInfo micfInfo, String getFormattedCurrentDate,
			String getCurrentDate) throws Exception {

		try {

			HashMap<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("INTAKE__LASTNAME", micfInfo.getClaimantLastName());
			attrMap.put("INTAKE__HOMEADDRESS1", micfInfo.getIntakeHomeAddress1());
			attrMap.put("INTAKE__HOMEADDRESS2", micfInfo.getIntakeHomeAddress2());
			attrMap.put("INTAKE__HOMECITY", micfInfo.getIntakeHomeCity());
			attrMap.put("INTAKE__HOMESTATE", micfInfo.getIntakeHomeState());
			attrMap.put("INTAKE__HOMEZIP", micfInfo.getIntakeHomeZip());
			attrMap.put("INTAKE__COUNTRY", micfInfo.getIntakeHomeCountry());

			attrMap.put("CLAIMANT_FNAME", micfInfo.getClaimantFirstName());
			attrMap.put("CLAIMANT_MNAME", micfInfo.getClaimantMiddleName());
			attrMap.put("CLAIMANT_LNAME", micfInfo.getClaimantLastName());
			attrMap.put("SSN", micfInfo.getClaimantSSN());
			attrMap.put("INTAKE__DOB", micfInfo.getIntakeDOB());

			attrMap.put("MATRIX_ADDRESS1", micfInfo.getMatrixAddress1());
			attrMap.put("MATRIX_ADDRESS2", micfInfo.getMatrixAddress2());
			attrMap.put("MATRIX_CITY", micfInfo.getMatrixCity());
			attrMap.put("MATRIX_STATE", micfInfo.getMatrixState());
			attrMap.put("MATRIX_ZIP", micfInfo.getMatrixZip());
			attrMap.put("MATRIX_COUNTRY", micfInfo.getMatrixCountry());
			attrMap.put("MATRIX_FAXNUMBER", micfInfo.getMatrixFaxNumber());
			attrMap.put("MATRIX_PHONENUMBER", micfInfo.getMatrixPhoneNumber());
			attrMap.put("MATRIX_TOLLFREE", micfInfo.getMatrixTollFree());
			attrMap.put("EVENT_CREATE_DATETIME", micfInfo.getMatrixEventCreateDateTime());
			attrMap.put("INTAKE__FIRSTDAYUNABLETOWORK", micfInfo.getIntakeFirstDayUnableToWork());
			attrMap.put("SCHEDULED_RTW_DATE", micfInfo.getRTWDateTime());
			attrMap.put("DOCS_DUE_DATE_15", micfInfo.getClaimantCurrDt15());
			attrMap.put("TECHNICIAN_EMAIL", micfInfo.getTechnicianMail());
			attrMap.put("TECHNICIAN_NAME", micfInfo.getTechnicianName());
			attrMap.put("RETURN_ADDRESS1", micfInfo.getIntakeHomeAddress1());
			attrMap.put("RETURN_ADDRESS2", micfInfo.getIntakeHomeAddress2());
			attrMap.put("RETURN_STATE", micfInfo.getIntakeHomeState());
			attrMap.put("RETURN_ZIP", micfInfo.getIntakeHomeZip());
			attrMap.put("CLIENT_NAME", micfInfo.getClientName());
			attrMap.put("CURRENT_DATE", getFormattedCurrentDate);
			attrMap.put("«CURRENT_DATE_F1»", getCurrentDate);

			// Following five are required for the new LOA packets.
			attrMap.put("MemberFirstName", micfInfo.getMemberFirstName());
			attrMap.put("MemberMiddleName", micfInfo.getMemberMiddleName());
			attrMap.put("MemberLastName", micfInfo.getMemberLastName());
			attrMap.put("MemberRelationship", micfInfo.getMemberRelationship());
			attrMap.put("MemberDateOfBirth", micfInfo.getMemberDateOfBirth());

			// For AutoFax pattern cert requirements
			attrMap.put("PHYSICIAN_NAME", micfInfo.getPhyName());
			attrMap.put("INTAKE_NUMBER", micfInfo.getIntakeNumber());
			attrMap.put("CLAIM_NUMBER", micfInfo.getClaimNumber());
			attrMap.put("FORM_NAME", micfInfo.getFormName());
			String s3 = micfInfo.getClientCode() + "-" + micfInfo.getIntakeNumber();
			attrMap.put("CLI-INTAKE", s3);

			return attrMap;
		} catch (Exception e1) {
			
			log.error("Error in the Getting the Merge Variables in \"AsposeWordToPDF\" Class ...");
			log.error(e1.getMessage());
			throw (e1);
		}

	}

	public void wordToPDF(String inputFile, String OutputDocFile, String pdfFilePath, MinorControlFileInfo micfInfo,
			String getFormattedCurrentDate, String getCurrentDate) throws Exception {

		try {

			String[] keySet;
			Object[] keyValue;
			HashMap<String, Object> attrMap = new HashMap<String, Object>();
			attrMap = this.getMergeAttributes(micfInfo, getFormattedCurrentDate, getCurrentDate);

			keySet = attrMap.keySet().toArray(new String[attrMap.size()]);
			;
			keyValue = attrMap.values().toArray();

			Document doc;
			doc = new Document(inputFile);
			doc.getMailMerge().setCleanupOptions(MailMergeCleanupOptions.REMOVE_UNUSED_FIELDS
					| MailMergeCleanupOptions.REMOVE_EMPTY_PARAGRAPHS | MailMergeCleanupOptions.REMOVE_CONTAINING_FIELDS
					| MailMergeCleanupOptions.REMOVE_UNUSED_REGIONS);

			doc.getMailMerge().execute(keySet, keyValue);

			doc.save(OutputDocFile);
			
//			FileFormatInfo info = FileFormatUtil.detectFileFormat(inputFile);
			
//			Charset encode =info.getEncoding();
//			System.out.println("The encoding type of the document is ..........."+encode.toString());
//			log.info(encode);

			// Load the document to render.
			Document output_Doc = new Document(OutputDocFile);
			// EmptyParagraphRemover remover = new EmptyParagraphRemover();
			// output_Doc.accept(remover);

			PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
			pdfSaveOptions.setEmbedFullFonts(true);
			output_Doc.save(pdfFilePath, pdfSaveOptions);

			log.info("@##############Completed the conversion of WORD document to PDF using ASPOSE ...###################@");

		} catch (Exception e1) {
			// TODO Auto-generated catch block

			log.error("Error in the conversion of WORD document to PDF using ASPOSE ...");
			log.error(e1.getMessage());
			throw (e1);
		}

	}

}
