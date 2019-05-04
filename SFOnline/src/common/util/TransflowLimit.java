package common.util;

import java.sql.Connection;
import java.util.Map;

import module.bean.TransAlertLog;
import module.dao.SystemDao;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;

import core.cache.CacheMap;
import core.log.SFLogger;


public class TransflowLimit {
	/**
	 * ������������������MAP����
	 */
	public static final String COBANK_REQ_FLOW="COBANK_REQ_FLOW"; 
	/**
	 * ֱ��ȯ��������������MAP����
	 */
	public static final String ZLSECU_REQ_FLOW="ZLSECU_REQ_FLOW"; 
	/**
	 * ��֤ͨȯ��������������MAP����
	 */
	public static final String SZTSECU_REQ_FLOW="SZTSECU_REQ_FLOW"; 
	/**
	 * �����е�����������MAP����
	 */
	public static final String COBANK_SEND_FLOW="COBANK_SEND_FLOW"; 
	/**
	 * 
	 *���պ�����������������
	 *1�������к�����������������
	 *2���Ե�������������������
	 * @param context
	 * @param bankNo  �����к�
	 * @return
	 * @throws SFException
	 */
	public static void coBankReqFlow( Context context,String bankNo) throws SFException{
		try {
			Map<String,Map<String,Integer>> mapTrad=CacheMap.getCache(SFConst.FLOW_CACHE_TRAD);
			Map<String, Integer> flowLimitMap = mapTrad.get(COBANK_REQ_FLOW);//���������������������
			synchronized (flowLimitMap) {
				Integer tradReqNum = flowLimitMap.get("FLOW_LIMIT_NUM");//���к����������¼��
				Integer coBankReqNum = flowLimitMap.get(bankNo);//�������������¼��
				
				if(coBankReqNum!=null){
					coBankReqNum=coBankReqNum+1;
				}else{
					coBankReqNum=1;
				}
				if(tradReqNum!=null){
					tradReqNum=tradReqNum+1;
				}else{
					tradReqNum=1;
				}
				flowLimitMap.put(bankNo, coBankReqNum);//���������¼��
				flowLimitMap.put("FLOW_LIMIT_NUM", tradReqNum);//�����ܼ�¼��
				mapTrad.put(COBANK_REQ_FLOW, flowLimitMap);
				
				Integer dbFlowLimit = 20;//���к��������������ܷ�ֵ
				String value1 = BizUtil.getParamValue1(context, "COBANK", "FLOW_LIMIT");
				if (value1!=null){
					dbFlowLimit = Integer.parseInt(value1);
				}
				Integer dbCoBankNoFlowLimit = 10;//��������������������ֵ
				value1 = BizUtil.getParamValue1(context, "COBANK", "FLOW_LIMIT"+"_"+bankNo);
				if (value1!=null){
					dbCoBankNoFlowLimit = Integer.parseInt(value1);
				}
				
				SFLogger.info(context,String.format("ȫ�����н��������������%s��,�������С�%s�����������������%s��",tradReqNum,bankNo,coBankReqNum));
				
				if (tradReqNum>dbFlowLimit || coBankReqNum>dbCoBankNoFlowLimit) {
					SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
					String errMsg = null;
					if (tradReqNum>dbFlowLimit){
						errMsg = String.format("ȫ�����н��������������%s������������ֵ��%s��",tradReqNum,dbFlowLimit) ;
					} else {
						errMsg = String.format("�������С�%s�������������%s������������ֵ��%s��",bankNo,coBankReqNum,dbCoBankNoFlowLimit) ;
					}
					SFLogger.error(context,"���ص��ע��"+errMsg);
					
					try {
						/*
						 * ���� ����Ԥ����־
						 */
						String txCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//��ȡ���ױ���
						String logId=SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);//��ȡϵͳ��־��
						Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
						
						TransAlertLog alertLog=new TransAlertLog();
						alertLog.setMacCode(BizUtil.getMacCode(context));
						alertLog.setTxCode(txCode);
						alertLog.setLogId(logId);
						alertLog.setType("31");//31-�����з���������
						alertLog.setTxDate(DateUtil.dateToString(DateUtil.getDate(),"yyyyMMdd"));
						alertLog.setTxTime(DateUtil.dateToString(DateUtil.getDate(),"HHmmss"));
						alertLog.setMome(errMsg);
						SystemDao dao=new SystemDao();
						dao.saveTransAlertLog(context, connection, alertLog);
						DBHandler.commitTransaction(context, connection);//�ύ����
					} catch (Exception e) {//�������ݿ�ʧ�ܲ����쳣����Ӱ���������
						SFLogger.error(context,e);
					}
					SFUtil.chkCond( context, "ST5798",String.format("Ŀǰ�������ϴ�����ʵʩ�������������Ժ�����"));
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch (Exception e) {
			SFLogger.error(context, String.format("ͳ��������¼��ʧ��,%s", e.getMessage()));
		}
	}
	
	/**
	 * 
	 * ����ȯ�������������� 
	 * 1��������ȯ��������������
	 * 2���Ե���ȯ������������
	 * @param context
	 * @param secCompCode  ȯ�̱��
	 * @return
	 * @throws SFException
	 */
	public  static void secuReqFlow( Context context,String secCompCode ) throws SFException{
		try {
			Map<String,Map<String,Integer>> mapTrad=CacheMap.getCache(SFConst.FLOW_CACHE_TRAD);
			String sztFlag = SFUtil.getDataValue(context, SFConst.PUBLIC_SECU_TYPE);//ȯ������ 1-��֤ͨ 0-ֱ��
			String secuFlowType = null;
			String secuFlagName = null;
			String secuFlagType = null;
			if (SFConst.SECU_SZT.equals(sztFlag)) {//��֤ͨ
				secuFlowType = SZTSECU_REQ_FLOW;
				secuFlagName="��֤ͨ";
				secuFlagType ="SZT";
			} else {
				secuFlowType = ZLSECU_REQ_FLOW;
				secuFlagName="ֱ��";
				secuFlagType ="ZL";
			}
			Map<String, Integer> flowLimitMap = mapTrad.get(secuFlowType);//ȯ�����������������
			synchronized (flowLimitMap) {
				Integer tradReqNum = flowLimitMap.get("FLOW_LIMIT_NUM");//����ȯ�������¼��
				Integer secuReqNum = flowLimitMap.get(secCompCode);//��ȯ�������¼��
				
				if(secuReqNum!=null){
					secuReqNum=secuReqNum+1;
				}else{
					secuReqNum=1;
				}
				if(tradReqNum!=null){
					tradReqNum=tradReqNum+1;
				}else{
					tradReqNum=1;
				}
				flowLimitMap.put(secCompCode, secuReqNum);//���������¼��
				flowLimitMap.put("FLOW_LIMIT_NUM", tradReqNum);//�����ܼ�¼��
				mapTrad.put(secuFlowType, flowLimitMap);
				
				Integer dbFlowLimit = 50;//����ȯ�������ܷ�ֵ
				String value1 = BizUtil.getParamValue1(context, secuFlagType, "FLOW_LIMIT");
				if (value1!=null){
					dbFlowLimit = Integer.parseInt(value1);
				}
				Integer dbSecuflowLimit = 10;//����ȯ��������ֵ
				value1 = BizUtil.getParamValue1(context, secuFlagType, "FLOW_LIMIT"+"_"+secCompCode);
				if (value1!=null){
					dbSecuflowLimit = Integer.parseInt(value1);
				}
				float alarmthreshold = 1;//Ԥ����ֵ
				value1 = BizUtil.getParamValue1(context, secuFlagType, "FLOW_LIMIT_ALARM_THRESHOLD");
				if (value1!=null){
					alarmthreshold = Float.parseFloat(value1);
				}
				
				SFLogger.info(context,String.format("ȫ��%s��ȯ�̽��������������%s��,��ȯ�̡�%s�����������������%s��",secuFlagName,tradReqNum,secCompCode,secuReqNum));
				
				if (tradReqNum>dbFlowLimit || secuReqNum>dbSecuflowLimit) {
					SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
					String errMsg = null;
					if (tradReqNum>dbFlowLimit ){
						errMsg = String.format("ȫ��%s��ȯ�̽��������������%s������������ֵ��%s��",secuFlagName,tradReqNum,dbFlowLimit);
					} else {
						errMsg = String.format("��ȯ�̡�%s�������������%s������������ֵ��%s��",secCompCode,secuReqNum,dbSecuflowLimit);
					}
					SFLogger.error(context,"���ص��ע��"+errMsg);
					
					try {
						/*
						 * ���� ����Ԥ����־
						 */
						String txCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//��ȡ���ױ���
						String logId=SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);//��ȡϵͳ��־��
						Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
						
						TransAlertLog alertLog=new TransAlertLog();
						alertLog.setMacCode(BizUtil.getMacCode(context));
						alertLog.setTxCode(txCode);
						alertLog.setLogId(logId);
						alertLog.setType("11");//11-ȯ�̷���������
						alertLog.setTxDate(DateUtil.dateToString(DateUtil.getDate(),"yyyyMMdd"));
						alertLog.setTxTime(DateUtil.dateToString(DateUtil.getDate(),"HHmmss"));
						alertLog.setMome(errMsg);
						SystemDao dao=new SystemDao();
						dao.saveTransAlertLog(context, connection, alertLog);
						DBHandler.commitTransaction(context, connection);//�ύ����
					} catch (Exception e) {//�������ݿ�ʧ�ܲ����쳣����Ӱ���������
						SFLogger.error(context, e);
					}
					SFUtil.chkCond( context, "ST5798",String.format("Ŀǰ�������ϴ�����ʵʩ�������������Ժ�����"));
				}
				
				//�ٷֱ�Ԥ���ж�
				if (SFConst.SECU_SZT.equals(sztFlag) && new Float(tradReqNum)/new Float(dbFlowLimit) >= alarmthreshold) {
					String errMsg = String.format("ȫ��%s��ȯ�̽������������ռ�ȡ�%s���ﵽԤ����ֵ��%s��",secuFlagName,(new Float(tradReqNum)/new Float(dbFlowLimit) * 100)+"%",(alarmthreshold * 100)+"%");
					SFLogger.error(context,"���ص��ע��"+errMsg);
					
					try {
						/*
						 * ���� ����Ԥ����־
						 */
						String txCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//��ȡ���ױ���
						String logId=SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);//��ȡϵͳ��־��
						Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
						
						TransAlertLog alertLog=new TransAlertLog();
						alertLog.setMacCode(BizUtil.getMacCode(context));
						alertLog.setTxCode(txCode);
						alertLog.setLogId(logId);
						alertLog.setType("12");//12-ȯ�̷�����Ԥ��
						alertLog.setTxDate(DateUtil.dateToString(DateUtil.getDate(),"yyyyMMdd"));
						alertLog.setTxTime(DateUtil.dateToString(DateUtil.getDate(),"HHmmss"));
						alertLog.setMome(errMsg);
						SystemDao dao=new SystemDao();
						dao.saveTransAlertLog(context, connection, alertLog);
						DBHandler.commitTransaction(context, connection);//�ύ����
					} catch (Exception e) {//�������ݿ�ʧ�ܲ����쳣����Ӱ���������
						SFLogger.error(context, e);
					}
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch (Exception e) {
			SFLogger.error(context, String.format("ͳ��������¼��ʧ��,%s", e.getMessage()));
		} 
	}
	
	/**
	 * 
	 *���ӵ��ú������������� 20180604 
	 *1�������к�����������������
	 *2���Ե�������������������
	 * @param context
	 * @param bankNo  �����к�
	 * @return
	 * @throws SFException
	 */
	public static Context sendCoBankFlow( Context context,String bankNo ) throws SFException{
		try {
			Map<String,Map<String,Integer>> mapTrad=CacheMap.getCache(SFConst.FLOW_CACHE_TRAD);
			Map<String, Integer> flowLimitMap = mapTrad.get(COBANK_SEND_FLOW);//���ú����������������
			synchronized (flowLimitMap) {
				Integer tradReqNum = flowLimitMap.get("FLOW_LIMIT_NUM");//���к����������¼��
				Integer bankReqNum = flowLimitMap.get(bankNo);//�������������¼��
				if(bankReqNum!=null){
					bankReqNum=bankReqNum+1;
				}else{
					bankReqNum=1;
				}
				if(tradReqNum!=null){
					tradReqNum=tradReqNum+1;
				}else{
					tradReqNum=1;
				}
				flowLimitMap.put(bankNo, bankReqNum);//���������¼��
				flowLimitMap.put("FLOW_LIMIT_NUM", tradReqNum);//�����ܼ�¼��
				mapTrad.put(COBANK_SEND_FLOW, flowLimitMap);
				
				Integer dbFlowLimit = 20;//���к����������ܷ�ֵ
				String value = BizUtil.getParamValue(context, "COBANK", "FLOW_LIMIT");
				if (value!=null){
					dbFlowLimit = Integer.parseInt(value);
				}
				Integer dbCoBankNoflowLimit = 10;//����������������ֵ
				value = BizUtil.getParamValue(context, "COBANK", "FLOW_LIMIT"+"_"+bankNo);
				if (value!=null){
					dbCoBankNoflowLimit = Integer.parseInt(value);
				}
				
				SFLogger.info(context,String.format("ȫ�����н��׶�������%s��,�������С�%s�����׶�������%s��",tradReqNum,bankNo,bankReqNum));
				
				if (tradReqNum>dbFlowLimit || bankReqNum>dbCoBankNoflowLimit) {
					SFUtil.setDataValue(context,SFConst.PUBLIC_RET_FLAG, SFConst.RET_FAILURE);
					String errMsg = null;
					if (tradReqNum>dbFlowLimit) {
						 errMsg = String.format("ȫ�����н��׶�������%s������������ֵ��%s��",tradReqNum,dbFlowLimit);
					} else {
						errMsg = String.format("�������С�%s�������������%s������������ֵ��%s��",bankNo,bankReqNum,dbCoBankNoflowLimit);
					}
					SFLogger.error(context,"���ص��ע��"+errMsg);
					/*
					 * ��д����
					 */
					KeyedCollection kColl=SFUtil.getDataElement(context,"A_RESPONSE_HEAD");
					if(kColl == null){
						kColl=new KeyedCollection("A_RESPONSE_HEAD");
						SFUtil.addDataField(context,kColl,"RESPCODE",SFConst.RESPCODE_FAILCODE_COBANK);
						SFUtil.addDataField(context,kColl,"RESPMSG",String.format("Ŀǰ�������ϴ�����ʵʩ�������������Ժ�����"));
						SFUtil.addDataElement(context, kColl);
					}else{
						SFUtil.setDataValue(context,kColl,"RESPCODE",SFConst.RESPCODE_FAILCODE_COBANK);
						SFUtil.setDataValue(context,kColl,"RESPMSG",String.format("Ŀǰ�������ϴ�����ʵʩ�������������Ժ�����"));
					}
					SFUtil.setDataValue(context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_FAILCODE_COBANK);
					SFUtil.setDataValue(context, SFConst.CTX_ERRMSG, String.format("Ŀǰ�������ϴ�����ʵʩ�������������Ժ�����"));
				}
			}
		} catch (Exception e) {
			SFLogger.error(context, String.format("ͳ��������¼��ʧ��,%s", e.getMessage()));
		}
		return context;
	}
	
	/**
	 *�ͷ�������¼��
	 * @param context
	 * @param flowName  �����кŻ�ȯ�̴���
	 * @param flowType  COBANK_REQ_FLOW/ZLSECU_REQ_FLOW/SZTSECU_REQ_FLOW/COBANK_SEND_FLOW
	 * @throws SFException
	 */
	public  static void  releaseFlowLimit( Context context, String flowName,String flowType) throws SFException{
		try {
			Map<String,Map<String,Integer>> mapTrad=CacheMap.getCache(SFConst.FLOW_CACHE_TRAD);
			Map<String, Integer> flowLimitMap = mapTrad.get(flowType);//�����������
			synchronized (flowLimitMap) {
				Integer initSideReqNum=flowLimitMap.get(flowName);//�����¼��
				Integer tradReqNum = flowLimitMap.get("FLOW_LIMIT_NUM");//�������¼��
				if(tradReqNum!=null&&tradReqNum>1){//�ͷ��ܼ�¼��
					tradReqNum=tradReqNum-1;
				}else{
					tradReqNum=0;
				}
				if(initSideReqNum!=null&&initSideReqNum>1){//�ͷŵ�����¼��
					initSideReqNum=initSideReqNum-1;
				}else{
					initSideReqNum=0;
				}
				flowLimitMap.put(flowName, initSideReqNum);//���µ�����¼��
				flowLimitMap.put("FLOW_LIMIT_NUM", tradReqNum);//�����ܼ�¼��
				mapTrad.put(flowType, flowLimitMap);
			}
			
		} catch (Exception e) {
			SFLogger.error(context, String.format("�ͷ�������¼��ʧ��,%s", e.getMessage()));
		}
		
	}
}