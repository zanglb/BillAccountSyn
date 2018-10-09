package com.chinaunicom.credit.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class FileUtils {
	
	/**
	 * 获取BillAccount的数据文件
	 * 
	 * @param regionCode
	 * @return
	 */
	public static List<File> getFileList(String filePath){
		File importDir = new File(filePath);
		
		if(importDir.exists() && importDir.isDirectory()) {
			File[] fileList = importDir.listFiles();
			if(ArrayUtils.isNotEmpty(fileList)) {
				List<File> hiveFileList = new ArrayList<File>();
				for(File file : fileList) {
					if(file.isFile() && !file.isHidden()) {
						hiveFileList.add(file);
					}
				}
				
				return hiveFileList;
			}
		}
		
		return null;
	}
}
