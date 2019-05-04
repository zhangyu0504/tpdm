package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;


public class SecServStatus {
	private String secCompCode;
	private String productType;
	private String secCompName;
	private boolean secCompNameChanged=false;
	private String acctServFlag;
	private boolean acctServFlagChanged=false;
	private String intServFlag;
	private boolean intServFlagChanged=false;
	private String transServFlag1;
	private boolean transServFlag1Changed=false;
	private String transServFlag2;
	private boolean transServFlag2Changed=false;
	private String transServFlag3;
	private boolean transServFlag3Changed=false;
	private String transServFlag4;
	private boolean transServFlag4Changed=false;
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getSecCompName() {
		return secCompName;
	}
	public void setSecCompName(String secCompName) {
		this.secCompName = secCompName;
		this.secCompNameChanged = true;
	}
	public String getAcctServFlag() {
		return acctServFlag;
	}
	public void setAcctServFlag(String acctServFlag) {
		this.acctServFlag = acctServFlag;
		this.acctServFlagChanged = true;
	}
	public String getIntServFlag() {
		return intServFlag;
	}
	public void setIntServFlag(String intServFlag) {
		this.intServFlag = intServFlag;
		this.intServFlagChanged = true;
	}
	public String getTransServFlag1() {
		return transServFlag1;
	}
	public void setTransServFlag1(String transServFlag1) {
		this.transServFlag1 = transServFlag1;
		this.transServFlag1Changed = true;
	}
	public String getTransServFlag2() {
		return transServFlag2;
	}
	public void setTransServFlag2(String transServFlag2) {
		this.transServFlag2 = transServFlag2;
		this.transServFlag2Changed = true;
	}
	public String getTransServFlag3() {
		return transServFlag3;
	}
	public void setTransServFlag3(String transServFlag3) {
		this.transServFlag3 = transServFlag3;
		this.transServFlag3Changed = true;
	}
	public String getTransServFlag4() {
		return transServFlag4;
	}
	public void setTransServFlag4(String transServFlag4) {
		this.transServFlag4 = transServFlag4;
		this.transServFlag4Changed = true;
	}
	
	public void resetChangedFlag(){
		this.secCompNameChanged = false;
		this.acctServFlagChanged = false;
		this.intServFlagChanged = false;
		this.transServFlag1Changed = false;
		this.transServFlag2Changed = false;
		this.transServFlag3Changed = false;
		this.transServFlag4Changed = false;
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean) {
					continue;
				}
				fieldValueList.add(f.get(this));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SFException(e);
			} 
		}
		return fieldValueList.toArray();
	}
	
	public SQLStruct getSaveSecServStatusSQLStruct() throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append(" MERGE INTO TRDSECSERVSTATUS T1");
		buffer.append(" USING (SELECT ? AS SECCOMPCODE, ? AS PRODUCTTYPE, ? AS SECCOMPNAME, ? AS ACCTSERVFLAG, ? AS INTSERVFLAG, ? AS TRANSSERVFLAG1, ? AS TRANSSERVFLAG2,");
		buffer.append(" ? AS TRANSSERVFLAG3, ? AS TRANSSERVFLAG4");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.SECCOMPCODE = T2.SECCOMPCODE AND T1.PRODUCTTYPE = T2.PRODUCTTYPE)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		
		if (secCompNameChanged) {
			buffer.append("T1.SECCOMPNAME = T2.SECCOMPNAME,");
		}
		if (acctServFlagChanged) {
			buffer.append("T1.ACCTSERVFLAG = T2.ACCTSERVFLAG,");
		}
		if (intServFlagChanged) {
			buffer.append("T1.INTSERVFLAG = T2.INTSERVFLAG,");
		}
		if (transServFlag1Changed) {
			buffer.append("T1.TRANSSERVFLAG1 = T2.TRANSSERVFLAG1,");
		}
		if (transServFlag2Changed) {
			buffer.append("T1.TRANSSERVFLAG2 = T2.TRANSSERVFLAG2,");
		}
		if (transServFlag3Changed) {
			buffer.append("T1.TRANSSERVFLAG3 = T2.TRANSSERVFLAG3,");
		}
		if (transServFlag4Changed) {
			buffer.append("T1.TRANSSERVFLAG4 = T2.TRANSSERVFLAG4");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(SECCOMPCODE,PRODUCTTYPE,SECCOMPNAME,ACCTSERVFLAG,INTSERVFLAG,");
		buffer.append("TRANSSERVFLAG1,TRANSSERVFLAG2,TRANSSERVFLAG3,TRANSSERVFLAG4)values(T2.SECCOMPCODE,T2.PRODUCTTYPE,T2.SECCOMPNAME,");
		buffer.append("T2.ACCTSERVFLAG,T2.INTSERVFLAG,T2.TRANSSERVFLAG1,T2.TRANSSERVFLAG2,T2.TRANSSERVFLAG3,T2.TRANSSERVFLAG4)");

		return new SQLStruct(buffer.toString(), valueObj);
		
	}
}
