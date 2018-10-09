package com.chinaunicom.credit.dao;

import java.util.List;

import com.chinaunicom.credit.pojo.BillAccountCompareResult;

public interface BillAccountCompareDao {

	/**
	 * 查询本月和上个月账目项比对结果
	 * 
	 * @param lastMonthTableName
	 * @param curMonthTableName
	 * @return
	 */
	public List<BillAccountCompareResult> query(String lastMonthTableName, String curMonthTableName, String provCode);
	
}
