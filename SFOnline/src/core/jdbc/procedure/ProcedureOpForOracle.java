package core.jdbc.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import oracle.jdbc.driver.OracleTypes;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.jdbc.EMPJDBCException;
import com.ecc.emp.log.EMPLog;

import core.jdbc.sql.SQLParameter;

public class ProcedureOpForOracle implements ProcedureOp {

	public ProcedureOpForOracle() {
		super();
	}

	public CallableStatement callProc(Connection aConnection, String aProcName,
			Map aInValues, List aInParams, List aOutParams, List aResultSet)
			throws EMPJDBCException {
		
		CallableStatement call = null;
		try {
			int maxIdx = 0;

			for (int i = 0; i < aInParams.size(); i++)
			{
				if (maxIdx < ((SQLParameter)aInParams.get(i)).paramIdx)
					maxIdx = ((SQLParameter)aInParams.get(i)).paramIdx;
			}
			
			for (int i = 0; i < aOutParams.size(); i++)
			{
				if (maxIdx < ((SQLParameter)aOutParams.get(i)).paramIdx)
					maxIdx = ((SQLParameter)aOutParams.get(i)).paramIdx;
			}

			for (int i = 0; i < aResultSet.size(); i++)
			{
				if (maxIdx < ((ResultSetDefine)aResultSet.get(i)).getIdx())
					maxIdx = ((ResultSetDefine)aResultSet.get(i)).getIdx();
			}

			// process the call sql statement for all params
			StringBuffer param = new StringBuffer("{ call " + aProcName + " (");
			for (int i = 0; i < maxIdx; i++)
				param = param.append("?, ");

			int len = param.length();
			if (param.charAt(len - 2) == ',')
				param.setCharAt(len - 2, ' ');

			param = param.append(") }");

			call = aConnection.prepareCall(param.toString());

			//EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0, param.toString());
			
			// set the procedure input paramaters
			for (int i = 0; i < aInParams.size(); i++) {
				SQLParameter aParam = (SQLParameter) aInParams.get(i);
				String aValue = (String) aInValues.get(aParam.dataName);
				if (aValue != null) {
					call.setObject(aParam.paramIdx, aValue.trim());
					//EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0, i+":"+aValue.trim());
				} else {
					call.setNull(aParam.paramIdx, aParam.dataType);
					//EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0, i+":NULL");
				}
			}

			// regist the ouput parameters
			for (int i = 0; i < aOutParams.size(); i++)
			{
				SQLParameter aParam = (SQLParameter) aOutParams.get(i);
				call.registerOutParameter(aParam.paramIdx, aParam.dataType);
			}

			// regist the output resultSets
			for (int i = 0; i < aResultSet.size(); i++) {
				call.registerOutParameter(((ResultSetDefine) aResultSet.get(i)).getIdx(), OracleTypes.CURSOR);
			}

			return call;

		} catch (SQLException se) {
			try {
				if (call != null)
					call.close();
			} catch (Exception e) {
			}
			throw new EMPJDBCException("Failed to creat CallableStatement:" + call, se);
		}
	}

	public void processResultSets(Context aContext, KeyedCollection kInColl,
			List aResultSets, CallableStatement aCall) throws EMPJDBCException {
		
		StringBuffer logBuffer = new StringBuffer();
		ResultSet rset = null;
		
		try {
			int i = 0;
			do {
				// retrieve the resultSet
				ResultSetDefine aResultDefine = (ResultSetDefine) aResultSets.get(i);
				rset = (ResultSet) aCall.getObject(aResultDefine.getIdx());
				if (rset != null) {
					if (i == aResultSets.size())
						continue;
					else
						i++;

					logBuffer.append("Procedure's ResultSets["+ i +"]:" + "\n");
					
					String iCollName = aResultDefine.getICollName();
					boolean isAppend = aResultDefine.getIsAppend();
					List resultSetParams = aResultDefine.getParameters();

					IndexedCollection iColl = null;
					if (aContext != null)
						iColl = (IndexedCollection) aContext.getDataElement(iCollName);
					else
						iColl = (IndexedCollection) kInColl.getDataElement(iCollName);
					
					KeyedCollection kColl = null;
					kColl = (KeyedCollection)iColl.getDataElement();
					
					// clear all elements
					if (!isAppend) {
						iColl.removeAll();
					}

					int idx = 0;
					while (rset.next()) {
/*						ShenDongjie modify 20100131 for wrong logic
   						if (!isAppend && idx < iColl.size()) {
							kColl = (KeyedCollection) iColl.getElementAt(idx);
						} else {
							kColl = (KeyedCollection) kColl.clone();
							iColl.addDataElement(kColl);
						}
*/						
						kColl = (KeyedCollection) kColl.clone();
						iColl.addDataElement(kColl);
						
						for (int j = 1; j <= resultSetParams.size(); j++) {
							String dataName = ((SQLParameter) resultSetParams.get(j-1)).dataName;
							String value = rset.getString(j);
							if (value != null)
								value = value.trim();
							kColl.setDataValue(dataName, value);
						}
						idx++;
					}
					logBuffer.append(iColl.toString() + "\n");
					rset.close();
				} 
				else { // non resultSet
					break;
				}

			} while (i < aResultSets.size());
			EMPLog.log(EMPConstance.EMP_JDBC, EMPLog.DEBUG, 0, logBuffer.toString());

		} catch (SQLException se) {
			try {
				if (rset != null)
					rset.close();
			} catch (Exception e) {
			}
			throw new EMPJDBCException("Failed to process ResultSet!", se);
		} catch(EMPException ee){
			throw new EMPJDBCException("Failed to process ResultSet!", ee);
		} finally {
			try {
				if (rset != null)
					rset.close();
			} catch (Exception e) {
			}
		}
	}
}
