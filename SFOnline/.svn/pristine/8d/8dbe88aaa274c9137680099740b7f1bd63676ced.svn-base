package module.batch;

import java.sql.Connection;
import java.util.List;

import module.bean.AutoBecif;
import module.dao.AutoBecifDao;

import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.dc.eai.data.FieldAttr;
import com.dc.eai.data.FieldType;
import com.ecc.emp.core.Context;
import com.isc.core.ISCMessageBroker;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
import core.schedule.ScheduleActionInterface;

/**
 * (轮询)发送签解约协议给BECIF系统
 * 
 * 
 * 功能描述： 轮询发送表trdautobecif中的记录给BECIF系统
 * 
 * 轮询时间段：工作日8:30开始
 * 间隔时间：5分钟
 *   
 * tran code :800020
 * @author 吕超鸿
 *
 */
public class T800020 implements ScheduleActionInterface {

	@Override
	public boolean init( Context context ) throws SFException {
		return true;
	}

	@Override
	public void execute( Context context ) throws SFException {
		SFLogger.info( context, "发送签解约协议给BECIF系统开始" );
		Connection tranConnection = null;
		try {
			// 获取连接
			tranConnection = DBHandler.getConnection( context );

			// 物理日期
			String curDate = DateUtil.getMacDate();

			// 查询全部待处理记录数
			AutoBecifDao autoBecifDao = new AutoBecifDao();
			List<AutoBecif> autoBecifList = autoBecifDao.qryAutoBecifByStatus( context, tranConnection, curDate, "0" );

			// 如全部待处理记录数=0即如果没记录即退出
			if( null == autoBecifList || autoBecifList.size() == 0 ) {
				SFLogger.info( context, String.format( "待发送becif签约总个数为0交易退出!" ) );
				return;
			}
			SFLogger.info( context, String.format( "待上送becif签解约总数为[%s]", String.valueOf( autoBecifList.size() ) ) );
			// 进入发送签解约becif协议循环体
			for( AutoBecif autoBecif : autoBecifList ) {
				if( SFUtil.isEmpty( autoBecif.getSubTxSeqId() ) ) {
					SFLogger.info( context, String.format( "subtxseqid不能为空，跳过进入下一条记录!" ) );
					continue;
				}
				if( SFUtil.isEmpty( autoBecif.getAgreementNo() ) ) {
					SFLogger.info( context, String.format( "agreementNo不能为空，跳过进入下一条记录!" ) );
					continue;
				}
				if( SFUtil.isEmpty( autoBecif.getBecifNo() ) ) {
					SFLogger.info( context, String.format( "becifNo不能为空，跳过进入下一条记录!" ) );
					continue;
				}
				if( SFUtil.isEmpty( autoBecif.getAgreementSubType() ) ) {
					SFLogger.info( context, String.format( "agreementSubType不能为空，跳过进入下一条记录!" ) );
					continue;
				}
				if( SFUtil.isEmpty( autoBecif.getAgreementStatus() ) ) {
					SFLogger.info( context, String.format( "agreementStatus不能为空，跳过进入下一条记录!" ) );
					continue;
				}

				SFLogger.info( context, "调用MQ发送消息到BECIF系统去建立取消协议" );
				String cmd = String.format( "MQ消息主体： %s %s %s %s %s %s %s %s %s %s %s %s  ", autoBecif.getSubTxSeqId(), autoBecif.getAgreementNo(), autoBecif.getBecifNo(), autoBecif.getAgreementType(), autoBecif.getAgreementSubType(), autoBecif.getAgreementStatus(), autoBecif.getProductNo(), autoBecif.getOpenDate(), autoBecif.getCloseDate(), autoBecif.getDeptNo(), autoBecif.getUserId(), autoBecif.getBusinessSeriesCD() );
				SFLogger.info( context, cmd );

				// 发送发送签解约协议到BECIF
				sendMsg( context, autoBecif );

				// 发送成功后更新状态为 "1"
				DBHandler.beginTransaction( context, tranConnection );
				autoBecif.setStatus( "1" );
				autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );
				DBHandler.commitTransaction( context, tranConnection );

			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			DBHandler.releaseConnection( context, tranConnection );
		}
		SFLogger.info( context, "发送签解约协议给BECIF系统结束" );
	}

