package com.chinaunicom.credit.dbpool;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.util.PropertiesUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * c3p0数据库连接池
 * 
 * @author zanglb
 *
 */
public class C3P0MySqlDataSourceFactory {
	
	private static Logger logger = LoggerFactory.getLogger(C3P0MySqlDataSourceFactory.class);
	
	private static final String fileName = "mysqlDatasource.properties";
	
	private static ComboPooledDataSource ds = null;
	
	private static int initialPoolSize = 5;
	
	private static int minPoolSize = 5;
	
	private static int maxPoolSize = 30;
	
	private static int maxIdleTime = 600;
	
	private static int acquireIncrement = 5;
	
	private static int acquireRetryAttempts = 3;
	
	private static int acquireRetryDelay = 10000;
	
	private static int idleConnectionTestPeriod = 90;
	
	private static int checkoutTimeout = 10000;
	
	private static void setDataSource() {
		
		Properties properties = PropertiesUtil.loadDataFromFile(fileName);
		if(null != properties && !properties.isEmpty()) {
			ds = new ComboPooledDataSource();
			String driverClass = properties.getProperty("driverClass");
			String jdbcUrl = properties.getProperty("jdbcUrl");
			String user = properties.getProperty("user");
			String password = properties.getProperty("password");
			
			try {
				ds.setDriverClass(driverClass);
			} catch (PropertyVetoException e) {
				logger.error("Set c3p0 data source error!", e);
			}
			
			ds.setJdbcUrl(jdbcUrl);
			ds.setUser(user);
			ds.setPassword(password);
			 
			
			String initialPoolSizeValue = properties.getProperty("initialPoolSize");
			if(StringUtils.isNotEmpty(initialPoolSizeValue) && StringUtils.isNotBlank(initialPoolSizeValue)) {
				initialPoolSize = Integer.parseInt(initialPoolSizeValue);
			}
			
			String minPoolSizeValue = properties.getProperty("minPoolSize");
			if(StringUtils.isNotEmpty(minPoolSizeValue) && StringUtils.isNotBlank(minPoolSizeValue)) {
				minPoolSize = Integer.parseInt(minPoolSizeValue);
			}
			
			String maxPoolSizeValue = properties.getProperty("maxPoolSize");
			if(StringUtils.isNotEmpty(maxPoolSizeValue) && StringUtils.isNotBlank(maxPoolSizeValue)) {
				maxPoolSize = Integer.parseInt(maxPoolSizeValue);
			}
			
			String maxIdleTimeValue = properties.getProperty("maxIdleTime");
			if(StringUtils.isNotEmpty(maxIdleTimeValue) && StringUtils.isNotBlank(maxIdleTimeValue)) {
				maxIdleTime = Integer.parseInt(maxIdleTimeValue);
			}
			
			String acquireIncrementValue = properties.getProperty("acquireIncrement");
			if(StringUtils.isNotEmpty(acquireIncrementValue) && StringUtils.isNotBlank(acquireIncrementValue)) {
				acquireIncrement = Integer.parseInt(acquireIncrementValue);
			}
			
			String acquireRetryAttemptsValue = properties.getProperty("acquireRetryAttempts");
			if(StringUtils.isNotEmpty(acquireRetryAttemptsValue) && StringUtils.isNotBlank(acquireRetryAttemptsValue)) {
				acquireRetryAttempts = Integer.parseInt(acquireRetryAttemptsValue);
			}
			
			String acquireRetryDelayValue = properties.getProperty("acquireRetryDelay");
			if(StringUtils.isNotEmpty(acquireRetryDelayValue) && StringUtils.isNotBlank(acquireRetryDelayValue)) {
				acquireRetryDelay = Integer.parseInt(acquireRetryDelayValue);
			}
			
			String idleConnectionTestPeriodValue = properties.getProperty("idleConnectionTestPeriod");
			if(StringUtils.isNotEmpty(idleConnectionTestPeriodValue) && StringUtils.isNotBlank(idleConnectionTestPeriodValue)) {
				idleConnectionTestPeriod = Integer.parseInt(idleConnectionTestPeriodValue);
			}
			
			String checkoutTimeoutValue = properties.getProperty("checkoutTimeout");
			if(StringUtils.isNotEmpty(checkoutTimeoutValue) && StringUtils.isNotBlank(checkoutTimeoutValue)) {
				checkoutTimeout = Integer.parseInt(checkoutTimeoutValue);
			}
			
			ds.setInitialPoolSize(initialPoolSize);
			ds.setMinPoolSize(minPoolSize);
			ds.setMaxPoolSize(maxPoolSize);
			ds.setMaxIdleTime(maxIdleTime);
			ds.setAcquireIncrement(acquireIncrement);
			ds.setAcquireRetryAttempts(acquireRetryAttempts);
			ds.setAcquireRetryDelay(acquireRetryDelay);
			ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			ds.setCheckoutTimeout(checkoutTimeout);
		} 
//		else {
//			ds = new ComboPooledDataSource();
//		}
		
	}
	
	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public synchronized static Connection getConnection() {
		try {
			
			if(null == ds) {
				setDataSource();
//				ds = new ComboPooledDataSource();
			}
			
			return ds.getConnection();
		} catch (SQLException e) {
			logger.error("Get connection error.", e);
		}
		
		return null;
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		if(null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				conn = null;
				logger.error("Close connection error.", e);
			}
		}
	}
	
	/**
	 * 关闭Statement
	 * 
	 * @param stmt
	 */
	public static void closeStatement(Statement stmt) {
		if(null != stmt) {
			try {
				stmt.close();
			} catch (SQLException e) {
				stmt = null;
				logger.error("Close Statement error.", e);
			}
		}
	}
	
	/**
	 * 关闭ResultSet
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
		if(null != rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				rs = null;
				logger.error("Close ResultSet error.", e);
			}
		}
	}
	
	/**
	 * 依次关闭ResultSet，Statement，Connection
	 * 
	 * @param rs ResultSet
	 * @param stmt Statement
	 * @param conn Connection
	 */
	public static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
		if(null != rs) {
			closeResultSet(rs);
		}
		
		if(null != stmt) {
			closeStatement(stmt);
		}
		
		if(null != conn) {
			closeConnection(conn);
		}
	}
	
}
