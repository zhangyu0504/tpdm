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
 * 810025 ��ѯ�����пͻ��ʻ������������壬����ƽ̨����
 * @author ����
 *
 */
public class T810025Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "�Ϻ����в�ѯ�����пͻ��ʻ����-��ʼ");	    
		//����������
		KeyedCollection inKeyColl = new KeyedCollection("810025_I");
		SFUtil.addDataField(context, inKeyColl, "ACCT_ID",msg.get("ACCT_ID"));//�����˺�account_no
		SFUtil.addDataField(context, inKeyColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));//ȯ�̴���SecCode
		SFUtil.addDataField(context, inKeyColl, "CAP_ACCT",msg.get("CAP_ACCT"));//֤ȯ�ʽ�̨�˺�StkAcct
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810025_O");
//		SFUtil.addDataField(context, outKeyColl, "ACCT_ID","");//�����˺�account_no
//		SFUtil.addDataField(context, outKeyColl, "INV_NAME","");//�ͻ�����CustName
//		SFUtil.addDataField(context, outKeyColl, "SEC_COMP_CODE","");//ȯ�̴���SecCode
//		SFUtil.addDataField(context, outKeyColl, "CAP_ACCT","");//֤ȯ�ʽ�̨�˺�StkAcct
//		SFUtil.addDataField(context, outKeyColl, "ACCT_BAL","");//�˻����Amount
//		SFUtil.addDataElement(context,outKeyColl);
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810025_I", inKeyColl);	
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"810025",bankNo);
		SFLogger.info(context, "�Ϻ����в�ѯ�����пͻ��ʻ����-����");
	    return msgContext;
	}

}
