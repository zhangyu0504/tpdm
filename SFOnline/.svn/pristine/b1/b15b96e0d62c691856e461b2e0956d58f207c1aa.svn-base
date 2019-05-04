package module.dao;

import java.sql.Connection;
import java.util.Map;

import module.bean.TransAlertLog;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;
/**
 * 系统dao
 * @author 汪华
 *
 */
public class SystemDao extends DaoBase {
	
	public Map<String, Object> qryDual(Context context, Connection connection)
			throws SFException {
		Map<String, Object> result = null;
		try {
			String sql = "SELECT 1 AS DUAL_VALUE FROM DUAL";
			result = super.qryMap(context, connection, sql);
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return result;
	}
	
	/**
	 * 新增系统预警日志
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int saveTransAlertLog(Context context,Connection connection,TransAlertLog alertLog)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("INSERT INTO TRDTRANSALERTLOG(TXCODE,LOGID,TYPE,TXDATE,TXTIME,VALUE1,VALUE2,MEMO,MACCODE)VALUES(?,?,?,?,?, ?,?,?,?)");
		Object[] params={alertLog.getTxCode(),alertLog.getLogId(),alertLog.getType(),alertLog.getTxDate(),alertLog.getTxTime(),
				alertLog.getValue1(),alertLog.getValue2(),alertLog.getMome(),alertLog.getMacCode()};
		return super.save(context, connection, sql.toString(),params);
	}


}
