package module.dao;

import java.sql.Connection;

import module.bean.BankCodeInfo;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class BankCodeInfoDao extends DaoBase {

	public BankCodeInfo qryBankCodeInfo( Context context, Connection connection, String msgCode ) throws SFException {
		BankCodeInfo bankCodeInfo = null;
		try {
			String sql = "SELECT CHMSG AS msg  FROM TRDBANKCODEINFO WHERE CHMSGCODE= ?";
			bankCodeInfo = super.qry( context, connection, sql, BankCodeInfo.class, msgCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return bankCodeInfo;
	}

}
