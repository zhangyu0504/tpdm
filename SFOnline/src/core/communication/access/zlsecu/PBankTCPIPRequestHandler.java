package core.communication.access.zlsecu;


import java.net.Socket;
import java.util.Map;

import module.bean.ErrorMap;
import module.bean.SecCompData;
import module.cache.ErrorMapCache;
import module.cache.ParamCache;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.access.stzsecu.SZTRequestHandler;
import core.communication.format.KeyedFormat;
import core.communication.tcpip.TCPIPService;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

/**
 * EMPƽ̨ʵ�ֵ�TCP/IP����������
 * <p>
 * ���ڴ���TCP/IP����ı���ͷ���Լ��ӱ���ͷ��ȡ��Ҫ����Ϣ��
 * ��Щ��Ϣ����sessionId��serviceId(�������TCP/IP����ID������)�ȡ�
 * <p>
 * �������£�
 * <p>
 * serviceIdField������ͷ�⿪���ServiceId������<br>
 * sessionIdField������ͷ�⿪���SessionId������<br>
 * appendReqHead���Ƿ���Ҫ��ȥ������ͷ����ѡ��true(Ĭ��)��false<br>
 * appendRepHead���Ƿ���Ҫ�ڷ��ر����и�������ͷ����ѡ��true(Ĭ��)��false<br>
 * errorCodeField��������������������<br>
 * encoding������ͷ����
 * <p>
 * ��Ԫ�أ�
 * <p>
 * requestHeadFormat��������ͷ��ʽ������<br>
 * responseHeadFormat����Ӧ����ͷ��ʽ������<br>
 * serviceIdMap��ServiceIdӳ�䣬�������������ServiceIdӳ�䵽��������ServiceId�У����ڽ��׷ַ�<br>
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-29
 * @lastmodified 2008-6-11
 * 
 */
public class PBankTCPIPRequestHandler implements TCPIPRequestHandler {

	/**
	 * ����ͷ����
	 */
	private String encoding = null;
	
	/**
	 * ����������������
	 */
	private String errorCodeField = "errorCode";

	/**
	 * ������ͷ��ʽ������
	 */
	private FormatElement requestHeadFormat = null;

	/**
	 * ��Ӧ����ͷ��ʽ������
	 */
	private FormatElement responseHeadFormat = null;

	/**
	 * �Ƿ���Ҫ��ȥ������ͷ
	 */
	private boolean appendReqHead = true;

	/**
	 * �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 */
	private boolean appendRepHead = true;

	/**
	 * ����ͷ�⿪���ServiceId������
	 */
	private String serviceIdField;

	/**
	 * ����ͷ�⿪���SessionId������
	 */
	private String sessionIdField;

	/**
	 * ServiceIdӳ�䣬�������������ServiceIdӳ�䵽��������ServiceId�У����ڽ��׷ַ�
	 */
	private Map serviceIdMap = null;

	public PBankTCPIPRequestHandler() {
		super();
	}
	
	/**
	 * �����������жϸ������Ƿ�Ϊҵ��������
	 * 
	 * @param msg ������
	 * @return �Ƿ�ҵ��������
	 */
	public boolean isRequestPackage(byte[] msg) {
		return true;
	}

	
	
