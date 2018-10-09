package com.chinaunicom.credit.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCommonDao;
import com.chinaunicom.credit.dbpool.C3P0MySqlDataSourceFactory;
import com.chinaunicom.credit.pojo.Province;
import com.chinaunicom.credit.pojo.Region;
import com.chinaunicom.credit.util.DBUtils;

public class BillAccountCommonDaoImpl implements BillAccountCommonDao {

	private static Logger logger = LoggerFactory.getLogger(BillAccountCommonDaoImpl.class);
	
	@Override
	public boolean testConnection() {
		
		Connection conn= null;
		Statement stmt = null;
		
		boolean result = true;
		
		try {
			
			String sql = "select NOW()";
			
			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();
			
			stmt.executeQuery(sql);
			
		} catch (SQLException e) {
			result = false;
			logger.error("Query import status about province have errors!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
		
		return result;
	}
	
	@Override
	public boolean isTableExists(String tableName) {
		
		Connection conn= null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = C3P0MySqlDataSourceFactory.getConnection();
			String isExistsSql = "select TABLE_NAME from information_schema.tables where table_name = ?";
			stmt = conn.prepareStatement(isExistsSql);
			
			stmt.setString(1, tableName.toLowerCase());
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			logger.error("Query import status about province have errors!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(rs, stmt, conn);
		}
		
		return false;
	}
	
	@Override
	public boolean truncateTable(String tableName) {
		Connection conn = null;
		Statement stmt = null;

		boolean isSuccess = true;
		try {

			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();

			String truncateSql = "truncate table " + tableName;
			stmt.execute(truncateSql);
			
		} catch (SQLException e) {
			isSuccess = false;
			logger.error("Create table to db failed!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
		
		return isSuccess;
	}
	
	@Override
	public void dropTables(List<String> tableNames) {
		Connection conn= null;
		Statement stmt = null;
		
		try {
			
			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();
			
			String sql = null;
			for(String tableName : tableNames) {
				sql = "drop table " + tableName;
				stmt.execute(sql);
			}
			
		} catch (SQLException e) {
			logger.error("Query import status about province have errors!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
		
	}

	@Override
	public List<Region> getAllRegion() {
		Connection conn= null;
		Statement stmt = null;
		ResultSet rs = null;
		
		Map<String, Region> regions = new HashMap<String, Region>();
 		try {
			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();
			
			String sql = "select CODE_VALUE1,VALUE_NAME1,CODE_VALUE,VALUE_NAME from iap_p_code where PARAM_CODE='PROVINCE_CODE' order by CODE_VALUE1,CODE_VALUE";
			rs = stmt.executeQuery(sql);
			
			String regionCode = null;
			String regionName = null;
			String provCode = null;
			String provName = null;
			
			Region region = null;
			List<Province> provList = null;
			Province prov = null;
			while(rs.next()) {
				regionCode = rs.getString("CODE_VALUE1");
				regionName = rs.getString("VALUE_NAME1");
				provCode = rs.getString("CODE_VALUE");
				provName = rs.getString("VALUE_NAME");
				
				if(regions.containsKey(regionCode)) {
					prov = new Province();
					prov.setProvCode(provCode);
					prov.setProvName(provName);
					
					provList = regions.get(regionCode).getProvList();
					provList.add(prov);
				} else {
					region = new Region();
					region.setRegionCode(regionCode);
					region.setRegionName(regionName);
					
					provList = new ArrayList<Province>();
					prov = new Province();
					prov.setProvCode(provCode);
					prov.setProvName(provName);
					
					provList.add(prov);
					region.setProvList(provList);
					
					regions.put(regionCode, region);
				}
			}
		} catch (SQLException e) {
			logger.error("Query all region error!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(rs, stmt, conn);
		}
 		
		return new ArrayList<Region>(regions.values());
	}

	@Override
	public void loadDataToMysql(String tableName, String filePath) {
		
		Connection conn = null;
		Statement stmt = null;

		try {
			String loadSql = DBUtils.generateLoadSql(tableName.toLowerCase(), filePath);
			
			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();
			
			logger.info("Load {} to {} start.", filePath, tableName);
			long t1 = System.currentTimeMillis();
			stmt.executeQuery(loadSql);
			long t2 = System.currentTimeMillis();
			logger.info("Load {} to {} end.Cost {} ms", filePath, tableName, (t2-t1));
			
		} catch (SQLException e) {
			logger.error("InsertBillAccount error!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
	}

	@Override
	public List<String> getTableNamesByRegex(String regexValue) {
		Connection conn= null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			String sql = "select TABLE_NAME from information_schema.tables where table_name regexp ?";
			
			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, regexValue);
			
			rs = stmt.executeQuery();
			
			List<String> tableNames = new ArrayList<String>();
			while(rs.next()) {
				tableNames.add(rs.getString(1));
			}
			
			return tableNames;
		} catch (SQLException e) {
			logger.error("Query import status about province have errors!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(rs, stmt, conn);
		}
		
		return null;
	}

	@Override
	public boolean createTable(String createSql) {
		Connection conn = null;
		Statement stmt = null;

		boolean isSuccess = true;
		try {

			conn = C3P0MySqlDataSourceFactory.getConnection();
			stmt = conn.createStatement();
			
			stmt.execute(createSql);
			
		} catch (SQLException e) {
			isSuccess = false;
			logger.error("Create table to db failed!", e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
		
		return isSuccess;
	}
}
