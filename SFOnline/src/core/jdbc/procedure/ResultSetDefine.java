package core.jdbc.procedure;

import java.util.ArrayList;
import java.util.List;

import core.jdbc.sql.SQLParameter;


/**
*
* <b>功能描述：</b><br>
* 存储过程返回的结果集与Context数据的对应关系定义。<br>
* 
* <b>变量说明:</b><br>
*	parameters--结果集数据列表。<br>
*	iCollName--返回结果集定义。<br>
*	isAppend--true：添加到已有结果集；false：添加到新结果集。<br>
*
*   @创建时间 2002-07-08
*   @author  ZhongMingChang
*   @modifier GaoLin 2006-10-26
*   
*/

public class ResultSetDefine {

	private ArrayList parameters=new ArrayList();

	private String iCollName = null;

	private boolean isAppend = false;
	
	private int paramIdx = 1; //1 based paramIdx

	public ResultSetDefine() {
		super();
	}

	public int getIdx() {
		return paramIdx;
	}

	public void setIdx(int paramIdx) {
		this.paramIdx = paramIdx;
	}

	public void addSQLParameter(SQLParameter param) {
		parameters.add(param);
	}
	
	public Object get(int index){
		return parameters.get(index);
	}
	
	public int size() {
		return parameters.size();
	}

	public String getICollName() {
		return this.iCollName;
	}

	public boolean getIsAppend() {
		return isAppend;
	}

	public List getParameters() {
		return parameters;
	}

	public void setICollName(String aName) {
		this.iCollName = aName;
	}

	public void setIsAppend(String isAppend) {
		if("true".equals(isAppend)){
			this.isAppend = true;
		}else{
			this.isAppend = false;
		}
	}

}
