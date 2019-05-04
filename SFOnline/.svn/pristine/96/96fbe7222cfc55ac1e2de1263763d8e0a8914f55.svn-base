package core.jdbc.procedure;

import java.sql.Connection;

import javax.sql.DataSource;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.ConnectionManager;
import com.ecc.emp.jdbc.EMPJDBCException;
import com.ecc.emp.jdbc.ProcedureFailedException;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.timerecorder.EMPTimerRecorder;
import com.ecc.emp.transaction.EMPTransaction;
import com.ecc.emp.transaction.EMPTransactionDef;

/**
 * 访问数据库执行存储过程的操作步骤。
 * <p>
 * 通过存储过程访问服务service (ProcedureAccessService)，调用并执行本地数据库的存储过程。
 * <p>
 * 配置示例：<pre>
 * &lt;action id="JDBCProcedureAction" implClass="com.ecc.emp.jdbc.procedure.JDBCProcedureAction"
 * 	transactionType="TRX_REQUIRE_NEW" iCollName="iCollName" isBatch="batch" throwException="false"
 * 	dataSource="JDBCDataSource" procedureService="ProcedureAccessService"
 * 	procedureDefine="JDBCProcedureDefine" label="labelName"/&gt;</pre>
 * 参数说明：<br>
 * transactionType：事务类型，TRX_REQUIRE_NEW，独有事务；TRX_REQUIRED，全局事务。<br>
 * iCollName：批量执行存储过程的输入数据集合定义。<br>
 * isBatch：是否批量执行存储过程。<br>
 * throwException：boolean型属性，决定当存储过程执行的返回值非0时，是否抛出异常：com.ecc.emp.jdbc.ProcedureFailedException。<br>
 * dataSource：数据源服务定义名称。<br>
 * procedureService：数据库存储过程访问服务定义名称。<br>
 * procedureDefine：数据库存储过程定义名称。
 * <p>
 * 返回状态：<br>
 * 0，正常；非0，异常（当throwException=false时）
 * 
 * @author lili
 * @author GaoLin 2006-10-30
 * @version 2.1
 * @since 1.0 2003-11-10
 * @lastmodified 2008-06-26
 * @emp:name 访问存储过程
 * @emp:iconName /images/db_obj.gif
 * @emp:document 访问存储过程的操作步骤，通过存储过程访问服务（ProcedureAccessService），调用并执行本地数据库的存储过程。
 */ 
public class JDBCProcedureAction extends EMPAction{
	
	/**
	 * 数据源服务定义名称
	 */
	private String dataSourceName=null;
	
	/**
	 * 数据库存储过程访问服务定义名称
	 */
	private String procedureServiceName=null;
	
	/**
	 * 数据库存储过程定义名称
	 */
	private String procedureDefineName=null;
	
	/**
	 * 返回值非0时是否抛出异常
	 */
	private boolean throwException=false;
	
    /**
     * 事务类型
     */
	private int trxType = EMPTransactionDef.TRX_REQUIRED;

	public JDBCProcedureAction() {
		super();
	}
	
