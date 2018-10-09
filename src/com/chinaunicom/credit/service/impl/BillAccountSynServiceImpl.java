package com.chinaunicom.credit.service.impl;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chinaunicom.credit.pojo.Region;
import com.chinaunicom.credit.service.BillAccountSynService;
import com.chinaunicom.credit.util.PropertiesUtil;

/**
 * BillAccount数据同步接口实现类
 * 实现同步BillAccount的数据到Mysql
 * 并检查超期的表，定期删除
 * 
 * @author zanglb
 *
 */
public class BillAccountSynServiceImpl implements BillAccountSynService{

	@Override
	public void synBillAccountData(List<Region> allRegions) {
		//读取文件
		//拼接成对象
		//多线程插入mysql
		String rootPath = PropertiesUtil.getValue("billaccount_import_rootpath");
		String threadNumValue = PropertiesUtil.getValue("billaccountThreadNum");

		File rootDir = new File(rootPath);
		if (rootDir.exists()) {

			int threadNum = Integer.parseInt(threadNumValue);

			ExecutorService pool = Executors.newFixedThreadPool(threadNum);

			for (Region region : allRegions) {
				pool.execute(new BillAccountSynRunnable(region.getRegionCode(), rootPath));
			}

			pool.shutdown();
		}
	}

}
