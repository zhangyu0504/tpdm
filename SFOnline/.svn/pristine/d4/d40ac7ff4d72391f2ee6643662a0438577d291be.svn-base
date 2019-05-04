package core.service;

import java.util.ArrayList;
import java.util.List;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.service.EMPService;
import common.exception.SFException;
import common.util.SFUtil;

/**
 * 
 * <b>功能描述：</b><br>
 * 使用BEANSHELL实现表达式计算。<br>
 * 
 * @创建时间 2010-01-02
 * @version 1.0
 * @author PBank
 * @modifier
 * 
 */

public class PBankExpressCalculate extends EMPService {
	private static ThreadLocal<Interpreter> bshInterpreter = new ThreadLocal<Interpreter>();

	public PBankExpressCalculate() {
	}

	/**
	 * 根据context中的数据计算表达式，修改此方法必须修改execute另外一个方法.
	 * 
	 * @param express
	 *            表达式.
	 * @param context
	 *            EMPContext
	 * @return 结果
	 * @throws SFException
	 */
	public Object execute(String expression, Context context)
			throws SFException, EMPException {
		String returnValue = null;
		int startIdx = 0, endIdx = 0, i = 0;
		String myExpression = expression;
		long beginTime = System.currentTimeMillis();

		EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute expression ["
				+ myExpression + "]");
		
		/*进行优化*/
		if (myExpression == null || "\"\"".equals(myExpression))
		{
			returnValue = "";
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-1 [" + returnValue + "]");
			return returnValue;
		}
		else if (myExpression.startsWith("\"") && myExpression.indexOf("\"", 1) == myExpression.length()-1){
			returnValue = myExpression.substring(1, myExpression.length()-1);
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-2 [" + returnValue + "]");
			return returnValue;

		}
		else if (myExpression.startsWith("$(") && myExpression.indexOf(")", 1) == myExpression.length()-1){
			returnValue = SFUtil.getContextValueInAction(context, myExpression);
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-3 [" + returnValue + "]");
			return returnValue;
		}
		
		List<String> feildsNameList = new ArrayList<String>();
		EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "begin ansisly expression["
				+ expression + "]");
		/* 获取表达式中的所有变量名 */
		while (true) {
			startIdx = myExpression.indexOf("$(", startIdx);

			if (startIdx < 0) {
				break;
			}
			endIdx = myExpression.indexOf(")", startIdx + 2);
			if (endIdx < 0) {
				throw new SFException("P0000S001", "解析表达式:[" + expression
						+ "]异常,无法匹配'$('相应之')'符号:["
						+ expression.substring(startIdx) + "]");
			}

			String fieldName = myExpression.substring(startIdx + 2, endIdx);
			if (!feildsNameList.contains(fieldName)) {
				feildsNameList.add(fieldName);
			}

			startIdx = endIdx;
		}

