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
 * EMP������������Servlet��
 * <p>
 * ��Servlet��Ӧһ��ServletContext�����ļ���MVCģ���ļ����������ļ�����web.xml�еĳ�ʼ������ָ����
 * �����ļ��ж�����MVCģ���Լ�һЩ��ش��������Ự�������ʿ��Ƶȣ���
 * <p>
 * ��Servlet��������󣬸���MVCģ�ͽ�����Ӧ�Ĵ���
 * EMP��ʵ�ֵĴ���ʽ�ǣ�ÿ��MVCģ�͵Ķ����Ӧһ��������Controllerʵ����
 * ����������Controller��ӳ���ϵ����������ַ������С�
 * ��Servlet�ӵ��û�������ʱ���ɷַ���ͨ������URL�ҵ���Ӧ��������
 * Ȼ���ɿ���������ҵ���߼��ĵ����Լ�ҳ����ת���ơ�
 * ���Servlet������������ģ���Լ���ͼ��ҳ�棩���ظ��ͻ���չ�֡�
 * <p>
 * ��һ��Ӧ���У������������˲�ͬ��MVCģ�͵�Servlet��Ӧͬһ��ҵ���߼�����ģ�ͣ���Ӧ��ͬ������
 */
public class EMPRequestServlet extends HttpServlet {

	SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
	
	private static final long serialVersionUID = 1L;
		
	/**
	 * �����ļ��ĸ�Ŀ¼��Ĭ��ΪWebӦ�õ�Context��Ŀ¼
	 */
	private String rootPath;
	
	/**
	 * �ⲿ�ַ�����Դ����
	 */
	private Map resources;
    
   /**
	 * �û��趨�ĳ�ʼ����������
	 * ��������е�ϵͳ��ʼ���󣬵��ô˽ӿڣ�����û�ϵͳ��Ҫ�ĳ�ʼ��
	 */
	private Initializer initializer;

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
	 * ����Ϊһ��Servlet���ö��RequestDispatcher��ÿ��Dispatcher������Ӧ�Ĵ�����������
	 * ������չ�ͷ��ʿ��ơ�
	 */
	private ArrayList requestDispatchers;

	/**
	 * Ĭ�ϵ�������������ڲ���Ҫ����������������£�ʹ��Ĭ�ϵķ�����
	 */
	//private FBSEMPRequestDispatcher defaultDispatcher;

	
	private String sessionManagerName;
	
	/**
	 * �Ự������
	 */
	private SessionManager sessionManager;

	/**
	 * �쳣������
	 */
	private ExceptionHandler exceptionHandler;

	/**
	 * ���ʿ�����
	 */
	private AccessManager accessManager;

	/**
	 * ���״̬
	 */
	private String state = "normal";
	
	/**
	 * �Ƿ����������Ϣ�ɼ�
	 */
	private boolean monitorStarted = false;

	/**
	 * ������Ϣ
	 */
	private AccessInfo accessInfo = new AccessInfo();

	/**
	 * ��Ҫ��������Ӧʱ�䣬������Ӧʱ�䳬����ֵʱ����ϵͳ����
	 */
	private long alarmResponseTime = 20000;
	
	/**
	 * JSP��·��
	 */
	private String jspRootPath;

	/**
	 * �û������Ի���������
	 */
	private LocaleResolver localeResolver;

	/**
	 * �ļ��ϴ�������
	 */
	private MultipartResolver multipartResolver;

	public EMPRequestServlet() {
	}
	
	/**
	 * ��������ӱ�Ҫ�����Զ��壬�������߼�������Ҫ��
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 */
	protected void addRequiredAttributes(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute(EMPConstance.ATTR_RESOURCE, resources );

		request.setAttribute(EMPConstance.ATTR_ROOTPATH, rootPath );
		request.setAttribute(EMPConstance.ATTR_DATA_TYPE, this.componentFactory.getDataTypeDefine() );
		request.setAttribute(EMPConstance.ATTR_SESSION_MGR, this.sessionManager );
		// add by liup �������������Ŀ����
		request.setAttribute(SFConst.SYS_FACTORY_NAME, request.getContextPath().substring(1) );
	}
	
