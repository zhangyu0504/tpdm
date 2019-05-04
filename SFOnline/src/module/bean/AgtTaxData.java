package module.bean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

public class AgtTaxData {
	private String txDate;
	private String bankId;
	private String stkCode;
	private BigDecimal sumAmount;
	public boolean sumAmountChanged = false;
	private BigDecimal taxAmount;
	public boolean taxAmountChanged = false;
	private BigDecimal partAmount;
	public boolean partAmountChanged = false;
	private String memo;
	public boolean memoChanged = false;

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getStkCode() {
		return stkCode;
	}

	public void setStkCode(String stkCode) {
		this.stkCode = stkCode;
	}

	public BigDecimal getSumAmount() {
		return sumAmount;
	}

	public void setSumAmount(BigDecimal sumAmount) {
		this.sumAmount = sumAmount;
		this.sumAmountChanged = true;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
		this.taxAmountChanged = true;
	}

	public BigDecimal getPartAmount() {
		return partAmount;
	}

	public void setPartAmount(BigDecimal partAmount) {
		this.partAmount = partAmount;
		this.partAmountChanged = true;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}
	
	/**
	 * 重置所有changedFlag
	 */
	public void resetChangedFlag(){
		this.sumAmountChanged=false;
		this.taxAmountChanged=false;
		this.partAmountChanged=false;
		this.memoChanged=false;
	}
	
	/**
	 * 将所有字段放入Object数组中
	 * @return
	 * @throws SFException
	 */
	private Object[] getFieldValues()throws SFException{
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValues = new ArrayList<Object>();
		try {
			for (Field f : fields) {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean) {
					continue;
				}
				fieldValues.add(f.get(this));
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw new SFException(e);
		}
		return fieldValues.toArray();
	}
	public SQLStruct  getSaveAgtTaxDataSQLConstruct()throws SFException{
		StringBuffer sb=new StringBuffer();
		Object[] valueObj=getFieldValues();
		sb.append(" MERGE INTO AGT_TAXDATA T1");
		sb.append(" USING (select ? as TXDATE,");
		sb.append("? as BANKID,? as STKCODE,? as SUMAMOUNT,? as TAXAMOUT,? as PARTAMOUNT,? as MEMO");
		sb.append(" from dual) T2");
		sb.append(" ON (T1.TXDATE = T2.TXDATE and T1.BANKID = T2.BANKID and T1.STKCODE=T2.STKCODE)");
		sb.append(" WHEN MATCHED THEN");
		sb.append(" UPDATE SET");
		if(this.sumAmountChanged){
			sb.append(" T1.SUMAMOUNT = T2.SUMAMOUNT,");

		}
		if(this.taxAmountChanged){
			sb.append(" T1.TAXAMOUT = T2.TAXAMOUT,");

		}
		if(this.partAmountChanged){
			sb.append(" T1.PARTAMOUNT = T2.PARTAMOUNT,");

		}
		if(this.memoChanged){
			sb.append(" T1.MEMO = T2.MEMO");

		}
		if(sb.substring(sb.length()-1).equals(",")){
			sb=sb.deleteCharAt(sb.length()-1);
		}
		sb.append(" WHEN NOT MATCHED THEN");
		sb.append("  insert");
		sb.append(" (TXDATE,BANKID,STKCODE,SUMAMOUNT,TAXAMOUT,PARTAMOUNT,MEMO)");
		sb.append(" values");
		sb.append(" (T2.TXDATE,T2.BANKID,T2.STKCODE,T2.SUMAMOUNT,T2.TAXAMOUT,T2.PARTAMOUNT,T2.MEMO)");
		return new SQLStruct(sb.toString(), valueObj);
	}
}
