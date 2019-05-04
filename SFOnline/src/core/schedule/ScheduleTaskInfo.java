/*
 * 创建日期 2010-4-22
 *
 * TODO 
 * 
 */
package core.schedule;

import org.quartz.JobDetail;
import org.quartz.Trigger;



/**
   * 调度任务信息
   * Serial NO: FINWARE_V3.5_TFS_2013120003 代码重构
   * Date 2013-6-13 
   * @author hh
   * @version 1.0
   */
public class ScheduleTaskInfo {
    
	public static final String SCHEDULE_GOON_FLAG = "__SCHEDULE_GO_FLAG";
	public static final String SCHEDULE_GOON = "__GO";
    
	public static final String SCHEDULE_TYPE_Auto= "1" ;//"Auto"
	public static final String SCHEDULE_TYPE_Manual = "2";//"Manual"
	public static final String SCHEDULE_TYPE_Forbidden= "3";//"Forbidden"
	
	public static final String JOB_TASKNAME = "taskName";
	public static final String JOB_ERRCOUNT = "errCount";
	public static final String JOB_MAXERRCOUNT = "maxErrCount";
	
	public static final String GROUP = "GROUP";		//tigger 和 jobdetail 共用组名
	
	private String name;
	private String schTaskType;
	private String schTaskEnDesc;
	private String schTaskDesc;
	
	private String jobDetailName;
	private String grpName;
	private JobDetail jobDetail;
	private Trigger trigger;
	private Class job;
	

	/**
	 * 获得字段值信息，可供调试时使用
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(",name=").append(name)
			.append(",schTaskType=").append(schTaskType)
			.append(",schTaskEnDesc=").append(schTaskEnDesc)
			.append(",schTaskDesc=").append(schTaskDesc)
			.append(",jobDetailName=").append(jobDetailName)
			.append(",grpName=").append(grpName)
			.append(",jobDetail=").append(jobDetail)
			.append(",trigger=").append(trigger)
			.append(",job=").append(job);
		
		return sb.toString();
	}
	
    public String getGrpName() {
        return grpName;
    }
    public void setGrpName(String grpName) {
        this.grpName = grpName;
    }
    public Class getJob() {
        return job;
    }
    public void setJob(Class job) {
        this.job = job;
    }
    public JobDetail getJobDetail() {
        return jobDetail;
    }
    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }
    public String getJobDetailName() {
        return jobDetailName;
    }
    public void setJobDetailName(String jobDetailName) {
        this.jobDetailName = jobDetailName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSchTaskDesc() {
        return schTaskDesc;
    }
    public void setSchTaskDesc(String schTaskDesc) {
        this.schTaskDesc = schTaskDesc;
    }
    public String getSchTaskEnDesc() {
        return schTaskEnDesc;
    }
    public void setSchTaskEnDesc(String schTaskEnDesc) {
        this.schTaskEnDesc = schTaskEnDesc;
    }
    public String getSchTaskType() {
        return schTaskType;
    }
    public void setSchTaskType(String paramType) {
        this.schTaskType = paramType;
    }
    public Trigger getTrigger() {
        return trigger;
    }
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
