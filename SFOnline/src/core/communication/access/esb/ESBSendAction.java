package core.communication.access.esb;

import com.dc.eai.data.CompositeData;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.access.tcpip.BBCPCommClient;
import core.log.SFLogger;



/**
 * 
 * ESB����TCPIPʵ��
 * @author ����
 *
 */
public class ESBSendAction{
	private final static int pkLen = 8;
	
	
	public String send(Context context,CompositeData requestCD) throws SFException{
		
		String ESBIp=null,ESBPort=null;
		int timeOut = 40000;
		String isTranSpond = SFUtil.getDataValue(context, SFConst.PUBLIC_IS_TRANSPOND);
		
		if("Y".equals(isTranSpond)){   //ת��������̨
			SFAccessParam SFParam = CacheMap.getCache("SF_ACCESS_PARAM");
			ESBIp = SFParam.getSFIp();
			ESBPort = SFParam.getSFPort();
			timeOut = SFParam.getSFTimeOut();
		}else{
			ESBAccessParam esbParam = CacheMap.getCache("ESB_ACCESS_PARAM");
			ESBIp = esbParam.getEsbIp();
			ESBPort = esbParam.getEsbPort();
			timeOut = esbParam.getEsbTimeOut();
		}
		
		BBCPCommClient client =  new BBCPCommClient(ESBIp,ESBPort,timeOut,timeOut,pkLen);
		
		String repMsg = PackUtil.packXmlStr(requestCD);
		byte[] retMsg  = repMsg.getBytes();
		
		String len = "00000000"+retMsg.length;
		byte[] toSendMessage = new byte[8+retMsg.length];
		System.arraycopy((len.substring(len.length()-pkLen, len.length())).getBytes(), 0, toSendMessage, 0, pkLen);
		System.arraycopy(retMsg, 0, toSendMessage, pkLen, retMsg.length);
		
		String ret = client.SendCMD(context,toSendMessage);
		if(!"0".equals(ret)){
//			if("2".equals(ret)){
//				SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPCODE", SFConst.RESPCODE_TIMEOUT_ESB);
//				SFUtil.setDataValue(context,"A_RESPONSE_HEAD.RESPMSG","������Ӧ����ʧ��");
//			}

			IndexedCollection iColl=SFUtil.getDataElement(context,"RET");
			KeyedCollection kColl=(KeyedCollection)iColl.getElementAt(0);
			SFUtil.setDataValue(context,kColl,"RET_CODE", SFConst.RESPCODE_TIMEOUT_ESB);
			SFUtil.setDataValue(context,kColl,"RET_MSG","������Ӧ����ʧ��");
			
			throw new SFException("YYPTERROR9999","BBCPTCPIPDynamicAccessActionͨѶ����"+ret);	
		}
		
		byte[] toReciveMessage = null;
		toReciveMessage = client.getResult().getBytes();//����������ȫ������
		if(toReciveMessage == null || toReciveMessage.length == 0){
			SFLogger.info(context,"BBCPTCPIPDynamicAccessActionͨѶ���󣺽��շ�������Ϊ�ա�������ʱ���ش��������س�ʱ4" );
		}
		
		String reqData = new String(toReciveMessage);
		return reqData.trim();
		
	}
	
	
	
	
	
}