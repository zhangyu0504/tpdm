package core.communication.access.zlsecu;

import java.sql.Connection;
import java.util.Map;

import module.bean.Param;
import module.cache.ParamCache;
import module.trans.TranHandler;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.access.tcpip.TCPIPRequestService;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataUtility;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.transaction.EMPTransactionManager;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

/**
 * EMP提供的TCP/IP渠道接入访问服务的实现。
 * <p>s
 * 可将EMP业务逻辑构件开放为供TCP/IP渠道接入访问的服务，
 * 使得通过TCP/IP渠道接入的系统可以通过定义输出输出报文的方式访问EMP的业务处理逻辑。
 * <p>
 * 可以有一个或多个定义包含在TcpipAccessServletContext配置文件中，参数如下：
 * <p>
 * id：TCP/IP Service的id，通过指定此id对Service进行访问<br>
 * serviceName：id的等价参数<br>
 * serviceType：Service访问类型，可选项有session(创建会话)、endSession(结束会话)以及其它(默认，普通请求)<br>
 * sessionContextName：当serviceType为session时有效，指定要创建会话的业务逻辑Context名称<br>
 * checkSession：该请求是否检查会话，可选项true(默认)、false<br>
 * EMPFlowId：该Service对应的EMP业务逻辑构件名<br>
 * opId：该Service对应的EMP业务逻辑构件中的Operation ID<br>
 * description：描述信息<br>
 * encoding：报文体编码<br>
 * enabled：该Service是否启用，可选项true(默认)、false<br>
 * <p>
 * 子元素：
 * <p>
 * requestDataFormat：请求报文体格式处理器<br>
 * responseDataFormat：响应报文体格式处理器
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-26
 * @lastmodified 2008-6-11
 * @emp:name TCP/IP渠道接入访问服务
 * @emp:document EMP提供的TCP/IP渠道接入访问服务，可访问EMP业务逻辑构件
 */
public class PBankTCPIPRequestService implements TCPIPRequestService{

	/**
	 * serviceType的取值之一，代表执行该请求前需要创建会话
	 */
	public static int SESSION_SERVICE = 0;

	/**
	 * serviceType的取值之一，代表执行该请求后需要销毁会话
	 */
	public static int END_SESSION_SERVICE = 1;

	/**
	 * serviceType的取值之一，代表执行该请求时若存在会话，则使用当前会话，否则是无会话请求
	 */
	public static int NORMAL_SERVICE = 2;

	/**
	 * TCP/IP Service的id，通过在请求数据中指定此id对Service进行访问
	 */
	protected String serviceName;

	/**
	 * 该请求是否检查会话
	 */
	protected boolean checkSession = true;

	/**
	 * TCP/IP Service访问类型
	 */
	protected int serviceType = NORMAL_SERVICE;

	/**
	 * 当请求为创建会话时有效，指定要创建会话的Session Context名
	 */
	protected String sessionContextName;

	/**
	 * 该Service对应的EMP业务逻辑构件名
	 */
	protected String EMPFlowId;

	/**
	 * 该Service对应的EMP业务逻辑构件中的Operation ID
	 */
	protected String opId;
	
	/**
	 * 请求报文体格式处理器
	 */
	private FormatElement requestDataFormat;

	/**
	 * 响应报文体格式处理器
	 */
	private FormatElement responseDataFormat;

	/**
	 * EMP业务逻辑构件实例化工厂
	 */
	protected EMPFlowComponentFactory factory = null;

	/**
	 * 该Service是否启用
	 */
	private boolean enabled = true;
	
	/**
	 * 报文体编码
	 */	
	protected String encoding = null;
	
	/**
	 * 该接口所处理的输入输出KeyedCollection。
	 */
	protected String inOutDataArea = null;

	/**
	 * 该Service对应的TranCode
	 */
	protected String tranCode = null;
	
	/**
	 * 是否是转发报文   如是 通过ESB转发给管理台
	 */
	
	protected String isTranSpond = null;
	
	/**
	 * 转发ESB的formatId
	 */
	protected String transformatId = null;
	

	public String getTransformatId() {
		return transformatId;
	}

