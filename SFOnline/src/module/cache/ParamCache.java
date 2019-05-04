package module.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.Param;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;

import core.cache.CacheHandler;
import core.cache.CacheMap;
import core.log.SFLogger;

/**
 * ���ײ���������
 *  @author ����
 */
public class ParamCache extends CacheHandler{
	private SFLogger logger=SFLogger.getLogger("000000");
	public static String CACHE_TRD_PARAM = "CACHE_TRD_PARAM";	//���泣��
	
	/**
	 * ��ȡ�����������
	 * @param type  ��������
	 * @param code	����ID
	 * @return
	 * @throws SFException
	 */
	public static Param getValue(String type,String id)throws SFException{
		Map<String,Param> cacheParam=getGroupParam(type);
		if(cacheParam == null || !cacheParam.containsKey(id)){
			return null;
		} 
		return cacheParam.get(id);		
	}
	
	/**
	 * ��ȡ��������VALUEֵ
	 * @param type
	 * @param id
	 * @return
	 * @throws SFException
	 */
	public static String getValue2(String type,String id)throws SFException{
		Map<String,Param> cacheParam=getGroupParam(type);
		if(cacheParam == null || !cacheParam.containsKey(id)){
			return null;
		}
		return cacheParam.get(id).getValue().trim();		
	}

	public static Map<String,Param> getGroupParam(String type)throws SFException{
		//��ȡϵͳ�������ü���
		Map<String,Map<String,Param>> cacheMap= CacheMap.getCache(CACHE_TRD_PARAM);
		
		if(cacheMap==null||!cacheMap.containsKey(type)){
			return null;
		}
		return cacheMap.get(type);
	}
	
	public void init(Context context,Connection connection) throws SFException {
		logger.info("��ʼ��-���������濪ʼ");
		DaoBase dao = new DaoBase();
		String sql = "";
		try {
			sql="SELECT T.TYPE type,T.TYPE_DESC typeDesc,T.ID id,T.VALUE value,T.VALUE1 value1,T.NAME name,T.MEMO memo FROM TRDPARAM T";
			List<Param> result = dao.qryForOList(context, connection, sql,null,Param.class);
			Map<String,Map<String,Param>> mGroupParam=new HashMap<String,Map<String,Param>>();
			if (result != null&&result.size()>0) {
				for (Param paramBean:result) {
					if (mGroupParam.get(paramBean.getType()) != null) {
						mGroupParam.get(paramBean.getType()).put(paramBean.getId(), paramBean);
					}else{
						Map<String, Param> mParam = new HashMap<String, Param>();
						mParam.put(paramBean.getId(),paramBean);
						mGroupParam.put(paramBean.getType(),mParam);
					}
				}
			}
			CacheMap.putCache(CACHE_TRD_PARAM, mGroupParam);
		} catch (Exception e) {
			logger.error("��ʼ������������ʧ�ܣ�"+e.getMessage());
			throw new SFException(e);
		} 
		
		logger.info("��ʼ��-�������������");
	}	
}