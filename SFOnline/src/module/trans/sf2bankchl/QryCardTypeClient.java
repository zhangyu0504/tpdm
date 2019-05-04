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
 * �Ͽ��ܲ�ѯ�����ͺ͵ȼ�
 * @author ����
 *
 */
public class QryCardTypeClient extends ESBClientBase{
	
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "�Ͽ���ϵͳ��ѯ������[BCARD93432]-��ʼ");
		//���ͱ���
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, keyColl, "ACCT_ID",msg.get("ACCT_ID"));//�˺�
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		Context msgContext=super.send(context,tmpMsg,"BCARD93432","04003000003_04");
		SFLogger.info(context, "�Ͽ���ϵͳ��ѯ������[BCARD93432]-����");
	    return msgContext;
	}
}