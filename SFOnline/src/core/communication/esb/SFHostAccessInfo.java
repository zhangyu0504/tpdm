package core.communication.esb;

import com.ecc.emp.flow.reversal.HostAccessInfo;

/**
 * 描述主机交易执行结果的对象
 * @author SF
 * @since 2010-02-23
 * @version 1.0.0
 *
 */
public class SFHostAccessInfo extends HostAccessInfo {
	
	public SFHostAccessInfo() {
		super();
	}
	/**
	 * 接口名称
	 */
	private String hostInterfaceClass = null;

	/**
	 * 交易失败是否抛出错误标志
	 */
	private Boolean throwExceptionFlag = null;
	
	/**
	 * 接口处理的数据域(KeyedCollection)名称
	 */
	private String inOutDataArea = null;

	/**
	 * 接口处理的数据对象
	 */
	private Object inOutDTO = null;

	/**
	 * 接口类
	 */
	private SFHostAccessAction hostAccessAction = null;

	/**
	 * 得到接口名称
	 * @return
	 */
	public String getHostInterfaceClass() {
		return hostInterfaceClass;
	}
	
	/**
	 * 设置接口名称
	 * @param hostInterfaceClass
	 */
	public void setHostInterfaceClass(String hostInterfaceClass) {
		this.hostInterfaceClass = hostInterfaceClass;
	}
	
	/**
	 * 注入该接口失败是否抛出错误标志。
	 * 
	 * @param reversalFlag 抛错标志
	 */
	public void setThrowExceptionFlag(Boolean throwExceptionFlag)
	{
		this.throwExceptionFlag = throwExceptionFlag;
	}
	
	/**
	 * 获得该接口失败是否抛出错误标志。
	 * 
	 */
	public Boolean getThrowExceptionFlag()
	{
		return this.throwExceptionFlag;
	}

	/**
	 * 注入该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 * @param inOutDataArea输入输出的数据区域(KeyedCollection)的名称
	 */
	public void setInOutDataArea(String inOutDataArea)
	{
		this.inOutDataArea = inOutDataArea;
	}

	/**
	 * 获取该接口输入输出的数据区域(KeyedCollection)的名称。
	 * 
	 */
	public String getInOutDataArea()
	{
		return this.inOutDataArea;
	}

	/**
	 * 获取该接口数据传输对象,留给ESB使用，或以后的扩展。
	 * 
	 */
	public Object getInOutDTO() {
		return this.inOutDTO;
	}

	/**
	 * 设置该接口数据传输对象,留给ESB使用，或以后的扩展。。
	 * 
	 */
	public void setInOutDTO(Object inOutDTO) {
		this.inOutDTO = inOutDTO;
	}

	/**
	 * 获取该主机访问action的实例。
	 * 
	 */
	public SFHostAccessAction getHostAccessAction() {
		return this.hostAccessAction;
	}

	/**
	 * 设置该主机访问action的实例。
	 * 
	 */
	public void setHostAccessAction(SFHostAccessAction hostAccessAction) {
		this.hostAccessAction = hostAccessAction;
	}
}
