package module.dao;

import java.sql.Connection;

import module.bean.Holiday;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * ��������
 * @author ex_kjkfb_zhangyu04
 * @date 2018-5-28 ����05:42:01
 * @since 1.0
 */
public class HolidayDao extends DaoBase {

	/**
	 * ��ѯ���ղ���������
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public Holiday qryHoliday( Context context, Connection connection, String txDate ) throws SFException {
		Holiday holiday = null;
		try {
			StringBuffer buffer = new StringBuffer();

			buffer.append( "SELECT TXDATE AS txDate ,HOLIDAYFLAG AS holiDayFlag FROM TRDHOLIDAY WHERE TXDATE =?" );

			holiday = super.qry( context, connection, buffer.toString(), Holiday.class, txDate );
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return holiday;
	}

}
