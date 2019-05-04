package core.communication.access.stzsecu;

import java.util.Map;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.format.FormatElement;
import common.util.SFUtil;

import core.cache.CacheMap;
import core.communication.format.KeyedFormat;
import core.communication.util.AccessConstance;
import core.log.SFLogger;

public class SZTResponseHandler {
	private String msgCode;
	
	public String getMsgCode() {
		return msgCode;
	}

	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}

	// ��������
	public void handleRequest(Context context,byte[] requestMsg,String msgCode) {
//		SFLogger.info(context,"SZTResponseHandler handleRequest start!");
		EMPTCPIPRequest request = new EMPTCPIPRequest();
//		Context context = SFUtil.getRootContext();
		try {
			KeyedCollection kColl = new KeyedCollection(AccessConstance.C_REQUEST_HEAD);
			kColl.setAppend(true);
			FormatElement requestHeadFormat = context
					.getFormat(AccessConstance.C_REQUEST_HEAD);
			if (requestHeadFormat.isBin())
				requestHeadFormat.unFormat(requestMsg, kColl);
			else{
				requestHeadFormat.unFormat(new String(requestMsg), kColl);
			}

			if (kColl != null) {
				// ȥ���ո�
				for (int i = 0; i < kColl.size(); i++) {
					String value = (String) kColl.getDataValue(kColl
							.getDataElement(i).getName());
					kColl.setDataValue(kColl.getDataElement(i).getName(),value.trim());
				}
			}
			request.setAttribute("headData", kColl);
			if (context.containsKey(AccessConstance.C_REQUEST_HEAD)) {
				context.removeDataElement(AccessConstance.C_REQUEST_HEAD);
			}
			context.addDataElement(kColl);

			KeyedFormat headFormat = (KeyedFormat) requestHeadFormat.getFormatElement();
			int headLen = headFormat.getHeadLen();
			byte[] buf = null;
			if (headLen > 0) {
				int length = requestMsg.length - headLen;
				buf = new byte[length];
				System.arraycopy(requestMsg, headLen, buf, 0, length);
				request.setAttribute("reqData", buf);
			} else{
				request.setAttribute("reqData", requestMsg);
			}

			requestMsg = (byte[]) request.getAttribute("reqData");

			String resData = "<?xml version=\"1.0\" encoding=\"GB18030\" ?>"+ new String(requestMsg);
			
			updateModel(resData.getBytes(), context, null,msgCode);

		

		} catch (Exception e) {
			e.printStackTrace();
			SFLogger.error(context,"Failed to parse the requestMsg in SZTResponseHandler to get sessionId"+e);
		}

//		SFLogger.info(context,"SZTResponseHandler handleRequest end!");
	}

	/**
	 * �����������ʵ�ַ�����
	 * 
	 * @param request
	 *            TCP/IP����
	 * @param sessionContext
	 *            Session Context
	 * @return �������ݣ�������õı�����
	 * @throws EMPException
	 */

	/**
	 * ʹ���������ݸ�������ģ�͡�
	 * <p>
	 * ʹ��EMP����ģ�����û�������<tt>DataElementSerializer</tt>��������ת�������ݡ�
	 * 
	 * @param requestMsg
	 *            �����İ�
	 * @param context
	 *            ����������
	 * @param dataElementDef
	 *            input����
	 * @throws EMPException
	 */
	public void updateModel(byte[] requestMsg, Context context,
			DataElement dataElementDef,String msgCode) throws EMPException {
		try {
			if (dataElementDef == null) {
				// ����head��Ϣ
				FormatElement headFormat = context.getFormat("MsgHdr");
				if (headFormat == null) {
					throw new EMPException(
							"The format:[MsgHdr] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
				}

				if (headFormat.isBin())
					headFormat.unFormat(requestMsg, context);
				else
					headFormat.unFormat(new String(requestMsg), context);

//				context.setDataValue(SFConst.CTX_PUBLIC_TX_CODE,
//						context.getDataValue("MsgHdr.InstrCd"));
				//String tranCode = SFUtil.getDataValue(context,SFConst.CTX_PUBLIC_TX_CODE);

				String serverId = SFUtil.getDataValue(context,"MsgHdr.InstrCd");
				Map<String,Map<String,String>> tcpipServiceMap =  CacheMap.getCache("SZT_TCPIPSERVICE");
				Map<String,String>tcpipService = tcpipServiceMap.get(serverId);
				
				
				// ����body��Ϣ
				String inFormat =  tcpipService.get("formatOutput"); //ReadProperty.getValue(msgCode + "_O");

				FormatElement msgFormat = context.getFormat(inFormat);
				if (msgFormat == null) {
					throw new EMPException("The format:["+ inFormat+"] not defined in context, please check the SFOnlineFormats.xml and settings.xml");
				}

				if (msgFormat.isBin())
					msgFormat.unFormat(requestMsg, context);
				else
					msgFormat.unFormat(new String(requestMsg), context);

			}
		} catch (EMPException e) {
			throw e;
		} catch (Exception e) {
			throw new EMPException("Failed to update dataModel in SZTResponseHandler:", e);
		}

	}



}