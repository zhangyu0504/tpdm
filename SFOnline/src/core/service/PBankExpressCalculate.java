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
 * <b>����������</b><br>
 * ʹ��BEANSHELLʵ�ֱ��ʽ���㡣<br>
 * 
 * @����ʱ�� 2010-01-02
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
	 * ����context�е����ݼ�����ʽ���޸Ĵ˷��������޸�execute����һ������.
	 * 
	 * @param express
	 *            ���ʽ.
	 * @param context
	 *            EMPContext
	 * @return ���
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
		
		/*�����Ż�*/
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
		/* ��ȡ���ʽ�е����б����� */
		while (true) {
			startIdx = myExpression.indexOf("$(", startIdx);

			if (startIdx < 0) {
				break;
			}
			endIdx = myExpression.indexOf(")", startIdx + 2);
			if (endIdx < 0) {
				throw new SFException("P0000S001", "�������ʽ:[" + expression
						+ "]�쳣,�޷�ƥ��'$('��Ӧ֮')'����:["
						+ expression.substring(startIdx) + "]");
			}

			String fieldName = myExpression.substring(startIdx + 2, endIdx);
			if (!feildsNameList.contains(fieldName)) {
				feildsNameList.add(fieldName);
			}

			startIdx = endIdx;
		}

		/* �ع����ʽ�ַ���,����ֵbsh������ */
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

			/* ִ�б��ʽ */
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
				throw new SFException("P0000S001", "�������ʽ:[" + expression
						+ "]�쳣,BEANSHELL�޷�������:[" + myExpression + "]��ֵ", e);
			}
		} catch (EMPException e) {
			throw new SFException("P0000S001", "�������ʽ:[" + expression
					+ "]�쳣,�޷����ñ���[" + feildsNameList.get(i) + "]��ֵ", e);
		} catch (EvalError e) {
			throw new SFException("P0000S001", "�������ʽ:[" + expression
					+ "]�쳣,BEANSHELL�޷�������:[" + myExpression + "]��ֵ", e);
		}
		finally {
			long endTime=System.currentTimeMillis()-beginTime;
			if (endTime > 20) {
				EMPLog.log("PBankEXPRESSCALCULATE", EMPLog.INFO, 0, "Execute expression [" + expression +"], times= " + endTime, null);
			}
		}
	}

	/**
	 * ����DataElement�е����ݼ�����ʽ���޸Ĵ˷��������޸�execute����һ������.
	 * 
	 * @param express
	 *            ���ʽ.
	 * @param dataElement
	 *            DataElement
	 * @return ���
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
		
		/*�����Ż�*/
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
		/* ��ȡ���ʽ�е����б����� */
		while (true) {
			startIdx = myExpression.indexOf("$(", startIdx);

			if (startIdx < 0) {
				break;
			}
			endIdx = myExpression.indexOf(")", startIdx + 2);
			if (endIdx < 0) {
				throw new SFException("P0000S001", "�������ʽ:[" + expression
						+ "]�쳣,�޷�ƥ��'$('��Ӧ֮')'����:["
						+ expression.substring(startIdx) + "]");
			}

			String fieldName = myExpression.substring(startIdx + 2, endIdx);
			if (!feildsNameList.contains(fieldName)) {
				feildsNameList.add(fieldName);
			}

			startIdx = endIdx;
		}

		/* �ع����ʽ�ַ���,����ֵbsh������ */
		try {
			Interpreter bshInterpreter = getInterpreter();
			for (i = 0; i < feildsNameList.size(); i++) {
				if (feildsNameList.get(i).equals("context")) {
					myExpression = myExpression.replace("$(context)", "context");
					//��ʱ��֧��ֱ��ʹ��$(Context)����Ҫ֧�֣�Ҫ�޸Ĵ˷�������ʱ��Ϊ��ָ��
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

			/* ִ�б��ʽ */
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
				throw new SFException("P0000S001", "�������ʽ:[" + expression
						+ "]�쳣,BEANSHELL�޷�������:[" + myExpression + "]��ֵ", e);
			}
		} catch (EMPException e) {
			throw new SFException("P0000S001", "�������ʽ:[" + expression
					+ "]�쳣,�޷����ñ���[" + feildsNameList.get(i) + "]��ֵ", e);
		} catch (EvalError e) {
			throw new SFException("P0000S001", "�������ʽ:[" + expression
					+ "]�쳣,BEANSHELL�޷�������:[" + myExpression + "]��ֵ", e);
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
