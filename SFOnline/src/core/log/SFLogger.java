package core.log;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

/**
 * ������־����
 * @author ����
 *
 */
public class SFLogger {
	private Logger logger=null;
	private static final Map<String,SFLogger> instanceMap=new HashMap<String,SFLogger>();
	/**
	 * ��־���� DEBUG/ERROR/INFO
	 */
	private static final String DEBUG="0";//debug
	private static final String INFO="1";//info
	private static final String ERROR="2";//error
	

	/**
	 * �������ƻ�ȡ��־����
	 * @param name ��־����
	 * @param logLevel ��־����
	 * @return
	 */
	private SFLogger(String txCode,String macCode,Level logLevel) {
		// �����µ�Logger ����ѽ�����һ��Loggerʵ�����جF�е�
		logger = Logger.getLogger(txCode);
		// ���Appender���؄e�ǲ���ʹ���ִ�ʵ��ʱһ��Ҫ���ڻ�
		logger.removeAllAppenders();
		// ����Logger���e,��������ݿ⻺�������л�ȡ
		logger.setLevel(logLevel);
		// �����Ƿ�̳и�Logger:1��Ĭ��true�̳�rootݔ��;2������false�󽫲����root��
		logger.setAdditivity(false);
		
		/*
		 * �����µ�Appender
		 */
		SFDailyRollingFileAppender appender = new SFDailyRollingFileAppender();
		appender.setDatePattern("'.'yyyy-MM-dd");
		//FileAppender appender = new RollingFileAppender();
		//�����ʽ����
		PatternLayout layout = new PatternLayout();
		// log�������ʽ
		String conversionPattern ="[%d{yyMMdd HH:mm:ss,SSS}|%-5p%m%n";
		layout.setConversionPattern(conversionPattern);
		appender.setLayout(layout);
		// log���·��
		String logFilePath = BizUtil.getLogPath();
		//�����ļ�·��
		appender.setFile(logFilePath + "/" + txCode+"-"+macCode + ".log");
		//����ʱʱ��־������Ի��������־�������־��ʧ
		appender.setBufferedIO(false);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
		//������־�����С8K=8192;16K=16384
		appender.setBufferSize(8192);
		//���ñ����ļ���󻯣������ý���ÿ�����ֻ����һ�������ļ���
		appender.setMaxBackupIndex(-1);
		//������־���ļ�
		appender.setMaxFileSize("500MB");
		//������־Ϊ�첽
		// log��������
		appender.setEncoding(SFConst.SYS_GBK);
		// true:���Ѵ���log�ļ�����׷�� false:��log������ǰ��log
		appender.setAppend(true);
		// ���õ�ǰ����
		appender.activateOptions();
		logger.addAppender(appender);
		
//		//�첽��־
//		AsyncAppender asAppender =new AsyncAppender();
//		asAppender.setBufferSize(16384);
//		asAppender.addAppender(appender);
//		asAppender.activateOptions();
//		logger.addAppender(asAppender);

		/*
		 * ���ÿ���̨��־���
		 */
		ConsoleAppender consAppender=new ConsoleAppender();
		consAppender.setLayout(layout);
		consAppender.setTarget("System.out");
		// log��������
		consAppender.setEncoding(SFConst.SYS_GBK);
		// ���õ�ǰ����
		consAppender.activateOptions();
		// ���µ�Appender�ӵ�Logger��
		logger.addAppender(consAppender);
	}
	/**
	 * ��ȡ������־����
	 */
	public static synchronized Map<String,SFLogger> getInstanceMap(){
		return instanceMap;
	}
	
	private static synchronized SFLogger getInstance(String logName,String macCode){
		if(!instanceMap.containsKey(logName)){
			Level level=null;
			try {
				String logLevel =BizUtil.getLogLevel(logName);
				// ��ȡ��ǰҵ������
				if (DEBUG.equals(logLevel)) {
					level=Level.DEBUG;
				}else if (INFO.equals(logLevel)) {
					level=Level.INFO;
				}else if (ERROR.equals(logLevel)) {
					level=Level.ERROR;
				}
			} catch (SFException e) {
				e.printStackTrace();
			}
			if(level==null){
				level=Level.INFO;
			}
			SFLogger logger=new SFLogger(logName,macCode,level);
			instanceMap.put(logName,logger);
		}
		return instanceMap.get(logName);		
	}
	
	/**
	 * ��ȡ��־����
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static SFLogger getLogger(Context context){
		//��ȡ��ǰҵ������
		String txCode=null;
		try {
			txCode = SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);
		} catch (SFException e) {
			//e.printStackTrace();
		}
		return getLogger(txCode);
	}
	/**
	 * ��ȡ��־����
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static SFLogger getLogger(String txCode){
		//��ȡ��ǰҵ������
		if(SFUtil.isEmpty(txCode)){
			 txCode=SFConst.DEFAULT_TRXCODE;
		}
		//�������л�ȡ��ǰʵ������
		String macCode=SFUtil.getSysProperty( "APP_CODE" );
		return getInstance(txCode,macCode);
	}


	/**
	 * debug
	 * @param context
	 * @param message
	 * @param e
	 */
	public void debug(Object message){
		String logMsg="]";
		if(message!=null){
			logMsg="]"+message.toString();
		}
		logger.debug(logMsg);
	}

