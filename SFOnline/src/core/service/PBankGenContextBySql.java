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
* <b>����������</b><br>
* ��PBankSqlDefine�������������Զ���װ����Context��<br>
* 
*  @����ʱ�� 2010-01-02
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
		// ���·�����Ϣ
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
			
			// ����Contextʱ��ֻ֧�������ŵ�IColl,��֧�������ŵ�KColl.
			if (iCollName != null)
			{
				iColl = new IndexedCollection(iCollName);
				kColl = new KeyedCollection(iCollName);
			}
			
			for (int i = 0; i < inParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) inParams.get(i);

				//INPUTֻ�ܰ�KCOLL.NAME��ʽ����
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
						//OUTPUT�������������ICOLL��һ����ICOLL���
						kColl.addDataField(new DataField(aParam.dataName));
					else {
						//���û������ICollName,����KCOLL.NAME��ʽ
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
				
				// sql���Ӱ��ļ�¼��ֻ����Ϊһ�������򱻷���context�С�
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
			
			// ����Context.
			if (iCollName != null) //�洢��������ѭ��ִ��ICOLL������,�洢������������������ڱ�ICOLL��
			{
				iColl = new IndexedCollection(iCollName);
				kColl = new KeyedCollection();
			}
			
			//�����������Context����KColl��
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
	
			//�����������Context����KColl��
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

			//����resultSet��IColl�������뵥�ȵ�Context�л��߷�������ִ�е�ICOLL��
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

		// ���·�����Ϣ
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
