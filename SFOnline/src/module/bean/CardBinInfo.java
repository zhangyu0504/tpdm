package module.bean;


/**
 * 深发展卡BIN信息
 * 
 * 记录深发展卡BIN信息
 * 
 * @author 吕超鸿
 *
 */
public class CardBinInfo {

	private String cardBin;//卡BIN
	private String memo;//备注
	
	private boolean memoChanged = false;
	
	public String getCardBin() {
		return cardBin;
	}
	public void setCardBin(String cardBin) {
		this.cardBin = cardBin;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
		this.memoChanged = true;
	}
	
	/**
	 * 重置所有changeFlag
	 */
	public void resetChangedFlag() {
		this.memoChanged = false;
	}
	
}
