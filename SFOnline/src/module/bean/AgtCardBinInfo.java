package module.bean;


/**
 * 
 * 代理行卡BIN信息
 * 
 * @author 吕超鸿
 *
 */
public class AgtCardBinInfo {

	private String cardBin;//卡BIN
	private String bankId;//银行号
	
	private boolean cardBinChanged = false;
	private boolean bankIdChanged = false;
	
	public String getCardBin() {
		return cardBin;
	}
	public void setCardBin(String cardBin) {
		this.cardBin = cardBin;
		this.cardBinChanged = true;
	}
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
		this.bankIdChanged = true;
	}
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.cardBinChanged = false;
		this.bankIdChanged = false;
	}
	
}
