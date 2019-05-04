#ifndef __SSCC_FDEPV4_MR2API_H__
#define __SSCC_FDEPV4_MR2API_H__

#ifdef __cplusplus
extern "C" {
#endif 

#ifdef WIN32
#else   /*UNIX or Linux*/
#define _stdcall
#endif

#define MR2_MSGFLAG_PERSIST          0x01   /*�־���Ϣ��־*/
#define MR2_MSGFLAG_COMPRESS         0x02   /*ѹ����־*/
#define MR2_MSGFLAG_REPLYTOME		 0X04   /*Ӧ����Զ����͵����ͷ���־*/


#define MR2_MAXLEN_ADDR              32     /*Դ��ַ��Ŀ�ĵ�ַ����󳤶� @2012.1.12*/
#define MR2_MAXLEN_PKGID             64     /*��ID����󳤶�(Ŀǰ�汾��32λOS�ϳ���Ϊ34,64λOS�ϳ���Ϊ39��44)*/
#define MR2_MAXLEN_USERDATA          256    /*�û����ݵ���󳤶�*/

#define MR2_MAXLEN_MSGTYPE			 8	/*Ϊ�˼��ݾɵģ�������ɰ�һ��*/

#define MR2_MAXLEN_IP                16


/* MrSend, Mr2Browse, MrReceive1��MrReceive2�������صĴ�����.
 * ʹ��ʱһ��ֻ��Ҫ�ж��Ƿ�Ϊ0, ���Ϊ0��ʾ�ɹ�,�����ʾʧ��.
 */
#define MR2_ERRCODE_OK                0
#define MR2_ERRCODE_PARAMERR         -1
#define MR2_ERRCODE_CONNERR          -2
#define MR2_ERRCODE_TIMEEXPIRED      -3
#define MR2_ERRCODE_TIMEOUT          -4
#define MR2_ERRCODE_NOMSG            -5
#define MR2_ERRCODE_BUFTOOSHORT      -6
#define MR2_ERRCODE_BUFTOOBIG        -7
#define MR2_ERRCODE_SYSERROR         -8
#define MR2_ERRCODE_COMMU_NOTALLOW   -9
#define MR2_ERRCODE_DEST_NOTONLINE   -10
#define MR2_ERRCODE_DEST_FULL        -11

/* ������Ϣ���Խṹ�� 
 * Ŀ���û�ֻ��һ����
 */
typedef struct _tagSTUMsgProperty2
{
    char                m_szSourceUserID[MR2_MAXLEN_ADDR];     /**< Դ�û���ʶ��������'\0'��β���ַ�����*/
    char                m_szSourceAppID[MR2_MAXLEN_ADDR];     /*ԴӦ�ñ�ʶ��������'\0'��β���ַ�����*/
    char                m_szDestUserID[MR2_MAXLEN_ADDR];       /*Ŀ���û���ʶ��������'\0'��β���ַ�����*/
    char                m_szDestAppID[MR2_MAXLEN_ADDR];       /*Ŀ��Ӧ�ñ�ʶ��������'\0'��β���ַ�����*/
    char                m_szPkgID[MR2_MAXLEN_PKGID];         /*��ID, ������'\0'��β���ַ���. �������û�ʹ��MrCreatePkgID��������,�������*/
    char                m_szCorrPkgID[MR2_MAXLEN_PKGID];     /*��ذ�ID, ������'\0'��β���ַ���, ���û�ʹ��*/
    char                m_szUserData1[MR2_MAXLEN_USERDATA];  /*�û�����1, ������'\0'��β���ַ���, ���û�ʹ��*/
    char                m_szUserData2[MR2_MAXLEN_USERDATA];  /*�û�����2, ������'\0'��β���ַ���, ���û�ʹ��*/
    
    unsigned char       m_ucFlag;      /*��־:������MR2_MSGFLAG_PERSIST��MR2_MSGFLAG_COMPRESS�ȱ�־��λ��.*/
	unsigned char       m_ucBizType;      /*ҵ�����ͣ���ǰȡֵ��(����������):0-�������,10-����ת��,11-����ת��,12-�ʽ𻮲�,13-��֤����,14-���Ӷ���,15-������ȯ,16-�����̺�,17-ת��ͨ,18-BתH. */
	unsigned char       m_ucPriority; /* ���ȼ��� 5Ϊ��ͣ�3Ϊ���*/
	unsigned char       m_ucSensitiveLevel;    /* �����Լ���0Ϊ��ͣ�255Ϊ���*/
	char				m_szMsgType[MR2_MAXLEN_MSGTYPE]; /*��Ϣ���ͣ�Ŀǰֻ��ȡֵ: M(��Ϣ)��F(�ļ�). ���ΪF(�ļ�),���ڽ�����־����Զ����ӡ������.*/
} STUMsgProperty2;

/* ���Ӳ����ṹ�� */
typedef struct _tagSTUConnInfo2
{
    char                m_szMRIP[MR2_MAXLEN_IP];       /*����MR��IP. ������'\0'��β���ַ���. "127.0.0.1"*/
    unsigned short      m_usMRPort;         /*����MR�Ķ˿�.    51231*/
} STUConnInfo2;


typedef int (*OnReceiveCallBack2)(const char* psPkg, int iPkgLen, const STUMsgProperty2* pMsgPropery, void* pvUserData);


/* ���ܣ����ӽ���ͻ��˵ĳ�ʼ��: psMyID���û�Ӧ�õ�����,����app1,app2��.
 * ע�⣺������Ϣ��ʱ������ֻ��ʹ�ú���MrInit�еĻص�����OnReceive���н��գ�����ֻ��ʹ��MrReceive1/MrReceive1_FreeBuf��Mr2Browse/MrReceive2��MrReceive3/MrReceive1_FreeBuf���н��գ����߲���ͬʱʹ�á�
 *     �ú������ص�Handleֻ����ͬһ��������ʹ��,��ͬһ�����̵Ķ���߳�֮��Ҳ����ʹ��,����ȫ������Ҳ���̰߳�ȫ��.
 *     �����ڲ�ͬ�Ľ���֮�䲻��ʹ��ͬһ��Handle��������Unix��fork��,���������е���MrInit���ɵ�Handle���ӽ����в�����Ч.
 * return: ��NULL-OK; NULL-failed
 */
void*  _stdcall Mr2Init(const char* psAppID, const char* psAppPasswd, 
	OnReceiveCallBack2 onReceive,const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, 
	void* pvUserData);


void   _stdcall Mr2Init2(void** ppHandle, const char* psAppID, const char* psAppPasswd,
	STUMsgProperty2* pOnRecvMsgPropery,OnReceiveCallBack2 onReceive,
	const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount,
	void* pvUserData, int iThreadCount);

/* ��������ĳ�ʼ��: psUserCertID���û���֤��������������ƻ����û���(�����Ҳ���ʱ��дΪ"/C=CN/CN=twtest1")
 * psAppID��Ӧ�õ����ƣ���֪��ʱ��app1��
 * return: ��NULL-OK; NULL-failed
*/
void*  _stdcall Mr2Init1(const char* psUserCertID, const char* psAppID, const char* psUserPasswd, 
                        OnReceiveCallBack2 OnReceive, const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, void* pvUserData);

void  _stdcall Mr2Init1Ex1(void** ppHandle, const char* psUserCertID, const char* psAppID, const char* psUserPasswd, STUMsgProperty2* pOnRecvMsgPropery,
						OnReceiveCallBack2 OnReceive, const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, void* pvUserData, int iThreadCount);

/* ���ɰ�ΨһID
 * return: 0-OK; others-failed
 */
int   _stdcall Mr2CreatePkgID(void* pHandle, char szPkgID[MR2_MAXLEN_PKGID]);


/* ������Ϣ����ֹͬһ���û������־���Ϣ
 * return: 0-send_OK; others-failed
 */
int   _stdcall Mr2Send(void* pHandle, const char* psPkg, int iPkgLen, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);




/* ָ��������mr��������, һ�������е��������������ǰ������.
 */
int _stdcall Mr2RegRecvCondition(void* pHandle, STUMsgProperty2* pArrMsgPropery, int iArrayCount);  


/* MrReceive1��MrReceive2�������������MrReceive1�����û������ڴ棬��ʹ
 *    ��MrReceive1_FreeBuf�ͷţ���MrReceive2��Ҫ�û��Լ������ڴ档
 * ע��MrReceive1�����е�ppsPkg��һ�����ز�����ָ����հ��Ļ�������
 *    �������û�ʹ��MrReceive1_FreeBuf�����ͷŸ��ڴ档
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */

int   _stdcall Mr2Receive1(void* pHandle, char** ppsPkg, int* piOutPkgLen, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);
void  _stdcall Mr2Receive1_FreeBuf(char* psPkg);


/* MrReceive2�ǽ�����Ϣ��������Ϣ�Ӷ�����ɾ����Ҳ����ֻ����MrReceive��������Mr2Browse��
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */
int   _stdcall Mr2Receive2(void* pHandle, char* psPkg, int* piOutPkgLen, int iBufLenIn, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);



/*  *piErrSXCode==0��ʾ����������������ֵ��Ϊ1��5���ֱ��ʾĿ�겻���ڡ�
 *    Ŀ����󡢶�����������ʱ���ڡ�ϵͳ���󣬴�ʱ*ppsPkg���Ǵ����ַ�����
 *        ���Ը�����ذ�id��pMsgPropery����д����ȡ��Ӧ���Ĵ�����Ϣ��
 *        ����ذ�id��pMsgPropery����д��Ϊ�գ��������ȫ������������Ľ�������
 */
int _stdcall  Mr2Receive3(void* pHandle, char** ppsPkg, int* piOutPkgLen, int* piErrSXCode, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);


/* �ж�API��MR��API�뽻������������Ƿ�����.
 * return: 0-link_not_ok; 1-link_ok
 */
int   _stdcall Mr2IsLinkOK(void* pHandle);


/*ȡ�ñ�API�İ汾��*/
void   _stdcall Mr2GetVersion(char* psBufVersion, int iBufLen);


/*�ͷ���Դ*/
void  _stdcall Mr2Destroy(void* pHandle);




typedef struct _tagSTUUserAddr
{
	char m_szUserID[MR2_MAXLEN_ADDR];	/*�û���ʶ��������'\0'��β���ַ�����*/
	char m_szAppID[MR2_MAXLEN_ADDR];	/*Ӧ�ñ�ʶ��������'\0'��β���ַ�����*/
} STUUserAddr;

/* Ⱥ����Ϣ���Խṹ�塣
 * �뵥����Ϣ���Բ�֮ͬ����Ŀ���û������Ƕ����
 * m_pArrDestUserAddr-��ʾĿ���û�����ָ�룬m_iDestUserCount-��ʾĿ���û�������
 */
typedef struct _tagSTUMultiDestMsgProperty
{
	char                m_szSourceUserID[MR2_MAXLEN_ADDR];     /*Դ�û���ʶ��������'\0'��β���ַ�����*/
	char                m_szSourceAppID[MR2_MAXLEN_ADDR];     /*ԴӦ�ñ�ʶ��������'\0'��β���ַ�����*/
	int					m_iDestUserCount;					/*Ŀ���û�����*/
	STUUserAddr*		m_pArrDestUserAddr;					/*Ŀ���û��ṹ����ָ��*/
	char                m_szPkgID[MR2_MAXLEN_PKGID];         /*��ID, ������'\0'��β���ַ���. �������û�ʹ��MrCreatePkgID��������,�������*/
	char                m_szCorrPkgID[MR2_MAXLEN_PKGID];     /*��ذ�ID, ������'\0'��β���ַ���, ���û�ʹ��*/
	char                m_szUserData1[MR2_MAXLEN_USERDATA];  /*�û�����1, ������'\0'��β���ַ���, ���û�ʹ��*/
	char                m_szUserData2[MR2_MAXLEN_USERDATA];  /*�û�����2, ������'\0'��β���ַ���, ���û�ʹ��*/

	unsigned char       m_ucFlag;      /*��־:������MR2_MSGFLAG_PERSIST��MR2_MSGFLAG_COMPRESS�ȱ�־��λ��.*/
	unsigned char       m_ucBizType;      /*ҵ�����ͣ�ȡֵΪ������ֵ����11Ϊ����ת��*/
	unsigned char       m_ucPriority; /* ���ȼ��� 5Ϊ��ͣ�3Ϊ���*/
	unsigned char       m_ucSensitiveLevel;    /* �����Լ���0Ϊ��ͣ�255Ϊ���*/
	char				m_szMsgType[MR2_MAXLEN_MSGTYPE]; /*��Ϣ���ͣ�Ŀǰֻ��ȡֵ: M(��Ϣ)��F(�ļ�). ���ΪF(�ļ�),���ڽ�����־����Զ����ӡ������.*/
} STUMultiDestMsgProperty;


/* Ⱥ����Ϣ�ӿڣ�pMsgPropery-Ⱥ����Ϣ����(�������Ŀ���û���ʶ)����֧�ַ��Ϳɿ���Ϣ. Ⱥ����Ϣ��������˷ֲ�ɶ����������Ϣ�ֱ���.
 * return: 0-send_OK; others-failed
 */
int   _stdcall Mr2MultiDestSend(void* pHandle, const char* psPkg, int iPkgLen, STUMultiDestMsgProperty* pMsgPropery, int iMillSecTimeo);

/* ��ȡͨ�ŶԶ��û�״̬�ӿڣ�pHandle-��ʼ�����صľ����psPeerUserID-�Զ��û���ʶ��
 * piOutStat-�����ز������Զ��û�״̬��1-���ߣ�0-�����ߡ�����ˢ��ʱ����
 * return: 0-�ɹ�; others-ʧ�ܣ���ϸ�ο������롣
 */
int   _stdcall Mr2GetPeerUserStat(void* pHandle, const char* psPeerUserID, int* piOutStat);




#ifdef JAVA_API


//wangcx20090815{����jni
#include <jni.h>
//����0 ����ʼ��ʧ�ܣ� ����1���ɹ�
//{ modify by lingb 20110401 ����֧��LINUX64λ����ϵͳ,��
// LINUX64��,sizeof(void *)=8����Ҫʹ��java��jlong ����,
//  ͬʱ����ͬһ������ʵ���ڵ�ͬһ��APP��ֻ������MR����ͬ
//  һ������


#if defined(_WIN32)  || defined(_LINUX64_JDK32) || defined(_JDK32)
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2Init
(JNIEnv *, jclass,jstring,jstring,jstring,jshort,jstring,jshort);
#else
#if defined(_LINUX64_JDK64)  || defined(_AIX64_JDK64) || defined(_JDK64)
JNIEXPORT jlong JNICALL Java_com_sscc_fdep_mrapi_Mr2Init
(JNIEnv *, jclass,jstring,jstring,jstring,jshort,jstring,jshort);
#endif
#endif


JNIEXPORT jstring JNICALL Java_com_sscc_fdep_mrapi_Mr2Send
(JNIEnv *, jclass, jbyteArray, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jbyte,jbyte,jbyte,jbyte,jint);

JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2Receive1
(JNIEnv *, jclass, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jint);

JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2IsLinkOK
(JNIEnv *, jclass, jstring);

JNIEXPORT void JNICALL Java_com_sscc_fdep_mrapi_Mr2Destroy
(JNIEnv *, jclass, jstring);


/// ��������:��ȡ��API�İ汾�ŵĽӿ�.
/// 
/// ��������:��ȡ��API�İ汾��.
/// @param  env:JNI�����ľ��   
/// @param  obj:JNI����
/// @return  jbyteArray:�汾��Ϣ
/// 
/// @note      
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2GetVersion
(JNIEnv *env, jclass obj);


/// ��������:Java��������Ϣ��ID
/// 
/// ��������:����һ��ȫ��Ψһ��UUID����Ϊ��Ϣ����ʶ
/// @param  env:JNI�����ľ��   
/// @param  obj:JNI����
/// @param  szHandleAppID:JNI����
/// @return    
/// @retval  jbyteArray: ������PKGID
/// 
/// @note      
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2CreatePkgID
(JNIEnv *env, jclass obj, jstring szHandleAppID);


/// ��������: Java�汾��ȡͨ�ŶԶ��û�״̬
/// 
/// ��������:
/// @param  env:JNI�����ľ��   
/// @param  obj:JNI����
/// @param  szHandleAppID:Init��ʼ������APPID
/// @param  szPeerUserID:�Զ�UserID
/// @return    
/// @retval  -1:��������ʧ��
/// @retval  0:������
/// @retval  1:����
/// 
/// @note      �ڳ�ʼ��֮����ã�����ˢ��ʱ����
///
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2GetPeerUserStat
(JNIEnv *env, jclass obj, jstring szHandleAppID,jstring szPeerUserID);


/// ��������:JAVA�汾һ���������н�������
/// 
/// @param  env:JNI�����ľ��   
/// @param  obj:JNI����
/// @param  szHandleAppID:Init��ʼ������APPID
/// @param  szSrcUserId:ԴUSERID
/// @param  szSrcAppId:ԴAPPID
/// @param  szDestUserId:Ŀ��USERID
/// @param  szDestAppId:Ŀ��APPID
/// @param  szPkgId:��ID
/// @param  szCorrPkgId:Դ��ID
/// @param  szUserData1:�����ֶ�1
/// @param  szUserData2:�����ֶ�2
/// @return    
/// @retval    0: �ɹ�.
/// @retval   <0: ʧ��.
/// 
/// @note      
///
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2RegRecvCondition
(JNIEnv * env, jclass obj, jstring szHandleAppId, jstring szSrcUserId, jstring szSrcAppId, jstring szDestUserId, jstring szDestAppId, 
 jstring szPkgId, jstring szCorrPkgId, jstring szUserData1, jstring szUserData2);

/// ��������:JAVA�汾��ӦReceive3����
/// 
/// @param  env:JNI�����ľ��   
/// @param  obj:JNI����
/// @param  szHandleAppID:Init��ʼ������APPID
/// @param  szSrcUserId:ԴUSERID
/// @param  szSrcAppId:ԴAPPID
/// @param  szDestUserId:Ŀ��USERID
/// @param  szDestAppId:Ŀ��APPID
/// @param  szPkgId:��ID
/// @param  szCorrPkgId:Դ��ID
/// @param  szUserData1:�����ֶ�1
/// @param  szUserData2:�����ֶ�2
/// @param  iArrayCount:�������
/// @return    
/// @retval    0: �ɹ�.
/// @retval   <0: ʧ��.
/// 
/// @note return "NULL��errmsg"-����ʧ��   ���򷵻�errcode(4byte)+pkID(64byte) + CorrpkID(64byte) + sourceUserID(32byte) + sourceAppID(32byte) +destUserID(32byte) + destAppID(32byte)+UserData1(256byte) + UserData2(256byte) + data     ����ǰ7���ֶ�ͬC�ӿ���pMsgPropery�и����ֶ� ��dataΪ���������� 
///       ����errcode= "0000"Ϊ�������������errcodeΪ��"0000"�ַ���������մ���  
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2Receive3
(JNIEnv * env, jclass obj, jstring szHandleAppId, jstring szSrcUserId, jstring szSrcAppId, jstring szDestUserId, jstring szDestAppId, 
 jstring szPkgId, jstring szCorrPkgid, jstring szUserData1, jstring szUserData2, jint MillSecTimeo);

#endif


#ifdef __cplusplus
}
#endif 


#endif



