package com.chinaunicom.credit.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountSynDao;
import com.chinaunicom.credit.dbpool.C3P0MySqlDataSourceFactory;

public class BillAccountSynDaoImpl implements BillAccountSynDao {

	private static Logger logger = LoggerFactory.getLogger(BillAccountSynDaoImpl.class);
	
	@Override
	public void insertSynLog(String tableName, String status) {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = C3P0MySqlDataSourceFactory.getConnection();
			String insertSql = "insert into bill_account_syn_log(tablename, status) values (?, ?)";
			stmt = conn.prepareStatement(insertSql);
			stmt.setString(1, tableName);
			stmt.setString(2, status);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("insert into {} error!", tableName, e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
	}

	@Override
	public void updateSynLog(String tableName, String status) {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = C3P0MySqlDataSourceFactory.getConnection();
			String updateSql = "update bill_account_syn_log set status = ? where tablename = ?";
			stmt = conn.prepareStatement(updateSql);
			stmt.setString(1, status);
			stmt.setString(2, tableName);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Update table {} set status= {} error!", tableName, status, e);
		} finally {
			C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
		}
	}

	@Override
	public void deleteSynLog(List<String> tableNameList) {
		
		if(null != tableNameList && !tableNameList.isEmpty()) {
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = C3P0MySqlDataSourceFactory.getConnection();
				StringBuffer deleteSql = new StringBuffer();
				deleteSql.append("delete from bill_account_syn_log where tableName in (");
				
				int l = tableNameList.size();
				for(int i = 0; i < l; i++) {
					deleteSql.append("?,");
				}
				
				deleteSql.deleteCharAt(deleteSql.length() - 1);
				deleteSql.append(")");
				
				stmt = conn.prepareStatement(deleteSql.toString());
				for(int i = 1; i <= l; i++) {
					stmt.setString(i, tableNameList.get(i-1));
				}
				
				stmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("Delete table {} error!", tableNameList.toString(), e);
			} finally {
				C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
			}
		}
	}
}
