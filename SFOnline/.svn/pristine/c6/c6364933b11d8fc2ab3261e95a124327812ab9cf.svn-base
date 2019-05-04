package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

public class AgtAutoAgt {
	private String txDate;
	private String picsLogNo;
	private String frontLogNo;
	private boolean frontLogNoChanged;
	private String txData;  
	private boolean txDataChanged;
	private String txTime;
	private boolean txTimeChanged;
	private String status;
	private boolean statusChanged;
	private Integer searchTimes;
	private boolean searchTimesChanged;
	private String bankId;
	private boolean bankIdChanged;
	public String getTxDate() {
		return txDate;
	}
	public void setTxDate(String txDate) {
		this.txDate = txDate;
		this.txDataChanged=true;
	}
	public String getPicsLogNo() {
		return picsLogNo;
	}
	public void setPicsLogNo(String picsLogNo) {
		this.picsLogNo = picsLogNo;
	}
	public String getFrontLogNo() {
		return frontLogNo;
	}
	public void setFrontLogNo(String frontLogNo) {
		this.frontLogNo = frontLogNo;
		this.frontLogNoChanged=true;
	}
	public String getTxTime() {
		return txTime;
	}
	public void setTxTime(String txTime) {
		this.txTime = txTime;
		this.txTimeChanged=true;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
		this.statusChanged=true;
	}
	public Integer getSearchTimes() {
		return searchTimes;
	}
	public void setSearchTimes(Integer searchTimes) {
		this.searchTimes = searchTimes;
		this.searchTimesChanged=true;
	}
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
		this.bankIdChanged=true;
	}
	
	
	public String getTxData() {
		return txData;
	}
	public void setTxData(String txData) {
		this.txData = txData;
		this.txDataChanged=true;
	}
	
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
	public SQLStruct  getSaveAgtAutoAgtSQLConstruct() throws SFException{
		StringBuffer sb=new StringBuffer();
		Object[] valueObj=getFieldValues();
		sb.append(" MERGE INTO AGT_AUTOAGT T1");
		sb.append(" USING (select ? as TXDATE,? as PICSLOGNO,? as FRONTLOGNO,? as TXDATA,? as TXTIME,? as STATUS,? as SEARCHTIMES,? as BANKID)");
		sb.append("from dual) T2");
		sb.append(" ON (T1.TXDATE = T2.TXDATE and T1.PICSLOGNO = T2.PICSLOGNO)");
		sb.append(" WHEN MATCHED THEN");
		sb.append(" UPDATE SET");
		if(this.frontLogNoChanged){
			sb.append(" T1.FRONTLOGNO = T2.FRONTLOGNO,");

		}
		if(this.txTimeChanged){
			sb.append(" T1.TXTIME = T2.TXTIME,");

		}
		if(this.statusChanged){
			sb.append(" T1.STATUS = T2.STATUS,");

		}
		if(this.searchTimesChanged){
			sb.append(" T1.SEARCHTIMES = T2.SEARCHTIMES,");

		}
		if(this.bankIdChanged){
			sb.append(" T1.BANKID = T2.BANKID,");

		}
		if(this.txDataChanged){
			sb.append(" T1.TXDATA = T2,TXDATA");
		}
		if(sb.substring(sb.length()-1).equals(",")){
			sb=sb.deleteCharAt(sb.length()-1);
		}
		sb.append(" WHEN NOT MATCHED THEN");
		sb.append("  insert");
		sb.append("(TXDATE,PICSLOGNO,FRONTLOGNO,TXTIME,STATUS,SEARCHTIMES,TXDATA,BANKID)");
		sb.append(" values");
		sb.append("(T2.TXDATE,T2.PICSLOGNO,T2.FRONTLOGNO,T2.TXTIME,T2.STATUS,T2.SEARCHTIMES,T2.TXDATA,T2.BANKID)");
		return new SQLStruct(sb.toString(), valueObj);
	}
	
	/**
	 * ÷ÿ÷√À˘”–chagedFlag
	 */
	public void resetChangedFlag(){
		this.txDataChanged=false;
		this.frontLogNoChanged=false;
		this.txTimeChanged=false;
		this.statusChanged=false;
		this.searchTimesChanged=false;
		this.txDataChanged=false;
		this.bankIdChanged=false;
	}
	
}
