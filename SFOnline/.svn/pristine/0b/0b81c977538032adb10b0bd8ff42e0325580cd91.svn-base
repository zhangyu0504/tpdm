package module.dao;
import java.sql.Connection;

import module.bean.AgtCardBinInfo;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;
/**
 * 代理行卡BIN信息
 * 
 * 
 * @author 吕超鸿
 *
 */
public class AgtCardBinInfoDao extends DaoBase {

	/**
	 * 查询代理行卡BIN信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public long qryCardBinInfoCount(Context context,Connection connection,String acctId) throws SFException{
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer("SELECT COUNT(BANKID) FROM AGT_CARDBININFO WHERE CARDBIN = SUBSTR(?,1,LENGTH(CARDBIN))");
			count = super.qryCount(context, connection, buffer.toString(), acctId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}  
		return count;
	}
	
	public AgtCardBinInfo qryAgtCardBinInfo(Context context,Connection connection,String bankId, String acctId) throws SFException{
		AgtCardBinInfo agtCardBinInfo = null;
		try {
			StringBuffer buffer = new StringBuffer("SELECT BANKID AS bankId,CARDBIN AS cardBin FROM AGT_CARDBININFO WHERE BANKID = ? AND  CARDBIN = SUBSTR(?,1,LENGTH(CARDBIN))");
			agtCardBinInfo = super.qry(context, connection, buffer.toString(), AgtCardBinInfo.class, bankId, acctId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} finally{
			if(agtCardBinInfo!=null){
				agtCardBinInfo.resetChangedFlag();
			}
		}
		return agtCardBinInfo;
	}
}
