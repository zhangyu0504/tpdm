package core.communication.esb;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import common.exception.SFException;

import core.communication.hostinterface.HostInterface;

/**
 * SFSendEsbAction.java<br>
 * EMP 交易步骤扩展<br>
 * Extends class EMPAction<br>
 * Created on  2010年02月25日21时08分04秒<br>
 * @autor        <br>

 * @emp:name 上送ESB数据总线
 * @emp:catalog SF扩展ACTION
 * @emp:states 0=成功;-1=异常;
 * @emp:document 根据配置接口名和输入输出KColl名，调用相应接口向ESB发送数据，并处理返回结果到相应输出区域
 */
public class SFSendEsbAction extends SFHostAccessAction {
	
	/*业务逻辑操作单元的执行入口*/
	public String doExecute(Context context) throws EMPException
	{
		HostInterface hostInterface = getHostInterface();
		if (hostInterface != null) {
			return (hostInterface.execute(context));
		}
	    else {
			throw new SFException("P0022S005", "ESB报文实现类未实例化!");
		}
	}
}
