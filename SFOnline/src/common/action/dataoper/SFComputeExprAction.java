package common.action.dataoper;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
import common.services.ExpressCalculateService;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * <b>�������������ʽ��ֵ</b><br>
 * <b>����ʾ��:</b><br>
 * &lt;OpStep id=&quot;ComputeExpressionAction&quot;<br>
 * &nbsp;&nbsp;param=&quot;paraName&quot;<br>
 * &nbsp;&nbsp;exp=&quot;$(payAmt)-9+$(payAmt)*2/(10)&quot;<br>
 * &nbsp;&nbsp;implClass=&quot;com.ecc.emp.action.ComputeExpressionAction&quot;/
 * &gt;<br>
 * <br>
 * <p/>
 * <b>����˵����</b><br>
 * param����ű��ʽ��ֵ�Ľ����������<br>
 * exp����Ҫ��ı��ʽ<br>
 * 
 * �������� 2004-12-3 15:25:28<br/>
 * 
 * @author Dragon@ECC.
 */
public class SFComputeExprAction extends EMPAction {
	private ExpressCalculateService analysis = null;
	String param = null;

	String exp = null;

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	/**
	 * ��ʼ������
	 */
	private void initService(Context context) throws SFException {
		try {
			analysis = (ExpressCalculateService) context.getService((String) context.getDataValue(SFConst.SERVICE_EXPRESSCALC));
		} catch (EMPException e) {
			SFLogger.error(context,"��ʼ�����ʽ�������ʧ�ܣ�");
			throw new SFException(e.toString());
		}
	}

	/**
	 * @param operation
	 * @return
	 * @throws EMPException
	 */
	public String execute(Context context) throws SFException {
		initService(context);
		if (param == null || param.trim().equals("")) {
			throw new SFException("�������Ϸ�");
		}
		String paramName = param.substring(2, param.length() - 1);
		if (exp == null || exp.trim().equals("")) {
			throw new SFException("�������Ϸ�");
		}
		try {
			context.setDataValue(paramName, analysis.execute(exp, context));
		} catch (Exception e) {
			SFLogger.error(context,"��ʼ�����ʽ�������ʧ�ܣ�");
			throw new SFException("ִ�б��ʽʧ��!,["+e.toString()+"]");
		}
		return "0";
	}

}
