/*
 * MinorControlFileInfo.java
 *
 * Created on September 24, 2007, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.objects;

import org.apache.log4j.Logger;

import com.matrix.intake.ecomm.packet.util.PacketConstants;

/**
 * 
 * @author Venkat
 */
public class MinorControlFileInfo {
    
    private static final String EMPTY = "";

	public static Logger log = Logger.getLogger(MinorControlFileInfo.class);

	private String internationalCountry;
	private String internaltionalAddress;
	private String internationalAddressFlag;
    
	private String returnAddress1;
    private String returnAddress2;
    private String returnState;
    private String returnZip;
    
	private String technicianName;
	private String technicianMail;
    
	private String intakeFirstDayUnableToWork;
    private String Doc15Dt;
	private String locRTW;
	private String matrixEventCreateDateTime;
	private String matrixTollFree;
	private String matrixPhoneNumber;
	private String matrixFaxNumber;

    private String matrixAddress1;
    private String matrixAddress2;
    private String matrixCity;
	private String matrixState;
    private String matrixZip;
    private String matrixCountry;
    
	private String intakeDOB;

    private String claimantFirstName;
	private String claimantLastName;
	private String claimantMiddleName;
	// TODO
	private String claimantSSN;

    private String intakeFirstName;
    private String intakeLastName;
    private String intakeHomeAddress1;
	private String intakeHomeAddress2;
    private String intakeHomeCity;
    private String intakeHomeState;
    private String intakeHomeZip;
    private String intakeHomeCountry;    

	private String addressType;
	private String clientName;
	private String clientCode;

	// From EF_FMLA_FAMILY_MEMBER
	private String memberFirstName;
	private String memberMiddleName;
	private String memberLastName;
	private String memberRelationship;
	private String memberDateOfBirth;

	//Auto cert Fax requirement
    private String phyName;
    private String claimNumber;
    private String intakeNumber;
    private String formName;

	
	/** Creates a new instance of MinorControlFileInfo */
	public MinorControlFileInfo() {
		internationalCountry = EMPTY;
		internaltionalAddress = EMPTY;
		internationalAddressFlag = EMPTY;
		
        // Below fields "return" fields seems that they are not being used.
        // To review and remove them later.
        returnAddress1 = "5225 Hellyer Ave #210";
        returnAddress2 = "San Jose";
        returnState = "CA";
        returnZip = "95138";
        
        technicianName = EMPTY;
		technicianMail = EMPTY;
		
        intakeFirstDayUnableToWork = EMPTY;
        locRTW=EMPTY;
        Doc15Dt=EMPTY;
		matrixEventCreateDateTime = EMPTY;
		
        matrixTollFree    = "(800) 980-1006";
		matrixPhoneNumber = "(408) 360-8370";
		matrixFaxNumber   = "(408) 360-9441";

        matrixAddress1 = "181 Metro Drive, Suite 300";
        matrixAddress2 = EMPTY;
        matrixCity = "San Jose";
        matrixState = PacketConstants.STATE.CA.toString();
        matrixZip = "95103";
        matrixCountry = PacketConstants.COUNTRY.USA.toString();
        
		intakeDOB = EMPTY;
		
        claimantLastName = EMPTY;
		claimantMiddleName = EMPTY;
		claimantFirstName = EMPTY;
		// TODO
		claimantSSN = EMPTY;
		
        intakeHomeAddress1 = EMPTY;
        intakeHomeAddress2 = EMPTY;
        intakeHomeCity = EMPTY;
        intakeHomeState = EMPTY;
        intakeHomeZip = EMPTY;
        intakeHomeCountry = EMPTY; 
        
        intakeLastName = EMPTY;
		intakeFirstName = EMPTY;
		
        addressType = "recepient";
		
        clientName = EMPTY;
		clientCode = EMPTY;		
        
		memberFirstName    = EMPTY;
		memberMiddleName   = EMPTY;
		memberLastName     = EMPTY;
		memberRelationship = EMPTY;
		memberDateOfBirth  = EMPTY;
	
        phyName = EMPTY;
        claimNumber=EMPTY;
        intakeNumber=EMPTY;
        formName=EMPTY;        
	}