		/* 重构表达式字符串,并赋值bsh解释器 */
		try {
			Interpreter bshInterpreter = getInterpreter();
			for (i = 0; i < feildsNameList.size(); i++) {
				if (feildsNameList.get(i).equals("context")) {
					myExpression = myExpression.replace("$(context)", "context");
					bshInterpreter.set("context", context);
					EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "put name[context] by value[context] into bsh!");
				} else {
					DataElement dataElement = context.getDataElement(feildsNameList.get(i));
					if (DataField.class.isAssignableFrom(dataElement.getClass())) {
						bshInterpreter.set("arg" + i, context.getDataValue(feildsNameList.get(i)));
						EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, 
								"put name[" + feildsNameList.get(i) + "] for arg" + i +	" by value[" + 
								context.getDataValue(feildsNameList.get(i)) + "] into bsh!");
					}
					else {
						bshInterpreter.set("arg" + i, context.getDataElement(feildsNameList.get(i)));
						EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, 
								"put name[" + feildsNameList.get(i) + "] for arg" + i +	" by value[" + 
								context.getDataElement(feildsNameList.get(i)).getName() + "] into bsh!");
					}
					myExpression = myExpression.replace("$(" + feildsNameList.get(i) + ")", "arg" + i);
				}
			}

			/* 执行表达式 */
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "eval expression ["
					+ myExpression + "]");
			Object obj = bshInterpreter.eval(myExpression);
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "clear the namespace !!!");
			bshInterpreter.getNameSpace().clear();
			if (obj != null)
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result ["
						+ obj + "],type[" + obj.getClass().getName() + "]");
			else
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result is null");
			return obj;
		} catch (TargetError e) {
			Throwable te = e.getTarget();
			if (te instanceof EMPException) {
				throw (EMPException)te ;
			}
			else {
				throw new SFException("P0000S001", "解析表达式:[" + expression
						+ "]异常,BEANSHELL无法解析串:[" + myExpression + "]的值", e);
			}
		} catch (EMPException e) {
			throw new SFException("P0000S001", "解析表达式:[" + expression
					+ "]异常,无法设置变量[" + feildsNameList.get(i) + "]的值", e);
		} catch (EvalError e) {
			throw new SFException("P0000S001", "解析表达式:[" + expression
					+ "]异常,BEANSHELL无法解析串:[" + myExpression + "]的值", e);
		}
		finally {
			long endTime=System.currentTimeMillis()-beginTime;
			if (endTime > 20) {
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.INFO, 0, "Execute expression [" + expression +"], times= " + endTime, null);
			}
		}
	}

	/**
	 * 根据DataElement中的数据计算表达式，修改此方法必须修改execute另外一个方法.
	 * 
	 * @param express
	 *            表达式.
	 * @param dataElement
	 *            DataElement
	 * @return 结果
	 * @throws SFException
	 */
	public Object execute(String expression, DataElement dataElement)
			throws SFException, EMPException {
		String returnValue = null;
		int startIdx = 0, endIdx = 0, i = 0;
		String myExpression = expression;
		long beginTime = System.currentTimeMillis();
		KeyedCollection kColl = (KeyedCollection) dataElement;

		EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute expression ["
				+ myExpression + "]");
		
		/*进行优化*/
		if (myExpression == null || "\"\"".equals(myExpression))
		{
			returnValue = "";
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-1 [" + returnValue + "]");
			return returnValue;
		}
		else if (myExpression.startsWith("\"") && myExpression.indexOf("\"", 1) == myExpression.length()-1){
			returnValue = myExpression.substring(1, myExpression.length()-1);
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-2 [" + returnValue + "]");
			return returnValue;

		}
		else if (myExpression.startsWith("$(") && myExpression.indexOf(")", 1) == myExpression.length()-1){
			returnValue = kColl.getDataValue(myExpression.substring(2, myExpression.length() - 1)).toString();
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result-3 [" + returnValue + "]");
			return returnValue;
		}
		
		List<String> feildsNameList = new ArrayList<String>();
		EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "begin ansisly expression["
				+ expression + "]");
		/* 获取表达式中的所有变量名 */
		while (true) {
			startIdx = myExpression.indexOf("$(", startIdx);

			if (startIdx < 0) {
				break;
			}
			endIdx = myExpression.indexOf(")", startIdx + 2);
			if (endIdx < 0) {
				throw new SFException("P0000S001", "解析表达式:[" + expression
						+ "]异常,无法匹配'$('相应之')'符号:["
						+ expression.substring(startIdx) + "]");
			}

			String fieldName = myExpression.substring(startIdx + 2, endIdx);
			if (!feildsNameList.contains(fieldName)) {
				feildsNameList.add(fieldName);
			}

			startIdx = endIdx;
		}

		/* 重构表达式字符串,并赋值bsh解释器 */
		try {
			Interpreter bshInterpreter = getInterpreter();
			for (i = 0; i < feildsNameList.size(); i++) {
				if (feildsNameList.get(i).equals("context")) {
					myExpression = myExpression.replace("$(context)", "context");
					//暂时不支持直接使用$(Context)，如要支持，要修改此方法，暂时置为空指针
					bshInterpreter.set("context", null);
					EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.ERROR, 0, "err!put name[context] by value[null] into bsh, please check!");
				} else {
					DataElement aDataElement = kColl.getDataElement(feildsNameList.get(i));
					if (DataField.class.isAssignableFrom(aDataElement.getClass())) {
						bshInterpreter.set("arg" + i, kColl.getDataValue(feildsNameList.get(i)));
						EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, 
								"put name[" + feildsNameList.get(i) + "] for arg" + i +	" by value[" + 
								kColl.getDataValue(feildsNameList.get(i)) + "] into bsh!");
					}
					else {
						bshInterpreter.set("arg" + i, kColl.getDataElement(feildsNameList.get(i)));
						EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, 
								"put name[" + feildsNameList.get(i) + "] for arg" + i +	" by value[" + 
								kColl.getDataElement(feildsNameList.get(i)).getName() + "] into bsh!");
					}
					myExpression = myExpression.replace("$(" + feildsNameList.get(i) + ")", "arg" + i);
				}
			}

			/* 执行表达式 */
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "eval expression ["
					+ myExpression + "]");
			Object obj = bshInterpreter.eval(myExpression);
			EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "clear the namespace !!!");
			bshInterpreter.getNameSpace().clear();
			if (obj != null)
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result ["
						+ obj + "],type[" + obj.getClass().getName() + "]");
			else
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.DEBUG, 0, "execute result is null");
			return obj;
		} catch (TargetError e) {
			Throwable te = e.getTarget();
			if (te instanceof EMPException) {
				throw (EMPException)te ;
			}
			else {
				throw new SFException("P0000S001", "解析表达式:[" + expression
						+ "]异常,BEANSHELL无法解析串:[" + myExpression + "]的值", e);
			}
		} catch (EMPException e) {
			throw new SFException("P0000S001", "解析表达式:[" + expression
					+ "]异常,无法设置变量[" + feildsNameList.get(i) + "]的值", e);
		} catch (EvalError e) {
			throw new SFException("P0000S001", "解析表达式:[" + expression
					+ "]异常,BEANSHELL无法解析串:[" + myExpression + "]的值", e);
		}
		finally {
			long endTime=System.currentTimeMillis()-beginTime;
			if (endTime > 20) {
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.INFO, 0, "Execute expression [" + expression +"], times= " + endTime, null);
			}
		}
	}

	private Interpreter getInterpreter() {
		Interpreter bshInter = (Interpreter)bshInterpreter.get();
		if (bshInter == null) {
			synchronized (bshInterpreter) {
				bshInter = (Interpreter)bshInterpreter.get();
				if (bshInter == null) {
					bshInterpreter.set(new Interpreter());
				}
			}
			return (Interpreter)bshInterpreter.get();
		}
		else {
			return bshInter;
		}
	}
}
