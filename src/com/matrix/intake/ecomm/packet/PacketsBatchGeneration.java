/*
 * ProcessIntake.java
 *
 * Created on September 23, 2007, 1:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.matrix.intake.ecomm.packet.objects.DocumentInfo;
import com.matrix.intake.ecomm.packet.objects.EcommVO;
import com.matrix.intake.ecomm.packet.objects.MajorControlFileInfo;
import com.matrix.intake.ecomm.packet.objects.MinorControlFileInfo;
import com.matrix.intake.ecomm.packet.util.DBUtil;
import com.matrix.intake.ecomm.packet.util.FOPUtil;
import com.matrix.intake.ecomm.packet.util.FileUtil;
import com.matrix.intake.ecomm.packet.util.MailUtil;
import com.matrixcos.conditionaldisplay.AsposeWordToPDF;

/**
 * Formerly the file name was ProcessIntake.java
 * 
 * @author Venkat, Kalyan Bobba(changes)
 */
public class PacketsBatchGeneration {

	private static final String EMPTY = "";

	private static final Logger log = Logger.getLogger(PacketsBatchGeneration.class);

	private static String fileSep = System.getProperty("file.separator");

	private DBUtil dbUtil;

	private FileUtil fileUtil;

	private FOPUtil foUtil;

	// private ResourceUtils resourceUtil;
	private static Properties props;

	private final String baseDir;

	private final int checkListSize;

	private int packetBatch;

	private BufferedWriter majorControlFileWriter = null;

	private String minorControlFileText = EMPTY;

	private MinorControlFileInfo micfInfo = null;

	private final Set<String> knownAttrSet;

	/** Creates a new instance of ProcessIntake */
	public PacketsBatchGeneration() {
		loadProperties();
		dbUtil = new DBUtil(props);
		fileUtil = new FileUtil();
		foUtil = new FOPUtil();

		baseDir = props.getProperty("packetgeneration.path");
		checkListSize = Integer.valueOf(props.getProperty("checklist.size"));

		knownAttrSet = new TreeSet<String>(new Comparator<String>() {

			public int compare(String s1, String s2) {
				if (s1 == null)
					return -1;
				if (s2 == null)
					return 1;
				return s1.compareToIgnoreCase(s2);
			}
		});
		initializeKnownAttrSet();
	}

	public static void loadProperties() {
		props = new Properties();
		try {
			String propertyFile = "application.properties";
			props.load(new FileInputStream(propertyFile));
		} catch (FileNotFoundException e) {
			log.error("loadProperties()", e);
		} catch (IOException e) {
			log.error("loadProperties()", e);
		}
	}// loadProperties

	public static Properties getProps() {
		return props;
	}

	/*
	 * Note: When you add new attributes, also handle the attributes such that
	 * you replace them with values in replaceAttrDesc() method
	 */
	private void initializeKnownAttrSet() {
		knownAttrSet.add("�INTAKE__FIRSTNAME�");
		knownAttrSet.add("�INTAKE__LASTNAME�");
		knownAttrSet.add("�INTAKE__HOMEADDRESS1�");
		knownAttrSet.add("�INTAKE__HOMEADDRESS2�");
		knownAttrSet.add("�INTAKE__HOMECITY�");
		knownAttrSet.add("�INTAKE__HOMESTATE�");
		knownAttrSet.add("�INTAKE__HOMEZIP�");
		knownAttrSet.add("�INTAKE__COUNTRY�");

		knownAttrSet.add("�CLAIMANT_FNAME�");
		knownAttrSet.add("�CLAIMANT_MNAME�");
		knownAttrSet.add("�CLAIMANT_LNAME�");
		// TODO
		knownAttrSet.add("�SSN�");
		knownAttrSet.add("�INTAKE__DOB�");

		knownAttrSet.add("�MATRIX_ADDRESS1�");
		knownAttrSet.add("�MATRIX_ADDRESS2�");
		knownAttrSet.add("�MATRIX_CITY�");
		knownAttrSet.add("�MATRIX_STATE�");
		knownAttrSet.add("�MATRIX_ZIP�");
		knownAttrSet.add("�MATRIX_COUNTRY�");

		knownAttrSet.add("�MATRIX_FAXNUMBER�");
		knownAttrSet.add("�MATRIX_PHONENUMBER�");
		knownAttrSet.add("�MATRIX_TOLLFREE�");

		knownAttrSet.add("�EVENT_CREATE_DATETIME�");
		knownAttrSet.add("�INTAKE__FIRSTDAYUNABLETOWORK�");
		knownAttrSet.add("�SCHEDULED_RTW_DATE�"); // 4/30/2015
		knownAttrSet.add("�DOCS_DUE_DATE_15�"); // 4/30/2015
		knownAttrSet.add("�TECHNICIAN_EMAIL�");
		knownAttrSet.add("�TECHNICIAN_NAME�");

		knownAttrSet.add("�RETURN_ADDRESS1�");
		knownAttrSet.add("�RETURN_ADDRESS2�");
		knownAttrSet.add("�RETURN_STATE�");
		knownAttrSet.add("�RETURN_ZIP�");
		knownAttrSet.add("�CLIENT_NAME�");

		knownAttrSet.add("�CURRENT_DATE�");
		knownAttrSet.add("�CURRENT_DATE_F1�");// 9/14/2009

		knownAttrSet.add("�MemberFirstName�");
		knownAttrSet.add("�MemberMiddleName�");
		knownAttrSet.add("�MemberLastName�");
		knownAttrSet.add("�MemberRelationship�");
		knownAttrSet.add("�MemberDateOfBirth�");

		knownAttrSet.add("�CLAIM_NUMBER�"); // 22.8.2015 -5 variables
		knownAttrSet.add("�INTAKE_NUMBER�");
		knownAttrSet.add("�FORM_NAME�");
		knownAttrSet.add("�CLI-INTAKE�");
		knownAttrSet.add("�PHYSICIAN_NAME�");

		for (int c = 1; c <= checkListSize; c++) {
			knownAttrSet.add("�document_" + c + "�");
		}
	}

	/**
	 * This method processes the packet details Finds all the Ecomm numbers that
	 * are new
	 * 
	 * @throws Exception
	 * 
	 */
	public void processPacketDetails() {

		packetBatch = dbUtil.isValidPacketNumberAvailable();

		log.info("THE RUN PACKET BATCH IS -> " + packetBatch);

		List<EcommVO> eCommList = dbUtil.findNewEcomms(packetBatch);

		log.info(" NewEcommsList size = " + eCommList.size());

		Iterator<EcommVO> eCommIter = eCommList.iterator();

		while (eCommIter.hasNext()) {

			EcommVO evo = eCommIter.next();

			log.info("START PROCESS ECOMM " + evo.getEcomm());

			micfInfo = dbUtil.getMinorControlFileInfo(evo.getEcomm());

			List<DocumentInfo> docList = dbUtil.findDocs(evo.getEcomm());

			processDocList(docList, evo);

			log.info("END PROCESS ECOMM " + evo.getEcomm());
		} // while
			// close all the open writers
		closeTheWriters();
	}// processPacketDetails

