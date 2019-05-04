/**
 * module.trans.secu2sf/SFOnline/T100301.java
 */
package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import module.bean.AgtCustomerInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.dao.AgtCustomerInfoDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryBalClient;
import module.trans.sf2cobank.T810025Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * A��ȯ�̶˷���--���ж�����ѯ����
 *
 * �����߼���
 *			1.����ȯ�̽��� ��<��>Acmt.009.01/11009��<ֱ>6027/612211��
 * 			2.����������Ч��
 *			3.����:����612325�ϴ�����ѯ���
 *			4.�������ڸ��ˣ�����D+�ӿ�R3034��ѯ�˻����
 *			5.�������ڶԹ�������D+�ӿ�C3019��ѯ�˻����
 *			6.����ȯ�� ��<��>Acmt.010.01/11009��<ֱ>6027/612211��
 *
 * tran code :100301
 * @author ������
 * @date 2017-9-19 ����08:39:04
 * @since 1.0
 */

public class T100301 extends TranBase {

	private String secCompCode = null;// ȯ�̴���

	private String capAcct = null;// ȯ�̶��ʽ�̨�˺�

	private String curCode = null;// ����

	private String acctId = null;// ��չ������/�Թ��˺�

	private String invType = null;// �ͻ����ͣ�1�����ˣ�2������

	private String invName = null;// �ͻ�����

	private String idType = null;// ֤������

	private String secuType = null;// ȯ�����ͣ�����ֱ������֤ͨ

	private String acctBal = null;// �ͻ��ʻ���������

	private String acctBalHKD = null;// �ͻ��ʻ��۱����

	private String acctBalUSD = null;// �ͻ��ʻ���Ԫ���

	private String chlSeqId = null; // 22λ��ˮ��

	private String initSeqId = null; // ������ˮ��

	private SignAccountData signAccountData = null;// ǩԼ��Ϣ

	@Override
	protected void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {
			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λǰ����ˮ��
			chlSeqId = BizUtil.getChlSeqId( context, BizUtil.getSubTxSeqId( initSeqId ) ); // ��ȡ22λ��ˮ��;

			// ��ȡȯ�̱���
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Acmt00901" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "��ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData );

