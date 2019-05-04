package core.emp.session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.service.Service;
import com.ecc.emp.session.EMPSession;
import com.ecc.emp.session.Session;
import com.ecc.emp.session.SessionManager;
import com.ecc.emp.session.SessionTimeoutListener;
/**
 * EMP平台实现的会话管理器。
 * <p>
 * 使用自行实现的Session超时管理机制，主要用于不是通过HTTP方式请求的业务处理。
 *
 * @version 2.1
 * @since 2017-7-1
 */
public class SFSessionManager implements SessionManager, Service {

	/**
	 * Session超时监听器
	 */
	private SessionTimeoutListener sessionTimeoutListener;
	
	/**
	 * 会话管理器名称
	 */
	private String name;
	
	/**
	 * 在使用URLRewrite方式时用于传递sessionId的参数名，默认为EMP_SID
	 */
	private String sessionIdLabel = EMPConstance.EMP_SESSION_ID_LABEL;
	
	/**
	 * FBS柜员ID在Cookie中的名称
	 */
	private String userIdLabel = "_FBS_USER_STAFFNO";

	/**
	 * 维护中的会话
	 */
	private Map sessions = new HashMap();
   
	/**
	 * 实例编号
	 */
	private int instanceIdx;
   
	/**
	 * 用于生成实例编号的静态变量
	 */
	static int instanceCount = 0;

	private String access;
   
	/**
	 * 用于检查Session的定时器
	*/
	private Timer timer;
   
	/**
	 * 这个计数器用于生成一个Key，这个Key用于生成Session Id
	*/
	private static long counter = 0;
   
	/**
	 * 会话超时时间，默认15分钟
	 */
	private int sessionTimeOut = 15*60000;	
   
	/**
	 * 检查会话超时的间隔时间，默认5分钟
	 */
	private int sessionCheckInterval = 5*60000;
   
	final static String SID_PREFEX = ";EMPSID="; //";EMPSID="; //";jsessionid="
	final static String SID_TAIL = ";";
	final static int PREFEX_LEN = 8;

	/**
	 * Session跟踪机制, 0 cookie; 1 URL_Rewrite;
	 */
	private int sessionTraceType = 0;

	/**
	 * 是否与客户端的IP绑定，避免非法获得或假冒SessionId
	 */
	private boolean bindClientIP = false;
   
	/**
	 * 是否与客户端cookie绑定（如果客户端允许cookie）
	 */
	private boolean bindCookie = false;
   
	/**
	 * 是否主应用
	 */
	private boolean masterFlag = false;

	/**
	 * 非主应用时配置主应用Url
	 */
	private String masterAppSSOUrl;

	/**
	 * 组件工厂名称
	 */
	private String factoryName = null;

	/**
	 * SessionContext名称
	 */
	private String sessionContextName;

	/**
    * cookie的生存时间，以秒为单位
    */
	private int cookieMaxAge = -1;
   
   /**
    * sessionTraceType的取值之一，代表会话跟踪机制为Cookie方式
    */
	static final String COOKIE_SESSION = "COOKIE";
   
   /**
    * sessionTraceType的取值之一，代表会话跟踪机制为URL传递参数方式
    */
	static final String URLREWRITE_SESSION="URLREWRITE";
   
   
   public SFSessionManager() 
   {
	   instanceIdx = instanceCount++;
   }
   
	/**
	 * 取当前请求的Session。
	 * 
	 * @param requestObj 请求对象，可以是String或HttpServletRequest
	 * @param responseObj 响应对象
	 * @param createWhenNotCreate 当sesison不存在是是否创建
	 * @return 当前请求的Session
	 */
   public Session getSession(Object requestObj, Object responseObj, boolean createWhenNotCreate) 
   {
	   String sessionId = null, SSOMasterFlag = null;
	   boolean CrtSessionFlag = false;
	   HttpServletRequest request = null;
	   EMPSession session = null;
	   
	   /*沈东杰新增变量用于cookie中存放用户信息begin 20140220*/
	   String fbsUserStaffNo = null;
	   Thread current = Thread.currentThread();

	   /*沈东杰新增变量用于cookie中存放用户信息end 20140220*/
	   
	   if( requestObj instanceof String )
		   sessionId = (String)requestObj;
	   else if( requestObj instanceof HttpServletRequest )
	   {
		   request = (HttpServletRequest)requestObj;
		   SSOMasterFlag = request.getParameter("SSOMasterFlag");
		   if( this.sessionTraceType == 0 )//cookie trace
		   {
			    Cookie[] cookies = request.getCookies();
			    if( cookies != null )
			    {
				    for( int i=0; i<cookies.length; i++)
				    {
				    	if( sessionIdLabel.equals( cookies[i].getName() ))
				    	{
				    		sessionId = cookies[i].getValue();
				    		break;
				    	}
				    }
			    }
			   /*沈东杰新增变量用于cookie中存放用户信息begin 20140220*/
			    if( cookies != null )
			    {
				    for( int i=0; i<cookies.length; i++)
				    {
				    	if( userIdLabel.equals( cookies[i].getName() ))
				    	{
				    		fbsUserStaffNo = cookies[i].getValue();
				    		break;
				    	}
				    }
			    }
			    EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "before Url:["+request.getRequestURL()+"], Cookie Info: SID [" + sessionId + "], _FBS_USER_STAFFNO [" + fbsUserStaffNo + "]");
			    /*沈东杰新增变量用于cookie中存放用户信息end 20140220*/
			    String tmpSessionId = (String)request.getAttribute("_emp_Session_FBSCrtNewSession");
			    if (tmpSessionId != null && tmpSessionId.trim().length() > 0) {
			    	sessionId = tmpSessionId;
			    	CrtSessionFlag = true;
			    }
			    EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "after Url:["+request.getRequestURL()+"], Cookie Info: SID [" + sessionId + "], _FBS_USER_STAFFNO [" + fbsUserStaffNo + "]");
			   
			    //更新Cookie，防止过期
			    if (sessionId != null && cookieMaxAge>0 && responseObj instanceof HttpServletResponse) {
			   		Cookie cookie = new Cookie(sessionIdLabel, sessionId);
			   		cookie.setMaxAge(this.cookieMaxAge);
				    ((HttpServletResponse)responseObj).addCookie( cookie );
			    }
		   }

