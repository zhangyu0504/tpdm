package core.communication.hostinterface;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import common.exception.SFException;

/**
 * PBank����ͨѶ�ӿڶ��塣
 *
 * @author PBank
 * @version 1.0
 * @since 1.0 2010-2-23
 * @lastmodified 2010-2-23
 */
public interface HostInterface {

	/**
	 * ���׷�������ʱ����ʵʱ������
	 * 
	 * @throws PBankException
	 */
	public void reversal(Context context) throws SFException;

	/**
	 * ִ�нӿ��߼�,����ӿ����ݡ�
	 * 
	 * @throws PBankException
	 */
	public String execute(Context context) throws EMPException;

	/**
	 * ��ȡ�����ַ������롣
	 * 
	 */
	public String getTranCode();

	/**
	 * ���ô����ַ������롣
	 * 
	 */
	public void setTranCode(String tranCode);
	
	/**
	 * ��ȡ�����ַ������롣
	 * 
	 */
	public String getEncoding();

	/**
	 * ���ô����ַ������롣
	 * 
	 */
	public void setEncoding(String encoding);
	
	/**
	 * ע��ýӿ�ʧ���Ƿ��׳������־��
	 * 
	 * @param reversalFlag �״��־
	 */
	public void setThrowExceptionFlag(boolean throwExceptionFlag);
	
	/**
	 * ��øýӿ�ʧ���Ƿ��׳������־��
	 * 
	 */
	public boolean getThrowExceptionFlag();

	/**
	 * ע��ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 * @param inOutDataArea�����������������(KeyedCollection)������
	 */
	public void setInOutDataArea(String inOutDataArea);

	/**
	 * ��ȡ�ýӿ������������������(KeyedCollection)�����ơ�
	 * 
	 */
	public String getInOutDataArea();

	/**
	 * ���ɸýӿ�������������ݴ�����������ġ�
	 * 
	 */
	public Object genInOutDTO(Context context) throws SFException;

	/**
	 * ��ȡTcpIp�������ơ�
	 * 
	 */
	public String getTcpipServiceName();

	/**
	 * ����TcpIp�������ơ�
	 * 
	 */
	public void setTcpipServiceName(String tcpipServiceName);
	
	/**
	 * ע���Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 * @param saveInDatabase �Ƿ񽫱��ļ�¼�����ݿ�ı�־
	 */
	public void setSaveInDatabase(boolean saveInDatabase);

	/**
	 * ��ȡ�Ƿ񽫱��ļ�¼�����ݿ�ı�־��
	 * 
	 */
	public boolean getSaveInDatabase();
	
	
	/**
	 * ��ȡ����źͳ�����
	 * 
	 */
	public String getServiceCode();
	public void setServiceCode(String serviceCode);
}
