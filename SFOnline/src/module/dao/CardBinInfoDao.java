package module.dao;

import java.sql.Connection;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * 深发展卡BIN信息
 * 
 * 记录深发展卡BIN信息
 * 
 * @author 吕超鸿
 *
 */
public class CardBinInfoDao extends DaoBase {

	/**
	 * 查询深发展卡BIN信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public long qryCardBinInfoCount(Context context,Connection connection,String acctId) throws SFException{
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer( "SELECT COUNT(CARDBIN) FROM CARDBININFO WHERE CARDBIN = SUBSTR(?,1,LENGTH(CARDBIN))");
			count = super.qryCount(context, connection, buffer.toString(), acctId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return count;
	}
}
