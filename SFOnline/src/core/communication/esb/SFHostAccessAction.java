package core.communication.esb;

import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.flow.reversal.HostAccessInfo;
import com.ecc.emp.flow.reversal.ReversalController;
import common.exception.SFException;

import core.communication.esbinterface.EsbInterfaceBase;
import core.communication.hostinterface.HostInterface;
import core.communication.util.AccessConstance;


/**
 * 后台主机访问业务逻辑处理步骤虚类，用于平台内实现的自动冲正处理机制，
 * 对于需要冲正的后台访问交易，其步骤从此类继承，并实现其处理方法 doExecute
 * 如果后台处理成功则正常返回，否则抛出异常
 * 
 * Copyright (c) 2010, 2010 SDB
 * @author SF
 * @version 1.0.0
 * @since 2010-02-23
 * @lastmodified 2010-02-23
 *
 */
public abstract class SFHostAccessAction extends EMPAction implements HostInterface
{
	/**
	 * 交易码
	 * 交易码并非指某一主机的交易码，而是指EMP应用所设定的交易码，与后台主机的交易码无关。
	 * 有可能一个交易码对应着多个后台主机交易码，也有可能本交易不需要访问后台系统。
	 */
	private String trxCode = null;
	/**
	 * 主机代码
	 */
	private String hostId = null;

	/**
	 * 字符编码
	 */
	protected String encoding = "GB18030";

	/**
	 * 是否帐务接口标志，如果是则纳入冲正管理
	 */
	private boolean acctInterfaceFlag = false;
	
	/**
	 * 接口名称
	 */
	private String hostInterfaceClass = null;

	/**
	 * 交易失败是否抛出错误标志
	 */
	private boolean throwExceptionFlag = false;
	
	/**
	 * 接口处理的数据域(KeyedCollection)名称
	 */
	private String inOutDataArea = null;
	
	/**
	 * TcpIp通讯服务的名称
	 */
	private String tcpipServiceName = null;

	/**
	 * 该接口的实例
	 */
	private HostInterface hostInterface = null;
	
	/**
	 * 是否将报文记录在数据库中
	 */
	private boolean saveInDatabase = false;
	
	
	
	private String serviceCode = null;
	