	public boolean checkTheClassFields() {
		boolean theReturnValue = true;
        String logStr = "         1 " + internationalCountry + " 2 "
				+ internaltionalAddress + " 3 "
				+ internationalAddressFlag + " 4 " + returnZip + " 5 "
				+ returnState + " 6 " + returnAddress2 + " 7 "
				+ returnAddress1 + " 8 " + technicianName + " 9 "
				+ technicianMail + " 10 " + intakeFirstDayUnableToWork
				+ " 11 " + matrixEventCreateDateTime + " 12 "
				+ matrixTollFree + " 13 " + matrixPhoneNumber + " 14 "
				+ matrixFaxNumber + " 15 " + matrixZip + " 16 "
				+ matrixState + " 17 " + matrixCity + " 18 "
				+ matrixAddress2 + " 19 " + matrixAddress1 + " 20 "
				+ intakeDOB + " 21 " + claimantLastName + " 21 "
				+ claimantMiddleName + " 23 " + claimantFirstName
				+ " 24 " + intakeHomeZip + " 25 " + intakeHomeState
				+ " 26 " + intakeHomeCity + " 27 " + intakeHomeAddress2
				+ " 28 " + intakeHomeAddress1 + " 29 " + intakeLastName
				+ " 30 " + intakeFirstName + " 31 " + addressType
				+ " 32 " + clientName + " 33" + clientCode

				+ " 34 " + memberFirstName
				+ " 35 " + memberMiddleName
				+ " 36 " + memberLastName
				+ " 37 " + memberRelationship
				+ " 38 " + memberDateOfBirth
				// TODO
				+ " 39 " + claimantSSN +" 40 "+locRTW
				+" 41 "+phyName+ " 42 "+ claimNumber +
				" 43 "+intakeNumber +" 44 "+ formName;
        log.info(logStr);

		if (internationalCountry == null || internaltionalAddress == null
				|| internationalAddressFlag == null || returnZip == null
				|| returnState == null || returnAddress2 == null
				|| returnAddress1 == null || technicianName == null
				|| technicianMail == null
				|| intakeFirstDayUnableToWork == null
				|| matrixEventCreateDateTime == null
				|| matrixTollFree == null || matrixPhoneNumber == null
				|| matrixFaxNumber == null || matrixZip == null
				|| matrixState == null || matrixCity == null
				|| matrixAddress2 == null || matrixAddress1 == null
				|| intakeDOB == null || claimantLastName == null
				|| claimantMiddleName == null
				|| claimantFirstName == null || intakeHomeZip == null
				|| intakeHomeState == null || intakeHomeCity == null
				|| intakeHomeAddress2 == null
				|| intakeHomeAddress1 == null || intakeLastName == null
				|| intakeFirstName == null || addressType == null
				|| clientName == null || clientCode == null
				
				|| memberFirstName  == null				
				|| memberMiddleName == null
				|| memberLastName     == null
				|| memberRelationship == null
                || memberDateOfBirth  == null || locRTW==null
                || phyName == null || claimNumber == null 
                || intakeNumber == null
                || formName == null
                // TODO
                || null == claimantSSN) {
			theReturnValue = false;
		}

		return theReturnValue;
	}
	
	

	public String getMemberFirstName() {
		return memberFirstName;
	}

	public void setMemberFirstName(String memberFirstName) {
		this.memberFirstName = memberFirstName;
	}

	public String getMemberMiddleName() {
		return memberMiddleName;
	}

	public void setMemberMiddleName(String memberMiddleName) {
		this.memberMiddleName = memberMiddleName;
	}

	public String getMemberLastName() {
		return memberLastName;
	}

	public void setMemberLastName(String memberLastName) {
		this.memberLastName = memberLastName;
	}

	public String getMemberRelationship() {
		return memberRelationship;
	}

	public void setMemberRelationship(String memberRelationship) {
		this.memberRelationship = memberRelationship;
	}

	public String getMemberDateOfBirth() {
		return memberDateOfBirth;
	}

	public void setMemberDateOfBirth(String memberDateOfBirth) {
		this.memberDateOfBirth = memberDateOfBirth;
	}

