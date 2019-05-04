package module.bean;

/**
 * 假日无忧
 */
public class Holiday {


	private String txDate;// 日期

	private String holiDayFlag;//节假日标志：1--节假日；0--工作日


	public String getTxDate() {
		return txDate;
	}

	public void setTxDate( String txDate ) {
		this.txDate = txDate;
	}

	public String getHoliDayFlag() {
		return holiDayFlag;
	}

	public void setHoliDayFlag( String holiDayFlag ) {
		this.holiDayFlag = holiDayFlag;
	}

}
