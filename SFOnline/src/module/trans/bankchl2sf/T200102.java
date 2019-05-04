package module.trans.bankchl2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AllyData;
import module.bean.AutoBecif;
import module.bean.BankSignData;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AllyDataDao;
import module.dao.AutoBecifDao;
import module.dao.BankSignDataDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryBalClient;
import module.trans.sf2bankchl.QryKeyInvestinfoClient;
import module.trans.sf2bankchl.SetCardStatusWordClient;
import module.trans.sf2secu.ChgAcctNoClient;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (���ж˷�����) ������н����ʺ� ������ : 200102
 * 
 * @author ����
 * 
 */
public class T200102 extends TranBase {

	private AllyData allyData = null;

	private AcctAdmDetail acctAdmDetail = null;

	private InvestData investData = null;

	private AutoBecif autoBecif = null;

	private SignAccountData signAccountData = null;

	private BankSignData bankSignData = null;

	private SecCompData secCompData = null;

	private LocalInfo localInfo = null;

	private AcctJour acctJour = null;

	private AcctJourDao acctJourDao = new AcctJourDao();

	private String curCode = null;

	private String oldAcctId = null;

	private String initSide = null;

	private String invType = null;

	private String idType = null;

	private String invIdCode = null;

	private String capAcct = null;

	private String newAcctId = null;

	private String capAcctPwd = null;

	private String secCompCode = null;

	private String invName = null;

	private String frontLogNo = null;

	private String newFrontLogNo = null;

	private String subTxSeqId = null;

	private String chOpenNode = null;

	private String chOpenBranch = null;

	private String respCode = null;

	private String respMsg = null;

	private String tranDate = null;

	private String tranTime = null;

	private String secAcct = null;

	private String account = null;

	private int secAcctSeq = 0;

	private String orgSignFlag = null;

	private String custMagId = null;

	private String signFlag = null;

	private String chNode = null;

	private String chBranch = null;

	private String brchId = null;

	private String cardlevel = null;

	private String secSeqId = null; // ��ȯ�̷��ص���ˮ��

	private String becifNo = null; // ��ȯ�̷��ص���ˮ��

	@Override
	protected void initialize( Context context ) throws SFException {

		tranTime = DateUtil.getMacTime();// ����ʱ��
		frontLogNo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λ��ʼ��ˮ��
		subTxSeqId = BizUtil.getSubTxSeqId( frontLogNo );// 16λsubTxSeqId��ˮ��initSeqId+������
		newFrontLogNo = BizUtil.getChlSeqId( context, subTxSeqId );// 22λESB��ˮ��
		custMagId = SFUtil.getReqDataValue( context, "CUS_MAG_NO" );// �ͻ�������
		curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
		oldAcctId = SFUtil.getReqDataValue( context, "OLD_ACCT_ID" );// ԭ����
		initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" );// ����
		invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
		if (SFConst.INV_TYPE_CORP.equals(invType)) {// �Թ��ͻ��Ż�ȡ��������
			chNode = SFUtil.getDataValue(context, "APP_HEAD.BRANCH_ID");// ��������
		}

		idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ֤������
		invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤��ID
		capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// �ʽ��ʺ�
		newAcctId = SFUtil.getReqDataValue( context, "NEW_ACCT_ID" );// �¿���
		capAcctPwd = SFUtil.getReqDataValue( context, "CAP_ACCT_PWD" );// ֤ȯ�ʽ�����
		secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ֤ȯ����
		invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// ��ʼ������
		doHost( context );

		// ��ʼ��ȯ��
		doSecu( context );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );
		Context msgContext = null;
		String retFlag = null;
		Map<String, Object> msg = null;

		try {

			idType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ת�����֤������
			tranDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
			investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );// Ͷ������Ϣ
			SFUtil.chkCond( context, null == investData, "ST4392", "Ͷ������Ϣ������" );//

			secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ȯ����Ϣ
			localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// Ӫҵʱ����Ϣ

			String txDate = localInfo.getBankDate();// Ӫҵ����
			secAcct = investData.getSecAcct();// �ͻ��ʽ�����ʺ�
			SFUtil.chkCond( context, SFUtil.isEmpty( secAcct ), "ST4377", "�ͻ���Ϣ������" );

			SFLogger.info( context, "���ǩԼ��ϵ��ʼ" );
			signAccountData = signAccountDataDao.qrySignAccountDataByBranch( context, tranConnection, oldAcctId, curCode, capAcct, secCompCode );
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", "ǩԼ��Ϣ������" );
			chOpenNode = signAccountData.getOpenDepId();// ���������
			String initSideFlag = signAccountData.getInitSide();
			signFlag = signAccountData.getSignFlag();// ǩԼ��Ϣ
			brchId = SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " ";// ǩԼ���ı�ע�ֶ�Ϊ�����ţ���Ϊ�ո�ֵ���ַ���

