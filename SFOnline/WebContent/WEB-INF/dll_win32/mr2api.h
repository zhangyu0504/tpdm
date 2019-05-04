#ifndef __SSCC_FDEPV4_MR2API_H__
#define __SSCC_FDEPV4_MR2API_H__

#ifdef __cplusplus
extern "C" {
#endif 

#ifdef WIN32
#else   /*UNIX or Linux*/
#define _stdcall
#endif

#define MR2_MSGFLAG_PERSIST          0x01   /*持久消息标志*/
#define MR2_MSGFLAG_COMPRESS         0x02   /*压缩标志*/
#define MR2_MSGFLAG_REPLYTOME		 0X04   /*应答包自动推送到发送方标志*/


#define MR2_MAXLEN_ADDR              32     /*源地址或目的地址的最大长度 @2012.1.12*/
#define MR2_MAXLEN_PKGID             64     /*包ID的最大长度(目前版本中32位OS上长度为34,64位OS上长度为39或44)*/
#define MR2_MAXLEN_USERDATA          256    /*用户数据的最大长度*/

#define MR2_MAXLEN_MSGTYPE			 8	/*为了兼容旧的，长度与旧版一样*/

#define MR2_MAXLEN_IP                16


/* MrSend, Mr2Browse, MrReceive1或MrReceive2函数返回的错误码.
 * 使用时一般只需要判断是否为0, 如果为0表示成功,否则表示失败.
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

/* 单发消息属性结构体 
 * 目的用户只有一个。
 */
typedef struct _tagSTUMsgProperty2
{
    char                m_szSourceUserID[MR2_MAXLEN_ADDR];     /**< 源用户标识，必须是'\0'结尾的字符串。*/
    char                m_szSourceAppID[MR2_MAXLEN_ADDR];     /*源应用标识，必须是'\0'结尾的字符串。*/
    char                m_szDestUserID[MR2_MAXLEN_ADDR];       /*目的用户标识，必须是'\0'结尾的字符串。*/
    char                m_szDestAppID[MR2_MAXLEN_ADDR];       /*目的应用标识，必须是'\0'结尾的字符串。*/
    char                m_szPkgID[MR2_MAXLEN_PKGID];         /*包ID, 必须是'\0'结尾的字符串. 或者由用户使用MrCreatePkgID函数生成,或者填空*/
    char                m_szCorrPkgID[MR2_MAXLEN_PKGID];     /*相关包ID, 必须是'\0'结尾的字符串, 供用户使用*/
    char                m_szUserData1[MR2_MAXLEN_USERDATA];  /*用户数据1, 必须是'\0'结尾的字符串, 供用户使用*/
    char                m_szUserData2[MR2_MAXLEN_USERDATA];  /*用户数据2, 必须是'\0'结尾的字符串, 供用户使用*/
    
    unsigned char       m_ucFlag;      /*标志:可以是MR2_MSGFLAG_PERSIST或MR2_MSGFLAG_COMPRESS等标志的位或.*/
	unsigned char       m_ucBizType;      /*业务类型，当前取值有(将来会扩充):0-三方存管,10-银期转账,11-银基转账,12-资金划拨,13-信证报盘,14-电子对账,15-融资融券,16-基金盘后,17-转融通,18-B转H. */
	unsigned char       m_ucPriority; /* 优先级， 5为最低，3为最高*/
	unsigned char       m_ucSensitiveLevel;    /* 敏感性级别，0为最低，255为最高*/
	char				m_szMsgType[MR2_MAXLEN_MSGTYPE]; /*消息类型，目前只能取值: M(消息)或F(文件). 如果为F(文件),则在交换日志中永远不打印包内容.*/
} STUMsgProperty2;

/* 连接参数结构体 */
typedef struct _tagSTUConnInfo2
{
    char                m_szMRIP[MR2_MAXLEN_IP];       /*主用MR的IP. 必须是'\0'结尾的字符串. "127.0.0.1"*/
    unsigned short      m_usMRPort;         /*主用MR的端口.    51231*/
} STUConnInfo2;


typedef int (*OnReceiveCallBack2)(const char* psPkg, int iPkgLen, const STUMsgProperty2* pMsgPropery, void* pvUserData);


/* 功能：连接接入客户端的初始化: psMyID是用户应用的名称,例如app1,app2等.
 * 注意：接收消息包时，或者只能使用函数MrInit中的回调函数OnReceive进行接收；或者只能使用MrReceive1/MrReceive1_FreeBuf、Mr2Browse/MrReceive2或MrReceive3/MrReceive1_FreeBuf进行接收，两者不能同时使用。
 *     该函数返回的Handle只能在同一个进程内使用,在同一个进程的多个线程之间也可以使用,以下全部函数也是线程安全的.
 *     但是在不同的进程之间不能使用同一个Handle，例如在Unix下fork后,在主进程中调用MrInit生成的Handle在子进程中不再有效.
 * return: 非NULL-OK; NULL-failed
 */
void*  _stdcall Mr2Init(const char* psAppID, const char* psAppPasswd, 
	OnReceiveCallBack2 onReceive,const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, 
	void* pvUserData);


void   _stdcall Mr2Init2(void** ppHandle, const char* psAppID, const char* psAppPasswd,
	STUMsgProperty2* pOnRecvMsgPropery,OnReceiveCallBack2 onReceive,
	const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount,
	void* pvUserData, int iThreadCount);

/* 连接中枢的初始化: psUserCertID是用户的证书的完整主题名称或者用户名(例如我测试时填写为"/C=CN/CN=twtest1")
 * psAppID是应用的名称，不知道时填app1。
 * return: 非NULL-OK; NULL-failed
*/
void*  _stdcall Mr2Init1(const char* psUserCertID, const char* psAppID, const char* psUserPasswd, 
                        OnReceiveCallBack2 OnReceive, const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, void* pvUserData);

void  _stdcall Mr2Init1Ex1(void** ppHandle, const char* psUserCertID, const char* psAppID, const char* psUserPasswd, STUMsgProperty2* pOnRecvMsgPropery,
						OnReceiveCallBack2 OnReceive, const STUConnInfo2* pArrConnInfo, int iArrConnInfoCount, void* pvUserData, int iThreadCount);

/* 生成包唯一ID
 * return: 0-OK; others-failed
 */
int   _stdcall Mr2CreatePkgID(void* pHandle, char szPkgID[MR2_MAXLEN_PKGID]);


/* 发送消息，禁止同一个用户互发持久消息
 * return: 0-send_OK; others-failed
 */
int   _stdcall Mr2Send(void* pHandle, const char* psPkg, int iPkgLen, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);




/* 指定条件由mr推送数据, 一次推所有的条件，并清空以前的条件.
 */
int _stdcall Mr2RegRecvCondition(void* pHandle, STUMsgProperty2* pArrMsgPropery, int iArrayCount);  


/* MrReceive1与MrReceive2的区别仅仅在于MrReceive1将替用户分配内存，并使
 *    用MrReceive1_FreeBuf释放；而MrReceive2需要用户自己分配内存。
 * 注意MrReceive1函数中的ppsPkg是一个返回参数，指向接收包的缓冲区，
 *    必须由用户使用MrReceive1_FreeBuf函数释放该内存。
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */

int   _stdcall Mr2Receive1(void* pHandle, char** ppsPkg, int* piOutPkgLen, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);
void  _stdcall Mr2Receive1_FreeBuf(char* psPkg);


/* MrReceive2是接收消息，并将消息从队列中删除。也可以只调用MrReceive而不调用Mr2Browse。
 * return: 0-has_recv_one_pkg_OK; others-recv_none_or_failed
 */
int   _stdcall Mr2Receive2(void* pHandle, char* psPkg, int* piOutPkgLen, int iBufLenIn, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);



/*  *piErrSXCode==0表示正常交换包；否则值将为1至5，分别表示目标不存在、
 *    目标错误、队列已满、超时过期、系统错误，此时*ppsPkg中是错误字符串。
 *        可以根据相关包id（pMsgPropery中填写）收取对应包的错误信息。
 *        如果关包id（pMsgPropery中填写）为空，则可以收全部错误或正常的交换包。
 */
int _stdcall  Mr2Receive3(void* pHandle, char** ppsPkg, int* piOutPkgLen, int* piErrSXCode, STUMsgProperty2* pMsgPropery, int iMillSecTimeo);


/* 判断API与MR或API与交换中枢的连接是否正常.
 * return: 0-link_not_ok; 1-link_ok
 */
int   _stdcall Mr2IsLinkOK(void* pHandle);


/*取得本API的版本号*/
void   _stdcall Mr2GetVersion(char* psBufVersion, int iBufLen);


/*释放资源*/
void  _stdcall Mr2Destroy(void* pHandle);




typedef struct _tagSTUUserAddr
{
	char m_szUserID[MR2_MAXLEN_ADDR];	/*用户标识，必须是'\0'结尾的字符串。*/
	char m_szAppID[MR2_MAXLEN_ADDR];	/*应用标识，必须是'\0'结尾的字符串。*/
} STUUserAddr;

/* 群发消息属性结构体。
 * 与单发消息属性不同之处：目的用户可以是多个；
 * m_pArrDestUserAddr-表示目的用户数组指针，m_iDestUserCount-表示目的用户个数。
 */
typedef struct _tagSTUMultiDestMsgProperty
{
	char                m_szSourceUserID[MR2_MAXLEN_ADDR];     /*源用户标识，必须是'\0'结尾的字符串。*/
	char                m_szSourceAppID[MR2_MAXLEN_ADDR];     /*源应用标识，必须是'\0'结尾的字符串。*/
	int					m_iDestUserCount;					/*目的用户个数*/
	STUUserAddr*		m_pArrDestUserAddr;					/*目的用户结构数组指针*/
	char                m_szPkgID[MR2_MAXLEN_PKGID];         /*包ID, 必须是'\0'结尾的字符串. 或者由用户使用MrCreatePkgID函数生成,或者填空*/
	char                m_szCorrPkgID[MR2_MAXLEN_PKGID];     /*相关包ID, 必须是'\0'结尾的字符串, 供用户使用*/
	char                m_szUserData1[MR2_MAXLEN_USERDATA];  /*用户数据1, 必须是'\0'结尾的字符串, 供用户使用*/
	char                m_szUserData2[MR2_MAXLEN_USERDATA];  /*用户数据2, 必须是'\0'结尾的字符串, 供用户使用*/

	unsigned char       m_ucFlag;      /*标志:可以是MR2_MSGFLAG_PERSIST或MR2_MSGFLAG_COMPRESS等标志的位或.*/
	unsigned char       m_ucBizType;      /*业务类型，取值为上述各值，如11为银基转帐*/
	unsigned char       m_ucPriority; /* 优先级， 5为最低，3为最高*/
	unsigned char       m_ucSensitiveLevel;    /* 敏感性级别，0为最低，255为最高*/
	char				m_szMsgType[MR2_MAXLEN_MSGTYPE]; /*消息类型，目前只能取值: M(消息)或F(文件). 如果为F(文件),则在交换日志中永远不打印包内容.*/
} STUMultiDestMsgProperty;


/* 群发消息接口，pMsgPropery-群发消息属性(包含多个目标用户标识)，不支持发送可靠消息. 群发消息将在中枢端分拆成多个独立的消息分别处理.
 * return: 0-send_OK; others-failed
 */
int   _stdcall Mr2MultiDestSend(void* pHandle, const char* psPkg, int iPkgLen, STUMultiDestMsgProperty* pMsgPropery, int iMillSecTimeo);

/* 获取通信对端用户状态接口，pHandle-初始化返回的句柄，psPeerUserID-对端用户标识，
 * piOutStat-（返回参数）对端用户状态：1-在线，0-不在线。存在刷新时间误差。
 * return: 0-成功; others-失败，详细参考错误码。
 */
int   _stdcall Mr2GetPeerUserStat(void* pHandle, const char* psPeerUserID, int* piOutStat);




#ifdef JAVA_API


//wangcx20090815{加入jni
#include <jni.h>
//返回0 ，初始化失败， 返回1，成功
//{ modify by lingb 20110401 增加支持LINUX64位操作系统,在
// LINUX64中,sizeof(void *)=8，需要使用java的jlong 类型,
//  同时对于同一个进程实现内的同一个APP，只允许与MR建立同
//  一条连接


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


/// 函数简述:获取该API的版本号的接口.
/// 
/// 函数详述:获取该API的版本号.
/// @param  env:JNI上下文句柄   
/// @param  obj:JNI对象
/// @return  jbyteArray:版本信息
/// 
/// @note      
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2GetVersion
(JNIEnv *env, jclass obj);


/// 函数简述:Java版生成消息包ID
/// 
/// 函数详述:生成一个全局唯一的UUID，作为消息包标识
/// @param  env:JNI上下文句柄   
/// @param  obj:JNI对象
/// @param  szHandleAppID:JNI对象
/// @return    
/// @retval  jbyteArray: 创建的PKGID
/// 
/// @note      
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2CreatePkgID
(JNIEnv *env, jclass obj, jstring szHandleAppID);


/// 函数简述: Java版本获取通信对端用户状态
/// 
/// 函数详述:
/// @param  env:JNI上下文句柄   
/// @param  obj:JNI对象
/// @param  szHandleAppID:Init初始化关联APPID
/// @param  szPeerUserID:对端UserID
/// @return    
/// @retval  -1:函数调用失败
/// @retval  0:不在线
/// @retval  1:在线
/// 
/// @note      在初始化之后调用，存在刷新时间误差。
///
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2GetPeerUserStat
(JNIEnv *env, jclass obj, jstring szHandleAppID,jstring szPeerUserID);


/// 函数简述:JAVA版本一次推送所有接收条件
/// 
/// @param  env:JNI上下文句柄   
/// @param  obj:JNI对象
/// @param  szHandleAppID:Init初始化关联APPID
/// @param  szSrcUserId:源USERID
/// @param  szSrcAppId:源APPID
/// @param  szDestUserId:目的USERID
/// @param  szDestAppId:目的APPID
/// @param  szPkgId:包ID
/// @param  szCorrPkgId:源包ID
/// @param  szUserData1:附加字段1
/// @param  szUserData2:附加字段2
/// @return    
/// @retval    0: 成功.
/// @retval   <0: 失败.
/// 
/// @note      
///
JNIEXPORT jint JNICALL Java_com_sscc_fdep_mrapi_Mr2RegRecvCondition
(JNIEnv * env, jclass obj, jstring szHandleAppId, jstring szSrcUserId, jstring szSrcAppId, jstring szDestUserId, jstring szDestAppId, 
 jstring szPkgId, jstring szCorrPkgId, jstring szUserData1, jstring szUserData2);

/// 函数简述:JAVA版本对应Receive3函数
/// 
/// @param  env:JNI上下文句柄   
/// @param  obj:JNI对象
/// @param  szHandleAppID:Init初始化关联APPID
/// @param  szSrcUserId:源USERID
/// @param  szSrcAppId:源APPID
/// @param  szDestUserId:目的USERID
/// @param  szDestAppId:目的APPID
/// @param  szPkgId:包ID
/// @param  szCorrPkgId:源包ID
/// @param  szUserData1:附加字段1
/// @param  szUserData2:附加字段2
/// @param  iArrayCount:数组个数
/// @return    
/// @retval    0: 成功.
/// @retval   <0: 失败.
/// 
/// @note return "NULL，errmsg"-接收失败   否则返回errcode(4byte)+pkID(64byte) + CorrpkID(64byte) + sourceUserID(32byte) + sourceAppID(32byte) +destUserID(32byte) + destAppID(32byte)+UserData1(256byte) + UserData2(256byte) + data     上述前7个字段同C接口中pMsgPropery中各个字段 ，data为真正的数据 
///       其中errcode= "0000"为接收正常，如果errcode为非"0000"字符串，则接收错误；  
///
JNIEXPORT jbyteArray JNICALL Java_com_sscc_fdep_mrapi_Mr2Receive3
(JNIEnv * env, jclass obj, jstring szHandleAppId, jstring szSrcUserId, jstring szSrcAppId, jstring szDestUserId, jstring szDestAppId, 
 jstring szPkgId, jstring szCorrPkgid, jstring szUserData1, jstring szUserData2, jint MillSecTimeo);

#endif


#ifdef __cplusplus
}
#endif 


#endif



