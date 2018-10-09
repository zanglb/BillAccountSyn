package com.chinaunicom.credit.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCommonDao;
import com.chinaunicom.credit.dao.BillAccountSynDao;
import com.chinaunicom.credit.dao.impl.BillAccountCommonDaoImpl;
import com.chinaunicom.credit.dao.impl.BillAccountSynDaoImpl;
import com.chinaunicom.credit.pojo.Region;
import com.chinaunicom.credit.service.BillAccountCommonService;
import com.chinaunicom.credit.util.CollectionUtils;
import com.chinaunicom.credit.util.PropertiesUtil;

public class BillAccountCommonServiceImpl implements BillAccountCommonService {

	private static Logger logger = LoggerFactory.getLogger(BillAccountCommonServiceImpl.class);
			
	@Override
	public void dropExpiredTables(List<Region> allRegions) {
		BillAccountCommonDao billAccountCommonDao = new BillAccountCommonDaoImpl();
		BillAccountSynDao billAccountDao = new BillAccountSynDaoImpl();
		String keepDaysValue = PropertiesUtil.getValue("billaccountDataKeepDays");
		int keepDays = Integer.parseInt(keepDaysValue);

		String regionCodeRegexValue = getRegionCodeRegexValue(allRegions);
		
		// 获取当前region下所有的billaccount数据表
		String billAccountTableRegexValue = getBillAccountTableRegexValue(regionCodeRegexValue);
		List<String> tableNames = billAccountCommonDao.getTableNamesByRegex(billAccountTableRegexValue);
		// 检查哪些表超过保存周期
		List<String> expiredTableNames = getExpiredTableNames(tableNames, keepDays);
		
		// drop超期的表
		if (null != expiredTableNames && !expiredTableNames.isEmpty()) {
			billAccountCommonDao.dropTables(expiredTableNames);
			logger.info("Drop expired talbes fro bill account syn.tables={}.", expiredTableNames.toString());
			billAccountDao.deleteSynLog(expiredTableNames);
			logger.info("Delete billaccount syn log, tableList={}", expiredTableNames.toString());
		}
		
		String billAcctCompareTableRegexValue = getBillAcctCompareTableRegexValue(regionCodeRegexValue);
		List<String> billAcctCompareTableNames = billAccountCommonDao.getTableNamesByRegex(billAcctCompareTableRegexValue);
		
		expiredTableNames = getExpiredTableNames4Compare(billAcctCompareTableNames);
		if(CollectionUtils.isNotEmpty(expiredTableNames)) {
			billAccountCommonDao.dropTables(expiredTableNames);
			logger.info("Drop expired talbes for bill account compare.tables={}.", expiredTableNames.toString());
		}
		
	}
	
	private String getRegionCodeRegexValue(List<Region> allRegions) {
		String regionCode = null;
		StringBuffer regionCodeRegexValue = new StringBuffer();
		regionCodeRegexValue.append('(');
		for (Region region : allRegions) {
			regionCode = region.getRegionCode();
			regionCodeRegexValue.append(regionCode);
			regionCodeRegexValue.append('|');
		}
		regionCodeRegexValue.deleteCharAt(regionCodeRegexValue.length() - 1);
		regionCodeRegexValue.append(')');
		
		return regionCodeRegexValue.toString();
	}
	
	private String getBillAccountTableRegexValue(String regionCodeRegexValue) {
		StringBuffer tableRegexValue = new StringBuffer();
		tableRegexValue.append(regionCodeRegexValue);
		tableRegexValue.append("_bill_account_");
		tableRegexValue.append("([1-9][0-9][0-9][0-9](0[1-9]|1[1-2])(0[1-9]|1[0-9]|2[0-9]|3[0-1]))$");
		
		return tableRegexValue.toString();
	}
	
	private String getBillAcctCompareTableRegexValue(String regionCodeRegexValue) {
		StringBuffer tableRegexValue = new StringBuffer();
		tableRegexValue.append(regionCodeRegexValue);
		tableRegexValue.append("_bill_account_");
		tableRegexValue.append("([1-9][0-9][0-9][0-9](0[1-9]|1[1-2]))");
		tableRegexValue.append("_compare");
		
		return tableRegexValue.toString();
	}
	
	/**
	 * 获取超过保存周期的数据表名
	 * 
	 * @param tableNames 指定region下所有的数据表名集合
	 * @return
	 */
	private List<String> getExpiredTableNames(List<String> tableNames, int keepDays){
		
		if(CollectionUtils.isNotEmpty(tableNames)) {
			//从名称中截取日期相关的字符串
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			
			int keepDaysValue = 0 - keepDays;
			calendar.add(Calendar.DAY_OF_MONTH, keepDaysValue);
			Date minDate = calendar.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			String minDateValue = sdf.format(minDate);
			
			List<String> expiredTableNames = new ArrayList<String>();
			for(String tableName : tableNames) {
				String[] elements = tableName.split("_");
				String date = elements[elements.length -1] + elements[1];
				
				if(date.compareTo(minDateValue) <= 0) {
					expiredTableNames.add(tableName);
				}
			}
			
			return expiredTableNames;
		}
		
		return null;
	}
	
	private List<String> getExpiredTableNames4Compare(List<String> tableNames){
		if(CollectionUtils.isNotEmpty(tableNames)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			
			calendar.add(Calendar.MONTH, -2);
			Date minDate = calendar.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
			String minDateValue = sdf.format(minDate);
			
			List<String> expiredTableNames = new ArrayList<String>();
			for(String tableName : tableNames) {
				String[] elements = tableName.split("_");
				String date = elements[elements.length -1] + elements[1];
				
				if(date.compareTo(minDateValue) <= 0) {
					expiredTableNames.add(tableName);
				}
			}
			
			return expiredTableNames;
		}
		
		return null;
	}

}
