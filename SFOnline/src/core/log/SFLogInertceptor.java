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
	 * ���������־��ʵ��
	 */
	private Log logInstance = null;

	/**
	 * ������ӵ�LogComponentId ǰ������ݣ������Ϳ��Խ�һ������־���з��࣬�����IDΪ����ID
	 */
	private String componentIdPrefix = null;

	/**
	 * component ID ӳ���ϵ����������ҵ���������־�����һ��������־�У����磺���ղ�ѯ�ཻ�ף��������ཻ�ף��Լ� ��ͬ���������
	 */
	private Map componentIdMap = null;

	/**
	 * ��û���ҵ�ӳ���IDʹ�ô�ID
	 */
	private String noneMapedComponentId = null;

	/**
	 * �Ƿ�����Щû�д�ӳ�����ҵ���componentId
	 */
	private boolean remainNoneMapedComponentId = true;

	/**
	 * �Ƿ���Log ��Ϣ�а��յ�һ���� ����Ψһ��ʶ
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
	 * ����Ƿ���Է���actionIdָ����ҵ��������Է��ʣ��򷵻ض�Ӧ�ķ��ʿ��� �������û�ж�����ʿ��ƣ���ֱ�ӷ���null,
	 * ��ֱ�ӷ��أ������׳�����쳣
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
	 * ��ʼ����actionIdָ����ҵ��������Ҫ���в������Ƶ�ҵ����ʿ���������ͳ�Ʋ���������ʱ��
	 */
	public void beginAccess(Object accessInfo) {
		// resources.set( accessInfo );
	}

	/**
	 * ��������actionIdָ����ҵ��������Ҫ���в������Ƶ�ҵ����ʿ���������ͳ�Ʋ���������ʱ��
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
