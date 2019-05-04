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
 * HTTP�������봦��Servlet��
 * <p>
 * EMP������ͨ��HTTP�����ͱ��ĵķ�ʽ������ĳ��EMPҵ���߼�����(�ڴ˳�ΪService)��
 * ��һ��HttpAccessServletContext�����ļ��������Է��ʵ�ҵ���߼��������塣
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
	 * EMPҵ���߼�����ʵ��������
	 */
	private EMPFlowComponentFactory componentFactory;

	/**
	 * EMPҵ���߼�����ʵ������������
	 */
	private String factoryName;

	/**
	 * EMPҵ���߼�����ʵ���������Ĺ��������ļ�������Ӧweb.xml��Servlet�ĳ�ʼ������iniFile
	 */
	private String empIniFileName;

	/**
	 * HTTP�������������ļ��ĸ�Ŀ¼��Ĭ��ΪWebӦ�õ�Context��Ŀ¼
	 */
	private String rootPath;
	
	/**
	 * HTTP��������
	 */
	private HttpRequestHandler httpRequestHandler;

	/**
	 * �Ự������
	 */
	private SessionManager sessionManager;

	/**
	 * ��ż����ݻỰID����
	 */
	private String sessionIdField;
	
	/**
	 * ���ʿ�����
	 */
	private AccessManager accessManager;

	/**
	 * ���пɹ�HTTP���ʵ�Service����
	 */
	private Map services;

	/**
	 * Servlet����ڷ�����
	 * <p>
	 * ���ȼ�������Ƿ��ѳ�ʼ���ã����û�г�ʼ���ã�������ʾ��Ϣ��
	 * Ȼ���ڿ���ҵ���߼�����ӳ���в����������ҵ���߼�������ִ�С�
	 * ��ִ��ǰ��ͨ��<tt>HttpRequestHandler</tt>������/��Ӧ���ݽ��д���
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) {
		
		Integer nIfExecuteFinalBiz = 0;
		String sessionId = "-1";

		String reqURI = request.getRequestURI();

		PBankHTTPRequestService reqService = null;

		Session session = null;
		Context sessionContext = null;

		// ���ʿ��������صĶ���
		Object accessObj = null;
		long beginTimeStamp = System.currentTimeMillis();

		try {
			EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.INFO, 0,
					"Accept new request from: " + request.getRemoteAddr());
			if (httpRequestHandler == null)
				throw new EMPException("HttpRequest handler not set for EMPHttpAccessServlet!");

			// ��������Ƿ��ѳ�ʼ���ã����û�г�ʼ���ã�������ʾ��Ϣ
			if (!this.checkInitialize()) {
				throw new EMPException("Container not initialized!");
			}

			// ������������
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

			if (reqService.isSessionService() && sessionManager != null) // ����session
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

			// ���ʿ���
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

			// ִ��ʧ�ܣ�ȡ��session
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
	 * �ر�Servlet��
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
	 * Servlet��ʼ����ڷ�����
	 * <p>
	 * ʹ��web.xml�е����ó�ʼ��Servlet��
	 * 
	 * @param config Servlet��ʼ������
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
	 * Servlet��ʼ��ʵ�ַ�����
	 * <p>
	 * ��ȡ��ʼ����������ȡ��ָ��ҵ���߼����������
	 * ����ָ�������ļ�ʵ�����µ����������
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
	 * HTTP������ģ�ͳ�ʼ��������
	 * <p>
	 * ����HttpAccessServletContext�����ļ���
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
	 * ����һ�����ŵ�Service���塣
	 * 
	 * @param service Ҫ���ŵ�Service����
	 * @isChild com.ecc.emp.access.http.HTTPRequestService
	 */
	public void addHTTPRequestService(HTTPRequestService service) {
		
		services.put(service.getServiceName(), service);
		service.setComponentFactory(this.componentFactory);
	}

	/**
	 * ��������Ƿ��ѳ�ʼ���á�
	 * <p>
	 * ��û�У����Servlet���г�ʼ����
	 * 
	 * @return �����Ƿ��ѳ�ʼ����
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
	 * ��ûỰ��������
	 * 
	 * @return �Ự������
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	/**
	 * ע��Ự��������
	 * 
	 * @param sessionManager �Ự������
	 * @emp:isChild com.ecc.emp.session.SessionManager
	 */
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * ���HTTP����������
	 * 
	 * @return HTTP��������
	 */	
	public HttpRequestHandler getHttpRequestHandler() {
		return httpRequestHandler;
	}

	/**
	 * ע��HTTP����������
	 * 
	 * @param httpRequestHandler HTTP��������
	 * @emp:isChild com.ecc.emp.access.http.HttpRequestHandler
	 */
	public void setHttpRequestHandler(HttpRequestHandler httpRequestHandler) {
		this.httpRequestHandler = httpRequestHandler;
	}

	/**
	 * ��÷��ʿ�������
	 * 
	 * @return ���ʿ�����
	 */
	public AccessManager getAccessManager() {
		return accessManager;
	}

	/**
	 * ע����ʿ�������
	 * 
	 * @param accessManager ���ʿ�����
	 * @emp:isChild com.ecc.emp.accesscontrol.AccessManager
	 */
	public void setAccessManager(AccessManager accessManager) {
		this.accessManager = accessManager;
	}

	/**
	 * ��ĳ�����ŵ�Service��Ϊ����״̬��
	 * 
	 * @param serviceId Ҫ���õ�Service id
	 */
	public void enableService(String serviceId) {
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
		if (svc != null)
			svc.setEnabled(true);
	}

	/**
	 * ��ĳ�����ŵ�Service��Ϊ������״̬��
	 * 
	 * @param serviceId Ҫ���õ�Service id
	 */
	public void disableService(String serviceId) {
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
		if (svc != null)
			svc.setEnabled(false);
	}

	/**
	 * �ж�ĳ�����ŵ�Service�Ƿ���á�
	 * 
	 * @param serviceId Ҫ�жϵ�Service id
	 * @return ��Service�Ƿ����
	 */
	public boolean isServiceEnabled(String serviceId) {
		HTTPRequestService svc = (HTTPRequestService) services.get(serviceId);
		if (svc != null)
			return svc.isEnabled();
		return false;
	}

	/**
	 * ���ַ���������ʽ���ص�ǰ����Service���б�
	 * 
	 * @return Service�б�
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
	 * �ڱ��ֻỰ��ͬʱ����������HttpAccessServletContext�����ļ���
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
