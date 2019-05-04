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
 * EMP平台实现的HTTP请求处理器。
 * <p>
 * 用于处理HTTP请求的报文头，以及从报文头获取必要的信息，
 * 这些信息包括sessionId、serviceId(所请求的HTTP服务ID或交易码)等。
 * <p>
 * 参数如下：
 * <p>
 * serviceIdField：报文头解开后的ServiceId数据域<br>
 * sessionIdField：报文头解开后的SessionId数据域<br>
 * appendReqHead：是否需要截去请求报文头，可选项true(默认)、false<br>
 * appendRepHead：是否需要在返回报文中附带报文头，可选项true(默认)、false<br>
 * errorCodeField：保存错误码的数据域名<br>
 * <p>
 * 子元素：
 * <p>
 * requestHeadFormat：请求报文头格式处理器<br>
 * responseHeadFormat：响应报文头格式处理器<br>
 * serviceIdMap：ServiceId映射，允许将多个请求ServiceId映射到其他定义ServiceId中，用于交易分发<br>
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
	 * 处理请求数据。
	 * <p>
	 * 从请求报文中解出报文头，从中取得Session id和Service id，
	 * 连同报文体一起放入request以进行进一步处理。
	 * 
	 * @param request HTTP请求
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
				len = len + readLen + 2048; // 扩大缓冲区
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
	 * 处理异常时的响应数据。
	 * <p>
	 * 将数据打包成返回报文头，并和异常信息一起放入response。
	 * 
	 * @param request HTTP请求
	 * @param response HTTP响应
	 * @param e 异常
	 * @param reqURI 请求URL
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
			
			//以下赋值给SYS_HEAD各必输字段
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
				retMsgField.setValue("内部错误");
			}
			CompositeData retCD = new CompositeData();
			retCD.addField("RET_CODE", retCodeField);
			retCD.addField("RET_MSG", retMsgField);
			retArray.addStruct(retCD);
			sysHeadCD.addArray("RET", retArray);
			
			//以下赋值给APP_HEAD各必输字段
			appHeadCD.addField("BRANCH_ID", reqCD.getStruct("APP_HEAD").getField("BRANCH_ID"));
			appHeadCD.addField("USER_ID", reqCD.getStruct("APP_HEAD").getField("USER_ID"));

			//以下赋值给应答CD
			respCD.addStruct("SYS_HEAD", sysHeadCD);
			respCD.addStruct("APP_HEAD", appHeadCD);
			respCD.addStruct("BODY", new CompositeData());

			writer.write(PackUtil.packXmlStr(respCD));

		} catch (Exception ee) {
			EMPLog.log(EMPConstance.EMP_HTTPACCESS, EMPLog.ERROR, 0, "Failed to handle exception!", ee);
		}
	}
}
