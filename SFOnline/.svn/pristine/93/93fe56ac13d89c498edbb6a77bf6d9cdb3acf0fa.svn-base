package module.bean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import common.exception.SFException;
import common.sql.bean.SQLStruct;

/**
 * 合作行信息实体类
 * @author 吕超鸿
 *
 */
public class AgtAgentInfo {

	private static final long serialVersionUID = 1L;
	
	private String bankId;//银行号
	private String bankName;//银行名称
	private boolean bankNameChanged=false;
	private String branchCode;//分行号
	private boolean branchCodeChanged=false;
	private String bankAcct;//银行账号
	private boolean bankAcctChanged=false;
	private String warnFlag;//警告标识
	private boolean warnFlagChanged=false;
	private String warnMoney;//警告金额
	private boolean warnMoneyChanged=false;
	private String bankIp;//银行IP地址
	private boolean bankIpChanged=false;
	private String bankPort;//银行端口
	private boolean bankPortChanged=false;
	private String ftpFlag;//ftp标识
	private boolean ftpFlagChanged=false;
	private String fsIp;//服务器IP地址
	private boolean fsIpChanged=false;
	private String fsPort;//服务器端口
	private boolean fsPortChanged=false;
	private String ftpUser;//FTP用户名
	private boolean ftpUserChanged=false;
	private String ftpPass;//FTP密码
	private boolean ftpPassChanged=false;
	private String desFlag;
	private boolean desFlagChanged=false;
	private String agentPath;//代理地址
	private boolean agentPathChanged=false;
	private String mackey;//mac密钥
	private boolean mackeyChanged=false;
	private String pinkey;//pin密钥
	private boolean pinkeyChanged=false;
	private String pinFlag;//pin标识
	private boolean pinFlagChanged=false;
	private String status;//状态
	private boolean statusChanged = false;
	private String openDate;//开立日期
	private boolean openDateChanged=false;
	private String macFlag;//mac标识
	private boolean macFlagChanged=false;
	

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
		this.bankNameChanged=true;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
		this.branchCodeChanged=true;

	}

	public String getBankAcct() {
		return bankAcct;
	}

	public void setBankAcct(String bankAcct) {
		this.bankAcct = bankAcct;
		this.bankAcctChanged=true;
	}

	public String getWarnFlag() {
		return warnFlag;
	}

	public void setWarnFlag(String warnFlag) {
		this.warnFlag = warnFlag;
		this.warnFlagChanged=true;

	}

	public String getWarnMoney() {
		return warnMoney;
	}

	public void setWarnMoney(String warnMoney) {
		this.warnMoney = warnMoney;
		this.warnMoneyChanged=true;

	}

	public String getBankIp() {
		return bankIp;
	}

	public void setBankIp(String bankIp) {
		this.bankIp = bankIp;
		this.bankIpChanged=true;

	}

	public String getBankPort() {
		return bankPort;
	}

	public void setBankPort(String bankPort) {
		this.bankPort = bankPort;
		this.bankPortChanged=true;

	}

	public String getFtpFlag() {
		return ftpFlag;
	}

	public void setFtpFlag(String ftpFlag) {
		this.ftpFlag = ftpFlag;
		this.ftpFlagChanged=true;

	}

	public String getFsIp() {
		return fsIp;
	}

	public void setFsIp(String fsIp) {
		this.fsIp = fsIp;
		this.fsIpChanged=true;

	}

	public String getFsPort() {
		return fsPort;
	}

	public void setFsPort(String fsPort) {
		this.fsPort = fsPort;
		this.fsPortChanged=true;

	}

	public String getFtpUser() {
		return ftpUser;
	}

	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
		this.ftpUserChanged=true;

	}

	public String getFtpPass() {
		return ftpPass;
	}

	public void setFtpPass(String ftpPass) {
		this.ftpPass = ftpPass;
		this.ftpPassChanged=true;

	}

	public String getDesflag() {
		return desFlag;
	}

	public void setDesflag(String desFlag) {
		this.desFlag = desFlag;
		this.desFlagChanged=true;

	}

	public String getAgentPath() {
		return agentPath;
	}

	public void setAgentPath(String agentPath) {
		this.agentPath = agentPath;
		this.agentPathChanged=true;

	}

	public String getMackey() {
		return mackey;
	}

	public void setMackey(String mackey) {
		this.mackey = mackey;
		this.mackeyChanged=true;

	}

	public String getPinkey() {
		return pinkey;
	}

	public void setPinkey(String pinkey) {
		this.pinkey = pinkey;
		this.pinkeyChanged=true;

	}

	public String getPinFlag() {
		return pinFlag;
	}

	public void setPinFlag(String pinFlag) {
		this.pinFlag = pinFlag;
		this.pinFlagChanged=true;

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
		this.openDateChanged=true;

	}

	public String getMacFlag() {
		return macFlag;
	}

	public void setMacFlag(String macFlag) {
		this.macFlag = macFlag;
		this.macFlagChanged=true;

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
	
	/**
	 * 获取新增或修改SQL
	 * @return
	 */
	public SQLStruct getSaveAgtAgentInfoSQLConstruct()throws SFException{
		StringBuffer sb=new StringBuffer();
		Object[] valueObj=getFieldValues();
		sb.append(" MERGE INTO AGT_AGENTINFO T1");
		sb.append(" USING (select ? as BANKID,? as BANKNAME,? as BRANCHCODE,? as BANKACCT,? as WARNFLAG,? as WARNMONEY,");
		sb.append("? as BANKIP,? as BANKPORT,? as FTPFLAG,? as FSIP,? as FSPORT,? as FTPUSER,? as FTPPASS,? as DESFLAG,");
		sb.append("? as AGENTPATH,? as MACKEY,? as PINKEY,? as PINFLAG,? as STATUS,? as OPENDATE,? as MACFLAG");
		sb.append(" from dual) T2");
		sb.append(" ON (T1.BANKID = T2.BANKID)");
		sb.append(" WHEN MATCHED THEN");
		sb.append(" UPDATE SET");
		if(this.bankNameChanged){
			sb.append("	T1.BANKNAME = T2.BANKNAME,");
		}
		if(this.branchCodeChanged){
			sb.append("	T1.BRANCHCODE = T2.BRANCHCODE,");
		}
		if(this.bankAcctChanged){
			sb.append("	T1.BANKACCT = T2.BANKACCT,");
		}
		if(this.warnFlagChanged){
			sb.append("	T1.WARNFLAG = T2.WARNFLAG,");
		}
		if(this.warnMoneyChanged){
			sb.append("	T1.WARNMONEY = T2.WARNMONEY,");
		}
		if(this.bankIpChanged){
			sb.append("	T1.BANKIP = T2.BANKIP,");
		}
		if(this.bankPortChanged){
			sb.append("	T1.BANKPORT = T2.BANKPORT,");
		}
		if(this.ftpFlagChanged){
			sb.append("	T1.FTPFLAG = T2.FTPFLAG,");
		}
		if(this.fsIpChanged){
			sb.append("	T1.FSIP = T2.FSIP,");
		}
		if(this.fsPortChanged){
			sb.append("	T1.FSPORT = T2.FSPORT,");
		}
		if(this.ftpUserChanged){
			sb.append("	T1.FTPUSER = T2.FTPUSER,");
		}
		if(this.ftpPassChanged){
			sb.append("	T1.FTPPASS = T2.FTPPASS,");
		}
		if(this.desFlagChanged){
			sb.append("	T1.DESFLAG = T2.DESFLAG,");
		}
		if(this.agentPathChanged){
			sb.append("	T1.AGENTPATH = T2.AGENTPATH,");
		}
		if(this.mackeyChanged){
			sb.append("	T1.MACKEY = T2.MACKEY,");
		}
		if(this.pinkeyChanged){
			sb.append("	T1.PINKEY = T2.PINKEY,");
		}
		if(this.pinFlagChanged){		
			sb.append("	T1.PINFLAG = T2.PINFLAG,");
		}
		if(this.statusChanged){
			sb.append("	T1.STATUS = T2.STATUS,");
		}
		if(this.openDateChanged){
			sb.append("	T1.OPENDATE = T2.OPENDATE,");
		}
		if(this.macFlagChanged){
			sb.append("	T1.MACFLAG = T2.MACFLAG");
		}
		
		if(sb.substring(sb.length()-1).equals(",")){
			sb=sb.deleteCharAt(sb.length()-1);
		}
		sb.append(" WHEN NOT MATCHED THEN");
		sb.append("  insert");
		sb.append("(BANKID,BANKNAME,BRANCHCODE,BANKACCT,WARNFLAG,WARNMONEY,BANKIP,BANKPORT,FTPFLAG,FSIP,FSPORT,");
		sb.append("FTPUSER,FTPPASS,DESFLAG,AGENTPATH,MACKEY,PINKEY,PINFLAG,STATUS,OPENDATE,MACFLAG)");
		sb.append(" values");
		sb.append("(T2.BANKID,T2.BANKNAME,T2.BRANCHCODE,T2.BANKACCT,T2.WARNFLAG,T2.WARNMONEY,T2.BANKIP,T2.BANKPORT,");
		sb.append("T2.FTPFLAG,T2.FSIP,T2.FSPORT,T2.FTPUSER,T2.FTPPASS,T2.DESFLAG,T2.AGENTPATH,T2.MACKEY,T2.PINKEY,");
		sb.append("T2.PINFLAG,T2.STATUS,T2.OPENDATE,T2.MACFLAG)");
		
		return new SQLStruct(sb.toString(), valueObj);		

	}
	/**
	 * 重置所有chagedFlag
	 */
	public void resetChangedFlag(){
		this.bankNameChanged=false;
		this.branchCodeChanged=false;
		this.bankAcctChanged=false;
		this.warnFlagChanged=false;
		this.warnMoneyChanged=false;
		this.bankIpChanged=false;
		this.bankPortChanged=false;
		this.ftpFlagChanged=false;
		this.fsIpChanged=false;
		this.fsPortChanged=false;
		this.ftpUserChanged=false;
		this.ftpPassChanged=false;
		this.desFlagChanged=false;
		this.agentPathChanged=false;
		this.mackeyChanged=false;
		this.pinkeyChanged=false;
		this.pinFlagChanged=false;
		this.statusChanged=false;
		this.openDateChanged=false;
		this.macFlagChanged=false;
		
	}
}