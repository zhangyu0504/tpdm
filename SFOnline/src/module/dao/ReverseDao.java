package module.dao;

import java.sql.Connection;
import java.util.List;

import module.bean.Reverse;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
/**
 * ������ϢDAO
 * @author ����
 *
 */
public class ReverseDao  extends DaoBase{
	/**
	 * ����������Ϣ
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int saveReverse(Context context,Connection connection,Reverse reverse)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("INSERT INTO TRDREVERSE(SEQID,TXDATE,SUBTXSEQID,TYPE,TXCODE,INITSIDE,CHANNEL,SCENECODE,");
		sql.append("REVERSESEQID,REVERSENUM,STATUS,RESPCODE,RESPMSG,REVERSESEQ,CREATEDATETIME,UPDATEDATETIME,MEMO)");
		sql.append("VALUES(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,sysdate, sysdate,?)");
		Object[] params={reverse.getSeqId(),reverse.getTxDate(),reverse.getSubTxSeqId(),reverse.getType(),reverse.getTxCode(),
				reverse.getInitside(),reverse.getChannel(),reverse.getSceneCode(),reverse.getReverseSeqId(),reverse.getReverseNum(),
				reverse.getStatus(),reverse.getRespCode(),reverse.getRespMsg(),reverse.getReverseSeq(),reverse.getMemo()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * ������ɺ��д������Ϣ
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateForRes(Context context,Connection connection,Reverse reverse)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDREVERSE SET REVERSENUM=REVERSENUM+1,STATUS=?,RESPCODE=?,RESPMSG=?,UPDATEDATETIME=sysdate,REVERSESEQID=? WHERE TXDATE=? AND SUBTXSEQID=? AND SEQID=?");
		Object[] params={reverse.getStatus(),reverse.getRespCode(),reverse.getRespMsg(),reverse.getReverseSeqId(),reverse.getTxDate(),
				reverse.getSubTxSeqId(),reverse.getSeqId()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * ������ʼʱ������������
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateForLock(Context context,Connection connection,Reverse reverse)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDREVERSE SET STATUS='9' WHERE TXDATE=? AND SUBTXSEQID=? AND SEQID=?");
		Object[] params={reverse.getTxDate(),reverse.getSubTxSeqId(),reverse.getSeqId()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * �޸ĳ���״̬
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateForStatus(Context context,Connection connection,Reverse reverse)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDREVERSE SET STATUS=? WHERE TXDATE=? AND SUBTXSEQID=? AND SEQID=?");
		Object[] params={reverse.getStatus(),reverse.getTxDate(),reverse.getSubTxSeqId(),reverse.getSeqId()};
		return super.save(context, connection, sql.toString(),params);
	}
	
	/**
	 * ��ǰҵ��ǰ�ó���ʧ�ܺ��޸ĺ�����������ˮ״̬Ϊ�ֶ�����
	 * @param context
	 * @param connection
	 * @param reverse
	 * @return
	 * @throws SFException
	 */
	public int updateForNextStatus(Context context,Connection connection,Reverse reverse)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("UPDATE TRDREVERSE SET STATUS=? WHERE TXDATE=? AND SUBTXSEQID=? AND SEQID=? AND REVERSESEQ<?");
		Object[] params={reverse.getStatus(),reverse.getTxDate(),reverse.getSubTxSeqId(),reverse.getSeqId(),reverse.getReverseSeq()};
		return super.save(context, connection, sql.toString(),params);
	}

	/***
	 * ��ѯ�������ĳ�����¼
	 * @param context
	 * @param connection
	 * @param defaulTimes Ĭ�ϳ�������
	 * @param serverType ��ǰӦ�÷�������
	 * @return
	 * @throws SFException
	 */
	public List<Reverse> queryReverses(Context context,Connection connection,String sqlCondition,int defaulTimes,String serverType)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT SEQID,TXDATE,SUBTXSEQID,TYPE,TXCODE,INITSIDE,CHANNEL,SCENECODE,REVERSESEQID,REVERSESEQ,");
		sql.append("REVERSENUM,STATUS,RESPCODE,RESPMSG,CREATEDATETIME,UPDATEDATETIME,MEMO ");
		sql.append("FROM TRDREVERSE WHERE STATUS IN('0','2') AND REVERSENUM<? AND TYPE=? ");
		sql.append(sqlCondition);
		sql.append(" ORDER BY TXDATE,SUBTXSEQID,REVERSESEQ");
		Object[] params={defaulTimes,serverType};
		return super.qryForOList(context, connection, sql.toString(),params,Reverse.class);
	}
	

	

	/***
	 * ��ѯ������¼��Ϣ
	 * @param context , SUBTXSEQID, 
	 * @param connection
	 * @param txDate
	 * @param subTxSeqId
	 * @param seqId
	 * @return
	 * @throws SFException
	 */
	public Reverse queryReverse(Context context,Connection connection,String txDate,String subTxSeqId,String seqId)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT SEQID,TXDATE,SUBTXSEQID,TYPE,TXCODE,INITSIDE,CHANNEL,SCENECODE,REVERSESEQID,REVERSESEQ,");
		sql.append("REVERSENUM,STATUS,RESPCODE,RESPMSG,CREATEDATETIME,UPDATEDATETIME,MEMO ");
		sql.append("FROM TRDREVERSE WHERE STATUS IN('0','2') AND TXDATE=? AND SUBTXSEQID=? AND SEQID=?");
		return super.qry(context, connection,sql.toString(),Reverse.class,txDate,subTxSeqId,seqId);
	}
	

	

	/***
	 * ��鵱ǰ������¼ǰ�����Ƿ���δ������ɵļ�¼
	 * @param context , SUBTXSEQID, 
	 * @param connection
	 * @param txDate
	 * @param subTxSeqId
	 * @param seqId
	 * @return
	 * @throws SFException
	 */
	public List<Reverse> queryUnReverses(Context context,Connection connection,String txDate,String subTxSeqId,String reverseSeq)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT SEQID,TXDATE,SUBTXSEQID,TYPE,TXCODE,INITSIDE,CHANNEL,SCENECODE,REVERSESEQID,REVERSESEQ,");
		sql.append("REVERSENUM,STATUS,RESPCODE,RESPMSG,CREATEDATETIME,UPDATEDATETIME,MEMO ");
		sql.append("FROM TRDREVERSE WHERE STATUS<>'1' AND TXDATE=? AND SUBTXSEQID=? AND REVERSESEQ<?");
		Object[] params={txDate,subTxSeqId,reverseSeq};
		return super.qryForOList(context, connection, sql.toString(),params,Reverse.class);
	}
	
	/**
	 * ����������ĳ�����¼��
	 * @param context
	 * @param connection
	 * @param defaulTimes
	 * @param serverType
	 * @return
	 * @throws SFException
	 */
	public long cutReverse(Context context,Connection connection,int defaulTimes,String serverType)throws SFException{
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT COUNT(SEQID) FROM TRDREVERSE WHERE STATUS IN('0','2') AND REVERSENUM<? AND TYPE=? ORDER BY TXDATE,SUBTXSEQID,REVERSESEQ");
		return super.qryCount(context, connection, sql.toString(), defaulTimes,serverType);
	}
	
	
}