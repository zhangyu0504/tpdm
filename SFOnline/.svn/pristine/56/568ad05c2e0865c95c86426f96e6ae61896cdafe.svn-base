package core.schedule.job.java;

import com.ecc.emp.core.Context;

import core.schedule.ScheduleActionInterface;
import core.schedule.job.AbstractScheduleAction;
/**
 * 任务Action
 * @author 汪华
 *
 */
public class ScheduleClassAction extends AbstractScheduleAction {

	@SuppressWarnings("rawtypes")
	@Override
	protected void doHandle(Context context,String jobName) throws Exception {
		/*
		 * 执行私有逻辑：先检查私有交易是否达到执行条件，满足检查后执行私有逻辑
		 */
		Class clazz = Class.forName(jobName);
		ScheduleActionInterface taskImpl = (ScheduleActionInterface) clazz.newInstance();
		boolean chkFlag = taskImpl.init(context);//前置检查
		if(!chkFlag){
			return ;
		}
		taskImpl.execute(context);//执行私有逻辑		
	}

}
