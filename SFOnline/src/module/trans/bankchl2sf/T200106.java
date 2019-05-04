package module.trans.bankchl2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.AllyData;
import module.bean.BankSignData;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.communication.SecuClientBase;
import module.dao.AcctJourDao;
import module.dao.AllyDataDao;
import module.dao.BankSignDataDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryBalClient;
import module.trans.sf2bankchl.QryCardLevelClient;
import module.trans.sf2bankchl.QryCardTypeClient;
import module.trans.sf2secu.OpenAcctClient;

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
 * ����Ϊ���ж˷���Ԥָ��
 * tran code :200106
 * @author ex_kjkfb_songshimin
 *
 */
public class T200106 extends TranBase {

	private String capAcct = null;// �ʽ��ʺ�

	private String cardLevel = null;// �������ȼ�

	private String cardType = null;// ������

	private String signFlag = null;// ǩԼ��ʶ

	private String bookNo = null;// ԤԼ��

	private String branchId = null;// ����������
	
	private String openDepId = null;// ���������

	private String initSeqId = null;// ��־��

	private String subTxSeqId = null;// ��ˮ��

	private String tranSeqId = null;// 22λ������ˮ��

	private AllyDataDao allyDataDao = new AllyDataDao();

	private BankSignDataDao bankSignDataDao = new BankSignDataDao();

	private AcctJourDao acctJourDao = new AcctJourDao();
	
