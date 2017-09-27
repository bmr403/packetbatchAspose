package com.matrix.intake.ecomm.packet.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringUtil {
	public static String formatDate(java.sql.Date date){
		//String str = null;
		return date!=null?toTheString(date):"";
	}

	public static String formatDateU(java.sql.Date date){
		//String str = null;
		return date!=null?toTheString(date):"unknown";
	}

	public static String toTheString (java.sql.Date date) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date(date.getTime()));
	  
	  int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);

	        char buf[] = "00-00-2000".toCharArray();
	        buf[0] =Character.forDigit(month/10,10); 
	        buf[1] =  Character.forDigit(month%10,10);
	        buf[6] = Character.forDigit(year/1000,10);
	        buf[7] = Character.forDigit((year/100)%10,10); 
	        buf[8] = Character.forDigit((year/10)%10,10); 
	        buf[9] = Character.forDigit(year%10,10);
	        buf[3] = Character.forDigit(day/10,10);
	        buf[4] = Character.forDigit(day%10,10);
			


		return new String(buf);
	    }
	
	
	
	
}