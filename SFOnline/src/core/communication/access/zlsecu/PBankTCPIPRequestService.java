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
 * EMP�ṩ��TCP/IP����������ʷ����ʵ�֡�
 * <p>s
 * �ɽ�EMPҵ���߼���������Ϊ��TCP/IP����������ʵķ���
 * ʹ��ͨ��TCP/IP���������ϵͳ����ͨ���������������ĵķ�ʽ����EMP��ҵ�����߼���
 * <p>
 * ������һ���������������TcpipAccessServletContext�����ļ��У��������£�
 * <p>
 * id��TCP/IP Service��id��ͨ��ָ����id��Service���з���<br>
 * serviceName��id�ĵȼ۲���<br>
 * serviceType��Service�������ͣ���ѡ����session(�����Ự)��endSession(�����Ự)�Լ�����(Ĭ�ϣ���ͨ����)<br>
 * sessionContextName����serviceTypeΪsessionʱ��Ч��ָ��Ҫ�����Ự��ҵ���߼�Context����<br>
 * checkSession���������Ƿ���Ự����ѡ��true(Ĭ��)��false<br>
 * EMPFlowId����Service��Ӧ��EMPҵ���߼�������<br>
 * opId����Service��Ӧ��EMPҵ���߼������е�Operation ID<br>
 * description��������Ϣ<br>
 * encoding�����������<br>
 * enabled����Service�Ƿ����ã���ѡ��true(Ĭ��)��false<br>
 * <p>
 * ��Ԫ�أ�
 * <p>
 * requestDataFormat�����������ʽ������<br>
 * responseDataFormat����Ӧ�������ʽ������
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-12-26
 * @lastmodified 2008-6-11
 * @emp:name TCP/IP����������ʷ���
 * @emp:document EMP�ṩ��TCP/IP����������ʷ��񣬿ɷ���EMPҵ���߼�����
 */
public class PBankTCPIPRequestService implements TCPIPRequestService{

	/**
	 * serviceType��ȡֵ֮һ������ִ�и�����ǰ��Ҫ�����Ự
	 */
	public static int SESSION_SERVICE = 0;

	/**
	 * serviceType��ȡֵ֮һ������ִ�и��������Ҫ���ٻỰ
	 */
	public static int END_SESSION_SERVICE = 1;

	/**
	 * serviceType��ȡֵ֮һ������ִ�и�����ʱ�����ڻỰ����ʹ�õ�ǰ�Ự���������޻Ự����
	 */
	public static int NORMAL_SERVICE = 2;

	/**
	 * TCP/IP Service��id��ͨ��������������ָ����id��Service���з���
	 */
	protected String serviceName;

	/**
	 * �������Ƿ���Ự
	 */
	protected boolean checkSession = true;

	/**
	 * TCP/IP Service��������
	 */
	protected int serviceType = NORMAL_SERVICE;

	/**
	 * ������Ϊ�����Ựʱ��Ч��ָ��Ҫ�����Ự��Session Context��
	 */
	protected String sessionContextName;

	/**
	 * ��Service��Ӧ��EMPҵ���߼�������
	 */
	protected String EMPFlowId;

	/**
	 * ��Service��Ӧ��EMPҵ���߼������е�Operation ID
	 */
	protected String opId;
	
	/**
	 * ���������ʽ������
	 */
	private FormatElement requestDataFormat;

	/**
	 * ��Ӧ�������ʽ������
	 */
	private FormatElement responseDataFormat;

	/**
	 * EMPҵ���߼�����ʵ��������
	 */
	protected EMPFlowComponentFactory factory = null;

	/**
	 * ��Service�Ƿ�����
	 */
	private boolean enabled = true;
	
	/**
	 * ���������
	 */	
	protected String encoding = null;
	
	/**
	 * �ýӿ����������������KeyedCollection��
	 */
	protected String inOutDataArea = null;

	/**
	 * ��Service��Ӧ��TranCode
	 */
	protected String tranCode = null;
	
	/**
	 * �Ƿ���ת������   ���� ͨ��ESBת��������̨
	 */
	
	protected String isTranSpond = null;
	
	/**
	 * ת��ESB��formatId
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
	 * �����������ʵ�ַ�����
	 * 
	 * @param request TCP/IP����
	 * @param sessionContext Session Context
	 * @return �������ݣ�������õı�����
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
				
				//���õ���class
				flowContext.addDataField("CLASS_NAME", className);
				
				
				//���ɶ���ID��ϵͳ��ʼ��ˮ��16λ��
				String logId=BizUtil.getInitSeqId(flowContext);
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_LOG_ID, logId);
				
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_TX_CODE, tranCode);
				
				//������Ӧ���ı�ʶ
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_YES);
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_INIT_SIDE, SFConst.INIT_SIDE_SECU);
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_SERVER_ID, this.serviceName);
				
				
				SFUtil.setDataValue(flowContext, SFConst.PUBLIC_IS_TRANSPOND, isTranSpond);
				
				//ȯ������
				SFUtil.setDataValue( flowContext, SFConst.PUBLIC_SECU_TYPE, SFConst.SECU_ZL );
				
				SFLogger.info(flowContext, "ֱ��ȯ�������� [" + this.serviceName + "]" + new String(requestMsg));
				
				/*
				 * ��������ͷ��Ϣ
				 */
				updateHeadModel(request,flowContext);
				
