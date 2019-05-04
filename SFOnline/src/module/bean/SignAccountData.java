package module.bean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;
import common.util.SFUtil;

/**
 * 签约信息表实体类
 * @author 申双江
 *
 */
public class SignAccountData {

	private boolean initSideChanged = false;
	private boolean secAcctChanged = false;
	private boolean secAcctSeqChanged = false;
	private boolean productTypeChanged = false;
	private boolean curCodeChanged = false;
	private boolean cashRemitFlagChanged = false;
	private boolean shsthCodeChanged = false;
	private boolean szsthCodeChanged = false;
	private boolean acctIdChanged = false;
	private boolean savAcctChanged = false;
	private boolean acctChldNumChanged = false;
	private boolean openDepIdChanged = false;
	private boolean openBranchIdChanged = false;
	private boolean invNameChanged = false;
	private boolean invTypeChanged = false;
	private boolean signFlagChanged = false;
	private boolean depIdChanged = false;
	private boolean branchIdChanged = false;
	private boolean desDepIdChanged = false;
	private boolean desBranchIdChanged = false;
	private boolean unitTellerIdChanged = false;
	private boolean desUnitTellerIdChanged = false;
	private boolean cusMagnoChanged = false;
	private boolean statFlagChanged = false;
	private boolean openDateChanged = false;
	private boolean closeDateChanged = false;
	private boolean preTxDateChanged = false;
	private boolean beginBalChanged = false;
	private boolean acctBalChanged = false;
	private boolean isMailBillChanged = false;
	private boolean mailDateChanged = false;
	private boolean flagsChanged = false;
	private boolean signDateChanged = false;
	private boolean activeFlagChanged = false;
	private boolean channelChanged = false;
	private boolean transferFlagChanged = false;
	private boolean signModeChanged = false;
	private boolean lmCardChanged = false;
	private boolean iiAcctCtlChanged = false;
	
	private String initSide;
	private String secAcct;
	private int secAcctSeq;
	private String secCompCode;
	private String productType;
	private String capAcct;
	private String curCode;
	private String cashRemitFlag;
	private String shsthCode;
	private String szsthCode;
	private String acctId;
	private String savAcct;
	private String acctChldNum;
	private String openDepId;
	private String openBranchId;
	private String invName;
	private String invType;
	private String signFlag;
	private String depId;
	private String branchId;
	private String desDepId;
	private String desBranchId;
	private String unitTellerId;
	private String desUnitTellerId;
	private String cusMagno;
	private String statFlag;
	private String openDate;
	private String closeDate;
	private String preTxDate;
	private BigDecimal beginBal;
	private BigDecimal acctBal;
	private String isMailBill;
	private String mailDate;
	private String flags;
	private String signDate;
	private String activeFlag;
	private String channel;
	private String transferFlag;
	private String signMode;
	private String lmCard;
	private String iiAcctCtl;
	
	private SecCompData secCompData = new SecCompData();
	private InvestData investData = new InvestData();
	
	
	public SecCompData getSecCompData() {
		return secCompData;
	}

	
	public void setSecCompData( SecCompData secCompData ) {
		this.secCompData = secCompData;
	}

	
	public InvestData getInvestData() {
		return investData;
	}

	
	public void setInvestData( InvestData investData ) {
		this.investData = investData;
	}

	public String getInitSide() {
		return initSide;
	}

	public void setInitSide(String initSide) {
		this.initSide = initSide;
		this.initSideChanged = true;
	}

	public String getSecAcct() {
		return secAcct;
	}

	public void setSecAcct(String secAcct) {
		this.secAcct = secAcct;
		this.secAcctChanged = true;
	}

	public int getSecAcctSeq() {
		return secAcctSeq;
	}

	public void setSecAcctSeq(int secAcctSeq) {
		this.secAcctSeq = secAcctSeq;
		this.secAcctSeqChanged = true;
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
	}