			SFUtil.chkCond( context, ( SFUtil.isEmpty( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ������" ) );
			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "ǩԼ��Ϣ����" ) );
			SFUtil.chkCond( context, ( !SFConst.SIGN_FLAG_SIGN.equals( signFlag ) ), "ST5590", String.format( "��ǰǩԼ״̬������������ѯ����" ) );
			SFLogger.info( context, "���ǩԼ��ϵ����" );

			SFLogger.info( context, String.format( "ȡ�����������[%s]", chOpenNode ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( chOpenNode ), "ST5602", "��ȡ�˺ſ�������ʧ��" );

			SFUtil.chkCond( context, ( SFConst.INIT_SIDE_COBANK.equals( initSideFlag ) ) && SFUtil.isEmpty( chOpenNode ), "ST5771", "�Ǳ��пͻ��������ӱ�������������" );

			BankUnit bankUnit = BankUnitCache.getValue(chNode);// ���ݿ�������Ż�ȡ�����������
			// ���˻�bankUnit����Ϊ��
			SFUtil.chkCond(context, SFConst.INV_TYPE_CORP.equals(invType)
					&& bankUnit == null, "ST5801", "��ѯ�����������������ʧ��");
			if (bankUnit != null) {
				chBranch = bankUnit.getBranchId();// �������к�
			}
			SFLogger.info( context, "������ˮ��ACCTJOUR��ʼ" );
			acctJour = addAcctJour( context );
			DBHandler.beginTransaction( context, tranConnection );// ��������
			int count = acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			SFUtil.chkCond( context, count <= 0, "ST4895", "������ˮ��ACCTJOURʧ��" );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����
			SFLogger.info( context, "������ˮ��ACCTJOUR����" );

			if( invType.equals( SFConst.INV_TYPE_CORP ) ) {// �Թ�
				BankSignDataDao bankSignDataDao = new BankSignDataDao();
				bankSignData = bankSignDataDao.qryBankSignDataChkNewAcctId( context, tranConnection, newAcctId, curCode );
				SFUtil.chkCond( context, null != bankSignData, "ST5772", "���˺�������Ԥָ�����ţ�����������˺�" );
			}

			SFLogger.info( context, "��鵱���Ƿ�����ת�˽��ף�������������������н����˺ſ�ʼ" );
			context.addDataField( "TRAN_DATE", tranDate );// ���ӽ�������
			BizUtil.chkTransfer( context );
			SFLogger.info( context, "��鵱���Ƿ�����ת�˽��ף�������������������н����˺Ž���" );

			SFLogger.info( context, "����Ƿ�Ϊ��������ʼ" );
			AllyDataDao allyDataDao = new AllyDataDao();
			allyData = allyDataDao.qryAllyData( context, tranConnection, newAcctId );
			if( null != allyData ) {
				SFLogger.info( context, "����������ǩԼ����֤ȯ��鿪ʼ" );

				String tmpSecCompCode = allyData.getSecCompCode();
				String tmpUseFlag = allyData.getUseFlag();

				if( !secCompCode.equals( tmpSecCompCode ) ) // ������ȯ�̴��벻��
				{
					DBHandler.beginTransaction( context, tranConnection );// ��������
					acctJourDao.updAcctJourDataByErr( context, tranConnection, "ST5125", "��������Ӧȯ�̲���", txDate, "6024", subTxSeqId );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����
					SFUtil.chkCond( context, "ST5125", String.format( "ǩԼȯ��[%s],��������Ӧȯ��[%s]", secCompCode, tmpSecCompCode ) );
				} else if( "2".equals( tmpUseFlag ) ) {
					DBHandler.beginTransaction( context, tranConnection );// ��������
					acctJourDao.updAcctJourDataByErr( context, tranConnection, "ST5122", "��֤���������ȼ���", txDate, "6024", subTxSeqId );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����
					SFUtil.chkCond( context, "ST5122", String.format( "��֤���������ȼ���" ) );
				}
				SFLogger.info( context, "����������ǩԼ����֤ȯ������" );
			}
			SFLogger.info( context, "����Ƿ�Ϊ����������" );

			SFLogger.info( context, "��������ѯУ�鿨״̬��ʼ" );
			Map<String, Object> qryCardAttrMsg = new HashMap<String, Object>();
			qryCardAttrMsg.put( "ACCT_ID", newAcctId );
			Context QueryMsgContext = BizUtil.qryCardAttrClient( context, qryCardAttrMsg );

			// ȡ�ͻ�BECIF��
			becifNo = SFUtil.getDataValue( QueryMsgContext, "MSG_O.BECIF_NO" );
			SFUtil.chkCond( QueryMsgContext, SFUtil.isEmpty( becifNo ), "ST5611", "��������ȡ�ͻ���ʧ��" );
			SFLogger.info( QueryMsgContext, String.format( "�ͻ�BECIF��BECIF_NO:[%s]", becifNo ) );
			SFLogger.info( context, "��������ѯУ�鿨״̬����" );

			SFLogger.info( context, "�Ͽ����ж��Ƿ����������Ͳ�ѯ�������ȼ���ʼ" );
			if( oldAcctId.startsWith( "621626" ) || oldAcctId.startsWith( "623058" ) ) {
				msg = new HashMap<String, Object>();
				msg.put( "ACCT_ID", newAcctId );// �˺�ACCT_NO
				msg.put( "SEC_COMP_CODE", secCompCode );// ȯ�̴���
				msgContext = BizUtil.qryCardTypeClient( context, msg );
				cardlevel = SFUtil.getDataValue( msgContext, "CARD_LEVEL" );// �������ȼ�
			}
			SFLogger.info( context, "�Ͽ����ж��Ƿ����������Ͳ�ѯ�������ȼ�����" );

			SFLogger.info( context, "��������ѯ�ͻ���Ϣ��ʼ" );
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) // ����
			{
				msg = new HashMap<String, Object>();
				msg.put( "ACCT_ID", newAcctId );
				QryKeyInvestinfoClient qryNewInvestinfoClient = new QryKeyInvestinfoClient();
				msgContext = qryNewInvestinfoClient.send( context, msg );
				String retNewFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retNewFlag ), "ST5603", "������[��ѯ�¿��ͻ���Ϣ]ʧ��" );

				SFLogger.info( context, String.format( "��˽�ͻ���Ϣ�˶Կ�ʼ" ) );

				// ��֤�¾ɿͻ���Ϣ
				String chHostType = SFUtil.getDataValue( msgContext, "MSG_O.ID_TYPE" );
				String chHostCode = SFUtil.getDataValue( msgContext, "MSG_O.INV_ID_CODE" );
				String chHostName = SFUtil.getDataValue( msgContext, "MSG_O.INV_NAME" );

