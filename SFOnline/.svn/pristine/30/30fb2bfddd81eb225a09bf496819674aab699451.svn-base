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
 * 810025 查询联网行客户帐户余额的请求报文体，代理平台发起
 * @author 汪华
 *
 */
public class T810025Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "上合作行查询联网行客户帐户余额-开始");	    
		//构建请求报文
		KeyedCollection inKeyColl = new KeyedCollection("810025_I");
		SFUtil.addDataField(context, inKeyColl, "ACCT_ID",msg.get("ACCT_ID"));//银行账号account_no
		SFUtil.addDataField(context, inKeyColl, "SEC_COMP_CODE",msg.get("SEC_COMP_CODE"));//券商代码SecCode
		SFUtil.addDataField(context, inKeyColl, "CAP_ACCT",msg.get("CAP_ACCT"));//证券资金台账号StkAcct
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810025_O");
//		SFUtil.addDataField(context, outKeyColl, "ACCT_ID","");//银行账号account_no
//		SFUtil.addDataField(context, outKeyColl, "INV_NAME","");//客户姓名CustName
//		SFUtil.addDataField(context, outKeyColl, "SEC_COMP_CODE","");//券商代码SecCode
//		SFUtil.addDataField(context, outKeyColl, "CAP_ACCT","");//证券资金台账号StkAcct
//		SFUtil.addDataField(context, outKeyColl, "ACCT_BAL","");//账户余额Amount
//		SFUtil.addDataElement(context,outKeyColl);
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810025_I", inKeyColl);	
		//发送报文
		Context msgContext=super.send(context,tmpMsg,"810025",bankNo);
		SFLogger.info(context, "上合作行查询联网行客户帐户余额-结束");
	    return msgContext;
	}

}
