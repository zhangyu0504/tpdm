package core.communication.access.stzsecu;

import com.ecc.emp.core.EMPException;
import com.sscc.fdep.mrapi;
import common.util.SFConst;

import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * 深证通轮训服务
 * 
 * @author ex_xxkjb_wh
 * 
 */
public class SZTSocketPolling implements Runnable {
	SFLogger logger = SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
	
	private String poolNo=null;
	
	private SZTThreadPoolState poolState=null;
	
	public SZTSocketPolling(SZTThreadPoolState poolState){
		this.poolState=poolState;
		this.poolNo=poolState.getPoolNo();
	}

	public void run() {
		while (true) {
			try {
							
				byte[] respData = null;
				/*
				 * Mr2Receive1的第一个参数为本应用的应标识
				 */
				respData = this.receive();
				String result = new String(respData);
				if (result!=null&&result.length() > 9) {
					logger.info("读取深证通消息开始，线程编号【"+poolNo+"】" );
					logger.info("深证通消息:" + result + "^");
					
					/*
					 * 交易处理前线程状态设置
					 */
					//this.poolState.setStatus("1");//取到消息时设置为运行状态
					//this.poolState.setTranNum(this.poolState.getTranNum()+1);//交易量每次递增
					
					/*
					 * 交易处理
					 */
					SZTRequestHandler handler = new SZTRequestHandler();
					handler.handleRequest(respData);
					
					/*
					 * 交易处理后线程状态设置
					 */
					//this.poolState.setStatus("0");//交易处理完后设置为空闲状态
					//this.poolState.setLastTime(DateUtil.getMacDateTimeShort());//设置最后处理时间
					
					logger.info("处理深证通消息结束，线程编号【"+poolNo+"】" );
				}
				/*
				 * 取完消息后等待1s再取消息
				 */
				Thread.sleep(1000);
			} catch (Exception e) {
				//this.poolState.setStatus("0");//未取到消息时设置为空闲状态
				logger.error("深证通获取消息失败", e);
			}
		}
	}

	/**
	 * 
	 * @return
	 * @throws EMPException
	 */
	public byte[] receive() throws EMPException {
		SZTAccessParam sztParam = CacheMap.getCache("SZT_ACCESS_PARAM");
		String appId = sztParam.getServerAppId();
		byte[] respData = mrapi.Mr2Receive1(appId, "", "", "", "", "",
				"<EMPTY>", "", "", 2000);
		return respData;
	}

}
