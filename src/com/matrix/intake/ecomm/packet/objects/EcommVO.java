package com.matrix.intake.ecomm.packet.objects;

import java.io.Serializable;

/**
 * 
 * @author kbobba
 * 
 */
public class EcommVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static String fileSep = System.getProperty("file.separator");

    private Long ecomm;

    private Long cliCode;

    private String ecommCreatedDateStr;

    private String ecommCreatedYear;

    private String ecommCreatedMonth;

    private Long intake;

    private String mailingAddressFlag;

    private String intakeYearMonthPath;

    /** 
     * @param ecomm
     * @param ecommCreatedDateStr
     */
    public EcommVO(long ecomm, String ecommCreatedDateStr) {
        this.ecomm = ecomm;        
        this.ecommCreatedDateStr = ecommCreatedDateStr;
        initYearMonth(ecommCreatedDateStr);
    }

    
    /** 
     * @param ecomm
     * @param cliCode
     * @param ecommCreatedDateStr
     */
    public EcommVO(long ecomm, Long cliCode, String ecommCreatedDateStr) {
        this(ecomm, ecommCreatedDateStr);
        this.cliCode = cliCode;
    }

    /** 
     * @param ecomm
     * @param cliCode
     * @param ecommCreatedDateStr
     * @param intake
     * @param mailingAddressFlag
     * @param intakeCreatedDateStr
     */
    public EcommVO(Long ecomm, Long cliCode, String ecommCreatedDateStr, Long intake,
            String mailingAddressFlag, String intakeCreatedDateStr) {
        this(ecomm, cliCode, ecommCreatedDateStr);
        this.intake = intake;
        this.mailingAddressFlag = mailingAddressFlag;
        initIntakeYearMonthPath(intakeCreatedDateStr);
    }

    /**
     * @param intakeCreatedDateStr
     */
    private void initIntakeYearMonthPath(String intakeCreatedDateStr) {
        if (null != intakeCreatedDateStr) {
            String[] resultDatesArray = intakeCreatedDateStr.split("-");
            intakeYearMonthPath = resultDatesArray[0] + fileSep + resultDatesArray[1];
        }
    }

    /**
     * @param ecommCreatedDateStr2
     */
    private void initYearMonth(String ecommCreatedDateStr2) {
        if (null != ecommCreatedDateStr) {
            String[] resultDatesArray = ecommCreatedDateStr.split("-");
            ecommCreatedYear = resultDatesArray[0];
            ecommCreatedMonth = resultDatesArray[1];
        }
    }// initYearMonth

    public Long getEcomm() {
        return ecomm;
    }

    public void setEcomm(Long ecomm) {
        this.ecomm = ecomm;
    }

    public Long getCliCode() {
        return cliCode;
    }

    public void setCliCode(Long cliCode) {
        this.cliCode = cliCode;
    }

    public String getEcommCreatedDateStr() {
        return ecommCreatedDateStr;
    }

    public void setEcommCreatedDateStr(String ecommCreatedDateStr) {
        this.ecommCreatedDateStr = ecommCreatedDateStr;
    }

    public String getEcommCreatedMonth() {
        return ecommCreatedMonth;
    }

    public void setEcommCreatedMonth(String ecommCreatedMonth) {
        this.ecommCreatedMonth = ecommCreatedMonth;
    }

    public String getEcommCreatedYear() {
        return ecommCreatedYear;
    }

    public void setEcommCreatedYear(String ecommCreatedYear) {
        this.ecommCreatedYear = ecommCreatedYear;
    }

    public Long getIntake() {
        return intake;
    }

    public void setIntake(Long intake) {
        this.intake = intake;
    }

    public String getMailingAddressFlag() {
        return mailingAddressFlag;
    }

    public void setMailingAddressFlag(String mailingAddressFlag) {
        this.mailingAddressFlag = mailingAddressFlag;
    }

    /*
     * Ex. 2011/03 Year 2011 and Month is 03
     */
    public String getIntakeYearMonthPath() {
        return intakeYearMonthPath;
    }

    // public void setIntakeYearMonthPath(String intakeYearMonthPath) {
    // this.intakeYearMonthPath = intakeYearMonthPath;
    // }

}// class
