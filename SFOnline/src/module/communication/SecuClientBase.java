package module.communication;

import java.util.Map;

import com.ecc.emp.core.Context;
import common.exception.SFException;
/**
 * ȯ�����Ѷ˴�����
 * @author ����
 *
 */
public abstract class SecuClientBase {
	/**
	 * ���ͱ���ͳһ���
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public Context send(Context context,Map<String,Object>msg)throws SFException{
		//˽��ҵ����������װ���뱨�ģ����������볡����
		return doHandle(context,msg);
	}
	
	/**
	 * ˽��ҵ����������װ���뱨�ģ����������볡����
	 * @param context
	 * @param msgCode
	 * @param serviceCode
	 * @return
	 * @throws SFException
	 */
	protected abstract Context doHandle(Context context,Map<String,Object>msg)throws SFException;
	
}
