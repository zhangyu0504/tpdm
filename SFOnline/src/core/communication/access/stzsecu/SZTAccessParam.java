package core.communication.access.stzsecu;

/**
 * 深证通通信参数配置
 * @author 汪华
 *
 */
public class SZTAccessParam {
	/*
	 * 服务端深证通配置
	 */
	private String serverAppId ;//B股深证通应用标识
	private String serverUserId;//B股深证通用户号
	private String serverPwd;//B股客户端密码	
	private String serverDestAppId;//B股存管应用标识
	private String serverDestUserId;//B股深证通用户号
	
	/*
	 * 客户深证配置
	 */
	private String clientAppId ;//B股深证通应用标识
	private String clientUserId;//B股深证通用户号
	private String clientPwd;//B股客户端密码	
	private String clientDestAppId;//B股存管应用标识
	private String clientDestUserId;//B股深证通用户号
	
	
	/*
	 * 深证通B股前置机IP配置
	 */
	private String mrIp;//B股接入客户端消息路由器的IP地址
	private short mrPort;//B股接入客户端消息路由器的连接端口
	private String mrIpBak;//B股备用消息路由器的IP 地址
	private short mrPortBak;//B股备用消息路由器的连接端口
	
	/*
	 * 深证通B股动态链接库配置	
	 */
	private String dllPath;//B股深证通动态链接库路径

	public String getServerAppId() {
		return serverAppId;
	}

	public void setServerAppId(String sevAppId) {
		this.serverAppId = sevAppId;
	}

	public String getServerUserId() {
		return serverUserId;
	}

	public void setServerUserId(String sevUserId) {
		this.serverUserId = sevUserId;
	}

	public String getServerPwd() {
		return serverPwd;
	}

	public void setServerPwd(String sevPwd) {
		this.serverPwd = sevPwd;
	}

	public String getServerDestAppId() {
		return serverDestAppId;
	}

	public void setServerDestAppId(String sevDestAppId) {
		this.serverDestAppId = sevDestAppId;
	}

	public String getServerDestUserId() {
		return serverDestUserId;
	}

	public void setServerDestUserId(String sevDestUserId) {
		this.serverDestUserId = sevDestUserId;
	}

	public String getClientAppId() {
		return clientAppId;
	}

	public void setClientAppId(String cltAppId) {
		this.clientAppId = cltAppId;
	}

	public String getClientUserId() {
		return clientUserId;
	}

	public void setClientUserId(String cltUserId) {
		this.clientUserId = cltUserId;
	}

	public String getClientPwd() {
		return clientPwd;
	}

	public void setClientPwd(String cltPwd) {
		this.clientPwd = cltPwd;
	}

	public String getClientDestAppId() {
		return clientDestAppId;
	}

	public void setClientDestAppId(String cltDestAppId) {
		this.clientDestAppId = cltDestAppId;
	}

	public String getClientDestUserId() {
		return clientDestUserId;
	}

	public void setClientDestUserId(String cltDestUserId) {
		this.clientDestUserId = cltDestUserId;
	}

	public String getMrIp() {
		return mrIp;
	}

	public void setMrIp(String mrIp) {
		this.mrIp = mrIp;
	}

	public short getMrPort() {
		return mrPort;
	}

	public void setMrPort(short mrPort) {
		this.mrPort = mrPort;
	}

	public String getMrIpBak() {
		return mrIpBak;
	}

	public void setMrIpBak(String mrIpBak) {
		this.mrIpBak = mrIpBak;
	}

	public short getMrPortBak() {
		return mrPortBak;
	}

	public void setMrPortBak(short mrPortBak) {
		this.mrPortBak = mrPortBak;
	}

	public String getDllPath() {
		return dllPath;
	}

	public void setDllPath(String dllPath) {
		this.dllPath = dllPath;
	}

	
}
