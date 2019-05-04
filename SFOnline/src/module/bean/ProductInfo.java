package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 * 产品信息
 * @author 吕超鸿
 *
 */
public class ProductInfo {

	private String secCompCode; //券商编号
	private String userId;//客户编号
	private String secCompName;//券商名称
	private String productType;//产品种类
	private String productName;//产品名称
	private String curCode;//币种
	private String curName;//币种名称
	private String permitFlag;//开通标志 1－支持 0－不支持
	private String firmCode;//公司代码
	private String truAcctId;//托管专户账号
	private String truOpnDepId;//托管专户账号开户网点编号
	private String secSelfAcctId; //券商自有资金账号
	private String selfOpnDepId; //自有资金账号开户网点编号
	private String secCorAcctId;//券商法人交收账号
	private String corOpnDepId;//法人交收账号开户网点编号
	private String tpdmFlag;//第三方存管模式 1-普通第三方存管 2-融资融券第三方存管
	private String openBranchId;
	
	private boolean userIdChanged = false;
	private boolean secCompNameChanged = false;
	private boolean productNameChanged = false;
	private boolean curNameChanged = false;
	private boolean permitFlagChanged = false;
	private boolean firmCodeChanged = false;
	private boolean truAcctIdChanged = false;
	private boolean truOpnDepIdChanged = false;
	private boolean secSelfAcctIdChanged = false;
	private boolean selfOpnDepIdChanged = false;
	private boolean secCorAcctIdChanged = false;
	private boolean corOpnDepIdChanged = false;
	private boolean tpdmFlagChanged = false;
	
	public String getSecCompCode() {
		return secCompCode;
	}
	public void setSecCompCode(String secCompCode) {
		this.secCompCode = secCompCode;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
		this.userIdChanged = true;
	}
	public String getSecCompName() {
		return secCompName;
	}
	public void setSecCompName(String secCompName) {
		this.secCompName = secCompName;
		this.secCompNameChanged = true;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
		this.productNameChanged = true;
	}
	public String getCurCode() {
		return curCode;
	}
	public void setCurCode(String curCode) {
		this.curCode = curCode;
	}
	public String getCurName() {
		return curName;
	}
	public void setCurName(String curName) {
		this.curName = curName;
		this.curNameChanged = true;
	}
	public String getPermitFlag() {
		return permitFlag;
	}
	public void setPermitFlag(String permitFlag) {
		this.permitFlag = permitFlag;
		this.permitFlagChanged = true;
	}
	public String getFirmCode() {
		return firmCode;
	}
	public void setFirmCode(String firmCode) {
		this.firmCode = firmCode;
		this.firmCodeChanged = true;
	}
	public String getTruAcctId() {
		return truAcctId;
	}
	public void setTruAcctId(String truAcctId) {
		this.truAcctId = truAcctId;
		this.truAcctIdChanged = true;
	}
	public String getTruOpnDepId() {
		return truOpnDepId;
	}
	public void setTruOpnDepId(String truOpnDepId) {
		this.truOpnDepId = truOpnDepId;
		this.truOpnDepIdChanged = true;
	}
	public String getSecSelfAcctId() {
		return secSelfAcctId;
	}
	public void setSecSelfAcctId(String secSelfAcctId) {
		this.secSelfAcctId = secSelfAcctId;
		this.secSelfAcctIdChanged = true;
	}
	public String getSelfOpnDepId() {
		return selfOpnDepId;
	}
	public void setSelfOpnDepId(String selfOpnDepId) {
		this.selfOpnDepId = selfOpnDepId;
		this.selfOpnDepIdChanged = true;
	}
	public String getSecCorAcctId() {
		return secCorAcctId;
	}
	public void setSecCorAcctId(String secCorAcctId) {
		this.secCorAcctId = secCorAcctId;
		this.secCorAcctIdChanged = true;
	}
	public String getCorOpnDepId() {
		return corOpnDepId;
	}
	public void setCorOpnDepId(String corOpnDepId) {
		this.corOpnDepId = corOpnDepId;
		this.corOpnDepIdChanged = true;
	}
	public String getTpdmFlag() {
		return tpdmFlag;
	}
	public void setTpdmFlag(String tpdmFlag) {
		this.tpdmFlag = tpdmFlag;
		this.tpdmFlagChanged = true;
	}
	public String getOpenBranchId() {
		return openBranchId;
	}
	
