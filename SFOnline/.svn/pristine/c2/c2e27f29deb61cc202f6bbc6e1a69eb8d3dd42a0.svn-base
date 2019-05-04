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
 * 根据参数配置开启深证通轮训线程<p>
 * 1、程序启动时根据参数配置初始化线程；
 * 2、刷新缓存时计算原本初始化的线程数与最新的参数配置线程数的差异，只有当最新参数配置线程数大于原来的才会新增线程（只增不能减）；
 * @author 汪华
 *
 */
public class SZTThreadInitialize {
	
	public void execute()throws SFException{
		/*
		 * 深证通开关不为1时将不启动深证通轮训取消息
		 */
		String sztStartFlag = SFUtil.getSysProperty("SZT_ENABLE");//深证通开关
		if(!"1".equals(sztStartFlag)){
			return;			
		}
		
		/*
		 * 参数中获取线程队例大小
		 */
		int currPoolSize=0;//参数配置的线程数
		String pPoolSize=ParamCache.getValue2("SZT","THREAD_NUM");
		if(SFUtil.isNotEmpty(pPoolSize)){
			currPoolSize=Integer.parseInt(pPoolSize);
		}else{
			currPoolSize=SZTTCPIPServiceServlet.DEFAULT_POOL_SIZE;
		}
		
		/*
		 * 缓存对象中获取已经初始化的线程
		 */
		Map<String, Object> cacheSzt=CacheMap.getCache(SZTTCPIPServiceServlet.CACHE_MAP_SZT);
		String cPoolSize=(String) cacheSzt.get("SZT_POOL_SIZE");
		int orgPoolSize=0;//已经开启的线程数
		if(SFUtil.isNotEmpty(cPoolSize)){
			orgPoolSize=Integer.parseInt(cPoolSize);
		}
		
		/*
		 * 计算需要开启多少个线程：
		 * 1、程序启动时根据参数配置初始化线程；
		 * 2、刷新缓存时计算原本初始化的线程数与最新的参数配置线程数的差异，只有当最新参数配置线程数大于原来的才会新增线程（只增不能减）；
		 */
		if(orgPoolSize>=currPoolSize){
			return ;
		}
		int difPoolSize=currPoolSize-orgPoolSize;//差异线程数
		cacheSzt.put("SZT_POOL_SIZE",String.valueOf(currPoolSize));//重新调置线程数
		
		/*
		 * 创建线程池
		 */
		ExecutorService executor=(ExecutorService) cacheSzt.get("SZT_EXECUTOR_SERVER");
//		if(executor!=null){
//			executor.shutdown();
//		}
		executor = Executors.newFixedThreadPool(difPoolSize);
		
		/*
		 * 开启SZT取消息轮循(默认开启5个线程获取）
		 */
		String appCode = SFUtil.getSysProperty("APP_CODE");
		for(int i=orgPoolSize;i<currPoolSize;i++){
			//计算线程编号
			String poolNo=appCode+"_"+BizUtil.getTxSeqId(2,String.valueOf(i+1));
			//构建线程运行对象
			SZTThreadPoolState poolState=new SZTThreadPoolState();
			poolState.setPoolNo(poolNo);			
			//启动线程
			executor.execute(new SZTSocketPolling(poolState));
			//线程状态添加到缓存中
			cacheSzt.put(poolNo, poolState);
			SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "启动读取深证通轮训线程，线程编号【"+poolNo+"】");
		}
		SFLogger.info(SFConst.DEFAULT_TRXCODE, null, "启动读取深证通轮训线程总数【"+currPoolSize+"】");		
		CacheMap.putCache(SZTTCPIPServiceServlet.CACHE_MAP_SZT,cacheSzt);//回写缓存对象
	}
}
