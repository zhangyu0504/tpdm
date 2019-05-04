package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 *  轮询服务参数
 * @author 吕超鸿
 *
 */
public class AutoBecif {

	private String txDate;//交易日期
	private String subTxSeqId;//流水号
	private String txTime;//交易时间
	private String status;//发送状态
	private String agreementNo;//协议号
	private String becifNo;//客户号
	private String agreementType;//协议大类
	private String agreementSubType;//协议小类   R81—A股   R82—B股    R83—融资融券
	private String agreementStatus;//协议状态1—	签约2—	解约
	private String productNo;//卡号
	private String openDate;//签约日期
	private String closeDate;//解约日期
	private String deptNo;//交易网点号
	private String userId;//操作柜员
	private String businessSeriesCD;//系统号
	
	private boolean txTimeChanged = false;
	private boolean statusChanged = false;
	private boolean agreementNoChanged = false;
	private boolean becifNoChanged = false;
	private boolean agreementTypeChanged = false;
	private boolean agreementSubTypeChanged = false;
	private boolean agreementStatusChanged = false;
	private boolean productNoChanged = false;
	private boolean openDateChanged = false;
	private boolean closeDateChanged = false;
	private boolean deptNoChanged = false;
	private boolean userIdChanged = false;
	private boolean businessSeriesCDChanged = false;
	
