package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import module.communication.ESBClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * �Ͽ��ܲ�ѯ���ȼ�:BCARD93439
 * @author ����
 *
 */
public class QryCardLevelClient extends ESBClientBase {

	protected Context doHandle( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "�Ͽ���ϵͳ��ѯ���ȼ�[BCARD93439]-��ʼ" );
		// ���ͱ���
		KeyedCollection keyColl = new KeyedCollection( "MSG_I" );
		SFUtil.addDataField( context, keyColl, "ACCT_ID", msg.get( "ACCT_ID" ) );// �˺�
		Map<String, Object> tmpMsg = new HashMap<String, Object>();
		tmpMsg.put( "MSG_I", keyColl );
		Context msgContext = super.send( context, tmpMsg, "BCARD93439", "04003000003_24" );
		SFLogger.info( context, "�Ͽ���ϵͳ��ѯ���ȼ�[BCARD93439]-����" );
		return msgContext;
	}
}