	/**
	 * 业务逻辑步骤执行入口。
	 * <p>
	 * 调用ProcedureAccessService服务，执行访问存储过程的操作。
	 * 
	 * @param context 交易上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；非0， 异常
	 */
	public String execute(Context context) throws EMPException {
		long begin = System.currentTimeMillis();
		DataSource dataSource = null;
		JDBCProcedureDefine procedureDefine = null;
		ProcedureAccessService procedureService = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named ["+dataSourceName+"]is not found in FBSJDBCProcedureAction:"+this.toString());
		
		if (procedureDefineName != null && procedureDefineName.length() != 0)
			procedureDefine = (JDBCProcedureDefine) context.getService(procedureDefineName);
		if (procedureDefine == null)
			throw new EMPException("FBSJDBCProcedureDefine not set for FBSJDBCProcedureAction:" + this.toString());
		
		if (procedureServiceName != null && procedureServiceName.length() != 0)
			procedureService = (ProcedureAccessService) context.getService(procedureServiceName);
		if (procedureService == null)
			throw new EMPException("FBSProcedureAccessService not set for FBSJDBCProcedureAction:" + this.toString());
		   
		Connection connection = null;

		/*ShenDongJie delete 20100130 begin
		EMPTransaction transaction = this.getTransaction();
		/*ShenDongJie delete 20100130 end */

		try {

			connection = ConnectionManager.getConnection(dataSource);
			String retCode="-1";

			retCode = procedureService.executeProcedure(procedureDefine,context,connection);

			/*ShenDongJie delete 20100130 begin
			if (!"0".equals(retCode) && transaction != null) {
				transaction.setRollbackOnly(true);
			/*ShenDongJie delete 20100130 end */
			if (!"0".equals(retCode)) {
				if(throwException)
					throw new ProcedureFailedException("Procedure [" + procedureDefine.getProcedureName() + "] execute failed with retCode=" + retCode);
			}

			long timeCost=System.currentTimeMillis()-begin;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The ProcedureService [" + procedureDefine.getName() + "],times= "+timeCost, null);
			return retCode;

		} catch (ProcedureFailedException pe) {
			throw pe;	
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-begin);
		}
	}
	
	/**
	 * 重载execute函数用于FBS扩展代码。
	 * <p>
	 * 调用FBSProcedureAccessService服务，执行已定义的存储过程语句。
	 * 
	 * @param context 交易上下文
	 * @param tmpContext 交易临时上下文
	 * @throws EMPException
	 * @return 返回值 0，正常；2，记录未找到
	 */
	
	public String execute(Context context, Context tmpContext) throws EMPException {
		long begin = System.currentTimeMillis();
		DataSource dataSource = null;
		JDBCProcedureDefine procedureDefine = null;
		ProcedureAccessService procedureService = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named ["+dataSourceName+"]is not found in FBSJDBCProcedureAction:"+this.toString());
		
		if (procedureDefineName != null && procedureDefineName.length() != 0)
			procedureDefine = (JDBCProcedureDefine) context.getService(procedureDefineName);
		if (procedureDefine == null)
			throw new EMPException("FBSJDBCProcedureDefine not set for FBSJDBCProcedureAction:" + this.toString());
		
		if (procedureServiceName != null && procedureServiceName.length() != 0)
			procedureService = (ProcedureAccessService) context.getService(procedureServiceName);
		if (procedureService == null)
			throw new EMPException("FBSProcedureAccessService not set for FBSJDBCProcedureAction:" + this.toString());
		   
		Connection connection = null;

		/*ShenDongJie delete 20100130 begin
		EMPTransaction transaction = this.getTransaction();
		/*ShenDongJie delete 20100130 end */

		try {

			connection = ConnectionManager.getConnection(dataSource);
			String retCode="-1";

			retCode = procedureService.executeProcedure(procedureDefine,tmpContext,connection);

			/*ShenDongJie delete 20100130 begin
			if (!"0".equals(retCode) && transaction != null) {
				transaction.setRollbackOnly(true);
			/*ShenDongJie delete 20100130 end */
			if (!"0".equals(retCode)) {
				if(throwException)
					throw new ProcedureFailedException("Procedure [" + procedureDefine.getProcedureName() + "] execute failed with retCode=" + retCode);
			}
			long timeCost=System.currentTimeMillis()-begin;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The ProcedureService [" + procedureDefine.getName() + "],times= "+timeCost, null);
			return retCode;

		} catch (ProcedureFailedException pe) {
			throw pe;	
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-begin);
		}
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
	 * 设置数据库存储过程定义名称。
	 * 
	 * @param procedureDefineName 数据库存储过程定义名称
	 * @emp:isAttribute true
	 * @emp:name 数据库存储过程定义名称
	 * @emp:desc 需要执行的数据库存储过程定义名称
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setProcedureDefine(String procedureDefineName) {
		this.procedureDefineName = procedureDefineName;
	}

	/**
	 * 设置数据库存储过程访问服务定义名称。
	 * 
	 * @param procedureServiceName 数据库存储过程访问服务定义名称
	 * @emp:isAttribute true
	 * @emp:name 数据库存储过程访问服务定义名称
	 * @emp:desc 所使用的数据库存储过程访问服务定义名称
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setProcedureService(String procedureServiceName) {
		this.procedureServiceName = procedureServiceName;
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
	 * 获得事务类型定义对象。
	 * 
	 * @return 事务类型定义对象
	 */
	public EMPTransactionDef getTransactionDef() {
		return new EMPTransactionDef(trxType);
	}

	/**
	 * 设置当存储过程返回值非0时是否抛出异常。
	 * 
	 * @param throwException 当存储过程返回值非0时是否抛出异常
	 * @emp:isAttribute true
	 * @emp:name 是否抛出异常
	 * @emp:desc 当存储过程返回值非0时是否抛出异常
	 * @emp:defaultValue false
	 */
	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

}
