package core.emp.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.accesscontrol.AccessInfo;
import com.ecc.emp.accesscontrol.AccessManager;
import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.component.factory.ServletContextFactory;
import com.ecc.emp.component.xml.XMLDocumentLoader;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jmx.support.EMPJMXManager;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.session.Session;
import com.ecc.emp.session.SessionManager;
import com.ecc.emp.web.jsptags.ResourceDefine;
import com.ecc.emp.web.multipart.MultipartHttpServletRequest;
import com.ecc.emp.web.multipart.MultipartResolver;
import com.ecc.emp.web.servlet.Initializer;
import com.ecc.emp.web.servlet.LocaleResolver;
import com.ecc.emp.web.servlet.ModelAndView;
import com.ecc.emp.web.servlet.mvc.BizLogicException;
import com.ecc.emp.web.servlet.mvc.ExceptionHandler;
import com.ecc.emp.web.servlet.mvc.InvalidInputException;
import com.ecc.emp.web.servlet.view.View;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * EMP请求的总体入口Servlet。
 * <p>
 * 该Servlet对应一个ServletContext配置文件（MVC模型文件），具体文件名由web.xml中的初始化参数指定。
 * 配置文件中定义了MVC模型以及一些相关处理器（会话管理、访问控制等）。
 * <p>
 * 当Servlet接收请求后，根据MVC模型进行相应的处理。
 * EMP所实现的处理方式是：每个MVC模型的定义对应一个控制器Controller实例，
 * 所有请求与Controller的映射关系保存在请求分发器当中。
 * 当Servlet接到用户的请求时，由分发器通过请求URL找到相应控制器，
 * 然后由控制器进行业务逻辑的调用以及页面跳转控制。
 * 最后Servlet将处理后的数据模型以及视图（页面）返回给客户端展现。
 * <p>
 * 在一个应用中，允许多个配置了不同的MVC模型的Servlet对应同一套业务逻辑处理模型，响应不同的请求。
 */
public class EMPRequestServlet extends HttpServlet {

	SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
	
	private static final long serialVersionUID = 1L;
		
	/**
	 * 配置文件的根目录，默认为Web应用的Context根目录
	 */
	private String rootPath;
	
	/**
	 * 外部字符串资源定义
	 */
	private Map resources;
    
   /**
	 * 用户设定的初始化处理器。
	 * 在完成所有的系统初始化后，调用此接口，完成用户系统需要的初始化
	 */
	private Initializer initializer;

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
	 * 可以为一个Servlet配置多个RequestDispatcher，每个Dispatcher配置相应的处理拦截器，
	 * 便于扩展和访问控制。
	 */
	private ArrayList requestDispatchers;

	/**
	 * 默认的请求分配器，在不需要配置拦截器的情况下，使用默认的分配器
	 */
	//private FBSEMPRequestDispatcher defaultDispatcher;

	
	private String sessionManagerName;
	
	/**
	 * 会话管理器
	 */
	private SessionManager sessionManager;

	/**
	 * 异常处理器
	 */
	private ExceptionHandler exceptionHandler;

	/**
	 * 访问控制器
	 */
	private AccessManager accessManager;

	/**
	 * 监控状态
	 */
	private String state = "normal";
	
	/**
	 * 是否启动监控信息采集
	 */
	private boolean monitorStarted = false;

	/**
	 * 访问信息
	 */
	private AccessInfo accessInfo = new AccessInfo();

	/**
	 * 需要报警的响应时间，即当响应时间超过此值时向监控系统报警
	 */
	private long alarmResponseTime = 20000;
	
	/**
	 * JSP根路径
	 */
	private String jspRootPath;

	/**
	 * 用户的语言环境处理器
	 */
	private LocaleResolver localeResolver;

	/**
	 * 文件上传处理器
	 */
	private MultipartResolver multipartResolver;

	public EMPRequestServlet() {
	}
	