		   else	//urlRewrite
		   {
			   sessionId = (String)request.getAttribute("SID");	//在EMPHttpAccess模式下，自动解析SID作为SessionId
			   
			   if( sessionId == null )
			   {
					sessionId = request.getParameter(sessionIdLabel );
			   }
			   
			   if( this.bindCookie && sessionId != null)	//同时绑定cookie, 如果客户打开了cookie则检查
			   {
				    Cookie[] cookies = request.getCookies();
				    String cSID = null;
				    
				    if( cookies != null )
				    {
					    for( int i=0; i<cookies.length; i++)
					    {
					    	if( sessionIdLabel.equals( cookies[i].getName() ))
					    	{
					    		cSID = cookies[i].getValue();
					    		break;
					    	}
					    }
				    }
				    
				    if( cSID != null && !(cSID.equals(sessionId )) )
				    {
				    	EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "Invalid Session SID " + sessionId + " not equal to cookie SID " + cSID );
				    	sessionId = null;
				    }

				    //更新Cookie，防止过期
				    if (sessionId != null && cookieMaxAge>0 && responseObj instanceof HttpServletResponse) {
				   		Cookie cookie = new Cookie(sessionIdLabel, sessionId);
				   		cookie.setMaxAge(this.cookieMaxAge);
					    ((HttpServletResponse)responseObj).addCookie( cookie );
				    }
			   }
			   
		   }
		   
		   if( sessionId == null && createWhenNotCreate )
		   {
			   //to avoid repeat create of session
			   session = (EMPSession)request.getAttribute(EMPConstance.ATTR_SESSION);
			   if( session != null )
			   {
				   sessionId = session.getId();
				   session.setLastAccessTime( System.currentTimeMillis() );
			   }
 
		   }
	   }
	   else if( requestObj != null )
		   sessionId = requestObj.toString();
	   
	   if( sessionId != null )
		   session = (EMPSession)sessions.get( sessionId );
	   
	   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Step1. search session in local pool, result is sid[" + sessionId + "], session is [" + session + "]!");
	   
	   if( session != null )
	   {
		   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "CheckTimeOut, Session [" + sessionId + "] currTime is [" + System.currentTimeMillis() + "], lastAccess is ["+session.getLastAccessTime()+"]["+(System.currentTimeMillis()-session.getLastAccessTime())+"], config is[" + this.sessionTimeOut + "]");
		   if( checkTimeout(session) )
		   {
			   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Session time out:" + sessionId );
			   session = null;	//session 超时，返回空
		   }
		   
	   }

	   if( session != null )
	   {
		   if( this.bindClientIP )	//检查客户端IP绑定
		   {
			   String saveIP = (String)session.getAttribute("_CLTIP");
			   String reqIP = request.getRemoteAddr();
			   
			   if( !reqIP.equals( saveIP ))
			   {
				   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "Invalid Session from IP " + saveIP + " to " + reqIP );
				   session = null;
				   return null;
			   }
		   }
	   }
	   
	   //如果通过sessionId找到了session，则取其中的柜员号，如果柜员号和上送的柜员号不一致，则认为没找到
	   if (session != null && !CrtSessionFlag){
		   Context sessionContext = (Context)session.getAttribute(EMPConstance.ATTR_CONTEXT);
		   String sessionStaffNo = null;
		   try {
			   if (sessionContext != null)
				   sessionStaffNo = (String)sessionContext.getDataValue(userIdLabel);
		   }
		   catch (Exception e){
		   }
		   if (fbsUserStaffNo == null || fbsUserStaffNo.trim().length() < 1 || 
			   sessionStaffNo == null || sessionStaffNo.trim().length() < 1 ||
			   !sessionStaffNo.equals(fbsUserStaffNo)){
			   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "Cookie's staffno is not matched with staffno in memory[" + fbsUserStaffNo + "][" + sessionStaffNo + "]");
			   session = null;
		   }
	   }
	   
	   //如果是主节点，又是createSession MVC 就直接创建吧，不用再去找了，费神
	   if( createWhenNotCreate && this.masterFlag)
	   {
		   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Master node, a create session mvc, so create a new session");
		   session = createNewSession(responseObj);
		   
		   if( request != null && session != null )
			   request.setAttribute("_emp_Session_FBSCrtNewSession", session.getId());

		   if( this.bindClientIP )	//检查客户端IP绑定
		   {
			   String reqIP = request.getRemoteAddr();
			   session.setAttribute("_CLTIP", reqIP);
		   }
	   }

	   //如果还没找到session
	   if (session == null) {
		   //如果是主节点
		   if (masterFlag){
			   //主节点或从节点到主节点扫描
			   if ("TRUE".equals(SSOMasterFlag) || "FALSE".equals(SSOMasterFlag)){
				   //到此为止，如果还没找到就这样吧
				   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Master node, receive a sso request, cant find session, over. sid[" + sessionId + "],SSOFlag[" + SSOMasterFlag + "]");
			   }
			   else {
				   //主节点的一般请求，如果没找到，如果有sessionId，则去其它主节点找找
				   if (sessionId != null) {
					   //去找找吧
					   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Master node, a normal request, cant find session, search other master node[" + sessionId + "]");
					   session = checkSSOSession(sessionId, fbsUserStaffNo, requestObj, responseObj);
				   }
			   }
		   }
		   //如果是从节点
		   else {
			   //主节点或从节点到从节点扫描，这应该是不可能的
			   if ("TRUE".equals(SSOMasterFlag) || "FALSE".equals(SSOMasterFlag)){
				   //这种情况不应该发生
				   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "Slave node, receive a sso request, this is a fault. sid[" + sessionId + "],SSOFlag[" + SSOMasterFlag + "]");
			   }
			   else {
				   //从节点的一般请求，如果没找到，如果有sessionId，则去其它主节点找找
				   if (sessionId != null) {
					   //去找找吧
					   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Slave node, a normal request, cant find session, search other master node[" + sessionId + "]");
					   session = checkSSOSession(sessionId, fbsUserStaffNo, requestObj, responseObj);
				   }
			   }
		   }
		   
		   //同步完之后，看看同步后和前端送上的柜员号是否一致，如果柜员号和上送的柜员号不一致，则设计上有问题
		   if (session != null){
			   Context sessionContext = (Context)session.getAttribute(EMPConstance.ATTR_CONTEXT);
			   String sessionStaffNo = null;
			   try {
				   if (sessionContext != null)
					   sessionStaffNo = (String)sessionContext.getDataValue(userIdLabel);
			   }
			   catch (Exception e){
			   }
			   if (fbsUserStaffNo == null || fbsUserStaffNo.trim().length() < 1 || 
				   sessionStaffNo == null || sessionStaffNo.trim().length() < 1 ||
				   !sessionStaffNo.equals(fbsUserStaffNo)){
				   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "After sync, userid is not match with staffno in memory[" + fbsUserStaffNo + "][" + sessionStaffNo + "]");
				   session = null;
			   }

			   //只有FBSBase的类有的一段逻辑
			   if (sessionStaffNo != null && sessionStaffNo.trim().length() > 0) {
				   //UserCache.clearUserCache(sessionStaffNo);
			   }
		   }
	   }

	   //增加主节点第一次创建session时的SSO柜员信息清除，不管登录是否成功,清除之,如果登录成功，在welcome.jsp中重新设定改用户的cookie
	   if (createWhenNotCreate && this.masterFlag && responseObj instanceof HttpServletResponse) {
		   Cookie cookie = new Cookie(userIdLabel, null);
		   cookie.setMaxAge(this.cookieMaxAge);
		   cookie.setPath("/");
		   ((HttpServletResponse)responseObj).addCookie(cookie);
		   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "Master Node: Clear cookie's staffno!");
	   }

	   if( request != null && session != null )
		   request.setAttribute(EMPConstance.ATTR_SESSION, session);

	   if( session != null )
		   session.setLastAccessTime(System.currentTimeMillis());

	   EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "getSession Function Return [" + session + "]");

	   return session;
   }

   
   /**
    * 创建新的Session。
    * 
    * @param responseObj 请求对象
    * @return Session
    */
   private EMPSession createNewSession(Object responseObj)
   {
		String sessionId = this.generateSessionId(); // create new session
		EMPSession session = new EMPSession(sessionId);
		Thread current = Thread.currentThread();
		// cookie session
		if (this.sessionTraceType == 0 && responseObj != null
				&& responseObj instanceof HttpServletResponse) {
			HttpServletResponse response = (HttpServletResponse) responseObj;
			Cookie cookie = new Cookie(sessionIdLabel, sessionId);
			cookie.setMaxAge(this.cookieMaxAge);
			cookie.setPath("/");
			response.addCookie(cookie);

/*			cookie = new Cookie(userIdLabel, null);
			cookie.setMaxAge(this.cookieMaxAge);
			cookie.setPath("/");
			response.addCookie(cookie);
*/		}

		// url rewrite session
		if (this.bindCookie && sessionTraceType == 1 && responseObj != null
				&& responseObj instanceof HttpServletResponse) {
			HttpServletResponse response = (HttpServletResponse) responseObj;
			Cookie cookie = new Cookie(sessionIdLabel, sessionId);
			cookie.setMaxAge(this.cookieMaxAge);
			cookie.setPath("/");
			response.addCookie(cookie);

/*			cookie = new Cookie(userIdLabel, null);
			cookie.setMaxAge(this.cookieMaxAge);
			cookie.setPath("/");
			response.addCookie(cookie);
*/		}

		EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0,
				"TID["+current.getId() + "],"+ "Create new Session:" + sessionId + " Total session size: "
						+ (sessions.size() + 1));
		synchronized (sessions) {
			sessions.put(sessionId, session);
		}

		return session;
   }
   
   /**
    * 创建新的Session。
    * 
    * @param responseObj 请求对象
    * @return Session
    */
   private EMPSession checkSSOSessionHost(String token, String staffNo, Object requestObj, Object responseObj, String hostIp)
   {
		if (requestObj instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) requestObj;
			String sessionId = token;
			EMPSession session = null;
			Context sessionContext = null;
			boolean findSession = false;
			Thread current = Thread.currentThread();
			
			if (sessionId != null){
				session = (EMPSession)sessions.get(sessionId);
				if (session != null) {
					findSession = true;
					sessionContext = (Context)session.getAttribute(EMPConstance.ATTR_CONTEXT);
				}
				else {
					session = new EMPSession(sessionId);
				}
				
				if (sessionContext == null)
				{
					try {
						sessionContext = ((EMPFlowComponentFactory)EMPFlowComponentFactory.getComponentFactory(this.factoryName)).getContext(null, this.sessionContextName);
					}
					catch (Exception e) {
						EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "Create SessionContext error[" + this.factoryName +"][" + this.sessionContextName + "]!", e);
						return null;
					}
					session.setAttribute(EMPConstance.ATTR_CONTEXT, sessionContext);
				}
			}

			String method = "POST";
			String Cookie = sessionIdLabel + "=" + token + ";" + userIdLabel + "=" + staffNo + ";";

