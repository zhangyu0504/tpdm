package core.jdbc.procedure;

import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.jdbc.sql.SQLParameterCollection;
import com.ecc.emp.service.EMPService;

/**
*
* <b>功能描述：</b><br>
* 数据库存储过程定义类，定义了存储过程的输入和返回数据集。<br>
* 
* <b>配置示例：</b><br>
* &lt;JDBCProcedureDefine  id="JDBCProcedureDefine"<br>
* &nbsp;&nbsp;procedureName="Package.Procedure"<br>
* &nbsp;&nbsp;retCodeName="retCode"/&gt;<br>
* &nbsp;&lt;!--存储过程接口顺序：输入场，输出场，cursor1,cursor2...--&gt; <br>
* &nbsp;&lt;!--输入场定义--&gt;<br>
* &nbsp;&nbsp;&lt;input&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;!--输入场有序列举--&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp;&nbsp;&lt;/input&gt;<br>
* &nbsp;&lt;!--输出场定义--&gt;<br>
* &nbsp;&nbsp;&lt;output&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;!--输出场有序枚举--&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="retCode" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&lt;/output&gt;<br>
* &nbsp; &nbsp;&lt;!--输出场中的cursor有序枚举:即可以定义多个resultSet--&gt;<br>
* &nbsp; &nbsp;&lt;resultSet iCollName="IndexedCollection0" isAppend="true"&gt;
* &nbsp; &nbsp;&lt;!--cursor中的输出场有序枚举--&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&lt;/resultSet&gt;<br>
* &nbsp; &nbsp;&lt;resultSet iCollName="IndexedCollection1" isAppend="true"&gt;
* &nbsp; &nbsp;&lt;!--cursor中的输出场有序枚举--&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&nbsp;&lt;param dataName="dataName" dataType="dataType"/&gt;<br>
* &nbsp; &nbsp;&lt;/resultSet&gt;<br>
* &lt;/JDBCProcedureDefine&gt;<br>
* 
* <b>参数说明:</b><br>
*  &nbsp; &nbsp; procedureName--存储过程名称<br>
*  &nbsp; &nbsp; retCodeName--存储过程返回值在结果定义中对应的数据项名称<br>
*  &nbsp; &nbsp; input--存储过程输入数据定义<br>
*  &nbsp; &nbsp; output--存储过程返回结果定义<br>
*  &nbsp; &nbsp; resultSet--存储过程返回结果集定义<br>
*  &nbsp; &nbsp; iCollName--结果集对应的数据集名称<br>
*  &nbsp; &nbsp; isAppend--true：添加到已有结果集；false：添加到新结果集<br>
*
*  @创建时间 2000-03-02
*  @version 1.0
*  @modifier GaoLin 2006-10-30
*
*/

public class JDBCProcedureDefine extends EMPService {

	private List inParams = new ArrayList();

	private List outParams = new ArrayList();

	private List resultSets = new ArrayList();
	
	private String procedureName = null;

	public String errorCodeField = null;
	
	public String retCodeName=null;

	/**
	 * 批量执行存储过程的输入数据集合定义
	 */
	private String iCollName = null;
	
	/**
	 * 是否批量执行存储过程
	 */
	private boolean isBatch=false;
	
	
	public JDBCProcedureDefine() {
		super();
	}

	public void setInput(SQLParameterCollection col) {
		inParams = col;
	}

	public void setOutput(SQLParameterCollection col) {
		outParams = col;
	}
	
	public void addResultSet(ResultSetDefine resultSet){
		resultSets.add(resultSet);
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public List getInParams() {
		return inParams;
	}

	public List getOutParams() {
		return outParams;
	}

	public List getResultSets() {
		return resultSets;
	}
	
	public void setRetCodeName(String retCodeName) {
		this.retCodeName = retCodeName;
	}

	public void setErrorCodeField(String errorCodeField) {
		this.errorCodeField = errorCodeField;
	}

	/**
	 * 设置是否批量执行。
	 * 
	 * @param isBatchStr 是否批量执行
	 * @emp:isAttribute true
	 * @emp:name 是否批量执行
	 * @emp:desc 批量执行存储过程的标识，若为批量执行则遇到错误就停止
	 * @emp:valueList true=批量;false=不批量
	 * @emp:defaultValue false
	 */
	public void setIsBatch(String isBatchStr) {
		if("batch".equals(isBatchStr) || "true".equals(isBatchStr))
			this.isBatch = true;
		else
			this.isBatch = false;
	}
	public boolean getIsBatch() {
		return isBatch;
	}
	
	/**
	 * 设置批量执行存储过程的输入数据集合定义。
	 * 
	 * @param collName 输入数据集合名
	 * @emp:isAttribute true
	 * @emp:name 输入数据集合名
	 * @emp:desc 批量执行存储过程的输入数据集合定义
	 * @emp:editClass com.ecc.ide.editor.transaction.DataNamePropertyEditor
	 */
	public void setICollName(String collName) {
		iCollName = collName;
	}
	public String getICollName() {
		return iCollName;
	}
}
