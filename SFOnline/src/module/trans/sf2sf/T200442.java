package module.trans.sf2sf;

import java.text.DecimalFormat;
import java.util.Map;

import module.bean.Trans;
import module.cache.TransCache;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.log.SFLogger;

/**
 *  ʵʱ��ѯ��ǰӦ�÷���״̬(�ڲ�ͨѶ�ӿڹ�����̨����)
 *  ����˵���� 
 *  <p>Ӧ������ֱ���ͷ�ֱ����Ҫ��ֱ����ʾ��ȯ�̴����ȯ�����ƣ���ǰ�������ʣ�������</p>
 *  <p>��ֱ����ʾ��������ͽ������ƣ���ǰ�������ʣ�������</p>
 *  
 *  
 * tran code :200442 
 * @author ������
 */
public class T200442 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {

	}

	@Override
	public void doHandle( Context context ) throws SFException {
		SFLogger.info( context, "ʵʱ��ѯ��ǰӦ�÷���״̬��ʼ" );
		DecimalFormat df = new DecimalFormat( "#.##%" );// ���ָ�ʽ����������λС��
		try {
			IndexedCollection indexColl = SFUtil.getDataElement( context, "200442_O_ICOLL" );

			// ��ϵͳ���Ի�ȡӦ�ñ�ź�Ӧ������
			String macType = BizUtil.getMacType( context );
			// ��Ӧ�����ڷ�ֱ���򷵻�A���������������Ϣ
			if( SFUtil.isEmpty( macType ) || !SFConst.SYS_SYSNAME.equals( macType ) ) {
				return;
			}
			// if( SFUtil.isNotEmpty( macType ) && SFConst.SYS_SYSNAME.equals( macType ) ) { [ 11��ֱ��ȯ�������Ϣ�����⽻��״̬ 20180416 edit]
			Map<String, Trans> transMap = TransCache.getAllValue();
			if( null != transMap ) {// keySet()����
				for( Map.Entry<String, Trans> entry : transMap.entrySet() ) {
					String txCode = entry.getKey();// ������
					Trans trans = entry.getValue();// ���׶���
					if( SFUtil.isNotEmpty( trans.getType() ) && "0".equals( trans.getType() ) ) {

						// ���㵱ǰ���׵�������:��������ʱ�������߼�¼��
						Map<String, Integer> mapTrad = CacheMap.getCache( SFConst.SYS_CACHE_TRAD );
						Integer tradReqNum = null != mapTrad.get( txCode ) ? mapTrad.get( txCode ) : 0;// �����¼��
						Integer maxProc = trans.getMaxProc();// ���������
						String activeRate = df.format( ( double )tradReqNum / maxProc );// ���(%)
						String idleRate = df.format( ( double )( maxProc - tradReqNum ) / maxProc );// ������(%)

						// ��װ��������
						KeyedCollection keyColl = new KeyedCollection();
						SFUtil.addDataField( context, keyColl, "APP_CODE", BizUtil.getMacCode( context ) );// Ӧ�ñ���
						SFUtil.addDataField( context, keyColl, "TX_CODE", trans.getTxCode() );// ������(ȯ�̴���)
						SFUtil.addDataField( context, keyColl, "TX_NAME", trans.getTxName() );// ��������(ȯ������)
						SFUtil.addDataField( context, keyColl, "CUR_PROC", String.valueOf( tradReqNum ) );// ��ǰ���
						SFUtil.addDataField( context, keyColl, "MAX_PROC", String.valueOf( maxProc ) );// ���������
						SFUtil.addDataField( context, keyColl, "ACTIVE_RATE", activeRate );// ���(%)
						SFUtil.addDataField( context, keyColl, "IDLE_RATE", idleRate );// ������(%)
						indexColl.add( keyColl );
					}
				}
			}
			// } else {// ����11��ֱ��ȯ�������Ϣ
			// Map<String, Param> cacheParam = ParamCache.getGroupParam( "SEC_ZL_MAX_LISTEN_NUM" );
			// if( null != cacheParam ) {// keySet()����
			// for( Map.Entry<String, Param> entry : cacheParam.entrySet() ) {
			// String secCompCode = entry.getKey();// ȯ�̴���
			// Param param = entry.getValue();// ȯ�̶���
			//
			// // ���㵱ǰ���׵�������:��������ʱ�������߼�¼��
			// Map<String, Integer> mapTrad = CacheMap.getCache( SFConst.SYS_CACHE_TRAD );
			// Integer tradReqNum = null != mapTrad.get( secCompCode ) ? mapTrad.get( secCompCode ) : 0;// �����¼��
			// Integer maxProc = Integer.valueOf( param.getValue() );// ���������
			// String activeRate = df.format( ( double )tradReqNum / maxProc );// ���(%)
			// String idleRate = df.format( ( double )( maxProc - tradReqNum ) / maxProc );// ������(%)
			//
			// // ��װ��������
			// KeyedCollection keyColl = new KeyedCollection();
			// SFUtil.addDataField( context, keyColl, "APP_CODE", BizUtil.getMacCode( context ) );// Ӧ�ñ���
			// SFUtil.addDataField( context, keyColl, "TX_CODE", param.getId() );// ������(ȯ�̴���)
			// SFUtil.addDataField( context, keyColl, "TX_NAME", param.getName() );// ��������(ȯ������)
			// SFUtil.addDataField( context, keyColl, "CUR_PROC", String.valueOf( tradReqNum ) );// ��ǰ���
			// SFUtil.addDataField( context, keyColl, "MAX_PROC", param.getValue() );// ���������
			// SFUtil.addDataField( context, keyColl, "ACTIVE_RATE", activeRate );// ���(%)
			// SFUtil.addDataField( context, keyColl, "IDLE_RATE", idleRate );// ������(%)
			// indexColl.add( keyColl );
			// }
			// }
			// }
			SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_SUCCCODE );
			SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, "���׳ɹ�" );

		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, true, "ST9999", "ʵʱ��ѯ��ǰӦ�÷���״̬�쳣" );
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, true, "ST9999", "ʵʱ��ѯ��ǰӦ�÷���״̬�쳣" );
		}
		SFLogger.info( context, "ʵʱ��ѯ��ǰӦ�÷���״̬����" );
	}

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {

	}

	@Override
	protected void chkStart( Context context ) throws SFException {

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {

	}
}