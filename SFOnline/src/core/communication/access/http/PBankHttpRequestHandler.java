package core.communication.access.http;

import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dc.eai.data.Array;
import com.dc.eai.data.CompositeData;
import com.dc.eai.data.Field;
import com.dc.eai.data.FieldAttr;
import com.dc.eai.data.FieldType;
import com.dcfs.esb.client.converter.PackUtil;
import com.ecc.emp.access.http.EMPHttpRequestHandler;
import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.log.EMPLog;
import common.util.SFConst;

/**
 * EMPƽ̨ʵ�ֵ�HTTP����������
 * <p>
 * ���ڴ���HTTP����ı���ͷ���Լ��ӱ���ͷ��ȡ��Ҫ����Ϣ��
 * ��Щ��Ϣ����sessionId��serviceId(�������HTTP����ID������)�ȡ�
 * <p>
 * �������£�
 * <p>
 * serviceIdField������ͷ�⿪���ServiceId������<br>
 * sessionIdField������ͷ�⿪���SessionId������<br>
 * appendReqHead���Ƿ���Ҫ��ȥ������ͷ����ѡ��true(Ĭ��)��false<br>
 * appendRepHead���Ƿ���Ҫ�ڷ��ر����и�������ͷ����ѡ��true(Ĭ��)��false<br>
 * errorCodeField��������������������<br>
 * <p>
 * ��Ԫ�أ�
 * <p>
 * requestHeadFormat��������ͷ��ʽ������<br>
 * responseHeadFormat����Ӧ����ͷ��ʽ������<br>
 * serviceIdMap��ServiceIdӳ�䣬�����������ServiceIdӳ�䵽��������ServiceId�У����ڽ��׷ַ�<br>
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-29
 * @lastmodified 2008-6-11
 * 
 */
public class PBankHttpRequestHandler extends EMPHttpRequestHandler {

	public PBankHttpRequestHandler() {
		super();
	}

	/**
	 * �����������ݡ�
	 * <p>
	 * ���������н������ͷ������ȡ��Session id��Service id��
	 * ��ͬ������һ�����request�Խ��н�һ������
	 * 
	 * @param request HTTP����
	 * @throws Exception
	 */
	public void parseRequest(HttpServletRequest request) throws Exception {

		InputStream in = request.getInputStream();
		int len = request.getContentLength();
		if (len <= 0)
			len = 2048;
		// return;
		byte[] tmp = new byte[2048];
		byte[] buffer = new byte[len];
		int totalLen = 0;
		while (true) {
			int readLen = in.read(tmp, 0, 2048);
			if (readLen <= 0)
				break;

			if ((readLen + totalLen) > len) {
				len = len + readLen + 2048; // ���󻺳���
				byte[] aa = new byte[len];
				System.arraycopy(buffer, 0, aa, 0, totalLen);
				buffer = aa;
			}
			System.arraycopy(tmp, 0, buffer, totalLen, readLen);
			totalLen = totalLen + readLen;
		}

		String reqData;
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			reqData = new String(buffer, 0, totalLen, "UTF-8");
			request.setCharacterEncoding("UTF-8");
		}
		else
			reqData = new String(buffer, 0, totalLen, encoding);

		CompositeData reqCD = null;
		reqCD = PackUtil.unpackXmlStr(reqData.trim());
		
		String sessionId = null;
		String serviceId = null;
		String sceneId = null;
		
		serviceId = reqCD.getStruct("SYS_HEAD").getField("SERVICE_CODE").strValue();
		sceneId = reqCD.getStruct("SYS_HEAD").getField("SERVICE_SCENE").strValue();
		
