package module.bean;


/**
 * �����������ʵ����
 * 
 * ������Ϣ�����������
 * 
 * @author ������
 *
 */
public class SpecialConfig {

	private String cfgId;//���ñ��
	private String secCompCode;//ȯ�̱��
	private String cfgValue;//����ֵ
	private String memo; //����˵��
	
	private boolean cfgValueChanged = false;
	private boolean memoChanged = false;
	
	
	public String getCfgId() {
		return cfgId;
	}
	public void setCfgId(String cfgId) {
		this.cfgId = cfgId;
	}
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}
	public String getCfgValue() {
		return cfgValue;
	}
	public void setCfgValue(String cfgValue) {
		this.cfgValue = cfgValue;
		this.cfgValueChanged = true;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}
	
	/**
	 * ��������changeFlag
	 */
	public void resetChangedFlag() {
		this.cfgValueChanged = false;
		this.memoChanged = false;
	}
	
}