	/**
	 * trace
	 * @param context
	 * @param message
	 * @param e
	 */
	public void trace(Object message){
		String logMsg="]";
		if(message!=null){
			logMsg="]"+message.toString();
		}
		logger.trace(logMsg);
	}

	/**
	 * info
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void info(Object message){
		String logMsg="]";
		if(message!=null){
			logMsg="]"+message.toString();
		}
		logger.info(logMsg);		
	}

	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void error(String message,Throwable exception){
		String logMsg="]";
		if(message!=null){
			logMsg="]"+message;
		}
		if(exception==null)
			logger.error(logMsg);
		else
			logger.error(logMsg,exception);
	}

	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void error(String message){
		error(message,null);
	}
	
	
	/**
	 * fatal
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void fatal(Object message){
		String logMsg="]";
		if(message!=null){
			logMsg="]"+message.toString();
		}
		logger.fatal(logMsg);
	}
	

	/**
	 * debug
	 * @param context
	 * @param message
	 * @param e
	 */
	public void debug(String logId,Object message){
		String logMsg="";
		if(SFUtil.isEmpty(logId)){
			if(message!=null){
				logMsg="]"+message.toString();
			}else{
				logMsg="]";
			}
		}else{
			if(message!=null){
				logMsg="|"+logId+"]"+message.toString();
			}else{
				logMsg="|"+logId+"]";
			}		
		}
		logger.debug(logMsg);
	}

	/**
	 * info
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void info(String logId,Object message){
		String logMsg="";
		if(SFUtil.isEmpty(logId)){
			if(message!=null){
				logMsg="]"+message.toString();
			}else{
				logMsg="]";
			}
		}else{
			if(message!=null){
				logMsg="|"+logId+"]"+message.toString();
			}else{
				logMsg="|"+logId+"]";
			}		
		}
		logger.info(logMsg);
	}

	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public void error(String logId,Object message,Throwable exception){
		//��ȡ��ǰҵ������ˮ
		String logMsg="";
		if(SFUtil.isEmpty(logId)){
			if(message!=null){
				logMsg="]"+message.toString();
			}else{
				logMsg="]";
			}
		}else{
			if(message!=null){
				logMsg="|"+logId+"]"+message.toString();
			}else{
				logMsg="|"+logId+"]";
			}		
		}
		if(exception==null)
			logger.error(logMsg);
		else
			logger.error(logMsg,exception);
	}
	
	
	/**
	 * debug
	 * @param context
	 * @param message
	 * @param e
	 */
	public static void debug(Context context,Object message){
		//��ȡ��ǰҵ������ˮ
		String logId=null;
		try {
			logId = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);
		} catch (SFException e) {
			e.printStackTrace();
		}
		SFLogger.getLogger(context).debug(logId,message);
	}

	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void error(Context context,Object message,Throwable exception){
		//��ȡ��ǰҵ������ˮ
		String logId=null;
		try {
			logId = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);
		} catch (SFException e) {
			e.printStackTrace();
		}
		SFLogger.getLogger(context).error(logId,message,exception);
	}
	
	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void error(Context context,Throwable exception){
		error( context, null,exception);
	}
	
	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void error(Context context,Object message){
		error( context, message,null);
	}

	/**
	 * info
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void info(Context context,Object message){
		//��ȡ��ǰҵ������ˮ
		String logId=null;
		try {
			logId = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);
		} catch (SFException e) {
			e.printStackTrace();
		}
		SFLogger.getLogger(context).info(logId,message);
	}
	
	
	/**
	 * debug
	 * @param context
	 * @param message
	 * @param e
	 */
	public static void debug(String txCode,String logId,Object message){
		SFLogger.getLogger(txCode).debug(logId,message);
	}

	/**
	 * info
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void info(String txCode,String logId,Object message){
		SFLogger.getLogger(txCode).info(logId,message);
	}

	/**
	 * error
	 * @param tranxCode
	 * @param logMsg
	 * @param e
	 */
	public static void error(String txCode,String logId,String message,Throwable exception){
		SFLogger.getLogger(txCode).error(logId,message,exception);
	}
	
	
	
	
	
	public static void main(String[] args){
		long start=System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			SFLogger.error("000001","201708080001","debug",null);
		}
		long end=System.currentTimeMillis();
		SFLogger.error("000001","201708080001",">>>"+String.valueOf(end-start),null);
		
		
	}
}