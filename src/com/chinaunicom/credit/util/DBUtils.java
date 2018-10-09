package com.chinaunicom.credit.util;

public class DBUtils {

	public static String generateLoadSql(String tableName, String filePath) {
		StringBuffer loadSqlBuffer = new StringBuffer();
		
		loadSqlBuffer.append("LOAD DATA LOCAL INFILE '");
		loadSqlBuffer.append(filePath);
		loadSqlBuffer.append("' INTO TABLE ");
		loadSqlBuffer.append(tableName);
		loadSqlBuffer.append(" FIELDS TERMINATED BY ','");
		
		return loadSqlBuffer.toString();
	}
}
