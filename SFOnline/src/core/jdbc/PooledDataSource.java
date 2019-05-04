package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.ConnectionHolder;
import com.ecc.emp.jdbc.EMPPooledDataSource;
import com.ecc.emp.jmx.support.EMPJMXManager;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.transaction.TransactionSynchronizer;

/**
 * EMPʵ�ֵ����ݿ����ӻ���ع������
 * <p>
 * ����ʾ����<pre>
 * &lt;EMPPooledDataSource dataSourceName="empPooled" driverName="oracle.jdbc.driver.OracleDriver"
 * 	dbURL="jdbc:oracle:thin:@219.239.212.43:1521:ctpDemo" userName="ctpuser" password="ctpuser"
 * 	validationSQL="validation connection SQL statement." checkInterval="120000" size="30"/></pre>
 * 
 * @version 2.1
 * @since 1.0 2006-12-1
 * @lastmodified 2008-7-2
 * @see com.ecc.util.database.ConnectionManager
 */
public class PooledDataSource extends EMPPooledDataSource {
	/**
	 * ���ӹ�����ʵ��
	 */
	private com.ecc.util.database.ConnectionManager fbsConnectionMgr;

	public PooledDataSource() {
		super();
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
			if (fbsConnectionMgr == null)
				initialize();

			//check if there are  inused connection exist
			ConnectionHolder holder = TransactionSynchronizer.getConnectionResource(this);
			if (holder == null)//no inused connection 
			{
				try {
					Connection origConn = fbsConnectionMgr.getConnection(userName, password);
					Connection connection = new SFEMPConnection(this, origConn);
					EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Apply new " + (TransactionSynchronizer.isHaveTransaction() == true?"transaction ":"") + "connection:" + origConn + " from data source:" + this.getId() + "...");
					if (TransactionSynchronizer.isHaveTransaction()) //there are transaction exits, so reserve the connection for other request
					{
						holder = new DBConnectionHolder(connection);
						TransactionSynchronizer.bindConnectionResource(this, holder);
						if (connection.getAutoCommit()) //�����������,AutoCommit Ӧ��Ϊfalse
						{
							connection.setAutoCommit(false);
							holder.setNeedRecoverAutoCommit(true); //�����ͷ�����ʱ���Զ��ָ�����ԭ�е�����
						}
					}
					else {
						connection.setAutoCommit(true);
					}
					return connection;
				} catch (SQLException e) {
					EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Failed to get connection from data source:" + this.getId(), e);
					throw e;
				}
			} else
				return holder.getConnection();

		} catch (SQLException e) {
			EMPJMXManager.sendNotification(this, this.getName(), "error", "Failed to get connection from data source:" + this.getId());
			throw e;
		}
	}

	/**
	 * ������Դ������ӡ�
	 * 
	 * @return ���ݿ�����
	 * @throws java.sql.SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (fbsConnectionMgr == null)
			initialize();

		//check if there are  inused connection exist
		ConnectionHolder holder = TransactionSynchronizer.getConnectionResource(this);
		if (holder == null)//no inused connection 
		{
			try {
				Connection origConn = fbsConnectionMgr.getConnection();
				Connection connection = new SFEMPConnection(this, origConn);
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Apply new " + (TransactionSynchronizer.isHaveTransaction() == true?"transaction ":"") + "connection:" + origConn + " from data source:" + this.getId() + "...");
				if (TransactionSynchronizer.isHaveTransaction()) //there are transaction exits, so reserve the connection for other request
				{
					holder = new DBConnectionHolder(connection);
					TransactionSynchronizer.bindConnectionResource(this, holder);
					if (connection.getAutoCommit()) //�����������,AutoCommit Ӧ��Ϊfalse
					{
						connection.setAutoCommit(false);
						holder.setNeedRecoverAutoCommit(true); //�����ͷ�����ʱ���Զ��ָ�����ԭ�е�����
					}
				}
				else {
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

	/**
	 * ��ʼ������Դ��������
	 */
	private void initialize() {
		com.ecc.util.database.ConnectionManager cm = new com.ecc.util.database.ConnectionManager(this.getDriverName(), this.getDbURL(), this.getUserName(), this.getPassword());

		cm.setIdleTimeOut(this.getIdleTimeOut());
		cm.setConnectionTimeOut(this.getConnectionTimeOut());
		cm.setMaxDBCon(this.getSize());
		cm.setCheckInterval(this.getCheckInterval());
		cm.setValidationSQL(this.getValidationSql());
		fbsConnectionMgr = cm;
	}

	/**
	 * �ͷ�ָ�����ӡ�
	 * 
	 * @param connection ���ͷŵ����ݿ�����
	 */
	public void releaseConnection(Connection connection) {
		EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.DEBUG, 0, "Do release the connection:" + connection + " from data source: " + this.getId());
		//no transaction, just close the connection, in JNDI mode will release the connection
		this.fbsConnectionMgr.releaseConnection(connection);
	}

}
