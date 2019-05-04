package core.communication.access.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dc.eai.data.CompositeData;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.access.http.HTTPRequestService;
import com.ecc.emp.access.http.HttpRequestHandler;
import com.ecc.emp.accesscontrol.AccessManager;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.component.factory.ServletContextFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.session.Session;
import com.ecc.emp.session.SessionManager;

/**
 * HTTP渠道接入处理Servlet。
 * <p>
 * EMP允许以通过HTTP请求发送报文的方式，访问某个EMP业务逻辑构件(在此称为Service)。
 * 由一份HttpAccessServletContext配置文件描述可以访问的业务逻辑构件定义。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-24
 * @lastmodified 2008-6-12
 * 
 */
public class PBankHttpAccessServlet extends HttpServlet {

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
	 * HTTP渠道访问配置文件的根目录，默认为Web应用的Context根目录
	 */
	private String rootPath;
	
	/**
	 * HTTP请求处理器
	 */
	private HttpRequestHandler httpRequestHandler;

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
	 * 所有可供HTTP访问的Service定义
	 */
	private Map services;

	/**
	 * Servlet的入口方法。
	 * <p>
	 * 首先检查容器是否已初始化好，如果没有初始化好，返回提示信息。
	 * 然后在开放业务逻辑构件映射中查找所请求的业务逻辑构件并执行。
	 * 在执行前后通过<tt>HttpRequestHandler</tt>对请求/响应数据进行处理。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) {
		
		Integer nIfExecuteFinalBiz = 0;
		String sessionId = "-1";

		String reqURI = request.getRequestURI();

		PBankHTTPRequestService reqService = null;

		Session session = null;
		Context sessionContext = null;

		// 访问控制器返回的对象
		Object accessObj = null;
		long beginTimeStamp = System.currentTimeMillis();

		try {
			EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
					"Accept new request from: " + request.getRemoteAddr());
			if (httpRequestHandler == null)
				throw new EMPException("HttpRequest handler not set for EMPHttpAccessServlet!");

			// 检查容器是否已初始化好，如果没有初始化好，返回提示信息
			if (!this.checkInitialize()) {
				throw new EMPException("Container not initialized!");
			}

			// 处理请求数据
			httpRequestHandler.parseRequest(request);
			
			String serviceName = httpRequestHandler.getRequestServiceName(request);

			if (sessionManager != null) {
				// sessionId = httpRequestHandler.getSessionId(request);
				session = sessionManager.getSession(request, response, false);
				if (session != null)
					sessionContext = (Context) session.getAttribute(EMPConstance.ATTR_CONTEXT);
				request.setAttribute(EMPConstance.ATTR_SESSION, session);
				// sessionId = session.getId();
			}

			if (serviceName == null)
				throw new EMPException("Execute serviceName not set for httpAccessServlet!");

			//EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0, "Required to execute Service: " + serviceName);

			reqService = (PBankHTTPRequestService) this.services.get(serviceName);

			if (reqService == null) {
				EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.ERROR, 0, 
						"EMPHTTPServlet [" + serviceName + "] receive request :[" + 
						PackUtil.packXmlStr((CompositeData)request.getAttribute("reqData")) + "]");
				throw new EMPException("Required to execute HTTPAccessService: " + serviceName	+ " not defined!");
			}

			if (!reqService.isEnabled())
				throw new EMPException("HTTP service " + serviceName + " is not enabled to access!");

			if (session == null && reqService.isCheckSession()) {
				if (!reqService.isSessionService())
					throw new EMPException("session time out or not established!");
			}

			if (reqService.isSessionService() && sessionManager != null) // 创建session
			{
				String sessionContextId = reqService.getSessionContextName();
				sessionContext = this.componentFactory.getContext(null, sessionContextId);

				session = sessionManager.getSession(null, null, true);
				session.setAttribute(EMPConstance.ATTR_CONTEXT, sessionContext);
				sessionId = session.getId();
				if (sessionIdField != null) {
					try {
						sessionContext.setDataValue(sessionIdField, sessionId);
					} catch (Exception e) {
						sessionContext.addDataField(sessionIdField, sessionId);
					}
				}
			}

			// 访问控制
			if (this.accessManager != null) {
				if(sessionContext != null)
					accessObj = accessManager.checkAccess(sessionContext, request, reqService.getServiceName());
				else{
					Context rootContext = this.componentFactory.getContextNamed(this.componentFactory.getRootContextName());
					accessObj = accessManager.checkAccess(rootContext, request,	reqService.getServiceName());
				}
				if (accessObj != null)
					accessManager.beginAccess(accessObj);
			}

			String retMsg = null;

			retMsg = reqService.handleRequest(request, response, sessionContext, sessionId);

			httpRequestHandler.handleResponse(request, response, retMsg, reqURI, sessionId);
			
			nIfExecuteFinalBiz = 0;
			if (reqService.isResponseFirst())
			{
				response.getWriter().close();
				nIfExecuteFinalBiz = 1;
				reqService.handleRequestForFinalBiz(request, response, sessionContext, sessionId);
			}
			
			if (reqService.isEndSessionService() && sessionManager != null) {
				sessionManager.removeSession(session);
				sessionContext.terminate();
			}

		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.ERROR, 0, "service failed!", e);

			// 执行失败，取消session
			if (reqService != null && reqService.isSessionService()	&& sessionManager != null) {
				sessionManager.removeSession(session);
				sessionContext.terminate();
			}

			if (sessionManager != null)
				reqURI = sessionManager.encodeURL(request, response, reqURI, "POST");
			
			if (httpRequestHandler != null && nIfExecuteFinalBiz != 1)
				httpRequestHandler.handleException(request, response, e, reqURI, sessionId);
		} finally {
			if (this.accessManager != null && accessObj != null)
				accessManager.endAccess(accessObj, beginTimeStamp);
			
			Context context = (Context) request.getAttribute(EMPConstance.ATTR_CONTEXT);
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
	 * 使用web.xml中的配置初始化Servlet。
	 * 
	 * @param config Servlet初始化参数
	 * @throws javax.servlet.ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
				"Start up  the servlet: " + this.getServletName());
		doInit();
		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
				"Start up  the servlet: " + this.getServletName() + " OK!");
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

		sessionIdField = getInitParameter("sessionIdField");

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

		String rootContextName = getInitParameter("rootContextName");

		try {
			if (empIniFileName != null) {
				componentFactory = new EMPFlowComponentFactory();
				EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Initialize EMP Flow Context from " + empIniFileName, null);
				componentFactory.setName("EMPFlow");
				componentFactory.setRootContextName(rootContextName);
				componentFactory.initializeComponentFactory(factoryName, rootPath + empIniFileName);
				// Context rootContext =
				// componentFactory.getContext(null,
				// rootContextName);

			} else {
				EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Share EMP flow Context from: " + factoryName);
				componentFactory = (EMPFlowComponentFactory) ComponentFactory.getComponentFactory(factoryName);
			}

		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
					"Initialize EMP Request Servlet from " + empIniFileName	+ " failed!", e);
			throw new ServletException("Failed to initialize the EMP context ",	e);
		}

		initHttpReqContext();
		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
				"Initialize EMP Request Servlet from " + empIniFileName	+ " OK!", null);
	}

	/**
	 * HTTP请求处理模型初始化方法。
	 * <p>
	 * 解析HttpAccessServletContext配置文件。
	 */
	private void initHttpReqContext() {

		String servletContextFile = getInitParameter("servletContextFile");

		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
				"Initialize EMP Servlet Context from " + servletContextFile, null);
		ServletContextFactory ctxFactory = new ServletContextFactory();
		ctxFactory.initializeComponentFactory("contextFactory", rootPath + servletContextFile);

