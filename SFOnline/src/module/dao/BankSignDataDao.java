package module.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import module.bean.BankSignData;
import module.trans.Page;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * 银行预指定信息表Dao
 * @author 申双江
 *
 */
public class BankSignDataDao extends DaoBase {

	private StringBuffer getQryBankSignDataSQLStruct() {
		StringBuffer buffer = new StringBuffer();

		buffer.append( "SELECT INVTYPE AS invType,ACCTID AS acctId,SAVACCT AS savAcct,ACCTCHLDNUM AS acctChldNum,CURCODE AS curCode," );
		buffer.append( "SECCOMPCODE AS secCompCode,PRODUCTTYPE AS productType,CAPACCT AS capAcct,INVNAME AS invName,IDTYPE AS idType," );
		buffer.append( "INVIDCODE AS invIdCode,SIGNFLAG AS signFlag,CUSMAGNO AS cusMagno,DEPID AS depId,BRANCHID AS branchId,BOOKNO AS bookNo," );
		buffer.append( "OPENDEPID AS openDepId,OPENBRANCHID AS openBranchId,UNITTELLERID AS unitTellerId,OPENDATE AS openDate,INITSIDE AS initSide," );
		buffer.append( "OPENTIME AS openTime,TXDATE AS txDate,TXTIME AS txTime,FLAGS AS flags,MEMO AS memo,SUBTXSEQID1 AS subTxSeqId1," );
		buffer.append( "SUBTXSEQID2 AS subTxSeqId2,SUBTXSEQID3 AS subTxSeqId3,DELDATE AS delDate,DELTIME AS delTime,CARDTYPE AS cardType," );
		buffer.append( "PHONE AS phone,MOBILE AS mobile,SECBRCHID AS secBrchId,CHANNEL AS channel,LMCARD AS lmCard,EMAIL AS email" );
		buffer.append( " FROM TRDBANKSIGNDATA WHERE " );

		return buffer;
	}

	/**
	 * 保存银行预指定信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveBankSignData( Context context, Connection connection, BankSignData bean ) throws SFException {
		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getAcctId() ), "ST4895", "必要参数[ACCTID]没有提供" );
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getSecCompCode() ), "ST4895", "必要参数[SECCOMPCODE]没有提供" );

			super.save( context, connection, bean.getSaveBankSignDataSQLStruct() );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			bean.resetChangedFlag();
		}
	}

	/**
	 *  将预指定记录信息移至银行预指定历史信息表
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void migrateBankSignDataToHistory( Context context, Connection connection, String acctId, String secCompCode ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();

			buffer.append( "INSERT INTO TRDBANKSIGNDATAHIS SELECT INVTYPE,ACCTID,SAVACCT,ACCTCHLDNUM,CURCODE,SECCOMPCODE,PRODUCTTYPE,CAPACCT,INVNAME,IDTYPE,INVIDCODE,SIGNFLAG," );
			buffer.append( " CUSMAGNO,DEPID,BRANCHID,OPENDEPID,OPENBRANCHID,UNITTELLERID,OPENDATE,OPENTIME,TXDATE,TXTIME,FLAGS,MEMO,SUBTXSEQID1," );
			buffer.append( " SUBTXSEQID2,SUBTXSEQID3,DELDATE,DELTIME,CARDTYPE,BOOKNO,INITSIDE,PHONE,MOBILE,SECBRCHID,CHANNEL,LMCARD,EMAIL" );
			buffer.append( " FROM TRDBANKSIGNDATA WHERE ACCTID = ? AND SECCOMPCODE= ?" );

			super.save( context, connection, buffer.toString(), acctId, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 查询银行预指定信息
	 * @param context
	 * @param connection
	 * @param AcctId 
	 * @param secCompCode
	 * @param idType
	 * @param invIdCode
	 * @param curCode
	 * @param invName
	 * @param txDate
	 * @param txTime
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignData( Context context, Connection connection, String secCompCode, String acctId ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " ACCTID = ? AND SECCOMPCODE = ?" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, acctId, secCompCode );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;

	}

	public BankSignData qryBankSignDataByIdTypeAndInvInvIdCode( Context context, Connection connection, String secCompCode, String idType, String idCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " SECCOMPCODE = ? AND IDTYPE = ? AND INVIDCODE =  ?" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, secCompCode, idType, idCode );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;

	}

	public BankSignData qryBankSignDataCheck( Context context, Connection connection, String secCompCode, String acctId, String bookNo ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " SECCOMPCODE = ? AND ACCTID = ? " );
			if( SFUtil.isNotEmpty( bookNo ) ) {
				buffer.append( " AND BOOKNO = " + bookNo );
			}
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, secCompCode, acctId );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	public BankSignData qryBankSignDataInfo( Context context, Connection connection, String secCompCode, String acctId, String curCode, String invName, String txDate, String txTime ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " SECCOMPCODE = ? AND ACCTID = ? AND CURCODE =  ? AND INVNAME = ? AND TXDATE = ? AND TXTIME = ?" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, secCompCode, acctId, curCode, invName, txDate, txTime );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;

	}

	/**
	 * (银行渠道发起) 预约查询
	 * @param context
	 * @param connection
	 * @param idType
	 * @param invIdCode18
	 * @param invIdCode15
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SFException
	 */
	public Page<BankSignData> qryBankSignDataBespeak( Context context, Connection connection, String startDate, String endDate, String idType, String invIdCode, String invIdCode18Card19, String invIdCode18Card20, Page<BankSignData> page ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT A.ACCTID AS acctId,A.BOOKNO AS bookNo,A.SECCOMPCODE AS secCompCode,B.SECCOMPNAME AS secCompName," );
			buffer.append( "A.TXDATE AS txDate,A.OPENDATE AS openDate,A.DELDATE AS delDate,A.SIGNFLAG AS signFlag" );
			buffer.append( " FROM TRDBANKSIGNDATA A, TRDSECCOMPDATA B" );
			buffer.append( " WHERE A.SECCOMPCODE = B.SECCOMPCODE" );
			buffer.append( " AND A.TXDATE BETWEEN ? AND ?" );
			buffer.append( " AND A.IDTYPE = ?" );
			buffer.append( " AND (A.INVIDCODE = ? OR A.INVIDCODE = ? OR A.INVIDCODE = ?)" );
			buffer.append( " ORDER BY TXDATE" );
			page = super.qryPageing( context, connection, buffer.toString(), BankSignData.class, page, startDate, endDate, idType, invIdCode, invIdCode18Card19, invIdCode18Card20 );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return page;
	}

