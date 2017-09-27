/*
 * DocumentInfo.java
 *
 * Created on September 23, 2007, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.objects;

import java.io.Serializable;

/**
 * 
 * @author Venkat
 */
public class DocumentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private long ecomm;

    private String documentId;

    private String documentDescription;

    private String documentType;

    private String templateName;

    private String fileName;

    public DocumentInfo(long ecomm, String documentId, String documentDescription,
            String documentType, String templateName) {
        super();
        this.ecomm = ecomm;
        this.documentId = documentId;
        this.documentDescription = documentDescription;        
        this.documentType = documentType;
        this.templateName = templateName;
        initFileName();
    }

    /**
     * 
     */
    private void initFileName() {
        if ("<PULL>".equalsIgnoreCase(getDocumentType())){
            this.fileName = "";
        }else{
            this.fileName = "wf-" + ecomm + "-" + documentId + ".pdf";
        }
    }

    
    public String getDocumentDescription() {
        return documentDescription;
    }

    
    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    
    public String getDocumentId() {
        return documentId;
    }

    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
        initFileName();
    }
    
    public String getDocumentType() {
        return documentType;
    }

    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    
    public long getEcomm() {
        return ecomm;
    }

    
    public void setEcomm(long ecomm) {
        this.ecomm = ecomm;
        initFileName();
    }

    
    public String getFileName() {
        return fileName;
    }

    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    
    public String getTemplateName() {
        return templateName;
    }

    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    

}
