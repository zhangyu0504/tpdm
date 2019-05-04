package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;
import core.log.SFLogger;

import module.communication.ESBClientBase;

/**
 * ����ϵͳ������ڣ�M8004 ��ǰ�����ղ�ѯ
 * <p>Description:		<p>
 * <p>Module:			<p>
 *
 * @author EX_KJKFB_LVCHAOHONG
 * @date 2017-11-2 ����11:07:42
 * @since 1.0
 */
public class UpdTxDateClient extends ESBClientBase {

	@Override
	protected Context doHandle( Context context, Map<String, Object> msg ) throws SFException {

		SFLogger.info( context, "��������ǰ�����ղ�ѯ[M8004]-��ʼ" );
		Map<String, Object> tmpMsg = new HashMap<String, Object>();
		Context msgContext = null;
		try {
			// ��SYS_HEAD
			KeyedCollection sysHeadKColl = SFUtil.getTemplateKColl( context, "SYS_HEAD" );
			SFUtil.setDataValue( context, sysHeadKColl, "CONSUMER_ID", msg.get( "CONSUMER_ID" ) );// ϵͳID
			SFUtil.setDataValue( context, sysHeadKColl, "TRAN_DATE", msg.get( "TRAN_DATE" ) );// ��������
			tmpMsg.put( "SYS_HEAD", sysHeadKColl );

			// ��MSG_I
			KeyedCollection msgIColl = new KeyedCollection( "MSG_I" );
			SFUtil.addDataField( context, msgIColl, "VALIDATION_FLAG", msg.get( "VALIDATION_FLAG" ) );// ��ѯ��־
			tmpMsg.put( "MSG_I", msgIColl );

			// ���ͱ���
			msgContext = super.send( context, tmpMsg, "M8004", "11003000018_06" );
		} catch( Exception e ) {
			SFLogger.info( context, String.format( "��������ǰ�����ղ�ѯʧ��[%s]", e.getMessage() ) );
			throw new SFException( "��������ǰ�����ղ�ѯʧ��" );
		}

		SFLogger.info( context, "��������ǰ�����ղ�ѯ[M8004]-����" );
		return msgContext;
	}

}
