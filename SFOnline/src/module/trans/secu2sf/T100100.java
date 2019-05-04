package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCardBinInfo;
import module.bean.AgtCustomerInfo;
import module.bean.AllyData;
import module.bean.BankSignData;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ParamCache;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtAgentInfoDao;
import module.dao.AgtCardBinInfoDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AllyDataDao;
import module.dao.BankSignDataDao;
import module.dao.CardBinInfoDao;
import module.dao.ProductInfoDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryBalClient;

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
import common.util.SpecialSecuUtil;

import core.log.SFLogger;

/**
 * A��ȯ�̶˷���--���������ཻ��
 * T100100��Ϊ�����ӽ��ף�
 * 
 * 1. Ԥָ��/ 2. һ��ʽǩԼ/ 3. ��������ԤԼ((���ж�ԤԼ��ȯ�̶�ȷ��))
 * 
 * �����߼���
 *          Ԥָ����
 *                  1.����ȯ�̽��� ��<��>Acmt.001.01/11002��<ֱ>6021/612201��
 *					2.����������Ч��,���ȯ�̺Ϳͻ����ϵ�
 *					3.����ȯ�� ��<��>Acmt.002.01/11002��<ֱ>6021/612201��
 *					
 *			һ��ʽǩԼ��
 *					1.����ȯ�̽��� ��<��>Acmt.001.01/11001��<ֱ>6025/612213��
 *					2.����������Ч��,���ȯ�̺Ϳͻ����ϵ�
 *					3.�Թ��ͻ���������һ��ʽǩԼ
 *					4.��֤�������жϣ����ÿ���BCARD93432(��Ƭ���Ͳ�ѯ)��BCARD93439(��Ƭ�ȼ���ѯ)����
 *					5.����M7030��ѯ�ͻ���Ϣ������R3036��ѯ��״̬�֡��ͻ��ţ�����R3034��ѯ������������Ϣ
 *					6������������612326�ϴ���ϵͳУ��һ��ʽǩԼ�ͻ���Ϣ
 *					7.����R3042�ÿ�״̬��
 *					8.����ȯ�� ��<��>Acmt.002.01/11001��<ֱ>6025/612213��
 *
 *			����ԤԼ��
 *					1.����ȯ�̽��� ��<��>Acmt.001.01/11013��ƽ����31002����̩���ϣ���<ֱ>6028/612212��
 *					2.����������Ч��
 *					3.�Թ��ͻ������������˽���
 *					4. �������и��˿ͻ�������D+�ӿ�R3036��ѯ�˻�״̬
 *					5. �������и��˿ͻ�������D+�ӿ�R3042���˻�״̬��
 *					6.����ȯ�� ��<��>Acmt.002.01/11013��ƽ����31002����̩���ϣ���<ֱ>6028/612212��
 *
 * tran code :100100
 * @author ������
 */
public class T100100 extends TranBase {

	private String subTxSeqId = null;// 16λ����ƽ̨��ˮ��

	private String initSeqId = null;// 14λ��־��(ǰ����ˮ��)

	private String secSeqId = null;// ȯ����ˮ��

	private String txSeqId = null; // 8λ������ˮ��

	private String chlSeqId = null; // 22λ������ˮ��

	private String txDate = null;// ȯ��������е�����

	private String txTime = null;// ��ȡʱ���HHMMSSttt

	private String hostIdType = null;// ������֤������

	private String secAcct = null;// ����Ͷ���˹����ʺ�

	private String userId = null;// �ͻ����

	private String signType = null;// ǩԼ���ͣ�����Ԥָ����һ��ʽ������ԤԼ

	private String secuType = null;// ȯ�����ͣ�����ֱ������֤ͨ

	private String secCompCode = null;// ȯ�̴���

	private String acctId = null;// ���п���

	private String initSide = null;// ����

	private int secAcctSeq = 0;// ��֤������˺����к�

	private String acctChldNum = null;// �˻�˳���

	private String dbSignFlag = null; // ����ǩԼ����ǩԼ��ʶ

	private String invType = null;// �ͻ�����

	private String invIdCode = null;// �ͻ�֤������

	private String capAcct = null;// �ʽ��ʺ�

	private String invName = null;// �ͻ�����

	private String curCode = null;// ����

	private String workMode = null;// ����ģʽ

	private String unitTellerId = null;// ����Ա���

	private InvestData investData = null;// Ͷ������Ϣ

	private SecCompData secu = null;// ȯ����Ϣ

	private SignAccountData signAccountData = null;// ǩԼ��Ϣ

	private ProductInfo productInfo = null;// ��Ʒ��Ϣ

	private boolean succResFlag = false;// ��ȷ�������ⷵ�ر�ʶ

	private DecimalFormat df = new DecimalFormat( "#0.00" );// ���ָ�ʽ����������λС��

	private CardBinInfoDao cardBinInfoDao = new CardBinInfoDao();

	private AgtCardBinInfoDao agtCardBinInfoDao = new AgtCardBinInfoDao();

	private BankSignDataDao bankSignDataDao = new BankSignDataDao();

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();

	private AcctJourDao acctJourDao = new AcctJourDao();

	/**
	 * ��ʼ��BIZ����
	 * @param context
	 * @throws SFException
	 */
	public void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {

			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λǰ����ˮ��
			subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16λ����ƽ̨��ˮ��
			chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // ��ȡ22λ��ˮ��;

			// ����ֱ����ʽ��ȡ��ȡ��������֤ͨ��ʽ��ȡȯ�̴���
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Acmt00101" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// ȯ�̱��
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "��ȯ����Ϣ������" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "��ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			// Ԥָ����ʶ�洢context�У����ڱ�ʶ�������ͣ�SECU_TRANS_TYPE = 1:ȯ��Ԥָ�� 2��һ�iʽǩԼ 3����������ԤԼ��
			signType = getsignTypeByTxCode( context );

			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������

			SFUtil.chkCond( context, ( SFUtil.isEmpty( secuType ) ), "ST5701", String.format( "[ȯ������]����Ϊ��" ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ

				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// ȯ����ˮ��
				unitTellerId = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TELLERID" );// ����Ա���

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_I" );
				SFUtil.setDataValue( context, keyColl, "BEGIN_BAL", AmtUtil.conv2SecuDivAmount( context, null != SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ? SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) : "0.00" ) );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );
				if( !keyColl.containsKey( "INTER_FLAG" ) ) {
					SFUtil.addDataField( context, keyColl, "INTER_FLAG", null );
				}
				SFUtil.addDataField( context, keyColl, "SEX", null );
				SFUtil.addDataField( context, keyColl, "NATIONALITY", null );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection senderKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Sender" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				KeyedCollection agtKcoll = SFUtil.getDataElement( context, kColl, "Agt" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );

				String idType = SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" );// ֤������
				String invTypeSZT = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// �ͻ�����
				String sex = SFUtil.getDataValue( context, custKcoll, "SEX" );// �Ա�
				String nationality = SFUtil.getDataValue( context, custKcoll, "NATIONALITY" );// ����
				String beginBal = ( null != SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) : "0.00";

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100100_I" );

				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// ����
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_TYPE", BizUtil.convSZT2SFInvType( context, invTypeSZT ) );// idType_SZT ת���ͻ�����Ϊ 1������ 2������
				SFUtil.addDataField( context, keyColl, "TRN_ID_CODE", SFUtil.getDataValue( context, agtKcoll, "TRN_ID_CODE" ) );
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// ��չ������/�Թ��˺�
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// ȯ�̶��ʽ�̨�˺�
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// ȯ����ˮ��
				SFUtil.addDataField( context, keyColl, "LEGAL_NAME", SFUtil.getDataValue( context, kColl, "LEGAL_NAME" ) );
				SFUtil.addDataField( context, keyColl, "LEGAL_ID_TYPE", SFUtil.getDataValue( context, kColl, "LEGAL_ID_TYPE" ) );
				SFUtil.addDataField( context, keyColl, "SEX", sex );// ���ݿͻ����������Ա�
				SFUtil.addDataField( context, keyColl, "NATIONALITY", nationality );// ���ù���
				SFUtil.addDataField( context, keyColl, "ADDR", SFUtil.getDataValue( context, custKcoll, "ADDR" ) );
				SFUtil.addDataField( context, keyColl, "ZIP", SFUtil.getDataValue( context, custKcoll, "ZIP" ) );
				SFUtil.addDataField( context, keyColl, "EMAIL_ADDR", SFUtil.getDataValue( context, custKcoll, "EMAIL_ADDR" ) );
				SFUtil.addDataField( context, keyColl, "FAX", SFUtil.getDataValue( context, custKcoll, "FAX" ) );
				SFUtil.addDataField( context, keyColl, "MOBILE", SFUtil.getDataValue( context, custKcoll, "MOBILE" ) );
				SFUtil.addDataField( context, keyColl, "PHONE", SFUtil.getDataValue( context, custKcoll, "PHONE" ) );
				SFUtil.addDataField( context, keyColl, "TRN_NAME", SFUtil.getDataValue( context, agtKcoll, "TRN_NAME" ) );
				SFUtil.addDataField( context, keyColl, "TRN_ID_TYPE", SFUtil.getDataValue( context, agtKcoll, "TRN_ID_TYPE" ) );
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", df.format( new BigDecimal( beginBal ) ) );
				SFUtil.addDataField( context, keyColl, "INIT_SIDE", SFConst.INIT_SIDE_SECU );// ȯ������ ��S
				SFUtil.addDataField( context, keyColl, "INTER_FLAG", BizUtil.convInterFlag( idType ) );// ת���������־ 0 �� 1
				SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.getDataValue( context, senderKcoll, "BrchId" ) );// ��֧������ʶ

