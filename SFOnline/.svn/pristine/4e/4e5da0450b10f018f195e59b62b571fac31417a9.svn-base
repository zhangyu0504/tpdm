package core.service;

/**
 *  数据库SQL定义服务，其定义如下：

	iCollName 代表有结果集查询
<JDBCSQLDef id="TransferProce" SQLStr="" iCollName="" maxRecords="50" >

		<input>		<!-- 输入参数定义 -->
			<param dataName="TellerID" idx="1" dataType="CHAR"/>
			<param dataName="data2" idx="2" dataType="CHAR"/>		
			<param dataName="data3" idx="3" dataType="CHAR"/>
		</input>
		
		<result>  <!-- 返回结果定义, 如果是无结果的，则不定义此节 -->
			<param dataName="TellerID" idx="1" dataType="CHAR"/>
			<param dataName="data4" idx="2" dataType="CHAR"/>		
			<param dataName="data5" idx="3" dataType="CHAR"/>
		</result>
	
</JDBCSQLDef>

 *
 *
 */

import java.util.List;
import com.ecc.emp.jdbc.sql.SQLParameterCollection;
import com.ecc.emp.service.EMPService;

/**
*
* <b>功能描述：</b><br>
* 数据库SQL语句定义类。<br>
* 
* <b>配置示例：</b><br>
* &nbsp;&lt;JDBCSQLDef id=" JDBCSQLDef"<br>
* &nbsp;&nbsp;&nbsp;SQLStr="Select table0.column1, table0. column2, table0. column3 From table0 Where Select table0.column0= ? " <br>
* &nbsp;&nbsp;&nbsp;sqlType="select" <br>
* &nbsp;&nbsp;&nbsp;iCollName="indexedCollection0"<br>
* &nbsp;&nbsp;&nbsp;maxRecords="10"&gt;<br>
* &nbsp;&lt;!--输入数据集定义--&gt; <br>
* &nbsp;&lt;input&gt;<br>
* &nbsp;&nbsp;&nbsp;&lt;!--输入数据有序列举--&gt;<br>
* &nbsp;&nbsp;&nbsp;&lt;param idx="1" dataName="data0" dataType="VARCHAR"/&gt;<br>
* &nbsp;&lt;/input&gt;<br>
* &nbsp;&lt;!--输出数据集定义--&gt; <br>
* &nbsp;&lt;output&gt;<br>
* &nbsp;&nbsp;&lt;!--输出数据有序列举--&gt;<br>
* &nbsp;&nbsp;&nbsp;&lt;param idx="1" dataName="data1" dataType="VARCHAR"/&gt;<br>
* &nbsp;&nbsp;&nbsp;&lt;param idx="2" dataName="data2" dataType="VARCHAR"/&gt;<br>
* &nbsp;&nbsp;&nbsp;&lt;param idx="3" dataName="data3" dataType="VARCHAR"/&gt;<br>
* &nbsp;&lt;/output&gt;<br>
* &nbsp;&lt;/JDBCSQLDef&gt;<br>
* 
* <b>参数说明:</b><br>
* &nbsp;&nbsp;SQLStr--待执行的SQL语句<br>
* &nbsp;&nbsp;sqlType--SQL语句的操作类型<br>
* &nbsp;&nbsp;iCollName--输出数据集所对应的IndexedCollection名称<br>
* &nbsp;&nbsp;maxRecords--返回数据集合的最大数量限制<br>
* &nbsp;&nbsp;input--输入数据集定义<br>
* &nbsp;&nbsp;output--输出数据集定义<br>
* &nbsp;&nbsp;param--数据项定义<br>
*
*  @创建时间 2000-03-02
*  @version 1.0
*  @author  ZhongMingChang
*  @modifier GaoLin 2006-10-30
*
*/

public class PBankSqlDefine extends EMPService {
	
	final public static int SQL_INSERT = 0;
	final public static int SQL_DELETE = 1;
	final public static int SQL_UPDATE = 2;
	final public static int SQL_SELECT = 3;
	
	private String SQLStr = null;
	private String iCollName = null;
	private String rowsDataName = null;
	private String sqlHint = null;
	private int maxRecords = -1;
	private int rowsBatchCommit = -1;
	
	private List inParams = null;
	private List outParams = null;

	private String errorCode;
	private String errorMessage;
	
	private int sqlType = -1;
	
	private boolean isAppend = false;

	private boolean paraBind = true;

	public boolean isParaBind() {
		return paraBind;
	}

	public void setParaBind(boolean paraBind) {
		this.paraBind = paraBind;
	}

	public void setInput(SQLParameterCollection col)
	{
		inParams = col;
	}
	
	public void setOutput(SQLParameterCollection col)
	{
		outParams = col;
	}
	
	public int getSqlType()
	{
		return sqlType;
	}
	
	public void setSqlType(String value)
	{
		if( "INSERT".equals( value.toUpperCase() ))
			sqlType = SQL_INSERT;

		else if( "DELETE".equals( value.toUpperCase() ))
			sqlType = SQL_DELETE;
		
		else if( "UPDATE".equals( value.toUpperCase() ))
			sqlType = SQL_UPDATE;
		
		else if( "SELECT".equals( value.toUpperCase() ))
			sqlType = SQL_SELECT;
		
	}
	
	public String getSQLStr()
	{
		return SQLStr;
	}

	public void setSQLStr(String value )
	{
		SQLStr = value;
	}
	
	public List getInParamaters()
	{
		return this.inParams;
	}
	
	public List getOutParamaters()
	{
		return this.outParams;
	}

	public String getICollName()
	{
		return iCollName;
	}
	
	public void setICollName(String value )
	{
		iCollName = value;
	}

	public int getMaxRecords()
	{
		return maxRecords;
	}
	
	public void setMaxRecords(int value)
	{
		maxRecords = value;
	}
	
	public int getRowsBatchCommit()
	{
		return rowsBatchCommit;
	}
	
	public void setRowsBatchCommit(int value)
	{
		rowsBatchCommit = value;
	}

	public void setErrorCode(String value )
	{
		errorCode = value;
	}
	
	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String value )
	{
		errorMessage  = value;
	}

	public String getRowsDataName() {
		return rowsDataName;
	}

	public void setRowsDataName(String rowsDataName) {
		this.rowsDataName = rowsDataName;
	}

	public boolean getIsAppend() {
		return isAppend;
	}

	public void setIsAppend(String isAppend) {
		if("true".equals(isAppend)){
			this.isAppend = true;
		}else{
			this.isAppend = false;
		}
	}

	public String getSqlHint()
	{
		return sqlHint;
	}
	
	public void setSqlHint(String value )
	{
		sqlHint = value;
	}
}
