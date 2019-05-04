package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

public class InvestData {

	private String invType;
	private boolean invTypeChanged = false;
	private String secAcct;
	private boolean secAcctChanged = false;
	private String invName;
	private boolean invNameChanged = false;
	private String idType;
	private String invIdCode;
	private String nationality;
	private boolean nationalityChanged = false;
	private String sex;
	private boolean sexChanged = false;
	private String legalName;
	private boolean legalNameChanged = false;
	private String legalIdCode;
	private boolean legalIdCodeChanged = false;
	private String trnName;
	private boolean trnNameChanged = false;
	private String trnIdType;
	private boolean trnIdTypeChanged = false;
	private String trnIdCode;
	private boolean trnIdCodeChanged = false;
	private String trnPhone;
	private boolean trnPhoneChanged = false;
	private String trnMobile;
	private boolean trnMobileChanged = false;
	private String addr;
	private boolean addrChanged = false;
	private String zip;
	private boolean zipChanged = false;
	private String phone;
	private boolean phoneChanged = false;
	private String mobile;
	private boolean mobileChanged = false;
	private String fax;
	private boolean faxChanged = false;
	private String emailAddr;
	private boolean emailAddrChanged = false;
	private String memo;
	private boolean memoChanged = false;
	private String interFlag;
	private boolean interFlagChanged = false;
	private String becifNo;
	private boolean becifNoChanged = false;
	private String globalType;
	private boolean globalTypeChanged = false;
	private String globalId;
	private boolean globalIdChanged = false;
	private String clientName;
	private boolean clientNameChanged = false;


	public String getInvType() {
		return invType;
	}

	public void setInvType(String invType) {
		this.invType = invType;
		this.invTypeChanged = true;
	}

	public String getSecAcct() {
		return secAcct;
	}

	public void setSecAcct(String secAcct) {
		this.secAcct = secAcct;
		this.secAcctChanged = true;
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
	}

	public String getInvIdCode() {
		return invIdCode;
	}

	public void setInvIdCode(String invIdCode) {
		this.invIdCode = invIdCode;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
		this.nationalityChanged = true;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
		this.sexChanged = true;
	}

	public String getLegalName() {
		return legalName;
	}

	public void setLegalName(String legalName) {
		this.legalName = legalName;
		this.legalNameChanged = true;
	}

	public String getLegalIdCode() {
		return legalIdCode;
	}

	public void setLegalIdCode(String legalIdCode) {
		this.legalIdCode = legalIdCode;
		this.legalIdCodeChanged = true;
	}

	public String getTrnName() {
		return trnName;
	}

	public void setTrnName(String trnName) {
		this.trnName = trnName;
		this.trnNameChanged = true;
	}

	public String getTrnIdType() {
		return trnIdType;
	}

	public void setTrnIdType(String trnIdType) {
		this.trnIdType = trnIdType;
		this.trnIdTypeChanged = true;
	}

	public String getTrnIdCode() {
		return trnIdCode;
	}

	public void setTrnIdCode(String trnIdCode) {
		this.trnIdCode = trnIdCode;
		this.trnIdCodeChanged = true;
	}

	public String getTrnPhone() {
		return trnPhone;
	}

	public void setTrnPhone(String trnPhone) {
		this.trnPhone = trnPhone;
		this.trnPhoneChanged = true;
	}

	public String getTrnMobile() {
		return trnMobile;
	}

	public void setTrnMobile(String trnMobile) {
		this.trnMobile = trnMobile;
		this.trnMobileChanged = true;
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

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
		this.mobileChanged = true;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
		this.faxChanged = true;
	}

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

	public String getInterFlag() {
		return interFlag;
	}

	public void setInterFlag(String interFlag) {
		this.interFlag = interFlag;
		this.interFlagChanged = true;
	}

	public String getBecifNo() {
		return becifNo;
	}

	public void setBecifNo(String becifNo) {
		this.becifNo = becifNo;
		this.becifNoChanged = true;
	}

	public String getGlobalType() {
		return globalType;
	}

