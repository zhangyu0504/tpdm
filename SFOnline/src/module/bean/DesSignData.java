package module.bean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.util.SFUtil;

public class DesSignData {

	private boolean initSideChanged = false;
	private boolean secAcctChanged = false;
	private boolean secAcctSeqChanged = false;
	private boolean secCompCodeChanged = false;
	private boolean productTypeChanged = false;
	private boolean capAcctChanged = false;
	private boolean curCodeChanged = false;
	private boolean shsthCodeChanged = false;
	private boolean szsthCodeChanged = false;
	private boolean cashRemitFlagChanged = false;
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
	private boolean openDateChanged = false;
	private boolean closeDateChanged = false;
	private boolean preTxDateChanged = false;
	private boolean beginBalChanged = false;
	private boolean acctBalChanged = false;
	private boolean isMailBillChanged = false;
	private boolean mailDateChanged = false;
	private boolean flagsChanged = false;
	private boolean channelChanged = false;

	private String initSide;
	private String secAcct;
	private int secAcctSeq;
	private String secCompCode;
	private String productType;
	private String capAcct;
	private String curCode;
	private String shsthCode;
	private String szsthCode;
	private String cashRemitFlag;
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
	private String openDate;
	private String closeDate;
	private String preTxDate;
	private BigDecimal beginBal;
	private BigDecimal acctBal;
	private String isMailBill;
	private String mailDate;
	private String flags;
	private String channel;

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
		this.secCompCodeChanged = true;
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

	public String getCurCode() {
		return curCode;
	}

	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged = true;
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

	public String getCashRemitFlag() {
		return cashRemitFlag;
	}

	public void setCashRemitFlag(String cashRemitFlag) {
		this.cashRemitFlag = cashRemitFlag;
		this.cashRemitFlagChanged = true;
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

	public String getInvnName() {
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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
		this.channelChanged = true;
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
				throw new SFException(e);
			} 
		}
		return fieldValueList.toArray();
	}
	


	/**
	 * ��������changeFlag
	 */
	public void resetChangedFlag() {
		this.initSideChanged = false;
		this.secAcctChanged = false;
		this.secAcctSeqChanged = false;
		this.secCompCodeChanged = false;
		this.productTypeChanged = false;
		this.capAcctChanged = false;
		this.curCodeChanged = false;
		this.shsthCodeChanged = false;
		this.szsthCodeChanged = false;
		this.cashRemitFlagChanged = false;
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
		this.openDateChanged = false;
		this.closeDateChanged = false;
		this.preTxDateChanged = false;
		this.beginBalChanged = false;
		this.acctBalChanged = false;
		this.isMailBillChanged = false;
		this.mailDateChanged = false;
		this.flagsChanged = false;
		this.channelChanged = false;
	}

}
