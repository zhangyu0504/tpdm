package module.trans.cobank2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctDetail;
import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCustomerInfo;
import module.bean.AgtTranList;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.Reverse;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ProductInfoCache;
import module.communication.CoBankClientBase;
import module.communication.SecuClientBase;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AgtTranListDao;
import module.dao.ReverseDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.TransferClient;
import module.trans.sf2cobank.T810022Client;
import module.trans.sf2secu.S2BTransferClient;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (�����ж˷���)֤ת��
 * ������ :300201 
 * @author ���ź�
 * 
 */
public class T300201 extends TranBase {

	/*
	 * ���ý���ȫ�ֱ��������ڴ�Ž��׳��õ�ҵ�����chkEnd�����л�ȡ����
	 */
	private SecCompData secCompData = null;

	private SignAccountData signAccountData = null;

	private InvestData investData = null;

	private LocalInfo localInfo = null;

	private AcctJour acctJour = new AcctJour();

	private AcctDetail acctDetail = new AcctDetail();

	private ProductInfo productInfo = new ProductInfo();

	private AcctDetailDao acctDetailDao = new AcctDetailDao();

	private AcctJourDao acctJourDao = new AcctJourDao();

	private String subTxSeqId = null; // ƽ̨��ˮ��

	private String txDate = null;// ��������

	private String srcTxAmmountString = "";

	String secCompCode = null;// ȯ�̴���

	String capAcct = null;// ȯ���ʽ��˻�

	String curCode = null;

	String acctId = null;// �����ʺ�

	String initSide = null;// ����

	String secuType = null;// ȯ������ 1-��֤ͨ 0-ֱ��

	private String logId = null;// 14λ��־��

	String reqSecCompCode = null;// �������е�ԭʼȯ�̴��루��Ϊ����ȯ�̴��������������Ҫ�����������е�ԭʼȯ�̶˴��룩

	BigDecimal txAmount = new BigDecimal( 0.00 );

	private String respCode = null;// ������

	private String respMsg = null;// ������Ϣ

