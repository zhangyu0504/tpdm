package core.communication.access.esb;

/**
 * ESB发报参数配置
 * @author 汪华
 *
 */
public class ESBAccessParam {
	private String esbIp;//A股ESB地址
	private String esbPort;//A股ESB端口
	private String esbBranch;//A股ESB分行号
	private String esbUserId;//A股ESB柜员号
	private String esbKey;//A股ESB密钥
	private String esbICSInnerAcct;//A股ESB内部户
	private String esbInnerAcct;//A股ESB内部户
	private String esbInnerBranch;//A股ESB内部户分行号
	private int esbTimeOut;//A股ESB超时时间
	public String getEsbIp() {
		return esbIp;
	}
	public void setEsbIp(String esbIp) {
		this.esbIp = esbIp;
	}
	public String getEsbPort() {
		return esbPort;
	}
	public void setEsbPort(String esbPort) {
		this.esbPort = esbPort;
	}
	public String getEsbBranch() {
		return esbBranch;
	}
	public void setEsbBranch(String esbBranch) {
		this.esbBranch = esbBranch;
	}
	public String getEsbUserId() {
		return esbUserId;
	}
	public void setEsbUserId(String esbUserId) {
		this.esbUserId = esbUserId;
	}
	public String getEsbKey() {
		return esbKey;
	}
	public void setEsbKey(String esbKey) {
		this.esbKey = esbKey;
	}
	public String getEsbICSInnerAcct() {
		return esbICSInnerAcct;
	}
	public void setEsbICSInnerAcct(String esbICSInnerAcct) {
		this.esbICSInnerAcct = esbICSInnerAcct;
	}
	public String getEsbInnerAcct() {
		return esbInnerAcct;
	}
	public void setEsbInnerAcct(String esbInnerAcct) {
		this.esbInnerAcct = esbInnerAcct;
	}
	public String getEsbInnerBranch() {
		return esbInnerBranch;
	}
	public void setEsbInnerBranch(String esbInnerBranch) {
		this.esbInnerBranch = esbInnerBranch;
	}
	public int getEsbTimeOut() {
		return esbTimeOut;
	}
	public void setEsbTimeOut(int esbTimeOut) {
		this.esbTimeOut = esbTimeOut;
	}
	
	
}
