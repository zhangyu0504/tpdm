package core.schedule.job.biz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.ecc.emp.core.Context;

import core.schedule.job.AbstractJobAction;
import core.schedule.job.AbstractScheduleAction;

/**
 * 调度实现类
 * 无状态任务，即调度任务之间互不影响
 * 其中一个调度任务僵死后不影响其他任务
 */
//调度假死后续动作不执行问题方案,将StatefulJob替换为Job
public class ScheduleJobTaskAction extends AbstractJobAction implements Job {
	
	@Override
	protected void doHandle(JobExecutionContext jobContext,Context context,String scheduleId)throws Exception {
		AbstractScheduleAction action=new ScheduleTaskAction();
		/*
		 * 执行具体任务
		 */
		action.doJob(jobContext,context,scheduleId);
	}
}