		request.setAttribute("SID", sessionId);
		request.setAttribute("serviceId", serviceId + "_" + sceneId);
		request.setAttribute("reqData", reqCD);
		
	}

	/**
	 * �����쳣ʱ����Ӧ���ݡ�
	 * <p>
	 * �����ݴ���ɷ��ر���ͷ�������쳣��Ϣһ�����response��
	 * 
	 * @param request HTTP����
	 * @param response HTTP��Ӧ
	 * @param e �쳣
	 * @param reqURI ����URL
	 * @param sessionId Session id
	 */
	public void handleException(HttpServletRequest request, HttpServletResponse response, 
			Exception e, String reqURI, String sessionId) {

		try {
			String encoding = request.getCharacterEncoding();
			if (encoding != null) {
				response.setCharacterEncoding(encoding);
				response.setContentType("text/xml; charset=" + encoding);
			}
			PrintWriter writer = response.getWriter();

			Context context = (Context) request.getAttribute(EMPConstance.ATTR_CONTEXT);

			CompositeData reqCD = (CompositeData)request.getAttribute("reqData");
			
			CompositeData respCD = new CompositeData();
			CompositeData sysHeadCD = new CompositeData();
			CompositeData appHeadCD = new CompositeData();
			
			//���¸�ֵ��SYS_HEAD�������ֶ�
			sysHeadCD.addField("SERVICE_CODE", reqCD.getStruct("SYS_HEAD").getField("SERVICE_CODE"));
			sysHeadCD.addField("CONSUMER_ID", reqCD.getStruct("SYS_HEAD").getField("CONSUMER_ID"));
			sysHeadCD.addField("SERVICE_SCENE", reqCD.getStruct("SYS_HEAD").getField("SERVICE_SCENE"));
			sysHeadCD.addField("ESB_SEQ_NO", reqCD.getStruct("SYS_HEAD").getField("CONSUMER_SEQ_NO"));
			sysHeadCD.addField("CONSUMER_SEQ_NO", reqCD.getStruct("SYS_HEAD").getField("CONSUMER_SEQ_NO"));
			sysHeadCD.addField("TRAN_DATE", reqCD.getStruct("SYS_HEAD").getField("TRAN_DATE"));
			sysHeadCD.addField("TRAN_TIMESTAMP", reqCD.getStruct("SYS_HEAD").getField("TRAN_TIMESTAMP"));
			Field retStatusField = new Field(new FieldAttr(FieldType.FIELD_STRING, 1, 0));
			retStatusField.setValue("F");
			sysHeadCD.addField("RET_STATUS", retStatusField);
			
			Array retArray = new Array();
			Field retCodeField = new Field(new FieldAttr(FieldType.FIELD_STRING, 15, 0));
			Field retMsgField = new Field(new FieldAttr(FieldType.FIELD_STRING, 512, 0));
			if (EMPException.class.isAssignableFrom(e.getClass())) {
				String errCode = ((EMPException)e).getErrorCode();
				if (errCode == null || errCode.length() < 1)
					errCode = "P9999S999";
				retCodeField.setValue(SFConst.SYS_SYSID + "00" + errCode);
				String errMsg = ((EMPException)e).getMessage();
				if (errMsg.length() > 256)
					errMsg = errMsg.substring(0, 256);
				retMsgField.setValue(errMsg);
			}
			else {
				retCodeField.setValue("P9999S990");
				retMsgField.setValue("�ڲ�����");
			}
			CompositeData retCD = new CompositeData();
			retCD.addField("RET_CODE", retCodeField);
			retCD.addField("RET_MSG", retMsgField);
			retArray.addStruct(retCD);
			sysHeadCD.addArray("RET", retArray);
			
			//���¸�ֵ��APP_HEAD�������ֶ�
			appHeadCD.addField("BRANCH_ID", reqCD.getStruct("APP_HEAD").getField("BRANCH_ID"));
			appHeadCD.addField("USER_ID", reqCD.getStruct("APP_HEAD").getField("USER_ID"));

			//���¸�ֵ��Ӧ��CD
			respCD.addStruct("SYS_HEAD", sysHeadCD);
			respCD.addStruct("APP_HEAD", appHeadCD);
			respCD.addStruct("BODY", new CompositeData());

			writer.write(PackUtil.packXmlStr(respCD));

		} catch (Exception ee) {
			EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.ERROR, 0, "Failed to handle exception!", ee);
		}
	}
}
