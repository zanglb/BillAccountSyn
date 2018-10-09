package com.chinaunicom.credit.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaunicom.credit.dao.BillAccountCommonDao;
import com.chinaunicom.credit.dao.BillAccountCompareDao;
import com.chinaunicom.credit.dao.impl.BillAccountCommonDaoImpl;
import com.chinaunicom.credit.dao.impl.BillAccountCompareDaoImpl;
import com.chinaunicom.credit.pojo.BillAccountCompareResult;
import com.chinaunicom.credit.pojo.Province;
import com.chinaunicom.credit.pojo.Region;
import com.chinaunicom.credit.service.BillAccountCompareService;
import com.chinaunicom.credit.util.CollectionUtils;
import com.chinaunicom.credit.util.DateUtils;
import com.chinaunicom.credit.util.FileUtils;
import com.chinaunicom.credit.util.PropertiesUtil;

/**
 * 每月2号账目项比较业务实现类
 * 
 * @author zanglb
 *
 */
public class BillAccountCompareServiceImpl implements BillAccountCompareService {

	private Logger logger = LoggerFactory.getLogger(BillAccountCompareServiceImpl.class);
			
	private BillAccountCommonDao billAccountCommonDao = new BillAccountCommonDaoImpl();
	
	@Override
	public void billAccountCompare(List<Region> allRegions) {
		/*
		 * 思路：
		 * 1.在mysql中创建对应的表
		 * 2.导入数据
		 * 3.按照域逐次遍历，生成Excel文件
		 */
		
		//获取所有的region和省分
		logger.info("Bill account compare start.");
		
		String hiveFileRootPath = PropertiesUtil.getValue("billaccount02_import_rootpath");
		String excelExportPath = PropertiesUtil.getValue("excelExportPath");
		
		//删除历史文件
		deleteFiles(excelExportPath);
		
		logger.info("Hive file path is {}.", hiveFileRootPath);
		logger.info("Excel export path is {}.", excelExportPath);
		String filePath = null;
		List<File> fileList = null;
		String regionCode = null;
		for(Region region : allRegions) {
			long s = System.currentTimeMillis();
			regionCode = region.getRegionCode();
			logger.info("Region {} bill account compare start.", regionCode);
			List<String> tableNames = createTable(regionCode);
			logger.info("Create table in mysql completed.tableNames={}", tableNames.toString());
			
			if(CollectionUtils.isNotEmpty(tableNames)) {
				//导入数据
				for(String tableName : tableNames) {
					filePath = hiveFileRootPath + File.separator + tableName;
					logger.info("Bill account compare result filePath={}", filePath);
					fileList = FileUtils.getFileList(filePath);
					
					if(CollectionUtils.isNotEmpty(fileList)) {
						logger.info("Load data to {}.", tableName);
						for(File file : fileList) {
							billAccountCommonDao.loadDataToMysql(tableName, file.getAbsolutePath().replaceAll("\\\\", "/"));
						}
					}
				}
				
				//数据导入Mysql以后，查询结果，并写入Excel
				writeToExcel(region, excelExportPath);
			}
			
			long e = System.currentTimeMillis();
			logger.info("Region {} bill account compare end.cost {}ms", regionCode, (e-s));
		}
		
		logger.info("Bill account compare end.");
	}

	/**
	 * 在MySQL中创建对应的{regionCode}_{dd}_bill_account_{yyyyMM}_PROV_STAT表
	 * 正常情况下只需要创建本月对应的表即可，上个月的表已经存在了
	 * 1.为了防止上个月的表不存在的情况首先检查上个月的表是否存在，如果不存在则创建上个月的表
	 * 2.检查本月的表是否存在，如果不存在创建本月的表
	 */
	private List<String> createTable(String regionCode) {
		
		if(StringUtils.isNotEmpty(regionCode) && StringUtils.isNotBlank(regionCode)) {
			
			List<String> tableNameList = new ArrayList<String>();
			
			String lastMonthTableName = getLastMonthTableName(regionCode);
			//检查上个月的表是否存在，如果不存在创建上个月的表
			if(!isTableExist(lastMonthTableName)) {
				//创建上个月的表
				String createSql = generateCreateTableSql(lastMonthTableName);
				if(billAccountCommonDao.createTable(createSql)) {
					tableNameList.add(lastMonthTableName);
				}				
			}
			
			String tableName = getCurrentMonthTableName(regionCode);
			//检查当月的表是否存在，如果不存在创建本月的表
			if(!isTableExist(tableName)) {
				//创建当月的表
				String createSql = generateCreateTableSql(tableName);
				if(billAccountCommonDao.createTable(createSql)) {
					tableNameList.add(tableName);
				}
			} else {
//				billAccountCommonDao.truncateTable(tableName);
				tableNameList.add(tableName);
			}
			
			return tableNameList;
		}
		
		return null;
	}
	