	/**
	 * 向请求添加必要的属性定义，供表现逻辑处理需要。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 */
	protected void addRequiredAttributes(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute(EMPConstance.ATTR_RESOURCE, resources );

		request.setAttribute(EMPConstance.ATTR_ROOTPATH, rootPath );
		request.setAttribute(EMPConstance.ATTR_DATA_TYPE, this.componentFactory.getDataTypeDefine() );
		request.setAttribute(EMPConstance.ATTR_SESSION_MGR, this.sessionManager );
		// add by liup 往请求中添加项目名称
		request.setAttribute(SFConst.SYS_FACTORY_NAME, request.getContextPath().substring(1) );
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

				componentFactory = (EMPFlowComponentFactory) ComponentFactory.getComponentFactory(factoryName);
				if (componentFactory == null) 
				{
					return false;
				} 
				else 	//使用公共的Factory，且公共factory已重启
				{
					try {
						
						   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Share EMP flow Context from: " + factoryName);
						   componentFactory = (EMPFlowComponentFactory)ComponentFactory.getComponentFactory(factoryName);
						   if( componentFactory != null )
						   {
							   try{
								   Context context = componentFactory.getContextNamed( componentFactory.getRootContextName() );
								   SessionManager sm = (SessionManager)context.getService( sessionManagerName );
								   this.sessionManager = sm;

								   if( this.initializer != null )
								   {
//									   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Invoker the Initializer...");
									   logger.info("Invoker the Initializer...");
									   initializer.initialize( componentFactory );
								   }
								   
							   }catch(Exception e)
							   {
//								   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, "Failed to get sessionManager!", e);
								  logger.error("Failed to get sessionManager!");
							   }
						   }
						
						
						
//						this.doInit();
					} catch (Exception e) {

					}
					return true;
				}
			}
		}
		return true;	
	
	}

	/**
	 * Servlet的入口方法。
	 * <p>
	 * 首先检查容器是否已初始化好，如果没有初始化好，返回提示信息。
	 * 然后通过分发器找到请求对应的控制器，并调用控制器的处理，
	 * 最后将经过处理的数据模型和视图作为响应返回给前端。
	 * 
	 * @see #doGet(HttpServletRequest, HttpServletResponse)
	 * @see #doPost(HttpServletRequest, HttpServletResponse)
	 * @param request HTTP请求
	 * @param response HTTP响应
	 */	
	public void service(HttpServletRequest request, HttpServletResponse response) {
/*	   try{
		   request.setCharacterEncoding("UTF-8");
	   }catch(Exception e)
	   {
		   
	   }*/
		long request_beginTime = System.currentTimeMillis();
	   String reqURI = request.getRequestURI();
	   
	   
	   if( request.getCharacterEncoding() == null )
	   {
		   try{
			   request.setCharacterEncoding(SFConst.SYS_GBK);
			   response.setContentType(SFConst.SYS_CONTENT_TYPE_GKB);
		   }catch(Exception e )
		   {
			   
		   }
	   }
	    
	   //检查容器是否已初始化好，如果没有初始化好，返回提示信息
	   if( !this.checkInitialize() )
	   {
		   try{
			   inInitializing(request, response);
		   }catch(Exception e)
		   {
			   
		   }
		   return;
	   }
	   

	   
	   HttpServletRequest theRequest = request;
	   theRequest.setAttribute("REQUEST_BEGINTIME",request_beginTime); //chenky add 统计时间
	   
	   ModelAndView mv = null;
	   
	   //访问控制器返回的对象
	   Object accessObj = null;
	   
	   long beginTimeStamp = System.currentTimeMillis();	   
	   
	   if( this.monitorStarted )
		   this.accessInfo.newAccess();
	   try
	   {	   
		   if( this.accessManager != null )
		   {

			   /**
			    * 访问控制处理
			    */
			   Context sessionContext = null;
			   Session session  = null;
			   if( sessionManager != null )
			   		session = this.sessionManager.getSession( theRequest, response, false);
			   
			   if (session != null )
				   sessionContext = (Context)session.getAttribute(EMPConstance.ATTR_CONTEXT );
			   
			   if( accessObj != null )
				   accessManager.beginAccess( accessObj );
		   }
		   
		   addRequiredAttributes( theRequest, response );
		   
			if( localeResolver != null )
			{
				Locale locale = localeResolver.resolveLocale( request, response, sessionManager );
				theRequest.setAttribute(EMPConstance.ATTR_LOCALE, locale );
			}
		   
		   //mv = controller.doRequest(theRequest, response);		   		  

		   //Do again
			if( localeResolver != null )
			{
				Locale locale = localeResolver.resolveLocale( request, response, sessionManager );
				theRequest.setAttribute(EMPConstance.ATTR_LOCALE, locale );
			}
		   	   
		   
		   if( mv != null )
		   {

			mv.getView().render(mv.getModel(), theRequest, response,  jspRootPath);
			   //Shendongjie 20160914 修改，不用进入JSP直接进入Utan框架 end
		   }
		   
		   logger.info("Process request: " + reqURI + " finished!");
	   }
	   catch(Exception e)
	   {

		   logger.info("Process request: " + reqURI + " failed!");
		   
		   Map model = new HashMap();
		   if (e instanceof BizLogicException){
			   model = ((BizLogicException)e).getModel();
			   e = ((BizLogicException)e).getException();			   
		   }
		   if (e instanceof InvalidInputException){
			   model = ((InvalidInputException)e).getModel();			  	  
		   }
		   model.put(EMPConstance.ATTR_EXCEPTION, e);
		   
		   if( this.exceptionHandler != null ) //find out the exception handler from common context's exception handler
		   {
			   View view = exceptionHandler.getExceptionView( e );
			   if( view != null )
			   {
				   view.render( model, request, response , jspRootPath);
				   return;
			   }
		   }
		   
		   renderException(request, response, e);
		   logger.error("Process request: " + reqURI + " failed!");
		   
	   }
	   finally
	   {
		  if( theRequest instanceof MultipartHttpServletRequest )  
		  {
			  MultipartHttpServletRequest mr = (MultipartHttpServletRequest)theRequest;
			  this.multipartResolver.cleanup( mr );
		  }
		  
		   if( accessManager != null && accessObj != null )
			   accessManager.endAccess(accessObj, beginTimeStamp); 
		   
		   if( (System.currentTimeMillis() - beginTimeStamp ) > alarmResponseTime )
			   EMPJMXManager.sendNotification(this, this.getServletName(), "error", "Response too low in execute " + reqURI  + " used time as:" + (System.currentTimeMillis() - beginTimeStamp ) );

		   if( this.monitorStarted )
			   this.accessInfo.endAccess(System.currentTimeMillis() - beginTimeStamp ); 

	   }
	   
   }
	