	/**
	 * MQ发送消息
	 */
	public void sendMsg( Context context, AutoBecif autoBecif ) throws SFException {
		SFLogger.info( context, "发送签解约协议给BECIF系统 sendMsg()开始" );
		try {

			// 数据结构体对象
			CompositeData MsgData = new CompositeData();

			CompositeData iscSysData = new CompositeData();
			CompositeData iscPubData = new CompositeData();
			CompositeData bodyData = new CompositeData();
			CompositeData iscRepBody = new CompositeData();

			MsgData.addStruct( "ISC_SYS_HEAD", iscSysData );
			MsgData.addStruct( "ISC_PUB_HEAD", iscPubData );
			MsgData.addStruct( "ISC_REP_BODY", iscRepBody );
			MsgData.addStruct( "BODY", bodyData );

			/**************ISC_SYS_HEAD************/

			// 主题  
			Field field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( SFConst.MAIN_TOPIC_AGREEMENT_SYNC );
			iscSysData.addField( "MAIN_TOPIC", field );

			// 子主题
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 48, 0 ) );
			field.setValue( SFConst.SUB_TOPIC_AGREEMENT_SYNC );
			iscSysData.addField( "SUB_TOPIC", field );

			// 应用流水号，每一条消息要唯一
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SYS_SYSID + autoBecif.getSubTxSeqId() );
			iscSysData.addField( "APP_MSG_SEQ_NO", field );

			/**************ISC_PUB_HEAD************/

			// 系统ID 
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SYS_SYSID );
			iscPubData.addField( "MSG_PRODUCER_ID", field );

			// 场景码
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SERVICE_CODE_AGREEMENT_SYNC );
			iscPubData.addField( "SERVICE_CODE", field );

			/**************BODY参数****************/

			// 协议号
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 50, 0 ) );
			field.setValue( autoBecif.getAgreementNo() );
			bodyData.addField( "AGREEMENT_NO", field );

			// 客户号
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 12, 0 ) );
			field.setValue( autoBecif.getBecifNo() );
			bodyData.addField( "BECIF_NO", field );

			// 协议大类
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( autoBecif.getAgreementType() );
			bodyData.addField( "AGREEMENT_TYPE", field );

			// 协议小类
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( autoBecif.getAgreementSubType() );
			bodyData.addField( "AGREEMENT_SUB_TYPE", field );

			// 协议状态
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 3, 0 ) );
			field.setValue( autoBecif.getAgreementStatus() );
			bodyData.addField( "AGREEMENT_STATUS", field );

			// 绑定的卡帐号
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 60, 0 ) );
			field.setValue( autoBecif.getProductNo() );
			bodyData.addField( "PRODUCT_NO", field );

			// 签约时间
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 8, 0 ) );
			field.setValue( autoBecif.getOpenDate() );
			bodyData.addField( "OPEN_DATE", field );

			// 解约时间
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 8, 0 ) );
			field.setValue( autoBecif.getCloseDate() );
			bodyData.addField( "CLOSE_DATE", field );

			// 签解约机构
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( autoBecif.getDeptNo() );
			bodyData.addField( "DEPT_NO", field );

			// 经办人
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 100, 0 ) );
			field.setValue( autoBecif.getUserId() );
			bodyData.addField( "USER_ID", field );

			// 业务系统代码
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( autoBecif.getBusinessSeriesCD() );
			bodyData.addField( "BUSINESS_SERIES_CD", field );

			// 开始发送ISC消息
			ISCMessageBroker.send( context, MsgData );

		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "发送签解约协议给BECIF系统 sendMsg()结束" );
	}
}