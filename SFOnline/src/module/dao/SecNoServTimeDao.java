package module.dao;

import java.sql.Connection;

import module.bean.SecNoServTime;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * ȯ�̲�����ʱ�� Dao
 * @author ������
 *
 */
public class SecNoServTimeDao extends DaoBase {

	private StringBuffer getQrySecNoServTimeSQLStruct(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT SECCOMPCODE AS secCompCode,TXDATE AS txDate,SECCOMPNAME AS secCompName,BEGINTIME AS beginTime,ENDTIME AS endTime");
		buffer.append(" FROM TRDSECNOSERVTIME WHERE ");
		
		return buffer;
	}
	
	/**
	 * ��ѯȯ�̲�����ʱ��
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public SecNoServTime qrySecNoServTime(Context context,Connection connection,String secCompCode,String txDate) throws SFException{
		SecNoServTime secNoServTime = null;
		try{
			StringBuffer buffer = getQrySecNoServTimeSQLStruct();
			
			buffer.append(" SECCOMPCODE = ? AND TXDATE = ? ");
			
			secNoServTime = super.qry(context, connection, buffer.toString(),SecNoServTime.class, secCompCode, txDate);
			
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		 return secNoServTime;
	}
	
	public SecNoServTime qrySecNoServTimeByBetweenTime(Context context,Connection connection,String secCompCode,String txDate,String beginTime,String endTime) throws SFException{
		SecNoServTime secNoServTime = null;
		try{
			StringBuffer buffer = getQrySecNoServTimeSQLStruct();
			
			buffer.append(" SECCOMPCODE = ? AND TXDATE = ? AND ? >= BEGINTIME AND ? <= ENDTIME");
			
			secNoServTime = super.qry(context, connection, buffer.toString(),SecNoServTime.class, secCompCode, txDate, beginTime, endTime);
			
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		 return secNoServTime;
	}
}