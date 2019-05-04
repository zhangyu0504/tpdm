package module.communication;

import java.util.Map;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.access.stzsecu.SZTSendAction;
import core.log.SFLogger;

/**
 * 深证通消费端处理类
 * @author 汪华
 *
 */
public final class SZTSecuClientBase{
	public Context send(Context msgContext,Map<String,Object>msg,String msgCode)throws SFException{
		/*
		 * 克隆新的通信上下文
		 */
		//Context msgContext=SFUtil.cloneMsgContext(context, msg);
		try{
			/*
//			 * 上下文获取当前券商信息
//			 */
			SecCompData secCompData =SFUtil.getDataValue(msgContext,SFConst.PUBLIC_SECU);
			//判断券商是否存在
			if(secCompData==null){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
				SFLogger.error(msgContext, String.format("深证通模式执行业务功能码【%s】报文失败，券商不存在！", msgCode));
				return msgContext;
			}
			/*
			 * 发送报文
			 */
			SZTSendAction sender=  new SZTSendAction();
			sender.setSvrCode(msgCode);
			sender.setSecCompCode(secCompData.getSecCompCode());
			String result=sender.doExecute(msgContext);
			if(!"0".equals(result)){
				SFUtil.chkCond(msgContext,SFConst.CTX_ERRCODE_UNKNOWN,SFConst.CTX_ERRMSG_UNKNOWN);
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
			SFLogger.error(msgContext, String.format("深证通模式执行业务功能码【%s】报文失败，%s", msgCode, e.getMessage()));
//			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
//			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
//			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);
			/*
			 * 设置返回值
			 */
			Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
			Map<String,String> tcpipService = tcpipServiceMap.get(msgCode);		
			String outKcollName = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
			outKcollName = outKcollName.replaceAll("\\.", "");
			boolean otherFlag=false;
			if(msgContext.containsKey(outKcollName)){
				KeyedCollection outKcoll = SFUtil.getDataElement(msgContext,outKcollName);
				if(outKcoll!=null&&outKcoll.containsKey("Rst")){
					KeyedCollection rstKcoll = SFUtil.getDataElement(msgContext,outKcoll, "Rst");
					//填充响应结果至公共上下文
					String retCode=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_CODE");
					String retMsg=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_MSG");
					SFLogger.info( msgContext, String.format( "深证通券商应答码【%s】,应答信息【%s】", retCode,retMsg ) );
					SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
					SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
					if(!SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//失败
						SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
					}else if(SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//超时
						SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
					}else{
						otherFlag=true;
					}
				}else{
					otherFlag=true;
				}
			}else{
				otherFlag=true;
			}
			/*
			 * 未知情况都以超时处理
			 */
			if(otherFlag){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
				SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);
				//回写错误
				KeyedCollection kColl = SFUtil.getDataElement(msgContext,outKcollName);
				if(kColl==null){
					kColl=new KeyedCollection(outKcollName);
					KeyedCollection rstKColl=new KeyedCollection("Rst");
					SFUtil.addDataField(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
					SFUtil.addDataField(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
					SFUtil.addDataElement(msgContext, kColl,rstKColl);
					SFUtil.addDataElement(msgContext, kColl);
				}else{
					KeyedCollection rstKColl = SFUtil.getDataElement(msgContext,outKcollName);
					if(rstKColl==null){
						rstKColl=new KeyedCollection("Rst");
						SFUtil.addDataField(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
						SFUtil.addDataField(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
						SFUtil.addDataElement(msgContext, kColl,rstKColl);						
					}
					SFUtil.setDataValue(msgContext,rstKColl,"RESP_CODE",SFConst.CTX_ERRCODE_UNKNOWN);
					SFUtil.setDataValue(msgContext,rstKColl,"RESP_MSG",SFConst.CTX_ERRMSG_UNKNOWN);
				}
			}
			return msgContext;
		}
		/*
		 * 设置返回值
		 */
		
		// 处理body信息
		//String tranCode = SFUtil.getDataValue(msgContext, SFConst.CTX_PUBLIC_TX_CODE);
		
		
		Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
		Map<String,String> tcpipService = tcpipServiceMap.get(msgCode);		
		String outKcollName = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
		outKcollName = outKcollName.replaceAll("\\.", "");
		KeyedCollection outKcoll = SFUtil.getDataElement(msgContext,outKcollName);		
		KeyedCollection rstKcoll = SFUtil.getDataElement(msgContext,outKcoll, "Rst");
		//填充响应结果至公共上下文
		String retCode=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_CODE");
		String retMsg=SFUtil.getDataValue(msgContext,rstKcoll,"RESP_MSG");
		if(SFUtil.isNotEmpty(retCode)){
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);	
		}else{
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, SFConst.CTX_ERRCODE_UNKNOWN);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, SFConst.CTX_ERRMSG_UNKNOWN);			
		}
		SFLogger.info( msgContext, String.format( "深证通券商应答码【%s】,应答信息【%s】", retCode,retMsg ) );
		if(SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)){//成功
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
		}else if(!SFConst.RESPCODE_SUCCCODE_SZTSECU.equals(retCode)&&!SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//失败
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
		}else if(SFConst.RESPCODE_TIMEOUT_SZTSECU.equals(retCode)){//超时
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
		}
		return msgContext;
	}
}
