package core.schedule;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;

import core.log.SFLogger;
import core.schedule.bean.ScheduleParam;
import core.schedule.dao.ScheduleDao;


/**
   * �������񹤳��� ��ȡ����������� 
   * Serial NO: FINWARE_V3.5_TFS_2013120003 �����ع�
   * Date 2013-6-13 
   * @author hh
   * @version 1.0
   */
public class ScheduleTaskFactory {
	private SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
    private static ScheduleTaskFactory schedTask;

	//���ȼ�����λ
	public static final String SCH_UNIT_ONETIME = "onetime";	
	public static final String SCH_UNIT_DAY = "day";	
	public static final String SCH_UNIT_WEEK = "week";
	public static final String SCH_UNIT_MONTH = "month";
	public static final String SCH_UNIT_CRON = "cron";
	
	public static final String SECOND = "s";	
	public static final String MINUTE = "m";	
	private ScheduleDao scheduleDao=new ScheduleDao();//�������dao
    
    private ScheduleTaskFactory() {
    }

    synchronized public static ScheduleTaskFactory getInstance() {
        if (schedTask == null)
            schedTask = new ScheduleTaskFactory();
        return schedTask;
    }
    /**
     * ��ȡ��������
     * @param scheduleId
     * @param cxtData
     * @return
     * @throws SFException
     */
    public ScheduleTaskInfo getScheduleTask(String scheduleId, ContextData cxtData) throws SFException {
        long begin = System.currentTimeMillis();
        ScheduleTaskInfo scheduleTaskInfo = null;
        try {
            if (scheduleId == null || scheduleId.length() <= 0)
                throw new SFException("schedTaskNameΪ�գ�����");

            if (scheduleTaskInfo == null) {
                scheduleTaskInfo = this.buildScheduleTaskInfo(scheduleId,cxtData.EMPContext);
            }
        } catch (Exception e) {
            logger.error("getScheduleTask fail:" + e.getMessage(), e);
            throw new SFException(e);
        }
        long end = System.currentTimeMillis();
        logger.debug("get getScheduleTask;" + scheduleId + " cost " + (end - begin));
        return scheduleTaskInfo;
    }
    /**
     * ������������
     * @param scheduleId
     * @param context
     * @return
     * @throws SFException
     */
	private ScheduleTaskInfo buildScheduleTaskInfo(String scheduleId,Context context) throws SFException{
    	Connection connection=null;
	    try{
	    	connection=DBHandler.getConnection(context);//���ݿ�����
	    	ScheduleParam scheduleParam=scheduleDao.qryScheduleParam(context, connection, scheduleId, SFConst.SYS_SYSNAME);	
	    	ScheduleTaskInfo taskInfo = new ScheduleTaskInfo();
	    	if(scheduleParam==null){
	    		return taskInfo;
	    	}
	    	taskInfo.setSchTaskType(scheduleParam.getEnable());
		    taskInfo.setName(scheduleId);
		    taskInfo.setSchTaskDesc(scheduleParam.getTaskname());
		    
		    // jobType 1 ������class 2������biz
		    String jobType = scheduleParam.getJobtype();
		    String statusType = scheduleParam.getStatustype();
		    String jobClass = null;
		   
		    
		    /**
		     *  statusType  
		     *  1  ������������֮�䰴����ִ�У�ǰһ����û����֮ǰ��һ�����ܿ�ʼ    
		     *  2  ���� ��������֮�以��Ӱ��
		     */
		    if("1".equals(jobType)){
		    	if("1".equals(statusType)){
		    		jobClass = "core.schedule.job.java.ScheduleStatefulJobAction";
		    	}else{
		    		jobClass = "core.schedule.job.java.ScheduleJobAction";
		    	}
		    }else if("2".equals(jobType)){
		    	if("1".equals(statusType)){
		    		jobClass = "core.schedule.job.biz.ScheduleStatefulJobTaskAction";
		    	}else{
		    		jobClass = "core.schedule.job.biz.ScheduleJobTaskAction";
		    	}
		    }else{
		    	jobClass = "core.schedule.job.java.ScheduleStatefulJobAction";
		    }
		    
		    Class cJobClass = Class.forName(jobClass);
		    JobDetail jobDetail = new JobDetail( scheduleId,ScheduleTaskInfo.GROUP ,cJobClass);
		    JobDataMap jobDataMap = new JobDataMap();
		    jobDataMap.put("taskName", scheduleParam.getJobid());
		    //����Ĭ����Զ��ֹͣ
		    jobDataMap.put("errCount", "0");
		    jobDataMap.put("maxErrCount", "-1");
		    
		    //begin createJobDetail
			jobDetail.setJobDataMap(jobDataMap);
		    taskInfo.setJobDetail( jobDetail );
		    //end createJobDetail

		    //begin create trigger
		    Trigger trigger = null;
		    String triggerType = SCH_UNIT_CRON;//(String)tmpContext.getDataValue("TRIGGER_TYPE");
			if( SCH_UNIT_ONETIME.equals(triggerType) ){	
				Map<String,String> triggerE = new HashMap<String,String>();
				triggerE.put("startTime", "");
				triggerE.put("startDate", "");
				Date startTime = getStartTime(triggerE);
				trigger = new SimpleTrigger(scheduleId,ScheduleTaskInfo.GROUP);
				trigger.setStartTime(startTime);
			}else{
				String cronExpression =  scheduleParam.getCron();// assembleExpress(triggerE);
				logger.debug(cronExpression);
				trigger = new CronTrigger(scheduleId, ScheduleTaskInfo.GROUP,cronExpression);
			}
		    taskInfo.setTrigger( trigger );
		    //end create trigger
		    
		    return taskInfo;
	    }catch (Exception e) {
	        throw new SFException(e);
        }finally{
			DBHandler.releaseConnection(context, connection);
		}
    }
    
	
	/**
	 * ��ô������Ŀ�ʼʱ��
	 */
	private Date getStartTime(Map<String,String> triggerNode){
		Date startDateTime = new Date();
		try {
			String sStartTime =  triggerNode.get("startTime");
			String startDate = triggerNode.get("startDate");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(null==startDate || "".equals(startDate.trim())){
				String st = sdf.format(startDateTime);
				startDateTime = DateFormat.getDateInstance().parse(st + " " + sStartTime);
			}else{
				startDateTime = DateFormat.getDateInstance().parse(startDate+ " " + sStartTime);
			}
		} catch (Exception e) {
			logger.error("getStartTime error:"+e.getMessage(), e);
		}
		logger.debug("startDateTime-->"+startDateTime);
		return startDateTime;
	}
	