		try {
			ctxFactory.parseTheContext(this);
			ctxFactory.exportMBean(this, this.getServletName());

			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
					"Initialize EMP Servlet Context from " + servletContextFile	+ " OK!", null);
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0,
					"Initialize EMP Servlet Context from " + servletContextFile	+ " Failed", e);
		}
	}

	/**
	 * 增加一个开放的Service定义。
	 * 
	 * @param service 要开放的Service定义
	 * @isChild com.ecc.emp.access.http.HTTPRequestService
	 */
	public void addHTTPRequestService(HTTPRequestService service) {
		
		services.put(service.getServiceName(), service);
		service.setComponentFactory(this.componentFactory);
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

				componentFactory = (EMPFlowComponentFactory) ComponentFactory.getComponentFactory(factoryName);
				if (componentFactory == null) {
					return false;
				} else {
					// try {
					// this.doInit();
					// } catch
					// (Exception e) {
					//
					// }
					return true;
				}
			}
		}
		return true;
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
	 * 获得HTTP请求处理器。
	 * 
	 * @return HTTP请求处理器
	 */	
	public HttpRequestHandler getHttpRequestHandler() {
		return httpRequestHandler;
	}

	/**
	 * 注入HTTP请求处理器。
	 * 
	 * @param httpRequestHandler HTTP请求处理器
	 * @emp:isChild com.ecc.emp.access.http.HttpRequestHandler
	 */
	public void setHttpRequestHandler(HttpRequestHandler httpRequestHandler) {
		this.httpRequestHandler = httpRequestHandler;
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
	 * 将某个开放的Service设为可用状态。
	 * 
	 * @param serviceId 要启用的Service id
	 */
	public void enableService(String serviceId) {
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
		if (svc != null)
			svc.setEnabled(true);
	}

	/**
	 * 将某个开放的Service设为不可用状态。
	 * 
	 * @param serviceId 要禁用的Service id
	 */
	public void disableService(String serviceId) {
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
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
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
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
	 * 在保持会话的同时，重新载入HttpAccessServletContext配置文件。
	 */
	public void reloadServiceContext() {
		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
				"Reload ServiceContext for " + this.getServletName());

		this.services = new HashMap();

		Map sessionMaps = null;
		if (this.getSessionManager() != null) {
			sessionMaps = this.getSessionManager().getSessions();
		}

		this.initHttpReqContext();

		if (this.getSessionManager() != null && sessionMaps != null) {
			this.getSessionManager().setSessions(sessionMaps);
		}

		EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
				"Reload ServiceContext for " + this.getServletName() + " OK!");
	}

}
