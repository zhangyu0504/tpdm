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
 * ���л�����Ϣ������
 * @author ����
 */
public class BankUnitCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	private final static String CACHE_BANK_UNIT="CACHE_BANK_UNIT";//���泣��
	/**
	 * ���ݻ����Ż�ȡ���л�����Ϣ�������
	 * @param depId ������
	 * @return
	 * @throws SFException
	 */
	public static BankUnit getValue(String depId)throws SFException{
		//���ݻ����Ż�ȡ���л�����Ϣ���ü���
		Map<String,BankUnit> bankUnitMap= CacheMap.getCache(CACHE_BANK_UNIT);
		
		if(bankUnitMap==null||!bankUnitMap.containsKey(depId)){
			return null;
		}
		
		return bankUnitMap.get(depId);
		
	}

	public void init(Context context,Connection connection) throws SFException {
		logger.info("��ʼ��-���л��������濪ʼ");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.DEPID,T.BRANCHID ,T.DEPNAME,T.ADDR,T.ZIP,T.PHONE,T.FAX,T.CONNAME,T.CONPHONE,T.QUASHFLAG,T.COMDEPID FROM TRDBANKUNIT T";
			logger.info("��ѯ���л�����SQL��"+sql);
			List<BankUnit> listBankUnit = dao.qryForOList(context, connection, sql,null,BankUnit.class);
			Map<String,Object> depIdMap = new HashMap<String,Object>();
			if(listBankUnit!=null&&listBankUnit.size()>0){
				for(BankUnit bankUnitBean:listBankUnit) {					
					depIdMap.put(bankUnitBean.getDepId(), bankUnitBean);
				}
			}
			CacheMap.putCache(CACHE_BANK_UNIT, depIdMap);
		} catch (Exception e) {
			logger.error("��ʼ�����л���������ʧ�ܣ�"+e.getMessage());
			throw new SFException(e);
		}
		logger.info("��ʼ��-���л������������");
	}
}