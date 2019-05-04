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
 * 银转证
 * @author 汪华
 *
 */
public class B2STransferClient extends SecuClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg)
			throws SFException {
		String secuType=SFUtil.getDataValue(context,SFConst.PUBLIC_SECU_TYPE);
		Context msgContext=null;
		if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
			SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		}
		/*if(SFUtil.isNotEmpty( msg.get( "SEC_BRCH_ID" ) )){
			SFUtil.addDataField( context, "SEC_BRCH_ID", msg.get( "SEC_BRCH_ID" )  );
		}*/
		//证件类型转换
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2Secu4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}
		if(SFConst.SECU_ZL.equals(secuType)){//直联
			msgContext=zl6032(context,msg);
		}else if(SFConst.SECU_SZT.equals(secuType)){//深证通
			msgContext=szt12001(context,msg);
		}
		return msgContext;
	}
	
	/**
	 *银转证（业务功能码：12001）
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context szt12001(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "发送报文至深证通银行端银行转证券-开始");	
		// 克隆深证通通信临时上下文
		Context msgContext=SFUtil.cloneMsgContextForSZT(context, "Trf.001.01");			
		KeyedCollection kColl= SFUtil.getDataElement(msgContext, "Trf00101");
		
		if( SFUtil.isNotEmpty( msg.get( "SEC_BRCH_ID" ) ) ) {
			if( !msgContext.containsKey( "MsgHdr" ) ) {
				SFUtil.addFormatToContext( msgContext, "MsgHdr" );
			}
			SFUtil.setDataValue( msgContext, "MsgHdr.Recver.BrchId", msg.get( "SEC_BRCH_ID" ) );
		}
		
		KeyedCollection custKcoll = SFUtil.getDataElement(msgContext,kColl	, "Cust"); 
		SFUtil.setDataValue(msgContext, custKcoll, "INV_NAME",msg.get("INV_NAME"));//Cust/Name
		SFUtil.setDataValue(msgContext, custKcoll, "ID_TYPE_SZT",msg.get("ID_TYPE"));//Cust/CertType
		SFUtil.setDataValue(msgContext, custKcoll, "INV_ID_CODE",msg.get("INV_ID_CODE"));//Cust/CertId
		SFUtil.setDataValue(msgContext, kColl, "RESEND","N");//Resend
		//客户类型转换
		Object invType=msg.get("INV_TYPE");
		if(SFUtil.isNotEmpty(invType)){
			String tmpInvType=BizUtil.convSF2SZTInvType(msgContext,invType.toString());
			SFUtil.setDataValue(msgContext, custKcoll, "INV_TYPE_SZT",tmpInvType);
		}
//		SFUtil.setDataValue(msgContext, custKcoll, "NATIONALITY",msg.get("NATIONALITY"));
//		SFUtil.setDataValue(msgContext, custKcoll, "ADDR",msg.get("ADDR"));
//		SFUtil.setDataValue(msgContext, custKcoll, "ZIP",msg.get("ZIP"));
//		SFUtil.setDataValue(msgContext, custKcoll, "EMAIL_ADDR",msg.get("EMAIL_ADDR"));
//		SFUtil.setDataValue(msgContext, custKcoll, "FAX",msg.get("FAX"));
//		SFUtil.setDataValue(msgContext, custKcoll, "MOBILE",msg.get("MOBILE"));
//		SFUtil.setDataValue(msgContext, custKcoll, "PHONE",msg.get("PHONE"));
		
		KeyedCollection bkAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "BkAcct");
		SFUtil.setDataValue(msgContext, bkAcctKcoll, "ACCT_ID",msg.get("ACCT_ID"));
		
		KeyedCollection scAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "ScAcct");
		SFUtil.setDataValue(msgContext, scAcctKcoll, "CAP_ACCT",msg.get("CAP_ACCT"));
		KeyedCollection acctSvcrKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll	, "AcctSvcr");
//		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_BRCH_ID",msg.get("SEC_BRCH_ID"));
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));
		
		KeyedCollection pwdKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll, "Pwd"); 
		SFUtil.setDataValue(msgContext, pwdKcoll, "CAP_ACCT_PWD",msg.get("CAP_ACCT_PWD"));
		SFUtil.setDataValue(msgContext, kColl,"CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.setDataValue(msgContext, kColl,"TX_AMOUNT",msg.get("TX_AMOUNT"));
		SFUtil.setDataValue(msgContext, kColl,"DGST","银行端银行转证券");
		//银行端银转
		//发送报文
		SZTSecuClientBase client=new SZTSecuClientBase();
		msgContext=client.send(msgContext,msg,"12001");
		SFLogger.info(context, "发送报文至深证通银行端银行转证券-结束");
	    return msgContext;
	}

	/**
	 * 6032 银行端银行转证券
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private Context zl6032(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "直联券商银行端银行转证券-开始");	
		KeyedCollection kColl=new KeyedCollection("6032_I");
		SFUtil.addDataField(context, kColl, "INV_TYPE",msg.get("INV_TYPE"));
		SFUtil.addDataField(context, kColl, "INV_NAME",msg.get("INV_NAME"));
		SFUtil.addDataField(context, kColl, "ID_TYPE_ZL",msg.get("ID_TYPE"));
		SFUtil.addDataField(context, kColl, "INV_ID_CODE",msg.get("INV_ID_CODE"));
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
		Map<String,Object> tmpMsg=new HashMap<String,Object>();
		tmpMsg.put("6032_I", kColl);		
		//发送报文
		ZLSecuClientBase client=new ZLSecuClientBase();
		Context msgContext=client.send(context,tmpMsg,"6032");
		SFLogger.info(context, "直联券商银行端银行转证券-结束");
	   return msgContext;
	}
	
}
