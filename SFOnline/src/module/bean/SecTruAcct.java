package module.bean;

/**
 *ȯ���й��˻���Ϣ
 * @author ����
 */
public class SecTruAcct {
	private String acctId;	//�˺�
	private int serial;		//���
	private String acctType;	//	"���� 0�C���ʺ�1-���ʺ�2�C����3-��ܷѿۿ��˺�"
	private String secCompCode;	//ȯ�̴���
	private String curCode;		//����
	private String acctName;	//�˻�����
	private String openDepId;	//������
	private String branchId;	//���к�
	
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
