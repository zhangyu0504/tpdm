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
 * ���ж˲�ѯȯ�̶��ʽ����
 * @author ����
 *
 */
public class QryBalClient extends SecuClientBase{

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
			msgContext=zl6052(context,msg);
		}else if(SFConst.SECU_SZT.equals(secuType)){//��֤ͨ
			msgContext=szt11008(context,msg);
		}
		return msgContext;
	}
	
	/**
	 * ���ж˲�ѯȯ�̶��ʽ���ҵ�����룺11008��
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context szt11008(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "���ͱ�������֤ͨ���ж˲�ѯȯ�̶��ʽ����-��ʼ");
		// ��¡��֤ͨͨ����ʱ������
		Context msgContext=SFUtil.cloneMsgContextForSZT(context, "Acmt.009.01");	
		KeyedCollection kColl= SFUtil.getDataElement(msgContext, "Acmt00901");
		if(SFUtil.isNotEmpty( msg.get( "SEC_BRCH_ID" ))){
			if(!msgContext.containsKey( "MsgHdr" )){
				SFUtil.addFormatToContext( msgContext, "MsgHdr" );
			}
			SFUtil.setDataValue( msgContext, "MsgHdr.Recver.BrchId", msg.get( "SEC_BRCH_ID" ) );
		}
		KeyedCollection custKcoll = SFUtil.getDataElement(msgContext,kColl	, "Cust"); 
		SFUtil.setDataValue(msgContext, custKcoll, "INV_NAME",msg.get("INV_NAME"));//Cust/Name
		SFUtil.setDataValue(msgContext, custKcoll, "ID_TYPE_SZT",msg.get("ID_TYPE"));//Cust/CertType
		SFUtil.setDataValue(msgContext, custKcoll, "INV_ID_CODE",msg.get("INV_ID_CODE"));//Agt/CertId
		//SFUtil.setDataValue(context, custKcoll, "INV_TYPE_SZT",msg.get("INV_TYPE_SZT"));//Cust/Type	
		//�ͻ�����ת��
		Object invType=msg.get("INV_TYPE");
		if(SFUtil.isNotEmpty(invType)){
			String tmpInvType=BizUtil.convSF2SZTInvType(msgContext,invType.toString());
			SFUtil.addDataField(msgContext, kColl, "INV_TYPE_SZT",tmpInvType);
		}
		KeyedCollection bkAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "BkAcct");
		SFUtil.setDataValue(msgContext, bkAcctKcoll, "ACCT_ID",msg.get("ACCT_ID"));
		KeyedCollection scAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "ScAcct");
		SFUtil.setDataValue(msgContext, scAcctKcoll, "CAP_ACCT",msg.get("CAP_ACCT"));
		KeyedCollection acctSvcrKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll	, "AcctSvcr");
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));
//		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_BRCH_ID",msg.get("SEC_BRCH_ID"));
		KeyedCollection pwdKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll, "Pwd"); 
		SFUtil.setDataValue(msgContext, pwdKcoll, "CAP_ACCT_PWD",msg.get("CAP_ACCT_PWD"));
		SFUtil.setDataValue(msgContext, kColl,"CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.setDataValue(msgContext, kColl,"DGST","��ѯȯ�̶�̨�����");

		//���ͱ���
		SZTSecuClientBase client=new SZTSecuClientBase();
		msgContext=client.send(msgContext,msg,"11008");
		SFLogger.info(context, "���ͱ�������֤ͨ���ж˲�ѯȯ�̶��ʽ����-����");
	    return msgContext;
	}

	/**
	 * 6052 ���ж˲�ѯȯ�̶��ʽ����
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context zl6052(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "ֱ��ȯ�����ж˲�ѯȯ�̶��ʽ����-��ʼ");	
		KeyedCollection kColl=new KeyedCollection("6052_I");
		SFUtil.addDataField(context, kColl, "ACCT_ID",msg.get("ACCT_ID"));
		SFUtil.addDataField(context, kColl, "SEC_ACCT",msg.get("SEC_ACCT"));
		SFUtil.addDataField(context, kColl, "CAP_ACCT",msg.get("CAP_ACCT"));
		SFUtil.addDataField(context, kColl, "CAP_ACCT_PWD",msg.get("CAP_ACCT_PWD"));
		SFUtil.addDataField(context, kColl, "CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.addDataField(context, kColl, "UNIT_TELLER_ID",msg.get("UNIT_TELLER_ID"));
		SFUtil.addDataField(context, kColl, "SUB_TX_SEQ_ID",msg.get("SUB_TX_SEQ_ID"));
		Map<String,Object> tmpMsg=new HashMap<String,Object>();
		tmpMsg.put("6052_I", kColl);		
		//���ͱ���
		ZLSecuClientBase client=new ZLSecuClientBase();
		Context msgContext=client.send(context,tmpMsg,"6052");
		SFLogger.info(context, "ֱ��ȯ�����ж˲�ѯȯ�̶��ʽ����-����");
	   return msgContext;
	}
	
}
