package core.schedule.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

import core.schedule.bean.ScheduleLog;
import core.schedule.bean.ScheduleParam;
/**
 * 批量调度Dao
 * @author 汪华
 *
 */
public class ScheduleDao extends DaoBase{
	/**
	 * 根据应用号查询当前应用的所有批量
	 * @param context
	 * @param connection
	 * @param sysApp
	 * @return
	 * @throws SFException
	 */
	public List<ScheduleParam> qryScheduleParamList(Context context,Connection connection,String sysApp)throws SFException {
		List<ScheduleParam> list = null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT SCHEDULEID,ENABLE,TASKNAME,JOBID,CRON,CRONDESC,JOBTYPE,IP,");
			sql.append("STATUSTYPE,MAXCOUNT,CURRINDEX,APP FROM TRDSCHEDULEPARAM WHERE APP=?" );
			Object[] param = {sysApp};
			list = super.qryForOList( context, connection, sql.toString(), param, ScheduleParam.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return list;
	}
	
	/**
	 * 查询批量参数配置
	 * @param context
	 * @param connection
	 * @param scheduleId
	 * @param sysApp
	 * @return
	 * @throws SFException
	 */
	public ScheduleParam qryScheduleParam(Context context,Connection connection,String scheduleId,String sysApp)throws SFException {
		ScheduleParam scheduleParam= null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT SCHEDULEID,ENABLE,TASKNAME,JOBID,CRON,CRONDESC,JOBTYPE,IP,");
			sql.append("STATUSTYPE,MAXCOUNT,CURRINDEX,APP FROM TRDSCHEDULEPARAM WHERE SCHEDULEID=? AND APP=?" );
			scheduleParam = super.qry(context,connection, sql.toString(),ScheduleParam.class,scheduleId,sysApp);
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return scheduleParam;
	}
	
	/**
	 * 记录活动的批量数量
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateForIndex(Context context,Connection connection,String scheduleId,String sysApp,int index)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDSCHEDULEPARAM SET CURRINDEX=NVL(CURRINDEX,0)+? WHERE SCHEDULEID=? AND APP=?");
		Object[] params={index,scheduleId,sysApp};
		return super.save(context, connection, sql.toString(),params);
	}
	

	/**
	 * 新增批量日志
	 * @param context
	 * @param connection
	 * @param scheduleLog
	 * @return
	 * @throws SFException
	 */
	public int saveScheduleLog(Context context,Connection connection,ScheduleLog scheduleLog)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("INSERT INTO TRDSCHEDULELOG (ID,SCHEDULEID,STARTTIME,ENDTIME,MEMO,REGISTERDATE,ERRCODE,ERRMSG,STARTDATE,ENDDATE,IP)");
		sql.append(" VALUES(?,?,?,?,?,SYSDATE,?,?,?,?,?)");
		Object[] params={scheduleLog.getId(),scheduleLog.getScheduleid(),scheduleLog.getStarttime(),scheduleLog.getEndtime(),
				scheduleLog.getMemo(),scheduleLog.getErrcode(),scheduleLog.getErrmsg(),
				scheduleLog.getStartdate(),scheduleLog.getEnddate(),scheduleLog.getIp()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * 查询调度计划系统级参数配置
	 * @param context
	 * @param connection
	 * @param scheduleId
	 * @param sysApp
	 * @return
	 * @throws SFException
	 */
	public String qryScheduleToken(Context context,Connection connection,String key)throws SFException {
		String strParam=null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT VALUE FROM TRDPARAM WHERE TYPE='SF_SCHEDULE' AND ID=?" );
			Map<String,Object> mapResult=super.qryMap(context, connection, sql.toString(),key);
			if(mapResult!=null){
				strParam=(String) mapResult.get("VALUE");
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return strParam;
	}
	
}
