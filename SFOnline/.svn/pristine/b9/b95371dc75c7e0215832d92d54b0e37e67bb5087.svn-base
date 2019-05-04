package module.trans.sf2secu;

import java.util.Map;

import module.communication.SZTSecuClientBase;
import module.communication.SecuClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 预约撤销发送券商（只有深证通:11004）
 * @author  
 *
 */
public class B2SRevocationClient extends SecuClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg) throws SFException {
		if(SFUtil.isNotEmpty(msg.get( "BIZ_SEQ_NO" ))){
			SFUtil.setDataValue( context, SFConst.PUBLIC_MSG_SEQ_NO, msg.get( "BIZ_SEQ_NO" ) );
		}
		//证件类型转换
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2Secu4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}
		Context msgContext = szt11004(context,msg);
		return msgContext;
	}

	/**
	 * 银行端发起预约撤销发送券商
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	private Context szt11004(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "发送报文至深证通银行端发起预约撤销-开始");	
		// 克隆深证通通信临时上下文
		Context msgContext=SFUtil.cloneMsgContextForSZT(context, "Acmt.003.01");	
		KeyedCollection kColl= SFUtil.getDataElement(msgContext, "Acmt00301");
		KeyedCollection custKcoll = SFUtil.getDataElement(msgContext,kColl	, "Cust"); 
		SFUtil.setDataValue(msgContext, custKcoll, "INV_NAME",msg.get("INV_NAME"));
		SFUtil.setDataValue(msgContext, custKcoll, "ID_TYPE_SZT",msg.get("ID_TYPE"));
		SFUtil.setDataValue(msgContext, custKcoll, "INV_ID_CODE",msg.get("INV_ID_CODE"));
		//SFUtil.setDataValue(context, custKcoll, "INV_TYPE_SZT",msg.get("INV_TYPE"));
		//客户类型转换
		Object invType=msg.get("INV_TYPE");
		if(SFUtil.isNotEmpty(invType)){
			String tmpInvType=BizUtil.convSF2SZTInvType(msgContext,invType.toString());
			SFUtil.addDataField(msgContext, kColl, "INV_TYPE_SZT",tmpInvType);
		}
		SFUtil.setDataValue(msgContext, custKcoll, "NATIONALITY",msg.get("NATIONALITY"));
		SFUtil.setDataValue(msgContext, custKcoll, "ADDR",msg.get("ADDR"));
		SFUtil.setDataValue(msgContext, custKcoll, "ZIP",msg.get("ZIP"));
		SFUtil.setDataValue(msgContext, custKcoll, "EMAIL_ADDR",msg.get("EMAIL_ADDR"));
		SFUtil.setDataValue(msgContext, custKcoll, "FAX",msg.get("FAX"));
		SFUtil.setDataValue(msgContext, custKcoll, "MOBILE",msg.get("MOBILE"));
		SFUtil.setDataValue(msgContext, custKcoll, "PHONE",msg.get("PHONE"));
		
		KeyedCollection bkAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "BkAcct");
		SFUtil.setDataValue(msgContext, bkAcctKcoll, "ACCT_ID",msg.get("ACCT_ID"));
		
		KeyedCollection scAcctKcoll = SFUtil.getDataElement(msgContext,kColl	, "ScAcct");
		SFUtil.setDataValue(msgContext, scAcctKcoll, "CAP_ACCT",msg.get("CAP_ACCT"));
		
		KeyedCollection acctSvcrKcoll = SFUtil.getDataElement(msgContext,scAcctKcoll	, "AcctSvcr");
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_TYPE",msg.get("SEC_COMP_TYPE"));
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));
		SFUtil.setDataValue(msgContext, acctSvcrKcoll, "SEC_BRCH_ID",msg.get("SEC_BRCH_ID"));
		
		KeyedCollection scBalKcoll = SFUtil.getDataElement(msgContext,kColl	, "ScBal");
		SFUtil.setDataValue(msgContext, scBalKcoll, "BEGIN_BAL",msg.get("BEGIN_BAL"));
		
		SFUtil.setDataValue(msgContext, kColl,"CUR_CODE",msg.get("CUR_CODE"));
		SFUtil.setDataValue(msgContext, kColl,"LEGAL_NAME",msg.get("LEGAL_NAME"));
		SFUtil.setDataValue(msgContext, kColl,"LEGAL_ID_TYPE",msg.get("LEGAL_ID_TYPE"));

		//发送报文
		SZTSecuClientBase client=new SZTSecuClientBase();
		msgContext=client.send(msgContext,msg,"11004");
		SFLogger.info(context, "发送报文至深证通银行端发起预约撤销-结束");	
		
	    return msgContext;
		 
	}
}
