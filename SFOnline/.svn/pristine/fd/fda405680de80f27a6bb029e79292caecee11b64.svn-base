package module.bean;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 * 平台服务状态控制
 * 
 *   通过设置此表，停止或启动日间银证转账、带资金销户交易
 *   每天一条记录
 * 
 * @author 吕超鸿
 *
 */
public class ServStatus {

	private String txDate;//交易日期
	private String txTime;//服务停止时间
	private String icsTime;//ICS时间
	
	private boolean txTimeChanged = false;
	private boolean icsTimeChanged = false;
	
	public String getTxDate() {
		return txDate;
	}
	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}
	public String getTxTime() {
		return txTime;
	}
	public void setTxTime(String txTime) {
		this.txTime = txTime;
	}
	public String getIcsTime() {
		return icsTime;
	}
	public void setIcsTime(String icsTime) {
		this.icsTime = icsTime;
	}
	

	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.txTimeChanged = false;
		this.icsTimeChanged = false;
	}
	
}
