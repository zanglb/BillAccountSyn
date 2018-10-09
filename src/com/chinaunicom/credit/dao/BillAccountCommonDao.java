package com.chinaunicom.credit.dao;

import java.util.List;

import com.chinaunicom.credit.pojo.Region;

public interface BillAccountCommonDao {

	/**
	 * 测试数据库连接
	 * @return
	 */
	public boolean testConnection();
	
	/**
	 * 检查表是否存在
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean isTableExists(String tableName);
	
	/**
	 * 插入静态账单数据到Mysql
	 * 
	 * @param tableName
	 * @param filePath
	 */
	public void loadDataToMysql(String tableName, String filePath);
	
	/**
	 * 清空表中的数据
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean truncateTable(String tableName);
	
	/**
	 * 创建表
	 * @param createSql
	 * @return
	 */
	public boolean createTable(String createSql);
	
	/**
	 * 删除数据表
	 * 
	 * @param tableNames 数据表名集合
	 */
	public void dropTables(List<String> tableNames);
	
	/**
	 * 获取所有的Region信息
	 * 
	 * @return
	 */
	public List<Region> getAllRegion();
	
	/**
	 * 根据表的正则获取所有表名
	 * 
	 * @param regexValue
	 * @return
	 */
	public List<String> getTableNamesByRegex(String regexValue);
	
}
