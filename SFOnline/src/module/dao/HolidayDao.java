package module.dao;

import java.sql.Connection;

import module.bean.Holiday;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * 假日无忧
 * @author ex_kjkfb_zhangyu04
 * @date 2018-5-28 下午05:42:01
 * @since 1.0
 */
public class HolidayDao extends DaoBase {

	/**
	 * 查询假日不无忧日期
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
