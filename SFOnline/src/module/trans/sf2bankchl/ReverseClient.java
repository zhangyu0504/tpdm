package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import module.communication.ESBClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
/**
 * ����ͨ�ó���:G1010
 * @author ����
 *
 */
public class ReverseClient extends ESBClientBase{
	
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "����������ͨ�ó���[G1010]-��ʼ");
		if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
			SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		}
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, keyColl, "OLD_BUSS_SEQ_NO",msg.get("OLD_BUSS_SEQ_NO"));//ԭҵ����ˮ��     TRANSEQID	
		SFUtil.addDataField(context, keyColl, "OLD_TRAN_SEQ_NO",msg.get("OLD_TRAN_SEQ_NO"));//ԭ������ˮ�� BUSISEQID	
		SFUtil.addDataField(context, keyColl, "REASON",msg.get("REASON"));//ԭ��
		SFUtil.addDataField(context, keyColl, "FLAG",msg.get("FLAG"));//��־
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"G1010","01004000001_24");
		SFLogger.info(context, "����������ͨ�ó���[G1010]-����");
	    return msgContext;
	}
}