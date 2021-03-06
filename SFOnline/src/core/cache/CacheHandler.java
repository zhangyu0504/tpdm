package core.cache;

import java.sql.Connection;

import com.ecc.emp.component.factory.ComponentFactory;
import com.ecc.emp.core.Context;
import com.ecc.emp.flow.reversal.HostAccessInfo;
import com.ecc.emp.flow.reversal.ReversalHandler;
import com.ecc.emp.service.EMPService;
import common.exception.SFException;

/**
 * 系统缓存管理器,用于服务在启动时
 * 加载数据到对象缓存中
 * @author 汪华
 *
 */
public abstract class CacheHandler extends EMPService implements ReversalHandler{
	/**
	 * 主机代码
	 */
	private String hostId;
	
	private String id;
	/**
	 * 初始化缓存
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
