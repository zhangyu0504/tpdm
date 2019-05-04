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
 *查询余额：R3034=零售账户余额查询;C3019=对公活期账户余额及利息查询
 * @author 汪华
 *
 */
public class QryBalClient extends ESBClientBase{
	protected Context doHandle(Context context,Map<String,Object>msg)throws SFException{
		Context msgContext=null;
		String invType=SFUtil.getDataValue(context, SFConst.PUBLIC_INV_TYPE);// 客户类型
		if(SFConst.INV_TYPE_RETAIL.equals(invType)){// 零售
			msgContext=this.qryRetailBal(context,msg);
		}else if(SFConst.INV_TYPE_CORP.equals(invType)){// 对公
			msgContext=this.qryCorpBal(context,msg);
		}		
	    return msgContext;
	}
	
	/**
	 * 11003000003.45 R3034 账户余额查询
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public Context qryRetailBal(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "上主机账户余额查询[R3034]-开始");	    
		//发送报文
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, keyColl, "ACCT_ID",msg.get("ACCT_ID"));//账号
		SFUtil.addDataField(context, keyColl, "CUR_CODE",msg.get("CUR_CODE"));//币种
		SFUtil.addDataField(context, keyColl, "INV_NAME",msg.get("INV_NAME"));//账户名称
		SFUtil.addDataField(context, keyColl, "QUERY_FLAG","N");//查询标志
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		Context msgContext=super.send(context,tmpMsg,"R3034","11003000003_45");
		SFLogger.info(context, "上主机账户余额查询[R3034]-结束");
	    return msgContext;
	}
	
	
	/**
	 * 11003000003.47 C3019 对公活期账户余额及利息查询
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public Context qryCorpBal(Context context,Map<String,Object>msg)throws SFException{
		SFLogger.info(context, "上主机对公活期账户余额及利息查询[C3019]-开始");	
		//发送报文
		//发送报文
		KeyedCollection keyColl = new KeyedCollection("MSG_I");
		SFUtil.addDataField(context, keyColl, "ACCT_ID",msg.get("ACCT_ID"));//账号
		SFUtil.addDataField(context, keyColl, "CUR_CODE",msg.get("CUR_CODE"));//币种
		Map<String,Object> tmpMsg = new HashMap<String,Object>();
		tmpMsg.put("MSG_I",keyColl);
		Context msgContext=super.send(context,tmpMsg,"C3019","11003000003_47");
		SFLogger.info(context, "上主机对公活期账户余额及利息查询[C3019]-结束");
	    return msgContext;
	}
}
