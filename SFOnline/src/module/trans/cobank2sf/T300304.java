package module.trans.cobank2sf;

import module.bean.AgtCustomerInfo;
import module.bean.AgtTranList;
import module.dao.AgtCustomerInfoDao;
import module.dao.AgtTranListDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 此类为合作行发起，查询流水处理情况
 * @author ex_kjkfb_songshimin
 * tran_code:300304
 */
public class T300304 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {

		// 查询流水处理情况
		SFLogger.info( context, String.format( "qryTranList()开始" ) );
		qryTranList( context );
		SFLogger.info( context, String.format( "qryTranList()结束" ) );
	}

	/**
	 * 查询流水处理情况
	 * @param context
	 * @throws SFException
	 */
	private void qryTranList( Context context ) throws SFException {
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 银行号在请求报文体中没有配置，需要确认是否存放在报文头中
		String queryLogNo = SFUtil.getReqDataValue( context, "PICS_LOG_NO" );
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		try {
			String acctId = null;
			String invName = null;
			String secCompCode = null;
			String capAcct = null;
			String txAmount = null;
			String status = null;
			String voidFlag = null;
			String tranResult = null;

			/* 通过流水号查出客户的流水处理情况 */
			AgtTranList agtTranList = agtTranListDao.qryAgtTranListByBankIdAndQueryLogNo( context, tranConnection, bankId, queryLogNo );
			if( agtTranList == null ) {
				status = "0";
			} else {
				acctId = agtTranList.getAcctNo();
				secCompCode = agtTranList.getStkCode();
				capAcct = agtTranList.getStkAcct();
				txAmount = agtTranList.getTranAmount();
				voidFlag = agtTranList.getVoidFlag();
				tranResult = agtTranList.getTranResult();
				AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
				AgtCustomerInfo agetCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
				if( null != agetCustomerInfo ) {
					invName = agetCustomerInfo.getInvName();
				}
				if( "1".equals( voidFlag ) || "W".equals( voidFlag ) ) {
					status = "2";
				//20180514-wanghua-modify for (原C存储是SC0000,JAVA存的是ST0000)-s
				//} else if( SFConst.RESPCODE_SUCCCODE_COBANK.equals( tranResult ) ) {
				} else if("SC0000".equals( tranResult )||"ST0000".equals( tranResult )) {
				//20180514-wanghua-modify for (原C存储是SC0000,JAVA存的是ST0000)-e
					status = "1";
				} else {
					status = "3";
				}
			}
			/**
			 * 组返回节点参数
			 */
			SFUtil.setResDataValue( context, "ACCT_ID", acctId );
			SFUtil.setResDataValue( context, "INV_NAME", invName );
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "TX_AMOUNT", txAmount );
			SFUtil.setResDataValue( context, "TX_STATUS", status );
//			SFUtil.setResDataValue( context, "MAC", "520D3647" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHost( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		// TODO Auto-generated method stub

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
