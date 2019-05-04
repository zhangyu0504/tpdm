package module.dao;

import java.sql.Connection;

import module.bean.HolidayDate;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * ���ղ���������
 * @author ������
 *
 */
public class HolidayDateDao extends DaoBase {

	/**
	 * ��ѯ���ղ���������
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public HolidayDate qryHolidayData( Context context, Connection connection, String secCompCode, String txDate ) throws SFException {
		HolidayDate holidayDate = null;
		try {
			StringBuffer buffer = new StringBuffer();

			buffer.append( "SELECT SECCOMPCODE AS secCompCode,SECCOMPNAME AS secCompName,TXDATE AS txDate FROM TRDHOLIDAYDATE WHERE SECCOMPCODE = ? AND TXDATE =?" );

			holidayDate = super.qry( context, connection, buffer.toString(), HolidayDate.class, secCompCode, txDate );
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(holidayDate!=null){
				holidayDate.resetChangedFlag();
			}
		}
		return holidayDate;
	}

}
