package com.sscc.fdep;

import common.exception.SFException;
import common.util.SFConst;

import core.cache.CacheMap;
import core.log.SFLogger;


public class mrapi {
	static {
		SFLogger logger=SFLogger.getLogger(SFConst.DEFAULT_TRXCODE);
//		System.loadLibrary("mrapi");
//		try {
//			String dllPath=ParamCache.getValue2("SZT", "SZT_DLL_PATH");//A����֤ͨ��̬���ӿ�·��
//			logger.info("loading szt mrapi lib="+dllPath);
//			System.load(dllPath);
//		} catch (SFException e) {
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		}
		
		try {
			//��ȡȫ��·��
			String rootPath=CacheMap.getCache(SFConst.GLOBEL_ROOT_PATH);
			rootPath=rootPath.replace("bizs/SFOnline/", "");
			System.load(rootPath+"\\dll_win32\\mrapi.dll");
		} catch (SFException e) {
			logger.error(e.getMessage());
		}
	}

	/********************************* FDEP V3 **********************************/

	/* ��ʼ��������Ϊ����
	 * return: 0-failed; 1-OK
	   App��AppPwd��Ӧc�ӿ��е�AppId��AppPwd�� ���ĸ�������ӦC�ӿ��еĽṹ��oConnInfo�и����ֶ�
	 */
	public native static int MrInit(String App,String AppPwd, String Ip,short Port,String Ipbak, short Portbak);



	/* ������Ϣ ��������SourceAppID���ʼ����ʱ���õ�App
	 * return:    ""-����ʧ��   pkgId-���ͳɹ�  
	   psPkg����Ҫ���͵����ݣ������ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� 
	 */

	public native static String MrSend( byte[] psPkg, String SourceUserID,String SourceAppID, String DestUserID, String DestAppID,
	String PkgID, String CorrPkgID, String UserData1, String UserData2, String ExpiredAbsTime, byte flag, byte Protocltype,int iMillSecTimeo);




	//return "NULL"-����ʧ��    ���򷵻�pkID(64byte) + CorrpkID(64byte) + sourceUserID(64byte) + sourceAppID(64byte) +destUserID(64byte) + UserData1(256byte) + UserData2(256byte) + data     ����ǰ7���ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� ��dataΪ���������� 

	public native static byte[] MrReceive1(String sAppID,String SourceUserID,String SourceAppID, String DestUserID, String DestAppID,
	String PkgID, String CorrPkgID, String UserData1, String UserData2, String ExpiredAbsTime, byte flag, byte Protocltype,int iMillSecTimeo);



	/* �ж��뽻������������Ƿ�����
	 * return: 0-link_not_ok; 1-link_ok
	   ����Ϊmrinit�����õ�app
	 */
	public native static int MrIsLinkOK(String sAppID);

	/*�ͷ���Դ
	  ����Ϊmrinit�����õ�app*/
	public native static void MrDestroy(String sAppID);


	/********************************* FDEP V4 **********************************/

	/* ��ʼ��������Ϊ����
	 * return: 0-failed; 1-OK
	   App��AppPwd��Ӧc�ӿ��е�AppId��AppPwd�� ���ĸ�������ӦC�ӿ��еĽṹ��oConnInfo�и����ֶ�
	 */
	public native static int Mr2Init(String App,String AppPwd, String Ip,short Port,String Ipbak, short Portbak);



	/* ������Ϣ ��������SourceAppID���ʼ����ʱ���õ�App
	 * return:    ""-����ʧ��   pkgId-���ͳɹ�  
	   psPkg����Ҫ���͵����ݣ������ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� 
	 */

	public native static String Mr2Send( byte[] psPkg, String SourceUserID,String SourceAppID, String DestUserID, String DestAppID,
	String PkgID, String CorrPkgID, String UserData1, String UserData2, String MsgType, byte flag, byte BizType, byte Priority, byte SensitiveLevel, int iMillSecTimeo);




	//return "NULL"-����ʧ��    ���򷵻�pkID(64byte) + CorrpkID(64byte) + sourceUserID(64byte) + sourceAppID(64byte) +destUserID(64byte) + UserData1(256byte) + UserData2(256byte) + data     ����ǰ7���ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� ��dataΪ���������� 

	public native static byte[] Mr2Receive1(String sAppID,String SourceUserID,String SourceAppID, String DestUserID, String DestAppID,
	String PkgID, String CorrPkgID, String UserData1, String UserData2, int iMillSecTimeo);



	/* �ж��뽻������������Ƿ�����
	 * return: 0-link_not_ok; 1-link_ok
	   ����Ϊmrinit�����õ�app
	 */
	public native static int Mr2IsLinkOK(String sAppID);

	/*�ͷ���Դ
	  ����Ϊmrinit�����õ�app*/
	public native static void Mr2Destroy(String sAppID);


	/* ��ȡ�汾��Ϣ
	 * return �汾���ַ���
	 */
	public native static byte[]  Mr2GetVersion();


	/* ������ID
	 * return PkgID
	 */
	public native static byte[]  Mr2CreatePkgID(String szHandleAppID);

	/* ��ȡ�Զ��û��Ƿ�����״̬
	/ return: -1:��������ʧ��  0:������    1:����
	*/
	public native static int  Mr2GetPeerUserStat(String szHandleAppID,String szPeerUserID);


	/* ����ע����������
	 * return: 0��ע��ɹ�,<0����ʧ��
	 */
	public native static int  Mr2RegRecvCondition(String szHandleAppId,String szSrcUserId, String szSrcAppId, String szDestUserId, String szDestAppId, 
	String szPkgId, String szCorrPkgId, String szUserData1,String szUserData2) ;


	///  "NULL��errmsg"-����ʧ��   ���򷵻�errcode(4byte)+pkID(64byte) + CorrpkID(64byte) + sourceUserID(32byte) + sourceAppID(32byte) +destUserID(32byte) + destAppID(32byte)+UserData1(256byte) + UserData2(256byte) + data     ����ǰ7���ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� ��dataΪ���������� 
	///   ����errcode= "0000"Ϊ�������������errcodeΪ��"0000"�ַ���������ϵͳ���صĴ�����Ϣ����ϸ������data�ֶ��У� 
	public native static byte[] Mr2Receive3(String sAppID,String SourceUserID,String SourceAppID, String DestUserID, String DestAppID,
	String PkgID, String CorrPkgID, String UserData1, String UserData2, int iMillSecTimeo);
}