	/**
	 * ��������Ƿ��ѳ�ʼ���á�
	 * <p>
	 * ��û�У����Servlet���г�ʼ����
	 * 
	 * @return �����Ƿ��ѳ�ʼ����
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
				else 	//ʹ�ù�����Factory���ҹ���factory������
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
	 * Servlet����ڷ�����
	 * <p>
	 * ���ȼ�������Ƿ��ѳ�ʼ���ã����û�г�ʼ���ã�������ʾ��Ϣ��
	 * Ȼ��ͨ���ַ����ҵ������Ӧ�Ŀ������������ÿ������Ĵ���
	 * ��󽫾������������ģ�ͺ���ͼ��Ϊ��Ӧ���ظ�ǰ�ˡ�
	 * 
	 * @see #doGet(HttpServletRequest, HttpServletResponse)
	 * @see #doPost(HttpServletRequest, HttpServletResponse)
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
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
	    
	   //��������Ƿ��ѳ�ʼ���ã����û�г�ʼ���ã�������ʾ��Ϣ
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
	   theRequest.setAttribute("REQUEST_BEGINTIME",request_beginTime); //chenky add ͳ��ʱ��
	   
	   ModelAndView mv = null;
	   
	   //���ʿ��������صĶ���
	   Object accessObj = null;
	   
	   long beginTimeStamp = System.currentTimeMillis();	   
	   
	   if( this.monitorStarted )
		   this.accessInfo.newAccess();
	   try
	   {	   
		   if( this.accessManager != null )
		   {

			   /**
			    * ���ʿ��ƴ���
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
			   //Shendongjie 20160914 �޸ģ����ý���JSPֱ�ӽ���Utan��� end
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
	 * �����쳣ʱ����Ӧ����
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param e �쳣
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
	 * ���һ������ַ�����
	 * 
	 * @param dispatcher ����ַ���
	 */
	public void addRequestDispatcher(RequestDispatcher dispatcher) {
		this.requestDispatchers.add(dispatcher);
	}
   
	/**
	 * Servlet��ʼ����ڷ�����
	 * <p>
	 * ��ʾEMP�汾�����ó�ʼ��ʵ�ַ�����
	 * 
	 * @param config Servlet��ʼ������
	 * @throws
	 *            javax.servlet.ServletException
	 */
	public void init(ServletConfig config)throws ServletException {
		super.init( config );
		doInit();
		logger.info("Start up servlet " + config.getServletName() + "OK");
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
		   //��ʼ���߼�������
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

		   //��ʼ��MVC ģ��
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
		   //��ӡϵͳ��־
//		   EMPLog.log(EMPConstance.EMP_MVC, EMPLog.INFO, 0, 
//				   "Initialize EMP Request Servlet from " + empIniFileName + " failed!", e);
		   //��ϵͳ��־��ӡ��ָ���ļ�
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
	 * MVC������ģ�ͳ�ʼ��������
	 * <p>
	 * ����ServletContext�����ļ���
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
	 * ��ʼ���ⲿ�ַ�����Դ���塣
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
	 * û���ҵ����ʵĿ�����ʱִ�еķ�����
	 * <p>
	 * ����HTTP������404��
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @throws IOException
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
	}

	/**
	 * ���ڳ�ʼ�������У���ֹ���ʵĴ�������
	 * <p>
	 * ����HTTP������403��
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @throws IOException
	 */
	protected void inInitializing(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Server in initializing please try again for minutes later.");		
	}	

	/**
	 * �ر�Servlet��
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

/* public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	} */

	/**
	 * ������Ի�����������
	 * 
	 * @return ���Ի���������
	 */
	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	/**
	 * ע�����Ի�����������
	 * 
	 * @param localeResolver ���Ի���������
	 * @emp:isChild com.ecc.web.servlet.LocaleResolver
	 */
	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	/**
	 * ����Զ����ʼ����������
	 * 
	 * @return �Զ����ʼ��������
	 */
	public Initializer getInitializer() {
		return initializer;
	}

	/**
	 * ע���Զ����ʼ����������
	 * 
	 * @param initializer �Զ����ʼ��������
	 * @emp:isChild com.ecc.web.servlet.Initializer
	 */
	public void setInitializer(Initializer initializer) {
		this.initializer = initializer;
	}