				SFLogger.info( context, String.format( "��˽�¿��ͻ�����:[%s]", chHostName ) );
				SFLogger.info( context, String.format( "��˽�¿�֤������:[%s]", chHostType ) );
				SFLogger.info( context, String.format( "��˽�¿�֤������:[%s]", chHostCode ) );

				String IdCode_18 = "";
				String InvIdCode18_20 = "";

				if( "1".equals( idType ) && "15".equals( invIdCode.length() ) ) {
					IdCode_18 = BizUtil.converTo18Card19( invIdCode );
					InvIdCode18_20 = BizUtil.converTo18Card20( invIdCode );
				} else {
					IdCode_18 = invIdCode;
				}
				String chHostCode_18 = "";
				String chHostCode_18_20 = "";
				if( "1".equals( chHostType ) && "15".equals( chHostCode.length() ) ) {
					chHostCode_18 = BizUtil.converTo18Card19( chHostCode );
					chHostCode_18_20 = BizUtil.converTo18Card20( chHostCode );
				} else {
					chHostCode_18 = chHostCode;
				}
				//Change By zhangyu 2018-7-4 Begin
				//˵��:�ͻ�������֤�������������ĵ�ֵ���Ƚϣ�����Ҫ�����ݿ��֤��������бȽϣ��������ıȽϼ���
				//SFUtil.chkCond( context, !signAccountData.getInvName().equals( chHostName ), "ST4090", "�ͻ��˻����Ʋ���" );// ȡ�ͻ��˺����ƣ���ȡ�ͻ�����
				//SFUtil.chkCond( context, !investData.getIdType().equals( chHostType ), "ST4090", "�ͻ�֤�����Ͳ���" );
				//SFUtil.chkCond( context, !investData.getInvIdCode().equals( chHostCode ), "ST4090", "�ͻ�֤�����벻��" );
				SFUtil.chkCond( context, !invName.equals( chHostName ), "ST4090", "�ͻ��˻����Ʋ���" );// ȡ�ͻ��˺����ƣ���ȡ�ͻ�����
				SFUtil.chkCond( context, !idType.equals( chHostType ), "ST4090", "�ͻ�֤�����Ͳ���" );
				//Change By zhangyu 2018-7-4 End

				
				SFUtil.chkCond( context, ( !IdCode_18.equals( chHostCode_18 ) ) && ( !IdCode_18.equals( chHostCode_18_20 ) ) && ( !InvIdCode18_20.equals( chHostCode_18_20 ) && ( !chHostCode_18.equals( InvIdCode18_20 ) ) ), "ST4090", "�ͻ�������Ϣ�˶Բ���" );

