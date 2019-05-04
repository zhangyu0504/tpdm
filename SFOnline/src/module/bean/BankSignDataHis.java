package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

public class BankSignDataHis {

	private boolean invTypeChanged = false;
	private boolean savAcctChanged = false;
	private boolean acctChldNumChanged = false;
	private boolean curCodeChanged = false;
	private boolean productTypeChanged = false;
	private boolean capAcctChanged = false;
	private boolean invNameChanged = false;
	private boolean idTypeChanged = false;
	private boolean invIdCodeChanged = false;
	private boolean signFlagChanged = false;
	private boolean cusMagnoChanged = false;
	private boolean depIdChanged = false;
	private boolean branchIdChanged = false;
	private boolean openDepIdChanged = false;
	private boolean openBranchIdChanged = false;
	private boolean unitTellerIdChanged = false;
	private boolean openDateChanged = false;
	private boolean openTimeChanged = false;
	private boolean txDateChanged = false;
	private boolean txTimeChanged = false;
	private boolean flagsChanged = false;
	private boolean memoChanged = false;
	private boolean subTxSeqId1Changed = false;
	private boolean subTxSeqId2Changed = false;
	private boolean subTxSeqId3Changed = false;
	private boolean delDateChanged = false;
	private boolean delTimeChanged = false;
	private boolean cardTypeChanged = false;
	private boolean bookNoChanged = false;
	private boolean initSideChanged = false;
	private boolean phoneChanged = false;
	private boolean mobileChanged = false;
	private boolean secBrchIdChanged = false;
	private boolean channelChanged = false;
	private boolean lmCardChanged = false;
	private boolean emailChanged = false;

	
	private String invType;
	private String acctId;
	private String savAcct;
	private String acctChldNum;
	private String curCode;
	private String secCompCode;
	private String productType;
	private String capAcct;
	private String invName;
	private String idType;
	private String invIdCode;
	
	private String signFlag;
	private String cusMagno;
	private String depId;
	private String branchId;
	private String openDepId;
	private String openBranchId;
	private String unitTellerId;
	private String openDate;
	private String openTime;
	private String txDate;
	private String txTime;
	private String flags;
	private String memo;
	private String subTxSeqId1;
	private String subTxSeqId2;
	private String subTxSeqId3;
	private String delDate;
	private String delTime;
	private String cardType;
	private String bookNo;
	private String initSide;
	private String phone;
	private String mobile;
	private String secBrchId;
	private String channel;
	private String lmCard;
	private String email;
	private SecCompData secCompBean = new SecCompData();
	
	public String getInvType() {
		return invType;
	}

	public void setInvType(String invType) {
		this.invType = invType;
		this.invTypeChanged = true;
	}

	public String getAcctId() {
		return acctId;
	}

	public void setAcctId(String acctId) {
		this.acctId = acctId;
	}

	public String getSavAcct() {
		return savAcct;
	}

	public void setSavAcct(String savAcct) {
		this.savAcct = savAcct;
	}

	public String getAcctChldNum() {
		return acctChldNum;
	}

	public void setAcctChldNum(String acctChldNum) {
		this.acctChldNum = acctChldNum;
		this.acctChldNumChanged = true;
	}

