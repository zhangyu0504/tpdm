package module.bean;


/**
 * 特殊参数配置实体类
 * 
 * 销户结息、清算等配置
 * 
 * @author 吕超鸿
 *
 */
public class SpecialConfig {

	private String cfgId;//配置编号
	private String secCompCode;//券商编号
	private String cfgValue;//参数值
	private String memo; //参数说明
	
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
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.cfgValueChanged = false;
		this.memoChanged = false;
	}
	
}