	public void setTransformatId(String transformatId) {
		this.transformatId = transformatId;
	}



	public String getIsTranSpond() {
		return isTranSpond;
	}

	public void setIsTranSpond(String isTranSpond) {
		this.isTranSpond = isTranSpond;
	}

	public String getTranCode() {
		return tranCode;
	}

	public void setTranCode(String tranCode) {
		this.tranCode = tranCode;
	}

	public PBankTCPIPRequestService() {
		super();
	}
	
	
	protected String className = null;
	
	
	
	

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 具体的请求处理实现方法。
	 * 
	 * @param request TCP/IP请求
	 * @param sessionContext Session Context
	 * @return 返回内容，即打包好的报文体
	 * @throws EMPException
	 */
	public byte[] handleRequest(EMPTCPIPRequest request, Context sessionContext ) throws EMPException {
		Context flowContext = null;
		try {
			flowContext = SFUtil.getRootContext();
			byte[] reqMsg = (byte[])request.getAttribute("reqData"); // getRequestMsg();
			byte[] requestMsg = (byte[])request.getAttribute("requestMsg"); // getRequestMsg();
			
			Connection connection = DBHandler.getConnection(flowContext);
			SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TRAN_CONNECTION, connection);
			request.setAttribute(EMPConstance.ATTR_CONTEXT , flowContext);			
			EMPTransactionManager transactionManager = null;

			try {
				transactionManager = (EMPTransactionManager) flowContext
						.getService(EMPConstance.TRX_SVC_NAME);

				flowContext.addDataElement(SFUtil.getTemplateKColl(flowContext, AccessConstance.B_REQUEST_HEAD));
				flowContext.addDataElement(SFUtil.getTemplateKColl(flowContext, AccessConstance.B_RESPONSE_HEAD));
				
				//设置调整class
				flowContext.addDataField("CLASS_NAME", className);
				
				
				//生成定长ID（系统初始流水号16位）
				String logId=BizUtil.getInitSeqId(flowContext);
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_LOG_ID, logId);
				
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TX_CODE, tranCode);
				
				//发送响应报文标识
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_YES);
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_INIT_SIDE, SFConst.INIT_SIDE_SECU);
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_SERVER_ID, this.serviceName);
				
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_IS_TRANSPOND, isTranSpond);
				
				//券商类型
				SFUtil.setDataValue( flowContext, SFConst.PUBLIC_SECU_TYPE, SFConst.SECU_ZL );
				
				SFLogger.info(flowContext, "直联券商请求报文 [" + this.serviceName + "]" + new String(requestMsg));
				
				/*
				 * 解析报文头信息
				 */
				updateHeadModel(request,flowContext);
				
				/*
				 * 解板报文体
				 */
				updateModel(reqMsg, flowContext,null);
				
				/*
				 * 私有业务处理逻辑
				 */
				if("Y".equals(isTranSpond)){ //是否是转发报文   如是 通过ESB转发给管理台
					ZLSecuTranspondAction zl = new ZLSecuTranspondAction();
					Context msgContext = zl.send(flowContext,transformatId,transformatId);
					IndexedCollection iColl=SFUtil.getDataElement(msgContext,"RET");
					KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
					String retCode=SFUtil.getDataValue(msgContext,kColl,"RET_CODE");//从响应报文中取响应码
					String retMsg=SFUtil.getDataValue(msgContext,kColl,"RET_MSG");//从响应报文中取响应码
					
			        SFUtil.setDataValue(flowContext,SFConst.CTX_ERRCODE,retCode);
					SFUtil.setDataValue(flowContext,SFConst.CTX_ERRMSG,retMsg);
				}else{
					TranHandler.doHandle(flowContext); //走私有交易逻辑
				}
				
				//交易事务提交
				if (transactionManager != null){
					transactionManager.commit();
				}
				
				//组装返回报文
				byte[] retMsg = getResponse(flowContext, null );
				
