package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.ProductInfo;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * 银行产品信息缓存类
 * @author 张钰
 */
public class ProductInfoCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	private final static String CACHE_PRODUCT_INFO="CACHE_PRODUCT_INFO";//缓存常量
	/**
	 * 根据券商代码获取银行产品信息缓存对象
	 * @param depId 机构号
	 * @return
	 * @throws SFException
	 */
	public static ProductInfo getValue(String secCompCode)throws SFException{
		//根据券商代码获取银行机构信息配置集合
		Map<String,ProductInfo> productInfoMap= CacheMap.getCache(CACHE_PRODUCT_INFO);
		
		if(productInfoMap==null||!productInfoMap.containsKey(secCompCode)){
			return null;
		}
		
		return productInfoMap.get(secCompCode);
		
	}

	public void init(Context context,Connection connection) throws SFException {
		logger.info("初始化-银行产品表缓存开始");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.SECCOMPCODE, T.USERID, T.SECCOMPNAME, T.PRODUCTTYPE, T.PRODUCTNAME, T.CURCODE, T.CURNAME, T.PERMITFLAG, T.FIRMCODE, T.TRUACCTID, T.TRUOPNDEPID, T.SECSELFACCTID, T.SELFOPNDEPID, T.SECCORACCTID, T.COROPNDEPID, T.TPDMFLAG FROM TRDPRODUCTINFO T";
			logger.info("查询银行产品表SQL："+sql);
			List<ProductInfo> listProduct = dao.qryForOList(context, connection, sql,null,ProductInfo.class);
			Map<String,Object> productMap = new HashMap<String,Object>();
			if(listProduct!=null&&listProduct.size()>0){
				for(ProductInfo ProductBean:listProduct) {					
					productMap.put(ProductBean.getSecCompCode(), ProductBean);
				}
			}
			CacheMap.putCache(CACHE_PRODUCT_INFO, productMap);
		} catch (Exception e) {
			logger.error("初始化银行产品表缓存失败！"+e.getMessage());
			throw new SFException(e);
		}
		
		logger.info("初始化-银行产品表缓存结束");
	}
}
