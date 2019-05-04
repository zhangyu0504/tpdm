package common.action.dataoper;

import java.util.Iterator;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
/**
 * 给IColl中的数据项赋值，支持多个IColl，具有相同的DataField
 * 
 * 
 */
public class SFUpdateICollAction extends EMPAction {
	//IColl名称
	String iCollName = null;
	//context中的更新的数据来源
	String retrieveFields = null;
	//IColl中要更新的DataField名称
	String updateFields = null;

	public String getICollName() {
		return iCollName;
	}

	public void setICollName(String collName) {
		iCollName = collName;
	}

	public String getRetrieveFields() {
		return retrieveFields;
	}

	public void setRetrieveFields(String retrieveFields) {
		this.retrieveFields = retrieveFields;
	}

	public String getUpdateFields() {
		return updateFields;
	}

	public void setUpdateFields(String updateFields) {
		this.updateFields = updateFields;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String execute(Context context) throws SFException {
		
		String[] iCollNameArray = iCollName.split(";"); 
		for (int j = 0;j<iCollNameArray.length;j++){
			IndexedCollection iColl = null;
			try {
				iColl = (IndexedCollection) context.getDataElement(iCollNameArray[j]);
			} catch (EMPException e) {
				throw new SFException("没有从上下文中获取到对象:["+iCollNameArray[j]+"]");
			} 
			String[] updateFieldArray = updateFields.split(";");
			String[] retrieveFiledArray = retrieveFields.split(";");
			
			if(updateFieldArray.length != retrieveFiledArray.length){
				//TODO 异常类型要修改
				throw new SFException("ICollAction define error, paramater named op not set!");
			}
			for (int i = 0; i < iColl.size(); i++) {
				KeyedCollection kColl = (KeyedCollection) iColl.getElementAt(i);
				Iterator<String> keyIterator = kColl.keySet().iterator();
				for (Iterator<String> iterator = keyIterator; iterator.hasNext();) {
					String aKey = iterator.next();
					int index = getIndex(updateFieldArray,aKey);
					if(index != -1){
						Object value;
						try {
							value = context.getDataValue(retrieveFiledArray[index]);
							kColl.setDataValue(aKey, value);
						} catch (EMPException e) {
							throw new SFException("ICollAction define error, paramater named op not set!");
						}
					}
				}
			}
			
		}
		
		
		return "0";

	}
	
	private int getIndex(String[] strArray, String key){
		for (int i = 0; i < strArray.length; i++) {
			String string = strArray[i];
			if(key.equals(string)){
				return i;
			}

		}
		return -1;
	}

}
