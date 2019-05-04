package core.jdbc.procedure;

import java.util.ArrayList;
import java.util.List;

import core.jdbc.sql.SQLParameter;


/**
*
* <b>����������</b><br>
* �洢���̷��صĽ������Context���ݵĶ�Ӧ��ϵ���塣<br>
* 
* <b>����˵��:</b><br>
*	parameters--����������б�<br>
*	iCollName--���ؽ�������塣<br>
*	isAppend--true����ӵ����н������false����ӵ��½������<br>
*
*   @����ʱ�� 2002-07-08
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
