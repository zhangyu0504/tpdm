package core.jdbc.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.jdbc.EMPJDBCException;

/**
*
* <b>功能描述：</b><br>
* 存储过程访问执行的辅助类接口，负责SQL语句的定义，以及返回结果集的处理。<br>
*
*   @创建时间 2002-07-08
*   @author  ZhongMingChang
*   @modifier GaoLin 2006-10-25
*   
*/

public interface ProcedureOp {
	
	/**
	 * 定义访问存储过程的SQL语句
	 * @param aInValues TODO
	 * @param Connection aConnection                    数据库连接
	 * @param String aProcName                          存储过程名称
	 * @param List aInValues                            输入数据值列表
	 * @param List aInParams                            输入数据项列表
	 * @param List aResults                             返回结果数据项列表
	 * @param int aResultSetCnt                         返回结果集列表长度
	 * @return CallableStatement                        访问存储过程的SQL语句定义
	 * @throws EMPJDBCException
	 */

	public CallableStatement callProc(Connection aConnection, String aProcName,
			Map aInValues, List aInParams, List aOutParams, List aResultSet)
			throws EMPJDBCException;
	
	/**
	 * 处理存储过程返回的结果集
	 * 
	 * @param Context aContext                          交易定义的Context
	 * @param KeyedCollection kColl                     批量执行的输入数据集合
	 * @param List aResultSets                          返回结果集列表
	 * @param CallableStatement aCall                   SQL语句声明
	 * @param int outIdx                                结果集索引
	 * @return void
	 * @throws EMPJDBCException
	 */

	public void processResultSets(Context aContext, KeyedCollection kInColl, List aResultSets,
			CallableStatement aCall) throws EMPJDBCException;
}
