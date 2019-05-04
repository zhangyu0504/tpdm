package core.emp.func;

import java.util.List;

import com.ecc.emp.log.EMPLog;
import com.ecc.util.formula.CFormula;
import com.ecc.util.formula.FormulaException;
import com.ecc.util.formula.FormulaValue;
import com.ecc.util.formula.function.Function;

/**
 * �ж��Ƿ�Ϊ���ַ���
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
			EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "��ǰ����ֻ�ܽ���һ����������������Ϊ��" + argList.size());
			return resValue;
		}
		FormulaValue stValue1 = ((CFormula) (argList.get(0))).getValue();

		Object element = stValue1.getValue();// �õ���������
		if (element == null) {
			EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "ֵΪNULL");
			resValue.nDataType = FormulaValue.dtBOOL;
			resValue.bBooleanValue(true);
		} else {
			if (element.toString().length() == 0) {
				resValue.nDataType = FormulaValue.dtBOOL;
				resValue.bBooleanValue(true);
				EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "ֵ��ΪNULL,����Ϊ0,����TRUE");
			} else {
				resValue.nDataType = FormulaValue.dtBOOL;
				resValue.bBooleanValue(false);
				EMPLog.log("FBSIsEmptyFunction", EMPLog.DEBUG, 0, "ֵ��ΪNULL,����FALSE");
			}
		}
		return resValue;
	}

}
