package com.chinaunicom.credit.pojo;

public class BillAccountCompareResult {

	private String itemCode = null;
	
	private String itemName = null;
	
	private String lastMonthTotalFee = null;
	
	private String lastMonthTotalUser = null;
	
	private String lastMonthAvgFee = null;
	
	private String curMonthTotalFee = null;
	
	private String curMonthTotalUser = null;
	
	private String curMonthAvgFee = null;
	
	//本月与上月总费用差值
	private String diffFee = null;
	
	//环比
	private String lrr = null;
	
	private String diffAvgFee = null;

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getLastMonthTotalFee() {
		return lastMonthTotalFee;
	}

	public void setLastMonthTotalFee(String lastMonthTotalFee) {
		this.lastMonthTotalFee = lastMonthTotalFee;
	}

	public String getLastMonthTotalUser() {
		return lastMonthTotalUser;
	}

	public void setLastMonthTotalUser(String lastMonthTotalUser) {
		this.lastMonthTotalUser = lastMonthTotalUser;
	}

	public String getLastMonthAvgFee() {
		return lastMonthAvgFee;
	}

	public void setLastMonthAvgFee(String lastMonthAvgFee) {
		this.lastMonthAvgFee = lastMonthAvgFee;
	}

	public String getCurMonthTotalFee() {
		return curMonthTotalFee;
	}

	public void setCurMonthTotalFee(String curMonthTotalFee) {
		this.curMonthTotalFee = curMonthTotalFee;
	}

	public String getCurMonthTotalUser() {
		return curMonthTotalUser;
	}

	public void setCurMonthTotalUser(String curMonthTotalUser) {
		this.curMonthTotalUser = curMonthTotalUser;
	}

	public String getCurMonthAvgFee() {
		return curMonthAvgFee;
	}

	public void setCurMonthAvgFee(String curMonthAvgFee) {
		this.curMonthAvgFee = curMonthAvgFee;
	}

	public String getDiffFee() {
		return diffFee;
	}

	public void setDiffFee(String diffFee) {
		this.diffFee = diffFee;
	}

	public String getLrr() {
		return lrr;
	}

	public void setLrr(String lrr) {
		this.lrr = lrr;
	}

	public String getDiffAvgFee() {
		return diffAvgFee;
	}

	public void setDiffAvgFee(String diffAvgFee) {
		this.diffAvgFee = diffAvgFee;
	}
	
}
