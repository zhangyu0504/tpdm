package module.bean;


/**
 * 联名信息实体类
 * @author 吕超鸿
 *
 */
public class AllyData {

	private String secCompCode;//券商编号
	private String secCompName;//券商名称
	private String secBrchId;//证券营业部代码
	private String secBrchName;//证券营业部名称
	private String acctId;//银行卡号
	private String capAcct;//券商端客户资金台账号
	private String useFlag;//使用标志
	private String uniteFlag;//是否联名卡标识
	private String branchId;//适用分行代码
	private String depId;//适用支行网点代码
	
	private boolean secCompCodeChanged = false;
	private boolean secCompNameChanged = false;
	private boolean secBrchIdChanged = false;
	private boolean secBrchNameChanged = false;
	private boolean capAcctChanged = false;
	private boolean useFlagChanged = false;
	private boolean uniteFlagChanged = false;
	private boolean branchIdChanged = false;
	private boolean depIdChanged = false;
	
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
		this.secCompCodeChanged = true;
	}
	public String getSecCompName() {
		return secCompName;
	}
	public void setSecCompName(String secCompName) {
		this.secCompName = secCompName;
		this.secCompNameChanged = true;
	}
	public String getSecBrchId() {
		return secBrchId;
	}
	public void setSecBrchId(String secBrchId) {
		this.secBrchId = secBrchId;
		this.secBrchIdChanged = true;
	}
	public String getSecBrchName() {
		return secBrchName;
	}
	public void setSecBrchName(String secBrchName) {
		this.secBrchName = secBrchName;
		this.secBrchNameChanged = true;
	}
	public String getAcctId() {
		return acctId;
	}
	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}
	public String getCapAcct() {
		return capAcct;
	}
	public void setCapAcct(String capAcct) {
		this.capAcct = capAcct;
		this.capAcctChanged = true;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
		this.useFlagChanged = true;
	}
	public String getUniteFlag() {
		return uniteFlag;
	}
	public void setUniteFlag(String uniteFlag) {
		this.uniteFlag = uniteFlag;
		this.uniteFlagChanged = true;
	}
	public String getBranchId() {
		return branchId;
	}
	public void setBranchId(String branchId) {
		this.branchId = branchId;
		this.branchIdChanged = true;
	}
	public String getDepId() {
		return depId;
	}
	public void setDepId(String depId) {
		this.depId = depId;
		this.depIdChanged = true;
	}
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.secCompCodeChanged = false;
		this.secCompNameChanged = false;
		this.secBrchIdChanged = false;
		this.secBrchNameChanged = false;
		this.capAcctChanged = false;
		this.useFlagChanged = false;
		this.uniteFlagChanged = false;
		this.branchIdChanged = false;
		this.depIdChanged = false;
	}
	
}