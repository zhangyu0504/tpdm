package module.bean;

/**
 *券商托管账户信息
 * @author 张钰
 */
public class SecTruAcct {
	private String acctId;	//账号
	private int serial;		//序号
	private String acctType;	//	"类型 0–主帐号1-分帐号2–其它3-存管费扣款账号"
	private String secCompCode;	//券商代码
	private String curCode;		//币种
	private String acctName;	//账户名称
	private String openDepId;	//开户行
	private String branchId;	//分行号
	
	public String getAcctId() {
		return acctId;
	}
	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}
	public int getSerial() {
		return serial;
	}
	public void setSerial(int serial) {
		this.serial = serial;
	}
	public String getAcctType() {
		return acctType;
	}
	public void setAcctType(String acctType) {
		this.acctType = acctType;
	}
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}
	public String getCurCode() {
		return curCode;
	}
	public void setCurCode(String curCode) {
		this.curCode = curCode;
	}
	public String getAcctName() {
		return acctName;
	}
	public void setAcctName(String acctName) {
		this.acctName = acctName;
	}
	public String getOpenDepId() {
		return openDepId;
	}
	public void setOpenDepId(String openDepId) {
		this.openDepId = openDepId;
	}
	public String getBranchId() {
		return branchId;
	}
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}
	
}
