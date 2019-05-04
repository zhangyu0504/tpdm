package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 * 营业时间
 * @author 吕超鸿
 *
 */
public class LocalInfo {

	
	private String subCenterId;//地区编号
	private String bankDate;//营业日期
	private String lastBankDate;//前一营业日期
	private String nextBankDate;//下一营业日期
	private String subCenterName;//系统名称
	private String subCenterStatus;//系统状态
	private String relName;//联系人
	private String addr;//联系地址
	private String zip;//邮政编码
	private String phone;//联系电话
	private String fax;//传真
	private String emailAddr;//E-mail
	private String memo;//备注
	private String lLastBankDate;//前前一营业日期
	private String workdate;//交易日期
	
	private boolean bankDateChanged = false;
	private boolean lastBankDateChanged = false;
	private boolean nextBankDateChanged = false;
	private boolean subCenterNameChanged = false;
	private boolean subCenterStatusChanged = false;
	private boolean relNameChanged = false;
	private boolean addrChanged = false;
	private boolean zipChanged = false;
	private boolean phoneChanged = false;
	private boolean faxChanged = false;
	private boolean emailAddrChanged = false;
	private boolean memoChanged = false;
	private boolean lLastBankDateChanged = false;
	private boolean workdateChanged = false;
	
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
		this.emailAddrChanged = true;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}
	public String getlLastBankDate() {
		return lLastBankDate;
	}
	public void setlLastBankDate(String lLastBankDate) {
		this.lLastBankDate = lLastBankDate;
		this.lLastBankDateChanged = true;
	}
	public String getWorkdate() {
		return workdate;
	}
	public void setWorkdate(String workdate) {
		this.workdate = workdate;
		this.workdateChanged = true;
	}
	public String getSubCenterId() {
		return subCenterId;
	}
	public void setSubCenterId(String subCenterId) {
		this.subCenterId = subCenterId;
	}
	public String getBankDate() {
		return bankDate;
	}
	public void setBankDate(String bankDate) {
		this.bankDate = bankDate;
		this.bankDateChanged = true;
	}
	public String getLastBankDate() {
		return lastBankDate;
	}
	public void setLastBankDate(String lastBankDate) {
		this.lastBankDate = lastBankDate;
		this.lastBankDateChanged = true;
	}
	public String getNextBankDate() {
		return nextBankDate;
	}
	public void setNextBankDate(String nextBankDate) {
		this.nextBankDate = nextBankDate;
		this.nextBankDateChanged = true;
	}
	public String getSubCenterName() {
		return subCenterName;
	}
	public void setSubCenterName(String subCenterName) {
		this.subCenterName = subCenterName;
		this.subCenterNameChanged = true;
	}
	public String getSubCenterStatus() {
		return subCenterStatus;
	}
	public void setSubCenterStatus(String subCenterStatus) {
		this.subCenterStatus = subCenterStatus;
		this.subCenterStatusChanged = true;
	}
	public String getRelName() {
		return relName;
	}
	public void setRelName(String relName) {
		this.relName = relName;
		this.relNameChanged = true;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
		this.addrChanged = true;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
		this.zipChanged = true;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
		this.phoneChanged = true;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
		this.faxChanged = true;
	}
	
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.bankDateChanged = false;
		this.lastBankDateChanged = false;
		this.nextBankDateChanged = false;
		this.subCenterNameChanged = false;
		this.subCenterStatusChanged = false;
		this.relNameChanged = false;
		this.addrChanged = false;
		this.zipChanged = false;
		this.phoneChanged = false;
		this.faxChanged = false;
		this.emailAddrChanged = false;
		this.memoChanged = false;
		this.lLastBankDateChanged = false;
		this.workdateChanged = false;
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean) {
					continue;
				}
				fieldValueList.add(f.get(this));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SFException(e);
			} 
		}
		return fieldValueList.toArray();
	}
	
	
	public SQLStruct getSaveLocalInfoSQLStruct() throws SFException{
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append("MERGE INTO LOCALINFO T1");
		buffer.append(" USING (SELECT ? as SUBCENTERID, ? as BANKDATE, ? as LASTBANKDATE, ? as NEXTBANKDATE, ? as SUBCENTERNAME, ? as SUBCENTERSTATUS, ? as RELNAME, ? as ADDR,");
		buffer.append(" ? as ZIP, ? as PHONE, ? as FAX, ? as EMAILADDR, ? as MEMO, ? as LLASTBANKDATE, ? as WORKDATE");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.SUBCENTERID = T2.SUBCENTERID)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (bankDateChanged) {
			buffer.append("T1.BANKDATE = T2.BANKDATE,");
		}
		if (lastBankDateChanged) {
			buffer.append("T1.LASTBANKDATE = T2.LASTBANKDATE,");
		}
		if (nextBankDateChanged) {
			buffer.append("T1.NEXTBANKDATE = T2.NEXTBANKDATE,");
		}
		if (subCenterNameChanged) {
			buffer.append("T1.SUBCENTERNAME = T2.SUBCENTERNAME,");
		}
		if (subCenterStatusChanged) {
			buffer.append("T1.SUBCENTERSTATUS = T2.SUBCENTERSTATUS,");
		}
		if (relNameChanged) {
			buffer.append("T1.RELNAME = T2.RELNAME,");
		}
		if (addrChanged) {
			buffer.append("T1.ADDR = T2.ADDR,");
		}
		if (zipChanged) {
			buffer.append("T1.ZIP = T2.ZIP,");
		}
		if (phoneChanged) {
			buffer.append("T1.PHONE = T2.PHONE,");
		}
		if (faxChanged) {
			buffer.append("T1.FAX = T2.FAX,");
		}
		if (emailAddrChanged) {
			buffer.append("T1.EMAILADDR = T2.EMAILADDR,");
		}
		if (memoChanged) {
			buffer.append("T1.MEMO = T2.MEMO,");
		}
		if (lLastBankDateChanged) {
			buffer.append("T1.LLASTBANKDATE = T2.LLASTBANKDATE,");
		}
		if (workdateChanged) {
			buffer.append("T1.WORKDATE = T2.WORKDATE");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(SUBCENTERID,BANKDATE,LASTBANKDATE,NEXTBANKDATE,SUBCENTERNAME,SUBCENTERSTATUS,RELNAME,ADDR,ZIP,PHONE,FAX,EMAILADDR,");
		buffer.append("MEMO,LLASTBANKDATE,WORKDATE)");
		buffer.append(" values(T2.SUBCENTERID,T2.BANKDATE,T2.LASTBANKDATE,T2.NEXTBANKDATE,T2.SUBCENTERNAME,T2.SUBCENTERSTATUS,T2.RELNAME,T2.ADDR,T2.ZIP,T2.PHONE,T2.FAX,");
		buffer.append("T2.EMAILADDR,T2.MEMO,T2.LLASTBANKDATE,T2.WORKDATE)");

		return new SQLStruct(buffer.toString(), valueObj);
	}
}
