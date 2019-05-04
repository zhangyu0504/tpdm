package module.dao;

import java.sql.Connection;

import module.bean.AgtAutoAgt;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class AgtAutoAgtDao extends DaoBase {
	
	/**
	 * 保存异步交易信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveAgtAutoAgt(Context context,Connection connection,AgtAutoAgt bean)throws SFException{
		try {
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getTxDate()), "ST4895", "必要参数[TXDATE]没有提供");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getPicsLogNo()), "ST4895", "必要参数[PICSLOGNO]没有提供");
			
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
