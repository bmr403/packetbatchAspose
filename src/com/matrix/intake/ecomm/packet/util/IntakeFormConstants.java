package com.matrix.intake.ecomm.packet.util;

import java.util.Hashtable;

public final class IntakeFormConstants {

    static Hashtable formDescriptionsHashtable = null;
    
    public static Hashtable getFormDescriptionsHashtable()
    {
    	    //Hashtable formDescriptionsHashtable = null;
    	    
    		if (formDescriptionsHashtable == null)
    		{
    			formDescriptionsHashtable =  new Hashtable();
		    	
    					    	 
    			formDescriptionsHashtable.put("OTHER", "OTHER");
    			
		    	
    			formDescriptionsHashtable.put("DISMED", "DISABILITY MEDICAL");
				
    			formDescriptionsHashtable.put("DISMEDFMLA","DISABILITY MEDICAL FMLA");
				
    			formDescriptionsHashtable.put("DISPREG", "DISABILITY PREGNANCY");
				
    			formDescriptionsHashtable.put("DISPREGFMLA", "DISABILITY PREGNANCY FMLA");
    			
    			
				
    			formDescriptionsHashtable.put("FMLACHILD", "FMLA CHILD");
    			
    			formDescriptionsHashtable.put("FMLAFAMMEM", "FMLA FAMILY MEMBER");
    			
       			formDescriptionsHashtable.put("FMLAPREG", "FMLA PREGNANCY");
	
    			formDescriptionsHashtable.put("FMLASELF", "FMLA SELF");

				
    			
    			formDescriptionsHashtable.put("LTDMED", "LTD MEDICAL");

    			formDescriptionsHashtable.put("HPM", "HPM");				
    			
    			formDescriptionsHashtable.put("PFLCHILD", "PFL CHILD");
				
    			formDescriptionsHashtable.put("PFLFAMMEM", "PFL FAMILY MEMBER");
    			
				
    			formDescriptionsHashtable.put("WC", "WORKERS COMP");	
				

				
    			
				
    		}	
		    	return formDescriptionsHashtable;
    }
}