package com.chinaunicom.credit.dao;

import java.util.List;

/**
 * 同步静态账单Dao层接口
 * 
 * @author zanglb
 *
 */
public interface BillAccountSynDao {
	
	public void insertSynLog(String tableName, String status);
	
	public void updateSynLog(String tableName, String status);
	
	public void deleteSynLog(List<String> tableNameList);
}
