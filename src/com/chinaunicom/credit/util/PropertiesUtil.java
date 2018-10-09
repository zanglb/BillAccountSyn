package com.chinaunicom.credit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties文件工具类
 * 
 * @author zanglb
 *
 */
public class PropertiesUtil {

	private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	
	public static Properties properties = new Properties();
	
	private PropertiesUtil() {
		
	}
	
	/**
	 * 加载blockproperties文件内容
	 */
	private static void loadData() {
		InputStream input = null;
		try {
			input = PropertiesUtil.class.getClassLoader().getResourceAsStream("blockstop.properties");
			properties.load(input);
		} catch (IOException e) {
			logger.error("Load blockstop.properties error.", e);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					input = null;
				}
			}
		}
	}
	
	/**
	 * 从blockproperties文件中获取指定key的值
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		
		if(properties.isEmpty()) {
			loadData();
		}
		
		if(!properties.isEmpty() && properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		
		return null;
	}
	
	public static Properties loadDataFromFile(String fileName) {
		InputStream input = null;
		try {
			input = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
			
			Properties p = new Properties();
			p.load(input);
			return p;
		} catch (IOException e) {
			logger.error("Load blockstop.properties error.", e);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					input = null;
				}
			}
		}
		
		return null;
	}
	
}
