package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import module.bean.AcctDetail;
import module.bean.AcctJour;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.ParamCache;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
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
 * A��ȯ�̶˷���--���������ཻ��
 * 
 * 
 * �����߼���
 * 
 * 			1.����ȯ�̽��� ��<��>Trf.007.01/12006��<ֱ>6045/612208��
 *			2.����������Ч��,���ȯ�̺Ϳͻ����ϡ�����
 *			3.����������ݿ�
 *			4.����ȯ��         ��<��>Trf.008.01/12006��<ֱ>6045/612208��
 * 
 * 
 * ������Ϣ
 * tran code :100105
 * @author ������
 *
 */
public class T100105 extends TranBase {

	private String subTxSeqId = null;// 16λ����ƽ̨��ˮ��

	private String initSeqId = null;// 14λ��־��(ǰ����ˮ��)

	private String secSeqId = null;// ȯ����ˮ��

	private String txSeqId = null; // 8λ������ˮ��

	private String chlSeqId = null; // 22λ������ˮ��

	private String txDate = null;// ��������

	private String txTime = null;// ����ʱ��

	private String invType = null;// �ͻ�����

	private String cashRemitFlag = null;// �����ʶ

	private BigDecimal txAmount = new BigDecimal( 0.00 );// ���׽��

	private BigDecimal amountTax = new BigDecimal( 0.00 );// ��Ϣ

	private String curCode = null;// ����

	private String clrAccrlType = null;// ��Ϣ����

	private String invName = null;// �ͻ�����

	private String acctId = null;// �����ʺ�

	private String secCompCode = null;// ȯ�̴���

	private String capAcct = null;// �ʽ��ʺ�

	private String userId = null;// �ͻ����

	private String hostIdType = null;// ����֤������

	private int secAcctSeq = 0;// ��֤������˺����к�

	private String secAcct = null;// ��֤������˺�

	private String secuType = null;// ȯ������

	private boolean succResFlag = false;// ��ȷ�������ⷵ�ر�ʶ

	private SecCompData secu = null;// ȯ����Ϣ

	private SignAccountData signAccountData = null;// ǩԼ��Ϣ

	private AcctJourDao acctJourDao = new AcctJourDao();// ��ˮDaoʵ����

