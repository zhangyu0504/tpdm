package module.dao;

import java.sql.Connection;

import module.bean.ReverseLog;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
/**
 * 冲正日志DAO
 * @author 汪华
 *
 */
public class ReverseLogDao  extends DaoBase{
	/**
	 * 新增冲正日志
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int saveReverseLog(Context context,Connection connection,ReverseLog reverseLog)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("INSERT INTO TRDREVERSELOG(MACDATE,MACTIME,TXSEQID,SUBTXSEQID,SEQID,REVERSESEQID,RESPCODE,RESPMSG,MEMO)");
		sql.append("VALUES(?,?,?,?,?, ?,?,?,?)");
		Object[] params={reverseLog.getMacDate(),reverseLog.getMacTime(),reverseLog.getTxSeqId(),reverseLog.getSubTxSeqId(),reverseLog.getSeqId(),
				reverseLog.getReverseSeqId(),reverseLog.getRespCode(),reverseLog.getRespMsg(),reverseLog.getMemo()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * 冲正完成后回写冲正日志信息
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateReverseLog(Context context,Connection connection,ReverseLog reverseLog)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDREVERSELOG SET RESPCODE=?,RESPMSG=?,REVERSESEQID=? WHERE TXSEQID=?");
		Object[] params={reverseLog.getRespCode(),reverseLog.getRespMsg(),reverseLog.getReverseSeqId(),reverseLog.getTxSeqId()};
		return super.save(context, connection, sql.toString(),params);
	}
}