	@Override
	protected void initialize( Context context ) throws SFException {
		initSeqId = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);//14λ��ʼ��ˮ��logid
		subTxSeqId = BizUtil.getSubTxSeqId(initSeqId);//16λsubTxSeqId��ˮ��initSeqId+������
		tranSeqId = BizUtil.getChlSeqId(context,subTxSeqId);//��������22λ��ˮ��
	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, "doHost()��ʼ" );
		doHost( context );
		SFLogger.info( context, "doHost()����" );
		
		SFLogger.info( context, "doSecu()��ʼ" );
		doSecu( context );
		SFLogger.info( context, "doSecu()����" );

	}

	@Override
	protected void chkStart( Context context ) throws SFException {
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		String initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" );// ���������л�ȡ����
		String invType = SFUtil.getReqDataValue( context,"INV_TYPE");// ���������л�ȡ�ͻ�����
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ֤������
		String secBrchId = SFUtil.getReqDataValue( context, "SEC_BRCH_ID" );// ȯ��Ӫҵ������
		if( SFConst.INIT_SIDE_ABBANK.equals( initSide ) && SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
			SFUtil.chkCond( context, SFUtil.isEmpty( idType ), "ST5712", "��֤�����Ͳ���������ҵ��" );
			SFUtil.chkCond( context, "21".equals( idType ), "ST5712", "��������21֤������" );
		}
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ��ȡȯ�̶���
		SFUtil.chkCond( context, secCompData == null, "ST5705", "ȯ�̴������" );
		SFUtil.chkCond( context, !"1".equals( secCompData.getBankPreSignFlag() ), "ST4371", "��ȯ��δ��ͨ����Ԥָ������" );
		// ��ȯ�̱�����ȯ��Ӫҵ������
		SFUtil.chkCond( context, "1".equals( secCompData.getSecBrchIdMode() ) && SFUtil.isEmpty( secBrchId ), "ST4522", "ȯ��Ӫҵ������SecBrchId������Ϊ��" );

	}

	@Override
	public void doHost( Context context ) throws SFException {

		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" );
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
		String initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ȡ��ȯ�̶���
		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// ���������л�ȡ�ͻ�����
		BankUnit bankUnit = null;
		AllyData allyData = null;
		BankSignData bankSignData = null;
		InvestData investData = null;
		SignAccountData signAccountData = null;
		String chBranch = null;
		try {
			KeyedCollection kColl = null;
			Context msgContext = null;
			String retFlag = null;
			/**************************************************************************
			*                    ��������ѯ��״̬��ʼ
			***************************************************************************/
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "ACCT_ID", acctId );// ����
			msgContext = BizUtil.qryCardAttrClient( context, msg );
			SFUtil.chkCond( context, "0".equals( SFUtil.objectToString( SFUtil.getDataValue( msgContext, "RESULT_FLAG" ) ) ), "ST5102", "�������˺�Ϊ[����������˺�]�����ܿ�ͨ���������ҵ��" );
			/**************************************************************************
			*                    ��������ѯ��״̬����
			***************************************************************************/

			/**************************************************************************
			 *                    ��������ѯ�����ʺſ�ʼ
			 * ************************************************************************/
			SFLogger.info( context, "��������ѯ�����ʺſ�ʼ" );

			msg = new HashMap<String, Object>();
			msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
			msg.put( "CUR_CODE", curCode );// ����CCY
			msg.put( "INV_NAME", invName );// �˻�����ACCT_NAME
			QryBalClient qryBalClient = new QryBalClient();
			msgContext = qryBalClient.send( context, msg );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5602", "��������ȡ[�����˺�]ʧ��" );

			IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
			kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// ��ȡ��Ӧ����
			String account = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// �����ʺ� CustNo�ο��ӿ��ֶζ���ֵ
			SFLogger.info( context, String.format( "ȡ�������ʺ�Account:[%s]", account ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( account ), "ST5602", "��������ȡ[�����˺�]ʧ��" );

			String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// ȡ�����˻�����
			SFLogger.info( context, String.format( "ȡ�����˻�����pbcAcctType:[%s]", pbcAcctType ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5602", "��������ȡ[�����˻�����]ʧ��" );

			if( "2".equals( pbcAcctType ) ) {// �����˻�
				SFLogger.info( context, String.format( "��ȯ���ѽ�������˻�У�飬SecCompCode:[%s]", secCompCode ) );
				// û�鵽���������˻�ǩԼ��¼�����ر���
				SFUtil.chkCond( context, !"1".equals( secCompData.getIIAcctFlag() ), "ST5421", String.format( "��ȯ�̲���������[�����˻�ǩԼ]" ) );
				SFLogger.info( context, String.format( "��ȯ�̶����˻�У��ͨ��" ) );
			}

			//String chOpenNode = SFUtil.getDataValue( msgContext, "APP_HEAD.BRANCH_ID" );// ���������
			String chOpenNode = branchId = SFUtil.getDataValue( msgContext, kColl, "OPEN_DEP_ID" );// ���������
			openDepId = chOpenNode;
			chBranch = chOpenNode;
			SFLogger.info( context, String.format( "ȡ�����������[%s]", openDepId ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( openDepId ), "ST5602", "��������ȡ[�˺ſ�������]ʧ��" );

			bankUnit = BankUnitCache.getValue(openDepId);// ���ݿ�������Ż�ȡ�����������
			SFUtil.chkCond( context, bankUnit == null, "ST5801", "��ѯ[�����������������]ʧ��" );
			branchId = bankUnit.getBranchId();// �������к�
			String capAcct = null;
			allyData = allyDataDao.qryAllyData( context, tranConnection, acctId );
			if( allyData == null ) {
				cardType = "1";
			}else{
				cardType = "2";
			}
			if( SFConst.INIT_SIDE_ABBANK.equals( initSide ) && SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
				if("2".equals(cardType) && allyData!=null){
						capAcct = allyData.getCapAcct();
						SFUtil.chkCond( context, "1".equals( allyData.getUseFlag() ), "ST5124", "����������ԤԼ��ͨ��������ܣ��뾡�쵽֤ȯ��˾����" );
						SFUtil.chkCond( context, !secCompCode.equals( allyData.getSecCompCode() ), "ST5705", "ȯ�̴������" );
						SFUtil.chkCond( context, "2".equals( secCompData.getSecCapAcctMode() ) && !chBranch.equals( allyData.getBranchId() ), "ST5761", "�뵽ָ�����а�������ԤԼ���������ҵ��" );
						SFUtil.chkCond( context, "3".equals( secCompData.getSecCapAcctMode() ) && !openDepId.equals( allyData.getDepId() ), "ST5761", "�뵽ָ�������������ԤԼ���������ҵ��" );	
					}
					
				
				/* ���TRDSignAccountData�����ÿ��Ż��ȯ�̺��ʽ��˺���ǩԼ��¼������������Ԥָ�� */
				if( "2".equals( cardType ) ) {
					signAccountData = signAccountDataDao.qrySignAccountDataCountBySignFlag( context, tranConnection, capAcct, secCompCode );
					SFUtil.chkCond( context, signAccountData != null, "ST5502", "�ÿ�����ʽ��˺���ǩԼ������������Ԥָ������" );
				}
				bankSignData = bankSignDataDao.qryBankSignDataBySignFlag( context, tranConnection, acctId, secCompCode );
				SFUtil.chkCond( context, bankSignData != null, "ST5521", "�ÿ����Ѿ�����������Ԥָ���������ظ�����" );
			}
			SFLogger.info( context, "��������ѯ�����ʺŽ���" );
			/**************************************************************************
			 *                    ��������ѯ�����ʺŽ���
			 * ************************************************************************/

			/**************************************************************************
			 *                    �Ͽ����ж��Ƿ�����������ʼ
			 * ************************************************************************/
			String lmCard = null;
			if( acctId.startsWith( "621626" ) || acctId.startsWith( "623058" ) ) {// ƽ����
				SFLogger.info( context, "�Ͽ����ж��Ƿ�����������ʼ" );
				msg = new HashMap<String, Object>();
				msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
				QryCardTypeClient qryCardTypeClient = new QryCardTypeClient();
				msgContext = qryCardTypeClient.send( context, msg );
				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST4377", "�ÿͻ�������" );
				IndexedCollection cardMsgiColl = SFUtil.getDataElement( msgContext, "MSG_O.CARD_MSG_ARRAY" );
				SFUtil.chkCond( context, ( null == cardMsgiColl || cardMsgiColl.size() <= 0 ), "ST4403", String.format( "��������ѯ��Ƭ���ͳ���" ) );

				// ƥ����֤�������Ŀ����ͣ�һ�����Ͼ������˳�,020,200,300,400�ֱ���֤�տ��ĵȼ���094��095��096��097Ϊ��֤IC��
				for( int i = 0; i < cardMsgiColl.size(); i++ ) {
					String tmpCardType = SFUtil.getDataValue( msgContext, ( KeyedCollection )cardMsgiColl.getElementAt( i ), "CARD_TYPE" );
					if( "040".equals( tmpCardType ) || "200".equals( tmpCardType ) || "300".equals( tmpCardType ) || "400".equals( tmpCardType ) || "094".equals( tmpCardType ) || "095".equals( tmpCardType ) || "096".equals( tmpCardType ) || "097".equals( tmpCardType ) ) {
						lmCard = "1";
						break;
					}
				}
				if( "1".equals( lmCard ) ) {
					SFLogger.info( context, String.format( "�˿�cardno=[%s]Ϊ��������", acctId ) );
					// �жϸ��������Ƿ���ҪǩԼƽ��֤ȯ�����������ֱ���˳�����
					if( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) {
						SFUtil.chkCond( context, "ST5111", "��֤����������ǩԼ��ƽ��֤ȯ" );
					}
				}
				SFLogger.info( context, "�Ͽ����ж��Ƿ�������������" );
			}
			/**********************�Ͽ����ж��Ƿ�������������***************************/

			/**************************************************************************
			*                    ������ǩԼ�Ͽ����жϿ��ȼ���ʼ
			***************************************************************************/
			if( "1".equals( lmCard ) && ( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) ) {
				SFLogger.info( context, "������ǩԼ�Ͽ����жϿ��ȼ���ʼ" );

				msg = new HashMap<String, Object>();
				msg.put( "ACCT_ID", acctId );// �˺�ACCT_NO
				QryCardLevelClient qryCardLevelClient = new QryCardLevelClient();
				msgContext = qryCardLevelClient.send( context, msg );
				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
				String retMsg = SFUtil.getDataValue( msgContext, "MSG_O.RET_MSG" );// ��Ӧ��Ϣ
				// �жϲ�ѯ�Ƿ�ɹ�
				if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
					SFUtil.chkCond( context, "ST4377", String.format( retMsg ) );
				}
				cardLevel = SFUtil.getDataValue( msgContext, "MSG_O.CARD_LEVEL" );// �������ȼ�
				SFLogger.info( context, String.format( "������ǩԼƽ��֤ȯ��Ƭ�ȼ�Ϊcardlv[%s]", cardLevel ) );

				SFLogger.info( context, "������ǩԼ�Ͽ����жϿ��ȼ�����" );
			}
			/**********************������ǩԼ�Ͽ����жϿ��ȼ�����*************************/
			String cardIdTmp = null;
			investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );// �������Ļ�ȡͶ������Ϣ����
			if( investData!=null ) {
				String secAcct = investData.getSecAcct();
				signAccountData = signAccountDataDao.qrySignAccountDataBySecAcct( context, tranConnection, secAcct, secCompCode );
				SFUtil.chkCond( context, signAccountData != null, "ST4378", "���Ѿ������ȯ�̽����˴�ܹ�ϵ����ѡ��'Ԥָ��ȷ��'" );
				signAccountData = signAccountDataDao.qrySignAccountDataBySignFlag( context, tranConnection, secAcct, secCompCode );
				SFUtil.chkCond( context, signAccountData != null, "ST5501", "�����ظ���ͨ��ȯ�̵��������" );
			}
			bankSignData = bankSignDataDao.qryBankSignDataByIdCodeAndIdType( context, tranConnection,  idType, idCode, secCompCode );
			SFUtil.chkCond( context, bankSignData != null, "ST5113", "����ԤԼ����Ч�������ظ�ԤԼ" );
			bankSignData = bankSignDataDao.qryBankSignDataByIdTypeAndInvInvIdCode( context, tranConnection, secCompCode, idType, idCode);
			if( bankSignData != null ) {
				signFlag = bankSignData.getSignFlag();
				cardIdTmp = bankSignData.getAcctId();
				if( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( signFlag ) ) {
					SFUtil.chkCond( context, !acctId.equals( cardIdTmp ), "ST4398", "����Ԥָ��ǩԼ״̬���ڴ�����ʱ��ԭǩԼ����������ǩԼ���Ų���������������ǩԼ��" );
					bookNo = bankSignData.getBookNo();
				}
			}

			// ����ˮ��
			DBHandler.beginTransaction( context, tranConnection );// ��������1
			acctJourDao.saveAcctJour( context, tranConnection, getAcctJour( context ) );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
			if( "1".equals( cardType ) && !SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( signFlag ) ) {
				bookNo = BizUtil.genBookId( context );
			}
			SFLogger.info( context, String.format( "������2Ϊ��������1Ϊ�տ�,��ԤԼ��[%s],������[%s]-����[%s]��Ӧ��ǩԼ״̬[%s]", bookNo, cardType, acctId, signFlag ) );
			if( "2".equals( cardType ) ) {// ����������
				DBHandler.beginTransaction( context, tranConnection );// ��������2
				/* �����ʽ��˺��á����á� */
				allyDataDao.updAllyData( context, tranConnection, secCompCode, capAcct );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����2
			}
			if( "2".equals( cardType ) || ( "1".equals( cardType ) && !"2".equals( secCompData.getBankPreSignMode2() ) ) ) {// ��ʵʱ��ȯ�̵Ĵ���
				if( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( signFlag ) ) {// ɾ��ԭ�еĴ�����״̬�ļ�¼
					DBHandler.beginTransaction( context, tranConnection );// ��������3
					bankSignDataDao.delBankSignDataByBookNo( context, tranConnection, acctId, secCompCode, bookNo);
					DBHandler.commitTransaction( context, tranConnection );// �ύ����3
				}
				// ������Ԥָ����Ϣ��¼TrdBankSignData��
				DBHandler.beginTransaction( context, tranConnection );// ��������4
				bankSignDataDao.saveBankSignData( context, tranConnection, getBankSignData( context ) );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����4

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "doHost()����ʧ��%s", e.getMessage() ) );
		}
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ֤������
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤��ID
		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// ����
		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
		String txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������
		String custMagNo = SFUtil.getReqDataValue( context, "CUS_MAG_NO" );// �ͻ�������
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String remark = SFUtil.getReqDataValue( context, "MEMO" );// ��ע
		String secBrchId = SFUtil.getReqDataValue( context, "SEC_BRCH_ID" );// ȯ��Ӫҵ������
		String channel = SFUtil.getReqDataValue( context, "CHANNEL" );// �������
		String phone = SFUtil.getReqDataValue( context, "PHONE" );// �ֻ�
		String mobile = SFUtil.getReqDataValue( context, "MOBILE" );// �绰
		String emailAddr = SFUtil.getReqDataValue( context, "EMAIL_ADDR" );// Email��ַ
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ����������ȡ��ȯ�̶���
		String sfIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ת����ϵͳ֤������
		BankSignData bankSignData = new BankSignData();
		AcctJour acctJour = new AcctJour();
		try {
			if( "1".equals( cardType ) && "2".equals( secCompData.getBankPreSignMode2() ) ) {// ��ͨ��ʵʱ��ȯ�̴���
				if( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( signFlag ) ) {// ɾ����ؼ�¼
					DBHandler.beginTransaction( context, tranConnection );// ��������1
					SFLogger.info( context, "ɾ��ԤԼ��Ϊ[" + bookNo + "],ǩԼ״̬Ϊ[7����ԤԼ������],����[" + acctId + "]����ؼ�¼" );
					bankSignDataDao.delBankSignDataByBookNo( context, tranConnection, acctId, secCompCode, bookNo);
					DBHandler.commitTransaction( context, tranConnection );// �ύ����1
				}
				DBHandler.beginTransaction( context, tranConnection );// ��������2
				// ������Ԥָ����Ϣ��¼TrdBankSignData��
				bankSignDataDao.saveBankSignData( context, tranConnection, getBankSignData( context ) );
				DBHandler.commitTransaction( context, tranConnection );// �ύ����2

				String sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ȯ������ 1-��֤ͨ 0-ֱ��
				String respMsg = null;
				Context secuContext = null;
				String retFlag = null;
				String secSeqId = null;
				Map<String, Object> secuMap = new HashMap<String, Object>();
				if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ��֤ͨ
					// ����֤ȯӪҵ�������͹̶�ֵ '000'
					if( SFConst.SECU_ZHAOSHANGZQ.equals( secCompCode ) ) {
						secBrchId = "000";
					}
					secuMap.put( "SEC_BRCH_ID", secBrchId );
					secuMap.put( "INV_TYPE", invType );
					secuMap.put( "CAP_ACCT", capAcct );
				} else {// ֱ��
						// �ǹ㷢�����������ֻ���ȯ��Ӫҵ������
					if( !SFConst.SECU_GUANGFAZQ.equals( secCompCode ) && !SFConst.SECU_GUANGFAXY.equals( secCompCode ) ) {
						secuMap.put( "MOBILE", mobile );
						secuMap.put( "PHONE", phone );
						secuMap.put( "SEC_BRCH_ID", secBrchId );
					}
					secuMap.put( "SUB_TX_SEQ_ID", subTxSeqId );
				}
				secuMap.put( "BOOK_NO", bookNo );
				secuMap.put( "ID_TYPE", sfIdType );
				secuMap.put( "INV_NAME", invName );
				secuMap.put( "INV_ID_CODE", invIdCode );
				secuMap.put( "CUR_CODE", "RMB" );
				secuMap.put( "SEC_COMP_CODE", secCompCode );
				secuMap.put( "ACCT_ID", acctId );
				secuMap.put("BIZ_SEQ_NO", subTxSeqId);
				secuMap.put( "BRANCH_ID", branchId );// ���ͻ�����
				secuMap.put( "OPEN_DEP_ID", openDepId );// ���Ϳ�������Dgst
				
				SecuClientBase secuClient = new OpenAcctClient();
				secuContext = secuClient.send( context, secuMap );
				retFlag = SFUtil.getDataValue( secuContext, SFConst.PUBLIC_RET_FLAG );
				/* ������֤ͨ��ֱ��ȡ������Ϣ����Ӧ���� */
				KeyedCollection kColl = SFUtil.getDataElement( secuContext, SFConst.SECU_SZT.equals( sztFlag ) ? "Acmt00201" : "6029_O" );
				if( SFConst.SECU_SZT.equals( sztFlag ) ) {
					KeyedCollection rstKcoll = SFUtil.getDataElement( secuContext, kColl, "Rst" );
					respMsg = SFUtil.getDataValue( secuContext, rstKcoll, "RESP_MSG" );// ������֤ͨ���ش�����Ϣ
					if( SFConst.RET_SUCCESS.equals( retFlag ) ) {
						// ��̩������ˮ�ż���ԤԼ��
						if( SFConst.SECU_LIANHEZQ.equals( secCompCode ) ) {
							KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
							KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
							secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȯ����ˮ��
							bookNo = secSeqId;
						}
					}
				} else {
					respMsg = SFUtil.getDataValue( secuContext, kColl, "RESP_MSG" );// ֱ�����ش�����Ϣ					
					if(  SFConst.RET_SUCCESS.equals( retFlag )) {// �ɹ���ȡ��Ҫ����
						secSeqId = SFUtil.getDataValue( secuContext, kColl, "SEC_SEQ_ID" );
						if( SFUtil.isEmpty( secSeqId ) ) {
							secSeqId = "0";
						}
						// �㷢֤ȯʵʱ�����ʽ��˺�
						if( SFConst.SECU_GUANGFAZQ.equals( secCompCode ) || SFConst.SECU_GUANGFAXY.equals( secCompCode ) ) {
							capAcct = SFUtil.getDataValue( secuContext, kColl, "CAP_ACCT" );
						}
					}
				}
				if(SFConst.RET_OVERTIME.equals( retFlag )){
					SFUtil.chkCond( context,"ST4035", "��ȯ��ͨѶ�쳣" );
				}else if( SFConst.RET_FAILURE.equals( retFlag ) ) {/* ʧ�ܺ�ع����� */
					DBHandler.beginTransaction( context, tranConnection );// ��������3
					bankSignDataDao.delBankSignDataBySignFlag( context, tranConnection, acctId, txDate, subTxSeqId );
					acctJour.setJourFlag( "02" );
					acctJour.setSecSeqId( secSeqId );
					acctJour.setRespMsg( respMsg );
					acctJour.setTxDate( txDate );
					acctJour.setSubTxSeqId( subTxSeqId );
					acctJourDao.saveAcctJour( context, tranConnection, acctJour );
					DBHandler.commitTransaction( context, tranConnection );// �ύ����3
					SFUtil.chkCond( context, "ST4034", String.format( "ȯ�̷���:[%s]", respMsg ) );
				}

				DBHandler.beginTransaction( context, tranConnection );// ��������4
				acctJour.setJourFlag( "00" );
				acctJour.setSecSeqId( secSeqId );
				acctJour.setTxDate( txDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );
				if( "1".equals( cardType ) && "2".equals( secCompData.getBankPreSignMode2() ) ) {
					bankSignData = new BankSignData();
					bankSignData.setAcctId( acctId );
					bankSignData.setTxDate( txDate );
					bankSignData.setSubTxSeqId1( subTxSeqId );
					if( SFConst.SECU_LIANHEZQ.equals( secCompCode ) ) {// ����֤ȯ����������Ϣ
						bankSignData.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE );
						bankSignData.setBookNo( bookNo );
						bankSignDataDao.updBankSignData( context, tranConnection, bankSignData );
					} else if( SFConst.SECU_GUANGFAZQ.equals( secCompCode ) || SFConst.SECU_GUANGFAXY.equals( secCompCode ) ) {// �㷢����������Ϣ
						bankSignData.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE );
						bankSignData.setCapAcct( capAcct );
						bankSignDataDao.updBankSignData( context, tranConnection, bankSignData );
					} else {
						bankSignData.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE );
						bankSignDataDao.updBankSignData( context, tranConnection, bankSignData );
					}
					if( SFUtil.isNotEmpty( cardLevel ) ) {/* ���±�trdbanksigndata�е�lmcard��ֵ */
						bankSignData.setLmCard( cardLevel );
						bankSignDataDao.updBankSignData( context, tranConnection, bankSignData );
					}
					if( SFUtil.isNotEmpty( emailAddr ) ) {
						bankSignData.setEmail( emailAddr );
						bankSignDataDao.updBankSignData( context, tranConnection, bankSignData );
					}
				}
				DBHandler.commitTransaction( context, tranConnection );// �ύ����4
			}

			// �鷵��ȯ�̱���
			SFUtil.setResDataValue( context, "ACCT_ID", acctId );
			SFUtil.setResDataValue( context, "INV_NAME", invName );
			SFUtil.setResDataValue( context, "CUR_CODE", "RMB" );
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "SEC_COMP_NAME", secCompData.getSecCompName() );
			SFUtil.setResDataValue( context, "ID_TYPE", idType );
			SFUtil.setResDataValue( context, "INV_ID_CODE", invIdCode );
			SFUtil.setResDataValue( context, "CUS_MAG_NO", custMagNo );
			SFUtil.setResDataValue( context, "OPEN_DEP_ID", openDepId );
			SFUtil.setResDataValue( context, "CHANNEL", channel );
			SFUtil.setResDataValue( context, "BOOK_SERIAL_NO", bookNo );
			SFUtil.setResDataValue( context, "TX_DATE", txDate );
			SFUtil.setResDataValue( context, "MEMO", remark );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "doSecu()����ʧ��%s", e.getMessage() ) );
		}
	}

	/**
	 * ��ˮ����ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AcctJour getAcctJour( Context context ) throws SFException {
		AcctJour acctJour = new AcctJour();
		acctJour.setTxDate( ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate() );
		acctJour.setUserId( SFUtil.objectToString( SFUtil.getDataValue( context, "APP_HEAD.USER_ID" ) ) );
		acctJour.setInitSide( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INIT_SIDE" ) ) );
		acctJour.setTxSeqId( BizUtil.getTxSeqId(initSeqId));
		acctJour.setSecSeqId( " " );
		acctJour.setSubTxSeqId( subTxSeqId );
		acctJour.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		acctJour.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		acctJour.setIdType( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE ) ) );
		acctJour.setInvIdCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_ID_CODE" ) ) );
		acctJour.setSecAcct( "888888888888888888888" );
		acctJour.setSecAcctSeq( 0 );
		acctJour.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		acctJour.setCapAcct( capAcct );
		acctJour.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		acctJour.setSavAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		acctJour.setOpenDepId( openDepId );
		acctJour.setOpenBranchId( branchId );
		acctJour.setCurCode( "RMB" );
		acctJour.setDcFlag( SFConst.CREDIT_FLAG );
		acctJour.setTxAmount( new BigDecimal(0.00) );
		acctJour.setAcctBal( new BigDecimal(0.00) );
		acctJour.setAbst( " " );
		acctJour.setAbstractStr( "����ԤԼ����" );
		acctJour.setJourFlag( "33" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_BANK_SIGN );
		acctJour.setBusiType( SFConst.BUSI_TYPE_BANK_SIGN );
		acctJour.setTxTime( DateUtil.getMacTime() );
		acctJour.setAbnDealTxTime( "000000" );
		acctJour.setDepId( openDepId );
		acctJour.setBranchId( branchId );
		acctJour.setUnitTellerId( "" );
		acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
		acctJour.setAcctDealId( initSeqId);
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "MEMO" ) ) );
		acctJour.setTranSeqId( initSeqId  );
		acctJour.setBusiSeqId( SFUtil.objectToString( SFUtil.getDataValue( context, "APP_HEAD.BIZ_SEQ_NO" ) ) );
		return acctJour;
	}

	/**
	 * ����ԤԼ����ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private BankSignData getBankSignData( Context context ) throws SFException {
		BankSignData bankSignData = new BankSignData();
		bankSignData.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		bankSignData.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		bankSignData.setSavAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		bankSignData.setAcctChldNum( "00" );
		bankSignData.setCurCode( "RMB" );
		bankSignData.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		bankSignData.setProductType( "03" );
		bankSignData.setCapAcct( capAcct );
		bankSignData.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		bankSignData.setIdType( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE ) ) );
		bankSignData.setInvIdCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_ID_CODE" ) ) );
		bankSignData.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS );
		bankSignData.setCusMagno( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) ) );
		bankSignData.setDepId( openDepId);
		bankSignData.setBranchId( branchId );
		bankSignData.setOpenDepId( openDepId );
		bankSignData.setOpenBranchId( branchId );
		bankSignData.setUnitTellerId( SFUtil.objectToString( SFUtil.getDataValue( context, "APP_HEAD.USER_ID" ) ) );
		bankSignData.setOpenDate( "" );
		bankSignData.setOpenTime( "" );
		bankSignData.setTxDate( ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate() );
		bankSignData.setTxTime( DateUtil.getMacTime() );
		bankSignData.setFlags( "" );
		bankSignData.setMemo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "MEMO" ) ) );
		bankSignData.setDelDate( "" );
		bankSignData.setDelTime( "" );
		bankSignData.setCardType( cardType );
		bankSignData.setBookNo( bookNo );
		bankSignData.setInitSide( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INIT_SIDE" ) ) );
		bankSignData.setSubTxSeqId1(subTxSeqId );
		bankSignData.setSubTxSeqId2( "" );
		bankSignData.setPhone( SFUtil.objectToString( SFUtil.getReqDataValue( context, "PHONE" ) ) );
		bankSignData.setMobile( SFUtil.objectToString( SFUtil.getReqDataValue( context, "MOIBLE" ) ) );
		bankSignData.setSecBrchId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_BRCH_ID" ) ) );
		bankSignData.setChannel( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INIT_SIDE" ) ) );
		return bankSignData;
	}
}