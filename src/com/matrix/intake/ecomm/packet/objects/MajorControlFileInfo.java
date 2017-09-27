/*
 * MajorControlFileInfo.java
 *
 * Created on September 24, 2007, 10:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.matrix.intake.ecomm.packet.objects;

/**
 * 
 * @author Venkat
 */
public class MajorControlFileInfo {

	private static final String EMPTY = "";
	private String theClientName;
	private String theClientCode;
	private String theLastName;
	private String theFirstName;
	private String theEcommName;
	private String theEcommId;
	private String theSetId;
	private String theNbrPull;
	private String theNbrPrint;
	private String theControlFileName;
	private String theHDR;

	/** Creates a new instance of MajorControlFileInfo */
	public MajorControlFileInfo() {
		theClientName = EMPTY;
		theClientCode = EMPTY;
		theLastName = EMPTY;
		theFirstName = EMPTY;
		theEcommName = "PACKET";
		theEcommId = EMPTY;
		theSetId = EMPTY;
		theNbrPull = EMPTY;
		theNbrPrint = EMPTY;
		theControlFileName = EMPTY;
		theHDR = "HDR|control_filename|nbr_print|nbr_pull|set_id|ecomm_id|ecomm_name|first_name|last_name|client_code|client_name|";
	}

	public void setTheHDR(String theHDR) {
		this.theHDR = theHDR;
	}

	public String getTheHDR() {
		return this.theHDR;
	}

	public void setTheControlFileName(String theControlFileName) {
		this.theControlFileName = theControlFileName;
	}

	public String getTheControlFileName() {
		return this.theControlFileName;
	}

	public void setTheNbrPrint(String theNbrPrint) {
		this.theNbrPrint = theNbrPrint;
	}

	public String getTheNbrPrint() {
		return this.theNbrPrint;
	}

	public void setTheNbrPull(String theNbrPull) {
		this.theNbrPull = theNbrPull;
	}

	public String getTheNbrPull() {
		return this.theNbrPull;
	}

	public void setTheSetId(String theSetId) {
		this.theSetId = theSetId;
	}

	public String getTheSetId() {
		return this.theSetId;
	}

	public void setTheEcommId(String theEcommId) {
		this.theEcommId = theEcommId;
	}

	public String getTheEcommId() {
		return this.theEcommId;
	}

	public void setTheEcommName(String theEcommName) {
		this.theEcommName = theEcommName;
	}

	public String getTheEcommName() {
		return this.theEcommName;
	}

	public void setTheFirstName(String theFirstName) {
		this.theFirstName = theFirstName;
	}

	public String getTheFirstName() {
		return this.theFirstName;
	}

	public void setTheLastName(String theLastName) {
		this.theLastName = theLastName;
	}

	public String getTheLastName() {
		return this.theLastName;
	}

	public void setTheClientCode(String theClientCode) {
		this.theClientCode = theClientCode;
	}

	public String getTheClientCode() {
		return this.theClientCode;
	}

	public void setTheClientName(String theClientName) {
		this.theClientName = theClientName;
	}

	public String getTheClientName() {
		return this.theClientName;
	}

}
