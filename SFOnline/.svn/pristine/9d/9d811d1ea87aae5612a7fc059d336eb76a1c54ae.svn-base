package core.communication.access.tcpip;

import java.net.Socket;

import com.ecc.emp.access.tcpip.EMPTCPIPRequest;
import core.communication.tcpip.TCPIPService;


/**
 * TCP/IP请求处理器接口。
 * <p>
 * 用于处理TCP/IP请求的报文头，以及从报文头获取必要的信息，
 * 这些信息包括sessionId、serviceId(所请求的TCP/IP服务ID或交易码)等。
 * 
 * @author zhongmc
 * @version 2.1
 * @since 1.0 2006-11-24
 * @lastmodified 2008-6-11
 * 
 */
public interface TCPIPRequestHandler {

	/**
	 * 从请求报文中判断该请求是否为业务处理请求。
	 * 
	 * @param msg 请求报文
	 * @return 是否业务处理请求
	 */
	public boolean isRequestPackage(byte[] msg);

	/**
	 * 获得请求的EMPTCPIPRequest封装对象。
	 * <p>
	 * 从请求报文中解出报文头，从中取得Session id和Service id，
	 * 连同报文体一起封装为EMPTCPIPRequest以进行进一步处理。
	 * 
	 * @param reqMessage 请求报文
	 * @return EMPTCPIPRequest
	 */
	public EMPTCPIPRequest getTCPIPRequest(byte[] reqMessage, TCPIPService service, Socket socket);

	/**
	 * 处理异常时的响应报文。
	 * <p>
	 * 将数据和异常信息打包成返回报文头。
	 * 
	 * @param request TCPIP请求
	 * @param e 异常
	 * @return 异常响应报文
	 */
	public byte[] getExceptionResponse(EMPTCPIPRequest request, Exception e);

	/**
	 * 处理正常的响应报文。
	 * <p>
	 * 将数据打包成返回报文头，并和返回报文体一起放入response。
	 * 
	 * @param request TCPIP请求
	 * @param retMsg 返回报文体
	 * @return 响应报文
	 */
	public byte[] getResponsePackage(EMPTCPIPRequest request, byte[] retMsg);

}
