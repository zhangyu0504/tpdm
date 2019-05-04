package core.jdbc.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.jdbc.EMPJDBCException;

/**
*
* <b>����������</b><br>
* �洢���̷���ִ�еĸ�����ӿڣ�����SQL���Ķ��壬�Լ����ؽ�����Ĵ���<br>
*
*   @����ʱ�� 2002-07-08
*   @author  ZhongMingChang
*   @modifier GaoLin 2006-10-25
*   
*/

public interface ProcedureOp {
	
	/**
	 * ������ʴ洢���̵�SQL���
	 * @param aInValues TODO
	 * @param Connection aConnection                    ���ݿ�����
	 * @param String aProcName                          �洢��������
	 * @param List aInValues                            ��������ֵ�б�
	 * @param List aInParams                            �����������б�
	 * @param List aResults                             ���ؽ���������б�
	 * @param int aResultSetCnt                         ���ؽ�����б���
	 * @return CallableStatement                        ���ʴ洢���̵�SQL��䶨��
	 * @throws EMPJDBCException
	 */

	public CallableStatement callProc(Connection aConnection, String aProcName,
			Map aInValues, List aInParams, List aOutParams, List aResultSet)
			throws EMPJDBCException;
	
	/**
	 * ����洢���̷��صĽ����
	 * 
	 * @param Context aContext                          ���׶����Context
	 * @param KeyedCollection kColl                     ����ִ�е��������ݼ���
	 * @param List aResultSets                          ���ؽ�����б�
	 * @param CallableStatement aCall                   SQL�������
	 * @param int outIdx                                ���������
	 * @return void
	 * @throws EMPJDBCException
	 */

	public void processResultSets(Context aContext, KeyedCollection kInColl, List aResultSets,
			CallableStatement aCall) throws EMPJDBCException;
}
