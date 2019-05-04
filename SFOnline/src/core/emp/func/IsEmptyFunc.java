package core.emp.func;

import java.util.List;

import com.ecc.emp.log.EMPLog;
import com.ecc.util.formula.CFormula;
import com.ecc.util.formula.FormulaException;
import com.ecc.util.formula.FormulaValue;
import com.ecc.util.formula.function.Function;

/**
 * 判断是否为空字符串
 * 
 * @author Double J
 * 
 */
public class IsEmptyFunc extends Function {
	@Override
	public FormulaValue getValue(List argList) throws FormulaException {
		FormulaValue resValue = new FormulaValue();
		resValue.nDataType = FormulaValue.dtERROR;
		
		if (argList.size() != 1) {
			EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "当前函数只能接收一个参数！参数个数为：" + argList.size());
			return resValue;
		}
		FormulaValue stValue1 = ((CFormula) (argList.get(0))).getValue();

		Object element = stValue1.getValue();// 得到参数数据
		if (element == null) {
			EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "值为NULL");
			resValue.nDataType = FormulaValue.dtBOOL;
			resValue.bBooleanValue(true);
		} else {
			if (element.toString().length() == 0) {
				resValue.nDataType = FormulaValue.dtBOOL;
				resValue.bBooleanValue(true);
				EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "值不为NULL,长度为0,返回TRUE");
			} else {
				resValue.nDataType = FormulaValue.dtBOOL;
				resValue.bBooleanValue(false);
				EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "值不为NULL,返回FALSE");
			}
		}
		return resValue;
	}

}