/*   public void reInitialize()
   {
	   
   }*/
   
	/**
	 * 发生异常时的响应处理。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 * @param e 异常
	 */
	protected void renderException(HttpServletRequest request, HttpServletResponse response, Exception e) {

		request.setAttribute(EMPConstance.ATTR_EXCEPTION, e);

		try {
			String url = "error.jsp";
			RequestDispatcher dispatcher = request.getRequestDispatcher(jspRootPath + "/" + url);
			dispatcher.forward(request, response);
		} catch (Exception ee) {
			logger.info("Failed to render the Exception!");
		}
	}

	/**
	 * 添加一个请求分发器。
	 * 
	 * @param dispatcher 请求分发器
	 */
	public void addRequestDispatcher(RequestDispatcher dispatcher) {
		this.requestDispatchers.add(dispatcher);
	}
   
	/**
	 * Servlet初始化入口方法。
	 * <p>
	 * 显示EMP版本，调用初始化实现方法。
	 * 
	 * @param config Servlet初始化参数
	 * @throws
	 *            javax.servlet.ServletException
	 */
	public void init(ServletConfig config)throws ServletException {
		super.init( config );
		doInit();
		logger.info("Start up servlet " + config.getServletName() + "OK");
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

	   //contextName = this.getServletContext().getServletContextName();

	   String svInfo = getServletContext().getServerInfo();
//	   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Server Info: " + svInfo );
	   logger.info("Server Info: " + svInfo);
	   
	   //Use the user setting factory root Path
	   rootPath = getServletContext().getInitParameter(EMPConstance.SETTINGS_ROOT);
		if( rootPath != null && rootPath.startsWith("./"))
			rootPath = getServletContext().getRealPath("/");
	   
	   if( rootPath == null )
	   {
		   rootPath = getServletContext().getRealPath("/");
	   }

	   rootPath = rootPath.replace('\\', '/');
	   
	   if( !rootPath.endsWith("/"))
		   rootPath = rootPath + "/";

	   jspRootPath = getInitParameter("jspRootPath");
	   
	   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
			   "Start up servlet " + getServletName()+ " from rootPath: " + rootPath );
	   logger.info("Start up servlet " + getServletName()+ " from rootPath: " + rootPath);
	   
	   factoryName = getInitParameter("factoryName");
//	   contentDivId = getInitParameter("contentDivId");
	   empIniFileName = this.getInitParameter("iniFile");
	   
	   sessionManagerName = this.getInitParameter("sessionManager"); 
	   String rootContextName = getInitParameter("rootContextName");
