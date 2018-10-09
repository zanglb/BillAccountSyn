package com.chinaunicom.credit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCommonDao;
import com.chinaunicom.credit.dao.impl.BillAccountCommonDaoImpl;
import com.chinaunicom.credit.pojo.Region;
import com.chinaunicom.credit.service.BillAccountCommonService;
import com.chinaunicom.credit.service.BillAccountCompareService;
import com.chinaunicom.credit.service.BillAccountSynService;
import com.chinaunicom.credit.service.impl.BillAccountCommonServiceImpl;
import com.chinaunicom.credit.service.impl.BillAccountCompareServiceImpl;
import com.chinaunicom.credit.service.impl.BillAccountSynServiceImpl;
import com.chinaunicom.credit.util.CollectionUtils;

public class BillAccountMain {
	
	private static Logger logger = LoggerFactory.getLogger(BillAccountMain.class);
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(null == args) {
			logger.info("Parameter is wrong, please check!");
			return;
		}
		
		logger.info("Parameter is {}",args[0]);
		
		BillAccountCommonDao billAccountCommonDao = new BillAccountCommonDaoImpl();
		boolean isHaveConnection = billAccountCommonDao.testConnection();
		
		if(isHaveConnection) {
			List<Region> allRegions = billAccountCommonDao.getAllRegion();
			if(CollectionUtils.isNotEmpty(allRegions)) {
				if("1".equals(args[0])) {
					BillAccountSynService billAccountSynService = new BillAccountSynServiceImpl();
				    billAccountSynService.synBillAccountData(allRegions);
				} else if("2".equals(args[0])) {
					BillAccountCompareService billAccountCompareService = new BillAccountCompareServiceImpl();
					billAccountCompareService.billAccountCompare(allRegions);
				} else if("3".equals(args[0])){
					BillAccountCommonService billAccountCommonService = new BillAccountCommonServiceImpl();
					billAccountCommonService.dropExpiredTables(allRegions);
				} else {
					logger.info("Parameter is wrong, please check!");
				}
			}
			
		} else {
			logger.error("No mysql connetion avaliable, please check.");
		}
	}
}
