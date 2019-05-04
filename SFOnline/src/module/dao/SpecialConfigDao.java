package module.dao;

import java.sql.Connection;

import module.bean.SpecialConfig;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class SpecialConfigDao extends DaoBase {

	public SpecialConfig qrySpecialConfig(Context context,Connection connection,String cfgId,String secCompCode) throws SFException{
		SpecialConfig specialConfig = null;
		try{
		   
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("SELECT CFGID,SECCOMPCODE,CFGVALUE,MEMO FROM TRDSPECIALCONFIG WHERE CFGID = ? and SECCOMPCODE = ?");
			
			specialConfig = super.qry(context, connection, buffer.toString(),SpecialConfig.class, cfgId, secCompCode);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} 
	 return specialConfig;
  }
}
