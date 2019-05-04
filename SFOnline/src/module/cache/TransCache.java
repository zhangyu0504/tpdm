package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.Trans;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;
/**
 * 交易配置缓存类
 * @author 汪华
 *
 */
public class TransCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	private final static String CACHE_TRD_TRANDS="CACHE_TRD_TRANDS";//缓存常量
	/**
	 * 获取交易缓存对象
	 * @param code 交易码
	 * @return
	 * @throws SFException
	 */
	public static Trans getValue(String code)throws SFException{
		//获取对象中获取系统交易配置集合
		Map<String,Trans> transMap= CacheMap.getCache(CACHE_TRD_TRANDS);
		
		if(transMap==null||!transMap.containsKey(code)){
			return null;
		}
		
		return transMap.get(code);
		
	}
	
	/**
	 * 获取所有交易缓存对象
	 * @return
	 * @throws SFException
	 */
	public static Map<String,Trans> getAllValue()throws SFException{
		//获取对象中获取系统交易配置集合
		Map<String,Trans> transMap= CacheMap.getCache(CACHE_TRD_TRANDS);
		
		if(transMap==null){
			return null;
		}		
		return transMap;		
	}
	

	public void init(Context context,Connection connection) throws SFException {
		logger.info("初始化-交易配置表缓存开始");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.TXCODE txCode,T.TXNAME txName,T.LOGLEVEL logLevel,T.TYPE type,T.SECUCTLFLAGS secuCtlFlags,T.SFCTLFLAGS sfCtlFlags,T.MAXPROC maxProc,T.MAXTIME maxTime,T.MEMO memo FROM TRDTRANS T";
			logger.info("查询交易码表SQL："+sql);
			List<Trans> listTrans = dao.qryForOList(context, connection, sql,null,Trans.class);
			Map<String,Object> codeMap = new HashMap<String,Object>();
			if(listTrans!=null&&listTrans.size()>0){
				for(Trans transBean:listTrans) {
					codeMap.put(transBean.getTxCode(), transBean);
				}
			}
			CacheMap.putCache(CACHE_TRD_TRANDS, codeMap);
		} catch (Exception e) {
			logger.error("初始化交易配置表缓存失败！"+e.getMessage());
			throw new SFException(e);
		}
		
		logger.info("初始化-交易配置表缓存结束");
	}
}