	/**
	 * ��������EMPTCPIPRequest��װ����
	 * <p>
	 * ���������н������ͷ������ȡ��Session id��Service id��
	 * ��ͬ������һ���װΪEMPTCPIPRequest�Խ��н�һ��������
	 * 
	 * @param requestMsg ������
	 * @return EMPTCPIPRequest
	 */
	public EMPTCPIPRequest getTCPIPRequest(byte[] requestMsg, TCPIPService service, Socket socket) {
		EMPTCPIPRequest request = new EMPTCPIPRequest();

		try{

			EMPFlowComponentFactory factory = (EMPFlowComponentFactory) ComponentFactory.getComponentFactory("SFOnline");
			Context rootContext = factory.getContextNamed(factory.getRootContextName());
			if("a".equals(service.getName())){
				KeyedCollection kColl = new KeyedCollection("head");
				kColl.setAppend( true );
				FormatElement requestHeadFormat = rootContext.getFormat(AccessConstance.B_REQUEST_HEAD);
				if( requestHeadFormat.isBin() )
					requestHeadFormat.unFormat(requestMsg, kColl );
				else
					requestHeadFormat.unFormat(new String(requestMsg), kColl );
				
				
				if(kColl != null){
					//ȥ���ո�
				    for(int i=0;i<kColl.size();i++){
				    	String value = (String) kColl.getDataValue(kColl.getDataElement(i).getName());
				    	
				    	
				    	value = value.trim();
				    	int t = value.length();
				    	//char m = '.';
				    	while(t>0){
				    		char m = value.charAt(t-1);
				    		if(m=='.'){
				    			value = value.substring(0, t-1);
				    			value = value.trim();
				    			t = value.length();
				    		}else{
				    			break;
				    		}
				    	}
				    	kColl.setDataValue(kColl.getDataElement(i).getName(), value.trim());
				    }
				    
				}
				
				request.setAttribute("headData", kColl);
				KeyedFormat headFormat = (KeyedFormat)requestHeadFormat.getFormatElement();
				int headLen = headFormat.getHeadLen();
				
				if( headLen > 0 )
				{
					int length = requestMsg.length - headLen;
					byte[] buf = new byte[length ];
					System.arraycopy(requestMsg, headLen, buf, 0, length);
					
					String rMsg = new String(buf).trim();
					int pos = rMsg.indexOf("?>");
					rMsg = "<?xml version=\"1.0\" encoding=\"GB18030\" ?>"+rMsg.substring(pos+2);
					request.setAttribute("reqData", rMsg.getBytes());
				}
				else
					request.setAttribute("reqData", requestMsg );
				
				
				request.setAttribute("requestMsg", requestMsg );
				String serviceId = (String)kColl.getDataValue( this.serviceIdField );
				request.setAttribute("serviceId", serviceId);					
				return request;
			}else if("b".equals(service.getName())){
				request.setAttribute( EMPConstance.ATTR_CONTEXT, rootContext);
				StringBuffer result = new StringBuffer("");
				result.append("1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              1");
				result.append(new String(requestMsg));
				requestMsg = result.toString().getBytes();
				
            	SZTRequestHandler handler = new SZTRequestHandler();
            	byte[] retMsg = handler.handleRequest(requestMsg);
            	
            	// ����������
				if(retMsg != null)
					service.send(retMsg, socket);
            	
				return request;
			}else{
				return null;
			}
			
			
			
			
			
		}
		catch(Exception e)
		{
			EMPLog.log(
					EMPConstance.EMP_TCPIPACCESS,
					EMPLog.ERROR,
					0,
					"Failed to parse the requestMsg in PBankTCPIPRequestHandler to get sessionId",
					e);
			return null;
		}
	}
	
	/**
	 * ������������Ӧ���ġ�
	 * <p>
	 * �����ݴ���ɷ��ر���ͷ�����ͷ��ر�����һ�����response��
	 * 
	 * @param request TCPIP����
	 * @param retMsg ���ر�����
	 * @return ��Ӧ����
	 */
	public byte[] getResponsePackage(EMPTCPIPRequest request, byte[] retMsg ) {
		try{
			Context context = (Context)request.getAttribute(EMPConstance.ATTR_CONTEXT );
			if( this.appendRepHead && this.responseHeadFormat != null )
			{
				byte[] headBytes;
				Object retHead = responseHeadFormat.format( context );
				if( responseHeadFormat.isBin() )
					headBytes = (byte[])retHead;
				else
					headBytes = ((String)retHead).getBytes();
				
				if( retMsg != null )
				{
					int len = headBytes.length;
					byte[] buf = new byte[len + retMsg.length];
			
					System.arraycopy(headBytes, 0, buf, 0, len);
					System.arraycopy(retMsg, 0, buf, len, retMsg.length);
					return buf;
				}
				else
					return headBytes;
			}
			return retMsg;
			
		}catch(Exception e)
		{
			
			EMPLog.log(EMPConstance.EMP_TCPIPACCESS, EMPLog.ERROR, 0, "Failed to process response package! ", e);
			return null;
		}
	}	
	
