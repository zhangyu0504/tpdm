package module.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import module.bean.AgtCustomerInfo;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.jdbc.GetConnectionFailedException;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * �����пͻ���ϢDao
 * @author ������
 *
 */
public class AgtCustomerInfoDao extends DaoBase{

	private StringBuffer getQryAgtCustomerInfoSQLStruct(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT INVTYPE AS invType,ACCTNO AS acctNo,STKACCT AS stkAcct,BANKID AS bankId,OPENBRANCH AS openBranch,STKCODE AS stkCode,");
		buffer.append("CURCODE AS curCode,INVNAME AS invName,IDTYPE AS idType,INVIDCODE AS invidCode,OPENDATE AS openDate,STATUS AS status,MEMO AS memo");
		buffer.append(" FROM AGT_CUSTOMERINFO WHERE ");
		
		return buffer;
	}
	
	/**
	 * ��ѯ�����пͻ���Ϣ
	 * @param context
	 * @param stkAcct:�ʽ��˺�
	 * @param stkCode:ȯ�̴���
	 * @return
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	 * @throws SFException 
	 * @throws GetConnectionFailedException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @throws EMPException
	 */
	public  AgtCustomerInfo qryAgtCustomerInfo(Context context,Connection connection,String stkAcct,String stkCode) throws SFException{
		AgtCustomerInfo agtCustomerInfo = null;
		try{
				StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
				buffer.append(" STKACCT = ? AND STKCODE= ?");
				
				agtCustomerInfo = super.qry(context, connection, buffer.toString(),AgtCustomerInfo.class,stkAcct,stkCode);
			} catch (SFException e){
				throw e;
			} catch (Exception e ){
				SFUtil.chkCond(context, "ST4895", e.getMessage());
			}finally{
				if(agtCustomerInfo!=null){
					agtCustomerInfo.resetChangedFlag();
				}
			}
		 return agtCustomerInfo;
    }
	
	
	/**
	 * ��������+BankId��AcctNo��ѯ�����пͻ���Ϣ
	 * @param context
	 * @param stkAcct:�ʽ��˺�
	 * @param stkCode:ȯ�̴���
	 * @return
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	 * @throws SFException 
	 * @throws GetConnectionFailedException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @throws EMPException
	 */
	public  AgtCustomerInfo qryAgtCustomerInfoByBankIdAndAcctNo(Context context,Connection connection,String stkAcct,String stkCode,String bankId,String acctNo) throws SFException{
		AgtCustomerInfo agtCustomerInfo = null;
		try{
				StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
				buffer.append(" STKACCT = ? AND STKCODE= ? AND BANKID=? AND ACCTNO=?");
				
				agtCustomerInfo = super.qry(context, connection, buffer.toString(),AgtCustomerInfo.class,stkAcct,stkCode,bankId,acctNo);
			} catch (SFException e){
				throw e;
			} catch (Exception e ){
				SFUtil.chkCond(context, "ST4895", e.getMessage());
			}finally{
				if(agtCustomerInfo!=null){
					agtCustomerInfo.resetChangedFlag();
				}
			}
		 return agtCustomerInfo;
    }
	
	
	/**
	 * ����status��ѯ�����пͻ���Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtCustomerInfo qryAgtCustomerInfoByStatus(Context context,Connection connection,String stkAcct,String stkCode,String status) throws SFException{
		AgtCustomerInfo agtCustomerInfo = null;
		try{	
			StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
			buffer.append(" STKCODE = ?  AND STKACCT = ? AND STATUS = ?");
		    agtCustomerInfo = super.qry(context, connection, buffer.toString() ,AgtCustomerInfo.class,stkCode,stkAcct,status);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtCustomerInfo!=null){
				agtCustomerInfo.resetChangedFlag();
			}
		}
	 return agtCustomerInfo;
}
	
	/**
	 * ɾ�������пͻ���Ϣ
	 * @param context
	 * @param stkAcct
	 * @param stkCode
	 * @return
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	 * @throws SFException 
	 * @throws GetConnectionFailedException 
	 * @throws SQLException 
	 * @throws Exception 
	 * @throws EMPException
	 */
	public int delAgtCustomerInfo(Context context,Connection connection,String stkAcct,String stkCode) throws SFException{
		int result = 0;
		try{
			StringBuffer buffer = new StringBuffer();
			Object[] params = new Object[2];
			
			buffer.append("DELETE FROM  AGT_CUSTOMERINFO  WHERE STKACCT = ? AND STKCODE= ?");
			
			params[0] = stkAcct;
			params[1] = stkCode;
			
			result = super.save(context, connection,buffer.toString(),params);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return result;
    }
	
	/**
	 * ��������пͻ���Ϣ
	 * @param context
	 * @param bean
	 * @return
	 * @throws SFException
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 * @throws InvalidArgumentException
	 */
	public int saveAgtCustomerInfo(Context context,Connection connection,AgtCustomerInfo bean)throws SFException{
		int result = 0;
		try{
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getStkAcct()), "ST4895", "��Ҫ����[STKACCT]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getStkCode()), "ST4895", "��Ҫ����[STKCODE]û���ṩ");
			
			result = super.save(context, connection, bean.getSaveAgtCustomerInfoSQLStruct());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} finally{
			bean.resetChangedFlag();
		}
		return result;
	}
	
