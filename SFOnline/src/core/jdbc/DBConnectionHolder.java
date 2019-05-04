package core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.jdbc.ConnectionHolder;
import com.ecc.emp.log.EMPLog;

/**
 * 维持一个数据库连接的封装类。用于事务处理。
 * 
 * @version 2.1
 * @since 1.0 2006-12-5
 * @lastmodified 2008-7-2
 */
public class DBConnectionHolder extends ConnectionHolder{

	public DBConnectionHolder() {
		super();
	}

	public DBConnectionHolder(Connection connection) {
		super(connection);
	}


	/**
	 * 释放数据库连接。
	 */
	public void releaseConnection() {
		Connection conn = this.getConnection();
		super.releaseConnection();
		if (SFEMPConnection.class.isAssignableFrom(conn.getClass())){
			SFEMPConnection FBSEMPConn = (SFEMPConnection)conn;
			try {
				FBSEMPConn.releaseConnection();
			} catch (SQLException e) {
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.ERROR, 0, "Do release the connection from data source exception", e);
			}
		}
		if (JNDIConnection.class.isAssignableFrom(conn.getClass())){
			JNDIConnection FBSJNDIConn = (JNDIConnection)conn;
			try {
				FBSJNDIConn.releaseConnection();
			} catch (SQLException e) {
				EMPLog.log(EMPConstance.EMP_TRANSACTION, EMPLog.ERROR, 0, "Do release the connection from data source exception", e);
			}
		}
	}
}
