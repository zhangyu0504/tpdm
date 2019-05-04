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
 * һ��һ����ת��:G1001
 * @author ����
 *
 */
public class TransferClient extends ESBClientBase{
	
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "������һ��һ����ת��[G1001]-��ʼ");
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		KeyedCollection sysKcoll = null;
		KeyedCollection appKcoll = null;
		//if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
		//	SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		//}
		try {
			//����������ȡ�ý�������
			String initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );
			//����������ȡ��������
			String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );

			/*KeyedCollection sysHeadColl = SFUtil.getTemplateKColl(context, "SYS_HEAD");
			SFUtil.addDataField(context, sysHeadColl, "SYS_HEAD.CONSUMER_SEQ_NO","1111111111111111111");
			SFUtil.addDataElement( context, sysHeadColl );*/
			if( SFUtil.isNotEmpty( msg.get( "CONSUMER_SEQ_NO" ) ) ) {//������ˮ
				if( context.containsKey( "SYS_HEAD" ) ) {
					sysKcoll = SFUtil.getDataElement( context, "SYS_HEAD" );
					sysKcoll = (KeyedCollection)sysKcoll.clone();
					SFUtil.setDataValue( context, sysKcoll, "CONSUMER_SEQ_NO", msg.get("CONSUMER_SEQ_NO"));
				} else {
					sysKcoll = SFUtil.getTemplateKColl( context, "SYS_HEAD" );
					sysKcoll = (KeyedCollection)sysKcoll.clone();
					SFUtil.setDataValue( context, sysKcoll, "CONSUMER_SEQ_NO", msg.get("CONSUMER_SEQ_NO") );
					SFUtil.addDataElement( context, sysKcoll );
				}
			}
			
			if( SFUtil.isNotEmpty( msg.get( "BIZ_SEQ_NO" ) ) ) {//ҵ����ˮ
				if( context.containsKey( "APP_HEAD" ) ) {
					appKcoll = SFUtil.getDataElement( context, "APP_HEAD" );
					appKcoll = (KeyedCollection)appKcoll.clone();
					SFUtil.setDataValue( context, appKcoll, "BIZ_SEQ_NO", msg.get("BIZ_SEQ_NO"));
				} else {
					appKcoll = SFUtil.getTemplateKColl( context, "APP_HEAD" );
					appKcoll = (KeyedCollection)appKcoll.clone();
					SFUtil.setDataValue( context, appKcoll, "BIZ_SEQ_NO", msg.get("BIZ_SEQ_NO") );
					SFUtil.addDataElement( context, appKcoll );
				}
			}
			
			//���ж˸�ȯ�̶���ת֤���������߼�
			if("100200".equals( txCode )||"200200".equals( txCode )){//100200 - ȯ����ת֤    200200 - ������ת֤
				if(SFUtil.isEmpty( initSide )){
					initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" );
				}
				if( context.containsKey( "APP_HEAD" ) ) {
					appKcoll = SFUtil.getDataElement( context, "APP_HEAD" );
					if(SFConst.INIT_SIDE_ABBANK.equals( initSide )){//�������Ϊ����
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "N" );
					}else{//��������
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "D" );
					}
				}else{
					appKcoll = SFUtil.getTemplateKColl( context, "APP_HEAD" );
					if(SFConst.INIT_SIDE_ABBANK.equals( initSide )){//�������Ϊ����
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "N" );
					}else{//��������
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "D" );
					}
					SFUtil.addDataElement( context, appKcoll );
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SFUtil.addDataField(context, keyColl, "VALIDATION_FLAG","GJFXEGDFXEWHSZRBXGED");//��֤��־VALIDATION_FLAG 
		SFUtil.addDataField(context, keyColl, "OUT_ACCT_NO",msg.get("OUT_ACCT_NO"));//ת���˺�OUT_ACCT_NO 
		SFUtil.addDataField(context, keyColl, "OUT_TOTAL_DETAIL_FLAG",msg.get("OUT_TOTAL_DETAIL_FLAG"));//ת��������ϸ��־OUT_TOTAL_DETAIL_FLAG 
		SFUtil.addDataField(context, keyColl, "OUT_ACCT_USAGE",msg.get("OUT_ACCT_USAGE"));//ת���˻���;OUT_ACCT_USAGE
		SFUtil.addDataField(context, keyColl, "OUT_BRANCH_ID",msg.get("OUT_BRANCH_ID"));//ת���к�OUT_BRANCH_ID
		SFUtil.addDataField(context, keyColl, "IN_ACCT_NO",msg.get("IN_ACCT_NO"));//ת���˺�IN_ACCT_NO
		SFUtil.addDataField(context, keyColl, "IN_TOTAL_DETAIL_FLAG",msg.get("IN_TOTAL_DETAIL_FLAG"));//ת�������ϸ��־IN_TOTAL_DETAIL_FLAG
		SFUtil.addDataField(context, keyColl, "IN_ACCT_USAGE",msg.get("IN_ACCT_USAGE"));//ת���˻���;IN_ACCT_USAGE
		SFUtil.addDataField(context, keyColl, "IN_BRANCH_ID",msg.get("IN_BRANCH_ID"));//ת���к�IN_BRANCH_ID
		SFUtil.addDataField(context, keyColl, "CERT_TYPE",msg.get("CERT_TYPE"));//ƾ֤����CERT_TYPE
		SFUtil.addDataField(context, keyColl, "CERT_NO",msg.get("CERT_NO"));//ƾ֤����CERT_NO
		SFUtil.addDataField(context, keyColl, "ISSUE_DATE",msg.get("ISSUE_DATE"));//ǩ������ISSUE_DATE
		SFUtil.addDataField(context, keyColl, "CANCEL_FLAG",msg.get("CANCEL_FLAG"));//������־CANCEL_FLAG
		SFUtil.addDataField(context, keyColl, "TX_AMOUNT",msg.get("TX_AMOUNT"));//���AMT
		SFUtil.addDataField(context, keyColl, "CUR_CODE",msg.get("CUR_CODE"));//����CCY
		SFUtil.addDataField(context, keyColl, "REMARK",msg.get("REMARK"));//��עREMARK
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_CLIENT_NAME",msg.get("OUT_COUNTER_CLIENT_NAME"));//ת���Է��пͻ�����OUT_COUNTER_CLIENT_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_BANK_NAME",SFConst.SYS_BANK_CNAME);//ת���Է�����������OUT_COUNTER_BANK_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_BRANCH_NAME",msg.get("OUT_COUNTER_BRANCH_NAME"));//ת���Է�������OUT_COUNTER_BRANCH_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_ACCT_NO",msg.get("OUT_COUNTER_ACCT_NO"));//ת���Է����˺�OUT_COUNTER_ACCT_NO
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_CLIENT_NAME",msg.get("IN_COUNTER_CLIENT_NAME"));//ת��Է��пͻ�����IN_COUNTER_CLIENT_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_BANK_NAME",SFConst.SYS_BANK_CNAME);//ת��Է�����������IN_COUNTER_BANK_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_BRANCH_NAME",msg.get("IN_COUNTER_BRANCH_NAME"));//ת��Է��з�������IN_COUNTER_BRANCH_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_ACCT_NO",msg.get("IN_COUNTER_ACCT_NO"));//ת��Է����˺�IN_COUNTER_ACCT_NO
		SFUtil.addDataField(context, keyColl, "STATEMENT_NO",msg.get("STATEMENT_NO"));//���˵���STATEMENT_NO
		SFUtil.addDataField(context, keyColl, "TRAN_TYPE",msg.get("TRAN_TYPE"));//��������TRAN_TYPE
		SFUtil.addDataField(context, keyColl, "TRADER_TYPE_CODE",msg.get("TRADER_TYPE_CODE"));//�̻����ʹ���TRADER_TYPE_CODE
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		if (SFUtil.isNotEmpty(sysKcoll))
			tmpMsg.put("SYS_HEAD",sysKcoll);
		if (SFUtil.isNotEmpty(appKcoll))
			tmpMsg.put("APP_HEAD",appKcoll);
		
		//���ͱ���
		Context msgContext=super.send(context,tmpMsg,"G1001","01001000001_29");
		SFLogger.info(context, "������һ��һ����ת��[G1001]-����");
	    return msgContext;
	}
}
