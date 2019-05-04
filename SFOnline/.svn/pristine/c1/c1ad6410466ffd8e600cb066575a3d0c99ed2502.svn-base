package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.JNDIDataSource;
import com.ecc.emp.log.EMPLog;

/**
 * 基于JNDI的数据源访问实现。
 * <p>
 * 它通过应用服务器申请连接，因此需要在应用服务器中预先设定好数据库资源。
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
	 * 从数据源获得连接。
	 * 
	 * @return 数据库连接
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection() throws SQLException {
		try {
			Connection origConn = super.getConnection();
			Connection connection = new NoTransJNDIConnection(this, origConn);;
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Apply new no transaction connection:" + origConn + " from data source:" + this.getId() + "...");
			if (!connection.getAutoCommit()) //设AutoCommit为true
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
	 * 以指定用户名密码从数据源获得连接。
	 * 
	 * @param userName 用户名
	 * @param password 密码
	 * @return 数据库连接
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection(String userName, String password) throws SQLException {
		try {
			Connection origConn = super.getConnection(userName, password);
			Connection connection = new NoTransJNDIConnection(this, origConn);
			EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Apply new no transaction connection:" + origConn + " from data source:" + this.getId() + "...");
			if (!connection.getAutoCommit()) //设AutoCommit为true
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