				SFLogger.info( context, String.format( "��˽�ͻ���Ϣ�˶Խ���" ) );

			} else if( SFConst.INV_TYPE_CORP.equals( invType ) ) { // �Թ�

				// add by songshimin 20180516
				// ˵�� ���Թ�����C3011��Ӧȡ��openDepId ����������
				chOpenNode = SFUtil.getDataValue(QueryMsgContext, "MSG_O.OPEN_DEP_ID");
				BankUnit bankUnitTpdm = BankUnitCache.getValue(chOpenNode);
				if (bankUnitTpdm != null) {
					chOpenBranch = bankUnitTpdm.getBranchId();
				}
				SFLogger.info( context, String.format( "�Թ��ͻ���Ϣ�˶Կ�ʼ" ) );

				String NewchHostName = SFUtil.getDataValue( QueryMsgContext, "MSG_O.INV_NAME" ); // ȡC3011�ӿڷ��ص��˻�����
				String NewchHostType = BizUtil.convHost2SF4IdType( context, SFUtil.getDataValue( QueryMsgContext, "MSG_O.ID_TYPE" ).toString() );// �¿�ת��֤������
				String NewchHostCode = SFUtil.getDataValue( QueryMsgContext, "MSG_O.INV_ID_CODE" ); // ȡC3011�ӿڷ��ص�֤������

				String secuTrustIDType = SFUtil.getDataValue( QueryMsgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_TYPE" );// C3011�ӿڷ���ȯ���й�֤������
				String secuTrust2SFIdType = BizUtil.convHost2SF4IdType( context, secuTrustIDType );// ����֤������ת����֤������
				String secuTrustIDCode = SFUtil.getDataValue( QueryMsgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_NO" );// C3011�ӿڷ���ȯ���й�֤������

				// ͬҵ�ʺ�
				if( "80".equals( NewchHostType ) ) {
					SFLogger.info( context, String.format( "ͬҵ�Թ��ͻ���Ϣ�˶Կ�ʼ" ) );

					msg = new HashMap<String, Object>();
					msg.put( "ACCT_ID", newAcctId );
					QryKeyInvestinfoClient qryNewInvestinfoClient = new QryKeyInvestinfoClient();
					msgContext = qryNewInvestinfoClient.send( context, msg );
					String retNewFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
					SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retNewFlag ), "ST5603", "��������ѯ�Թ��ͻ���Ϣʧ��" );

					if( SFConst.ID_TYPE_COMPANY_YYZZ.equals( investData.getIdType() ) )// �����Ӫҵִ�պ���
					{
						String licenseNo = SFUtil.getDataValue( msgContext, "MSG_O.LICENSE_NO" );
						if( SFUtil.isNotEmpty( licenseNo ) ) {
							NewchHostCode = SFUtil.getDataValue( msgContext, "MSG_O.LICENSE_NO" );
						} else {
							NewchHostCode = SFUtil.getDataValue( msgContext, "MSG_O.CREDIT_ORG_CODE_GLOBAL_ID" );
						}

					} else if( SFConst.ID_TYPE_COMPANY_ZZJGDMZ.equals( investData.getIdType() ) )// �������֯��������
					{
						NewchHostCode = SFUtil.getDataValue( msgContext, "MSG_O.ORG_CODE_GLOBAL_ID" );
					}

					if( SFUtil.isEmpty( NewchHostCode ) ) {
						NewchHostCode = SFUtil.getDataValue( msgContext, "MSG_O.GLOBAL_ID" );
					}

					String hostRetIdType = SFUtil.getDataValue( msgContext, "MSG_O.OTHER_PROVE_FILE_TYPE" );// M8010�ӿڷ���ͬҵ֤������
					hostRetIdType = BizUtil.convHost2SF4IdType( msgContext, hostRetIdType );// ����֤������ת����֤������
					String hostRetIdCode = SFUtil.getDataValue( msgContext, "MSG_O.OTHER_PROVE_FILE_NO" );// M8010�ӿڷ���ͬҵ֤������
					SFUtil.chkCond( context, ( !investData.getIdType().equals( hostRetIdType ) || !investData.getInvIdCode().equals( hostRetIdCode ) ) && ( !investData.getIdType().equals( secuTrust2SFIdType ) || !investData.getInvIdCode().equals( secuTrustIDCode ) ), "ST4090", "֤������" );
					idType = hostRetIdType;

					SFLogger.info( context, String.format( "ͬҵ�Թ��ͻ���Ϣ�˶Խ���" ) );
				} else {
					SFUtil.chkCond( context, ( !investData.getIdType().equals( NewchHostType ) || !investData.getInvIdCode().equals( NewchHostCode ) ) && ( !investData.getIdType().equals( secuTrust2SFIdType ) || !investData.getInvIdCode().equals( secuTrustIDCode ) ), "ST4090", "֤������" );
				}

				SFLogger.info( context, String.format( "�Թ��¿��ͻ�����:[%s]", NewchHostName ) );
				SFLogger.info( context, String.format( "�Թ��¿�֤������:[%s]", NewchHostType ) );
				SFLogger.info( context, String.format( "�Թ��¿�֤������:[%s]", NewchHostCode ) );

				SFLogger.info( context, String.format( "�Թ��ɿ��ͻ�����:[%s]", signAccountData.getInvName() ) );
				SFLogger.info( context, String.format( "�Թ��ɿ�֤������:[%s]", investData.getIdType() ) );
				SFLogger.info( context, String.format( "�Թ��ɿ�֤������:[%s]", investData.getInvIdCode() ) );

				// SFUtil.chkCond(context,!signAccountData.getInvName().equals(NewchHostName), "ST4090", "�ͻ��˻����Ʋ���");//ȡ�ͻ��˺����ƣ���ȡ�ͻ�����
				// SFUtil.chkCond(context,!investData.getIdType().equals(NewchHostType), "ST4090", "�ͻ�֤�����Ͳ���");
				// SFUtil.chkCond(context,!investData.getInvIdCode().equals(NewchHostCode), "ST4090", "�ͻ�֤�����벻��");

				SFLogger.info( context, String.format( "�Թ��ͻ���Ϣ�˶Խ���" ) );
			}

			SFLogger.info( context, "��������ѯ�ͻ���Ϣ����" );

			SFLogger.info( context, "��������ѯ�˻���ʼ" );
			msg = new HashMap<String, Object>();
			msg.put( "ACCT_ID", newAcctId );
			msg.put( "CUR_CODE", curCode );
			msg.put( "INV_NAME", invName );
			QryBalClient qryBalanceClient = new QryBalClient();
			msgContext = qryBalanceClient.send( context, msg ); // ���ͱ���
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			IndexedCollection retColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection retkColl = ( KeyedCollection )retColl.getElementAt( 0 );// ��ȡ��Ӧ����
			String retMsg = SFUtil.getDataValue( msgContext, retkColl, "RET_MSG" );// ��Ӧ��Ϣ

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5604", String.format( "������[��ѯ�˻����]ʧ�ܣ��������أ�[%s]", retMsg ) );

			// ����
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
				SFLogger.info( context, "�����˻�У�鿪ʼ" );
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
				KeyedCollection kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// ��ȡ��Ӧ����
				// ���˻���R3034��Ӧȡ������������
				chOpenNode = SFUtil.getDataValue(context, kColl, "OPEN_DEP_ID");
				BankUnit bankUnitTpdm = BankUnitCache.getValue(chOpenNode);
				if (bankUnitTpdm != null) {
					chOpenBranch = bankUnitTpdm.getBranchId();
				}
				account = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// �����ʺ�
																				// CustNo�ο��ӿ��ֶζ���ֵ
				SFLogger.info( context, String.format( "ȡ�������ʺ�Account:[%s]", account ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( account ), "ST5612", "��������ȡ[�����˺�]ʧ��" );

				String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// ȡ�����˻�����
				SFLogger.info( context, String.format( "ȡ�����˻�����pbcAcctType:[%s]", pbcAcctType ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5613", "��������ȡ[�����˻�����]ʧ��" );

				// �����˻�
				if( "2".equals( pbcAcctType ) ) {
					SFLogger.info( context, String.format( "��ȯ���ѽ�������˻�У�飬SecCompCode:[%s]", secCompCode ) );
					// û�鵽���������˻�ǩԼ��¼�����ر���
					SFUtil.chkCond( context, !"1".equals( secCompData.getIIAcctFlag() ), "ST5421", String.format( "��ȯ�̲���������[�����˻�ǩԼ]" ) );
					SFLogger.info( context, String.format( "��ȯ�̶����˻�У��ͨ��" ) );
				}
				SFLogger.info( context, "�����˻�У�����" );
			}
			SFLogger.info( context, "��������ѯ�˻�������" );

			SFLogger.info( context, "�����������¿�״̬�ֿ�ʼ" );
			BizUtil.setCardStatusWord( context, newAcctId, "A", chOpenNode );// ������ A-����
			SFLogger.info( context, "�����������¿�״̬�ֽ���" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", "doHost()����ʧ��" + e.getMessage() );
		}
		SFLogger.info( context, "doHost()����" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()��ʼ" );
		try {

			frontLogNo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14λ��ʼ��ˮ��
			String signMode = "3".equals( orgSignFlag ) || "6".equals( orgSignFlag ) ? "0" : "1";

			String sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������ 1-��֤ͨ 0-ֱ��
			capAcctPwd = BizUtil.convBankChLEncryptPwd( context, secCompCode, initSide, invType, capAcctPwd );// �ӽ���ȯ���ʽ�����
			orgSignFlag = signAccountData.getSignFlag();// ǩԼ��Ϣ

			/*********************** ����ȯ�̿�ʼ *******************************/
			// ��ȯ�̲���map
			Map<String, Object> msg = new HashMap<String, Object>();
			BizUtil.setSecBrchId( secCompCode, msg, brchId );//���ӡ�������ͻ�����
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ��֤ͨ
				msg.put( "BIZ_SEQ_NO", subTxSeqId );
			} else {
				msg.put( "BIZ_SEQ_NO", BizUtil.getTxSeqId( frontLogNo ) );
			}
			msg.put( "INV_TYPE", invType );
			msg.put( "INV_NAME", invName );
			msg.put( "ID_TYPE", idType );
			msg.put( "INV_ID_CODE", invIdCode );
			msg.put( "OLD_ACCT_ID", oldAcctId );
			msg.put( "NEW_ACCT_ID", newAcctId );
			msg.put( "DEP_NAME", SFConst.SYS_BANK_CNAME );
			msg.put( "SEC_ACCT", secAcct );
			msg.put( "SEC_COMP_CODE", secCompCode );
			msg.put( "CAP_ACCT", capAcct );
			msg.put( "CAP_ACCT_PWD", capAcctPwd );
			msg.put( "CUR_CODE", curCode );
			msg.put( "STH_CODE", "" );
			msg.put( "SIGN_MODE", signMode );
			msg.put( "LEGAL_NAME", "" );
			msg.put( "LEGAL_ID_TYPE", "" );
			msg.put( "TRN_NAME", "" );
			msg.put( "TRN_ID_TYPE", "" );
			msg.put( "TRN_ID_CODE", "" );
			msg.put( "ZIP", "" );
			msg.put( "ADDR", "" );
			msg.put( "EMAIL_ADDR", "" );
			msg.put( "PHONE", "" );
			msg.put( "FAX", "" );
			msg.put( "MOBILE", "" );
			msg.put( "SUB_TX_SEQ_ID", subTxSeqId );

			ChgAcctNoClient secuClient = new ChgAcctNoClient();
			Context secuContext = secuClient.send( context, msg );
			String retFlag = SFUtil.getDataValue( secuContext, SFConst.PUBLIC_RET_FLAG );

			String retMsg = null;
			String retCode = null;
			// ��ȯ�̷��أ�������֤ͨ��ֱ��ȡ����Ҫ����
			KeyedCollection kColl = null;

			// ��֤ͨ��ʽ
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {
				kColl = SFUtil.getDataElement( secuContext, "Acmt00801" );
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				retMsg = SFUtil.getDataValue( context, rstKcoll, "RESP_MSG" );// ��֤ͨ���ش�����Ϣ
				retCode = SFUtil.getDataValue(context, rstKcoll, "RESP_CODE");// ��֤ͨ���ش�����
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��֤ͨ���سɹ�
					KeyedCollection msgKcoll = SFUtil.getDataElement( secuContext, "MsgHdr" );// ȡ����Ӧ����ͷ
					KeyedCollection rltKcoll = SFUtil.getDataElement( secuContext, msgKcoll, "Ref" );
					secSeqId = SFUtil.getDataValue( secuContext, rltKcoll, "Ref" );// ȡ��ȯ����ˮ��
				}

				// ֱ����ʽ
			} else {
				kColl = SFUtil.getDataElement( secuContext, "6024_O" );
				retMsg = SFUtil.getDataValue(context, kColl, "RESP_MSG");// ֱ�����ش�����Ϣ
				retCode = SFUtil.getDataValue(context, kColl, "RESP_CODE");// ֱ�����ش�����
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ֱ�����سɹ�
					secSeqId = SFUtil.getDataValue( secuContext, kColl, "SEC_SEQ_ID" );// ȡ��ȯ����ˮ��
					if( SFUtil.isEmpty( secSeqId ) ) {
						secSeqId = "0";
					}
				}
			}

			// ��ȯ��ʧ��
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {
				SFLogger.info( context, String.format( "����ȯ��ʧ�ܻع� TranDate=[%s] subTxSeqId=[%s]", tranDate, subTxSeqId ) );
				List<SignAccountData> list = signAccountDataDao.qrySignAccountDataListBySignFlag( context, tranConnection, newAcctId );
				if( null == list || list.size() == 0 ) {
					// ��ǩԼ��¼������Ҫȡ��״̬��
					SFLogger.info( context, "����ȯ��ʧ��,����ǩԼ��¼,������ȡ����״̬��ʼ" );
					BizUtil.setCardStatusWord( context, newAcctId, "D", chOpenNode );// ������D-ȡ��
					SFLogger.info( context, "����ȯ��ʧ��,��ǩԼ��¼,������ȡ����״̬����" );
				}
				acctJourDao.updAcctJourDataByErr(context, tranConnection,
						retCode, retMsg, tranDate, "6024", subTxSeqId);
				DBHandler.commitTransaction(context, tranConnection);
				SFUtil.chkCond( context, "ST4034", String.format( "����ȯ��ʧ�ܣ�ȯ�̷���[%s]", retMsg ) );

			} // ��ȯ���쳣
			else if( SFConst.RET_OVERTIME.equals( retFlag ) ) {
				DBHandler.beginTransaction( context, tranConnection );// ��������
				acctJourDao.updAcctJourDataByErr(context, tranConnection,
						retCode, "����ȯ�̳�ʱ�쳣", tranDate, "6024", subTxSeqId);
				DBHandler.commitTransaction( context, tranConnection );// �ύ����
				SFUtil.chkCond( context, "ST4558", String.format( "����ȯ�̳�ʱ�쳣��ȯ�̷���[%s]", retMsg ) );// ���޸Ĵ�����

			} else // �ɹ�
			{
				signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
				secAcctSeq = signAccountData.getSecAcctSeq() + 1;
				SFLogger.info( context, String.format( "����secAcctSeq=[%s]", secAcctSeq ) );
				AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();

				DBHandler.beginTransaction( context, tranConnection );// ��������
				acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, addAcctAdmDetail( context ) );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����

				SFLogger.info( context, "��ȯ�̳ɹ�,������ˮ����ʼ" );
				DBHandler.beginTransaction( context, tranConnection );// ��������
				// ������ˮ��
				AcctJour acctJour = new AcctJour();
				acctJour.setRespCode(retCode);
				acctJour.setRespMsg(retMsg);
				acctJour.setJourFlag( "00" );
				acctJour.setSecSeqId( secSeqId );
				acctJour.setOpenDepId( chOpenNode );
				acctJour.setOpenBranchId( chOpenBranch );
				acctJour.setTxDate( tranDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );
				SFLogger.info( context, "��ȯ�̳ɹ�,������ˮ������" );

				SFLogger.info( context, "��ȯ�̳ɹ�,���¿ͻ���BECIF�ſ�ʼ" );
				// ��ѯ��BECIF��
				investData = investDataDao.qryInvestDataBySecAcct( context, tranConnection, secAcct );
				String OldBecifNo = investData.getBecifNo();

				// ���¿ͻ���BECIF��
				investData.setBecifNo( becifNo );
				investData.setSecAcct( secAcct );
				int count = investDataDao.saveInvestData( context, tranConnection, investData );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����
				SFUtil.chkCond( context, count <= 0, "ST5821", "����[�ͻ�BECIF��]ʧ��" );
				SFLogger.info( context, "��ȯ�̳ɹ�,���¿ͻ���BECIF�Ž���" );

				if( invType.equals( SFConst.INV_TYPE_RETAIL ) ) {// ����

					// ����ǩԼ��Ϣ������
					signAccountData = new SignAccountData();
					signAccountData.setAcctId( newAcctId );
					signAccountData.setSavAcct( account );
					signAccountData.setAcctChldNum( "00" );
					signAccountData.setOpenDepId( chOpenNode );
					signAccountData.setOpenBranchId( chOpenBranch );
					signAccountData.setIiAcctCtl( "0" );
					signAccountData.setSecCompCode( secCompCode );
					signAccountData.setCapAcct( capAcct );

					DBHandler.beginTransaction( context, tranConnection );// ��������
					signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����

					SFLogger.info( context, String.format( "��ʼ������֤��������״̬λ�����п��ȼ�Ϊ[%s]", cardlevel ) );
					if( SFUtil.isNotEmpty( cardlevel ) ) {
						SFLogger.info( context, "�µĽ����ʺ���������" );

						signAccountData.setLmCard( cardlevel );
						signAccountData.setSecCompCode( secCompCode );
						signAccountData.setCapAcct( capAcct );
						signAccountData.setCurCode( curCode );

						DBHandler.beginTransaction( context, tranConnection );// ��������
						signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
						DBHandler.commitTransaction( context, tranConnection );// �ύ����

					} else {
						SFLogger.info( context, "�µĽ����ʺŲ���������" );
						// �µĽ����ʺŲ������������ж�ԭ���Ľ����˺��Ƿ�Ϊ�����������������Ҫ���������ı�־ȥ����
						List<SignAccountData> list = signAccountDataDao.qrySignAccountDataByLmcard( context, tranConnection, secCompCode, capAcct );
						if( null != list && list.size() == 1 ) {
							SFLogger.info( context, "����֤���������Ϊ����֤������" );

							signAccountData = new SignAccountData();
							signAccountData.setLmCard( "" );
							signAccountData.setSecCompCode( secCompCode );
							signAccountData.setCapAcct( capAcct );
							signAccountData.setCurCode( curCode );

							DBHandler.beginTransaction( context, tranConnection );// ��������
							signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
							DBHandler.commitTransaction( context, tranConnection );// �ύ����
						}
					}

					SFLogger.info( context, String.format( "������֤��������״̬����" ) );

					try {
						/*********************** �ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECI���� *******************************/

						SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ȯ����Ϣ
						String tpdmFlag = secCompData.getTpdmFlag();

						String counterID = "";
						int signListCount = 1;
						List<SignAccountData> signList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, newAcctId, secCompCode );

						// ��ѯ���Ŷ�Ӧ�ɹ�ǩԼ��ȯ�̵ĸ��������ֻ��һ�������Ǹո�ǩԼ����һ������Ҫ��Э�鵽BECIF
						if( null != signList && signList.size() > 0 ) {
							signListCount = signList.size();
						}

						// �ж�����֤ͨȯ����������ȯ
						String subType = null;
						if( "1".equals( tpdmFlag ) ) {// ��֤ͨȯ
							subType = "R81";
						} else if( "2".equals( tpdmFlag ) ) {// ������ȯ
							subType = "R83";
						}
						// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����
						String AgreementNo = secCompCode + "-" + subType + "-" + newAcctId;
						AutoBecifDao autoBecifDao = new AutoBecifDao();
						if( signListCount == 1 && !SFUtil.isEmpty( subType ) ) {
							if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) ) {// �ǹ���ĵ����������Ľ��ף�������Ա��ΪEB001
								counterID = "EB001";
								chOpenNode = "9998";
							}
							// ƴ��Э��� ȯ�̴��루8λ��-Э��С�ࣨR81 ΪA�ɡ�R82ΪB�� ��R83Ϊ������ȯ��-����*/
							autoBecif = new AutoBecif();
							autoBecif.setTxDate( tranDate );
							autoBecif.setSubTxSeqId( subTxSeqId );
							autoBecif.setTxTime( tranTime );
							autoBecif.setStatus( "0" );
							autoBecif.setAgreementNo( AgreementNo );
							autoBecif.setBecifNo( becifNo );
							autoBecif.setAgreementType( "R8" );
							autoBecif.setAgreementSubType( subType );
							autoBecif.setAgreementStatus( "1" );
							autoBecif.setProductNo( newAcctId );
							autoBecif.setOpenDate( tranDate );
							autoBecif.setCloseDate( "" );
							autoBecif.setDeptNo( chOpenNode );
							autoBecif.setUserId( counterID );
							autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );

							DBHandler.beginTransaction( context, tranConnection );// ��������
							autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );
							DBHandler.commitTransaction( context, tranConnection );// �ύ����
						}

						List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, oldAcctId, secCompCode );
						if( null == signAccountDataList || signAccountDataList.size() == 0 && !"".equals( OldBecifNo ) && !"".equals( subType ) ) {
							if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) ) {// �ǹ���ĵ����������Ľ��ף�������Ա��ΪEB001
								counterID = "EB001";
								chOpenNode = "9998";
							}
							autoBecif = new AutoBecif();
							autoBecif.setTxDate( tranDate );
							autoBecif.setSubTxSeqId( subTxSeqId );
							autoBecif.setTxTime( tranTime );
							autoBecif.setStatus( "0" );
							autoBecif.setAgreementNo( AgreementNo );
							autoBecif.setBecifNo( becifNo );
							autoBecif.setAgreementType( "R8" );
							autoBecif.setAgreementSubType( subType );
							autoBecif.setAgreementStatus( "2" );
							autoBecif.setProductNo( newAcctId );
							autoBecif.setOpenDate( tranDate );
							autoBecif.setCloseDate( "" );
							autoBecif.setDeptNo( chOpenNode );
							autoBecif.setUserId( counterID );
							autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );

							DBHandler.beginTransaction( context, tranConnection );// ��������
							autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );
							DBHandler.commitTransaction( context, tranConnection ); // �ύ����
						}
						/*********************** �ɹ�ǩԼ�����ݲ��뵽��TRDAUTOBECIF�У�������ѯ��Э�鵽BECI���� *******************************/
					} catch( Exception e ) {// BECIF����ʧ�ܣ���Ӱ�콻��
						SFLogger.error( context, e.getMessage() );
					}
				} else if( invType.equals( SFConst.INV_TYPE_CORP ) ) {// �Թ�
					SFLogger.info( context, "�Թ�-����ǩԼ��Ϣ�����ݿ�ʼ" );
					signAccountData = new SignAccountData();
					signAccountData.setAcctId( newAcctId );
					signAccountData.setSavAcct( "" );
					signAccountData.setAcctChldNum( "00" );
					signAccountData.setOpenDepId( chOpenNode );
					signAccountData.setBranchId(chBranch);
					signAccountData.setDepId( chNode );
					signAccountData.setOpenBranchId( chOpenBranch );
					signAccountData.setSecCompCode( secCompCode );
					signAccountData.setCapAcct( capAcct );

					DBHandler.beginTransaction( context, tranConnection );// ��������
					signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����
					SFLogger.info( context, "�Թ�-����ǩԼ��Ϣ�����ݽ���" );

				}

				SFLogger.info( context, "����ɹ���,������˻���û��ǩԼ��Ϣ������Ҫȡ��״̬�ֿ�ʼ" );
				long DBcount = signAccountDataDao.qrySignAccountDataByOldAcctIdCount( context, tranConnection, oldAcctId, secCompCode, capAcct );
				if( DBcount == 0 ) {
					try {
						// ȡ���ɿ���״̬�֣�����Ҫ��֤�Ƿ�ɹ��������Ͽ��Ѿ�������
						Map<String, Object> msgMap = new HashMap<String, Object>();
						msgMap.put( "BIZ_SEQ_NO", BizUtil.getChlSeqId( context, BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ) );// ������22λ��ˮSYS_HEAD.CONSUMER_SEQ_NO�ֶ�
						msgMap.put( "ACCT_ID", oldAcctId );// �˺�ACCT_NO
						msgMap.put( "FUNCTION_CODE", "D" );// ������ D-ȡ��
						msgMap.put( "BRANCH_ID", chOpenNode );// ���������
						SetCardStatusWordClient setCardStatusWordClient = new SetCardStatusWordClient();// ���ÿ�״̬
						setCardStatusWordClient.send( context, msgMap );
					} catch( Exception e ) {
						SFLogger.error( context, "ȡ���ɿ���״̬��ʧ��,��Ӱ�콻������!" );
					}

				}

				SFLogger.info( context, "����ɹ���,������˻���û��ǩԼ��Ϣ������Ҫȡ��״̬�ֽ���" );
			}

			/*********************** ����ȯ�̽��� *******************************/

			// ���׳ɹ����鷵�ر���
			SFUtil.setResDataValue( context, "OLD_ACCT_ID", oldAcctId );// ԭ����
			SFUtil.setResDataValue( context, "NEW_ACCT_ID", newAcctId );// �¿���
			SFUtil.setResDataValue( context, "REMARK", "����ʺŽ��׳ɹ�" );// ��ע

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", "��ȯ��ʧ��" + e.getMessage() );
		}
		SFLogger.info( context, "doSecu()����" );
	}

	@Override
	protected void chkStart( Context context ) throws SFException {}

	@Override
	protected void chkEnd( Context context ) throws SFException {}

	/**
	 * Ԥ����ˮ
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AcctJour addAcctJour( Context context ) throws SFException {
		acctJour = new AcctJour();
		acctJour.setTxDate( tranDate );
		acctJour.setUserId( SFConst.DEFAULT_USER_ID );
		acctJour.setInitSide( initSide );
		acctJour.setTxSeqId( BizUtil.getTxSeqId( frontLogNo ) );
		acctJour.setSecSeqId( "" );
		acctJour.setSubTxSeqId( subTxSeqId );
		acctJour.setInvType( invType );
		acctJour.setInvName( invName );
		acctJour.setIdType( idType );
		acctJour.setInvIdCode( invIdCode );
		acctJour.setSecAcct( secAcct );
		acctJour.setSecAcctSeq( 0 );
		acctJour.setSecCompCode( secCompCode );
		acctJour.setCapAcct( capAcct );
		acctJour.setAcctId( newAcctId );
		acctJour.setOldAcctId( oldAcctId );
		acctJour.setSavAcct( "" );
		acctJour.setOpenDepId( chOpenNode );
		acctJour.setOpenBranchId( "" );
		acctJour.setCurCode( curCode );
		acctJour.setDcFlag( SFConst.DEBIT_FLAG );
		acctJour.setTxAmount( new BigDecimal( 0.00 ) );
		acctJour.setAcctBal( new BigDecimal( 0.00 ) );
		acctJour.setAbst( "" );
		acctJour.setAbstractStr( "������н����˺�" );
		acctJour.setJourFlag( "33" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_CHANGE_ACC );
		acctJour.setBusiType(SFConst.BUSI_TYPE_CHANGE_ACC);
		acctJour.setTxTime( tranTime );
		acctJour.setDepId(chNode);
		acctJour.setBranchId(chBranch);
		acctJour.setUnitTellerId( ( String )SFUtil.getDataValue( context, "APP_HEAD.USER_ID" ) );
		acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
		acctJour.setAcctDealId( frontLogNo );
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( "" );
		acctJour.setTranSeqId( newFrontLogNo );
		acctJour.setBusiSeqId( ( String )SFUtil.getDataValue( context, "APP_HEAD.BIZ_SEQ_NO" ) );
		acctJour.setRespCode( respCode );
		acctJour.setRespMsg( respMsg );
		return acctJour;
	}

	private AcctAdmDetail addAcctAdmDetail( Context context ) throws SFException {
		acctAdmDetail = new AcctAdmDetail();
		acctAdmDetail.setTxDate( tranDate );
		acctAdmDetail.setInitSide( initSide );
		acctAdmDetail.setUserId( SFConst.DEFAULT_USER_ID );
		acctAdmDetail.setTxSeqId( BizUtil.getTxSeqId( frontLogNo ) );
		acctAdmDetail.setSecSeqId( secSeqId );
		acctAdmDetail.setSubTxSeqId( subTxSeqId );
		acctAdmDetail.setInvType( invType );
		acctAdmDetail.setInvName( invName );
		acctAdmDetail.setIdType( idType );
		acctAdmDetail.setInvIdCode( invIdCode );
		acctAdmDetail.setSecAcct( secAcct );
		acctAdmDetail.setSecAcctSeq( secAcctSeq );
		acctAdmDetail.setSecCompCode( secCompCode );
		acctAdmDetail.setCapAcct( capAcct );
		acctAdmDetail.setAcctId( newAcctId );
		acctAdmDetail.setOpenDepId( chOpenNode );
		acctAdmDetail.setOpenBranchId( chOpenBranch );
		acctAdmDetail.setOldAcctId( oldAcctId );
		acctAdmDetail.setCurCode( curCode );
		acctAdmDetail.setDcFlag( SFConst.CREDIT_FLAG );
		acctAdmDetail.setTxAmount( new BigDecimal( 0.00 ) );
		acctAdmDetail.setAbStract( " " );
		acctAdmDetail.setAbstractStr( "������н����˺�" );
		acctAdmDetail.setJourFlag("00");
		acctAdmDetail.setSignFlag( orgSignFlag );
		acctAdmDetail.setnSignFlag( orgSignFlag );
		acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_CHANGE_ACC );
		acctAdmDetail.setBusiType(SFConst.BUSI_TYPE_CHANGE_ACC);
		acctAdmDetail.setTxTime( tranTime );
		acctAdmDetail.setDepId(chNode);
		acctAdmDetail.setBranchId(chBranch);
		acctAdmDetail.setUnitTellerId( ( String )SFUtil.getDataValue( context, "APP_HEAD.USER_ID" ) );
		acctAdmDetail.setCashRemitFlag( "2" );
		acctAdmDetail.setCusMagNo( custMagId );
		acctAdmDetail.setAcctDealId( frontLogNo );
		acctAdmDetail.setColFlag( "0" );
		acctAdmDetail.setMemo( "" );
		return acctAdmDetail;
	}

}