	private void processDocList(List<DocumentInfo> docList, EcommVO evo) {
		long ecomm = evo.getEcomm();

		minorControlFileText = EMPTY;

		boolean isAllDocumentsProcessedSuccessFully = true;
		String packetGenErr = EMPTY;
		String mailSubject = EMPTY;
		DocumentInfo docInfo = null;
		int docCounter = 0;
		int pullDocCounter = 0;

		if (null == docList || docList.size() == 0) {
			isAllDocumentsProcessedSuccessFully = false;
			// send email to the IT team
			log.error("Documents are not configured for this ecomm=" + ecomm);
			dbUtil.updateWDPacketDetails("E", ecomm, packetBatch);
			mailSubject = EMPTY;
			packetGenErr = "There are no documents configured for this Packet Ecomm.";
			packetGenErr = packetGenErr + " The ecomm will be marked as 'CANCELLED'. Cli_Code=" + evo.getCliCode();
			dbUtil.updateWDEcommHeader("CANCELLED", ecomm);
			MailUtil.sendMail(ecomm, mailSubject, packetGenErr);
			return;
		}

		Iterator<DocumentInfo> docIter = docList.iterator();

		log.info("STATUS : PROCESSING THE DOCUMENTS FOR ECOMM -> " + ecomm);

		String currentDocInfo = EMPTY;

		while (docIter.hasNext()) {
			docCounter++;
			docInfo = docIter.next();

			packetGenErr = EMPTY;
			mailSubject = EMPTY;

			if (null == docInfo) {
				mailSubject = "null docInfo";
				packetGenErr = " null docInfo object ";
				log.error(packetGenErr);
				isAllDocumentsProcessedSuccessFully = false;
				deleteAllCreatedDocumentsDuringException(docList, ecomm);
				currentDocInfo = EMPTY;
				break;
			}

			// if (null != docInfo) {
			currentDocInfo = docCounter + ". " + " docId=" + docInfo.getDocumentId() + " docType="
					+ docInfo.getDocumentType() + " templateName=" + docInfo.getTemplateName() + " docDesc="
					+ docInfo.getDocumentDescription() + " eCommCreatedDate=";

			log.info(EMPTY);
			log.info(currentDocInfo);
			log.info(EMPTY);

			if ("pdf".equalsIgnoreCase(docInfo.getDocumentType())) {
				// as this is a pdf document
				// copy the document from the location
				// actual pdfs are copied in a directory and the
				// location path is stored in the property file
				// get the path and append the file name, this will give
				// the actual pdf file location
				// copy the file over to the packet number directory
				// FILE NAMING CONVENTION
				// wf-<eComm_number>-<DOCUMENT_ID>.pdf

				String pdffilesPath = props.getProperty("pdffiles.path");
				// build the entire path

				// THIS IS COMMENTED FOR NOW
				String fileToBeCopied = docInfo.getTemplateName();

				// Step 2
				// String theFileToBeCopied =
				// "oracle_disability_ee_checklist.pdf";

				// build the destination file name
				String desctinationDirectory = props.getProperty("packetgeneration.path") + packetBatch
						+ props.getProperty("file.pathSeparator");
				// String destinationFileName = "wf-" + ecomm + "-" +
				// docInfo.getDocumentId() + ".pdf";

				// set the file name back
				// docInfo.setDocumentName(destinationFileName);

				// add the description to what needs to be written in to
				// the minor control file
				minorControlFileText = minorControlFileText + docInfo.getDocumentId() + " ";
				// add to what needs to be written in to the minor
				// control file
				minorControlFileText = minorControlFileText + docInfo.getFileName() + "\n";

				// log.info("PDF PDF PDF -> " +
				// thePathToActualFile + " ---- \n" +
				// theDesctinationFile);

				try {
					// copy the file
					File srcFile = new File(pdffilesPath, fileToBeCopied);
					File destFile = new File(desctinationDirectory, docInfo.getFileName());
					log.info("PDF FILE BEING COPIED IS -> " + fileToBeCopied + " TO THE FILE " + docInfo.getFileName());
					Long sourceFileChecksumVal = fileUtil.copyFile(srcFile, destFile);
					Long destinationFileChecksumVal = fileUtil.getChecksum(destFile);
					if (sourceFileChecksumVal.equals(destinationFileChecksumVal)) {
						log.info(" OK, files are equal.");
					} else {

						isAllDocumentsProcessedSuccessFully = false;

						packetGenErr = " 'pdf' docType processing templateName=" + docInfo.getTemplateName()
								+ " Checksum's differ.";
						log.error(packetGenErr);
						mailSubject = "pdf docType ";

						deleteAllCreatedDocumentsDuringException(docList, ecomm);
						break;
					}
				} catch (Exception eee) {
					packetGenErr = " 'pdf' docType processing eComm=" + ecomm + " templateName="
							+ docInfo.getTemplateName();
					mailSubject = "pdf docType ";
					log.error(packetGenErr, eee);
					packetGenErr = packetGenErr + "\n " + eee.getMessage();
					isAllDocumentsProcessedSuccessFully = false;
					deleteAllCreatedDocumentsDuringException(docList, ecomm);
					break;
				}

			} else if ("word".equalsIgnoreCase(docInfo.getDocumentType())) {
				// this is a word document
				// get the fo file associated to this
				// use the FOP util to create the pdf document in the
				// packet number directory
				// FILE NAMING CONVENTION
				// wf-<eComm_number>-<DOCUMENT_ID>.pdf

				String locationToAppend = props.getProperty("fofiles.path");

				String foFileName = docInfo.getTemplateName();

				// Step 3
				// String theFOFileToBeUsed = "a_generic_letter_ca.fo";

				// build the destination file name
				String packetBatchDir = baseDir + packetBatch + props.getProperty("file.pathSeparator");
				// String outputFileName = "wf-" + ecomm + "-" +
				// docInfo.getDocumentId() + ".pdf";

				// set the file name back
				// docInfo.setDocumentName(outputFileName);

				// add the description to what needs to be written in to
				// the minor control file
				minorControlFileText = minorControlFileText + docInfo.getDocumentId() + " ";
				// add to what needs to be written in to the minor
				// control file
				minorControlFileText = minorControlFileText + docInfo.getFileName() + "\n";

				try {
					// copy the file
					File foFile = new File(locationToAppend, foFileName);

					log.info(EMPTY);
					log.info("foFile " + foFile.getCanonicalPath());
					log.info(EMPTY);

					if (foFile != null && !foFile.exists()) {
						isAllDocumentsProcessedSuccessFully = false;
						mailSubject = "word docType ";
						packetGenErr = " foFile does not exist " + foFile;
						log.info(EMPTY);
						log.error(packetGenErr);
						log.info(EMPTY);
						break;
					}
					{
						// the fo file is found
						// now substitute values for the attrs
						String processedTempFOFileName = null;
						processedTempFOFileName = replaceMatrixAttrs(foFile, docList, evo, docInfo.getDocumentId());

						if (processedTempFOFileName == null) {
							// foFile does not exist
							// OR
							// there are few attrs in the minor control file
							// whose values are null
							// do not process this ECOMM ...start clean up
							// process for this ECOMM
							packetGenErr = " NULL ATTRIBUTE VALUES in minorControlFileInfo";
							mailSubject = "word docType ";
							log.info(EMPTY);
							log.error(packetGenErr);
							log.info(EMPTY);
							isAllDocumentsProcessedSuccessFully = false;
							deleteAllCreatedDocumentsDuringException(docList, ecomm);
							break;
						} else {
							File processedTempFOFile = new File(baseDir, processedTempFOFileName);
							// check if the FO files exists
							// if not clean up
							if (processedTempFOFile.exists()) {
								File destFile = new File(packetBatchDir, docInfo.getFileName());
								log.info("FOP FILE BEING GENERATED FROM -> " + processedTempFOFile + " TO THE FILE "
										+ destFile);
								try {
									foUtil.convertFO2PDF(processedTempFOFile, destFile);
								} catch (Exception ex) {
									isAllDocumentsProcessedSuccessFully = false;
									packetGenErr = " THE PDF GENERATION FAILED --- CALLING CLEANUP eComm=" + ecomm;
									mailSubject = "word docType ";
									log.error(packetGenErr, ex);
									packetGenErr = packetGenErr + "\n " + ex.getMessage();
									deleteAllCreatedDocumentsDuringException(docList, ecomm);
									break;
								} finally {
									processedTempFOFile.delete();
								}

								// delete the temp file
								// theProcessedFOFile.delete();
							} else {
								isAllDocumentsProcessedSuccessFully = false;
								packetGenErr = " THE processedTempFOFile DOES NOT EXIST -> " + processedTempFOFile
										+ " --- CALLING CLEANUP";
								mailSubject = "word docType ";
								log.error(packetGenErr);
								deleteAllCreatedDocumentsDuringException(docList, ecomm);
								break;
							} // else processedTempFOFile.exists()
						} // else processedTempFOFileName == null
					} // else foFile exists

				} catch (Exception eee) {
					packetGenErr = " 'word' docType processing eComm=" + ecomm;
					mailSubject = "word docType ";
					log.error(packetGenErr, eee);
					packetGenErr = packetGenErr + "\n " + eee.getMessage();
					isAllDocumentsProcessedSuccessFully = false;
					deleteAllCreatedDocumentsDuringException(docList, ecomm);
					break;
				}
				// "word" docType
			}

			///////////////////////////// #####ASPOSE#####/////////////////////////////
			else if ("docx".equalsIgnoreCase(docInfo.getDocumentType())) {

				log.info("@#############Aspose WORD #######################@");
				log.info("This File is a Word File..");
				log.info("Document ID : " + docInfo.getDocumentId());
				log.info("Template Name : " + docInfo.getTemplateName());
				log.info("Entering the Aspose Tool to Process the Word File");
				try {
					String locationToAppend = props.getProperty("docxFiles.path");
					String docxFileName = docInfo.getTemplateName();
					String inputFileName = locationToAppend + docxFileName;
					String desctinationDirectory = props.getProperty("packetgeneration.path") + packetBatch
							+ props.getProperty("file.pathSeparator") + docInfo.getFileName();

					String tempFileDirctory = props.getProperty("tempdocxFiles.path") + "tempDocFileAspose.docx";

					// add the description to what needs to be written in to
					// the minor control file
					minorControlFileText = minorControlFileText + docInfo.getDocumentId() + " ";
					// add to what needs to be written in to the minor
					// control file
					minorControlFileText = minorControlFileText + docInfo.getFileName() + "\n";

					// copy the file
					File docxFile = new File(locationToAppend, docxFileName);

					log.info(EMPTY);
					log.info("Docx File " + docxFile.getCanonicalPath());
					log.info(EMPTY);

					if (docxFile != null && !docxFile.exists()) {
						isAllDocumentsProcessedSuccessFully = false;
						mailSubject = "Docx docType ";
						packetGenErr = " docx does not exist " + docxFile;
						log.info(EMPTY);
						log.error(packetGenErr);
						log.info(EMPTY);
						break;
					}
					{
						try {
							com.matrixcos.conditionaldisplay.AsposeWordToPDF asposeWordToPDF = new AsposeWordToPDF();
							asposeWordToPDF.wordToPDF(inputFileName, tempFileDirctory, desctinationDirectory, micfInfo,
									getFormattedCurrentDate(), getCurrentDate("MMMM dd, yyyy"));

						} catch (Exception e1) {
							packetGenErr = " 'Docx (Aspose)' docType processing eComm=" + ecomm;
							mailSubject = "Aspose Failure";
							log.error(packetGenErr, e1);
							packetGenErr = packetGenErr + "\n " + e1.getMessage();
							isAllDocumentsProcessedSuccessFully = false;
							deleteAllCreatedDocumentsDuringException(docList, ecomm);
							break;
						}
					}

				}

				catch (Exception eee) {
					packetGenErr = " 'word' docType processing eComm=" + ecomm;
					mailSubject = "word docType ";
					log.error(packetGenErr, eee);
					packetGenErr = packetGenErr + "\n " + eee.getMessage();
					isAllDocumentsProcessedSuccessFully = false;
					deleteAllCreatedDocumentsDuringException(docList, ecomm);
					break;
				}
			}

			else if ("<PULL>".equalsIgnoreCase(docInfo.getDocumentType())) {
				// increment the pull document counter
				pullDocCounter++;

				// add the description to what needs to be written in to
				// the minor control file
				minorControlFileText = minorControlFileText + docInfo.getDocumentId() + " ";
				// add to what needs to be written in to the minor
				// control file
				minorControlFileText = minorControlFileText + "PULL" + "\n";
			} else if ("INTAKEFORM".equalsIgnoreCase(docInfo.getDocumentId())) {
				// this is an intake document
				// get the intake number and create date
				// create date shoud be used as
				// <intake_dir_location>\yyyyy\mm\<intake_number>.pdf

				log.debug("INTAKE intakeYearMonthPath " + evo.getIntakeYearMonthPath() + " Intake=" + evo.getIntake());

				String intakeFileLocation = props.getProperty("intakes.path") + evo.getIntakeYearMonthPath() + fileSep;

				log.info("THE ACTUAL INTAKE DIRECTORY IS -> " + intakeFileLocation);

				String fileToBeCopied = evo.getIntake() + ".pdf";
				String destinationDirectory = props.getProperty("packetgeneration.path") + packetBatch
						+ props.getProperty("file.pathSeparator");

				// add the description to what needs to be
				// written in to the minor control file
				minorControlFileText = minorControlFileText + docInfo.getDocumentId() + " ";
				// add to what needs to be written in to the
				// minor control file
				minorControlFileText = minorControlFileText + docInfo.getFileName() + "\n";

				try {
					// copy the file
					File srcFile = new File(intakeFileLocation, fileToBeCopied);
					File destFile = new File(destinationDirectory, docInfo.getFileName());

					log.info("INTAKE FILE BEING COPIED FROM -> " + fileToBeCopied + " TO THE FILE "
							+ docInfo.getFileName());

					Long sourceFileChecksumVal = fileUtil.copyFile(srcFile, destFile);
					Long destinationFileChecksumVal = fileUtil.getChecksum(destFile);

					if (sourceFileChecksumVal.equals(destinationFileChecksumVal)) {
						log.info("OK, files are equal.");
					} else {
						log.error("processDocList(). Checksums differ.");
						isAllDocumentsProcessedSuccessFully = false;
						deleteAllCreatedDocumentsDuringException(docList, ecomm);
						break;
					}

				} catch (Exception ee) {
					packetGenErr = " 'intakeform' docType processing eComm=" + ecomm;
					log.error(packetGenErr, ee);
					packetGenErr = packetGenErr + "\n " + ee.getMessage();
					mailSubject = "intake docType ";
					ee.printStackTrace();
					isAllDocumentsProcessedSuccessFully = false;
					deleteAllCreatedDocumentsDuringException(docList, ecomm);
					break;
				}
			} else {
				isAllDocumentsProcessedSuccessFully = false;
				packetGenErr = "Unknown DocumentType for processing eComm=" + ecomm + " documentType="
						+ docInfo.getDocumentType();
				mailSubject = "Unknown DocumentType";
				log.error(packetGenErr);
				deleteAllCreatedDocumentsDuringException(docList, ecomm);
				break;
			}

		} // while (docIter.hasNext())

		final String mailAddrFlag = evo.getMailingAddressFlag();
		// check if all documents are processed successfully for this eComm
		// if not send email to the IT team
		if (isAllDocumentsProcessedSuccessFully) {
			if ("I".equalsIgnoreCase(mailAddrFlag) || "O".equalsIgnoreCase(mailAddrFlag)
					|| "E".equalsIgnoreCase(mailAddrFlag)) {
				log.info("Do not ADD to PacketsBatch ecomm=" + ecomm + " mailAddrFlag=" + mailAddrFlag);
			} else {
				// Make entries in the control file only if the mailAddrFlag
				// is not I(International Packet),O(Online Packet),E(Email
				// Packet).
				// Note:Only the entries in Major/Minor Control files will be
				// sent/printed/mailed.
				makeAnEntryInTheMajorControlFile(ecomm, docCounter, pullDocCounter);
			}

			checkIfEcommDirExists(evo);

			if ("I".equalsIgnoreCase(mailAddrFlag) || "O".equalsIgnoreCase(mailAddrFlag)
					|| "E".equalsIgnoreCase(mailAddrFlag)) {
				// Move the ecomm docs to the Ecomms folder
				moveEcommDocsToEcommsDir(docList, evo);
			} else {
				// Copy the ecomm docs to the Ecomms folder
				copyGeneratedEcommDocsToEcommsDir(docList, evo);
			}

			// make entries in the wd_ecomm_documents table on the locations
			if (dbUtil.updateDocFileName(docList, ecomm)) {
				dbUtil.updateWDPacketDetails("C", ecomm, packetBatch);
				dbUtil.updateWDEcommHeader("COMPLETE", ecomm);
			} else {
				dbUtil.updateWDPacketDetails("E", ecomm, packetBatch);
			}

		} else {
			// send email to the IT team
			log.error("Problem in Packet Generation for eComm " + ecomm);
			dbUtil.updateWDPacketDetails("E", ecomm, packetBatch);
			packetGenErr = packetGenErr + "\n\n For the document " + currentDocInfo;
			MailUtil.sendMail(ecomm, mailSubject, packetGenErr);
		}
	}// processDocList

