package core.communication.access.webservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import com.ecc.emp.access.webservice.WSDLBuilder;
import com.ecc.emp.access.webservice.WebServiceInfo;
import com.ecc.emp.accesscontrol.AccessManager;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.component.factory.ServletContextFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPBusinessLogic;
import com.ecc.emp.flow.EMPFlow;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.session.Session;
import com.ecc.emp.session.SessionManager;
import com.ecc.emp.transaction.EMPTransactionManager;

/**
 * EMP WebService接入HTTP方式入口。
 * <p>
 * EMP中允许将业务逻辑构件导出为一个标准的WebService，
 * 由一份WebService Context配置文件描述可以导出的业务逻辑构件定义，以及其导出方式。
 * <p>
 * 以下是配置文件的例子：
 * <pre><code> &lt;?xml version="1.0" encoding="GB18030" ?>
 * &lt;servletContext>
 * 	&lt;sessionManager name="sessionManager" label="会话管理器" sessionCheckInterval="120000" class="com.ecc.emp.session.EMPSessionManager" sessionTimeOut="600000"/>
 * 	&lt;WebService id="test" label="测试" checkSession="true" class="com.ecc.emp.access.webservice.WebServiceInfo" EMPFlowId="test"/>
 * 	&lt;WebService id="login" label="登录" checkSession="false" class="com.ecc.emp.access.webservice.WebServiceInfo" EMPFlowId="login"/>
 * &lt;/servletContext></code></pre>
 * 配置文件可包含会话管理器、访问控制器，以及一到多个要开放为WebService的业务逻辑构件的映射定义。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-24
 * @lastmodified 2008-6-10
 *
 */