	@Override
	protected void initialize( Context context ) throws SFException {

		// ���׹�������
		SFUtil.addDataField( context, "CTX_ACCT_BAL", "" );// �˻����

		// ȯ�̴��봦��
		reqSecCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// �������ǰ��ȯ�̴��룬���ڷ��ر���ʱʹ��
		BizUtil.setZhongXinSecuCompCode( context );// ����ȯ�̴���

		// ���׹���������ʼ��
		logId = SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID ) );
		subTxSeqId = BizUtil.getSubTxSeqId( logId );
		secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴��룬����ȡ�������
		capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ȯ���ʽ��˻�
		curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );
		// ����Ĭ��ΪRMB
		if (SFUtil.isEmpty(curCode)) {
			curCode = SFConst.CUR_CODE_RMB;
		}

		acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );// ����

		srcTxAmmountString = SFUtil.getReqDataValue( context, "TX_AMOUNT" );

	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		String realTxAmount = AmtUtil.conv2CoBankDivAmount( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );// �����б����Է�Ϊ��λ��ת��ΪԪ

		// ���׽���С��0
		SFUtil.chkCond( context, SFUtil.object2Double( realTxAmount ) <= 0, "ST5333", "ת�˽��������0" );
		// ǧ�ڼ��
		AmtUtil.chkMaxAmount( context, SFUtil.object2Double( realTxAmount ) );

		SFUtil.setReqDataValue( context, "TX_AMOUNT", realTxAmount );// ������ԪΪ��λ�Ľ��׽��

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {

		// ��context�л�ȡ���ױ��ö��󲢽��г�ʼ����������chkSecu��chkInvestor����֮�����ȡ��
		secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
		secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������ 1-��֤ͨ 0-ֱ��
		localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
		txDate = localInfo.getWorkdate();

		SFUtil.chkCond( context, investData == null, "ST4049", "�ͻ�������Ϣ������" );
		SFUtil.chkCond( context, signAccountData == null, "ST5800", "�ͻ�ǩԼ�˺���Ϣ��ѯʧ��" );
		SFUtil.chkCond( context, "1".equals( signAccountData.getActiveFlag() ), "ST5762", "���ȴ����ж˷�����ת֤�����Լ�����ʽ��˺�" );

		productInfo = ProductInfoCache.getValue( secCompData.getSecCompCode() );// ����ȯ�̴����ȡȯ�̲�Ʒ�������
		SFUtil.chkCond( context, productInfo == null, "ST5423", "��ȯ��δ��ͨ�˱��ֵ�ҵ��" );
		BankUnit bankUnit = BankUnitCache.getValue( productInfo.getTruOpnDepId() );// ��������Ż�ȡ�����������
		SFUtil.chkCond( context, bankUnit == null, "ST5804", "��ѯ[ȯ���й�ר���˺ſ�������]ʧ��" );

		txAmount = new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );// ���׽��
		// ��ȡ�����������,������
		BigDecimal acctBal = signAccountData.getAcctBal().subtract( txAmount );
		if( "1".equals( secCompData.getOverDraftFlag() ) ) {// ��ȯ������͸֧
			if( txDate.equals( signAccountData.getOpenDate() ) ) {// �������տ������
				SFUtil.chkCond( context, SFUtil.object2Double( acctBal ) < 0, "ST4113", "��������" );
			}
		} else {
			SFUtil.chkCond( context, SFUtil.object2Double( acctBal ) < 0, "ST4113", "��������" );
		}

		SFUtil.setDataValue( context, "CTX_ACCT_BAL", acctBal );// �˻������������ģ���ʱ��Ϊδ��������׼ȷ

		String signFlag = signAccountData.getSignFlag();
		SFUtil.chkCond( context, SFConst.SIGN_FLAG_CANCEL.equals( signFlag ), "ST5540", "�ѳ���ǩԼ" );
		SFUtil.chkCond( context, !SFConst.SIGN_FLAG_SIGN.equals( signFlag ), "ST5590", "��ǰǩԼ״̬������������ҵ��" );

	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// ���ɽ�����ˮ
		addAcctJour( context );

		// ����ȯ��
		doSecu( context );

		// �����пͻ������ú����л���ס�D+����ײ�������ˮ(IOMONEY)
		doHost( context );

	}

	/**
	 * ���ú�����֤ת�������
	 * @see module.trans.TranBase#doHost(com.ecc.emp.core.Context)
	 */
	@Override
	public void doHost( Context context ) throws SFException {

		SFLogger.info( context, "doHost()��ʼ" );
		// ���ú����л���ס�D+����ף��ж��Ƿ�ɹ�
		String retFlag = this.S2BTransfer( context );
		/*
		 * ���ݽ������TRDAcctJour������ˮ���� TRDAcctDetailת�˽�����ϸ���� TRDSignAccountDataǩԼ�˻�����
		 */
		if( SFConst.RET_OVERTIME.equals( retFlag ) || SFConst.RET_FAILURE.equals( retFlag ) ) {
			// ���淵����Ϣ
			DBHandler.beginTransaction( context, tranConnection );// ��������
			acctJour.setRespCode( respCode );
			acctJour.setRespMsg( respMsg );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
			SFUtil.chkCond( context, respCode, String.format( respMsg ) );// ������ʱʧ�ܻ��ߺ�����ʧ���˳�����
		} else {// �����л���ɹ�

			DBHandler.beginTransaction( context, tranConnection );// ��������

			/* ���� TRDAcctJour ����������ǰ����ˮ�� */
			acctJour.setAcctDealDate( DateUtil.getMacDate() );
			acctJour.setJourFlag( "00" );
			acctJour.setRespCode( "000000" );
			acctJour.setRespMsg( SFConst.RESPCODE_SUCCMSG );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );

			// ���� TRDAcctDetail ����������ǰ����ˮ��
			acctDetail.setAcctDealDate( DateUtil.getMacDate() );
			acctDetail.setTranSeqId( acctJour.getTranSeqId() );
			acctDetail.setBusiSeqId( acctJour.getBusiSeqId() );
			acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );

			DBHandler.commitTransaction( context, tranConnection );// �ύ����
		}

		SFLogger.info( context, "doHost()����" );
		SFUtil.setResDataValue( context, "ACCT_ID", acctId );// �����˺�
		SFUtil.setResDataValue( context, "INV_NAME", acctJour.getInvName() );// �ͻ�����
		SFUtil.setResDataValue( context, "CUR_CODE", curCode );// ����
		SFUtil.setResDataValue( context, "TX_AMOUNT", BizUtil.getCobankTranAmount( context, srcTxAmmountString ) );// ���Է�Ϊ��λ
		SFUtil.setResDataValue( context, "SEC_COMP_NAME", secCompData.getSecCompName() );// ȯ������
		SFUtil.setResDataValue( context, "SEC_COMP_CODE", reqSecCompCode );// ȯ�̴���
		SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�
		SFUtil.setResDataValue( context, "PICS_LOG_NO", logId );// ��ˮ��
	}

	/** 
	 * ����ȯ��֤ת������
	 * @see module.trans.TranBase#doSecu(com.ecc.emp.core.Context)
	 */
	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );

		SecuClientBase secuClient = new S2BTransferClient();// ��֤ͨ��ת֤ͨѶ����
		Context secuContext = secuClient.send( context, this.getS2BTransferMap( context ) );// ��������

		// �������ؽ��
		String retFlag = SFUtil.getDataValue( secuContext, SFConst.PUBLIC_RET_FLAG );
		String respCode = null;
		String respMsg = null;
		String secSeqId = null;// ȯ�̽�����ˮ��

		// ��ȯ�̷��أ�������֤ͨ��ֱ��ȡ����Ҫ����
		if( SFConst.RET_FAILURE.equals( retFlag ) || SFConst.RET_SUCCESS.equals( retFlag ) ) {
			if( SFConst.SECU_SZT.equals( secuType ) ) {// ��֤ͨ��ʽ
				KeyedCollection kColl = SFUtil.getDataElement( secuContext, "Trf00201" );
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				respCode = SFUtil.getDataValue( context, rstKcoll, "RESP_CODE" );// ��֤ͨ���ش�����
				respMsg = SFUtil.getDataValue( context, rstKcoll, "RESP_MSG" );// ��֤ͨ���ش�����Ϣ
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��֤ͨ���سɹ�
					KeyedCollection msgKcoll = SFUtil.getDataElement( secuContext, "MsgHdr" );// ȡ����Ӧ����ͷ
					KeyedCollection rltKcoll = SFUtil.getDataElement( context, msgKcoll, "Ref" );
					secSeqId = SFUtil.getDataValue( context, rltKcoll, "Ref" );// ȡ��ȯ����ˮ��
					SFUtil.chkCond( context, SFUtil.isEmpty( secSeqId ), "ST4895", String.format( "��ȡȯ����ˮ��[%s]ʧ��", secSeqId ) );
				}
			} else {// ֱ����ʽ
				KeyedCollection kColl = SFUtil.getDataElement( secuContext, "6031_O" );
				respCode = SFUtil.getDataValue( context, kColl, "RESP_CODE" );// ֱ�����ش�����
				respMsg = SFUtil.getDataValue( context, kColl, "RESP_MSG" );// ֱ�����ش�����Ϣ

				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ֱ�����سɹ�
					secSeqId = SFUtil.getDataValue( context, kColl, "SEC_SEQ_ID" );// ȡ��ȯ����ˮ��
					SFUtil.chkCond( context, SFUtil.isEmpty( secSeqId ), "ST4895", String.format( "��ȡȯ����ˮ��[%s]ʧ��", secSeqId ) );

				}
			}
		}
		if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��ȯ�̳ɹ�
			DBHandler.beginTransaction( context, tranConnection );// ��������
			// ���½���������ѯ���Ա�������
			signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
			// �������ٴβ�ѯ�����Ϸ��Լ��
			// Double acctBal = SFUtil.object2Double(signAccountData.getAcctBal()) - txAmount; // ����֤ת��������
			SFLogger.info( context, String.format( "�ۼ�ǰ��AcctBal=[%s]", signAccountData.getAcctBal() ) );
			BigDecimal acctBal = signAccountData.getAcctBal().subtract( txAmount );// ���׽��
			SFLogger.info( context, String.format( "�ۼ����AcctBal=[%s]", acctBal ) );
			if( "1".equals( secCompData.getOverDraftFlag() ) ) {// ��ȯ������͸֧
				if( txDate.equals( signAccountData.getOpenDate() ) ) {// �������տ������
					DBHandler.rollBackTransaction( context, tranConnection );// �ع����񣬱�������
					SFUtil.chkCond( context, SFUtil.object2Double( acctBal ) < 0, "ST4113", "��������" );
				}
			} else {
				DBHandler.rollBackTransaction( context, tranConnection );// �ع����񣬱�������
				SFUtil.chkCond( context, SFUtil.object2Double( acctBal ) < 0, "ST4113", "��������" );
			}
			SFUtil.setDataValue( context, "CTX_ACCT_BAL", acctBal );// Ϊ�Ѽ�ȥ���ν��׽��������·ŵ�������
			// 1.����������ˮ
			this.addAcctDetail( context, secSeqId );
			// 2.signAccountData�ۼ����
			signAccountData.setPreTxDate( txDate );// TODO *
			signAccountData.setAcctBal( acctBal );
			try {
				signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
			} catch( SFException e ) {
				SFLogger.error( context, String.format( "UpdateBalFailure" ) );
				throw e;
			} catch( Exception e ) {
				SFLogger.error( context, String.format( "UpdateBalFailure" ) );
				SFUtil.chkCond( context, "ST4895", String.format( "saveSignAccountData�������ʧ��" ) );
			}
			// 3.���з���֤ת���ɹ������� TRDAcctJour
			acctJour.setJourFlag( "30" );
			acctJour.setSecSeqId( secSeqId );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			/* �������տ������ */
			if( txDate.equals( signAccountData.getOpenDate() ) && SFUtil.object2Double( signAccountData.getAcctBal() ) < -0.000001 ) {
				SFUtil.chkCond( context, SFUtil.object2Double( acctBal ) < 0, "ST4894", "�ʽ��˻������쳣" );// �ʽ��˻���������
			}
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
		} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {// ȯ����Ӧʧ��
			DBHandler.beginTransaction( context, tranConnection );// ��������
			// ���� TRDAcctJour
			acctJour.setJourFlag( "w2" );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setRespCode( respCode );
			acctJour.setRespMsg( respMsg );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
			SFUtil.chkCond( context, "ST4035", String.format( "������֤ת��ʧ�ܣ�ȯ�̷��أ�[%s]", respMsg ) );// ���쳣���� ����

		}// ��ȯ�̳�ʱ��������������
		else if( SFConst.RET_OVERTIME.equals( retFlag ) ) {
			/* ʧ�ܣ�����ȯ�� */
			Reverse reverse = new Reverse();
			reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ); // N ��� N-����Ϊ���� Y-�Ǳ�����
			reverse.setTxDate( txDate ); // N ԭ��������
			reverse.setSubTxSeqId( subTxSeqId );// N ԭ������ˮ��
			reverse.setType( "0" ); // N ��������: 0:��������1:����
			reverse.setTxCode( "300201" ); // N ������:ԭ���׵Ľ�����
			reverse.setInitside( initSide ); // N ��������
			reverse.setChannel( "SECU" ); // N ��������������HOST������������COBANK�������У�SECU��ȯ��
			reverse.setSceneCode( "35" );// ���ⳡ���룬֤ת��ȯ�̳�ʱ�����ߺ����߼���ֻҪȥȯ�̳���
			reverse.setReverseSeqId( "" );// Y ����������ˮ��:ÿ���������ɵ���ˮ��
			reverse.setReverseSeq( "30" ); // N ����˳��
			reverse.setReverseNum( 0 ); // N ��������
			reverse.setStatus( "0" ); // N ����״̬: 0��������1�������ɹ�2������ʧ��3�����˹���4�����˹���5���������9������������
			reverse.setMemo( "������֤ת��ȯ�̳���" );// Y ��ע
			new ReverseDao().saveReverse( context, tranConnection, reverse );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����7
			SFUtil.chkCond( context, "ST4035", String.format( "������֤ת��ʧ�ܣ�ȯ����Ӧ��ʱ" ) );// ���쳣���� ����
		}
		SFLogger.info( context, "doSecu()����" );
	}

	/**
	 * ��ʼ��������ˮ�����浽���ݿ�
	 * @param context
	 * @throws SFException
	 */
	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()��ʼ" );
		SFLogger.info( context, "����������ˮ" );

		String txTime = DateUtil.getMacTime();// ����ʱ�䣬ȡ����ʱ��
		String txSeqId = BizUtil.getTxSeqId( logId );// ��ȡ���𷽽�����ˮ��
		String chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId );// ���ɽ�����ˮ��

		String userId = SFConst.DEFAULT_USER_ID_COBANK;
		String invType = signAccountData.getInvType();
		String invName = signAccountData.getInvName();
		String secAcct = signAccountData.getSecAcct();
		int secAcctSeq = signAccountData.getSecAcctSeq();
		String savAcct = signAccountData.getSavAcct();
		String openDepId = signAccountData.getOpenDepId();
		String openBranchId = signAccountData.getOpenBranchId();
		String idType = investData.getIdType();
		String invIdCode = investData.getInvIdCode();
		// Double acctBal = SFUtil.getDataValue( context, "CTX_ACCT_BAL" );// Ϊ�Ѽ�ȥ���ν��׽�����δ�����������ܲ�׼ȷ
		BigDecimal acctBal = new BigDecimal( SFUtil.objectToString( SFUtil.getDataValue( context, "CTX_ACCT_BAL" ) ) );

		String cashRemitFlag = SFUtil.getReqDataValue( context, SFConst.PUBLIC_CASH_REMIT_FLAG );
		cashRemitFlag = SFConst.REMIT_FLAG.equals( cashRemitFlag ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG;// �����־

		String abst = SFConst.INV_TYPE_RETAIL.equals( invType ) ? "1002" : "2002";// ����Ͷ����-1002 ����Ͷ����-2002

		acctJour.setTxDate( txDate );
		acctJour.setInitSide( initSide );
		acctJour.setUserId( userId );
		acctJour.setTxSeqId( txSeqId );
		acctJour.setSubTxSeqId( subTxSeqId );
		acctJour.setSecSeqId( null );// ��ʼ����ˮ�ݲ���ֵ
		acctJour.setInvType( invType );
		acctJour.setInvName( invName );
		acctJour.setIdType( idType );
		acctJour.setInvIdCode( invIdCode );
		acctJour.setSecAcct( secAcct );
		acctJour.setSecAcctSeq( secAcctSeq );
		acctJour.setSecCompCode( secCompCode );
		acctJour.setCapAcct( capAcct );
		acctJour.setAcctId( acctId );
		acctJour.setCurCode( curCode );
		acctJour.setDcFlag( SFConst.DEBIT_FLAG );
		acctJour.setTxAmount( txAmount );
		acctJour.setAcctBal( acctBal );
		acctJour.setAbst( abst );
		acctJour.setAbstractStr( "���з���֤ת��" );// ����ժҪ��Ϣ
		acctJour.setJourFlag( "w3" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_BANK_S2B );
		acctJour.setBusiType( SFConst.BUSI_TYPE_S2B );
		acctJour.setTxTime( txTime );// ȡ����ʱ��
		acctJour.setDepId( openDepId );
		acctJour.setBranchId( openBranchId );
		acctJour.setOpenDepId( openDepId );
		acctJour.setOpenBranchId( openBranchId );
		acctJour.setCashRemitFlag( cashRemitFlag );
		acctJour.setUnitTellerId( "EB001" );// ����UM�š�ȯ��EB001������VIRTU������EB001
		acctJour.setAcctDealId( logId );
		acctJour.setPreSeqId( logId );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );// ��������
		acctJour.setColFlag( "0" );
		acctJour.setBankSeqId( SFUtil.objectToString( SFUtil.getDataValue( context, "A_REQUEST_HEAD.CHANNELSERIALNO" ) ) );
		acctJour.setProductType( "03" );
		acctJour.setMemo( "���з���֤ת��" );
		acctJour.setSavAcct( savAcct );
		acctJour.setTranSeqId( chlSeqId );
		acctJour.setBusiSeqId( chlSeqId );

		DBHandler.beginTransaction( context, tranConnection );// ��������
		acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		DBHandler.commitTransaction( context, tranConnection );// �ύ����

		SFLogger.info( context, "addAcctJour()����" );
	}

	/**
	 * Ԥ��������ˮ
	 * @param tranType ��������
	 * @throws SFException
	 */
	private void addTranList( Context context, String tranType ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ��ʺ�
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		String frontLogNo = BizUtil.getInitSeqId( context );// ������һ����ˮ
		SFUtil.addDataField( context, "FRONT_LOG_NO", frontLogNo );// �Ѻ����ɵ���ˮ������������,��ΪfrontLogNo������
		String voidFlag = "0";
		String hostCheck = "0";
		String tranResult = "MA1111";
		String tranDate = DateUtil.getMacDate();// ��ȡ���� yyyyMMdd�������������û�������
		String thirdTime = DateUtil.getMacDateTimeShort();// ��ȡ����yyyyMMddHHmmss
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		AgtTranList agtTranList = new AgtTranList();
		AgtCustomerInfo agtCustomerInfo = null;

		try {
			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoAndAgentInfo( context, tranConnection, secCompCode, stkAcct, acctNo );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST4377", "�ÿͻ�������" );

			SFUtil.addDataField( context, "AGT_CUSTOMERINFO", agtCustomerInfo );
			DBHandler.beginTransaction( context, tranConnection );// ��������1
			agtTranList.setTranFunc( "812322" );
			agtTranList.setBusinessType( "MS999" );
			agtTranList.setTranType( tranType );
			agtTranList.setPicsLogNo( logId );
			agtTranList.setFrontLogNo( frontLogNo );
			agtTranList.setAcctNo( acctNo );
			agtTranList.setStkCode( secCompCode );
			agtTranList.setStkAcct( stkAcct );
			agtTranList.setCompAcct( productInfo.getTruAcctId() );
			agtTranList.setCcyCode( "RMB" );
			agtTranList.setTranAmount( srcTxAmmountString );
			// --add by songshimin date 2018-04-17 10:26
			agtTranList.setInsertTime( thirdTime );
			// Remark ��Ϊ�����ļ�shell�ű���ѯ����insertTime���ʴ��ڴ˲���insertTime��ֵ
			agtTranList.setVoidFlag( voidFlag );
			agtTranList.setHostCheck( hostCheck );
			agtTranList.setThirdTime( thirdTime );
			agtTranList.setTradeDate( tranDate );
			agtTranList.setTranResult( tranResult );
			agtTranList.setBankId( agtCustomerInfo.getBankId() );
			agtTranList.setOpenBranch( agtCustomerInfo.getOpenBranch() );
			agtTranList.setBankAcct( agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// Ԥ����������ˮ
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "chkStart()����ʧ��%s", e.getMessage() ) );
		}
	}

	/**
	 * ֤ת���Ϻ����л���
	 * @throws SFException
	 */
	public String S2BTransfer( Context context ) throws SFException {
		SFLogger.info( context, "֤ת���Ϻ����л��ʼ" );
		addTranList( context, "1" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ��ʺ�
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		String frontLogNo = SFUtil.getDataValue( context, "FRONT_LOG_NO" );
		AgtCustomerInfo agtCustomerInfo = SFUtil.getDataValue( context, "AGT_CUSTOMERINFO" );// ��������ȡ�ÿͻ�����
		String bankId = agtCustomerInfo.getBankId();
		String tranResult = "MA1111";
		String thirdTime = DateUtil.getMacDateTimeShort();// ��ȡ����yyyyMMddHHmmss
		String retFlag = null;
		Context msgContext = null;
		AgtTranList agtTranList = new AgtTranList();
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		try {
			String retCode = null;
			SFLogger.info( context, "����������ͨ�ü��˿�ʼ" );
			Map<String, Object> msgMap = new HashMap<String, Object>();
			msgMap.put( "CONSUMER_SEQ_NO", BizUtil.getChlSeqId( context, subTxSeqId ) );// ������ˮ��
			msgMap.put( "BIZ_SEQ_NO", BizUtil.getChlSeqId( context, subTxSeqId ) );// ����ESBҵ����ˮ��
			msgMap.put( "OUT_ACCT_NO", productInfo.getTruAcctId() );
			msgMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_S2B );
			msgMap.put( "OUT_TOTAL_DETAIL_FLAG", "1" );
			msgMap.put( "OUT_COUNTER_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "IN_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "IN_BRANCH_ID", agtCustomerInfo.getAgtAgentInfo().getBranchCode() );
			msgMap.put( "IN_COUNTER_ACCT_NO", productInfo.getTruAcctId() );
			msgMap.put( "TX_AMOUNT", txAmount );
			msgMap.put( "CUR_CODE", SFConst.CUR_CODE_RMB );
			msgMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_S2B );
			msgMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );
			TransferClient transferClient = new TransferClient();
			msgContext = transferClient.send( context, msgMap );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// ����������Ӧ��
			String retMsg = SFUtil.getDataValue( context, kColl, "RET_MSG" );
			SFLogger.info( context, String.format( "������Ӧ��:[%s],��Ӧ��Ϣ:[%s]", retCode, retMsg ) );
			SFUtil.addDataField( context, "ACCOUNT_DATE", SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" ) );// ������������
			/* ���������˲��ɹ�������ȯ�� */
			if( SFConst.RET_OVERTIME.equals( retFlag ) || SFConst.RET_FAILURE.equals( retFlag ) ) {

				/* ���ɹ� ���ӳ������� �������� */
				Reverse reverse = new Reverse();
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ); // N ��� N-����Ϊ���� Y-�Ǳ�����
				reverse.setTxDate( txDate ); // N ԭ��������
				reverse.setSubTxSeqId( subTxSeqId );// N ԭ������ˮ��
				reverse.setType( "0" ); // N ��������: 0:��������1:����
				reverse.setTxCode( "300201" ); // N ������:ԭ���׵Ľ�����
				reverse.setInitside( initSide ); // N ��������
				reverse.setChannel( "SECU" ); // N ��������������HOST������������COBANK�������У�SECU��ȯ��
				reverse.setSceneCode( "31" );
				reverse.setReverseSeq( "30" ); // N ����˳��
				reverse.setReverseNum( 0 ); // N ��������
				reverse.setStatus( "0" ); // N ����״̬: 0��������1�������ɹ�2������ʧ��3�����˹���4�����˹���5���������9������������
				reverse.setMemo( "������֤ת��ȯ�̳���" );// Y ��ע
				new ReverseDao().saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����3

				if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// ��ʱ
					reverse = new Reverse();
					reverse.setChannel( "HOST" );
					reverse.setInitside( initSide );
					reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
					reverse.setTxDate( DateUtil.getMacDate() );
					reverse.setSubTxSeqId( subTxSeqId );
					reverse.setType( "0" );
					reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
					reverse.setSceneCode( "32" );
					reverse.setReverseSeq( "20" );
					reverse.setReverseNum( 0 );
					reverse.setStatus( "0" );
					reverse.setMemo( "������֤ת�����������˳���" );// ��ע���Խ�����������
					new ReverseDao().saveReverse( context, tranConnection, reverse );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����3
					SFLogger.info( context, "���½��׽��" );
					DBHandler.beginTransaction( context, tranConnection );// ��������1
					agtTranList.setTranResult( "MA0003" );
					agtTranList.setCompleteTime( thirdTime );
					agtTranList.setFrontLogNo( frontLogNo );
					agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����1
					respCode = retCode;
					respMsg = "ͨ�ü��˽��׷��س�ʱ";
					return retFlag;
				} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {
					SFLogger.info( context, "ͨ�ü��˽��׷���ʧ��" );
					agtTranList.setFrontLogNo( frontLogNo );
					agtTranList.setTranResult( retCode );
					agtTranList.setCompleteTime( thirdTime );
					agtTranList.setHostDate( SFUtil.objectToString( SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" ) ) );

					DBHandler.beginTransaction( context, tranConnection );// ��������3
					agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����3
					respCode = retCode;
					respMsg = "ͨ�ü��˽��׷���ʧ��";
					return retFlag;
				}
			}
			SFLogger.info( context, "ͨ�ü��˽����������ɹ���Ĵ���" );
			agtTranList.setTranResult( "ST0000" );
			agtTranList.setCompleteTime( thirdTime );
			agtTranList.setHostLogNo( "" );
			agtTranList.setHostDate( SFUtil.objectToString( SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" ) ) );
			agtTranList.setReserve( "" );
			agtTranList.setVoucherNo( "" );
			agtTranList.setFrontLogNo( frontLogNo );
			DBHandler.beginTransaction( context, tranConnection );// ��������2
			agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����2

			tranResult = "SF0000";
			// ����������
			msgMap = new HashMap<String, Object>();
			msgMap.put( "ACCT_ID", acctNo );// �����ʺ�account_no
			msgMap.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
			msgMap.put( "CAP_ACCT", stkAcct );// ֤ȯ�ʽ�̨�˺�CapAcct
			msgMap.put( "TX_AMOUNT", srcTxAmmountString );// ��ת���exch_bal
			msgMap.put( "TRADE_TYPE", "1" );// �ʽ�����access_type
			msgMap.put( "BANK_ID", bankId );// ������
			CoBankClientBase coBankClient = new T810022Client();
			msgContext = coBankClient.send( context, msgMap, bankId );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );// ��ȡ���ر�ʶ
			retCode = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPCODE" );// �����Ϻ����л������
			retMsg = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPMSG" );// ���պ����з��ش�����Ϣ
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// �����л���ɹ�
				kColl = SFUtil.getDataElement( msgContext, "810022_O" );
				String agtLogNo = SFUtil.getDataValue( msgContext, kColl, "AGENT_LOG_NO" );// ȡ����������ˮ��
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setAgentLogNo( agtLogNo );
				DBHandler.beginTransaction( context, tranConnection );// ��������4
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// ���º�������ˮ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����4
				respCode = "ST0000";
				respMsg = "֤ת���Ϻ����л���ɹ�";
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {// �Ϻ������ʽ𻮿�ʧ��
				tranResult = "777779";
				SFLogger.info( context, String.format( "֤ת���Ϻ����л������,�����з���[%s],������������", retMsg ) );
				DBHandler.beginTransaction( context, tranConnection );// ��������5
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����5
				/* ʧ�ܣ��������� */
				ReverseDao reverseDao = new ReverseDao();
				Reverse reverse = new Reverse();
				reverse.setChannel( "HOST" );
				reverse.setInitside( initSide );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "32" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "������֤ת�����������˳���" );// ��ע���Խ�����������
				reverseDao.saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����6

				/* ʧ�ܣ�����ȯ�� */
				reverse = new Reverse();
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ); // N ��� N-����Ϊ���� Y-�Ǳ�����
				reverse.setTxDate( txDate ); // N ԭ��������
				reverse.setSubTxSeqId( subTxSeqId );// N ԭ������ˮ��
				reverse.setType( "0" ); // N ��������: 0:��������1:����
				reverse.setTxCode( "300201" ); // N ������:ԭ���׵Ľ�����
				reverse.setInitside( initSide ); // N ��������
				reverse.setChannel( "SECU" ); // N ��������������HOST������������COBANK�������У�SECU��ȯ��
				reverse.setSceneCode( "31" );// N ������ HOST:1XX��COBANK:2XX��SECU:3XX 102-���ж˳����� 103-�����г����� 302 - ���г�ȯ�� 303-�����г�ȯ��
				reverse.setReverseSeq( "30" ); // N ����˳��
				reverse.setReverseNum( 0 ); // N ��������
				reverse.setStatus( "0" ); // N ����״̬: 0��������1�������ɹ�2������ʧ��3�����˹���4�����˹���5���������9������������
				reverse.setMemo( "������֤ת��ȯ�̳���" );// Y ��ע
				reverseDao.saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����7
				SFLogger.error( context, retMsg );
				respCode = retCode;
				respMsg = "ȥ�������ʽ𻮿��ʧ��";
				return retFlag;
			} else if( SFConst.RET_OVERTIME.equals( retFlag ) || "YY6005".equals( retCode ) ) {// �Ϻ������ʽ𻮿ʱ
				SFLogger.error( context, "ȥ�������ʽ𻮿�׳�ʱ" );
				tranResult = "MA0003";
				respCode = "MA0003";
				respMsg = "ȥ�������ʽ𻮿�׳�ʱ";
				return SFConst.RET_SUCCESS;// ��ʱ���ɹ�����
			}
		} catch( SFException e ) {
			SFLogger.info( context, e );
			return retFlag;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "doHost()����ʧ��%s", e.getMessage() ) );
		}
		SFLogger.info( context, "֤ת���Ϻ����л������" );
		return retFlag;
	}

	/**
	 * @param context
	 * @return
	 * @throws SFException
	 * ��װ֤ת��������Map
	 */
	private Map<String, Object> getS2BTransferMap( Context context ) throws SFException {

		String bookNo = SFUtil.getReqDataValue( context, "BOOK_NO" );// ȯ��ԤԼ��
		String secAcctPwd = SFUtil.getReqDataValue( context, "CAP_ACCT_PWD" );// ȯ���ʽ�����
		SFUtil.chkCond(context, SFUtil.isEmpty(secAcctPwd), "ST4223",
				String.format("֤ȯ�ʽ�����[%s]����ȷ����ȷ��", secAcctPwd));
		// ת�����ʽ�����
		AgtAgentInfo agtAgentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );// ��������ȡ�������ж���
		String capAcctPwd = BizUtil.convCobankEncryptPwd( context, secCompCode, agtAgentInfo, secAcctPwd );
		SFLogger.info( context, String.format( "ת����֤������hostIdType[%s] ", SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE ) ) );

		String invType = acctJour.getInvType();
		String invName = signAccountData.getInvName();
		String invIdCode = acctJour.getInvIdCode();
		String unitTellerId = acctJour.getUnitTellerId();
		String secAcct = signAccountData.getSecAcct();

		// �����map begin
		Map<String, Object> transferMap = new HashMap<String, Object>();
		if( SFConst.SECU_SZT.equals( secCompData.getSztFlag() ) ) {
			transferMap.put( "BIZ_SEQ_NO", subTxSeqId );
		} else {// ֱ����8λ��ˮ��
			transferMap.put( "BIZ_SEQ_NO", BizUtil.getTxSeqId( logId ) );
		}
		transferMap.put( "INV_TYPE", invType );
		transferMap.put( "INV_NAME", invName );
		transferMap.put( "ID_TYPE", investData.getIdType() );
		transferMap.put( "INV_ID_CODE", invIdCode );
		transferMap.put( "RESEND", "N" );
		transferMap.put( "ACCT_ID", acctId );
		transferMap.put( "SEC_ACCT", secAcct );
		transferMap.put( "SEC_COMP_CODE", secCompCode );
		transferMap.put( "CAP_ACCT", capAcct );
		transferMap.put( "CAP_ACCT_PWD", capAcctPwd );
		transferMap.put( "CUR_CODE", curCode );
		transferMap.put( "CASH_REMIT_FLAG", SFConst.CASH_FLAG );// �����־ 2-��
		transferMap.put( "TX_AMOUNT", AmtUtil.conv2SecuMulAmount( context, txAmount ) );
		transferMap.put( "BOOK_NO", bookNo );
		transferMap.put( "REF", bookNo );// TODO * ��֤ͨ��ֱ������������Ϊͬһ��
		transferMap.put( "ISSRTYPE", "B" );
		transferMap.put( "UNIT_TELLER_ID", unitTellerId );
		transferMap.put( "REPT_FLAG", "0" );
		transferMap.put( "NEW_SUB_TX_SEQ_ID", subTxSeqId );
		transferMap.put( "DGST", "���ж�֤ȯת����" );
		transferMap.put( "CHANNEL", "0005" );
		// ����ͷ���⴦��
		String brchId = signAccountData.getFlags();
		BizUtil.setSecBrchId( secCompCode, transferMap, brchId );
		transferMap.put( "TXSEQID", subTxSeqId );// ����ͷ��ˮ��
		return transferMap;
	}

	/**
	 * @param context
	 * @param secSeqId ȯ����ˮ�ţ��ӷ��ر���ͷ�л�ȡ
	 * @throws SFException
	 * TRDAcctDetail����������������ˮ
	 */
	private void addAcctDetail( Context context, String secSeqId ) throws SFException {

		acctDetail.setTxDate( acctJour.getTxDate() );
		acctDetail.setInitSide( initSide );
		acctDetail.setUserId( acctJour.getUserId() );
		acctDetail.setTxSeqId( acctJour.getTxSeqId() );
		acctDetail.setSecSeqId( secSeqId );
		acctDetail.setSubTxSeqId( acctJour.getSubTxSeqId() );
		acctDetail.setInvType( acctJour.getInvType() );
		acctDetail.setSecAcct( acctJour.getSecAcct() );
		acctDetail.setSecAcctSeq( acctJour.getSecAcctSeq() );
		acctDetail.setSecCompCode( acctJour.getSecCompCode() );
		acctDetail.setCapAcct( acctJour.getCapAcct() );
		acctDetail.setAcctId( acctJour.getAcctId() );
		acctDetail.setCurCode( acctJour.getCurCode() );
		acctDetail.setDcFlag( acctJour.getDcFlag() );// D
		acctDetail.setTxAmount( txAmount );
		acctDetail.setAcctBal( new BigDecimal( SFUtil.objectToString( SFUtil.getDataValue( context, "CTX_ACCT_BAL" ) ) ) );
		acctDetail.setAbst( acctJour.getAbst() );
		acctDetail.setAbstractStr( acctJour.getAbstractStr() );
		acctDetail.setJourFlag( "00" );
		acctDetail.setTxCode( acctJour.getTxCode() );// 6031
		acctDetail.setBusiType( acctJour.getBusiType() );// 02
		acctDetail.setTxTime( acctJour.getTxTime() );
		acctDetail.setDepId( acctJour.getDepId() );
		acctDetail.setBranchId( acctJour.getBranchId() );
		acctDetail.setOpenDepId( acctJour.getOpenDepId() );
		acctDetail.setOpenBranchId( acctJour.getOpenBranchId() );
		acctDetail.setCashRemitFlag( acctJour.getCashRemitFlag() );
		acctDetail.setUnitTellerId( acctJour.getUnitTellerId() );
		acctDetail.setAcctDealId( logId );
		acctDetail.setAcctHostSeqId( acctJour.getAcctHostSeqId() );
		acctDetail.setPreSeqId( acctJour.getPreSeqId() );
		acctDetail.setAcctDealDate( DateUtil.getMacDate() );
		acctDetail.setColFlag( acctJour.getColFlag() );
		acctDetail.setMemo( acctJour.getMemo() );

		acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );
	}
}