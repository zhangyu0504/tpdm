package module.trans.cobank2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctDetail;
import module.bean.AcctJour;
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
import module.trans.sf2cobank.T810024Client;
import module.trans.sf2secu.B2STransferClient;

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
 * (�����ж˷���)��ת֤
 * ������ :300200 
 * @author ���ź�
 * 
 */
public class T300200 extends TranBase {

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

	private String logId = null;// 14λ��־��

	private String txDate = null;// ��������

	private String srcTxAmmountString = "";

	String secCompCode = null;// ȯ�̴���

	String capAcct = null;// ȯ���ʽ��˻�

	String curCode = null;

	String acctId = null;// �����ʺ�

	String initSide = null;// ����

	String secuType = null;// ȯ������ 1-��֤ͨ 0-ֱ��

	BigDecimal txAmount = new BigDecimal( 0.00 );

	String reqSecCompCode = null;// �������е�ԭʼȯ�̴��루��Ϊ����ȯ�̴������������Ҫ�����������е�ԭʼȯ�̶˴��룩

	@Override
	protected void initialize( Context context ) throws SFException {

		// ȯ�̴��봦��
		reqSecCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// �������ǰ��ȯ�̴��룬���ڷ��ر���ʱʹ��
		BizUtil.setZhongXinSecuCompCode( context );// ����ȯ�̴���

		// ���׹���������ʼ��
		logId = SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID ) );
		subTxSeqId = BizUtil.getSubTxSeqId( logId );
		secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴��룬����ȡ�������
		capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ȯ���ʽ��˻�
		curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );
		acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );// ����
		// ����Ĭ��Ϊ�����
		if (SFUtil.isEmpty(curCode)) {
			curCode = SFConst.CUR_CODE_RMB;
		}
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

		SFUtil.chkCond( context, investData == null, "ST4049", "�ͻ������Ϣ������" );
		SFUtil.chkCond( context, signAccountData == null, "ST5800", "�ͻ�ǩԼ�˺���Ϣ��ѯʧ��" );

		productInfo = ProductInfoCache.getValue( secCompData.getSecCompCode() );// ����ȯ�̴����ȡȯ�̲�Ʒ�������
		SFUtil.chkCond( context, productInfo == null, "ST5423", "��ȯ��δ��ͨ�˱��ֵ�ҵ��!" );
		BankUnit bankUnit = BankUnitCache.getValue( productInfo.getTruOpnDepId() );// ��������Ż�ȡ�����������
		SFUtil.chkCond( context, bankUnit == null, "ST5804", "��ѯ[ȯ���й�ר���˺ſ�������]ʧ��" );
		SFUtil.chkCond( context, !SFConst.SIGN_FLAG_SIGN.equals( signAccountData.getSignFlag() ), "ST5590", "��ǰǩԼ״̬����������ҵ��" );

	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// ���ɽ�����ˮ
		addAcctJour( context );

		// �����пͻ������ú����л���ס�D+����ײ�������ˮ(IOMONEY)
		doHost( context );

		// ����ȯ��
		doSecu( context );

	}

	@Override
	public void doHost( Context context ) throws SFException {

		SFLogger.info( context, "doHost()��ʼ" );

		// �����Ϻ����л���Ĳ���
		SFUtil.addDataField( context, "TRU_ACCT_ID", productInfo.getTruAcctId() );// ����ȯ���й�ר���˺�
		// ���ú����л���ס�D+����ף��ж��Ƿ�ɹ�
		String retFlag = this.B2STransfer( context );

		/*
		 * ���ݽ������TRDAcctJour������ˮ�� TRDAcctDetailת�˽�����ϸ��TRDSignAccountDataǩԼ�˻�����
		 */
		String respCode = SFUtil.getDataValue( context, "RESP_CODE" );// ������
		String respMsg = SFUtil.getDataValue( context, "RESP_MSG" );// ��������
		if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// �����л���ɹ�
			DBHandler.beginTransaction( context, tranConnection );// ��������
			// 0.���½��������ѯ���������
			signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
			BigDecimal acctBal = signAccountData.getAcctBal().add( txAmount );// ������ת֤���������ԭ������ת�˽��
			SFUtil.addDataField( context, "CTX_ACCT_BAL", acctBal );
			// 1.TRDSignAccountData����
			signAccountData.setAcctBal( acctBal ); // ��ǰ���
			signAccountData.setPreTxDate( txDate );// �ϴν�������
			if( "1".equals( signAccountData.getActiveFlag() ) ) {// �����־ ��������һ��ʽǩԼ
				signAccountData.setActiveFlag( "2" );
			}
			try {
				signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
			} catch( SFException e ) {
				SFLogger.error( context, String.format( "UpdateBalFailure" ) );
				throw e;
			} catch( Exception e ) {
				SFLogger.error( context, String.format( "UpdateBalFailure" ) );
				SFUtil.chkCond( context, "ST4895", String.format( "saveSignAccountData�������ʧ��" ) );
			}
			// 2.Trdacctjour����
			acctJour.setJourFlag( "03" );
			acctJour.setRespCode( "000000" );
			acctJour.setRespMsg( "�ϴ���ϵͳת�ʳɹ�" );
			// acctJour.setAcctDealDate(DateUtil.getMacDate());//��������
			acctJour.setAcctBal( acctBal );// ������ˮ����ֶ�
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			// 3.TRDAcctDetail����
			addAcctDetail( context );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
		} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {// �����л���ʧ��
			acctJour.setJourFlag( "2w" );
			acctJour.setRespCode( respCode );
			acctJour.setRespMsg( respMsg );
			DBHandler.beginTransaction( context, tranConnection );// ��������
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
			SFUtil.chkCond( context, respCode, respMsg );
		} else if( SFConst.RET_OVERTIME.equals( retFlag ) ) {
			SFUtil.chkCond( context, respCode, respMsg );// ��ʱֱ������
		}
		SFLogger.info( context, "doHost()����" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );

		SecuClientBase secuClient = new B2STransferClient();// ��֤ͨ��ת֤ͨѶ����
		Context secuContext = secuClient.send( context, this.getB2STransferMap( context ) );// ��������

		// �����ؽ��
		String retFlag = SFUtil.getDataValue( secuContext, SFConst.PUBLIC_RET_FLAG );
		String respCode = null;
		String respMsg = null;
		String secSeqId = null;// ȯ�̽�����ˮ��

		// ��ȯ�̷��أ�������֤ͨ��ֱ��ȡ����Ҫ����
		if( !SFConst.RET_OVERTIME.equals( retFlag ) ) {
			if( SFConst.SECU_SZT.equals( secuType ) ) {// ��֤ͨ��ʽ
				KeyedCollection kColl = SFUtil.getDataElement( secuContext, "Trf00201" );

				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				respCode = SFUtil.getDataValue( context, rstKcoll, "RESP_CODE" );// ��֤ͨ���ش�����
				respMsg = SFUtil.getDataValue( context, rstKcoll, "RESP_MSG" );// ��֤ͨ���ش�����Ϣ
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��֤ͨ���سɹ�
					KeyedCollection msgKcoll = SFUtil.getDataElement( secuContext, "MsgHdr" );// ȡ����Ӧ����ͷ
					KeyedCollection rltKcoll = SFUtil.getDataElement( context, msgKcoll, "Ref" );
					secSeqId = SFUtil.getDataValue( context, rltKcoll, "Ref" );// ȡ��ȯ����ˮ��
				}
			} else {// ֱ����ʽ
				KeyedCollection kColl = SFUtil.getDataElement( secuContext, "6032_O" );
				respCode = SFUtil.getDataValue( context, kColl, "RESP_CODE" );// ֱ�����ش�����
				respMsg = SFUtil.getDataValue( context, kColl, "RESP_MSG" );// ֱ�����ش�����Ϣ
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ֱ�����سɹ�
					secSeqId = SFUtil.getDataValue( context, kColl, "SEC_SEQ_ID" );// ȡ��ȯ����ˮ��
				}
			}
		} else {
			// ��ʱֱ���˳�
			SFUtil.chkCond( context, "ST4035", String.format( "��������ת֤ʧ��,ȯ����Ӧ��ʱ" ) );
		}
		// ���±�
		try {
			DBHandler.beginTransaction( context, tranConnection );// ��������
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��ȯ�̳ɹ�
				// ���� TRDAcctJour
				acctJour.setJourFlag( "00" );
				acctJour.setRespCode( "000000" );
				acctJour.setRespMsg( SFConst.RESPCODE_SUCCMSG );
				acctJour.setSecSeqId( secSeqId );
				acctJour.setTxDate( txDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );

				// ���� TRDAcctDetail
				acctDetail.setSecSeqId( secSeqId );
				acctDetail.setTranSeqId( acctJour.getTranSeqId() );
				acctDetail.setBusiSeqId( acctJour.getBusiSeqId() );
				acctDetail.setTxDate( txDate );
				acctDetail.setSubTxSeqId( subTxSeqId );
				acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {// ��ȯ��ʧ�ܻ��쳣
				// ��ӳ�����ʶ����Ҫ����,��������
				Reverse reverse = new Reverse();
				reverse.setChannel( "HOST" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "32" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "��������ת֤����ͨ�ü��˳���" );// ��ע���Խ����������
				new ReverseDao().saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );
				// ��ӳ�����ʶ����Ҫ����,����������
				reverse = new Reverse();
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "33" );
				reverse.setReverseSeq( "30" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "��������ת֤�����г���" );// ��ע���Խ����������
				new ReverseDao().saveReverse( context, tranConnection, reverse );

				AcctJour acctJour = new AcctJour();
				acctJour.setRespCode( respCode );
				acctJour.setRespMsg( respMsg );
				acctJour.setTxDate( txDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );
				DBHandler.commitTransaction( context, tranConnection );
				SFUtil.chkCond( context, "ST4895", String.format( "��ȯ��ʧ��,ȯ�̷���:[%s]", respMsg ) );
			} else if( SFConst.RET_OVERTIME.equals( retFlag ) ) {
				SFUtil.chkCond( context, "ST4895", String.format( "ȥȯ��ȷ����֤ת���쳣" ) );
			}
			// �ύ����
			DBHandler.commitTransaction( context, tranConnection );
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );// �����쳣����ع�
			SFLogger.error( context, "doSecu()��ˮ����ϸ����ʧ��" + e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ʧ��" );
		}

		SFLogger.info( context, "doSecu()����" );
		SFUtil.setResDataValue( context, "ACCT_ID", acctId );// �����˺�
		SFUtil.setResDataValue( context, "INV_NAME", acctJour.getInvName() );// �ͻ�����
		SFUtil.setResDataValue( context, "CUR_CODE", curCode );// ����
		SFUtil.setResDataValue( context, "TX_AMOUNT", srcTxAmmountString );// ���Է�Ϊ��λ
		SFUtil.setResDataValue( context, "SEC_COMP_NAME", secCompData.getSecCompName() );// ȯ������
		SFUtil.setResDataValue( context, "SEC_COMP_CODE", reqSecCompCode );// ȯ�̴���
		SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�
		SFUtil.setResDataValue( context, "PICS_LOG_NO", logId );// ��ˮ��
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
		txAmount = new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );// ��ȡ�����н��׽��
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

		// Double acctBal= SFUtil.object2Double(signAccountData.getAcctBal()) + txAmount;// ������ת֤���������ԭ������ת�˽��
		BigDecimal acctBal = signAccountData.getAcctBal().add( txAmount );// ������ת֤���������ԭ������ת�˽��
		String cashRemitFlag = SFUtil.getReqDataValue( context, SFConst.PUBLIC_CASH_REMIT_FLAG );
		cashRemitFlag = SFConst.REMIT_FLAG.equals( cashRemitFlag ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG;// �����־

		String abst = SFConst.INV_TYPE_RETAIL.equals( invType ) ? "1001" : "2001";// ����Ͷ����-1001 ����Ͷ����-2001

		acctJour.setTxDate( txDate );
		acctJour.setInitSide( initSide );
		acctJour.setUserId( userId );
		acctJour.setTxSeqId( BizUtil.getTxSeqId( logId ) );
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
		acctJour.setSavAcct( savAcct );
		acctJour.setOpenDepId( openDepId );
		acctJour.setOpenBranchId( openBranchId );
		acctJour.setOldAcctId( null );
		acctJour.setCurCode( curCode );
		acctJour.setDcFlag( SFConst.CREDIT_FLAG );
		acctJour.setTxAmount( txAmount );
		acctJour.setAcctBal( acctBal );
		acctJour.setAbst( abst );
		acctJour.setAbstractStr( "���з�����ת֤" );// ����ժҪ��Ϣ
		acctJour.setJourFlag( "3w" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_BANK_B2S );
		acctJour.setBusiType( SFConst.BUSI_TYPE_B2S );
		acctJour.setTxTime( txTime );// ȡ����ʱ��
		acctJour.setDepId( openDepId );
		acctJour.setBranchId( openBranchId );
		acctJour.setUnitTellerId( "EB001" );// ����UM�š�ȯ��EB001������VIRTU������EB001
		acctJour.setCashRemitFlag( cashRemitFlag );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );
		acctJour.setAcctDealId( logId );// ���׼�����ˮ��ͬƽ̨��ˮ��subTxSeqId
		acctJour.setPreSeqId( logId );// ǰ����ˮ��ͬƽ̨��ˮ��subTxSeqId
		acctJour.setAcctDealDate( DateUtil.getMacDate() );// ��������
		acctJour.setColFlag( "0" );
		acctJour.setProductType( "03" );
		acctJour.setMemo( "���з�����ת֤" );
		acctJour.setTranSeqId( chlSeqId );
		acctJour.setBusiSeqId( chlSeqId );

		try {
			DBHandler.beginTransaction( context, tranConnection );// ��������
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", "addAcctJour()ʧ��" + e.getMessage() );
		}

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
		String compAcct = SFUtil.getDataValue( context, "TRU_ACCT_ID" );// �����ʺ�
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
			agtTranList.setCompAcct( compAcct );
			agtTranList.setCcyCode( "RMB" );
			agtTranList.setTranAmount( srcTxAmmountString );
			agtTranList.setVoidFlag( voidFlag );
			agtTranList.setHostCheck( hostCheck );
			agtTranList.setThirdTime( thirdTime );
			// --add by songshimin date 2018-04-17 10:26
			agtTranList.setInsertTime( thirdTime );
			// Remark ��Ϊ�����ļ�shell�ű���ѯ����insertTime���ʴ��ڴ˲���insertTime��ֵ
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
	 * ��ת֤�Ϻ����л���	
	 * @param connection
	 */
	public String B2STransfer( Context context ) throws SFException {
		SFLogger.info( context, "��ת֤�Ϻ����л��ʼ" );
		addTranList( context, "0" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ��ʺ�
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		String frontLogNo = SFUtil.getDataValue( context, "FRONT_LOG_NO" );
		AgtCustomerInfo agtCustomerInfo = SFUtil.getDataValue( context, "AGT_CUSTOMERINFO" );// ��������ȡ�ÿͻ�����
		String bankId = agtCustomerInfo.getBankId();
		String tranResult = "MA1111";
		String thirdTime = DateUtil.getMacDateTimeShort();// ��ȡ����yyyyMMddHHmmss
		String warnFlag = agtCustomerInfo.getAgtAgentInfo().getWarnFlag();
		String warnMoney = agtCustomerInfo.getAgtAgentInfo().getWarnMoney();
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		AgtTranList agtTranList = new AgtTranList();
		String retFlag = null;
		Context msgContext = null;
		Map<String, Object> msgMap = null;
		CoBankClientBase coBankClient = null;
		try {
			tranResult = "ST4895";
			// ����������
			msgMap = new HashMap<String, Object>();
			msgMap.put( "BANK_ID", bankId );// ������
			msgMap.put( "ACCT_ID", acctNo );// �����ʺ�account_no
			msgMap.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
			msgMap.put( "CAP_ACCT", stkAcct );// ֤ȯ�ʽ�̨�˺�CapAcct
			msgMap.put( "TX_AMOUNT", srcTxAmmountString );// ��ת���exch_bal
			msgMap.put( "TRADE_TYPE", "0" );// �ʽ����� ��ת֤ ���̶�ֵ 0
			coBankClient = new T810022Client();
			String retCode = null;
			// ���ú������ʽ�ת
			msgContext = coBankClient.send( context, msgMap, bankId );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			if( SFConst.RET_OVERTIME.equals( retFlag ) || SFConst.RET_FAILURE.equals( retFlag ) ) {// ��ʱ��ʧ��
				String retMsg = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPMSG" );// ���պ����з��ش�����Ϣ
				retCode = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPCODE" );// ���պ����з��ش�����
				if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// �Ϻ������ʽ𻮿ʱ
					/* ��ʱ������������ */
					Reverse reverse = new Reverse();
					// ��ӳ�����ʶ����Ҫ����,����������
					reverse.setChannel( "COBANK" );
					reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
					reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
					reverse.setTxDate( DateUtil.getMacDate() );
					reverse.setSubTxSeqId( subTxSeqId );
					reverse.setType( "0" );
					reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
					reverse.setSceneCode( "33" );
					reverse.setReverseSeq( "30" );
					reverse.setReverseNum( 0 );
					reverse.setStatus( "0" );
					reverse.setMemo( "��������ת֤�����г���" );// ��ע���Խ����������
					new ReverseDao().saveReverse( context, tranConnection, reverse );
					DBHandler.commitTransaction( context, tranConnection );
					SFLogger.error( context, "ȥ�������ʽ𻮿�׳�ʱ" );
					tranResult = "MA0003";
				}
				if( SFConst.RET_FAILURE.equals( retFlag ) ) {// �Ϻ������ʽ𻮿�ʧ��
					SFLogger.error( context, retMsg );
					tranResult = "777779";
				}
				DBHandler.beginTransaction( context, tranConnection );// ��������2
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����2
				SFUtil.addDataField( context, "MSG_TYPE", "E" );
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", SFUtil.isEmpty( retMsg ) ? "ȥ�������ʽ𻮿�׳���" : retMsg );
				return retFlag;
			} else {
				KeyedCollection kColl = SFUtil.getDataElement( msgContext, "810022_O" );
				String agtLogNo = SFUtil.getDataValue( context, kColl, "AGENT_LOG_NO" );// ȡ����������ˮ��
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setAgentLogNo( agtLogNo );
				DBHandler.beginTransaction( context, tranConnection );// ��������3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// ���º�������ˮ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����3
			}
			SFLogger.info( context, "����������ͨ�ü��˿�ʼ" );
			msgMap = new HashMap<String, Object>();
			msgMap.put( "CONSUMER_SEQ_NO", BizUtil.getChlSeqId( context, subTxSeqId ) );// ������ˮ��
			msgMap.put( "BIZ_SEQ_NO", BizUtil.getChlSeqId( context, subTxSeqId ) );// ����ESBҵ����ˮ��
			msgMap.put( "OUT_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "OUT_BRANCH_ID", agtCustomerInfo.getAgtAgentInfo().getBranchCode() );
			msgMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "IN_TOTAL_DETAIL_FLAG", "1" );
			msgMap.put( "OUT_COUNTER_ACCT_NO", SFUtil.getDataValue( context, "TRU_ACCT_ID" ) );
			msgMap.put( "IN_ACCT_NO", SFUtil.getDataValue( context, "TRU_ACCT_ID" ) );
			msgMap.put( "IN_COUNTER_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "TX_AMOUNT", txAmount );
			msgMap.put( "CUR_CODE", SFConst.CUR_CODE_RMB );
			msgMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );
			TransferClient transferClient = new TransferClient();
			msgContext = transferClient.send( context, msgMap );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );
			/* ���ɹ���ӳ������� */
			if( SFConst.RET_OVERTIME.equals( retFlag ) || SFConst.RET_FAILURE.equals( retFlag ) ) {

				Reverse reverse = new Reverse();
				// ��ӳ�����ʶ����Ҫ����,����������
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "33" );
				reverse.setReverseSeq( "30" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "��������ת֤�����г���" );// ��ע���Խ����������
				new ReverseDao().saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����6
			}
			if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// ��ʱ
				/* ��ʱ���������� */
				Reverse reverse = new Reverse();
				reverse.setChannel( "HOST" );
				reverse.setInitside( initSide );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "34" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "��������ת֤���������˳���" );// ��ע���Խ����������
				new ReverseDao().saveReverse( context, tranConnection, reverse );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����6

				tranResult = "MA0003";
				SFLogger.info( context, "���½��׽��" );
				DBHandler.beginTransaction( context, tranConnection );// ��������1
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����1
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", "ͨ�ü��˽��׷��س�ʱ" );
				return SFConst.RET_OVERTIME;
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {
				SFLogger.info( context, "ͨ�ü��˽��׷���ʧ��" );
				String accountDate = SFUtil.getDataValue( msgContext, "SYS_HEAD.ACCOUNT_DATE" );// ȡϵͳ�������
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				if( SFUtil.isNotEmpty( accountDate ) ) {
					agtTranList.setHostDate( accountDate );
				}
				DBHandler.beginTransaction( context, tranConnection );// ��������3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����3
				SFUtil.addDataField( context, "MSG_TYPE", "E" );
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", "ͨ�ü��˽��׷���ʧ��" );
				return SFConst.RET_FAILURE;
			}
			SFLogger.info( context, "ͨ�ü��˽���'�������ɹ���Ĵ���" );
			tranResult = "ST0000";
			agtTranList.setTranResult( tranResult );
			agtTranList.setCompleteTime( thirdTime );
			agtTranList.setHostLogNo( "" );
			agtTranList.setHostDate( SFUtil.objectToString( SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" ) ) );
			agtTranList.setReserve( "" );
			agtTranList.setVoucherNo( "" );
			agtTranList.setFrontLogNo( frontLogNo );
			DBHandler.beginTransaction( context, tranConnection );// ��������2
			agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����2
			/*********************************************
			 * ��ת֤Ԥ����ʼ
			 *********************************************/
			double bankBalance = 0.0;
			double warnBalance = 0.0;
			String bankBal = SFUtil.getDataValue( msgContext, "MSG_O.AVAIL_BALANCE" );// ȡ���������
			if( SFUtil.isNotEmpty( bankBal ) ) {
				bankBalance = SFUtil.object2Double( bankBal );
			}
			if( SFUtil.isNotEmpty( warnMoney ) ) {
				warnBalance = SFUtil.object2Double( warnMoney );
			}
			String sumLim = "000000000000000";
			String useLim = "000000000000000";
			String limBal = "000000000000000";
			if( ( "1".equals( warnFlag ) && bankBalance < warnBalance ) || ( "2".equals( warnFlag ) && Integer.parseInt( limBal ) < warnBalance ) ) {
				SFLogger.info( context, "�Ϻ������ʽ�Ԥ��-��ʼ" );
				// ����������
				msgMap = new HashMap<String, Object>();
				msgMap.put( "BANK_ACCT", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );// �������˺�BankAcct
				msgMap.put( "ACCT_BAL", AmtUtil.conv2CoBankMulAmount( context, bankBal ) );// �˺����BankBal,��Ҫ����100 TODO *
				msgMap.put( "TOTAL_LIMIT", sumLim );// �ܶ��SumLim
				msgMap.put( "USED_LIMIT", useLim );// ���ö��UseLim
				msgMap.put( "AVAIL_LIMIT", limBal );// ���ö��LimBal
				msgMap.put( "WARN_FLAG", warnFlag );// Ԥ������WarnFlag
				msgMap.put( "WARN_MONEY", warnMoney );// Ԥ�����WarnMoney
				coBankClient = new T810024Client();
				coBankClient.send( context, msgMap, bankId );
				SFLogger.info( context, "�Ϻ������ʽ�Ԥ��-����" );
			}
			/*********************************************
			 * ��ת֤Ԥ������
			 *********************************************/
			SFUtil.addDataField( context, "MSG_TYPE", "N" );
			SFUtil.addDataField( context, "RESP_CODE", "ST0000" );
			SFUtil.addDataField( context, "RESP_MSG", "��ת֤�Ϻ����л���ɹ�" );
			SFLogger.info( context, "��ת֤�Ϻ����л������" );
		} catch( SFException e ) {
			SFLogger.info( context, e );
			SFUtil.addDataField( context, "RESP_CODE", "ST4895" );
			SFUtil.addDataField( context, "RESP_MSG", "��ת֤�Ϻ����л����ʧ��" );
			return SFConst.RET_FAILURE;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "doHandle()����ʧ��%s", e.getMessage() ) );
		}
		return SFConst.RET_SUCCESS;
	}

	/**
	 * @param context
	 * @throws SFException
	 * ������ϸ
	 */
	private void addAcctDetail( Context context ) throws SFException {

		acctDetail.setTxDate( acctJour.getTxDate() );
		acctDetail.setInitSide( acctJour.getInitSide() );
		acctDetail.setUserId( acctJour.getUserId() );
		acctDetail.setTxSeqId( acctJour.getTxSeqId() );
		acctDetail.setSecSeqId( "" );
		acctDetail.setSubTxSeqId( acctJour.getSubTxSeqId() );
		acctDetail.setInvType( acctJour.getInvType() );
		acctDetail.setSecAcct( acctJour.getSecAcct() );
		acctDetail.setSecAcctSeq( acctJour.getSecAcctSeq() );
		acctDetail.setSecCompCode( acctJour.getSecCompCode() );
		acctDetail.setCapAcct( acctJour.getCapAcct() );
		acctDetail.setAcctId( acctJour.getAcctId() );
		acctDetail.setCurCode( acctJour.getCurCode() );
		acctDetail.setDcFlag( acctJour.getDcFlag() );// C
		acctDetail.setTxAmount( acctJour.getTxAmount() );
		acctDetail.setAcctBal( new BigDecimal( SFUtil.objectToString( SFUtil.getDataValue( context, "CTX_ACCT_BAL" ) ) ) );
		acctDetail.setAbst( acctJour.getAbst() );
		acctDetail.setAbstractStr( acctJour.getAbstractStr() );
		acctDetail.setJourFlag( "00" );
		acctDetail.setTxCode( acctJour.getTxCode() );// 6032
		acctDetail.setBusiType( acctJour.getBusiType() );// 01
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

		// ��������
		acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );
	}

	/**
	 * @param context
	 * @return
	 * @throws SFException
	 * ��װ����֤ͨ��ת֤��������map
	 */
	private Map<String, Object> getB2STransferMap( Context context ) throws SFException {
		String invIdCode = acctJour.getInvIdCode();
		String unitTellerId = acctJour.getUnitTellerId();
		String invType = acctJour.getInvType();
		String invTypeSzt = BizUtil.convSF2SZTInvType( context, invType );

		// ��ȯ�̲���map
		Map<String, Object> transferMap = new HashMap<String, Object>();
		if( SFConst.SECU_SZT.equals( secCompData.getSztFlag() ) ) {
			transferMap.put( "BIZ_SEQ_NO", subTxSeqId );
		} else {// ֱ����8λ��ˮ��
			transferMap.put( "BIZ_SEQ_NO", BizUtil.getTxSeqId( logId ) );
		}
		transferMap.put( "INV_TYPE", invType );
		transferMap.put( "INV_TYPE_SZT", invTypeSzt );
		transferMap.put( "INV_NAME", acctJour.getInvName() );
		transferMap.put( "ID_TYPE", investData.getIdType() );
		transferMap.put( "INV_ID_CODE", invIdCode );
		transferMap.put( "RESEND", "N" );
		transferMap.put( "ACCT_ID", acctId );
		transferMap.put( "SEC_ACCT", investData.getSecAcct() );
		transferMap.put( "SEC_COMP_CODE", secCompCode );
		transferMap.put( "CAP_ACCT", capAcct );
		transferMap.put( "CUR_CODE", curCode );
		transferMap.put( "CASH_REMIT_FLAG", SFConst.CASH_FLAG );
		transferMap.put( "TX_AMOUNT", AmtUtil.conv2SecuMulAmount( context, txAmount ) );
		transferMap.put( "UNIT_TELLER_ID", unitTellerId );
		transferMap.put( "REPT_FLAG", "0" );
		transferMap.put( "SUB_TX_SEQ_ID", subTxSeqId );
		transferMap.put( "DGST", "���ж�����ת֤ȯ" );
		transferMap.put( "CHANNEL", "0002" );

		// ����ͷ���⴦��
		String brchId = signAccountData.getFlags();
		BizUtil.setSecBrchId( secCompCode, transferMap, brchId );
		transferMap.put( "TXSEQID", subTxSeqId );// ����ͷ��ˮ��

		return transferMap;
	}
}
