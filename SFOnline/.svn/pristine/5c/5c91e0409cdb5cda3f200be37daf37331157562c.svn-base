package module.trans.cobank2sf;

import module.bean.AgtCustomerInfo;
import module.dao.AgtCustomerInfoDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 此类为合作行发起账户信息修改
 * @author ex_kjkfb_songshimin
 * 交易码：300104
 */
public class T300104 extends TranBase {

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
	
	@Override
	protected void initialize( Context context ) throws SFException {
		BizUtil.setZhongXinSecuCompCode( context );
	}

	@Override
	public void doHandle( Context context ) throws SFException {
		// 更新账户信息
		SFLogger.info( context, String.format( "updAgtCustomerInfo()开始" ) );
		updAgtCustomerInfo( context );
		SFLogger.info( context, String.format( "updAgtCustomerInfo()结束" ) );

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
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
		AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
		try {
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST5041", String.format( "该客户不属于该合作行" ) );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	/**
	 * 更新账户信息
	 * @param context
	 * @throws SFException
	 */
	private void updAgtCustomerInfo( Context context ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" );
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );
		String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
		AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
		try {
			DBHandler.beginTransaction( context, tranConnection );// 开启事务1
			agtCustomerInfo.setStkAcct( capAcct );
			agtCustomerInfo.setStkCode( secCompCode );
			agtCustomerInfo.setInvName( invName );
			agtCustomerInfo.setIdType( idType );
			agtCustomerInfo.setInvidCode( invIdCode );
			agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, agtCustomerInfo );
			DBHandler.commitTransaction( context, tranConnection );// 提交事务1
			/**
			 * 组返回节点参数
			 */
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
			SFUtil.setResDataValue( context, "INV_NAME", invName );
			SFUtil.setResDataValue( context, "ID_TYPE", idType );
			SFUtil.setResDataValue( context, "INV_ID_CODE", invIdCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

}
