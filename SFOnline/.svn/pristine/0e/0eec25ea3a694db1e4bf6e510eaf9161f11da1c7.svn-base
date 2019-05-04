package core.communication.hostinterface;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import common.exception.SFException;

/**
 * PBank主机通讯接口定义。
 *
 * @author PBank
 * @version 1.0
 * @since 1.0 2010-2-23
 * @lastmodified 2010-2-23
 */
public interface HostInterface {

	/**
	 * 交易发生错误时发起实时冲正。
	 * 
	 * @throws PBankException
	 */
	public void reversal(Context context) throws SFException;

	/**
	 * 执行接口逻辑,处理接口数据。
	 * 
	 * @throws PBankException
	 */
	public String execute(Context context) throws EMPException;

	/**
	 * 获取传输字符集代码。
	 * 
	 */
	public String getTranCode();

	/**
	 * 设置传输字符集代码。
	 * 
	 */
	public void setTranCode(String tranCode);
	
	/**
	 * 获取传输字符集代码。
	 * 
	 */
	public String getEncoding();

	/**
	 * 设置传输字符集代码。
	 * 
	 */
	public void setEncoding(String encoding);
	
	/**
	 * 注入该接口失败是否抛出错误标志。
	 * 
	 * @param reversalFlag 抛错标志
	 */
	public void setThrowExceptionFlag(boolean throwExceptionFlag);
	
	/**
	 * 获得该接口失败是否抛出错误标志。
	 * 
	 */
	public boolean getThrowExceptionFlag();

	/**
	 * 注入该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 * @param inOutDataArea输入输出的数据区域(KeyedCollection)的名称
	 */
	public void setInOutDataArea(String inOutDataArea);

	/**
	 * 获取该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 */
	public String getInOutDataArea();

	/**
	 * 生成该接口输入输出的数据传输对象到上下文。
	 * 
	 */
	public Object genInOutDTO(Context context) throws SFException;

	/**
	 * 获取TcpIp服务名称。
	 * 
	 */
	public String getTcpipServiceName();

	/**
	 * 设置TcpIp服务名称。
	 * 
	 */
	public void setTcpipServiceName(String tcpipServiceName);
	
	/**
	 * 注入是否将报文记录到数据库的标志。
	 * 
	 * @param saveInDatabase 是否将报文记录到数据库的标志
	 */
	public void setSaveInDatabase(boolean saveInDatabase);

	/**
	 * 获取是否将报文记录到数据库的标志。
	 * 
	 */
	public boolean getSaveInDatabase();
	
	
	/**
	 * 获取服务号和场景号
	 * 
	 */
	public String getServiceCode();
	public void setServiceCode(String serviceCode);
}
