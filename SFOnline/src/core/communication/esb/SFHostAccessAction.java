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
 * ��̨��������ҵ���߼����������࣬����ƽ̨��ʵ�ֵ��Զ�����������ƣ�
 * ������Ҫ�����ĺ�̨���ʽ��ף��䲽��Ӵ���̳У���ʵ���䴦���� doExecute
 * �����̨����ɹ����������أ������׳��쳣
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
	 * ������
	 * �����벢��ָĳһ�����Ľ����룬����ָEMPӦ�����趨�Ľ����룬���̨�����Ľ������޹ء�
	 * �п���һ���������Ӧ�Ŷ����̨���������룬Ҳ�п��ܱ����ײ���Ҫ���ʺ�̨ϵͳ��
	 */
	private String trxCode = null;
	/**
	 * ��������
	 */
	private String hostId = null;

	/**
	 * �ַ�����
	 */
	protected String encoding = "GB18030";

	/**
	 * �Ƿ�����ӿڱ�־��������������������
	 */
	private boolean acctInterfaceFlag = false;
	
	/**
	 * �ӿ�����
	 */
	private String hostInterfaceClass = null;

	/**
	 * ����ʧ���Ƿ��׳������־
	 */
	private boolean throwExceptionFlag = false;
	
	/**
	 * �ӿڴ����������(KeyedCollection)����
	 */
	private String inOutDataArea = null;
	
	/**
	 * TcpIpͨѶ���������
	 */
	private String tcpipServiceName = null;

	/**
	 * �ýӿڵ�ʵ��
	 */
	private HostInterface hostInterface = null;
	
	/**
	 * �Ƿ񽫱��ļ�¼�����ݿ���
	 */
	private boolean saveInDatabase = false;
	
	
	
	private String serviceCode = null;
	
	/**
	 * EMPAction�鷽��ʵ��
	 * ��ҪĿ���ǹ�����Ҫ��������ĺ�̨�������ʲ��������������ִ�н������HostAccessInfo���ý�������ڡ�_HostAccessList'���ݶ������С�
	 * ������������ʴ������ʵ��ʵ���ڱ����ṩ���鷽��doExecute()�����У�������Ҫ���г�������ĺ�̨�������ʲ����������Ҫ�ӱ���̳в�ʵ��doExecute������
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
							
							
							//�޸��мǣ��˴�������������ж�����Ϊ����ESB֧��ǰ,δ��os390�Ľӿڸ�ֵ������,os390���������ڸ������г�ʼ��
							//��ʷԭ�������ӣ��������os390�๤���п��ܲ�����
							if (EsbInterfaceBase.class.isAssignableFrom(hostInterface.getClass())) {
								hostInterface.setTranCode(trxCode);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new SFException("P0022S005", "ʵ�����ӿ���ʧ��!", e);
		}
		
		Object inOutDTO = hostInterface.genInOutDTO(context);
		if( ctrl != null && hostId != null && acctInterfaceFlag)
		{
			//EMP�������Ƶ����������б�
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
			 
			//SF�������Ƶ������ӿڷ����б�
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
	  * �鷽������̨�������ʵĴ������ʵ��
	  * @param context
	  * @return
	  * @throws EMPException
	  */
	public abstract String doExecute(Context context )throws EMPException;

	/**
	 * �õ�������
	 * @return
	 */
	public String getTrxCode() {
		return trxCode;
	}
	public String getTranCode() {
		return trxCode;
	}

	/**
	 * ���ý�����
	 * @param trxCode
	 */
	public void setTrxCode(String trxCode) {
		this.trxCode = trxCode;
	}
	public void setTranCode(String trxCode) {
		this.trxCode = trxCode;
	}
	
	/**
	 * �õ����ν��״��������漰�����������ʽ��׽��������б�
	 * һ�ν��ײ���һ��ֻ����һ����̨���п��ܷ��ʶ����̨ϵͳ��EMP���̴���ɼ�¼�����̨���ʽ������
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
	 * �õ����ν��״��������漰�����������ʽ��׽��������б�
	 * һ�ν��ײ���һ��ֻ����һ����̨���п��ܷ��ʶ����̨ϵͳ��SF���̴���ɼ�¼�����̨���ʽ������
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
	 * ��ȡ��������
	 * @return
	 */
	public String getHostId() {
		return hostId;
	}

	/**
	 * ������������
	 * @param hostId
	 */
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	/**
	 * �õ��ӿ�����
	 * @return
	 */
	public String getHostInterfaceClass() {
		return hostInterfaceClass;
	}
	
	/**
	 * ���ýӿ�����
	 * @param hostInterfaceClass
	 */
	public void setHostInterfaceClass(String hostInterfaceClass) {
		this.hostInterfaceClass = hostInterfaceClass;
	}
	
	/**
	 * ע��ýӿ�ʧ���Ƿ��׳������־��
	 * 
	 * @param reversalFlag �״��־
	 */
	public void setThrowExceptionFlag(boolean throwExceptionFlag)
	{
		this.throwExceptionFlag = throwExceptionFlag;
	}
	
	/**
	 * ��øýӿ�ʧ���Ƿ��׳������־��
	 * 
	 */
	public boolean getThrowExceptionFlag()
	{
		return this.throwExceptionFlag;
	}

	/**
	 * ע���Ƿ�����ӿڱ�־��
	 * 
	 * @param acctInterfaceFlag �Ƿ�����ӿڱ�־
	 */
	public void setAcctInterfaceFlag(boolean acctInterfaceFlag)
	{
		this.acctInterfaceFlag = acctInterfaceFlag;
	}
	
	/**
	 * ����Ƿ�����ӿڱ�־��
	 * 
	 */
	public Boolean getAcctInterfaceFlag()
	{
		return this.acctInterfaceFlag;
	}

	/**
	 * ע��ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 * @param inOutDataArea�����������������(KeyedCollection)������
	 */
	public void setInOutDataArea(String inOutDataArea)
	{
		this.inOutDataArea = inOutDataArea;
	}

	/**
	 * ��ȡ�ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 */
	public String getInOutDataArea()
	{
		return this.inOutDataArea;
	}
	
	/**
	 * ע��TcpIpͨѶ��������ơ�
	 * 
	 * @param tcpipServiceName TcpIpͨѶ���������
	 */
	public void setTcpipServiceName(String tcpipServiceName)
	{
		this.tcpipServiceName = tcpipServiceName;
	}

	/**
	 * ��ȡTcpIpͨѶ��������ơ�
	 * 
	 */
	public String getTcpipServiceName()
	{
		return this.tcpipServiceName;
	}

	/**
	 * �õ��ӿ���ʵ�����������̰߳�ȫ��
	 * @return
	 */
	public HostInterface getHostInterface() {
		return hostInterface;
	}
	
	/**
	 * ע���Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 * @param saveInDatabase �Ƿ񽫱��ļ�¼�����ݿ�ı�־
	 */
	public void setSaveInDatabase(boolean saveInDatabase)
	{
		this.saveInDatabase = saveInDatabase;
	}

	/**
	 * ��ȡ�Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 */
	public boolean getSaveInDatabase()
	{
		return this.saveInDatabase;
	}
	
	/**
	 * ��ȡ�����ַ������롣
	 * 
	 */
	public String getEncoding()
	{
		return this.encoding;
	}

	/**
	 * ���ô����ַ������롣
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