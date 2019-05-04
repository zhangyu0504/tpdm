package common.action.expand.exception;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.log.EMPLog;
import common.exception.SFException;
import common.util.SFUtil;

/**
 * SFExceptionAction.java<br>
 * EMP ���ײ�����չ<br>
 * Extends class EMPAction<br>
 * Created on  2010��03��12��14ʱ17��50��<br>
 * @autor        <br>

 * @emp:name ���ϵͳ(SF)�쳣�׳�
 * @emp:catalog ���ϵͳ(SF)��չACTION
 * @emp:states 0=�ɹ�;-1=�쳣;
 * @emp:document ����һ����ϵͳ�쳣�׳�
 */
public class SFExceptionAction extends EMPAction {

	/*ҵ���߼�������Ԫ��ִ�����*/
	public String execute(Context context) throws EMPException
	{
		String tmpErrCode = null, tmpErrMsg = null;
		
		/* ������������м��: */
		if (errCode == null || errCode.length() <= 0 || errMsg == null
				|| errMsg.length() <= 0 ) {
			EMPLog.log("SFExceptionAction", EMPLog.ERROR, 0,
					"��������Ĳ�������Ϊ��!�������/������Ϣ��"
							+ errCode + '/' + errMsg);
			throw new SFException("P0001S007",
					"��������Ĳ�������Ϊ��!�������/������Ϣ��"
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
  * @emp:name ������
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
  * @emp:name ������Ϣ
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