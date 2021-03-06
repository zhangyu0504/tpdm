package module.trans.cobank2sf;

import module.bean.AgtAgentInfo;
import module.dao.AgtAgentInfoDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 此类为合作行发起资金预警查询/设置
 * @author ex_kjkfb_songshimin
 * tran_code:300204
 */
public class T300204 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {
		// 更新或查询资金预警信息
		SFLogger.info( context, String.format( "updOrQryAgtAgentInfo()开始" ) );
		updOrQryAgtAgentInfo( context );
		SFLogger.info( context, String.format( "updOrQryAgtAgentInfo()结束" ) );
	}

	/**
	 * 更新资金预警信息
	 * @param context
	 * @throws SFException
	 */
	private void updOrQryAgtAgentInfo( Context context ) throws SFException {
		String flag = SFUtil.getReqDataValue( context, "FLAG" );// 操作类型
		String warnFlag = null;
		String warnMoney = null;
		String bankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 报文配置文档没有体现该字段
		AgtAgentInfo agtAgentInfo = new AgtAgentInfo();
		AgtAgentInfoDao agtAgentDao = new AgtAgentInfoDao();
		try {
			if( "1".equals( flag ) ) {// 设置预警
				warnFlag = SFUtil.getReqDataValue( context, "WARN_FLAG" );// 预警类型
				warnMoney = SFUtil.getReqDataValue( context, "WARN_MONEY" );// 预警金额
				if( !"1".equals( warnFlag ) && !"2".equals( warnFlag ) ) {
					SFUtil.chkCond( context, "ST5702", String.format( "[预警类型[%s]非法", warnFlag ) );

				}
				DBHandler.beginTransaction( context, tranConnection );// 开启事务1
				agtAgentInfo.setBankId( bankId );
				agtAgentInfo.setWarnFlag( warnFlag );
				agtAgentInfo.setWarnMoney( warnMoney );
				agtAgentDao.saveAgtAgentInfo( context, tranConnection, agtAgentInfo );
				DBHandler.commitTransaction( context, tranConnection );

			} else if( "2".equals( flag ) ) {// 查询预警
				agtAgentInfo.setBankId( bankId );
				agtAgentInfo = agtAgentDao.qryAgtAgentInfo( context, tranConnection, bankId );
				warnFlag = agtAgentInfo.getWarnFlag();
				warnMoney = agtAgentInfo.getWarnMoney();
			}

			/**
			 * 组返回节点参数
			 */
			SFUtil.setResDataValue( context, "WARN_FLAG", warnFlag );
			SFUtil.setResDataValue( context, "WARN_MONEY", warnMoney );
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
		String flag = SFUtil.getReqDataValue( context, "FLAG" );// 操作类型
		SFUtil.chkCond( context, !"1".equals( flag ) && !"2".equals( flag ), "ST5702", String.format( "[交易类型[%s]]非法", flag ) );

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}
}
