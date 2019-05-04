/**
 * 
 */
package core.log;

import java.util.Map;

import com.ecc.emp.accesscontrol.AccessController;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.log.Log;
import com.ecc.emp.log.LogInterceptor;
import common.util.SFConst;

/**
 * @author heqiang
 * 
 */
public class SFLogInertceptor  implements
		AccessController, LogInterceptor {

	private ThreadLocal componentIdRes = new ThreadLocal();

	private ThreadLocal uniqIdRes = new ThreadLocal();

	private int uniqIdLen = 8;

	private static int uniqId = 0;

	private String sessionIdField = null;

	/**
	 * 用于输出日志的实现
	 */
	private Log logInstance = null;

	/**
	 * 用于添加到LogComponentId 前面的内容，这样就可以进一部对日志进行分类，比如此ID为渠道ID
	 */
	private String componentIdPrefix = null;

	/**
	 * component ID 映射关系，允许将多种业务请求的日志输出到一个类别的日志中，比如：按照查询类交易，和帐务类交易，以及 不同主机类分类
	 */
	private Map componentIdMap = null;

	/**
	 * 将没有找到映射的ID使用此ID
	 */
	private String noneMapedComponentId = null;

	/**
	 * 是否保留那些没有从映射中找到的componentId
	 */
	private boolean remainNoneMapedComponentId = true;

	/**
	 * 是否在Log 信息中按照单一请求 附加唯一标识
	 */
	private boolean appendUniqId = false;

	private static Object synObj = new Object();

	public SFLogInertceptor() {
		super();
		synchronized (synObj) {
			if (EMPLog.logInterceptors.size() == 0) {
				EMPLog.registLogInterceptor(this);
			}
			else {
				for (int i = 0; i <EMPLog.logInterceptors.size(); i++) {
					if (EMPLog.logInterceptors.get(i).getClass().isInstance(this))
						break;
					
					if (i > EMPLog.logInterceptors.size())
						EMPLog.registLogInterceptor(this);
				}
			}
		}
	}

	public boolean log(String component, int type, int level, String message,
			Throwable te) {
//		String logComponentId = (String) componentIdRes.get();
//
//		if (logComponentId == null){
//			if (logInstance == null)
//				return false;
//
//			componentIdRes.set("NoSession");
//			logComponentId = "NoSession";
//
//			if (this.uniqIdRes.get() == null)
//				this.uniqIdRes.set(this.getUniqId());
//		}
//
//		
//		String tmpMsg = msg;
//		if (this.appendUniqId)
//			tmpMsg = this.uniqIdRes.get() + " " + msg;
//		else
//			tmpMsg = msg;
//
//		tmpMsg = logComponentId + " " + tmpMsg;
//		logInstance.log(component, type, level, tmpMsg, te);

		SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
		
		//Logger logger = Logger.getLogger( component );
		
		//this.sendNotify(component, type, level, message, exception );
		
		switch( type )
		{
		case 0: //DEBUG
			logger.debug( message);
			break;
		case 1:	//TRACE
			logger.trace( message );
			break;
		case 2: //INFO:
			logger.info( message );
			break;
		case 3:// WARNING:
			logger.info( message );
			break;
		case 4:// ERROR:
			logger.error( message,te);
			break;
		case 5:// FATAL:
			logger.fatal( message );
			break;
		}
		return true;
	}

	/**
	 * 检查是否可以访问actionId指定的业务，如果可以访问，则返回对应的访问控制 对象，如果没有定义访问控制，则直接返回null,
	 * 则直接返回，否则抛出相关异常
	 * 
	 * @param context
	 *            : sessionContext
	 * @param actionId
	 * @return
	 * @throws EMPException
	 */
	public Object checkAccess(Context context, Object requestObj,
			String actionId) throws EMPException {
		String userId = null, branchNo = null;
		try {
			userId = (String) context.getDataValue("_FBS_USER_STAFFNO");
		} catch (Exception e) {
			userId = "";
		}
		String realId = actionId;
		if (this.componentIdMap != null) {
			realId = (String) componentIdMap.get(actionId);
		}

		if (realId == null) {
			if (this.remainNoneMapedComponentId)
				realId = actionId;
			else if (this.noneMapedComponentId != null)
				realId = noneMapedComponentId;
			else
				return null;
		}

		realId = userId + "-" + realId;

		if (this.appendUniqId)
			this.uniqIdRes.set(this.getUniqId());

		if (this.componentIdPrefix != null)
			realId = componentIdPrefix + "-" + realId;

		componentIdRes.set(realId);

		return actionId;
	}

	/**
	 * 开始访问actionId指定的业务，用于需要进行并发控制的业务访问控制器，来统计并发及访问时间
	 */
	public void beginAccess(Object accessInfo) {
		// resources.set( accessInfo );
	}

	/**
	 * 结束访问actionId指定的业务，用于需要进行并发控制的业务访问控制器，来统计并发及访问时间
	 * 
	 * @param beginTimeStamp
	 */
	public void endAccess(Object accessInfo, long beginTimeStamp) {
		componentIdRes.set(null);

		if (this.appendUniqId)
			this.uniqIdRes.set(null);

	}

	private String getUniqId() {
		int id;
		synchronized (synObj) {
			uniqId++;
			id = uniqId;
		}

		String tmp = String.valueOf(id);

		char[] chrTmp = new char[this.uniqIdLen];
		int len = tmp.length();
		if (len > uniqIdLen)
			len = uniqIdLen;
		tmp.getChars(0, len, chrTmp, uniqIdLen - len);
		for (int i = 0; i < uniqIdLen - len; i++)
			chrTmp[i] = '0';
		String logIdStr = new String(chrTmp);
		return logIdStr;
	}

	public void setLog(Log log) {
		this.logInstance = log;
	}

	public Log getLog() {
		return logInstance;
	}

	public String getComponentIdPrefix() {
		return componentIdPrefix;
	}

	public void setComponentIdPrefix(String componentIdPrefix) {
		this.componentIdPrefix = componentIdPrefix;
	}

	public Map getComponentIdMap() {
		return componentIdMap;
	}

	public void setComponentIdMap(Map componentIdMap) {
		this.componentIdMap = componentIdMap;
	}

	public boolean isAppendUniqId() {
		return appendUniqId;
	}

	public void setAppendUniqId(boolean appendUniqId) {
		this.appendUniqId = appendUniqId;
	}

	public int getUniqIdLen() {
		return uniqIdLen;
	}

	public void setUniqIdLen(int uniqIdLen) {
		this.uniqIdLen = uniqIdLen;
	}

	public String getNoneMapedComponentId() {
		return noneMapedComponentId;
	}

	public void setNoneMapedComponentId(String noneMapedComponentId) {
		this.noneMapedComponentId = noneMapedComponentId;
	}

	public boolean isRemainNoneMapedComponentId() {
		return remainNoneMapedComponentId;
	}

	public void setRemainNoneMapedComponentId(boolean remainNoneMapedComponentId) {
		this.remainNoneMapedComponentId = remainNoneMapedComponentId;
	}

	public String getSessionIdField() {
		return sessionIdField;
	}

	public void setSessionIdField(String sessionIdField) {
		this.sessionIdField = sessionIdField;
	}

}
