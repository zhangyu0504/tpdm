package module.bean;


/**
 * ���ղ���������
 * @author ������
 *
 */
public class HolidayDate {

	private String secCompCode;// ȯ�̴���

	private String txDate;// ����

	private String secCompName;// ȯ������

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
	 * ��������changeFlag
	 */
	public void resetChangedFlag() {
		this.secCompNameChanged = false;
	}

}
