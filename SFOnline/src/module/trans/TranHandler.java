package module.trans;


import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import module.bean.Trans;
import module.bean.TransAlertLog;
import module.cache.TransCache;
import module.dao.SystemDao;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;
import common.util.TransflowLimit;

import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * 公共跳转类
 * @author 邹磊
 *
 */
public class TranHandler{
	
	public static void doHandle(Context context) throws SFException{
		String initSide = SFUtil.getDataValue(context, SFConst.PUBLIC_INIT_SIDE);//渠道
		String txCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//获取交易编码
		String sztFlag = null;	//券商类型
		String secCompCode = null;//券商代码
		String coBankId = null;//合作行ID
		try {
			if (!"100120".equals(txCode)&&SFConst.INIT_SIDE_SECU.equals(initSide)) {//券商端渠道限流，券商端发起签到/签退 交易不做限流控制
				secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 从上下文取出券商代码
				sztFlag = SFUtil.getDataValue(context, SFConst.PUBLIC_SECU_TYPE);//券商类型 1-深证通 0-直联
				if (SFUtil.isEmpty(secCompCode)&&SFConst.SECU_SZT.equals(sztFlag)){
					String keyInputName = SFUtil.getDataValue(context, SFConst.PUBLIC_KEY_INPUTNAME);
					KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, keyInputName+".ScAcct.AcctSvcr" );
					if( acctSvcrKcoll != null ) {
						secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );
					}
				} 
				if (SFUtil.isNotEmpty(secCompCode)&&(SFConst.SECU_SZT.equals(sztFlag)||SFConst.SECU_ZL.equals(sztFlag))){
					TransflowLimit.secuReqFlow(context, secCompCode);
				}
			} else if (SFConst.INIT_SIDE_COBANK.equals(initSide)){//合作行渠道
				coBankId = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 从头信息取出BankId
				if (SFUtil.isNotEmpty(coBankId)){
					TransflowLimit.coBankReqFlow(context, coBankId);
				}
			}
			doExecute(context);
		} catch( SFException e ) {
			throw e;
		} catch (Exception e) {
			SFLogger.error(context,e);
			throw new SFException(e);
		} finally {
			//请求结束时释放在线计录数
			if (!"100120".equals(txCode)&&SFConst.INIT_SIDE_SECU.equals(initSide)&&SFUtil.isNotEmpty(secCompCode)) {//券商端发起签到/签退 交易不做限流控制
				if (SFConst.SECU_SZT.equals(sztFlag)) {
					TransflowLimit.releaseFlowLimit(context,secCompCode,TransflowLimit.SZTSECU_REQ_FLOW);
				} else if (SFConst.SECU_ZL.equals(sztFlag)) {
					TransflowLimit.releaseFlowLimit(context,secCompCode,TransflowLimit.ZLSECU_REQ_FLOW);
				}
			} else if (SFConst.INIT_SIDE_COBANK.equals(initSide)&&SFUtil.isNotEmpty(coBankId)){
				TransflowLimit.releaseFlowLimit(context,coBankId,TransflowLimit.COBANK_REQ_FLOW);
			}
		}
	}
	/**
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void doExecute(Context context) throws SFException{
		//SFLogger.info(context,"公共交易处理类模型执行开始");
		String initSide = SFUtil.getDataValue(context, SFConst.PUBLIC_INIT_SIDE);//渠道
		String txCode=SFUtil.getDataValue(context, SFConst.PUBLIC_TX_CODE);//获取交易编码
		String logId=SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);//获取系统日志号
		Trans trans = TransCache.getValue(txCode);//从缓存中获取交易对象
		if (trans==null){
			SFUtil.chkCond( context, "ST5798","业务处理失败，交易未配置！");
		}
		
		Date dtStart=null,dtEnd=null;
		SystemDao dao=new SystemDao();
		Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
		try {
			String tranClass = SFUtil.getDataValue(context,"CLASS_NAME"); 
			dtStart=DateUtil.getDate();//获取系统开始时间
			/*
			 * 计算当前交易的连接数:接收请求时增加在线计录数
			 */
			Map<String,Integer> mapTrad=CacheMap.getCache(SFConst.SYS_CACHE_TRAD);
			synchronized (mapTrad) {
				Integer tradReqNum=mapTrad.get(txCode);//请求记录数
				if(tradReqNum!=null){
					tradReqNum=tradReqNum+1;
				}else{
					tradReqNum=1;
				}
				mapTrad.put(txCode, tradReqNum);//增加请求记录数
				/*
				 * 在线最大连接限制
				 */
				if(trans.getMaxProc()<tradReqNum){
					/*
					 * 插入 限流预警日志
					 */
					TransAlertLog alertLog=new TransAlertLog();
					alertLog.setMacCode(BizUtil.getMacCode(context));
					alertLog.setTxCode(txCode);
					alertLog.setLogId(logId);
					/* 根据渠道区分限流类型 */
					if (SFConst.INIT_SIDE_SECU.equals(initSide)){
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERLOAD_SEC);
					} else if (SFConst.INIT_SIDE_COBANK.equals(initSide)){
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERLOAD_COBANK);
					} else{
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERLOAD_BANK);
					}					
					alertLog.setTxDate(DateUtil.dateToString(dtStart,"yyyyMMdd"));
					alertLog.setTxTime(DateUtil.dateToString(dtStart,"HHmmss"));
					alertLog.setMome(String.format("[%s]在线人数[OVERLOAD]",txCode));
					dao.saveTransAlertLog(context, connection, alertLog);
					DBHandler.commitTransaction(context, connection);//提交事务
					SFLogger.error(SFConst.DEFAULT_TRXCODE,logId, String.format("[%s]在线人数[OVERLOAD]",txCode),null);
					SFUtil.chkCond( context, "ST5798",String.format("[%s]在线人数[OVERLOAD]",txCode));
				}
				SFLogger.info(context,String.format("[%s]在线人数[%s]",txCode,tradReqNum));
			}
			
			/*
			 * 私有业务执行
			 */
			TranBase tranBase = (TranBase)Class.forName(tranClass).newInstance();
			tranBase.execute(context);
			
			
			/*
			 * 计算业务处理用时长
			 */
			if(dtStart!=null&&trans.getMaxTime()>0){
				dtEnd=DateUtil.getDate();//获取系统结束时间
				long duringTime=dtEnd.getTime()-dtStart.getTime();
				//20180420-wanghua-modify for(超时预警只用统一配置即可不可需要按交易控制)-s
				//long maxTime=trans.getMaxTime()*1000;//数据库配置最大超时以秒为单位
				long maxTime=10000;//统一时间预警
				//20180420-wanghua-modify for(超时预警只用统一配置即可不可需要按交易控制)-e
				if(duringTime>maxTime){
					/*
					 * 插入超时预警日志
					 */
					TransAlertLog alertLog=new TransAlertLog();
					alertLog.setMacCode(BizUtil.getMacCode(context));
					alertLog.setTxCode(txCode);
					alertLog.setLogId(logId);
					/* 根据渠道区分超时类型 */
					if (SFConst.INIT_SIDE_SECU.equals(initSide)){
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERTIME_SEC);
					} else if (SFConst.INIT_SIDE_COBANK.equals(initSide)){
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERTIME_COBANK);
					} else{
						alertLog.setType(SFConst.STANS_ALERT_LOG_OVERTIME_BANK);
					}
					alertLog.setTxDate(DateUtil.dateToString(dtStart,"yyyyMMdd"));
					alertLog.setTxTime(DateUtil.dateToString(dtStart,"HHmmss"));
					alertLog.setMome(String.format("[%s]交易预设[%s]毫秒，实际用时[%s]毫秒",txCode,maxTime,duringTime));
					dao.saveTransAlertLog(context, connection, alertLog);
					DBHandler.commitTransaction(context, connection);//提交事务
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch (Exception e) {
			SFLogger.error(context,e);
			throw new SFException(e);
		} finally{
			/*
			 * 计算当前交易的连接数:请求结束时释放在线计录数
			 */
			Map<String,Integer> mapTrad=CacheMap.getCache(SFConst.SYS_CACHE_TRAD);
			synchronized (mapTrad) {
				Integer reqNum=mapTrad.get(txCode);//请求记录数
				if(reqNum!=null&&reqNum>1){
					reqNum=reqNum-1;
				}else{
					reqNum=0;
				}
				mapTrad.put(txCode, reqNum);
			}
		}
		//SFLogger.info(context,"公共交易处理类模型执行结束");		
	}
}