	/**
	 * 
	 * 根据证件类型和签约状态查询预签约数量
	 * @param context
	 * @param connection
	 * @param idType
	 * @param invIdCode
	 * @param invIdCode18Card19
	 * @param invIdCode18Card20
	 * @return
	 * @throws SFException
	 */
	public long qryBankSignDataCountByInvIdCode( Context context, Connection connection, String idType, String invIdCode, String invIdCode18Card19, String invIdCode18Card20 ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(1) AS count FROM TRDBANKSIGNDATA A" );
			buffer.append( " WHERE A.IDTYPE = ?" );
			if( SFUtil.isNotEmpty( invIdCode18Card19 ) || SFUtil.isNotEmpty( invIdCode18Card20 ) ) {
				buffer.append( " AND (A.INVIDCODE = ? OR A.INVIDCODE = ? OR A.INVIDCODE = ?) AND SIGNFLAG IN ('2','7','9') " );
				count = super.qryCount( context, connection, buffer.toString(), idType, invIdCode, invIdCode18Card19, invIdCode18Card20 );
			} else {
				buffer.append( " AND A.INVIDCODE = ? AND A.SIGNFLAG IN ('2','7','9')" );
				count = super.qryCount( context, connection, buffer.toString(), idType, invIdCode );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 查询签约最新日期
	 * 检查投资人信息是否与银行预指定信息一致
	 * @param context
	 * @param connection
	 * @param invName
	 * @param secCompCode
	 * @param acctId
	 * @param curCode
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataMaxTxDate( Context context, Connection connection, String invName, String secCompCode, String acctId, String curCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT INVTYPE AS invType,ACCTID AS acctId,SAVACCT AS savAcct,ACCTCHLDNUM AS acctChldNum,CURCODE AS curCode," );
			buffer.append( "SECCOMPCODE AS secCompCode,PRODUCTTYPE AS productType,CAPACCT AS capAcct,INVNAME AS invName,IDTYPE AS idType," );
			buffer.append( "INVIDCODE AS invIdCode,SIGNFLAG AS signFlag,CUSMAGNO AS cusMagno,DEPID AS depId,BRANCHID AS branchId,BOOKNO AS bookNo," );
			buffer.append( "OPENDEPID AS openDepId,OPENBRANCHID AS openBranchId,UNITTELLERID AS unitTellerId,OPENDATE AS openDate,INITSIDE AS initSide," );
			buffer.append( "OPENTIME AS openTime,a.TXDATE AS txDate,a.TXTIME AS txTime,FLAGS AS flags,MEMO AS memo,SUBTXSEQID1 AS subTxSeqId1," );
			buffer.append( "SUBTXSEQID2 AS subTxSeqId2,SUBTXSEQID3 AS subTxSeqId3,DELDATE AS delDate,DELTIME AS delTime,CARDTYPE AS cardType," );
			buffer.append( "PHONE AS phone,MOBILE AS mobile,SECBRCHID AS secBrchId,CHANNEL AS channel,LMCARD AS lmCard,EMAIL AS email" );
			buffer.append( " FROM (SELECT MAX(TXDATE) AS TXDATE,MAX(TXTIME) AS TXTIME FROM TRDBANKSIGNDATA WHERE SECCOMPCODE=? AND ACCTID=? AND CURCODE=? AND INVNAME=?) a, TRDBANKSIGNDATA b " );
			buffer.append( " WHERE a.TXDATE = b.TXDATE AND a.TXTIME = b.TXTIME" );

			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, secCompCode, acctId, curCode, invName );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally{
			if(bankSignData!=null){
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 根据IdType+InvIdCode+SecCompCode+SignFlag in('0','1','2','5','6','9')查询
	 * @param context
	 * @param connection
	 * @param cardId
	 * @param secCompCode
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataByIdCodeAndIdType( Context context, Connection connection, String idType, String idCode, String secCompCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " IDTYPE = ? AND INVIDCODE = ? AND SECCOMPCODE=? AND SIGNFLAG IN ('0','1','2','5','6','9')" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, idType, idCode, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 根据主键+SignFlag in('0','1','2','5','6','9')查询
	 * @param context
	 * @param connection
	 * @param cardId
	 * @param secCompCode
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataBySignFlag( Context context, Connection connection, String cardId, String secCompCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " SECCOMPCODE=? AND ACCTID=? AND SIGNFLAG IN ('0','1','2','5','6','9')" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, secCompCode, cardId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 删除联名卡信息
	 * @param context
	 * @param connection
	 * @param bookId
	 * @param acctId
	 * @throws SFException
	 */
	public void delBankSignDataBySubTxSeqId( Context context, Connection connection, String AcctId, String txDate, String subTxSeqId ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer( "DELETE FROM TRDBANKSIGNDATA WHERE ACCTID=?" );
			buffer.append( " AND TXDATE=?" );
			buffer.append( " AND SUBTXSEQID1=?" );
			super.save( context, connection, buffer.toString(), AcctId, txDate, subTxSeqId );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 删除联名卡信息
	 * @param context
	 * @param connection
	 * @param bookId
	 * @param acctId
	 * @throws SFException
	 */
	public void delBankSignDataBySignFlag( Context context, Connection connection, String AcctId, String txDate, String subTxSeqId ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer( "DELETE FROM TRDBANKSIGNDATA WHERE ACCTID=?" );
			buffer.append( " AND TXDATE=?" );
			buffer.append( " AND SUBTXSEQID1=? AND SIGNFLAG = '7'" );
			super.save( context, connection, buffer.toString(), AcctId, txDate, subTxSeqId );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 根据主键删除联名卡信息
	 * @param context
	 * @param connection
	 * @param bookId
	 * @param acctId
	 * @throws SFException
	 */
	public void delBankSignData( Context context, Connection connection, String AcctId, String secCompCode ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer( "DELETE FROM TRDBANKSIGNDATA WHERE ACCTID=? AND SECCOMPCODE=?" );
			super.save( context, connection, buffer.toString(), AcctId, secCompCode );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 根据主键删除联名卡信息
	 * @param context
	 * @param connection
	 * @param bookId
	 * @param acctId
	 * @throws SFException
	 */
	public void delBankSignDataByBookNo( Context context, Connection connection, String acctId, String secCompCode, String bookNo ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer( "DELETE FROM TRDBANKSIGNDATA WHERE ACCTID=? AND SECCOMPCODE=? " );
			if( SFUtil.isNotEmpty( bookNo ) ) {
				buffer.append( " AND BOOKNO=" + bookNo );
			}
			super.save( context, connection, buffer.toString(), acctId, secCompCode );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 *200120开户预约成功后修改TrdBankSignData记录
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void updBankSignData( Context context, Connection connection, BankSignData bean ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();
			List<String> parmList = new ArrayList<String>();

			buffer.append( "UPDATE TRDBANKSIGNDATA SET " );
			if( SFUtil.isNotEmpty( bean.getSignFlag() ) ) {
				buffer.append( " SIGNFLAG= ?" );
				parmList.add( bean.getSignFlag() );
			}
			if( SFUtil.isNotEmpty( bean.getBookNo() ) ) {
				buffer.append( ",BOOKNO= ?" );
				parmList.add( bean.getBookNo() );
			}
			if( SFUtil.isNotEmpty( bean.getCapAcct() ) ) {
				buffer.append( ",CAPACCT= ?" );
				parmList.add( bean.getCapAcct() );
			}
			if( SFUtil.isNotEmpty( bean.getLmCard() ) ) {
				buffer.append( ",LMCARD= ?" );
				parmList.add( bean.getLmCard() );
			}
			if( SFUtil.isNotEmpty( bean.getEmail() ) ) {
				buffer.append( ",EMAIL= ?" );
				parmList.add( bean.getEmail() );
			}
			buffer.append( " WHERE ACCTID= ? AND TXDATE=? AND SUBTXSEQID1=?" );
			if( SFUtil.isNotEmpty( bean.getSignFlag() ) ) {
				buffer.append( " AND SIGNFLAG = '7'" );
			}

			parmList.add( bean.getAcctId() );
			parmList.add( bean.getTxDate() );
			parmList.add( bean.getSubTxSeqId1() );
			super.update( context, connection, buffer.toString(), parmList.toArray() );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 查询银行预指定日期TxDate+激活日期OpenDate
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataByAcctId( Context context, Connection connection, String acctId, String idType, String invIdCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT INVTYPE AS invType,ACCTID AS acctId,SAVACCT AS savAcct,ACCTCHLDNUM AS acctChldNum,CURCODE AS curCode," );
			buffer.append( "SECCOMPCODE AS secCompCode,PRODUCTTYPE AS productType,CAPACCT AS capAcct,INVNAME AS invName,IDTYPE AS idType," );
			buffer.append( "INVIDCODE AS invIdCode,SIGNFLAG AS signFlag,CUSMAGNO AS cusMagno,DEPID AS depId,BRANCHID AS branchId,BOOKNO AS bookNo," );
			buffer.append( "OPENDEPID AS openDepId,OPENBRANCHID AS openBranchId,UNITTELLERID AS unitTellerId,OPENDATE AS openDate,INITSIDE AS initSide," );
			buffer.append( "OPENTIME AS openTime,TXDATE AS txDate,TXTIME AS txTime,FLAGS AS flags,MEMO AS memo,SUBTXSEQID1 AS subTxSeqId1," );
			buffer.append( "SUBTXSEQID2 AS subTxSeqId2,SUBTXSEQID3 AS subTxSeqId3,DELDATE AS delDate,DELTIME AS delTime,CARDTYPE AS cardType," );
			buffer.append( "PHONE AS phone,MOBILE AS mobile,SECBRCHID AS secBrchId,CHANNEL AS channel,LMCARD AS lmCard,EMAIL AS email" );
			buffer.append( " FROM TRDBANKSIGNDATAHIS WHERE " );
			buffer.append( " PRODUCTTYPE='03' AND TRIM(ACCTID)=? " );
			buffer.append( " AND SIGNFLAG  IN ('0','8','9') AND ROWNUM = 1 " );
			if( SFUtil.isNotEmpty( idType ) && SFUtil.isNotEmpty( invIdCode ) ) {
				buffer.append( "  AND TRIM(IDTYPE)=? AND TRIM(INVIDCODE)=?" );
				bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, acctId, idType, invIdCode );
			} else {
				bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, acctId );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 检查是否存在预约记录
	 * @param context
	 * @param connection
	 * @param AcctId
	 * @param secCompCode
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataChkInfo( Context context, Connection connection, String capAcct, String secCompCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " CAPACCT=? AND SECCOMPCODE=?" );
			buffer.append( " AND SIGNFLAG in ('2','7','9')" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	public long qryBankSignDataChkInfoBySignFlag( Context context, Connection connection, String capAcct, String secCompCode ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(ACCTID) AS COUNT FROM TRDBANKSIGNDATA WHERE " );
			buffer.append( " CAPACCT=? AND SECCOMPCODE=?" );
			buffer.append( " AND SIGNFLAG = '2'" );
			count = super.qryCount( context, connection, buffer.toString(), capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 检查账号是否银行预指定卡号
	 * @param context
	 * @param connection
	 * @param acctId
	 * @param curCode
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataChkNewAcctId( Context context, Connection connection, String acctId, String curCode ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();
			buffer.append( " ACCTID = ? AND CURCODE = ? AND SIGNFLAG in ('2','7','9')" );
			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, acctId, curCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 撤销不发券商,修改BankSignData数据
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updBankSignDataBySignFlag( Context context, Connection connection, BankSignData bean ) throws SFException {
		int count = 0;
		try {

			StringBuffer buffer = new StringBuffer( "UPDATE TRDBANKSIGNDATA SET SIGNFLAG='8'" );
			buffer.append( ",DELDATE=?,DELTIME=?,SUBTXSEQID3=?" );
			buffer.append( " WHERE ACCTID=? AND SECCOMPCODE=? AND SIGNFLAG IN ('2', '7', '9')" );
			if( SFUtil.isNotEmpty( bean.getBookNo() ) ) {
				buffer.append( " AND BOOKNO=" + bean.getBookNo() );
			}
			count = super.save( context, connection, buffer.toString(), bean.getDelDate(), bean.getDelTime(), bean.getSubTxSeqId3(), bean.getAcctId(), bean.getSecCompCode() );

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
	 * 发券商,修改BankSignData数据
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updBankSignDataSignFlagToSendMSG( Context context, Connection connection, BankSignData bean ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer( "UPDATE TRDBANKSIGNDATA SET SIGNFLAG='9'" );
			buffer.append( ",DELDATE=?,SUBTXSEQID3=?" );
			buffer.append( " WHERE ACCTID=? AND SECCOMPCODE=?" );

			if( SFUtil.isNotEmpty( bean.getBookNo() ) ) {
				buffer.append( " AND BOOKNO = " + bean.getBookNo() );
			}
			count = super.save( context, connection, buffer.toString(), bean.getDelDate(), bean.getSubTxSeqId3(), bean.getAcctId(), bean.getSecCompCode() );

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
	 * 查询银行预指定信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public BankSignData qryBankSignDataToHisTory( Context context, Connection connection, String AcctId, String secCompCode, String bookNo ) throws SFException {
		BankSignData bankSignData = null;
		try {
			StringBuffer buffer = getQryBankSignDataSQLStruct();

			buffer.append( "  ACCTID = ? AND SECCOMPCODE = ? AND BOOKNO = ?" );

			bankSignData = super.qry( context, connection, buffer.toString(), BankSignData.class, AcctId, secCompCode, bookNo );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( bankSignData != null ) {
				bankSignData.resetChangedFlag();
			}
		}
		return bankSignData;
	}

	/**
	 * 回滚银行预指定信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @param delDate
	 * @throws SFException
	 */
	public int updBankSignDataBySignFlagToRollback( Context context, Connection connection, BankSignData bean ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer( "UPDATE TRDBANKSIGNDATA SET " );
			buffer.append( "SECCOMPCODE=?,DELDATE=?,SIGNFLAG=?" );
			if( SFUtil.isEmpty( bean.getDelTime() ) ) {
				buffer.append( ",DELTIME=''" );
			} else {
				buffer.append( ",DELTIME=" + bean.getDelTime() );
			}

			buffer.append( " WHERE ACCTID=? AND SECCOMPCODE=? AND DELDATE = ? AND SUBTXSEQID3 = ?" );
			if( SFUtil.isNotEmpty( bean.getBookNo() ) ) {
				buffer.append( " AND BOOKNO=" + bean.getBookNo() );
			}
			count = super.save( context, connection, buffer.toString(), bean.getSecCompCode(), bean.getDelDate(), bean.getSignFlag(), bean.getAcctId(), bean.getSecCompCode(), bean.getDelDate(), bean.getSubTxSeqId3() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * 
	 * 根据证件类型证件号码查询签约总数
	 * @param context
	 * @param connection
	 * @param idType
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public long qryBankSignDataCountByInvIdCode( Context context, Connection connection, String idType, String invIdCode ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(1) FROM TRDBANKSIGNDATA A" );
			buffer.append( " WHERE A.IDTYPE = ? AND  A.INVIDCODE =? " );
			count = super.qryCount( context, connection, buffer.toString(), idType, invIdCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}
}
