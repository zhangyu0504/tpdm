package module.batch;

import java.sql.Connection;

import module.bean.LocalInfo;
import module.dao.LocalInfoDao;
import module.dao.SignAccountDataDao;

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
 * (��ѯ)���͵��տ���������ǩԼ����
 * 
 * 
 * ���������� ���͵��տ���������ǩԼ����
 * 
 * ��ѯʱ��Σ�9��00~16��00
 * ���ʱ�䣺5����
 * ��ѯ���������ͨ��ISC���ļ����ͳ�ȥ
 *   
 * tran code :800030
 * @author ������
 *
 */
public class T800030 implements ScheduleActionInterface {

	@Override
	public boolean init( Context context ) throws SFException {
		return true;
	}

	@Override
	public void execute( Context context ) throws SFException {
		SFLogger.info( context, "���͵��տ���������ǩԼ������ʼ" );
		Connection tranConnection = null;

		try {
			tranConnection = DBHandler.getConnection( context );

			// ȡ����������
			LocalInfoDao localInfoDao = new LocalInfoDao();
			LocalInfo localInfo = localInfoDao.qryLocalInfo( context, tranConnection );
			String tranDate = localInfo.getBankDate();

			// ����ʱ��
			String sysTime = DateUtil.getMacDateTimeShort();

			/**
			 * ��������������˻������Ϣ 
			 */
			SignAccountDataDao signAccountDataDao = new SignAccountDataDao();
			long openNum = signAccountDataDao.qrySignAccountDataByOpenData( context, tranConnection, tranDate );
			if( openNum > 0 ) {
				SFLogger.info( context, String.format( "��ѯ���յĿ����������ɹ������յĿ�����Ϊ��%d��", openNum ) );
			}

			/**
			 * ������еĿ�����
			 */
			long totalNum = signAccountDataDao.qrySignAccountDataByOpenData( context, tranConnection, null );
			if( totalNum > 0 ) {
				SFLogger.info( context, String.format( "��ѯ���еĿ����������ɹ������еĿ�����Ϊ��%d��", totalNum ) );
			}

			/**
			 * ͨ��ISC��Ϣ���͵��տ���������ǩԼ����
			 */
			sendMsg( context, sysTime, openNum, totalNum );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", "���͵��տ���������ǩԼ����ʧ��" );
		} finally {
			DBHandler.releaseConnection( context, tranConnection );
		}
		SFLogger.info( context, "���͵��տ���������ǩԼ��������" );
	}

	/**
	 * MQ������Ϣ
	 */
	public void sendMsg( Context context, String sysTime, long openNum, long totalNum ) throws SFException {

		try {
			// 14λǰ����ˮ��
			String initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );

			// ���ݽṹ�����
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

			// ����  
			Field field = new Field( new FieldAttr( FieldType.FIELD_STRING, 10, 0 ) );
			field.setValue( SFConst.MAIN_TOPIC_SEND_ACOOUNT_NUM );
			iscSysData.addField( "MAIN_TOPIC", field );

			// ������
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 48, 0 ) );
			field.setValue( SFConst.SUB_TOPIC_SEND_ACOOUNT_NUM );
			iscSysData.addField( "SUB_TOPIC", field );

			// Ӧ����ˮ�ţ�ÿһ����ϢҪΨһ
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SYS_SYSID + initSeqId );
			iscSysData.addField( "APP_MSG_SEQ_NO", field );

			/**************ISC_PUB_HEAD************/

			// ϵͳID
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SYS_SYSID );
			iscPubData.addField( "MSG_PRODUCER_ID", field );

			// ������
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 42, 0 ) );
			field.setValue( SFConst.SERVICE_SEND_ACOOUNT_NUM );
			iscPubData.addField( "SERVICE_CODE", field );

			/**************BODY����****************/
			// ����ʱ��
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 50, 0 ) );
			field.setValue( sysTime );
			bodyData.addField( "TRAN_TIME", field );

			// ���յĿ�����
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 50, 0 ) );
			field.setValue( String.valueOf( openNum ) );
			bodyData.addField( "Open_num", field );

			// ���еĿ�����
			field = new Field( new FieldAttr( FieldType.FIELD_STRING, 50, 0 ) );
			field.setValue( String.valueOf( totalNum ) );
			bodyData.addField( "totle_num", field );

			// ��ʼ����ISC��Ϣ
			ISCMessageBroker.send( context, MsgData );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}