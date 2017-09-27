/*
 * CopyUtil.java
 *
 * Created on September 18, 2007, 5:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 
 * @author Venkat, kbobba(changes)
 */
public class FileUtil {

    private static final Logger log = Logger.getLogger(FileUtil.class);

    private static final int bufferSize = 100 * 1024;

    private byte[] buffer = new byte[bufferSize];

    public FileUtil() {
    }

    public static boolean createDirectory(String directoryName) {
        return (new File(directoryName)).mkdir();
    }

    public static boolean deleteFile(String directoryName, String fileName) {
        return (new File(directoryName + fileName)).delete();
    }

    public static boolean checkIfDirectoryExists(String directoryName) {
        return (new File(directoryName)).exists();
    }

    /**
     * This method copies a file from on directory to another
     * 
     * @param srcFile
     *            the source file to be copied
     * @param destFile
     *            the destination file
     * @return the checksum value of the source file passed
     */
    public Long copyFile(File srcFile, File destFile) throws IOException {
        InputStream inputStream = new FileInputStream(srcFile);
        OutputStream outputStream = new FileOutputStream(destFile);

        CRC32 checksum = new CRC32();
        checksum.reset();

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) >= 0) {
            checksum.update(buffer, 0, bytesRead);
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        return checksum.getValue();
    }

    /**
     * 
     * @param srcFile
     * @param destFile
     * @return
     * @throws IOException
     */
    public boolean moveFile(File srcFile, File destFile){
        try {
            long srcChecksum = copyFile(srcFile, destFile);
            long destFileChecksum = getChecksum(destFile);
            if (srcChecksum == destFileChecksum) {
                return srcFile.delete();
            } else {
                log.error("moveFile. Checksum's do not match srcFile=" + srcFile.getCanonicalPath()
                        + " destFile=" + srcFile.getCanonicalPath());
                return false;
            }
        } catch (IOException e) {
            log.error("moveFile",e);
            return false;
        }
    }

    /**
     * This method finds the checksum value of a file
     * 
     * @param file
     *            the file whose checksum value has has to be computed.
     * @return the checksum value of the source file passed
     */
    public Long getChecksum(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        CRC32 checksum = new CRC32();
        checksum.reset();

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) >= 0) {
            checksum.update(buffer, 0, bytesRead);
        }

        inputStream.close();
        return checksum.getValue();
    }

    /**
     * This method copies a file from on directory to another
     * 
     * @param directory
     *            the directory to be zip
     */
    public void zipTheDirectory(String directory) throws IOException {
        System.out.println("THE INPUT DIRECTORY RECIEVED IS -> " + directory);

        byte theByteArray[] = new byte[1024];
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(directory
                + ".zip"));

        // File theDirectory = new File("C:\\Raama\\TestingMail");
        File theDirectory = new File(directory);
        String[] theFilesArray = theDirectory.list();
        if (theFilesArray != null) {
            for (int ii = 0; ii < theFilesArray.length; ii++) {
                String fileBeingProcessed = theFilesArray[ii];
                if (fileBeingProcessed.indexOf("-") > -1) {
                    System.out.println("FOUND");
                }
                fileBeingProcessed = fileBeingProcessed.replaceAll("-", "\\-");
                if (fileBeingProcessed.indexOf("-") > -1) {
                    System.out.println("FOUND");
                }
                System.out.println("THE FILE BEING PROCESSED IS -> " + fileBeingProcessed);
                InputStream theInputStream = new FileInputStream(fileBeingProcessed);
                System.out.println("1");
                ZipEntry theZipEntry = new ZipEntry(theFilesArray[ii].replace(File.separatorChar,
                    '/'));
                System.out.println("2");
                zipOutputStream.putNextEntry(theZipEntry);
                System.out.println("3");
                int theLength = 0;
                System.out.println("4");
                while ((theLength = theInputStream.read(theByteArray)) != -1) {
                    System.out.println("1sdfsdfs");
                    zipOutputStream.write(theByteArray, 0, theLength);
                }
                System.out.println("5");
                zipOutputStream.closeEntry();
                System.out.println("6");
                theInputStream.close();
            }
        }
        zipOutputStream.close();
    }

}// class
