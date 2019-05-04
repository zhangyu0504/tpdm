package module.batch;

import java.io.File;
import java.util.Map;

import module.bean.Trans;
import module.cache.ParamCache;
import module.cache.TransCache;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;
import core.schedule.ScheduleActionInterface;
/**
 * ��־����</br>
 * ÿ���賿��ʱ��־���ݣ�
 * 1���Ȱ��������ÿ�ʼ������־��
 * 2���ٰ�ȯ�̴��뿪ʼ����������־
 * 3���ƶ�������־�ļ���ָ��·����
 * @author ����
 *
 */
public class T800500 implements ScheduleActionInterface{
	
	@Override
	public boolean init(Context context) throws SFException {
		return true;
	}

	@Override
	public void execute(Context context) throws SFException {
		try{
			SFLogger.info(context, "��־���ݿ�ʼ");
			
			/*
			 * �������뱸����־
			 */
			Map<String,Trans>transMap= TransCache.getAllValue();
			String trCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//��ȡ��ǰ������
			if(transMap!=null){
				//log���·��
				String logFilePath = BizUtil.getLogPath();
				//�������л�ȡ��ǰʵ������
				String macCode=SFUtil.getSysProperty( "APP_CODE" );
				for(Trans tran:transMap.values()){
					if("0".equals(tran.getType())||"3".equals(tran.getType())){//���׷���: 0:A���������� ��1��A�ɺ�̨����������̨ ��2:B������ ��3:A����������
						/*
						 * �ж��ļ��Ƿ���ڣ������ڻ��ǿ���־�ļ�����������־
						 */
						File file=new File(logFilePath+"/" + tran.getTxCode()+"-"+macCode + ".log");
						if(!file.exists()||file.length()<=89L){
							continue;
						}						
						
						/*
						 * �����´�ӡ��־��ʽ��������־
						 */
						SFUtil.setDataValue(context, SFConst.PUBLIC_TX_CODE, tran.getTxCode());
						SFLogger.info(context, "^_^  ���õ�һ�쿪ʼ�ˣ�ߣ�����Ӽ��͸ɰɣ�  ^_^");
					}
				}
			}
			SFUtil.setDataValue(context, SFConst.PUBLIC_TX_CODE, trCode);//���õ�ǰ������
			
			/*
			 * ��־ת��
			 */
			String backUpLogPath =ParamCache.getValue2("BACKUP", "LOG_PATH");//������־·��
			backUpLogPath = SFUtil.processPath(context,backUpLogPath);
			String logPath = ParamCache.getValue2("SF_SHELL", "LOG_PATH");//��־·��
			logPath = SFUtil.processPath(context,logPath);
			SFUtil.executeShellFile(context,"backup_logs.sh", backUpLogPath, logPath);
		}catch (SFException e) {//�����߼�����			
			throw e;
		} catch (Exception e) {//ϵͳ�����쳣
			SFLogger.error(context, e.getMessage());
		}finally{
			SFLogger.info(context, "��־���ݽ���");			
		}
	}
}