	/**
	 * CRON����ʽ
	 * @param triggerNode
	 * @return
	 * @throws SFException
	 */
	private String assembleExpress(Element triggerNode) throws SFException {
		String express = "";
		try{
			String type = triggerNode.attributeValue("type");
			
			if(SCH_UNIT_DAY.equals(type)){
				express = getDayExpress(triggerNode);
			}else if(SCH_UNIT_WEEK.equals(type)){
				express = getWeekExpress(triggerNode);
			}else if(SCH_UNIT_MONTH.equals(type)){
				express = getMonthExpress(triggerNode);
			}else if(SCH_UNIT_CRON.equals(type)){
			    express = triggerNode.elementText("cron");
			}else 
			    throw new SFException("Scheduler type format error:" + type);
		}catch(Exception e){
			logger.error("assembleExpress fail" + e.getMessage(), e);
			throw new SFException(e);
		}
		logger.debug("assembleExpress---->"+express);
		return express;
	}

	/**
	 * ��Ϊ��λ��CRON����ʽ
	 */
	private String getDayExpress(Element triggerNode) throws SFException {
		String express = "";
		try{
			String startTime = triggerNode.elementText("startTime");
			String endTime = triggerNode.elementText("endTime");
			String interval = triggerNode.elementText("interval");
			String iu = triggerNode.elementText("intervalUnit");
			
			String hour = startTime + "-" + endTime;
			if( SECOND.equals(iu.trim() ) ){
				express = "0/" + interval + " 0-59 "+ hour +" * * ?";
			}else if(MINUTE.equals(iu.trim())){
				express = "0 0/" + interval+ " " + hour+ " * * ?";
			}else{
				throw new SFException("ʱ�䵥λ����");
			}

		}catch(Exception e){
			logger.error("getDayExpress fail:"+e.getMessage(), e);
			throw new SFException(e);
		}
		return express;
	}	
	
	
	/**
	 * ��Ϊ��λ��CRON����ʽ
	 * @param triggerNode
	 * @return
	 * @throws SFException
	 */
	private String getWeekExpress(Element triggerNode) throws SFException {
		String express = "";
		try{
			String startTime = triggerNode.elementText("startTime");
			String endTime = triggerNode.elementText("endTime");
			String interval = triggerNode.elementText("interval");
			String iu = triggerNode.elementText("intervalUnit");
			String days = triggerNode.elementText("days");
			
			String hour = startTime + "-" + endTime;
			
			if(SECOND.equals(iu.trim())){
				express = "0/" + interval + " 0-59 " + hour + " * * " + days;
			}else if(MINUTE.equals(iu.trim())){
				express = "0 0/" + interval + " " + hour + " * * " + days;
			}else{
				throw new SFException("ʱ�䵥λ����");
			}
			

		}catch(Exception e){
			logger.error("getMonthExpress fail:"+e.getMessage(), e);
			throw new SFException(e);
		}
		
		return express;
	}

	/**
	 * ��Ϊ��λ��CRON����ʽ
	 * @param triggerNode
	 * @return
	 * @throws SFException
	 */
	private String getMonthExpress(Element triggerNode) throws SFException {
		String express = "";
		try{
			Element eFreq = triggerNode.element("freq");
			String freqUnit = eFreq.attributeValue("unit");
			String interval = triggerNode.elementText("interval");
			String iu = triggerNode.elementText("intervalUnit");
			
			String cronSecond = "0";
			String cronMinute = "0-59";
			String cronHour = "*";
			String cronDayOfMonth = "?";
			String cronMonth = "*";
			String cronDayOfWeek = "?";

			//���
			if(SECOND.equals(iu.trim())){
				cronSecond = "0/" + interval;
			}else if(MINUTE.equals(iu.trim())){
				cronMinute = "0/" + interval;
			}else{
				throw new SFException("ʱ�䵥λ����");
			}
			
			//�ܻ���
			if(SCH_UNIT_WEEK.equals(freqUnit)){
				String month = eFreq.elementText("month");
				String weekno = eFreq.elementText("weekno");
				String weekday = eFreq.elementText("weekday");
				cronMonth = month;
				cronDayOfWeek = weekday + "#" + weekno;
			}else{
				String month = eFreq.elementText("month");
				String day = eFreq.elementText("day");
				cronMonth = month;
				cronDayOfMonth = day;
			}
			
			express = cronSecond + " " + cronMinute + " " + cronHour + " " 
					+ cronDayOfMonth + " " + cronMonth + " " + cronDayOfWeek;
			
		}catch(Exception e){
			logger.error("getMonthExpress fail:"+e.getMessage(), e);
			throw new SFException(e);
		}
		
		return express;
	}
}
