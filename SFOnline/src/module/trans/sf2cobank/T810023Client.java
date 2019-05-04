package module.trans.sf2cobank;

import java.util.HashMap;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;
import core.log.SFLogger;

import module.communication.CoBankClientBase;
/**
 * 810023 �ʽ�������������壬����ƽ̨����
 * @author ����
 *
 */
public class T810023Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "�Ϻ������ʽ����-��ʼ");
		KeyedCollection inKeyColl = null;
		//����������
		if(!context.containsKey( "810023_I" )){
			inKeyColl = new KeyedCollection("810023_I");
			SFUtil.addDataField(context, inKeyColl, "PICS_LOG_NO",msg.get("PICS_LOG_NO"));//ԭƽ̨��ˮ��serial_no
			SFUtil.addDataElement(context,inKeyColl);	
		}else{
			inKeyColl = SFUtil.getDataElement( context, "810023_I" );
			SFUtil.setDataValue( context, inKeyColl, "PICS_LOG_NO", msg.get("PICS_LOG_NO") );
		}
		
//		KeyedCollection outKeyColl = new KeyedCollection("810023_O");
//		SFUtil.addDataField(context, outKeyColl, "AGENT_LOG_NO  ","");//��������ˮ��serial_no
//		SFUtil.addDataElement(context,outKeyColl);	
		//���ͱ���
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810023_I", inKeyColl);	
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"810023",bankNo);
		SFLogger.info(context, "�Ϻ������ʽ����-����");
	    return msgContext;
	}

}