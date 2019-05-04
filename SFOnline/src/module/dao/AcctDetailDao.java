package module.dao;

import java.sql.Connection;

import module.bean.AcctDetail;
import module.trans.Page;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFConst;
import common.util.SFUtil;

/**
 * ��������Dao
 * @author ��˫��
 *
 */
public class AcctDetailDao extends DaoBase {
	/* ������ */
	private static final String CTX_PUBLIC_TAB_ACCTDETAIL_ZB = "TRDACCTDETAIL";
	private static final String CTX_PUBLIC_TAB_ACCTDETAIL_FB = "TRDACCTDETAIL_FB";
	private static final String CTX_PUBLIC_TAB_ACCTDETAIL_LB = "TRDACCTDETAILHIS";
	
	/**
	 * ���潻������
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveAcctDetail(Context context, Connection connection, AcctDetail bean) throws SFException {
		String tableName = null;
		try {
			
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getTxDate()), "ST4895", "��Ҫ����[TXDATE]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getSubTxSeqId()), "ST4895", "��Ҫ����[SUBTXSEQID]û���ṩ");
			
			//�Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue(context, SFConst.PUBLIC_WORKMODE);
			
			if(SFConst.WORKMODE_724CLEAR.equals(workMode)){
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_FB;
			}else{
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_ZB;
			}
			super.save(context, connection, bean.getSaveAcctDetailSQLStruct(tableName));
			
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} finally {
			if(null!=bean){
				bean.resetChangedFlag();
			}
		}
	}

	public void updAcctDetail(Context context, Connection connection, String workMode,String secAcct,int secAcctSeq,String capAcct,String secCompCode) throws SFException {
		String tableName = null;
		try {
			if(SFConst.WORKMODE_724CLEAR.equals(workMode)){
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_FB;
			}else{
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_ZB;
			}
			
			String sql = "UPDATE "+ tableName + " SET SECACCT = ?,SECACCTSEQ = ? WHERE CAPACCT = ? AND SECCOMPCODE = ?";
			
			super.save( context, connection, sql,  secAcct, secAcctSeq, capAcct, secCompCode);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
	}
	
	/**
	 * ����ʱ���޸���ˮ
	 * @param context
	 * @param connection
	 * @param workMode
	 * @param revTxSeqId
	 * @param txDate
	 * @param userId
	 * @param TxSeqId
	 * @return
	 * @throws SFException
	 */
	public int updAcctDetailByReverseJourFlag( Context context, Connection connection, String jourFlag, String txDate, String userId, String TxSeqId ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_FB;
			} else {
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_ZB;
			}
			String sql = "UPDATE " + tableName + " SET JOURFLAG = ? WHERE TXDATE = ? AND USERID = ? AND TXSEQID = ?";

			count = super.save( context, connection, sql, jourFlag, txDate, userId, TxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}
	
	/**
	 * ��ѯ��������
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AcctDetail qryAcctDetail(Context context, Connection connection, String subTxSeqId,String txDate) throws SFException {
		AcctDetail acctDetail = null;
		String tableName = null;
		try {
			//�Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue(context, SFConst.PUBLIC_WORKMODE);
			
			if(SFConst.WORKMODE_724CLEAR.equals(workMode)){
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_FB;
			}else{
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_ZB;
			}		
			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT O.TXDATE,O.INITSIDE,O.USERID,O.TXSEQID,O.SECSEQID,O.SUBTXSEQID,O.INVTYPE,");
			buffer.append("O.SECACCT,O.SECACCTSEQ,O.ACCTID,O.OPENDEPID,O.OPENBRANCHID,O.SECCOMPCODE,O.CAPACCT,");
			buffer.append("O.CURCODE,O.DCFLAG,O.TXAMOUNT,O.ACCTBAL,O.ABSTRACT  abst,O.ABSTRACTSTR,O.JOURFLAG,O.TXCODE,");
			buffer.append("O.BUSITYPE,O.TXTIME,O.DEPID,O.BRANCHID,O.UNITTELLERID,O.CASHREMITFLAG,O.ACCTDEALID,");
			buffer.append("O.ACCTHOSTSEQID,O.PRESEQID,O.ACCTDEALDATE,O.COLFLAG,O.MEMO,O.TRANSEQID,O.BUSISEQID,O.UMID");
			buffer.append(" FROM " + tableName + " O");
			buffer.append(" WHERE O.SUBTXSEQID=? AND O.TXDATE=?");		
			acctDetail = super.qry(context, connection, buffer.toString(), AcctDetail.class,subTxSeqId,txDate);			
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(acctDetail!=null){
				acctDetail.resetChangedFlag();
			}
		}
		return acctDetail;
	}
	
	/**
	 * (������������)��(�����з���) ��ѯ������ϸ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public Page<AcctDetail> qryAcctDetailInfo(Context context,
			Connection connection, String startDate, String endDate,String acctId, String capAcct,
			String secCompCode,String curCode,String workDate, Page<AcctDetail> page)
			throws SFException {
		try {	
			//�Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue(context, SFConst.PUBLIC_WORKMODE);				
			StringBuffer buffer = new StringBuffer();
			if(startDate.equals(workDate)){	
				buffer.append("SELECT A.CURCODE curCode,A.TXAMOUNT txAmount,A.TXDATE txDate,A.CAPACCT capAcct,A.SECCOMPCODE secCompCode,B.SECCOMPNAME secCompName,A.INITSIDE initSide,A.BUSITYPE busiType,A.ACCTID acctId ,A.INVTYPE invType FROM ");
				if(SFConst.WORKMODE_724CLEAR.equals(workMode)){ //����ʱ��β�������
					buffer.append(CTX_PUBLIC_TAB_ACCTDETAIL_FB + " A, TRDSECCOMPDATA B");//��ѯ����TRDACCTDETAIL_FB	
				}else{
					buffer.append(CTX_PUBLIC_TAB_ACCTDETAIL_ZB + " A, TRDSECCOMPDATA B");//��ѯ��ʽ��TRDACCTDETAIL
				}
			}else{
				/*
				 * ��acctIdΪ��ʱINDEX(a TRDACCTDETAILHIS_IDX3),INDEX(b SYS_C0016997)��Ӱ��Ч��
				 */
				if(acctId!=null){
					buffer.append("SELECT /*+ INDEX(a TRDACCTDETAILHIS_IDX3),INDEX(b SYS_C0016997) */ A.CURCODE curCode,A.TXAMOUNT txAmount,A.TXDATE txDate,A.CAPACCT capAcct,A.SECCOMPCODE secCompCode,B.SECCOMPNAME secCompName,A.INITSIDE initSide,A.BUSITYPE busiType,A.ACCTID acctId ,A.INVTYPE invType FROM ");
				}else{
					buffer.append("SELECT A.CURCODE curCode,A.TXAMOUNT txAmount,A.TXDATE txDate,A.CAPACCT capAcct,A.SECCOMPCODE secCompCode,B.SECCOMPNAME secCompName,A.INITSIDE initSide,A.BUSITYPE busiType,A.ACCTID acctId ,A.INVTYPE invType FROM ");
				}
				
