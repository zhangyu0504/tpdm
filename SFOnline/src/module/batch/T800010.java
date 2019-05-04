package module.batch;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import module.bean.TrcNoTBL;
import module.dao.TrcNoTBLDao;
import module.trans.sf2bankchl.UpdTxDateClient;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
import core.schedule.ScheduleActionInterface;

/**
 * (轮询)更新系统会计日期
 * 
 * 
 * 功能描述： 24小时每分钟执行一次，轮询更新系统会计日期
 * 
 *   调用主机接口（11003000018_06）获取会记日期
 *   
 *   如果和数据库的日期不一致，则更新TRCNOTBL表记录
 *   
 * tran code :800010
 * @author 吕超鸿
 *
 */
public class T800010 implements ScheduleActionInterface {

	@Override
	public boolean init( Context context ) throws SFException {
		return true;
	}

	@Override
	public void execute( Context context ) throws SFException {
		SFLogger.info( context, "更新系统会计日期开始" );
		Connection tranConnection = null;

		try {

			// 获取连接
			tranConnection = DBHandler.getConnection( context );
			// 获取连接放到context
			SFUtil.setDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION, tranConnection );

			// 三方存管获取流水号
			String initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );
			// 获取22位流水号
			String chlSeqId = BizUtil.getChlSeqId( context, BizUtil.getSubTxSeqId( initSeqId ) );

			// 组装M8004报文,发往核心并接收核心返回信息
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "TRAN_DATE", DateUtil.getMacDate() );// 交易日期
			msg.put( "CONSUMER_ID", SFConst.SYS_SYSID );// 三方存管系统ID
			msg.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msg.put( "VALIDATION_FLAG", "Y" );// 查询标志

			// 发送报文到核心
			Context hostContext = new UpdTxDateClient().send( context, msg );

			// 获取返回码
			String retFlag = SFUtil.getDataValue( hostContext, SFConst.PUBLIC_RET_FLAG );
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "上主机[查询当前工作日]失败" ) );

			// 解析返回报文
			String tranDate = SFUtil.getDataValue( hostContext, "MSG_O.DATE" );
			SFUtil.chkCond( context, SFUtil.isEmpty( tranDate ), "ST4895", String.format( "获取交易日期TRAN_DATE失败" ) );
			SFUtil.chkCond( context, tranDate.length() != 8, "ST4895", String.format( "交易日期长度不是8位,不处理，退出程序" ) );

			// 查询TRCNOTBL表中的日期，判断及更新
			TrcNoTBLDao trcNoTBLDao = new TrcNoTBLDao();
			TrcNoTBL trcNoTBL = trcNoTBLDao.qryTrcNoTBL( context, tranConnection );
			if( null != trcNoTBL ) {
				String trcDate = trcNoTBL.getTrcDate();
				SFLogger.info( context, String.format( "上主机查询交易日期[%s],TRCNOTBL表交易日期[%s]", tranDate, trcDate ) );
				// 如果日期不同，则更新日期
				if( !tranDate.equals( trcDate ) ) {
					DBHandler.beginTransaction( context, tranConnection );// 开启事务
					trcNoTBLDao.saveTrcNoTBL( context, tranConnection, tranDate );
					DBHandler.commitTransaction( context, tranConnection );// 提交事务
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e1 ) {
			SFUtil.chkCond( context, "ST4895", e1.getMessage() );
		} finally {
			DBHandler.releaseConnection( context, tranConnection );
		}
		SFLogger.info( context, "更新系统会计日期结束" );
	}
}