package core.jdbc.procedure;

import java.sql.Connection;

import javax.sql.DataSource;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.ConnectionManager;
import com.ecc.emp.jdbc.EMPJDBCException;
import com.ecc.emp.jdbc.ProcedureFailedException;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.timerecorder.EMPTimerRecorder;
import com.ecc.emp.transaction.EMPTransaction;
import com.ecc.emp.transaction.EMPTransactionDef;

/**
 * �������ݿ�ִ�д洢���̵Ĳ������衣
 * <p>
 * ͨ���洢���̷��ʷ���service (ProcedureAccessService)�����ò�ִ�б������ݿ�Ĵ洢���̡�
 * <p>
 * ����ʾ����<pre>
 * &lt;action id="JDBCProcedureAction" implClass="com.ecc.emp.jdbc.procedure.JDBCProcedureAction"
 * 	transactionType="TRX_REQUIRE_NEW" iCollName="iCollName" isBatch="batch" throwException="false"
 * 	dataSource="JDBCDataSource" procedureService="ProcedureAccessService"
 * 	procedureDefine="JDBCProcedureDefine" label="labelName"/&gt;</pre>
 * ����˵����<br>
 * transactionType���������ͣ�TRX_REQUIRE_NEW����������TRX_REQUIRED��ȫ������<br>
 * iCollName������ִ�д洢���̵��������ݼ��϶��塣<br>
 * isBatch���Ƿ�����ִ�д洢���̡�<br>
 * throwException��boolean�����ԣ��������洢����ִ�еķ���ֵ��0ʱ���Ƿ��׳��쳣��com.ecc.emp.jdbc.ProcedureFailedException��<br>
 * dataSource������Դ���������ơ�<br>
 * procedureService�����ݿ�洢���̷��ʷ��������ơ�<br>
 * procedureDefine�����ݿ�洢���̶������ơ�
 * <p>
 * ����״̬��<br>
 * 0����������0���쳣����throwException=falseʱ��
 * 
 * @author lili
 * @author GaoLin 2006-10-30
 * @version 2.1
 * @since 1.0 2003-11-10
 * @lastmodified 2008-06-26
 * @emp:name ���ʴ洢����
 * @emp:iconName /images/db_obj.gif
 * @emp:document ���ʴ洢���̵Ĳ������裬ͨ���洢���̷��ʷ���ProcedureAccessService�������ò�ִ�б������ݿ�Ĵ洢���̡�
 */ 
public class JDBCProcedureAction extends EMPAction{
	
	/**
	 * ����Դ����������
	 */
	private String dataSourceName=null;
	
	/**
	 * ���ݿ�洢���̷��ʷ���������
	 */
	private String procedureServiceName=null;
	
	/**
	 * ���ݿ�洢���̶�������
	 */
	private String procedureDefineName=null;
	
	/**
	 * ����ֵ��0ʱ�Ƿ��׳��쳣
	 */
	private boolean throwException=false;
	
    /**
     * ��������
     */
	private int trxType = EMPTransactionDef.TRX_REQUIRED;

	public JDBCProcedureAction() {
		super();
	}
	
