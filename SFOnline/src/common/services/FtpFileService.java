package common.services;

import com.ecc.emp.core.Context;
import com.ecc.emp.service.EMPService;
import common.exception.SFException;
import common.util.DesCrypt;

import core.log.SFLogger;

/**
 * <b>功能描述：</b><br>
 * 该类为FTP文件服务类，提供通过FTP传输文件的服务。<br>
 * <b>配置示例:</b><br>
 * <br>&lt;ftpFileService id=&quot;ftpFileService&quot; <br>
 *  &nbsp; &nbsp;ftpServer=&quot;127.0.0.1&quot; <br>
 *  &nbsp; &nbsp;userName=&quot;V&quot; <br>
 *  &nbsp; &nbsp;password=&quot;V&quot; <br>
 *  &nbsp; &nbsp;implClass=&quot;com.ecc.emp.ftp.FtpFileService&quot;
 *  /&gt;<br>
 * <b>参数说明:</b><br>
 *  &nbsp; &nbsp; ftpServer：FTP服务器地址<br>
 *  &nbsp; &nbsp; userName：登陆FTP用户名<br>
 *  &nbsp; &nbsp; password：登陆密码<br>
 */
public class FtpFileService extends EMPService {
	
	private String ftpServer;

	/**
	 * 默认21端口
	 */
	private int port = 21;
	
	private String userName = "anonymous";

	private String password = "user@anonymous.com";
	
	

	public String getFtpServer() {
		return ftpServer;
	}

	public int getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * FtpFileService 构造子注解。
	 */
	public FtpFileService() {
		super();
	}

	/**
	 * FtpFileService 构造子注解。
	 * @param arg1 java.lang.String
	 * @exception java.io.IOException 异常说明。
	 */
	public FtpFileService(String arg1) throws java.io.IOException {
		super();
		super.setName(arg1);
	}

	/**
	 * @param fileType	 	  java.lang.String 		传输文件类型，0表示按照二进制传输，1表示按照文本传输；
	 * @param tranType	 	  java.lang.String 		ftp文件的方式，0表示是get,1表示是put;
	 * @param localFileName  java.lang.String 		本地文件名称
	 * @param localPath 	  java.lang.String 		本地文件路径,如：c:\\dse\\simulatepackage\\
	 * @param remoteFileName java.lang.String 		ftp服务器文件名称
	 * @param remotePath 	  java.lang.String 		ftp服务器文件路径,如：/tmp/ftptest/
	 * @return
	 */

