package module.dao;

import java.sql.Connection;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * ����ǩԼ��Ϣ��Dao
 * @author ��˫��
 *
 */
public class DesSignDataDao extends DaoBase{
	
	/**
	 * ��TRDSignAccountData����Ǩ�Ƶ�DesSignData��
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public int migrateSignAccountData(Context context,Connection connection,String secCompCode,String capAcct,String acctId) throws SFException {
		int count = 0;
		try {
			SFUtil.chkCond(context, SFUtil.isEmpty(acctId), "ST4895", "��Ҫ����[ACCTID]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(secCompCode), "ST4895", "��Ҫ����[SECCOMPCODE]û���ṩ");
			SFUtil.chkCond(context, SFUtil.isEmpty(capAcct), "ST4895", "��Ҫ����[CAPACCT]û���ṩ");
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("INSERT INTO TRDDesSignData(InitSide, SecAcct, SecAcctSeq, SecCompCode, ProductType, CapAcct, CurCode, CashRemitFlag, SHSthCode, SZSthCode, AcctId, SavAcct, AcctChldNum, OpenDepId, OpenBranchId,	InvName, InvType, SignFlag, DepId, BranchId, DesDepId, DesBranchId, UnitTellerId, DesUnitTellerId,	CusMagNo, OpenDate, CloseDate, PreTxDate, BeginBal, AcctBal, IsMailBill, MailDate, Flags)");
			buffer.append(" SELECT InitSide, SecAcct, SecAcctSeq, SecCompCode, ProductType, CapAcct, CurCode, CashRemitFlag, SHSthCode, SZSthCode, AcctId, SavAcct, AcctChldNum, OpenDepId, OpenBranchId,	InvName, InvType, SignFlag, DepId, BranchId, DesDepId, DesBranchId, UnitTellerId, DesUnitTellerId,	CusMagNo, OpenDate, CloseDate, PreTxDate, BeginBal, AcctBal, IsMailBill, MailDate, Flags");
			buffer.append(" FROM TRDSignAccountData O");
			buffer.append(" WHERE O.SecCompCode=? AND O.ProductType='03' AND O.CapAcct=? AND O.CurCode='RMB' AND O.AcctId=? AND O.SignFlag='4'");
			
			count = super.save(context, connection, buffer.toString(), secCompCode, capAcct, acctId);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}
		return count;
	}
}
