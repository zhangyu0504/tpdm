package module.bean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;
import common.util.SFUtil;

/**
 * �˻�����������ϸ
 * @author ������
 *
 */
public class AcctAdmDetail {

	private String txDate; //��������
	private String initSide; //���׷���
	private boolean initSideChanged = false;
	private String userId; //�ͻ����
	private boolean userIdChanged = false;
	private String txSeqId; //���𷽽�����ˮ��
	private boolean txSeqIdChanged = false;
	private String secSeqId; //ȯ����ˮ��
	private boolean secSeqIdChanged = false;
	private String subTxSeqId; //����ƽ̨��ˮ��
	private String invType; //�ͻ�����
	private boolean invTypeChanged = false;
	private String invName; //Ͷ��������
	private boolean invNameChanged = false;
	private String idType; //֤������
	private boolean idTypeChanged = false;
	private String invIdCode; //֤������
	private boolean invIdCodeChanged = false;
	private String oldInvName; //Ͷ��������
	private boolean oldInvNameChanged = false;
	private String odIdType; //֤������
	private boolean odIdTypeChanged = false;
	private String oldInvIdCode; //֤������
	private boolean oldInvIdCodeChanged = false;
	private String secAcct; //��֤������˺�
	private boolean secAcctChanged = false;
	private int secAcctSeq; //��֤������˻�˳���
	private boolean secAcctSeqChanged = false;
	private String secCompCode; //ȯ�̱��
	private boolean secCompCodeChanged = false;
	private String capAcct; //ȯ�̶˿ͻ��ʽ�̨�˺�
	private boolean capAcctChanged = false;
	private String acctId; //�����˺�
	private boolean acctIdChanged = false;
	private String openDepId; //�˻���������
	private boolean openDepIdChanged = false;
	private String openBranchId; //�˻���������
	private boolean openBranchIdChanged = false;
	private String oldAcctId; //�������˺�
	private boolean oldAcctIdChanged = false;
	private String curCode; //����
	private boolean curCodeChanged = false;
	private String dcFlag; //�����־
	private boolean dcFlagChanged = false;
	private BigDecimal txAmount; //�������
	private boolean txAmountChanged = false;
	private String abStract; //ժҪ��
	private boolean abStractChanged = false;
	private String abstractStr; //ժҪ
	private boolean abstractStrChanged = false;
	private String jourFlag; //��ˮ��־
	private boolean jourFlagChanged = false;
	private String signFlag; //ǩԼ��־
	private boolean signFlagChanged = false;
	private String txCode; //���״���
	private boolean txCodeChanged = false;

