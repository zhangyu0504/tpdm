package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.JNDIDataSource;
import com.ecc.emp.log.EMPLog;

/**
 * ����JNDI������Դ����ʵ�֡�
 * <p>
 * ��ͨ��Ӧ�÷������������ӣ������Ҫ��Ӧ�÷�������Ԥ���趨�����ݿ���Դ��
 * 
 * @version 2.1
 * @since 1.0 2006-12-1
 * @lastmodified 2008-7-2
 */
public class NoTransJNDIDataSource extends JNDIDataSource {

	public NoTransJNDIDataSource() {
		super();
	}

	/**
	 * ������Դ������ӡ�
	 * 
	 * @return ���ݿ�����
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection() throws SQLException {
		try {
			Connection origConn = super.getConnection();
			Connection connection = new NoTransJNDIConnection(this, origConn);;
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Apply new no transaction connection:" + origConn + " from data source:" + this.getId() + "...");
			if (!connection.getAutoCommit()) //��AutoCommitΪtrue
			{
				connection.setAutoCommit(true);
			}
			return connection;
		} catch (SQLException e) {
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Failed to get no transaction connection from :" + this.getId(), e);
			throw e;
		}
	}

	/**
	 * ��ָ���û������������Դ������ӡ�
	 * 
	 * @param userName �û���
	 * @param password ����
	 * @return ���ݿ�����
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection(String userName, String password) throws SQLException {
		try {
			Connection origConn = super.getConnection(userName, password);
			Connection connection = new NoTransJNDIConnection(this, origConn);
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Apply new no transaction connection:" + origConn + " from data source:" + this.getId() + "...");
			if (!connection.getAutoCommit()) //��AutoCommitΪtrue
			{
				connection.setAutoCommit(true);
			}
			return connection;
		} catch (SQLException e) {
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Failed to get no transaction connection from data source:" + this.getId(), e);
			throw e;
		}
	}
}
