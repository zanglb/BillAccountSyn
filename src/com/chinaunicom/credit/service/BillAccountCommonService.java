package com.chinaunicom.credit.service;

import java.util.List;

import com.chinaunicom.credit.pojo.Region;

public interface BillAccountCommonService {

	public void dropExpiredTables(List<Region> allRegions);
}
