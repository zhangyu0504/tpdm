package core.communication.access.esb;


import java.util.Iterator;
import java.util.Map;

import com.dc.eai.data.CompositeData;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.access.tcpip.TCPIPRequestHandler;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.format.FormatElement;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.format.PBankFormatElement;
import core.communication.format.paesb.ESBCDFormat;
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
	public EMPTCPIPRequest getTCPIPRequest(byte[] requestMsg){		
		EMPTCPIPRequest request = new EMPTCPIPRequest();		
		String reqData = new String(requestMsg);		
		CompositeData reqCD = null;
		reqCD = PackUtil.unpackXmlStr(reqData.trim());		
		String sessionId = null;
		String serviceId = null;
		String sceneId = null;		
		serviceId = reqCD.getStruct("SYS_HEAD").getField("SERVICE_CODE").strValue();
		sceneId = reqCD.getStruct("SYS_HEAD").getField("SERVICE_SCENE").strValue();		
		request.setAttribute("SID", sessionId);
		request.setAttribute("serviceId", serviceId + "_" + sceneId);
		request.setAttribute("reqData", reqCD);
		return request;
		
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
			SFLogger.error(SFConst.DEFAULT_TRXCODE, null, "Failed to process response package! ", e);
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
		try {			
			Context context = (Context)request.getAttribute(EMPConstance.ATTR_CONTEXT );
			if( context == null )
				context = (Context)request.getAttribute(EMPConstance.ATTR_SESSION_CONTEXT );
			
			initPubMsgHead(1,context);
			
			context.setDataValue("SYS_HEAD.RET_STATUS", "F");
			
			if(e instanceof SFException){
				SFException sfe = (SFException)e;
				String errorCode = sfe.getErrorCode();
				String errMsg =  sfe.getMessage();
				context.setDataValue("RET.RET_CODE", errorCode);
				if(SFUtil.isEmpty(errMsg)){
					context.setDataValue("RET.RET_MSG", "ʧ��");
				}else{
					context.setDataValue("RET.RET_MSG",errMsg);
				}
			}
			
			FormatElement respHeadFormat = context.getFormat("PAESBHEAD_O");
			PBankFormatElement headFormat = (ESBCDFormat)respHeadFormat.getFormatElement();
			CompositeData responseHeadCD = (CompositeData)headFormat.format(context);
			
			
			String svcId = (String) request.getAttribute("serviceId");
			FormatElement msgFormat = context.getFormat(svcId + "_O");
			PBankFormatElement cdFormat = (ESBCDFormat)msgFormat.getFormatElement();
			CompositeData responseCD = (CompositeData)cdFormat.format(context);
			
			if(responseCD!=null){
				Iterator ir = responseCD.iterator();
				while(ir.hasNext()){
					String key = (String)ir.next();
					CompositeData cd = responseCD.getStruct(key);
					responseHeadCD.addStruct(key, cd);
					
				}
			}
			

			return PackUtil.packXmlStr(responseHeadCD).getBytes();
			
		} catch (Exception e2) {
			SFLogger.error(SFConst.DEFAULT_TRXCODE, null,"Failed to handle exception!", e2);
		}		
		return null;
	}

	
	/**
	 * ��ʼ��ESB��������ͷ���ݡ�
	 * 
	 */
	public void initPubMsgHead(int type,Context context) throws Exception
	{
		String[] tmpStrings = null;
		String tmpName = null;
		
		for (int i = 0; i < 5; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, "");
		}
		
		
		if (type == 0) {
			//����_ESB_HEAD����KCOLL
			context.addDataElement(SFUtil.getTemplateKColl(context, "SYS_HEAD"));
			context.addDataElement(SFUtil.getTemplateKColl(context, "APP_HEAD"));
			context.addDataElement(SFUtil.getTemplateKColl(context, "RET"));
			
//			//����_ESB_HEAD����KCOLL
//			context.addDataElement(SFUtil.getTemplateKColl(context, "_ESB_HEAD"));
			
			//���������ʼ��
			tmpName = "SYS_HEAD;APP_HEAD;;";
			
		}
		else {
			//���������ʼ��
			tmpName = "SYS_HEAD;RET;APP_HEAD;";
		}
		tmpStrings = tmpName.split(";");

		for (int i = 0; i < tmpStrings.length; i++) {
			context.remove("_ESB_PARAM_" + i);
			context.addDataField("_ESB_PARAM_" + i, tmpStrings[i]);
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