	public String getTxDate() {
		return txDate;
	}
	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}
	public String getSubTxSeqId() {
		return subTxSeqId;
	}
	public void setSubTxSeqId(String subTxSeqId) {
		this.subTxSeqId = subTxSeqId;
	}
	public String getTxTime() {
		return txTime;
	}
	public void setTxTime(String txTime) {
		this.txTime = txTime;
		this.txTimeChanged = true;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
		this.statusChanged = true;
	}
	public String getAgreementNo() {
		return agreementNo;
	}
	public void setAgreementNo(String agreementNo) {
		this.agreementNo = agreementNo;
		this.agreementNoChanged = true;
	}
	public String getBecifNo() {
		return becifNo;
	}
	public void setBecifNo(String becifNo) {
		this.becifNo = becifNo;
		this.becifNoChanged = true;
	}
	public String getAgreementType() {
		return agreementType;
	}
	public void setAgreementType(String agreementType) {
		this.agreementType = agreementType;
		this.agreementTypeChanged = true;
	}
	public String getAgreementSubType() {
		return agreementSubType;
	}
	public void setAgreementSubType(String agreementSubType) {
		this.agreementSubType = agreementSubType;
		this.agreementSubTypeChanged = true;
	}
	public String getAgreementStatus() {
		return agreementStatus;
	}
	public void setAgreementStatus(String agreementStatus) {
		this.agreementStatus = agreementStatus;
		this.agreementStatusChanged = true;
	}
	public String getProductNo() {
		return productNo;
	}
	public void setProductNo(String productNo) {
		this.productNo = productNo;
		this.productNoChanged = true;
	}
	public String getOpenDate() {
		return openDate;
	}
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
		this.openDateChanged = true;
	}
	public String getCloseDate() {
		return closeDate;
	}
	public void setCloseDate(String closeDate) {
		this.closeDate = closeDate;
		this.closeDateChanged = true;
	}
	public String getDeptNo() {
		return deptNo;
	}
	public void setDeptNo(String deptNo) {
		this.deptNo = deptNo;
		this.deptNoChanged = true;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
		this.userIdChanged = true;
	}
	public String getBusinessSeriesCD() {
		return businessSeriesCD;
	}
	public void setBusinessSeriesCD(String businessSeriesCD) {
		this.businessSeriesCD = businessSeriesCD;
		this.businessSeriesCDChanged = true;
	}
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.txTimeChanged = false;
		this.statusChanged = false;
		this.agreementNoChanged = false;
		this.becifNoChanged = false;
		this.agreementTypeChanged = false;
		this.agreementSubTypeChanged = false;
		this.agreementStatusChanged = false;
		this.productNoChanged = false;
		this.openDateChanged = false;
		this.closeDateChanged = false;
		this.deptNoChanged = false;
		this.userIdChanged = false;
		this.businessSeriesCDChanged = false;
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
	
	
	public SQLStruct getSaveAutoBecifSQLStruct() throws SFException {
		StringBuffer buffer = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		buffer.append("MERGE INTO TRDAUTOBECIF T1");
		buffer.append(" USING (SELECT ? as TXDATE, ? as SUBTXSEQID, ? as TXTIME, ? as STATUS, ? as AGREEMENT_NO, ? as BECIF_NO, ? as AGREEMENT_TYPE, ? as AGREEMENT_SUB_TYPE,");
		buffer.append(" ? as AGREEMENT_STATUS, ? as PRODUCT_NO, ? as OPEN_DATE, ? as CLOSE_DATE, ? as DEPT_NO, ? as USER_ID, ? as BUSINESS_SERIES_CD");
		buffer.append(" FROM DUAL) T2");
		buffer.append(" ON (T1.TXDATE = T2.TXDATE and T1.SUBTXSEQID = T2.SUBTXSEQID)");
		buffer.append(" WHEN MATCHED THEN");
		buffer.append(" UPDATE SET ");
		if (txTimeChanged) {
			buffer.append("T1.TXTIME = T2.TXTIME,");
		}
		if (statusChanged) {
			buffer.append("T1.STATUS = T2.STATUS,");
		}
		if (agreementNoChanged) {
			buffer.append("T1.AGREEMENT_NO = T2.AGREEMENT_NO,");
		}
		if (becifNoChanged) {
			buffer.append("T1.BECIF_NO = T2.BECIF_NO,");
		}
		if (agreementTypeChanged) {
			buffer.append("T1.AGREEMENT_TYPE = T2.AGREEMENT_TYPE,");
		}
		if (agreementSubTypeChanged) {
			buffer.append("T1.AGREEMENT_SUB_TYPE = T2.AGREEMENT_SUB_TYPE,");
		}
		if (agreementStatusChanged) {
			buffer.append("T1.AGREEMENT_STATUS = T2.AGREEMENT_STATUS,");
		}
		if (productNoChanged) {
			buffer.append("T1.PRODUCT_NO = T2.PRODUCT_NO,");
		}
		if (openDateChanged) {
			buffer.append("T1.OPEN_DATE = T2.OPEN_DATE,");
		}
		if (closeDateChanged) {
			buffer.append("T1.CLOSE_DATE = T2.CLOSE_DATE,");
		}
		if (deptNoChanged) {
			buffer.append("T1.DEPT_NO = T2.DEPT_NO,");
		}
		if (userIdChanged) {
			buffer.append("T1.USER_ID = T2.USER_ID,");
		}
		if (businessSeriesCDChanged) {
			buffer.append("T1.BUSINESS_SERIES_CD = T2.BUSINESS_SERIES_CD");
		}
		
		if (buffer.substring(buffer.length() - 1).equals(",")) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(" WHEN NOT MATCHED THEN");
		buffer.append(" insert(TXDATE,SUBTXSEQID,TXTIME,STATUS,AGREEMENT_NO,BECIF_NO,AGREEMENT_TYPE,AGREEMENT_SUB_TYPE,AGREEMENT_STATUS,PRODUCT_NO,OPEN_DATE,CLOSE_DATE,DEPT_NO,");
		buffer.append(" USER_ID,BUSINESS_SERIES_CD)");
		buffer.append(" values(T2.TXDATE,T2.SUBTXSEQID,T2.TXTIME,T2.STATUS,T2.AGREEMENT_NO,T2.BECIF_NO,T2.AGREEMENT_TYPE,T2.AGREEMENT_SUB_TYPE,T2.AGREEMENT_STATUS,T2.PRODUCT_NO,T2.OPEN_DATE,");
		buffer.append("T2.CLOSE_DATE,T2.DEPT_NO,T2.USER_ID,T2.BUSINESS_SERIES_CD)");

		return new SQLStruct(buffer.toString(), valueObj);
		
	}
}
