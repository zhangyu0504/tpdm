/*
* Classname: ContextData.java
* Version information (Serial ID: FINWARE_V3.5_EMP_2013100001)
* Date 2013-7-5
* Copyright notice (Author: QF.wulei, Area: New Product/Project)
* Description: Detail Information
* 2013 Nanjing Utan Info Technology CO., LTD. 
*/
package core.schedule;

import java.io.Serializable;
import java.util.HashMap;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.log.EMPLog;
import common.exception.SFException;
import common.util.SFConst;

/**
   * 全局上下文对象类
   * Serial NO: FINWARE_V3.5_EMP_2013100001
   * Date 2013-6-6 
   * @author QF.wulei
   * @version 1.0
   */
public class ContextData implements Serializable{
	private static final long serialVersionUID = 5681118087115566513L;
	
	public Context EMPContext;
	
	/**
	 * 带Context构造函数
	 */
	public ContextData(Context ct) {
		this.EMPContext = ct;
	}
	
	/**
	   * 获取字段值
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-7-1
	   * @author ex_xxkjb_hx002
	   * @version 1.0
	   * @param key
	   * @return
	 * @throws InvalidArgumentException 
	 * @throws ObjectNotFoundException 
	   */
	public String getFieldValue(String key) throws SFException {
		try {
			key = this.getContextFieldKey(key);
			return (String)this.EMPContext.getDataValue(key);
		} catch (Exception e) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, 0, EMPLog.ERROR, "获取上下文字段["+key+"]值失败！"+e.getMessage(), e);
			throw new SFException(e);
		}
	}
	/**
	 * 
	   * 获取对象类型的字段值
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-25
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @return
	   * @throws SFException
	 */
	public Object getObjectValue(String key) throws SFException {
		try {
			key = this.getContextFieldKey(key);
			Object value=this.EMPContext.getDataValue(key);
			return value;
		} catch (Exception e) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, 0, EMPLog.ERROR, "获取上下文字段["+key+"]值失败！"+e.getMessage(), e);
			throw new SFException(e);
		}
		
	}
	/**
	 * 
	   * 获取对象类型的字段值，如果字段不存在，返回null，不抛异常
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-25
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @return
	 */
	public Object getObjectValueOrNull(String key) {
		try {
			key = this.getContextFieldKey(key);
			Object value=this.EMPContext.getDataValue(key);
			return value;
		} catch (Exception e) {
//			EMPLog.log(SFConstance.DEFAULT_TRXCODE, 0, EMPLog.ERROR, "文字段["+key+"]在上下文中不存在，返回空值！");
			return null;
		}
	}
	/**
	   * 获取字段值,如果该字段在上下文不存在，则返回空值，不抛异常
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-7-16
	   * @author QF.wulei
	   * @version 1.0
	   * @param key
	   * @return
	   * @throws SFException
	   */
	public String getFieldValueUnNull(String key) {
		try {
			String value = "";
			key = this.getContextFieldKey(key);
			value = (String)this.EMPContext.getDataValue(key);
			if(value==null) return "";
			return value;
		} catch (Exception e) {
//			EMPLog.log(SFConstance.DEFAULT_TRXCODE, 0, EMPLog.ERROR, "文字段["+key+"]在上下文中不存在，返回空值！");
			return "";
		}
	}
	
	
	
	
	
	/**
	   * 从上下文和request中取数据，如果request中没有，则从上下文中取
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-13
	   * @author QF.wulei
	   * @version 1.0
	   * @param key
	   * @return
	   */
//	public String getFieldValueRoC(String key) {
//		String value = "";
//		//优先从request中找
//		value = getFieldValueFromRequest(key);
//		
//		//如果request中没找到，从上下文中找，如session中的数据
//		if (!this.verifyString(value)) {
//			value = getFieldValueUnNull(key);
//		}
//		
//		return value;
//	}
	
	/**
	   * 上下文字段赋值
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-7-3
	   * @author QF.wulei
	   * @version 1.0
	   * @param key
	   * @return
	   */
	public void setFieldValue(String key, Object value) throws SFException {
		try {
			key=this.getContextFieldKey(key);
			this.EMPContext.setDataValue(key, value);
		} catch (Exception e) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, 0, EMPLog.ERROR, " 设置上下文字段["+key+"]值失败！"+e.getMessage(), e);
			throw new SFException(e);
		}
	}
	

	
	/**
	 * 
	   * 判断是否包含某字段
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-26
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @return
	 */
	public boolean containsKey(String key) {
		return this.EMPContext.containsKey(key);
	}
	
	/**
	 * 
	   * 添加字段值
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-26
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @param value
	   * @throws SFException
	 */
	public void addFieldValue(String key,Object value) throws SFException {
		try {
			key = this.getContextFieldKey(key);
			this.EMPContext.setDataValue(key, value);
		} catch (Exception e) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, 0, EMPLog.ERROR, " 添加上下文字段["+key+"]值失败！"+e.getMessage(), e);
			throw new SFException(e);
		}
	}
	
	/**
	 * 
	   * 添加字段值
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-26
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @param value
	   * @throws SFException
	 */
	public void addFieldValueNotExit(String key,Object value) throws SFException {
		try {
			key = this.getContextFieldKey(key);
			if(this.EMPContext.containsKey(key)){
				this.EMPContext.setDataValue(key, value);
			}else{
				this.EMPContext.addDataField(key, value);
			}
			
		} catch (Exception e) {
			EMPLog.log(SFConst.DEFAULT_TRXCODE, 0, EMPLog.ERROR, " 添加上下文字段["+key+"]值失败！"+e.getMessage(), e);
			throw new SFException(e);
		}
	}
	/**
	   * 删除一个元素
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-7-10
	   * @author QF.wulei
	   * @version 1.0
	   * @param key
	   */
	public void remove(Object key) {
		this.EMPContext.remove(key);
	}
	
	public void putAll(HashMap m) throws SFException, EMPException{
		Object value = null;
		String tmpKey = null;
		for (Object key : m.keySet()) {
			if (key != null && key.toString().trim().length() != 0) {
				tmpKey = key.toString();
				key = this.getContextFieldKey(key.toString());
				value = m.get(tmpKey);
				if(null != value && !value.equals("")){
					if(this.EMPContext.containsKey(key)){
						this.EMPContext.setDataValue(key.toString(), value);
					}else{
						this.EMPContext.addDataField(key.toString(), value);
					}
				}
				
			}
		}
	}
	
	
	/**
	 * 
	   * 返回EMP Context字段值真实的KEY 
	   * Serial NO: FINWARE_V3.5_EMP_2013100001
	   * Date 2013-9-25
	   * @author liup
	   * @version 1.0
	   * @param key
	   * @return
	   * @throws SFException
	 */
	public String getContextFieldKey(String key) throws SFException
	{
		if (key == null || key.trim().length() == 0) {
			throw new SFException("The key:["+key+"] is null or empty!");
		}
		
//		/**将字段中的[_DOT_]替换成[.] */
//		if (key.indexOf(DataDictConstants.DATA_DICT_DOT_FLAG) > 0) {
//			key = key.replace(DataDictConstants.DATA_DICT_DOT_FLAG, DataDictConstants.DATA_DICT_DOT);
//		}
		return key;
	}
}
