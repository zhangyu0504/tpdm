package module.bean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;
import common.util.SFConst;
import common.util.SFUtil;

/**
 * 交易详情表实体类
 * @author 申双江
 *
 */
public class AcctDetail {

	private String txDate;
	
	private String initSide;
	
	private String userId;
	
	private String txSeqId;
	
	private String secSeqId;
	
	private String subTxSeqId;
	
	private String invType;
	
	private String secAcct;
	
	private int secAcctSeq;
	
	private String acctId;
	
	private String openDepId;
	
	private String openBranchId;
	
	private String secCompCode;
	
	private String secCompName;
	
	private String capAcct;
	
	private String curCode;
	
	private String dcFlag;
	
	private BigDecimal txAmount;
	
	private BigDecimal acctBal;
	
	private String abst;// 对应TRDACCTJOUR表abstract字段
	
	private String abstractStr;
	
	private String jourFlag;
	
	private String txCode;
	
	private String busiType;
	
	private String txTime;
	
	private String depId;
	
	private String branchId;
	
	private String unitTellerId;
	
	private String cashRemitFlag;
	
	private String acctDealId;
	
	private String acctHostSeqId;
	
	private String preSeqId;
	
	private String acctDealDate;
	
	private String colFlag;
	
	private String memo;
	
	private String tranSeqId;
	
	private String busiSeqId;
	
	private String UMId;
	
	private boolean initSideChanged = false;
	private boolean userIdChanged = false;
	private boolean txSeqIdChanged = false;
	private boolean secSeqIdChanged = false;
	private boolean invTypeChanged = false;
	private boolean secAcctChanged = false;
	private boolean secAcctSeqChanged = false;
	private boolean acctIdChanged = false;
	private boolean openDepIdChanged = false;
	private boolean openBranchIdChanged = false;
	private boolean secCompCodeChanged = false;
	private boolean capAcctChanged = false;
	private boolean curCodeChanged = false;
	private boolean dcFlagChanged = false;
	private boolean txAmountChanged = false;
	private boolean acctBalChanged = false;
	private boolean abstChanged = false;
	private boolean abstractStrChanged = false;
	private boolean jourFlagChanged = false;
	private boolean txCodeChanged = false;
	private boolean busiTypeChanged = false;
	private boolean txTimeChanged = false;
	private boolean depIdChanged = false;
	private boolean branchIdChanged = false;
	private boolean unitTellerIdChanged = false;
	private boolean cashRemitFlagChanged = false;
	private boolean acctDealIdChanged = false;
	private boolean acctHostSeqIdChanged = false;
	private boolean preSeqIdChanged = false;
	private boolean acctDealDateChanged = false;
	private boolean colFlagChanged = false;
	private boolean memoChanged = false;
	private boolean tranSeqIdChanged = false;
	private boolean busiSeqIdChanged = false;
	private boolean UMIdChanged = false;

	private SecCompData secCompBean = new SecCompData();
	

	public String getSecCompName() {
		return secCompName;
	}

