package module.bean;


/**
 * 券商不服务时间 实体
 * @author 吕超鸿
 *
 */
public class SecNoServTime {

	private String secCompCode;//券商代码
	private String txDate;//日期
	private String secCompName;//券商名称
	private String beginTime;//开始时间
	private String endTime;//结束时间
	
	private boolean secCompNameChanged = false;
	private boolean beginTimeChanged = false;
	private boolean endTimeChanged = false;
	
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}
	public String getTxDate() {
		return txDate;
	}
	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}
	public String getSecCompName() {
		return secCompName;
	}
	public void setSecCompName(String secCompName) {
		this.secCompName = secCompName;
		this.secCompNameChanged = true;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
		this.beginTimeChanged = true;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
		this.endTimeChanged = true;
	}
	

	/**
	 * 重置所有changedFlag
	 */
	public void resetChangedFlag(){
		this.secCompNameChanged = false;
		this.beginTimeChanged = false;
		this.endTimeChanged = false;
	}
	
	
}
