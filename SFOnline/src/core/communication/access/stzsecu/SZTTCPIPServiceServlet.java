package core.communication.access.stzsecu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecc.emp.component.factory.ServletContextFactory;
import com.ecc.emp.component.xml.XMLDocumentLoader;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.sscc.fdep.mrapi;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.util.AccessConstance;
import core.log.SFLogger;
/**
 * 深证通监听启动服务
 * @author 汪华
 *
 */
public class SZTTCPIPServiceServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2094403173683344008L;
	
	/**
	 * 深证通缓存MAP对象
	 */
	public static final String CACHE_MAP_SZT="CACHE_MAP_SZT"; 
	
	/**
	 * 缺省连接池大小：5
	 */
	public static final int DEFAULT_POOL_SIZE = 5;
	
	/**
	 *  缺省超时时间:40秒
	 */
	private static final int DEFAULT_TIMEOUT=40000;
	
	/**
	 * 线程池
	 */
	private ExecutorService executor;
	
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
		super.service(request, response);
	}

	/**
	 * 关闭Servlet。
	 */
	public void destroy() {
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
				"Destory the servlet: " + this.getServletName());
		
		if(executor!=null){
			executor.shutdown();
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
		try {
			doInit();
		} catch (SFException e) {
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Start up  the servlet: " + this.getServletName() + " Fail!"+e.toString());
			throw new ServletException(e);
		}
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,"Start up  the servlet: " + this.getServletName() + " OK!");
	}
	
	public void doInit() throws SFException{
		// 获得配置文件根目录
		rootPath = getServletContext().getInitParameter(
				EMPConstance.SETTINGS_ROOT);// getInitParameter("factoryRootPath");
		if (rootPath != null && rootPath.startsWith("./")){
			rootPath = getServletContext().getRealPath("/");
		}
		if (rootPath == null) {
			rootPath = getServletContext().getRealPath("/");
		}
		rootPath = rootPath.replace('\\', '/');
		if (!rootPath.endsWith("/")){
			rootPath = rootPath + "/";
		}
		
		/*
		 * 初始化深证通缓存对象
		 */
		Map<String, Object> cacheSzt=new HashMap<String,Object>();
		cacheSzt.put("SZT_EXECUTOR_SERVER", executor);
		CacheMap.putCache(CACHE_MAP_SZT,cacheSzt);
		
		/*
		 * 缓存SZT超时时间
		 */
		String sTimeout = getInitParameter("timeout");//超时时间
		if(SFUtil.isEmpty(sTimeout)){
			CacheMap.putCache(AccessConstance.SZT_TIMEOUT,this.DEFAULT_TIMEOUT);
		}else{
			CacheMap.putCache(AccessConstance.SZT_TIMEOUT,Integer.parseInt(sTimeout));			
		}
		
		/*
		 * TCP/IP请求处理模型初始化方法。
		 */
		this.initAccessContext();
		
		/*
		 * SZT初始化
		 */
		this.initializeMrapi();
		
		/*
		 * 根据参数配置开启深证通轮训线程
		 */
		SZTThreadInitialize threadInitialize=new SZTThreadInitialize();
		threadInitialize.execute();
	}
	
	
	/**
	 * TCP/IP请求处理模型初始化方法。
	 * <p>
	 * 解析AccessServletContext配置文件。
	 */
	private void initAccessContext() {
		String servletContextFile = getInitParameter("servletContextFile");
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null,
				"Initialize EMP Servlet Context from " + servletContextFile);
		ServletContextFactory ctxFactory = new ServletContextFactory();
		ctxFactory.initializeComponentFactory("contextFactory", rootPath+ servletContextFile);
		Element element = null;
		NodeList nodeList = null;
		try {
			Document fbsSqlDefDoc = loadXMLDocument(rootPath+ servletContextFile);
			element = fbsSqlDefDoc.getDocumentElement();
			nodeList = element.getChildNodes();
			Map<String,Map<String,String>> tcpipServiceMap=new HashMap<String,Map<String,String>>();
			
			
			for (int j = 0; j < nodeList.getLength(); j++) {
				Node node = nodeList.item(j);
				
				if("TCPIPService".equals(node.getNodeName())){
					NamedNodeMap attributes=node.getAttributes();
					Map<String,String> tcpipService=new HashMap<String,String>();
					for(int i=0;i<attributes.getLength();i++){
						Node attr=attributes.item(i);
						tcpipService.put(attr.getNodeName(), attr.getNodeValue());
					}
					tcpipServiceMap.put(tcpipService.get("id"), tcpipService);
				}
			}
			
			
			
			CacheMap.putCache("SZT_TCPIPSERVICE", tcpipServiceMap);
			
		} catch (Exception e) {
			SFLogger.error(SFConst.DEFAULT_TRXCODE, null,
					"Initialize EMP Servlet Context from " + servletContextFile
							+ " Failed", e);
		}
	}
	

	/**
	 * load XML document
	 * inner method，provide just for SFInitializer invoke。
	 * @param fileName
	 * @return Document XML object
	 * @throws Exception
	 */
	protected Document loadXMLDocument(String fileName) throws Exception
	{
		   XMLDocumentLoader loader = new XMLDocumentLoader();
		   Document document = loader.loadXMLDocument(fileName);
		   return document;
	}

	
	/**
	 * WebService渠道访问配置文件的根目录，默认为Web应用的Context根目录
	 */
	private String rootPath;
	
	/**
	 * 初始化SZT 前置机
	 * @throws EMPException
	 */
	private void initializeMrapi() throws SFException{
		SZTAccessParam sztParam=CacheMap.getCache("SZT_ACCESS_PARAM");
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "初始化深证通连接开始");
		/*
		 * 初始化服务端深证通
		 */
		String serverAppId =  sztParam.getServerAppId();
		String serverPassword = sztParam.getServerPwd();
		String mrIP1 = sztParam.getMrIp();
		short mrPort1 =  sztParam.getMrPort();
		String mrIP2 = sztParam.getMrIpBak();
		short mrPort2 =  sztParam.getMrPortBak();
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "server ->appId:"+serverAppId+",appPasswd:"+serverPassword+",mrIP1:"+mrIP1+",mrPort1:"+mrPort1+",mrIP2:"+mrIP2+",mrPort2:"+mrPort2);
		mrapi.Mr2Init(serverAppId, serverPassword,mrIP1, mrPort1, mrIP2, mrPort2);
		if(mrapi.Mr2IsLinkOK(serverAppId) != 0){
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "接收深证通信息初始化完成【 OK】!");
		}else{
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "接收深证通信息初始化完成【 FAILED】 !");
		}
		
		
		/*
		 * 初始化客户端深证通
		 */
		String clientAppId=sztParam.getClientAppId();
		if(!serverAppId.equalsIgnoreCase(clientAppId)){
			String clientPawword = sztParam.getClientPwd();
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "clent ->appId:"+clientAppId+",appPasswd:"+clientPawword+",mrIP1:"+mrIP1+",mrPort1:"+mrPort1+",mrIP2:"+mrIP2+",mrPort2:"+mrPort2);
			mrapi.Mr2Init(clientAppId, clientPawword,mrIP1, mrPort1, mrIP2, mrPort2);
			if(mrapi.Mr2IsLinkOK(clientAppId) != 0){
				SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "发送深证通信息初始化完成【 OK】!");
			}else{
				SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "发送深证通信息初始化完成【 FAILED】 !");
			}
		}		
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "初始化深证通连接结束");
	}
}
