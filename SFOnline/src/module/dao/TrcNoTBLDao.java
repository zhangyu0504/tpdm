package module.dao;

import java.sql.Connection;

import module.bean.TrcNoTBL;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class TrcNoTBLDao extends DaoBase{

	public TrcNoTBL qryTrcNoTBL(Context context,Connection connection)throws SFException{
		TrcNoTBL trcNoTBL = null;
		try {
			   String sql = "SELECT TRC_DATE AS trcDate FROM TRCNOTBL WHERE MOD_TYPE = '000000'";
			
			   trcNoTBL = super.qry(context, connection, sql, TrcNoTBL.class);
		} catch (SFException e){
			throw new SFException(e);
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return trcNoTBL;
	}
	
	public void saveTrcNoTBL(Context context,Connection connection,String trcDate)throws SFException{
		try {
			   String sql = "UPDATE TRCNOTBL SET TRC_DATE = ? WHERE MOD_TYPE = '000000'";
			
			   super.save(context, connection, sql, trcDate);
		} catch (SFException e){
			throw new SFException(e);
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
	}
}