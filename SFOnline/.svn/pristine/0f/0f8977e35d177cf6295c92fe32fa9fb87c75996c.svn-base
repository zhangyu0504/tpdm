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
 * 810026 代理平台发起一步式签约交易，合作行返回账户信息以及账户状态
 * @author 汪华
 *
 */
public class T810026Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "上合作行一步式签约交易-开始");	
		//证件类型转换
		Object idType=msg.get("ID_TYPE");
		if(SFUtil.isNotEmpty(idType)){
			idType=BizUtil.convSF2CoBank4IdType(context,idType.toString());
			msg.put("ID_TYPE", idType);
		}    
		//构建请求报文
		KeyedCollection inKeyColl = new KeyedCollection("810026_I");
		SFUtil.addDataField(context, inKeyColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));//券商代码SecCode
		SFUtil.addDataField(context, inKeyColl, "CAP_ACCT",msg.get("CAP_ACCT"));//证券资金台账号CapAcct
		SFUtil.addDataField(context, inKeyColl, "INV_NAME",msg.get("INV_NAME"));//券商端客户名称InvName
		SFUtil.addDataField(context, inKeyColl, "ID_TYPE",msg.get("ID_TYPE"));//券商端证件类型IdType
		SFUtil.addDataField(context, inKeyColl, "INV_ID_CODE",msg.get("INV_ID_CODE"));//券商端证件号码InvIdCode
		SFUtil.addDataField(context, inKeyColl, "ACCT_ID",msg.get("ACCT_ID"));//银行账号account_no
		SFUtil.addDataField(context, inKeyColl, "CUR_CODE",msg.get("CUR_CODE"));//币种CurCode
		SFUtil.addDataField(context, inKeyColl, "REMARK",msg.get("REMARK"));//备注Memo
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810026_O");
//		SFUtil.addDataField(context, outKeyColl, "ACCT_ID","");//银行账户Account
//		SFUtil.addDataField(context, outKeyColl, "INV_NAME","");//银行端客户名称InvName
//		SFUtil.addDataField(context, outKeyColl, "ID_TYPE","");//银行端证件类型IdType
//		SFUtil.addDataField(context, outKeyColl, "INV_ID_CODE","");//银行端证件号码InvIdCode
//		SFUtil.addDataField(context, outKeyColl, "STATUS","");//账户状态Status
//		SFUtil.addDataField(context, outKeyColl, "BRANCH_ID","");//分行号
//		SFUtil.addDataField(context, outKeyColl, "BANK_ID","");//合作行号
//		SFUtil.addDataField(context, outKeyColl, "REMARK","");//备注Memo
//		SFUtil.addDataElement(context,outKeyColl);
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810026_I", inKeyColl);	
		//发送报文
		Context msgContext=super.send(context,tmpMsg,"810026",bankNo);
		SFLogger.info(context, "上合作行一步式签约交易-结束");
	    return msgContext;
	}

}
