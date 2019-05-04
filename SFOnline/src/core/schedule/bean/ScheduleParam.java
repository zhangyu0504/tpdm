package core.schedule.bean;

/**
 * 批量参数对象
 * @author 汪华
 *
 */
public class ScheduleParam {
	private String scheduleid;
	private String enable;
	private String taskname;
	private String jobid;
	private String cron;
	private String crondesc;
	private String jobtype;
	private String ip;
	private String app;
	private String statustype;
	private int maxcount;
	private int currindex;
	public String getScheduleid() {
		return scheduleid;
	}
	public void setScheduleid(String scheduleid) {
		this.scheduleid = scheduleid;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public String getTaskname() {
		return taskname;
	}
	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
	public String getJobid() {
		return jobid;
	}
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public String getCrondesc() {
		return crondesc;
	}
	public void setCrondesc(String crondesc) {
		this.crondesc = crondesc;
	}
	public String getJobtype() {
		return jobtype;
	}
	public void setJobtype(String jobtype) {
		this.jobtype = jobtype;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public String getStatustype() {
		return statustype;
	}
	public void setStatustype(String statustype) {
		this.statustype = statustype;
	}
	public int getMaxcount() {
		return maxcount;
	}
	public void setMaxcount(int maxcount) {
		this.maxcount = maxcount;
	}
	public int getCurrindex() {
		return currindex;
	}
	public void setCurrindex(int currindex) {
		this.currindex = currindex;
	}
	
}