//	   String resourceFileName = getInitParameter("resourceFileName");
	   
	   
	   
	   try
	   {
		   //初始化逻辑处理器
		   if( empIniFileName != null )
		   {
			   componentFactory = (EMPFlowComponentFactory)ComponentFactory.getComponentFactory(factoryName);
			   if( componentFactory != null )
			   {
				   
				   try{
					   Context context = componentFactory.getContextNamed( rootContextName );
					   SessionManager sm = (SessionManager)context.getService( sessionManagerName );
					   this.sessionManager = sm;
//					   TransactionManager tm = (TransactionManager)context.getService("transactionManager");
//					   this.transactionManager = tm;
					   
				   }catch(Exception e)
				   {
//					   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, 
//							   "Failed to get sessionManager!", e);
					   logger.info("Failed to get sessionManager!");
				   }

//				   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
//						   "Initialize EMP Flow Context from " + empIniFileName + "haven been done!");
				   logger.info("Initialize EMP Flow Context from " + empIniFileName + "haven been done!");
			   }
			   else
			   {
				   componentFactory = new EMPFlowComponentFactory();
				   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
						   "Initialize EMP Flow Context from " + empIniFileName, null);
				   logger.info("Initialize EMP Flow Context from " + empIniFileName);
				   componentFactory.setName( factoryName );
				   componentFactory.setRootContextName( rootContextName );
				   componentFactory.initializeComponentFactory(factoryName, rootPath + empIniFileName);
				   
				   try{
					   Context context = componentFactory.getContextNamed( rootContextName );
					   
					   SessionManager sm = (SessionManager)context.getService( sessionManagerName );
					   this.sessionManager = sm;
//					   TransactionManager tm = (TransactionManager)context.getService("transactionManager");
//					   this.transactionManager = tm;
					   
				   }catch(Exception e)
				   {
					   logger.error("Failed to get sessionManager!");
				   }
			   }
		   }
		   else
		   {
			   logger.error("Share EMP flow Context from: " + factoryName);
			   componentFactory = (EMPFlowComponentFactory)ComponentFactory.getComponentFactory(factoryName);
			   if( componentFactory != null )
			   {
				   try{
					   Context context = componentFactory.getContextNamed( componentFactory.getRootContextName() );
					   SessionManager sm = (SessionManager)context.getService( sessionManagerName );
					   this.sessionManager = sm;
	//				   TransactionManager tm = (TransactionManager)context.getService("transactionManager");
	//				   this.transactionManager = tm;
					   
				   }catch(Exception e)
				   {
//					   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, 
//							   "Failed to get sessionManager!", e);
					   logger.error("Failed to get sessionManager!");
				   }
			   }
		   }

		   //初始化MVC 模型
		   initMVCContext();

		   //only initialize when do EMPFlow context initialize
		   if( componentFactory != null &&  this.initializer != null )
		   {
//			   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
//					   "Invoker the Initializer...");
			   logger.info("Invoker the Initializer...");
			   initializer.initialize( componentFactory );
		   }
		   
	   }catch(Exception e)
	   {
		   //打印系统日志
//		   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
//				   "Initialize EMP Request Servlet from " + empIniFileName + " failed!", e);
		   //将系统日志打印到指定文件
		   logger.error("Initialize EMP Request Servlet from " + empIniFileName + " failed!");
		   throw new ServletException("Failed to initialize the EMP context ", e);
	   }

	   this.resources = (Map)getServletContext().getAttribute("resources");
	   
//	   initializeExternResource(rootPath + "/" + resourceFileName);
	   
