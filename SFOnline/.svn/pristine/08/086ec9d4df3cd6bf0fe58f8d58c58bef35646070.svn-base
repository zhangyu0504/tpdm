package module.dao;

import java.sql.Connection;

import module.bean.ServStatus;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class ServStatusDao extends DaoBase {

	/**
	 * 查询平台服务状态
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public ServStatus qryServStatus(Context context,Connection connection,String txDate) throws SFException{
		ServStatus servStatus = null;
		try{
		  
			String sql = "SELECT TXDATE,TXTIME,ICSTIME FROM TRDSERVSTATUS WHERE TXDATE = ?";
			servStatus = super.qry(context, connection, sql,ServStatus.class, txDate);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(servStatus!=null){
				servStatus.resetChangedFlag();
			}
		}
	 return servStatus;
  }
}
