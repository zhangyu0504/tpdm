package module.trans.cobank2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCustomerInfo;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.communication.SecuClientBase;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.trans.TranBase;
import module.trans.sf2secu.ChgAcctNoClient;

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
 * ����Ϊ�����з����������ʺ�
 * @author ex_kjkfb_songshimin
 * ������:300102
 */
public class T300102 extends TranBase {

	private String idType = null;

	private String idCode = null;

	private String invName = null;

	private String branchId = null;

	private String depId = null;

	private String userId = null;
	
	private String oldSecCompCode = null;
	
	private String txDate = null;
	
	private String logId = null;//14λ����־��
	
	private String subTxSeqId = null;//16λ�Ľ�����ˮ��

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
	private AcctJourDao acctJourDao = new AcctJourDao();
	/**
	 * ��ʼ����������
	 * @param context
	 * @throws SFException
	 */
	public void initialize( Context context ) throws SFException {
		oldSecCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		BizUtil.setZhongXinSecuCompCode( context );
		logId = SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID ) );
		subTxSeqId = BizUtil.getSubTxSeqId( logId );
	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, String.format( "addAcctJour()��ʼ" ) );
		addAcctJour( context );
		SFLogger.info( context, String.format( "addAcctJour()����" ) );

		SFLogger.info( context, String.format( "doSecu()��ʼ" ) );
		doSecu( context );
		SFLogger.info( context, String.format( "doSecu()����" ) );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		String retFlag = null;
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// �ͻ�����
		String secSeqId = null;// ȯ����ˮ��
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ�̨�˺�
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
		String oldAcctId = SFUtil.getReqDataValue( context, "OLD_ACCT_ID" );// �������˺�
		String newAcctId = SFUtil.getReqDataValue( context, "NEW_ACCT_ID" );// �������˺�
		String capAcctPwd = SFUtil.getReqDataValue( context, "CAP_ACCT_PWD" );// ȯ��֤ȯ�ʽ�����
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// �ͻ�����
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// ����
		String sfIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// ת�����ϵͳ֤������
		String tranDate = txDate;
		AgtAgentInfo agtAgentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );//��������ȡ�������ж���
		Context msgContext = null;
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			String sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// ��ȡȯ������
			String secAcct = ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct();// ��Ͷ������Ϣ����ȡ��secAcct
			String brchId = SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " ";// ǩԼ���ı�ע�ֶ�Ϊ�����ţ���Ϊ�ո�ֵ���ַ���
			  //ת�����ʽ�����
	   	    String secAcctPwd = BizUtil.convCobankEncryptPwd(context,secCompCode,agtAgentInfo, capAcctPwd);
			// ����ȫ����ȯ��map���ϣ�����֤ͨ��ֱ�������������map��
			Map<String, Object> map = new HashMap<String, Object>();
			BizUtil.setSecBrchId( secCompCode, map, brchId );
			if(SFConst.SECU_SZT.equals( sztFlag )){
				map.put( "BIZ_SEQ_NO",  subTxSeqId);
			}else{//ֱ����8λ��ˮ��
				map.put( "BIZ_SEQ_NO",  BizUtil.getTxSeqId( logId ));
			}
			map.put( "INV_TYPE", invType );
			map.put( "INV_NAME", invName );
			map.put( "ID_TYPE", sfIdType );
			map.put( "INV_ID_CODE", invIdCode );
			map.put( "OLD_ACCT_ID", oldAcctId );
			map.put( "NEW_ACCT_ID", newAcctId );
			map.put( "DEP_NAME", SFConst.SYS_BANK_CNAME );
			map.put( "SEC_ACCT", secAcct );
			map.put( "SEC_COMP_CODE", secCompCode );
			map.put( "CAP_ACCT", capAcct );
			map.put( "CAP_ACCT_PWD", secAcctPwd );
			map.put( "CUR_CODE", curCode );
			map.put( "SUB_TX_SEQ_ID", subTxSeqId );
			SecuClientBase secuClient = new ChgAcctNoClient();
			msgContext = secuClient.send( context, map );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );// ��ȡ��ȯ�̷�����
			SFUtil.chkCond( context, SFConst.RET_OVERTIME.equals( retFlag ), "ST4035", "��ȯ��ͨѶ�쳣" );
			String retMsg = null;// ���շ�����Ϣ
			String retCode = null;//���շ��ش�����
			KeyedCollection kColl = SFUtil.getDataElement( msgContext, SFConst.SECU_SZT.equals( sztFlag ) ? "Acmt00801" : "6024_O" );
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ����ȯ����Ӧ����֤ͨģʽ
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				KeyedCollection msgKcoll = SFUtil.getDataElement( msgContext, "MsgHdr" );// ȡ����Ӧ����ͷ
				KeyedCollection rltKcoll = SFUtil.getDataElement( context, msgKcoll, "Ref" );
				retMsg = SFUtil.getDataValue( context, rstKcoll, "RESP_MSG" );//������Ϣ
				retCode = SFUtil.getDataValue( context, rstKcoll,"RESP_CODE" );//������
				
				secSeqId = SFUtil.getDataValue( context, rltKcoll, "Ref" );// ȡ��ȯ����ˮ��
			} else {// ����ȯ����Ӧ,ֱ��ģʽ
				retMsg = SFUtil.getDataValue( context, kColl, "RESP_MSG" );//������Ϣ
				retCode = SFUtil.getDataValue( context, kColl, "RESP_CODE" );//������
				secSeqId = SFUtil.getDataValue( context, kColl, "SEC_SEQ_ID" );
			}
			if(SFConst.RET_OVERTIME.equals( retFlag )){//��ʱ
				//��ʱ������ˮ������Ϣ
				AcctJour acctJour = new AcctJour();
			 	acctJour.setRespCode("ST4035");
	 	       	acctJour.setRespMsg(retMsg);
	 	       	acctJour.setTxDate(txDate);
	 	   		acctJour.setSubTxSeqId(subTxSeqId);
	 	       	acctJourDao.saveAcctJour(context, tranConnection, acctJour);
	 	       	DBHandler.commitTransaction(context, tranConnection);
				SFUtil.chkCond( context,"ST4035", "��ȯ��ͨѶ�쳣" );
			}else if(retFlag.equals( SFConst.RET_FAILURE )){//ʧ��
				//ʧ�ܸ�����ˮ������Ϣ
				AcctJour acctJour = new AcctJour();
			 	acctJour.setRespCode("ST4035");
	 	       	acctJour.setRespMsg(retMsg);
	 	       	acctJour.setTxDate(txDate);
	 	   		acctJour.setSubTxSeqId(subTxSeqId);
	 	       	acctJourDao.saveAcctJour(context, tranConnection, acctJour);
	 	       	DBHandler.commitTransaction(context, tranConnection);
				if("2001".equals( retCode ) || "E1218".equals( retCode )){//������2001����֤ͨ������E1218(ֱ��)  �ʽ��������
					SFUtil.chkCond( context, "ST4110", retMsg);
				}
				
				SFUtil.chkCond( context,"ST4034", retMsg );
			}
			DBHandler.beginTransaction( context, tranConnection );// ��������2
			// �����˻�����������ϸ��
			AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, getAcctAdmDetail( context,secSeqId ) );
			// ����ǩԼ�˻�����
			signAccountData.setAcctId( newAcctId );
			signAccountData.setSavAcct( newAcctId );
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
			// ������ˮ��
			AcctJour acctJour = new AcctJour();
			acctJour.setJourFlag( "00" );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setTxDate( tranDate );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����2
			
			//���¿ͻ���Ϣ
			DBHandler.beginTransaction( context, tranConnection );// ��������3
			agtCustomerInfoDao.updAgtCustomerInfoByBankIdAndAcctNo( context, tranConnection, newAcctId, bankId, secCompCode, capAcct, oldAcctId );// ����Ϊ���ʺ�
			DBHandler.commitTransaction( context, tranConnection );// �ύ����3

			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", oldSecCompCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "INV_NAME", invName );
			SFUtil.setResDataValue( context, "OLD_ACCT_ID", oldAcctId );
			SFUtil.setResDataValue( context, "NEW_ACCT_ID", newAcctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	/**
	 * ����������
	 * @param context
	 * @throws SFException
	 */
	private void addAcctJour( Context context ) throws SFException {
		try {
			DBHandler.beginTransaction( context, tranConnection );// ��������1
			// /*������ˮ*/
			acctJourDao.saveAcctJour( context, tranConnection, getAcctJour( context ) );
			DBHandler.commitTransaction( context, tranConnection );// �ύ����1
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// ��������;
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// ȯ�̴���
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// ֤ȯ�ʽ�̨�˺�
		String oldAccount = SFUtil.getReqDataValue( context, "OLD_ACCT_ID" );// �������˺�
		String newAccount = SFUtil.getReqDataValue( context, "NEW_ACCT_ID" );// �������˺�
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// ֤������
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );
		String sfIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );
		AgtCustomerInfo agtCustomerInfo = null;
		SignAccountData signAccountData = null;
		try {
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoByBankIdAndAcctNo( context, tranConnection, capAcct, secCompCode, bankId, oldAccount );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST4392", String.format( "�ÿͻ�[%s]�����ڸú�����", oldAccount ) );
			signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			if( signAccountData == null ) {// �ͻ�������Ϣ�����ڣ�����capAcct��ѯǩԼ��Ϣ
				int record = 0;
				List<SignAccountData> signList = signAccountDataDao.qrySignAccountDataListBySearchFlag( context, tranConnection, capAcct, "2" );
				// ѭ������ǩԼ��Ϣ���� �� ԭʼ���ݶԱ�
				for( SignAccountData sign : signList ) {
					record++;
					// �Ƚ�ԭcapkAcct��secCompCode,idType,idCode,newAccount ������ͬ����ѭ��
					if( !sign.getCapAcct().equals( capAcct ) && !sign.getSecCompCode().equals( secCompCode ) && !sign.getInvestData().getIdType().equals( sfIdType ) && !sign.getInvestData().getInvIdCode().equals( invIdCode ) && !sign.getAcctId().equals( newAccount ) ) {
						break;
					}
					SFUtil.chkCond( context, record == signList.size(), "ST4895", String.format( "�ÿͻ��ڵ�������ܵ��˺ź���Ҫ���б�������˺Ų�һ��" ) );
				}
			}
			signAccountData = signAccountDataDao.qrySignAcctountDataByAcctId( context, tranConnection, oldAccount, secCompCode, capAcct );
			SFUtil.chkCond( context, signAccountData == null, "ST4114", String.format( "�޴����п���[%s],�ͻ�������Ϣ������", oldAccount ) );
			SFUtil.setDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA, signAccountData );// ����ԭ�����ĵ�ǩԼ��Ϣ����
			SFUtil.chkCond( context, !SFConst.SIGN_FLAG_SIGN.equals( signAccountData.getSignFlag() ), "ST5590", String.format( "�ͻ���ǰ״̬[%s]�����������˽���", signAccountData.getSignFlag() ) );
			BizUtil.chkTransfer( context );// ��鵱���Ƿ�����ת�˽���
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ��ˮ��������ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AcctJour getAcctJour( Context context ) throws SFException {
		AcctJour acctJour = new AcctJour();
		SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
		if( secCompData != null ) {
			userId = secCompData.getUserId();
		}
		if( signAccountData != null ) {
			branchId = signAccountData.getBranchId();
			depId = signAccountData.getDepId();
		}
		if( investData != null ) {
			idType = investData.getIdType();
			idCode = investData.getInvIdCode();
			invName = investData.getInvName();
		}
		acctJour.setTxDate( txDate );
		acctJour.setUserId( userId );
		acctJour.setInitSide( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
		acctJour.setTxSeqId( BizUtil.getTxSeqId(logId) );
		acctJour.setSecSeqId( "" );
		acctJour.setSubTxSeqId(subTxSeqId );
		acctJour.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		acctJour.setInvName( invName );
		acctJour.setIdType( idType );
		acctJour.setInvIdCode( idCode );
		acctJour.setSecAcct( SFUtil.objectToString( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() ) );
		acctJour.setSecAcctSeq( 0 );
		acctJour.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		acctJour.setCapAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		acctJour.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "NEW_ACCT_ID" ) ) );
		acctJour.setSavAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "NEW_ACCT_ID" ) ) );
		acctJour.setDepId( depId );
		acctJour.setOpenBranchId( branchId );
		acctJour.setCurCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUR_CODE" ) ) );
		acctJour.setDcFlag( SFConst.DEBIT_FLAG );
		acctJour.setTxAmount( new BigDecimal(0.00) );
		acctJour.setAcctBal( new BigDecimal(0.00) );
		acctJour.setAbst( " " );
		acctJour.setAbstractStr( "������н����˺�" );
		acctJour.setJourFlag( "33" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_CHANGE_ACC );
		acctJour.setBusiType( SFConst.BUSI_TYPE_CHANGE_ACC );
		acctJour.setTxTime( DateUtil.getMacTime() );
		acctJour.setOpenDepId( depId );
		acctJour.setBranchId( branchId );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );
		acctJour.setUnitTellerId( " " );
		acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
		acctJour.setPreSeqId( logId );
		acctJour.setAcctDealId(logId);//����14λ����־��
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "REMARK" ) ) );
		acctJour.setAcctHostSeqId( " " );
		return acctJour;
	}

	/**
	 * �˻���ϸ��ֵ
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private AcctAdmDetail getAcctAdmDetail( Context context,String secSeqId ) throws SFException {
		AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
		SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
		String signFlag = null;
		if( secCompData != null ) {
			userId = secCompData.getUserId();
		}
		if( signAccountData != null ) {
			branchId = signAccountData.getBranchId();
			depId = signAccountData.getDepId();
			signFlag = signAccountData.getSignFlag();
		}
		if( investData != null ) {
			idType = investData.getIdType();
			idCode = investData.getInvIdCode();
			invName = investData.getInvName();
		}
		acctAdmDetail.setTxDate( txDate );
		acctAdmDetail.setUserId( userId );
		acctAdmDetail.setInitSide( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
		acctAdmDetail.setTxSeqId( BizUtil.getTxSeqId(logId) );
		acctAdmDetail.setSecSeqId(secSeqId);
		acctAdmDetail.setSubTxSeqId(subTxSeqId);
		acctAdmDetail.setInvType( SFUtil.objectToString( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) );
		acctAdmDetail.setInvName( invName );
		acctAdmDetail.setIdType( idType );
		acctAdmDetail.setInvIdCode( idCode );
		acctAdmDetail.setSecAcct( SFUtil.objectToString( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() ) );
		acctAdmDetail.setSecAcctSeq( 0 );
		acctAdmDetail.setSecCompCode( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) ) );
		acctAdmDetail.setCapAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CAP_ACCT" ) ) );
		acctAdmDetail.setAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "NEW_ACCT_ID" ) ) );
		acctAdmDetail.setDepId( depId );
		acctAdmDetail.setOpenDepId( depId );
		acctAdmDetail.setOpenBranchId( branchId );
		acctAdmDetail.setOldAcctId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "OLD_ACCT_ID" ) ) );
		acctAdmDetail.setCurCode( "RMB" );
		acctAdmDetail.setDcFlag( SFConst.CREDIT_FLAG );
		acctAdmDetail.setTxAmount( new BigDecimal(0.00) );
		acctAdmDetail.setBusiType( SFConst.BUSI_TYPE_CHANGE_ACC );
		acctAdmDetail.setAbStract( " " );
		acctAdmDetail.setAbstractStr( "������н����˺�" );
		acctAdmDetail.setJourFlag( "00" );
		acctAdmDetail.setSignFlag( signFlag );
		acctAdmDetail.setnSignFlag( signFlag );
		acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_CHANGE_ACC );
		acctAdmDetail.setTxTime( DateUtil.getMacTime() );
		acctAdmDetail.setBranchId( branchId );
		acctAdmDetail.setUnitTellerId( "" );
		acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
		acctAdmDetail.setAcctDealDate( DateUtil.getMacDate() );
		acctAdmDetail.setAcctDealId(logId);//����14λ����־��
		acctAdmDetail.setPreSeqId( logId );
		acctAdmDetail.setColFlag( "0" );
		acctAdmDetail.setMemo( "" );
		acctAdmDetail.setAcctHostSeqId( "" );
		return acctAdmDetail;
	}
}