	public String getCurCode() {
		return curCode;
	}

	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged = true;
	}

	public String getSecCompCode() {
		return secCompCode;
	}

	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
		this.productTypeChanged = true;
	}

	public String getCapAcct() {
		return capAcct;
	}

	public void setCapAcct(String capAcct) {
		this.capAcct = capAcct;
		this.capAcctChanged = true;
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

	public String getInvIdCode() {
		return invIdCode;
	}

	public void setInvIdCode(String invIdCode) {
		this.invIdCode = invIdCode;
		this.invIdCodeChanged = true;
	}

	public String getSignFlag() {
		return signFlag;
	}

	public void setSignFlag(String signFlag) {
		this.signFlag = signFlag;
		this.signFlagChanged = true;
	}

	public String getCusMagno() {
		return cusMagno;
	}

	public void setCusMagno(String cusMagno) {
		this.cusMagno = cusMagno;
		this.cusMagnoChanged = true;
	}

	public String getDepId() {
		return depId;
	}

	public void setDepId(String depId) {
		this.depId = depId;
		this.depIdChanged = true;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
		this.branchIdChanged = true;
	}

	public String getOpenDepId() {
		return openDepId;
	}

	public void setOpenDepId(String openDepId) {
		this.openDepId = openDepId;
		this.openDepIdChanged = true;
	}

	public String getOpenBranchId() {
		return openBranchId;
	}

	public void setOpenBranchId(String openBranchId) {
		this.openBranchId = openBranchId;
		this.openBranchIdChanged = true;
	}

	public String getUnitTellerId() {
		return unitTellerId;
	}

	public void setUnitTellerId(String unitTellerId) {
		this.unitTellerId = unitTellerId;
		this.unitTellerIdChanged = true;
	}

	public String getOpenDate() {
		return openDate;
	}

	public void setOpenDate(String openDate) {
		this.openDate = openDate;
		this.openDateChanged = true;
	}

	public String getOpenTime() {
		return openTime;
	}

	public void setOpenTime(String openTime) {
		this.openTime = openTime;
		this.openTimeChanged = true;
	}

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate(String txDate) {
		this.txDate = txDate;
		this.txDateChanged = true;
	}

	public String getTxTime() {
		return txTime;
	}

	public void setTxTime(String txTime) {
		this.txTime = txTime;
		this.txTimeChanged = true;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
		this.flagsChanged = true;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}

	public String getSubTxSeqId1() {
		return subTxSeqId1;
	}

	public void setSubTxSeqId1(String subTxSeqId1) {
		this.subTxSeqId1 = subTxSeqId1;
		this.subTxSeqId1Changed = true;
	}

	public String getSubTxSeqId2() {
		return subTxSeqId2;
	}

	public void setSubTxSeqId2(String subTxSeqId2) {
		this.subTxSeqId2 = subTxSeqId2;
		this.subTxSeqId2Changed = true;
	}

	public String getSubTxSeqId3() {
		return subTxSeqId3;
	}

	public void setSubTxSeqId3(String subTxSeqId3) {
		this.subTxSeqId3 = subTxSeqId3;
		this.subTxSeqId3Changed = true;
	}

	public String getDelDate() {
		return delDate;
	}

	public void setDelDate(String delDate) {
		this.delDate = delDate;
		this.delDateChanged = true;
	}

	public String getDelTime() {
		return delTime;
	}

	public void setDelTime(String delTime) {
		this.delTime = delTime;
		this.delTimeChanged = true;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
		this.cardTypeChanged = true;
	}

	public String getBookNo() {
		return bookNo;
	}

	public void setBookNo(String bookNo) {
		this.bookNo = bookNo;
		this.bookNoChanged = true;
	}

	public String getInitSide() {
		return initSide;
	}

	public void setInitSide(String initSide) {
		this.initSide = initSide;
		this.initSideChanged = true;
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

	public String getSecBrchId() {
		return secBrchId;
	}

	public void setSecBrchId(String secBrchId) {
		this.secBrchId = secBrchId;
		this.secBrchIdChanged = true;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
		this.channelChanged = true;
	}

	public String getLmCard() {
		return lmCard;
	}

	public void setLmCard(String lmCard) {
		this.lmCard = lmCard;
		this.lmCardChanged = true;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		this.emailChanged = true;
	}	


	public SecCompData getSecCompBean() {
		return secCompBean;
	}

	public void setSecCompBean(SecCompData secCompBean) {
		this.secCompBean = secCompBean;
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
	

	public SQLStruct getSaveBankSignDataHisSQLStruct() throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append(" MERGE INTO TRDBANKSIGNDATAHIS T1");
		buffer.append(" USING (SELECT ? AS INVTYPE, ? AS ACCTID, ? AS SAVACCT, ? AS ACCTCHLDNUM, ? AS CURCODE, ? AS SECCOMPCODE, ? AS PRODUCTTYPE,");
		buffer.append(" ? AS CAPACCT, ? AS INVNAME, ? AS IDTYPE, ? AS INVIDCODE, ? AS SIGNFLAG, ? AS CUSMAGNO, ? AS DEPID, ? AS BRANCHID, ? AS OPENDEPID,");
		buffer.append(" ? AS OPENBRANCHID, ? AS UNITTELLERID, ? AS OPENDATE, ? AS OPENTIME, ? AS TXDATE, ? AS TXTIME, ? AS FLAGS, ? AS MEMO, ? AS SUBTXSEQID1,");
		buffer.append(" ? AS SUBTXSEQID2, ? AS SUBTXSEQID3, ? AS DELDATE, ? AS DELTIME, ? AS CARDTYPE, ? AS BOOKNO, ? AS INITSIDE, ? AS PHONE, ? AS MOBILE,");
		buffer.append(" ? AS SECBRCHID, ? AS CHANNEL, ? AS LMCARD, ? AS EMAIL");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.ACCTID = T2.ACCTID and T1.SECCOMPCODE = T2.SECCOMPCODE)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (invTypeChanged) {
			buffer.append("T1.INVTYPE = T2.INVTYPE,");
		}
		if (savAcctChanged) {
			buffer.append("T1.SAVACCT = T2.SAVACCT,");
		}
		if (acctChldNumChanged) {
			buffer.append("T1.ACCTCHLDNUM = T2.ACCTCHLDNUM,");
		}
		if (curCodeChanged) {
			buffer.append("T1.CURCODE = T2.CURCODE,");
		}
		if (productTypeChanged) {
			buffer.append("T1.PRODUCTTYPE = T2.PRODUCTTYPE,");
		}
		if (capAcctChanged) {
			buffer.append("T1.CAPACCT = T2.CAPACCT,");
		}
		if (invNameChanged) {
			buffer.append("T1.INVNAME = T2.INVNAME,");
		}
		if (idTypeChanged) {
			buffer.append("T1.IDTYPE = T2.IDTYPE,");
		}
		if (invIdCodeChanged) {
			buffer.append("T1.INVIDCODE = T2.INVIDCODE,");
		}
		if (signFlagChanged) {
			buffer.append("T1.SIGNFLAG = T2.SIGNFLAG,");
		}
		if (cusMagnoChanged) {
			buffer.append("T1.CUSMAGNO = T2.CUSMAGNO,");
		}
		if (depIdChanged) {
			buffer.append("T1.DEPID = T2.DEPID,");
		}
		if (branchIdChanged) {
			buffer.append("T1.BRANCHID = T2.BRANCHID,");
		}
		if (openDepIdChanged) {
			buffer.append("T1.OPENDEPID = T2.OPENDEPID,");
		}
		if (openBranchIdChanged) {
			buffer.append("T1.OPENBRANCHID = T2.OPENBRANCHID,");
		}
		if (unitTellerIdChanged) {
			buffer.append("T1.UNITTELLERID = T2.UNITTELLERID,");
		}
		if (openDateChanged) {
			buffer.append("T1.OPENDATE = T2.OPENDATE,");
		}
		if (openTimeChanged) {
			buffer.append("T1.OPENTIME = T2.OPENTIME,");
		}
		if (txDateChanged) {
			buffer.append("T1.TXDATE = T2.TXDATE,");
		}
		if (txTimeChanged) {
			buffer.append("T1.TXTIME = T2.TXTIME,");
		}
		if (flagsChanged) {
			buffer.append("T1.FLAGS = T2.FLAGS,");
		}
		if (memoChanged) {
			buffer.append("T1.MEMO = T2.MEMO,");
		}
		if (subTxSeqId1Changed) {
			buffer.append("T1.SUBTXSEQID1 = T2.SUBTXSEQID1,");
		}
		if (subTxSeqId2Changed) {
			buffer.append("T1.SUBTXSEQID2 = T2.SUBTXSEQID2,");
		}
		if (subTxSeqId3Changed) {
			buffer.append("T1.SUBTXSEQID3 = T2.SUBTXSEQID3,");
		}
		if (delDateChanged) {
			buffer.append("T1.DELDATE = T2.DELDATE,");
		}
		if (delTimeChanged) {
			buffer.append("T1.DELTIME = T2.DELTIME,");
		}
		if (cardTypeChanged) {
			buffer.append("T1.CARDTYPE = T2.CARDTYPE,");
		}
		if (bookNoChanged) {
			buffer.append("T1.BOOKNO = T2.BOOKNO,");
		}
		if (initSideChanged) {
			buffer.append("T1.INITSIDE = T2.INITSIDE,");
		}
		if (phoneChanged) {
			buffer.append("T1.PHONE = T2.PHONE,");
		}
		if (mobileChanged) {
			buffer.append("T1.MOBILE = T2.MOBILE,");
		}
		if (secBrchIdChanged) {
			buffer.append("T1.SECBRCHID = T2.SECBRCHID,");
		}
		if (channelChanged) {
			buffer.append("T1.CHANNEL = T2.CHANNEL,");
		}
		if (lmCardChanged) {
			buffer.append("T1.LMCARD = T2.LMCARD,");
		}
		if (emailChanged) {
			buffer.append("T1.EMAIL = T2.EMAIL");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(INVTYPE,ACCTID,SAVACCT,ACCTCHLDNUM,CURCODE,SECCOMPCODE,PRODUCTTYPE,CAPACCT,INVNAME,IDTYPE,");
		buffer.append("INVIDCODE,SIGNFLAG,CUSMAGNO,DEPID,BRANCHID,OPENDEPID,OPENBRANCHID,UNITTELLERID,OPENDATE,OPENTIME,TXDATE,");
		buffer.append("TXTIME,FLAGS,MEMO,SUBTXSEQID1,SUBTXSEQID2,SUBTXSEQID3,DELDATE,DELTIME,CARDTYPE,BOOKNO,INITSIDE,PHONE,");
		buffer.append("MOBILE,SECBRCHID,CHANNEL,LMCARD,EMAIL)values(T2.INVTYPE,T2.ACCTID,T2.SAVACCT,T2.ACCTCHLDNUM,T2.CURCODE,");
		buffer.append("T2.SECCOMPCODE,T2.PRODUCTTYPE,T2.CAPACCT,T2.INVNAME,T2.IDTYPE,T2.INVIDCODE,T2.SIGNFLAG,T2.CUSMAGNO,");
		buffer.append("T2.DEPID,T2.BRANCHID,T2.OPENDEPID,T2.OPENBRANCHID,T2.UNITTELLERID,T2.OPENDATE,T2.OPENTIME,T2.TXDATE,");
		buffer.append("T2.TXTIME,T2.FLAGS,T2.MEMO,T2.SUBTXSEQID1,T2.SUBTXSEQID2,T2.SUBTXSEQID3,T2.DELDATE,T2.DELTIME,T2.CARDTYPE,");
		buffer.append("T2.BOOKNO,T2.INITSIDE,T2.PHONE,T2.MOBILE,T2.SECBRCHID,T2.CHANNEL,T2.LMCARD,T2.EMAIL)");

		return new SQLStruct(buffer.toString(), valueObj);
	}

	/**
	 * ÷ÿ÷√À˘”–changeFlag
	 */
	public void resetChangedFlag() {
		this.invTypeChanged = false;
		this.savAcctChanged = false;
		this.acctChldNumChanged = false;
		this.curCodeChanged = false;
		this.productTypeChanged = false;
		this.capAcctChanged = false;
		this.invNameChanged = false;
		this.idTypeChanged = false;
		this.invIdCodeChanged = false;
		this.signFlagChanged = false;
		this.cusMagnoChanged = false;
		this.depIdChanged = false;
		this.branchIdChanged = false;
		this.openDepIdChanged = false;
		this.openBranchIdChanged = false;
		this.unitTellerIdChanged = false;
		this.openDateChanged = false;
		this.openTimeChanged = false;
		this.txDateChanged = false;
		this.txTimeChanged = false;
		this.flagsChanged = false;
		this.memoChanged = false;
		this.subTxSeqId1Changed = false;
		this.subTxSeqId2Changed = false;
		this.subTxSeqId3Changed = false;
		this.delDateChanged = false;
		this.delTimeChanged = false;
		this.cardTypeChanged = false;
		this.bookNoChanged = false;
		this.initSideChanged = false;
		this.phoneChanged = false;
		this.mobileChanged = false;
		this.secBrchIdChanged = false;
		this.channelChanged = false;
		this.lmCardChanged = false;
		this.emailChanged = false;
	}

}