	public void setClientCode(String theClientCode) {
		this.clientCode = theClientCode;
	}

	public String getClientCode() {
		return this.clientCode;
	}

	public void setClientName(String theClientName) {
		this.clientName = theClientName;
	}

	public String getClientName() {
		return this.clientName;
	}

	public void setAddressType(String theAddressType) {
		this.addressType = theAddressType;
	}

	public String getAddressType() {
		return this.addressType;
	}

	public void setIntakeFirstName(String theIntakeFirstName) {
		this.intakeFirstName = theIntakeFirstName;
	}

	public String getIntakeFirstName() {
		return this.intakeFirstName;
	}

	public void setIntakeLastName(String theIntakeLastName) {
		this.intakeLastName = theIntakeLastName;
	}

	public String getIntakeLastName() {
		return this.intakeLastName;
	}

	public void setIntakeHomeAddress1(String theIntakeHomeAddress1) {
		this.intakeHomeAddress1 = theIntakeHomeAddress1;
	}

	public String getIntakeHomeAddress1() {
		return this.intakeHomeAddress1;
	}

	public void setIntakeHomeAddress2(String theIntakeHomeAddress2) {
		this.intakeHomeAddress2 = theIntakeHomeAddress2;
	}

	public String getIntakeHomeAddress2() {
		return this.intakeHomeAddress2;
	}

	public void setIntakeHomeCity(String theIntakeHomeCity) {
		this.intakeHomeCity = theIntakeHomeCity;
	}

	public String getIntakeHomeCity() {
		return this.intakeHomeCity;
	}

	public void setIntakeHomeState(String theIntakeHomeState) {
		this.intakeHomeState = theIntakeHomeState;
	}

	public String getIntakeHomeState() {
		return this.intakeHomeState;
	}

	public void setIntakeHomeZip(String theIntakeHomeZip) {
		this.intakeHomeZip = theIntakeHomeZip;
	}

	public String getIntakeHomeZip() {
		return this.intakeHomeZip;
	}
    
    public String getIntakeHomeCountry() {
        return intakeHomeCountry;
    }
    
    public void setIntakeHomeCountry(String intakeHomeCountry) {
        this.intakeHomeCountry = intakeHomeCountry;
    }

    public void setClaimantFirstName(String theClaimantFirstName) {
		this.claimantFirstName = theClaimantFirstName;
	}

	public String getClaimantFirstName() {
		return this.claimantFirstName;
	}

	public void setClaimantMiddleName(String theClaimantMiddleName) {
		this.claimantMiddleName = theClaimantMiddleName;
	}

	public String getClaimantMiddleName() {
		return this.claimantMiddleName;
	}

	public void setClaimantLastName(String theClaimantLastName) {
		this.claimantLastName = theClaimantLastName;
	}

	public String getClaimantLastName() {
		return this.claimantLastName;
	}

	// TODO
	public String getClaimantSSN() {
		return claimantSSN;
	}

	public void setClaimantSSN(String claimantSSN) {
		this.claimantSSN = claimantSSN;
	}

	public void setIntakeDOB(String theIntakeDOB) {
		this.intakeDOB = theIntakeDOB;
	}

	public String getIntakeDOB() {
		return this.intakeDOB;
	}

	public void setMatrixAddress1(String theMatrixAddress1) {
		this.matrixAddress1 = theMatrixAddress1;
	}

	public String getMatrixAddress1() {
		return this.matrixAddress1;
	}

	public void setMatrixAddress2(String theMatrixAddress2) {
		this.matrixAddress2 = theMatrixAddress2;
	}

	public String getMatrixAddress2() {
		return this.matrixAddress2;
	}

	public void setMatrixCity(String theMatrixCity) {
		this.matrixCity = theMatrixCity;
	}

	public String getMatrixCity() {
		return this.matrixCity;
	}

	public void setMatrixState(String theMatrixState) {
		this.matrixState = theMatrixState;
	}

	public String getMatrixState() {
		return this.matrixState;
	}

	public void setMatrixZip(String theMatrixZip) {
		this.matrixZip = theMatrixZip;
	}

	public String getMatrixZip() {
		return this.matrixZip;
	}
    
