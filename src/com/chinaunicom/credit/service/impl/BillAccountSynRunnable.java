package com.chinaunicom.credit.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCommonDao;
import com.chinaunicom.credit.dao.BillAccountSynDao;
import com.chinaunicom.credit.dao.impl.BillAccountCommonDaoImpl;
import com.chinaunicom.credit.dao.impl.BillAccountSynDaoImpl;
import com.chinaunicom.credit.util.DateUtils;
import com.chinaunicom.credit.util.FileUtils;
import com.chinaunicom.credit.util.PropertiesUtil;

/**
 * BillAccount数据同步线程类
 * 
 * @author zanglb
 *
 */
public class BillAccountSynRunnable implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(BillAccountSynRunnable.class);
	
	private static final String BILL_ACCOUNT = "BILL_ACCOUNT";
	
	private static final char UNDERLINE = '_';
	
	private static final String BILLACCOUNT_SYN_STATUS_RUNNING = "1";
	
	private static final String BILLACCOUNT_SYN_STATUS_FINISH = "2";
	
	private String rootPath = null;
	
	private String regionCode = null;
	
	public BillAccountSynRunnable(String regionCode, String rootPath) {
		this.regionCode = regionCode;
		this.rootPath = rootPath;
	}

	@Override
	public void run() {
		if(StringUtils.isNotEmpty(regionCode) 
				&& StringUtils.isNotBlank(regionCode)
				&& StringUtils.isNotEmpty(rootPath)
				&& StringUtils.isNotBlank(rootPath)) {

			logger.info("Region {} syn billaccount start.", regionCode);
			
			String fullPath = rootPath + File.separator + getSubDirectoryName(regionCode);
			List<File> fileList = FileUtils.getFileList(fullPath);
			if(null != fileList) {
				
				BillAccountSynDao billAccountDao = new BillAccountSynDaoImpl();
				BillAccountCommonDao billAccountCommonDao = new BillAccountCommonDaoImpl();
				
				//检查当天对应的表是否存在
				String tableName = getTableName(regionCode);
				boolean isTableExists = billAccountCommonDao.isTableExists(tableName);
				logger.info("Region {} check table {} {}.", regionCode, tableName, isTableExists ? "exists" : "not exist");
				
				boolean isSuccess = false;
				if(!isTableExists) {
					//如果不存在创建表
					String createSql = generateCreateTableSql(tableName);
					isSuccess = billAccountCommonDao.createTable(createSql);
					logger.info("Region {} table {} not exist, create table {}.", regionCode, tableName, isSuccess ? "success" : "failure");
				} else {
					//如果表存在，清空表中的数据
					isSuccess = billAccountCommonDao.truncateTable(tableName);
					logger.info("Region {} Table {} exists.truncate table {}.", regionCode, tableName, isSuccess ? "success" : "failure");
				}
				
				if(isSuccess) {
					//导入数据				
					long t1 = System.currentTimeMillis();
					
					//插入状态
					billAccountDao.insertSynLog(tableName, BILLACCOUNT_SYN_STATUS_RUNNING);
					for(File file : fileList) {
						billAccountCommonDao.loadDataToMysql(tableName, file.getAbsolutePath().replaceAll("\\\\", "/"));
					}
					long t2 = System.currentTimeMillis();
					logger.info("Region {} syn billaccount data end.cost {}s", regionCode, (t2-t1)/1000);
					
					billAccountDao.updateSynLog(tableName, BILLACCOUNT_SYN_STATUS_FINISH);
				}
			}
			
			logger.info("Region {} syn billaccount end.", regionCode);
		}
	}

	/**
	 * 获取存储Hive文件的子目录名称
	 * 
	 * @param regionCode
	 * @return
	 */
	private String getSubDirectoryName(String regionCode) {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(regionCode).append(UNDERLINE);
		buffer.append(DateUtils.getDDValue()).append(UNDERLINE);
		buffer.append(BILL_ACCOUNT).append(UNDERLINE).append(DateUtils.getYYYYMMValue());
		
		return buffer.toString();
	}
	
	/**
	 * 生成表名
	 * 
	 * @param regionCode
	 * @return
	 */
	private String getTableName(String regionCode) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(regionCode).append(UNDERLINE);
		buffer.append(BILL_ACCOUNT).append(UNDERLINE);
		buffer.append(DateUtils.getYYYYMMDDValue());
		
		return buffer.toString();
	}
	
	private String generateCreateTableSql(String tableName) {
		String createSql = PropertiesUtil.getValue("billaccountSynCreateTableSql");

		if (null != createSql) {
			createSql = createSql.replaceAll("#tablename#", tableName);

			return createSql;
		} else {
			StringBuffer createSqlValue = new StringBuffer();
			createSqlValue.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
			createSqlValue.append(" (acct_id varchar(16) NOT NULL,");
			createSqlValue.append(" fee decimal(11,0) DEFAULT NULL,");
			createSqlValue.append(" PRIMARY KEY (acct_id))");
			createSqlValue.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8");

			return createSqlValue.toString();
		}
		
	}
	
}
