package core.communication.access.esb;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.access.tcpip.TCPIPRequestHandler;
import com.ecc.emp.access.tcpip.TCPIPRequestService;
import com.ecc.emp.accesscontrol.AccessManager;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.component.factory.ServletContextFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.session.Session;
import com.ecc.emp.session.SessionManager;
import common.util.SFConst;
import common.util.SFUtil;

import core.communication.tcpip.PackageProcessor;
import core.communication.tcpip.TCPIPServerService;
import core.communication.tcpip.TCPIPService;
import core.log.SFLogger;





/**
 * TCP/IP渠道接入处理Servlet。
 * <p>
 * EMP允许以通过TCP/IP请求发送报文的方式，访问某个EMP业务逻辑构件(在此称为Service)。
 * 由一份TcpipAccessServletContext配置文件描述可以访问的业务逻辑构件定义。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-24
 * @lastmodified 2008-6-12
 * 
 */
public class PBankTCPIPServiceServlet extends HttpServlet implements
		PackageProcessor {

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
	 * TCP/IP渠道访问配置文件的根目录，默认为Web应用的Context根目录
	 */
	private String rootPath;

	/**
	 * TCP/IP请求处理器
	 */
	private TCPIPRequestHandler requestHandler;

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
	 * 所有可供TCP/IP访问的Service定义
	 */
	private Map services;

	/**
	 * Servlet所使用的TCP/IP访问服务，用于监听端口
	 */
	private List TCPIPServices;

	public PBankTCPIPServiceServlet() {
		super();
		TCPIPServices = new ArrayList();
	}

	/**
	 * Servlet的入口方法。
	 * <p>
	 * 检查容器是否已初始化好，如果没有初始化好，返回提示信息。
	 * 实际TCP/IP的请求并不通过HTTP协议访问Servlet，因此该方法并无实际逻辑处理内容
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
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
	 * 关闭Servlet。
	 */
	public void destroy() {
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Destory the servlet: " + this.getServletName());

		if (this.sessionManager != null)
			sessionManager.terminate();

		if (empIniFileName != null)
			componentFactory.close();

		for (int i = 0; i < this.TCPIPServices.size(); i++) {
			TCPIPServerService svc = (TCPIPServerService) TCPIPServices.get(i);
			svc.terminate();
		}
	}

	/**
	 * Servlet初始化入口方法。
	 * <p>
	 * 使用web.xml中的配置初始化Servlet。
	 * 
	 * @param config Servlet初始化参数
	 * @throws javax.servlet.ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Start up  the servlet: " + this.getServletName());
		doInit();

		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Start up  the servlet: " + this.getServletName() + " OK!");
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

		// Use the user setting factory root Path
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

		factoryName = getInitParameter("factoryName");
		empIniFileName = this.getInitParameter("iniFile");

		sessionIdField = getInitParameter("sessionIdField");

		String rootContextName = getInitParameter("rootContextName");

		try {
			if (empIniFileName != null) {
				componentFactory = new EMPFlowComponentFactory();
				SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
						"Initialize EMP Flow Context from " + empIniFileName);
				componentFactory.setName("EMPFlow");
				componentFactory.setRootContextName(rootContextName);
				componentFactory.initializeComponentFactory(factoryName,
						rootPath + empIniFileName);
				// Context rootContext = componentFactory.getContext(null, rootContextName);

			} else {
				SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Share EMP flow Context from: " + factoryName);
				componentFactory = (EMPFlowComponentFactory) ComponentFactory.getComponentFactory(factoryName);
			}

		} catch (Exception e) {
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Initialize EMP Request Servlet from " + empIniFileName	+ " failed!"+ e);
			throw new ServletException("Failed to initialize the EMP context ",	e);
		}

		initTCPIPAccessContext();
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Initialize EMP Request Servlet from " + empIniFileName+ " OK!");
	}

	/**
	 * TCP/IP请求处理模型初始化方法。
	 * <p>
	 * 解析TcpipAccessServletContext配置文件。
	 */
	private void initTCPIPAccessContext() {
		String servletContextFile = getInitParameter("servletContextFile");
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Initialize EMP Servlet Context from " + servletContextFile);
		ServletContextFactory ctxFactory = new ServletContextFactory();
		ctxFactory.initializeComponentFactory("contextFactory", rootPath + servletContextFile);

		try {
			ctxFactory.parseTheContext(this);

			ctxFactory.exportMBean(this, this.getServletName());

			for (int i = 0; i < this.TCPIPServices.size(); i++) {
				TCPIPServerService svc = (TCPIPServerService) TCPIPServices.get(i);
				svc.setPackageProcessor(this);
				svc.startUp();
			}

			SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Initialize EMP Servlet Context from " + servletContextFile	+ " OK!");
		} catch (Exception e) {
			SFLogger.error(SFConst.DEFAULT_TRXCODE, null,"Initialize EMP Servlet Context from " + servletContextFile+ " Failed", e);
		}
	}

	/**
	 * 增加一个TCP/IP服务端服务，用于监听端口。
	 * 
	 * @param service TCP/IP服务端服务
	 * @isChild com.ecc.emp.tcpip.TCPIPServerService
	 */
	public void addTCPIPServerService(TCPIPServerService service) {
		TCPIPServices.add(service);
	}
	
	/**
	 * 增加一个开放的Service定义。
	 * 
	 * @param svc 要开放的Service定义
	 * @isChild com.ecc.emp.access.tcpip.TCPIPRequestService
	 */
	public void addTCPIPRequestService(TCPIPRequestService svc) {
		services.put(svc.getServiceName(), svc);
		svc.setComponentFactory(this.componentFactory);
	}

	/**
	 * 检查容器是否已初始化好。
	 * <p>
	 * 若没有，则对Servlet进行初始化。
	 * 
	 * @return 容器是否已初始化好
	 */
	private boolean checkInitialize() {
		if (this.componentFactory == null || componentFactory.isClosed()) {
			synchronized (this) {
				if (componentFactory != null && !componentFactory.isClosed())
					return true;

				componentFactory = (EMPFlowComponentFactory) ComponentFactory
						.getComponentFactory(factoryName);
				if (componentFactory == null) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * 判断报文包是否为合法的请求数据。
	 * 
	 * @param msg 报文包
	 * @return msg是否合法的请求数据
	 */
	public boolean isRequestPackage(byte[] msg) {
		return this.requestHandler.isRequestPackage(msg);
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
	 * 获得TCP/IP请求处理器。
	 * 
	 * @return TCP/IP请求处理器
	 */	
	public TCPIPRequestHandler getTCPIPRequestHandler() {
		return requestHandler;
	}
	
	/**
	 * 注入TCP/IP请求处理器。
	 * 
	 * @param handler TCP/IP请求处理器
	 * @emp:isChild com.ecc.emp.access.tcpip.TCPIPRequestHandler
	 */
	public void setTCPIPRequestHandler(TCPIPRequestHandler handler) {
		requestHandler = handler;
	}

	/**
	 * 接收到新数据包的处理接口。
	 * <p>
	 * 当通信服务接收到新的数据包时，调用此接口，
	 * 
	 * @param msg 接收到的数据包
	 * @param service TCPIPService服务对象
	 * @param socket 套接字连接
	 * @return 固定返回null
	 */
	public byte[] processNewPackage(byte[] msg, TCPIPService service,
			Socket socket) throws EMPException {

		
		String sessionId = "-1";

		// 访问控制器返回的对象
		Object accessObj = null;

		long beginTimeStamp = System.currentTimeMillis();

		Session session = null;
		Context sessionContext = null;
		TCPIPRequestService svc = null;

		EMPTCPIPRequest request = null;

		try {
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
					"Accept new request from: "
							+ socket.getRemoteSocketAddress().toString());
			if (requestHandler == null)
				throw new EMPException(
						"TCPIPRequest handler not set for EMPTCPIPServiceServlet!");

			// 检查容器是否已初始化好，如果没有初始化好，返回提示信息
			if (!this.checkInitialize()) {
				throw new EMPException("Container not initialized!");
			}

			request = requestHandler.getTCPIPRequest(msg);

			// 处理请求数据
			String svcId = (String) request.getAttribute("serviceId"); // requestHandler.getRequestServiceName(msg);
			if (svcId == null)
				throw new EMPException(
						"Execute serviceName not set for TcpipAccessServlet!");

			//SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Required to execute Service: " + svcId);

			svc = (TCPIPRequestService) this.services.get(svcId);

			if (sessionManager != null) {
				sessionId = (String) request.getAttribute("SID");

				session = sessionManager.getSession(sessionId, null, false);
				if (session != null)
					sessionContext = (Context) session
							.getAttribute(EMPConstance.ATTR_CONTEXT);

				request.setAttribute(EMPConstance.ATTR_SESSION_CONTEXT,
						sessionContext);
			}

			if (svc == null)
				throw new EMPException("Required to execute TCPIPService: "
						+ svcId + " not defined!");

			if (!svc.isEnabled())
				throw new EMPException("tcpip Service " + svcId
						+ " is not enabled!");

			if (session == null && svc.isCheckSession()) {
				if (!svc.isSessionService())
					throw new EMPException(
							"session time out or not established!");
			}

			if (svc.isSessionService() && sessionManager != null) // 创建session
			{
				String sessionContextId = svc.getSessionContextName();
				sessionContext = this.componentFactory.getContext(null,
						sessionContextId);

				session = sessionManager.getSession(null, null, true);
				session.setAttribute(EMPConstance.ATTR_CONTEXT, sessionContext);
				sessionId = session.getId();

				if (sessionIdField != null) {
					try {
						SFUtil.setDataValue(sessionContext, sessionIdField, sessionId);//  sessionContext.setDataValue(sessionIdField, sessionId);
					} catch (Exception e) {
						SFUtil.addDataField(sessionContext, sessionIdField, sessionId);// sessionContext.addDataField(sessionIdField, sessionId);
					}
				}

				request.setAttribute(EMPConstance.ATTR_SESSION_CONTEXT,
						sessionContext);
			}

			// 访问控制
			if (this.accessManager != null) {
				if(sessionContext != null)
					accessObj = accessManager.checkAccess(sessionContext, request,
							svcId);
				else{
					Context rootContext = this.componentFactory.getContextNamed(this.componentFactory.getRootContextName());
					accessObj = accessManager.checkAccess(rootContext, request,
							svcId);
				}
				if (accessObj != null)
					accessManager.beginAccess(accessObj);
			}

			// 处理请求
			svc.setComponentFactory(this.componentFactory);
			byte[] retMsg = svc.handleRequest(request, sessionContext);

//			byte[] retPackage = requestHandler.getResponsePackage(request,
//					retMsg); // getResponsePackage(sessionId, svcResult);
			
			//是否返回响应报文
			Context context = (Context) request.getAttribute(EMPConstance.ATTR_CONTEXT);
			if(SFConst.RET_RESP_YES.equals( SFUtil.getDataValue(context, SFConst.PUBLIC_IS_RET_RESP ))){
				// 返回请求结果
				if(retMsg != null){
					retMsg = service.send(retMsg, socket);
					SFLogger.info(context, "ESB应答报文：["+ new String(retMsg) + "]");
				}
			}
			
			

			if (svc.isEndSessionService() && sessionManager != null) {
				sessionManager.removeSession(session);
				sessionContext.terminate();
			}

		} catch (Exception e) {
			e.printStackTrace();
			//SFLogger.error(SFConst.DEFAULT_TRXCODE, null,"service failed to handle TCPIP request!" , e);
			// execute failed remove the established session
			if (svc != null && svc.isSessionService() && sessionManager != null) 
			{
				sessionManager.removeSession(session);
				sessionContext.terminate();
			}

			byte[] retMsg = requestHandler.getExceptionResponse(request, e);
			if(retMsg != null){
				try {
					retMsg = service.send(retMsg, socket);
					Context context = (Context) request.getAttribute(EMPConstance.ATTR_CONTEXT);
					SFLogger.info(context, "ESB异常返回报文内容：["+ new String(retMsg) + "]");
				} catch (Exception ee) {
					SFLogger.error(SFConst.DEFAULT_TRXCODE, null, "service failed!", ee);
					
					
				}
			}
		} finally {
			if (this.accessManager != null && accessObj != null)
				accessManager.endAccess(accessObj, beginTimeStamp);

			Context context = (Context) request
					.getAttribute(EMPConstance.ATTR_CONTEXT);
			if (context != null)
				context.terminate();
		}
		return null;
	}

	/**
	 * 将某个开放的Service设为可用状态。
	 * 
	 * @param serviceId 要启用的Service id
	 */
	public void enableService(String serviceId) {
		TCPIPRequestService svc = (TCPIPRequestService) this.services
				.get(serviceId);
		if (svc != null)
			svc.setEnabled(true);

	}

	/**
	 * 将某个开放的Service设为不可用状态。
	 * 
	 * @param serviceId 要禁用的Service id
	 */
	public void disableService(String serviceId) {
		TCPIPRequestService svc = (TCPIPRequestService) this.services
				.get(serviceId);
		if (svc != null)
			svc.setEnabled(false);

	}

	/**
	 * 判断某个开放的Service是否可用。
	 * 
	 * @param serviceId 要判断的Service id
	 * @return 该Service是否可用
	 */
	public boolean isServiceEnabled(String serviceId) {
		TCPIPRequestService svc = (TCPIPRequestService) this.services
				.get(serviceId);
		if (svc != null)
			return svc.isEnabled();
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

	/**
	 * 在保持会话的同时，重新载入TcpipAccessServletContext配置文件。
	 */
	public void reloadServiceContext() {
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
				"Reload ServiceContext for " + this.getServletName());

		// reserve the sessions
		Map sessionMaps = null;
		if (this.getSessionManager() != null) {
			sessionMaps = this.getSessionManager().getSessions();
		}

		// remove old services;
		this.services = new HashMap();

		this.initTCPIPAccessContext();

		// reload the session
		if (this.getSessionManager() != null && sessionMaps != null) {
			this.getSessionManager().setSessions(sessionMaps);
		}

		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
				"Reload ServiceContext for " + this.getServletName() + " OK!");
	}

	/**
	 * 将自身注册为MBean，供监控系统操作。
	 *//*
	public void registAsMBean() {
		EMPMBeanInfo beanInfo = new EMPMBeanInfo();
		beanInfo.setId(this.getServletName());
		beanInfo.setName(this.getServletName());
		beanInfo.setDisplayName(this.getServletName());
		beanInfo.setDescription("the application init Component");
		beanInfo.setType("SERVLET");

		AttrInfo attr1 = new AttrInfo("rootPath", "R", "rootPath",
				"Rootpath of Application");
		beanInfo.addAttrInfo(attr1);
		AttrInfo attr3 = new AttrInfo("factoryName", "R", "factoryName",
				"Component factory name");
		beanInfo.addAttrInfo(attr3);

		// OperationInfo(String opName,
		// String displayName,
		// String[]paramNames, String
		// description )
		OperationInfo opInfo0 = new OperationInfo("reloadServiceContext",
				"reloadServiceContext", null,
				"reload the tcpipservice components");
		beanInfo.addOperationInfo(opInfo0);

		OperationInfo opInfo = new OperationInfo("enableService",
				"enableService", new String[] { "serviceName" },
				"enabled tcpip channel service");
		beanInfo.addOperationInfo(opInfo);

		OperationInfo opInfo1 = new OperationInfo("disableService",
				"disableService", new String[] { "serviceName" },
				"disabled tcpip channel service");
		beanInfo.addOperationInfo(opInfo1);

		OperationInfo opInfo2 = new OperationInfo("getServiceList",
				"getServiceList", null, "get service Lists");
		beanInfo.addOperationInfo(opInfo2);

		OperationInfo opInfo3 = new OperationInfo("isServiceEnabled",
				"isServiceEnabled", new String[] { "serviceName" },
				"tcpip channel service is or not enabled");
		beanInfo.addOperationInfo(opInfo3);

		if (EMPJMXManager.getInstance() != null)
			EMPJMXManager.getInstance().doRegistMBean(this, beanInfo);
	}*/
}
