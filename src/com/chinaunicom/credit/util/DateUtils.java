package com.chinaunicom.credit.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static String getDDValue() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		return sdf.format(new Date());
	}
	
	public static String getYYYYMMValue() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		return sdf.format(new Date());
	}
	
	public static String getYYYYMMDDValue() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(new Date());
	}
	
	public static String getYYYYMMValue(int offset) {
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();    
		calendar.setTime(now);
		calendar.add(Calendar.MONDAY, offset);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		return sdf.format(calendar.getTime());
	}
}
