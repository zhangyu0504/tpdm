package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.SecTruAcct;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * ȯ���й��˻�������
 * @author ����
 */
public class SecTruAcctCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	private final static String CACHE_SECTRU_ACCT="CACHE_SECTRU_ACCT";//���泣��
	/**
	 * ����ȯ�̴����ȡȯ���й��˻��������
	 * @param depId ������
	 * @return
	 * @throws SFException
	 */
	public static SecTruAcct getValue(String secCompCode)throws SFException{
		//����ȯ�̴����ȡȯ���й��˻��������
		Map<String,SecTruAcct> secTruAcctMap= CacheMap.getCache(CACHE_SECTRU_ACCT);
		
		if(secTruAcctMap==null||!secTruAcctMap.containsKey(secCompCode)){
			return null;
		}
		
		return secTruAcctMap.get(secCompCode);
		
	}

	public void init(Context context,Connection connection) throws SFException {
		logger.info("��ʼ��-ȯ���й��˻������濪ʼ");
		DaoBase dao=new DaoBase();
		try {
			String sql = "SELECT T.ACCTID, T.SERIAL, T.ACCTTYPE, T.SECCOMPCODE, T.CURCODE, T.ACCTNAME, T.OPENDEPID, T.BRANCHID FROM TRDSECTRUACCT T";
			logger.info("��ѯȯ���й��˻���SQL��"+sql);
			List<SecTruAcct> listSecTruAcct = dao.qryForOList(context, connection, sql,null,SecTruAcct.class);
			Map<String,Object> secTruAcctMap = new HashMap<String,Object>();
			if(listSecTruAcct!=null&&listSecTruAcct.size()>0){
				for(SecTruAcct secTruAcctBean:listSecTruAcct) {					
					secTruAcctMap.put(secTruAcctBean.getSecCompCode(), secTruAcctBean);
				}
			}
			CacheMap.putCache(CACHE_SECTRU_ACCT, secTruAcctMap);
		} catch (Exception e) {
			logger.error("��ʼ��ȯ���й��˻�������ʧ�ܣ�"+e.getMessage());
			throw new SFException(e);
		}
		
		logger.info("��ʼ��-ȯ���й��˻����������");
	}
}