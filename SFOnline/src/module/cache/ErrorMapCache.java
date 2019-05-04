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
 * ���״����뻺����
 *  @author ����
 */
public class ErrorMapCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	public static String CACHE_ERROR_SDBCODE = "CACHE_ERROR_SDBCODE";//���泣��
	public static String CACHE_ERROR_ZLCODE = "CACHE_ERROR_ZLCODE";	//���泣��
	public static String CACHE_ERROR_SZTCODE = "CACHE_ERROR_SZTCODE";//���泣��
	
	/**
	 * ���ݴ�ܴ������ȡ�������
	 * @param code
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getSdbValue(String sdbCode)throws SFException{
		//��ȡ�����л�ȡϵͳ���״��������ü���
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_SDBCODE);
		if(errorMap==null||errorMap.get(sdbCode)==null){
			return null;
		}
		
		return errorMap.get(sdbCode);
	}
	/**
	 * ����ֱ���������ȡ�������
	 * @param zlCode
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getZlValue(String zlCode)throws SFException{
		//��ȡ�����л�ȡϵͳ���״��������ü���
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_ZLCODE);
		
		if(errorMap==null||errorMap.get(zlCode)==null){
			return null;
		}
		
		return errorMap.get(zlCode);
	}
	/**
	 * ������֤ͨ�������ȡ�������
	 * @param sztCode
	 * @return
	 * @throws SFException
	 */
	public static ErrorMap getSztValue(String sztCode)throws SFException{
		//��ȡ�����л�ȡϵͳ���״��������ü���
		Map<String,ErrorMap> errorMap= CacheMap.getCache(CACHE_ERROR_SZTCODE);
		
		if(errorMap==null||errorMap.get(sztCode)==null){
			return null;
		}
		
		return errorMap.get(sztCode);
	}
	
	public void init(Context context,Connection connection) throws SFException {
		logger.info("��ʼ��-���״�������濪ʼ");
		DaoBase dao=new DaoBase();
		String sql = "";
		try {
			sql = "SELECT T.SZTCODE sztCode,T.ZLCODE zlCode,T.SDBCODE sdbCode,T.ERREXPLAIN errExplain FROM TRDERRORMAP T";
			logger.info("��ѯ�������SQL��"+sql);
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
			logger.error("��ʼ�����״��������ʧ�ܣ�"+e.getMessage());
			throw new SFException(e);
		}
		logger.info("��ʼ��-���״�����������");
		
	}
}
