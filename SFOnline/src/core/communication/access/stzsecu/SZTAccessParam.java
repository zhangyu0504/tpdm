package core.communication.access.stzsecu;

/**
 * ��֤ͨͨ�Ų�������
 * @author ����
 *
 */
public class SZTAccessParam {
	/*
	 * �������֤ͨ����
	 */
	private String serverAppId ;//B����֤ͨӦ�ñ�ʶ
	private String serverUserId;//B����֤ͨ�û���
	private String serverPwd;//B�ɿͻ�������	
	private String serverDestAppId;//B�ɴ��Ӧ�ñ�ʶ
	private String serverDestUserId;//B����֤ͨ�û���
	
	/*
	 * �ͻ���֤����
	 */
	private String clientAppId ;//B����֤ͨӦ�ñ�ʶ
	private String clientUserId;//B����֤ͨ�û���
	private String clientPwd;//B�ɿͻ�������	
	private String clientDestAppId;//B�ɴ��Ӧ�ñ�ʶ
	private String clientDestUserId;//B����֤ͨ�û���
	
	
	/*
	 * ��֤ͨB��ǰ�û�IP����
	 */
	private String mrIp;//B�ɽ���ͻ�����Ϣ·������IP��ַ
	private short mrPort;//B�ɽ���ͻ�����Ϣ·���������Ӷ˿�
	private String mrIpBak;//B�ɱ�����Ϣ·������IP ��ַ
	private short mrPortBak;//B�ɱ�����Ϣ·���������Ӷ˿�
	
	/*
	 * ��֤ͨB�ɶ�̬���ӿ�����	
	 */
	private String dllPath;//B����֤ͨ��̬���ӿ�·��

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