	public void setSecCompName(String secCompName) {
		this.secCompName = secCompName;
	}

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}

	public String getInitSide() {
		return initSide;
	}

	public void setInitSide(String initSide) {
		this.initSide = initSide;
		this.initSideChanged = true;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
		this.userIdChanged = true;
	}

	public String getTxSeqId() {
		return txSeqId;
	}

	public void setTxSeqId(String txSeqId) {
		this.txSeqId = txSeqId;
		this.txSeqIdChanged = true;
	}

	public String getSecSeqId() {
		return secSeqId;
	}

	public void setSecSeqId(String secSeqId) {
		this.secSeqId = secSeqId;
		this.secSeqIdChanged = true;
	}

	public String getSubTxSeqId() {
		return subTxSeqId;
	}

	public void setSubTxSeqId(String subTxSeqId) {
		this.subTxSeqId = subTxSeqId;
	}

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

	public int getSecAcctSeq() {
		return secAcctSeq;
	}

	public void setSecAcctSeq(int secAcctSeq) {
		this.secAcctSeq = secAcctSeq;
		this.secAcctSeqChanged = true;
	}

	public String getAcctId() {
		return acctId;
	}

	public void setAcctId(String acctId) {
		this.acctId = acctId;
		this.acctIdChanged = true;
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

	public String getSecCompCode() {
		return secCompCode;
	}

	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
		this.secCompCodeChanged = true;
	}

	public String getCapAcct() {
		return capAcct;
	}

	public void setCapAcct(String capAcct) {
		this.capAcct = capAcct;
		this.capAcctChanged = true;
	}

	public String getCurCode() {
		return curCode;
	}

	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged = true;
	}

	public String getDcFlag() {
		return dcFlag;
	}

	public void setDcFlag(String dcFlag) {
		this.dcFlag = dcFlag;
		this.dcFlagChanged = true;
	}

	public BigDecimal getTxAmount() {
		return txAmount;
	}

	public void setTxAmount(BigDecimal txAmount) {
		this.txAmount = txAmount;
		this.txAmountChanged = true;
	}

	public BigDecimal getAcctBal() {
		return acctBal;
	}

	public void setAcctBal(BigDecimal acctBal) {
		this.acctBal = acctBal;
		this.acctBalChanged = true;
	}

	public String getAbst() {
		return abst;
	}

	public void setAbst(String abst) {
		this.abst = abst;
		this.abstChanged = true;
	}

	public String getAbstractStr() {
		return abstractStr;
	}

	public void setAbstractStr(String abstractStr) {
		this.abstractStr = abstractStr;
		this.abstractStrChanged = true;
	}

	public String getJourFlag() {
		return jourFlag;
	}

	public void setJourFlag(String jourFlag) {
		this.jourFlag = jourFlag;
		this.jourFlagChanged = true;
	}

	public String getTxCode() {
		return txCode;
	}

	public void setTxCode(String txCode) {
		this.txCode = txCode;
		this.txCodeChanged = true;
	}

	public String getBusiType() {
		return busiType;
	}

	public void setBusiType(String busiType) {
		this.busiType = busiType;
		this.busiTypeChanged = true;
	}

	public String getTxTime() {
		return txTime;
	}

	public void setTxTime(String txTime) {
		this.txTime = txTime;
		this.txTimeChanged = true;
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

	public String getUnitTellerId() {
		return SFUtil.isNotEmpty( unitTellerId ) ? unitTellerId.trim() : unitTellerId;
	}

	public void setUnitTellerId(String unitTellerId) {
		this.unitTellerId = unitTellerId;
		this.unitTellerIdChanged = true;
	}

	public String getCashRemitFlag() {
		return cashRemitFlag;
	}

	public void setCashRemitFlag(String cashRemitFlag) {
		this.cashRemitFlag = cashRemitFlag;
		this.cashRemitFlagChanged = true;
	}

	public String getAcctDealId() {
		return acctDealId;
	}

	public void setAcctDealId(String acctDealId) {
		this.acctDealId = acctDealId;
		this.acctDealIdChanged = true;
	}

	public String getAcctHostSeqId() {
		return acctHostSeqId;
	}

	public void setAcctHostSeqId(String acctHostSeqId) {
		this.acctHostSeqId = acctHostSeqId;
		this.acctHostSeqIdChanged = true;
	}

	public String getPreSeqId() {
		return preSeqId;
	}

	public void setPreSeqId(String preSeqId) {
		this.preSeqId = preSeqId;
		this.preSeqIdChanged = true;
	}

	public String getAcctDealDate() {
		return acctDealDate;
	}

	public void setAcctDealDate(String acctDealDate) {
		this.acctDealDate = acctDealDate;
		this.acctDealDateChanged = true;
	}

	public String getColFlag() {
		return colFlag;
	}

	public void setColFlag(String colFlag) {
		this.colFlag = colFlag;
		this.colFlagChanged = true;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}

	public String getTranSeqId() {
		return tranSeqId;
	}

	public void setTranSeqId(String tranSeqId) {
		this.tranSeqId = tranSeqId;
		this.tranSeqIdChanged = true;
	}

	public String getBusiSeqId() {
		return busiSeqId;
	}

	public void setBusiSeqId(String busiSeqId) {
		this.busiSeqId = busiSeqId;
		this.busiSeqIdChanged = true;
	}

	public String getUmId() {
		return SFUtil.isNotEmpty( UMId ) ? UMId.trim() : UMId;
	}

	public void setUmId(String UMId) {
		this.UMId = UMId;
		this.UMIdChanged = true;
	}
	

	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean || f.get(this) instanceof SecCompData || f.getName().equals("secCompName")) {
					continue;
				}
				fieldValueList.add(f.get(this));
			} catch (Exception e) {
				throw new SFException(e);
			} 
		}
		return fieldValueList.toArray();
	}


	public SecCompData getSecCompBean() {
		return secCompBean;
	}

	public void setSecCompBean(SecCompData secCompBean) {
		this.secCompBean = secCompBean;
	}

	/**
	 * 交易时间段操作转账交易明细主表
	 * @return
	 * @throws SFException
	 */
	public SQLStruct getSaveAcctDetailSQLStruct(String tableName) throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();		

		buffer.append("MERGE INTO " + tableName + " T1");
		buffer.append(" USING (SELECT ? as TXDATE, ? as INITSIDE, ? as USERID, ? as TXSEQID, ? as SECSEQID, ? as SUBTXSEQID,");
		buffer.append(" ? as INVTYPE, ? as SECACCT, ? as SECACCTSEQ, ? as ACCTID, ? as OPENDEPID, ? as OPENBRANCHID,? as SECCOMPCODE,? as CAPACCT,");
