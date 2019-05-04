package module.dao;

import java.sql.Connection;

import module.bean.AgtAutoAgt;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class AgtAutoAgtDao extends DaoBase {
	
	/**
	 * �����첽������Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveAgtAutoAgt(Context context,Connection connection,AgtAutoAgt bean)throws SFException{
		try {
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getTxDate()), "ST4895", "��Ҫ����[TXDATE]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getPicsLogNo()), "ST4895", "��Ҫ����[PICSLOGNO]û���ṩ");
			
			super.save(context,connection, bean.getSaveAgtAutoAgtSQLConstruct());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} finally{
			if(null != bean){
				bean.resetChangedFlag();	
			}
			
		}
	}
}
