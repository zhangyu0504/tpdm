package core.schedule.job.biz;

import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;

import com.ecc.emp.core.Context;

import core.schedule.job.AbstractJobAction;
import core.schedule.job.AbstractScheduleAction;

/**
 * 调度实现类
 * 有态任务，即调度任务之间按队列执行，前一任务没结束之前后一任务不能开始
 * 注意：其中一个调度任务僵死后影响其他任务
 */
//调度假死后续动作不执行问题方案,将StatefulJob替换为Job
public class ScheduleStatefulJobTaskAction extends AbstractJobAction implements StatefulJob {
	
	@Override
	protected void doHandle(JobExecutionContext jobContext,Context context,String scheduleId)throws Exception {
		AbstractScheduleAction action=new ScheduleTaskAction();
		/*
		 * 执行具体任务
		 */
		action.doJob(jobContext,context,scheduleId);
	}
}