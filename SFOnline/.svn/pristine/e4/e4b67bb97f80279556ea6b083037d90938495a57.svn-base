package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;


/**
 * 联网行客户信息实体类
 * @author 吕超鸿
 *
 */
public class AgtCustomerInfo{

	private static final long serialVersionUID = 1L;
	
	private String invType;//客户类型
	private String acctNo;//账号
	private String stkAcct;//资金账号
	private String bankId;//银行号
	private String openBranch;//开户分行
	private String stkCode;//券商代码
	private String curCode;//币种
	private String invName;//客户名
	private String idType;//证件类型
	private String invidCode;//证件号
	private String openDate;//开户日期
	private String status;//状态
	private String memo;//备注
	
	private boolean invTypeChanged = false;
	private boolean acctNoChanged = false;
	private boolean bankIdChanged = false;
	private boolean openBranchChanged = false;
	private boolean curCodeChanged = false;
	private boolean invNameChanged = false;
	private boolean idTypeChanged = false;
	private boolean invidCodeChanged = false;
	private boolean openDateChanged = false;
	private boolean statusChanged = false;
	private boolean memoChanged = false;
	
	private AgtAgentInfo agtAgentInfo = new AgtAgentInfo();
	
	

	public AgtAgentInfo getAgtAgentInfo() {
		return agtAgentInfo;
	}

	public void setAgtAgentInfo(AgtAgentInfo agtAgentInfo) {
		this.agtAgentInfo = agtAgentInfo;
	}

	public String getInvType() {
		return invType;
	}

	public void setInvType(String invType) {
		this.invType = invType;
		this.invTypeChanged = true;
	}

	public String getAcctNo() {
		return acctNo;
	}

	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
		this.acctNoChanged = true;
	}

	public String getStkAcct() {
		return stkAcct;
	}

	public void setStkAcct(String stkAcct) {
		this.stkAcct = stkAcct;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
		this.bankIdChanged = true;
	}

	public String getOpenBranch() {
		return openBranch;
	}

	public void setOpenBranch(String openBranch) {
		this.openBranch = openBranch;
		this.openBranchChanged = true;
	}

	public String getStkCode() {
		return stkCode;
	}

	public void setStkCode(String stkCode) {
		this.stkCode = stkCode;
	}

	public String getCurCode() {
		return curCode;
	}

	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged = true;
	}

	public String getInvName() {
		return invName;
	}

	public void setInvName(String invName) {
		this.invName = invName;
		this.invNameChanged = true;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
		this.idTypeChanged = true;
	}

	public String getInvidCode() {
		return invidCode;
	}

	public void setInvidCode(String invidCode) {
		this.invidCode = invidCode;
		this.invidCodeChanged = true;
	}

	public String getOpenDate() {
		return openDate;
	}

	public void setOpenDate(String openDate) {
		this.openDate = openDate;
		this.openDateChanged = true;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		this.statusChanged = true;
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
		this.invTypeChanged = false;
		this.acctNoChanged = false;
		this.bankIdChanged = false;
		this.openBranchChanged = false;
		this.curCodeChanged = false;
		this.invNameChanged = false;
		this.idTypeChanged = false;
		this.invidCodeChanged = false;
		this.openDateChanged = false;
		this.statusChanged = false;
		this.memoChanged = false;
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean || f.get(this) instanceof AgtAgentInfo) {
					continue;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new SFException(e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new SFException(e);
			}
			try {
				fieldValueList.add(f.get(this));
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				throw new SFException(e1);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				throw new SFException(e1);
			}
		}

		return fieldValueList.toArray();
	}
	
	/**
	 * 获取保存SQL结构体
	 * @return
	 */
	public SQLStruct getSaveAgtCustomerInfoSQLStruct() throws SFException{
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append("MERGE INTO AGT_CUSTOMERINFO T1");
		buffer.append(" USING (select ? as INVTYPE, ? as ACCTNO, ? as STKACCT, ? as BANKID, ? as OPENBRANCH,");
		buffer.append(" ? as STKCODE, ? as CURCODE, ? as INVNAME, ? as IDTYPE, ? as INVIDCODE, ? as OPENDATE,");
		buffer.append(" ? as STATUS, ? as MEMO from dual) T2");
		buffer.append(" ON (T1.STKACCT = T2.STKACCT and T1.STKCODE = T2.STKCODE)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET");
		
		if(this.invTypeChanged){
			buffer.append(" T1.INVTYPE = T2.INVTYPE,");
		}
		if(this.acctNoChanged){
			buffer.append(" T1.ACCTNO = T2.ACCTNO,");
		}
		if(this.bankIdChanged){
			buffer.append(" T1.BANKID = T2.BANKID,");
		}
		if(this.openBranchChanged){
			buffer.append(" T1.OPENBRANCH = T2.OPENBRANCH,");
		}
		if(this.curCodeChanged){
			buffer.append(" T1.CURCODE = T2.CURCODE,");
		}
		if(this.invNameChanged){
			buffer.append(" T1.INVNAME = T2.INVNAME,");
		}
		if(this.idTypeChanged){
			buffer.append(" T1.IDTYPE = T2.IDTYPE,");
		}
		if(this.invidCodeChanged){
			buffer.append(" T1.INVIDCODE = T2.INVIDCODE,");
		}
		if(this.openDateChanged){
			buffer.append(" T1.OPENDATE = T2.OPENDATE,");
		}
		if(this.statusChanged){
			buffer.append(" T1.STATUS = T2.STATUS,");
		}
		if(this.memoChanged){
			buffer.append(" T1.MEMO = T2.MEMO");
		}
		if(buffer.substring(buffer.length()-1).equals(",")){
			buffer=buffer.deleteCharAt(buffer.length()-1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert");
		buffer.append(" (INVTYPE,");
		buffer.append(" ACCTNO,");
		buffer.append(" STKACCT,");
		buffer.append(" BANKID,");
		buffer.append(" OPENBRANCH,");
		buffer.append(" STKCODE,");
		buffer.append(" CURCODE,");
		buffer.append(" INVNAME,");
		buffer.append(" IDTYPE,");
		buffer.append(" INVIDCODE,");
		buffer.append(" OPENDATE,");
		buffer.append(" STATUS,");
		buffer.append(" MEMO)");
		
		buffer.append(" values");
		
		buffer.append(" (T2.INVTYPE,");
		buffer.append(" T2.ACCTNO,");
		buffer.append(" T2.STKACCT,");
		buffer.append(" T2.BANKID,");
		buffer.append(" T2.OPENBRANCH,");
		buffer.append(" T2.STKCODE,");
		buffer.append(" T2.CURCODE,");
		buffer.append(" T2.INVNAME,");
		buffer.append(" T2.IDTYPE,");
		buffer.append(" T2.INVIDCODE,");
		buffer.append(" T2.OPENDATE,");
		buffer.append(" T2.STATUS,");
		buffer.append(" T2.MEMO)");
		
		return new SQLStruct(buffer.toString(), valueObj);
	}
	
}