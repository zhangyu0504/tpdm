package module.trans.sf2bankchl;

import java.util.HashMap;
import java.util.Map;

import module.communication.ESBClientBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;

import core.log.SFLogger;
/**
 * 上卡管查询卡类型和等级
 * @author 汪华
 *
 */
public class QryCardTypeClient extends ESBClientBase{
	
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "上卡管系统查询卡类型[BCARD93432]-开始");
		//发送报文
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, keyColl, "ACCT_ID",msg.get("ACCT_ID"));//账号
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		Context msgContext=super.send(context,tmpMsg,"BCARD93432","04003000003_04");
		SFLogger.info(context, "上卡管系统查询卡类型[BCARD93432]-结束");
	    return msgContext;
	}
}