	/**
	 * ����ļ��ϴ���������
	 * 
	 * @return �ļ��ϴ�������
	 */
	public MultipartResolver getMultipartResolver() {
		return multipartResolver;
	}

	/**
	 * ע���ļ��ϴ���������
	 * 
	 * @param multipartResolver �ļ��ϴ�������
	 * @emp:isChild com.ecc.web.multipart.MultipartResolver
	 */
	public void setMultipartResolver(MultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	/**
	 * ����쳣��������
	 * 
	 * @return �쳣������
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * ע���쳣��������
	 * 
	 * @param exceptionHandler �쳣������
	 * @emp:isChild com.ecc.web.servlet.mvc.ExceptionHandler
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
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
	 * ����Servlet��
	 * <p>
	 * ���³�ʼ��Servlet�ж��������Ԫ�أ�����ҵ���߼������������һ�㹩���ϵͳ���á�
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
	 * �ڱ��ֻỰ��ͬʱ����������ServletContext�����ļ�����������Controller����
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
	 * ���¼���ServletContext�����ļ����������ҵ���߼������������Ҳ���¼��ء�
	 */
	public void reloadServlet(){
		if(this.componentFactory != null)
			this.componentFactory.reloadAllFlow();
		//this.reloadMVCContext();
	}
	
	/**
	 * ��ü��״̬��
	 * 
	 * @return ���״̬
	 */
	public String getState() {
		return state;
	}

	/**
	 * ���ü��״̬��
	 * 
	 * @param state ���״̬
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * �����Ƿ����ü�ء�
	 * 
	 * @return �Ƿ����ü��
	 */
	public boolean isMonitorStarted() {
		return monitorStarted;
	}

	/**
	 * �����Ƿ����ü�ء�
	 * 
	 * @param monitorStarted �Ƿ����ü��
	 */
	public void setMonitorStarted(boolean monitorStarted) {
		this.monitorStarted = monitorStarted;
		//this.defaultDispatcher.setControllerMonitorStarted(monitorStarted);
	}
	
	/**
	 * ����ĳ��Controller�ļ����Ϣ�ɼ���
	 * 
	 * @param actionId controller��Ψһ��ʶ
	 */
//	public void startControllerMonitor(String actionId)	{
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller != null )
//			controller.startMonitor();
//	}
//	
//	/**
//	 * ����ĳ��Controller�ļ����Ϣ�ɼ���
//	 * 
//	 * @param actionId controller��Ψһ��ʶ
//	 */
//	public void stopControllerMonitor(String actionId ) {
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller != null )
//			controller.stopMonitor();		
//	}
//	
//	/**
//	 * ȡ��ĳ��Controller�ļ����Ϣ�ɼ�״̬��
//	 * 
//	 * @param actionId controller��Ψһ��ʶ
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
//	 * ȡ��ĳ��Controller�ķ�����Ϣ��
//	 * 
//	 * @param actionId controller��Ψһ��ʶ
//	 * @return ������Ϣ
//	 */
//	public AccessInfo getControllerAccessInfo(String actionId )	{
//		Controller controller = this.defaultDispatcher.getController( actionId );
//		if( controller == null )
//			return null;
//		
//		return controller.getAccessInfo();		
//	}

	/**
	 * ȡ����Ҫ��������Ӧʱ�䡣
	 * 
	 * @return ��Ҫ��������Ӧʱ��
	 */
	public long getAlarmResponseTime() {
		return alarmResponseTime;
	}

	/**
	 * ������Ҫ��������Ӧʱ�䡣����Ӧʱ�䳬����ֵʱ����ϵͳ������
	 * 
	 * @param alarmResponseTime ��Ҫ��������Ӧʱ��
	 */
	public void setAlarmResponseTime(long alarmResponseTime) {
		this.alarmResponseTime = alarmResponseTime;
	}

	/**
	 * ȡ�÷�����Ϣ��
	 * 
	 * @return ������Ϣ��
	 */
	public AccessInfo getAccessInfo()	{
		return this.accessInfo;
	}
}
