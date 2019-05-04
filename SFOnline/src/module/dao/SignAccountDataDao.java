package module.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import module.bean.SignAccountData;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.bean.SQLStruct;
import common.sql.dao.DaoBase;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;
import core.log.SFLogger;

/**
 * �ͻ�ǩԼ��Ϣ��Dao
 * 
 * @author ��˫��
 * 
 */
public class SignAccountDataDao extends DaoBase {

	private static final String CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB = "TRDSIGNACCOUNTDATA";

	private static final String CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB = "TRDSIGNACCOUNTDATA_FB";

	/**
	 * ��ȡǩԼ����ѯSQL���
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private StringBuffer getQrySignAccountDataSQLStruct( Context context ) throws SFException {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "SELECT O.INITSIDE AS initSide,O.SECACCT AS secAcct,O.SECACCTSEQ AS secAcctSeq,O.SECCOMPCODE AS secCompCode,O.PRODUCTTYPE AS productType," );
		buffer.append( "O.CAPACCT AS capAcct,O.CURCODE AS curCode,O.CASHREMITFLAG AS cashRemitFlag,O.SHSTHCODE AS shsthCode,O.SZSTHCODE AS szsthCode," );
		buffer.append( "O.ACCTID AS acctId,O.SAVACCT AS savAcct,O.ACCTCHLDNUM AS acctChldNum,O.OPENDEPID AS openDepId,O.OPENBRANCHID AS openBranchId," );
		buffer.append( "O.INVNAME AS invName,O.INVTYPE AS invType,O.SIGNFLAG AS signFlag,O.DEPID AS depId,O.BRANCHID AS branchId,O.DESDEPID AS desDepId," );
		buffer.append( "O.DESBRANCHID AS desBranchId,O.UNITTELLERID AS unitTellerId,O.DESUNITTELLERID AS desUnitTellerId,O.CUSMAGNO AS cusMagno,O.STATFLAG AS statFlag," );
		buffer.append( "O.OPENDATE AS openDate,O.CLOSEDATE AS closeDate,O.PRETXDATE AS preTxDate,O.BEGINBAL AS beginBal,O.ACCTBAL AS acctBal,O.ISMAILBILL AS isMailBill," );
		buffer.append( "O.MAILDATE AS mailDate,O.FLAGS AS flags,O.SIGNDATE AS signDate,O.ACTIVEFLAG AS activeFlag,O.CHANNEL AS channel,O.TRANSFERFLAG AS transferFlag," );
		buffer.append( "O.SIGNMODE AS signMode,O.LMCARD AS lmCard,O.IIACCTCTL AS iiAcctCtl FROM " );

		// �Ƿ�����ʱ��α�ʶ
		String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
		if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
			buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " O" );
		} else {
			buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " O" );
		}
		return buffer;
	}

	/**
	 * ����ͻ�ǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public int saveSignAccountData( Context context, Connection connection, SignAccountData signAccountData ) throws SFException {
		int count = 0;
		String tableName = null;
		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( signAccountData.getSecCompCode() ), "ST4895", "��Ҫ����[SECCOMPCODE]û���ṩ" );
			SFUtil.chkCond( context, SFUtil.isEmpty( signAccountData.getCapAcct() ), "ST4895", "��Ҫ����[CAPACCT]û���ṩ" );

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				tableName = CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB;
			} else {
				tableName = CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB;
			}
			count = super.save( context, connection, signAccountData.getSaveSignAcctDataSQLStruct( tableName ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return count;
	}

	/**
	 * ǩԼ�ʺż�¼-����Ǩ��
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public int migrateSignAccountData( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "INSERT INTO TRDDESSIGNDATA (INITSIDE, SECACCT, SECACCTSEQ, SECCOMPCODE, PRODUCTTYPE, CAPACCT, CURCODE, CASHREMITFLAG," );
			buffer.append( " SHSTHCODE, SZSTHCODE, ACCTID, SAVACCT, ACCTCHLDNUM, OPENDEPID, OPENBRANCHID,INVNAME, INVTYPE, SIGNFLAG, DEPID, BRANCHID, DESDEPID, DESBRANCHID," );
			buffer.append( " UNITTELLERID, DESUNITTELLERID,CUSMAGNO, OPENDATE, CLOSEDATE, PRETXDATE, BEGINBAL, ACCTBAL, ISMAILBILL, MAILDATE, FLAGS )" );
			buffer.append( " SELECT INITSIDE, SECACCT, SECACCTSEQ, SECCOMPCODE, PRODUCTTYPE, CAPACCT, CURCODE, CASHREMITFLAG,SHSTHCODE, SZSTHCODE, ACCTID," );
			buffer.append( " SAVACCT, ACCTCHLDNUM, OPENDEPID, OPENBRANCHID,INVNAME, INVTYPE, SIGNFLAG, DEPID, BRANCHID, DESDEPID, DESBRANCHID, UNITTELLERID, DESUNITTELLERID," );
			buffer.append( " CUSMAGNO, OPENDATE, CLOSEDATE, PRETXDATE, BEGINBAL, ACCTBAL, ISMAILBILL, MAILDATE, FLAGS FROM " );
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECCOMPCODE=? AND CAPACCT=? AND PRODUCTTYPE='03' AND CURCODE='RMB' " );
			Object[] values = new Object[ 2 ];
			values[ 0 ] = secCompCode;
			values[ 1 ] = capAcct;
			SQLStruct sqlStruct = new SQLStruct( buffer.toString(), values );
			count = super.save( context, connection, sqlStruct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ��������¼�Ƶ� TRDDesSignData ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public int migrateSignAccountDataBySignFlag( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "INSERT INTO TRDDESSIGNDATA (INITSIDE, SECACCT, SECACCTSEQ, SECCOMPCODE, PRODUCTTYPE, CAPACCT, CURCODE, CASHREMITFLAG," );
			buffer.append( " SHSTHCODE, SZSTHCODE, ACCTID, SAVACCT, ACCTCHLDNUM, OPENDEPID, OPENBRANCHID,INVNAME, INVTYPE, SIGNFLAG, DEPID, BRANCHID, DESDEPID, DESBRANCHID," );
			buffer.append( " UNITTELLERID, DESUNITTELLERID,CUSMAGNO, OPENDATE, CLOSEDATE, PRETXDATE, BEGINBAL, ACCTBAL, ISMAILBILL, MAILDATE, FLAGS )" );
			buffer.append( " SELECT INITSIDE, SECACCT, SECACCTSEQ, SECCOMPCODE, PRODUCTTYPE, CAPACCT, CURCODE, CASHREMITFLAG,SHSTHCODE, SZSTHCODE, ACCTID," );
			buffer.append( " SAVACCT, ACCTCHLDNUM, OPENDEPID, OPENBRANCHID,INVNAME, INVTYPE, SIGNFLAG, DEPID, BRANCHID, DESDEPID, DESBRANCHID, UNITTELLERID, DESUNITTELLERID," );
			buffer.append( " CUSMAGNO, OPENDATE, CLOSEDATE, PRETXDATE, BEGINBAL, ACCTBAL, ISMAILBILL, MAILDATE, FLAGS  FROM " );
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECCOMPCODE=? AND CAPACCT=? AND PRODUCTTYPE='03' AND CURCODE='RMB' AND SignFlag='4'" );
			Object[] values = new Object[ 2 ];
			values[ 0 ] = secCompCode;
			values[ 1 ] = capAcct;
			SQLStruct sqlStruct = new SQLStruct( buffer.toString(), values );
			count = super.save( context, connection, sqlStruct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ��������ɾ���ͻ�ǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @throws SFException
	 */
	public void delSignAccountData( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			buffer.append( "DELETE FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECCOMPCODE=? AND CAPACCT=? AND PRODUCTTYPE='03' AND CURCODE='RMB'" );
			super.save( context, connection, buffer.toString(), secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ��������+SIGNFLAG='4'ɾ��ǩԼ��¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @throws SFException
	 */
	public void delSignAccountDataBySignFlag( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			buffer.append( "DELETE FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECCOMPCODE=? AND CAPACCT=? AND PRODUCTTYPE='03' AND CURCODE='RMB' AND SIGNFLAG='4'" );
			super.save( context, connection, buffer.toString(), secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ��������+ACCTID��SIGNFLAG='4'ɾ��ǩԼ��¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public int delSignAccountByAcctId( Context context, Connection connection, String secCompCode, String capAcct, String acctId ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			buffer.append( "DELETE FROM " );
			// ����ʱ��β�������
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {// ����ʱ��β�������
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECCOMPCODE=? AND PRODUCTTYPE='03' AND CAPACCT=? AND CURCODE='RMB' AND ACCTID=? AND SIGNFLAG='4'" );

			count = super.save( context, connection, buffer.toString(), secCompCode, capAcct, acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ��ѯ��������Ϊ�յļ�¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataByLmcard( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		List<SignAccountData> list = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE SECCOMPCODE = ? AND PRODUCTTYPE = '03' AND CAPACCT = ? AND CURCODE = 'RMB'" );
			buffer.append( " AND LMCARD IS NOT NULL" );

			Object[] param = new Object[ 2 ];
			param[ 0 ] = secCompCode;
			param[ 1 ] = capAcct;
			list = super.qryForOList( context, connection, buffer.toString(), param, SignAccountData.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally {
			if( list.size() > 0 ) {
				for( SignAccountData signAccountData : list ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}
		return list;
	}

	/**
	 * ��ѯ�Ͽ��Ƿ񻹴���ǩԼ��Ϣ
	 * @param context
	 * @param connection
	 * @param acctId
	 * @return
	 * @throws SFException
	 */
	public long qrySignAccountDataByOldAcctIdCount( Context context, Connection connection, String acctId, String secCompCode, String capAcct ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( " SELECT COUNT(*)FROM TRDSignAccountData  WHERE AcctId = ? AND SecCompCode = ? AND Capacct = ? AND  SignFlag != '4'" );
			count = super.qryCount( context, connection, buffer.toString(), acctId, secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ����������ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountData( Context context, Connection connection, String capAcct, String secCompCode, boolean isUpdate ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE  O.CAPACCT=? AND O.SECCOMPCODE=? AND O.CURCODE = 'RMB' AND O.PRODUCTTYPE = '03'" );
			if( isUpdate ) {
				buffer.append( " FOR UPDATE" );
			}
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��������+AcctId��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param capAcct
	 * @param secCompCode
	 * @param acctId
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByAcctId( Context context, Connection connection, String capAcct, String secCompCode, String acctId ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE  O.CAPACCT=? AND O.SECCOMPCODE=? AND O.ACCTID = ?" );

			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, capAcct, secCompCode, acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��������+SignFlag='4'+ClostDate��ѯǩԼ��¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByCloseDate( Context context, Connection connection, String secCompCode, String capAcct, String closeDate ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE ( O.SECCOMPCODE=? AND O.PRODUCTTYPE='03' AND O.CAPACCT=?)AND O.CURCODE='RMB' AND O.SIGNFLAG='4' AND O.CLOSEDATE=?" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secCompCode, capAcct, closeDate );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��������+AcctId+SignFlag='4'+AcctBal>0��ѯǩԼ��¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByAcctIdAndAcctBal( Context context, Connection connection, String secCompCode, String capAcct, String acctId ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE O.SECCOMPCODE=? AND O.PRODUCTTYPE='03' AND O.CAPACCT=? AND O.ACCTID=? AND O.CURCODE='RMB' AND O.SIGNFLAG='4' AND O.ACCTBAL>0" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secCompCode, capAcct, acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����AcctBal��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByAcctBal( Context context, Connection connection, String secCompCode, String capAcct, String acctId ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE (O.SECCOMPCODE=? AND O.PRODUCTTYPE='03' AND O.CAPACCT=? OR O.ACCTID=? ) AND O.CURCODE='RMB' AND O.SIGNFLAG='4' AND O.ACCTBAL>0" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secCompCode, capAcct, acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��ѯ���SECACCTSEQ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataMaxSeqBySecAcct( Context context, Connection connection, String secAcct ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT NVL(MAX(SECACCTSEQ),0) AS SECACCTSEQ FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECACCT=? AND CURCODE='RMB'" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����capAcct��secCompCode SignFlag='0'��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataBySignFlag( Context context, Connection connection, String capAcct, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE O.SIGNFLAG = '0' AND O.SECACCT=? AND O.SECCOMPCODE=?" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����capAcct��secCompCode SignFlag='0'��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByacctId( Context context, Connection connection, String acctId, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE O.SIGNFLAG = '0' AND O.ACCTID =? AND O.SECCOMPCODE=?" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����ACCTID��SECCOMPCODE��SIGNFLAG = '0'��ѯǩԼ�б�
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataListByAcctId( Context context, Connection connection, String acctId, String secCompCode ) throws SFException {
		List<SignAccountData> signList = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			Object[] param = null;
			if( SFUtil.isNotEmpty( acctId ) && SFUtil.isNotEmpty( secCompCode ) ) {
				buffer.append( " WHERE O.ACCTID = ? AND O.SECCOMPCODE = ? AND O.SIGNFLAG = '0'" );
				param = new Object[ 2 ];
				param[ 0 ] = acctId;
				param[ 1 ] = secCompCode;
				signList = super.qryForOList( context, connection, buffer.toString(), param, SignAccountData.class );
			} else if( SFUtil.isNotEmpty( acctId ) ) {
				buffer.append( " WHERE O.ACCTID = ? AND O.SIGNFLAG = '0'" );
				param = new Object[ 1 ];
				param[ 0 ] = acctId;
				signList = super.qryForOList( context, connection, buffer.toString(), param, SignAccountData.class );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if(null != signList && signList.size() > 0 ) {
				for( SignAccountData signAccountData : signList ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}
		return signList;
	}

	/**
	 * ����AcctId SIGNFLAG��ѯǩԼ���ݼ���
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataListBySignFlag( Context context, Connection connection, String acctId ) throws SFException {
		List<SignAccountData> signList = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE O.ACCTID = ? AND O.SIGNFLAG <> '4'" );
			Object[] param = new Object[ 1 ];
			param[ 0 ] = acctId;
			signList = super.qryForOList( context, connection, buffer.toString(), param, SignAccountData.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData signAccountData : signList ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}
		return signList;
	}

	/**
	 * ����SignAccountData��InvestData��ѯ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAcctDataAndInvestDateBySignFlag( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		SignAccountData signAccountData = null;
		Map<String, Object> map = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT B.INVNAME, B.IDTYPE, B.INVIDCODE, B.SECACCT," );
			buffer.append( "NVL(LEGALNAME,' ') AS LEGALNAME, NVL(LEGALIDCODE,' ') AS LEGALIDCODE,NVL(TRNNAME,' ') AS TRNNAME," );
			buffer.append( " NVL(TRNIDCODE,' ') AS TRNIDCODE, NVL(TRNMOBILE,' ') AS TRNMOBILE," );
			buffer.append( "A.SECACCTSEQ, A.ACCTID, A.OPENDEPID, A.OPENBRANCHID, A.SIGNFLAG,NVL(A.FLAGS,' ') AS FLAGS, A.INITSIDE FROM " );

			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " A,TRDINVESTDATA B" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " A,TRDINVESTDATA B" );
			}
			buffer.append( " WHERE  B.SECACCT=A.SECACCT AND A.SECCOMPCODE=? AND A.CAPACCT=? AND A.PRODUCTTYPE='03'" );
			buffer.append( " AND A.SIGNFLAG IN ('0', '3', '6', '9') AND A.INVTYPE = B.INVTYPE" );

			map = super.qryMap( context, connection, buffer.toString(), secCompCode, capAcct );
			if( map != null ) {
				signAccountData = new SignAccountData();
				signAccountData.getInvestData().setInvName( SFUtil.objectToString( map.get( "INVNAME" ) ) );
				signAccountData.getInvestData().setIdType( SFUtil.objectToString( map.get( "IDTYPE" ) ) );
				signAccountData.getInvestData().setInvIdCode( SFUtil.objectToString( map.get( "INVIDCODE" ) ) );
				signAccountData.getInvestData().setSecAcct( SFUtil.objectToString( map.get( "SECACCT" ) ) );
				signAccountData.getInvestData().setLegalName( SFUtil.objectToString( map.get( "LEGALNAME" ) ) );
				signAccountData.getInvestData().setLegalIdCode( SFUtil.objectToString( map.get( "LEGALIDCODE" ) ) );
				signAccountData.getInvestData().setTrnName( SFUtil.objectToString( map.get( "TRNNAME" ) ) );
				signAccountData.getInvestData().setTrnIdCode( SFUtil.objectToString( map.get( "TRNIDCODE" ) ) );
				signAccountData.getInvestData().setTrnMobile( SFUtil.objectToString( map.get( "TRNMOBILE" ) ) );
				signAccountData.setSecAcctSeq( Integer.valueOf( SFUtil.objectToString( map.get( "SECACCTSEQ" ) ) ) );
				signAccountData.setAcctId( SFUtil.objectToString( map.get( "ACCTID" ) ) );
				signAccountData.setOpenDepId( SFUtil.objectToString( map.get( "OPENDEPID" ) ) );
				signAccountData.setOpenBranchId( SFUtil.objectToString( map.get( "OPENBRANCHID" ) ) );
				signAccountData.setSignFlag( SFUtil.objectToString( map.get( "SIGNFLAG" ) ) );
				signAccountData.setFlags( SFUtil.objectToString( map.get( "FLAGS" ) ) );
				signAccountData.setInitSide( SFUtil.objectToString( map.get( "INITSIDE" ) ) );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * �����ѯ��������ѯ TRDSignAccountData��TRDSecCompData��TRDInvestData
	 * һ��ͨ��ѯǩԼ��ϵ
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataAndSecInv( Context context, Connection connection, String acctId, String invType, String invIdCode, String invIdCode18Card19, String invIdCode18Card20 ) throws SFException {

		List<SignAccountData> signList = null;
		try {

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT A.ACCTID,A.INVNAME,A.CURCODE,D.IDTYPE,D.INVIDCODE,A.CAPACCT,A.INVTYPE,B.SECCOMPCODE,B.SECCOMPNAME,A.SIGNFLAG,A.CHANNEL,A.CUSMAGNO,A.DEPID,A.OPENDATE,A.SIGNDATE,A.BEGINBAL,A.ACCTBAL FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " A, TRDSECCOMPDATA B, TRDINVESTDATA D" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " A, TRDSECCOMPDATA B, TRDINVESTDATA D" );
			}
			buffer.append( " WHERE A.SECCOMPCODE = B.SECCOMPCODE AND A.SECACCT = D.SECACCT" );
			if( SFUtil.isNotEmpty( acctId ) ) {
				buffer.append( " AND A.ACCTID=?" );// �����Ų�ѯ
				Object[] obj = { acctId };
				signList = super.qryForOList( context, connection, buffer.toString(), obj, SignAccountData.class );
			} else {
				buffer.append( " AND D.IDTYPE=? AND (D.INVIDCODE=? OR D.INVIDCODE=? OR D.INVIDCODE=?)" );// ��֤������+֤�������ѯ
				Object[] obj = new Object[ 4 ];
				obj[ 0 ] = invType;
				obj[ 1 ] = invIdCode;
				obj[ 2 ] = invIdCode18Card19;
				obj[ 3 ] = invIdCode18Card20;
				signList = super.qryForOList( context, connection, buffer.toString(), obj, SignAccountData.class );
			}
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData signAccountData : signList ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}

		return signList;
	}

	/**
	 * ����capAcct��secCompCode SignFlag!='4'��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataCountBySignFlag( Context context, Connection connection, String capAcct, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE O.SIGNFLAG != '4' AND O.CAPACCT=? AND O.SECCOMPCODE=?" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ���½������ڣ��˻����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @throws SFException
	 */
	public void updSignAccountData( Context context, Connection connection, String secCompCode, String capAcct, BigDecimal txAmount ) throws SFException {
		try {
			// String secCompCode = (String) SFUtil.getReqDataValue(context, "SEC_COMP_CODE");
			// String capAcct = (String) SFUtil.getReqDataValue(context, "CAP_ACCT");

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "UPDATE " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " SET PRETXDATE=?, ACCTBAL=ACCTBAL+? WHERE SECCOMPCODE=? AND CAPACCT=? " );
			super.save( context, connection, buffer.toString(), DateUtil.getMacDate(), txAmount, secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, String.format( "UpdateBalFailure[%s]",e.getMessage() ) );
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ���ݿ��Ž���/ȡ����״̬��
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public void updSignAccountDataByAcctId( Context context, Connection connection, String acctId, String statFlag ) throws SFException {
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "UPDATE " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}

			if( "1".equals( statFlag ) ) {// ������״̬��
				buffer.append( " SET STATFLAG='1' WHERE ACCTID=? AND SIGNFLAG='0'" );
			} else if( "0".equals( statFlag ) ) {// ȡ����״̬��
				buffer.append( " SET STATFLAG='0' WHERE ACCTID=? AND SIGNFLAG<>'0'" );
			}
			super.save( context, connection, buffer.toString(), acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ����SecAcct���¿ͻ�����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @throws SFException
	 */
	public void updSignAccoutDataBySignFlag( Context context, Connection connection, String invName, String secAcct ) throws SFException {
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "UPDATE " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
				buffer.append( " SET INVNAME=? WHERE SECACCT=? AND SIGNFLAG <>'4'" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
				buffer.append( " SET INVNAME=? WHERE SECACCT=? AND SIGNFLAG <>'4'" );
			}
			super.save( context, connection, buffer.toString(), invName, secAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ����SignAcctData�� SecCompData��ѯ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return ���ݼ���
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAcctountDataListBySecAcct( Context context, Connection connection, String secAcct ) throws SFException {
		StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
		List<SignAccountData> signList = null;
		try {
			buffer.append( " ,TRDSECCOMPDATA T" );
			buffer.append( " WHERE O.SECCOMPCODE = T.SECCOMPCODE AND O.SECACCT=? " );
			Object[] obj = { secAcct };
			signList = super.qryForOList( context, connection, buffer.toString(), obj, SignAccountData.class );
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData signAccountData : signList ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}
		return signList;
	}

	/**
	 * ����SignAcctData�� SecCompData��ѯ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return �������ݶ���
	 * @throws SFException
	 */
	public SignAccountData qrySignAcctountDataByAcctId( Context context, Connection connection, String acctId, String secCompCode, String capAcct ) throws SFException {

		StringBuffer buffer = new StringBuffer();
		SignAccountData signAccountData = null;
		try {
			signAccountData = new SignAccountData();
			Map<String, Object> map = null;
			buffer.append( "SELECT A.SECCOMPCODE, A.USERID, NVL(A.SZTFLAG,'0') AS SZTFLAG, NVL(A.IP,' ') AS IP, NVL(A.PORT,' ') AS PORT,B.SIGNFLAG,B.CAPACCT,NVL(B.FLAGS,' ') AS FLAGS,NVL(B.DEPID,' ') AS DEPID,NVL(B.BRANCHID,' ') AS BRANCHID FROM " );
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " B,TRDSECCOMPDATA A" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " B,TRDSECCOMPDATA A" );
			}
			buffer.append( " WHERE B.ACCTID=?  AND A.SECCOMPCODE=B.SECCOMPCODE AND B.SECCOMPCODE=? AND B.CAPACCT=? " );
			map = super.qryMap( context, connection, buffer.toString(), acctId, secCompCode, capAcct );
			if( map != null ) {
				signAccountData.getSecCompData().setSecCompCode( SFUtil.objectToString( map.get( "SECCOMPCODE" ) ) );
				signAccountData.setSecCompCode( SFUtil.objectToString( map.get( "SECCOMPCODE" ) ) );
				signAccountData.getSecCompData().setUserId( SFUtil.objectToString( map.get( "USERID" ) ) );
				signAccountData.getSecCompData().setSztFlag( SFUtil.objectToString( map.get( "SZTFLAG" ) ) );
				signAccountData.getSecCompData().setIp( SFUtil.objectToString( map.get( "IP" ) ) );
				signAccountData.getSecCompData().setPort( SFUtil.objectToString( map.get( "PORT" ) ) );
				signAccountData.setSignFlag( SFUtil.objectToString( map.get( "SIGNFLAG" ) ) );
				signAccountData.setCapAcct( SFUtil.objectToString( map.get( "CAPACCT" ) ) );
				signAccountData.setFlags( SFUtil.objectToString( map.get( "FLAGS" ) ) );
				signAccountData.setDepId( SFUtil.objectToString( map.get( "DEPID" ) ) );
				signAccountData.setBranchId( SFUtil.objectToString( map.get( "BRANCHID" ) ) );
			}
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}

		return signAccountData;
	}

	/**
	 * ����SignAccountData�� SecCompData��InvestData��ѯǩԼ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return ���ݼ���
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataListBySecAcct( Context context, Connection connection, String secAcct ) throws SFException {
		StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
		List<SignAccountData> signList = null;
		try {
			buffer.append( " ,TRDSECCOMPDATA T,TRDINVESTDATA D" );
			buffer.append( " WHERE O.SECCOMPCODE = T.SECCOMPCODE AND O.SECACCT=D.SECACCT AND O.SECACCT=? " );
			Object[] obj = { secAcct };
			signList = super.qryForOList( context, connection, buffer.toString(), obj, SignAccountData.class );
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData signAccountData : signList ) {
					if( signAccountData != null ) {
						signAccountData.resetChangedFlag();
					}
				}
			}
		}

		return signList;
	}

	/**
	 * ����������ѯ InvestData SignAccountData SecCompData
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @param searchFalg
	 *            ��ѯ��ʶ
	 * @return ���ݼ���
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountDataListBySearchFlag( Context context, Connection connection, String searchNum, String searchFlag ) throws SFException {
		SignAccountData signAccountData =null;
		List<SignAccountData> signList = new ArrayList<SignAccountData>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		// �Ƿ�����ʱ��α�ʶ
		try {
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT A.INVNAME, A.IDTYPE, A.INVIDCODE, A.INVTYPE, NVL(A.PHONE,' ') AS PHONE, NVL(A.MOBILE,' ') AS MOBILE, NVL(A.ADDR,' ') AS ADDR, NVL(A.ZIP,' ') AS ZIP, NVL(A.EMAILADDR,' ') AS EMAILADDR, B.CAPACCT,B.SIGNFLAG, C.SECCOMPNAME, NVL(B.ACCTID,' ') AS ACCTID, B.CURCODE, B.SECCOMPCODE FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " B, TRDINVESTDATA A, TRDSECCOMPDATA C" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " B, TRDINVESTDATA A, TRDSECCOMPDATA C" );
			}
			buffer.append( " WHERE  B.SECACCT = A.SECACCT AND C.SECCOMPCODE = B.SECCOMPCODE " );

			if( "2".equals( searchFlag ) ) {// ֤ȯ�ʽ��˺�������ѯ
				buffer.append( " AND B.CAPACCT = ?" );
			} else if( "3".equals( searchFlag ) ) {// ǩԼ�����ʺ�������ѯ
				buffer.append( " AND B.ACCTID = ?" );
			} else if( "4".equals( searchFlag ) ) {// ����֤������������ѯ
				buffer.append( " AND A.INVIDCODE = ? " );
			}
			list = super.qryListMap( context, connection, buffer.toString(), searchNum );
			for( int i = 0; i < list.size(); i++ ) {
				map = list.get( i );
				signAccountData = new SignAccountData();
				signAccountData.getInvestData().setInvName( SFUtil.objectToString( map.get( "INVNAME" ) ) );
				signAccountData.getInvestData().setIdType( SFUtil.objectToString( map.get( "IDTYPE" ) ) );
				signAccountData.getInvestData().setInvIdCode( SFUtil.objectToString( map.get( "INVIDCODE" ) ) );
				signAccountData.getInvestData().setInvType( SFUtil.objectToString( map.get( "INVTYPE" ) ) );
				signAccountData.getInvestData().setPhone( SFUtil.objectToString( map.get( "PHONE" ) ) );
				signAccountData.getInvestData().setMobile( SFUtil.objectToString( map.get( "MOBILE" ) ) );
				signAccountData.getInvestData().setAddr( SFUtil.objectToString( map.get( "ADDR" ) ) );
				signAccountData.getInvestData().setZip( SFUtil.objectToString( map.get( "ZIP" ) ) );
				signAccountData.getInvestData().setEmailAddr( SFUtil.objectToString( map.get( "EMAILADDR" ) ) );
				signAccountData.setCapAcct( SFUtil.objectToString( map.get( "CAPACCT" ) ) );
				signAccountData.setSignFlag( SFUtil.objectToString( map.get( "SIGNFLAG" ) ) );
				signAccountData.getSecCompData().setSecCompName( SFUtil.objectToString( map.get( "SECCOMPNAME" ) ) );
				signAccountData.setAcctId( SFUtil.objectToString( map.get( "ACCTID" ) ) );
				signAccountData.setCurCode( SFUtil.objectToString( map.get( "CURCODE" ) ) );
				signAccountData.setSecCompCode( SFUtil.objectToString( map.get( "SECCOMPCODE" ) ) );
				signList.add( signAccountData );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData sign : signList ) {
					if( sign != null ) {
						sign.resetChangedFlag();
					}
				}
			}
		}
		return signList;
	}

	/**
	 * ����ACCTID��ѯ�����Ƿ�ǩԼ��ϵ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByAcctId( Context context, Connection connection, String acctId ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE ACCTID=? AND ROWNUM=1" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����CapAcct��ѯ�Ƿ�ǩԼ��ϵ
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByCapAcct( Context context, Connection connection, String capAcct ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT SECACCT, SECCOMPCODE, CAPACCT, SAVACCT FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE CAPACCT=? AND ROWNUM=1" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��ѯ�Ƿ�ǩԼ��ϵ
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataInfo( Context context, Connection connection, String acctId, String capAcct, String secCompCode, String curCode, String invType ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT SECACCT, INVNAME, ACCTBAL, NVL(FLAGS,' ') AS FLAGS FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			if( SFConst.INV_TYPE_CORP.equals( invType ) ) {// �Թ�
				buffer.append( " WHERE AcctId =? and SignFlag='0'" );
				buffer.append( " AND CAPACCT=? AND SECCOMPCODE=? AND PRODUCTTYPE='03'" );
				signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId, capAcct, secCompCode );
			} else { // ���۸���
				buffer.append( " WHERE AcctId =? and SignFlag='0'" );
				buffer.append( " AND CAPACCT=? AND SECCOMPCODE=? AND CURCODE=?" );
				signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId, capAcct, secCompCode, curCode );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ��ȡ�ͻ��Ŀ���������Ϣ
	 * @param context
	 * @param connection
	 * @param acctId
	 * @param curCode
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataByBranch( Context context, Connection connection, String acctId, String curCode, String capAcct, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE ACCTID=? AND CURCODE = ? AND ProductType = '03' AND Capacct = ? AND SecCompCode = ?" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId, curCode, capAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ͨ����֤������ʺŲ���ͻ��˻�
	 * @param context
	 * @param connection
	 * @param secAcct
	 * @return
	 * @throws SFException
	 */
	public List<SignAccountData> qrySignAccountListDataInfo( Context context, Connection connection, String secAcct ) throws SFException {
		List<SignAccountData> signList = null;
		try {
			StringBuffer buffer = new StringBuffer( "SELECT INVNAME FROM TRDSIGNACCOUNTDATA O WHERE O.SECACCT = ?" );
			Object[] param = { secAcct };
			signList = super.qryForOList( context, connection, buffer.toString(), param, SignAccountData.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally {
			if( signList != null && signList.size() > 0 ) {
				for( SignAccountData sign : signList ) {
					if( sign != null ) {
						sign.resetChangedFlag();
					}
				}
			}
		}
		return signList;
	}

	/**
	 * 
	 * ���������������� ��ѯǩԼ�ʺŸ���
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public long qrySignAccountDataCountBySecAcct( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		long count = 0;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();

			buffer.append( "SELECT COUNT(SECACCT) AS COUNT FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECACCT IN ( SELECT SECACCT FROM " );
			
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			
			buffer.append( "  WHERE SECCOMPCODE=? AND CAPACCT=? AND PRODUCTTYPE='03' ) " );
			Object[] param = new Object[ 2 ];
			param[ 0 ] = secCompCode;
			param[ 1 ] = capAcct;

			count = super.qryCount( context, connection, buffer.toString(), param );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ��֤���ף�������¼
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData signAccountDataLock( Context context, Connection connection, String secCompCode, String capAcct ) throws SFException {
		SignAccountData signAccountData = null;
		try {

			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT O.INITSIDE,O.SECACCT,NVL(SecAcctSeq,1) SECACCTSEQ,O.SECCOMPCODE,O.PRODUCTTYPE,O.CAPACCT,O.CURCODE,O.CASHREMITFLAG," );
			buffer.append( "O.SHSTHCODE,O.SZSTHCODE,O.ACCTID,NVL(SavAcct,' ') SAVACCT,NVL(AcctChldNum,' ') ACCTCHLDNUM,NVL(OpenDepId,' ') OPENDEPID,NVL(OpenBranchId,' ') OPENBRANCHID,O.INVNAME,O.INVTYPE," );
			buffer.append( "O.SIGNFLAG,O.DEPID,O.BRANCHID,O.DESDEPID,O.DESBRANCHID,O.UNITTELLERID,O.DESUNITTELLERID,O.CUSMAGNO,O.STATFLAG," );
			buffer.append( "O.OPENDATE,O.CLOSEDATE,O.PRETXDATE,O.BEGINBAL,NVL(AcctBal,0) ACCTBAL,O.ISMAILBILL,O.MAILDATE,NVL(Flags,' ') FLAGS,O.SIGNDATE,O.ACTIVEFLAG," );
			buffer.append( "O.CHANNEL,O.TRANSFERFLAG,O.SIGNMODE,O.LMCARD,NVL(IIAcctCtl,'0') IIACCTCTL FROM " );

			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " O" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " O" );
			}

			buffer.append( " WHERE O.SECCOMPCODE=? AND O.CAPACCT=? AND O.PRODUCTTYPE='03' AND O.CURCODE='RMB' FOR UPDATE" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ���Ż��ȯ�̺��ʽ��˺��Ƿ���ǩԼ��¼ ��ѯSignFlag
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignFlagByAcctId( Context context, Connection connection, String acctId, String secCompCode, String capAcct ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT SIGNFLAG FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE ( ACCTID=? OR ( SECCOMPCODE=? AND CAPACCT=? ) ) AND ROWNUM=1" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, acctId, secCompCode, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����SecAcct �� SecCompCode ��ѯSignFlag ��ѯSignFlag
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignFlagBySecAcct( Context context, Connection connection, String secAcct, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT SIGNFLAG FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECACCT = ? AND SECCOMPCODE = ? AND ROWNUM=1" );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	/**
	 * ����SecAcct + SecCompCode + SignFlag ='3'��ѯǩԼ��Ϣ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public SignAccountData qrySignAccountDataBySecAcct( Context context, Connection connection, String secAcct, String secCompCode ) throws SFException {
		SignAccountData signAccountData = null;
		try {
			StringBuffer buffer = getQrySignAccountDataSQLStruct( context );
			buffer.append( " WHERE  O.SECACCT=? AND O.SECCOMPCODE=? AND SIGNFLAG='3' " );
			signAccountData = super.qry( context, connection, buffer.toString(), SignAccountData.class, secAcct, secCompCode );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( signAccountData != null ) {
				signAccountData.resetChangedFlag();
			}
		}
		return signAccountData;
	}

	public long qrySignAccountDataByOpenData( Context context, Connection connection, String tranDate ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(1) FROM " );
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			if( SFUtil.isNotEmpty( tranDate ) ) {
				buffer.append( " WHERE  OPENDATE=? " );
				count = ( int )super.qryCount( context, connection, buffer.toString(), tranDate );
			} else {
				count = ( int )super.qryCount( context, connection, buffer.toString() );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * (������������) ��ѯǩԼ��ϵ
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public List<Map<String, Object>> qrySignAccountDataList( Context context, Connection connection, String acctId, String singFlag, String secuType, String catAcct, String idTpye, String invIdCode, String convInvIdCode1, String convInvIdCode2 ) throws SFException {
		List<Map<String, Object>> result = null;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT A.ACCTID,A.INVNAME,A.CURCODE,D.IDTYPE,D.INVIDCODE,A.CAPACCT,A.INVTYPE,B.SECCOMPCODE,B.SECCOMPNAME,B.TPDMFLAG,A.SIGNFLAG,A.CHANNEL,A.CUSMAGNO,A.DEPID,A.OPENDATE,A.SIGNDATE,A.SIGNMODE FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB + " A, TRDSECCOMPDATA B, TRDINVESTDATA D" );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB + " A, TRDSECCOMPDATA B, TRDINVESTDATA D" );
			}
			buffer.append( " WHERE A.SECCOMPCODE=B.SECCOMPCODE AND A.SECACCT=D.SECACCT AND (A.SIGNFLAG = ? OR ? is null)" );

			// ���݄������͵Ĳ�ͬ �鲻ͬ�Ĳ�ѯ���
			if( "0".equals( secuType ) ) {
				buffer.append( " " );
			} else if( SFConst.TPDM_FLAG_NORMAL.equals( secuType ) ) {// ��ͨ���������
				buffer.append( " AND A.SecCompCode not like '%9999' " );
			} else if( SFConst.TPDM_FLAG_MARGIN.equals( secuType ) ) {// ������ȯ���������
				buffer.append( " AND A.SecCompCode like '%9999' " );
			}

			// ������Ϊ����,ǩԼ��־��Ϊȯ��Ԥָ���������м���
			if( !SFConst.SIGN_FLAG_SECU_PRE.equals( singFlag ) && SFUtil.isNotEmpty( acctId ) ) {

				buffer.append( " AND A.ACCTID =?" );// �����Ų�ѯ
				result = super.qryListMap( context, connection, buffer.toString(), singFlag, singFlag, acctId );

			} else if( SFUtil.isNotEmpty( catAcct ) ) {

				buffer.append( " AND A.CAPACCT =?" );// ����֤ȯ�ʽ�̨�˺Ų�ѯ
				result = super.qryListMap( context, connection, buffer.toString(), singFlag, singFlag, catAcct );

			} else { // ��֤������+֤�������ѯ
				
				buffer.append( " AND D.IDTYPE =? AND (D.INVIDCODE = ? OR D.INVIDCODE = ? OR D.INVIDCODE = ?)" );
				result = super.qryListMap( context, connection, buffer.toString(), singFlag, singFlag, idTpye, invIdCode, convInvIdCode1, convInvIdCode2 );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return result;
	}

	/**
	 * ��ѯ��֤������ʺŵ�ǩԼ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public long qrySignAccountDataTotalCountBySecAcct( Context context, Connection connection, String secAcct ) throws SFException {
		long count = 0;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(CAPACCT) AS count FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECACCT=? AND CURCODE='RMB'" );
			count = super.qryCount( context, connection, buffer.toString(), secAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ��ѯ��֤������ʺŵ�ǩԼ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public long qrySignAccountDataTotalCountBySecAcct( Context context, Connection connection, String secAcct, String capAcct ) throws SFException {
		long count = 0;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(CAPACCT) AS count FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE SECACCT=? AND CAPACCT=? " );
			count = super.qryCount( context, connection, buffer.toString(), secAcct, capAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ���ݿ��Ų�ѯ��֤������ʺŵ�ǩԼ����
	 * 
	 * @param context
	 * @param connection
	 * @param signAccountData
	 * @return
	 * @throws SFException
	 */
	public long qrySignAccountDataTotalCountByCardId( Context context, Connection connection, String cardId ) throws SFException {
		long count = 0;
		try {
			// �Ƿ�����ʱ��α�ʶ
			String workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT NVL(COUNT(1),0) AS count FROM " );
			if( SFConst.WORKMODE_724CLEAR.equals( workMode ) ) {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_FB );
			} else {
				buffer.append( CTX_PUBLIC_TAB_SIGNACCOUNTDATA_ZB );
			}
			buffer.append( " WHERE ACCTID=? AND SIGNFLAG NOT IN('4','6','1')" );
			count = super.qryCount( context, connection, buffer.toString(), cardId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}
}