	public int FtpFile(
		String fileType,
		String tranType,
		String localFileName,
		String localPath,
		String remoteFileName,
		String remotePath,Context context) throws SFException {

		SFLogger logger=SFLogger.getLogger(context);
		if (ftpServer == null){
			logger.info("param ftpServer not set for service ["+this.getName()+"]!");
			throw new SFException("param ftpServer not set for service ["+this.getName()+"]!");
		}
		
		//long btime = System.currentTimeMillis();
		sun.net.ftp.FtpClient ftpClient = null;
		byte[] buffer = null;

		String serverDesc = "ftp://"+userName+":"+password+"@"+ftpServer+":"+port;
		
		try {
			ftpClient = new sun.net.ftp.FtpClient();
			try {
				ftpClient.openServer(ftpServer,port);
				ftpClient.login(userName, password);
				//日志打印
				logger.info("Connected to FTP server: "+serverDesc);
			} catch (Exception ee) { //连接失败
				//日志打印
				logger.error("Connect to FTP server: "+serverDesc+" failed!");
				throw new SFException("Connect to FTP server: "+serverDesc+" failed!",ee);				
			}
			
			try {
				ftpClient.cd(remotePath);
				logger.info(serverDesc+": CD "+remotePath);
			} catch (Exception ee) { //找不到远程目录
				logger.error(serverDesc+": CD "+remotePath+" failed!");
				throw new SFException(serverDesc+": CD "+remotePath+" failed!",ee);
				
			}
			if (fileType.trim().equals("0")) {//表示二进制传输	
				logger.info(serverDesc+": TYPE I (set to binary mode)");
				ftpClient.binary();
				logger.info(serverDesc + ftpClient.getResponseString() + " (ftp response)");
			} else if (fileType.trim().equals("1")) {//表示文本传输
				logger.info(serverDesc+": TYPE A (set to ascii mode)");
				ftpClient.ascii();
				logger.info(serverDesc + ftpClient.getResponseString() + " (ftp response)");
			}
			buffer = new byte[1024];
			if (tranType.equals("0")) {//表示下传
				logger.info(serverDesc+": RETR "+remoteFileName+" (download file)");
				sun.net.TelnetInputStream os = null;
				try {
					os = ftpClient.get(remoteFileName);
					logger.info(serverDesc + ftpClient.getResponseString() + " (ftp response)");
				} catch (Exception ee) {//找不到远程文件
					logger.error(serverDesc+": RETR "+remoteFileName+" failed!");
					throw new SFException(serverDesc+": RETR "+remoteFileName+" failed!",ee);
				}
				java.io.FileOutputStream fi = null;
				try {
					fi =new java.io.FileOutputStream(localPath + localFileName);
					logger.info(serverDesc+": Save file as "+localPath+localFileName);
				} catch (Exception ee) {//本地路径有错
					logger.error(serverDesc+": Save file as "+localPath+localFileName+" failed!");
					throw new SFException(serverDesc+": Save file as "+localPath+localFileName+" failed!",ee);
				}

				while (true) {
					int len = os.read(buffer);
					if (len == 0 || len == -1) {
						break;
					}
					fi.write(buffer, 0, len);
					fi.flush();
				}
				os.close();
				fi.close();
			} else if (tranType.equals("1")) {//表示上传
//				EMPLog.log(EMPConstance.EMP_COMM, EMPLog.INFO, 0, 
//						serverDesc+": STOR "+remoteFileName+" (upload file)");
				logger.info(serverDesc+": STOR "+remoteFileName+" (upload file)");
				sun.net.TelnetOutputStream os = null;
				try {
					ftpClient.sendServer("STOR "+remoteFileName);
					os = ftpClient.put(remoteFileName);
					logger.info(serverDesc + ftpClient.getResponseString());
				} catch (Exception e) {//远程文件有错
					logger.info(serverDesc+": STOR "+remoteFileName+" failed!");
					throw new SFException(serverDesc+": STOR "+remoteFileName+" failed!",e);
				}
				
				java.io.FileInputStream fi = null;
				try {
					fi = new java.io.FileInputStream(localPath + localFileName);
				} catch (Exception ee) {//本地路径或者文件有错
					logger.info(serverDesc+": Open file "+localPath+localFileName+" failed!");
					throw new SFException(serverDesc+": Open file "+localPath+localFileName+" failed!",ee);
				}

				buffer = new byte[1024];
				while (true) {
					int len = fi.read(buffer);
					if (len == 0 || len == -1) {
						break;
					}
					os.write(buffer, 0, len);
					logger.info(serverDesc+": SEND "+buffer+" (upload send buffer)");
					os.flush();
				}
				logger.info(serverDesc+": SEND "+localFileName+"完成 (upload )");
				os.close();
				fi.close();
			}
			ftpClient.closeServer();

/*
			CTEConstance.appendMCData(
				"hsftp",
				"",
				new Context(),
				"服务["+getName()+"]耗时："+String.valueOf(System.currentTimeMillis() - btime)+"ms",
				String.valueOf(System.currentTimeMillis() - btime));
*/
			
		} catch (SFException e) {
			throw e;
		} catch (Exception e1) {
			logger.error(serverDesc+": Ftp operation failed!");
			throw new SFException(serverDesc+": Ftp operation failed!",e1);
		}
		return 0;
	}

	/*
	 * 获得各个参数的值
	 */
	public void setFtpServer(String value){
		this.ftpServer = value;
	}
	
	public void setUserName(String value){
		this.userName = value;
	}
	
	public void setPassword(String value) throws Exception{
		this.password = DesCrypt.defaultDecode(value);
	}
	
	public void setPort(int value){
		this.port = value;
	}
	
//	public  static void main(String argv[]) throws Exception
//	{
//		FFtpFileService FBSFFS=new FBSFtpFileService();
//		try {
//			FBSFFS.setFtpServer("10.203.130.94");
//			FBSFFS.setUserName("sdb");
//			FBSFFS.setPassword("8E9D882C91E10EAA");
//			
//			FBSFFS.FtpFile("0", "1", "WH20120930.txt", "D:\\fbs\\UPLOAD\\", "WH20120930.txt", ".");
//		} catch (EMPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return;
//	}
}