	/**
	 * �����쳣ʱ����Ӧ���ġ�
	 * <p>
	 * �����ݺ��쳣��Ϣ����ɷ��ر���ͷ��
	 * 
	 * @param request TCPIP����
	 * @param e �쳣
	 * @return �쳣��Ӧ����
	 */
	public byte[] getExceptionResponse(EMPTCPIPRequest request, Exception e) {
		try 
		{
			String encoding=this.encoding;
			Context context = (Context)request.getAttribute(EMPConstance.ATTR_CONTEXT );
			if( context == null )
				context = (Context)request.getAttribute(EMPConstance.ATTR_SESSION_CONTEXT );
			
			
			String svcId = (String) request.getAttribute("serviceId");
			initHeadKcoll(context,svcId);
			String tranCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );
			
			if(e instanceof SFException){
				SFException sfe = (SFException)e;
				String errCode = sfe.getErrorCode();
				String errMsg =  sfe.getMessage();
				ErrorMap errorMap  = (ErrorMap)ErrorMapCache.getSdbValue(errCode);
				if(errorMap!=null){
					// ���쳣��Ӧ��Ϣ�������ó���ʱ���н�ȡ
					//if(errMsg.getBytes().length>256){
					//	errMsg = BizUtil.getResponseMsg( context, errMsg, 256 );
					//}
					SFUtil.setDataValue( context,  tranCode+ "_O.RESP_MSG", SFUtil.isEmpty(errMsg)? errorMap.getErrExplain():BizUtil.returnErrMsg(context,"ZL", errMsg));
					SFUtil.setDataValue( context, tranCode + "_O.RESP_CODE", errorMap.getZlCode() );
				}else{
					SFUtil.setDataValue( context, tranCode + "_O.RESP_CODE", errCode);
					SFUtil.setDataValue( context,  tranCode+ "_O.RESP_MSG", SFUtil.isEmpty(errMsg)? "����ʧ��":BizUtil.returnErrMsg(context,"ZL", errMsg));
				}
				
			}else{
				SFUtil.setDataValue( context, tranCode + "_O.RESP_CODE", SFConst.RESPCODE_FAILCODE_ZLSECU );
				SFUtil.setDataValue( context,  tranCode+ "_O.RESP_MSG", "����ʧ��" );
			}
			
			String subTxSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );
			String txSeqId = SFUtil.getDataValue(context, "ZLSECU_REQUEST_HEAD.TXSEQID");
			
			if(context.containsKey( tranCode+ "_O.SEC_SEQ_ID" )){
				SFUtil.setDataValue( context,  tranCode+ "_O.SEC_SEQ_ID", txSeqId);
			}
			
			if(context.containsKey(tranCode+ "_O.SUB_TX_SEQ_ID")){
				SFUtil.setDataValue( context,  tranCode+ "_O.SUB_TX_SEQ_ID", subTxSeqId);
			}
			
			
	        FormatElement rootHeadFormat = context.getFormat(AccessConstance.ZLSECU_RESPONSE_HEAD);
			
			String responseHeadData = (String)rootHeadFormat.format(context);
			
			String responseData = "";
			
			if(svcId!=null){
				FormatElement msgFormat = context.getFormat(svcId + "_O");
				responseData = (String)msgFormat.format(context);
			}
			String resData = "<?xml version=\"1.0\"?>\r\n<Root>\r\n"+responseHeadData+"\r\n"+responseData+"\r\n</Root>";//String.valueOf(len)+
			resData = resData.replaceAll("\r\n", "");
			int len = resData.getBytes().length;
			KeyedCollection headColl = SFUtil.getDataElement(context,AccessConstance.B_RESPONSE_HEAD);
			headColl.setDataValue("MSGDATALEN", String.valueOf(len));
			FormatElement headFormat = context.getFormat(AccessConstance.B_RESPONSE_HEAD);
			String headData = (String)headFormat.format(headColl);
			
