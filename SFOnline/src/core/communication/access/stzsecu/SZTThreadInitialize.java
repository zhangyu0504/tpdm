package core.communication.access.stzsecu;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import module.cache.ParamCache;

import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * ���ݲ������ÿ�����֤ͨ��ѵ�߳�<p>
 * 1����������ʱ���ݲ������ó�ʼ���̣߳�
 * 2��ˢ�»���ʱ����ԭ����ʼ�����߳��������µĲ��������߳����Ĳ��죬ֻ�е����²��������߳�������ԭ���ĲŻ������̣߳�ֻ�����ܼ�����
 * @author ����
 *
 */
public class SZTThreadInitialize {
	
	public void execute()throws SFException{
		/*
		 * ��֤ͨ���ز�Ϊ1ʱ����������֤ͨ��ѵȡ��Ϣ
		 */
		String sztStartFlag = SFUtil.getSysProperty("SZT_ENABLE");//��֤ͨ����
		if(!"1".equals(sztStartFlag)){
			return;			
		}
		
		/*
		 * �����л�ȡ�̶߳�����С
		 */
		int currPoolSize=0;//�������õ��߳���
		String pPoolSize=ParamCache.getValue2("SZT","THREAD_NUM");
		if(SFUtil.isNotEmpty(pPoolSize)){
			currPoolSize=Integer.parseInt(pPoolSize);
		}else{
			currPoolSize=SZTTCPIPServiceServlet.DEFAULT_POOL_SIZE;
		}
		
		/*
		 * ��������л�ȡ�Ѿ���ʼ�����߳�
		 */
		Map<String, Object> cacheSzt=CacheMap.getCache(SZTTCPIPServiceServlet.CACHE_MAP_SZT);
		String cPoolSize=(String) cacheSzt.get("SZT_POOL_SIZE");
		int orgPoolSize=0;//�Ѿ��������߳���
		if(SFUtil.isNotEmpty(cPoolSize)){
			orgPoolSize=Integer.parseInt(cPoolSize);
		}
		
		/*
		 * ������Ҫ�������ٸ��̣߳�
		 * 1����������ʱ���ݲ������ó�ʼ���̣߳�
		 * 2��ˢ�»���ʱ����ԭ����ʼ�����߳��������µĲ��������߳����Ĳ��죬ֻ�е����²��������߳�������ԭ���ĲŻ������̣߳�ֻ�����ܼ�����
		 */
		if(orgPoolSize>=currPoolSize){
			return ;
		}
		int difPoolSize=currPoolSize-orgPoolSize;//�����߳���
		cacheSzt.put("SZT_POOL_SIZE",String.valueOf(currPoolSize));//���µ����߳���
		
		/*
		 * �����̳߳�
		 */
		ExecutorService executor=(ExecutorService) cacheSzt.get("SZT_EXECUTOR_SERVER");
//		if(executor!=null){
//			executor.shutdown();
//		}
		executor = Executors.newFixedThreadPool(difPoolSize);
		
		/*
		 * ����SZTȡ��Ϣ��ѭ(Ĭ�Ͽ���5���̻߳�ȡ��
		 */
		String appCode = SFUtil.getSysProperty("APP_CODE");
		for(int i=orgPoolSize;i<currPoolSize;i++){
			//�����̱߳��
			String poolNo=appCode+"_"+BizUtil.getTxSeqId(2,String.valueOf(i+1));
			//�����߳����ж���
			SZTThreadPoolState poolState=new SZTThreadPoolState();
			poolState.setPoolNo(poolNo);			
			//�����߳�
			executor.execute(new SZTSocketPolling(poolState));
			//�߳�״̬��ӵ�������
			cacheSzt.put(poolNo, poolState);
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "������ȡ��֤ͨ��ѵ�̣߳��̱߳�š�"+poolNo+"��");
		}
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "������ȡ��֤ͨ��ѵ�߳�������"+currPoolSize+"��");		
		CacheMap.putCache(SZTTCPIPServiceServlet.CACHE_MAP_SZT,cacheSzt);//��д�������
	}
}
