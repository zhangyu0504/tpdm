package core.cache;

import java.sql.Connection;

import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.flow.reversal.HostAccessInfo;
import com.ecc.emp.flow.reversal.ReversalHandler;
import com.ecc.emp.service.EMPService;
import common.exception.SFException;

/**
 * ϵͳ���������,���ڷ���������ʱ
 * �������ݵ����󻺴���
 * @author ����
 *
 */
public abstract class CacheHandler extends EMPService implements ReversalHandler{
	/**
	 * ��������
	 */
	private String hostId;
	
	private String id;
	/**
	 * ��ʼ������
	 * @param context
	 * @throws SFException
	 */
	public abstract void init(Context context,Connection connection) throws SFException;
	
	

	public void doReversal(Context arg0, ComponentFactory arg1) {
		
	}

	public String getHostId() {
		return this.hostId;
	}
	
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public boolean isNeedGlobalReversal(Context arg0, HostAccessInfo arg1) {
		return false;
	}

	public boolean isNeedReversal(Context arg0, HostAccessInfo arg1) {
		return false;
	}
}