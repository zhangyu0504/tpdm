package common.action.expand.ftp;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
import common.services.FtpFileService;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * <b>����������</b><br>
 * �ý��ײ����ṩͨ��FTP�ϴ������ļ��Ĺ��ܡ�<br>
 * <b>����ʾ��:</b><br>
 * <br>
 * &lt;opStep id=&quot;ftpFileAction&quot; <br>
 * &nbsp; &nbsp;fileType=&quot;0&quot; <br>
 * &nbsp; &nbsp;tranType=&quot;0&quot; <br>
 * &nbsp; &nbsp;localFileNameField=&quot;httpParam&quot; <br>
 * &nbsp; &nbsp;localPathField=&quot;merchantURL&quot;<br>
 * &nbsp; &nbsp;remoteFileNameField=&quot;msg&quot;<br>
 * &nbsp; &nbsp;remotePathField=&quot;msg&quot;<br>
 * &nbsp; &nbsp;serviceName=&quot;FtpFileService&quot;<br>
 * &nbsp; &nbsp;implClass=&quot;com,ecc.emp.comm.ftp.FtpFileAction&quot;&gt;<br>
 * &lt;/opStep&gt;<br>
 * <b>����˵��:</b><br>
 * &nbsp; &nbsp; fileType�������ļ����ͣ�0��ʾ���ն����ƴ��䣬1��ʾ�����ı�����<br>
 * &nbsp; &nbsp; tranType: ftp�ļ��ķ�ʽ��0��ʾ��get,1��ʾ��put<br>
 * &nbsp; &nbsp; localFileNameField�������ļ���<br>
 * &nbsp; &nbsp; localPathField������·���������ļ��ľ���·����<br>
 * &nbsp; &nbsp; remoteFileNameField��Զ�˷��������ļ���<br>
 * &nbsp; &nbsp; remotePathField��Զ�˷��������ļ�����·��<br>
 * &nbsp; &nbsp; serviceName��FtpFileService��������<br>
 * <b>����״̬��</b><br>
 * ���ײ��践������״̬��0Ϊ������-1Ϊ�쳣��<br>
 * 
 * @create date: 2017-8-11 17:42:50
 */
public class FtpFileAction extends EMPAction {

	String fileType = null;   // 0:�����ƴ��� 1:�ı�����

	String tranType = null;   // 0:���� 1:�ϴ�
	/**
	 * �������
	 */
	String serverAlias = null;

//	String serviceName = null;

	public FtpFileAction() {
		super();
	}

	/**
	 * @return String
	 * @throws EMPException 
	 * @see com.ecc.emp.flow.EMPAction#execute(com.ecc.emp.core.Context)
	 */
	public String execute(Context context) throws SFException {
		SFLogger logger=SFLogger.getLogger(context);
		//�����ļ���
		String localFileName = null;
		//��ű���·��
		String localPath = null;
		//Զ���ļ���
		String remoteFileName = null;
		//Զ��·��
		String remotePath = null;
		try {		
			checkParamNull("fileType",fileType);
			checkParamNull("tranType",tranType);

			FtpFileService service = (FtpFileService)context.getService(SFConst.SERVICE_FTP);
			service.setPassword("");
			service.setUserName("");
			service.setPort(21);
			if (!(localPath.endsWith("\\") || localPath.endsWith("/"))) {
				localPath = localPath + System.getProperty("file.separator");
			}

			service.FtpFile(fileType, tranType, localFileName, localPath, remoteFileName, remotePath,context);

		} catch (Exception e) {
			logger.info("FTP�����ļ�ʧ��,ԭ��:" + e.getMessage());
			return "-1";
		}
		return "0";

	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public void setTranType(String tranType) {
		this.tranType = tranType;
	}

	public String getServerAlias() {
		return serverAlias;
	}

	public void setServerAlias(String serverAlias) {
		this.serverAlias = serverAlias;
	}
	
	

}