	/**
	 * ����BankId+AcctNo�����������ʺ�
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updAgtCustomerInfoByBankIdAndAcctNo(Context context,Connection connection,String newAcctNo,String bankId,String secCompCode,String capAcct,String oldAcctNo)throws SFException{
		int result = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("UPDATE AGT_CUSTOMERINFO SET ACCTNO=? WHERE BANKID=? AND STKCODE=? AND STKACCT=? AND ACCTNO=?");
			result = super.save(context, connection, buffer.toString(), newAcctNo, bankId, secCompCode, capAcct,oldAcctNo);
		
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return result;
	}
	
	
	/**
	 * ����BankId���������¿�������
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updAgtCustomerInfoByBankId(Context context,Connection connection,String stkAcct,String stkCode,String bankId,String openBranch)throws SFException{
		int result = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("UPDATE AGT_CUSTOMERINFO SET OPENBRANCH=? WHERE BANKID=? AND STKCODE=? AND STKACCT=?");
			result = super.save(context, connection, buffer.toString(), openBranch, bankId, stkCode, stkAcct);
		
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return result;
	}
	
	/**
	 * ����AcctNo���������¿�������
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int updAgtCustomerInfoByAcctNo(Context context,Connection connection,String stkAcct,String stkCode,String acctNo,String status,String memo)throws SFException{
		int result = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("UPDATE AGT_CUSTOMERINFO SET STATUS=?,MEMO=? WHERE STKACCT=? AND STKCODE=? AND ACCTNO=?");
			result = super.save(context, connection, buffer.toString(), status, memo, stkAcct, stkCode, acctNo);
			
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return result;
	}
	/**
	 * ����bankId��ѯ�����пͻ�
	 * ��ѯ�Ƿ�Ϊ�ú����еĿͻ�
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtCustomerInfo qryAgtCustomerInfoByBankId(Context context,Connection connection,String capAcct,String secCompCode,String bankId) throws SFException{
		AgtCustomerInfo  agtCustomerInfo = null;
		try{	
			
			StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
			buffer.append( " STKACCT = ? AND BANKID = ? AND STKCODE = ?" );
		    agtCustomerInfo = super.qry(context, connection, buffer.toString(), AgtCustomerInfo.class, capAcct, secCompCode, bankId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtCustomerInfo!=null){
				agtCustomerInfo.resetChangedFlag();
			}
		}
	 return agtCustomerInfo;
}
	
	/**
	 * ����CapAcct��ѯ�����пͻ�
	 * ��ѯ�Ƿ�Ϊ�ú����еĿͻ�
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtCustomerInfo qryAgtCustomerInfoByCapAcct(Context context,Connection connection,String capAcct,String secCompCode) throws SFException{
		AgtCustomerInfo  agtCustomerInfo = null;
		try{	
			StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
			buffer.append( " STKACCT = ? AND STKCODE = ?" );
		    agtCustomerInfo = super.qry(context, connection, buffer.toString(), AgtCustomerInfo.class, capAcct, secCompCode);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtCustomerInfo!=null){
				agtCustomerInfo.resetChangedFlag();
			}
		}
	 return agtCustomerInfo;
}
	
	/**
	 * ����acctNo��ѯ�����пͻ�
	 * ��ѯ�Ƿ�Ϊ�ú����еĿͻ�
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtCustomerInfo qryAgtCustomerInfoByAcctNo(Context context,Connection connection,String stkAcct,String stkCode,String acctNo) throws SFException{
		AgtCustomerInfo agtCustomerInfo = null;
		try{	
			StringBuffer buffer = getQryAgtCustomerInfoSQLStruct();
			buffer.append( " ACCTNO = ? AND STKCODE = ? AND STKACCT = ?" );
		    agtCustomerInfo = super.qry(context, connection, buffer.toString(), AgtCustomerInfo.class, acctNo, stkCode, stkAcct);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtCustomerInfo!=null){
				agtCustomerInfo.resetChangedFlag();
			}
		}
	 return agtCustomerInfo;
}	
	/**
	 * ����AgtCustomerInfo �� AgtAgentInfo ��ѯ��¼
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public AgtCustomerInfo qryAgtCustomerInfoAndAgentInfo(Context context,Connection connection,String stkCode,String stkAcct,String acctNo) throws SFException{
		AgtCustomerInfo agtCustomerInfo = null;
		StringBuffer buffer = new StringBuffer();
		List<Map<String,Object>> result = null;
		try{		
			buffer.append("SELECT A.BANKID AS bankId,A.OPENBRANCH AS openBranch,B.BANKNAME AS bankName,B.BRANCHCODE AS branchCode,");
			buffer.append("B.BANKACCT AS bankAcct,B.WARNFLAG AS warnFlag,B.WARNMONEY AS warnMoney ");
		    buffer.append(" FROM AGT_CUSTOMERINFO A,AGT_AGENTINFO B");
		    buffer.append(" WHERE A.STKCODE=? AND A.STKACCT=? AND A.ACCTNO=? AND A.STATUS='0' AND A.BANKID=B.BANKID AND B.STATUS='0'");
		    result = super.qryListMap(context, connection, buffer.toString(),stkCode, stkAcct,acctNo);
		    if(null != result && result.size() > 0){
		    	for( Map<String, Object> map : result ) {
					agtCustomerInfo = new AgtCustomerInfo();
			    	agtCustomerInfo.setBankId((String)map.get( "bankId" ));
			    	agtCustomerInfo.setOpenBranch((String)map.get( "openBranch" ));
			    	agtCustomerInfo.getAgtAgentInfo().setBankName((String)map.get( "bankName" ));
			    	agtCustomerInfo.getAgtAgentInfo().setBranchCode((String)map.get( "branchCode" ));
			    	agtCustomerInfo.getAgtAgentInfo().setBankAcct((String)map.get( "bankAcct" ));
			    	agtCustomerInfo.getAgtAgentInfo().setWarnFlag((String)map.get( "warnFlag" ));
			    	agtCustomerInfo.getAgtAgentInfo().setWarnMoney((String)map.get( "warnMoney" ));
				}
		    }
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtCustomerInfo!=null){
				agtCustomerInfo.resetChangedFlag();
			}
		}
	 return agtCustomerInfo;
}	
}