			// ȯ������
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "ȯ�����Ͳ���Ϊ��[%s]", secuType ) );

			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ

				secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ); // ȯ�̴���
				capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" ); // ȯ�̶��ʽ�̨�˺�
				acctId = SFUtil.getReqDataValue( context, "ACCT_ID" ); // ��չ������/�Թ��˺�
				curCode = SFUtil.getReqDataValue( context, "CUR_CODE" ); // ����
				invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				String secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��

				curCode = SFUtil.getDataValue( context, kColl, "CUR_CODE" );
				invName = SFUtil.getDataValue( context, custKcoll, "INV_NAME" );
				idType = SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" );
				invType = BizUtil.convSZT2SFInvType( context, ( String )SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" ) );
				acctId = SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );
				capAcct = SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" );

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100301_I" );
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );
				SFUtil.addDataField( context, keyColl, "CUR_CODE", curCode );
				SFUtil.addDataField( context, keyColl, "INV_NAME", invName );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );
				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// ת���ͻ�����
				SFUtil.addDataField( context, keyColl, "ACCT_ID", acctId );
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );

				SFUtil.addDataElement( context, keyColl );
			}

			// Ĭ��RMB
			if( SFUtil.isEmpty( curCode ) ) {
				curCode = SFConst.CUR_CODE_RMB;
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

		// �������ȯ��
		doSecu( context );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );
		Context msgContext = null;
		Map<String, Object> msg = null;
		String initSide = null;
		DecimalFormat df = new DecimalFormat( "#0.00" );// ���ָ�ʽ����������λС��
		try {

			initSide = signAccountData.getInitSide();// ����
			if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) { // �����пͻ�
				AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
				AgtCustomerInfo agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
				String bankId = SFUtil.getReqDataValue( context, "BANK_ID" );
				if( null != agtCustomerInfo ) {
					bankId = agtCustomerInfo.getBankId();
				}

				// ����������
				msg = new HashMap<String, Object>();
				msg.put( "ACCT_ID", acctId );// �����˺�account_no
				msg.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
				msg.put( "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�StkAcct

				/*
				 * ���������ĵ�������
				 */
				T810025Client coBankProcess = new T810025Client();
				Context coBankMsgContext = coBankProcess.send( context, msg, bankId );

				String retFlag = SFUtil.getDataValue( coBankMsgContext, SFConst.PUBLIC_RET_FLAG );

				/* �ж��Ƿ�ɹ� */
				SFUtil.chkCond( context, ( !SFConst.RET_SUCCESS.equals( retFlag ) ), "ST5604", "��ѯ���п����ʧ��" );

				// �Ϻ����з��سɹ�
				KeyedCollection oKeyCol = SFUtil.getDataElement( coBankMsgContext, "810025_O" );
				invName = SFUtil.getDataValue( coBankMsgContext, oKeyCol, "INV_NAME" );
				acctBal = df.format( new BigDecimal( SFUtil.div( SFUtil.getDataValue( coBankMsgContext, oKeyCol, "ACCT_BAL" ) ) ) );
				SFLogger.info( coBankMsgContext, String.format( "��ѯ�����пͻ��ʻ����:�ͻ��˺�[%s],ȯ�̴���[%s],�ʽ��˺�[%s],�ͻ�����[%s],���[%s]���", acctId, secCompCode, capAcct, invName, acctBal ) );

				// ���ڿͻ�
			} else {
				SFLogger.info( context, "��������ѯ�ʻ���ʼ" );

				/*
				 * ���������ĵ�����
				 */
				msg = new HashMap<String, Object>();
				msg.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
				msg.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
				msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
				msg.put( "CUR_CODE", curCode );// ����CCY
				msg.put( "INV_NAME", invName );// �˻�����

				QryBalClient qryBalClient = new QryBalClient();
				msgContext = qryBalClient.send( context, msg );
				String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

				/* �ж��Ƿ�ɹ� */
				SFUtil.chkCond( context, ( !SFConst.RET_SUCCESS.equals( retFlag ) ), "ST5604", "��ѯ���п����ʧ��" );

				// ���ۿͻ�
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {

					// ��ȡ���ر���MSG_O_ICOLL�ڵ�
					IndexedCollection iColl = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
					KeyedCollection kColl1 = ( KeyedCollection )iColl.getElementAt( 0 );
//					acctId = SFUtil.getDataValue( msgContext, kColl1, "ACCT_ID" );// �ʽ��ʺ�
					curCode = SFUtil.getDataValue( msgContext, kColl1, "CUR_CODE" );// ����
					acctBal = df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, kColl1, "ACCT_BAL" ) ) );// ���
					acctBalHKD = ( null != SFUtil.getDataValue( msgContext, kColl1, "HKD_ACCT_BAL" ) ) ? df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, kColl1, "HKD_ACCT_BAL" ) ) ) : "0";// �۱����
					acctBalUSD = ( null != SFUtil.getDataValue( msgContext, kColl1, "USD_ACCT_BAL" ) ) ? df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, kColl1, "HKD_ACCT_BAL" ) ) ) : "0";// ��Ԫ���

				}
				// �Թ��ͻ�
				if( SFConst.INV_TYPE_CORP.equals( invType ) ) {

					// ��ȡ���ر���MSG_O�ڵ�
					KeyedCollection outKColl = SFUtil.getDataElement( msgContext, "MSG_O" );
					acctBal = df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, outKColl, "ACCT_BAL" ) ) );// ���
					acctBalHKD = ( null != SFUtil.getDataValue( msgContext, outKColl, "HKD_ACCT_BAL" ) ) ? df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, outKColl, "HKD_ACCT_BAL" ) ) ) : "0";// �۱����
					acctBalUSD = ( null != SFUtil.getDataValue( msgContext, outKColl, "USD_ACCT_BAL" ) ) ? df.format( SFUtil.object2Double( SFUtil.getDataValue( msgContext, outKColl, "HKD_ACCT_BAL" ) ) ) : "0";// ��Ԫ���
				}
				SFLogger.info( context, String.format( "��ѯ����[%s]���ɹ�:[%s]", curCode, acctBal ) );

				SFLogger.info( context, "��������ѯ�ʻ�������" );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
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

				SFUtil.setResDataValue( context, "CAP_ACCT", SFUtil.getReqDataValue( context, "CAP_ACCT" ) );// �ʽ�̨�˺�
				SFUtil.setResDataValue( context, "SEC_COMP_CODE", SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) );// ȯ�̱��
				SFUtil.setResDataValue( context, "ACCT_ID", SFUtil.getReqDataValue( context, "ACCT_ID" ) );// ���������˺�
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );// ȯ����ˮ��
				SFUtil.setResDataValue( context, "HKD_ACCT_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, acctBalHKD ) ) );// �۱����
				SFUtil.setResDataValue( context, "USD_ACCT_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, acctBalUSD ) ) );// ��Ԫ���
				SFUtil.setResDataValue( context, "ACCT_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, acctBal ) ) );// ��������

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				// ��װ����ȯ�̱���

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt01001" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				KeyedCollection scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				// KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				IndexedCollection acctSvcrIcoll = SFUtil.getDataElement( context, "AcctSvcr" );
				KeyedCollection acctSvcrKcoll = ( KeyedCollection )acctSvcrIcoll.get( 0 );
				// KeyedCollection acctSvcrKcoll2= (KeyedCollection) acctSvcrKcoll.clone();
				// acctSvcrIcoll.addDataElement(acctSvcrKcoll2); //Icoll ����������
				KeyedCollection bkBalKcoll = SFUtil.getDataElement( context, kColl, "BkBal" );

				SFUtil.setDataValue( context, bkAcctKcoll, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, scAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				// SFUtil.setDataValue( context, acctSvcrKcoll2, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, bkBalKcoll, "BEGIN_BAL", acctBal );
				SFUtil.setDataValue( context, bkBalKcoll, "TYPE", "0" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

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

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {

			// ��Ԥ���巵�ر���-�����쳣�˳�ʱ����
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100301_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// ����
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, keyColl, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );
			}
			// ��֤ͨģʽ
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt01001" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				KeyedCollection scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				IndexedCollection acctSvcrIcoll = SFUtil.getDataElement( context, "AcctSvcr" );
				KeyedCollection acctSvcrKcoll = ( KeyedCollection )acctSvcrIcoll.get( 0 );
				KeyedCollection bkBalKcoll = SFUtil.getDataElement( context, kColl, "BkBal" );

				SFUtil.setDataValue( context, bkAcctKcoll, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, scAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, bkBalKcoll, "BEGIN_BAL", acctBal );
				SFUtil.setDataValue( context, bkBalKcoll, "TYPE", "0" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
			}

			// �ͻ�����
			invType = SFUtil.getReqDataValue( context, "INV_TYPE" );
			signAccountData = signAccountDataDao.qrySignAccountDataByBranch( context, tranConnection, acctId, curCode, capAcct, secCompCode );
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );

			// �������֤ȯRever�ڵ���Ҫ����brchId�����ţ������ӵ�context��
			SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " " );

			// ���ǩԼ��ϵ,�˴�ɾȥ����������֤�ͻ�����֤
			String signFlag = signAccountData.getSignFlag();

			// ���ͻ����Ͳ���Ϊ��
			SFUtil.chkCond( context, ( SFUtil.isEmpty( signAccountData.getInvType() ) ), "ST5413", String.format( "�������ݿ��пͻ�����Ϊ�գ�����������ϵ" ) );

			// ���ͻ�����һ����
			SFUtil.chkCond( context, ( SFUtil.isNotEmpty( invType ) && ( !invType.equals( signAccountData.getInvType() ) ) ), "ST5413", String.format( "ȯ���͹����Ŀͻ����������в���������������ϵ" ) );
			invType = signAccountData.getInvType();

			/* ���ǩԼ��ϵ */
			if( SFConst.CUR_CODE_RMB.equals( curCode ) || SFConst.CUR_CODE_HKD.equals( curCode ) || SFConst.CUR_CODE_USD.equals( curCode ) ) {

				if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
					signFlag = "*";
				}
				if( SFUtil.isEmpty( signAccountData.getSignFlag() ) ) {
					signFlag = "-";
				}
				SFUtil.chkCond( context, ( "-".equals( signFlag ) ), "ST4377", String.format( "�ÿͻ�������" ) );
				SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5720", String.format( "�ѳ���ǩԼ" ) );
				SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ����" ) );
				SFUtil.chkCond( context, ( !SFConst.SIGN_FLAG_SIGN.equals( signFlag ) ), "ST5590", String.format( "��ǰǩԼ״̬������������ѯ����" ) );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}