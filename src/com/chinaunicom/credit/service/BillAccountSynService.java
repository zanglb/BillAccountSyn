package com.chinaunicom.credit.service;

import java.util.List;

import com.chinaunicom.credit.pojo.Region;

/**
 * BillAccount数据同步接口类
 * 
 * @author zanglb
 *
 */
public interface BillAccountSynService {

	/**
	 * 同步BillAccount数据到Mysql
	 */
	public void synBillAccountData(List<Region> allRegions);
}
