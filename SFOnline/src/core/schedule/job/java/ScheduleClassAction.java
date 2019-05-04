package core.schedule.job.java;

import com.ecc.emp.core.Context;

import core.schedule.ScheduleActionInterface;
import core.schedule.job.AbstractScheduleAction;
/**
 * ����Action
 * @author ����
 *
 */
public class ScheduleClassAction extends AbstractScheduleAction {

	@SuppressWarnings("rawtypes")
	@Override
	protected void doHandle(Context context,String jobName) throws Exception {
		/*
		 * ִ��˽���߼����ȼ��˽�н����Ƿ�ﵽִ���������������ִ��˽���߼�
		 */
		Class clazz = Class.forName(jobName);
		ScheduleActionInterface taskImpl = (ScheduleActionInterface) clazz.newInstance();
		boolean chkFlag = taskImpl.init(context);//ǰ�ü��
		if(!chkFlag){
			return ;
		}
		taskImpl.execute(context);//ִ��˽���߼�		
	}

}
