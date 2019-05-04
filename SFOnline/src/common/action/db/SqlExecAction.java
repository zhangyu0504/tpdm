package common.action.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.ConnectionManager;
import com.ecc.emp.jdbc.EMPJDBCException;
import com.ecc.emp.jdbc.RecordNotFoundException;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.timerecorder.EMPTimerRecorder;
import com.ecc.emp.transaction.EMPTransactionDef;
import common.services.SQLDefine;
import common.services.SqlExecService;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * 数据库SQL语句执行操作步骤。
 * <p>
 * 通过调用SFSqlExecService，实现在一个事务中执行多条SQL语句的操作。
 * <p>
 * 配置示例：<pre>
 * &lt;action id="SQLExecAction" implClass="com.ecc.emp.jdbc.sql.SQLExecAction"
 * 	transactionType="TRX_REQUIRE_NEW" dataSource="JDBCDataSource"
 * 	sqlService ="SFSqlExecService" refSQL="SFSqlDefine1;SFSqlDefine2;SFSqlDefine3" /&gt;</pre>
 * 参数说明：<br>
 * transactionType：事务类型，TRX_REQUIRE_NEW，独有事务；TRX_REQUIRED，全局事务。<br>
 * dataSource：数据源服务定义名称。<br>
 * sqlService：SQL语句执行服务定义名称。<br>
 * refSQL：SQL语句定义名称，连续执行多个SQL时中间用分号隔开。
 * <p>
 * 返回状态：<br>
 * 0，正常；2，记录未找到；
 * 
 * @since 1.1 2003-11-10
 * @version 2.1
 * @lastmodified 2010-08-08
 * @emp:name 执行SQL语句
 * @emp:iconName /images/db_obj.gif
 * @emp:document 执行SQL语句的操作步骤，通过执行SQL语句服务（SFSqlExecService），实现在一个事务中执行多条SQL语句的操作。
 * @emp:states 0=正常;2=记录未找到;
 */
public class SqlExecAction extends EMPAction {

	/** 
	 * 数据源服务名称 
	 */
	private String dataSourceName = null;

	/**
	 * SQL语句执行服务名称
	 */
	private String sqlServiceName = null;

	/**
	 * 事务类型，默认为全局事务
	 */
	private int trxType = EMPTransactionDef.TRX_REQUIRED;

	/**
	 * SQL语句定义服务名称，连续执行多个SQL时中间用分号隔开 
	 */
	private String refSQL = null;

	/**
	 * 拆开后的SQL语句定义服务名称
	 */
	private List sqlDefineRef = null;
	
	private static Long MAXTIME=0L;

	/**
	 * 实例初始化方法。
	 * <p> 
	 * 将多个用分号相连的SQL语句定义服务名称拆成List。
	 */
	public void initialize() {
		this.sqlDefineRef = new ArrayList();

		if (refSQL != null && !"".equals(refSQL)) {

			java.util.StringTokenizer sqls = new StringTokenizer(this.refSQL,
					";");
			while (sqls.hasMoreTokens()) {
				String sqlDefineName = (String) sqls.nextElement();
				this.sqlDefineRef.add(sqlDefineName);
			}
		}
	}

	/**
	 * 业务逻辑步骤执行入口。
	 * <p>
	 * 调用FBSSqlExecService服务，执行已定义的SQL语句。
	 * 
	 * @param context 交易上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；2，记录未找到
	 */
	public String execute(Context context) throws EMPException {
		long beginTime=System.currentTimeMillis();
		DataSource dataSource = null;
		SqlExecService sqlService = null;
		Connection connection = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named \"" + dataSourceName
					+ "\"is not found in JDBCSQLExecAction:" + this.toString());

		if (sqlServiceName != null && sqlServiceName.length() != 0)
			sqlService = (SqlExecService) context.getService(sqlServiceName);
		if (sqlService == null)
			throw new EMPException(
					"JDBCSQLExecService not set for JDBCSQLAction:"
							+ this.toString());

