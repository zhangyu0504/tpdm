package module.dao;

import java.sql.Connection;

import module.bean.AllyData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * 联名信息Dao
 * @author 吕超鸿
 *
 */
public class AllyDataDao extends DaoBase {
	
	private StringBuffer getQryAllyDataSQLStruct(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT SECCOMPCODE AS secCompCode,SECCOMPNAME AS secCompName,SECBRCHID AS secBrchId,SECBRCHNAME AS secBrchName,");
		buffer.append("ACCTID AS acctId,CAPACCT AS capAcct,USEFLAG AS useFlag,UNITEFLAG AS uniteFlag,BRANCHID AS branchId,DEPID AS depId");
		buffer.append(" FROM TRDALLYDATA WHERE  ");
		
		return buffer;
	}

	/**
	 * 查询联名信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AllyData qryAllyData(Context context,Connection connection,String acctId) throws SFException{
		AllyData allyData = null;
		try{
			StringBuffer buffer = getQryAllyDataSQLStruct();
			buffer.append(" ACCTID = ?");
			
			allyData = super.qry(context, connection, buffer.toString(),AllyData.class, acctId);
		} catch (SFException e){
			throw new SFException(e);
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(allyData!=null){
				allyData.resetChangedFlag();
			}
		}
		return allyData;
	}
	
	public AllyData qryAllyDataByCapAcct(Context context,Connection connection,String secCompCode,String capAcct) throws SFException{
		AllyData allyData = null;
		try{
			StringBuffer buffer = getQryAllyDataSQLStruct();
			buffer.append(" SECCOMPCODE = ? AND CAPACCT = ?");
			
			allyData = super.qry(context, connection, buffer.toString(),AllyData.class, secCompCode, capAcct);
		} catch (SFException e){
			throw new SFException(e);
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(allyData!=null){
				allyData.resetChangedFlag();
			}
		}
		return allyData;
	}
	
	/**
	 * 保存联名信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public void updAllyData(Context context,Connection connection,String stkCompCode,String capAcct) throws SFException{
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("UPDATE TRDALLYDATA SET USEFLAG='1' WHERE SECCOMPCODE= ? AND CAPACCT = ?");
			super.save(context, connection, buffer.toString(),stkCompCode,capAcct);
		} catch (SFException e){
			throw new SFException(e);
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
	}
}
