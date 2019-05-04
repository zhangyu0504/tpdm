package core.service;

import com.ecc.emp.component.factory.EMPFlowComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.flow.EMPFlow;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.service.EMPService;

/**
 * PBank提供的善后处理器，根据定义的条件，进行善后处理
 * Copyright (c) 2010 SDB
 * @author PBank
 * @version 1.0.0
 * @since 2010-02-23
 * @lastmodified 2010-02-23
 *
 */
public class PBankFinalHandler extends EMPService 
{
	/**
	 * 业务逻辑构件Id
	 */
	private String bizId;
	/**
	 * 业务逻辑流程对象Id
	 */
	private String opId;
	
	/**
	 * 本交易是否需要发起条件
	 */
	private String finalFormulaStr;

	/**
	 * 业务逻辑定义的组件工厂名称，在FinalHandler中通过此组件工程来获取业务逻辑处理流程定义
	 */
	private String factoryName = null;

	/**
	 * 得到处理业务逻辑构件对象Id
	 * @return
	 */
	public String getBizId() {
		return bizId;
	}

	/**
	 * 设置处理业务逻辑构件对象Id
	 * @param bizId
	 */
	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	/**
	 * 得到处理流程对象Id
	 * @return
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * 设置处理流程对象Id
	 * @param opId
	 */
	public void setOpId(String opId) {
		this.opId = opId;
	}

	/**
	 * 得到是否需要进行全局冲正处理的判断公式定义
	 * @return
	 */
	public String getFinalFormulaStr() {
		return finalFormulaStr;
	}

	/**
	 * 设置是否需要进行全局冲正处理的判断公式定义
	 * @param globalReversalFormulaStr
	 */
	public void setFinalFormulaStr(String finalFormulaStr) {
		this.finalFormulaStr = finalFormulaStr;
	}

	/**
	 * 得到组件工厂对象名称
	 * @return
	 */
	public String getFactoryName() {
		return factoryName;
	}

	/**
	 * 设置组件工厂对象名称
	 * @param factoryName
	 */
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * 判断是否需要执行本Flow
	 */
	public boolean isNeedExecuteFinal(Context context)
	{
		try {
			String flag = (String)context.getDataValue("_PBank_CHECKPOINT_FLAG");
			if (flag == null)
				return false;
			if (!flag.equals("Y"))
				return false; 
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * 流程处理
	 * 调用一个定义好的bizId.opId来完成具体的final流程处理。
	 * 
	 */
	public void doFinal(Context context) throws Exception{
		if (isNeedExecuteFinal(context)){
			try{
				EMPFlowComponentFactory factory = (EMPFlowComponentFactory)EMPFlowComponentFactory.getComponentFactory(factoryName);
				EMPFlow flow = factory.getEMPFlow( bizId );
				flow.execute(context, opId );
			}catch(Exception e)
			{
				EMPLog.log("PBankFinalHandler", EMPLog.ERROR, 0, "Failed to do the PBankFinal flow[" + bizId + "],op[" + opId + "]context value: " + context.getDataElement(), e);
				throw e;
			}
		}
	}

	/**
	 * 本对象的字符串输出定义
	 */
	public String toString()
	{
		return "PBankFinalHandle"  + " BIZ [" + bizId + "] OP [" + opId + "]"; 
	}
}