/*			String strURL = request.getRequestURL().substring(0,
					request.getRequestURL().indexOf(request.getRequestURI())-5)+ this.masterAppSSOUrl;
*/
			String strURL = "http://" + hostIp + this.masterAppSSOUrl + "?SSOMasterFlag=" + String.valueOf(this.masterFlag).toUpperCase();

			URL reqUrl = null;
			EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "TID["+current.getId() + "],"+ "find session from a host[" + token + "] url is:" + strURL);
			HttpURLConnection urlConn = null;
			try {
				reqUrl = new URL(strURL);
				urlConn = (HttpURLConnection) reqUrl.openConnection();

				urlConn.setRequestMethod(method);
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				urlConn.setUseCaches(false);

				urlConn.addRequestProperty("POST", "/  HTTP/1.1");
				urlConn.addRequestProperty("Cookie", Cookie);

				urlConn.getOutputStream().write(Cookie.getBytes());
				urlConn.getOutputStream().flush();
				urlConn.getOutputStream().close();

				InputStream in = urlConn.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(in, "UTF-8"));
				Vector<String> returnStr = new Vector<String>();
				StringBuffer temp = new StringBuffer();
				String line = bufferedReader.readLine();
				while (line != null) {
					if (line.trim().length() > 0) {
						returnStr.add(line);
					}
					line = bufferedReader.readLine();
				}
				bufferedReader.close();

				if (HttpURLConnection.HTTP_OK == urlConn.getResponseCode()) {
					String tmpUserStaffNo = null;
					String[] tmpStrings =  returnStr.toString().replace("[", "").replace("]", "").split("\\|");
					for (int i = 0; i < tmpStrings.length; i++){
						if (tmpStrings[i].trim().length() > 0) {
							String[] tmpUserInfo = tmpStrings[i].split("\\:");
							if ("RETCODE".equals(tmpUserInfo[0])) {
								if ("FAIL".equals(tmpUserInfo[1])) {
									EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "the host return fail!, Please check reason!["+ sessionId + "]");
									return null;
								}
							}
							else {
								if (tmpUserInfo.length > 1) {
									sessionContext.setDataValue(tmpUserInfo[0], tmpUserInfo[1]);
									
									if (userIdLabel.equals(tmpUserInfo[0])){
										tmpUserStaffNo = tmpUserInfo[1];
									}
								}
							}
						}
					}
					
					if (!findSession) {
						synchronized (sessions) {
							sessions.put(sessionId, session);
						}
						EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0,
								"TID["+current.getId() + "],"+ "after sync, create a new session:[" + sessionId + "],staffno["+tmpUserStaffNo+"], Total session size: " + (sessions.size() + 1));
					}
					else {
						EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0,
								"TID["+current.getId() + "],"+ "after sync, use old session:[" + sessionId + "],staffno["+tmpUserStaffNo+"] Total session size: " + (sessions.size() + 1));
					}
					
					return session;
				} 
				else {
					EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "the host is Down!, Please check reason![" + urlConn.getResponseCode()+"]");
					return null;
				}
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "TID["+current.getId() + "],"+ "this conn to the host occurs error!, please check reason!", e);
				return null;
			} finally {
				if (urlConn != null)
					urlConn.disconnect();
			}
		}

		return null;
  }

   /**
    * 创建新的Session。
    * 
    * @param responseObj 请求对象
    * @return Session
    */
   private EMPSession checkSSOSession(String token, String staffNo, Object requestObj, Object responseObj)
   {
	   EMPSession session = null;

	  /* String SSOHost1 = ReadProperty.getValue("SSO.HOST1");
	   String SSOHost2 = ReadProperty.getValue("SSO.HOST2");
	   String SSOHost3 = ReadProperty.getValue("SSO.HOST3");
	   String SSOHost4 = ReadProperty.getValue("SSO.HOST4");
	   if (session == null && SSOHost1 != null && SSOHost1.trim().length() > 0){
		   session = checkSSOSessionHost(token, staffNo, requestObj, responseObj, SSOHost1);
	   }
	   if (session == null && SSOHost2 != null && SSOHost2.trim().length() > 0) {
		   session = checkSSOSessionHost(token, staffNo, requestObj, responseObj, SSOHost2);
	   }
	   if (session == null && SSOHost3 != null && SSOHost3.trim().length() > 0) {
		   session = checkSSOSessionHost(token, staffNo, requestObj, responseObj, SSOHost3);
	   }
	   if (session == null && SSOHost4 != null && SSOHost4.trim().length() > 0) {
		   session = checkSSOSessionHost(token, staffNo, requestObj, responseObj, SSOHost4);
	   }*/

	   return session;
  }
 
   /**
	 * 清除拥有指定id的session。
	 * 
	 * @param sessionId 要清除的Session的id
	 */
    private String getHostIp() {
		try {
	    	Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while(netInterfaces.hasMoreElements())    
			{    
			    NetworkInterface ni= (NetworkInterface)netInterfaces.nextElement();
		        Enumeration<InetAddress> ips = ni.getInetAddresses();   
		        while (ips.hasMoreElements()) {   
		        	InetAddress oneAddress=(InetAddress) ips.nextElement();
				    if (oneAddress.isSiteLocalAddress() && 
				    	!oneAddress.isLoopbackAddress() && 
				    	oneAddress.getHostAddress().indexOf(":") == -1) {
					    if (oneAddress.getHostAddress().startsWith("10.1")){
							EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "SSO Session Find Host Ip Address is [" + oneAddress.getHostAddress() + "]!");
					    	return oneAddress.getHostAddress();
					    }
				    }
		        }   
			}
		}
		catch(Exception e) {
			EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "SSO Session Check Find Host Ip Address Error, Please check reason!", e);
		}
		
		return "127.0.0.1";
    }

	/**
	 * 清除拥有指定id的session。
	 * 
	 * @param sessionId 要清除的Session的id
	 */
	public void removeSession(String sessionId) {
		removeSession((Session) sessions.get(sessionId));
	}
   
	/**
	 * 清除指定Session。
	 * 
	 * @param session 要清除的session
	 */
	public void removeSession(Session session) {
		if (session == null)
			return;

		synchronized (sessions) {

			sessions.remove(session.getId());

			//do terminate the session context
			Context sessionContext = (Context) session
					.getAttribute(EMPConstance.ATTR_CONTEXT);
			if (sessionContext != null)
				sessionContext.terminate();
		}
		
		
	}
   
	/**
	 * 指定Session的本次请求结束时调用，在其中处理Session的保存和持续。
	 * 
	 * @param session Session
	 */
	public void sessionRequestEnd(Session session) {

	}
   
   
	private static final char DICTIONARY[] = { 'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
		'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
   

	/**
	 * 生成唯一的SessionId。
	 * 
	 * @return session id
	 */
	private String generateSessionId() {

		String sessionKey = getSessionKey();

		byte abyte0[] = sessionKey.getBytes();
		try {
			String encryptionAlgorithm = "SHA";
			MessageDigest messageDigest = MessageDigest
					.getInstance(encryptionAlgorithm);
			abyte0 = messageDigest.digest(abyte0);

			StringBuffer stringbuffer = new StringBuffer(40);

			for (int l = 0; l < abyte0.length; l++) {
				int k = abyte0[l] + 128;
				int i = k / DICTIONARY.length;
				int j = k % DICTIONARY.length;
				stringbuffer.append(DICTIONARY[i]);
				stringbuffer.append(DICTIONARY[j]);
			}

			return stringbuffer.toString();

		} catch (Exception _ex) {
			EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0,
					"Failed to generate sessionId !");
		}

		return null;
	}
   
   
	private static Object syncObj = new Object();

   /**
    * 生成用于生成SessionId的key。
    * 
    * @return key
    */
	private String getSessionKey() {
		char SEPARATOR = '#';
		StringBuffer buf = new StringBuffer();

		buf.append(getLocalHost()); //机器地址

		buf.append(SEPARATOR);

		buf.append(String.valueOf(instanceIdx)); //实例计数
		buf.append(SEPARATOR);

		long l;
		synchronized (syncObj) {
			l = ++counter;
		}

		buf.append(Long.toHexString(l)); //计数器
		buf.append(SEPARATOR);

		buf.append(Long.toHexString(System.currentTimeMillis())); //当前时间
		buf.append(SEPARATOR);

		return buf.toString();

	}
   
	private String computerAddr;
   
   /**
	 * 获得本地IP地址。
	 * 
	 * @return 本地IP地址
	 */
	private String getLocalHost() {
		if (computerAddr == null) {
			try {
				computerAddr = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception _ex) {
				computerAddr = "UNKNOW_IP";
			}
		}
		return computerAddr;
	}

	/**
	 * 检查会话超时的间隔时间。
	 * 
	 * @return 检查会话超时的间隔时间
	 */
	public int getSessionCheckInterval() {
		return sessionCheckInterval;
	}

	/**
	 * 设置检查会话超时的间隔时间。
	 * 
	 * @param sessionCheckInterval 检查会话超时的间隔时间
	 */
	public void setSessionCheckInterval(int sessionCheckInterval) {
		this.sessionCheckInterval = sessionCheckInterval;
	}

	/**
	 * 获得会话超时时间。
	 * 
	 * @return 会话超时时间
	 */
	public int getSessionTimeOut() {
		return sessionTimeOut;
	}

	/**
	 * 设置会话超时时间。
	 * 
	 * @param sessionTimeOut 会话超时时间
	 */
	public void setSessionTimeOut(int sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}
	
	
	/**
	 * 初始化方法。
	 * 创建定时器和定时任务。
	 */
	public void initialize() {
		EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.INFO, 0, "Start up session checker for FBSEMPSessionManager [" + name + "] sessionTimeOut=" + this.sessionTimeOut + " check interval=" + this.sessionCheckInterval);
		timer =  new Timer();
		timer.schedule( new FBSSessionCheckTask(), this.sessionTimeOut, this.sessionCheckInterval);
		EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.INFO, 0, "Start up session checker for FBSEMPSessionManager [" + name + "] ok!" );
	}
	
 
	/**
	 * Session超时检查内部类，定时执行超时检查。
	 *
	 * @author zhongmc
	 * @version 2.1
	 * @since 1.0 2006-10-27
	 * @lastmodified 2008-7-1
	 */
	private class FBSSessionCheckTask extends TimerTask {
		public void run()
 		{
			clearFBSSessionOfTimeout();
		}
	}
	
	/**
	 * 清除超时的session
	 * @author liubq
	 * @since 2009-8-31
	 */
	public void clearFBSSessionOfTimeout() {
		EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "Do the session check for FBSEMPSessionManager [" + name + "]");
		Object[] keys = null;
		try{
			keys = sessions.keySet().toArray();
		}catch(Exception e)
		{
			EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "Failed to do session time out check: ", e );
			return;
		}
		for( int i=0; i<keys.length; i++)
		{
			try {
				EMPSession session = (EMPSession)sessions.get( keys[i]);
				if( session == null )
					continue;
				
				EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "Looping a Session [" + session.getSessionId() + "] currTime is [" + System.currentTimeMillis() + "], lastAccess is ["+session.getLastAccessTime()+"]["+(System.currentTimeMillis()-session.getLastAccessTime())+"], config is[" + this.sessionTimeOut + "]");				
				if(  checkTimeout(session) )	//remove the timeout session
				{
					EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.DEBUG, 0, "Session time out: " + session.getSessionId() + " in " + name );
					
					if( sessionTimeoutListener != null )
						sessionTimeoutListener.sessionTimeout(session.getId(), (Context)session.getAttribute(EMPConstance.ATTR_CONTEXT ));

					removeSession(session );
				}
			} catch (Exception e) {
				EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "Failed to do session time out check", e );
			}
		}
	}
	
	/**
	 * 使用当前请求参数对请求URL进行处理，如附加Session ID等。
	 * 
	 * @param request  HTTP请求
	 * @param response HTTP响应
	 * @param url 请求URL
	 * @param method 请求类型，GET/POST等
	 * @return URL
	 */
	public String encodeURL(HttpServletRequest request, HttpServletResponse response, String url, String method) {
		 
		if (this.sessionTraceType == 0) //cookie session trace
			return url;

		if ("POST".equalsIgnoreCase(method))
			return url;

		EMPSession session = (EMPSession) request
				.getAttribute(EMPConstance.ATTR_SESSION);
		if (session == null)
			return url;

		String sessionId = session.getSessionId();

		int idx = url.indexOf('?');
		if (idx == -1) {
			return url + "?" + sessionIdLabel + "=" + sessionId;
		} else {
			return url + "&" + sessionIdLabel + "=" + sessionId;
		}
		   
//		   if( url.indexOf(SID_PREFEX) != -1 )
//			   return url;
//		   
//		   EMPSession session = (EMPSession)request.getAttribute(EMPConstance.ATTR_SESSION);
//		   if( session == null )
//			   return url;
//		   String sessionId = session.getSessionId();
//		   int idx = url.indexOf('?');
//		   if( idx == -1 )
//		   {
//			   return url + SID_PREFEX + sessionId + SID_TAIL;
//		   }
//		   else
//		   {
//			   String tmp = url.substring(0, idx);
//			   String tmp1 = url.substring(idx);
//			   return tmp + SID_PREFEX + sessionId + SID_TAIL + tmp1;
//		   }
		   
	}

	/**
	 * 获得会话管理器名称。
	 * 
	 * @return 会话管理器名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置会话管理器名称。
	 * 
	 * @param name 会话管理器名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获得Session跟踪方式。
	 * 
	 * @return Session跟踪方式
	 */
	public int getSessionTraceType() {
		return sessionTraceType;
	}

	/**
	 * 设置Session跟踪方式(int)。
	 * 
	 * @param sessionTraceType Session跟踪方式的int值
	 */
	public void setSessionTraceType(int sessionTraceType) {
		this.sessionTraceType = sessionTraceType;
	}
	
	/**
	 * 设置Session跟踪方式，取值：COOKIE or URLREWRITE。
	 * <p>
	 * 若为Cookie方式，则会自动将Session id保存在浏览器Cookie中，但一个浏览器进程同时只能对应一个Session；
	 * URLRewrite则要求每次请求都将Session id作为参数提交，实现同时对应多个Session的情况。
	 * 
	 * @param value Session跟踪方式
	 */
	public void setSessionTraceType(String value) {
		if( COOKIE_SESSION.equals( value ))
			sessionTraceType = 0;
		else if( URLREWRITE_SESSION.equals( value ))
			sessionTraceType = 1;
	}

	/**
	 * 当POST请求时追加的一些请求域。
	 * sessionTraceType为URLREWRITE时需要追加SessionId。
	 * 
	 * @param request HTTP请求
	 * @param targetClient 客户端种类
	 * @return 追加的请求域HTML内容
	 */
	public String getAppendPostField(HttpServletRequest request, String targetClient) {
		Session session = (Session)request.getAttribute(EMPConstance.ATTR_SESSION );
		if( session == null )
			return "";
		
		if( sessionTraceType == 0 )	//cookie session trace, append nothing
			return "";
		
		
		if("WML".equalsIgnoreCase( targetClient ))
		{
			StringBuffer buf = new StringBuffer("\n\t<postfield name=\"");
			buf.append( sessionIdLabel );
			buf.append( "\" value=\"");
			buf.append( session.getId());
		    buf.append( "\"/>\n");
			return buf.toString();
		}
		else
		{
			StringBuffer buf = new StringBuffer("<input type=\"hidden\" name=\"" );
			buf.append( sessionIdLabel );
			buf.append( "\" value=\"");
			buf.append( session.getId());
			buf.append( "\"/>");
			
			return buf.toString();
		}
	}

	/**
	 * 获得在使用URLRewrite方式时用于传递sessionId的参数名。
	 * 
	 * @return sessionId参数名
	 */
	public String getSessionIdLabel() {
		return sessionIdLabel;
	}

	/**
	 * 设置在使用URLRewrite方式时用于传递sessionId的参数名，默认为EMP_SID。
	 * 
	 * @param sessionIdLabel sessionId参数名
	 */
	public void setSessionIdLabel(String sessionIdLabel) {
		this.sessionIdLabel = sessionIdLabel;
	}

	/**
	 * 获得会话超时监听器。
	 * 
	 * @return 会话超时监听器
	 */
	public SessionTimeoutListener getSessionTimeoutListener() {
		return sessionTimeoutListener;
	}

	/**
	 * 注入会话超时监听器。
	 * 
	 * @param sessionTimeoutListener
	 */
	public void setSessionTimeoutListener(SessionTimeoutListener sessionTimeoutListener) {
		this.sessionTimeoutListener = sessionTimeoutListener;
	}
	
	/**
	 * 获得当前会话管理器中Session总数量。
	 * 
	 * @return Session数量
	 */
	public int getSessionCount() {
		return sessions.size();
	}

	/**
	 * 设置会话管理器ID。
	 * 
	 * @param value 会话管理器ID
	 */
	public void setId(String value) {
		this.setName(value);
	}

	/**
	 * 以(id, Session)的Map形式获得当前会话管理器中的所有Session。
	 * 
	 * @return (id, Session)的映射Map
	 */
	public Map getSessions() {
		return sessions;
	}

	/**
	 * 将(id,Session)形式的Map注入当前会话管理器。
	 * 
	 * @param sessions (id, Session)的映射Map
	 */
	public void setSessions(Map sessions) {
		this.sessions = sessions;
	}
	
	/**
	 * 终止会话管理器。
	 */
	public void terminate() {
		if( timer == null )
			return;
		
		//EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.INFO, 0, "Terminate the EMPSessionManager [" + name + "]");		

		try{
			timer.cancel();
			timer = null;
			this.sessions = new HashMap();
		}catch(Exception e)
		{
			EMPLog.log(EMPConstance.EMP_SESSION_MGR, EMPLog.ERROR, 0, "failed to Terminate the EMPSessionManager [" + name + "]", e);		
			
		}
	}

	/**
	 * Service别名
	 */
	private String alias;
	
	/**
	 * 设置Service别名。
	 * 
	 * @param value 别名
	 */
	public void setAlias(String value) {
		alias = value;
	}

	/**
	 * 获得Service别名。
	 */
	public String getAlias() {
		return this.alias;
	}

	/**
	 * 判断是否唯一实例化。
	 * 
	 * @return 是否唯一实例化
	 */
	public boolean isSingleton() {
		return true;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	/**
	 * 判断是否与客户端的IP绑定。
	 * 
	 * @return 是否与客户端的IP绑定
	 */
	public boolean isBindClientIP() {
		return bindClientIP;
	}

	/**
	 * 设置是否与客户端的IP绑定，避免非法获得或假冒SessionId。
	 * 
	 * @param bindClientIP 是否与客户端的IP绑定
	 */
	public void setBindClientIP(boolean bindClientIP) {
		this.bindClientIP = bindClientIP;
	}

	/**
	 * 判断是否与客户端cookie绑定。
	 * 
	 * @return 是否与客户端cookie绑定
	 */
	public boolean isBindCookie() {
		return bindCookie;
	}

	/**
	 * 设置是否与客户端cookie绑定。
	 * 
	 * @param bindCookie 是否与客户端cookie绑定
	 */
	public void setBindCookie(boolean bindCookie) {
		this.bindCookie = bindCookie;
	}
	
	/**
	 * 判断是否Session主应用。
	 * 
	 * @return 是否Session主应用
	 */
	public boolean isMasterFlag() {
		return masterFlag;
	}

	/**
	 * 设置是否Session主应用。
	 * 
	 * @param masterFlag是否Session主应用
	 */
	public void setMasterFlag(boolean masterFlag) {
		this.masterFlag = masterFlag;
	}

	/**
	 * 主应用URL。
	 * 
	 * @return 主应用URL
	 */
	public String getMasterAppUrl() {
		return masterAppSSOUrl;
	}

	/**
	 * 非主应用设置主应用URL。
	 * 
	 * @param masterAppUrl主应用URL
	 */
	public void setMasterAppSSOUrl(String masterAppSSOUrl) {
		this.masterAppSSOUrl = masterAppSSOUrl;
	}

	/**
	 * 得到组件工厂对象名称
	 * @return
	 */
	public String getFactoryName() {
		return factoryName;
	}

	/**
	 * 设置组件工厂对象名称
	 * @param factoryName
	 */
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	* 获得SessionContext名称。
	* 
	* @return SessionContext名称
	*/
	public String getSessionContextName() 
	{
		return sessionContextName;
	}
	
	/**
	 * 设置SessionContext名称。
	 * 
	 * @param sessionContextName SessionContext名称
	 */
	public void setSessionContextName(String sessionContextName) 
	{
		this.sessionContextName = sessionContextName;
	}

	/**
	 * 判断Session是否已经超时，可以通过扩展此方法，实现特定的Session检查机制
	 * @param session
	 * @return
	 */
	protected boolean checkTimeout(EMPSession session){
		 return System.currentTimeMillis() - session.getLastAccessTime() > this.sessionTimeOut;	
	}

	public int getCookieMaxAge() {
		return cookieMaxAge;
	}

	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

//	public void registAsMBean()
//	{
//		EMPMBeanInfo beanInfo = new EMPMBeanInfo();
//		beanInfo.setId( this.getName());
//		beanInfo.setName( this.getName());
//		beanInfo.setDisplayName(this.getName());
//		beanInfo.setDescription("the application session Manager Component");
//		beanInfo.setType("SESSION");
//
//		AttrInfo attr1 = new AttrInfo("sessionCount", "R", "sessionCount", "Session Count");
//		beanInfo.addAttrInfo( attr1 );
//		AttrInfo attr3 = new AttrInfo("sessionTimeOut", "RW", "sessionTimeOut", "Session TimeOut time");
//		beanInfo.addAttrInfo( attr3 );
//		AttrInfo attr2 = new AttrInfo("sessionCheckInterval", "RW", "sessionCheckInterval", "Session Check Interval Time");
//		beanInfo.addAttrInfo( attr2 );
//	
//	//OperationInfo(String opName, String displayName, String[]paramNames,  String description )	
//		OperationInfo opInfo0 = new OperationInfo("getLocalHost", "getLocalHost", null, "get address IP of server");
//		beanInfo.addOperationInfo( opInfo0 );
//		
//		if( EMPJMXManager.getInstance() != null )
//			EMPJMXManager.getInstance().doRegistMBean(this, beanInfo );
//	}
}