				buffer.append(" TRDACCTDETAILHIS A, TRDSECCOMPDATA B");//��ѯ��ʷ��TRDACCTDETAILHIS
			}
			buffer.append(" WHERE A.SECCOMPCODE = B.SECCOMPCODE ");
			if(acctId!=null){
				buffer.append(" AND A.ACCTID = ? ");
			}
			buffer.append(" AND A.BUSITYPE IN ('01','02')");
			buffer.append(" AND A.TXDATE BETWEEN ? AND ? ");
			buffer.append(" AND JOURFLAG LIKE '0%'");
			//buffer.append(" AND A.CURCODE = ? ");
			
			
			if(SFUtil.isNotEmpty(secCompCode) && SFUtil.isNotEmpty(capAcct)){
				buffer.append(" AND A.CAPACCT = ? AND A.SECCOMPCODE = ? ORDER BY A.TXDATE");
				if(acctId!=null){
					page = super.qryPageing(context, connection, buffer.toString(),AcctDetail.class,page,acctId,startDate,endDate,capAcct,secCompCode);					
				}else{
					page = super.qryPageing(context, connection, buffer.toString(),AcctDetail.class,page,startDate,endDate,capAcct,secCompCode);
				}
			}else{
				buffer.append(" ORDER BY A.TXDATE");
				if(acctId!=null){
					page = super.qryPageing(context, connection, buffer.toString(),AcctDetail.class,page,acctId,startDate,endDate);
				}else{
					page = super.qryPageing(context, connection, buffer.toString(),AcctDetail.class,page,startDate,endDate);
				}
			}					
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return page;
	}	
	
	public AcctDetail qryAcctDetailByFlag(Context context, Connection connection, String txDate, String subTxSeqId, String flag) throws SFException {
		AcctDetail acctDetail = null;
		String tableName = null;
		try {
			if ("Z".equals(flag)){
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_ZB;
			}
			else if ("F".equals(flag)){
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_FB;
			}
			else{
				tableName = CTX_PUBLIC_TAB_ACCTDETAIL_LB;
			}	
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT O.TXDATE,O.INITSIDE,O.USERID,O.TXSEQID,O.SECSEQID,O.SUBTXSEQID,O.INVTYPE,");
			buffer.append("O.SECACCT,O.SECACCTSEQ,O.ACCTID,O.OPENDEPID,O.OPENBRANCHID,O.SECCOMPCODE,O.CAPACCT,");
			buffer.append("O.CURCODE,O.DCFLAG,O.TXAMOUNT,O.ACCTBAL,O.ABSTRACT  abst,O.ABSTRACTSTR,O.JOURFLAG,O.TXCODE,");
			buffer.append("O.BUSITYPE,O.TXTIME,O.DEPID,O.BRANCHID,O.UNITTELLERID,O.CASHREMITFLAG,O.ACCTDEALID,");
			buffer.append("O.ACCTHOSTSEQID,O.PRESEQID,O.ACCTDEALDATE,O.COLFLAG,O.MEMO,O.TRANSEQID,O.BUSISEQID,O.UMID");
			buffer.append(" FROM " + tableName + " O");
			buffer.append(" WHERE O.TXDATE=? AND O.SUBTXSEQID=?");
			acctDetail = (AcctDetail) super.qry(context, connection, buffer.toString(), AcctDetail.class, new Object[] { txDate, subTxSeqId });
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally {
			if( null != acctDetail ) {
				acctDetail.resetChangedFlag();
			}
		}
		return acctDetail;
	}
	
}