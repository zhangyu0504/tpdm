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
 * 一对一活期转账:G1001
 * @author 汪华
 *
 */
public class TransferClient extends ESBClientBase{
	
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "上主机一对一活期转账[G1001]-开始");
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		KeyedCollection sysKcoll = null;
		KeyedCollection appKcoll = null;
		//if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
		//	SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		//}
		try {
			//从上下文中取得交易类型
			String initSide = SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE );
			//从上下文中取出交易码
			String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );

			/*KeyedCollection sysHeadColl = SFUtil.getTemplateKColl(context, "SYS_HEAD");
			SFUtil.addDataField(context, sysHeadColl, "SYS_HEAD.CONSUMER_SEQ_NO","1111111111111111111");
			SFUtil.addDataElement( context, sysHeadColl );*/
			if( SFUtil.isNotEmpty( msg.get( "CONSUMER_SEQ_NO" ) ) ) {//交易流水
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
			
			if( SFUtil.isNotEmpty( msg.get( "BIZ_SEQ_NO" ) ) ) {//业务流水
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
			
			//银行端跟券商端银转证进入特殊逻辑
			if("100200".equals( txCode )||"200200".equals( txCode )){//100200 - 券商银转证    200200 - 银行银转证
				if(SFUtil.isEmpty( initSide )){
					initSide = SFUtil.getReqDataValue( context, "INIT_SIDE" );
				}
				if( context.containsKey( "APP_HEAD" ) ) {
					appKcoll = SFUtil.getDataElement( context, "APP_HEAD" );
					if(SFConst.INIT_SIDE_ABBANK.equals( initSide )){//如果渠道为柜面
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "N" );
					}else{//其他渠道
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "D" );
					}
				}else{
					appKcoll = SFUtil.getTemplateKColl( context, "APP_HEAD" );
					if(SFConst.INIT_SIDE_ABBANK.equals( initSide )){//如果渠道为柜面
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "N" );
					}else{//其他渠道
						SFUtil.setDataValue( context, appKcoll, "TRANT_FLAG", "D" );
					}
					SFUtil.addDataElement( context, appKcoll );
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SFUtil.addDataField(context, keyColl, "VALIDATION_FLAG","GJFXEGDFXEWHSZRBXGED");//验证标志VALIDATION_FLAG 
		SFUtil.addDataField(context, keyColl, "OUT_ACCT_NO",msg.get("OUT_ACCT_NO"));//转出账号OUT_ACCT_NO 
		SFUtil.addDataField(context, keyColl, "OUT_TOTAL_DETAIL_FLAG",msg.get("OUT_TOTAL_DETAIL_FLAG"));//转出汇总明细标志OUT_TOTAL_DETAIL_FLAG 
		SFUtil.addDataField(context, keyColl, "OUT_ACCT_USAGE",msg.get("OUT_ACCT_USAGE"));//转出账户用途OUT_ACCT_USAGE
		SFUtil.addDataField(context, keyColl, "OUT_BRANCH_ID",msg.get("OUT_BRANCH_ID"));//转出行号OUT_BRANCH_ID
		SFUtil.addDataField(context, keyColl, "IN_ACCT_NO",msg.get("IN_ACCT_NO"));//转入账号IN_ACCT_NO
		SFUtil.addDataField(context, keyColl, "IN_TOTAL_DETAIL_FLAG",msg.get("IN_TOTAL_DETAIL_FLAG"));//转入汇总明细标志IN_TOTAL_DETAIL_FLAG
		SFUtil.addDataField(context, keyColl, "IN_ACCT_USAGE",msg.get("IN_ACCT_USAGE"));//转入账户用途IN_ACCT_USAGE
		SFUtil.addDataField(context, keyColl, "IN_BRANCH_ID",msg.get("IN_BRANCH_ID"));//转入行号IN_BRANCH_ID
		SFUtil.addDataField(context, keyColl, "CERT_TYPE",msg.get("CERT_TYPE"));//凭证类型CERT_TYPE
		SFUtil.addDataField(context, keyColl, "CERT_NO",msg.get("CERT_NO"));//凭证号码CERT_NO
		SFUtil.addDataField(context, keyColl, "ISSUE_DATE",msg.get("ISSUE_DATE"));//签发日期ISSUE_DATE
		SFUtil.addDataField(context, keyColl, "CANCEL_FLAG",msg.get("CANCEL_FLAG"));//撤销标志CANCEL_FLAG
		SFUtil.addDataField(context, keyColl, "TX_AMOUNT",msg.get("TX_AMOUNT"));//金额AMT
		SFUtil.addDataField(context, keyColl, "CUR_CODE",msg.get("CUR_CODE"));//币种CCY
		SFUtil.addDataField(context, keyColl, "REMARK",msg.get("REMARK"));//备注REMARK
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_CLIENT_NAME",msg.get("OUT_COUNTER_CLIENT_NAME"));//转出对方行客户名称OUT_COUNTER_CLIENT_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_BANK_NAME",SFConst.SYS_BANK_CNAME);//转出对方行银行名称OUT_COUNTER_BANK_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_BRANCH_NAME",msg.get("OUT_COUNTER_BRANCH_NAME"));//转出对方行行名OUT_COUNTER_BRANCH_NAME
		SFUtil.addDataField(context, keyColl, "OUT_COUNTER_ACCT_NO",msg.get("OUT_COUNTER_ACCT_NO"));//转出对方行账号OUT_COUNTER_ACCT_NO
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_CLIENT_NAME",msg.get("IN_COUNTER_CLIENT_NAME"));//转入对方行客户名称IN_COUNTER_CLIENT_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_BANK_NAME",SFConst.SYS_BANK_CNAME);//转入对方行银行名称IN_COUNTER_BANK_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_BRANCH_NAME",msg.get("IN_COUNTER_BRANCH_NAME"));//转入对方行分行名称IN_COUNTER_BRANCH_NAME
		SFUtil.addDataField(context, keyColl, "IN_COUNTER_ACCT_NO",msg.get("IN_COUNTER_ACCT_NO"));//转入对方行账号IN_COUNTER_ACCT_NO
		SFUtil.addDataField(context, keyColl, "STATEMENT_NO",msg.get("STATEMENT_NO"));//对账单号STATEMENT_NO
		SFUtil.addDataField(context, keyColl, "TRAN_TYPE",msg.get("TRAN_TYPE"));//交易类型TRAN_TYPE
		SFUtil.addDataField(context, keyColl, "TRADER_TYPE_CODE",msg.get("TRADER_TYPE_CODE"));//商户类型代码TRADER_TYPE_CODE
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		if (SFUtil.isNotEmpty(sysKcoll))
			tmpMsg.put("SYS_HEAD",sysKcoll);
		if (SFUtil.isNotEmpty(appKcoll))
			tmpMsg.put("APP_HEAD",appKcoll);
		
		//发送报文
		Context msgContext=super.send(context,tmpMsg,"G1001","01001000001_29");
		SFLogger.info(context, "上主机一对一活期转账[G1001]-结束");
	    return msgContext;
	}
}
