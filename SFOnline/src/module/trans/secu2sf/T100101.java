package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtCustomerInfo;
import module.bean.AutoBecif;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AutoBecifDao;
import module.trans.TranBase;
import module.trans.sf2cobank.T810021Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;
import common.util.SpecialSecuUtil;

import core.log.SFLogger;

/**
 * A��ȯ�̶˷���--���������ཻ��
 * 
 * �����߼���
 * 			1.����ȯ�̽��� ��<��>Acmt.003.01/11004��<ֱ>6023/612202��
 *			2.����������Ч��,���ȯ�̺Ϳͻ����ϡ�����
 *			3.�����ͻ�������612321�ϴ���ϵͳ����ǩԼ����
 *			4.�������и��˿ͻ�������D+�ӿ�R3042�ÿ�״̬��
 *			5.�������жԹ��ͻ�������D+�ӿ�C3048�ÿ�״̬��
 *			6.����ȯ��       ��<��>Acmt.004.01/11004��<ֱ>6023/612202��
 * 
 * ����ǩԼ��ϵ
 * tran code :100101
 * @author ������
 *
 */
public class T100101 extends TranBase {

	private String subTxSeqId = null;// 16λ����ƽ̨��ˮ��

	private String initSeqId = null;// 14λ��־��(ǰ����ˮ��)

	private String secSeqId = null;// ȯ����ˮ��

	private String txSeqId = null; // 8λ������ˮ��

	private String chlSeqId = null; // 22λ������ˮ��

	private String txDate = null;// ȯ��������е�����

	private String txTime = null;// ����ʱ��

	private String initSide = null;// ����

	private String userId = null;// �ͻ����

	private String curCode = null;// ����

	private String invName = null;// �ͻ�����

	private String invIdCode = null;// �ͻ�֤������

	private String invType = null;// �ͻ�����

	private String acctId = null;// ���п���

	private String capAcct = null;// �ʽ��ʺ�

	private String secCompCode = null;// ȯ�̱��

	private String hostIdType = null;// ��������

	private int secAcctSeq = 0;// ��֤������˺����к�

	private String secuType = null;// ȯ�����ͣ�����ֱ������֤ͨ

	private String bankId = null;// �����к�

	private boolean isSuccRetFlag = false;// ��ʶ�ѳ���ǩԼ�ɹ�����

	private DecimalFormat df = new DecimalFormat( "#0.00" );// ���ָ�ʽ����������λС��

	private SecCompData secu = null;// ȯ����Ϣ

	private SignAccountData signAccountData = null;// ǩԼ��Ϣ

	private AcctJourDao acctJourDao = new AcctJourDao();

