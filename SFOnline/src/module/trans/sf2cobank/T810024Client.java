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
 * 810024 �ʽ�Ԥ�����������壬����ƽ̨����
 * @author ����
 *
 */
public class T810024Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "�Ϻ������ʽ�Ԥ��-��ʼ");	    
		//����������
		KeyedCollection inKeyColl = new KeyedCollection("810024_I");
		SFUtil.addDataField(context, inKeyColl, "BANK_ACCT",msg.get("BANK_ACCT"));//�������˺�BankAcct
		SFUtil.addDataField(context, inKeyColl, "ACCT_BAL",msg.get("ACCT_BAL"));//�˺����BankBal
		SFUtil.addDataField(context, inKeyColl, "TOTAL_LIMIT",msg.get("TOTAL_LIMIT"));//�ܶ��SumLim
		SFUtil.addDataField(context, inKeyColl, "USED_LIMIT",msg.get("USED_LIMIT"));//���ö��UseLim
		SFUtil.addDataField(context, inKeyColl, "AVAIL_LIMIT",msg.get("AVAIL_LIMIT"));//���ö��LimBal
		SFUtil.addDataField(context, inKeyColl, "WARN_FLAG",msg.get("WARN_FLAG"));//Ԥ������WarnFlag
		SFUtil.addDataField(context, inKeyColl, "WARN_MONEY",msg.get("WARN_MONEY"));//Ԥ�����WarnMoney
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810024_O");
//		SFUtil.addDataField(context, outKeyColl, "AGENT_LOG_NO  ","");//��������ˮ��serial_no
//		SFUtil.addDataElement(context,outKeyColl);		
		//���ͱ���
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810024_I", inKeyColl);
		Context msgContext=super.send(context,tmpMsg,"810024",bankNo);
		SFLogger.info(context, "�Ϻ������ʽ�Ԥ��-����");
	    return msgContext;
	}

}
