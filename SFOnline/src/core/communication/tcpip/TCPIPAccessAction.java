package core.communication.tcpip;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.reversal.HostAccessAction;
import com.ecc.emp.format.FormatElement;
import com.ecc.emp.log.EMPLog;
import com.ecc.emp.timerecorder.EMPTimerRecorder;

/**
 * TCPIP通讯步骤。
 * <p>
 * 实现通过TCPIP协议向主机发送请求，得到主机返回数据的功能。
 * 首先将请求数据按照SendFormatName指定的格式打包，向主机发起请求，
 * 然后接受主机的响应，把返回数据按照ReceiveFormatName指定的格式解包。
 * <p>
 * 配置示例：<pre>
 * &lt;action id="TCPIPAccessAction" serviceName="HostComm" timeOut="5000" 
 * 	SendFormatName="sendHostFormat" ReceiveFormatName="receiveHostFormat"
 * 	encoding="UTF-8"/></pre>
 * 参数说明：<br> 
 * serviceName：TCPIP通讯服务ID<br>
 * timeOut：通讯超时时间(ms)<br>
 * SendFormatName：发送报文格式定义名称，默认为sendHostFormat<br>
 * ReceiveFormatName：接收报文格式定义名称，默认为receiveFormatName<br>
 * encoding：字符编码
 * <p>
 * 返回状态：<br>
 * 0，成功；2，得到TCPIPService服务错误；3，主机通讯异常
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.1 2007-11-16
 * @lastmodified 2008-6-27
 * @emp:name TCPIP主机访问
 * @emp:document 访问TCPIP主机的操作步骤，通过访问TCPIP主机服务（TCPIPService），实现TCP/IP协议的通讯功能。
 * @emp:states 0=成功;2=得到TCPIPService服务错误;3=主机通讯异常;
 */
public class TCPIPAccessAction extends HostAccessAction {
	
	/**
	 * TCPIP通讯服务ID
	 */
	private String serviceName;

	/**
	 * 发送报文格式定义名称
	 */
	private String sendFormatName = "sendHostFormat";
	
	/**
	 * 接收报文格式定义名称
	 */
	private String receiveFormatName = "receiveFormatName";
	
	/**
	 * 超时时间
	 */
	private int timeOut = -1;
	
	/**
	 * 报文鉴别对象所在数据域
	 */
	private String identityField;
	
	/**
	 * 字符编码
	 */
	private String encoding;

	public TCPIPAccessAction() {
		super();
	}

	/**
	 * HostAccessAction的执行入口。调用TCPIPService进行通讯处理。
	 * 
	 * @param context 交易上下文
	 * @return 0，成功；2，得到TCPIPService服务错误；3，主机通讯异常
	 * @throws EMPException
	 */
	public String doExecute(Context context) throws EMPException {
		//调用TCP/IP通讯服务
		TCPIPService tcpipService = null;
		long beginTime=System.currentTimeMillis();
		try {
			tcpipService =(TCPIPService) context.getService( serviceName );
		} 
		catch (Exception e) 
		{
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "TCPIPAccessAction: Fail to get TCPIPService." , e);
			return "2";
		}

		try {
			FormatElement reqfmt = (FormatElement) context.getFormat(sendFormatName);
			Object pkg = reqfmt.format(context);

			byte[] reqMsg;
			
			if( reqfmt.isBin() )
				reqMsg = (byte[])pkg;
			else
			{
				String tmp = (String)pkg;
				if( encoding == null )
					reqMsg = tmp.getBytes();
				else 
					reqMsg = tmp.getBytes( encoding );
			}

			byte[] repMsg = null;
			long beg = System.currentTimeMillis();
			
			Object identity = null;
			
			if( identityField != null )
				identity = context.getDataValue( identityField );
			
			repMsg =tcpipService.sendAndWait(identity, reqMsg, timeOut );
			
			long curTime = System.currentTimeMillis();
			long intvl = curTime - beg;
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.INFO, 0, "TCPIP Communication ["+ this.getName() + "] takes "+String.valueOf(intvl)+"(ms)" );
			FormatElement repFmt = (FormatElement) context.getFormat(receiveFormatName);
			
			if( repFmt.isBin() )
			{
				repFmt.unFormat( repMsg, context );
			}
			else
			{
				String receivePackage;
				
				if( encoding == null )
					receivePackage = new String(repMsg);
				else
					receivePackage = new String(repMsg, encoding );
					
				repFmt.unFormat(receivePackage,context);
			}	
		} catch (Exception tempE) {
			EMPLog.log(EMPConstance.EMP_TCPIP, EMPLog.ERROR, 0, "TCPIPAccessAction:  Fail to sendAnd wait to host.",  tempE );
			return "3";
		}finally{
			long timeCost=System.currentTimeMillis()-beginTime;
			EMPLog.log(EMPConstance.EMP_TIME_CONSUMING, EMPLog.INFO, 0, "The TCPIPAccessAction:"+this.getName()+",times="+timeCost, null);		
			EMPTimerRecorder.addThreadValue(EMPTimerRecorder.TYPE_COMM, timeCost);
		}
		return "0";
	}
	
	/**
	 * 设置接收报文格式定义名称。
	 * 
	 * @param receiveFormatName 接收报文格式定义名称
	 * @emp:isAttribute true
	 * @emp:name 接收报文
	 * @emp:desc 接收报文格式定义名称
	 * @emp:mustSet true
	 */
	public void setReceiveFormatName(String receiveFormatName) 	{
		this.receiveFormatName = receiveFormatName;
	}

	/**
	 * 设置发送报文格式定义名称。
	 * 
	 * @param sendFormatName 发送报文格式定义名称
	 * @emp:isAttribute true
	 * @emp:name 发送报文
	 * @emp:desc 发送报文格式定义名称
	 * @emp:mustSet true
	 */
	public void setSendFormatName(String sendFormatName) {
		this.sendFormatName = sendFormatName;
	}

	/**
	 * 设置TCPIP通讯服务定义名称。
	 * 
	 * @param svcName TCPIP通讯服务定义名称
	 * @emp:isAttribute true
	 * @emp:name TCPIP通讯服务
	 * @emp:desc 所使用的TCPIP通信服务定义名称
	 * @emp:mustSet true
	 * @emp:editClass com.ecc.ide.editor.service.ServicePropertyEditor
	 */
	public void setServiceName(String svcName) {
		this.serviceName = svcName;
	}

	/**
	 * 设置通讯超时时间。
	 * 
	 * @param timeOut 通讯超时时间
	 * @emp:isAttribute true
	 * @emp:name 超时时间
	 * @emp:desc 通讯超时时间
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * 设置字符编码。
	 * 
	 * @param encoding 字符编码
	 * @emp:isAttribute true
	 * @emp:name 字符编码
	 * @emp:desc 报文转换为字节流所使用的字符编码
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * 设置报文鉴别对象所在数据域。
	 * 会将该数据域中存放的对象传给TCPIPService，在接收到响应报文后根据该对象判断是否为需要报文。
	 * 
	 * @param identityField 报文鉴别对象所在数据域
	 * @emp:isAttribute true
	 * @emp:name 报文鉴别对象数据域
	 * @emp:desc 会将该数据域中存放的对象传给TCPIPService，在接收到响应报文后根据该对象判断是否为需要报文
	 * @emp:editClass com.ecc.ide.editor.transaction.DataNamePropertyEditor
	 */
	public void setIdentityField(String identityField) {
		this.identityField = identityField;
	}
}