package core.communication.esb;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import common.exception.SFException;

import core.communication.hostinterface.HostInterface;

/**
 * SFSendEsbAction.java<br>
 * EMP ���ײ�����չ<br>
 * Extends class EMPAction<br>
 * Created on  2010��02��25��21ʱ08��04��<br>
 * @autor        <br>

 * @emp:name ����ESB��������
 * @emp:catalog SF��չACTION
 * @emp:states 0=�ɹ�;-1=�쳣;
 * @emp:document �������ýӿ������������KColl����������Ӧ�ӿ���ESB�������ݣ��������ؽ������Ӧ�������
 */
public class SFSendEsbAction extends SFHostAccessAction {
	
	/*ҵ���߼�������Ԫ��ִ�����*/
	public String doExecute(Context context) throws EMPException
	{
		HostInterface hostInterface = getHostInterface();
		if (hostInterface != null) {
			return (hostInterface.execute(context));
		}
	    else {
			throw new SFException("P0022S005", "ESB����ʵ����δʵ����!");
		}
	}
}