	@Override
	protected void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {
			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λǰ����ˮ��
			subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16λ����ƽ̨��ˮ��
			chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // ��ȡ22λ��ˮ��;

			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Acmt00301" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// ȯ�̱��
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "��ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			// ȯ������
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "ȯ�����Ͳ���Ϊ��[%s]", secuType ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				String unitTellerId = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TELLERID" );// ����Ա���
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// ȯ����ˮ��

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100101_I" );
				// ����ʱ���
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
				SFUtil.addDataField( context, keyColl, "UNIT_TELLER_ID", unitTellerId );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection senderKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Sender" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				String invType = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// �ͻ�����

				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );

				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				String beginBal = ( null != SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) : "0.00";

				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );

				// idType_SZT ת���ͻ�����Ϊ 1������ 2������
				invType = BizUtil.convSZT2SFInvType( context, invType );

				// �ͻ�����Ĭ��Ϊ1������
				invType = SFUtil.isEmpty( invType ) ? SFConst.INV_TYPE_RETAIL : invType;

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100101_I" );

				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", df.format( new BigDecimal( beginBal ) ) );// ��Ϣ���
				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// ����
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" ) );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// ��չ������/�Թ��˺�
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// ȯ�̶��ʽ�̨�˺�
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// ȯ����ˮ��
				SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.getDataValue( context, senderKcoll, "BrchId" ) );// ��֧������ʶ

				SFUtil.addDataElement( context, keyColl );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// �ѳ���ǩԼ��ֱ�ӷ���ȯ�̳ɹ������ߺ�����������
		if( isSuccRetFlag ) {
			doSecu( context );
			return;
		}
		// ��¼������ˮ,д���˻�����������ϸ,����ǩԼ�˻���Ϣ,������ˮ״̬��ʶ���ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF
		addPublicInfo( context );

		// ��AcctIdΪ��,��Ϊֻ��ȯ��Ԥָ��,��������������ָ��,�������ɹ�,���Ͽ���ȡ����״̬��
		if( SFUtil.isNotEmpty( acctId ) ) {
			doHost( context );
		}

		// �������ȯ��
		doSecu( context );

		// ���ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECIF
		if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
			addAutoBecif( context );
		}
	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()��ʼ" );

		try {

			DBHandler.beginTransaction( context, tranConnection );// �������� 1

			// ��¼������ˮ
			addAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // �ύ����1

			DBHandler.beginTransaction( context, tranConnection ); // �������� 2

			// д���˻�����������ϸ
			addAcctAdmDetail( context );

			// ����ǩԼ��Ϣ
			updSignAccountData( context );

			// ������ˮ״̬��־
			updAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // �ύ����2

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()����" );
	}

	/**
	 * ��¼������ˮ
	 */
	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()��ʼ" );
		try {

			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setInvType( invType );
			acctJour.setInvName( invName );
			acctJour.setIdType( hostIdType );
			acctJour.setInvIdCode( invIdCode );
			acctJour.setSecAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
			acctJour.setSecAcctSeq( 0 );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setOpenDepId( signAccountData.getOpenDepId() );
			acctJour.setOpenBranchId( signAccountData.getOpenDepId() );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.DEBIT_FLAG );
			acctJour.setTxAmount( new BigDecimal( 0.00 ) );
			acctJour.setAcctBal( new BigDecimal( 0.00 ) );
			acctJour.setAbst( "" );
			acctJour.setAbstractStr( "ȯ�̶˳���ǩԼ" );
			acctJour.setJourFlag( "33" );
			acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_DEL_SIGN );
			acctJour.setBusiType( SFConst.BUSI_TYPE_DEL_SIGN );
			acctJour.setTxTime( txTime );
			acctJour.setDepId( signAccountData.getDepId() );
			acctJour.setBranchId( signAccountData.getBranchId() );
			acctJour.setUnitTellerId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "UNIT_TELLER_ID" ) ) );
			acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
			acctJour.setAcctDealId( initSeqId );// ȡ14λ��־��
			acctJour.setProductType( "03" );
			acctJour.setColFlag( "0" );
			acctJour.setMemo( "" );
			acctJour.setTranSeqId( chlSeqId );
			acctJour.setBusiSeqId( chlSeqId );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���뽻����ˮʧ��" );
		}
		SFLogger.info( context, "addAcctJour()����" );
	}

	private void updAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "updAcctJour()��ʼ" );
		try {

			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setJourFlag( "00" );
			acctJour.setSavAcct( signAccountData.getSavAcct() );
			acctJour.setDepId( signAccountData.getOpenDepId() );
			acctJour.setBranchId( signAccountData.getOpenDepId() );
			acctJour.setOpenDepId( signAccountData.getOpenDepId() );
			acctJour.setOpenBranchId( signAccountData.getOpenDepId() );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���½�����ˮʧ��" );
		}
		SFLogger.info( context, "updAcctJour()����" );
	}

	/**
	 *	����ǩԼ�˻����������
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public void updSignAccountData( Context context ) throws SFException {
		SFLogger.info( context, "updSignAccountData()��ʼ" );
		try {

			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setProductType( "03" );
			signAccountData.setDesDepId( signAccountData.getDepId() );
			signAccountData.setDesBranchId( signAccountData.getBranchId() );
			signAccountData.setCloseDate( txDate );
			signAccountData.setSignFlag( SFConst.SIGN_FLAG_CANCEL );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ�˻����������ʧ��" );
		}
		SFLogger.info( context, "updSignAccountData()����" );
	}

	/**
	 * 
	 * д������ϸ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public void addAcctAdmDetail( Context context ) throws SFException {
		SFLogger.info( context, "addAcctAdmDetail()��ʼ" );
		try {

			AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
			acctAdmDetail.setTxDate( txDate );
			acctAdmDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctAdmDetail.setUserId( userId );
			acctAdmDetail.setTxSeqId( txSeqId );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetail.setInvType( invType );
			acctAdmDetail.setInvName( invName );
			acctAdmDetail.setIdType( hostIdType );
			acctAdmDetail.setInvIdCode( invIdCode );
			acctAdmDetail.setSecAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
			acctAdmDetail.setSecAcctSeq( secAcctSeq );
			acctAdmDetail.setSecCompCode( secCompCode );
			acctAdmDetail.setCapAcct( capAcct );
			acctAdmDetail.setAcctId( acctId );
			acctAdmDetail.setOpenDepId( signAccountData.getOpenDepId() );
			acctAdmDetail.setOpenBranchId( signAccountData.getOpenBranchId() );
			acctAdmDetail.setOldAcctId( acctId );
			acctAdmDetail.setCurCode( curCode );
			acctAdmDetail.setDcFlag( SFConst.DEBIT_FLAG );
			acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
			acctAdmDetail.setAbStract( " " );
			acctAdmDetail.setAbstractStr( "ȯ�̶˳���ǩԼ" );
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setSignFlag( signAccountData.getSignFlag() );
			acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_CANCEL );
			acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_DEL_SIGN );
			acctAdmDetail.setBusiType( SFConst.BUSI_TYPE_DEL_SIGN );
			acctAdmDetail.setTxTime( txTime );
			acctAdmDetail.setDepId( signAccountData.getOpenDepId() );
			acctAdmDetail.setBranchId( signAccountData.getOpenBranchId() );
			acctAdmDetail.setUnitTellerId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "UNIT_TELLER_ID" ) ) );
			acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
			acctAdmDetail.setCusMagNo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) ) );
			acctAdmDetail.setAcctDealId( initSeqId );// 14λ��ˮ��
			acctAdmDetail.setColFlag( "0" );
			acctAdmDetail.setMemo( "" );

			AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "�����˻�������ϸ��ʧ��" );
		}
		SFLogger.info( context, "addAcctAdmDetail()����" );
	}

	/**
	 * ���º����пͻ���Ϣ
	 * @param context
	 * @throws SFException
	 */
	public void doCoBankCancleSign( Context context ) throws SFException {
		SFLogger.info( context, "doCoBankCancleSign()��ʼ" );
		try {
			// �����пͻ����
			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			AgtCustomerInfo agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoByAcctNo( context, tranConnection, capAcct, secCompCode, acctId );
			if( null == agtCustomerInfo ) {
				SFLogger.info( context, String.format( "�����ڸú����пͻ�" ) );
				return;
			}

			bankId = agtCustomerInfo.getBankId();
			String memo = agtCustomerInfo.getMemo();
			if( SFUtil.isNotEmpty( memo ) && memo.trim().length() > 1007 ) {
				memo = memo.substring( 1007 ) + "-D:" + DateUtil.getMacDateTimeShort();
			} else {
				memo = memo + "-D:" + DateUtil.getMacDateTimeShort();
			}
			SFLogger.info( context, String.format( "�����к�bankId [%s],��עmemo [%s]", bankId, memo ) );
			DBHandler.beginTransaction( context, tranConnection ); // ��������
			agtCustomerInfoDao.updAgtCustomerInfoByAcctNo( context, tranConnection, capAcct, secCompCode, acctId, "4", memo );
			DBHandler.commitTransaction( context, tranConnection ); // �ύ����

			// ����������
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
			msg.put( "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�CapAcct
			msg.put( "INV_NAME", invName );// �ͻ�����InvName
			msg.put( "ID_TYPE", hostIdType );// ������������ʵ����ֱ����֤������
			msg.put( "INV_ID_CODE", invIdCode );// ֤������InvIdCode
			msg.put( "ACCT_ID", acctId );// �����˺�account_no
			msg.put( "CUR_CODE", curCode );// ����CurCode
			msg.put( "REMARK", "" );// ��עMemo

			// ���ͱ���
			T810021Client coBankProcess = new T810021Client();
			Context msgContext = coBankProcess.send( context, msg, bankId );

			// ���ر�����Ϣ
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			/* ���Ӵ���ƽ̨���� */
			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
				SFLogger.info( context, String.format( "����ǩԼ�ɹ����Ϻ�����ȡ��״̬��ʧ��[%s]", retFlag ) );
			}

		} catch( SFException e ) {
			SFLogger.info( context, String.format( e.getMessage() ) );
		} catch( Exception e ) {
			SFLogger.info( context, String.format( e.getMessage() ) );
		}
		SFLogger.info( context, "doCoBankCancleSign()����" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );

		// �Ϻ����г���ǩԼ��ϵ
		if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {
			SFLogger.info( context, "�Ϻ����г���ǩԼ��ϵ��ʼ" );

			this.doCoBankCancleSign( context );

			SFLogger.info( context, "�Ϻ����г���ǩԼ��ϵ����" );

		} else if( SFConst.INIT_SIDE_SECU.equals( initSide ) ) {

			// ���˺�����ǩԼ��ϵ���Ͽ���ȡ��״̬��
			List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListBySignFlag( context, tranConnection, acctId );
			if( null == signAccountDataList || signAccountDataList.size() == 0 ) {
				try {
					// ȯ�̷�����ǩԼ�Ͽ��ܲ��ܳɹ�ʧ�ܻ��쳣��Ӱ���������
					BizUtil.setCardStatusWord( context, acctId, "D", signAccountData.getOpenDepId() );
				} catch( Exception e ) {
					SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, null );
					SFLogger.error( context, String.format( "����ǩԼ�ɹ����Ͽ���ȡ��״̬��ʧ�ܵ���Ӱ�콻������!" ) );
				}
			}
		}
		// ����Ϻ����к�������ȡ������ǩԼ��ϵ��ʱ����ȷʧ�ܶ�����ʧ�ܴ���������ȡ����״̬�ֱ�����������ʱ����ȡ��
		SFLogger.info( context, "doHost()����" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			// ���׳ɹ�-�������ȯ��
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ

				SFUtil.setResDataValue( context, "SEC_ACCT", SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) ) );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ�������ȯ��

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00401" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// ��<BkAcct>���
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doSecu()����" );
	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		try {
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );
			invName = SFUtil.getReqDataValue( context, "INV_NAME" );
			invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
			invType = SFUtil.getReqDataValue( context, "INV_TYPE" );
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // ת��ǰ�ͻ�֤������

			// ��Ԥ���巵�ر���-�����쳣�˳�ʱ���� --��ʼ
			if( SFConst.SECU_ZL.equals( secuType ) ) {// ֱ��ģʽ
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100101_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// ����
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
			}
			// ��֤ͨģʽ
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00401" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// ��<BkAcct>���
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
			}

			// ���ؼ��ֶ��Ƿ�Ϊ��
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "ȯ�̱�Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "ȯ�̶˿ͻ��ʽ�̨�˺Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "֤�����Ͳ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "֤�����벻��Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "Ͷ�������Ʋ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "�ͻ����Ͳ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "�ұ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "ȯ����ˮ�Ų���Ϊ��" ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {

			// ����Ƿ�����ظ���ˮ
			BizUtil.chkRepeatAcctJour( context, tranConnection );

			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// ���ɷ�����ˮ��
			signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );// ǩԼ��Ϣ
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );
			InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
			SFUtil.chkCond( context, ( null == investData ), "ST4392", "Ͷ������Ϣ������" );
			initSide = signAccountData.getInitSide();

			// ����֤ȯ�����֤ȯ��У��ȯ��Ӫҵ����Ϣ
			SpecialSecuUtil.chkSecBrchIdBySecCompCode( context, secCompCode, SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100101_I" );
			String secAcct = signAccountData.getSecAcct();
			String ecifNo = investData.getBecifNo();// ��ȡbecifNo
			SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );
			SFUtil.addDataField( context, keyIColl, "SEC_ACCT", secAcct );

			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
			txTime = DateUtil.getMacTime();// ȡ����ʱ��
			userId = secu.getUserId();

			/***************************************************
			 * ������飺 ���ȯ�̷���״̬ *
			 *  		  ��鵱ǰȯ���Ƿ����������ñ��ֽ��� * 
			 *  		  ��ǰȯ���Ƿ����������˻��ཻ�� * 
			 *  		  ���Ͷ��������
			 **************************************************/

			String signFlag = signAccountData.getSignFlag();// ǩԼ��Ϣ
			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			if( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ) {// �ѳ���ǩԼ
				isSuccRetFlag = true;
				return;
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ����" ) );

			SFLogger.info( context, "��鵱���Ƿ�����ת�˽��ף�����������������ǩԼ��ϵ��ʼ" );
			// ���ӽ�������
			context.addDataField( "TRAN_DATE", txDate );
			// ��鵱���Ƿ�����ת�˽��ף�����������������ǩԼ��ϵ
			BizUtil.chkTransfer( context );
			SFLogger.info( context, "��鵱���Ƿ�����ת�˽��ף�����������������ǩԼ��ϵ����" );

			// �������Ƿ�һ��
			BigDecimal txAmount = new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );
			if( txAmount.compareTo( signAccountData.getBeginBal() ) != 0 || txAmount.compareTo( signAccountData.getAcctBal() ) != 0 ) {
				SFUtil.chkCond( context, "ST5332", String.format( "[���׽������һ��]�������������˽���" ) );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 
	 * ����ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECIF
	 * @param context
	 * @throws SFException
	 */
	private void addAutoBecif( Context context ) throws SFException {
		try {
			String subType = null;
			String tpdmFlag = secu.getTpdmFlag();
			String ecifNo = SFUtil.getReqDataValue( context, "ECIF_NO" );
			List<SignAccountData> signAccountDataList = null;

			/* �ж�����֤ͨȯ����������ȯ */
			if( SFConst.TPDM_FLAG_NORMAL.equals( tpdmFlag ) ) {// ��֤ͨȯ
				subType = "R81";
			} else if( SFConst.TPDM_FLAG_MARGIN.equals( tpdmFlag ) ) {// ������ȯ
				subType = "R83";
			}
			// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����
			String agreementNo = secCompCode + "-" + subType + "-" + acctId;
			SFLogger.info( context, String.format( "ecifNo:[%s],userId:[%s],agreementNo��[%s]", ecifNo, userId, agreementNo ) );
			// ��ѯ���Ŷ�Ӧ�ɹ�ǩԼ��ȯ�̵ĸ��������Ϊ0������Ҫ����Ϣ��BECIF�Ӵ�Э��
			signAccountDataList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, acctId, secCompCode );
			if( ( signAccountDataList == null || signAccountDataList.size() == 0 ) && SFUtil.isNotEmpty( subType ) && SFUtil.isNotEmpty( ecifNo ) && SFUtil.isNotEmpty( acctId ) ) {

				// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����
				AutoBecif autoBecif = new AutoBecif();
				autoBecif.setTxTime( txTime );
				autoBecif.setStatus( "0" );
				autoBecif.setAgreementNo( agreementNo );
				autoBecif.setBecifNo( ecifNo );
				autoBecif.setAgreementType( "R8" );
				autoBecif.setAgreementSubType( subType );
				autoBecif.setAgreementStatus( "2" );
				autoBecif.setProductNo( acctId );
				autoBecif.setOpenDate( "" );
				autoBecif.setCloseDate( txDate );
				autoBecif.setDeptNo( "9998" );
				autoBecif.setUserId( "EB001" );
				autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );
				autoBecif.setTxDate( txDate );
				autoBecif.setSubTxSeqId( subTxSeqId );

				DBHandler.beginTransaction( context, tranConnection );// ��������
				AutoBecifDao autoBecifDao = new AutoBecifDao();
				autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );
				DBHandler.commitTransaction( context, tranConnection ); // �ύ����
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
	}
}