			/*
			 * ����ͬȯ���ش�����Ӧ���ı����ʽ
			 */
			SecCompData secCompData = (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
			String secCompCode = null;
			if(secCompData!=null){
				 secCompCode = secCompData.getSecCompCode();
				//�ӻ����л�ȡ�ӿڱ���
				String secEncoding=ParamCache.getValue2("SEC_ZL_CHARSET", secCompCode+"_"+svcId);
				if(SFUtil.isNotEmpty(secEncoding)){
					encoding=secEncoding;
				}else{
					secEncoding=ParamCache.getValue2("SEC_ZL_CHARSET", secCompCode);
					if(SFUtil.isNotEmpty(secEncoding)){
						encoding=secEncoding;
					}
				}
			}
			resData = headData+resData;

			SFLogger.info(context,"ֱ��ȯ���쳣��Ӧ���ı����ʽ��["+encoding+"]");
			if (encoding != null)
				return resData.getBytes(encoding);
			else
				return resData.getBytes();
			
		} catch (Exception ee) {
			EMPLog.log(EMPConstance.EMP_TCPIPACCESS, EMPLog.ERROR, 0,
					"Failed to handle exception!", ee);
		}
		return null;
	}

	
	public void initHeadKcoll(Context context,String serverId) throws EMPException {
		
		KeyedCollection headColl = null;
		if(context.containsKey(AccessConstance.B_REQUEST_HEAD)){
			KeyedCollection reqColl = SFUtil.getDataElement(context,AccessConstance.B_REQUEST_HEAD);
			headColl = (KeyedCollection) reqColl.clone();
			
			headColl.setName(AccessConstance.B_RESPONSE_HEAD);
			
		}else{
			throw new EMPException("not find "+AccessConstance.B_REQUEST_HEAD);
		
		}
		headColl.setDataValue("VERSION", "1.0" );
		headColl.setDataValue("FUNCCODE", "000" );
		headColl.setDataValue("TOTALBLOCK", "0" );
		headColl.setDataValue("CURBLOCK", "0" );
		headColl.setDataValue("MSGEXTLEN", "0" );
		
		if(context.containsKey(AccessConstance.B_RESPONSE_HEAD)){
			context.removeDataElement(AccessConstance.B_RESPONSE_HEAD);
			context.addDataElement(headColl);
		}else{
			
			context.addDataElement(headColl);
		}
		
		
		
		KeyedCollection rootHeadColl = null;
		if(context.containsKey("ZLSECU_REQUEST_HEAD")){
			KeyedCollection reqColl = SFUtil.getDataElement(context,"ZLSECU_REQUEST_HEAD");
			
			rootHeadColl = (KeyedCollection) reqColl.clone();
			rootHeadColl.setName("ZLSECU_RESPONSE_HEAD");
			String txSeqId = SFUtil.getDataValue(context, "ZLSECU_REQUEST_HEAD.TXSEQID");
			
			if(SFUtil.isEmpty(txSeqId)){
				txSeqId = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,BizUtil.getInitSeqId(context));
			}else{
				// �޶�txSeqId �������⣬txSeqId �̶�Ϊ8λ����8λ��0 -edit by lch 20180224
				txSeqId = BizUtil.getTxSeqId(SFConst.SEQ_ID_LEN_8,txSeqId);
			}
			rootHeadColl.setDataValue("TXSEQID", txSeqId);
			rootHeadColl.setDataValue("TXDATE", DateUtil.getMacDate());
			rootHeadColl.setDataValue("TXTIME", DateUtil.getMacTime());
			rootHeadColl.setDataValue("FUNCCODE", "100" );
			// �޶�SUBCENTERID Ϊ�����⣬ȯ�̶˽���SUBCENTERID �̶�Ϊ0110 -edit by lch 20180224
			rootHeadColl.setDataValue("SUBCENTERID", "0110" );
			
			if("6065".equals(serverId)){
				SecCompData secCompData = (SecCompData)context.getDataValue(SFConst.PUBLIC_SECU);
				String secCompCode =null;
				if(secCompData!=null){
					secCompCode = secCompData.getSecCompCode();
				}
				if(SFConst.SECU_GUANGDAZQ.equals(secCompCode)){
					rootHeadColl.setDataValue("NODEID", "0110000000010007");
					rootHeadColl.setDataValue("CHANNEL", "0002");
				}else{
					rootHeadColl.setDataValue("NODEID", "0110800000000000");
					rootHeadColl.setDataValue("CHANNEL", "0005");
					
				}
			}
			// ������ϢNODEID Ϊ"0110800000000000",CHANNEL Ϊ"0005" -edit by lch 20180224
			if("6045".equals(serverId)){
				rootHeadColl.setDataValue("NODEID", "0110800000000000");
				rootHeadColl.setDataValue("CHANNEL", "0005");
			}
		}else{
			throw new EMPException(
					"not find "+"ZLSECU_REQUEST_HEAD");
		}
		rootHeadColl.setDataValue("FUNCCODE", "100" );
		if(context.containsKey("ZLSECU_RESPONSE_HEAD")){
			context.removeDataElement("ZLSECU_RESPONSE_HEAD");
			context.addDataElement(rootHeadColl);
		}else{
			context.addDataElement(rootHeadColl);
		}
		
		
	}
	
	/**
	 * ��ñ���������������
	 * 
	 * @return ����������������
	 */
	public String getErrorCodeField() {
		return errorCodeField;
	}
	
	/**
	 * ���ñ���������������
	 * 
	 * @param errorCodeField ����������������
	 * @emp:isAttribute true
	 * @emp:name ����������������
	 * @emp:desc ��ִ���з����쳣���򽫴����뱣���ڸ���������
	 * @emp:defaultValue errorCode
	 */
	public void setErrorCodeField(String errorCodeField) {
		this.errorCodeField = errorCodeField;
	}

	/**
	 * ���������ͷ��ʽ��������
	 * 
	 * @return ������ͷ��ʽ������
	 */
	public FormatElement getRequestHeadFormat() {
		return requestHeadFormat;
	}

	/**
	 * ע��������ͷ��ʽ��������
	 * 
	 * @param headFormat ������ͷ��ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setRequestHeadFormat(FormatElement headFormat) {
		this.requestHeadFormat = headFormat;
	}

	/**
	 * �����Ӧ����ͷ��ʽ��������
	 * 
	 * @return ��Ӧ����ͷ��ʽ������
	 */
	public FormatElement getResponseHeadFormat() {
		return responseHeadFormat;
	}

	/**
	 * ע����Ӧ����ͷ��ʽ��������
	 * 
	 * @param responseHeadFormat ��Ӧ����ͷ��ʽ������
	 * @emp:isChild com.ecc.emp.format.FormatElement
	 */
	public void setResponseHeadFormat(FormatElement responseHeadFormat) {
		this.responseHeadFormat = responseHeadFormat;
	}

	/**
	 * ��ñ���serviceId��������
	 * 
	 * @return ����serviceId��������
	 */
	public String getServiceIdField() {
		return serviceIdField;
	}

	/**
	 * ���ñ���serviceId��������
	 * 
	 * @param serviceIdField ����serviceId����������
	 * @emp:isAttribute true
	 * @emp:name ����serviceId��������
	 * @emp:desc �⿪����ͷ�󣬴����������еĸ���������ȡ��service id
	 * @emp:mustSet true
	 */
	public void setServiceIdField(String serviceIdField) {
		this.serviceIdField = serviceIdField;
	}

	/**
	 * ��ñ���sessionId��������
	 * 
	 * @return ����sessionId��������
	 */
	public String getSessionIdField() {
		return sessionIdField;
	}

	/**
	 * ���ñ���sessionId��������
	 * 
	 * @param sessionIdField ����sessionId����������
	 * @emp:isAttribute true
	 * @emp:name ����sessionId��������
	 * @emp:desc �⿪����ͷ�󣬴����������еĸ���������ȡ��session id
	 * @emp:mustSet true
	 */
	public void setSessionIdField(String sessionIdField) {
		this.sessionIdField = sessionIdField;
	}

	/**
	 * ����Ƿ���Ҫ��ȥ������ͷ��
	 * 
	 * @return �Ƿ���Ҫ��ȥ������ͷ
	 */
	public boolean isAppendReqHead() {
		return appendReqHead;
	}

	/**
	 * �����Ƿ���Ҫ��ȥ������ͷ��
	 * 
	 * @param appendHead �Ƿ���Ҫ��ȥ������ͷ����ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name �Ƿ���Ҫ��ȥ������ͷ
	 * @emp:desc ��Ϊtrue����������ͷ�⿪������ֻ�������������request����һ������
	 * @emp:defaultValue true
	 */
	public void setAppendReqHead(boolean appendHead) {
		this.appendReqHead = appendHead;
	}

	/**
	 * ����Ƿ���Ҫ�ڷ��ر����и�������ͷ��
	 * 
	 * @return �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 */
	public boolean isAppendRepHead() {
		return appendRepHead;
	}

	/**
	 * �����Ƿ���Ҫ�ڷ��ر����и�������ͷ��
	 * 
	 * @param appendHead �Ƿ���Ҫ�ڷ��ر����и�������ͷ����ѡ��true(Ĭ��)��false
	 * @emp:isAttribute true
	 * @emp:name �Ƿ���Ҫ�ڷ��ر����и�������ͷ
	 * @emp:desc ��Ϊtrue������Ӧ���ݰ�����Ӧ����ͷ��ʽ�������׷������Ӧ������֮ǰ
	 * @emp:defaultValue true
	 */
	public void setAppendRepHead(boolean appendHead) {
		this.appendRepHead = appendHead;
	}

	/**
	 * ���ServiceIdӳ�䡣
	 * 
	 * @return ServiceIdӳ��
	 */
	public Map getServiceIdMap() {
		return serviceIdMap;
	}

	/**
	 * ע��ServiceIdӳ�䡣
	 * 
	 * @param serviceIdMap ServiceIdӳ��
	 * @emp:isChild java.util.Map
	 */
	public void setServiceIdMap(Map serviceIdMap) {
		this.serviceIdMap = serviceIdMap;
	}	

	/**
	 * ��ñ���ͷ���롣
	 * 
	 * @return ����ͷ����
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * ���ñ���ͷ���롣
	 * 
	 * @param encoding ����ͷ����
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}