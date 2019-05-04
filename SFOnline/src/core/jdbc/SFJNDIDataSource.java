package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.ConnectionHolder;
import com.ecc.emp.jdbc.JNDIDataSource;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.transaction.TransactionSynchronizer;

/**
 * ����JNDI������Դ����ʵ�֡�
 * <p>
 * ��ͨ��Ӧ�÷������������ӣ������Ҫ��Ӧ�÷�������Ԥ���趨�����ݿ���Դ��
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
	 * ������Դ������ӡ�
	 * 
	 * @return ���ݿ�����
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
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.INFO, 0, "Failed to get connection from :" + this.getId(), e);
				throw e;
			}
		} else
			return holder.getConnection();
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
					if (connection.getAutoCommit()) //�����������,AutoCommit Ӧ��Ϊfalse
					{
						connection.setAutoCommit(false);
						holder.setNeedRecoverAutoCommit(true); //�����ͷ�����ʱ���Զ��ָ�����ԭ�е�����
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
