package core.jdbc.sql;


/**
*
* <b>功能描述：</b><br>
* SQL语句输入输出数据项定义。<br>
* 
* <b>变量说明:</b><br>
*	dataName--SQL语句中的表列对应的Context数据名称<br>
*	dataType--数据类型，支持的类型有：CHAR,VARCHAR,LONGVARCHAR,INTEGER,DECIMAL等<<br>
*	paramIdx--数据项索引值<br>
*
* 
*   @创建时间 2001-03-02
*   @author  ZhongMingChang
*   @modifier GaoLin 2006-10-23
*   
*/

public class SQLQueryParameter extends SQLParameter{
	private String chnName = null;
	private String formatType = null;
	private String transferType = null;
	private String align = null;
	private String url = null;
	private String args = null;
	private String noPrint;
	private String isAutoTitle;
	public String getIsAutoTitle() {
		return isAutoTitle;
	}
	public void setIsAutoTitle(String isAutoTitle) {
		this.isAutoTitle = isAutoTitle;
	}
	public String getNoPrint() {
		return noPrint;
	}
	public void setNoPrint(String noPrint) {
		this.noPrint = noPrint;
	}

	private String contextUrl;

	public String getContextUrl() {
		return contextUrl;
	}
	public void setContextUrl(String contextUrl) {
		this.contextUrl = contextUrl;
	}
	
	private String width;
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	private int displayIdx = -1;

	public SQLQueryParameter() {
		super();
	}
	public void setAlign(String align) {
		this.align = align;
	}
	public void setArgs(String args) {
		this.args = args;
	}
	public void setChnName(String chnName) {
		this.chnName = chnName;
	}
	public void setDisplayIdx(int displayIdx) {
		this.displayIdx = displayIdx;
	}
	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}
	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAlign() {
		return align;
	}
	public String getArgs() {
		return args;
	}
	public String getChnName() {
		return chnName;
	}
	public int getDisplayIdx() {
		return displayIdx;
	}
	public String getFormatType() {
		return formatType;
	}
	public String getTransferType() {
		return transferType;
	}
	public String getUrl() {
		return url;
	}
}
