package common.action.dataoper;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
import common.services.ExpressCalculateService;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * <b>功能描述：表达式求值</b><br>
 * <b>配置示例:</b><br>
 * &lt;OpStep id=&quot;ComputeExpressionAction&quot;<br>
 * &nbsp;&nbsp;param=&quot;paraName&quot;<br>
 * &nbsp;&nbsp;exp=&quot;$(payAmt)-9+$(payAmt)*2/(10)&quot;<br>
 * &nbsp;&nbsp;implClass=&quot;com.ecc.emp.action.ComputeExpressionAction&quot;/
 * &gt;<br>
 * <br>
 * <p/>
 * <b>参数说明：</b><br>
 * param：存放表达式求值的结果的数据域<br>
 * exp：所要求的表达式<br>
 * 
 * 创建日期 2004-12-3 15:25:28<br/>
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
	 * 初始化服务
	 */
	private void initService(Context context) throws SFException {
		try {
			analysis = (ExpressCalculateService) context.getService((String) context.getDataValue(SFConst.SERVICE_EXPRESSCALC));
		} catch (EMPException e) {
			SFLogger.error(context,"初始化表达式计算服务失败！");
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
			throw new SFException("参数不合法");
		}
		String paramName = param.substring(2, param.length() - 1);
		if (exp == null || exp.trim().equals("")) {
			throw new SFException("参数不合法");
		}
		try {
			context.setDataValue(paramName, analysis.execute(exp, context));
		} catch (Exception e) {
			SFLogger.error(context,"初始化表达式计算服务失败！");
			throw new SFException("执行表达式失败!,["+e.toString()+"]");
		}
		return "0";
	}

}
