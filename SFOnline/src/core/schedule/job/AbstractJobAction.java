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
 * ����Job action����
 * @author ����
 *
 */
public abstract class AbstractJobAction {
	private SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
	private ScheduleDao scheduleDao=new ScheduleDao();//�������dao

	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		String scheduleId = jobContext.getTrigger().getName();
		Connection connection=null;
		Context context = null, rootContext = null;
		SFCompLocalMachineIpAction macAction = new SFCompLocalMachineIpAction();//������Action
		try {
			/*
			 * �������������ģ������������ǴӸ������Ŀ�¡����
			 */
			EMPFlowComponentFactory factory =(EMPFlowComponentFactory) EMPFlowComponentFactory.getComponentFactory(SFConst.SYS_SYSNAME);
			rootContext = factory.getContextNamed(factory.getRootContextName());
			context = (Context) rootContext.clone();
			connection=DBHandler.getConnection(context);//��ȡ���ݿ�����
			DBHandler.beginTransaction(context, connection);//��������
			/*
			 * ���ȼƻ��ܿ���
			 */
			String sToken=scheduleDao.qryScheduleToken(context, connection,SFConst.SYS_SYSNAME);
			if(!"1".equals(sToken)){
				return ;
			}
			
			/*
			 * ��ѯ��������
			 */
			ScheduleParam scheduleInfo=scheduleDao.qryScheduleParam(context, connection, scheduleId, SFConst.SYS_SYSNAME);
			if(scheduleInfo==null){
				return ;
			}			
			/*
			 * IP����
			 */
			String result=null;
			macAction.setIpAddress(scheduleInfo.getIp());
			result = macAction.execute(context);
			if("0".equals(result)){
				/*
				 * ����¼�����
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
				 * ����ִ��ǰ��¼�������1��
				 */
				scheduleDao.updateForIndex(context, connection, scheduleId, SFConst.SYS_SYSNAME, 1);
				DBHandler.commitTransaction(context, connection);
				DBHandler.releaseConnection(context, connection);//�������������ݿ����Ӽ��ü��ͷ�
				/*
				 * ִ��job����
				 */
				try{
					this.doHandle(jobContext,context,scheduleId);			
				}catch (Exception e){
					logger.error("execute schedule error:" + e.getMessage(), e);
				}
				/*
				 * ����ִ�к��¼�������1��
				 */
				connection=DBHandler.getConnection(context);//��ȡ���ݿ�����
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
					DBHandler.releaseConnection(context, connection);//�������������ݿ����Ӽ��ü��ͷ�
				} catch (SFException e) {
					logger.error("execute schedule error:" + e.getMessage(), e);
				}
				context.terminate();
			}
		}
	}
	
	/**
	 * ˽��job����
	 * @param jobContext
	 * @throws Exception
	 */
	protected abstract void doHandle(JobExecutionContext jobContext,Context context,String scheduleId) throws Exception;
}