	private void checkIfEcommDirExists(EcommVO evo) {
		String year = evo.getEcommCreatedYear();
		String month = evo.getEcommCreatedMonth();

		if (null != year && !EMPTY.equals(year) && year.length() > 0) {
			// check if the directory exists
			// if it does not exists create it
			String yearDirToCheck = props.getProperty("ecomms.path") + year;
			if (!FileUtil.checkIfDirectoryExists(yearDirToCheck)) {
				// if the directory does not exist, create the directory
				if (!FileUtil.createDirectory(yearDirToCheck)) {
					log.error("ERROR WHILE CREATING THE DIRECTORY -> " + yearDirToCheck);
				}
			}
		}

		if (null != month && !EMPTY.equals(month) && month.length() > 0) {
			// check if the directory exists
			// if it does not exists create it
			String monthDirToCheck = props.getProperty("ecomms.path") + year + props.getProperty("file.pathSeparator")
					+ month;
			if (!FileUtil.checkIfDirectoryExists(monthDirToCheck)) {
				// if the directory does not exist, create the directory
				if (!FileUtil.createDirectory(monthDirToCheck))
					log.error("ERROR WHILE CREATING THE DIRECTORY -> " + monthDirToCheck);
			}
		}
	}// checkIfEcommDirExists()

	private void moveEcommDocsToEcommsDir(List<DocumentInfo> docList, EcommVO evo) {
		if (null == docList) {
			log.error("moveGeneratedEcommDocsToEcommsDir docList is null");
			return;
		}
		log.info("START moveEcommDocsToEcommsDir eComm=" + evo.getEcomm());
		Iterator<DocumentInfo> docIterator = docList.iterator();
		while (docIterator.hasNext()) {
			DocumentInfo docInfo = docIterator.next();

			if (null == docInfo.getFileName() || EMPTY.equals(docInfo.getFileName())
					|| docInfo.getFileName().length() == 0) {
				continue;
			}

			String moveFromDirectory = props.getProperty("packetgeneration.path") + packetBatch;
			String moveToDirectory = props.getProperty("ecomms.path") + evo.getEcommCreatedYear() + fileSep
					+ evo.getEcommCreatedMonth();

			File srcFile = new File(moveFromDirectory, docInfo.getFileName());
			File destFile = new File(moveToDirectory, docInfo.getFileName());
			if (fileUtil.moveFile(srcFile, destFile)) {
				if (log.isDebugEnabled()) {
					log.debug("Move Success srcFile=" + srcFile + " destFile=" + destFile);
				}
			} else {
				log.error("moveEcommDocsToEcommsDir for ecomm=" + evo.getEcomm() + " srcFile=" + moveFromDirectory
						+ docInfo.getFileName() + " destFile=" + moveToDirectory + docInfo.getFileName());
			}
		} // while
		log.info("STOP moveEcommDocsToEcommsDir eComm=" + evo.getEcomm());
	}// moveGeneratedEcommDocsToEcommsDir