		try {
			connection = ConnectionManager.getConnection(dataSource);
			for (int i = 0; i < sqlDefineRef.size(); i++) {
				String sqlId = (String) sqlDefineRef.get(i);

				SQLDefine aDefine = (SQLDefine) context.getService(sqlId);
				if (aDefine == null) {
					EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.ERROR, 0,
							"JDBCSQLDefine [" + sqlId + "] not valid!");
					throw new EMPException("JDBCSQLDefine [" + sqlId
							+ "] not valid!");
				}

				sqlService.executeSQLDef(aDefine, context, connection);

			}
			
//			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The SQLService [" + refSQL + "],times= "+timeCost, null);
			
			return "0";
		} catch (RecordNotFoundException e) {
			return "2";
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			long etime = System.currentTimeMillis();
			long timeCost=etime-beginTime;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The SQLService [" + refSQL + "],times= "+timeCost, null);
			setRunTime(context,beginTime,refSQL); //chenky add 统计时间
			
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-beginTime);
		}
	}

	/**
	 * 业务逻辑步骤执行入口。
	 * <p>
	 * 调用SqlExecService服务，执行SFSqlDefine。
	 * 
	 * @param context 交易上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；2，记录未找到
	 */
	public static String execute(Context context, SQLDefine sqlDefine) throws EMPException {
		long beginTime=System.currentTimeMillis();
		DataSource dataSource = null;
		SqlExecService sqlService = null;
		Connection connection = null;

		if (sqlDefine == null) {
			throw new EMPException("parameter sqlDefine is null!");
		}
		
		dataSource = (DataSource) context.getService((String) context.getDataValue(SFConst.SERVICE_DATASOURCE));
		sqlService = (SqlExecService) context.getService((String) context.getDataValue(SFConst.SERVICE_SQL));

		try {
			connection = ConnectionManager.getConnection(dataSource);
			sqlService.executeSQLDef(sqlDefine, context, connection);
			return "0";
		} catch (RecordNotFoundException e) {
			return "2";
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			long timeCost=System.currentTimeMillis()-beginTime;
			setRunTime(context,beginTime,sqlDefine.getName()); //chenky add 统计时间 
			
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-beginTime);
		}
	}
	
	/**
	 * 重载execute函数用于FBS扩展代码。
	 * <p>
	 * 调用SqlExecService服务，执行已定义的SQL语句。
	 * 
	 * @param context 交易上下文
	 * @param tmpContext 交易临时上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；2，记录未找到
	 */
	public String execute(Context context, Context tmpContext) throws EMPException {
		SFLogger logger=SFLogger.getLogger(context);
		long beginTime=System.currentTimeMillis();
		DataSource dataSource = null;
		SqlExecService sqlService = null;
		Connection connection = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named \"" + dataSourceName
					+ "\"is not found in JDBCSQLExecAction:" + this.toString());

		if (sqlServiceName != null && sqlServiceName.length() != 0)
			sqlService = (SqlExecService) context.getService(sqlServiceName);
		if (sqlService == null)
			throw new EMPException(
					"JDBCSQLExecService not set for JDBCSQLAction:"
							+ this.toString());

		try {
			connection = ConnectionManager.getConnection(dataSource);
			for (int i = 0; i < sqlDefineRef.size(); i++) {
				String sqlId = (String) sqlDefineRef.get(i);

				SQLDefine aDefine = (SQLDefine) context.getService(sqlId);
				if (aDefine == null) {
					EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.ERROR, 0,
							"JDBCSQLDefine [" + sqlId + "] not valid!");
					throw new EMPException("JDBCSQLDefine [" + sqlId
							+ "] not valid!");
				}

				sqlService.executeSQLDef( aDefine, tmpContext, connection);
				
				long end = System.currentTimeMillis();
				long timeCost=end-beginTime;
			}

			return "0";
		} catch (RecordNotFoundException e) {
			return "2";
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			long timeCost=System.currentTimeMillis()-beginTime;
			logger.info("The SQLService [" + refSQL + "],times= "+timeCost);
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The SQLService [" + refSQL + "],times= "+timeCost, null);
			setRunTime(context,beginTime,refSQL); //chenky add 统计时间 

			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-beginTime);
		}
	}

	/**
	 * 重载execute函数用于FBS扩展代码。
	 * <p>
	 * 调用SqlExecService服务，执行已定义的SQL语句。
	 * 
	 * @param context 交易上下文
	 * @param tmpContext 交易临时上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；2，记录未找到
	 */
	public static String execute(Context context, Context tmpContext, SQLDefine sqlDefine) throws EMPException {
		SFLogger logger=SFLogger.getLogger(context);
		long beginTime=System.currentTimeMillis();
		DataSource dataSource = null;
		SqlExecService sqlService = null;
		Connection connection = null;
		
		if (sqlDefine == null) {
			EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.ERROR, 0, "parameter SFSqlDefine is null!");
			throw new EMPException("parameter SFSqlDefine is null!");
		}
		
		dataSource = (DataSource) context.getService((String) context.getDataValue(SFConst.SERVICE_DATASOURCE));
		sqlService = (SqlExecService) context.getService((String) context.getDataValue(SFConst.SERVICE_SQL));

		try {
			connection = ConnectionManager.getConnection(dataSource);
			sqlService.executeSQLDef(sqlDefine, tmpContext, connection);

			long timeCost=System.currentTimeMillis()-beginTime;
			logger.info("The SQLService [" + sqlDefine.getName() + "],times= "+timeCost);
			return "0";
		} catch (RecordNotFoundException e) {
			return "2";
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			long timeCost=System.currentTimeMillis()-beginTime;
			logger.info("The SQLService [" + sqlDefine.getName() + "],times= "+timeCost);
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The SQLService [" + sqlDefine.getName() + "],times= "+timeCost, null);
			setRunTime(context,beginTime,sqlDefine.getName()); //chenky add 统计时间
			
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-beginTime);
		}
	}


	/**
	 * 获得事务类型定义对象。
	 * 
	 * @return 事务类型定义对象
	 */
	public EMPTransactionDef getTransactionDef() {
		return new EMPTransactionDef(trxType);
	}

	/**
	 * 设置事务类型。
	 * 
	 * @param value 事务类型
	 * @emp:isAttribute true
	 * @emp:name 事务类型
	 * @emp:desc 选择该步骤是全局事务还是独有事务
	 * @emp:mustSet true
	 * @emp:valueList TRX_REQUIRED=应用全局事务;TRX_REQUIRE_NEW=创建独有事务;
	 * @emp:defaultValue TRX_REQUIRED 
	 */
	public void setTransactionType(String value) {
		if ("TRX_REQUIRED".equals(value))
			this.trxType = EMPTransactionDef.TRX_REQUIRED;
		else if ("TRX_REQUIRE_NEW".equals(value))
			this.trxType = EMPTransactionDef.TRX_REQUIRE_NEW;
	}

	/**
	 * 设置数据源服务定义名称。
	 * 
	 * @param dataSourceName 数据源服务定义名称
	 * @emp:isAttribute true
	 * @emp:name 数据源服务定义名称
	 * @emp:desc 所使用的数据源的定义名称
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setDataSource(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/**
	 * 设置SQL语句执行服务定义名称。
	 * 
	 * @param sqlServiceName SQL语句执行服务定义名称
	 * @emp:isAttribute true
	 * @emp:name SQL语句执行服务定义名称
	 * @emp:desc 所使用的SQL语句执行服务定义名称
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setSqlService(String sqlServiceName) {
		this.sqlServiceName = sqlServiceName;
	}

	/**
	 * 设置SQL语句定义名称，可以顺序执行多个，用分号隔开。
	 * 
	 * @param refSQL SQL语句定义名称
	 * @emp:isAttribute true
	 * @emp:name SQL语句定义名称
	 * @emp:desc 需要执行的SQL语句定义服务名称，可以顺序执行多个，用分号隔开
	 * @emp:mustSet true
	 */
	public void setRefSQL(String refSQL) {
		this.refSQL = refSQL;
	}
	
	/**
	 * 转化日期格式
	 * @param time
	 * @return
	 */
	private static String getTime(long time){
		Timestamp stamp = new Timestamp(time);
		return stamp.toString();
	}
	
	private static void setRunTime(Context context,Long beginTime,String sqlName) throws EMPException{
		long etime = System.currentTimeMillis();
		long timeCost=etime-beginTime;
		if(context.containsKey("SQLTIME")){    //chenky add 统计时间
			String str = (String) context.getDataValue("SQLTIME");
			str += "The SQLService [" + sqlName + "],begin_Time:"+ getTime(beginTime)+",end_Time:"+ getTime(etime)+",times= "+timeCost + "\r\n";
			if(timeCost > MAXTIME){
				context.setDataValue("SQLTIME", str);
			}
		}
	}


}
