package module.dao;

import java.sql.Connection;

import module.bean.AgtTaxData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class AgtTaxDataDao extends DaoBase {
	
	/**
	 * ����˰��
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveAgtTaxData(Context context,Connection connection,AgtTaxData bean)throws SFException{
		 try {
			 	SFUtil.chkCond(context, SFUtil.isEmpty(bean.getTxDate()), "ST4895", "��Ҫ����[TXDATE]û���ṩ");
				SFUtil.chkCond(context, SFUtil.isEmpty(bean.getBankId()), "ST4895", "��Ҫ����[BANKID]û���ṩ");
				SFUtil.chkCond(context, SFUtil.isEmpty(bean.getStkCode()), "ST4895", "��Ҫ����[STKCODE]û���ṩ");
				
				super.save(context,connection, bean.getSaveAgtTaxDataSQLConstruct());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(null != bean){
				bean.resetChangedFlag();
			}
		}
	}
}
