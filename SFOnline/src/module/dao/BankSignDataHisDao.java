package module.dao;

import java.sql.Connection;

import module.bean.BankSignDataHis;

import com.ecc.emp.core.Context;

import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

public class BankSignDataHisDao extends DaoBase{

	private StringBuffer getQryInvestDataSQLStruct(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("SELECT INVTYPE AS invType,ACCTID AS acctId,SAVACCT AS savAcct,ACCTCHLDNUM AS acctChldNum,CURCODE AS curCode," );
		buffer.append("SECCOMPCODE AS secCompCode,PRODUCTTYPE AS productType,CAPACCT AS capAcct,INVNAME AS invName,IDTYPE AS idType,");
		buffer.append("INVIDCODE AS invIdCode,SIGNFLAG AS signFlag,CUSMAGNO AS cusMagno,DEPID AS depId,BRANCHID AS branchId,BOOKNO AS bookNo,");
		buffer.append("OPENDEPID AS openDepId,OPENBRANCHID AS openBranchId,UNITTELLERID AS unitTellerId,OPENDATE AS openDate,INITSIDE AS initSide," );
		buffer.append("OPENTIME AS openTime,TXDATE AS txDate,TXTIME AS txTime,FLAGS AS flags,MEMO AS memo,SUBTXSEQID1 AS subTxSeqId1," );
		buffer.append("SUBTXSEQID2 AS subTxSeqId2,SUBTXSEQID3 AS subTxSeqId3,DELDATE AS delDate,DELTIME AS delTime,CARDTYPE AS cardType,");
		buffer.append("PHONE AS phone,MOBILE AS mobile,SECBRCHID AS secBrchId,CHANNEL AS channel,LMCARD AS lmCard,EMAIL AS email");
		buffer.append(" FROM TRDBANKSIGNDATA WHERE ");
		
		return buffer;
	}
	
	/**
	 * 查询银行预指定信息
	 * @param context
	 * @param connection
	 * @param acctId
	 * @param secCompCode
	 * @return
	 * @throws SFException
	 */
	public BankSignDataHis qryBankSignDataHis(Context context,Connection connection,String acctId,String secCompCode)throws SFException{
		BankSignDataHis bankSignDataHis = null;
		try {
			StringBuffer buffer = getQryInvestDataSQLStruct();
			buffer.append(" ACCTID = ? AND SECCOMPCODE= ?");
			
			bankSignDataHis = super.qry(context, connection,buffer.toString(),BankSignDataHis.class,acctId,secCompCode);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(bankSignDataHis!=null){
				bankSignDataHis.resetChangedFlag();
			}
		}
		return bankSignDataHis;
	}
	
	/**
	 * 保存银行预指定信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @throws SFException
	 */
	public void saveBankSignDataHis(Context context,Connection connection,BankSignDataHis bean) throws SFException {
		try {
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getAcctId()), "ST4895", "必要参数[ACCTID]没有提供");
			SFUtil.chkCond(context, SFUtil.isEmpty(bean.getSecCompCode()), "ST4895", "必要参数[SECCOMPCODE]没有提供");
			
			super.save(context, connection, bean.getSaveBankSignDataHisSQLStruct());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(bean != null){
				bean.resetChangedFlag();
			}
		}
	}
}
