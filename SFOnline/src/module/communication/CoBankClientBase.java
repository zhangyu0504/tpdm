package module.communication;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import module.bean.AgtAgentInfo;
import module.bean.Param;
import module.dao.AgtAgentInfoDao;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;
import common.util.TransflowLimit;

import core.cache.CacheMap;
import core.communication.access.tcpip.TCPIPSendAction;
import core.log.SFLogger;
/**
 * 合作行消费端处理类
 * @author 汪华
 *
 */
public abstract class CoBankClientBase {
	
	/**
	 * 发送报文统一入口
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,Map<String,Object>msg,String bankNo)throws SFException{
		//私有业务处理方法：组装输入报文，定义服务号与场景号
		return doHandle(context,msg,bankNo);
	}
	
	
	/**
	 * 实际发送报文
	 * @param context
	 * @param msg
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected Context send(Context context,Map<String,Object>msg,String msgCode,String bankNo)throws SFException{
		
		Context msgContext = null;
		AgtAgentInfo agtAgent  = null;
		try {
			/*
			 * 克隆新的通信上下文
			*/
			msgContext=SFUtil.cloneMsgContext(context, msg);
			agtAgent  = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );//从上下文中取出合作行对象
			if(agtAgent == null){
				Connection tranConnection=SFUtil.getDataValue(context,SFConst.PUBLIC_TRAN_CONNECTION);//从上下文中获取数据库联接，不需要私有交易关闭联接
				agtAgent  = new AgtAgentInfoDao().qryAgtAgentInfo(context, tranConnection, bankNo);
				//判断合作行是否存在
				if(agtAgent==null){
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
					SFLogger.error(msgContext, String.format("上合作行执行【%s】报文失败，合作行【%s】不存在！", msgCode, bankNo));
					return msgContext;
				}
			}
		} catch (Exception e) {
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
			SFLogger.error(msgContext, String.format("上合作行执行【%s】报文失败，%s", msgCode, e.getMessage()));
			return msgContext;
		}
		
		
		try{
			//报文发送前增加合作行限流控制
			msgContext = TransflowLimit.sendCoBankFlow (msgContext,bankNo);
			if (SFConst.RET_FAILURE.equals(SFUtil.getDataValue(msgContext, SFConst.PUBLIC_RET_FLAG)) && 
				SFConst.RESPCODE_FAILCODE_COBANK.equals(SFUtil.getDataValue(msgContext,"A_RESPONSE_HEAD.RESPCODE"))){
				return msgContext;
			}
			/*
			 * 发送报文
			 */
			TCPIPSendAction access=  new TCPIPSendAction();
			access.setHostIP(agtAgent.getBankIp());
			access.setHostPort(agtAgent.getBankPort());
			access.setSvrCode(msgCode);
			access.setBankNo(bankNo);
			String result=access.doExecute(msgContext);
			if(!"0".equals(result)){
				SFUtil.chkCond( msgContext,SFConst.CTX_ERRCODE_UNKNOWN,SFConst.CTX_ERRMSG_UNKNOWN);
			}
			
		}catch(Exception e){
			/*
			 * 所有不明异常都做超时处理
			 * <p>
			 * 1、无法建立连接
			 * 2、发送超时
			 * 3、接收超时
			 * 4、SOCKET 或IO出现异常
			 * 5、其他错误（包含无法解析返回报文）
			 */
			SFLogger.error(msgContext, String.format("上合作行执行【%s】报文失败，%s", msgCode, e.getMessage()));
			/*
			 * 设置返回值
			 */
			//填充响应结果至公共上下文
			String retCode = SFUtil.getDataValue(msgContext,"A_RESPONSE_HEAD.RESPCODE");//从响应报文中取响应码
			String retMsg= SFUtil.getDataValue(msgContext,"A_RESPONSE_HEAD.RESPMSG");//从响应报文中取响应信息
			SFLogger.info( context, String.format( "合作行应答码【%s】,应答信息【%s】", retCode,retMsg ) );
			String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//手工设置超时
			if(SFConst.RESPCODE_TIMEOUT_COBANK.equals(retCode)||SFConst.RET_OVERTIME.equals(retFlag)){//超时
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.RESPCODE_TIMEOUT_COBANK;
				retMsg="合作行响应报文失败";
			}
//			else if(SFUtil.isNotEmpty(retCode)){
//				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
//				if(SFUtil.isEmpty(retCode)||SFConst.RESPCODE_SUCCCODE_COBANK.equals(retCode)){
//					retCode=SFConst.CTX_ERRCODE_UNKNOWN;
//					retMsg=SFConst.CTX_ERRMSG_UNKNOWN;
//				}				
//			}
			else{
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.CTX_ERRCODE_UNKNOWN;
				retMsg=SFConst.CTX_ERRMSG_UNKNOWN;
			}
			
			/*
			 * 回写错误
			 */
			KeyedCollection kColl=SFUtil.getDataElement(msgContext,"A_RESPONSE_HEAD");
			if(kColl==null){
				kColl=new KeyedCollection("A_RESPONSE_HEAD");
				SFUtil.addDataField(msgContext,kColl,"RESPCODE",retCode);
				SFUtil.addDataField(msgContext,kColl,"RESPMSG",retMsg);
				SFUtil.addDataElement(msgContext, kColl);
			}else{
				SFUtil.setDataValue(msgContext,kColl,"RESPCODE",retCode);
				SFUtil.setDataValue(msgContext,kColl,"RESPMSG",retMsg);
			}
			
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
			return msgContext;
		} finally{
			//请求结束时释放在线计录数
			TransflowLimit.releaseFlowLimit(context,bankNo,TransflowLimit.COBANK_SEND_FLOW);
		}
		/*
		 * 设置返回值
		 */
		//填充响应结果至公共上下文
		String retCode = SFUtil.getDataValue(msgContext,"A_RESPONSE_HEAD.RESPCODE");//从响应报文中取响应码
		String retMsg= SFUtil.getDataValue(msgContext,"A_RESPONSE_HEAD.RESPMSG");//从响应报文中取响应信息
		SFLogger.info( context, String.format( "合作行应答码【%s】,应答信息【%s】", retCode,retMsg ) );
		String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//手工设置超时
		if(SFConst.RESPCODE_SUCCCODE_COBANK.equals(retCode)){//成功
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
		}else if(!SFConst.RET_OVERTIME.equals(retFlag)
				&&!SFConst.RESPCODE_SUCCCODE_COBANK.equals(retCode)
				&&!SFConst.RESPCODE_TIMEOUT_COBANK.equals(retCode)){//失败
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
		}else if(SFConst.RESPCODE_TIMEOUT_COBANK.equals(retCode)||SFConst.RET_OVERTIME.equals(retFlag)){//超时
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			//SFUtil.setDataValue(msgContext,"A_RESPONSE_HEAD.RESPMSG",SFConst.RESPCODE_TIMEOUT_COBANK);//从响应报文中取响应信息
			// SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG,"合作行响应报文失败");//手工设置超时
			retCode=SFConst.RESPCODE_TIMEOUT_COBANK;
			retMsg="合作行响应报文失败";
		}
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
		return msgContext;
	}
	
	/**
	 * 私有业务处理方法：组装输入报文，定义服务号与场景号
	 * @param context
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected abstract Context doHandle(Context context,Map<String,Object>msg,String bankNo)throws SFException;

}
