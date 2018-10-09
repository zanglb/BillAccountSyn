package com.chinaunicom.credit.service;

import java.util.List;

import com.chinaunicom.credit.pojo.Region;

/**
 * 每月2号账目项实现接口
 * 
 * @author zanglb
 *
 */
public interface BillAccountCompareService {

	/**
	 * 实现账目项比较,并将结果写入Excel
	 */
	public void billAccountCompare(List<Region> allRegions);
}