				/*
				 * ��屨����
				 */
				updateModel(reqMsg, flowContext,null);
				
				/*
				 * ˽��ҵ�����߼�
				 */
				if("Y".equals(isTranSpond)){ //�Ƿ���ת������   ���� ͨ��ESBת��������̨
					ZLSecuTranspondAction zl = new ZLSecuTranspondAction();
					Context msgContext = zl.send(flowContext,transformatId,transformatId);
					IndexedCollection iColl=SFUtil.getDataElement(msgContext,"RET");
					KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
					String retCode=SFUtil.getDataValue(msgContext,kColl,"RET_CODE");//����Ӧ������ȡ��Ӧ��
					String retMsg=SFUtil.getDataValue(msgContext,kColl,"RET_MSG");//����Ӧ������ȡ��Ӧ��
					
			        SFUtil.setDataValue(flowContext,SFConst.CTX_ERRCODE,retCode);
					SFUtil.setDataValue(flowContext,SFConst.CTX_ERRMSG,retMsg);
				}else{
					TranHandler.doHandle(flowContext); //��˽�н����߼�
				}
				
				//���������ύ
				if (transactionManager != null){
					transactionManager.commit();
				}
				
				//��װ���ر���
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
				 * ����ֱ��ȯ������������:�������㣨˽��ҵ������ɺ�����������һ��
				 */
				if(flowContext!=null&&flowContext.containsKey(tranCode+"_I.SEC_COMP_CODE")){
					String secCompCode=SFUtil.getDataValue(flowContext, tranCode+"_I.SEC_COMP_CODE");
					Map<String,Integer> mapTrad=CacheMap.getCache(SFConst.SYS_CACHE_TRAD);
					synchronized (mapTrad) {
						Integer reqNum=mapTrad.get(secCompCode);//�����¼��
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
	 * ʹ���������ݸ��±���ͷ����ģ�͡�
	 * <p>
	 * �������������ĸ�ʽ���������������Ľ�������µ�����ģ���С�
	 * 
	 * @param request ��������
	 * @param context ����������
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
	 * ʹ���������ݸ�������ģ�͡�
	 * <p>
	 * �������������ĸ�ʽ���������������Ľ�������µ�����ģ���С�
	 * 
	 * @param requestMsg ������
	 * @param context ����������
	 * @param dataElementDef input����
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
	 * ��÷��ر����塣
	 * <p>
	 * �������˷��ر��ĸ�ʽ����������ʹ����Խ������ݽ��д����
	 * 
	 * @param context ����������
	 * @param dataElement output����
	 * @return ���ر�����
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
	 * ע�����������
	 * 
	 * @param factory �������
	 */
	public void setComponentFactory(ComponentFactory factory) {
		this.factory = (EMPFlowComponentFactory) factory;
	}

	/**
	 * ��ø�Service��Ӧ��EMPҵ���߼���������
	 * 
	 * @return ҵ���߼�������
	 * @deprecated ����ʹ��getBizId()
	 */
	public String getEMPFlowId() {
		return EMPFlowId;
	}

	/**
	 * ���ø�Service��Ӧ��EMPҵ���߼���������
	 * 
	 * @param flowId ҵ���߼�������
	 * @deprecated ����ʹ��setBizId()
	 */
	public void setEMPFlowId(String flowId) {
		EMPFlowId = flowId;
	}

	/**
	 * ��ø�Service��Ӧ��EMPҵ���߼���������
	 * 
	 * @return ҵ���߼�������
	 */
	public String getBizId() {
		return EMPFlowId;
	}
	
	/**
	 * ���ø�Service��Ӧ��EMPҵ���߼���������
	 * 
	 * @param bizId ҵ���߼�������
	 * @emp:isAttribute true
	 * @emp:name ҵ���߼�������
	 * @emp:desc Ҫ����ΪTCP/IP Service��ҵ���߼�����id
	 * @emp:mustSet true
	 */
	public void setBizId(String bizId) {
		EMPFlowId = bizId;
	}

	/**
	 * ���TCP/IP Service��id��
	 * 
	 * @return TCP/IP Service��id
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * ����TCP/IP Service��id��
	 * 
	 * @param serviceName TCP/IP Service��id
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * ��ô����Ựʱ��Session Context����
	 * 
	 * @return �����Ựʱ��Session Context��
	 */
	public String getSessionContextName() {
		return sessionContextName;
	}

	/**
	 * ���ô����Ựʱ��Session Context����
	 * 
	 * @param sessionContextName �����Ựʱ��Session Context��
	 * @emp:isAttribute true
	 * @emp:name Session Context��
	 * @emp:desc ������Ϊ�����Ựʱ������ָ����Session Context
	 * @emp:enableFormula $serviceType='session'
	 * @emp:mustSetFormula $serviceType='session'
	 */
	public void setSessionContextName(String sessionContextName) {
		this.sessionContextName = sessionContextName;
	}

	/**
	 * ����TCP/IP Service��id��
	 * 
	 * @param id TCP/IP Service��id
	 * @emp:isAttribute true
	 * @emp:name TCP/IP Service��id
	 * @emp:desc TCP/IP Service��Ψһ��ʶ����ͨ��ָ����id��Service���з���
	 * @emp:mustSet true
	 */
	public void setId(String id) {
		serviceName = id;
	}

	/**
	 * ȡ��TCP/IP Service�������͡�
	 * 
	 * @return TCP/IP Service��������
	 */
	public int getServiceType() {
		return serviceType;
	}

	/**
	 * ����TCP/IP Service�������͡�
	 * 
	 * @param serviceType TCP/IP Service��������(����)
	 */
	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}
	
	/**
	 * ����TCP/IP Service�������͡�
	 * 
	 * @param value TCP/IP Service��������(�ַ���)����ѡ����session(�����Ự)��endSession(�����Ự)�Լ�����(Ĭ�ϣ���ͨ����)
	 * @emp:isAttribute true
	 * @emp:name ��������
	 * @emp:desc TCP/IP Service�ķ������ͣ��Ƿ񴴽��Ự
	 * @emp:valueList normal=��ͨ����;session=�����Ự;endSession=�����Ự
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
	 * ��ø������Ƿ���Ự��
	 * 
	 * @return �������Ƿ���Ự
	 */
	public boolean isCheckSession() {
		return checkSession;
	}

	/**
	 * ���ø������Ƿ���Ự��
	 * 
	 * @param checkSession �������Ƿ���Ự����ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name ���Ự
	 * @emp:desc �������Ƿ���Ự
	 * @emp:defaultValue true
	 */
	public void setCheckSession(boolean checkSession) {
		this.checkSession = checkSession;
	}

	/**
	 * ��ø�Service��Ӧ��EMPҵ���߼�op id��
	 * 
	 * @return ҵ���߼�op id
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * ���ø�Service��Ӧ��EMPҵ���߼�op id��
	 * 
	 * @param opId ҵ���߼�op id
	 * @emp:isAttribute true
	 * @emp:name ҵ���߼�op id
	 * @emp:desc Ҫ����ΪHTTP Service��ҵ���߼������е�Operation id
	 * @emp:mustSet true
	 */
	public void setOpId(String opId) {
		this.opId = opId;
	}

	/**
	 * �жϸ������Ƿ���Ҫ����session��
	 * 
	 * @return �������Ƿ���Ҫ����session
	 */
	public boolean isSessionService() {
		return serviceType == SESSION_SERVICE;
	}

	/**
	 * �жϸ������Ƿ���Ҫ����session��
	 * 
	 * @return �������Ƿ���Ҫ����session
	 */	
	public boolean isEndSessionService() {
		return serviceType == END_SESSION_SERVICE;
	}

	/**
	 * ������������ʽ��������
	 * 
	 * @return ���������ʽ������
	 */
	public FormatElement getRequestDataFormat() {
		return requestDataFormat;
	}

	/**
	 * ע�����������ʽ��������
	 * 
	 * @param requestDataFormat ���������ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setRequestDataFormat(FormatElement requestDataFormat) {
		this.requestDataFormat = requestDataFormat;
	}

	/**
	 * �����Ӧ�������ʽ��������
	 * 
	 * @return ��Ӧ�������ʽ������
	 */
	public FormatElement getResponseDataFormat() {
		return responseDataFormat;
	}

	/**
	 * ע����Ӧ�������ʽ��������
	 * 
	 * @param responseDataFormat ��Ӧ�������ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setResponseDataFormat(FormatElement responseDataFormat) {
		this.responseDataFormat = responseDataFormat;
	}

	/**
	 * ��ø�����ַ������֡�
	 * 
	 * @return ������ַ�������
	 */
	public String toString() {
		return "EMPTCPIPService name=\"" + serviceName + "\" ";
	}

	/**
	 * ��ñ�������롣
	 * 
	 * @return ���������
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * ���ñ�������롣
	 * 
	 * @param encoding ���������
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * ��ø�Service�Ƿ����á�
	 * 
	 * @return ��Service�Ƿ�����
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * ���ø�Service�Ƿ����á�
	 * 
	 * @param enabled ��Service�Ƿ�����
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * ע��ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 * @param inOutDataArea�����������������(KeyedCollection)������
	 */
	public void setInOutDataArea(String inOutDataArea)
	{
		this.inOutDataArea = inOutDataArea;
	}

	/**
	 * ��ȡ�ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 */
	public String getInOutDataArea()
	{
		return this.inOutDataArea;
	}


}