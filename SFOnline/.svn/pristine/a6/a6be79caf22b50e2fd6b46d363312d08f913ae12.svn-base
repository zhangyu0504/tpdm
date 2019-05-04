package core.jdbc.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecc.emp.accesscontrol.AccessInfo;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.jdbc.EMPJDBCException;
import com.ecc.emp.jdbc.InvalidParamException;
import com.ecc.emp.jdbc.JDBCMBean;
import com.ecc.emp.jmx.support.EMPJMXManager;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.service.EMPService;

import core.jdbc.sql.SQLParameter;

/**
 * 
 * <b>功能描述：</b><br>
 * 数据库存储过程访问的Service服务类，调用并执行本地数据库的存储过程。<br>
 * 
 * <b>配置示例：</b><br>
 * &lt;ProcedureAccessService id="ProcedureAccessService"<br>
 * &nbsp;&nbsp;opClass="com.ecc.emp.jdbc.procedure.ProcedureOpForDB2"/&gt;<br>
 * 
 * <b>参数说明:</b><br>
 * opClass--执行存储过程的辅助类名称。<br>
 * 
 * @创建时间 2000-03-02
 * @version 1.0
 * @author ZhongMingChang
 * @modifier GaoLin 2006-10-30
 * 
 */

public class ProcedureAccessService extends EMPService {

	private String opClass = null;

	static long alarmResponseTime = 1000;

	AccessInfo accessInfo = new AccessInfo();

	boolean alarmOpened = true;

	Object syncObj = new Object();
	
	public ProcedureAccessService() {
		super();
	}
	/**
	 * 调用存储过程，处理批量的输入数据
	 * 
	 * @param JDBCProcedureDefine
	 *            aProcedureDefine 存储过程定义
	 * @param String
	 *            iCollName 存储过程输入数据集合名称
	 * @param boolean
	 *            isBatch 批量执行存储过程的标识
	 * @param Context
	 *            aContext 当前context
	 * @param Connection
	 *            connection 当前数据库连接
	 * @return String 存储过程返回码
	 * @throws EMPJDBCException
	 */
	public String executeProcedure(JDBCProcedureDefine aProcedureDefine, Context aContext, Connection connection) throws EMPJDBCException {
		String retCode = "-1";
		CallableStatement call = null;
		ProcedureOp procedureOp = null;
		IndexedCollection iColl = null;
		KeyedCollection kColl = null;

		synchronized (syncObj) {
			this.accessInfo.newAccess();
		}
		long beginTime=System.currentTimeMillis();
		
		int i = 0, circleTimes = 0;
		try {
			StringBuffer logBuffer = new StringBuffer();
			String procedureName = aProcedureDefine.getProcedureName();

			boolean isBatch = aProcedureDefine.getIsBatch();
			String iCollName = aProcedureDefine.getICollName();
			if (iCollName == null) {
				circleTimes = 1;
			} else {
				iColl = (IndexedCollection) aContext.getDataElement(iCollName);
				circleTimes = iColl.size();
			}
				
			if (circleTimes <= 0)
				throw new InvalidParamException("No Input values!iColl.size()<=0");

			procedureOp = (ProcedureOp) Class.forName(opClass).newInstance();

			for (int j = 0; j < circleTimes; j++) {
				if (iCollName != null)
					kColl = (KeyedCollection) iColl.getElementAt(j);
				
				List inParams = aProcedureDefine.getInParams();
				Map inputValues = new HashMap();

				logBuffer.append("Procedure[" + procedureName + "]'s input paramaters: \n");
				for (i = 0; i < inParams.size(); i++) {
					String inName = ((SQLParameter) inParams.get(i)).dataName;
					String inValue = null;
					if (iCollName == null)
						inValue = (String) aContext.getDataValue(inName);
					else
						inValue = (String) kColl.getDataValue(inName);

					inputValues.put(inName, inValue);
					logBuffer.append(inName + " = " + inValue + "\n");
				}

				List outParams = aProcedureDefine.getOutParams();
				List resultSets = aProcedureDefine.getResultSets();

				call = procedureOp.callProc(connection, procedureName, inputValues, inParams, outParams, resultSets);

				// execute the procedure
				call.execute();

				// retrieve the result
				logBuffer.append("Procedure[" + procedureName + "]'s output paramaters: \n");
				for (i = 0; i < outParams.size(); i++) {
					SQLParameter aParam = (SQLParameter) outParams.get(i);
					Object value = call.getObject(aParam.paramIdx);

					if (value != null)
						// value = value.trim();
						// 强制加入ErrorMsgField
						if (aProcedureDefine.errorCodeField != null	&& 
							aProcedureDefine.errorCodeField.equals(aParam.dataName)){
							try {
								if (iCollName == null)
									aContext.setDataValue(aParam.dataName, value.toString());
								else
									kColl.setDataValue(aParam.dataName, value.toString());
							} catch (EMPException ee) {
								EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0,
										"ErrorMsgField[" + aProcedureDefine.errorCodeField + "] not Defined in Context", ee);
								if (iCollName == null)
									aContext.addDataField(aParam.dataName, value.toString());
								else
									kColl.addDataField(aParam.dataName, value.toString());

							}
						} else {
							if (iCollName == null)
								aContext.setDataValue(aParam.dataName, value.toString());
							else
								kColl.setDataValue(aParam.dataName, value.toString());
						}
					logBuffer.append(aParam.dataName + " = " + value + "\n");
				}

				// get the procedure execute retCode
				// 如果定义了retCodeName字段，则取之，否则认为成功
				if (aProcedureDefine.retCodeName != null)
				{
					int retCodeIdx = this.getIndexOf(outParams, aProcedureDefine.retCodeName);
					if (retCodeIdx > 0)
						retCode = call.getString(retCodeIdx);
					else
						retCode = "0";
				}
				else {
					retCode = "0";
				}

				EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0, logBuffer.toString());

				if (Integer.parseInt(retCode) == 0) {
					// return when there is no resultSets
					if (resultSets.size() > 0) {
						if (iCollName == null)
							procedureOp.processResultSets(aContext, null, resultSets, call);
						else
							procedureOp.processResultSets(null, kColl, resultSets, call);
					}
				} 
				else { 
					//返回值非0
					// 单次执行存储过程时直接返回该返回值，   
					// IColl批量执行该存储过程时,如果没有设置批量标志位，程序继续执行，如果设置了批量标志位，返回值不为0直接跳出
					if (isBatch)
						return retCode;
				}
			}

