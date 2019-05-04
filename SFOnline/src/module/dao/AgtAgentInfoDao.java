package module.dao;

import java.sql.Connection;
import java.util.List;

import module.bean.AgtAgentInfo;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class AgtAgentInfoDao extends DaoBase {

	/**
	 * ��ѯ��������Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @return
	 * @throws SFException
	 */
	public AgtAgentInfo qryAgtAgentInfo( Context context, Connection connection, String bankId ) throws SFException {
		AgtAgentInfo agtAgentInfo = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT BANKID,BANKNAME,BRANCHCODE,BANKACCT," );
			buffer.append( "WARNFLAG,WARNMONEY,BANKIP,BANKPORT," );
			buffer.append( "FTPFLAG,FSIP,FSPORT,FTPUSER,FTPPASS," );
			buffer.append( "DESFLAG,AGENTPATH,MACKEY,PINKEY,PINFLAG," );
			buffer.append( "STATUS,OPENDATE,MACFLAG" );
			buffer.append( " FROM AGT_AGENTINFO WHERE BANKID = ?" );
			agtAgentInfo = super.qry( context, connection, buffer.toString(), AgtAgentInfo.class, bankId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( agtAgentInfo != null ) {
				agtAgentInfo.resetChangedFlag();
			}
		}
		return agtAgentInfo;
	}

	/**
	 * ����BankId��MacFlag='0' ��ѯ��������Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtAgentInfo qryAgtAgentInfoByMacFlag( Context context, Connection connection, String bankId ) throws SFException {
		AgtAgentInfo agtAgentInfo = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT BANKID,BANKNAME,BRANCHCODE,BANKACCT," );
			buffer.append( "WARNFLAG,WARNMONEY,BANKIP,BANKPORT," );
			buffer.append( "FTPFLAG,FSIP,FSPORT,FTPUSER,FTPPASS," );
			buffer.append( "DESFLAG,AGENTPATH,MACKEY,PINKEY,PINFLAG," );
			buffer.append( "STATUS,OPENDATE,MACFLAG" );
			buffer.append( " FROM AGT_AGENTINFO WHERE BANKID = ? AND MACFLAG='0'" );
			agtAgentInfo = super.qry( context, connection, buffer.toString(), AgtAgentInfo.class, bankId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( agtAgentInfo != null ) {
				agtAgentInfo.resetChangedFlag();
			}
		}
		return agtAgentInfo;
	}

	/**
	 * ɾ����������Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @param branchId��������
	 * @return
	 * @throws SFException
	 */
	public void delAgtAgentInfo( Context context, Connection connection, String bankId ) throws SFException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "DELETE FROM AGT_AGENTINFO WHERE BANKID=?" );
			super.save( context, connection, buffer.toString(), bankId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * ����������MACFLAG='0'ɾ����¼
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int delAgtAgentInfoByMacFlag( Context context, Connection connection, String bankId ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "DELETE FROM AGT_AGENTINFO WHERE BANKID=? AND MACFLAG='0'" );
			count = super.save( context, connection, buffer.toString(), bankId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * �����������Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveAgtAgentInfo( Context context, Connection connection, AgtAgentInfo bean ) throws SFException {
		try {
			SFUtil.chkCond( context, SFUtil.isEmpty( bean.getBankId() ), "ST4895", "��Ҫ����[BANKID]û���ṩ" );

			super.save( context, connection, bean.getSaveAgtAgentInfoSQLConstruct() );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( null != bean ) {
				bean.resetChangedFlag();
			}

		}
	}

	/**
	 * (��Eͨ����)��ѯ��������Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @return
	 * @throws SFException
	 */
	public List<AgtAgentInfo> qryAgtAgentInfoList( Context context, Connection connection, String bankId ) throws SFException {
		List<AgtAgentInfo> list = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT BANKID,BANKNAME,BRANCHCODE,BANKACCT,STATUS,OPENDATE" );
			buffer.append( " FROM AGT_AGENTINFO" );
			buffer.append( " WHERE (BANKID = ? OR ? IS NULL)" );
			Object[] obj = new Object[ 2 ];
			obj[ 0 ] = bankId;
			obj[ 1 ] = bankId;
			list = super.qryForOList( context, connection, buffer.toString(), obj, AgtAgentInfo.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally{
			if(list!=null && list.size()>0){
				for( AgtAgentInfo agent : list ) {
					if(agent!=null){
						agent.resetChangedFlag();
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * ��ѯֱ����������Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @return
	 * @throws SFException
	 */
	public List<AgtAgentInfo> qryAgtAgentInfoZLList( Context context, Connection connection) throws SFException {
		List<AgtAgentInfo> list = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT BANKID,BANKNAME,BRANCHCODE,BANKACCT,STATUS,OPENDATE,BANKIP,BANKPORT" );
			buffer.append( " FROM AGT_AGENTINFO" );
			buffer.append( " WHERE MACFLAG = '1' AND STATUS = '0' " );
			Object[] obj = new Object[]{};
//			obj[ 0 ] = bankId;
//			obj[ 1 ] = bankId;
			list = super.qryForOList( context, connection, buffer.toString(), obj, AgtAgentInfo.class );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}finally{
			if(list!=null && list.size()>0){
				for( AgtAgentInfo agent : list ) {
					if(agent!=null){
						agent.resetChangedFlag();
					}
				}
			}
		}
		return list;
	}

	/**
	 * (��Eͨ����)��ѯ��������Ϣ�ܼ�¼��
	 * @param context
	 * @param bankId:���к�
	 * @return
	 * @throws SFException
	 */
	public long qryCountAgtAgentInfoList( Context context, Connection connection ) throws SFException {
		long count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT COUNT(BANKID) AS COUNT FROM AGT_AGENTINFO" );
			count = super.qryCount( context, connection, buffer.toString());
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ����BankId��MacFlag='0'���º�������Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updAgtAgentInfo( Context context, Connection connection, AgtAgentInfo bean ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "UPDATE AGT_AGENTINFO SET " );
			buffer.append( "BANKNAME=?,BRANCHCODE=?,BANKACCT=?,BANKIP=?,BANKPORT=?,FSIP=?,FSPORT=?,FTPUSER=?,FTPPASS=?,DESFLAG='0',AGENTPATH=?,STATUS=?" );
			buffer.append( " WHERE BANKID=? AND MACFLAG='0'" );
			count = super.save( context, connection, buffer.toString(), bean.getBankName(), bean.getBranchCode(), bean.getBankAcct(), bean.getBankIp(), bean.getBankPort(), bean.getFsIp(), bean.getFsPort(), bean.getFtpUser(), bean.getFtpPass(), bean.getAgentPath(), bean.getStatus(), bean.getBankId() );
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
	 * ����BankId���º�����status״̬
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updAgtAgentInfoStatus( Context context, Connection connection, String bankid, String status ) throws SFException {
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "UPDATE AGT_AGENTINFO SET " );
			buffer.append( "STATUS=?" );
			buffer.append( " WHERE BANKID=? " );
			count = super.save( context, connection,buffer.toString(),status, bankid );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return count;
	}

	/**
	 * ������ѯAgtAgentInfo + AgtCardBinInfo
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtAgentInfo qryAgtAgentInfoAndCardBinInfo( Context context, Connection connection, String bankAcct ) throws SFException {
		AgtAgentInfo agtAgentInfo = null;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append( "SELECT B.BANKIP,B.BANKPORT, A.BANKID,B.BRANCHCODE FROM AGT_CARDBININFO A,AGT_AGENTINFO B WHERE A.CARDBIN=SUBSTR(?,0,LENGTH(A.CARDBIN)) AND A.BANKID=B.BANKID AND ROWNUM=1" );
			agtAgentInfo = super.qry( context, connection, buffer.toString(), AgtAgentInfo.class, bankAcct );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		} finally {
			if( null != agtAgentInfo ) {
				agtAgentInfo.resetChangedFlag();
			}

		}
		return agtAgentInfo;
	}
}