package core.service;

import java.util.List;

import com.ecc.emp.accesscontrol.AccessInfo;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.DataField;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.service.EMPService;

import core.jdbc.procedure.JDBCProcedureDefine;
import core.jdbc.procedure.ResultSetDefine;
import core.jdbc.sql.SQLParameter;

/**
*
* <b>功能描述：</b><br>
* 将PBankSqlDefine定义的输入输出自动组装生成Context。<br>
* 
*  @创建时间 2010-01-02
*  @version 1.0
*  @author  PBank
*  @modifier 
*
*/

public class PBankGenContextBySql extends EMPService {

	AccessInfo accessInfo = new AccessInfo();
	Object syncObj = new Object();
	
	public Context getContext(Context appContext, String sqlDefineName) {
		
		Context resultContext = null;
		// 更新访问信息
		synchronized (syncObj) {
			this.accessInfo.newAccess();
		}
		long beginTime=System.currentTimeMillis();
		EMPService aDef = (EMPService) appContext.getService(sqlDefineName);
		
		if (PBankSqlDefine.class.isAssignableFrom(aDef.getClass()))
		{
			PBankSqlDefine sqlDefInfo = (PBankSqlDefine) aDef;
			
			List inParams = sqlDefInfo.getInParamaters();
			List outParams = sqlDefInfo.getOutParamaters();
			String iCollName = sqlDefInfo.getICollName();
			IndexedCollection iColl = null;
			KeyedCollection kColl = null;
			
			resultContext = new Context();
			resultContext.setDataElement(new KeyedCollection());
			
			// 反推Context时，只支持输出存放到IColl,不支持输出存放到KColl.
			if (iCollName != null)
			{
				iColl = new IndexedCollection(iCollName);
				kColl = new KeyedCollection(iCollName);
			}
			
			for (int i = 0; i < inParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) inParams.get(i);

				//INPUT只能按KCOLL.NAME格式处理
				int index = aParam.dataName.indexOf('.');
				try {
					if (index < 0) {
						resultContext.addDataElement(new DataField(aParam.dataName));
					}
					else {
						String inputKCollName = aParam.dataName.substring(0, index);
						String inputFieldName = aParam.dataName.substring(index+1);
						KeyedCollection inputKColl = null;
						if (resultContext.containsKey(inputKCollName)) {
							inputKColl = (KeyedCollection)resultContext.getDataElement(inputKCollName);
						}
						else {
							inputKColl = new KeyedCollection(inputKCollName);
							resultContext.addDataElement(inputKColl);
						}

						if (!inputKColl.containsKey(inputFieldName)) {
							inputKColl.addDataField(new DataField(inputFieldName));
						}
					}
				}
				catch (Exception e) {
				}
			}
	
			for (int i = 0; i < outParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) outParams.get(i);
				try {
					if (iCollName != null)
						//OUTPUT参数如果配置了ICOLL，一定按ICOLL存放
						kColl.addDataField(new DataField(aParam.dataName));
					else {
						//如果没有配置ICollName,则处理KCOLL.NAME格式
						int index = aParam.dataName.indexOf('.');

						if (index < 0) {
							resultContext.addDataElement(new DataField(aParam.dataName));
						}
						else {
							String outputKCollName = aParam.dataName.substring(0, index);
							String outputFieldName = aParam.dataName.substring(index+1);
							KeyedCollection outputKColl = null;
							if (resultContext.containsKey(outputKCollName)) {
								outputKColl = (KeyedCollection)resultContext.getDataElement(outputKCollName);
							}
							else {
								outputKColl = new KeyedCollection(outputKCollName);
								resultContext.addDataElement(outputKColl);
							}

							if (!outputKColl.containsKey(outputFieldName)) {
								outputKColl.addDataField(new DataField(outputFieldName));
							}
						}
					}
				}
				catch (Exception e) {
				}
			}
	
			try {
				if (iCollName != null)
				{
					iColl.setDataElement(kColl);
					resultContext.addDataElement(iColl);
				}
				
				// sql语句影响的记录数只是作为一个数据域被放入context中。
				if (sqlDefInfo.getRowsDataName() != null)
					resultContext.addDataElement(new DataField(sqlDefInfo.getRowsDataName()));
			}
			catch (Exception e) {
			}
		}
		else if (JDBCProcedureDefine.class.isAssignableFrom(aDef.getClass()))
		{
			JDBCProcedureDefine procDefInfo = (JDBCProcedureDefine) aDef;
			
			List inParams = procDefInfo.getInParams();
			List outParams = procDefInfo.getOutParams();
			List resultSets = procDefInfo.getResultSets();
			String iCollName = procDefInfo.getICollName();
			
			IndexedCollection iColl = null;
			KeyedCollection kColl = null;
			
			resultContext = new Context();
			resultContext.setDataElement(new KeyedCollection());
			
			// 反推Context.
			if (iCollName != null) //存储过程批量循环执行ICOLL的数据,存储过程所有输入输出都在本ICOLL中
			{
				iColl = new IndexedCollection(iCollName);
				kColl = new KeyedCollection();
			}
			
			//输入参数放入Context或者KColl中
			for (int i = 0; i < inParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) inParams.get(i);
				try {
					if (iCollName == null) 
						resultContext.addDataElement(new DataField(aParam.dataName));
					else
						kColl.addDataField(new DataField(aParam.dataName));
				}
				catch (Exception e) {
				}
			}
	
			//输出参数放入Context或者KColl中
			for (int i = 0; i < outParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) outParams.get(i);
				try {
					if (iCollName == null)
						resultContext.addDataElement(new DataField(aParam.dataName));
					else
						kColl.addDataField(new DataField(aParam.dataName));
				}
				catch (Exception e) {
				}
			}

			//生成resultSet的IColl，并放入单比的Context中或者放入批量执行的ICOLL中
			for (int i = 0; i < resultSets.size(); i++) {
				ResultSetDefine aParam = (ResultSetDefine) resultSets.get(i);
				IndexedCollection resultIColl = new IndexedCollection(aParam.getICollName());
				KeyedCollection resultKColl = new KeyedCollection();
				
				int nCount = aParam.getParameters().size();
				for (int j = 0; j < nCount; j++) {
					try {
						resultKColl.addDataField(new DataField(((SQLParameter)aParam.get(j)).dataName));
					}
					catch (Exception e) {
					}
				}
				resultIColl.setDataElement(resultKColl);
				
				try {
					if (iCollName != null)
						kColl.addIndexedCollection(resultIColl);
					else
						resultContext.addDataElement(resultIColl);
				}
				catch (Exception e) {
				}			
			}
	
			try {
				if (iCollName != null)
				{
					iColl.setDataElement(kColl);
					resultContext.addDataElement(iColl);
				}
			}
			catch (Exception e) {
			}			
		}

		// 更新访问信息
		long endTime = System.currentTimeMillis()-beginTime;
		synchronized (syncObj) {
			this.accessInfo.endAccess(endTime);
		}
		
		return resultContext;
	}

	public AccessInfo getAccessInfo(){
		return this.accessInfo;
	}
}
