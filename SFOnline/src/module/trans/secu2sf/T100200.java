package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import module.bean.SecTruAcct;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ProductInfoCache;
import module.cache.SecTruAcctCache;
import module.communication.CoBankClientBase;
import module.communication.ESBClientBase;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AgtTranListDao;
import module.dao.ReverseDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.TransferClient;
import module.trans.sf2cobank.T810022Client;
import module.trans.sf2cobank.T810024Client;

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
 * �������ƣ���ȯ�̶˷�����ת֤ 
 * 
 * �������̣�
 *  		1.����ȯ�̽��ף�<��>Trf.001.01/12001��<ֱ>6042/612206��
 *			2.����������Ч��,���ȯ����Ϣ,�ͻ���Ϣ��Ч�Ե�
 *			3.��������612322ת������ƽ̨ �Ǵ�������G1001ת�ˣ��ͻ��ʵ�4031529�����첽��������4031529��ȯ�̻����˻���
 *			4.����ȯ��  ��<��>Trf.002.01/12001��<ֱ>6042/612206��"
 *
 * �����룺100200
 * @author ���ź��
 * 
 */

public class T100200 extends TranBase {

	private AcctJour acctJour = new AcctJour();

	private AcctDetail acctDetail = new AcctDetail();

	private SignAccountData signAccountData = null;

	private AcctJourDao acctJourDao = new AcctJourDao();

	private InvestData investData = null;

	private LocalInfo localInfo = null;

	private ProductInfo productInfo = null;

	private SecTruAcct secTruAcct = null;

	private SecCompData secCompData = null;

	private AgtCustomerInfo agtCustomerInfo = null;

	private String initSeqId = null;

	private String subTxSeqId = null;

	private String chlSeqId = null;

	private String secCompCode = null;

	private String capAcct = null;

	private String secuType = null;

	private String secSeqId = null;

	private String acctId = null;

	private String truAcctId = null;

	private String txDate = null;

	private BigDecimal txAmount = new BigDecimal( 0.00 );

	private DecimalFormat df = new DecimalFormat( "#0.00" );// ���ָ�ʽ����������λС��

