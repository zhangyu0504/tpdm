package core.schedule.job;

import java.sql.Connection;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;

import core.log.SFLogger;
import core.schedule.SFCompLocalMachineIpAction;
import core.schedule.bean.ScheduleParam;
import core.schedule.dao.ScheduleDao;

/**
 * 抽象Job action基类
 * @author 汪华
 *
 */
public abstract class AbstractJobAction {
	private SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
	private ScheduleDao scheduleDao=new ScheduleDao();//任务调度dao

	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		String scheduleId = jobContext.getTrigger().getName();
		Connection connection=null;
		Context context = null, rootContext = null;
		SFCompLocalMachineIpAction macAction = new SFCompLocalMachineIpAction();//服务器Action
		try {
			/*
			 * 构建交易上下文（交易上下文是从根上下文克隆来）
			 */
			EMPFlowComponentFactory factory =(EMPFlowComponentFactory) EMPFlowComponentFactory.getComponentFactory(SFConst.SYS_SYSNAME);
			rootContext = factory.getContextNamed(factory.getRootContextName());
			context = (Context) rootContext.clone();
			connection=DBHandler.getConnection(context);//获取数据库连接
			DBHandler.beginTransaction(context, connection);//开启事务
			/*
			 * 调度计划总开关
			 */
			String sToken=scheduleDao.qryScheduleToken(context, connection,SFConst.SYS_SYSNAME);
			if(!"1".equals(sToken)){
				return ;
			}
			
			/*
			 * 查询批量任务
			 */
			ScheduleParam scheduleInfo=scheduleDao.qryScheduleParam(context, connection, scheduleId, SFConst.SYS_SYSNAME);
			if(scheduleInfo==null){
				return ;
			}			
			/*
			 * IP检验
			 */
			String result=null;
			macAction.setIpAddress(scheduleInfo.getIp());
			result = macAction.execute(context);
			if("0".equals(result)){
				/*
				 * 最大记录数检查
				 */
				int maxCount=scheduleInfo.getMaxcount();
				int currIndex=scheduleInfo.getCurrindex();
				if(maxCount<=0){
					maxCount=1;
				}
				if(maxCount<=currIndex){
					return;
				}
				/*
				 * 任务执行前记录活动数（加1）
				 */
				scheduleDao.updateForIndex(context, connection, scheduleId, SFConst.SYS_SYSNAME, 1);
				DBHandler.commitTransaction(context, connection);
				DBHandler.releaseConnection(context, connection);//批量调度中数据库连接即用即释放
				/*
				 * 执行job动作
				 */
				try{
					this.doHandle(jobContext,context,scheduleId);			
				}catch (Exception e){
					logger.error("execute schedule error:" + e.getMessage(), e);
				}
				/*
				 * 任务执行后记录活动数（减1）
				 */
				connection=DBHandler.getConnection(context);//获取数据库连接
				scheduleDao.updateForIndex(context, connection, scheduleId, SFConst.SYS_SYSNAME, -1);
				DBHandler.commitTransaction(context, connection);
			}
		} catch (Exception e) {
			if(context!=null){
				SFLogger.error(context,"execute schedule error:" + e.getMessage(), e);
			}else{
				SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
				logger.error(context,"execute schedule error:" + e.getMessage(), e);
			}
		}finally {
			if(context != null){
				try {
					DBHandler.releaseConnection(context, connection);//批量调度中数据库连接即用即释放
				} catch (SFException e) {
					logger.error("execute schedule error:" + e.getMessage(), e);
				}
				context.terminate();
			}
		}
	}
	
	/**
	 * 私有job动作
	 * @param jobContext
	 * @throws Exception
	 */
	protected abstract void doHandle(JobExecutionContext jobContext,Context context,String scheduleId) throws Exception;
}
