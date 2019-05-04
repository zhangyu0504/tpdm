package module.trans.sf2secu;

import java.util.HashMap;
import java.util.Map;

import module.communication.SZTSecuClientBase;
import module.communication.SecuClientBase;
import module.communication.ZLSecuClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
/**
 * ���ж�֤ȯת������ȯ�̳���
 * @author ����
 *
 */
public class S2BReverseClient extends SecuClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg)
			throws SFException {
		String secuType=SFUtil.getDataValue(context,SFConst.PUBLIC_SECU_TYPE);
		Context msgContext=null;
		if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
			SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		}
		//֤������ת��
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2Secu4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}	
		if(SFConst.SECU_ZL.equals(secuType)){//ֱ��
			msgContext=zl6034(context,msg);
		}else if(SFConst.SECU_SZT.equals(secuType)){//��֤ͨ
			msgContext=szt12004(context,msg);
		}
		return msgContext;
	}
	
	/**
	 * ֤ת��������ҵ�����룺12004��
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context szt12004(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "���ͱ�������֤ͨ���ж�֤ȯת������ȯ�̳���-��ʼ");	
		// ��¡��֤ͨͨ����ʱ������
		Context msgContext=SFUtil.cloneMsgContextForSZT(context, "Trf.003.01");		
		KeyedCollection kColl= SFUtil.getDataElement(msgContext, "Trf00301");
		KeyedCollection custKcoll = SFUtil.getDataElement(msgContext,kColl, "Cust"); 
		SFUtil.setDataValue(msgContext, custKcoll, "INV_NAME",msg.get("INV_NAME"));//Cust/Name
		SFUtil.setDataValue(msgContext, custKcoll, "ID_TYPE_SZT",msg.get("ID_TYPE"));//Cust/CertType
		SFUtil.setDataValue(msgContext, custKcoll, "INV_ID_CODE",msg.get("INV_ID_CODE"));//Cust/CertId
		//�ͻ�����ת��
		Object invType=msg.get("INV_TYPE");
		if(SFUtil.isNotEmpty(invType)){
			String tmpInvType=BizUtil.convSF2SZTInvType(msgContext,invType.toString());
			SFUtil.setDataValue(msgContext, custKcoll, "INV_TYPE_SZT",tmpInvType);
		}
		
		KeyedCollection bkAcctKcoll = SFUtil.getDataElement(msgContext,kColl, "BkAcct");
		SFUtil.setDataValue(msgContext, bkAcctKcoll, "ACCT_ID",msg.get("ACCT_ID"));
		
		KeyedCollection scAcctKcoll = SFUtil.getDataElement(msgContext,kColl, "ScAcct");
		SFUtil.setDataValue(msgContext, scAcctKcoll, "CAP_ACCT",msg.get("CAP_ACCT"));
		KeyedCollection acctSvcrKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll, "AcctSvcr");
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_TYPE","S");
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));
		KeyedCollection pwdKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll, "Pwd"); 
		SFUtil.setDataValue(msgContext, pwdKcoll, "CAP_ACCT_PWD",msg.get("CAP_ACCT_PWD"));
		
		KeyedCollection cnRefKcoll = SFUtil.getDataElement(msgContext,kColl, "CnRef"); 
		SFUtil.setDataValue(msgContext, cnRefKcoll, "ORG_TX_SEQ_ID",msg.get("ORG_TX_SEQ_ID"));
		SFUtil.setDataValue(msgContext, cnRefKcoll, "ORG_TX_ISSRTYPE",msg.get("ORG_TX_ISSRTYPE"));
		SFUtil.setDataValue(msgContext, kColl,"DGST","���ж�֤ȯת���г���");

		SFUtil.setDataValue(msgContext, kColl,"CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.setDataValue(msgContext, kColl,"TX_AMOUNT",msg.get("TX_AMOUNT"));
		//���ͱ���
		SZTSecuClientBase client=new SZTSecuClientBase();
		msgContext=client.send(msgContext,msg,"12004");
		SFLogger.info(context, "���ͱ�������֤ͨ���ж�֤ȯת������ȯ�̳���-����");
	    return msgContext;
	}

	/**
	 * 6034 ���ж�֤ȯת������ȯ�̳���
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context zl6034(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "ֱ��ȯ�����ж�֤ȯת������ȯ�̳���-��ʼ");	
		KeyedCollection kColl=new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, kColl, "INV_TYPE",msg.get("INV_TYPE"));
		SFUtil.addDataField(context, kColl, "INV_NAME",msg.get("INV_NAME"));
		SFUtil.addDataField(context, kColl, "ID_TYPE_ZL",msg.get("ID_TYPE"));
		SFUtil.addDataField(context, kColl, "ACCT_ID",msg.get("ACCT_ID"));
		SFUtil.addDataField(context, kColl, "SEC_ACCT",msg.get("SEC_ACCT"));
		SFUtil.addDataField(context, kColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));
		SFUtil.addDataField(context, kColl, "CAP_ACCT",msg.get("CAP_ACCT"));
		SFUtil.addDataField(context, kColl, "CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.addDataField(context, kColl, "CASH_REMIT_FLAG",msg.get("CASH_REMIT_FLAG"));
		SFUtil.addDataField(context, kColl, "TX_AMOUNT",msg.get("TX_AMOUNT"));
		SFUtil.addDataField(context, kColl, "UNIT_TELLER_ID",msg.get("UNIT_TELLER_ID"));
		SFUtil.addDataField(context, kColl, "REPT_FLAG",msg.get("REPT_FLAG"));
		SFUtil.addDataField(context, kColl, "SUB_TX_SEQ_ID",msg.get("SUB_TX_SEQ_ID"));
		SFUtil.addDataField(context, kColl, "ORG_TX_SEQ_ID",msg.get("ORG_TX_SEQ_ID"));
		Map<String,Object> tmpMsg=new HashMap<String,Object>();
		tmpMsg.put("MSG_I", kColl);		
		//���ͱ���
		ZLSecuClientBase client=new ZLSecuClientBase();
		Context msgContext=client.send(context,tmpMsg,"6034");
		SFLogger.info(context, "ֱ��ȯ�����ж�֤ȯת������ȯ�̳���-����");
	   return msgContext;
	}
	
}