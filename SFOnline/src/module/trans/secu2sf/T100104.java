package module.trans.secu2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * A��ȯ�̶˷���--�˻���Ϣ�޸Ľ���
 * 
 * �����߼���
 * 			1.����ȯ�̽��� ��<��>Acmt.005.01/11005��<ֱ>6026/612203��
 *			2.����������Ч��
 *			3.�������ڸ���, ����D+�ӿ�M7030��ѯ�ͻ���Ϣ
 *			4.�������ڶԹ�, ����D+�ӿ�M8010��ѯ�ͻ���Ϣ
 *			5.�����ͻ�����612435�����, ȡ�����ͻ���������
 *			6.����ȯ�� ��<��>Acmt.006.01/11005��<ֱ>6026/612203��
 * 
 * 
 * ȯ�̷�����¿ͻ�������Ϣ
 * 
 * tran code :100104
 * @author ������
 *
 */
public class T100104 extends TranBase {

	private String subTxSeqId = null;// 16λ����ƽ̨��ˮ��

	private String initSeqId = null;// 14λ��־��(ǰ����ˮ��)

	private String secSeqId = null;// ȯ����ˮ��

	private String txSeqId = null; // 8λ������ˮ��

	private String chlSeqId = null; // 22λ������ˮ��

	private String legalName = null;// ��������

	private String legalIdCode = null;// ����֤������

	private String trnName = null;// ����������

	private String trnIdCode = null;// ������֤����

	private String trnMobile = null;// �������ֻ�

	private String secCompCode = null;// ȯ�̴���

	private String capAcct = null;// �ʽ��ʺ�

	private String invName = null;// �ͻ�����

	private String txDate = null;// ��������

	private String txTime = null;// ����ʱ��

	private String secAcct = null;// ��֤������ʺ�

	private String oldInvName = null;// �ɿͻ�����

	private String oldInvIdCode = null;// �ɿͻ�֤������

	private String oldIdType = null;// �ɿͻ�֤������

	private String acctId = null;// �����ʺ�

	private String openDepId = null;// �ͻ��ſ���������

	private String openBranchId = null;// �ͻ��ſ�������

	private String signFlag = null;// ǩԼ��ʶ

	private String initSide = null;// ����

	private String oldLegalName = null;// �ɷ�������

	private String oldLegalIdCode = null;// �ɷ���֤������

	private String oldTrnName = null;// �ɾ���������

	private String oldTrnIdCode = null;// �ɾ�����֤������

	private String oldTrnMobile = null;// �ɾ������ֻ�

	private String userId = null;// �ͻ����

	private int secAcctSeq = 0;// ��֤������˺����к�

	private String invType = null;// �ͻ�����

	private String idType = null;// ֤������

	private String invIdCode = null;// ֤������

	private String hostIdType = null;// ������֤������

	private String secuType = null;// ȯ������

	private String curCode = "RMB";// ����

	private SecCompData secu = null;// ȯ����Ϣ

	private InvestData oldInvestData = null;// Ͷ������Ϣ

	private AcctDetailDao acctDetailDao = new AcctDetailDao();

