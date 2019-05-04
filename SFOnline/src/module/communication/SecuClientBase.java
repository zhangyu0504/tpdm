package module.communication;

import java.util.Map;

import com.ecc.emp.core.Context;
import common.exception.SFException;
/**
 * 券商消费端处理类
 * @author 汪华
 *
 */
public abstract class SecuClientBase {
	/**
	 * 发送报文统一入口
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,Map<String,Object>msg)throws SFException{
		//私有业务处理方法：组装输入报文，定义服务号与场景号
		return doHandle(context,msg);
	}
	
	/**
	 * 私有业务处理方法：组装输入报文，定义服务号与场景号
	 * @param context
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected abstract Context doHandle(Context context,Map<String,Object>msg)throws SFException;
	
}
