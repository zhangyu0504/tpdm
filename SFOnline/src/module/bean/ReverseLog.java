package module.bean;
/**
 * 冲正日志
 * @author 汪华
 *
 */
public class ReverseLog {
	private String macDate;
	private String macTime;
	private String txSeqId;
	private String subTxSeqId;
	private String seqId;
	private String reverseSeqId;
	private String respCode;
	private String respMsg;
	private String memo;
	public String getMacDate() {
		return macDate;
	}
	public void setMacDate(String macDate) {
		this.macDate = macDate;
	}
	public String getMacTime() {
		return macTime;
	}
	public void setMacTime(String macTime) {
		this.macTime = macTime;
	}
	public String getTxSeqId() {
		return txSeqId;
	}
	public void setTxSeqId(String txSeqId) {
		this.txSeqId = txSeqId;
	}
	public String getSubTxSeqId() {
		return subTxSeqId;
	}
	public void setSubTxSeqId(String subTxSeqId) {
		this.subTxSeqId = subTxSeqId;
	}
	public String getSeqId() {
		return seqId;
	}
	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}
	public String getReverseSeqId() {
		return reverseSeqId;
	}
	public void setReverseSeqId(String reverseSeqId) {
		this.reverseSeqId = reverseSeqId;
	}
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	
}
