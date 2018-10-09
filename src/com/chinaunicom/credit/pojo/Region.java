package com.chinaunicom.credit.pojo;

import java.util.List;

/**
 * 域对象类
 * 
 * @author zanglb
 *
 */
public class Region {

	private String regionCode = null;
	
	private String regionName = null;
	
	List<Province> provList = null;

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public List<Province> getProvList() {
		return provList;
	}

	public void setProvList(List<Province> provList) {
		this.provList = provList;
	}
	
}
