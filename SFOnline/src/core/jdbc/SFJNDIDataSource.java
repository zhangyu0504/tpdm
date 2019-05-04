package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.ConnectionHolder;
import com.ecc.emp.jdbc.JNDIDataSource;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.transaction.TransactionSynchronizer;

/**
 * 基于JNDI的数据源访问实现。
 * <p>
 * 它通过应用服务器申请连接，因此需要在应用服务器中预先设定好数据库资源。
 * 
 * @version 2.1
 * @since 1.0 2006-12-1
 * @lastmodified 2008-7-2
 */
public class SFJNDIDataSource extends JNDIDataSource {

	public SFJNDIDataSource() {
		super();
	}

	/**
	 * 从数据源获得连接。
	 * 
	 * @return 数据库连接
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection() throws SQLException {
		//check if there are  inused connection exist
		ConnectionHolder holder = TransactionSynchronizer.getConnectionResource(this);
		if (holder == null)//no inused connection 
		{
			try {
				Connection origConn = super.getConnection();
				Connection connection = new JNDIConnection(this, origConn);;
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Apply new " + (TransactionSynchronizer.isHaveTransaction() == true?"transaction ":"") + "connection:" + origConn + " from data source:" + this.getId() + "...");
				if (TransactionSynchronizer.isHaveTransaction()) //there are transaction exits, so reserve the connection for other request
				{
					holder = new DBConnectionHolder(connection);
					TransactionSynchronizer.bindConnectionResource(this, holder);
					if (connection.getAutoCommit()) //在事务管理下,AutoCommit 应设为false
					{
						connection.setAutoCommit(false);
						holder.setNeedRecoverAutoCommit(true); //用于释放连接时，自动恢复连接原有的属性
					}
				}
				else {
					connection.setAutoCommit(true);
				}
				return connection;
			} catch (SQLException e) {
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Failed to get connection from :" + this.getId(), e);
				throw e;
			}
		} else
			return holder.getConnection();
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
		//check if there are  inused connection exist
		ConnectionHolder holder = TransactionSynchronizer.getConnectionResource(this);
		if (holder == null)//no inused connection 
		{
			try {
				Connection origConn = super.getConnection(userName, password);
				Connection connection = new JNDIConnection(this, origConn);
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Apply new " + (TransactionSynchronizer.isHaveTransaction() == true?"transaction ":"") + "connection:" + origConn + " from data source:" + this.getId() + "...");
				if (TransactionSynchronizer.isHaveTransaction()) //there are transaction exits, so reserve the connection for other request
				{
					holder = new DBConnectionHolder(connection);
					TransactionSynchronizer.bindConnectionResource(this, holder);
					if (connection.getAutoCommit()) //在事务管理下,AutoCommit 应设为false
					{
						connection.setAutoCommit(false);
						holder.setNeedRecoverAutoCommit(true); //用于释放连接时，自动恢复连接原有的属性
					}
				}
				else{
					connection.setAutoCommit(true);
				}
				return connection;
			} catch (SQLException e) {
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Failed to get connection from data source:" + this.getId(), e);
				throw e;
			}
		} else
			return holder.getConnection();
	}
}