	public void setGlobalType(String globalType) {
		this.globalType = globalType;
		this.globalTypeChanged = true;
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
		this.globalIdChanged = true;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
		this.clientNameChanged = true;
	}
	
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean || f.get(this) instanceof SecCompData || f.get(this) instanceof SignAccountData) {
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
	

	public SQLStruct getSaveInvestDataSQLStruct() throws SFException{
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();

		buffer.append(" MERGE INTO TRDINVESTDATA T1");
		buffer.append(" USING (SELECT ? AS INVTYPE, ? AS SECACCT, ? AS INVNAME, ? AS IDTYPE, ? AS INVIDCODE, ? AS NATIONALITY, ? AS SEX,");
		buffer.append(" ? AS LEGALNAME, ? AS LEGALIDCODE, ? AS TRNNAME, ? AS TRNIDTYPE, ? AS TRNIDCODE, ? AS TRNPHONE, ? AS TRNMOBILE,");
		buffer.append(" ? AS ADDR, ? AS ZIP, ? AS PHONE, ? AS MOBILE, ? AS FAX, ? AS EMAILADDR,");
		buffer.append(" ? AS MEMO,");
		buffer.append(" ? AS INTERFLAG,");
		buffer.append(" ? AS BECIFNO,");
		buffer.append(" ? AS GLOBALTYPE,");
		buffer.append(" ? AS GLOBALID,");
		buffer.append(" ? AS CLIENTNAME");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.IDTYPE = T2.IDTYPE AND T1.INVIDCODE = T2.INVIDCODE)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (invTypeChanged) {
			buffer.append(" T1.INVTYPE = T2.INVTYPE,");
		}
		if (secAcctChanged) {
			buffer.append(" T1.SECACCT = T2.SECACCT,");
		}
		if (invNameChanged) {
			buffer.append(" T1.INVNAME = T2.INVNAME,");
		}
		if (nationalityChanged) {
			buffer.append(" T1.NATIONALITY = T2.NATIONALITY,");
		}
		if (sexChanged) {
			buffer.append(" T1.SEX = T2.SEX,");
		}
		if (legalNameChanged) {
			buffer.append(" T1.LEGALNAME = T2.LEGALNAME,");
		}
		if (legalIdCodeChanged) {
			buffer.append(" T1.LEGALIDCODE = T2.LEGALIDCODE,");
		}
		if (trnNameChanged) {
			buffer.append(" T1.TRNNAME = T2.TRNNAME,");
		}
		if (trnIdTypeChanged) {
			buffer.append(" T1.TRNIDTYPE = T2.TRNIDTYPE,");
		}
		if (trnIdCodeChanged) {
			buffer.append(" T1.TRNIDCODE = T2.TRNIDCODE,");
		}
		if (trnPhoneChanged) {
			buffer.append(" T1.TRNPHONE = T2.TRNPHONE,");
		}
		if (trnMobileChanged) {
			buffer.append(" T1.TRNMOBILE = T2.TRNMOBILE,");
		}
		if (addrChanged) {
			buffer.append(" T1.ADDR = T2.ADDR,");
		}
		if (zipChanged) {
			buffer.append(" T1.ZIP = T2.ZIP,");
		}
		if (phoneChanged) {
			buffer.append(" T1.PHONE = T2.PHONE,");
		}
		if (mobileChanged) {
			buffer.append(" T1.MOBILE = T2.MOBILE,");
		}
		if (faxChanged) {
			buffer.append(" T1.FAX = T2.FAX,");
		}
		if (emailAddrChanged) {
			buffer.append(" T1.EMAILADDR = T2.EMAILADDR,");
		}
		if (memoChanged) {
			buffer.append(" T1.MEMO = T2.MEMO,");
		}
		if (interFlagChanged) {
			buffer.append(" T1.INTERFLAG = T2.INTERFLAG,");
		}
		if (becifNoChanged) {
			buffer.append(" T1.BECIFNO = T2.BECIFNO,");
		}
		if (globalTypeChanged) {
			buffer.append(" T1.GLOBALTYPE = T2.GLOBALTYPE,");
		}
		if (globalIdChanged) {
			buffer.append(" T1.GLOBALID = T2.GLOBALID,");
		}
		if (clientNameChanged) {
			buffer.append(" T1.CLIENTNAME = T2.CLIENTNAME");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(INVTYPE,SECACCT,INVNAME,IDTYPE,INVIDCODE,NATIONALITY,SEX,LEGALNAME,LEGALIDCODE,");
		buffer.append("TRNNAME,TRNIDTYPE,TRNIDCODE,TRNPHONE,TRNMOBILE,ADDR,ZIP,PHONE,MOBILE,FAX,EMAILADDR,MEMO,");
		buffer.append("INTERFLAG,BECIFNO,GLOBALTYPE,GLOBALID,CLIENTNAME)values(T2.INVTYPE,T2.SECACCT,T2.INVNAME,");
		buffer.append("T2.IDTYPE,T2.INVIDCODE,T2.NATIONALITY,T2.SEX,T2.LEGALNAME,T2.LEGALIDCODE,T2.TRNNAME,T2.TRNIDTYPE,T2.TRNIDCODE,");
		buffer.append("T2.TRNPHONE,T2.TRNMOBILE,T2.ADDR,T2.ZIP,T2.PHONE,T2.MOBILE,T2.FAX,T2.EMAILADDR,T2.MEMO,T2.INTERFLAG,");
		buffer.append("T2.BECIFNO,T2.GLOBALTYPE,T2.GLOBALID,T2.CLIENTNAME)");

		return new SQLStruct(buffer.toString(), valueObj);
	}
	


	/**
	 * ÷ÿ÷√À˘”–changeFlag
	 */
	public void resetChangedFlag() {
		this.invTypeChanged = false;
		this.secAcctChanged = false;
		this.invNameChanged = false;
		this.nationalityChanged = false;
		this.sexChanged = false;
		this.legalNameChanged = false;
		this.legalIdCodeChanged = false;
		this.trnNameChanged = false;
		this.trnIdTypeChanged = false;
		this.trnIdCodeChanged = false;
		this.trnPhoneChanged = false;
		this.trnMobileChanged = false;
		this.addrChanged = false;
		this.zipChanged = false;
		this.phoneChanged = false;
		this.mobileChanged = false;
		this.faxChanged = false;
		this.emailAddrChanged = false;
		this.memoChanged = false;
		this.interFlagChanged = false;
		this.becifNoChanged = false;
		this.globalTypeChanged = false;
		this.globalIdChanged = false;
		this.clientNameChanged = false;
	}
}