public class PBankWebServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * EMP业务逻辑构件实例化工厂
	 */
	private EMPFlowComponentFactory componentFactory;

	/**
	 * EMP业务逻辑构件实例化工厂名称
	 */
	private String factoryName;

	/**
	 * EMP业务逻辑构件实例化工厂的公共配置文件名，对应web.xml中Servlet的初始化参数iniFile
	 */
	private String empIniFileName;

	/**
	 * WebService渠道访问配置文件的根目录，默认为Web应用的Context根目录
	 */
	private String rootPath;
	
	/**
	 * 所有被开放的Service定义
	 */
	private Map services;

	/**
	 * 创建SOAPMessage对象的工厂
	 */
	private MessageFactory msgFactory;
	
	/**
	 * 会话管理器
	 */
	private SessionManager sessionManager;

	/**
	 * 存放及传递会话ID的域
	 */
	private String sessionIdField;
	
	/**
	 * 访问控制器
	 */
	private AccessManager accessManager;

	/**
	 * Servlet的入口方法。
	 * <p>
	 * 首先检查容器是否已初始化好，如果没有初始化好，返回提示信息。
	 * 若为GET请求，则自动转向doGet方法取得某个Service的WSDL定义；
	 * 若为POST请求，则自动转向doPost方法执行某个Service。
	 * 
	 * @see #doGet(HttpServletRequest, HttpServletResponse)
	 * @see #doPost(HttpServletRequest, HttpServletResponse)
	 * @param request HTTP请求
	 * @param response HTTP响应
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 检查容器是否已初始化好，如果没有初始化好，返回提示信息
		if (!this.checkInitialize()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		} else {
			super.service(request, response);
		}
	}
	   
   /**
	 * 取得某个Service的WSDL定义。
	 * <p>
	 * 请求URL应按照如下格式：http://server:port/webContext/servletName/serviceId，
	 * 其中servletName部分为本Servlet的映射url-pattern，
	 * serviceId即要取得WSDL定义的Service名称。
	 * <p>
	 * 若要取得的Service不存在，则返回当前所有Service的列表。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		try {
			String reqURL = request.getRequestURI();

			int idx = reqURL.lastIndexOf('/');
			if (idx == -1)
				throw new EMPException("Invalid request URI!");

			String serviceId = reqURL.substring(idx + 1);

			WebServiceInfo service = (WebServiceInfo) this.services
					.get(serviceId);
			if (service == null) {
				response.setContentType("text/html; charset=utf-8");
				reqURL = reqURL.substring(0, idx + 1);
				response.getWriter().write(this.getServiceList(reqURL));
				return;
				// throw new EMPException("WebService named " + serviceId + " not found!");
			}

			String flowId = service.getEMPFlowId();
			EMPBusinessLogic flow = (EMPBusinessLogic) this.componentFactory
					.getEMPFlow(flowId);

			WSDLBuilder builder = new WSDLBuilder();
			builder.setTargetURL(request.getRequestURL().toString());

			builder.setDataTypeDefs(componentFactory.getDataTypeDefine());

			String wsdlContent = builder.buildWSDLFromEMPBiz(flow);
			response.setContentType("text/xml; charset=utf-8");

			response.getWriter().write(wsdlContent);

		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0,
					"Failed to get WebService info for "
							+ request.getRequestURI(), e);
		}
	}
	
	/**
	 * 以HTML格式返回当前所有Service的列表。
	 * 
	 * @param uri 当前Servlet的URL，用于拼接链接
	 * @return Service列表的HTML内容
	 */
	private String getServiceList(String uri) {
		
		StringBuffer buf = new StringBuffer("<html><head><title>EMP WebService List</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>");

		buf.append("<table><tr><th>ServiceID</th><th>Description</th></tr>");	
		Object[] svcIds = services.keySet().toArray();
		for (int i = 0; i < svcIds.length; i++) {
			String key = (String) svcIds[i];
			WebServiceInfo svcInfo = (WebServiceInfo) services.get(key);
			buf.append("<tr><td><a href=\"");
			buf.append(uri);
			buf.append("/");
			buf.append(key);
			buf.append("\">");
			buf.append(key);
			buf.append("</a></td>");
			buf.append("<td>");
			buf.append(svcInfo.getDescription());
			buf.append("</td></tr>");
		}
		buf.append("</table></body></html>");

		return buf.toString();		   
	}
   
	/**
	 * 请求某个EMP的WebService。 
	 * <p>
	 * 请求URL应按照如下格式：http://server:port/webContext/servletName/serviceId，
	 * 其中servletName部分为本Servlet的映射url-pattern，
	 * serviceId即要请求的Service名称。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		try {
			String remoteIP = request.getRemoteAddr();
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
					"Request from [" + remoteIP + "] "
							+ request.getRequestURI() + " ...");
			
			MimeHeaders mimeHeaders = this.getMimeHeaders(request);
			SOAPMessage soapMessage = msgFactory.createMessage(mimeHeaders,
					request.getInputStream());

			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				soapMessage.writeTo(out);
				EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
						"SOAP Message: \n" + out.toString("utf-8"));
			}
			
//			Document doc = soapMessage.getSOAPPart().getEnvelope().getBody().extractContentAsDocument();
//            String str = doc.getElementsByTagName("arg0").item(0).getTextContent();
			SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
			SOAPElement element = (SOAPElement)body.getChildElements().next();
			element = (SOAPElement)element.getChildElements().next();
			String str = element.getTextContent();
 //           context.setDataValue("REQUEST", str);
			
			SOAPMessage soapMessageReply = null;
			soapMessageReply = this.doProcessServiceRequest(request,
					soapMessage);
			
			if (soapMessageReply != null) {
				if (soapMessageReply.saveRequired())
					soapMessageReply.saveChanges();

				// Check to see if presence of SOAPFault
				if (containsFault(soapMessageReply)) {
					response.setStatus(500);
					// EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0, "It has a fault");
				} else {
					response.setStatus(200);
				}

				// Send the response back to the sender by placing the mime headers into the response, and
				// externalizing the soapmessage onto the response object.
				response.setContentType("text/xml; charset=utf-8");
				putHeaders(soapMessageReply.getMimeHeaders(), response);

				ServletOutputStream sOutputStream = response.getOutputStream();
				soapMessageReply.writeTo(sOutputStream);
				sOutputStream.flush();

				// if(
				// EMPLog.isLogEnbled(EMPConstance.EMP_WEBSERVICE,
				// EMPLog.INFO))
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					soapMessageReply.writeTo(out);
					EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
							"Reply SOAP Message: \n"
									+ new String(out.toByteArray()));
				}
			}

			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
					"Process Request from [" + remoteIP + "] "
							+ request.getRequestURI() + " OK.");

		} catch (Exception exception) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0,
					"Failed to process WebService request for "
							+ request.getRequestURI(), exception);
		}
	}

	/**
	 * 取请求的Mime头。
	 * 
	 * @param request HTTP请求
	 * @return 请求的Mime头
	 */
	protected MimeHeaders getMimeHeaders(HttpServletRequest request) {

		MimeHeaders mimeHeaders = new MimeHeaders();

		Enumeration en = request.getHeaderNames();
		while (en.hasMoreElements()) {
			String headerName = (String) en.nextElement();
			String headerVal = request.getHeader(headerName);
			StringTokenizer tk = new StringTokenizer(headerVal, ",");
			while (tk.hasMoreTokens()) {
				mimeHeaders.addHeader(headerName, tk.nextToken().trim());
			}
		}
		return mimeHeaders;
	}

	/**
	 * 将Mime头写入到响应中。
	 * 
	 * @param headers 要写入到响应的Mime头
	 * @param response HTML响应
	 */
	private void putHeaders(MimeHeaders headers, HttpServletResponse response) {
		
		Iterator hs = headers.getAllHeaders();
		while (hs.hasNext()) {
			MimeHeader header = (MimeHeader) hs.next();
			response.addHeader(header.getName(), header.getValue());
		}
	}
	   
	/**
	 * 处理SOAP请求，返回SOAP报文。
	 * 
	 * @param request HTML请求
	 * @param msg 请求SOAP报文
	 * @return 响应SOAP报文
	 */
	protected SOAPMessage doProcessServiceRequest(HttpServletRequest request, SOAPMessage msg) {

		SOAPMessage resp = null;
		Context context = null;
		Object accessObj = null;		//访问控制器的控制对象

		long beginTimeStamp = System.currentTimeMillis();

		try {
			
			// Create a reply message from the msgFactory of JAXMServlet
			resp = msgFactory.createMessage();

		//	String methodname = "";
		//	Iterator iterator = null;

			// 获取所请求Service的id
			String reqURL = request.getRequestURI();
			int idx = reqURL.lastIndexOf('/');
			if (idx == -1) {
				throw new EMPException("invalid service request!" );
			}
			String serviceId = reqURL.substring(idx + 1);

			// 根据serviceId获取Service，并判断是否可用
			WebServiceInfo service = (WebServiceInfo) this.services
					.get(serviceId);
			if (service == null)
				throw new EMPException("WebService named " + serviceId
						+ " not found!");
			if (!service.isEnabled()) {
				throw new EMPException("WebService named " + serviceId
						+ " not enabled to access!");
			}
			
			PBankSOAPPackage soapPackage = new PBankSOAPPackage();

			// 检查Session，获得SessionContext
			Session session = null;
			Context sessionContext = null;
			String sessionId;
			if (service.getServiceType() == WebServiceInfo.SESSION_SERVICE) {
				sessionContext = this.componentFactory.getContext(null, service
						.getSessionContextName());
			} else {
				try {
			//		sessionId = soapPackage.getSessionId(msg, this.sessionIdField);
					sessionId = soapPackage.getFieldValue(msg, this.sessionIdField);
					session = this.sessionManager.getSession(sessionId, null, false);
				} catch (Exception e) {
				}
				if (session != null)
					sessionContext = (Context) session
							.getAttribute(EMPConstance.ATTR_CONTEXT);
				if (session == null && service.isCheckSession()) {
					throw new EMPException(
							"session time out or not established!");
				}
			}

			// 访问控制处理
			if (this.accessManager != null) {
				if(sessionContext != null)
					accessObj = accessManager.checkAccess(sessionContext, msg,
							service.getServiceName());
				else{
					Context rootContext = this.componentFactory.getContextNamed(this.componentFactory.getRootContextName());
					accessObj = accessManager.checkAccess(rootContext, msg,
							service.getServiceName());
				}
				if (accessObj != null)
					accessManager.beginAccess(accessObj);
			}

			// 获得WebSevice对应的EMP业务逻辑构件，及对应Context
			String flowId = service.getEMPFlowId();
			EMPFlow flow = this.componentFactory.getEMPFlow(flowId);
			context = (Context) flow.getContext().clone();
			if (sessionContext != null)
				context.chainedTo(sessionContext);

			EMPBusinessLogic bizLogic = (EMPBusinessLogic) flow;
			String operationId = soapPackage.getOperationId(msg);

			// 将请求数据放入Context
			soapPackage.setDataTypeDefs(componentFactory.getDataTypeDefine());
			soapPackage.updateContext(msg, context, (KeyedCollection) bizLogic.getInput(operationId), operationId);

			EMPTransactionManager transactionManager = null;
			try {
				transactionManager = (EMPTransactionManager) context.getService(EMPConstance.TRX_SVC_NAME);

				String retValue = flow.execute(context, operationId);
				
				if ("0".equals(retValue) && (service.getServiceType() == WebServiceInfo.SESSION_SERVICE)) {
					// do establish the session if execute OK, OK flag is retValue='0'
					session = sessionManager.getSession(null, null, true);
					session.setAttribute(EMPConstance.ATTR_CONTEXT, sessionContext);
					sessionId = session.getId();
					sessionContext.setDataValue(this.sessionIdField, sessionId);
					request.setAttribute(EMPConstance.ATTR_SESSION, session);
				}

				if ("0".equals(retValue) && (service.getServiceType() == WebServiceInfo.END_SESSION_SERVICE)) {
					sessionManager.removeSession(session);
				}

				// commit the transaction if exist
				if (transactionManager != null)
					transactionManager.commit();

//				context.setDataValue("RESPONSE", "<Result>OK</Result>");
				soapPackage.updateSoapMessage(resp, context, (KeyedCollection) bizLogic.getOutput(operationId), operationId);

			} catch (Exception e) {
				if (transactionManager != null)
					transactionManager.rollback();
				throw e;
			}

			return resp;
			
		} catch (Exception e) {

			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0, "Failed to process WebService request!", e);

			try {
				SOAPBody body = resp.getSOAPBody();
				SOAPFault fault = body.addFault();
			//	QName faultName = new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server");
				fault.setFaultCode("Exception");
				fault.setFaultActor(request.getRequestURL().toString());
				fault.setFaultString(e.getMessage());	
			//	returnBodyElement.addChildElement("error").addTextNode("ERROR: " + e.getMessage());
			} catch (SOAPException ex) {
			}
			return resp;
			
		} finally {
			
		   if( this.accessManager!= null && accessObj != null )
			   accessManager.endAccess( accessObj, beginTimeStamp );
			
			if (context != null)
				context.terminate();
		}
	}
	   
	/**
	 * 关闭Servlet。
	 */
	public void destroy() {
		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
				"Destory the servlet: " + this.getServletName());
		if (this.sessionManager != null)
			sessionManager.terminate();

		if (empIniFileName != null)
			componentFactory.close();
	}

	/**
	 * Servlet初始化入口方法。
	 * <p>
	 * 使用web.xml中的配置初始化Servlet，并生成MessageFactory实例。
	 * 
	 * @param config Servlet初始化参数
	 * @throws javax.servlet.ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Start up the servlet: " + this.getServletName());
		doInit();

		try {
			msgFactory = MessageFactory.newInstance();
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE,	EMPLog.ERROR, 0,
							"Failed to initialize MessageFactory! EMP Webservice will not available! ", e);
		}
		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Start up the servlet: " + this.getServletName() + " OK!");
	}
	   
   /**
	 * Servlet初始化实现方法。
	 * <p>
	 * 获取初始化参数，并取得指定业务逻辑组件工厂，
	 * 或按照指定配置文件实例化新的组件工厂。
	 * 
	 * @throws javax.servlet.ServletException
	 */
	private void doInit() throws ServletException {

		componentFactory = null;

		services = new HashMap();
		   
		// 获得配置文件根目录
		rootPath = getServletContext().getInitParameter(
				EMPConstance.SETTINGS_ROOT);// getInitParameter("factoryRootPath");
		if (rootPath != null && rootPath.startsWith("./"))
			rootPath = getServletContext().getRealPath("/");
		if (rootPath == null) {
			rootPath = getServletContext().getRealPath("/");
		}
		rootPath = rootPath.replace('\\', '/');
		if (!rootPath.endsWith("/"))
			rootPath = rootPath + "/";

		// 获得初始化参数
		factoryName = getInitParameter("factoryName");
		empIniFileName = getInitParameter("iniFile");
		sessionIdField = getInitParameter("sessionIdField");

		String rootContextName = getInitParameter("rootContextName");
		// String sessionManagerName = this.getInitParameter("sessionManager");

		try {
			if (empIniFileName != null) {
				componentFactory = new EMPFlowComponentFactory();
				EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
						"Initialize EMP Flow Context from " + empIniFileName,
						null);
				componentFactory.setName("EMPFlow");
				componentFactory.setRootContextName(rootContextName);
				componentFactory.initializeComponentFactory(factoryName,
						rootPath + empIniFileName);
				// Context rootContext = componentFactory.getContext(null, rootContextName);

			} else {
				EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
						"Share EMP flow Context from: " + factoryName);
				componentFactory = (EMPFlowComponentFactory) ComponentFactory
						.getComponentFactory(factoryName);
			}

		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
					"Initialize EMP Request Servlet from " + empIniFileName
							+ " failed!", e);
			throw new ServletException("Failed to initialize the EMP context ",
					e);
		}

		initWebServiceContext();
		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Initialize EMP Request Servlet from " + empIniFileName
						+ " OK!", null);
	}

	/**
	 * HTTP请求处理模型初始化方法。
	 * <p>
	 * 解析WebService Context配置文件。
	 */
	private void initWebServiceContext() {

		String servletContextFile = getInitParameter("servletContextFile");

		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Initialize EMP Servlet Context from " + servletContextFile,
				null);
		ServletContextFactory ctxFactory = new ServletContextFactory();
		ctxFactory.initializeComponentFactory("contextFactory", rootPath
				+ servletContextFile);

		try {
			ctxFactory.parseTheContext(this);
			ctxFactory.exportMBean(this, this.getServletName());

			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
					"Initialize EMP Servlet Context from " + servletContextFile
							+ " OK!", null);
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0,
					"Initialize EMP Servlet Context from " + servletContextFile
							+ " Failed", e);
		}

	}

	/**
	 * 增加一个开放的Service定义。
	 * 
	 * @param service 要开放的Service定义
	 * @emp:isChild com.ecc.emp.access.webservice.WebServiceInfo
	 */
	public void addWebService(WebServiceInfo service) {
		services.put(service.getServiceName(), service);
	}

	/**
	 * 检查响应SOAP Message是否包含失败信息。
	 * 
	 * @param msg SOAP Message
	 * @return 若包含失败信息，返回true
	 */
	public boolean containsFault(SOAPMessage msg) {
		try {
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPBody sb = se.getBody();
			return (sb.hasFault());
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.ERROR, 0,
					"Failed to check message fault!", e);
			return false;
		}
	}   
   
	/**
	 * 检查容器是否已初始化好。
	 * <p>
	 * 若没有，则对Servlet进行初始化。
	 * 
	 * @return 容器是否已初始化好
	 */
	private boolean checkInitialize() {
		
		if (this.componentFactory == null || componentFactory.isClosed()) 
		{
			synchronized (this) 
			{
				if (componentFactory != null && !componentFactory.isClosed())
					return true;

				componentFactory = (EMPFlowComponentFactory) ComponentFactory
						.getComponentFactory(factoryName);
				if (componentFactory == null) {
					return false;
				} else {
					try {
						this.doInit();
					} catch (Exception e) {

					}
					return true;
				}
			}
		}
		return true;
	}
		
	/**
	 * 在保持会话的同时，重新载入WebService Context配置文件。
	 */
	public void reloadServiceContext() {
		
		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Reload ServiceContext for " + this.getServletName());
		//reserve the sessions
		Map sessionMaps = null;
		if (this.getSessionManager() != null) {
			sessionMaps = this.getSessionManager().getSessions();
		}

		//remove old services;
		this.services = new HashMap();

		this.initWebServiceContext();

		//reload the session
		if (this.getSessionManager() != null && sessionMaps != null) {
			this.getSessionManager().setSessions(sessionMaps);
		}

		EMPLog.log(EMPConstance.EMP_WEBSERVICE, EMPLog.INFO, 0,
				"Reload ServiceContext for " + this.getServletName() + " OK!");
	}

	/**
	 * 将某个开放的Service设为可用状态。
	 * 
	 * @param serviceId 要启用的Service id
	 */
	public void enableService(String serviceId) {
		WebServiceInfo service = (WebServiceInfo) this.services.get(serviceId);
		if (service != null)
			service.setEnabled(true);
	}
	
	/**
	 * 将某个开放的Service设为不可用状态。
	 * 
	 * @param serviceId 要禁用的Service id
	 */
	public void disableService(String serviceId) {
		WebServiceInfo service = (WebServiceInfo) this.services.get(serviceId);
		if (service != null)
			service.setEnabled(false);
	}

	/**
	 * 判断某个开放的Service是否可用。
	 * 
	 * @param serviceId 要判断的Service id
	 * @return 该Service是否可用
	 */
	public boolean isServiceEnabled(String serviceId) {
		WebServiceInfo service = (WebServiceInfo) this.services.get(serviceId);
		if (service != null)
			return service.isEnabled();
		return false;
	}

	/**
	 * 以字符串数组形式返回当前所有Service的列表。
	 * 
	 * @return Service列表
	 */
	public String[] getServiceList() {
		Object[] ll = this.services.keySet().toArray();
		if (ll == null || ll.length == 0)
			return null;
		String[] tmp = new String[ll.length];

		for (int i = 0; i < ll.length; i++)
			tmp[i] = ll[i].toString();

		return tmp;
	}

	/**
	 * 获得会话管理器。
	 * 
	 * @return 会话管理器
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	/**
	 * 注入会话管理器。
	 * 
	 * @param sessionManager 会话管理器
	 * @emp:isChild com.ecc.emp.session.SessionManager
	 */
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * 获得访问控制器。
	 * 
	 * @return 访问控制器
	 */
	public AccessManager getAccessManager() {
		return accessManager;
	}

	/**
	 * 注入访问控制器。
	 * 
	 * @param accessManager 访问控制器
	 * @emp:isChild com.ecc.emp.accesscontrol.AccessManager
	 */
	public void setAccessManager(AccessManager accessManager) {
		this.accessManager = accessManager;
	}
	   
}