	/**
	 * 查询比对结果并写入Excel文件
	 * 
	 * @param region 域实例
	 * @param excelExportPath 导出excel的根目录
	 */
	private void writeToExcel(Region region, String excelExportPath) {
		String regionCode = region.getRegionCode();
		String lastMonthTableName = getLastMonthTableName(regionCode);
		String curMonthTableName = getCurrentMonthTableName(regionCode);
		String excelfilePath = excelExportPath + File.separator + region.getRegionCode() + "_" +DateUtils.getYYYYMMValue() + ".xlsx";
		
		logger.info("Write query result to {}.", excelfilePath);
		
		List<Province> provList = region.getProvList();
		if(CollectionUtils.isNotEmpty(provList)) {
			Workbook wb = new XSSFWorkbook();
			
			CellStyle titleCellStyle = setCellFormat(wb, true);
			CellStyle dataCellStyle = setCellFormat(wb, false);
			
			BillAccountCompareDao billAccountCompareDao = new BillAccountCompareDaoImpl();
			for(Province prov : provList) {
				List<BillAccountCompareResult> resultList = billAccountCompareDao.query(lastMonthTableName, curMonthTableName, prov.getProvCode());
				if(CollectionUtils.isNotEmpty(resultList)) {
					Sheet sheet = wb.createSheet(prov.getProvCode() + "_" + prov.getProvName());
					setColumnWidth(sheet);
					createTitleRow(sheet, titleCellStyle);
					createDataRow(sheet, dataCellStyle, resultList);
				}
			}	
			
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(excelfilePath);
				wb.write(outputStream);
			} catch (FileNotFoundException e) {
				logger.error("Write query result to excel error!Region is {}.", regionCode, e);
			} catch (IOException e) {
				logger.error("Write query result to excel error!Region is {}.", regionCode, e);
			} finally {
				if(null != outputStream) {
					try {
						outputStream.close();
					} catch (IOException e) {
						outputStream = null;
						logger.error("Write query result to excel error!Region is {}.", regionCode, e);
					}
				}
				
				if(null != wb) {
					try {
						wb.close();
					} catch (IOException e) {
						wb = null;
						logger.error("Write query result to excel error!Region is {}.", regionCode, e);
					}
				}
			}
		}
	}
	
	/**
	 * 判断表明是否存在
	 * 
	 * @param tableName 表名
	 * @return true or false
	 */
	private boolean isTableExist(String tableName) {
		return billAccountCommonDao.isTableExists(tableName);
	}
	
	/**
	 * 获取上月的数据表名称
	 * 
	 * @param regionCode 域编码
	 * @return 域编码_dd_bill_account_yyyyMM_compare
	 */
	private String getLastMonthTableName(String regionCode) {
		String dd = DateUtils.getDDValue();
		String month = DateUtils.getYYYYMMValue(-1);
		
		return getTableName(dd, month, regionCode);
	}
	
	/**
	 * 获取本月的数据表名称
	 * 
	 * @param regionCode 域编码
	 * @return 域编码_dd_bill_account_yyyyMM_compare
	 */
	private String getCurrentMonthTableName(String regionCode) {
		String dd = DateUtils.getDDValue();
		String month = DateUtils.getYYYYMMValue();
		
		return getTableName(dd, month, regionCode);
	}
	
	/**
	 * 拼接数据表名称
	 * 
	 * @param dd
	 * @param yyyyMM
	 * @param regionCode
	 * @return
	 */
	private String getTableName(String dd, String yyyyMM, String regionCode) {
		if(StringUtils.isNotEmpty(dd) && StringUtils.isNotBlank(dd)
				&& StringUtils.isNotEmpty(yyyyMM) && StringUtils.isNotBlank(yyyyMM)
				&& StringUtils.isNotEmpty(regionCode) && StringUtils.isNotBlank(regionCode)) {
			StringBuffer tableName = new StringBuffer();
			tableName.append(regionCode);
			tableName.append('_');
			tableName.append(dd);
			tableName.append('_');
			tableName.append("BILL_ACCOUNT");
			tableName.append('_');
			tableName.append(yyyyMM);
			tableName.append('_');
			tableName.append("compare");
			
			return tableName.toString();
		}
		
		return null;
	}
	
	/**
	 * 设置列宽
	 * 
	 * @param sheet
	 */
	private void setColumnWidth(Sheet sheet) {
		for(int i = 0; i < 11;i++) {
			if(i != 1) {
				sheet.setColumnWidth(i, 15*256);
			} else {
				sheet.setColumnWidth(i, 50*256);
			}
		}
	}
	
	/**
	 * 设置标题行单元格样式和数据
	 * 
	 * @param sheet
	 * @param cellStyle
	 */
	private void createTitleRow(Sheet sheet, CellStyle cellStyle) {
		String titles = PropertiesUtil.getValue("excelTitle");
		String[] titleArray = titles.split(",");
		
		Row row = sheet.createRow(0);
		int i = 0;
		Cell cell = null;
		for(String title : titleArray) {
			cell = row.createCell(i++, CellType.STRING);
			cell.setCellValue(title);
			cell.setCellStyle(cellStyle);
		}
	}
	
	/**
	 * 创建数据行
	 * 
	 * @param sheet
	 * @param cellStyle
	 * @param resultList
	 */
	private void createDataRow(Sheet sheet, CellStyle cellStyle, List<BillAccountCompareResult> resultList) {
		Row row = null;
		
		int rowCount = 1;
		for(BillAccountCompareResult result : resultList) {
			row = sheet.createRow(rowCount++);
			createDataCell(row, cellStyle, result);
		}
	}
	
	/**
	 * 设置数据行单元格样式和数据
	 * 
	 * @param row
	 * @param cellStyle
	 * @param result
	 */
	private void createDataCell(Row row, CellStyle cellStyle, BillAccountCompareResult result) {
		Cell cell = null;
		int cellCount = 0;
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getItemCode());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getItemName());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getLastMonthTotalFee());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getCurMonthTotalFee());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getDiffFee());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getLrr());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getLastMonthTotalUser());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getCurMonthTotalUser());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getLastMonthAvgFee());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getCurMonthAvgFee());
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellCount++, CellType.STRING);
		cell.setCellValue(result.getDiffAvgFee());
		cell.setCellStyle(cellStyle);
	}
	
	/**
	 * 创建单元格样式对象
	 * 
	 * @param workbook
	 * @param isTitleCell
	 * @return
	 */
	private CellStyle setCellFormat(Workbook workbook, boolean isTitleCell) {
		CellStyle cellStyle = workbook.createCellStyle();
		
		//设置字体
		Font font = workbook.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short)12);
		if(isTitleCell) {
			font.setBold(true);
		}
		
		cellStyle.setFont(font);
		//设置边框
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		
		return cellStyle;
	}
	
	private String generateCreateTableSql(String tableName) {
		String createSql = PropertiesUtil.getValue("billaccountCompareCreateTableSql");
			
		if(null != createSql) {
			createSql = createSql.replaceAll("#tablename#", tableName);
		
			return createSql;	
		} else {
			StringBuffer createSqlValue = new StringBuffer();
			createSqlValue.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
			createSqlValue.append(" (PROV_CODE varchar(2) NOT NULL,");
			createSqlValue.append(" DETAIL_ITEM_CODE varchar(10) NOT NULL,");
			createSqlValue.append(" usernum int(10) DEFAULT NULL,");
			createSqlValue.append(" avgfee decimal(19,4) DEFAULT NULL,");
			createSqlValue.append(" PRIMARY KEY (PROV_CODE,DETAIL_ITEM_CODE))");
			createSqlValue.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8");
			
			return createSqlValue.toString();
		}
	}
	
	/**
	 * 删除文件
	 * 
	 * @param excelExportPath
	 */
	private void deleteFiles(String excelExportPath) {
		File excelExportDir = new File(excelExportPath);
		if(excelExportDir.exists()) {
			if(excelExportDir.isDirectory()) {
				File[] excelList = excelExportDir.listFiles();
				for(File excel : excelList) {
					excel.delete();
				}
			}
		} else {
			excelExportDir.mkdirs();
		}
	}
}
