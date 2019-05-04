package common.action.expand.ftp;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import common.exception.SFException;
import common.services.FtpFileService;
import common.util.SFConst;

import core.log.SFLogger;

/**
 * <b>功能描述：</b><br>
 * 该交易步骤提供通过FTP上传下载文件的功能。<br>
 * <b>配置示例:</b><br>
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
 * <b>参数说明:</b><br>
 * &nbsp; &nbsp; fileType：传输文件类型，0表示按照二进制传输，1表示按照文本传输<br>
 * &nbsp; &nbsp; tranType: ftp文件的方式，0表示是get,1表示是put<br>
 * &nbsp; &nbsp; localFileNameField：本地文件名<br>
 * &nbsp; &nbsp; localPathField：本地路径（本地文件的绝对路径）<br>
 * &nbsp; &nbsp; remoteFileNameField：远端服务器方文件名<br>
 * &nbsp; &nbsp; remotePathField：远端服务器方文件绝对路径<br>
 * &nbsp; &nbsp; serviceName：FtpFileService服务名称<br>
 * <b>返回状态：</b><br>
 * 交易步骤返回两个状态，0为正常，-1为异常。<br>
 * 
 * @create date: 2017-8-11 17:42:50
 */
public class FtpFileAction extends EMPAction {

	String fileType = null;   // 0:二进制传输 1:文本传输

	String tranType = null;   // 0:下载 1:上传
	/**
	 * 服务别名
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
		//本地文件名
		String localFileName = null;
		//存放本地路径
		String localPath = null;
		//远程文件名
		String remoteFileName = null;
		//远程路径
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
			logger.info("FTP下载文件失败,原因:" + e.getMessage());
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