	private AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();

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
				kColl = SFUtil.getDataElement( context, "Acmt00501" );
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
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "[ȯ������]����Ϊ��" ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				legalName = SFUtil.getReqDataValue( context, "LEGAL_NAME" );// ��������
				legalIdCode = SFUtil.getReqDataValue( context, "LEGAL_ID_TYPE" );// ��������֤
				trnName = SFUtil.getReqDataValue( context, "TRN_NAME" );// ��������֤
				trnIdCode = SFUtil.getReqDataValue( context, "TRN_ID_CODE" );// ��������֤
				trnMobile = SFUtil.getReqDataValue( context, "TRN_MOBILE" );// ��������֤
				capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" ); // ȯ�̶��ʽ�̨�˺�
				invName = SFUtil.getReqDataValue( context, "INV_NAME" ); // �ͻ�����
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ); // ȯ����ˮ��
				secAcct = SFUtil.getReqDataValue( context, "SEC_ACCT" );// ��֤������ʺ�
				invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // �ͻ�����
				idType = SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ); // ֤������
				invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // ֤������

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100104_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				acctId = ( String )SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" );

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				invName = ( String )SFUtil.getDataValue( context, custKcoll, "INV_NAME" );// �ͻ�����
				idType = ( String )SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" );// ֤������
				invType = ( String )SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// �ͻ�����
				trnIdCode = ( String )SFUtil.getDataValue( context, custKcoll, "TRN_ID_CODE" );// ������֤������
				invIdCode = ( String )SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" );// �ͻ�֤������
				capAcct = ( String )SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" );// ȯ�̶˿ͻ��ʽ�̨�˺�

				legalName = ( String )SFUtil.getDataValue( context, kColl, "LEGAL_NAME" );
				trnName = ( String )SFUtil.getDataValue( context, kColl, "TRN_NAME" );
				trnIdCode = ( String )SFUtil.getDataValue( context, kColl, "TRN_ID_CODE" );
				trnMobile = ( String )SFUtil.getDataValue( context, kColl, "TRN_MOBILE" );

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��

				// idType_SZT ת���ͻ�����Ϊ 1������ 2������
				invType = BizUtil.convSZT2SFInvType( context, invType );

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100104_I" );

				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", invIdCode );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "INV_NAME", invName );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", capAcct );// �ʽ��ʺ�
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���

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

		// ��ʼ������
		doHost( context );

		// ��¼������ˮ�͸�����д���˻�����������ϸ���͸���������Ͷ���˻�����Ϣ���͸���������ǩԼ��Ϣ���͸���
		addPublicInfo( context );

		// ����ȯ��
		doSecu( context );

	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()��ʼ" );

		try {

			DBHandler.beginTransaction( context, tranConnection );

			// ��¼������ˮ
			addAcctJour( context );

			// д���˻�����������ϸ��
			addAcctAdmDetail( context );

			// �ͻ���������ϢǩԼ����ʽ��˺ţ����Ա��
			long signAcctDataCount = signAccountDataDao.qrySignAccountDataCountBySecAcct( context, tranConnection, secCompCode, capAcct );

			SFLogger.info( context, String.format( "ȯ��[%s]��Ӧ���ʽ��ʺ�[%s]����[%s]��ǩԼ��¼", secCompCode, capAcct, String.valueOf( signAcctDataCount ) ) );

			// ͬһ���ͻ�ǩԼ����ʽ��ʺ�
			if( signAcctDataCount > 1 ) {

				// �����µ�֤�����͡�֤�������ѯ���Ѿ������¿���
				InvestData investData = investDataDao.qryInvestDataByInvType( context, tranConnection, hostIdType, invIdCode, invType );

				/******************************�����¿ͻ�***************************/
				if( null != investData ) {
					SFLogger.info( context, String.format( "ǩԼ��������1�Ҷ��������ϣ��Ѿ�������֤ͬ���ͻ�,����Ͷ���˻�����Ϣ��" ) );
					secAcct = investData.getSecAcct();

					// ����Ͷ���˻�����Ϣ��
					updInvestData( context );

					// ��ȡ���secAcctSeq
					SignAccountData signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
					if( null == signAccountData ) {
						secAcctSeq = 1;
					} else {
						secAcctSeq = signAccountData.getSecAcctSeq() + 1;
					}

					SFLogger.info( context, String.format( "����secAcctSeq=%s", secAcctSeq ) );

					// ����TRDSignAccountData
					updSignAccountData( context );
				} else {
					SFLogger.info( context, String.format( "ǩԼ��������1�Ҷ��������ϣ���������֤ͬ���ͻ�,����ͻ���Ϣ����" ) );
					// ���ͻ���ϢΪ��,�����ͻ���Ϣ��
					if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
						if( SFUtil.isNotEmpty( legalName ) ) {
							oldLegalName = legalName;
						}
						if( SFUtil.isNotEmpty( legalIdCode ) ) {
							oldLegalIdCode = legalIdCode;
						}
						if( SFUtil.isNotEmpty( trnName ) ) {
							oldTrnName = trnName;
						}
						if( SFUtil.isNotEmpty( trnIdCode ) ) {
							oldTrnIdCode = trnIdCode;
						}
						if( SFUtil.isNotEmpty( trnMobile ) ) {
							oldTrnMobile = trnMobile;
						}
					}

					// ����Ͷ���˹����ʺ�
					secAcct = BizUtil.genSecAcctId( context );// ����Ͷ���˹����ʺ�
					SFUtil.chkCond( context, SFUtil.isEmpty( secAcct ), "ST5711", String.format( "����[����Ͷ���˹����˺�]ʧ��" ) );//
					SFLogger.info( context, String.format( "[�����ɵ�Ͷ���˹����ʺ�]: %s", secAcct ) );

					// ����Ͷ������Ϣ
					// ��IdType�޸�ΪhostIdType���ڲ���Ͷ������Ϣ������䣬�����޸�ǰ��
					addInvestData( context );

					secAcctSeq = 1;

					// ����TRDSignAccountData
					updSignAccountData( context );
				}
			} else { // ֻǩһ���ͻ�������ȡԭ������

				// �����µ�֤�����͡�֤�������ѯ���ж��Ƿ��Ѿ������¿���
				InvestData investData = investDataDao.qryInvestDataByInvType( context, tranConnection, hostIdType, invIdCode, invType );

				if( null != investData ) {
					SFLogger.info( context, String.format( "���������ϣ��Ѿ�������֤ͬ���ͻ�,����Ͷ���˻�����Ϣ��" ) );
					secAcct = investData.getSecAcct();

					// ����Ͷ���˻�����Ϣ��
					updInvestData( context );

					// ��ȡ���secAcctSeq
					SignAccountData signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
					if( null == signAccountData ) {
						secAcctSeq = 1;
					} else {
						secAcctSeq = signAccountData.getSecAcctSeq() + 1;
					}

					SFLogger.info( context, String.format( "����secAcctSeq=%s", secAcctSeq ) );

					// ����TRDSignAccountData
					updSignAccountData( context );

					/******************************�������¿ͻ�***************************/
				} else {
					SFLogger.info( context, String.format( "���������ϣ���������֤ͬ���ͻ�,����Ͷ���˻�����Ϣ��" ) );
					// ����Ͷ���˻�����Ϣ��
					updInvestData( context );

					// ����ǩԼ��Ϣ��
					signAccountDataDao.updSignAccoutDataBySignFlag( context, tranConnection, invName, secAcct );
				}
			}

			// �ύ����1
			DBHandler.commitTransaction( context, tranConnection );

			// ��������2
			DBHandler.beginTransaction( context, tranConnection );

			// UPDATE TRDAcctJour
			SFLogger.info( context, "UPDATE AcctJour ��ʼ" );
			acctJourDao.updAcctJour( context, tranConnection, "0", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE AcctJour ����" );

			// UPDATE TRDAcctDetail
			SFLogger.info( context, "UPDATE TRDAcctDetail ��ʼ" );
			acctDetailDao.updAcctDetail( context, tranConnection, "0", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE TRDAcctDetail ����" );

			// UPDATE TRDAcctAdmDetail
			SFLogger.info( context, "UPDATE TRDAcctAdmDetail ��ʼ" );
			acctAdmDetailDao.updAcctAdmDetailByCapAcct( context, tranConnection, "0", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE TRDAcctAdmDetail ����" );

			// UPDATE TRDAcctJour_fb
			SFLogger.info( context, "UPDATE TRDAcctJour_fb ��ʼ" );
			acctJourDao.updAcctJour( context, tranConnection, "1", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE TRDAcctJour_fb ����" );

			// UPDATE TRDAcctDetail_fb
			SFLogger.info( context, "UPDATE TRDAcctDetail_fb ��ʼ" );
			acctDetailDao.updAcctDetail( context, tranConnection, "1", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE TRDAcctDetail_fb ����" );

			// UPDATE TRDAcctAdmDetail_fb
			SFLogger.info( context, "UPDATE TRDAcctAdmDetail_fb ��ʼ" );
			acctAdmDetailDao.updAcctAdmDetailByCapAcct( context, tranConnection, "1", secAcct, secAcctSeq, capAcct, secCompCode );
			SFLogger.info( context, "UPDATE TRDAcctAdmDetail_fb ����" );

			// �ύ����2
			DBHandler.commitTransaction( context, tranConnection );

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
			acctJour.setSecAcct( secAcct );
			acctJour.setInvType( invType );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCapAcct( capAcct );
			acctJour.setAbstractStr( "����ͻ�������Ϣ" );
			acctJour.setJourFlag( "00" );
			acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_UPD_CUST_INFO );
			acctJour.setBusiType( "26" );
			acctJour.setTxTime( txTime );
			acctJour.setOpenDepId( openDepId );
			acctJour.setOpenBranchId( openBranchId );
			acctJour.setDepId( openDepId );
			acctJour.setBranchId( openBranchId );
			acctJour.setOldInvName( oldInvName );
			acctJour.setOldIdType( oldIdType );
			acctJour.setOldInvIdCode( oldInvIdCode );
			acctJour.setColFlag( "0" );
			acctJour.setAcctDealId( initSeqId );
			acctJour.setTranSeqId( chlSeqId );
			acctJour.setBusiSeqId( chlSeqId );
			acctJour.setTxAmount(new BigDecimal("0.00"));
			acctJour.setAcctBal(new BigDecimal("0.00"));

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���뽻����ˮʧ��" );
		}
		SFLogger.info( context, "addAcctJour()����" );
	}

	private void updInvestData( Context context ) throws SFException {
		SFLogger.info( context, "updInvestData()��ʼ" );
		try {
			InvestData investData = new InvestData();
			investData.setInvType( invType );
			investData.setInvName( invName );
			investData.setIdType( hostIdType );
			investData.setInvIdCode( invIdCode );

			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
				investData.setLegalName( legalName );
				investData.setLegalIdCode( legalIdCode );
				investData.setTrnName( trnName );
				investData.setTrnIdCode( trnIdCode );
				investData.setTrnMobile( trnMobile );

			}
			investData.setSecAcct( secAcct );

			investDataDao.updInvestDataInvIdCodeBySecAcct( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����Ͷ������Ϣʧ��" );
		}
		SFLogger.info( context, "updInvestData()����" );
	}

	private void updSignAccountData( Context context ) throws SFException {
		SFLogger.info( context, "updSignAccountData()��ʼ" );
		try {
			SignAccountData signAccountData = new SignAccountData();
			signAccountData.setSecAcct( secAcct );
			signAccountData.setSecAcctSeq( secAcctSeq );
			signAccountData.setInvName( invName );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setSecCompCode( secCompCode );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ��Ϣʧ��" );
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
			acctAdmDetail.setUserId( userId );
			acctAdmDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctAdmDetail.setTxSeqId( txSeqId );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetail.setSecAcct( secAcct );
			acctAdmDetail.setInvType( invType );
			acctAdmDetail.setInvName( invName );
			acctAdmDetail.setIdType( hostIdType );
			acctAdmDetail.setInvIdCode( invIdCode );
			acctAdmDetail.setOldInvName( oldInvName );
			acctAdmDetail.setOdIdType( oldIdType );
			acctAdmDetail.setOldInvIdCode( oldInvIdCode );
			acctAdmDetail.setSecAcctSeq( secAcctSeq );
			acctAdmDetail.setSecCompCode( secCompCode );
			acctAdmDetail.setCapAcct( capAcct );
			acctAdmDetail.setAcctId( acctId );
			acctAdmDetail.setOpenDepId( openDepId );
			acctAdmDetail.setOpenBranchId( openBranchId );
			acctAdmDetail.setDepId( openDepId );
			acctAdmDetail.setBranchId( openBranchId );
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setSignFlag( signFlag );
			acctAdmDetail.setnSignFlag( signFlag );
			acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_UPD_CUST_INFO );
			acctAdmDetail.setBusiType( "26" );
			acctAdmDetail.setTxTime( txTime );
			acctAdmDetail.setAcctDealId( initSeqId );
			acctAdmDetail.setColFlag( "0" );
			acctAdmDetail.setAbStract( " " );
			acctAdmDetail.setCurCode(curCode);
			acctAdmDetail.setTxAmount(new BigDecimal("0.00"));
			acctAdmDetail.setAbstractStr( "����ͻ�������Ϣ" );

			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "�����˻�������ϸ��Ϣʧ��" );
		}
		SFLogger.info( context, "addAcctAdmDetail()����" );
	}

	private void addInvestData( Context context ) throws SFException {
		SFLogger.info( context, "addInvestData()��ʼ" );
		try {
			InvestData investData = new InvestData();
			investData.setInvType( invType );
			investData.setSecAcct( secAcct );
			investData.setInvName( invName );
			investData.setIdType( hostIdType );
			investData.setInvIdCode( invIdCode );//
			investData.setNationality( oldInvestData.getNationality() );//
			investData.setSex( oldInvestData.getSex() );//
			investData.setLegalName( oldLegalName );
			investData.setLegalIdCode( oldLegalIdCode );
			investData.setTrnName( oldTrnName );
			investData.setTrnIdType( oldInvestData.getTrnIdType() );//
			investData.setTrnIdCode( oldTrnIdCode );
			investData.setTrnPhone( oldInvestData.getTrnPhone() );//
			investData.setTrnMobile( oldTrnMobile );
			investData.setAddr( oldInvestData.getAddr() );//
			investData.setZip( oldInvestData.getZip() );//
			investData.setPhone( oldInvestData.getPhone() );//
			investData.setMobile( oldInvestData.getMobile() );//
			investData.setFax( oldInvestData.getFax() );//
			investData.setEmailAddr( oldInvestData.getEmailAddr() );//
			investData.setInterFlag( oldInvestData.getInterFlag() );//
			investData.setMemo( oldInvestData.getMemo() );//

			investDataDao.saveInvestData( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����Ͷ������Ϣʧ��" );
		}
		SFLogger.info( context, "addInvestData()����" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );

		try {
			// ȯ��Ԥָ����������
			if( SFUtil.isNotEmpty( acctId ) && !SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {
				// ���ͻ�������֤�����͡�֤������������ͬ,����������ͻ�����,�˴���pics��ͬ
				if( !oldInvName.equals( invName ) || !oldIdType.equals( hostIdType ) || !oldInvIdCode.equals( invIdCode ) ) {

					SFLogger.info( context, "��������ѯ�ͻ���Ϣ��ʼ" );

					Map<String, Object> msg = new HashMap<String, Object>();
					msg.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
					msg.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
					msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO

					// ���ù���������������ѯ�ͻ���Ϣ-�������ۺͶԹ�
					BizUtil.chkKeyInvestInfoClient( context, msg );

					SFLogger.info( context, "��������ѯ�ͻ���Ϣ����" );
				}
			} else if( SFUtil.isNotEmpty( acctId ) && SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {

				SFLogger.info( context, "�����в�ѯ�ͻ���Ϣ��ʼ" );

				// ���ù������� �Ϻ�����У��ͻ���Ϣ
				BizUtil.qryKeyInvestorBycoBankClient( context, tranConnection, capAcct, secCompCode );

				SFLogger.info( context, "�����в�ѯ�ͻ���Ϣ����" );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHost()����" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			// ���׳ɹ�-�������ȯ��
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );
			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ�������ȯ��

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00601" );

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

			// ��Ԥ���巵�ر���-�����쳣�˳�ʱ���� --��ʼ
			if( SFConst.SECU_ZL.equals( secuType ) ) {// ֱ��ģʽ
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100104_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// ����
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, keyColl, "SEC_ACCT", secAcct );
			}
			// ��֤ͨģʽ
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00601" );

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

			userId = secu.getUserId();// �ͻ����
			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// ���ɷ�����ˮ��
			SignAccountData signAccountData = signAccountDataDao.qrySignAcctDataAndInvestDateBySignFlag( context, tranConnection, secCompCode, capAcct );
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );

			// �������֤ȯRever�ڵ���Ҫ����brchId�����ţ������ӵ�context��
			SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " " );

			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
			txTime = DateUtil.getMacTime();// ȡ����ʱ��

			/* **************************************************
			 * ������飺 ��ǰȯ���Ƿ����������˻��ཻ�� 
			 * ************************************************/

			secAcctSeq = signAccountData.getSecAcctSeq();
			acctId = signAccountData.getAcctId();// ��չ������/�Թ��˺�
			openDepId = signAccountData.getOpenDepId();// �˻���������
			openBranchId = signAccountData.getOpenBranchId();// �˻���������
			signFlag = signAccountData.getSignFlag();// ǩԼ��ʶ
			initSide = signAccountData.getInitSide();// ���׷���

			if( null != signAccountData.getInvestData() ) {
				oldInvestData = signAccountData.getInvestData();// Ͷ���˾���Ϣ
				secAcct = oldInvestData.getSecAcct();
				oldLegalName = oldInvestData.getLegalName();// �ɵķ�������
				oldLegalIdCode = oldInvestData.getLegalIdCode();// �ɵķ���֤������
				oldTrnName = oldInvestData.getTrnName();// �ɵľ���������
				oldTrnIdCode = oldInvestData.getTrnIdCode();// �ɵľ�����֤������
				oldTrnMobile = oldInvestData.getTrnMobile();// �ɵľ������ֻ���
				oldInvName = oldInvestData.getInvName();
				oldIdType = oldInvestData.getIdType();
				oldInvIdCode = oldInvestData.getInvIdCode();
				SFUtil.chkCond( context, ( "#".equals( oldInvIdCode ) ), "ST5720", String.format( "ǩԼ��Ϣ����" ) );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}