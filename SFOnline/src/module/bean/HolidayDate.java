package module.bean;


/**
 * 假日不无忧日期
 * @author 吕超鸿
 *
 */
public class HolidayDate {

	private String secCompCode;// 券商代码

	private String txDate;// 日期

	private String secCompName;// 券商名称

	private boolean secCompNameChanged = false;

	public String getSecCompCode() {
		return secCompCode;
	}

	public void setSecCompCode( String secCompCode ) {
		this.secCompCode = secCompCode;
	}

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate( String txDate ) {
		this.txDate = txDate;
	}

	public String getSecCompName() {
		return secCompName;
	}

	public void setSecCompName( String secCompName ) {
		this.secCompName = secCompName;
		this.secCompNameChanged = true;
	}

	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.secCompNameChanged = false;
	}

}