	private DecimalFormat df = new DecimalFormat( "#0.00" );// ���ָ�ʽ����������λС��

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
				kColl = SFUtil.getDataElement( context, "Trf00701" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// ȯ�̱��
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "��ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "ȯ�����Ͳ���Ϊ��" ) );
			// ֱ��ģʽ
			if( SFConst.SECU_ZL.equals( secuType ) ) {

				cashRemitFlag = SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ); // �����ʾ

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100105_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

				// ��֤ͨģʽ
			} else if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );

				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��
				invType = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// �ͻ�����
				String txAmountStr = ( null != SFUtil.getDataValue( context, kColl, "TX_AMOUNT" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "TX_AMOUNT" ) ) : "0.00";
				String amountTaxStr = ( null != SFUtil.getDataValue( context, kColl, "AMOUNT_TAX" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "AMOUNT_TAX" ) ) : "0.00";
				txAmount = new BigDecimal( txAmountStr );// ���
				amountTax = new BigDecimal( amountTaxStr );// ��Ϣ˰

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100105_I" );

				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", df.format( txAmount ) );// ��Ϣ���
				SFUtil.addDataField( context, keyColl, "AMOUNT_TAX", df.format( amountTax ) );// ��Ϣ˰
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" ) );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_TYPE", BizUtil.convSZT2SFInvType( context, invType ) );// ת���ͻ�����Ϊ 1������ 2������
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// ȯ�̶��ʽ�̨�˺�
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );//
				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// ����
				SFUtil.addDataField( context, keyColl, "CLC_ACCRL_TYPE", SFUtil.getDataValue( context, kColl, "CLC_ACCRL_TYPE" ) );// ��Ϣ����
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// ��չ������/�Թ��˺�
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// ȯ����ˮ��

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

		// ��¼������ˮ,����ǩԼ�˻����������,���뽻����ϸ
		if( !succResFlag ) {
			addPublicInfo( context );
		}

		// �������ȯ��
		doSecu( context );

	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()��ʼ" );

		try {
			DBHandler.beginTransaction( context, tranConnection ); // ��������

			/* ��¼������ˮ */
			addAcctJour( context );

			/* ����ǩԼ�˻���������� */
			updSignAccountData( context );

			/* ���뽻����ϸ */
			addAcctDetail( context );

			DBHandler.commitTransaction( context, tranConnection );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()����" );
	}

	/**
	 * ����ǩԼ�˻����������
	 *����������������
	 * @param context
	 * @throws SFException
	 */
	public void updSignAccountData( Context context ) throws SFException {
		SFLogger.info( context, "updSignAccountData()��ʼ" );

		try {

			// ��ѯTRDSignAccountData������¼
			SignAccountData signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
			SFUtil.chkCond( context, null == signAccountData, "ST5800", "�ͻ�ǩԼ�˺���Ϣ��ѯʧ��!" );

			// ����ǩԼ�˻����������
			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setProductType( "03" );
			signAccountData.setPreTxDate( txDate );
			signAccountData.setAcctBal( signAccountData.getAcctBal().add( new BigDecimal( SFUtil.objectToString( txAmount ) ) ) );
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ�������Ϣʧ��" );
		}
		SFLogger.info( context, "updSignAccountData()����" );
	}

	/**
	 * 
	 * ���뽻����ϸ
	 * @param context
	 * @throws SFException
	 */
	public void addAcctDetail( Context context ) throws SFException {
		SFLogger.info( context, "doAddAcctDetail()��ʼ" );
		try {

			AcctDetail acctDetail = new AcctDetail();
			acctDetail.setTxDate( txDate );
			acctDetail.setUserId( userId );
			acctDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctDetail.setTxSeqId( txSeqId );
			acctDetail.setSecSeqId( secSeqId );
			acctDetail.setSubTxSeqId( subTxSeqId );
			acctDetail.setSecAcct( secAcct );
			acctDetail.setSecAcctSeq( secAcctSeq );
			acctDetail.setCapAcct( capAcct );
			acctDetail.setAcctId( acctId );
			acctDetail.setSecCompCode( secCompCode );
			acctDetail.setCurCode( curCode );
			acctDetail.setDcFlag( SFConst.CREDIT_FLAG );
			acctDetail.setTxAmount( txAmount );
			acctDetail.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
			acctDetail.setAbst( "****" );
			acctDetail.setAbstractStr( "ȯ�̶�������Ϣ" );
			acctDetail.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctDetail.setJourFlag( "00" );
			acctDetail.setTxCode( SFConst.SF_TX_CODE_SECU_INTEREST );
			acctDetail.setBusiType( "05" );
			acctDetail.setTxTime( txTime );
			acctDetail.setDepId( "" );
			acctDetail.setUnitTellerId( "" );
			acctDetail.setCashRemitFlag( cashRemitFlag );
			acctDetail.setMemo( "" );
			acctDetail.setColFlag( "0" );
			acctDetail.setAcctDealId( "" );
			acctDetail.setAcctHostSeqId( "" );
			acctDetail.setPreSeqId( "" );
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���뽻����ϸ��Ϣʧ��" );
		}
		SFLogger.info( context, "doAddAcctDetail()����" );
	}

	/**
	 * ��¼������ˮ
	 */
	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()��ʼ" );
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );// ��������
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setSecAcct( secAcct );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.CREDIT_FLAG );
			acctJour.setTxAmount( txAmount );
			acctJour.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
			acctJour.setAbstractStr( "ȯ�̶�������Ϣ" );
			acctJour.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctJour.setJourFlag( "00" );
			acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_INTEREST );
			acctJour.setBusiType( "05" );
			acctJour.setTxTime( txTime );// ��������
			acctJour.setDepId( "" );
			acctJour.setUnitTellerId( "" );
			acctJour.setCashRemitFlag( cashRemitFlag );
			acctJour.setMemo( "" );
			acctJour.setColFlag( "0" );
			acctJour.setAcctDealId( "" );
			acctJour.setAcctHostSeqId( "" );
			acctJour.setPreSeqId( "" );
			acctJour.setOldInvName( invName );
			acctJour.setOldIdType( hostIdType );
			acctJour.setOldInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctJour.setOldAcctId( acctId );
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

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			// ���׳ɹ�-�������ȯ��
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ

				// ��̩֤ȯ �� ��̩���� ��Ҫ����capAcct��curCode��secSeqId�ֶ�
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) || SFConst.SECU_ZHONGJINZQ.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
					SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				}
				// �н�˾��Ҫ���� SecSeqId��AcctId��SecAcct��CapAcct��CurCode��CashRemitFlag��TxAmount��SubTxSeqId�ֶ�
				if( SFConst.SECU_ZHONGJINZQ.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "ACCT_ID", acctId );
					SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
					SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", cashRemitFlag );
					SFUtil.setResDataValue( context, "TX_AMOUNT", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.add( signAccountData.getAcctBal(), txAmount ) ) ) );
					SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				}
				// ����ȯ��ֻ�����SecSeqId�ֶ�
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ�������ȯ��

				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00801" );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( SFUtil.add( signAccountData.getAcctBal(), txAmount ) ) );
				SFUtil.setDataValue( context, kColl, "AMOUNT_TAX", df.format( amountTax ) );
				SFUtil.setDataValue( context, kColl, "CLC_ACCRL_TYPE", clrAccrlType );
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

			invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
			invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // �ͻ�����
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ��չ������/�Թ��˺�
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ȯ�̶��ʽ�̨�˺�
			secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// ȯ�̶���ˮ��
			txAmount = new BigDecimal( AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
			clrAccrlType = SFUtil.getReqDataValue( context, "CLC_ACCRL_TYPE" );// ��Ϣ����

			// �����쳣�˳�ʱ����
			if( SFConst.SECU_ZL.equals( secuType ) ) {// ֱ��ģʽ
				// ��̩֤ȯ �� ��̩���� ��Ҫ����capAcct��curCode��secSeqId�ֶ�,����ȯ��ֻ�����SecSeqId�ֶ�
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
					SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				}
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );
			}
			// ��֤ͨģʽ
			if( SFConst.SECU_SZT.equals( secuType ) ) {
				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00801" );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );
				SFUtil.setDataValue( context, kColl, "AMOUNT_TAX", df.format( amountTax ) );
				SFUtil.setDataValue( context, kColl, "CLC_ACCRL_TYPE", clrAccrlType );

				// �����Ϣ˰С��λ��Ч��
				AmtUtil.chkAmtValid( context, df.format( txAmount ) );
				AmtUtil.chkAmtValid( context, df.format( amountTax ) );
			}
			// ���ؼ��ֶ��Ƿ�Ϊ��
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "ȯ�̱�Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "ȯ�̶˿ͻ��ʽ�̨�˺Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "Ͷ�������Ʋ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "�ͻ����Ͳ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "�ұ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "ȯ����ˮ�Ų���Ϊ��" ) );

			// �����С��λ��Ч��
			AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );

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

			// �������֤ȯRever�ڵ���Ҫ����brchId�����ţ������ӵ�context��
			SFUtil.addDataField( context, "SEC_BRCH_ID", ( null != signAccountData && SFUtil.isNotEmpty( signAccountData.getFlags() ) ) ? signAccountData.getFlags() : " " );

			// ���ǩԼ��ϵ,�˴�ɾȥ��pics��������֤�ͻ�����֤
			String signFlag = signAccountData.getSignFlag();// ǩԼ��Ϣ
			SFUtil.chkCond( context, ( SFUtil.isEmpty( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );

			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ����" ) );
			SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5720", String.format( "�ѳ���ǩԼ" ) );

			secAcctSeq = signAccountData.getSecAcctSeq();
			secAcct = signAccountData.getSecAcct();// ��֤������˺�
			userId = secu.getUserId();// �ͻ����
			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
			txTime = DateUtil.getMacTime();// ȡ����ʱ��

			// ��ѯȯ����Ϣ˰������ʽ �������ֵ��ȡ
			String secInterestTax = ParamCache.getValue2( "SEC_INTEREST_TAX", secCompCode );
			secInterestTax = SFUtil.isEmpty( secInterestTax ) ? "0" : secInterestTax;
			// ��Ϣ ��֤ͨʱ ��Ϣ���ڽ��txAmount,ֱ��δ����Ϣ˰�ֶ�
			if( "1".equals( secInterestTax ) ) {
				txAmount = txAmount.subtract( amountTax ).abs();// ȡ����ֵ
			}
			SFLogger.info( context, String.format( "�����Ľ�Ϣ���TxAmount:[%s]", df.format( txAmount ) ) );

			// ��鵱���Ƿ��н�Ϣ
			context.addDataField( "TRAN_DATE", txDate );// ���ӽ�������
			AcctJour acctJour = acctJourDao.qryAcctJourByTxCode( context, tranConnection );
			if( SFUtil.isNotEmpty( acctJour ) ) {
				SFUtil.chkCond( context, ( txAmount.compareTo( acctJour.getTxAmount() ) != 0 ), "ST5721", String.format( "����[�ѽ�Ϣ]���������ظ�����" ) );
				// �������ȯ�̳ɹ�
				if( txAmount.compareTo( acctJour.getTxAmount() ) == 0 ) {
					succResFlag = true;
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}