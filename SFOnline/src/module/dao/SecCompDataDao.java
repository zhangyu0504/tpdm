package module.dao;

import java.sql.Connection;
import java.util.List;

import module.bean.SecCompData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class SecCompDataDao extends DaoBase {
	
	
	private StringBuffer getQrySecCompDataSQLStruct(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT SECCOMPCODE AS secCompCode, USERID AS userId, SECCOMPNAME AS secCompName, SECCURMAXSEQ AS secCurMaxSeq, WEBSITE AS website, SHSEATCODE AS SHSeatCode, SZSEATCODE AS SZSeatCode, SHORTSEATCODE AS ShortSeatCode,");
		buffer.append(" SECPHONE AS secPhone, NVL(SZTFLAG,'0') AS sztFlag, SZTSECUSERID AS sztSecUserId, RECVPATH AS recvPath, SENDPATH AS sendPath, OVERDRAFTFLAG AS overDraftFlag, JGZZFLAG AS JGZZFlag, JGZZFLAG1 AS JGZZFlag1, NVL(IP,' ') AS ip,");
		buffer.append(" NVL(PORT,' ') AS port, FTPUSER AS ftpUser, FTPPASS AS ftpPass, QSPATH AS qsPath, FTPIP AS ftpIp, FTPPORT AS ftpPort, QSRECVPATH AS qsRecvPath, BANKPRESIGNFLAG AS bankPreSignFlag, BANKPRESIGNMODE AS bankPreSignMode,");
		buffer.append(" CAPACCTMODE AS capAcctMode, SECCAPACCTMODE AS secCapAcctMode, TPDMFLAG AS tpdmFlag, BANKPRESIGNMODE2 AS bankPreSignMode2, NETACTIVEFLAG AS netActiveFlag, BANKPREDESMODE AS bankPreDesMode, PROTOCOL AS protocol,");	
		buffer.append(" SECBRCHIDMODE AS secBrchIdMode, CHECKBOOKNOFLAG AS checkBookNoFlag, SECONESIGNFLAG AS secOneSignFlag, HOLIDAYFLAG AS holidayFlag, STARTTIME AS startTime, ENDTIME AS endTime, ALLDAYFLAG AS allDayFlag, ADBEGINTIME AS ADBeginTime, ADENDTIME AS ADENDTIME, IIACCTFLAG AS IIAcctFlag");
		buffer.append(" FROM TRDSECCOMPDATA WHERE ");	
		return buffer;
	}
	
	/**
	 * 查询券商信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public SecCompData qrySecCompData(Context context,Connection connection,String secCompCode) throws SFException{
		SecCompData secCompData = null;
		try {
			StringBuffer buffer = getQrySecCompDataSQLStruct();
			buffer.append(" SECCOMPCODE=?");
			
			secCompData = super.qry(context, connection, buffer.toString(), SecCompData.class, secCompCode) ;
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return secCompData ;
	}
	
	/**
	 * 查询三方券商列表
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */	
	public List<SecCompData> qrySecCompDataBySecCompType(Context context,Connection connection,String secCompType) throws SFException{	
			StringBuffer buffer = getQrySecCompDataSQLStruct();		
			if("0".equals(secCompType)){
				buffer.append(" TPDMFLAG = '1'");	
			}else if("1".equals(secCompType)){
				buffer.append(" TPDMFLAG = '2'");
			}else if("2".equals(secCompType)){
				buffer.append(" BANKPRESIGNFLAG = '1'");
			}else if("3".equals(secCompType)){
				buffer.append(" NETACTIVEFLAG = '1'");
			} else {
				buffer.append(" 1 = 1");
			}
			List<SecCompData> list = super.qryForOList(context, connection,buffer.toString(),new Object[0],SecCompData.class);

		return list ;
	}
	
}