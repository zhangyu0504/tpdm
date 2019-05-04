#ifndef __MR_MRAPI_H__
#define __MR_MRAPI_H__

#ifdef __cplusplus
extern "C" {
#endif 


#ifdef WIN32
#else   /*UNIX or Linux*/
#define _stdcall
#endif

#define MR_PROTOCOLTYPE_MRSTANDAND     0x00  /*本系统支持的标准协议*/
#define MR_PROTOCOLTYPE_SELFCUSTOM     0x00  /*用户自定义类型协议*/

#define MR_MSGFLAG_PERSIST          0x01   /*持久消息标志*/
#define MR_MSGFLAG_COMPRESS         0x02   /*压缩标志*/


#define MR_MAXLEN_ADDR              64     /*源地址或目的地址的最大长度*/
#define MR_MAXLEN_PKGID             64     /*包ID的最大长度(目前版本中实际长度为36)*/
#define MR_MAXLEN_USERDATA          256    /*用户数据的最大长度*/
#define MR_FIXLEN_EXPIREDABSTIME    20     /*过期时间的固定长度*/


/* MrSend, MrBrowse, MrReceive1或MrReceive2函数返回的错误码.
 * 使用时一般只需要判断是否为0, 如果为0表示成功,否则表示失败.
 */
#define MR_ERRCODE_OK                0
#define MR_ERRCODE_PARAMERR         -1
#define MR_ERRCODE_CONNERR          -2
#define MR_ERRCODE_TIMEEXPIRED      -3
#define MR_ERRCODE_TIMEOUT          -4
#define MR_ERRCODE_NOMSG            -5
#define MR_ERRCODE_BUFTOOSHORT      -6
#define MR_ERRCODE_BUFTOOBIG        -7
#define MR_ERRCODE_SYSERROR         -8
#define MR_ERRCODE_COMMU_NOTALLOW   -9
#define MR_ERRCODE_DEST_NOTONLINE   -10
#define MR_ERRCODE_DEST_FULL        -11



typedef struct _tagSTUMsgProperty
{
    char                m_szSourceUserID[MR_MAXLEN_ADDR];     /*源用户标识，必须是'\0'结尾的字符串。*/
    char                m_szSourceAppID[MR_MAXLEN_ADDR];     /*源应用标识，必须是'\0'结尾的字符串。*/
    char                m_szDestUserID[MR_MAXLEN_ADDR];       /*目的用户标识，必须是'\0'结尾的字符串。*/
    char                m_szDestAppID[MR_MAXLEN_ADDR];       /*目的应用标识，必须是'\0'结尾的字符串。*/
    char                m_szPkgID[MR_MAXLEN_PKGID];         /*包ID, 必须是'\0'结尾的字符串. 或者由用户使用MrCreatePkgID函数生成,或者填空*/
    char                m_szCorrPkgID[MR_MAXLEN_PKGID];     /*相关包ID, 必须是'\0'结尾的字符串, 供用户使用*/
    char                m_szUserData1[MR_MAXLEN_USERDATA];  /*用户数据1, 必须是'\0'结尾的字符串, 供用户使用*/
    char                m_szUserData2[MR_MAXLEN_USERDATA];  /*用户数据2, 必须是'\0'结尾的字符串, 供用户使用*/
    char                m_szExpiredAbsTime[MR_FIXLEN_EXPIREDABSTIME];   /*过期时间(绝对时间表示). 必须是'\0'结尾的字符串. 格式为YYYY-MM-DD hh:mm:ss. 例如2006-09-21 03:45:00,默认过期时间为当天23:59:59.*/
                                                                        /* 当为空时，如果对方用户不在线，或者对方应用未连接，则立即过期。*/
    unsigned char       m_ucFlag;      /*标志:可以是MR_MSGFLAG_PERSIST或MR_MSGFLAG_COMPRESS等标志的位或.*/
	unsigned char       m_ucProtocolType;      /*业务类型，取值为上述各值，如11为银基转帐*/
#ifdef _SSE  /*wangcx20081218交易所需求用的扩展字段*/
	unsigned char       m_ucPriority; /* 优先级， 0为最低，255为最高*/
	unsigned char       m_ucLevel;    /* 敏感性级别，0为最低，255为最高*/
	char                m_szMsgType[MR_MAXLEN_USERDATA]; /* 消息类型， "M"为即使消息（默认），"F"为文件消息，其他为订阅消息*/
#endif	
} STUMsgProperty;


typedef struct _tagSTUConnInfo
{
    char                m_szMRIP[16];       /*主用MR的IP. 必须是'\0'结尾的字符串. "127.0.0.1"*/
    unsigned short      m_usMRPort;         /*主用MR的端口.    51231*/
    char                m_szMRIPBak[16];    /*备用MR的IP. 必须是'\0'结尾的字符串. 不用时,可以为空*/
    unsigned short      m_usMRPortBak;      /*备用MR的端口.  不用时,可以为0*/
} STUConnInfo;


typedef int (*OnReceiveCallBack)(const char* psPkg, int iPkgLen, const STUMsgProperty* pMsgPropery, void* pvUserData);


/* 功能：连接接入客户端的初始化: psMyID是用户应用的名称,例如app1,app2等.
 * 注意：接收消息包时，或者只能使用函数MrInit中的回调函数OnReceive进行接收；或者只能使用MrReceive1/MrReceive1_FreeBuf、MrBrowse/MrReceive2或MrReceive3/MrReceive1_FreeBuf进行接收，两者不能同时使用。
 *     该函数返回的Handle只能在同一个进程内使用,在同一个进程的多个线程之间也可以使用,以下全部函数也是线程安全的.
 *     但是在不同的进程之间不能使用同一个Handle，例如在Unix下fork后,在主进程中调用MrInit生成的Handle在子进程中不再有效.
 * return: 非NULL-OK; NULL-failed
 */
void* _stdcall MrInit(const char* psAppID, const char* psAppPasswd,
                        OnReceiveCallBack onReceive,
                        const STUConnInfo oConnInfo, void* pvUserData);


void _stdcall MrInit2(void** ppHandle, const char* psAppID, const char* psAppPasswd, 
	STUMsgProperty* pOnRecvMsgPropery,OnReceiveCallBack onReceive,
	const STUConnInfo* pConnInfo, void* pvUserData, int iThreadCount);


void* _stdcall MrInit1(const char* psUserCertID, const char* psAppID, const char* psUserPasswd,
						OnReceiveCallBack OnReceive, 
						const STUConnInfo oConnInfo, void* pvUserData);


void _stdcall MrInit1Ex1(void** ppHandle, const char* psUserCertID, const char* psAppID, const char* psUserPasswd,
	STUMsgProperty* pOnRecvMsgPropery,OnReceiveCallBack OnReceive,
	const STUConnInfo* pConnInfo, void* pvUserData, int iThreadCount);





/* 生成包唯一ID
 * return: 0-OK; others-failed
 */
int   _stdcall MrCreatePkgID(void* pHandle, char szPkgID[MR_MAXLEN_PKGID]);


/* 发送消息
 * return: 0-send_OK; others-failed
 */
int   _stdcall MrSend(void* pHandle, const char* psPkg, int iPkgLen, STUMsgProperty* pMsgPropery, int iMillSecTimeo);


/* 接收消息包时，或者只能使用函数MrInit中的回调函数OnReceive进行接收；
 *   或者只能使用MrReceive1/MrReceive1_FreeBuf、MrBrowse/MrReceive2
 *   或MrReceive3/MrReceive1_FreeBuf进行接收，两者不能同时使用。
 * 亦即：当OnReceive==NULL时，才能使用MrReceive1/MrReceive1_FreeBuf、
 *   MrBrowse/MrReceive2或MrReceive3/MrReceive1_FreeBuf函数；
 *   当OnReceive!=NULL时,不能使用MrBrowse和MrReceive函数。
 */


/* MrBrowse是浏览指定的消息的下一个消息，如果oMsgPropery的各个成员都为
 *    空，则是浏览第一个消息.
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */
int   _stdcall MrBrowse(void* pHandle, int* piOutPkgLen, STUMsgProperty* pMsgPropery, int iMillSecTimeo);


/* MrReceive1与MrReceive2的区别仅仅在于MrReceive1将替用户分配内存，并使
 *    用MrReceive1_FreeBuf释放；而MrReceive2需要用户自己分配内存。
 * 注意MrReceive1函数中的ppsPkg是一个返回参数，指向接收包的缓冲区，
 *    必须由用户使用MrReceive1_FreeBuf函数释放该内存。
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */
int   _stdcall MrReceive1(void* pHandle, char** ppsPkg, int* piOutPkgLen, STUMsgProperty* pMsgPropery, int iMillSecTimeo);
void  _stdcall MrReceive1_FreeBuf(char* psPkg);


/* MrReceive2是接收消息，并将消息从队列中删除。也可以只调用MrReceive而不调用MrBrowse。
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */
int   _stdcall MrReceive2(void* pHandle, char* psPkg, int* piOutPkgLen, int iBufLenIn, STUMsgProperty* pMsgPropery, int iMillSecTimeo);


/*  *piErrSXCode==0表示正常交换包；否则值将为1至5，分别表示目标不存在、
 *    目标错误、队列已满、超时过期、系统错误，此时*ppsPkg中是错误字符串。
 */
int   _stdcall MrReceive3(void* pHandle, char** ppsPkg, int* piOutPkgLen, int* piErrSXCode, STUMsgProperty* pMsgPropery, int iMillSecTimeo);



/*  *piErrSXCode==0表示正常交换包；否则值将为1至5，分别表示目标不存在、
 *    目标错误、队列已满、超时过期、系统错误，此时*ppsPkg中是错误字符串。
 *   与MrReceive3的区别在于可以根据相关包id（pMsgPropery中填写）收取对应包的错误信息。
 */
int _stdcall  MrReceive4(void* pHandle, char** ppsPkg, int* piOutPkgLen, int* piErrSXCode, STUMsgProperty* pMsgPropery, int iMillSecTimeo);


/* 判断与交换中枢的连接是否正常
 * return: 0-link_not_ok; 1-link_ok
 */
int   _stdcall MrIsLinkOK(void* pHandle);


/*取得本API的版本号*/
void   _stdcall MrGetVersion(char* psBufVersion, int iBufLen);


/* 设置mrapi日志配置文件名称，原因是有些用户是多进程调用此API，导致多个进程将写同一个日志文件.
* 该文件本来应该在MrInit中，但为了保持兼容性，只好增加在此
*/
/*int   _stdcall MrSetLogConf(void* pHandle, const char* psMrapiLogConf);*/


/*释放资源*/
void  _stdcall MrDestroy(void* pHandle);


/*wangcx20081020 加入按条件推送数据的接口*/
/*指定条件由mr推送数据*/
int _stdcall MrRegRecvCondition(void* pHandle, STUMsgProperty* pMsgPropery, int iType);/*iType 0 增加一个条件， 1删除一个条件 2 清空所有条件*/
/*wangcx20081020 加入按条件推送数据的接口*/


#ifdef JAVA_API

//wangcx20090815{加入jni
#include <jni.h>
//返回0 ，初始化失败， 返回1，成功
//{ modify by lingb 20110401 增加支持LINUX64位操作系统,在
// LINUX64中,sizeof(void *)=8，需要使用java的jlong 类型,
//  同时对于同一个进程实现内的同一个APP，只允许与MR建立同
//  一条连接

#if defined(_WIN32)  || defined(_LINUX64_JDK32)  || defined(_JDK32)
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_MrInit
(JNIEnv *, jclass,jstring,jstring,jstring,jshort,jstring,jshort);
#else
#if defined(_LINUX64_JDK64)  || defined(_AIX64_JDK64)  || defined(_JDK64)
JNIEXPORT jlong JNICALL Java_com_sscc_fdep_mrapi_MrInit
(JNIEnv *, jclass,jstring,jstring,jstring,jshort,jstring,jshort);
#endif
#endif
//} modify by lingb 20110401


JNIEXPORT jstring JNICALL Java_com_sscc_fdep_mrapi_MrSend
(JNIEnv *, jclass, jbyteArray, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jbyte,jbyte,jint);

JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_MrReceive1
(JNIEnv *, jclass, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring,jbyte,jbyte, jint);

JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_MrIsLinkOK
(JNIEnv *, jclass, jstring);

JNIEXPORT void JNICALL Java_com_sscc_fdep_mrapi_MrDestroy
(JNIEnv *, jclass, jstring);


//wangcx20090815加入jni}
////////////////////////////////////////////////////////////////
//平安信托版本
/*
#ifdef _LINUX64
JNIEXPORT jlong JNICALL Java_com_paic_ncm_common_biz_service_mrapi_MrInit
(JNIEnv *, jclass,jstring,jstring,jstring,jshort,jstring,jshort);
#endif
//} modify by lingb 20110401

JNIEXPORT jstring JNICALL Java_com_paic_ncm_common_biz_service_mrapi_MrSend
(JNIEnv *, jclass, jbyteArray, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jbyte, jbyte, jint);


JNIEXPORT jbyteArray JNICALL Java_com_paic_ncm_common_biz_service_mrapi_MrReceive1
(JNIEnv *, jclass, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jstring, jbyte, jbyte, jint);


JNIEXPORT jint JNICALL Java_com_paic_ncm_common_biz_service_mrapi_MrIsLinkOK
(JNIEnv *, jclass, jstring);


JNIEXPORT void JNICALL Java_com_paic_ncm_common_biz_service_mrapi_MrDestroy
(JNIEnv *, jclass, jstring);
*/
#endif

#ifdef __cplusplus
}
#endif 


#endif



