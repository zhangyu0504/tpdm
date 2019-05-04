package core.schedule.job.java;

import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;

import com.ecc.emp.core.Context;

import core.schedule.job.AbstractJobAction;
import core.schedule.job.AbstractScheduleAction;

public class ScheduleStatefulJobAction extends AbstractJobAction implements StatefulJob {

	
	@Override
	protected void doHandle(JobExecutionContext jobContext,Context context,String scheduleId)throws Exception {
		AbstractScheduleAction action=new ScheduleClassAction();
		/*
		 * 执行具体任务
		 */
		action.doJob(jobContext,context,scheduleId);
	}
}
