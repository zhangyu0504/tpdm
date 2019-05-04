package module.dao;

import java.sql.Connection;

import module.bean.AcctAdmDetail;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFConst;
import common.util.SFUtil;

/**
 * �˻���������ϸDao
 * @author ������
 *
 */
public class AcctAdmDetailDao extends DaoBase {

	private static final String CTX_PUBLIC_TAB_ACCTADMDETAIL_ZB = "TRDACCTADMDETAIL";

	private static final String CTX_PUBLIC_TAB_ACCTADMDETAIL_FB = "TRDACCTADMDETAIL_FB";

	/**
	 * �����˻���������ϸ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int saveAcctAdmDetail( Context context, Connection connection, AcctAdmDetail bean ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getTxDate() ), "ST4895", "��Ҫ����[TXDATE]û���ṩ" );
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getSubTxSeqId() ), "ST4895", "��Ҫ����[SUBTXSEQID]û���ṩ" );

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			// ����ʱ��β�������
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_FB;
			} else {// ����ʱ��β�������
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_ZB;
			}
			count = super.save( context, connection, bean.getSaveAcctDetailSQLStruct( tableName ) );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( null != bean ) {
				bean.resetChangedFlag();
			}
		}
		return count;
	}

	/**
	 * ����SecCompCode+CapAcct���½�����ϸ
	 * @param context
	 * @param connection
	 * @param workMode
	 * @param secAcct
	 * @param secAcctSeq
	 * @param capAcct
	 * @param secCompCode
	 * @return
	 * @throws SFException
	 */
	public int updAcctAdmDetailByCapAcct( Context context, Connection connection, String workMode, String secAcct, int secAcctSeq, String capAcct, String secCompCode ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			// ����ʱ��β�������
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_FB;
			} else {// ����ʱ��β�������
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_ZB;
			}
			String sql = "UPDATE " + tableName + " SET SECACCT = ?,SECACCTSEQ = ? WHERE CAPACCT = ? AND SECCOMPCODE = ?";
			count = super.save( context, connection, sql, secAcct, secAcctSeq, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ɾ��AcctAdmDetail
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int delAcctAdmDetail( Context context, Connection connection, String txDate, String subTxSeqId ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			// ����ʱ��β�������
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_FB;
			} else {// ����ʱ��β�������
				tableName = CTX_PUBLIC_TAB_ACCTADMDETAIL_ZB;
			}
			String sql = "DELETE FROM " + tableName + " WHERE TXDATE=? AND SUBTXSEQID=?";
			count = super.save( context, connection, sql, txDate, subTxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

}
