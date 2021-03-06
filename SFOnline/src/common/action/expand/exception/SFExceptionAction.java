package common.action.expand.exception;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.log.EMPLog;
import common.exception.SFException;
import common.util.SFUtil;

/**
 * SFExceptionAction.java<br>
 * EMP 交易步骤扩展<br>
 * Extends class EMPAction<br>
 * Created on  2010年03月12日14时17分50秒<br>
 * @autor        <br>

 * @emp:name 存管系统(SF)异常抛出
 * @emp:catalog 存管系统(SF)扩展ACTION
 * @emp:states 0=成功;-1=异常;
 * @emp:document 生成一个本系统异常抛出
 */
public class SFExceptionAction extends EMPAction {

	/*业务逻辑操作单元的执行入口*/
	public String execute(Context context) throws EMPException
	{
		String tmpErrCode = null, tmpErrMsg = null;
		
		/* 对输入参数进行检查: */
		if (errCode == null || errCode.length() <= 0 || errMsg == null
				|| errMsg.length() <= 0 ) {
			EMPLog.log("SFExceptionAction", EMPLog.ERROR, 0,
					"必须输入的参数不能为空!错误代码/错误信息："
							+ errCode + '/' + errMsg);
			throw new SFException("P0001S007",
					"必须输入的参数不能为空!错误代码/错误信息："
							+ errCode + '/' + errMsg);
		}
		
		tmpErrCode = SFUtil.getContextValueInAction(context,
				errCode);
		tmpErrMsg = SFUtil
				.getContextValueInAction(context, errMsg);
		
		EMPLog.log("SFExceptionAction", EMPLog.ERROR, 0, tmpErrCode + ':' + tmpErrMsg);
		throw new SFException(tmpErrCode, tmpErrMsg );
	}
	
	private String errCode;

 /**
  * @emp:name 错误码
  * @emp:desc 
  * @emp:mustSet true
  * @emp:attrType string
  * @emp:isAttribute true
  * @emp:valueList 
 */
	public void setErrCode(String newErrCode) {
		errCode = newErrCode;
	}
	public String getErrCode() {
		return errCode;
	}
	private String errMsg;

 /**
  * @emp:name 错误信息
  * @emp:desc 
  * @emp:mustSet true
  * @emp:attrType string
  * @emp:isAttribute true
  * @emp:valueList 
 */
	public void setErrMsg(String newErrMsg) {
		errMsg = newErrMsg;
	}
	public String getErrMsg() {
		return errMsg;
	}
}