//				String retStr = null;
//				if(retMsg != null)
//					retStr = new String(retMsg);
//				SFLogger.info(flowContext, "TCPIPService [" + this.serviceName + "] return: " + retStr);
				return retMsg;

			} catch (Exception e) {
				if (transactionManager != null)
					transactionManager.rollback();
				request.setAttribute(EMPConstance.ATTR_EXCEPTION, e);
				throw e;
			}finally{
				/*
				 * 计算直联券商总在线人数:单机计算（私有业务处理完成后在线人数减一）
				 */
				if(flowContext!=null&&flowContext.containsKey(tranCode+"_I.SEC_COMP_CODE")){
					String secCompCode=SFUtil.getDataValue(flowContext, tranCode+"_I.SEC_COMP_CODE");
					Map<String,Integer> mapTrad=CacheMap.getCache(SFConst.SYS_CACHE_TRAD);
					synchronized (mapTrad) {
						Integer reqNum=mapTrad.get(secCompCode);//请求记录数
						if(reqNum!=null&&reqNum>1){
							reqNum=reqNum-1;
						}else{
							reqNum=0;
						}
						mapTrad.put(secCompCode, reqNum);
					}
				}
			}
		}catch (EMPException e){
			request.setAttribute(EMPConstance.ATTR_EXCEPTION, e);
			throw e;
		}catch(Exception e){
			request.setAttribute(EMPConstance.ATTR_EXCEPTION, e);
			throw new EMPException("Exception in handle TCPIPService " + this.toString(), e);
		}
	}
	
	
	/**
	 * 使用请求数据更新报文头数据模型。
	 * <p>
	 * 若设置了请求报文格式处理器，则将请求报文解包，更新到数据模型中。
	 * 
	 * @param request 交易请求
	 * @param context 交易上下文
	 * @throws EMPException
	 */
	private void updateHeadModel(EMPTCPIPRequest request,Context flowContext)throws EMPException {
		//update head of pkg
		try
		{
			KeyedCollection common = SFUtil.getDataElement(flowContext, AccessConstance.B_REQUEST_HEAD);//(KeyedCollection)flowContext.getDataElement(AccessConstance.B_REQUEST_HEAD);
			KeyedCollection head = (KeyedCollection)request.getAttribute("headData");
			Object[] keys = head.keySet().toArray();
			for( int i=0; i<keys.length; i++ )
		    {
			   String name = (String)keys[i];
			   String value = (String)head.getDataValue(name);
	           common.setDataValue(name, value.trim());
	           //flowContext.removeDataElement(name);
		    }
			
		}catch (EMPException ee) {
			throw ee;
		}catch (Exception e) {
			throw new EMPException("Failed to update dataModel in BBCPTCPIPRequestHandler:", e);
		}
	}
	
	
	/**
	 * 使用请求数据更新数据模型。
	 * <p>
	 * 若设置了请求报文格式处理器，则将请求报文解包，更新到数据模型中。
	 * 
	 * @param requestMsg 请求报文
	 * @param context 交易上下文
	 * @param dataElementDef input定义
	 * @throws EMPException
	 */
	public void updateModel(byte[] requestMsg, Context context,	DataElement dataElementDef) throws EMPException 
	{
		try 
		{
			if (dataElementDef == null) // no input defined just update the
			// model directly
			{
				DataElement element = null;
				if (getRequestDataFormat() != null) 
				{
					element = new KeyedCollection();
					element.setAppend(true);
					
					if(this.requestDataFormat.isBin() )
					{
						requestDataFormat.unFormat(requestMsg, element);
					}
					else
					{
						String reqData = null;
						if (encoding == null)
							reqData = new String(requestMsg);
						else
							reqData = new String(requestMsg, encoding);
						getRequestDataFormat().unFormat(reqData, element);
					}
					DataUtility.updateDataModel(context, element, factory.getDataTypeDefine());
				} 
			} 
			else 
			{

				DataElement dstElement = (DataElement) dataElementDef.clone();
				if (getRequestDataFormat() != null) 
				{
					if( requestDataFormat.isBin() )
					{
						getRequestDataFormat().unFormat(requestMsg, dstElement);
						
					}
					else
					{
						String reqData = null;
						if (encoding == null)
							reqData = new String(requestMsg);
						else
							reqData = new String(requestMsg, encoding);
						getRequestDataFormat().unFormat(reqData, dstElement);
					}
				} 
				DataUtility.updateDataModel(context, dstElement, factory.getDataTypeDefine());
			}
		} catch (EMPException ee) {
			throw ee;
		} catch (Exception e) 
		{
			throw new EMPException("Failed to update dataModel in EMPTCPIPRequestHandler:", e);
		}

	}
	
	/**
	 * 获得返回报文体。
	 * <p>
	 * 若设置了返回报文格式处理器，则使用其对交易数据进行打包。
	 * 
	 * @param context 交易上下文
	 * @param dataElement output定义
	 * @return 返回报文体
	 * @throws Exception
	 */
	protected byte[] getResponse(Context context, DataElement dataElement) throws Exception {
			// response.setCharacterEncoding("utf-8");
			
			//if (dataElement != null)
			//{
				if (getResponseDataFormat() != null) 
				{
					Object resData = getResponseDataFormat().format( context);
					if( getResponseDataFormat().isBin() )
						return (byte[])resData;
					else
					{
						if( encoding != null )
							return ((String)resData).getBytes(encoding);
						else
							return ((String)resData).getBytes();
					}
				} 
			//}

			return null;

	}

	/**
	 * 注入组件工厂。
	 * 
	 * @param factory 组件工厂
	 */
	public void setComponentFactory(ComponentFactory factory) {
		this.factory = (EMPFlowComponentFactory) factory;
	}

	/**
	 * 获得该Service对应的EMP业务逻辑构件名。
	 * 
	 * @return 业务逻辑构件名
	 * @deprecated 建议使用getBizId()
	 */
	public String getEMPFlowId() {
		return EMPFlowId;
	}

	/**
	 * 设置该Service对应的EMP业务逻辑构件名。
	 * 
	 * @param flowId 业务逻辑构件名
	 * @deprecated 建议使用setBizId()
	 */
	public void setEMPFlowId(String flowId) {
		EMPFlowId = flowId;
	}

	/**
	 * 获得该Service对应的EMP业务逻辑构件名。
	 * 
	 * @return 业务逻辑构件名
	 */
	public String getBizId() {
		return EMPFlowId;
	}
	
	/**
	 * 设置该Service对应的EMP业务逻辑构件名。
	 * 
	 * @param bizId 业务逻辑构件名
	 * @emp:isAttribute true
	 * @emp:name 业务逻辑构件名
	 * @emp:desc 要开放为TCP/IP Service的业务逻辑构件id
	 * @emp:mustSet true
	 */
	public void setBizId(String bizId) {
		EMPFlowId = bizId;
	}

	/**
	 * 获得TCP/IP Service的id。
	 * 
	 * @return TCP/IP Service的id
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * 设置TCP/IP Service的id。
	 * 
	 * @param serviceName TCP/IP Service的id
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * 获得创建会话时的Session Context名。
	 * 
	 * @return 创建会话时的Session Context名
	 */
	public String getSessionContextName() {
		return sessionContextName;
	}

	/**
	 * 设置创建会话时的Session Context名。
	 * 
	 * @param sessionContextName 创建会话时的Session Context名
	 * @emp:isAttribute true
	 * @emp:name Session Context名
	 * @emp:desc 当请求为创建会话时，生成指定的Session Context
	 * @emp:enableFormula $serviceType='session'
	 * @emp:mustSetFormula $serviceType='session'
	 */
	public void setSessionContextName(String sessionContextName) {
		this.sessionContextName = sessionContextName;
	}

	/**
	 * 设置TCP/IP Service的id。
	 * 
	 * @param id TCP/IP Service的id
	 * @emp:isAttribute true
	 * @emp:name TCP/IP Service的id
	 * @emp:desc TCP/IP Service的唯一标识符，通过指定此id对Service进行访问
	 * @emp:mustSet true
	 */
	public void setId(String id) {
		serviceName = id;
	}

	/**
	 * 取得TCP/IP Service访问类型。
	 * 
	 * @return TCP/IP Service访问类型
	 */
	public int getServiceType() {
		return serviceType;
	}

	/**
	 * 设置TCP/IP Service访问类型。
	 * 
	 * @param serviceType TCP/IP Service访问类型(整型)
	 */
	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}
	
	/**
	 * 设置TCP/IP Service访问类型。
	 * 
	 * @param value TCP/IP Service访问类型(字符串)，可选项有session(创建会话)、endSession(结束会话)以及其它(默认，普通请求)
	 * @emp:isAttribute true
	 * @emp:name 访问类型
	 * @emp:desc TCP/IP Service的访问类型，是否创建会话
	 * @emp:valueList normal=普通请求;session=创建会话;endSession=结束会话
	 * @emp:defaultValue normal
	 */
	public void setServiceType(String value) {
		if ("session".equals(value)) {
			serviceType = SESSION_SERVICE;
		} else if ("endSession".equals(value)) {
			serviceType = END_SESSION_SERVICE;
		}
	}

	/**
	 * 获得该请求是否检查会话。
	 * 
	 * @return 该请求是否检查会话
	 */
	public boolean isCheckSession() {
		return checkSession;
	}

	/**
	 * 设置该请求是否检查会话。
	 * 
	 * @param checkSession 该请求是否检查会话，可选项true(默认)、false
	 * @emp:isAttribute true
	 * @emp:name 检查会话
	 * @emp:desc 该请求是否检查会话
	 * @emp:defaultValue true
	 */
	public void setCheckSession(boolean checkSession) {
		this.checkSession = checkSession;
	}

	/**
	 * 获得该Service对应的EMP业务逻辑op id。
	 * 
	 * @return 业务逻辑op id
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * 设置该Service对应的EMP业务逻辑op id。
	 * 
	 * @param opId 业务逻辑op id
	 * @emp:isAttribute true
	 * @emp:name 业务逻辑op id
	 * @emp:desc 要开放为HTTP Service的业务逻辑构件中的Operation id
	 * @emp:mustSet true
	 */
	public void setOpId(String opId) {
		this.opId = opId;
	}

	/**
	 * 判断该请求是否需要创建session。
	 * 
	 * @return 该请求是否需要创建session
	 */
	public boolean isSessionService() {
		return serviceType == SESSION_SERVICE;
	}

	/**
	 * 判断该请求是否需要销毁session。
	 * 
	 * @return 该请求是否需要销毁session
	 */	
	public boolean isEndSessionService() {
		return serviceType == END_SESSION_SERVICE;
	}

	/**
	 * 获得请求报文体格式处理器。
	 * 
	 * @return 请求报文体格式处理器
	 */
	public FormatElement getRequestDataFormat() {
		return requestDataFormat;
	}

	/**
	 * 注入请求报文体格式处理器。
	 * 
	 * @param requestDataFormat 请求报文体格式处理器
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setRequestDataFormat(FormatElement requestDataFormat) {
		this.requestDataFormat = requestDataFormat;
	}

	/**
	 * 获得响应报文体格式处理器。
	 * 
	 * @return 响应报文体格式处理器
	 */
	public FormatElement getResponseDataFormat() {
		return responseDataFormat;
	}

	/**
	 * 注入响应报文体格式处理器。
	 * 
	 * @param responseDataFormat 响应报文体格式处理器
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setResponseDataFormat(FormatElement responseDataFormat) {
		this.responseDataFormat = responseDataFormat;
	}

	/**
	 * 获得该类的字符串表现。
	 * 
	 * @return 该类的字符串表现
	 */
	public String toString() {
		return "EMPTCPIPService name=\"" + serviceName + "\" ";
	}

	/**
	 * 获得报文体编码。
	 * 
	 * @return 报文体编码
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * 设置报文体编码。
	 * 
	 * @param encoding 报文体编码
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * 获得该Service是否启用。
	 * 
	 * @return 该Service是否启用
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * 设置该Service是否启用。
	 * 
	 * @param enabled 该Service是否启用
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 注入该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 * @param inOutDataArea输入输出的数据区域(KeyedCollection)的名称
	 */
	public void setInOutDataArea(String inOutDataArea)
	{
		this.inOutDataArea = inOutDataArea;
	}

	/**
	 * 获取该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 */
	public String getInOutDataArea()
	{
		return this.inOutDataArea;
	}


}
