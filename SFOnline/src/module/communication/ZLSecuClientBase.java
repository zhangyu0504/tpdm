package module.communication;

import java.util.Map;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.access.zlsecu.TCPIPSendAction;
import core.communication.format.KeyedFormat;
import core.log.SFLogger;
/**
 * 直联券商消费端处理类
 * @author 汪华
 *
 */
public final class ZLSecuClientBase {
	public Context send(Context context,Map<String,Object>msg,String msgCode)throws SFException{
		/*
		 * 克隆新的通信上下文
		 */
		Context msgContext=SFUtil.cloneMsgContext(context, msg);
		try{
			/*
			 * 上下文获取当前券商信息
			 */
			SecCompData secCompData = SFUtil.getDataValue(context,SFConst.PUBLIC_SECU); // (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
			//判断券商是否存在
			if(secCompData==null){
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
				SFLogger.error(msgContext, String.format("直联券商执行【%s】报文失败，券商【不存在！", msgCode));
				return msgContext;
			}
			/*
			 * 发送报文
			 */
			TCPIPSendAction access=  new TCPIPSendAction();
			access.setHostIP(secCompData.getIp());
			access.setHostPort(secCompData.getPort());
			access.setSvrCode(msgCode);
			access.setSecCompCode(secCompData.getSecCompCode());
			access.setUserId(secCompData.getUserId());
			String result=access.doExecute(msgContext);
			if(!"0".equals(result)){
				SFUtil.chkCond( context,SFConst.CTX_ERRCODE_UNKNOWN,SFConst.CTX_ERRMSG_UNKNOWN);
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
			SFLogger.error(msgContext, String.format("直联券商执行【%s】报文失败，%s", msgCode, e.getMessage()));
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			/*
			 * 设置返回值
			 */
			String outputName = msgCode + "_O";
			FormatElement outputFormat = context.getFormat(outputName);
			KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
			String outKcollName = tranCodeO.getKcollName();
			//填充响应结果至公共上下文
			String retCode=SFUtil.getDataValue(msgContext,outKcollName+".RESP_CODE");
			String retMsg=SFUtil.getDataValue(msgContext,outKcollName+"RESP_MSG");
			SFLogger.info( msgContext, String.format( "直联券商应答码【%s】,应答信息【%s】", retCode,retMsg ) );
			String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//手工设置超时
			if(SFUtil.isNotEmpty(retCode)){
				if(retCode.startsWith("M")){//成功
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
				}else if(retCode.startsWith("R") || "E1011".equals(retCode)
						|| "E9999".equals(retCode)
						||SFConst.RET_OVERTIME.equals(retFlag)){//超时
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
					retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
					retMsg="券商响应报文失败";
				}else{
					SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
				}
			}else{
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.CTX_ERRCODE_UNKNOWN;
				retMsg=SFConst.CTX_ERRMSG_UNKNOWN;
				//回写错误
				KeyedCollection kColl=SFUtil.getDataElement(msgContext,outKcollName);
				if(kColl==null){
					kColl=new KeyedCollection(outKcollName);
					SFUtil.addDataField(msgContext,kColl,"RESP_CODE",retCode);
					SFUtil.addDataField(msgContext,kColl,"RESP_MSG",retMsg);
					SFUtil.addDataElement(msgContext, kColl);
				}else{
					SFUtil.setDataValue(msgContext,kColl,"RESP_CODE",retCode);
					SFUtil.setDataValue(msgContext,kColl,"RESP_MSG",retMsg);
				}
			}
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
			SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);
			return msgContext;
		}
		/*
		 * 设置返回值
		 */
		String outputName = msgCode + "_O";
		FormatElement outputFormat = context.getFormat(outputName);
		KeyedFormat tranCodeO = (KeyedFormat)outputFormat.getFormatElement();
		String outKcollName = tranCodeO.getKcollName();
		
		if(!msgContext.containsKey( outKcollName )){
			try {
				tranCodeO.addFormatToContext(msgContext);
			} catch( Exception e ) {
				SFLogger.error(msgContext, String.format("直联券商执行【%s】报文失败，%s", msgCode, e.getMessage()));
			}
		}
		
//		String tranCode = SFUtil.getDataValue(msgContext, SFConst.PUBLIC_TX_CODE);
		//填充响应结果至公共上下文
		String retCode=SFUtil.getDataValue(msgContext,outKcollName+".RESP_CODE");
		String retMsg=SFUtil.getDataValue(msgContext,outKcollName+".RESP_MSG");
		SFLogger.info( msgContext, String.format( "直联券商应答码【%s】,应答信息【%s】", retCode,retMsg ) );
		String retFlag=SFUtil.getDataValue(context,SFConst.PUBLIC_RET_FLAG);//手工设置超时
		if(SFUtil.isNotEmpty(retCode)){
			if(retCode.startsWith("M")){//成功
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_SUCCESS);
			}else if(retCode.startsWith("R") 
					|| "E1011".equals(retCode)
					|| "E9999".equals(retCode)
					||SFConst.RET_OVERTIME.equals(retFlag)){//超时
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
				retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
				retMsg="券商响应报文失败";
			}else{
				SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);//增加errorMSG参数
			}
		}else{
			SFUtil.setDataValue(msgContext,SFConst.PUBLIC_RET_FLAG, SFConst.RET_OVERTIME);
			retCode=SFConst.RESPCODE_TIMEOUT_ZLSECU;
			retMsg="券商响应报文失败";
		}
		
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRCODE, retCode);
		SFUtil.setDataValue(msgContext, SFConst.CTX_ERRMSG, retMsg);		
		//直联返回码取后4位
		if(SFUtil.isNotEmpty(retCode)){
			SFUtil.setDataValue(msgContext,outKcollName+".RESP_CODE",retCode.length()>3?retCode.substring( retCode.length()-4 ):retCode);
			SFUtil.setDataValue(msgContext,outKcollName+".RESP_MSG",retMsg);

		}
	
		
		
		
		return msgContext;
	}
}
