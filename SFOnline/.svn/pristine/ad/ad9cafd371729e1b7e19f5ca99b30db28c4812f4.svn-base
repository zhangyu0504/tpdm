package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 * 代理行机构信息实体类
 * @author 吕超鸿
 *
 */
public class AgtAgentBranch {

	private static final long serialVersionUID = 1L;
	
	private String bankId;//银行号
	private String branchId;//机构号
	private String branchName;//机构名称
	private String fatherBranch;//上级机构号
	private String status;//状态
	private String openDate;//开立日期
	
	private boolean branchNameChanged = false;
	private boolean fatherBranchChanged = false;
	private boolean statusChanged = false;
	private boolean openDateChanged = false;

	
	
	public AgtAgentBranch() {
		super();
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
		this.branchNameChanged = true;
	}

	public String getFatherBranch() {
		return fatherBranch;
	}

	public void setFatherBranch(String fatherBranch) {
		this.fatherBranch = fatherBranch;
		this.fatherBranchChanged = true;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		this.statusChanged = true;
	}

	public String getOpenDate() {
		return openDate;
	}

	public void setOpenDate(String openDate) {
		this.openDate = openDate;
		this.openDateChanged = true;
	}

	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.branchNameChanged = false;
		this.fatherBranchChanged = false;
		this.statusChanged = false;
		this.openDateChanged = false;
		
	}
	
	public Object[] getFieldValues() throws SFException {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> fieldValueList = new ArrayList<Object>();

		for (Field f : fields) {
			try {
				if (f.getModifiers() > 2||f.get(this) instanceof Boolean) {
					continue;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new SFException(e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new SFException(e);
			}
			try {
				fieldValueList.add(f.get(this));
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				throw new SFException(e1);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				throw new SFException(e1);
			}
		}

		return fieldValueList.toArray();
	}

	/**
	 * 获取保存SQL结构体
	 * @return
	 */
	public SQLStruct getSaveAgtAgentBranchSQLStruct() throws SFException{
		StringBuffer sb = new StringBuffer();
		Object[] valueObj = this.getFieldValues();
		
		sb.append("MERGE INTO AGT_AGENTBRANCH T1");
		sb.append(" USING (select ? as BANKID, ? as BRANCHID, ? as BRANCHNAME, ? as FATHERBRANCH, ? as STATUS, ? as OPENDATE from dual) T2");
		sb.append(" ON (T1.BANKID = T2.BANKID and T1.BRANCHID = T2.BRANCHID)");
		sb.append(" WHEN MATCHED THEN");
		sb.append(" UPDATE SET");
		
		if(this.branchNameChanged){
			sb.append(" T1.BRANCHNAME = T2.BRANCHNAME,");
		}
		if(this.fatherBranchChanged){
			sb.append(" T1.FATHERBRANCH = T2.FATHERBRANCH,");
		}
		if(this.statusChanged){
			sb.append(" T1.STATUS = T2.STATUS,");
		}
		if(this.openDateChanged){
			sb.append(" T1.OPENDATE = T2.OPENDATE");
		}
		if(sb.substring(sb.length()-1).equals(",")){
			sb=sb.deleteCharAt(sb.length()-1);
		}
		sb.append(" WHEN NOT MATCHED THEN");
		sb.append(" insert");
		sb.append(" (BANKID,");
		sb.append(" BRANCHID,");
		sb.append(" BRANCHNAME,");
		sb.append(" FATHERBRANCH,");
		sb.append(" STATUS,");
		sb.append(" OPENDATE)");
	
		sb.append(" values");
		
		sb.append(" (T2.BANKID,");
		sb.append(" T2.BRANCHID,");
		sb.append(" T2.BRANCHNAME,");
		sb.append(" T2.FATHERBRANCH,");
		sb.append(" T2.STATUS,");
		sb.append(" T2.OPENDATE)");
		return new SQLStruct(sb.toString(), valueObj);
	}

}