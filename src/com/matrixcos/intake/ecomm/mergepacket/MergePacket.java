/**
 * 
 */
package com.matrixcos.intake.ecomm.mergepacket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.matrix.intake.ecomm.packet.objects.EcommVO;
import com.matrixcos.intake.ecomm.mergepacket.dao.MergePacketDao;

/**
 * @author kbobba
 * 
 */
public class MergePacket {

    private final Logger log = Logger.getLogger(MergePacket.class);

    private static Properties props;

    private static String fileSep = System.getProperty("file.separator");

    private MergePacketDao mergePacketDao;

    /**
     * @param args
     * @throws SQLException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        MergePacket mp = new MergePacket();
        mp.mergePacketsCreatedToday();
    }

    public MergePacket() {
        super();
        loadProperties();
        mergePacketDao = new MergePacketDao(MergePacket.props);
    }

    /**
     * 
     */
    public void mergePacketsCreatedToday() {
        List<EcommVO> ecommList = mergePacketDao.getPacketEcommsCreatedTodayForMerge();
        for (EcommVO evo : ecommList) {
            log.info("ToMerge PacketEcomm=" + evo.getEcomm());
            mergePacketEcomm(evo);
        }
    }

    /**
     * @param ecomm
     */
    private void mergePacketEcomm(EcommVO evo) {

        List<String> cmdList = new ArrayList<String>();
        cmdList.add(props.getProperty("java"));
        cmdList.add("-jar");
        cmdList.add(props.getProperty("mergepacket.lib"));
        cmdList.add("PDFMerger");

        Iterator<String> docIterator = mergePacketDao.getPacketEcommDocs(evo.getEcomm()).iterator();
        while (docIterator.hasNext()) {
            cmdList.add(getEcommFilePath(evo, docIterator.next()));
        }// while

        String packetFileName = "packet-ecomm-" + evo.getEcomm() + ".pdf";
        // Check if packet folder exist. If not we create it.
        cmdList.add(getPacketFilePath(evo, packetFileName));

        if (log.isDebugEnabled()) {
            log.debug("commandList " + cmdList);
        }

        try {
            int exitVal = -100;
            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            String line = null;
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = input.readLine()) != null) {
                log.info("Process Output = " + line);
            }
            input.close();

            try {
                exitVal = proc.waitFor();
                if (exitVal != 0) {
                    String err = "Process Error. exitValue=" + exitVal + " ecomm=" + evo.getEcomm();
                    log.error(err);
                    log.error("commandList " + cmdList);
                    mergePacketDao.updateMergePacketEcomm("E", evo.getEcomm());
                    sendEmail(getSubject(evo), err);
                } else {
                    mergePacketDao.updateMergePacketEcomm("C", evo.getEcomm());
                }
            } catch (InterruptedException e) {
                log.error("mergePacketEcomm proc.waitFor ecomm=" + evo.getEcomm(), e);
                mergePacketDao.updateMergePacketEcomm("E", evo.getEcomm());
                sendEmail(getSubject(evo), e.getMessage());
            }
        } catch (IOException e) {
            log.error("mergePacketEcomm runtime.exec ecomm=" + evo.getEcomm(), e);
            mergePacketDao.updateMergePacketEcomm("E", evo.getEcomm());
            sendEmail(getSubject(evo), e.getMessage());
        }
    }// mergePacketEcomm

    /**
     * @param evo
     * @return
     */
    private String getSubject(EcommVO evo) {
        return evo != null ? props.getProperty("mail.mergepacket.subject") + evo.getEcomm()
                          : "MERGE PACKET ERROR";
    }

    /**
     * @param evo
     * @param fileName
     * @return
     */
    private static String getEcommFilePath(EcommVO evo, String fileName) {
        return props.getProperty("ecomms.path") + evo.getEcommCreatedYear() + "/"
                + evo.getEcommCreatedMonth() + "/" + fileName;
    }

    /**
     * @param evo
     * @param fileName
     * @return
     */
    private String getPacketFilePath(EcommVO evo, String fileName) {
        String dirPath = props.getProperty("packets.path") + evo.getEcommCreatedYear() + fileSep
                + evo.getEcommCreatedMonth();
        File dir = new File(dirPath);
        if (dir.exists() == false) {
            if (log.isDebugEnabled()) {
                log.debug("Creating Directory " + dirPath);
            }
            dir.mkdirs();
        }
        return dirPath + fileSep + fileName;
    }

    /**
     * 
     */
    public void mergePacketsCreatedInPast() {
        List<EcommVO> ecommList = mergePacketDao.getPacketEcommsCreatedPastForMerge();
        int count = 0;
        for (EcommVO evo : ecommList) {
            log.info("ToMerge Past ecomm=" + evo.getEcomm() + " No=" + ++count);
            mergePacketEcomm(evo);
        }
    }

    private void loadProperties() {
        if (props == null) {
            props = new Properties();
            try {
                String propertyFile = "application.properties";
                props.load(new FileInputStream(propertyFile));
            } catch (FileNotFoundException e) {
                log.error("loadProperties()", e);
                sendEmail(getSubject(null), e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                log.error("loadProperties()", e);
                sendEmail(getSubject(null), e.getMessage());
                System.exit(1);
            }
        }
    }// loadProperties

    public void sendEmail(String subject, String body) {
        String smtpHost = props.getProperty("mail.smtp.host");
        String mailDebug = props.getProperty("mail.debug");
        String messageFrom = props.getProperty("mail.mergepacket.from");
        String messageToList = props.getProperty("mail.mergepacket.to");

        Properties mailProp = new Properties();
        mailProp.put("mail.smtp.host", smtpHost);
        mailProp.put("mail.debug", mailDebug);
        Session mailSession = Session.getInstance(mailProp);

        Message message = new MimeMessage(mailSession);

        InternetAddress fromAddress = null;
        List<InternetAddress> sendToEmailList = new ArrayList<InternetAddress>();
        // Set message attributes
        try {
            fromAddress = new InternetAddress(messageFrom);
            // toAddress = new InternetAddress(messageToList);
            if (messageToList != null) {
                String[] emailArr = messageToList.split(",");
                for (String email : emailArr) {
                    sendToEmailList.add(new InternetAddress(email));
                }
            } else {
                sendToEmailList.add(new InternetAddress("kalyan.bobba@matrixcos.com"));
            }
        } catch (AddressException e) {
            log.error("Email Address", e);
            sendEmail(getSubject(null), e.getMessage());
        }

        subject = subject == null ? "TEST Subject" : subject;
        body = body == null ? "TEST Body Text." : body;

        // System.out.println(sendToEmailList.toArray()[0].getClass().getName());
        // System.exit(0);

        try {
            InternetAddress[] iaList = new InternetAddress[sendToEmailList.size()];
            int i = 0;
            for (InternetAddress ia : sendToEmailList) {
                iaList[i++] = ia;
            }
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, iaList);
            message.setSubject(subject);
            message.setSentDate(new Date());
            message.setText((String) (body == null ? "" : body));
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Error Emailing", e);
        }

    }// sendEmail

    /**
     * 
     */
    public void mergePackets() {
        List<EcommVO> ecommList = mergePacketDao.getPacketEcommsForMerge();
        int count = 0;
        for (EcommVO evo : ecommList) {
            log.info("ToMerge ecomm=" + evo.getEcomm() + " No=" + ++count);
            mergePacketEcomm(evo);
        }
    }// mergePackets

}// class