	public void setOpenBranchId( String openBranchId ) {
		this.openBranchId = openBranchId;
	}
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.userIdChanged = false;
		this.secCompNameChanged = false;
		this.productNameChanged = false;
		this.curNameChanged = false;
		this.permitFlagChanged = false;
		this.firmCodeChanged = false;
		this.truAcctIdChanged = false;
		this.truOpnDepIdChanged = false;
		this.secSelfAcctIdChanged = false;
		this.selfOpnDepIdChanged = false;
		this.secCorAcctIdChanged = false;
		this.corOpnDepIdChanged = false;
		this.tpdmFlagChanged = false;
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean || f.getName().equals( "openBranchId" )) {
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
	
	public SQLStruct getSaveProductInfoSQLStruct() throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append("MERGE INTO TRDPRODUCTINFO T1");
		buffer.append(" USING (SELECT ? as SECCOMPCODE, ? as USERID, ? as SECCOMPNAME, ? as PRODUCTTYPE, ? as PRODUCTNAME, ? as CURCODE, ? as CURNAME, ? as PERMITFLAG,");
		buffer.append(" ? as FIRMCODE, ? as TRUACCTID, ? as TRUOPNDEPID, ? as SECSELFACCTID, ? as SELFOPNDEPID, ? as SECCORACCTID, ? as COROPNDEPID, ? as TPDMFLAG");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.SECCOMPCODE = T2.SECCOMPCODE and T1.PRODUCTTYPE = T2.PRODUCTTYPE and T1.CURCODE = T2.CURCODE)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (userIdChanged) {
			buffer.append("T1.USERID = T2.USERID,");
		}
		if (secCompNameChanged) {
			buffer.append("T1.SECCOMPNAME = T2.SECCOMPNAME,");
		}
		if (productNameChanged) {
			buffer.append("T1.PRODUCTNAME = T2.PRODUCTNAME,");
		}
		if (curNameChanged) {
			buffer.append("T1.CURNAME = T2.CURNAME,");
		}
		if (permitFlagChanged) {
			buffer.append("T1.PERMITFLAG = T2.PERMITFLAG,");
		}
		if (firmCodeChanged) {
			buffer.append("T1.FIRMCODE = T2.FIRMCODE,");
		}
		if (truAcctIdChanged) {
			buffer.append("T1.TRUACCTID = T2.TRUACCTID,");
		}
		if (truOpnDepIdChanged) {
			buffer.append("T1.TRUOPNDEPID = T2.TRUOPNDEPID,");
		}
		if (secSelfAcctIdChanged) {
			buffer.append("T1.SECSELFACCTID = T2.SECSELFACCTID,");
		}
		if (selfOpnDepIdChanged) {
			buffer.append("T1.SELFOPNDEPID = T2.SELFOPNDEPID,");
		}
		if (secCorAcctIdChanged) {
			buffer.append("T1.SECCORACCTID = T2.SECCORACCTID,");
		}
		if (corOpnDepIdChanged) {
			buffer.append("T1.COROPNDEPID = T2.COROPNDEPID,");
		}
		if (tpdmFlagChanged) {
			buffer.append("T1.TPDMFLAG = T2.TPDMFLAG");
		}
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(SECCOMPCODE,USERID,SECCOMPNAME,PRODUCTTYPE,PRODUCTNAME,CURCODE,CURNAME,PERMITFLAG,FIRMCODE,TRUACCTID,TRUOPNDEPID,SECSELFACCTID,SELFOPNDEPID,");
		buffer.append("SECCORACCTID,COROPNDEPID,TPDMFLAG)");
		buffer.append(" values(T2.SECCOMPCODE,T2.USERID,T2.SECCOMPNAME,T2.PRODUCTTYPE,T2.PRODUCTNAME,T2.CURCODE,T2.CURNAME,T2.PERMITFLAG,T2.FIRMCODE,T2.TRUACCTID,T2.TRUOPNDEPID,");
		buffer.append("T2.SECSELFACCTID,T2.SELFOPNDEPID,T2.SECCORACCTID,T2.COROPNDEPID,T2.TPDMFLAG)");

		return new SQLStruct(buffer.toString(), valueObj);
		
	}
	
	
}
