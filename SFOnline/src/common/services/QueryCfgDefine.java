package common.services;

import java.util.List;

import core.jdbc.sql.SQLQueryParameter;

/**
 *  通用查询的配置定义服务，其定义如下：

	iCollName 代表有结果集查询
<FBSQueryCfgDefine id="TransferProce" SQLStr="" iCollName="" maxRecords="50" reqDataKey="RATE;CURRENCY" inputView="b.jsp" outputView="a.jsp">

		<input>		<!-- 输入参数定义 -->
			<param dataName="TellerID" idx="1" dataType="CHAR"/>
			<param dataName="data2" idx="2" dataType="CHAR"/>		
			<param dataName="data3" idx="3" dataType="CHAR"/>
		</input>
		
		<output>  <!-- 返回结果定义, 如果是无结果的，则不定义此节 -->
			<QParam idx="-1" dataName="button" dataType="CHAR" displayIdx="1" chnName="中文名" width="5" formatType="BUTTON" transferType="STATE" align="left" url="a.do" args="data1,data2,data3"/>
			<QParam idx="1" dataName="data1" dataType="CHAR" displayIdx="1" chnName="中文名" width="5" formatType="AMOUNT" transferType="STATE" align="left" url="a.do" args="data1,data2,data3"/>
		</output>
	
</FBSQueryCfgDefine>

 *
 *
 */

/**
*
* <b>功能描述：</b><br>
* 通用查询SQL语句和展现界面的定义类。<br>
*
*  @创建时间 2000-03-02
*  @version 1.0
*  @author  ZhongMingChang
*  @modifier GaoLin 2006-10-30
*
*/

public class QueryCfgDefine extends SQLDefine {
	final public static int QUERY_MAX_RECORDS = 5000; 
	
	private String reqDataKey = null;
	private String inputView = null;
	private String outputView = null;
	private String orderBySql = null;
	private String excelName = null;
	private String printDirect = null;
	private String printTitle = null;
	private String printDate = null;
	
	public String getPrintDate() {
		return printDate;
	}

	public void setPrintDate(String printDate) {
		this.printDate = printDate;
	}

	public String getPrintTitle() {
		return printTitle;
	}

	public void setPrintTitle(String printTitle) {
		this.printTitle = printTitle;
	}

	public String getPrintDirect() {
		return printDirect;
	}

	public void setPrintDirect(String printDirect) {
		this.printDirect = printDirect;
	}

	public String getExcelName() {
		return excelName;
	}

	public void setExcelName(String excelName) {
		this.excelName = excelName;
	}

	public String getOrderBySql() {
		return orderBySql;
	}

	public void setOrderBySql(String orderBySql) {
		this.orderBySql = orderBySql;
	}

	public QueryCfgDefine() {
		super();
		setSqlType("select");
		setICollName("ICOLL");
		setMaxRecords(QUERY_MAX_RECORDS);
	}

	public String getInputView() {
		return inputView;
	}

	public void setInputView(String inputView) {
		this.inputView = inputView;
	}

	public String getOutputView() {
		return outputView;
	}

	public void setOutputView(String outputView) {
		this.outputView = outputView;
	}

	public String getReqDataKey() {
		return reqDataKey;
	}

	public void setReqDataKey(String reqDataKey) {
		this.reqDataKey = reqDataKey;
	}
	
	public SQLQueryParameter getParamByDisplayIdx(int displayIdx) {
		List outputParams = getOutParamaters();
		for (int i = 0; i < outputParams.size(); i++) {
			SQLQueryParameter tmpSqlQueryParam = (SQLQueryParameter)outputParams.get(i);
			if (tmpSqlQueryParam.getDisplayIdx() == displayIdx) {
				return tmpSqlQueryParam;
			}
		}
		return null;
	}
}