				if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
					/* ƽ��֤ȯ�����Ƽ�����Ϣ ��ԤԼ�� */
					String bookNo = null;// ԤԼ��
					if( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) {
						KeyedCollection bookRefKcoll = SFUtil.getDataElement( context, kColl, "BookRef" );
						bookNo = SFUtil.getDataValue( context, bookRefKcoll, "Ref" );

						KeyedCollection recomKcoll = SFUtil.getDataElement( context, kColl, "Recom" );
						String cusMagNo = SFUtil.getDataValue( context, recomKcoll, "RemmCode" );// ƽ��֤ȯ�����Ƽ�����Ϣ
						SFUtil.addDataField( context, keyColl, "CUS_MAG_NO", cusMagNo );
					} else {
						bookNo = SFUtil.getDataValue( context, kColl, "UEAppntID" );
					}
					SFUtil.addDataField( context, keyColl, "BOOK_NO", bookNo );
				}

				SFUtil.addDataElement( context, keyColl );
			}
			// Ĭ�Ͽͻ���������
			SFUtil.setReqDataValue( context, "INV_TYPE", SFUtil.isEmpty( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) ? SFConst.INV_TYPE_RETAIL : ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );

			// Ĭ���Ա�����
			SFUtil.setReqDataValue( context, "SEX", BizUtil.convSZT2SFSex( context, ( String )SFUtil.getReqDataValue( context, "SEX" ), invType ) );

			// Ĭ�Ͼ������ʶ����
			SFUtil.setReqDataValue( context, "INTER_FLAG", SFUtil.isEmpty( SFUtil.getReqDataValue( context, "INTER_FLAG" ) ) ? SFConst.INTER_FLAG_DOMESTIC : ( String )SFUtil.getReqDataValue( context, "INTER_FLAG" ) );

			// Ĭ�Ϲ�������CHN
			SFUtil.setReqDataValue( context, "NATIONALITY", BizUtil.convSZT2SFNationality( context, ( String )SFUtil.getReqDataValue( context, "NATIONALITY" ) ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// һ�iʽǩԼ&��������ԤԼ
		if( !SFConst.SIGN_TYPE_PRE.equals( signType ) ) {

			// ����������ѯ�����ͺͿ��ȼ������˻���Ϣ���ÿ�״̬
			doHost( context );
		}

		// һ��ʽ�Ѿ��ɹ�ֱ�ӷ���ȯ�̳ɹ�
		if( succResFlag && SFConst.SIGN_TYPE_ONE.equals( signType ) ) {
			return;
		}

		// ����Ͷ������Ϣ��������ˮ��д��ǩԼ��¼��д���˻�����������ϸ
		addPublicInfo( context );

		// Ԥָ���Ѿ��ɹ�ֱ�ӷ���ȯ�̳ɹ�
		if( succResFlag && SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
			return;
		}

		// �������ȯ��
		doSecu( context );

		// ���ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECIF
		addAutoBecif( context );
	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()��ʼ" );

		try {

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // ȯ��Ԥָ��

				DBHandler.beginTransaction( context, tranConnection ); // ��������1

				// ���ͻ���ϢΪ��,�����ͻ���Ϣ��
				if( null == investData ) {
					addInvestData( context );
				}

				// ���潻����ˮ
				addAcctJour( context );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����1

				/* ���ǩԼ��ϵ */
				chkSignRalation( context );

				// �ɹ�Ԥָ��ֱ�ӷ���
				if( succResFlag ) {
					return;
				}

				DBHandler.beginTransaction( context, tranConnection ); // ��������3

				/* д��ǩԼ��¼����ȯ��Ԥָ��ʱȷ���ͻ�ǩԼģʽ */
				addSignAccountData( context );// ��ǩԼ��־SignFlag��3-ȯ��Ԥָ��

				/* д���˻�����������ϸ */
				addAcctAdmDetail( context );

				// ������ˮ״̬��ʶ
				updAcctJourJourFlag( context );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����3

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // һ�iʽǩԼ

				DBHandler.beginTransaction( context, tranConnection );// ��������3

				/* ����ǩԼ��¼TRDSignAccountData��״̬�ֱ�־SignFlagΪ '1-������'��ǩԼ״̬Ϊ��'1-ǩԼ������' */
				addSignAccountData( context );
				if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {
					// ����Ǻ����У���ѯagt_customerinfo���Ƿ���status = 4�Ŀͻ�������о�ֱ�ӣ���Ҫ��ɾ���ٲ���
					AgtCustomerInfo customerInfo = agtCustomerInfoDao.qryAgtCustomerInfoByStatus( context, tranConnection, capAcct, secCompCode, SFConst.SIGN_FLAG_CANCEL );
					if( null != customerInfo ) {
						agtCustomerInfoDao.delAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
					}
					// ��������пͻ���Ϣ
					addAgtCustomerInfo( context );

					// ����trdsignaccountdata�޸�initside��־ΪA
					updSignAccountDataInitSide( context, SFConst.INIT_SIDE_COBANK );

				}

				// д���˻�����������ϸ,����TRDAcctAdmDetail
				addAcctAdmDetail( context );

				// ������ˮ״̬��ʶ
				updAcctJourJourFlag( context );

				// ����ǩԼ����trdsignaccountdata��lmcard��״ֵ̬cardLevel
				String cardLevel = SFUtil.getReqDataValue( context, "CARD_LEVEL" );
				if( SFUtil.isNotEmpty( cardLevel ) ) {
					updSignAccountDataLmCard( context, cardLevel );
				}

				DBHandler.commitTransaction( context, tranConnection );// �ύ����3

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // ��������ԤԼ

				DBHandler.beginTransaction( context, tranConnection ); // ��������1

				// ���ͻ���ϢΪ��,�����ͻ���Ϣ��
				if( null == investData ) {
					addInvestData( context );
				}

				// ��¼������ˮ
				addAcctJour( context );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����1

				DBHandler.beginTransaction( context, tranConnection );// ��������2

				// ��TrdBankSignData��������д��TRDSignAccountData��
				addSignAccountData( context );

				addBankSignData( context );

				// д���˻�����������ϸ
				addAcctAdmDetail( context );

				// ��֤������ԤԼ����ɹ�����Ҫ��trdbanksigndata��lmcard��ֵ���¹�ȥ
				if( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) {
					BankSignData bankSignData = bankSignDataDao.qryBankSignData( context, tranConnection, secCompCode, acctId );
					if( null != bankSignData ) {
						// ��Ҫ����������
						if( SFUtil.isNotEmpty( bankSignData.getLmCard() ) ) {
							updSignAccountDataLmCard( context, bankSignData.getLmCard() );
						}
						// ��Ҫ����email�ֶ�
						if( SFUtil.isNotEmpty( bankSignData.getEmail() ) ) {
							updInvestDataEmail( context, bankSignData.getEmail() );
						}
					}
				}

				// ȯ�̼���ɹ�֮������ԤԼ��¼������ʷ����
				bankSignDataDao.migrateBankSignDataToHistory( context, tranConnection, acctId, secCompCode );

				bankSignDataDao.delBankSignData( context, tranConnection, acctId, secCompCode );

				// ������ˮ״̬��ʶ
				updAcctJourJourFlag( context );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����2

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()����" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );

		try {

			if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

				// ������-һ�iʽǩԼ
				doHostOfOneSign( context );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {

				// ������-��������ԤԼ
				doHostOfActive( context );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHost()����" );
	}

	/**
	 * 
	 * һ��ʽǩԼ���Ͽ����ж��Ƿ���������   ��֤�������ж� & ���ȼ���ѯ 
	 * @param context
	 * @throws SFException
	 */
	private void doHostOfOneSign( Context context ) throws SFException {
		SFLogger.info( context, "doHostOfOneSign()��ʼ" );
		Context msgContext = null;// ������Ӧ

		String resultFlag = "1";// �Ƿ���������ϵͳ��У��λ��0-�ǣ�1-��

		try {

			// �鷢�ͱ���map����
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
			msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
			msg.put( "CUR_CODE", curCode );// ����CCY
			msg.put( "INV_NAME", invName );// �˻�����ACCT_NAME
			msg.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			if( acctId.startsWith( "621626" ) || acctId.startsWith( "623058" ) ) {// ƽ����

				// ���ù�����ѯ�������Ϳ��ȼ��������ر���
				msgContext = BizUtil.qryCardTypeClient( context, msg );
				SFLogger.info( context, String.format( "��ѯ�������ݷ��ؿ��ȼ� [%s]", SFUtil.getDataValue( msgContext, "CARD_LEVEL" ) ) );
				SFUtil.addDataField( context, keyIColl, "CARD_LEVEL", SFUtil.getDataValue( msgContext, "CARD_LEVEL" ) );

			}

			if( SFConst.INIT_SIDE_SECU.equals( initSide ) ) { // ����

				SFLogger.info( context, "��������ѯ�ͻ���Ϣ��ʼ" );

				// ��������ѯ���˿ͻ���Ϣ ��֤���ŵ�֤�����͡�֤�����롢����
				BizUtil.chkKeyInvestInfoClient( context, msg );

				SFLogger.info( context, "��������ѯ�ͻ���Ϣ����" );

				SFLogger.info( context, "��������ѯ��״̬��ʼ" );

				// ��������ѯ��״̬���жϿ��Ƿ��ʧ����ʽ����ͷ��
				msgContext = BizUtil.qryCardAttrClient( context, msg );

				// ��ȡ��������״̬��
				resultFlag = SFUtil.getDataValue( msgContext, "RESULT_FLAG" );

				String ecifNo = SFUtil.getDataValue( msgContext, "MSG_O.BECIF_NO" );// ȡ�ͻ�ECIF��
				SFUtil.chkCond( msgContext, SFUtil.isEmpty( ecifNo ), "ST5602", String.format( "��������ȡ[�ͻ���]ʧ��" ) );
				SFLogger.info( msgContext, String.format( "�ͻ�ECIF��[%s]", ecifNo ) );
				SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );// �ͻ�ECIF�ŷ���context

				SFLogger.info( context, "��������ѯ��״̬����" );

				SFLogger.info( context, "��������ѯ�����ʺſ�ʼ" );

				// ��������ѯ�����ʺ�,ȡ�¿��ŵĿ�����������ʺ�
				qryHostAcctIdAndBranchId( context );

				SFLogger.info( context, "��������ѯ�����ʺŽ���" );

			} else if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {// ����һ��ʽǩԼ�������п���Ϣ��У��

				SFLogger.info( context, "�Ϻ�����һ��ʽǩԼ�������п���ϢУ�鿪ʼ" );

				// ������һ��ʽǩԼ�������п���ϢУ��
				chkKeyInvestorByCoBank( context );

				SFLogger.info( context, "�Ϻ�����һ��ʽǩԼ�������п���ϢУ�����" );

			}

			DBHandler.beginTransaction( context, tranConnection );// ��������1

			// ���ͻ���ϢΪ��,�����ͻ���Ϣ��
			if( null == investData ) {
				addInvestData( context );
			}

			// ��¼������ˮ
			addAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // �ύ����1

			// ���ǩԼ��ϵ
			chkSignRalation( context );

			// �ɹ�ֱ�ӷ���ȯ��
			if( succResFlag ) {
				return;
			}

			// ����FCR 31�Žӿڣ��ÿ���50״̬ʱ���������п�������FCR��״̬
			if( !"0".equals( resultFlag ) && SFConst.INIT_SIDE_SECU.equals( initSide ) ) {

				// ������FCR���ӿ�״̬�ַ���
				BizUtil.setCardStatusWord( context, acctId, "A", null );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHostOfOneSign()����" );
	}

	/**
	 * 
	 * ��������ѯ�����ʺ�,ȡ�¿��ŵĿ�����������ʺ�
	 * @param context
	 * @throws SFException
	 */
	private void qryHostAcctIdAndBranchId( Context context ) throws SFException {
		SFLogger.info( context, "qryHostAcctIdAndBranchId()��ʼ" );
		KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
		KeyedCollection kColl = null;
		try {
			// �鷢�ͱ���map����
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
			msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
			msg.put( "CUR_CODE", curCode );// ����CCY
			msg.put( "INV_NAME", invName );// �˻�����ACCT_NAME

			QryBalClient qryBalClient = new QryBalClient();
			Context msgContext = qryBalClient.send( context, msg );

			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "������[��ѯ��״̬]ʧ��" ) );

			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// ����
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
				kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// ��ȡ��Ӧ����
				String hostAcctId = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// �����ʺ� CustNo�ο��ӿ��ֶζ���ֵ

				SFLogger.info( context, String.format( "ȡ�������ʺ�Account:[%s]", hostAcctId ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( hostAcctId ), "ST5602", "��������ȡ[�����˺�]ʧ��" );
				SFUtil.addDataField( context, keyIColl, "SAV_ACCT", hostAcctId );

				String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// ȡ�����˻�����
				SFLogger.info( context, String.format( "ȡ�����˻�����pbcAcctType:[%s]", pbcAcctType ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5602", "��������ȡ[�����˻�����]ʧ��" );

				if( "2".equals( pbcAcctType ) ) {// �����˻�
					SFLogger.info( context, String.format( "��ȯ���ѽ�������˻�У�飬SecCompCode:[%s]", secCompCode ) );
					// û�鵽���������˻�ǩԼ��¼�����ر���
					SFUtil.chkCond( context, !"1".equals( secu.getIIAcctFlag() ), "ST5421", String.format( "��ȯ�̲���������[�����˻�ǩԼ]" ) );
					SFLogger.info( context, String.format( "��ȯ�̶����˻�У��ͨ��" ) );
				}
			}

			// �޶���������Ŵ�AppHead��ȡ����ĳ�R3034-TRAN_LIST_ARRAY�ڵ��ȡ edit by lch 20180515
			IndexedCollection tranListIColl = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
			KeyedCollection trankColl = ( KeyedCollection )tranListIColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String openDepId = SFUtil.getDataValue( msgContext, trankColl, "OPEN_DEP_ID" );// ���������
			SFLogger.info( context, String.format( "ȡ�����������[%s]", openDepId ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( openDepId ), "ST5602", String.format( "��������ȡ[�˺ſ�������]ʧ��" ) );

			SFUtil.setDataValue( context, keyIColl, "OPEN_DEP_ID", openDepId.trim() );// ���������
			SFUtil.setDataValue( context, keyIColl, "DEP_ID", openDepId.trim() );// ���������

			BankUnit bankUnit = BankUnitCache.getValue( openDepId );// ���ݿ�������Ż�ȡ�����������
			SFUtil.chkCond( context, ( null == bankUnit ), "ST5801", String.format( "��ѯ[�����������������]ʧ��" ) );
			SFLogger.info( context, String.format( "�����������������[%s]", bankUnit.getBranchId() ) );
			SFUtil.setDataValue( context, keyIColl, "OPEN_BRANCH_ID", bankUnit.getBranchId() );// �����������������
			SFUtil.setDataValue( context, keyIColl, "BRANCH_ID", bankUnit.getBranchId() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "qryHostAcctIdAndBranchId()����" );
	}

	/**
	 * 
	 * ������ϵͳ��ѯ�ͻ���Ϣ
	 * @param context
	 * @throws SFException
	 */
	private void chkKeyInvestorByCoBank( Context context ) throws SFException {
		SFLogger.info( context, "chkKeyInvestorByCoBank()��ʼ" );
		try {
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			AgtAgentInfo agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfoAndCardBinInfo( context, tranConnection, acctId );
			SFUtil.chkCond( context, ( null == agtAgentInfo ), "ST5799", String.format( "�ʽ��˺�[%s]ȯ�̴���[%s]�ͻ��˻�[%s]һ��ʽǩԼУ��ͻ���Ϣ�Ľ���,�ú�����û�п�ͨһ��ʽǩԼ��ֱ�ӷ���ʧ��", capAcct, secCompCode, acctId ) );
			String bankId = agtAgentInfo.getBankId();
			String coBankBranchId = agtAgentInfo.getBranchCode();

			// �������ϵͳ��ѯ�ͻ���Ϣ����
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
			msg.put( "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�CapAcct
			msg.put( "INV_NAME", invName );// �ͻ�����InvName
			msg.put( "ID_TYPE", hostIdType );// ֤������IdType
			msg.put( "INV_ID_CODE", invIdCode );// ֤������InvIdCode
			msg.put( "ACCT_ID", acctId );// �����˺�account_no
			msg.put( "CUR_CODE", curCode );// ����CurCode
			msg.put( "REMARK", "һ��ʽǩԼ" );// ��עMemo

			// ���������Ϻ�����У��һ��ʽǩԼ�ͻ���Ϣ--�������Ѵ����쳣��ʧ��
			Context msgContext = BizUtil.chkKeyInvestorBycoBankClient( context, tranConnection, msg, bankId );

			// �Ϻ����з��سɹ�
			String coBankStatus = SFUtil.getDataValue( msgContext, "810026_O.STATUS" );// �˻�״̬Status
			String agtBankId = SFUtil.getDataValue( msgContext, "810026_O.REMARK" );// ��עMemo

			SFUtil.chkCond( context, ( "1".equals( coBankStatus ) ), "ST5719", String.format( "�����ͻ����п�״̬�쳣��������������ǩԼ" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( coBankBranchId ) ), "ST5614", String.format( "ȡ�����������ʧ��" ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			SFUtil.setDataValue( context, keyIColl, "OPEN_DEP_ID", coBankBranchId );
			SFUtil.setDataValue( context, keyIColl, "DEP_ID", coBankBranchId );

			// �����з��غ������к� agtBankId �޺�����agtBankId����bankId
			if( SFUtil.isNotEmpty( agtBankId ) ) {
				SFLogger.info( context, String.format( "�����з��غ������к�Ϊ[%s]", agtBankId ) );
				AgtCardBinInfo agtCardBinInfo = agtCardBinInfoDao.qryAgtCardBinInfo( context, tranConnection, agtBankId, acctId );
				if( null == agtCardBinInfo ) {
					agtBankId = bankId;
				}
			} else {
				agtBankId = bankId;
			}

			// �����Ÿ�ֵ��ǩԼ��savacct�ֶ�
			SFUtil.addDataField( context, keyIColl, "SAV_ACCT", acctId );// ���������ʽ��ʺ����������
			SFUtil.addDataField( context, keyIColl, "BANK_ID", agtBankId );// �����������к����������

			/* �Ը۰�̨����������½ͨ��֤���д��� */

			// ͨ������������ҵ��������к�
			BankUnit bankUnit = BankUnitCache.getValue( coBankBranchId );// ���ݿ�������Ż�ȡ�����������
			SFUtil.chkCond( context, ( null == bankUnit ), "ST5801", String.format( "��ѯ[�����������������]ʧ��" ) );
			SFLogger.info( context, String.format( "�����������������[%s]", bankUnit.getBranchId() ) );
			SFUtil.setDataValue( context, keyIColl, "OPEN_BRANCH_ID", bankUnit.getBranchId() );// �����������������
			SFUtil.setDataValue( context, keyIColl, "BRANCH_ID", bankUnit.getBranchId() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkKeyInvestorByCoBank()����" );
	}

	/**
	 * 
	 * ��������ԤԼ��������ѯ��״̬�����ӿ�״̬�ַ���
	 * @param context
	 * @throws SFException
	 */
	private void doHostOfActive( Context context ) throws SFException {
		SFLogger.info( context, "doHostOfActive()��ʼ" );
		try {

			// �鷢�ͱ���map����
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
			msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
			msg.put( "CUR_CODE", curCode );// ����CCY
			msg.put( "INV_NAME", invName );// �˻�����ACCT_NAME

			// ��������ѯ��״̬���жϿ��Ƿ��ʧ����ʽ����ͷ��
			Context msgContext = BizUtil.qryCardAttrClient( context, msg );

			// ��ȡ��������״̬��
			String resultFlag = SFUtil.getDataValue( msgContext, "RESULT_FLAG" );

			String ecifNo = SFUtil.getDataValue( msgContext, "MSG_O.BECIF_NO" );// ȡ�ͻ�ECIF��
			SFUtil.chkCond( msgContext, SFUtil.isEmpty( ecifNo ), "ST5602", String.format( "��������ȡ[�ͻ���]ʧ��" ) );
			SFLogger.info( msgContext, String.format( "�ͻ�ECIF��ECIF_NO:[%s]", ecifNo ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );// �ͻ�ECIF�ŷ���context

			// �������ӿ�״̬�ַ���
			if( !"0".equals( resultFlag ) ) {
				BizUtil.setCardStatusWord( context, acctId, "A", null );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHostOfActive()����" );
	}

	/**
	 * ���ǩԼ��ϵ
	 *  1��Ԥָ�� 2 ��һ��ʽǩԼ
	 *
	 * @param context
	 * @throws SFException
	 */
	private void chkSignRalation( Context context ) throws SFException {
		SFLogger.info( context, "chkSignRalation()��ʼ" );

		try {

			String dbAcctId = null;
			/* ���ǩԼ��ϵ */
			SignAccountData signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );

			// ��ǩԼ��ϵ��¼
			if( signAccountData == null || SFUtil.isEmpty( signAccountData.getSignFlag() ) ) {
				dbSignFlag = "-";
			} else {
				// ���ݿ��������ʺ�
				dbAcctId = signAccountData.getAcctId();
				// ���ݿ���ǩԼ��ʶ
				dbSignFlag = signAccountData.getSignFlag();
				// ��֤������ʺŲ�һ��
				if( !secAcct.equals( signAccountData.getSecAcct() ) && ( !SFConst.SIGN_FLAG_CANCEL.equals( dbSignFlag ) || "-".equals( dbSignFlag ) ) ) {
					dbSignFlag = "*";
				}
			}

			// ǩԼ�����С�ǩԼȷ�ϴ����н�ȯ�̵Ŀ��Ÿ�ֵ��dbAcctId
			if( SFUtil.isNotEmpty( acctId ) && SFUtil.isEmpty( dbAcctId ) && ( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) ) ) {
				dbAcctId = acctId;
			}

			// ǩԼ�ɹ���dbAcctId��acctId���п���һ��
			if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) && !dbAcctId.equals( acctId ) ) {
				dbSignFlag = "*";
			}

			// ǩԼ��Ϣ����
			SFUtil.chkCond( context, ( "*".equals( dbSignFlag ) ), "ST5720", String.format( "ǩԼ��ϵ�Ѵ���,�Ҳ���" ) );

			// Ԥָ����0��1��5��6��9ǩԼ��ʶ����������
			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) || SFConst.SIGN_FLAG_BANK_PRE.equals( dbSignFlag ) || SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_PRE_IN_PROCESS.equals( dbSignFlag ) ) {
					SFUtil.chkCond( context, "ST5590", String.format( "�ͻ���ǰ״̬����������Ԥָ��" ) );
				} else if( SFConst.SIGN_FLAG_SECU_PRE.equals( dbSignFlag ) ) {
					// �óɹ�״̬��
					succResFlag = true;
					// �������ȯ��
					doSecu( context );
					return;
				}
				// һ��ʽ��1��3��5��6��9ǩԼ��ʶ����������
			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {
				if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_SECU_PRE.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_PRE_IN_PROCESS.equals( dbSignFlag ) ) {
					SFUtil.chkCond( context, "ST5590", String.format( "�ͻ���ǰ״̬����������ȯ��һ��ʽǩԼ" ) );
				} else if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) ) {
					// �óɹ�״̬��
					succResFlag = true;
					// �������ȯ��
					doSecu( context );
					return;
				}
			}

			/* �ѳ���ǩԼ��ϵ, ��������¼�Ƶ� TRDDesSignData ���� */
			if( SFConst.SIGN_FLAG_CANCEL.equals( dbSignFlag ) || "-".equals( dbSignFlag ) ) {

				/* ��Ϊ0,�ͻ���ǰ״̬����������Ԥָ�� */
				SFUtil.chkCond( context, ( null != signAccountData && SFUtil.object2Double( signAccountData.getAcctBal() ) > 0 ), "ST5331", String.format( "[��Ϊ0]�������������˽���" ) );

				/* ��̩����,���쳷����������Ԥָ����ǩԼ */
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) ) {
					SignAccountData signAccountData2 = signAccountDataDao.qrySignAccountDataByCloseDate( context, tranConnection, secCompCode, capAcct, txDate );

					SFUtil.chkCond( context, ( null != signAccountData2 ), "ST5541", String.format( "ǩԼ״̬Ϊ[����ǩԼ]����̩�����ͻ����쳷�������������˽���" ) );
				}

				DBHandler.beginTransaction( context, tranConnection ); // ��������2

				/* ����TRDDesSignData */
				signAccountDataDao.migrateSignAccountData( context, tranConnection, secCompCode, capAcct );

				/* ɾ��TRDSignAccountData */
				signAccountDataDao.delSignAccountData( context, tranConnection, secCompCode, capAcct );

				DBHandler.commitTransaction( context, tranConnection ); // �ύ����2
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSignRalation()����" );
	}

	/**
	 * ���ݽӿڽ������жϽ�������
	 * @param txCode
	 * @return
	 */
	public String getsignTypeByTxCode( Context context ) throws SFException {
		SFLogger.info( context, "getsignTypeByTxCode()��ʼ" );

		String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_SERVER_ID );// ����ӿں�
		if( SFConst.PRE_SIGN_SZT.equals( txCode ) || SFConst.PRE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_PRE;
		} else if( SFConst.ONE_SIGN_SZT.equals( txCode ) || SFConst.ONE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_ONE;
		} else if( SFConst.ACTIVE_SIGN_SZT_PA.equals( txCode ) || SFConst.ACTIVE_SIGN_SZT_OTHER.equals( txCode ) || SFConst.ACTIVE_SIGN_SZT_HT.equals( txCode ) || SFConst.ACTIVE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_ACTIVE;
		}

		SFLogger.info( context, "getsignTypeByTxCode()����" );
		return signType;
	}

	/**
	 * ���BIN,�������л��Ǻ����пͻ�
	 * @param context
	 * @param acctId
	 * @param sql
	 * @return
	 * @throws SFException
	 */
	private void chkCardBin( Context context, String acctId ) throws SFException {
		SFLogger.info( context, "chkCardBin()��ʼ" );
		if( SFUtil.isNotEmpty( acctId ) ) {
			// ��ѯ���п�bin��
			long sfCardBinCount = cardBinInfoDao.qryCardBinInfoCount( context, tranConnection, acctId );
			if( sfCardBinCount > 0 ) {
				initSide = SFConst.INIT_SIDE_SECU;
			} else {
				// ��ѯ�����п�bin��
				long coBankCardBinCount = agtCardBinInfoDao.qryCardBinInfoCount( context, tranConnection, acctId );
				if( coBankCardBinCount > 0 ) {
					initSide = SFConst.INIT_SIDE_COBANK;
				}
			}
		}
		SFLogger.info( context, "chkCardBin()����" );
	}

	/**
	 * 
	 * ǩԼ��ϵ�����ڣ���������ʺ����
	 * ��ȡ���secAcctSeq
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private int getSecAcctSeq( Context context ) throws SFException {
		SignAccountData signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
		secAcctSeq = signAccountData.getSecAcctSeq() + 1;
		SFLogger.info( context, String.format( "����secAcctSeq=%s", secAcctSeq ) );
		return secAcctSeq;
	}

	/**
	 * ����Ͷ������Ϣ 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addInvestData( Context context ) throws SFException {
		try {

			InvestData investData = new InvestData();
			investData.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			investData.setSecAcct( secAcct );
			investData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			investData.setIdType( hostIdType );
			investData.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			investData.setTrnName( ( String )SFUtil.getReqDataValue( context, "TRN_NAME" ) );
			investData.setTrnIdCode( ( String )SFUtil.getReqDataValue( context, "TRN_ID_CODE" ) );
			investData.setNationality( ( String )SFUtil.getReqDataValue( context, "NATIONALITY" ) );
			investData.setSex( ( String )SFUtil.getReqDataValue( context, "SEX" ) );
			investData.setTrnIdType( ( String )SFUtil.getReqDataValue( context, "TRN_ID_TYPE" ) );
			investData.setTrnPhone( ( String )SFUtil.getReqDataValue( context, "TRN_PHONE" ) );
			investData.setAddr( ( String )SFUtil.getReqDataValue( context, "ADDR" ) );
			investData.setZip( ( String )SFUtil.getReqDataValue( context, "ZIP" ) );
			investData.setPhone( ( String )SFUtil.getReqDataValue( context, "PHONE" ) );
			investData.setMobile( ( String )SFUtil.getReqDataValue( context, "MOBILE" ) );
			investData.setFax( ( String )SFUtil.getReqDataValue( context, "FAX" ) );
			investData.setInterFlag( ( String )SFUtil.getReqDataValue( context, "INTER_FLAG" ) );
			investData.setEmailAddr( ( String )SFUtil.getReqDataValue( context, "EMAIL_ADDR" ) );
			investData.setTrnMobile( ( String )SFUtil.getReqDataValue( context, "TRN_MOBILE" ) );
			investData.setLegalName( ( String )SFUtil.getReqDataValue( context, "LEGAL_NAME" ) );
			investData.setLegalIdCode( ( String )SFUtil.getReqDataValue( context, "LEGAL_ID_TYPE" ) );
			investData.setMemo( "" );

			if( SFConst.SIGN_TYPE_ONE.equals( signType ) || SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // һ�iʽǩԼ&����ԤԼ
				if( SFUtil.isNotEmpty( secAcct ) && SFConst.INIT_SIDE_SECU.equals( initSide ) ) { // ���п��Ÿ���BECIF��
					investData.setBecifNo( ( String )SFUtil.getReqDataValue( context, "ECIF_NO" ) );
				}
			}
			investDataDao.saveInvestData( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����Ͷ����ʧ��" );
		}
	}

	/**
	 * ����Ͷ����Email
	 * @param context
	 * @param email
	 * @return
	 * @throws SFException
	 */
	private void updInvestDataEmail( Context context, String email ) throws SFException {
		try {
			InvestData investData = new InvestData();
			investData.setEmailAddr( email );
			investData.setSecAcct( secAcct );

			investDataDao.saveInvestData( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����Ͷ����ʧ��" );
		}
	}

	/**
	 * ������ˮ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAcctJour( Context context ) throws SFException {
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setTxDate( txDate );
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			acctJour.setIdType( hostIdType );
			acctJour.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctJour.setSecAcct( secAcct );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			acctJour.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.CREDIT_FLAG );
			acctJour.setAcctBal( new BigDecimal( 0.00 ) );
			acctJour.setAbst( "" );
			acctJour.setJourFlag( "33" );
			acctJour.setTxTime( txTime );
			acctJour.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
			acctJour.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );
			acctJour.setUnitTellerId( unitTellerId );
			acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
			acctJour.setAcctDealId( initSeqId );
			acctJour.setProductType( "03" );
			acctJour.setColFlag( "0" );
			acctJour.setMemo( "" );
			acctJour.setTranSeqId( chlSeqId );
			acctJour.setBusiSeqId( chlSeqId );
			acctJour.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // ȯ��Ԥָ��
				acctJour.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctJour.setAbstractStr( "ȯ�̶�Ԥָ��" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_PRE_SIGN );
				acctJour.setBusiType( "21" );
			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // һ�iʽǩԼ
				acctJour.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctJour.setAbstractStr( "ȯ��һ��ʽǩԼ" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_ONE_SIGN );
				acctJour.setBusiType( "22" );
			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // ��������ԤԼ
				acctJour.setTxAmount( new BigDecimal( 0.00 ) );
				acctJour.setAbstractStr( "��������Ԥָ��" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SIGN );
				acctJour.setBusiType( "22" );
			}

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���뽻����ˮʧ��" );
		}
	}

	/**
	 * ������ˮ״̬��ʶ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void updAcctJourJourFlag( Context context ) throws SFException {
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setJourFlag( "00" );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���½�����ˮʧ��" );
		}
	}

	/**
	 * 
	 *
	 * �����˻�����������ϸ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAcctAdmDetail( Context context ) throws SFException {
		try {
			AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
			acctAdmDetail.setTxDate( txDate );
			acctAdmDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctAdmDetail.setUserId( userId );
			acctAdmDetail.setTxSeqId( txSeqId );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetail.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctAdmDetail.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			acctAdmDetail.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctAdmDetail.setSecAcct( secAcct );
			acctAdmDetail.setSecAcctSeq( secAcctSeq );
			acctAdmDetail.setSecCompCode( secCompCode );
			acctAdmDetail.setCapAcct( capAcct );
			acctAdmDetail.setAcctId( acctId );
			acctAdmDetail.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			acctAdmDetail.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			acctAdmDetail.setOldAcctId( acctId );
			acctAdmDetail.setCurCode( curCode );
			acctAdmDetail.setDcFlag( SFConst.CREDIT_FLAG );
			acctAdmDetail.setAbStract( " " );
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setTxTime( txTime );
			acctAdmDetail.setUnitTellerId( unitTellerId );
			acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
			acctAdmDetail.setCusMagNo( ( String )SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) );
			acctAdmDetail.setAcctDealId( initSeqId );// 14λ��־��
			acctAdmDetail.setColFlag( "0" );
			acctAdmDetail.setMemo( "" );

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // ȯ��Ԥָ��
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctAdmDetail.setAbstractStr( "ȯ�̶�Ԥָ��" );
				acctAdmDetail.setSignFlag( dbSignFlag );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SECU_PRE );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_PRE_SIGN );
				acctAdmDetail.setBusiType( "21" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // һ�iʽǩԼ
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctAdmDetail.setAbstractStr( "ȯ��һ��ʽǩԼ" );
				acctAdmDetail.setSignFlag( dbSignFlag );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SIGN );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_ONE_SIGN );
				acctAdmDetail.setBusiType( "22" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // ��������ԤԼ
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( 0.00 ) );
				acctAdmDetail.setAbstractStr( "ȯ�̼�������Ԥָ��" );
				acctAdmDetail.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SIGN );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SIGN );
				acctAdmDetail.setBusiType( "22" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			}
			AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "�����˻�������ϸʧ��" );
		}
	}

	/**
	 * ����ǩԼ�˻����
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addSignAccountData( Context context ) throws SFException {
		try {

			SignAccountData signAccountData = new SignAccountData();
			signAccountData.setInitSide( SFConst.INIT_SIDE_SECU );
			signAccountData.setSecAcct( secAcct );
			signAccountData.setSecAcctSeq( secAcctSeq );
			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setProductType( "03" );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setCashRemitFlag( SFConst.CASH_FLAG );
			signAccountData.setShsthCode( "" );
			signAccountData.setSzsthCode( "" );
			signAccountData.setAcctId( acctId );
			signAccountData.setSavAcct( ( String )SFUtil.getReqDataValue( context, "SAV_ACCT" ) );
			signAccountData.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			signAccountData.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			signAccountData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			signAccountData.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			signAccountData.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
			signAccountData.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );
			signAccountData.setDesDepId( "" );
			signAccountData.setDesBranchId( "" );
			signAccountData.setUnitTellerId( unitTellerId );
			signAccountData.setDesUnitTellerId( "" );
			signAccountData.setCusMagno( ( String )SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) );
			signAccountData.setOpenDate( ( String )SFUtil.getReqDataValue( context, "MAX_OPEN_DATE" ) );
			signAccountData.setPreTxDate( "19000101" );
			signAccountData.setBeginBal( new BigDecimal( 0.00 ) );
			signAccountData.setAcctBal( new BigDecimal( 0.00 ) );
			signAccountData.setIsMailBill( "0" );
			signAccountData.setMailDate( "" );
			signAccountData.setFlags( SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );// Ӫҵ������

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // ȯ��Ԥָ��
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SECU_PRE );//
				signAccountData.setOpenDate( txDate );//
				signAccountData.setSignMode( "1" );//

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // һ�iʽǩԼ

				signAccountData.setAcctChldNum( "00" );
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
				signAccountData.setStatFlag( "1" );
				signAccountData.setActiveFlag( "1" );
				signAccountData.setOpenDate( txDate );
				signAccountData.setSignDate( txDate );
				signAccountData.setSignMode( "3" );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // ��������ԤԼ

				signAccountData.setAcctChldNum( acctChldNum );// �˻�˳���
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
				signAccountData.setStatFlag( "1" );
				signAccountData.setOpenDate( ( String )SFUtil.getReqDataValue( context, "MAX_TX_DATE" ) );
				signAccountData.setSignDate( txDate );
				signAccountData.setSignMode( "2" );
				signAccountData.setChannel( ( String )SFUtil.getReqDataValue( context, "CHANNEL" ) );
			}
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ��¼ʧ��" );
		}
	}

	/**
	 * ����ǩԼ�˻������������Ϣ
	 * @param context
	 * @param cardLv
	 * @return
	 * @throws SFException
	 */
	private void updSignAccountDataLmCard( Context context, String cardLv ) throws SFException {

		try {
			SignAccountData signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );
			signAccountData.setLmCard( cardLv );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ�˻���������Ϣʧ��" );
		}
	}

	/**
	 * ����ǩԼ�˻�����
	 * @param context
	 * @param initSide
	 * @return
	 * @throws SFException
	 */
	private void updSignAccountDataInitSide( Context context, String initSide ) throws SFException {
		try {

			SignAccountData signAccountData = new SignAccountData();
			signAccountData.setInitSide( initSide );
			signAccountData.setSecAcct( secAcct );
			signAccountData.setSecAcctSeq( secAcctSeq );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setSecCompCode( secCompCode );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����ǩԼ�˻�������Ϣʧ��" );
		}
	}

	/**
	 * ��������Ԥָ����Ϣ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addBankSignData( Context context ) throws SFException {
		try {
			BankSignData bankSignData = new BankSignData();
			bankSignData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
			bankSignData.setCapAcct( capAcct );
			bankSignData.setOpenDate( txDate );
			bankSignData.setOpenTime( txTime );
			bankSignData.setSubTxSeqId2( subTxSeqId );
			bankSignData.setSecCompCode( secCompCode );
			bankSignData.setAcctId( acctId );
			bankSignData.setCurCode( curCode );
			bankSignData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			bankSignData.setTxDate( ( String )SFUtil.getReqDataValue( context, "MAX_TX_DATE" ) );
			bankSignData.setTxTime( txTime );

			bankSignDataDao.saveBankSignData( context, tranConnection, bankSignData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "��������Ԥָ����Ϣʧ��" );
		}
	}

	/**
	 * ���������пͻ���Ϣ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAgtCustomerInfo( Context context ) throws SFException {
		try {
			AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
			agtCustomerInfo.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			agtCustomerInfo.setAcctNo( acctId );
			agtCustomerInfo.setStkAcct( capAcct );
			agtCustomerInfo.setBankId( ( String )SFUtil.getReqDataValue( context, "BANK_ID" ) );// �к����з��غ������к�ʹ��agtBankId,����ʹ��bankId
			agtCustomerInfo.setOpenBranch( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			agtCustomerInfo.setStkCode( secCompCode );
			agtCustomerInfo.setCurCode( curCode );
			agtCustomerInfo.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			agtCustomerInfo.setIdType( ( String )SFUtil.getReqDataValue( context, "ID_TYPE" ) );// ������ȡ֤�����͸�ֵ
			agtCustomerInfo.setInvidCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			agtCustomerInfo.setOpenDate( txDate );
			agtCustomerInfo.setStatus( "0" );
			agtCustomerInfo.setMemo( "һ��ʽǩԼ" );

			agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, agtCustomerInfo );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "���������пͻ���Ϣʧ��" );
		}
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			if( SFConst.SECU_ZL.equals( secuType ) ) { // ֱ��ģʽ
				SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
				SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", ( String )SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );

				// ȯ��Ԥָ��&һ�iʽǩԼ
				if( SFConst.SIGN_TYPE_PRE.equals( signType ) || SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

					SFUtil.setResDataValue( context, "BEGIN_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );

					// ��������ԤԼ
				} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {

					SFUtil.setResDataValue( context, "ACCT_ID", acctId );

				}
			}

			if( SFConst.SECU_SZT.equals( secuType ) ) { // ��֤ͨģʽ

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00201" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// ��<BkAcct>���
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

				// ��<ScBal>���
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				SFUtil.setDataValue( context, scBalKcoll, "BEGIN_BAL", SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doSecu()����" );
	}

	/**
	 * ��д����ǰ�ü��
	 */
	@Override
	protected void chkStart( Context context ) throws SFException {

		try {

			invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // �ͻ�����
			invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // ֤������
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" ); // ��չ������/�Թ��˺�
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" ); // ȯ�̶��ʽ�̨�˺�
			invName = SFUtil.getReqDataValue( context, "INV_NAME" ); // �ͻ�����
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" ); // ����
			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// ���ɷ�����ˮ��
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // ת��ǰ�ͻ�֤������

			// ��Ԥ���巵�ر���-�����쳣�˳�ʱ���� --��ʼ
			if( SFConst.SECU_ZL.equals( secuType ) ) {// ֱ��ģʽ
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// ����
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, keyColl, "SEC_ACCT", secAcct );
				SFUtil.setDataValue( context, keyColl, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setDataValue( context, keyColl, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );
				SFUtil.setDataValue( context, keyColl, "BEGIN_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
			}
			// ��֤ͨģʽ
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00201" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// ��<BkAcct>���
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

				// ��<ScBal>���
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				SFUtil.setDataValue( context, scBalKcoll, "BEGIN_BAL", SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );
			}
			// ���ؼ��ֶ��Ƿ�Ϊ��
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "ȯ�̱�Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "ȯ�̶˿ͻ��ʽ�̨�˺Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( !SFConst.SIGN_TYPE_PRE.equals( signType ) && SFUtil.isEmpty( acctId ) ), "ST4388", String.format( "�����˺Ų���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "֤�����Ͳ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "֤�����벻��Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "Ͷ�������Ʋ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "�ͻ����Ͳ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "�ұ���Ϊ��" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "ȯ����ˮ�Ų���Ϊ��" ) );

			// �����С��λ��Ч��
			AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );

			// �����ж�֤��������ͻ����͵Ĺ�ϵ��֤������Ϊ10�ģ��ͻ�����ֻ��Ϊ1��֤������Ϊ51��52�ģ��ͻ�����ֻ��Ϊ2
			if( SFConst.INV_TYPE_CORP.equals( invType ) && "10".equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_TYSHXYDM.equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_YYZZ.equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_ZZJGDMZ.equals( idType ) ) {
				SFUtil.chkCond( context, "ST4386", String.format( "֤�����Ͳ���" ) );
			}

			// ����֤ȯ�����֤ȯ��У��ȯ��Ӫҵ����Ϣ
			SpecialSecuUtil.chkSecBrchIdBySecCompCode( context, secCompCode, SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );

			// ֤������20��21��������Ԥָ����һ��ʽ����
			SFUtil.chkCond( context, ( SFConst.ID_TYPE_PERSON_WGGMQTZJ.equals( idType ) || "21".equals( idType ) ) && !SFConst.SIGN_TYPE_ACTIVE.equals( signType ), "ST5100", String.format( "֤�����Ͳ���" ) );

			// һ�iʽǩԼ
			if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

				// ���������������˽���
				SFUtil.chkCond( context, ( SFConst.INV_TYPE_CORP.equals( invType ) ), "ST5422", String.format( "[�Թ��ͻ�]�����������˽���" ) );

				// ��鿨BIN
				chkCardBin( context, acctId );

				// �������кͺ����п�����һ��ʽǩԼ
				SFUtil.chkCond( context, ( SFUtil.isEmpty( initSide ) ), "ST5103", String.format( "�ÿ��������˽���" ) );

			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		long bankSignCount = 0;
		long sfCapAcctNum = 0;
		String invIdCode18Card19 = null; // 15λת18λ֤������,20����
		String invIdCode18Card20 = null;
		try {

			// ����Ƿ�����ظ���ˮ
			BizUtil.chkRepeatAcctJour( context, tranConnection );

			KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_I" );
			userId = secu.getUserId();
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
			txTime = DateUtil.getMacTime();// ȡ����ʱ��
			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ȯ��֤������ת��Ϊ����֤������
			workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			// �����и��˿ͻ�ֻ����������ʱ��һ��ʽǩԼ
			SFUtil.chkCond( context, ( SFConst.INIT_SIDE_COBANK.equals( initSide ) && !SFConst.WORKMODE_NORMAL.equals( workMode ) ), "ST5104", String.format( "����������ʱ��θÿ��������˽���" ) );

			investData = investDataDao.qryInvestData( context, tranConnection, hostIdType, invIdCode );
			if( null != investData ) {
				secAcct = investData.getSecAcct();
				// Ԥָ�� &һ��ʽ ��������������ƶ����ʽ��ʺ�ֻҪδ��������������ƶ�����
				if( !SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
					AllyDataDao allyDataDao = new AllyDataDao();
					AllyData allyData = allyDataDao.qryAllyDataByCapAcct( context, tranConnection, secCompCode, capAcct );
					long bankCount = bankSignDataDao.qryBankSignDataChkInfoBySignFlag( context, tranConnection, capAcct, secCompCode );

					// ���ʽ��˺Ŵ�������Ԥָ��״̬����������Ԥָ���ʽ��˺ţ���������ȯ��Ԥָ������
					SFUtil.chkCond( context, ( allyData != null && SFConst.SIGN_FLAG_BANK_PRE.equals( allyData.getUseFlag() ) || bankCount != 0 ), "ST4525", String.format( "����Ԥָ���ʽ��˺Ų�������ȯ��Ԥָ������" ) );

					// ͳ�Ƹÿͻ����ʽ��˺�ǩԼ�������ͬʱǩԼn(��������)���ʽ��˺�
					long signCount = signAccountDataDao.qrySignAccountDataTotalCountBySecAcct( context, tranConnection, secAcct );

					// �˶�֤�����͡�֤�����룬����֤��15/18λ����
					if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) && invIdCode.length() == 15 ) {
						invIdCode18Card19 = BizUtil.converTo18Card19( invIdCode ); // 15λת18λ֤������,20����
						invIdCode18Card20 = BizUtil.converTo18Card20( invIdCode ); // 15λת18λ֤�����룬21����
					}
					bankSignCount = bankSignDataDao.qryBankSignDataCountByInvIdCode( context, tranConnection, hostIdType, invIdCode, invIdCode18Card19, invIdCode18Card20 );

					if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
						sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_RETAIL", "CAPACCT_NUM" ).getValue() );
						SFUtil.chkCond( context, ( SFUtil.add( bankSignCount, signCount ) > sfCapAcctNum ), "ST5100", String.format( "���˿ͻ�������ͬʱǩԼ[%s]���ʽ��˺�", sfCapAcctNum ) );
					} else {
						sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_CORP", "CAPACCT_NUM" ).getValue() );
						SFUtil.chkCond( context, ( signCount > sfCapAcctNum ), "ST5100", String.format( "�Թ��ͻ�������ͬʱǩԼ[%s]���ʽ��˺�", sfCapAcctNum ) );
					}
				}

			} else {
				secAcct = BizUtil.genSecAcctId( context );// ����Ͷ���˹����ʺ�
				SFUtil.chkCond( context, SFUtil.isEmpty( secAcct ), "ST5711", "����[����Ͷ���˹����˺�]ʧ��" );
			}
			secAcctSeq = getSecAcctSeq( context );

			// ��������ԤԼ
			if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
				// ���TRDSignAccountData��ȯ���ʽ��˺��Ƿ��Ѵ���
				signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );// ǩԼ��Ϣ
				if( null != signAccountData ) {
					String signFlag = signAccountData.getSignFlag();
					SFUtil.chkCond( context, ( !SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5590", String.format( "��ǰǩԼ״̬������������ҵ��" ) );
					SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5541", String.format( "ǩԼ״̬Ϊ[����ǩԼ]����������ȯ�̼�������Ԥָ��" ) );
				}

				// ��֤ͨ������Ϊ0
				SFUtil.chkCond( context, ( SFConst.SECU_SZT.equals( secuType ) && SFUtil.object2Double( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) != 0 ), "ST4433", String.format( "������Ϊ��" ) );
				String bookNo = SFUtil.getReqDataValue( context, "BOOK_NO" );// ԤԼ��

				// ��鵱ǰȯ���Ƿ�����������������Ԥָ������
				SFUtil.chkCond( context, ( !"1".equals( secu.getBankPreSignFlag() ) ), "ST5421", String.format( "��ȯ�̲���������[����Ԥָ������]" ) );

				// ԤԼ�Ų���Ϊ��
				SFUtil.chkCond( context, ( "1".equals( secu.getCheckBookNoFlag() ) && SFUtil.isEmpty( bookNo ) ), "ST5701", String.format( "[ԤԼ��ˮ��]����Ϊ��" ) );

				// ���Ͷ������Ϣ�Ƿ�������Ԥָ����Ϣһ��
				// �����ϵ�ԤԼ�ſ�����ͬ������Ҫ�����һ����¼
				BankSignData bankSignData = bankSignDataDao.qryBankSignDataMaxTxDate( context, tranConnection, invName, secCompCode, acctId, curCode );
				SFUtil.chkCond( context, ( null == bankSignData ), "ST5712", String.format( "�ͻ����Ͳ���" ) );
				SFUtil.chkCond( context, ( SFUtil.isEmpty( bankSignData.getTxDate() ) || SFUtil.isEmpty( bankSignData.getTxTime() ) ), "ST5042", String.format( "[�ͻ���Ϣ]��������[����Ԥָ��]��Ϣ" ) );
				SFUtil.addDataField( context, keyColl, "MAX_TX_DATE", bankSignData.getTxDate() );
				SFUtil.addDataField( context, keyColl, "CHANNEL", bankSignData.getChannel() );
				SFUtil.setDataValue( context, keyColl, "OPEN_DEP_ID", bankSignData.getOpenDepId() );
				SFUtil.setDataValue( context, keyColl, "OPEN_BRANCH_ID", bankSignData.getOpenBranchId() );
				SFUtil.setDataValue( context, keyColl, "DEP_ID", bankSignData.getDepId() );
				SFUtil.setDataValue( context, keyColl, "BRANCH_ID", bankSignData.getBranchId() );
				// �˻�˳���
				acctChldNum = bankSignData.getAcctChldNum();

				// У��ͻ�����
				SFUtil.chkCond( context, ( !invType.equals( bankSignData.getInvType() ) ), "ST4385", String.format( "�ͻ����Ͳ���" ) );

				// ��������֤�����ͽ��бȽ�
				SFUtil.chkCond( context, ( !hostIdType.equals( bankSignData.getIdType() ) ), "ST4385", String.format( "֤�����Ͳ���" ) );

				// �˶�ԤԼ��
				if( "1".equals( secu.getCheckBookNoFlag() ) ) {
					SFUtil.chkCond( context, ( !bookNo.equals( bankSignData.getBookNo() ) ), "ST4513", String.format( "ԤԼ��ˮ��Ϣ����" ) );
				}
				// �˶Թ㷢���ʽ��˺�
				SpecialSecuUtil.chkCapAcctBySecCompCode( context, secCompCode, capAcct, bankSignData.getCapAcct() );

				// �˶�֤�����͡�֤�����룬����֤��15/18λ����
				String bankInvIdCode = bankSignData.getInvIdCode();
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {
					BizUtil.chkRetailInvIdCode( context, invIdCode, bankInvIdCode );
				}
				String bankSignFlag = bankSignData.getSignFlag();
				SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_SIGN.equals( bankSignFlag ) ), SFConst.RESPCODE_SUCCCODE, String.format( "������Ԥָ����Ϣ�Ѽ���ɹ�" ) );
				SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL_PRE.equals( bankSignFlag ) ), "ST5581", String.format( "ǩԼ״̬Ϊ[ԤԼ�ѳ���]�������������˽���" ) );
				SFUtil.chkCond( context, ( ( SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( bankSignFlag ) ) || ( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( bankSignFlag ) ) ), "ST5591", String.format( "ǩԼ״̬������" ) );

			} else if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				ProductInfoDao productInfoDao = new ProductInfoDao();
				productInfo = productInfoDao.qryProductInfoByDepId( context, tranConnection, secCompCode );
				if( null != productInfo ) {
					SFUtil.setDataValue( context, keyColl, "OPEN_DEP_ID", productInfo.getTruOpnDepId() );
					SFUtil.setDataValue( context, keyColl, "OPEN_BRANCH_ID", productInfo.getOpenBranchId() );
					SFUtil.setDataValue( context, keyColl, "DEP_ID", productInfo.getTruOpnDepId() );
					SFUtil.setDataValue( context, keyColl, "BRANCH_ID", productInfo.getOpenBranchId() );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 
	 * һ�iʽǩԼ&��������ԤԼ:
	 * ���ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECIF
	 * @param context
	 * @throws SFException
	 */
	private void addAutoBecif( Context context ) throws SFException {

		try {
			if( !SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				Map<String, Object> becifMap = new HashMap<String, Object>();
				becifMap.put( "ECIF_NO", SFUtil.objectToString( SFUtil.getReqDataValue( context, "ECIF_NO" ) ) );
				becifMap.put( "TX_DATE", txDate );
				becifMap.put( "TX_TIME", txTime );
				becifMap.put( "INV_TYPE", invType );
				becifMap.put( "SUB_TX_SEQ_ID", subTxSeqId );
				becifMap.put( "USER_ID", userId );
				becifMap.put( "SEC_COMP_CODE", secCompCode );
				becifMap.put( "ACCT_ID", acctId );
				becifMap.put( "INIT_SIDE", initSide );

				BizUtil.addAutoBecif( context, tranConnection, becifMap );
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
	}
}