			return retCode;

		} catch (SQLException se) {
			EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.ERROR, 0,
					"ProcedureAccessService[" + getName() + "].executeProcedure", se);
			throw new EMPJDBCException("Failed to execute procedure[" + aProcedureDefine.getProcedureName() + "]!", se);
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.ERROR, 0, "ProcedureAccessService[" + getName() + "].executeProcedure", e);
			throw new EMPJDBCException("ProcedureAccessService[" + getName() + "].execute", e);
		} finally {
			try {
				if (call != null)
					call.close();
			} 
			catch (Exception ex) {
			}
			long endTime = System.currentTimeMillis() - beginTime;
			if (endTime > alarmResponseTime && this.alarmOpened) {
				StringBuffer buf = new StringBuffer("Warning!!! procedure execute time=" + endTime + " over(>) " + alarmResponseTime);
				buf.append("\nprocedureId=");
				buf.append(aProcedureDefine.getProcedureName());
				try {
					EMPJMXManager.sendLogNotification(this, JDBCMBean.JDBC_MBEAN_ID, "WARNING", buf.toString());
				} 
				catch (Exception ee) {
				}
			}
			synchronized (syncObj) {
				this.accessInfo.endAccess(endTime);
			}
		}
	}

	public void setOpClass(String opClass) {
		this.opClass = opClass;
	}

	private int getIndexOf(List outParams, String dataName) {
		if (outParams.size() > 0 && (dataName == null || dataName.trim().length() == 0))
			return ((SQLParameter) outParams.get(1)).paramIdx;
		for (int i = 0; i < outParams.size(); i++) {
			SQLParameter param = (SQLParameter) outParams.get(i);
			if (param.dataName.equals(dataName))
				return param.paramIdx;
		}
		return -1;
	}

	public boolean isAlarmOpened() {
		return alarmOpened;
	}

	public void setAlarmOpened(boolean alarmOpened) {
		this.alarmOpened = alarmOpened;
	}

	public static long getAlarmResponseTime() {
		return alarmResponseTime;
	}

	public static void setAlarmResponseTime(long notifyInterval) {
		alarmResponseTime = notifyInterval;
	}

	public AccessInfo getAccessInfo() {
		return accessInfo;
	}
}