	private String busiType; //ҵ������
	private boolean busiTypeChanged = false;
	private String txTime; //����ʱ��
	private boolean txTimeChanged = false;
	private String depId; //���������
	private boolean depIdChanged = false;
	private String branchId; //���׷���
	private boolean branchIdChanged = false;
	private String unitTellerId;//�������Ա���
	private boolean unitTellerIdChanged = false;
	private String cashRemitFlag;//�����ʶ
	private boolean cashRemitFlagChanged = false;
	private String cusMagNo;//�ͻ�������
	private boolean cusMagNoChanged = false;
	private String acctDealId;//���׼�����ˮ��
	private boolean acctDealIdChanged = false;
	private  String acctHostSeqId;//������ˮ��
	private boolean acctHostSeqIdChanged = false;
	private String preSeqId;//ǰ����ˮ��
	private boolean preSeqIdChanged = false;
	private String acctDealDate;//������������
	private boolean acctDealDateChanged = false;
	private String colFlag;//���˱�־
	private boolean colFlagChanged = false;
	private  String memo;//��ע
	private boolean memoChanged = false;
	private String nSignFlag; //���֮��ǩԼ��־0-ǩԼ 1-ǩԼ������ 2-����ԤǩԼ 3-ȯ��Ԥָ�� 4-����ǩԼ 5-���������� 6-ǩԼȷ�ϴ�����
	private boolean nSignFlagChanged = false;
	private String tranSeqId;//������ˮ��
	private boolean tranSeqIdChanged = false;
	private String busiSeqId;//ҵ����ˮ��
	private boolean busiSeqIdChanged = false;
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
		this.initSideChanged=true;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
		this.userIdChanged=true;
	}
	public String getTxSeqId() {
		return txSeqId;
	}
	public void setTxSeqId(String txSeqId) {
		this.txSeqId = txSeqId;
		this.txSeqIdChanged=true;
	}
	public String getSecSeqId() {
		return secSeqId;
	}
	public void setSecSeqId(String secSeqId) {
		this.secSeqId = secSeqId;
		this.secSeqIdChanged=true;
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
		this.invTypeChanged=true;
	}
	public String getInvName() {
		return invName;
	}
	public void setInvName(String invName) {
		this.invName = invName;
		this.invNameChanged=true;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
		this.idTypeChanged=true;
	}
	public String getInvIdCode() {
		return invIdCode;
	}
	public void setInvIdCode(String invIdCode) {
		this.invIdCode = invIdCode;
		this.invIdCodeChanged=true;
	}
	public String getOldInvName() {
		return oldInvName;
	}
	public void setOldInvName(String oldInvName) {
		this.oldInvName = oldInvName;
		this.oldInvNameChanged=true;
	}
	public String getOdIdType() {
		return odIdType;
	}
	public void setOdIdType(String odIdType) {
		this.odIdType = odIdType;
		this.odIdTypeChanged=true;
	}
	public String getOldInvIdCode() {
		return oldInvIdCode;
	}
	public void setOldInvIdCode(String oldInvIdCode) {
		this.oldInvIdCode = oldInvIdCode;
		this.oldInvIdCodeChanged=true;
	}
	public String getSecAcct() {
		return secAcct;
	}
	public void setSecAcct(String secAcct) {
		this.secAcct = secAcct;
		this.secAcctChanged=true;
	}
	public int getSecAcctSeq() {
		return secAcctSeq;
	}
	public void setSecAcctSeq(int secAcctSeq) {
		this.secAcctSeq = secAcctSeq;
		this.secAcctSeqChanged=true;
	}
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
		this.secCompCodeChanged=true;
	}
	public String getCapAcct() {
		return capAcct;
	}
	public void setCapAcct(String capAcct) {
		this.capAcct = capAcct;
		this.capAcctChanged=true;
	}
	public String getAcctId() {
		return acctId;
	}
	public void setAcctId(String acctId) {
		this.acctId = acctId;
		this.acctIdChanged=true;
	}
	public String getOpenDepId() {
		return SFUtil.isNotEmpty( openDepId ) ? openDepId.trim() : openDepId;
	}
	public void setOpenDepId(String openDepId) {
		this.openDepId = openDepId;
		this.openDepIdChanged=true;
	}
	public String getOpenBranchId() {
		return SFUtil.isNotEmpty( openBranchId ) ? openBranchId.trim() : openBranchId;
	}
	public void setOpenBranchId(String openBranchId) {
		this.openBranchId = openBranchId;
		this.openBranchIdChanged=true;
	}
	public String getOldAcctId() {
		return oldAcctId;
	}
	public void setOldAcctId(String oldAcctId) {
		this.oldAcctId = oldAcctId;
		this.oldAcctIdChanged=true;
	}
	public String getCurCode() {
		return curCode;
	}
	public void setCurCode(String curCode) {
		this.curCode = curCode;
		this.curCodeChanged=true;
	}
	public String getDcFlag() {
		return dcFlag;
	}
	public void setDcFlag(String dcFlag) {
		this.dcFlag = dcFlag;
		this.dcFlagChanged=true;
	}
	public BigDecimal getTxAmount() {
		return txAmount;
	}
	public void setTxAmount(BigDecimal txAmount) {
		this.txAmount = txAmount;
		this.txAmountChanged=true;
	}
	public String getAbStract() {
		return abStract;
	}
	public void setAbStract(String abStract) {
		this.abStract = abStract;
		this.abStractChanged=true;
	}
	public String getAbstractStr() {
		return abstractStr;
	}
	public void setAbstractStr(String abstractStr) {
		this.abstractStr = abstractStr;
		this.abstractStrChanged=true;
	}
	public String getJourFlag() {
		return jourFlag;
	}
	public void setJourFlag(String jourFlag) {
		this.jourFlag = jourFlag;
		this.jourFlagChanged=true;
	}
	public String getSignFlag() {
		return signFlag;
	}
	public void setSignFlag(String signFlag) {
		this.signFlag = signFlag;
		this.signFlagChanged=true;
	}
	public String getTxCode() {
		return txCode;
	}
	public void setTxCode(String txCode) {
		this.txCode = txCode;
		this.txCodeChanged=true;
	}
	public String getBusiType() {
		return busiType;
	}
	public void setBusiType(String busiType) {
		this.busiType = busiType;
		this.busiTypeChanged=true;
	}
	public String getTxTime() {
		return txTime;
	}
	public void setTxTime(String txTime) {
		this.txTime = txTime;
		this.txTimeChanged=true;
	}
	public String getDepId() {
		return SFUtil.isNotEmpty( depId ) ? depId.trim() : depId;
	}
	public void setDepId(String depId) {
		this.depId = depId;
		this.depIdChanged=true;
	}
	public String getBranchId() {
		return SFUtil.isNotEmpty( branchId ) ? branchId.trim() : branchId;
	}
	public void setBranchId(String branchId) {
		this.branchId = branchId;
		this.branchIdChanged=true;
	}
	public String getUnitTellerId() {
		return unitTellerId;
	}
	public void setUnitTellerId(String unitTellerId) {
		this.unitTellerId = unitTellerId;
		this.unitTellerIdChanged=true;
	}
	public String getCashRemitFlag() {
		return cashRemitFlag;
	}
	public void setCashRemitFlag(String cashRemitFlag) {
		this.cashRemitFlag = cashRemitFlag;
		this.cashRemitFlagChanged=true;
	}
	public String getCusMagNo() {
		return SFUtil.isNotEmpty( cusMagNo ) ? cusMagNo.trim() : cusMagNo;
	}
	public void setCusMagNo(String cusMagNo) {
		this.cusMagNo = cusMagNo;
		this.cusMagNoChanged=true;
	}
	public String getAcctDealId() {
		return acctDealId;
	}
	public void setAcctDealId(String acctDealId) {
		this.acctDealId = acctDealId;
		this.acctDealIdChanged=true;
	}
	public String getAcctHostSeqId() {
		return acctHostSeqId;
	}
	public void setAcctHostSeqId(String acctHostSeqId) {
		this.acctHostSeqId = acctHostSeqId;
		this.acctHostSeqIdChanged=true;
	}
	public String getPreSeqId() {
		return preSeqId;
	}
	public void setPreSeqId(String preSeqId) {
		this.preSeqId = preSeqId;
		this.preSeqIdChanged=true;
	}
	public String getAcctDealDate() {
		return acctDealDate;
	}
	public void setAcctDealDate(String acctDealDate) {
		this.acctDealDate = acctDealDate;
		this.acctDealDateChanged=true;
	}
	public String getColFlag() {
		return colFlag;
	}
	public void setColFlag(String colFlag) {
		this.colFlag = colFlag;
		this.colFlagChanged=true;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged=true;
	}
	public String getTranSeqId() {
		return tranSeqId;
	}
	public void setTranSeqId(String tranSeqId) {
		this.tranSeqId = tranSeqId;
		this.tranSeqIdChanged=true;
	}
	public String getBusiSeqId() {
		return busiSeqId;
	}
	public void setBusiSeqId(String busiSeqId) {
		this.busiSeqId = busiSeqId;
		this.busiSeqIdChanged=true;
	}
	public String getnSignFlag() {
		return nSignFlag;
	}
	public void setnSignFlag(String nSignFlag) {
		this.nSignFlag = nSignFlag;
		this.nSignFlagChanged=true;
	}
	
	/**
	 * ��������changeFlag
	 */
	public void resetChangedFlag() {
		this.initSideChanged = false;
		this.userIdChanged = false;
		this.txSeqIdChanged = false;
		this.secSeqIdChanged = false;
		this.invTypeChanged = false;
		this.invNameChanged = false;
		this.idTypeChanged = false;
		this.invIdCodeChanged = false;
		this.oldInvNameChanged = false;
		this.odIdTypeChanged = false;
		this.oldInvIdCodeChanged = false;
		this.secAcctChanged = false;
		this.secAcctSeqChanged = false;
		this.secCompCodeChanged = false;
		this.capAcctChanged = false;
		this.acctIdChanged = false;
		this.openDepIdChanged = false;
		this.openBranchIdChanged = false;
		this.oldAcctIdChanged = false;
		this.curCodeChanged = false;
		this.dcFlagChanged = false;
		this.txAmountChanged = false;
		this.abStractChanged = false;
		this.abstractStrChanged = false;
		this.jourFlagChanged = false;
		this.signFlagChanged = false;
		this.nSignFlagChanged = false;
		this.txCodeChanged = false;
		this.busiTypeChanged = false;
		this.txTimeChanged = false;
		this.depIdChanged = false;
		this.branchIdChanged = false;
		this.unitTellerIdChanged = false;
		this.cashRemitFlagChanged = false;
		this.cusMagNoChanged = false;
		this.acctDealIdChanged = false;
		this.acctHostSeqIdChanged = false;
		this.preSeqIdChanged = false;
		this.acctDealDateChanged = false;
		this.colFlagChanged = false;
		this.memoChanged = false;
		this.tranSeqIdChanged = false;
		this.busiSeqIdChanged = false;
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean) {
					continue;
				}
				fieldValueList.add( f.get( this ) );
			} catch( Exception e ) {
				throw new SFException(e);
			}
		}
		return fieldValueList.toArray();
	}
	
	public SQLStruct getSaveAcctDetailSQLStruct(String tableName) throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();		

		buffer.append("MERGE INTO " + tableName + " T1");
		buffer.append(" USING (SELECT ? as TXDATE, ? as INITSIDE, ? as USERID, ? as TXSEQID, ? as SECSEQID, ? as SUBTXSEQID,");
		buffer.append(" ? as INVTYPE, ? as INVNAME, ? as IDTYPE, ? as INVIDCODE, ? as OLDINVNAME, ? as OLDIDTYPE, ? as OLDINVIDCODE, ? as SECACCT,");
		buffer.append(" ? as SECACCTSEQ, ? as SECCOMPCODE, ? as CAPACCT, ? as ACCTID, ? as OPENDEPID, ? as OPENBRANCHID, ? as OLDACCTID, ? as CURCODE,");
		buffer.append(" ? as DCFLAG, ? as TXAMOUNT, ? as ABSTRACT, ? as ABSTRACTSTR, ? as JOURFLAG, ? as SIGNFLAG, ? as TXCODE, ? as BUSITYPE,");
		buffer.append(" ? as TXTIME, ? as DEPID, ? as BRANCHID, ? as UNITTELLERID, ? as CASHREMITFLAG, ? as CUSMAGNO, ? as ACCTDEALID,");
		buffer.append(" ? as ACCTHOSTSEQID, ? as PRESEQID, ? as ACCTDEALDATE, ? as COLFLAG, ? as MEMO, ? as NSIGNFLAG, ? as TRANSEQID , ? as BUSISEQID");
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
		if (invNameChanged) {
			buffer.append("T1.INVNAME = T2.INVNAME,");
		}
		if (idTypeChanged) {
			buffer.append("T1.IDTYPE = T2.IDTYPE,");
		}
		if (invIdCodeChanged) {
			buffer.append("T1.INVIDCODE = T2.INVIDCODE,");
		}
		if (oldInvNameChanged) {
			buffer.append("T1.OLDINVNAME = T2.OLDINVNAME,");
		}
		if (odIdTypeChanged) {
			buffer.append("T1.OLDIDTYPE = T2.OLDIDTYPE,");
		}
		if (oldInvIdCodeChanged) {
			buffer.append("T1.OLDINVIDCODE = T2.OLDINVIDCODE,");
		}
		if (secAcctChanged) {
			buffer.append("T1.SECACCT = T2.SECACCT,");
		}
		if (secAcctSeqChanged) {
			buffer.append("T1.SECACCTSEQ = T2.SECACCTSEQ,");
		}
		if (secCompCodeChanged) {
			buffer.append("T1.SECCOMPCODE = T2.SECCOMPCODE,");
		}
		if (capAcctChanged) {
			buffer.append("T1.CAPACCT = T2.CAPACCT,");
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
		if (oldAcctIdChanged) {
			buffer.append("T1.OLDACCTID = T2.OLDACCTID,");
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
		if (abStractChanged) {
			buffer.append("T1.ABSTRACT = T2.ABSTRACT,");
		}
		if (abstractStrChanged) {
			buffer.append("T1.ABSTRACTSTR = T2.ABSTRACTSTR,");
		}
		if (jourFlagChanged) {
			buffer.append("T1.JOURFLAG = T2.JOURFLAG,");
		}
		if (signFlagChanged) {
			buffer.append("T1.SIGNFLAG = T2.SIGNFLAG,");
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
		if (cusMagNoChanged) {
			buffer.append("T1.CUSMAGNO = T2.CUSMAGNO,");
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
		if (nSignFlagChanged) {
			buffer.append("T1.NSIGNFLAG = T2.NSIGNFLAG,");
		}
		if (tranSeqIdChanged) {
			buffer.append("T1.TRANSEQID = T2.TRANSEQID,");
		}
		if (busiSeqIdChanged) {
			buffer.append("T1.BUSISEQID = T2.BUSISEQID");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(TXDATE,INITSIDE,USERID,TXSEQID,SECSEQID,SUBTXSEQID,INVTYPE,INVNAME,IDTYPE,");
		buffer.append(" INVIDCODE,OLDINVNAME,OLDIDTYPE,OLDINVIDCODE,SECACCT,SECACCTSEQ,SECCOMPCODE,");
		buffer.append(" CAPACCT,ACCTID,OPENDEPID,OPENBRANCHID,OLDACCTID,CURCODE,DCFLAG,TXAMOUNT,");
		buffer.append(" ABSTRACT,ABSTRACTSTR,JOURFLAG,SIGNFLAG,TXCODE,BUSITYPE,TXTIME,DEPID,BRANCHID,");
		buffer.append(" UNITTELLERID,CASHREMITFLAG,CUSMAGNO,ACCTDEALID,ACCTHOSTSEQID,PRESEQID,ACCTDEALDATE,");
		buffer.append(" COLFLAG,MEMO,NSIGNFLAG,TRANSEQID,BUSISEQID )");
		buffer.append(" values");
		buffer.append("(T2.TXDATE,T2.INITSIDE,T2.USERID,T2.TXSEQID,T2.SECSEQID,T2.SUBTXSEQID,T2.INVTYPE,T2.INVNAME,T2.IDTYPE,");
		buffer.append("T2.INVIDCODE,T2.OLDINVNAME,T2.OLDIDTYPE,T2.OLDINVIDCODE,T2.SECACCT,T2.SECACCTSEQ,T2.SECCOMPCODE,");
		buffer.append("T2.CAPACCT,T2.ACCTID,T2.OPENDEPID,T2.OPENBRANCHID,T2.OLDACCTID,T2.CURCODE,T2.DCFLAG,T2.TXAMOUNT,");
		buffer.append("T2.ABSTRACT,T2.ABSTRACTSTR,T2.JOURFLAG,T2.SIGNFLAG,T2.TXCODE,T2.BUSITYPE,T2.TXTIME,T2.DEPID,T2.BRANCHID,");
		buffer.append("T2.UNITTELLERID,T2.CASHREMITFLAG,T2.CUSMAGNO,T2.ACCTDEALID,T2.ACCTHOSTSEQID,T2.PRESEQID,T2.ACCTDEALDATE,");
		buffer.append("T2.COLFLAG,T2.MEMO,T2.NSIGNFLAG,T2.TRANSEQID,T2.BUSISEQID)");

		return new SQLStruct(buffer.toString(), valueObj);

	}
	
}