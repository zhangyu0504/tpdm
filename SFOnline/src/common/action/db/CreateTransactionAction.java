package common.action.db;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.transaction.EMPTransactionDef;
import com.ecc.emp.transaction.EMPTransactionManager;

public class CreateTransactionAction extends EMPAction {
	/**
	 * �������ͣ�Ĭ��Ϊȫ������
	 */
	private int trxType = EMPTransactionDef.TRX_REQUIRED;

	/**
	 * �����������͡�
	 * 
	 * @param value
	 *            ��������
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

	public String execute(Context context) throws EMPException {

		EMPTransactionManager transactionManager = (EMPTransactionManager) context
				.getService(EMPConstance.TRX_SVC_NAME);
		transactionManager.getTransaction(new EMPTransactionDef(this.trxType));
		return "0";
	}

}