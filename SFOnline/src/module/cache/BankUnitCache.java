package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.BankUnit;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * 银行机构信息缓存类
 * @author 张钰
 */
public class BankUnitCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	private final static String CACHE_BANK_UNIT="CACHE_BANK_UNIT";//缓存常量
	/**
	 * 根据机构号获取银行机构信息缓存对象
	 * @param depId 机构号
	 * @return
	 * @throws SFException
	 */
	public static BankUnit getValue(String depId)throws SFException{
		//根据机构号获取银行机构信息配置集合
		Map<String,BankUnit> bankUnitMap= CacheMap.getCache(CACHE_BANK_UNIT);
		
		if(bankUnitMap==null||!bankUnitMap.containsKey(depId)){
			return null;
		}
		
		return bankUnitMap.get(depId);
		
	}

	public void init(Context context,Connection connection) throws SFException {
		logger.info("初始化-银行机构表缓存开始");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.DEPID,T.BRANCHID ,T.DEPNAME,T.ADDR,T.ZIP,T.PHONE,T.FAX,T.CONNAME,T.CONPHONE,T.QUASHFLAG,T.COMDEPID FROM TRDBANKUNIT T";
			logger.info("查询银行机构表SQL："+sql);
			List<BankUnit> listBankUnit = dao.qryForOList(context, connection, sql,null,BankUnit.class);
			Map<String,Object> depIdMap = new HashMap<String,Object>();
			if(listBankUnit!=null&&listBankUnit.size()>0){
				for(BankUnit bankUnitBean:listBankUnit) {					
					depIdMap.put(bankUnitBean.getDepId(), bankUnitBean);
				}
			}
			CacheMap.putCache(CACHE_BANK_UNIT, depIdMap);
		} catch (Exception e) {
			logger.error("初始化银行机构表缓存失败！"+e.getMessage());
			throw new SFException(e);
		}
		logger.info("初始化-银行机构表缓存结束");
	}
}