	/**
	 * ҵ���߼�����ִ����ڡ�
	 * <p>
	 * ����ProcedureAccessService����ִ�з��ʴ洢���̵Ĳ�����
	 * 
	 * @param context ����������
	 * @throws EMPException
	 * @return ����ֵ 0����������0�� �쳣
	 */
	public String execute(Context context) throws EMPException {
		long begin = System.currentTimeMillis();
		DataSource dataSource = null;
		JDBCProcedureDefine procedureDefine = null;
		ProcedureAccessService procedureService = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named ["+dataSourceName+"]is not found in FBSJDBCProcedureAction:"+this.toString());
		
		if (procedureDefineName != null && procedureDefineName.length() != 0)
			procedureDefine = (JDBCProcedureDefine) context.getService(procedureDefineName);
		if (procedureDefine == null)
			throw new EMPException("FBSJDBCProcedureDefine not set for FBSJDBCProcedureAction:" + this.toString());
		
		if (procedureServiceName != null && procedureServiceName.length() != 0)
			procedureService = (ProcedureAccessService) context.getService(procedureServiceName);
		if (procedureService == null)
			throw new EMPException("FBSProcedureAccessService not set for FBSJDBCProcedureAction:" + this.toString());
		   
		Connection connection = null;

		/*ShenDongJie delete 20100130 begin
		EMPTransaction transaction = this.getTransaction();
		/*ShenDongJie delete 20100130 end */

		try {

			connection = ConnectionManager.getConnection(dataSource);
			String retCode="-1";

			retCode = procedureService.executeProcedure(procedureDefine,context,connection);

			/*ShenDongJie delete 20100130 begin
			if (!"0".equals(retCode) && transaction != null) {
				transaction.setRollbackOnly(true);
			/*ShenDongJie delete 20100130 end */
			if (!"0".equals(retCode)) {
				if(throwException)
					throw new ProcedureFailedException("Procedure [" + procedureDefine.getProcedureName() + "] execute failed with retCode=" + retCode);
			}

			long timeCost=System.currentTimeMillis()-begin;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The ProcedureService [" + procedureDefine.getName() + "],times= "+timeCost, null);
			return retCode;

		} catch (ProcedureFailedException pe) {
			throw pe;	
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-begin);
		}
	}
	
	/**
	 * ����execute��������FBS��չ���롣
	 * <p>
	 * ����FBSProcedureAccessService����ִ���Ѷ���Ĵ洢������䡣
	 * 
	 * @param context ����������
	 * @param tmpContext ������ʱ������
	 * @throws EMPException
	 * @return ����ֵ 0��������2����¼δ�ҵ�
	 */
	
	public String execute(Context context, Context tmpContext) throws EMPException {
		long begin = System.currentTimeMillis();
		DataSource dataSource = null;
		JDBCProcedureDefine procedureDefine = null;
		ProcedureAccessService procedureService = null;
		
		if (dataSourceName != null && dataSourceName.length() != 0)
			dataSource = (DataSource) context.getService(dataSourceName);
		if (dataSource == null)
			throw new EMPException("dataSource named ["+dataSourceName+"]is not found in FBSJDBCProcedureAction:"+this.toString());
		
		if (procedureDefineName != null && procedureDefineName.length() != 0)
			procedureDefine = (JDBCProcedureDefine) context.getService(procedureDefineName);
		if (procedureDefine == null)
			throw new EMPException("FBSJDBCProcedureDefine not set for FBSJDBCProcedureAction:" + this.toString());
		
		if (procedureServiceName != null && procedureServiceName.length() != 0)
			procedureService = (ProcedureAccessService) context.getService(procedureServiceName);
		if (procedureService == null)
			throw new EMPException("FBSProcedureAccessService not set for FBSJDBCProcedureAction:" + this.toString());
		   
		Connection connection = null;

		/*ShenDongJie delete 20100130 begin
		EMPTransaction transaction = this.getTransaction();
		/*ShenDongJie delete 20100130 end */

		try {

			connection = ConnectionManager.getConnection(dataSource);
			String retCode="-1";

			retCode = procedureService.executeProcedure(procedureDefine,tmpContext,connection);

			/*ShenDongJie delete 20100130 begin
			if (!"0".equals(retCode) && transaction != null) {
				transaction.setRollbackOnly(true);
			/*ShenDongJie delete 20100130 end */
			if (!"0".equals(retCode)) {
				if(throwException)
					throw new ProcedureFailedException("Procedure [" + procedureDefine.getProcedureName() + "] execute failed with retCode=" + retCode);
			}
			long timeCost=System.currentTimeMillis()-begin;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The ProcedureService [" + procedureDefine.getName() + "],times= "+timeCost, null);
			return retCode;

		} catch (ProcedureFailedException pe) {
			throw pe;	
		} catch (EMPJDBCException je) {
			throw je;
		} catch (Exception e) {
			throw new EMPException(e);
		} finally {
			if (connection != null)
				ConnectionManager.releaseConnection(dataSource, connection);
			long end = System.currentTimeMillis();
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_DBACCESS, end-begin);
		}
	}	

	/**
	 * ��������Դ���������ơ�
	 * 
	 * @param dataSourceName ����Դ����������
	 * @emp:isAttribute true
	 * @emp:name ����Դ����������
	 * @emp:desc ��ʹ�õ�����Դ�Ķ�������
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setDataSource(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/**
	 * �������ݿ�洢���̶������ơ�
	 * 
	 * @param procedureDefineName ���ݿ�洢���̶�������
	 * @emp:isAttribute true
	 * @emp:name ���ݿ�洢���̶�������
	 * @emp:desc ��Ҫִ�е����ݿ�洢���̶�������
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setProcedureDefine(String procedureDefineName) {
		this.procedureDefineName = procedureDefineName;
	}

	/**
	 * �������ݿ�洢���̷��ʷ��������ơ�
	 * 
	 * @param procedureServiceName ���ݿ�洢���̷��ʷ���������
	 * @emp:isAttribute true
	 * @emp:name ���ݿ�洢���̷��ʷ���������
	 * @emp:desc ��ʹ�õ����ݿ�洢���̷��ʷ���������
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setProcedureService(String procedureServiceName) {
		this.procedureServiceName = procedureServiceName;
	}

	/**
	 * �����������͡�
	 * 
	 * @param value ��������
	 * @emp:isAttribute true
	 * @emp:name ��������
	 * @emp:desc ѡ��ò�����ȫ�������Ƕ�������
	 * @emp:mustSet true
	 * @emp:valueList TRX_REQUIRED=Ӧ��ȫ������;TRX_REQUIRE_NEW=������������;
	 * @emp:defaultValue TRX_REQUIRED 
	 */
	public void setTransactionType(String value) {
		if ("TRX_REQUIRED".equals(value))
			this.trxType = EMPTransactionDef.TRX_REQUIRED;
		else if ("TRX_REQUIRE_NEW".equals(value))
			this.trxType = EMPTransactionDef.TRX_REQUIRE_NEW;
	}
	
	/**
	 * ����������Ͷ������
	 * 
	 * @return �������Ͷ������
	 */
	public EMPTransactionDef getTransactionDef() {
		return new EMPTransactionDef(trxType);
	}

	/**
	 * ���õ��洢���̷���ֵ��0ʱ�Ƿ��׳��쳣��
	 * 
	 * @param throwException ���洢���̷���ֵ��0ʱ�Ƿ��׳��쳣
	 * @emp:isAttribute true
	 * @emp:name �Ƿ��׳��쳣
	 * @emp:desc ���洢���̷���ֵ��0ʱ�Ƿ��׳��쳣
	 * @emp:defaultValue false
	 */
	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

}