//	   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
//			   "Initialize EMP Request Servlet from " + empIniFileName + " OK!", null);
	   
	   logger.info("Initialize EMP Request Servlet from " + empIniFileName + " OK!");
	}
   
	/**
	 * MVC请求处理模型初始化方法。
	 * <p>
	 * 解析ServletContext配置文件。
	 */
	private void initMVCContext() {
//	   String rootPath = getServletContext().getRealPath("/");
//	   rootPath = rootPath.replace('\\', '/');
//	   if( !rootPath.endsWith("/"))
//		   rootPath = rootPath + "/";

	   String servletContextFile = getInitParameter("servletContextFile");

//		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
//				"Initialize EMP Servlet Context from " + servletContextFile, null);
	   logger.info("Initialize EMP Servlet Context from " + servletContextFile);
		ServletContextFactory ctxFactory = new ServletContextFactory();
		ctxFactory.initializeComponentFactory(this.getServletName()
				+ "CtxFactory", rootPath + servletContextFile);

		try {
			ctxFactory.parseTheContext(this);
			ctxFactory.exportMBean(this, this.getServletName());
//			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
//					"Initialize EMP Servlet Context from " + servletContextFile + " OK!", null);
			logger.info("Initialize EMP Servlet Context from " + servletContextFile + " OK!");
		} catch (Exception e) {
//			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0,
//					"Initialize EMP Servlet Context from " + servletContextFile + " Failed", e);
			logger.error("Initialize EMP Servlet Context from " + servletContextFile + " Failed");
		}
	}
   
	/**
	 * 初始化外部字符串资源定义。
	 * 
	 * @param fileName
	 */
	private void initializeExternResource(String fileName) {
	    
		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Initialize external resource from " + fileName + " ...");
		try{
		    resources = new HashMap();
		    
		    XMLDocumentLoader loader = new XMLDocumentLoader();
		    Document document = loader.loadXMLDocument(fileName );
		    Element element = document.getDocumentElement();

		    NodeList nodeList = element.getChildNodes();
		    
		    for( int i=0; i<nodeList.getLength(); i++)
		    {
		    	Node node = nodeList.item( i );
		    	if( node.getNodeType() != Node.ELEMENT_NODE)
		    		continue;
		    	if( !("resource".equals(node.getNodeName())))
		    		continue;
		    	try{
			    	String resourceId = node.getAttributes().getNamedItem("id").getNodeValue();
				    ResourceDefine res = new ResourceDefine();
				    res.setResourceId( resourceId );
				    
				    this.resources.put( resourceId, res );
				    
				    NodeList childs = node.getChildNodes();
				    for( int k=0; k<childs.getLength(); k++)
				    {
				    	Node childNode = childs.item( k );
				    	if( childNode.getNodeType() != Node.ELEMENT_NODE)
				    		continue;
				    	if(!("resourceValue".equals(childNode.getNodeName())))
				    		continue;
				    	
				    	String lId = childNode.getAttributes().getNamedItem("id").getNodeValue();
				    	String value = childNode.getFirstChild().getNodeValue();
				    	
				    	res.addResource(lId, value );
				    }
		    	}catch(Exception e )
		    	{
		    		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, "Some thing wrong with externalresource file: " + fileName + " of " + node );
		    	}
		    }
		    
			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Initialize external resource from " + fileName + " OK!");
		}catch(Exception e)
		{
			EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, "Initialize external resource from " + fileName + " failed.", e);
		}	    
	}
   
	/**
	 * 没有找到合适的控制器时执行的方法。
	 * <p>
	 * 返回HTTP错误码404。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 * @throws IOException
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
	}

	/**
	 * 正在初始化过程中，禁止访问的处理方法。
	 * <p>
	 * 返回HTTP错误码403。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 * @throws IOException
	 */
	protected void inInitializing(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Server in initializing please try again for minutes later.");		
	}	

	/**
	 * 关闭Servlet。
	 */
	public void destroy() {
		EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0,
				"Destory the servlet: " + this.getServletName());
		if (this.sessionManager != null)
			sessionManager.terminate();

		if (empIniFileName != null)
			componentFactory.close();
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

/* public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	} */

	/**
	 * 获得语言环境处理器。
	 * 
	 * @return 语言环境处理器
	 */
	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	/**
	 * 注入语言环境处理器。
	 * 
	 * @param localeResolver 语言环境处理器
	 * @emp:isChild com.ecc.web.servlet.LocaleResolver
	 */
	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	/**
	 * 获得自定义初始化处理器。
	 * 
	 * @return 自定义初始化处理器
	 */
	public Initializer getInitializer() {
		return initializer;
	}

	/**
	 * 注入自定义初始化处理器。
	 * 
	 * @param initializer 自定义初始化处理器
	 * @emp:isChild com.ecc.web.servlet.Initializer
	 */
	public void setInitializer(Initializer initializer) {
		this.initializer = initializer;
	}

	/**
	 * 获得文件上传处理器。
	 * 
	 * @return 文件上传处理器
	 */
	public MultipartResolver getMultipartResolver() {
		return multipartResolver;
	}

	/**
	 * 注入文件上传处理器。
	 * 
	 * @param multipartResolver 文件上传处理器
	 * @emp:isChild com.ecc.web.multipart.MultipartResolver
	 */
	public void setMultipartResolver(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	/**
	 * 获得异常处理器。
	 * 
	 * @return 异常处理器
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * 注入异常处理器。
	 * 
	 * @param exceptionHandler 异常处理器
	 * @emp:isChild com.ecc.web.servlet.mvc.ExceptionHandler
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
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
	 * 重启Servlet。
	 * <p>
	 * 重新初始化Servlet中定义的所有元素（包括业务逻辑组件工厂）。一般供监控系统调用。
	 */
	public void restart() {
		   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Restart [" + this.getServletName() + "] ...");

		   try{
			   if( componentFactory != null )
				   componentFactory.close();
	
			   componentFactory = null;
			   this.doInit( );
	
			   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Restart [" + this.getServletName() + "] OK.");
		   }catch(Exception e)
		   {
			   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, "Restart [" + this.getServletName() + "] Failed.", e);
		   }
	}
	
	/**
	 * 在保持会话的同时，重新载入ServletContext配置文件（包括所有Controller）。
	 */
