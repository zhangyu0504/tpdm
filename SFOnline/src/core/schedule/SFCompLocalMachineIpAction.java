package core.schedule;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;

import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * <b>����������</b><br>
 * �ý��ײ����ṩ�ͱ���IP�ȽϵĹ��ܡ�<br>
 * <b>����˵��:</b><br>
 * &nbsp; &nbsp; ipAdrress����Ҫ�жϵ�IP��ַ<br>
 * <b>����״̬��</b><br>
 * ���ײ��践������״̬��0Ϊ��ȣ�-1Ϊ���ȡ�<br>
 * 
 * @create date: 2002-7-29 17:42:50
 * @author ZhongMingChang
 * @modifier liubq 2006-10-17
 */
public class SFCompLocalMachineIpAction extends EMPAction {
	private SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);

	String ipAddress = null;

	public SFCompLocalMachineIpAction() {
		super();
	}

	/**
	 * @return String
	 * @throws EMPException 
	 * @see com.ecc.emp.flow.EMPAction#execute(com.ecc.emp.core.Context)
	 */
	public String execute(Context context) throws EMPException {
//		InetAddress oneAddress = null;
//		Boolean	IPFind = false;
//		try {		
//			Enumeration<NetworkInterface> netInterfaces=NetworkInterface.getNetworkInterfaces();
//			while(netInterfaces.hasMoreElements())    
//			{    
//				NetworkInterface ni= (NetworkInterface)netInterfaces.nextElement();
//				Enumeration<InetAddress> address = ni.getInetAddresses();
//				while (address.hasMoreElements()){
//					oneAddress = (InetAddress)address.nextElement();
//					if (ipAddress.equals(oneAddress.getHostAddress())){
//						IPFind = true;
//						break ;
//					}
//				}
//				if (IPFind){
//					break;					
//				}
//			    logger.error(oneAddress.getHostAddress());
//			}
//
//			if (!IPFind){
//				//EMPLog.log("SFCompLocalMachineIpAction", EMPLog.ERROR, 0,"δ�ҵ�Ŀ��IP[" + ipAddress + "]");
//				return "-1";
//			}
//		} catch (Exception e) {
//			logger.error("���г���:"+ e);
//			return "-1";
//		}
//		return "0";
		/*
		 * �ӻ��������л�ȡ������
		 */
		if(SFUtil.isNotEmpty(ipAddress)){
			String macCode = BizUtil.getMacCode(context);
			String strIP[]=ipAddress.split("\\|");
			for (int i = 0; i < strIP.length; i++){
				if (macCode.equals(strIP[i])){
					return "0";
				}
			}
		} 
		return "-1";
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}