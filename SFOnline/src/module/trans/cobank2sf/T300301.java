package module.trans.cobank2sf;

import java.util.HashMap;
import java.util.Map;

import module.bean.AgtAgentInfo;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.communication.SecuClientBase;
import module.trans.TranBase;
import module.trans.sf2secu.QryBalClient;

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
 * ����Ϊ��ѯ�ʽ�̨�����
 * @author ex_kjkfb_songshimin
 * tran_code:300301
 */
public class T300301 extends TranBase {

	private String logId = null;// 14λ����־��

	private String subTxSeqId = null;// 16λ�Ľ�����ˮ��

	@Override
	protected void initialize( Context context ) throws SFException {
		BizUtil.setZhongXinSecuCompCode( context );
		logId = SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID ) );
		subTxSeqId = BizUtil.getSubTxSeqId( logId );
	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, String.format( "doSecu()��ʼ" ) );
		doSecu( context );
		SFLogger.info( context, String.format( "doSecu()����" ) );
	}

	@Override
	public void doHost( Context context ) throws SFException {}

	@Override
	public void doSecu( Context context ) throws SFException {
		String capAcctPwd = SFUtil.getReqDataValue( context, "CAP_ACCT_PWD" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
		String tranDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();
		try {
			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
			SFUtil.chkCond( context, signAccountData == null, "ST4785", String.format( "ǩԼ��ϵ������" ) );
			// ת�����ʽ�����
			AgtAgentInfo agtAgentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );
			String secAcctPwd = BizUtil.convCobankEncryptPwd( context, secCompCode, agtAgentInfo, capAcctPwd );
			// signAccountData = signAccountDataDao.qrySignAccountDataBySignFlag( context, tranConnection, capAcct, secCompCode );
			String brchId = null;
			if( signAccountData != null ) {
				brchId = signAccountData.getFlags();
			}
			String truBal = AmtUtil.conv2CoBankMulAmount( context, signAccountData.getAcctBal() );
			SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// ����������ȡ��ȯ�̶���
			String sztFlag = SFUtil.isEmpty( secCompData.getSztFlag() ) ? "0" : secCompData.getSztFlag();

			// ��װȥȯ��map���ϣ�����֤ͨ��ֱ����Ҫ��������map��
			Map<String, Object> map = new HashMap<String, Object>();
			BizUtil.setSecBrchId( secCompCode, map, brchId );
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {
				map.put( "BIZ_SEQ_NO", subTxSeqId );
			} else {// ֱ����8λ��ˮ��
				map.put( "BIZ_SEQ_NO", BizUtil.getTxSeqId( logId ) );
			}
			map.put( "SEC_COMP_CODE", secCompCode );
			map.put( "ACCT_ID", signAccountData.getAcctId() );
			map.put( "SEC_ACCT", signAccountData.getSecAcct() );
			map.put( "CAP_ACCT", capAcct );
			map.put( "CAP_ACCT_PWD", secAcctPwd );
			map.put( "CUR_CODE", signAccountData.getCurCode() );
			map.put( "UNIT_TELLER_ID", "" );
			map.put( "SUB_TX_SEQ_ID", subTxSeqId );
			map.put( "INV_NAME", signAccountData.getInvName() );
			SecuClientBase secuClient = new QryBalClient();
			Context msgContext = secuClient.send( context, map );
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			String retMsg = null;
			String acctBal = null;// �������
			String availBal = null;// ��ȡ���
			KeyedCollection kColl = SFUtil.getDataElement( msgContext, SFConst.SECU_SZT.equals( sztFlag ) ? "Acmt01001" : "6052_O" );
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// ��֤ͨ��ȡ��Ӧ����
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );
				retMsg = SFUtil.getDataValue( msgContext, rstKcoll, "RESP_MSG" );// ������֤ͨ���ش�����Ϣ
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ��֤ͨ���سɹ� ȡ���
					IndexedCollection scBaliColl = SFUtil.getDataElement( msgContext, "ScBal" );
					for( int i = 0; i < scBaliColl.size(); i++ ) {
						KeyedCollection bkBalKcoll = ( KeyedCollection )scBaliColl.get( i );
						String type = SFUtil.getDataValue( context, bkBalKcoll, "TYPE" );
						if( "1".equals( type ) ) {
							acctBal = SFUtil.getDataValue( context, bkBalKcoll, "BEGIN_BAL" );
						} else if( "2".equals( type ) ) {
							availBal = SFUtil.getDataValue( context, bkBalKcoll, "BEGIN_BAL" );
						}

					}
					acctBal = AmtUtil.conv2CoBankMulAmount( context, acctBal );// ��֤ͨ����Ԫ��λ�����Ҫ����100
					availBal = AmtUtil.conv2CoBankMulAmount( context, availBal );
				}
			} else {// ֱ����ȡ��Ӧ����
				retMsg = SFUtil.getDataValue( context, kColl, "RESP_MSG" );
				if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// ֱ�����سɹ���ȡ���
					acctBal = SFUtil.getDataValue( context, kColl, "ACCT_BAL" );// ֱ�����طֵ�λ���
					availBal = SFUtil.getDataValue( context, kColl, "RMB_AVAIL_WITHDRAW_BAL" );// ֱ��ȯ�̿�ȡ���

				}
			}
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {// ʧ��
				SFUtil.chkCond( context, "ST4110", retMsg );
			} else if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// ��ʱ
				SFUtil.chkCond( context, "ST4035", String.format( "��ȯ��[%s]ͨѶ�쳣", secCompCode ) );// ��ʱ
			}
			/**
			 * �鷵�ؽڵ����
			 */
			SFUtil.setResDataValue( context, "QUERY_DATE", tranDate );// ��ѯ����
			SFUtil.setResDataValue( context, "ACCT_ID", signAccountData.getAcctId() );// �����˺�
			SFUtil.setResDataValue( context, "INV_NAME", signAccountData.getInvName() );// �ͻ�����
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );// ȯ�̴���
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );// ֤ȯ�ʽ�̨�˺�
			// if( SFConst.SECU_ZHONGXINZQ.equals( secCompCode ) ) {
			// SFUtil.setResDataValue( context, "ACCT_BAL", "0" );// ȯ�̶�֤ȯ�ʽ�������֤ȯ��֧�ָ�����ѯ��ֱ�ӷ���0������ҿ������
			// }
			SFUtil.setResDataValue( context, "ACCT_BAL", BizUtil.getCobankTranAmount( context, acctBal ) );// ����ҿ������
			SFUtil.setResDataValue( context, "RMB_AVAIL_WITHDRAW_BAL", BizUtil.getCobankTranAmount( context, availBal ) );// ����ҿ������
			SFUtil.setResDataValue( context, "RMB_TRU_BAL", BizUtil.getCobankTranAmount( context, truBal ) );// ����ҹ����˺����
			SFUtil.setResDataValue( context, "USD_ACCT_BAL", "0000000000000" );
			SFUtil.setResDataValue( context, "USD_AVAIL_WITHDRAW_BAL", "0000000000000" );
			SFUtil.setResDataValue( context, "USD_TRU_BAL", "0000000000000" );
			SFUtil.setResDataValue( context, "HKD_ACCT_BAL", "0000000000000" );
			SFUtil.setResDataValue( context, "HKD_AVAIL_WITHDRAW_BAL", "0000000000000" );
			SFUtil.setResDataValue( context, "HKD_TRU_BAL", "0000000000000" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}
}