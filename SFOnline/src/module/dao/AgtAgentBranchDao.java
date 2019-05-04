package module.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import module.bean.AgtAgentBranch;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.ObjectNotFoundException;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * �����л�����ϢDao
 * @author ������
 *
 */
public class AgtAgentBranchDao extends DaoBase{
	
	/**
	 * ��ѯ�����л�����Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @param branchId��������
	 * @return
	 * @throws EMPException
	 * @throws SQLException
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	 */
	public AgtAgentBranch qryAgtAgentBranch(Context context,Connection connection,String bankId,String secuBranchId)throws SFException{
		AgtAgentBranch agtAgentBranch = null;
		try{
			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT BANKID,BRANCHID,BRANCHNAME,FATHERBRANCH,STATUS,OPENDATE FROM AGT_AGENTBRANCH WHERE BANKID=? AND BRANCHID=?");	   		
			agtAgentBranch = super.qry(context, connection, buffer.toString(),AgtAgentBranch.class,bankId,secuBranchId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtAgentBranch!=null){
				agtAgentBranch.resetChangedFlag();
			}
		}
		return agtAgentBranch;
    }
	
	/**
	 * ɾ�������л�����Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @param branchId��������
	 * @return
	 * @throws SFException
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 * @throws InvalidArgumentException
	 */
	public int delAgtAgentBranch(Context context,Connection connection,String bankId,String branchId)throws SFException{
		int result = 0;
		try{
			StringBuffer buffer = new StringBuffer();
			buffer.append("DELETE FROM  AGT_AGENTBRANCH  WHERE BANKID  = ? AND BRANCHID = ?");
			result = super.save(context, connection, buffer.toString(),bankId,branchId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} 
		return result;
    }
	
	/**
	 * ����BankIdɾ�������л�����Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int delAgtAgentBranchByBankId(Context context,Connection connection,String bankId)throws SFException{
		int count = 0;
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("DELETE FROM AGT_AGENTBRANCH WHERE BANKID=?");
			count = super.save(context, connection, buffer.toString(),bankId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return count;
	}
	
	/**
	 * ��������л�����Ϣ
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int saveAgtAgentBranch(Context context,Connection connection,AgtAgentBranch bean)throws SFException{
		int result = 0;
		
		try{
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getBankId()), "ST4895", "��Ҫ����[BANKID]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getBranchId()), "ST4895", "��Ҫ����[BRANCHID]û���ṩ");
			
			result = super.save(context, connection, bean.getSaveAgtAgentBranchSQLStruct());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		} finally{
			if(null != bean){
				bean.resetChangedFlag();	
			}		
		}
		return result;
	}
	
	/**
	 * ��ѯ����������������Ϣ
	 * @param context
	 * @param bankId:���к�
	 * @param branchId��������
	 * @return
	 * @throws EMPException
	 * @throws SQLException
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	 */
	public List<AgtAgentBranch> qryAgtAgentBranchByBranchId(Context context,Connection connection,String bankId,String branchId)throws SFException{
		List<AgtAgentBranch> agtAgentBranch = null;
		try{
			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT BANKID,BRANCHID,BRANCHNAME,FATHERBRANCH,STATUS,OPENDATE FROM AGT_AGENTBRANCH WHERE (BANKID=? OR ? IS NULL) AND BRANCHID=?");	   		
			Object[] obj = new Object[3];
			obj[0] = bankId;
			obj[1] = bankId;
			obj[2] = branchId;
			agtAgentBranch = super.qryForOList(context, connection, buffer.toString(),obj,AgtAgentBranch.class);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(agtAgentBranch!=null && agtAgentBranch.size()>0){
				for( AgtAgentBranch branch : agtAgentBranch ) {
					if(branch!=null){
						branch.resetChangedFlag();
					}
				}
			}
		}
		return agtAgentBranch;
    }	
}