	public String getCurCode() {
		return curCode;
	}

	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged = true;
	}

	public String getCashRemitFlag() {
		return cashRemitFlag;
	}

	public void setCashRemitFlag(String cashRemitFlag) {
		this.cashRemitFlag = cashRemitFlag;
		this.cashRemitFlagChanged = true;
	}

	public String getShsthCode() {
		return shsthCode;
	}

	public void setShsthCode(String shsthCode) {
		this.shsthCode = shsthCode;
		this.shsthCodeChanged = true;
	}

	public String getSzsthCode() {
		return szsthCode;
	}

	public void setSzsthCode(String szsthCode) {
		this.szsthCode = szsthCode;
		this.szsthCodeChanged = true;
	}

	public String getAcctId() {
		return acctId;
	}

	public void setAcctId(String acctId) {
		this.acctId = acctId;
		this.acctIdChanged = true;
	}

	public String getSavAcct() {
		return savAcct;
	}

	public void setSavAcct(String savAcct) {
		this.savAcct = savAcct;
		this.savAcctChanged = true;
	}

	public String getAcctChldNum() {
		return acctChldNum;
	}

	public void setAcctChldNum(String acctChldNum) {
		this.acctChldNum = acctChldNum;
		this.acctChldNumChanged = true;
	}

	public String getOpenDepId() {
		return SFUtil.isNotEmpty( openDepId ) ? openDepId.trim() : openDepId;
	}

	public void setOpenDepId(String openDepId) {
		this.openDepId = openDepId;
		this.openDepIdChanged = true;
	}

	public String getOpenBranchId() {
		return SFUtil.isNotEmpty( openBranchId ) ? openBranchId.trim() : openBranchId;
	}

	public void setOpenBranchId(String openBranchId) {
		this.openBranchId = openBranchId;
		this.openBranchIdChanged = true;
	}

	public String getInvName() {
		return invName;
	}

	public void setInvName(String invName) {
		this.invName = invName;
		this.invNameChanged = true;
	}

	public String getInvType() {
		return invType;
	}

	public void setInvType(String invType) {
		this.invType = invType;
		this.invTypeChanged = true;
	}

	public String getSignFlag() {
		return signFlag;
	}

	public void setSignFlag(String signFlag) {
		this.signFlag = signFlag;
		this.signFlagChanged = true;
	}

	public String getDepId() {
		return SFUtil.isNotEmpty( depId ) ? depId.trim() : depId;
	}

	public void setDepId(String depId) {
		this.depId = depId;
		this.depIdChanged = true;
	}

	public String getBranchId() {
		return SFUtil.isNotEmpty( branchId ) ? branchId.trim() : branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
		this.branchIdChanged = true;
	}

	public String getDesDepId() {
		return SFUtil.isNotEmpty( desDepId ) ? desDepId.trim() : desDepId;
	}

	public void setDesDepId(String desDepId) {
		this.desDepId = desDepId;
		this.desDepIdChanged = true;
	}

	public String getDesBranchId() {
		return SFUtil.isNotEmpty( desBranchId ) ? desBranchId.trim() : desBranchId;
	}

	public void setDesBranchId(String desBranchId) {
		this.desBranchId = desBranchId;
		this.desBranchIdChanged = true;
	}

	public String getUnitTellerId() {
		return SFUtil.isNotEmpty( unitTellerId ) ? unitTellerId.trim() : unitTellerId;
	}

	public void setUnitTellerId(String unitTellerId) {
		this.unitTellerId = unitTellerId;
		this.unitTellerIdChanged = true;
	}

	public String getDesUnitTellerId() {
		return SFUtil.isNotEmpty( desUnitTellerId ) ? desUnitTellerId.trim() : desUnitTellerId;
	}

	public void setDesUnitTellerId(String desUnitTellerId) {
		this.desUnitTellerId = desUnitTellerId;
		this.desUnitTellerIdChanged = true;
	}

	public String getCusMagno() {
		return SFUtil.isNotEmpty( cusMagno ) ? cusMagno.trim() : cusMagno;
	}

	public void setCusMagno(String cusMagno) {
		this.cusMagno = cusMagno;
		this.cusMagnoChanged = true;
	}

	public String getStatFlag() {
		return statFlag;
	}

	public void setStatFlag(String statFlag) {
		this.statFlag = statFlag;
		this.statFlagChanged = true;
	}

	public String getOpenDate() {
		return openDate;
	}

	public void setOpenDate(String openDate) {
		this.openDate = openDate;
		this.openDateChanged = true;
	}

	public String getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(String closeDate) {
		this.closeDate = closeDate;
		this.closeDateChanged = true;
	}

	public String getPreTxDate() {
		return preTxDate;
	}

	public void setPreTxDate(String preTxDate) {
		this.preTxDate = preTxDate;
		this.preTxDateChanged = true;
	}

	public BigDecimal getBeginBal() {
		return beginBal;
	}

	public void setBeginBal(BigDecimal beginBal) {
		this.beginBal = beginBal;
		this.beginBalChanged = true;
	}

	public BigDecimal getAcctBal() {
		return acctBal;
	}

	public void setAcctBal(BigDecimal acctBal) {
		this.acctBal = acctBal;
		this.acctBalChanged = true;
	}

	public String getIsMailBill() {
		return isMailBill;
	}

	public void setIsMailBill(String isMailBill) {
		this.isMailBill = isMailBill;
		this.isMailBillChanged = true;
	}

	public String getMailDate() {
		return mailDate;
	}

	public void setMailDate(String mailDate) {
		this.mailDate = mailDate;
		this.mailDateChanged = true;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
		this.flagsChanged = true;
	}

	public String getSignDate() {
		return signDate;
	}

	public void setSignDate(String signDate) {
		this.signDate = signDate;
		this.signDateChanged = true;
	}

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
		this.activeFlagChanged = true;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
		this.channelChanged = true;
	}

	public String getTransferFlag() {
		return transferFlag;
	}

	public void setTransferFlag(String transferFlag) {
		this.transferFlag = transferFlag;
		this.transferFlagChanged = true;
	}

	public String getSignMode() {
		return signMode;
	}

	public void setSignMode(String signMode) {
		this.signMode = signMode;
		this.signModeChanged = true;
	}

	public String getLmCard() {
		return lmCard;
	}

	public void setLmCard(String lmCard) {
		this.lmCard = lmCard;
		this.lmCardChanged = true;
	}

	public String getIiAcctCtl() {
		return iiAcctCtl;
	}

	public void setIiAcctCtl(String iiAcctCtl) {
		this.iiAcctCtl = iiAcctCtl;
		this.iiAcctCtlChanged = true;
	}		


	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean || f.get(this) instanceof SecCompData || f.get(this) instanceof InvestData) {
					continue;
				}
				fieldValueList.add(f.get(this));
			} catch (Exception e) {
				throw new SFException(e);
			} 
		}
		return fieldValueList.toArray();
	}
	

	public SQLStruct getSaveSignAcctDataSQLStruct(String tableName) throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();

		buffer.append(" MERGE INTO " + tableName + " T1");
		buffer.append(" USING (SELECT ? AS INITSIDE, ? AS SECACCT, ? AS SECACCTSEQ, ? AS SECCOMPCODE, ? AS PRODUCTTYPE, ? AS CAPACCT, ? AS CURCODE,");
		buffer.append(" ? AS CASHREMITFLAG, ? AS SHSTHCODE, ? AS SZSTHCODE, ? AS ACCTID, ? AS SAVACCT, ? AS ACCTCHLDNUM, ? AS OPENDEPID, ? AS OPENBRANCHID,");
		buffer.append(" ? AS INVNAME, ? AS INVTYPE, ? AS SIGNFLAG, ? AS DEPID, ? AS BRANCHID, ? AS DESDEPID, ? AS DESBRANCHID, ? AS UNITTELLERID, ? AS DESUNITTELLERID,");
		buffer.append(" ? AS CUSMAGNO, ? AS STATFLAG, ? AS OPENDATE, ? AS CLOSEDATE, ? AS PRETXDATE, ? AS BEGINBAL, ? AS ACCTBAL, ? AS ISMAILBILL, ? AS MAILDATE,");
		buffer.append(" ? AS FLAGS, ? AS SIGNDATE, ? AS ACTIVEFLAG, ? AS CHANNEL, ? AS TRANSFERFLAG, ? AS SIGNMODE, ? AS LMCARD, ? AS IIACCTCTL");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.SECCOMPCODE = T2.SECCOMPCODE AND T1.CAPACCT = T2.CAPACCT)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (initSideChanged) {
			buffer.append("T1.INITSIDE = T2.INITSIDE,");
		}
		if (secAcctChanged) {
			buffer.append("T1.SECACCT = T2.SECACCT,");
		}
		if (productTypeChanged) {
			buffer.append("T1.PRODUCTTYPE = T2.PRODUCTTYPE,");
		}
		if (secAcctSeqChanged) {
			buffer.append("T1.SECACCTSEQ = T2.SECACCTSEQ,");
		}
		if (curCodeChanged) {
			buffer.append("T1.CURCODE = T2.CURCODE,");
		}
		if (cashRemitFlagChanged) {
			buffer.append("T1.CASHREMITFLAG = T2.CASHREMITFLAG,");
		}
		if (shsthCodeChanged) {
			buffer.append("T1.SHSTHCODE = T2.SHSTHCODE,");
		}
		if (szsthCodeChanged) {
			buffer.append("T1.SZSTHCODE = T2.SZSTHCODE,");
		}
		if (acctIdChanged) {
			buffer.append("T1.ACCTID = T2.ACCTID,");
		}
		if (savAcctChanged) {
			buffer.append("T1.SAVACCT = T2.SAVACCT,");
		}
		if (acctChldNumChanged) {
			buffer.append("T1.ACCTCHLDNUM = T2.ACCTCHLDNUM,");
		}
		if (openDepIdChanged) {
			buffer.append("T1.OPENDEPID = T2.OPENDEPID,");
		}
		if (openBranchIdChanged) {
			buffer.append("T1.OPENBRANCHID = T2.OPENBRANCHID,");
		}
		if (invNameChanged) {
			buffer.append("T1.INVNAME = T2.INVNAME,");
		}
		if (invTypeChanged) {
			buffer.append("T1.INVTYPE = T2.INVTYPE,");
		}
		if (signFlagChanged) {
			buffer.append("T1.SIGNFLAG = T2.SIGNFLAG,");
		}
		if (depIdChanged) {
			buffer.append("T1.DEPID = T2.DEPID,");
		}
		if (branchIdChanged) {
			buffer.append("T1.BRANCHID = T2.BRANCHID,");
		}
		if (desDepIdChanged) {
			buffer.append("T1.DESDEPID = T2.DESDEPID,");
		}
		if (desBranchIdChanged) {
			buffer.append("T1.DESBRANCHID = T2.DESBRANCHID,");
		}
		if (unitTellerIdChanged) {
			buffer.append("T1.UNITTELLERID = T2.UNITTELLERID,");
		}
		if (desUnitTellerIdChanged) {
			buffer.append("T1.DESUNITTELLERID = T2.DESUNITTELLERID,");
		}
		if (cusMagnoChanged) {
			buffer.append("T1.CUSMAGNO = T2.CUSMAGNO,");
		}
		if (statFlagChanged) {
			buffer.append("T1.STATFLAG = T2.STATFLAG,");
		}
		if (openDateChanged) {
			buffer.append("T1.OPENDATE = T2.OPENDATE,");
		}
		if (closeDateChanged) {
			buffer.append("T1.CLOSEDATE = T2.CLOSEDATE,");
		}
		if (preTxDateChanged) {
			buffer.append("T1.PRETXDATE = T2.PRETXDATE,");
		}
		if (beginBalChanged) {
			buffer.append("T1.BEGINBAL = T2.BEGINBAL,");
		}
		if (acctBalChanged) {
			buffer.append("T1.ACCTBAL = T2.ACCTBAL,");
		}
		if (isMailBillChanged) {
			buffer.append("T1.ISMAILBILL = T2.ISMAILBILL,");
		}
		if (mailDateChanged) {
			buffer.append("T1.MAILDATE = T2.MAILDATE,");
		}
		if (flagsChanged) {
			buffer.append("T1.FLAGS = T2.FLAGS,");
		}
		if (signDateChanged) {
			buffer.append("T1.SIGNDATE = T2.SIGNDATE,");
		}
		if (activeFlagChanged) {
			buffer.append("T1.ACTIVEFLAG = T2.ACTIVEFLAG,");
		}
		if (channelChanged) {
			buffer.append("T1.CHANNEL = T2.CHANNEL,");
		}
		if (transferFlagChanged) {
			buffer.append("T1.TRANSFERFLAG = T2.TRANSFERFLAG,");
		}
		if (signModeChanged) {
			buffer.append("T1.SIGNMODE = T2.SIGNMODE,");
		}
		if (lmCardChanged) {
			buffer.append("T1.LMCARD = T2.LMCARD,");
		}
		if (iiAcctCtlChanged) {
			buffer.append("T1.IIACCTCTL = T2.IIACCTCTL");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(INITSIDE,SECACCT,SECACCTSEQ,SECCOMPCODE,PRODUCTTYPE,CAPACCT,CURCODE,CASHREMITFLAG,SHSTHCODE,SZSTHCODE,");
		buffer.append("ACCTID,SAVACCT,ACCTCHLDNUM,OPENDEPID,OPENBRANCHID,INVNAME,INVTYPE,SIGNFLAG,DEPID,BRANCHID,DESDEPID,DESBRANCHID,");
		buffer.append("UNITTELLERID,DESUNITTELLERID,CUSMAGNO,STATFLAG,OPENDATE,CLOSEDATE,PRETXDATE,BEGINBAL,ACCTBAL,ISMAILBILL,MAILDATE,");
		buffer.append("FLAGS,SIGNDATE,ACTIVEFLAG,CHANNEL,TRANSFERFLAG,SIGNMODE,LMCARD,IIACCTCTL)values(T2.INITSIDE,T2.SECACCT,T2.SECACCTSEQ,");
		buffer.append("T2.SECCOMPCODE,T2.PRODUCTTYPE,T2.CAPACCT,T2.CURCODE,T2.CASHREMITFLAG,T2.SHSTHCODE,T2.SZSTHCODE,T2.ACCTID,T2.SAVACCT,");
		buffer.append("T2.ACCTCHLDNUM,T2.OPENDEPID,T2.OPENBRANCHID,T2.INVNAME,T2.INVTYPE,T2.SIGNFLAG,T2.DEPID,T2.BRANCHID,T2.DESDEPID,");
		buffer.append("T2.DESBRANCHID,T2.UNITTELLERID,T2.DESUNITTELLERID,T2.CUSMAGNO,T2.STATFLAG,T2.OPENDATE,T2.CLOSEDATE,T2.PRETXDATE,");
		buffer.append("T2.BEGINBAL,T2.ACCTBAL,T2.ISMAILBILL,T2.MAILDATE,T2.FLAGS,T2.SIGNDATE,T2.ACTIVEFLAG,T2.CHANNEL,T2.TRANSFERFLAG,");
		buffer.append("T2.SIGNMODE,T2.LMCARD,T2.IIACCTCTL)");

		return new SQLStruct(buffer.toString(), valueObj);
	}

	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.initSideChanged = false;
		this.secAcctChanged = false;
		this.secAcctSeqChanged = false;
		this.productTypeChanged = false;
		this.curCodeChanged = false;
		this.cashRemitFlagChanged = false;
		this.shsthCodeChanged = false;
		this.szsthCodeChanged = false;
		this.acctIdChanged = false;
		this.savAcctChanged = false;
		this.acctChldNumChanged = false;
		this.openDepIdChanged = false;
		this.openBranchIdChanged = false;
		this.invNameChanged = false;
		this.invTypeChanged = false;
		this.signFlagChanged = false;
		this.depIdChanged = false;
		this.branchIdChanged = false;
		this.desDepIdChanged = false;
		this.desBranchIdChanged = false;
		this.unitTellerIdChanged = false;
		this.desUnitTellerIdChanged = false;
		this.cusMagnoChanged = false;
		this.statFlagChanged = false;
		this.openDateChanged = false;
		this.closeDateChanged = false;
		this.preTxDateChanged = false;
		this.beginBalChanged = false;
		this.acctBalChanged = false;
		this.isMailBillChanged = false;
		this.mailDateChanged = false;
		this.flagsChanged = false;
		this.signDateChanged = false;
		this.activeFlagChanged = false;
		this.channelChanged = false;
		this.transferFlagChanged = false;
		this.signModeChanged = false;
		this.lmCardChanged = false;
		this.iiAcctCtlChanged = false;
	}

}