	private void copyGeneratedEcommDocsToEcommsDir(List<DocumentInfo> docList, EcommVO evo) {

		long ecomm = evo.getEcomm();
		String year = evo.getEcommCreatedYear();
		String month = evo.getEcommCreatedMonth();
		log.info("START copyGeneratedEcommDocsToEcommsDir eComm=" + ecomm);

		if (null != docList) {
			Iterator<DocumentInfo> docIterator = docList.iterator();

			while (docIterator.hasNext()) {
				DocumentInfo docInfo = docIterator.next();

				// String recvdDocumentName = docInfo.getDocumentName();

				// log.info(" THE LOCATION RECEIVED IS -> " +
				// theRecvdDocumentName);
				if (null != docInfo.getFileName() && !EMPTY.equals(docInfo.getFileName())
						&& docInfo.getFileName().length() > 0) {
					String copyFromDirectory = props.getProperty("packetgeneration.path") + packetBatch + fileSep;
					String copyToDirectory = props.getProperty("ecomms.path") + year + fileSep + month;
					File srcFile = new File(copyFromDirectory, docInfo.getFileName());
					File destFile = new File(copyToDirectory, docInfo.getFileName());

					if (log.isDebugEnabled()) {
						log.debug("ECOMM FILE BEING COPIED FROM " + copyFromDirectory + " TO " + copyToDirectory
								+ " fileName=" + docInfo.getFileName());
					}

					String err = "EXCEPTION OCCURED WHEN COPYING THE ECOMM DOC " + " FROM=" + copyFromDirectory
							+ " TO THE FILE=" + copyToDirectory;

					try {
						Long sourceFileChecksumVal = fileUtil.copyFile(srcFile, destFile);
						Long destinationFileChecksumVal = fileUtil.getChecksum(destFile);
						if (sourceFileChecksumVal.equals(destinationFileChecksumVal)) {
							// log.info(" OK, files are equal.");
						} else {
							log.error(err + " CHECKSUM VALUE ARE NOT EQUAL");
						}
					} catch (Exception e) {
						log.error(err, e);
					}
				}
			}
		}
		log.info("STOP copyGeneratedEcommDocsToEcommsDir eComm=" + ecomm);
	}

