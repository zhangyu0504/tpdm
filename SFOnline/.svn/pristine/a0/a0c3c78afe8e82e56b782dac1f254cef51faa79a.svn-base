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
 * 810024 资金预警的请求报文体，代理平台发起
 * @author 汪华
 *
 */
public class T810024Client extends CoBankClientBase{

	@Override
	protected Context doHandle(Context context, Map<String, Object> msg,
			String bankNo) throws SFException {
		SFLogger.info(context, "上合作行资金预警-开始");	    
		//构建请求报文
		KeyedCollection inKeyColl = new KeyedCollection("810024_I");
		SFUtil.addDataField(context, inKeyColl, "BANK_ACCT",msg.get("BANK_ACCT"));//联网行账号BankAcct
		SFUtil.addDataField(context, inKeyColl, "ACCT_BAL",msg.get("ACCT_BAL"));//账号余额BankBal
		SFUtil.addDataField(context, inKeyColl, "TOTAL_LIMIT",msg.get("TOTAL_LIMIT"));//总额度SumLim
		SFUtil.addDataField(context, inKeyColl, "USED_LIMIT",msg.get("USED_LIMIT"));//已用额度UseLim
		SFUtil.addDataField(context, inKeyColl, "AVAIL_LIMIT",msg.get("AVAIL_LIMIT"));//可用额度LimBal
		SFUtil.addDataField(context, inKeyColl, "WARN_FLAG",msg.get("WARN_FLAG"));//预警类型WarnFlag
		SFUtil.addDataField(context, inKeyColl, "WARN_MONEY",msg.get("WARN_MONEY"));//预警金额WarnMoney
		SFUtil.addDataElement(context,inKeyColl);
		
//		KeyedCollection outKeyColl = new KeyedCollection("810024_O");
//		SFUtil.addDataField(context, outKeyColl, "AGENT_LOG_NO  ","");//联网行流水号serial_no
//		SFUtil.addDataElement(context,outKeyColl);		
		//发送报文
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("810024_I", inKeyColl);
		Context msgContext=super.send(context,tmpMsg,"810024",bankNo);
		SFLogger.info(context, "上合作行资金预警-结束");
	    return msgContext;
	}

}
