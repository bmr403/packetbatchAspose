/*
 * FOPUtil.java
 *
 * Created on September 24, 2007, 8:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.util;

// Java
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.log4j.Logger;

/**
 * 
 * @author Venkat
 */
public class FOPUtil {

    private static Logger log = Logger.getLogger(FOPUtil.class);

    /** Creates a new instance of FOPUtil */
    public FOPUtil() {
    }

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();

    /**
     * Converts an FO file to a PDF file using FOP
     * 
     * @param fo
     *            the FO file
     * @param pdf
     *            the target PDF file
     * @throws Exception
     * @throws IOException
     *             In case of an I/O problem
     * @throws FOPException
     *             In case of a FOP problem
     */
    public void convertFO2PDF(File foFile, File pdfFile) throws Exception {

        String err = "convertFO2PDF() foFile=" + foFile + " pdfFile=" + pdfFile;

        try {

            OutputStream outputStream = null;

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            // configure foUserAgent as desired

            // Setup output stream. Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            outputStream = new FileOutputStream(pdfFile);
            outputStream = new BufferedOutputStream(outputStream);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outputStream);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity
            // transformer
            Source src = null;
            try {
                // Setup input stream
                src = new StreamSource(foFile);
            } catch (Exception ee) {
                throw (ee);
            }

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            if (null != src) {
                // Start XSLT transformation and FOP processing
                transformer.transform(src, res);

                // Result processing
                FormattingResults foResults = fop.getResults();
                List pageSequences = foResults.getPageSequences();
                for (Iterator it = pageSequences.iterator(); it.hasNext();) {
                    PageSequenceResults pageSequenceResults = (PageSequenceResults) it.next();
                    log
                        .debug("PageSequence "
                                + (String.valueOf(pageSequenceResults.getID()).length() > 0
                                                                                           ? pageSequenceResults
                                                                                               .getID()
                                                                                           : "<no id>")
                                + " generated " + pageSequenceResults.getPageCount() + " pages.");
                }
                log.debug("Generated " + foResults.getPageCount() + " pages in total.");
            }
            outputStream.close();
        } catch (TransformerConfigurationException ex) {
            log.error(err, ex);
            ex.printStackTrace();
            throw new Exception(ex);
        } catch (FOPException ex) {
            log.error(err, ex);
            throw new Exception(ex);
        } catch (FileNotFoundException ex) {
            log.error(err, ex);
            throw new Exception(ex);
        } catch (TransformerFactoryConfigurationError ex) {
            log.error(err, ex);
            throw new Exception(ex);
        } catch (Exception ex) {
            log.error(err, ex);
            throw new Exception(ex);
        }
    }// convertFO2PDF

    /**
     * MergePacketMain method.
     * 
     * @param args
     *            command-line arguments
     */
    public static void main(String[] args) {
        try {
            log.info("FOP ExampleFO2PDF\n");
            log.info("Preparing...");

            // Setup input and output files
            File fofile = new File(
                    "C:\\Documents and Settings\\Venkat\\PacketGeneration\\fos\\a_generic_letter_ca.fo");
            // File fofile = new File(baseDir,
            // "../fo/pagination/franklin_2pageseqs.fo");
            File pdffile = new File(
                    "C:\\Documents and Settings\\Venkat\\PacketGeneration\\output.pdf");

            log.info("Transforming...");

            FOPUtil app = new FOPUtil();
            app.convertFO2PDF(fofile, pdffile);

            log.info("Success!");
        } catch (Exception e) {
            log.error("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", e);
            System.exit(-1);
        }
    }

}
