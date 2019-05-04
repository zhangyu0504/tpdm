package module.trans.cobank2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtAgentBranch;
import module.bean.AgtAgentInfo;
import module.bean.AgtCustomerInfo;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ParamCache;
import module.communication.SecuClientBase;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtAgentBranchDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.DesSignDataDao;
import module.trans.TranBase;
import module.trans.sf2secu.SignContractClient;

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
 * ����Ϊ�����з���ȷ��ǩԼ��ϵ
 * @author ex_kjkfb_songshimin
 * ������:300100
 */
public class T300100 extends TranBase {

	/* �����ڽ����в���䶯��ֵ����chkEnd��ʱ����и�ֵ���� */
	private String depId = null;

	private String branchId = null;

	private String secAcct = null;

	private String interFlag = null;

	private int secAcctSeq;

	private String logId = null;// 14λ����־��

	private String subTxSeqId = null;// 16λ�Ľ�����ˮ��

	private AgtAgentInfo agtAgentInfo = null;

	private String secCompName = null;

	private String txDate = null;

	private AgtCustomerInfo agtCustomerInfo = null;

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();

	private AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();

	private AcctJourDao acctJourDao = new AcctJourDao();

	/**
	 * ��ʼ����������
	 * @param context
	 * @throws SFException
	 */
	public void initialize( Context context ) throws SFException {
		logId = SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID ) );
		subTxSeqId = BizUtil.getSubTxSeqId( logId );
	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, String.format( "addAcctJour()��ʼ" ) );
		String respMsg = addAcctJour( context );
		SFLogger.info( context, String.format( "addAcctJour()����" ) );
		if( !SFConst.RESPCODE_SUCCCODE.equals( respMsg ) ) {
			SFLogger.info( context, String.format( "doSecu()��ʼ" ) );
			doSecu( context );
			SFLogger.info( context, String.format( "doSecu()����" ) );
		}

	}

	@Override
	public void doHost( Context context ) throws SFException {}

	@Override
	public void doSecu( Context context ) throws SFException {
		try {
			Context msgContext = null;
			String capAcctPwd = SFUtil.getReqDataValue( context, "CAP_ACCT_PWD" );// ȯ��֤ȯ�ʽ�����
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
			String signFlag = ( ( SignAccountData )SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA ) ).getSignFlag();// ��ǩԼ�����ȡǩԼ��־
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
			String trnName = SFUtil.getReqDataValue( context, "TRN_NAME" );
			String trnIdType = SFUtil.getReqDataValue( context, "TRN_ID_TYPE" );
			String trnIdCode = SFUtil.getReqDataValue( context, "TRN_ID_CODE" );
			String emailAddr = SFUtil.getReqDataValue( context, "EMAIL_ADDR" );
			String zip = SFUtil.getReqDataValue( context, "ZIP" );
			String address = SFUtil.getReqDataValue( context, "ADDR" );
			String phone = SFUtil.getReqDataValue( context, "PHONE" );
			String mobile = SFUtil.getReqDataValue( context, "MOBILE" );
			String fax = SFUtil.getReqDataValue( context, "FAX" );
			String memo = SFUtil.getReqDataValue( context, "MEMO" );
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����˺�
			String secAcct = ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct();// ���Ͷ������Ϣͨ����ſ�������������ȡ����֤������ʺ�
			String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ�̨�˺�
			String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
			String sfIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );
			String beginBal = "0.00";
			String initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );
			String txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();
			SignAccountData signAccountData = null;
			// ת�����ʽ�����
			String secAcctPwd = BizUtil.convCobankEncryptPwd( context, secCompCode, agtAgentInfo, capAcctPwd );
			String sztFlag = ( ( SecCompData )SFUtil.getDataValue( context, SFConst.PUBLIC_SECU ) ).getSztFlag();// ��ȡ��֤ͨ��ʶ
			// ����ȫ��map���ϣ�����֤ͨ��ֱ�����в�����Ҫ��������map��
			Map<String, Object> map = new HashMap<String, Object>();
			SecuClientBase secuClient = new SignContractClient();
			String signMode = null;
			if( SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signMode = "0";//
			} else {
				signMode = "1";//
			}
			BizUtil.setSecBrchId( secCompCode, map, ( ( SignAccountData )SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA ) ).getFlags() );
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {
				map.put( "BIZ_SEQ_NO", subTxSeqId );
			} else {// ֱ����8λ��ˮ��
				map.put( "BIZ_SEQ_NO", BizUtil.getTxSeqId( logId ) );
			}
			map.put( "SIGN_FLAG", signFlag ); // ��֤ͨ����signFlag�ֱ����11003 11001�ӿ�
			map.put( "INV_TYPE", invType );
			map.put( "INV_NAME", invName );
			map.put( "ID_TYPE", sfIdType );
			map.put( "INV_ID_CODE", invIdCode );
			map.put( "ACCT_ID", acctId );
			map.put( "DEP_NAME", SFConst.SYS_BANK_CNAME );
			map.put( "SEC_ACCT", secAcct );
			map.put( "SEC_COMP_CODE", secCompCode );
			map.put( "CAP_ACCT", capAcct );
			map.put( "CAP_ACCT_PWD", secAcctPwd );
			map.put( "CUR_CODE", curCode );
			map.put( "STH_CODE", "" );// �ɶ�����
			map.put( "SIGN_MODE", signMode );
			map.put( "TRN_NAME", trnName );
			map.put( "TRN_ID_TYPE", trnIdType );
			map.put( "TRN_ID_CODE", trnIdCode );
			map.put( "ZIP", zip );
			map.put( "ADDR", address );
			map.put( "EMAIL_ADDR", emailAddr );
			map.put( "PHONE", phone );
			map.put( "FAX", fax );
			map.put( "MOBILE", mobile );
			map.put( "SUB_TX_SEQ_ID", subTxSeqId );
			msgContext = secuClient.send( context, map );
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			String secSeqId = null;
			String retMsg = null;
			String retCode = null;
			// ��ȯ�̷��أ�������֤ͨ��ֱ��ȡ����Ҫ����
			KeyedCollection kColl = null;
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ��֤ͨ��ʽ
				kColl = SFUtil.getDataElement( msgContext, "Acmt00201" );
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				KeyedCollection msgKcoll = SFUtil.getDataElement( msgContext, "MsgHdr" );// ȡ����Ӧ����ͷ
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgKcoll, "Ref" );
				retCode = SFUtil.getDataValue( context, rstKcoll, "RESP_CODE" );// ��֤ͨ���ش�����
				retMsg = SFUtil.getDataValue( context, rstKcoll, "RESP_MSG" );// ��֤ͨ���ش�����Ϣ
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��֤ͨ���سɹ�
					// ��������
					beginBal = SFUtil.objectToString( SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) );
					secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// ȡ��ȯ����ˮ��
				}
			} else {// ֱ����ʽ
				if( SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
					kColl = SFUtil.getDataElement( msgContext, "6022_O" );
				} else {
					// ��̩֤ȯ����һվʽǩԼʹ��6025������
					if( SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) || SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) ) {
						kColl = SFUtil.getDataElement( msgContext, "100100_O" );
					} else {
						kColl = SFUtil.getDataElement( msgContext, "6022_O" );
					}
				}
				/*
				 * if( SFConst.SECU_GUOTAIJAXY.equals( secCompCode )) {// ��̩֤ȯ���� kColl = SFUtil.getDataElement( msgContext, "100100_O" ); } else { kColl = SFUtil.getDataElement( msgContext, "6022_O" ); }
				 */
				retMsg = SFUtil.getDataValue( context, kColl, "RESP_MSG" );// ֱ�����ش�����Ϣ
				retCode = SFUtil.getDataValue( context, kColl, "RESP_CODE" );// ֱ�����ش�����
				SFLogger.info( context, String.format( "ȯ����Ӧ��:[%s],��Ӧ��Ϣ:[%s]", retCode, retMsg ) );
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ֱ�����سɹ�
					secSeqId = SFUtil.getDataValue( context, kColl, "SEC_SEQ_ID" );// ȡ��ȯ����ˮ��
					if( SFUtil.isEmpty( secSeqId ) ) {
						secSeqId = "0";
					}
					if( SFUtil.isNotEmpty( SFUtil.getDataValue( context, kColl, "BEGIN_BAL" ) ) )
						beginBal = AmtUtil.conv2CoBankDivAmount( context, SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "BEGIN_BAL" ) ) );
				}

			}
			if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// ��ʱ
				// ��ʱ������ˮ������Ϣ
				AcctJour acctJour = new AcctJour();
				acctJour.setRespCode( "ST4035" );
				acctJour.setRespMsg( retMsg );
				acctJour.setTxDate( txDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );
				DBHandler.commitTransaction( context, tranConnection );
				SFUtil.chkCond( context, "ST4035", String.format( "��ȯ��[%s]ͨѶ�쳣", secCompCode ) );
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) {/* ʧ�ܺ�ع����� */
				DBHandler.beginTransaction( context, tranConnection );// ��������6
				acctAdmDetailDao.delAcctAdmDetail( context, tranConnection, txDate, subTxSeqId );
				/* ԭǩԼ״̬ΪδǩԼ��ǩԼ������, ɾ��ǩԼ��¼ */
				if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ) {
					signAccountDataDao.delSignAccountData( context, tranConnection, secCompCode, capAcct );
				} else if( SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {/* ԭǩԼ��¼Ϊ-ȯ��Ԥָ�� */
					signAccountData = new SignAccountData();
					signAccountData.setInitSide( SFConst.INIT_SIDE_SECU );
					signAccountData.setAcctId( " " );
					signAccountData.setOpenDepId( " " );
					signAccountData.setOpenBranchId( " " );
					signAccountData.setSignFlag( SFConst.SIGN_FLAG_SECU_PRE );
					signAccountData.setStatFlag( "0" );
					signAccountData.setSignDate( " " );
					signAccountData.setSecCompCode( secCompCode );
					signAccountData.setCapAcct( capAcct );
					signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
				}
				DBHandler.commitTransaction( context, tranConnection );// �ύ����6

				DBHandler.beginTransaction( context, tranConnection );// ��������7
				investDataDao.delInvestDataBySecAcct( context, tranConnection, secAcct );// ɾ����ǩԼ����û�м�¼��Ͷ������Ϣ
				DBHandler.commitTransaction( context, tranConnection );// �ύ����7
				// ���Ӽ�¼��ˮ�������������Ϣ
				AcctJour acctJour = new AcctJour();
				acctJour.setRespCode( retCode );
				acctJour.setRespMsg( retMsg );
				acctJour.setTxDate( txDate );
				acctJour.setSubTxSeqId( subTxSeqId );
				acctJourDao.saveAcctJour( context, tranConnection, acctJour );
				DBHandler.commitTransaction( context, tranConnection );
				if( "2001".equals( retCode ) || "E1218".equals( retCode ) ) {// ������2001����֤ͨ������E1218(ֱ��) �ʽ��������
					SFUtil.chkCond( context, "ST4110", retMsg );
				}
				SFUtil.chkCond( context, "ST4398", retMsg );

			}
			DBHandler.beginTransaction( context, tranConnection );// ��������8
			// ������ˮ��ʶ
			AcctJour acctJour = new AcctJour();
			acctJour.setJourFlag( "00" );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );

			// �����˻�������ϸ
			AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setTxAmount( SFUtil.isNotEmpty( beginBal ) ? new BigDecimal( beginBal ) : new BigDecimal( "0.00" ) );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SIGN );
			acctAdmDetail.setTxDate( txDate );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );

			// ����ǩԼ��Ϣ
			signAccountData = new SignAccountData();
			signAccountData.setInitSide( initSide );
			signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
			signAccountData.setUnitTellerId( "99999" );
			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setCapAcct( capAcct );
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����8

			// ���¿ͻ�״̬
			DBHandler.beginTransaction( context, tranConnection );// ��������9
			AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
			agtCustomerInfo.setStatus( "0" );
			agtCustomerInfo.setStkAcct( capAcct );
			agtCustomerInfo.setStkCode( secCompCode );
			agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, agtCustomerInfo );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����9
			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "ACCT_ID", acctId );
			SFUtil.setResDataValue( context, "INV_NAME", invName );
			SFUtil.setResDataValue( context, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE" ) );
			SFUtil.setResDataValue( context, "INV_ID_CODE", invIdCode );
			SFUtil.setResDataValue( context, "SEC_COMP_NAME", secCompName );
			SFUtil.setResDataValue( context, "CUR_CODE", curCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "REMARK", memo );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������;
		AgtAgentBranch agtAgentBranch = new AgtAgentBranch();
		AgtAgentBranchDao agtAgentBranchDao = new AgtAgentBranchDao();
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// �����к�
		String openBranch = SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" );// ��������
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ�̨�˺�
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ԭ֤������
		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����

		/* ��鿪�������Ƿ����ڸú����� */
		agtAgentBranch = agtAgentBranchDao.qryAgtAgentBranch( context, tranConnection, bankId, openBranch );
		SFUtil.chkCond( context, agtAgentBranch == null, "ST5705", String.format( "[��������[%s]]������", openBranch ) );
		DBHandler.beginTransaction( context, tranConnection );
		String status = null;
		String memo = null;

		/* ��ѯ�ͻ�״̬ */
		agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
		if( agtCustomerInfo != null ) {
			status = agtCustomerInfo.getStatus();
			memo = agtCustomerInfo.getMemo();
			SFUtil.chkCond( context, "0".equals( status ), "ST4393", String.format( "�ͻ���ǩԼ" ) );
		}
		if( SFUtil.isEmpty( status ) || !"0".equals( status ) ) {// �¿ͻ�
			if( SFUtil.isEmpty( status ) ) {
				memo = "N:";
				memo += DateUtil.getMacDateTimeShort();
			} else {// ʧ�ܻ� �������ͻ��ؿ�
				memo += "-R:";
				memo += DateUtil.getMacDateTimeShort();
			}
			agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, getAgtCustomerInfo( context, memo ) );// ����һ���ͻ���Ϣ
		}
		DBHandler.commitTransaction( context, tranConnection );// �ύ����1
		// �ܾ�20��21����֤������
		SFUtil.chkCond( context, "20".equals( idType ) || "21".equals( idType ), "ST5100", String.format( "��֤������[%s]����������ҵ��", idType ) );
		SFUtil.chkCond( context, SFUtil.getDataValue( context, SFConst.PUBLIC_SECU ) == null, "ST5711", String.format( "ȯ��[%s]��Ϣ������", secCompCode ) );
		secCompName = ( ( SecCompData )SFUtil.getDataValue( context, SFConst.PUBLIC_SECU ) ).getSecCompName();
		if( !SFConst.SECU_GUOXINZQ.equals( secCompCode ) ) {// ����֤ȯ���⴦��
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );// ���������л�ȡǩԼ���ݶ���
			SFUtil.chkCond( context, signAccountData == null || SFConst.SIGN_FLAG_CANCEL.equals( signAccountData.getSignFlag() ), "ST4444", String.format( "��֧������һ��ʽǩԼ" ) );
		}
		agtAgentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );
		if( agtAgentInfo != null ) {
			depId = agtAgentInfo.getBranchCode();
		}
		BankUnit bankUnit = BankUnitCache.getValue( depId );// ���ݿ�������Ż�ȡ�����������
		if( bankUnit != null ) {
			branchId = bankUnit.getBranchId();
		}
		if( "15".equals( idType ) || "16".equals( idType ) || "17".equals( idType ) || "18".equals( idType ) ) {// �ж�ԭ֤������Ϊ�⼸��ʱΪ����
			interFlag = SFConst.INTER_FLAG_ABROAD;
		} else {
			interFlag = SFConst.INTER_FLAG_DOMESTIC;
		}
		InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );// �������Ļ�ȡͶ������Ϣ����
		// ���ɱ�֤������ʺ�
		if( investData == null ) {
			secAcct = BizUtil.genSecAcctId( context );
		} else {
			// ������ǩԼ��
			secAcct = investData.getSecAcct();
			long retailAcctNum = Long.valueOf( ParamCache.getValue( "SF_RETAIL", "CAPACCT_NUM" ).getValue() );// ���ۿͻ����ǩԼ��
			long corpAcctNum = Long.valueOf( ParamCache.getValue( "SF_CORP", "CAPACCT_NUM" ).getValue() );// �����ͻ����ǩԼ��
			long secAcctCount = signAccountDataDao.qrySignAccountDataTotalCountBySecAcct( context, tranConnection, secAcct );
			long capAcctCount = signAccountDataDao.qrySignAccountDataTotalCountBySecAcct( context, tranConnection, secAcct, capAcct );
			long sfCapAcctNum = 0;
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// ���ۿͻ�
				/* һ���ͻ����ͬʱ��5���ʽ��˺� 0311 */
				sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_RETAIL", "CAPACCT_NUM" ).getValue() );
				SFUtil.chkCond( context, ( ( capAcctCount == 0 && secAcctCount > sfCapAcctNum ) || ( capAcctCount != 0 && secAcctCount > retailAcctNum ) ), "ST5100", String.format( "���˿ͻ�������ͬʱǩԼ[%s]���ʽ��˺�", sfCapAcctNum ) );
			} else {// �����ͻ�
				sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_CORP", "CAPACCT_NUM" ).getValue() );
				SFUtil.chkCond( context, ( ( capAcctCount == 0 && secAcctCount > sfCapAcctNum ) || ( capAcctCount != 0 && secAcctCount > corpAcctNum ) ), "ST5100", String.format( "�����ͻ�������ͬʱǩԼ[%s]���ʽ��˺�", sfCapAcctNum ) );
			}
		}
	}

	/**
	 * ����������
	 * @param context
	 * @throws SFException
	 */
	private String addAcctJour( Context context ) throws SFException {
		// ���������Ĳ���
		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// �����˺�
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ�̨�˺�
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// ԭ֤������
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
		String remark = SFUtil.getReqDataValue( context, "REMARK" );// ��ע
		AcctJour acctJour = null;
		SignAccountData signAccountData = null;
		try {
			String status = null;
			String memo = null;
			if( agtCustomerInfo != null ) {
				status = agtCustomerInfo.getStatus();
				memo = agtCustomerInfo.getMemo();
			}
			if( SFUtil.isEmpty( status ) || !"0".equals( status ) ) {// �¿ͻ�
				if( SFUtil.isEmpty( status ) ) {
					memo = "N:";
					memo += DateUtil.getMacDateTimeShort();
				} else {// ʧ�ܻ� �������ͻ��ؿ�
					memo += "-R:";
					memo += DateUtil.getMacDateTimeShort();
				}
				agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, getAgtCustomerInfo( context, memo ) );// ����һ���ͻ���Ϣ
			}
			// Ͷ���˻�����Ϣ��������£�������������
			investDataDao.saveInvestData( context, tranConnection, getInvestData( context ) );
			DBHandler.commitTransaction( context, tranConnection );
			DBHandler.beginTransaction( context, tranConnection );// ��������2
			/********************APSCreateStkAcc�߼�Begin******************************/
			secAcctSeq = 0;
			String initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );// �������Ļ�ȡ����
			acctJourDao.saveAcctJour( context, tranConnection, getAcctJour( context ) );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����2
			String signFlag = ( ( SignAccountData )SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA ) ).getSignFlag();// ��ǩԼ�����ȡǩԼ��־
			if( SFConst.SIGN_FLAG_SIGN.equals( signFlag ) || SFConst.SIGN_FLAG_CANCEL_PRE_IN_PROCESS.equals( signFlag ) ) {
				DBHandler.beginTransaction( context, tranConnection );// ��������3
				signAccountData = new SignAccountData();
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
				signAccountData.setInitSide( initSide );
				signAccountData.setCapAcct( capAcct );
				signAccountData.setSecCompCode( secCompCode );
				signAccountData.setSecAcct( secAcct );
				signAccountData.setSecAcctSeq( secAcctSeq );
				signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
				DBHandler.commitTransaction( context, tranConnection );// ִ�гɹ����ύ����3
				// �鷵�ر���
				SFUtil.setResDataValue( context, "ACCT_ID", acctId );
				SFUtil.setResDataValue( context, "INV_NAME", invName );
				SFUtil.setResDataValue( context, "ID_TYPE", idType );
				SFUtil.setResDataValue( context, "INV_ID_CODE", invIdCode );
				SFUtil.setResDataValue( context, "SEC_COMP_NAME", secCompName );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "REMARK", remark );
				return SFConst.RESPCODE_SUCCCODE;
			}
			/* ԭ������/ȯ��Ԥָ�� */
			if( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) || SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag ) ) {
				DBHandler.beginTransaction( context, tranConnection );// ��������4
				// ��̩���������쳷����������Ԥָ����ǩԼ
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) ) {

					signAccountData = signAccountDataDao.qrySignAccountDataByCloseDate( context, tranConnection, secCompCode, capAcct, txDate );
					if( signAccountData != null ) {
						acctJour = new AcctJour();
						acctJour.setRespCode( "" );
						acctJour.setRespMsg( "" );
						acctJour.setTxDate( txDate );
						acctJour.setSubTxSeqId( subTxSeqId );
						acctJour.setTxCode( SFConst.SF_TX_CODE_SIGN );
						acctJourDao.saveAcctJour( context, tranConnection, acctJour );// ������ˮ
						DBHandler.commitTransaction( context, tranConnection );// �ύ����4
						SFUtil.chkCond( context, "ST5590", String.format( "�ͻ���ǰ״̬����������ǩԼ" ) );

					}
				}
				SignAccountData signAccountDataTpm = signAccountDataDao.qrySignAccountDataByAcctIdAndAcctBal( context, tranConnection, secCompCode, secCompCode, acctId );
				if( signAccountDataTpm != null ) {
					acctJour = new AcctJour();
					acctJour.setRespCode( "" );
					acctJour.setRespMsg( "" );
					acctJour.setTxDate( txDate );
					acctJour.setSubTxSeqId( subTxSeqId );
					acctJour.setTxCode( SFConst.SF_TX_CODE_SIGN );
					acctJourDao.saveAcctJour( context, tranConnection, acctJour );// ������ˮ
					DBHandler.commitTransaction( context, tranConnection );// �ύ����4
					SFUtil.chkCond( context, "ST5590", String.format( "�ͻ���ǰ״̬����������ǩԼ" ) );
				}
				DesSignDataDao desSignDataDao = new DesSignDataDao();
				/* ȯ��Ԥָ�� */
				if( SFConst.SIGN_FLAG_SECU_PRE.equals( signFlag ) ) {
					signFlag = SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS;
					/* �ſ�һ��ֻ��ǩԼһ������ */
					desSignDataDao.migrateSignAccountData( context, tranConnection, secCompCode, capAcct, acctId );
					signAccountDataDao.delSignAccountByAcctId( context, tranConnection, secCompCode, capAcct, acctId );

					signAccountData = new SignAccountData();
					signAccountData.setInitSide( initSide );
					signAccountData.setAcctId( acctId );
					signAccountData.setSavAcct( acctId );
					signAccountData.setAcctChldNum( "00" );
					signAccountData.setOpenDepId( depId );
					signAccountData.setOpenBranchId( branchId );
					signAccountData.setDepId( depId );
					signAccountData.setBranchId( branchId );
					signAccountData.setSignFlag( signFlag );
					signAccountData.setStatFlag( "1" );
					signAccountData.setCusMagno( "99999" );
					signAccountData.setSignDate( txDate );
					signAccountData.setSecCompCode( secCompCode );
					signAccountData.setCapAcct( capAcct );
					signAccountData.setCurCode( curCode );
					signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
				} else {

					/* ����������Ǩ���������� */
					desSignDataDao.migrateSignAccountData( context, tranConnection, secCompCode, capAcct, acctId );
					/* ɾ��ǩԼ��¼ */
					signAccountDataDao.delSignAccountByAcctId( context, tranConnection, secCompCode, capAcct, acctId );
					/* д��ǩԼ��¼ */
					signAccountDataDao.saveSignAccountData( context, tranConnection, getSignAccountData( context ) );
				}
				DBHandler.commitTransaction( context, tranConnection );// �ύ����4
			}

			DBHandler.beginTransaction( context, tranConnection );// ��������5
			/* д���˻�����������ϸ */
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, getAcctAdmDetail( context ) );

			/* ������ˮ��ʶ */
			acctJour = new AcctJour();
			acctJour.setJourFlag( "03" );
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����5
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return "";

	}

	/**
	 * ��ˮ��������ֵ
	 * @return
	 * @throws SFException
	 */
	private AcctJour getAcctJour( Context context ) throws SFException {
		AcctJour acctJour = new AcctJour();
		acctJour.setTxDate( txDate );
		acctJour.setUserId( "0110900000000000" );// ����ԭC�ڱ������õĹ̶�ֵ
		acctJour.setInitSide( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
		acctJour.setTxSeqId( BizUtil.getTxSeqId( logId ) );
		acctJour.setSecSeqId( "" );
		acctJour.setSubTxSeqId( subTxSeqId );//
		acctJour.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		acctJour.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		acctJour.setIdType( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE ) ) );
		acctJour.setInvIdCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_ID_CODE" ) ) );
		acctJour.setSecAcct( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() );
		acctJour.setSecAcctSeq( 0 );
		acctJour.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		acctJour.setCapAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		acctJour.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		acctJour.setSavAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		acctJour.setOpenDepId( depId );
		acctJour.setOpenBranchId( branchId );
		acctJour.setCurCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUR_CODE" ) ) );
		acctJour.setDcFlag( SFConst.CREDIT_FLAG );
		acctJour.setTxAmount( new BigDecimal( 0.00 ) );
		acctJour.setAcctBal( new BigDecimal( 0.00 ) );
		acctJour.setAbst( " " );
		acctJour.setAbstractStr( "����ȷ��/����ǩԼ" );
		acctJour.setJourFlag( "33" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_SIGN );
		acctJour.setBusiType( SFConst.BUSI_TYPE_SIGN );
		acctJour.setTxTime( DateUtil.getMacTime() );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );
		acctJour.setAbnDealTxTime( "000000" );
		acctJour.setDepId( depId );
		acctJour.setBranchId( branchId );
		acctJour.setUnitTellerId( "99999" );
		acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
		acctJour.setPreSeqId( logId );// ��ˮ�Ÿ��죬��AcctDealId����һ��
		acctJour.setAcctDealId( logId );// ��Ҫ�����˱���һ�£�ȡ��14λ
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "REMARK" ) ) );
		return acctJour;
	}

	/**
	 * �˻���ϸ��ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AcctAdmDetail getAcctAdmDetail( Context context ) throws SFException {
		AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
		acctAdmDetail.setTxDate( txDate );
		acctAdmDetail.setInitSide( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
		acctAdmDetail.setUserId( "0110900000000000" );
		acctAdmDetail.setTxSeqId( BizUtil.getTxSeqId( logId ) );
		acctAdmDetail.setSecSeqId( "" );
		acctAdmDetail.setSubTxSeqId( subTxSeqId );
		acctAdmDetail.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		acctAdmDetail.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		acctAdmDetail.setIdType( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE ) ) );
		acctAdmDetail.setInvIdCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_ID_CODE" ) ) );
		acctAdmDetail.setSecAcct( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() );
		acctAdmDetail.setSecAcctSeq( secAcctSeq );
		acctAdmDetail.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		acctAdmDetail.setCapAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		acctAdmDetail.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		acctAdmDetail.setOpenDepId( depId );
		acctAdmDetail.setOpenBranchId( branchId );
		acctAdmDetail.setOldAcctId( "" );
		acctAdmDetail.setCurCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUR_CODE" ) ) );
		acctAdmDetail.setDcFlag( SFConst.CREDIT_FLAG );
		acctAdmDetail.setTxAmount( new BigDecimal( 0.00 ) );
		acctAdmDetail.setAbStract( " " );
		acctAdmDetail.setAbstractStr( "����ȷ��/����ǩԼ" );
		acctAdmDetail.setSignFlag( ( ( SignAccountData )SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA ) ).getSignFlag() );
		acctAdmDetail.setnSignFlag( ( ( SignAccountData )SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA ) ).getSignFlag() );
		acctAdmDetail.setJourFlag( "03" );
		acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SIGN );
		acctAdmDetail.setBusiType( SFConst.BUSI_TYPE_SIGN );
		acctAdmDetail.setTxTime( DateUtil.getMacTime() );
		acctAdmDetail.setAcctDealDate( DateUtil.getMacDate() );
		acctAdmDetail.setDepId( depId );
		acctAdmDetail.setBranchId( branchId );
		acctAdmDetail.setUnitTellerId("99999");
		acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
		acctAdmDetail.setCusMagNo( "99999" );
		acctAdmDetail.setPreSeqId( logId );
		acctAdmDetail.setAcctDealId( logId );// ��Ҫ�����˱���һ�£�ȡ��14λ
		acctAdmDetail.setColFlag( "0" );
		acctAdmDetail.setMemo( " " );
		return acctAdmDetail;
	}

	/**
	 * �����пͻ���Ϣ������ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AgtCustomerInfo getAgtCustomerInfo( Context context, String memo ) throws SFException {
		AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
		agtCustomerInfo.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		agtCustomerInfo.setAcctNo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		agtCustomerInfo.setStkAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		agtCustomerInfo.setBankId( SFUtil.objectToString( SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" ) ) );
		agtCustomerInfo.setOpenBranch( SFUtil.objectToString( SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) ) );
		agtCustomerInfo.setStkCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		agtCustomerInfo.setCurCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUR_CODE" ) ) );
		agtCustomerInfo.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		agtCustomerInfo.setIdType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ID_TYPE" ) ) );// ��ԭ֤������
		agtCustomerInfo.setInvidCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_ID_CODE" ) ) );
		agtCustomerInfo.setOpenDate( txDate );
		agtCustomerInfo.setStatus( "4" );
		agtCustomerInfo.setMemo( memo );
		return agtCustomerInfo;
	}

	/**
	 * ǩԼ��Ϣ������ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private SignAccountData getSignAccountData( Context context ) throws SFException {
		SignAccountData signAccountData = new SignAccountData();
		String secAcct = ( SFUtil.objectToString( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() ) );
		signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
		if( signAccountData != null ) {
			secAcctSeq = signAccountData.getSecAcctSeq() + 1;
		}
		signAccountData.setInitSide( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
		signAccountData.setSecAcct( secAcct );
		signAccountData.setSecAcctSeq( secAcctSeq );
		signAccountData.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		signAccountData.setProductType( "03" );
		signAccountData.setCapAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		signAccountData.setCurCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUR_CODE" ) ) );
		signAccountData.setCashRemitFlag( SFConst.CASH_FLAG );
		signAccountData.setShsthCode( " " );
		signAccountData.setSzsthCode( " " );
		signAccountData.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		signAccountData.setSavAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ACCT_ID" ) ) );
		signAccountData.setAcctChldNum( "00" );
		signAccountData.setOpenDepId( depId );
		signAccountData.setOpenBranchId( branchId );
		signAccountData.setInvName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_NAME" ) ) );
		signAccountData.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN_IN_PROCESS );
		signAccountData.setDepId( depId );
		signAccountData.setBranchId( branchId );
		signAccountData.setDesDepId( depId );
		signAccountData.setDesBranchId( branchId );
		signAccountData.setUnitTellerId( " " );
		signAccountData.setDesUnitTellerId( " " );
		signAccountData.setCusMagno( "99999" );
		signAccountData.setStatFlag( "1" );
		signAccountData.setOpenDate( txDate );
		signAccountData.setPreTxDate( "19000101" );
		signAccountData.setBeginBal( new BigDecimal( 0.00 ) );
		signAccountData.setAcctBal( new BigDecimal( 0.00 ) );
		signAccountData.setIsMailBill( "0" );
		signAccountData.setMailDate( " " );
		signAccountData.setFlags( "0000000000" );
		signAccountData.setSignDate( txDate );
		return signAccountData;
	}

	/**
	 * ����Ͷ������Ϣ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private InvestData getInvestData( Context context ) throws SFException {
		InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );// �������Ļ�ȡͶ������Ϣ����
		investData.setLegalName( "" );
		investData.setLegalIdCode( "" );
		investData.setTrnName( SFUtil.objectToString( SFUtil.getReqDataValue( context, "TRN_NAME" ) ) );
		investData.setTrnIdType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "TRN_ID_TYPE" ) ) );
		investData.setTrnIdCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "TRN_ID_CODE" ) ) );
		investData.setTrnPhone( "" );
		investData.setTrnMobile( "" );
		investData.setZip( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ZIP" ) ) );
		investData.setAddr( SFUtil.objectToString( SFUtil.getReqDataValue( context, "ADDR" ) ) );
		investData.setPhone( SFUtil.objectToString( SFUtil.getReqDataValue( context, "PHONE" ) ) );
		investData.setMobile( SFUtil.objectToString( SFUtil.getReqDataValue( context, "MOBILE" ) ) );
		investData.setFax( SFUtil.objectToString( SFUtil.getReqDataValue( context, "FAX" ) ) );
		investData.setEmailAddr( SFUtil.objectToString( SFUtil.getReqDataValue( context, "EMAIL_ADDR" ) ) );
		investData.setMemo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "REMARK" ) ) );
		investData.setInterFlag( interFlag );
		return investData;
	}
}