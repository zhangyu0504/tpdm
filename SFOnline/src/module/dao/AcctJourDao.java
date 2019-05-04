package module.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import module.bean.AcctJour;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 交易流水Dao
 * @author 申双江
 *
 */
public class AcctJourDao extends DaoBase {

	private static final String CTX_PUBLIC_TAB_ACCTJOUR_ZB = "TRDACCTJOUR";

	private static final String CTX_PUBLIC_TAB_ACCTJOUR_FB = "TRDACCTJOUR_FB";

	/**
	 * 保存交易流水
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int saveAcctJour( Context context, Connection connection, AcctJour bean ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getTxDate() ), "ST4895", "必要参数[TXDATE]没有提供" );
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getSubTxSeqId() ), "ST4895", "必要参数[SUBTXSEQID]没有提供" );

			// 是否清算时间段标识
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			// 清算时间段操作副表
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_FB;
			} else {// 其他时间段操作主表
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_ZB;
			}
			count = super.save( context, connection, bean.getSaveAcctJourSQLStruct( tableName ) );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "数据库操作错误!" );
		} finally {
			if( null != bean ) {
				bean.resetChangedFlag();
			}

		}
		return count;
	}

	public int updAcctJour( Context context, Connection connection, String workMode, String secAcct, int secAcctSeq, String capAcct, String secCompCode ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_FB;
			} else {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_ZB;
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

	private StringBuffer getQryAcctJourSqlStruct( Context context ) throws SFException {
		String tableName = null;
		StringBuffer buffer = null;
		try {
			// 是否清算时间段标识
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			// 清算时间段操作副表
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_FB;
			} else {// 交易时间段操作主表
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_ZB;
			}

			buffer = new StringBuffer();
			buffer.append( "SELECT O.TXDATE,O.INITSIDE,O.USERID,O.TXSEQID,O.REVTXSEQID,O.SECSEQID,O.SUBTXSEQID,O.INVTYPE,O.INVNAME,O.IDTYPE,O.INVIDCODE,O.OLDINVNAME,O.OLDIDTYPE," );
			buffer.append( "O.OLDINVIDCODE,O.SECACCT,O.SECACCTSEQ,O.SECCOMPCODE,O.CAPACCT,O.ACCTID,O.SAVACCT,O.OPENDEPID,O.OPENBRANCHID,O.OLDACCTID,O.CURCODE,O.DCFLAG,O.TXAMOUNT," );
			buffer.append( "O.ACCTBAL,O.ABSTRACT abst,O.ABSTRACTSTR,O.JOURFLAG,O.TXCODE,O.BUSITYPE,O.TXTIME,O.DEPID,O.BRANCHID,O.UNITTELLERID,O.CASHREMITFLAG,O.ACCTDEALID,O.ACCTHOSTSEQID," );
			buffer.append( "O.PRESEQID,O.ACCTDEALDATE,O.BANKSEQID,O.PRODUCTTYPE,O.COLFLAG,O.ABORTFLAG,O.ABNDEALTIMES,O.ABNDEALTXTIME,O.MEMO,O.RESPCODE,O.RESPMSG,O.TRANSEQID,O.BUSISEQID," );
			buffer.append( "O.CHLSEQID,O.CHLTRANSEQID,O.UMID,O.YBSTATUS" );
			buffer.append( " FROM " + tableName + " O " );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return buffer;

	}

	/**
	 * 查询交易流水
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AcctJour qryAcctJour( Context context, Connection connection, String txDate, String UserId, String TxSeqId ) throws SFException {
		AcctJour acctJour = null;
		try {

			StringBuffer buffer = getQryAcctJourSqlStruct( context );
			buffer.append( " WHERE O.TXDATE=? AND O.USERID  = ? AND O.TXSEQID = ?" );

			acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, txDate, UserId, TxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( acctJour != null ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}

	/**
	 * 查询交易流水
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AcctJour qryAcctJour( Context context, Connection connection, String txDate, String subTxSeqId ) throws SFException {
		AcctJour acctJour = null;
		try {
			StringBuffer buffer = getQryAcctJourSqlStruct( context );
			buffer.append( " WHERE O.TXDATE=? AND O.SUBTXSEQID = ?" );
			acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, txDate, subTxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( acctJour != null ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}


	/**
	 * 检查是否存在重复流水
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AcctJour qryAcctJourByJourFlag( Context context, Connection connection, String secSeqId, String secCompCode, String acctId, String jourFlag, String txDate ) throws SFException {
		AcctJour acctJour = null;
		try {
			StringBuffer buffer = getQryAcctJourSqlStruct( context );
			buffer.append( " WHERE O.SECSEQID=? AND O.SECCOMPCODE=? AND O.JOURFLAG =? AND O.TXDATE = ?" );
			if( SFUtil.isNotEmpty( acctId ) ) {
				buffer.append( "  AND O.ACCTID=? " );
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, secSeqId, secCompCode, jourFlag, txDate, acctId );
			} else {
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, secSeqId, secCompCode, jourFlag, txDate );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( acctJour != null ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}

	/**
	 * 根据TXCODE组装SQL进行查询
	 * 针对查询当天是否发生过转账交易
	 * @param context
	 * @param connection
	 * @param bean
	 * @param txCode 交易码
	 * @return
	 * @throws SFException
	 */
	public AcctJour qryAcctJourByTxCode( Context context, Connection connection ) throws SFException {
		Object txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 从上下文中获取交易码
		AcctJour acctJour = null;

		try {

			SignAccountData signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );

			StringBuffer buffer = getQryAcctJourSqlStruct( context );
			buffer.append( " WHERE O.CURCODE='RMB' AND ROWNUM=1 " );
			if( "300102".equals( txCode ) ) {// (合作行发起)变更结算账号
				buffer.append( " AND O.SECACCT=? AND O.TXDATE=?  AND O.SECCOMPCODE=? AND O.CAPACCT=? AND O.BUSITYPE IN ('01','02','03','04','05','06','07','22')" );
				// 所有参数值从Context中取出
				String secAcct = SFUtil.objectToString( ( ( InvestData )SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA ) ).getSecAcct() );// 从投资人信息对象取出secAcct
				String secCompCode = signAccountData.getSecCompCode();// 券商代码
				String capAcct = signAccountData.getCapAcct();// 证券资金账号
				String txDate = ( ( LocalInfo )( SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ) ).getWorkdate();// 交易日期
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, secAcct, txDate, secCompCode, capAcct );
			} else if( "200102".equals( txCode ) ) {// (银行端发起)变更结算帐号
				buffer.append( " AND (O.ACCTID=? OR O.ACCTID=? OR O.CAPACCT=?) AND O.TXDATE=?  AND O.BUSITYPE IN ('01','02','03','04','05','06','07','22')" );
				// 所有参数值从Context中取出
				String acctId = SFUtil.getReqDataValue( context, "NEW_ACCT_ID" );// 新帐号
				String oldAcctId = SFUtil.getReqDataValue( context, "OLD_ACCT_ID" );// 旧帐号
				String capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// 证券资金账号
				String txDate = SFUtil.getDataValue( context, "TRAN_DATE" );// 交易日期
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, acctId, oldAcctId, capAcct, txDate );
			} else if( "100101".equals( txCode ) ) {// (券商端发起) 撤销签约关系
				String secAcct = signAccountData.getSecAcct();// 保证金管理账号
				String capAcct = signAccountData.getCapAcct();// 证券资金账号
				String txDate = SFUtil.getDataValue( context, "TRAN_DATE" );// 交易日期
				buffer.append( " AND O.SECACCT=? AND O.CAPACCT=? AND O.TXDATE=?  AND O.BUSITYPE IN ('01','02','03','04','05','06','07')" );
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, secAcct, capAcct, txDate );
			} else if( "100105".equals( txCode ) ) {// (券商端发起) 销户结息
				SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
				String userId = secCompData.getUserId();
				String secAcct = signAccountData.getSecAcct();// 保证金管理账号
				String capAcct = signAccountData.getCapAcct();// 证券资金账号
				String txDate = SFUtil.getDataValue( context, "TRAN_DATE" );// 交易日期
				String secCompCode = signAccountData.getSecCompCode();
				buffer.append( " AND O.SECACCT=? AND O.CAPACCT=? AND O.TXDATE=?  AND O.SECCOMPCODE=? AND O.USERID = ? AND TXCODE='6045' AND JOURFLAG = '00'" );
				acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, secAcct, capAcct, txDate, secCompCode, userId );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( acctJour != null ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}


	/**
	 * 银转证交易
	 * 9点前 同一券商必须有客户做过成功查询券商余额或券商预指定或券商发起银证转账交易
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AcctJour qryB2SAcctJour( Context context, Connection connection, String txDate, String secCompCode ) throws SFException {
		AcctJour acctJour = null;
		try {

			StringBuffer buffer = getQryAcctJourSqlStruct( context );

			buffer.append( " WHERE O.TXDATE = ? AND O.SECCOMPCODE = ?" );
			buffer.append( "   AND ((O.INITSIDE IN ('B','A','D','E','T','M','C','F') AND O.BUSITYPE IN ('52','22','02','25') AND O.JOURFLAG = '00')" );
			buffer.append( "   OR (O.INITSIDE = 'S' AND O.BUSITYPE IN ('01','02','21') AND O.JOURFLAG = '00')) " );
			buffer.append( "   AND ROWNUM = 1" );

			acctJour = super.qry( context, connection, buffer.toString(), AcctJour.class, txDate, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( acctJour != null ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}

	/**
	 * 修改流水表状态
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public int updAcctJourDataByJourFlag( Context context, Connection connection, String JourFlag, String secSeqId, String respMsg, String txDate, String subTxSeqId ) throws SFException {
		int count = 0;
		try {
			// 是否清算时间段标识
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			List<String> parmList = new ArrayList<String>();

			StringBuffer buffer = new StringBuffer( "UPDATE " );
			// 清算时间段操作副表
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_ACCTJOUR_FB );
			} else {// 交易时间段操作主表
				buffer.append( CTX_PUBLIC_TAB_ACCTJOUR_ZB );
			}

			buffer.append( " SET JOURFLAG = ?" );
			buffer.append( ",SECSEQID = ?" );

			parmList.add( JourFlag );
			parmList.add( secSeqId );
			if( SFUtil.isNotEmpty( respMsg ) ) {
				buffer.append( ",RESPMSG = ?" );
				parmList.add( respMsg );
			}
			buffer.append( " WHERE TXDATE = ? AND SUBTXSEQID = ?" );
			parmList.add( txDate );
			parmList.add( subTxSeqId );

			count = super.update( context, connection, buffer.toString(), parmList.toArray() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 冲正时，修改流水
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
	public int updAcctJourByReverseJourFlag( Context context, Connection connection, String revTxSeqId, String jourFlag, String txDate, String userId, String TxSeqId ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			// 是否清算时间段标识
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_FB;
			} else {
				tableName = CTX_PUBLIC_TAB_ACCTJOUR_ZB;
			}
			String sql = "UPDATE " + tableName + " SET REVTXSEQID = ?,JOURFLAG = ? WHERE TXDATE = ? AND USERID = ? AND TXSEQID = ?";

			count = super.save( context, connection, sql, revTxSeqId, jourFlag, txDate, userId, TxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 修改流水表返回的错误信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public int updAcctJourDataByErr( Context context, Connection connection, String respCode, String respMsg, String txDate, String txCode, String subTxSeqId ) throws SFException {
		int count = 0;
		try {
			// 是否清算时间段标识
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer( "UPDATE " );
			// 清算时间段操作副表
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_ACCTJOUR_FB );
			} else {// 交易时间段操作主表
				buffer.append( CTX_PUBLIC_TAB_ACCTJOUR_ZB );
			}

			buffer.append( " SET RESPCODE = ?" );
			buffer.append( ",RESPMSG = ?" );

			buffer.append( " WHERE TXDATE=? AND TXCODE = ? AND SUBTXSEQID=?" );
			count = super.save( context, connection, buffer.toString(), respCode, respMsg, txDate, txCode, subTxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 通过渠道交易流水号查询交易流水记录
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */	
	public AcctJour qryAcctJourByChlTranSeqId(Context context, Connection connection, String bankDate, String WorkDate, String chlTranSeqId) throws SFException {
		AcctJour acctJour = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("select *                                       ");
			buffer.append("  from (SELECT A.txdate       as txDate,       ");
			buffer.append("               A.busitype     as busiType,     ");
			buffer.append("               A.subtxseqid   as subtxseqid,   ");
			buffer.append("               A.chltranseqid as chlTranSeqId, ");
			buffer.append("               A.chlseqid     as chlSeqId,     ");
			buffer.append("               A.respcode     as respCode,     ");
			buffer.append("               A.respmsg      as respMsg,      ");
			buffer.append("               A.jourflag     as jourFlag,     ");
			buffer.append("               'Z'            as ybStatus      "); // 使用ybStatus字段保存当前流水记录存在于何表 :'Z' 正表 ;'F' 副表; 'L' 历史表;
			buffer.append("          FROM TRDACCTJOUR A                   ");
			buffer.append("         WHERE ( A.txdate = ? or A.txdate = ? )");
			buffer.append("           AND A.chltranseqid = ?              ");
			buffer.append("        union all                              ");
			buffer.append("        SELECT A.txdate       as txDate,       ");
			buffer.append("               A.busitype     as busiType,     ");
			buffer.append("               A.subtxseqid   as subtxseqid,   ");
			buffer.append("               A.chltranseqid as chlTranSeqId, ");
			buffer.append("               A.chlseqid     as chlSeqId,     ");
			buffer.append("               A.respcode     as respCode,     ");
			buffer.append("               A.respmsg      as respMsg,      ");
			buffer.append("               A.jourflag     as jourFlag,     ");
			buffer.append("               'F'            as ybStatus      "); // 使用ybStatus字段保存当前流水记录存在于何表 :'Z' 正表 ;'F' 副表; 'L' 历史表;
			buffer.append("          FROM TRDACCTJOUR_fb A                ");
			buffer.append("         WHERE ( A.txdate = ? or A.txdate = ? )");
			buffer.append("           AND A.chltranseqid = ?              ");
			buffer.append(" )where rownum = 1                             ");
			acctJour = (AcctJour) super.qry(context, connection, buffer.toString(), AcctJour.class, new Object[] { bankDate, WorkDate, chlTranSeqId, bankDate, WorkDate, chlTranSeqId });
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally {
			if( null != acctJour ) {
				acctJour.resetChangedFlag();
			}
		}
		return acctJour;
	}

}
