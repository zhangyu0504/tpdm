package common.action.dataoper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
import common.services.ExpressCalculateService;
import common.util.SFConst;

import core.log.SFLogger;

public class SFInsIcollAction extends EMPAction {
	private boolean flag = false;
	private String retrieveFields;
	private String updateFields;

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
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

	private String icollName;

	public String getIcollName() {
		return icollName;
	}

	public void setIcollName(String icollName) {
		this.icollName = icollName;
	}

	public String execute(Context context) throws SFException {
		SFLogger logger=SFLogger.getLogger(context);
		ExpressCalculateService analysis = null;
		IndexedCollection iColl = null;
		try {
			analysis = (ExpressCalculateService) context
					.getService((String) context
							.getDataValue(SFConst.SERVICE_EXPRESSCALC));
			iColl = (IndexedCollection) context.getDataElement(icollName);
		} catch (EMPException e) {
			logger.info("获取指定上下文对象失败！");
			throw new SFException(e.toString());
		}
		List retrieveFieldArray = (ArrayList) this.getList(retrieveFields);
		List updateFieldArray = (ArrayList) this.getList(updateFields);
		if (flag) {
			iColl.removeAll();
		}
		KeyedCollection kColl = (KeyedCollection) iColl.getDataElement();
		kColl = (KeyedCollection) kColl.clone();
		iColl.addDataElement(kColl);
		for (int i = 0; i < updateFieldArray.size(); i++) {
			Object value = analysis.execute(retrieveFieldArray.get(i).toString(), context);
			try {
				kColl.setDataValue(updateFieldArray.get(i).toString(), value);
			} catch (EMPException e) {
				logger.info("获取指定上下文对象失败！");
				throw new SFException(e.toString());
			}
		}
		return "0";
	}

	private List getList(String str) {
		List<Object> retList = new ArrayList<Object>();
		
		StringTokenizer stName = new StringTokenizer(str, "|");
		while (stName.hasMoreTokens()) {
			retList.add(stName.nextElement());
		}
		
		return retList;
	}
}