	protected void initialize( Context context ) throws SFException {

		initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λǰ����ˮ��
		subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16λ����ƽ̨��ˮ��
		chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // ��ȡ22λ��ˮ��;

		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ); // ȯ�̴���
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Trf00101" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "[ȯ�̴���]������Ϊ��" ) );
			secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "ȯ����Ϣ������" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// ��ȯ�����ͷ�����������
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData );

			// ȯ������
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "[ȯ������]����Ϊ��" ) );

			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );

				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = ( String )SFUtil.getDataValue( context, refKcoll, "Ref" );

				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				acctId = ( String )bkAcctKcoll.getDataValue( "ACCT_ID" );// �����˺�

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				String invName = ( String )custKcoll.getDataValue( "INV_NAME" );// �ͻ�����
				String idType = ( String )custKcoll.getDataValue( "ID_TYPE_SZT" );// ֤������
				String invIdCode = ( String )custKcoll.getDataValue( "INV_ID_CODE" );// ֤������
				String invTypeSZT = ( String )custKcoll.getDataValue( "INV_TYPE_SZT" );// �ͻ�����
				capAcct = ( String )scAcctKcoll.getDataValue( "CAP_ACCT" );// ȯ�̶˿ͻ��ʽ�̨�˺�
				String curCode = ( String )kColl.getDataValue( "CUR_CODE" );// ����
				String txAmount = df.format( new BigDecimal( SFUtil.objectToString( kColl.getDataValue( "TX_AMOUNT" ) ) ) );// ���
				String invType = BizUtil.convSZT2SFInvType( context, invTypeSZT );

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = new KeyedCollection( "100200_I" );
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// ȯ����ˮ��
				SFUtil.addDataField( context, keyColl, "CUR_CODE", curCode );// ����
				SFUtil.addDataField( context, keyColl, "INV_NAME", invName );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", invIdCode );// ֤������
				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// �ͻ�����
				SFUtil.addDataField( context, keyColl, "ACCT_ID", acctId );// ��չ������/�Թ��˺�
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", capAcct );// ȯ�̶��ʽ�̨�˺�
				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", txAmount );// ���
				SFUtil.addDataElement( context, keyColl );

				// �鷵�ر���
				KeyedCollection retKColl = SFUtil.getDataElement( context, "Trf00201" );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, retKColl, "ScAcct" );
				KeyedCollection acctSvcrRetKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, acctSvcrRetKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, retKColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, retKColl, "TX_AMOUNT", txAmount );

				// ��<Rst>���
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, retKColl, "Rst" );

				SFUtil.setDataValue( context, rstKcoll, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setDataValue( context, rstKcoll, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );

			} else {
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );
				acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );
				capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );

				// ��װȯ��ͨ��������
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100200_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

				// �鷵�ر���
				SFUtil.setResDataValue( context, "SEC_ACCT", SFUtil.getReqDataValue( context, "SEC_ACCT" ) );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );
				SFUtil.setResDataValue( context, "ACCT_ID", acctId );
				SFUtil.setResDataValue( context, "CAP_ACCT", SFUtil.getReqDataValue( context, "CAP_ACCT" ) );
				SFUtil.setResDataValue( context, "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );
				SFUtil.setResDataValue( context, "TX_AMOUNT", SFUtil.getReqDataValue( context, "TX_AMOUNT" ) );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", SFConst.CASH_FLAG );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	public void doHandle( Context context ) throws SFException {

		addAcctJour( context ); // ��¼������ˮ��

		doHost( context ); // ���пͻ�

		doSecu( context ); // ��ֵ����ȯ��

	}

	protected void chkStart( Context context ) throws SFException {
		txAmount = new BigDecimal( AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
		// ���׽���С��0
		SFUtil.chkCond( context, SFUtil.object2Double( txAmount ) <= 0, "ST5333", String.format( "���׽���С�ڵ�����" ) );
		AmtUtil.chkMaxAmount( context, SFUtil.object2Double( txAmount ) );// ��齻�׽��
		AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );// �����С��λ��Ч��

		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // �ͻ�����
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // ֤������
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" ); // �ͻ�����
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" ); // ����
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // ת��ǰ�ͻ�֤������
		acctId = SFUtil.getReqDataValue( context, "ACCT_ID" ); // ��չ������/�Թ��˺�

		// ���ؼ��ֶ��Ƿ�Ϊ��
		SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "ȯ�̱�Ų���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "ȯ�̶˿ͻ��ʽ�̨�˺Ų���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "֤�����Ͳ���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "֤�����벻��Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "Ͷ�������Ʋ���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "�ͻ����Ͳ���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "�ұ���Ϊ��" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "ȯ����ˮ�Ų���Ϊ��" ) );
		SFUtil.chkCond( context, ( 10 < secSeqId.length() ), "ST4430", String.format( "ȯ����ˮ�ų��Ȳ��ܳ���10λ" ) );
	}

	protected void chkEnd( Context context ) throws SFException {
		secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
		localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
		txDate = localInfo.getWorkdate();

		SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );
		SFUtil.chkCond( context, ( null == investData ), "ST4392", "Ͷ������Ϣ������" );

		// �������֤ȯRever�ڵ���Ҫ����brchId�����ţ�����ӵ�context��
		SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " " );

		SFUtil.chkCond( context, !SFConst.SIGN_FLAG_SIGN.equals( signAccountData.getSignFlag() ), "ST5591", String.format( "�ͻ���ǰ״̬[%s]���������˽���!", signAccountData.getSignFlag() ) );

		SFUtil.chkCond( context, SFUtil.isEmpty( secCompData.getSecCompCode() ), "ST5705", String.format( "ȯ�̴������" ) );

		// ���ȯ���Ƿ�����Թ��ͻ�����ת֤����
		SFUtil.chkCond( context, ( SFConst.INV_TYPE_CORP.equals( signAccountData.getInvType() ) ) && ( !"1".equals( secCompData.getJGZZFlag1() ) ), "ST4396", String.format( "������ȯ�̷���Թ��ͻ���ת֤����" ) );

		// ����������ʱ��β���������пͻ�������
		SFUtil.chkCond( context, ( !SFConst.WORKMODE_NORMAL.equals( ( String )SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE ) ) ) && ( SFConst.INIT_SIDE_COBANK.equals( signAccountData.getInitSide() ) ), "ST4525", String.format( "����������ʱ��β���������пͻ�������" ) );
	}

	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()��ʼ" );
		SFLogger.info( context, "����������ˮ" );

		String cashRemitFlag = ( SFConst.REMIT_FLAG.equals( ( String )SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ) ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG ); // �����־
		acctJour.setTxDate( txDate );
		acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
		acctJour.setUserId( secCompData.getUserId() );
		acctJour.setTxSeqId( BizUtil.getTxSeqId( secSeqId ) );
		acctJour.setSubTxSeqId( subTxSeqId );
		acctJour.setSecSeqId( secSeqId );
		acctJour.setInvType( signAccountData.getInvType() );
		acctJour.setInvName( signAccountData.getInvName() );
		acctJour.setIdType( investData.getIdType() );
		acctJour.setInvIdCode( investData.getInvIdCode() );
		acctJour.setSecAcct( signAccountData.getSecAcct() );
		acctJour.setSecAcctSeq( signAccountData.getSecAcctSeq() );
		acctJour.setSecCompCode( signAccountData.getSecCompCode() );
		acctJour.setCapAcct( signAccountData.getCapAcct() );
		acctJour.setAcctId( signAccountData.getAcctId() );
		acctJour.setSavAcct( signAccountData.getSavAcct() );
		acctJour.setOpenDepId( signAccountData.getOpenDepId() );
		acctJour.setOpenBranchId( signAccountData.getOpenBranchId() );
		acctJour.setCurCode( ( String )SFUtil.getReqDataValue( context, "CUR_CODE" ) );
		acctJour.setDcFlag( SFConst.CREDIT_FLAG );
		acctJour.setTxAmount( txAmount );
		acctJour.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
		acctJour.setAbst( ( SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "1003" : "2003" ) );
		acctJour.setAbstractStr( "ȯ�̷�����ת֤" );
		acctJour.setJourFlag( "30" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_B2S );
		acctJour.setBusiType( SFConst.BUSI_TYPE_B2S );
		acctJour.setTxTime( DateUtil.getMacTime() );
		acctJour.setDepId( signAccountData.getOpenDepId() );
		acctJour.setBranchId( signAccountData.getBranchId() );
		acctJour.setUnitTellerId( "EB001" );
		acctJour.setCashRemitFlag( cashRemitFlag );
		acctJour.setAcctDealId( initSeqId );
		acctJour.setPreSeqId( initSeqId );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( "ȯ�̷�����ת֤" );
		acctJour.setTranSeqId( chlSeqId );
		acctJour.setBusiSeqId( chlSeqId );

		try {
			DBHandler.beginTransaction( context, tranConnection );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );
		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����������ˮʧ��" );
		}
		SFLogger.info( context, "addAcctJour()����" );
	}

	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );
		try {
			productInfo = ProductInfoCache.getValue( secCompCode );// ����ȯ�̴����ȡȯ�̲�Ʒ�������
			secTruAcct = SecTruAcctCache.getValue( secCompCode );// ����ȯ�̴����ȡȯ���й��˻��������
			BankUnit bankUnitOutCounter = BankUnitCache.getValue( productInfo.getTruOpnDepId() );// ���ݿ�������Ż�ȡ�����������
			BankUnit bankUnitInCounter = BankUnitCache.getValue( signAccountData.getOpenBranchId() );// ���ݿ�������Ż�ȡ�����������
			truAcctId = productInfo.getTruAcctId();

			String retFlag = null;
			String retCode = null;
			String retMsg = null;
			String outDepName = bankUnitOutCounter != null ? bankUnitOutCounter.getDepName() : "";
			String inDepName = bankUnitInCounter != null ? bankUnitInCounter.getDepName() : "";
			if( SFConst.INIT_SIDE_COBANK.equals( signAccountData.getInitSide() ) ) {
				SFUtil.setDataValue( context, SFConst.PUBLIC_INIT_SIDE, SFConst.INIT_SIDE_COBANK );
				retFlag = cobankB2STransfer( context );
				retCode = SFUtil.getDataValue( context, "RESP_CODE" );// ��Ӧ��
				retMsg = SFUtil.getDataValue( context, "RESP_MSG" );// ��Ӧ��Ϣ
			} else {
				SFLogger.info( context, String.format( "����������,�ͻ��ʺ�[%s]->ȯ���ʺ�[%s]", acctId, truAcctId ) );
				// �齻�������
				Map<String, Object> hostMap = new HashMap<String, Object>();
				hostMap.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
				hostMap.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
				hostMap.put( "OUT_ACCT_NO", acctId );// ת���˺�OUT_ACCT_NO
				hostMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );// ת���˻���;OUT_ACCT_USAGE
				// hostMap.put( "OUT_BRANCH_ID", signAccountData.getOpenBranchId() );// ת���к�OUT_BRANCH_ID
				hostMap.put( "OUT_COUNTER_ACCT_NO", truAcctId );// ת���Է����˺�OUT_COUNTER_ACCT_NO
				hostMap.put( "OUT_COUNTER_CLIENT_NAME", secTruAcct.getAcctName() );// ת���Է��пͻ�����OUT_COUNTER_CLIENT_NAME
				hostMap.put( "OUT_COUNTER_BRANCH_NAME", outDepName );// ת���Է�������OUT_COUNTER_BRANCH_NAME
				hostMap.put( "TX_AMOUNT", txAmount );// ���AMT
				hostMap.put( "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );// ����CCY
				hostMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );// ���˵���STATEMENT_NO
				hostMap.put( "IN_ACCT_NO", truAcctId );// ת���˺�IN_ACCT_NO
				hostMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );// ת���˻���;IN_ACCT_USAGE
				hostMap.put( "IN_TOTAL_DETAIL_FLAG", "1" );// ת�������ϸ��־IN_TOTAL_DETAIL_FLAG
				// hostMap.put( "IN_BRANCH_ID", bankUnitOutCounter.getBranchId() );// ת���к�IN_BRANCH_ID
				hostMap.put( "IN_COUNTER_ACCT_NO", acctId );// ת��Է����˺�IN_COUNTER_ACCT_NO
				hostMap.put( "IN_COUNTER_BRANCH_NAME", inDepName );// ת��Է��з�������IN_COUNTER_BRANCH_NAME
				hostMap.put( "TRAN_TYPE", SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "B" : "" );// ��������TRAN_TYPE
				hostMap.put( "TRADER_TYPE_CODE", SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "SFCG" : "" );// �̻����ʹ���TRADER_TYPE_CODE

				ESBClientBase esbClient = new TransferClient();
				Context msgContext = esbClient.send( context, hostMap );// ���ͱ���

				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

				IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
				retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );
				retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ
			}

			if( SFConst.RET_SUCCESS.equals( retFlag ) ) { // �ɹ�
				acctJour.setJourFlag( "00" );
				acctJour.setRespCode( "000000" );
				acctJour.setRespMsg( "������ת�ʳɹ�" );
				SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_SUCCCODE );
				SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, SFConst.RESPCODE_SUCCMSG );
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) { // ʧ��
				acctJour.setJourFlag( "20" );
				acctJour.setRespCode( retCode );
				acctJour.setRespMsg( String.format( "������ת��ʧ��[%s]", retMsg ) );
				SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, "ST4376" );
				SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, retMsg );
			} else if( SFConst.RET_OVERTIME.equals( retFlag ) ) { // ��ʱ/�쳣
				acctJour.setJourFlag( "30" );
				acctJour.setRespCode( "E9999" );
				acctJour.setRespMsg( String.format( "���������쳣[%s]", retMsg ) );
				SFUtil.setDataValue( context, SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_NO );
			}

			DBHandler.beginTransaction( context, tranConnection );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour ); // ���½�����ˮ

			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {
				// ����������ϸ
				addAcctDetail( context );

				// ��ѯTRDSignAccountData������¼
				SignAccountData signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
				SFUtil.chkCond( context, ( null == signAccountData ), "ST5800", "�ͻ�ǩԼ�˺���Ϣ����ʧ��!" );

				// ����ǩԼ��Ϣ��
				signAccountDataDao.updSignAccountData( context, tranConnection, secCompCode, capAcct, txAmount );

			}

			DBHandler.commitTransaction( context, tranConnection );

			SFUtil.chkCond( context, SFConst.RET_FAILURE.equals( retFlag ), "ST4376", retMsg );

		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "ͨ����������ʧ��" ) );
		}

		SFLogger.info( context, "doHost()����" );
	}

	private void addAcctDetail( Context context ) throws SFException {
		SFLogger.info( context, "addAcctDetail()��ʼ" );
		SFLogger.info( context, "����������ϸ" );

		String cashRemitFlag = ( SFConst.REMIT_FLAG.equals( ( String )SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ) ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG ); // �����־

		acctDetail.setTxDate( txDate );
		acctDetail.setInitSide( SFConst.INIT_SIDE_SECU );
		acctDetail.setUserId( secCompData.getUserId() );
		acctDetail.setTxSeqId( BizUtil.getTxSeqId( secSeqId ) );
		acctDetail.setSubTxSeqId( subTxSeqId );
		acctDetail.setSecSeqId( secSeqId );
		acctDetail.setInvType( signAccountData.getInvType() );
		acctDetail.setSecAcct( signAccountData.getSecAcct() );
		acctDetail.setSecAcctSeq( signAccountData.getSecAcctSeq() );
		acctDetail.setAcctId( signAccountData.getAcctId() );
		acctDetail.setOpenDepId( signAccountData.getOpenDepId() );
		acctDetail.setOpenBranchId( signAccountData.getOpenBranchId() );
		acctDetail.setSecCompCode( signAccountData.getSecCompCode() );
		acctDetail.setCapAcct( signAccountData.getCapAcct() );
		acctDetail.setCurCode( ( String )SFUtil.getReqDataValue( context, "CUR_CODE" ) );
		acctDetail.setDcFlag( SFConst.CREDIT_FLAG );
		acctDetail.setTxAmount( txAmount );
		acctDetail.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
		acctDetail.setAbst( ( SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "1003" : "2003" ) );
		acctDetail.setAbstractStr( "ȯ�̷�����ת֤" );
		acctDetail.setJourFlag( "00" );
		acctDetail.setTxCode( SFConst.SF_TX_CODE_SECU_B2S );
		acctDetail.setBusiType( SFConst.BUSI_TYPE_B2S );
		acctDetail.setTxTime( DateUtil.getMacTime() );
		acctDetail.setDepId( signAccountData.getOpenDepId() );
		acctDetail.setBranchId( signAccountData.getBranchId() );
		acctDetail.setUnitTellerId( "EB001" );
		acctDetail.setCashRemitFlag( cashRemitFlag );
		acctDetail.setAcctDealId( initSeqId );
		acctDetail.setPreSeqId( initSeqId );
		acctDetail.setAcctDealDate( DateUtil.getMacDate() );
		acctDetail.setColFlag( "0" );
		acctDetail.setMemo( "ȯ�̷�����ת֤" );
		acctDetail.setTranSeqId( chlSeqId );
		acctDetail.setBusiSeqId( chlSeqId );

		try {
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );
		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "����������ϸʧ��" );
		}

		SFLogger.info( context, "addAcctDetail()����" );
	}

	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {
			if( SFConst.SECU_ZL.equals( secuType ) ) {
				SFUtil.setResDataValue( context, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setResDataValue( context, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "SEC_ACCT", signAccountData.getSecAcct() );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", SFConst.CASH_FLAG );

			} else {
				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00201" );

				// ��<ScAcct>���
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", SFUtil.getReqDataValue( context, "CAP_ACCT" ) );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );

				// ��<Rst>���
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );

				SFUtil.setDataValue( context, rstKcoll, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setDataValue( context, rstKcoll, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "doSecu()����" );
	}

	private String cobankB2STransfer( Context context ) throws SFException {
		SFLogger.info( context, "��ת֤�Ϻ����л��ʼ" );
		SFLogger.info( context, "cobankB2STransfer()��ʼ" );

		/**
		 * 1.Ԥ�Ǻ����н�����ˮ
		 */
		AddAgtTranList( context );

		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ��ʺ�
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		String frontLogNo = SFUtil.getDataValue( context, "FRONT_LOG_NO" );// ǰ����ˮ��
		String bankId = agtCustomerInfo.getBankId();// �����к�
		String tranResult = "MA1111";// Ԥ���彻�׽��
		String thirdTime = DateUtil.getMacDateTimeShort();// ��ȡ����yyyyMMddHHmmss
		String warnFlag = agtCustomerInfo.getAgtAgentInfo().getWarnFlag();// �ʽ𾯸��ʶ
		String warnMoney = agtCustomerInfo.getAgtAgentInfo().getWarnMoney();// �ʽ�Ԥ�����

		String retFlag = null;
		Context msgContext = null;
		Map<String, Object> msgMap = null;
		CoBankClientBase coBankClient = null;
		TransferClient transferClient = new TransferClient();
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		ReverseDao reverseDao = new ReverseDao();
		AgtTranList agtTranList = new AgtTranList();
		try {

			/**
			 * 2.�������ʽ�ת
			 */
			msgMap = new HashMap<String, Object>();
			msgMap.put( "ACCT_ID", acctNo );// �����ʺ�account_no
			msgMap.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���SecCode
			msgMap.put( "CAP_ACCT", stkAcct );// ֤ȯ�ʽ�̨�˺�CapAcct
			msgMap.put( "TX_AMOUNT", AmtUtil.conv2CoBankMulAmount( context, txAmount ) );// ��ת���exch_bal
			msgMap.put( "TRADE_TYPE", "0" );// �ʽ����� ��ת֤ ���̶�ֵ 0
			msgMap.put( "BANK_ID", bankId );// ������
			coBankClient = new T810022Client();
			msgContext = coBankClient.send( context, msgMap, bankId );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��ʱ��ʧ��
				String retMsg = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPMSG" );// ���պ����з��ش�����Ϣ
				String retCode = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPCODE" );// ���պ����з��صĴ�����
				if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// �Ϻ������ʽ𻮿ʱ
					// ��ʱ����������
					Reverse reverse = new Reverse();
					reverse.setChannel( "COBANK" );
					reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
					reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
					reverse.setTxDate( DateUtil.getMacDate() );
					reverse.setSubTxSeqId( subTxSeqId );
					reverse.setType( "0" );
					reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
					reverse.setSceneCode( "13" );
					reverse.setReverseSeq( "20" );
					reverse.setReverseNum( 0 );
					reverse.setStatus( "0" );
					reverse.setMemo( "��������ת֤�����г���" );// ��ע���Խ����������
					reverseDao.saveReverse( context, tranConnection, reverse );
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
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", String.format( "ȥ�������ʽ𻮿�׳���[%s]", retMsg ) );

				return retFlag;

			}
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// �ɹ�
				KeyedCollection kColl = SFUtil.getDataElement( msgContext, "810022_O" );
				String agtLogNo = SFUtil.getDataValue( context, kColl, "AGENT_LOG_NO" );// ȡ����������ˮ��
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setAgentLogNo( agtLogNo );
				DBHandler.beginTransaction( context, tranConnection );// ��������3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// ���º�������ˮ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����3
			}

			/**
			 * 3.������ͨ�ü���
			 */
			SFLogger.info( context, "����������ͨ�ü��˿�ʼ" );
			msgMap = new HashMap<String, Object>();
			msgMap.put( "BIZ_SEQ_NO", chlSeqId );// ҵ����ˮ��
			msgMap.put( "CONSUMER_SEQ_NO", chlSeqId );// ������ˮ��
			msgMap.put( "OUT_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "OUT_BRANCH_ID", agtCustomerInfo.getAgtAgentInfo().getBranchCode() );
			msgMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "IN_TOTAL_DETAIL_FLAG", "1" );
			msgMap.put( "OUT_COUNTER_ACCT_NO", truAcctId );
			msgMap.put( "IN_ACCT_NO", truAcctId );
			msgMap.put( "IN_COUNTER_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "TX_AMOUNT", txAmount );
			msgMap.put( "CUR_CODE", SFConst.CUR_CODE_RMB );
			msgMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );
			msgContext = transferClient.send( context, msgMap );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// ��Ӧ��
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// ��Ӧ��Ϣ
			if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// ��ʱ

				SFLogger.info( context, "ͨ���������˽��׳�ʱ" );
				// �����гɹ����������ʱ���ȳ���������
				Reverse reverse = new Reverse();
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "13" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "ȯ����ת֤�����л������" );// ��ע���Խ����������
				reverseDao.saveReverse( context, tranConnection, reverse );// ����������Ϣ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����

				// �����гɹ����������ʱ���ٳ�������
				reverse = new Reverse();
				reverse.setChannel( "HOST" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "14" );
				reverse.setReverseSeq( "10" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "ȯ����ת֤���������˳���" );// ��ע���Խ����������
				reverseDao.saveReverse( context, tranConnection, reverse );// ����������Ϣ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����

				/*
				 * ���½��׽��
				 */
				SFLogger.info( context, "�����гɹ����������ʱ��������������½��׽��" );
				tranResult = "MA0003";
				DBHandler.beginTransaction( context, tranConnection );// ��������1
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����1
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", "���������˳�ʱ" );

				return SFConst.RET_OVERTIME;
			}
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {// ʧ��

				SFLogger.info( context, "ͨ���������˽��׷���ʧ��" );

				// �����гɹ�����������ʧ�ܣ�����������
				Reverse reverse = new Reverse();
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "13" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "ȯ����ת֤�����л������" );// ��ע���Խ����������
				reverseDao.saveReverse( context, tranConnection, reverse );// ����������Ϣ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����

				/*
				 * ���½��׽��
				 */
				SFLogger.info( context, "�����гɹ�����������ʧ�ܣ����������к���½��׽��" );
				tranResult = "ST4895";
				String accountDate = SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" );// ȡϵͳ�������
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				if( SFUtil.isNotEmpty( accountDate ) ) {
					agtTranList.setHostDate( accountDate );
				}
				DBHandler.beginTransaction( context, tranConnection );// ��������3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����3
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", String.format( "����������ʧ��[%s]", retMsg ) );
				return SFConst.RET_FAILURE;
			}
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// �ɹ�
				SFLogger.info( context, "ͨ�����������������ɹ���Ĵ���" );

				/*
				 * ���½��׽��
				 */
				SFLogger.info( context, "�����гɹ�����������ɹ������½��׽��" );
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

			}

			/**
			 * 4.��ת֤Ԥ��
			 */
			double bankBalance = 0.0;
			double warnBalance = 0.0;
			SFLogger.info( context, "��ת֤Ԥ��" );
			String bankBal = SFUtil.getDataValue( msgContext, "MSG_O.AVAIL_BALANCE" );// ȡ���������
			if( SFUtil.isNotEmpty( bankBal ) ) {
				bankBalance = Double.parseDouble( bankBal );
			}
			if( SFUtil.isNotEmpty( warnMoney ) ) {
				warnBalance = Double.parseDouble( warnMoney );
			}
			String sumLim = "000000000000000";
			String useLim = "000000000000000";
			String limBal = "000000000000000";
			if( ( "1".equals( warnFlag ) && bankBalance < warnBalance ) || ( "2".equals( warnFlag ) && Integer.parseInt( limBal ) < warnBalance ) ) {
				SFLogger.info( context, "�Ϻ������ʽ�Ԥ��-��ʼ" );
				// ����������
				msgMap = new HashMap<String, Object>();
				msgMap.put( "BANK_ACCT", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );// �������˺�BankAcct
				msgMap.put( "ACCT_BAL", AmtUtil.conv2CoBankMulAmount( context, bankBal ) );// �˺����BankBal,��Ҫ����100
				msgMap.put( "TOTAL_LIMIT", sumLim );// �ܶ��SumLim
				msgMap.put( "USED_LIMIT", useLim );// ���ö��UseLim
				msgMap.put( "AVAIL_LIMIT", limBal );// ���ö��LimBal
				msgMap.put( "WARN_FLAG", warnFlag );// Ԥ������WarnFlag
				msgMap.put( "WARN_MONEY", warnMoney );// Ԥ�����WarnMoney
				coBankClient = new T810024Client();
				coBankClient.send( context, msgMap, bankId );
				SFLogger.info( context, "�Ϻ������ʽ�Ԥ��-����" );
			}
			SFUtil.addDataField( context, "RESP_CODE", "ST0000" );
			SFUtil.addDataField( context, "RESP_MSG", "��ת֤�Ϻ����л���ɹ�" );
			SFLogger.info( context, "��ת֤�Ϻ����л������" );
			SFLogger.info( context, "cobankB2STransfer()����" );

		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.addDataField( context, "RESP_CODE", "ST4895" );
			SFUtil.addDataField( context, "RESP_MSG", "��ת֤�Ϻ����л����ʧ��" );
			return SFConst.RET_OVERTIME;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "��ת֤�Ϻ����л����ʧ��" ) );
		}
		return SFConst.RET_SUCCESS;
	}

	/**
	 * Ԥ�Ǻ����н�����ˮ
	 * 
	 * @param
	 * @throws SFException
	 */
	private void AddAgtTranList( Context context ) throws SFException {
		String tranAmount = AmtUtil.conv2CoBankMulAmount( context, txAmount );// ת�˽��
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ��ʺ�
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����ʺ�
		String compAcct = truAcctId;// �����ʺ�
		String frontLogNo = BizUtil.getInitSeqId( context );// ������һ����ˮ
		SFUtil.addDataField( context, "FRONT_LOG_NO", frontLogNo );// �Ѻ����ɵ���ˮ������������,��ΪfrontLogNo������
		String voidFlag = "0";
		String hostCheck = "0";
		String tranResult = "MA1111";
		String tranDate = DateUtil.getMacDate();// ��ȡ���� yyyyMMdd�������������û�������
		String thirdTime = DateUtil.getMacDateTimeShort();// ��ȡ����yyyyMMddHHmmss
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		AgtTranList agtTranList = new AgtTranList();

		try {
			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoAndAgentInfo( context, tranConnection, secCompCode, stkAcct, acctNo );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST4377", "�ÿͻ�������" );

			DBHandler.beginTransaction( context, tranConnection );// ��������1
			agtTranList.setTranFunc( "812322" );
			agtTranList.setBusinessType( "MS999" );
			agtTranList.setTranType( "0" );
			agtTranList.setPicsLogNo( initSeqId );
			agtTranList.setFrontLogNo( frontLogNo );
			agtTranList.setAcctNo( acctNo );
			agtTranList.setStkCode( secCompCode );
			agtTranList.setStkAcct( stkAcct );
			agtTranList.setCompAcct( compAcct );
			agtTranList.setCcyCode( "RMB" );
			agtTranList.setTranAmount( BizUtil.getCobankTranAmount( context, tranAmount ) );
			agtTranList.setVoidFlag( voidFlag );
			agtTranList.setHostCheck( hostCheck );
			// --add by songshimin date 2018-04-17 10:26
			agtTranList.setInsertTime( thirdTime );
			// Remark ��Ϊ�����ļ�shell�ű���ѯ����insertTime���ʴ��ڴ˲���insertTime��ֵ
			agtTranList.setThirdTime( thirdTime );
			agtTranList.setTradeDate( tranDate );
			agtTranList.setTranResult( tranResult );
			agtTranList.setBankId( agtCustomerInfo.getBankId() );
			agtTranList.setOpenBranch( agtCustomerInfo.getOpenBranch() );
			agtTranList.setBankAcct( agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// Ԥ����������ˮ
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "Ԥ�Ǻ����н�����ˮʧ��" ) );
		}
	}

}
