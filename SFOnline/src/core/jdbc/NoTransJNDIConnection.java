package core.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.EMPDataSource;
import com.ecc.emp.jdbc.EMPPooledDataSource;
import com.ecc.emp.log.EMPLog;

/**
 * EMP平台对数据库连接的封装类。
 * 
 * @version 2.1
 * @since 1.0 2006-12-1
 * @lastmodified 2008-7-2
 * @see EMPPooledDataSource
 */
public class NoTransJNDIConnection implements Connection {

	/**
	 * 数据库连接
	 */
	private Connection connection;

	/**
	 * EMPPooledDataSource数据源服务
	 */
	private DataSource dataSource;

	public NoTransJNDIConnection(DataSource dataSource, Connection connection) {
		this.connection = connection;
		this.dataSource = dataSource;
	}

	/**
	 *  清除为此 Connection 对象报告的所有警告。
	 * 
	 * @throws SQLException
	 */
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();

	}

	/**
	 * 立即释放此 Connection 对象的数据库和 JDBC 资源，而不是等待它们被自动释放。
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (connection == null)
			return;
		EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Do release no transaction connection:" + this + " from data source: " + ((EMPDataSource)dataSource).getId());
		//no transaction, just close the connection, in JNDI mode will release the connection
		connection.close();
		connection = null;
	}

	/**
	 * 使自从上一次提交/回滚以来进行的所有更改成为持久更改，并释放此 Connection 对象当前保存的所有数据库锁定。
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		connection.commit();

	}

	/**
	 * 创建一个 Statement 对象来将 SQL 语句发送到数据库。
	 */
	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	/**
	 * 创建一个 Statement 对象，该对象将生成具有给定类型和并发性的 ResultSet 对象。
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	/**
	 * 创建一个 Statement 对象，该对象将生成具有给定类型、并发性和可保存性的 ResultSet 对象。
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * 获得此 Connection 对象的当前自动提交模式。
	 */
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/**
	 * 获得此 Connection 对象的当前目录名称。
	 */
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	/**
	 * 获得使用此 Connection 对象创建的 ResultSet 对象的当前可保存性。
	 */
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	/**
	 * 获取 DatabaseMetaData 对象，该对象包含关于 Connection 对象连接到的数据库的元数据。
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/**
	 * 获得此 Connection 对象的当前事务隔离级别。
	 */
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/**
	 * 获得将给定的 TypeMap 对象安装为此 Connection 对象的类型映射表。
	 */
	public Map getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/**
	 * 获得此 Connection 对象上的调用报告的第一个警告。
	 */
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/**
	 * 判断此 Connection 对象是否已经被关闭。
	 */
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	/**
	 * 判断此 Connection 对象是否处于只读模式。
	 */
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/**
	 * 将给定的 SQL 语句转换成系统本机 SQL 语法。
	 */
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	/**
	 * 创建一个 CallableStatement 对象来调用数据库存储过程。
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	/**
	 * 创建一个 CallableStatement 对象，该对象将生成具有给定类型和并发性的 ResultSet 对象。
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * 创建一个 CallableStatement 对象，该对象将生成具有给定类型和并发性的 ResultSet 对象。
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	/**
	 * 创建一个 PreparedStatement 对象来将参数化的 SQL 语句发送到数据库。
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	/**
	 * 创建一个默认 PreparedStatement 对象，该对象能检索自动生成的键。
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	/**
	 * 创建一个能够返回由给定数组指定的自动生成键的默认 PreparedStatement 对象。
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	/**
	 * 创建一个 PreparedStatement 对象，该对象将生成具有给定类型和并发性的 ResultSet 对象。
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	/**
	 * 创建一个 PreparedStatement 对象，该对象将生成具有给定类型、并发性和可保存性的 ResultSet 对象。
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}
	
	/**
	 * 创建一个能够返回由给定数组指定的自动生成键的默认 PreparedStatement 对象。
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * 从当前事务中移除给定 Savepoint 对象。
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	/**
	 * 回滚事务。取消在当前事务中进行的所有更改，并释放此 Connection 对象当前保存的所有数据库锁定。
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/**
	 * 取消设置给定 Savepoint 对象之后进行的所有更改。
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	/**
	 * 将此连接的自动提交模式设置为给定状态。
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * 设置给定目录名称，以便选择要在其中进行工作的此 Connection 对象数据库的子空间。
	 */
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	/**
	 * 将使用此 Connection 对象创建的 ResultSet 对象的可保存性 (holdability) 更改为给定可保存性。
	 */
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	/**
	 * 将此连接设置为只读模式，作为驱动程序启用数据库优化的提示。
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/**
	 * 在当前事务中创建一个未命名的保存点 (savepoint)，并返回表示它的新 Savepoint 对象。
	 */
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	/**
	 * 在当前事务中创建一个具有给定名称的保存点，并返回表示它的新 Savepoint 对象。
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	/**
	 * 试图将此 Connection 对象的事务隔离级别更改为给定的级别。
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/**
	 * 安装给定的 TypeMap 对象作为此 Connection 对象的特定类型映射。
	 */
	public void setTypeMap(Map map) throws SQLException {
		connection.setTypeMap(map);
	}


	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return connection.isWrapperFor(arg0);
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return connection.unwrap(arg0);
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return connection.createArrayOf(arg0, arg1);
	}

	@Override
	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return connection.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return connection.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return connection.createSQLXML();
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return connection.createStruct(arg0, arg1);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return connection.getClientInfo();
	}

	@Override
	public String getClientInfo(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return connection.getClientInfo(arg0);
	}

	@Override
	public boolean isValid(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return connection.isValid(arg0);
	}

	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		connection.setClientInfo(arg0);
	}

	@Override
	public void setClientInfo(String arg0, String arg1)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		connection.setClientInfo(arg0, arg1);
	}
}