	/**
	 * EMPAction虚方法实现
	 * 主要目的是构造需要冲正处理的后台主机访问操作后的主机交易执行结果对象HostAccessInfo，该结果保存在‘_HostAccessList'数据定义域中。
	 * 具体的主机访问处理程序实际实现在本类提供的虚方法doExecute()方法中，所有需要进行冲正处理的后台主机访问操作步骤均需要从本类继承并实现doExecute方法。
	 */
	public final String execute(Context context) throws EMPException 
	{
		List hostAccessList = null, SFHostAccessList = null;
		HostAccessInfo accessInfo = null;
		SFHostAccessInfo SFAccessInfo = null;
		ReversalController ctrl = (ReversalController)context.getService( EMPConstance.REVERSAL_CONTROLLER );
 
		try
		{
			if (hostInterface == null) {
				synchronized (this) {
					if (hostInterface == null) {
						if (hostInterfaceClass != null) {
							hostInterface = (HostInterface) Class.forName(hostInterfaceClass).newInstance();
							hostInterface.setEncoding(encoding);
							hostInterface.setInOutDataArea(inOutDataArea);
							hostInterface.setTcpipServiceName(tcpipServiceName);
							hostInterface.setThrowExceptionFlag(throwExceptionFlag);
							
							hostInterface.setServiceCode(serviceCode);
							
							
							//修改切记：此处增加类的条件判断是因为增加ESB支持前,未对os390的接口赋值交易码,os390各交易码在各子类中初始化
							//历史原因请重视，否则造成os390类工作有可能不正常
							if (EsbInterfaceBase.class.isAssignableFrom(hostInterface.getClass())) {
								hostInterface.setTranCode(trxCode);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new SFException("P0022S005", "实例化接口类失败!", e);
		}
		
		Object inOutDTO = hostInterface.genInOutDTO(context);
		if( ctrl != null && hostId != null && acctInterfaceFlag)
		{
			//EMP冲正机制的主机访问列表
			hostAccessList = getHostAccessList(context);
			if (hostAccessList.size() < 1)
			{
				accessInfo = new HostAccessInfo();
				accessInfo.setHostId( hostId );
				accessInfo.setTrxCode( trxCode );
				hostAccessList.add( accessInfo );
			}
			else
			{
				accessInfo = (HostAccessInfo)hostAccessList.get(0);
			}
			 
			//SF冲正机制的主机接口访问列表
			SFHostAccessList = getSFHostAccessList(context);
			
			SFAccessInfo = new SFHostAccessInfo();
			SFAccessInfo.setHostId(hostId);
			SFAccessInfo.setTrxCode(trxCode);
			SFAccessInfo.setThrowExceptionFlag(throwExceptionFlag);
			SFAccessInfo.setHostInterfaceClass(hostInterfaceClass);
			SFAccessInfo.setInOutDataArea(inOutDataArea);
			SFAccessInfo.setInOutDTO(inOutDTO);
			SFAccessInfo.setHostAccessAction(this);
			SFHostAccessList.add(SFAccessInfo);
		}
		 
		try{
			String retValue = doExecute( context );
			if( accessInfo != null)
				accessInfo.setResult( retValue );

			if( SFAccessInfo != null)
				SFAccessInfo.setResult( retValue );
			 
			return retValue;
		}catch(EMPException e)
		{
			if( accessInfo != null )
			{
				accessInfo.setResult( e.getErrorCode() );
				accessInfo.setException( e );
			}
			 
			if( SFAccessInfo != null )
			{
				SFAccessInfo.setResult( e.getErrorCode() );
				SFAccessInfo.setException( e );
			}
			 
			throw e;
		}
	}

	 /**
	  * 虚方法，后台主机访问的处理程序实现
	  * @param context
	  * @return
	  * @throws EMPException
	  */
	public abstract String doExecute(Context context )throws EMPException;

	/**
	 * 得到交易码
	 * @return
	 */
	public String getTrxCode() {
		return trxCode;
	}
	public String getTranCode() {
		return trxCode;
	}

	/**
	 * 设置交易码
	 * @param trxCode
	 */
	public void setTrxCode(String trxCode) {
		this.trxCode = trxCode;
	}
	public void setTranCode(String trxCode) {
		this.trxCode = trxCode;
	}
	
	/**
	 * 得到本次交易处理中所涉及到的主机访问交易结果对象的列表
	 * 一次交易并不一定只访问一个后台，有可能访问多个后台系统。EMP流程处理可记录多个后台访问结果对象。
	 * @param context
	 * @return
	 */
	private List getHostAccessList(Context context )throws EMPException
	{
		List accList = null;
		try{
			accList = (List)context.getDataValue(EMPConstance.HOST_ACCESS_LIST);
		}
		catch(Exception e)
		{
			accList = new ArrayList();
			context.addDataField(EMPConstance.HOST_ACCESS_LIST, accList);
		}
		return accList;
	}

	/**
	 * 得到本次交易处理中所涉及到的主机访问交易结果对象的列表
	 * 一次交易并不一定只访问一个后台，有可能访问多个后台系统。SF流程处理可记录多个后台访问结果对象。
	 * @param context
	 * @return
	 */
	private List getSFHostAccessList(Context context)throws EMPException
	{
		List SFaccList = null;
		try {
			SFaccList = (List)context.getDataValue(AccessConstance.SF_HOST_ACCESS_LIST);
		}
		catch(Exception e)
		{
			SFaccList = new ArrayList();
			context.addDataField(AccessConstance.SF_HOST_ACCESS_LIST, SFaccList);
		}
		return SFaccList;
	}

	/**
	 * 获取主机代码
	 * @return
	 */
	public String getHostId() {
		return hostId;
	}

	/**
	 * 设置主机代码
	 * @param hostId
	 */
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

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
	public void setThrowExceptionFlag(boolean throwExceptionFlag)
	{
		this.throwExceptionFlag = throwExceptionFlag;
	}
	
	/**
	 * 获得该接口失败是否抛出错误标志。
	 * 
	 */
	public boolean getThrowExceptionFlag()
	{
		return this.throwExceptionFlag;
	}

	/**
	 * 注入是否帐务接口标志。
	 * 
	 * @param acctInterfaceFlag 是否帐务接口标志
	 */
	public void setAcctInterfaceFlag(boolean acctInterfaceFlag)
	{
		this.acctInterfaceFlag = acctInterfaceFlag;
	}
	
	/**
	 * 获得是否帐务接口标志。
	 * 
	 */
	public Boolean getAcctInterfaceFlag()
	{
		return this.acctInterfaceFlag;
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
	 * 注入TcpIp通讯服务的名称。
	 * 
	 * @param tcpipServiceName TcpIp通讯服务的名称
	 */
	public void setTcpipServiceName(String tcpipServiceName)
	{
		this.tcpipServiceName = tcpipServiceName;
	}

	/**
	 * 获取TcpIp通讯服务的名称。
	 * 
	 */
	public String getTcpipServiceName()
	{
		return this.tcpipServiceName;
	}

	/**
	 * 得到接口类实例，必须是线程安全类
	 * @return
	 */
	public HostInterface getHostInterface() {
		return hostInterface;
	}
	
	/**
	 * 注入是否将报文记录到数据库的标志。
	 * 
	 * @param saveInDatabase 是否将报文记录到数据库的标志
	 */
	public void setSaveInDatabase(boolean saveInDatabase)
	{
		this.saveInDatabase = saveInDatabase;
	}

	/**
	 * 获取是否将报文记录到数据库的标志。
	 * 
	 */
	public boolean getSaveInDatabase()
	{
		return this.saveInDatabase;
	}
	
	/**
	 * 获取传输字符集代码。
	 * 
	 */
	public String getEncoding()
	{
		return this.encoding;
	}

	/**
	 * 设置传输字符集代码。
	 * 
	 */
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
	
	public void reversal(Context context) throws SFException
	{
		return;
	}
	
	public Object genInOutDTO(Context context) throws SFException
	{
		return null;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	
	
}