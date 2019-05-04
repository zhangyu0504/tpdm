package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.ErrorMap;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * 交易错误码缓存类
 *  @author 张钰
 */
public class ErrorMapCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	public static String CACHE_ERROR_SDBCODE = "CACHE_ERROR_SDBCODE";//缓存常量
	public static String CACHE_ERROR_ZLCODE = "CACHE_ERROR_ZLCODE";	//缓存常量
	public static String CACHE_ERROR_SZTCODE = "CACHE_ERROR_SZTCODE";//缓存常量
	
	/**
	 * 根据存管错误码获取缓存对象
	 * @param code
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getSdbValue(String sdbCode)throws SFException{
		//获取对象中获取系统交易错误码配置集合
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_SDBCODE);
		if(errorMap==null||errorMap.get(sdbCode)==null){
			return null;
		}
		
		return errorMap.get(sdbCode);
	}
	/**
	 * 根据直联错误码获取缓存对象
	 * @param zlCode
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getZlValue(String zlCode)throws SFException{
		//获取对象中获取系统交易错误码配置集合
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_ZLCODE);
		
		if(errorMap==null||errorMap.get(zlCode)==null){
			return null;
		}
		
		return errorMap.get(zlCode);
	}
	/**
	 * 根据深证通错误码获取缓存对象
	 * @param sztCode
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getSztValue(String sztCode)throws SFException{
		//获取对象中获取系统交易错误码配置集合
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_SZTCODE);
		
		if(errorMap==null||errorMap.get(sztCode)==null){
			return null;
		}
		
		return errorMap.get(sztCode);
	}
	
	public void init(Context context,Connection connection) throws SFException {
		logger.info("初始化-交易错误码表缓存开始");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.SZTCODE sztCode,T.ZLCODE zlCode,T.SDBCODE sdbCode,T.ERREXPLAIN errExplain FROM TRDERRORMAP T";
			logger.info("查询错误码表SQL："+sql);
			List<ErrorMap> listError= dao.qryForOList(context, connection, sql,null,ErrorMap.class);
			Map<String,Object> sdbMap = new HashMap<String,Object>();
			Map<String,Object> zlMap = new HashMap<String,Object>();
			Map<String,Object> sztMap = new HashMap<String,Object>();
			
			if(listError!=null&&listError.size()>0){
				for(ErrorMap errorMap:listError){
					sdbMap.put(errorMap.getSdbCode(), errorMap);
					zlMap.put(errorMap.getZlCode(), errorMap);
					sztMap.put(errorMap.getSztCode(), errorMap);
				}
			}
			
			CacheMap.putCache(CACHE_ERROR_SDBCODE, sdbMap);
			CacheMap.putCache(CACHE_ERROR_ZLCODE, zlMap);
			CacheMap.putCache(CACHE_ERROR_SZTCODE, sztMap);
		} catch (Exception e) {
			logger.error("初始化交易错误码表缓存失败！"+e.getMessage());
			throw new SFException(e);
		}
		logger.info("初始化-交易错误码表缓存结束");
		
	}
}
