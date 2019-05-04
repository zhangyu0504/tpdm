package module.trans.bankchl2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.AllyData;
import module.bean.BankSignData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.dao.AcctJourDao;
import module.dao.AllyDataDao;
import module.dao.BankSignDataDao;
import module.trans.TranBase;
import module.trans.sf2secu.B2SRevocationClient;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (������������) ԤԼ���� ������:200121
 * 
 * 
 * 
 */
public class T200121 extends TranBase {

	private AcctJour acctJour = null;
	private AllyData allyData = null;
	private BankSignDataDao bankSignDataDao = new BankSignDataDao();
	private AcctJourDao acctJourDao = new AcctJourDao();

	@Override
	public void doHandle(Context context) throws SFException {
		try {
			// ��ʼ��ȯ��
			doSecu(context);

		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("����ʧ��", e.getMessage()));
		}
	}

	@Override
	public void doHost(Context context) throws SFException {

	}

	@Override
	public void doSecu(Context context) throws SFException {
		SFLogger.info(context, "doSecu()��ʼ");

		String secSeqId = "";
		String ProductType = "03";
		String invName = SFUtil.getReqDataValue(context, "INV_NAME");// �ͻ�����
		String idType = SFUtil.getReqDataValue(context, "ID_TYPE");// ֤������
		String idCode = SFUtil.getReqDataValue(context, "INV_ID_CODE");// ֤��ID
		String invType = SFUtil.getReqDataValue(context, "INV_TYPE");// �ͻ�����
		String acctId = SFUtil.getReqDataValue(context, "ACCT_ID");// �����ʺ�
		String bookNo = SFUtil.getReqDataValue(context, "BOOK_SERIAL_NO");// ԤԼ˳���
		String secCompCode = SFUtil.getReqDataValue(context, "SEC_COMP_CODE");// ȯ�̴���
		String InitSide = SFUtil.getReqDataValue(context, "INIT_SIDE");// ��������
		String tranDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������DateUtil.getMacDate();// ��������
		String tranTime = DateUtil.getMacTime();// ����ʱ��

		String frontLogNo = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);// 14λ��ʼ��ˮ��
		String subTxSeqId = BizUtil.getSubTxSeqId(frontLogNo);//16λsubTxSeqId��ˮ��initSeqId+������
		String newFrontLogNo = BizUtil.getChlSeqId(context,subTxSeqId);// 22λESB��ˮ��
		String tempBizSeq = SFUtil.getDataValue(context, "APP_HEAD.BIZ_SEQ_NO");//ҵ����ˮ��
		int count = 0;

		SecCompData secCompData = SFUtil.getDataValue(context,
				SFConst.PUBLIC_SECU);// ȯ����Ϣ
		idType = SFUtil.getDataValue(context, SFConst.PUBLIC_ID_TYPE);// ת�����֤������

		try {

			SFLogger.info(context, "ȯ���Ƿ����������Ԥָ��ҵ��ʼ��ʼ");
			SFUtil.chkCond(context,!"1".equals(secCompData.getBankPreSignFlag()), "ST5422",	"��ȯ�̲��������[����Ԥָ������]");
			SFLogger.info(context, "ȯ���Ƿ����������Ԥָ��ҵ��ʼ����");

			SFLogger.info(context, "����Ƿ��������������˽��׿�ʼ");
			AllyDataDao allyDataDao = new AllyDataDao();
			allyData = allyDataDao.qryAllyData(context, tranConnection, acctId);
			if (null != allyData) {
				//cardType = "2";
				SFUtil.chkCond(context, "ST5120", "��֤�������������˽���");
			} else {
				//cardType = "1";
			}
			SFLogger.info(context, "����Ƿ��������������˽��׽���");

			SFLogger.info(context, "���TrdBankSignData��ԤԼ��¼�Ƿ���ڿ�ʼ");
			BankSignData bankSignData = bankSignDataDao.qryBankSignDataCheck(context, tranConnection, secCompData.getSecCompCode(),acctId, bookNo);
			SFUtil.chkCond(context, SFUtil.isEmpty(bankSignData), "ST5803","��ѯ[ԤԼ��Ϣ]���޼�¼");
			SFLogger.info(context, "���TrdBankSignData��ԤԼ��¼�Ƿ���ڽ���");
			String signFalg = bankSignData.getSignFlag();
			SFLogger.info(context, "������ˮ��ʼ");
			DBHandler.beginTransaction(context, tranConnection);// ��������
			// ��ʼ������
			acctJour = new AcctJour();
			acctJour.setTxDate(tranDate);
			acctJour.setUserId(SFConst.DEFAULT_USER_ID);
			acctJour.setInitSide(InitSide);
			acctJour.setTxSeqId(BizUtil.getTxSeqId(frontLogNo));
			acctJour.setSecSeqId(secSeqId);
			acctJour.setSubTxSeqId(subTxSeqId);
			acctJour.setInvType(invType);
			acctJour.setInvName(invName);
			acctJour.setIdType(idType);
			acctJour.setInvIdCode(idCode);
			acctJour.setSecAcct("888888888888888888888");
			acctJour.setSecAcctSeq(0);
			acctJour.setSecCompCode(secCompCode);
			acctJour.setCapAcct("");
			acctJour.setAcctId(acctId);
			acctJour.setSavAcct("");
			acctJour.setOpenDepId("");
			acctJour.setOpenBranchId("");
			acctJour.setCurCode("RMB");
			acctJour.setDcFlag("C");
			acctJour.setTxAmount(new BigDecimal(0.00));
			acctJour.setAcctBal(new BigDecimal(0.00));
			acctJour.setAbst("");
			acctJour.setAbstractStr("����ԤԼ����");
			acctJour.setJourFlag("33");
			acctJour.setTxCode(SFConst.SF_TX_CODE_BANK_CANCEL);
			acctJour.setBusiType(SFConst.BUSI_TYPE_BANK_CANCEL);
			acctJour.setTxTime(tranTime);
			acctJour.setAbnDealTimes(0);
			acctJour.setDepId("");
			acctJour.setBranchId("");
			acctJour.setUnitTellerId("");
			acctJour.setCashRemitFlag(SFConst.REMIT_FLAG);
			acctJour.setAcctDealId(frontLogNo);
			acctJour.setProductType(ProductType);
			acctJour.setColFlag("0");
			acctJour.setMemo(bookNo);
			acctJour.setTranSeqId(newFrontLogNo);
			acctJour.setBusiSeqId(tempBizSeq);

			acctJourDao.saveAcctJour(context, tranConnection, acctJour);
			DBHandler.commitTransaction(context, tranConnection);// �ύ����

			SFLogger.info(context, "������ˮ�����");

			SFLogger.info(context, "��ȯ�̿�ʼ");
			DBHandler.beginTransaction(context, tranConnection);// ��������
			if ("1".equals(secCompData.getBankPreDesMode())) {
				// �޸�TrdBankSignData��״̬
				bankSignData = new BankSignData();
				bankSignData.setDelDate(tranDate);
				bankSignData.setDelTime(tranTime);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				count = bankSignDataDao.updBankSignDataBySignFlag(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895", String.format("�޸����ݿ����ʧ��[%s]", "TRDBANKSIGNDATA"));

				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				// ԤԼ����ʱ����¼��������Ԥָ����ʷ��Ϣ��
				DBHandler.beginTransaction(context, tranConnection);// ��������
				bankSignDataDao.migrateBankSignDataToHistory(context, tranConnection, acctId, secCompCode);
				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				// ɾ������Ԥָ����Ϣ��
				DBHandler.beginTransaction(context, tranConnection);// ��������
				bankSignData = new BankSignData();
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				bankSignDataDao.delBankSignDataByBookNo(context,tranConnection, acctId, secCompCode, bookNo);
				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				// ��ͨ��ʵʱ��ȯ�̴���
			} else {
				DBHandler.beginTransaction(context, tranConnection);// ��������
				// �޸�TrdBankSignData��״̬
				bankSignData = new BankSignData();
				bankSignData.setDelDate(tranDate);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				count = bankSignDataDao.updBankSignDataSignFlagToSendMSG(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895",String.format("�޸����ݿ����ʧ��[%s]", "TRDBANKSIGNDATA"));

				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				String retFlag = null;
				Context secuContext = null;

				if (SFConst.SECU_SZT.equals(secCompData.getSztFlag())) {
					// ��֤ͨ
					// ��ȯ�̲���map
					Map<String, Object> secuMap = new HashMap<String, Object>();
					secuMap.put("BIZ_SEQ_NO", subTxSeqId);
					secuMap.put("INV_NAME", invName);
					secuMap.put("ID_TYPE", idType);
					secuMap.put("TRN_ID_CODE", idCode);
					secuMap.put("ACCT_ID", acctId);
					secuMap.put("CAP_ACCT", "");
					secuMap.put("SEC_COMP_TYPE", "");
					secuMap.put("SEC_COMP_CODE", secCompCode);
					secuMap.put("SEC_BRCH_ID", "");
					secuMap.put("BEGIN_BAL", "");
					secuMap.put("CUR_CODE", "RMB");
					secuMap.put("LEGAL_NAME", "");
					secuMap.put("LEGAL_ID_TYPE", "");

					B2SRevocationClient secuClient = new B2SRevocationClient();
					secuContext = secuClient.send(context, secuMap);

					retFlag = SFUtil.getDataValue(secuContext,SFConst.PUBLIC_RET_FLAG);

				} else {
					// ����ֱ��ģʽȯ�̣��ݲ�����ֱ�ӱ���
					SFUtil.chkCond(context, "5003", "ԤԼ��������ֱ��ģʽȯ��,���д���ʧ�ܣ�");
				}

				// ����ȯ��ʧ�ܺ���
				if (!SFConst.RET_SUCCESS.equals(retFlag)) {
					SFLogger.info(context, "����ȯ��ʧ�ܺ���-��ʼ");
					// �޸�TrdBankSignData��״̬
					DBHandler.beginTransaction(context, tranConnection);// ��������bankSignData
					//String signFlag = bankSignData.getSignFlag();
					bankSignData = new BankSignData();
					bankSignData.setDelDate(tranDate);
					bankSignData.setSubTxSeqId3(subTxSeqId);
					bankSignData.setAcctId(acctId);
					bankSignData.setBookNo(bookNo);
					bankSignData.setSignFlag(signFalg);
					bankSignData.setDelTime("");
					bankSignData.setSecCompCode(secCompData.getSecCompCode());
					count = bankSignDataDao.updBankSignDataBySignFlagToRollback(context,tranConnection, bankSignData);
					SFUtil.chkCond(context, count <= 0, "ST4895", String.format("����ȯ��ʧ��,����[%s]��״̬ʧ��", "TRDBANKSIGNDATA"));
					DBHandler.commitTransaction(context, tranConnection);// �ύ����

					// ������ˮ��
					DBHandler.beginTransaction(context, tranConnection);// ��������
					count = acctJourDao.updAcctJourDataByJourFlag(context,tranConnection, "02", secSeqId, "����ȯ��ʧ��", tranDate,subTxSeqId);
					SFUtil.chkCond(context, count <= 0, "ST4895",String.format("����ȯ��ʧ��,����[%s]��״̬ʧ��", "TRDACCTJOUR"));
					DBHandler.commitTransaction(context, tranConnection);// �ύ����
					SFLogger.info(context, "����ȯ��ʧ�ܺ���-����");
				}
			}

			DBHandler.beginTransaction(context, tranConnection);// ��������
			acctJour = new AcctJour();
			acctJour.setSecSeqId(secSeqId);
			acctJour.setTxDate(tranDate);
			acctJour.setSubTxSeqId(subTxSeqId);
			count = acctJourDao.updAcctJourDataByJourFlag(context,tranConnection, "00", secSeqId, null, tranDate, subTxSeqId);
			SFUtil.chkCond(context, count <= 0, "ST4895",String.format("����ȯ�̳ɹ�,����[%s]��״̬ʧ��", "TRDACCTJOUR"));
			DBHandler.commitTransaction(context, tranConnection);// �ύ����

			// ʵʱ��ȯ�̵ĺ�������
			if ("2".equals(secCompData.getBankPreDesMode())) {
				DBHandler.beginTransaction(context, tranConnection);// ��������
				bankSignData = new BankSignData();
				bankSignData.setSignFlag("8");
				bankSignData.setDelTime(tranTime);
				bankSignData.setAcctId(acctId);
				bankSignData.setSecCompCode(secCompCode);
				bankSignData.setBookNo(bookNo);
				bankSignData.setDelDate(tranDate);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				count = bankSignDataDao.updBankSignDataBySignFlagToRollback(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895",String.format("��ȯ�̳ɹ���,����[%s]��ʧ��", "TRDBANKSIGNDATA"));
				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				// ԤԼ����ʱ����¼��������Ԥָ����ʷ��Ϣ��
				DBHandler.beginTransaction(context, tranConnection);// ��������
				bankSignDataDao.migrateBankSignDataToHistory(context,tranConnection, acctId, secCompCode);
				DBHandler.commitTransaction(context, tranConnection);// �ύ����

				// ɾ������Ԥָ����Ϣ��
				DBHandler.beginTransaction(context, tranConnection);// ��������
				bankSignData = new BankSignData();
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				bankSignDataDao.delBankSignDataByBookNo(context,tranConnection, acctId, secCompCode, bookNo);
				DBHandler.commitTransaction(context, tranConnection);// �ύ����
			}
			SFLogger.info(context, "��ȯ�̽���");
			// �鷵��ȯ�̱���
			SFUtil.setResDataValue( context, "ACCT_ID", acctId );
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "BOOK_SERIAL_NO", bookNo );
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("����ʧ��", e.getMessage()));
		}

		SFLogger.info(context, "doSecu()����");
	}

	@Override
	protected void chkStart(Context context) throws SFException {

	}

	@Override
	protected void chkEnd(Context context) throws SFException {

	}

	@Override
	protected void initialize(Context context) throws SFException {

	}

}
