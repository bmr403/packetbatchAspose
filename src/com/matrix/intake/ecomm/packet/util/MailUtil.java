/*
 * MailUtil.java
 *
 * Created on September 24, 2007, 9:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.util;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 
 * @author Venkat
 */
public class MailUtil {

	/** Creates a new instance of MailUtil */
	public MailUtil() {
	}

	/**
	 * This method sends an email out using the SMTP server mail.properties file
	 * should reside in the class path
	 * @param subject TODO
	 * @param body TODO
	 * @param theSubject
	 *            the subject of the mail sent
	 * @param theConent
	 *            the mail content
	 * @return the actual user permission's string value
	 */
	public static void sendMail(long errorEcomm, String subject, String body) {
		String theMessageTo = null; // message addresse
		String theMessageFrom = null; // message from
		String theSMTPHost = null; // smtp server
		String theMailDebugFlag = null; // mail debug value

		// read all the values from properties file
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("mail.properties"));
			theMessageTo = properties.getProperty("intake.mail.to");
			theMessageFrom = properties.getProperty("intake.mail.from");
			theSMTPHost = properties.getProperty("intake.smtp.server");
			theMailDebugFlag = properties.getProperty("intake.mail.debug");
		} catch (Exception excep) {
			System.out.println(excep);
			System.exit(-1);
		}

		// Create properties, get Session
		Properties theMailProperties = new Properties();
		theMailProperties.put("mail.smtp.host", theSMTPHost);
		// To see what is going on behind the scene
		theMailProperties.put("mail.debug", theMailDebugFlag);
		Session theMailSsession = Session.getInstance(theMailProperties);

		try {
			// Instantiate a message
			Message theMailMessage = new MimeMessage(theMailSsession);

			// Set message attributes
			theMailMessage.setFrom(new InternetAddress(theMessageFrom));
			InternetAddress[] theSendAddress = { new InternetAddress(
					theMessageTo) };
			theMailMessage.setRecipients(Message.RecipientType.TO,
					theSendAddress);
			theMailMessage
					.setSubject(subject + "  PACKET GENERATION FAILED FOR THE ECOMM NUMBER -> "
							+ errorEcomm);
			theMailMessage.setSentDate(new Date());

			// Set message content
			theMailMessage.setText(
					"PACKET GENERATION FAILURE\n\n"
					+ (body==null?"":body) );

			// Send the message
			Transport.send(theMailMessage);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

}
