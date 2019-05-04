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
 * <b>参数说明：</b><br>
 * exp1：表达式1<br>
 * exp2：表达式2<br>
 * 
 * 创建日期 2017-8-11 15:25:28<br/>
 * 
 */
public class SFCompareExprAction extends EMPAction {
	private ExpressCalculateService analysis = null;
	String exp1 = null;

	String exp2 = null;

	public String getExp1() {
		return exp1;
	}

	public void setExp1(String exp1) {
		this.exp1 = exp1;
	}

	public String getExp2() {
		return exp2;
	}

	public void setExp2(String exp2) {
		this.exp2 = exp2;
	}

	/**
	 * 初始化服务
	 */
	private void initService(Context context) throws SFException {
		SFLogger logger=SFLogger.getLogger(context);
		try {
			analysis = (ExpressCalculateService) context.getService((String) context.getDataValue(SFConst.SERVICE_EXPRESSCALC));
		} catch (EMPException e) {
			logger.info("初始化表达式计算服务失败！");
			throw new SFException(e.toString());
		}
	}

	/**
	 * @param operation
	 * @return
	 * @throws EMPException
	 */
	public String execute(Context context) throws SFException {
		SFLogger logger=SFLogger.getLogger(context);
		Object objExp1 = null, objExp2 = null;
		initService(context);
		if (exp1 == null || exp1.trim().equals("")) {
			throw new SFException("参数不合法");
		}
		if (exp2 == null || exp2.trim().equals("")) {
			throw new SFException("参数不合法");
		}
		
		try {
			objExp1 = analysis.execute(exp1, context);
			objExp2 = analysis.execute(exp2, context);
		} catch (Exception e) {
			logger.info("执行表达式失败！");
			throw new SFException(e.toString());
		}
		logger.info("exp1's result is [" + (objExp1 == null ? "null" : (objExp1.getClass().getName() + " : " + objExp1.toString())) + "]");
		logger.info("exp2's result is [" + (objExp2 == null ? "null" : (objExp2.getClass().getName() + " : " + objExp2.toString())) + "]");
		if (objExp1 == objExp2)
			return "0";

		if (objExp1 == null || objExp2 == null)
			return "1";

		if (objExp1.getClass() != objExp2.getClass())
			return "1";

		if (objExp1.equals(objExp2))
				return "0";
		
		return "1";
	}

}
