package core.schedule.job.biz;

import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.flow.EMPFlow;
import com.ecc.emp.log.EMPLog;
import common.util.SFConst;

import core.schedule.job.AbstractScheduleAction;
/**
 * ����Action
 * @author ����
 *
 */
public class ScheduleTaskAction extends AbstractScheduleAction {

	@Override
	protected void doHandle(Context context,String jobName) throws Exception {
    	//begin------------ִ��.do
    	String[] jobs = jobName.split(":");
    	String flowId = jobs[0];
    	String opId = jobs[1];
    	EMPFlow tranFlow = null;
    	
    	EMPLog.log(EMPConstance.EMP_SCHEDULE, EMPLog.INFO, 0, "Begin to execute the scheduled flow: " + flowId + "...");
		EMPFlowComponentFactory factory =(EMPFlowComponentFactory) EMPFlowComponentFactory.getComponentFactory(SFConst.SYS_SYSNAME);
		tranFlow = factory.getEMPFlow(flowId);			
		context = (Context)tranFlow.getContext().clone();
		/*
		 * ִ��˽�н���BIZ
		 */
		tranFlow.execute(context, opId);			
	}

}
