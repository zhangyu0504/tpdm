package module.trans.sf2cobank;

import java.util.HashMap;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFUtil;
import core.log.SFLogger;

import module.communication.CoBankClientBase;
/**
 * 810026 ����ƽ̨����һ��ʽǩԼ���ף������з����˻���Ϣ�Լ��˻�״̬
 * @author ����
 *
 */
public class T810026Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "�Ϻ�����һ��ʽǩԼ����-��ʼ");	
		//֤������ת��
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2CoBank4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}    
		//����������
		KeyedCollection inKeyColl = new KeyedCollection("810026_I");
		SFUtil.addDataField(context, inKeyColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));//ȯ�̴���SecCode
		SFUtil.addDataField(context, inKeyColl, "CAP_ACCT",msg.get("CAP_ACCT"));//֤ȯ�ʽ�̨�˺�CapAcct
		SFUtil.addDataField(context, inKeyColl, "INV_NAME",msg.get("INV_NAME"));//ȯ�̶˿ͻ�����InvName
		SFUtil.addDataField(context, inKeyColl, "ID_TYPE",msg.get("ID_TYPE"));//ȯ�̶�֤������IdType
		SFUtil.addDataField(context, inKeyColl, "INV_ID_CODE",msg.get("INV_ID_CODE"));//ȯ�̶�֤������InvIdCode
		SFUtil.addDataField(context, inKeyColl, "ACCT_ID",msg.get("ACCT_ID"));//�����˺�account_no
		SFUtil.addDataField(context, inKeyColl, "CUR_CODE",msg.get("CUR_CODE"));//����CurCode
		SFUtil.addDataField(context, inKeyColl, "REMARK",msg.get("REMARK"));//��עMemo
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810026_O");
//		SFUtil.addDataField(context, outKeyColl, "ACCT_ID","");//�����˻�Account
//		SFUtil.addDataField(context, outKeyColl, "INV_NAME","");//���ж˿ͻ�����InvName
//		SFUtil.addDataField(context, outKeyColl, "ID_TYPE","");//���ж�֤������IdType
//		SFUtil.addDataField(context, outKeyColl, "INV_ID_CODE","");//���ж�֤������InvIdCode
//		SFUtil.addDataField(context, outKeyColl, "STATUS","");//�˻�״̬Status
//		SFUtil.addDataField(context, outKeyColl, "BRANCH_ID","");//���к�
//		SFUtil.addDataField(context, outKeyColl, "BANK_ID","");//�����к�
//		SFUtil.addDataField(context, outKeyColl, "REMARK","");//��עMemo
//		SFUtil.addDataElement(context,outKeyColl);
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810026_I", inKeyColl);	
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"810026",bankNo);
		SFLogger.info(context, "�Ϻ�����һ��ʽǩԼ����-����");
	    return msgContext;
	}

}