//	public void reloadMVCContext() {
//		   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Reload MVC context for [" + this.getServletName() + "] ...");
//
//		   try{
//			   Map sessionMaps=null;
//			   if(this.getSessionManager()!=null){
//				  sessionMaps=this.getSessionManager().getSessions();
//			   }
//			   //remove the old controllers
//			   defaultDispatcher = new FBSEMPRequestDispatcher();
//
//			   this.initMVCContext( );
//			   
//			   if(this.getSessionManager()!=null && sessionMaps != null )
//			   {
//				   this.getSessionManager().setSessions(sessionMaps);
//			   }
//			   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, "Reload MVC context for [" + this.getServletName() + "] OK.");
//		   }catch(Exception e)
//		   {
//			   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.ERROR, 0, "Reload MVC context for [" + this.getServletName() + "] Failed.", e);
//		   }
//		
//	}

	/**
	 * 重新加载ServletContext配置文件，如果存在业务逻辑组件工厂，则也重新加载。
	 */
	public void reloadServlet(){
		if(this.componentFactory != null)
			this.componentFactory.reloadAllFlow();
		//this.reloadMVCContext();
	}
	
	/**
	 * 获得监控状态。
	 * 
	 * @return 监控状态
	 */
	public String getState() {
		return state;
	}

	/**
	 * 设置监控状态。
	 * 
	 * @param state 监控状态
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * 返回是否启用监控。
	 * 
	 * @return 是否启用监控
	 */
	public boolean isMonitorStarted() {
		return monitorStarted;
	}

	/**
	 * 设置是否启用监控。
	 * 
	 * @param monitorStarted 是否启用监控
	 */
	public void setMonitorStarted(boolean monitorStarted) {
		this.monitorStarted = monitorStarted;
		//this.defaultDispatcher.setControllerMonitorStarted(monitorStarted);
	}
	
	/**
	 * 启动某个Controller的监控信息采集。
	 * 
	 * @param actionId controller的唯一标识
	 */
//	public void startControllerMonitor(String actionId)	{
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller != null )
//			controller.startMonitor();
//	}
//	
//	/**
//	 * 结束某个Controller的监控信息采集。
//	 * 
//	 * @param actionId controller的唯一标识
//	 */
//	public void stopControllerMonitor(String actionId ) {
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller != null )
//			controller.stopMonitor();		
//	}
//	
//	/**
//	 * 取得某个Controller的监控信息采集状态。
//	 * 
//	 * @param actionId controller的唯一标识
//	 */
//	public boolean getControllerMonitorState(String actionId )	{
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller == null )
//			return false;
//		
//		return controller.getMonitorState();
//	}
//
//	/**
//	 * 取得某个Controller的访问信息。
//	 * 
//	 * @param actionId controller的唯一标识
//	 * @return 访问信息
//	 */
//	public AccessInfo getControllerAccessInfo(String actionId )	{
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller == null )
//			return null;
//		
//		return controller.getAccessInfo();		
//	}

	/**
	 * 取得需要报警的响应时间。
	 * 
	 * @return 需要报警的响应时间
	 */
	public long getAlarmResponseTime() {
		return alarmResponseTime;
	}

	/**
	 * 设置需要报警的响应时间。当响应时间超过此值时向监控系统报警。
	 * 
	 * @param alarmResponseTime 需要报警的响应时间
	 */
	public void setAlarmResponseTime(long alarmResponseTime) {
		this.alarmResponseTime = alarmResponseTime;
	}

	/**
	 * 取得访问信息。
	 * 
	 * @return 访问信息。
	 */
	public AccessInfo getAccessInfo()	{
		return this.accessInfo;
	}
}
