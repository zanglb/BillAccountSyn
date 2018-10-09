package com.chinaunicom.credit.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCompareDao;
import com.chinaunicom.credit.dbpool.C3P0MySqlDataSourceFactory;
import com.chinaunicom.credit.pojo.BillAccountCompareResult;
import com.chinaunicom.credit.util.PropertiesUtil;

public class BillAccountCompareDaoImpl implements BillAccountCompareDao {

	private static Logger logger = LoggerFactory.getLogger(BillAccountCompareDaoImpl.class);

	@Override
	public List<BillAccountCompareResult> query(String lastMonthTableName, String curMonthTableName, String provCode) {
		
		if(StringUtils.isNotEmpty(lastMonthTableName) && StringUtils.isNotBlank(lastMonthTableName)
				&& StringUtils.isNoneEmpty(curMonthTableName) && StringUtils.isNotBlank(curMonthTableName)
				&& StringUtils.isNotEmpty(provCode) && StringUtils.isNotBlank(provCode)) {
			String querySql = generateQuerySql(lastMonthTableName, curMonthTableName);
			
			if(null != querySql) {
				Connection conn = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try {

					conn = C3P0MySqlDataSourceFactory.getConnection();
					stmt = conn.prepareStatement(querySql);
					stmt.setString(1, provCode);
					
					List<BillAccountCompareResult> result = new ArrayList<BillAccountCompareResult>();
					rs = stmt.executeQuery();
					
					BillAccountCompareResult r = null;
					while(rs.next()) {
						r = new BillAccountCompareResult();
						r.setItemCode(rs.getString("itemCode"));
						r.setItemName(rs.getString("itemName"));
						r.setLastMonthTotalFee(rs.getString("lastMonthTotalFee"));
						r.setCurMonthTotalFee(rs.getString("curMonthTotalFee"));
						r.setLastMonthTotalUser(rs.getString("lastMonthTotalUser"));
						r.setCurMonthTotalUser(rs.getString("curMonthTotalUser"));
						r.setLastMonthAvgFee(rs.getString("lastMonthAvgFee"));
						r.setCurMonthAvgFee(rs.getString("curMonthAvgFee"));
						r.setDiffFee(rs.getString("diffFee"));
						r.setLrr(rs.getString("lrr"));
						r.setDiffAvgFee(rs.getString("diffAvgFee"));
						
						result.add(r);
					}
					
					return result;
				} catch (SQLException e) {
					logger.error("Query billAccountCompare result error!", e);
				} finally {
					C3P0MySqlDataSourceFactory.closeAll(null, stmt, conn);
				}
			}
		}
		
		return null;
	}
	
	private String generateQuerySql(String lastMonthTableName, String curMonthTableName) {
		
		String querySql = PropertiesUtil.getValue("billaccountCompareQuerySql");
			
		if(StringUtils.isNotEmpty(querySql) && StringUtils.isNotBlank(querySql)) {
			querySql = querySql.replaceAll("#tablename1#", lastMonthTableName);
			querySql = querySql.replaceAll("#tablename2#", curMonthTableName);
				
			return querySql;
		}
		return null;
	}

}
