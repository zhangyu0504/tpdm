package core.communication.access.stzsecu;

import java.util.Date;
import java.util.Map;

import module.bean.LocalInfo;
import module.bean.Param;
import module.cache.ParamCache;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.reversal.HostAccessAction;
import com.ecc.emp.format.FormatElement;
import com.sscc.fdep.mrapi;
import common.exception.SFException;
import common.services.PublicService;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.format.xml.XMLWrapFormat;
import core.communication.util.AccessConstance;
import core.log.SFLogger;
/**
 * 深证通发送报文
 * @author 汪华
 *
 */
public class SZTSendAction extends HostAccessAction{
	/**
	 * 超时时间
	 */
	private int timeOut = 40000;
	
	/**
	 * 字符编码
	 */
	private String encoding;
	
	/**
	 * 服务代码
	 */
	private String svrCode;
	
	/**
	 * 券商编号
	 */
	private String userId;
	

	/**
	 * 券商编号
	 */
	private String secCompCode;
	
	public SZTSendAction() {
		super();
	}
	
	
	private Map<String,String> tcpipService = null;
	
	@Override
	public String doExecute(Context context) throws EMPException {
		/*
		 * 初始化:联接深证通服务器
		 */
    	//SZTInitializer.initialize();
		
    	initRequestHeadMsg(context);
    	/*
		 * 按请求报文打包上下文中
		 */
		String reqMsg = getRequestMsg(context);
		/*
		 * 发送报文
		 */
		String pkgId=this.send(context,reqMsg);
		/*
		 * 按应答报文解压上下文中
		 */
		if(!"0".equals(pkgId)){
			if(SFUtil.isNotEmpty(pkgId)){
				handleResponse(context,pkgId);			
			}else{
				return "-1";
			}
		}
		
		return "0";
	}
	
	
	/**
	 * 打包深证通报文
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	public void initRequestHeadMsg(Context context) throws EMPException {
		
		
		
		if(!context.containsKey("MsgHdr")){
			SFUtil.addFormatToContext(context, "MsgHdr");
		}
		
//		FormatElement msgFormat = context.getFormat("MsgHdr");
//		// 增加输出字段到上下文
//		XMLWrapFormat tranCodeO = (XMLWrapFormat) msgFormat
//				.getFormatElement();
//		try {
//			tranCodeO.addFormatToContext(context);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		KeyedCollection kColl= SFUtil.getDataElement(context, "MsgHdr");
		
		SFUtil.setDataValue(context, kColl,"Ver","1.0");
		
		
		String sType = secCompCode.substring(secCompCode.length()-4);
		//取对应系统类型
		if("9999".equals(sType)){
			SFUtil.setDataValue(context, kColl,"SysType","3");
		}else{
			SFUtil.setDataValue(context, kColl,"SysType","0");
		}
		
		SFUtil.setDataValue(context, kColl,"InstrCd",this.svrCode);
		SFUtil.setDataValue(context, kColl,"TradSrc","B");
		
		LocalInfo localInfo = SFUtil.getDataValue(context,SFConst.PUBLIC_LOCAL_INFO);//营业时间信息
		String txDate = localInfo.getWorkdate();//营业日期
		
		SFUtil.setDataValue(context, kColl,"Date",txDate); //DateUtil.getMacDate()
		SFUtil.setDataValue(context, kColl,"Time",DateUtil.getMacTime());
		SFUtil.setDataValue(context, kColl,"LstFrag","Y");
		
		KeyedCollection senderKcoll = SFUtil.getDataElement(context,kColl, "Sender"); 
		
		SFUtil.setDataValue(context, senderKcoll, "InstType","B");
		SFUtil.setDataValue(context, senderKcoll, "InstId","2");
		
		KeyedCollection recverKcoll = SFUtil.getDataElement(context,kColl, "Recver"); 
		SFUtil.setDataValue(context, recverKcoll, "InstType","S");
		SFUtil.setDataValue(context, recverKcoll, "InstId",secCompCode);
		String secBrchId = SFUtil.getDataValue( context, "SEC_BRCH_ID" );
		if(SFUtil.isNotEmpty( secBrchId )){
			SFUtil.setDataValue( context, recverKcoll,"BrchId", secBrchId );
		}
		KeyedCollection refKcoll = SFUtil.getDataElement(context,kColl, "Ref"); 
		
		String ref = SFUtil.getDataValue(context,refKcoll, "Ref");
		
		if(SFUtil.isEmpty(ref)){
//			ref = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,BizUtil.getInitSeqId(context));
			ref = SFUtil.getDataValue(context, SFConst.PUBLIC_MSG_SEQ_NO);
			if(SFUtil.isEmpty(ref)){
				ref =  BizUtil.getInitSeqId(context);
			}
		}		
		
		SFUtil.setDataValue(context, refKcoll, "Ref",ref);
		SFUtil.setDataValue(context, refKcoll, "IssrType","B");
		
		
	}
	
	
	/**
	 * 打包深证通报文
	 * @param context
	 * @return
	 * @throws EMPException
	 */
	public String getRequestMsg(Context context) throws EMPException {
        FormatElement headFormat = context.getFormat("MsgHdr");		
		String responseHeadData = (String)headFormat.format(context);
		
		
		String serverId = SFUtil.getDataValue(context,"MsgHdr.InstrCd");
		Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
		tcpipService = tcpipServiceMap.get(serverId);
		
		
		
		String bodyFormat  = tcpipService.get("formatInput");// ReadProperty.getValue(this.svrCode + "_I");        
		FormatElement msgFormat = context.getFormat(bodyFormat);		
		String responseData = (String)msgFormat.format(context);
		
		if(SFUtil.isEmpty(responseData)){
			throw new SFException("发送深证通报文为空，请检查context！");
		}
		responseData = responseData.trim();
		
		/*
		 * 报文体
		 */		
		int pos = responseData.indexOf(bodyFormat);
		
		String resp1 = responseData.substring(pos-1,bodyFormat.length()+2);
		
		String resp2 = responseData.substring(bodyFormat.length()+3);
		
		String responseBodyData = resp1+"\r\n"+responseHeadData+resp2;
		
		
		/*
		 * 组装发送报文		
		 */
		StringBuffer sendData=new StringBuffer();
		int msgLength=responseBodyData.getBytes().length;//报文体长度
		msgLength=msgLength+105;//报文体加93个字符报文头长度
		String strMsgLength=SFUtil.fixChar(String.valueOf(msgLength), 5, '0', "left");
		
		sendData.append("<IFTS Len=\""+strMsgLength+"\" DataVer=\"1.0.0.1\" SeqNo=\"0\" Type=\"B\" Dup=\"N\" CheckSum=\"\"><MsgText>\r\n");	
		sendData.append(responseBodyData);
		sendData.append("\r\n</MsgText></IFTS>");		
		return sendData.toString();
		
	}
	/**
	 * 主动发送报文
	 * @param sendData
	 * @return
	 * @throws EMPException
	 */
	private String send(Context context,String sendData) throws EMPException{
		SFLogger.info(context,"请求深证通报文=["+sendData+"]");
		Param param = ParamCache.getValue(AccessConstance.SZT_TIMEOUT, "SZT");
		int timeout=Integer.parseInt(param.getValue());
		//挡板检查
		PublicService pubService = ((PublicService) context.getService((String) context.getDataValue(SFConst.SERVICE_PUBLICSERVICENAME)));
		/*
		 * 判断三方往外发报是否存在挡板
		 */
		if(pubService.hasBaffle(context, "4", svrCode,secCompCode,timeout)){
			String reqData = pubService.baffle(context, "4", svrCode,secCompCode,timeout);
			if(SFUtil.isNotEmpty(reqData)){
				//处理请求数据
		    	SZTResponseHandler handler = new SZTResponseHandler();
		    	handler.handleRequest(context,reqData.getBytes(),this.svrCode);
				return "0";
			}else{
				throw new EMPException( "YYPTERROR9999", "SZTSendAction通讯错误：返回报文为空！");
			}
		}
		
		/*
		 * 获取深证通配置
		 */
		SZTAccessParam sztParam=CacheMap.getCache("SZT_ACCESS_PARAM");
		String sourceAppId = sztParam.getClientDestAppId();
		String sourceUserId =sztParam.getClientDestUserId();

		
		/*
		 * 获取券商级深证通配置
		 */
		String destAppId=ParamCache.getValue2("SEC_APP_ID",secCompCode);
		String destUerId=ParamCache.getValue2("SEC_USER_ID",secCompCode);

		
		byte[] sztMessage = sendData.getBytes();
		//发送消息，得到消息包标识pkgid
		String pkgId = mrapi.Mr2Send(sztMessage, sourceUserId, sourceAppId, destUerId, SFUtil.isEmpty(destAppId)?"app1":destAppId,"", "", "", "", "", (byte)0, (byte)0, (byte)0, (byte)0, 2000);
		//如果pkgid字符串为空，代表发送失败。
		if(SFUtil.isEmpty(pkgId))
		{
			SFLogger.info(context,"主动发报失败：调用深证通 Mr2Send方法失败！");
			throw new EMPException("主动发报失败：调用深证通 Mr2Send方法失败！");
		}
//		mrapi.MrDestroy("app2");
		return pkgId;
	}
	/**
	 * 处理应答结果
	 * @param sendData
	 * @param pkgId
	 * @return
	 * @throws EMPException
	 */
	private void handleResponse(Context context,String pkgId) throws EMPException {
		Param param = ParamCache.getValue(AccessConstance.SZT_TIMEOUT, "SZT");
		int timeout=Integer.parseInt(param.getValue());
		long start=(new Date()).getTime();
		while(true){
			long next=(new Date()).getTime();
			if(next-start>timeout){
				SFLogger.info(context,"深证通应答报文超时!");
				
				String outputFormat = tcpipService.get("formatOutput");// ReadProperty.getValue(msgCode+ "_O");		
				
				FormatElement msgFormat = context.getFormat(outputFormat);
				if (msgFormat == null) {
					throw new EMPException("The format:["+ outputFormat+ "] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
				}
				// 增加输出字段到上下文
				XMLWrapFormat tranCodeO = (XMLWrapFormat) msgFormat.getFormatElement();
				try {
					tranCodeO.addFormatToContext(context);
					String outKcollName = outputFormat.replaceAll("\\.", "");
					KeyedCollection outKcoll = SFUtil.getDataElement(context,outKcollName);		
					KeyedCollection rstKcoll = SFUtil.getDataElement(context,outKcoll, "Rst");
					SFUtil.setDataValue(context,rstKcoll,"RESP_CODE",SFConst.RESPCODE_TIMEOUT_SZTSECU);
					SFUtil.setDataValue(context,rstKcoll,"RESP_MSG","深证通应答报文超时");
				} catch (Exception e) {
					SFLogger.error(context, "深证通应答报文失败:",e);
				}
				throw new EMPException("深证通应答报文超时");
			}
			
			SZTAccessParam sztParam = CacheMap.getCache("SZT_ACCESS_PARAM");
			
			String clientAppId =  sztParam.getClientAppId();//
			SFLogger.info(context,"SZTSendAction appId=["+clientAppId+"],Mr2CreatePkgID=[" + pkgId + "]");		
			// 发送消息，得到消息包标识pkgid
			byte[] rspData = mrapi.Mr2Receive1(clientAppId, "", "", "", "", "",pkgId, "", "", 2000);
			String result = new String(rspData);	
	        if(result.length()>9)
	        {
	        	SFLogger.info(context, "深证通应答报文:[" + result+"]^");
				//处理请求数据
		    	SZTResponseHandler handler = new SZTResponseHandler();
		    	handler.handleRequest(context,rspData,this.svrCode);
		    	break;  	
	        }else{
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					SFLogger.error(context, "深证通等待失败:",e);
				}
	        }
		}
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSvrCode() {
		return svrCode;
	}

	public void setSvrCode(String svrCode) {
		this.svrCode = svrCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSecCompCode() {
		return secCompCode;
	}

	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}

	
}
