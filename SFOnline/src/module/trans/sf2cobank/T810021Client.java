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
 * 810021 ����ǩԼ��ϵ���������壬����ƽ̨����
 * @author ����
 *
 */
public class T810021Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "�Ϻ����г���ǩԼ��ϵ-��ʼ");
		//֤������ת��
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2CoBank4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}	
		//����������
		KeyedCollection inKeyColl = new KeyedCollection("810021_I");
		SFUtil.addDataField(context, inKeyColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));//ȯ�̴���SecCode
		SFUtil.addDataField(context, inKeyColl, "CAP_ACCT",msg.get("CAP_ACCT"));//֤ȯ�ʽ�̨�˺�CapAcct
		SFUtil.addDataField(context, inKeyColl, "INV_NAME",msg.get("INV_NAME"));//�ͻ�����InvName
		SFUtil.addDataField(context, inKeyColl, "ID_TYPE",msg.get("ID_TYPE"));//֤������IdType
		SFUtil.addDataField(context, inKeyColl, "INV_ID_CODE",msg.get("INV_ID_CODE"));//֤������InvIdCode
		SFUtil.addDataField(context, inKeyColl, "ACCT_ID",msg.get("ACCT_ID"));//�����˺�account_no
		SFUtil.addDataField(context, inKeyColl, "CUR_CODE",msg.get("CUR_CODE"));//����CurCode
		SFUtil.addDataField(context, inKeyColl, "REMARK","����ǩԼ");//��עMemo
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810021_O");
//		SFUtil.addDataField(context, outKeyColl, "REMARK","");//��עMemo
//		SFUtil.addDataElement(context,outKeyColl);	
		//���ͱ���
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810021_I", inKeyColl);
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"810021",bankNo);
		SFLogger.info(context, "�Ϻ����г���ǩԼ��ϵ-����");
	    return msgContext;
	}

}