//		buffer.append(" ? as INVTYPE, ? as SECACCT, ? as SECACCTSEQ, ? as SECCOMPCODE, ? as CAPACCT, ? as ACCTID, ? as OPENDEPID, ? as OPENBRANCHID,");
		buffer.append(" ? as CURCODE, ? as DCFLAG, ? as TXAMOUNT, ? as ACCTBAL, ? as ABSTRACT, ? as ABSTRACTSTR, ? as JOURFLAG, ? as TXCODE,");
		buffer.append(" ? as BUSITYPE, ? as TXTIME, ? as DEPID, ? as BRANCHID, ? as UNITTELLERID, ? as CASHREMITFLAG, ? as ACCTDEALID, ? as ACCTHOSTSEQID,");
		buffer.append(" ? as PRESEQID, ? as ACCTDEALDATE, ? as COLFLAG, ? as MEMO, ? as TRANSEQID, ? as BUSISEQID, ? as UMID");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.TXDATE = T2.TXDATE and T1.SUBTXSEQID = T2.SUBTXSEQID)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (initSideChanged) {
			buffer.append("T1.INITSIDE = T2.INITSIDE,");
		}
		if (userIdChanged) {
			buffer.append("T1.USERID = T2.USERID,");
		}
		if (txSeqIdChanged) {
			buffer.append("T1.TXSEQID = T2.TXSEQID,");
		}
		if (secSeqIdChanged) {
			buffer.append("T1.SECSEQID = T2.SECSEQID,");
		}
		if (invTypeChanged) {
			buffer.append("T1.INVTYPE = T2.INVTYPE,");
		}
		if (secAcctChanged) {
			buffer.append("T1.SECACCT = T2.SECACCT,");
		}
		if (secAcctSeqChanged) {
			buffer.append("T1.SECACCTSEQ = T2.SECACCTSEQ,");
		}
		if (acctIdChanged) {
			buffer.append("T1.ACCTID = T2.ACCTID,");
		}
		if (openDepIdChanged) {
			buffer.append("T1.OPENDEPID = T2.OPENDEPID,");
		}
		if (openBranchIdChanged) {
			buffer.append("T1.OPENBRANCHID = T2.OPENBRANCHID,");
		}
		if (secCompCodeChanged) {
			buffer.append("T1.SECCOMPCODE = T2.SECCOMPCODE,");
		}
		if (capAcctChanged) {
			buffer.append("T1.CAPACCT = T2.CAPACCT,");
		}
		if (curCodeChanged) {
			buffer.append("T1.CURCODE = T2.CURCODE,");
		}
		if (dcFlagChanged) {
			buffer.append("T1.DCFLAG = T2.DCFLAG,");
		}
		if (txAmountChanged) {
			buffer.append("T1.TXAMOUNT = T2.TXAMOUNT,");
		}
		if (acctBalChanged) {
			buffer.append("T1.ACCTBAL = T2.ACCTBAL,");
		}
		if (abstChanged) {
			buffer.append("T1.ABSTRACT = T2.ABSTRACT,");
		}
		if (abstractStrChanged) {
			buffer.append("T1.ABSTRACTSTR = T2.ABSTRACTSTR,");
		}
		if (jourFlagChanged) {
			buffer.append("T1.JOURFLAG = T2.JOURFLAG,");
		}
		if (txCodeChanged) {
			buffer.append("T1.TXCODE = T2.TXCODE,");
		}
		if (busiTypeChanged) {
			buffer.append("T1.BUSITYPE = T2.BUSITYPE,");
		}
		if (txTimeChanged) {
			buffer.append("T1.TXTIME = T2.TXTIME,");
		}
		if (depIdChanged) {
			buffer.append("T1.DEPID = T2.DEPID,");
		}
		if (branchIdChanged) {
			buffer.append("T1.BRANCHID = T2.BRANCHID,");
		}
		if (unitTellerIdChanged) {
			buffer.append("T1.UNITTELLERID = T2.UNITTELLERID,");
		}
		if (cashRemitFlagChanged) {
			buffer.append("T1.CASHREMITFLAG = T2.CASHREMITFLAG,");
		}
		if (acctDealIdChanged) {
			buffer.append("T1.ACCTDEALID = T2.ACCTDEALID,");
		}
		if (acctHostSeqIdChanged) {
			buffer.append("T1.ACCTHOSTSEQID = T2.ACCTHOSTSEQID,");
		}
		if (preSeqIdChanged) {
			buffer.append("T1.PRESEQID = T2.PRESEQID,");
		}
		if (acctDealDateChanged) {
			buffer.append("T1.ACCTDEALDATE = T2.ACCTDEALDATE,");
		}
		if (colFlagChanged) {
			buffer.append("T1.COLFLAG = T2.COLFLAG,");
		}
		if (memoChanged) {
			buffer.append("T1.MEMO = T2.MEMO,");
		}
		if (tranSeqIdChanged) {
			buffer.append("T1.TRANSEQID = T2.TRANSEQID,");
		}
		if (busiSeqIdChanged) {
			buffer.append("T1.BUSISEQID = T2.BUSISEQID,");
		}
		if (UMIdChanged) {
			buffer.append("T1.UMID = T2.UMID");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(TXDATE,INITSIDE,USERID,TXSEQID,SECSEQID,SUBTXSEQID,INVTYPE,SECACCT,SECACCTSEQ,");
		buffer.append("ACCTID,OPENDEPID,OPENBRANCHID,SECCOMPCODE,CAPACCT,CURCODE,DCFLAG,TXAMOUNT,ACCTBAL,ABSTRACT,ABSTRACTSTR,JOURFLAG,");
		buffer.append("TXCODE,BUSITYPE,TXTIME,DEPID,BRANCHID,UNITTELLERID,CASHREMITFLAG,ACCTDEALID,ACCTHOSTSEQID,PRESEQID,");
		buffer.append("ACCTDEALDATE,COLFLAG,MEMO,TRANSEQID,BUSISEQID,UMID)");
		buffer.append(" values");
		buffer.append("(T2.TXDATE,T2.INITSIDE,T2.USERID,T2.TXSEQID,T2.SECSEQID,T2.SUBTXSEQID,T2.INVTYPE,T2.SECACCT,T2.SECACCTSEQ,");
		buffer.append("T2.ACCTID,T2.OPENDEPID,T2.OPENBRANCHID,T2.SECCOMPCODE,T2.CAPACCT,T2.CURCODE,T2.DCFLAG,T2.TXAMOUNT,T2.ACCTBAL,T2.ABSTRACT,T2.ABSTRACTSTR,");
		buffer.append("T2.JOURFLAG,T2.TXCODE,T2.BUSITYPE,T2.TXTIME,T2.DEPID,T2.BRANCHID,T2.UNITTELLERID,T2.CASHREMITFLAG,T2.ACCTDEALID,T2.ACCTHOSTSEQID,T2.PRESEQID,");
		buffer.append("T2.ACCTDEALDATE,T2.COLFLAG,T2.MEMO,T2.TRANSEQID,T2.BUSISEQID,T2.UMID)");

		return new SQLStruct(buffer.toString(), valueObj);

	}
	
	

	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.initSideChanged = false;
		this.userIdChanged = false;
		this.txSeqIdChanged = false;
		this.secSeqIdChanged = false;
		this.invTypeChanged = false;
		this.secAcctChanged = false;
		this.secAcctSeqChanged = false;
		this.acctIdChanged = false;
		this.openDepIdChanged = false;
		this.openBranchIdChanged = false;
		this.secCompCodeChanged = false;
		this.capAcctChanged = false;
		this.curCodeChanged = false;
		this.dcFlagChanged = false;
		this.txAmountChanged = false;
		this.acctBalChanged = false;
		this.abstChanged = false;
		this.abstractStrChanged = false;
		this.jourFlagChanged = false;
		this.txCodeChanged = false;
		this.busiTypeChanged = false;
		this.txTimeChanged = false;
		this.depIdChanged = false;
		this.branchIdChanged = false;
		this.unitTellerIdChanged = false;
		this.cashRemitFlagChanged = false;
		this.acctDealIdChanged = false;
		this.acctHostSeqIdChanged = false;
		this.preSeqIdChanged = false;
		this.acctDealDateChanged = false;
		this.colFlagChanged = false;
		this.memoChanged = false;
		this.tranSeqIdChanged = false;
		this.busiSeqIdChanged = false;
		this.UMIdChanged = false;
	}


	public String getBusiTypeCN() {
		if (SFConst.BUSI_TYPE_B2S.equals(busiType)) {
			return "银转证";
		} else if (SFConst.BUSI_TYPE_S2B.equals(busiType)) {
			return "证转银";
		} else {
			return "其他";
		}
	}
	
	/**
	 * 渠道中文转换
	 * @return
	 */
	public String getInitSideCN() {
		if("B".equals(initSide)){
			return "柜台";
		}else if("E".equals(initSide)){
			return "机构网银";
		}else if("F".equals(initSide)){
			return "个人网银";
		}else if("T".equals(initSide)){
			return "电话银行";
		}else if("G".equals(initSide)){
			return "平安电话中心坐席";
		}else if("M".equals(initSide)){
			return "手机银行";
		}else if("S".equals(initSide)){
			return "券商";
		}else if("C".equals(initSide)){
			return "自助终端";
		}else if("D".equals(initSide)){
			return "代理第三方存管";
		}else if("A".equals(initSide)){
			return "新代理第三方存管";
		}else if("H".equals(initSide)){
			return "平安IVR";
		}else if("I".equals(initSide)){
			return "资产托管指令系统";
		}else if("J".equals(initSide)){
			return "远程一体机";
		}else{
			return "其他";
		}
	}

}
