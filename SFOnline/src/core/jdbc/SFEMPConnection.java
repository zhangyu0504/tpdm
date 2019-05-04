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

import com.ecc.emp.jdbc.ConnectionHolder;
import com.ecc.emp.jdbc.EMPPooledDataSource;
import com.ecc.emp.transaction.TransactionSynchronizer;

/**
 * EMPƽ̨�����ݿ����ӵķ�װ�ࡣ
 * 
 * @version 2.1
 * @since 1.0 2006-12-1
 * @lastmodified 2008-7-2
 * @see EMPPooledDataSource
 */
public class SFEMPConnection implements Connection {

	/**
	 * ���ݿ�����
	 */
	private Connection connection;

	/**
	 * EMPPooledDataSource����Դ����
	 */
	private EMPPooledDataSource dataSource;

	public SFEMPConnection(EMPPooledDataSource dataSource, Connection connection) {
		this.connection = connection;
		this.dataSource = dataSource;
	}

	/**
	 *  ���Ϊ�� Connection ���󱨸�����о��档
	 * 
	 * @throws SQLException
	 */
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();

	}

	/**
	 * �����ͷŴ� Connection ��������ݿ�� JDBC ��Դ�������ǵȴ����Ǳ��Զ��ͷš�
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (connection == null)
			return;
		//		connection.close();
		//check if there are transaction exist
		ConnectionHolder holder = TransactionSynchronizer.getConnectionResource(this.dataSource);
		if (holder != null)//there are transaction exist, do nothing, let TransactionManager to do the correct things 
		{
			//EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Do release the transaction connection:" + connection + " from data source: " + this.dataSource.getId());
			//		   holder.releaseConnection();
			return;
		}
		dataSource.releaseConnection(connection);
		connection = null;
	}

	/**
	 * �����ͷŴ� Connection ��������ݿ�� JDBC ��Դ�������ǵȴ����Ǳ��Զ��ͷš�
	 * 
	 * @throws SQLException
	 */
	public void releaseConnection() throws SQLException {
		if (connection == null)
			return;
		dataSource.releaseConnection(connection);
		connection = null;
	}

	/**
	 * ʹ�Դ���һ���ύ/�ع��������е����и��ĳ�Ϊ�־ø��ģ����ͷŴ� Connection ����ǰ������������ݿ�������
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		connection.commit();

	}

	/**
	 * ����һ�� Statement �������� SQL ��䷢�͵����ݿ⡣
	 */
	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	/**
	 * ����һ�� Statement ���󣬸ö������ɾ��и������ͺͲ����Ե� ResultSet ����
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	/**
	 * ����һ�� Statement ���󣬸ö������ɾ��и������͡������ԺͿɱ����Ե� ResultSet ����
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * ��ô� Connection ����ĵ�ǰ�Զ��ύģʽ��
	 */
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/**
	 * ��ô� Connection ����ĵ�ǰĿ¼���ơ�
	 */
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	/**
	 * ���ʹ�ô� Connection ���󴴽��� ResultSet ����ĵ�ǰ�ɱ����ԡ�
	 */
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	/**
	 * ��ȡ DatabaseMetaData ���󣬸ö���������� Connection �������ӵ������ݿ��Ԫ���ݡ�
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/**
	 * ��ô� Connection ����ĵ�ǰ������뼶��
	 */
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/**
	 * ��ý������� TypeMap ����װΪ�� Connection ���������ӳ�����
	 */
	public Map getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/**
	 * ��ô� Connection �����ϵĵ��ñ���ĵ�һ�����档
	 */
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/**
	 * �жϴ� Connection �����Ƿ��Ѿ����رա�
	 */
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	/**
	 * �жϴ� Connection �����Ƿ���ֻ��ģʽ��
	 */
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/**
	 * �������� SQL ���ת����ϵͳ���� SQL �﷨��
	 */
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	/**
	 * ����һ�� CallableStatement �������������ݿ�洢���̡�
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	/**
	 * ����һ�� CallableStatement ���󣬸ö������ɾ��и������ͺͲ����Ե� ResultSet ����
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * ����һ�� CallableStatement ���󣬸ö������ɾ��и������ͺͲ����Ե� ResultSet ����
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	/**
	 * ����һ�� PreparedStatement ���������������� SQL ��䷢�͵����ݿ⡣
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	/**
	 * ����һ��Ĭ�� PreparedStatement ���󣬸ö����ܼ����Զ����ɵļ���
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	/**
	 * ����һ���ܹ������ɸ�������ָ�����Զ����ɼ���Ĭ�� PreparedStatement ����
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	/**
	 * ����һ�� PreparedStatement ���󣬸ö������ɾ��и������ͺͲ����Ե� ResultSet ����
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	/**
	 * ����һ�� PreparedStatement ���󣬸ö������ɾ��и������͡������ԺͿɱ����Ե� ResultSet ����
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}
	
	/**
	 * ����һ���ܹ������ɸ�������ָ�����Զ����ɼ���Ĭ�� PreparedStatement ����
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * �ӵ�ǰ�������Ƴ����� Savepoint ����
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	/**
	 * �ع�����ȡ���ڵ�ǰ�����н��е����и��ģ����ͷŴ� Connection ����ǰ������������ݿ�������
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/**
	 * ȡ�����ø��� Savepoint ����֮����е����и��ġ�
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	/**
	 * �������ӵ��Զ��ύģʽ����Ϊ����״̬��
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * ���ø���Ŀ¼���ƣ��Ա�ѡ��Ҫ�����н��й����Ĵ� Connection �������ݿ���ӿռ䡣
	 */
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	/**
	 * ��ʹ�ô� Connection ���󴴽��� ResultSet ����Ŀɱ����� (holdability) ����Ϊ�����ɱ����ԡ�
	 */
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	/**
	 * ������������Ϊֻ��ģʽ����Ϊ���������������ݿ��Ż�����ʾ��
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/**
	 * �ڵ�ǰ�����д���һ��δ�����ı���� (savepoint)�������ر�ʾ������ Savepoint ����
	 */
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	/**
	 * �ڵ�ǰ�����д���һ�����и������Ƶı���㣬�����ر�ʾ������ Savepoint ����
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	/**
	 * ��ͼ���� Connection �����������뼶�����Ϊ�����ļ���
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/**
	 * ��װ������ TypeMap ������Ϊ�� Connection ������ض�����ӳ�䡣
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