	private void deleteAllCreatedDocumentsDuringException(List<DocumentInfo> docList, long eComm) {
		log.info("STATUS : deleteAllCreatedDocumentsDuringException ");
		if (null == docList) {
			log.error("deleteAllCreatedDocumentsDuringException docList is null");
			return;
		}
		Iterator<DocumentInfo> documentListIterator = docList.iterator();
		while (documentListIterator.hasNext()) {
			DocumentInfo recvdDocumentInfo = documentListIterator.next();

			String packetBatchDirectory = props.getProperty("packetgeneration.path") + packetBatch
					+ props.getProperty("file.pathSeparator");
			String fileName = "wf-" + eComm + "-" + recvdDocumentInfo.getDocumentId() + ".pdf";

			File file = new File(packetBatchDirectory + fileName);

			if (file.exists()) {
				boolean isFileDelete = file.delete();
				log.info("FILE DELETED = " + isFileDelete + ". fileName=" + fileName);
			} else {
				log.warn("FILE NOT FOUND. fileName=" + fileName);
			}
		}
	}

	private static String formatMonthValueForIntakeLocation(int theDefaultMonthValue) {
		String theMonthValueToReturn = EMPTY;
		theDefaultMonthValue = theDefaultMonthValue + 1;
		if (theDefaultMonthValue < 10)
			theMonthValueToReturn = "0" + theDefaultMonthValue;
		else
			theMonthValueToReturn = new Integer(theDefaultMonthValue).toString();

		return theMonthValueToReturn;
	}

	private static String getFormattedCurrentDate() {
		java.util.Date now = new java.util.Date();
		Calendar theCalendar = Calendar.getInstance();
		theCalendar.setTime(now);
		int year = theCalendar.get(Calendar.YEAR);
		int month = theCalendar.get(Calendar.MONTH);
		String monthStr = formatMonthValueForIntakeLocation(month);

		String currentDate = monthStr + "-" + theCalendar.get(Calendar.DATE) + "-" + year;
		return currentDate;
	}