    public void setMatrixCountry(String matrixCountry) {
        this.matrixCountry = matrixCountry;
    }

    public String getMatrixCountry() {
        return matrixCountry;
    }

    public void setMatrixFaxNumber(String theMatrixFaxNumber) {
		this.matrixFaxNumber = theMatrixFaxNumber;
	}

	public String getMatrixFaxNumber() {
		return this.matrixFaxNumber;
	}

	public void setMatrixPhoneNumber(String theMatrixPhoneNumber) {
		this.matrixPhoneNumber = theMatrixPhoneNumber;
	}

	public String getMatrixPhoneNumber() {
		return this.matrixPhoneNumber;
	}

	public void setMatrixTollFree(String theMatrixTollFree) {
		this.matrixTollFree = theMatrixTollFree;
	}

	public String getMatrixTollFree() {
		return this.matrixTollFree;
	}

	public void setMatrixEventCreateDateTime(
			String theMatrixEventCreateDateTime) {
		this.matrixEventCreateDateTime = theMatrixEventCreateDateTime;
	}

	public String getMatrixEventCreateDateTime() {
		return this.matrixEventCreateDateTime;
	}

	public void setIntakeFirstDayUnableToWork(
			String theIntakeFirstDayUnableToWork) {
		this.intakeFirstDayUnableToWork = theIntakeFirstDayUnableToWork;
	}
	
	public String getIntakeFirstDayUnableToWork() {
		return this.intakeFirstDayUnableToWork;
	}

	public void setRTWDateTime(String RTWDate)
	{
		this.locRTW=RTWDate;
	}
	
public String getRTWDateTime()
{
	return this.locRTW;
}
	
public void setTechnicianMail(String theTechnicianMail) {
		this.technicianMail = theTechnicianMail;
	}

	public String getTechnicianMail() {
		return this.technicianMail;
	}

	public void setTechnicianName(String theTechnicianName) {
		this.technicianName = theTechnicianName;
	}

	public String getTechnicianName() {
		return this.technicianName;
	}

	public void setReturnAddress1(String theReturnAddress1) {
		this.returnAddress1 = theReturnAddress1;
	}

	public String getReturnAddress1() {
		return this.returnAddress1;
	}

	public void setReturnAddress2(String theReturnAddress2) {
		this.returnAddress2 = theReturnAddress2;
	}

	public String getReturnAddress2() {
		return this.returnAddress2;
	}

	public void setReturnState(String theReturnState) {
		this.returnState = theReturnState;
	}

	public String getReturnState() {
		return this.returnState;
	}

	public void setReturnZip(String theReturnZip) {
		this.returnZip = theReturnZip;
	}

	public String getReturnZip() {
		return this.returnZip;
	}

	public void setInternationalAddressFlag(
			String theInternationalAddressFlag) {
		this.internationalAddressFlag = theInternationalAddressFlag;
	}

	public String getInternationalAddressFlag() {
		return this.internationalAddressFlag;
	}

	public void setInternaltionalAddress(String theInternaltionalAddress) {
		this.internaltionalAddress = theInternaltionalAddress;
	}

	public String getInternaltionalAddress() {
		return this.internaltionalAddress;
	}

	public void setInternationalCountry(String theInternationalCountry) {
		this.internationalCountry = theInternationalCountry;
	}

	public String getInternationalCountry() {
		return this.internationalCountry;
	}
	
	public void setClaimantCurrDt15(String DocRetDate15) {
		this.Doc15Dt = DocRetDate15;
	}

	public String getClaimantCurrDt15() {
		return this.Doc15Dt;
	}	


// AutoFax Cert Requirements
	
    public String getPhyName()
    {
        return phyName;
    }

    public void setPhyName(String s)
    {
        phyName = s;
    }

    public String getIntakeNumber()
    {
        return intakeNumber;
    }

    public void setIntakeNumber(String s)
    {
        intakeNumber = s;
    }

    public String getClaimNumber()
    {
        return claimNumber;
    }

    public void setClaimNumber(String s)
    {
        claimNumber = s;
    }

    public String getFormName()
    {
        return formName;
    }

    public void setFormName(String s)
    {
        formName = s;
    }


}