	private static String getCurrentDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new java.util.Date());
	}

	private void makeAnEntryInTheMajorControlFile(long eComm, int docCtr, int pullDocCtr) {
		String minorControlFileName = createTheMinorControlFile(eComm);
		MajorControlFileInfo maCFInfo = dbUtil.getMajorControlFileInfo(eComm);

		String stringToWrite = minorControlFileName + "|" + docCtr + "|" + pullDocCtr + "|" + packetBatch + "|" + eComm
				+ "|" + maCFInfo.getTheEcommName() + "|" + maCFInfo.getTheFirstName() + "|" + maCFInfo.getTheLastName()
				+ "|" + maCFInfo.getTheClientCode() + "|" + maCFInfo.getTheClientName() + "|";

		// if the file is not created already
		// create the file
		// if present, append to the file

		String packetBatchDirName = props.getProperty("packetgeneration.path") + packetBatch
				+ props.getProperty("file.pathSeparator");
		String majorControlFileName = "set_" + packetBatch + "_control_file.txt";

		try {
			File majorControlFile = new File(packetBatchDirName, majorControlFileName);

			// Raama added Dec 17 07
			// check if the file exists, if exists then do not write the header
			if (!majorControlFile.exists()) {
				majorControlFileWriter = new BufferedWriter(new FileWriter(majorControlFile, true));
				majorControlFileWriter.write(maCFInfo.getTheHDR() + "\n");
				majorControlFileWriter.flush();
			} else if (majorControlFileWriter == null) {
				majorControlFileWriter = new BufferedWriter(new FileWriter(majorControlFile, true));
			}

			majorControlFileWriter.write(stringToWrite + "\n");
			majorControlFileWriter.flush();

		} catch (Exception ex) {
			log.error(" PacketsBatchGeneration.makeAnEntryInTheMajorControlFile", ex);
			// System.exit( -1 );
		}
	}

	private String createTheMinorControlFile(long eComm) {
		// MinorControlFileInfo theRecvdMinorControlFile =
		// theDBUtil.populateTheMinorControlFileInfo(theEcommNumber);

		String minorControlFileName = "wf-" + eComm + "-a_control_file.txt";

		if (null != micfInfo) {
			// wf-<ecomm_number>-a_control_file.txt
			String packetBatchDirName = props.getProperty("packetgeneration.path") + packetBatch
					+ props.getProperty("file.pathSeparator");

			StringBuilder strBuffer = new StringBuilder();

			strBuffer.append("HDR|set_id|ecomm_id|ecomm_name|first_name|last_name|client_code|client_name|\n");
			strBuffer.append("HDR|" + packetBatch + "|" + eComm + "|" + "PACKET" + "|" + micfInfo.getClaimantFirstName()
					+ "|" + micfInfo.getClaimantLastName() + "|" + micfInfo.getClientCode() + "|"
					+ micfInfo.getClientName() + "|\n");

			strBuffer.append("HDR|addr_type|name|addr1|addr2|city|state|zip|country|\n");
			strBuffer.append("HDR|recipient|" + micfInfo.getClaimantFirstName() + " " + micfInfo.getClaimantMiddleName()
					+ " " + micfInfo.getClaimantLastName() + "|" + micfInfo.getIntakeHomeAddress1() + "|"
					+ micfInfo.getIntakeHomeAddress2() + "|" + micfInfo.getIntakeHomeCity() + "|"
					+ micfInfo.getIntakeHomeState() + "|" + micfInfo.getIntakeHomeZip() + "|"
					+ micfInfo.getIntakeHomeCountry() + "|\n");

			// get the return address
			strBuffer.append("HDR|return_address|||" + micfInfo.getMatrixAddress1() + " " + micfInfo.getMatrixAddress2()
					+ "|" + micfInfo.getMatrixCity() + "|" + micfInfo.getMatrixState() + "|" + micfInfo.getMatrixZip()
					+ "|" + micfInfo.getMatrixCountry() + "|\n");

			strBuffer.append(minorControlFileText);

			try {
				BufferedWriter minorControlFileWriter = new BufferedWriter(
						new FileWriter(new File(packetBatchDirName, minorControlFileName)));
				minorControlFileWriter.write(strBuffer.toString());
				minorControlFileWriter.flush();
				minorControlFileWriter.close();
			} catch (Exception excp) {
				log.error("createTheMinorControlFile()", excp);
				// System.exit( -1 );
			}
		}
		return minorControlFileName;
	}

	private void closeTheWriters() {
		try {
			if (null != majorControlFileWriter) {
				majorControlFileWriter.flush();
				majorControlFileWriter.close();
			}
		} catch (Exception eee) {
			log.error("closeTheWriters()", eee);
		}
	}

	private String replaceMatrixAttrs(File foFile, List<DocumentInfo> docList, EcommVO evo, String documentId)
			throws Exception {

		log.info("STATUS : Entering replaceMatrixAttrs method " + micfInfo);
		String tempFoFileName = null;

		try {
			boolean isAnyFieldNullInMinorControl = micfInfo.checkTheClassFields();
			if (!isAnyFieldNullInMinorControl) {
				// there are few attrs in the minor control file
				// whose values are null
				// do not process this ECOMM
				// start clean up process for this ECOMM
				return null;
			}

			BufferedReader foReader = new BufferedReader(new InputStreamReader(new FileInputStream(foFile), "UTF8"));
			StringBuilder foStrBuffer = new StringBuilder();
			String foStr = null;

			while ((foStr = foReader.readLine()) != null) {

				foStr = replaceAttrDesc(foStr);

				foStr = replaceDocAttrDesc(foStr, docList);

				log.info("STATUS : Check for UNKOWN Attributes ");
				checkUnkownAttributes(foStr);

				foStrBuffer.append(foStr + "\n");

			} // while(readline)
			foReader.close();

			log.info(
					"FO Reader Closed::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

			String finalFileString = foStrBuffer.toString();

			log.info(
					"Template Created::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

			try {

				com.matrixcos.conditionaldisplay.FoJavaUtil foJavaUtil = new com.matrixcos.conditionaldisplay.FoJavaUtil();

				log.info(
						"Start Calling Conditional Display Method ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

				finalFileString = foJavaUtil.processFO(foStrBuffer.toString(), evo.getIntake().longValue(),
						evo.getCliCode().longValue(), documentId, props.getProperty("database.url"),
						props.getProperty("workdesk.user"), props.getProperty("workdesk.pass"));

				log.info(
						"End Conditional Display Method ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			} catch (Exception ex) {
				log.info("Exception:::::::::::::::::::::::::::::::" + ex.getMessage());
			}

			tempFoFileName = "tempFOFile.fo";

			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(props.getProperty("packetgeneration.path") + tempFoFileName), "UTF8"));

			out.write(finalFileString.toString());
			out.flush();
			out.close();

			log.info("STATUS : LEAVING REPLACE MATRIX ATTRS METHOD ");

		} catch (Exception e) {
			String err = "ProcessIntake.replaceMatrixAttrs() " + e.getMessage();
			log.info(EMPTY);
			log.error(err, e);
			log.info(EMPTY);
			throw e;
		}
		return tempFoFileName;
	}

	public static void main(String[] args) throws Exception {
		PacketsBatchGeneration pbg = new PacketsBatchGeneration();
		String foStr = "<fo:root font-family=\"TimesNewRoman\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:w=\"http://schemas.microsoft.com/office/word/2003/wordml\"><fo:layout-master-set xmlns:rx=\"http://www.renderx.com/XSL/Extensions\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wx=\"http://schemas.microsoft.com/office/word/2003/auxHint\" xmlns:aml=\"http://schemas.microsoft.com/aml/2001/core\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:dt=\"uuid:C2F41010-65B3-11d1-A29F-00AA00C14882\"><fo:simple-page-master master-name=\"section1-first-page\" page-width=\"8.5in\" page-height=\"11in\" margin-top=\"36pt\" margin-bottom=\"36pt\" margin-right=\"36pt\" margin-left=\"36pt\"><fo:region-body margin-top=\"-23.75pt\" margin-bottom=\"-23.75pt\"></fo:region-body><fo:region-before region-name=\"first-page-header\" extent=\"11in\"></fo:region-before><fo:region-after region-name=\"first-page-footer\" extent=\"11in\" display-align=\"after\"></fo:region-after></fo:simple-page-master><fo:simple-page-master master-name=\"section1-odd-page\" page-width=\"8.5in\" page-height=\"11in\" margin-top=\"36pt\" margin-bottom=\"36pt\" margin-right=\"36pt\" margin-left=\"36pt\"><fo:region-body margin-top=\"-23.75pt\" margin-bottom=\"-23.75pt\"></fo:region-body><fo:region-before region-name=\"odd-page-header\" extent=\"11in\"></fo:region-before><fo:region-after region-name=\"odd-page-footer\" extent=\"11in\" display-align=\"after\"></fo:region-after></fo:simple-page-master><fo:simple-page-master master-name=\"section1-even-page\" page-width=\"8.5in\" page-height=\"11in\" margin-top=\"36pt\" margin-bottom=\"36pt\" margin-right=\"36pt\" margin-left=\"36pt\"><fo:region-body margin-top=\"-23.75pt\" margin-bottom=\"-23.75pt\"></fo:region-body><fo:region-before region-name=\"even-page-header\" extent=\"11in\"></fo:region-before><fo:region-after region-name=\"even-page-footer\" extent=\"11in\" display-align=\"after\"></fo:region-after></fo:simple-page-master><fo:page-sequence-master master-name=\"section1-page-sequence-master\"><fo:repeatable-page-master-alternatives><fo:conditional-page-master-reference odd-or-even=\"odd\" master-reference=\"section1-odd-page\" /><fo:conditional-page-master-reference odd-or-even=\"even\" master-reference=\"section1-even-page\" /></fo:repeatable-page-master-alternatives></fo:page-sequence-master></fo:layout-master-set><fo:page-sequence master-reference=\"section1-page-sequence-master\" id=\"IDAEI2JC\" format=\"1\" xmlns:rx=\"http://www.renderx.com/XSL/Extensions\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wx=\"http://schemas.microsoft.com/office/word/2003/auxHint\" xmlns:aml=\"http://schemas.microsoft.com/aml/2001/core\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:dt=\"uuid:C2F41010-65B3-11d1-A29F-00AA00C14882\"><fo:static-content flow-name=\"first-page-header\"><fo:retrieve-marker retrieve-class-name=\"first-page-header\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"first-page-footer\"><fo:retrieve-marker retrieve-class-name=\"first-page-footer\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"odd-page-header\"><fo:retrieve-marker retrieve-class-name=\"odd-page-header\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"odd-page-footer\"><fo:retrieve-marker retrieve-class-name=\"odd-page-footer\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"even-page-header\"><fo:retrieve-marker retrieve-class-name=\"odd-page-header\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"even-page-footer\"><fo:retrieve-marker retrieve-class-name=\"odd-page-footer\" retrieve-position=\"first-including-carryover\" retrieve-boundary=\"page\" /></fo:static-content><fo:static-content flow-name=\"xsl-footnote-separator\"><fo:block><fo:leader leader-pattern=\"rule\" leader-length=\"144pt\" rule-thickness=\"0.5pt\" rule-style=\"solid\" color=\"gray\" /></fo:block></fo:static-content><fo:flow flow-name=\"xsl-region-body\"><fo:block widows=\"2\" orphans=\"2\" font-size=\"10pt\" line-height=\"1.147\" white-space-collapse=\"false\"><fo:marker marker-class-name=\"first-page-header\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:marker marker-class-name=\"first-page-footer\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:marker marker-class-name=\"odd-page-header\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:marker marker-class-name=\"odd-page-footer\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:marker marker-class-name=\"even-page-header\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:marker marker-class-name=\"even-page-footer\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\" /><fo:table font-family=\"TimesNewRoman\" start-indent=\"-12.6pt\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\"><fo:table-column column-number=\"1\" column-width=\"324pt\" /><fo:table-column column-number=\"2\" column-width=\"252pt\" /><fo:table-body start-indent=\"0pt\" end-indent=\"0pt\"><fo:table-row padding-top=\"0pt\" padding-bottom=\"0pt\" height=\"729pt\"><fo:table-cell padding-top=\"0pt\" padding-left=\"5.4pt\" padding-bottom=\"0pt\" padding-right=\"5.4pt\" border-top-style=\"none\" border-top-color=\"black\" border-top-width=\"0.5pt\" border-left-style=\"none\" border-left-color=\"black\" border-left-width=\"0.5pt\" border-bottom-style=\"none\" border-bottom-color=\"black\" border-bottom-width=\"0.5pt\" border-right-style=\"none\" border-right-color=\"black\" border-right-width=\"0.5pt\"><fo:block-container><fo:block font-family=\"Arial\" font-size=\"12pt\" language=\"EN-US\" keep-with-next.within-page=\"always\" space-before=\"12pt\" space-before.conditionality=\"retain\" space-after=\"3pt\" space-after.conditionality=\"retain\" font-weight=\"bold\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />MATRIX ABSENCE MANAGEMENT, INC.</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />�MATRIX_ADDRESS1�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" /> </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�MATRIX_ADDRESS2�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />�MATRIX_CITY�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" /> </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�MATRIX_STATE�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />  </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�MATRIX_ZIP�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__FIRSTNAME�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" /> </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__LASTNAME�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__HOMEADDRESS1�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" /> </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__HOMEADDRESS2�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__HOMECITY�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" /> </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__HOMESTATE�</fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />  </fo:inline><fo:inline><fo:leader leader-length=\"0pt\" />�INTAKE__HOMEZIP�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block></fo:block-container></fo:table-cell><fo:table-cell padding-top=\"0pt\" padding-left=\"5.4pt\" padding-bottom=\"0pt\" padding-right=\"5.4pt\" border-top-style=\"none\" border-top-color=\"black\" border-top-width=\"0.5pt\" border-bottom-style=\"none\" border-bottom-color=\"black\" border-bottom-width=\"0.5pt\" border-right-style=\"none\" border-right-color=\"black\" border-right-width=\"0.5pt\" border-left-style=\"none\" border-left-color=\"black\" border-left-width=\"0.5pt\"><fo:block-container><fo:block font-family=\"TimesNewRoman\" font-size=\"16pt\" language=\"EN-US\" text-align=\"center\" font-weight=\"bold\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" text-align=\"center\"><fo:inline font-family=\"Arial\" font-weight=\"bold\"><fo:leader leader-length=\"0pt\" />Packet Checklist</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" font-weight=\"bold\"><fo:leader /></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_1�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_2�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_3�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_4�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_5�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_6�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_7�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_8�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_9�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_10�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_11�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_12�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_13�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_14�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_15�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_16�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_17�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_18�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_19�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_20�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_21�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_22�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_23�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_24�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_25�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_26�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_27�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_28�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_29�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_30�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_31�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_32�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_33�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_34�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_35�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_36�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_37�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_38�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_39�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:inline><fo:leader leader-length=\"0pt\" />�DOCUMENT_40�</fo:inline></fo:block><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\"><fo:leader /></fo:block></fo:block-container></fo:table-cell></fo:table-row></fo:table-body></fo:table><fo:block font-family=\"TimesNewRoman\" font-size=\"12pt\" language=\"EN-US\" xmlns:st1=\"urn:schemas-microsoft-com:office:smarttags\" xmlns:svg=\"http://www.w3.org/2000/svg\"><fo:leader /></fo:block></fo:block><fo:block id=\"IDACI2JC\" /></fo:flow></fo:page-sequence></fo:root>";
		pbg.checkUnkownAttributes(foStr);
	}

	public void checkUnkownAttributes(String foStr) throws Exception {

		// All the current attributes to look for in the foStr
		// Set<String> knownAttrSet = new
		// TreeSet<String>(caseInsensitiveComparator);

		// Look for all the attributes in the foStr string.
		Set<String> foAttrSet = new TreeSet<String>();

		String regex = "�[^�]*�";
		Pattern regexPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = regexPattern.matcher(foStr);
		while (matcher.find()) {
			foAttrSet.add(matcher.group());
		}

		// Now check if any of docAttrSet
		// are not in known attributes i.e attrSet.
		Set<String> unkownAttrSet = new HashSet<String>();
		StringBuilder unkownAttrStr = new StringBuilder();
		for (String attr : foAttrSet) {
			if (!knownAttrSet.contains(attr)) {
				unkownAttrSet.add(attr);
				unkownAttrStr.append("\n        " + attr);
			}
		}

		if (unkownAttrSet.size() > 0) {
			String err = "Unkown attributes found in the fo document. Here are the list of UNKOWN Attributes "
					+ unkownAttrStr.toString();
			unkownAttrStr = null;
			throw new Exception(err);
		}
		unkownAttrStr = null;
		unkownAttrSet = null;

	} // checkUnkownAttributes

	/**
	 * Note: When you add new attributes, also add them to the set in
	 * initializeKnownAttrSet() method
	 * 
	 * @param foStr
	 * @return
	 */
	private String replaceAttrDesc(String foStr) {

		// Following three replacements are customized due to
		// some existing fo's with mixed case error attribute names
		// Commented these after search
		// TODO verify in production FOFILES folder.
		// foStr = foStr.replaceAll("intake__homeaddress1�", micfInfo
		// .getTheIntakeHomeAddress1());
		// foStr = foStr.replaceAll("Intake__Homeaddress1�", micfInfo
		// .getTheIntakeHomeAddress1());
		// foStr = foStr.replaceAll("intake__homestate�", micfInfo
		// .getTheIntakeHomeState());

		StringBuilder logSB = new StringBuilder(500);

		foStr = repAttrAndLogg(foStr, "�INTAKE__FIRSTNAME�", micfInfo.getClaimantFirstName(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__LASTNAME�", micfInfo.getClaimantLastName(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__HOMEADDRESS1�", micfInfo.getIntakeHomeAddress1(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__HOMEADDRESS2�", micfInfo.getIntakeHomeAddress2(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__HOMECITY�", micfInfo.getIntakeHomeCity(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__HOMESTATE�", micfInfo.getIntakeHomeState(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__HOMEZIP�", micfInfo.getIntakeHomeZip(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE__COUNTRY�", micfInfo.getIntakeHomeCountry(), logSB);

		foStr = repAttrAndLogg(foStr, "�CLAIMANT_FNAME�", micfInfo.getClaimantFirstName(), logSB);
		foStr = repAttrAndLogg(foStr, "�CLAIMANT_MNAME�", micfInfo.getClaimantMiddleName(), logSB);
		foStr = repAttrAndLogg(foStr, "�CLAIMANT_LNAME�", micfInfo.getClaimantLastName(), logSB);
		// TODO
		foStr = repAttrAndLogg(foStr, "�SSN�", micfInfo.getClaimantSSN(), logSB);

		foStr = repAttrAndLogg(foStr, "�INTAKE__DOB�", micfInfo.getIntakeDOB(), logSB);

		foStr = repAttrAndLogg(foStr, "�MATRIX_ADDRESS1�", micfInfo.getMatrixAddress1(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_ADDRESS2�", micfInfo.getMatrixAddress2(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_CITY�", micfInfo.getMatrixCity(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_STATE�", micfInfo.getMatrixState(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_ZIP�", micfInfo.getMatrixZip(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_COUNTRY�", micfInfo.getMatrixCountry(), logSB);

		foStr = repAttrAndLogg(foStr, "�MATRIX_FAXNUMBER�", micfInfo.getMatrixFaxNumber(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_PHONENUMBER�", micfInfo.getMatrixPhoneNumber(), logSB);
		foStr = repAttrAndLogg(foStr, "�MATRIX_TOLLFREE�", micfInfo.getMatrixTollFree(), logSB);

		foStr = repAttrAndLogg(foStr, "�EVENT_CREATE_DATETIME�", micfInfo.getMatrixEventCreateDateTime(), logSB);

		foStr = repAttrAndLogg(foStr, "�INTAKE__FIRSTDAYUNABLETOWORK�", micfInfo.getIntakeFirstDayUnableToWork(),
				logSB);

		foStr = repAttrAndLogg(foStr, "�SCHEDULED_RTW_DATE�", micfInfo.getRTWDateTime(), logSB);
		foStr = repAttrAndLogg(foStr, "�DOCS_DUE_DATE_15�", micfInfo.getClaimantCurrDt15(), logSB);

		foStr = repAttrAndLogg(foStr, "�TECHNICIAN_EMAIL�", micfInfo.getTechnicianMail(), logSB);
		foStr = repAttrAndLogg(foStr, "�TECHNICIAN_NAME�", micfInfo.getTechnicianName(), logSB);

		foStr = repAttrAndLogg(foStr, "�RETURN_ADDRESS1�", micfInfo.getIntakeHomeAddress1(), logSB);
		foStr = repAttrAndLogg(foStr, "�RETURN_ADDRESS2�", micfInfo.getIntakeHomeAddress2(), logSB);
		foStr = repAttrAndLogg(foStr, "�RETURN_STATE�", micfInfo.getIntakeHomeState(), logSB);
		foStr = repAttrAndLogg(foStr, "�RETURN_ZIP�", micfInfo.getIntakeHomeZip(), logSB);

		foStr = repAttrAndLogg(foStr, "�CLIENT_NAME�", micfInfo.getClientName(), logSB);

		foStr = repAttrAndLogg(foStr, "�CURRENT_DATE�", getFormattedCurrentDate(), logSB);
		foStr = repAttrAndLogg(foStr, "�CURRENT_DATE_F1�", getCurrentDate("MMMM dd, yyyy"), logSB);

		// Following five are required for the new LOA packets.
		foStr = repAttrAndLogg(foStr, "�MemberFirstName�", micfInfo.getMemberFirstName(), logSB);
		foStr = repAttrAndLogg(foStr, "�MemberMiddleName�", micfInfo.getMemberMiddleName(), logSB);
		foStr = repAttrAndLogg(foStr, "�MemberLastName�", micfInfo.getMemberLastName(), logSB);
		foStr = repAttrAndLogg(foStr, "�MemberRelationship�", micfInfo.getMemberRelationship(), logSB);
		foStr = repAttrAndLogg(foStr, "�MemberDateOfBirth�", micfInfo.getMemberDateOfBirth(), logSB);

		// For AutoFax pattern cert requirements
		foStr = repAttrAndLogg(foStr, "�PHYSICIAN_NAME�", micfInfo.getPhyName(), logSB);
		foStr = repAttrAndLogg(foStr, "�INTAKE_NUMBER�", micfInfo.getIntakeNumber(), logSB);
		foStr = repAttrAndLogg(foStr, "�CLAIM_NUMBER�", micfInfo.getClaimNumber(), logSB);
		foStr = repAttrAndLogg(foStr, "�FORM_NAME�", micfInfo.getFormName(), logSB);

		String s3 = micfInfo.getClientCode() + "-" + micfInfo.getIntakeNumber();
		foStr = repAttrAndLogg(foStr, "�CLI-INTAKE�", s3, logSB);

		log.info("Replace Attribute(s) " + logSB.toString());

		return foStr;
	}

	/**
	 * When it finds the attribute in foStr it will replace all the attributes
	 * in foStr with the value. Note : 'attr' is case insensitive.
	 * 
	 * @param foStr
	 * @param attr
	 * @param value
	 * @param logSB
	 * @return
	 */
	private static String repAttrAndLogg(String foStr, String attr, String value, StringBuilder logSB) {
		String tempfoStr = foStr.toUpperCase();
		String tempAttr = attr.toUpperCase();

		if (tempfoStr.indexOf(tempAttr) != -1) {
			foStr = foStr.replaceAll("(?i)" + attr, value);
			logSB.append(" " + attr + " with Value " + value);
		}
		return foStr;
	}

	private String replaceDocAttrDesc(String foStr, List<DocumentInfo> docList) throws Exception {
		StringBuilder logSB = new StringBuilder(800);
		String err = " Error in ProcessIntake.replaceDocAttrDesc()";
		String docAttr = EMPTY;
		String docDescription = EMPTY;

		String docFieldPrefixStr = "(?i)�document_";
		String docFieldSuffixStr = "�";

		if (docList == null || foStr == null) {
			String err1 = err + " theDocumentsListReceived or foStr is null";
			log.info(err1);
			throw new Exception(err1);
		}

		String tempfoStr = foStr.toUpperCase();

		Iterator<DocumentInfo> docIter = docList.iterator();
		int docIndex = 1;
		while (docIter.hasNext()) {
			DocumentInfo doc = docIter.next();
			if (doc == null || (doc != null & doc.getDocumentDescription() == null)) {
				String err2 = err + " DocumentInfo object or theDocumentDescription is null";
				throw new Exception(err2);
			}
			docAttr = docFieldPrefixStr + docIndex + docFieldSuffixStr;
			docDescription = docIndex + ". " + doc.getDocumentDescription();

			if (tempfoStr.indexOf(docAttr.substring(4).toUpperCase()) != -1) {
				foStr = foStr.replaceAll(docAttr, docDescription);
				logSB.append(" " + docAttr.substring(4) + " with " + docDescription);
			}
			docIndex++;
		} // while

		log.info("Replace document_xx attributes " + logSB.toString());

		// Replace the remaining document_XX fields with null strings.
		for (; docIndex <= checkListSize; docIndex++) {
			docAttr = docFieldPrefixStr + docIndex + docFieldSuffixStr;
			docDescription = EMPTY;
			foStr = foStr.replaceAll(docAttr, docDescription);
		}

		if (docList.size() > checkListSize) {
			String err2 = err + " docList size = " + docList.size() + " is greater than the checkList size limit = "
					+ checkListSize;
			throw new Exception(err2);
		}

		return foStr;
	}// replaceDocAttrDesc

	///// Getting Aspose Atttributes

}// class
