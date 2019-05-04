package common.util;

/**
 * 存放系统变量总类,所有的静态变量都定义于此
 */
public class SFConst {
	/**
	 * 存放系统变量
	 */
	public static final String SYS_BANK_CNAME = "平安银行";// 我行名称
	public static final String SYS_FACTORY_NAME = "factoryName";
	public static final String SYS_GBK = "GBK";// GBK
	public static final String SYS_UTF8 = "UTF-8";// UTF-8
	public static final String SYS_CONTENT_TYPE_GKB = "text/html;charset=GBK";// GBK
	public static final String SYS_SYSID = "429696";// ESB系统ID
	public static final String SYS_SYSNAME = "SFOnline";// ESB系统英文名称TPDM-CORE
	public static final String GLOBEL_ROOT_PATH = "GlobelRootPath";//全局路径
	 //public static final String EB_ENCRYPT_KEY="0123456789ABCDEF";//测试网银渠道加密密钥
    public static final String EB_ENCRYPT_KEY="3B2B8C6AA6A5D1BE";//生产网银渠道加密密钥

	public static final String SYS_CACHE_TRAD="CACHE_TRAD";//系统交易缓存
	public static final String FLOW_CACHE_TRAD="FLOW_CACHE_TRAD";//交易流量缓存
	/**
	 * 上下文常量
	 */
	public static final String CTX_ERRCODE = "errcode";
	public static final String CTX_ERRMSG = "errmsg";
	public static final String CTX_ERRCODE_UNKNOWN = "ST8888";
	public static final String CTX_ERRMSG_UNKNOWN = "解析报文失败";
	/**
	 * 上下文公共服务
	 */
	public static final String SERVICE_FACTORY = "SERVICE.factory";//
	public static final String SERVICE_DATASOURCE = "SERVICE.dataSource";//
	public static final String SERVICE_SQL = "SERVICE.sqlExecService";// SQL服务
	public static final String SERVICE_GENCONTEXTBYSQL = "SERVICE.genContextBySql";// SQL动态生成上下文
	public static final String SERVICE_EXPRESSCALC = "SERVICE.expressCalculateService";// BEANSHELL 表达式服务
	public static final String SERVICE_PUBLIC = "SERVICE.public";// 公共服务对象
	public static final String SERVICE_CACHEMANAGER = "SERVICE.cacheManager";// 缓存对象
	public static final String SERVICE_PROCEDURE = "SERVICE.procedureAccessService";// 存储过程
	// public static final String SERVICE_SOCKET="SERVICE.socket";//socket服务
	public static final String SERVICE_FTP = "SERVICE.ftpFileService";
	public static final String SQLCA_SQLNROWS = "_SQLCA_SQLNROWS";//SQL相关
	public static final String SERVICE_PUBLICSERVICENAME = "SERVICE.publicservice";// 公共服务对象

	/**
	 * 三方存管公共头信息:公共KEYCOLL名称
	 */
	public static final String PUBLIC = "PUBLIC";
	/*
	 * 通信标识： 定义在接口中作为常量值
	 */
	public static final String PUBLIC_RET_FLAG = "PUBLIC.retflag";
	public static final String RET_SUCCESS = "0"; // 成功
	public static final String RET_FAILURE = "1"; // 失败
	public static final String RET_OVERTIME = "2";// 超时
	
	/*
	 * 是否发送响应报文
	 */
	public static final String PUBLIC_IS_RET_RESP = "PUBLIC.isretresp";
	public static final String RET_RESP_YES = "0";// 需要返回报文
	public static final String RET_RESP_NO = "1";// 不需要返回报文

	/*
	 * 券商类型
	 */
	public static final String PUBLIC_SECU_TYPE = "PUBLIC.secutype";
	public static final String SECU_SZT = "1";
	public static final String SECU_ZL = "0";
	
	public static final String PUBLIC_SECU = "PUBLIC.secu";// 券商对象
	public static final String PUBLIC_SIGN_ACCOUNT_DATA = "PUBLIC.signaccountdata";// 签约对象
	public static final String PUBLIC_INVEST_DATA = "PUBLIC.investdata";// 客户对象
	public static final String PUBLIC_LOCAL_INFO = "PUBLIC.localinfo";// 系统工作日期表
	public static final String PUBLIC_PRODUCT_INFO = "PUBLIC.productinfo";// 产品信息
	public static final String PUBLIC_SEC_SERV_STATUS = "PUBLIC.trdsecservstatus";// 券商签到表
	public static final String PUBLIC_AGENTINFO = "PUBLIC.agentinfo";// 合作行对象
	public static final String PUBLIC_LOG_ID = "PUBLIC.logid";// 流水号
	public static final String PUBLIC_TRAN_CONNECTION = "PUBLIC.tranconnection";//公共数据联接
	public static final String PUBLIC_TX_CODE = "PUBLIC.txcode";// 交易号
	public static final String PUBLIC_KEY_INPUTNAME = "PUBLIC.keyInputName";// keyInputName
	
	public static final String PUBLIC_MSG_SEQ_NO = "PUBLIC.msgSeqNo";// 请求报文流水号
	public static final String PUBLIC_SERVER_ID = "PUBLIC.serverId";// 请求接口号
	
	public static final String PUBLIC_IS_TRANSPOND = "PUBLIC.isTranSpond";// 是否是转发接口

	
	
	public static final String DEFAULT_TRXCODE = "000000";

	/*******************
	 *** 交易常量配置***
	 *******************/
	/* 工作模式 */
	public static final String PUBLIC_WORKMODE = "PUBLIC.workmode";
	public static final String WORKMODE_NORMAL = "0"; // 0：正常交易时间段
	public static final String WORKMODE_724CLEAR = "1"; // 1：清算时间段
	public static final String WORKMODE_724HOLIDAY = "2"; // 2：节假日时间段
	public static final String WORKMODE_724OTHER = "3"; // 3：其他724时间段

	// 客户类型
	public static final String PUBLIC_INV_TYPE = "PUBLIC.invtype";
	public static final String INV_TYPE_RETAIL = "1"; // 零售
	public static final String INV_TYPE_CORP = "2"; // 对公
	public static final String INV_TYPE_RETAIL_SZT = "INVE"; // 零售
	public static final String INV_TYPE_CORP_SZT = "INVI"; // 对公

	/* 境内外标识常量 */
	public static final String INTER_FLAG_DOMESTIC = "0";// 境内
	public static final String INTER_FLAG_ABROAD = "1";// 境外

	/* 钞汇标记常量 */
	public static final String PUBLIC_CASH_REMIT_FLAG = "PUBLIC.cashremitflag";
	public static final String REMIT_FLAG = "1";// 汇
	public static final String CASH_FLAG = "2";// 钞
	
	/* 第三方存管模式 */
	public static final String TPDM_FLAG_NORMAL = "1";// 普通第三方存管
	public static final String TPDM_FLAG_MARGIN = "2";// 融资融券第三方存管

	/* 币种常量 */
	public static final String PUBLIC_CUR_CODE = "PUBLIC.curcode";
	public static final String CUR_CODE_RMB = "RMB";// 人民币
	public static final String CUR_CODE_HKD = "HKD";// 港币
	public static final String CUR_CODE_USD = "USD";// 美元
	
	/* 签约标志 */
	public static final String PUBLIC_SIGN_FLAG = "PUBLIC.signflag";
	public static final String SIGN_FLAG_SIGN = "0"; // 签约
	public static final String SIGN_FLAG_SIGN_IN_PROCESS = "1"; // 签约处理中
	public static final String SIGN_FLAG_BANK_PRE = "2"; // 银行预约、待券商激活
	public static final String SIGN_FLAG_SECU_PRE = "3"; // 券商预指定、待银行激活
	public static final String SIGN_FLAG_CANCEL = "4"; // 已解约
	public static final String SIGN_FLAG_CANCEL_IN_PROCESS = "5"; // 撤消处理中
	public static final String SIGN_FLAG_CONFIRM_IN_PROCESS = "6"; // 签约确认处理中
	public static final String SIGN_FLAG_BANK_PRE_IN_PROCESS = "7"; // 7-银行预约处理中 
	public static final String SIGN_FLAG_CANCEL_PRE = "8"; // 8-预约已撤销
	public static final String SIGN_FLAG_CANCEL_PRE_IN_PROCESS = "9"; // 9-预约撤销处理中

	/* 渠道常量 */
	public static final String PUBLIC_INIT_SIDE = "PUBLIC.initside";
	public static final String INIT_SIDE_ABBANK = "B"; // B:柜面
	public static final String INIT_SIDE_NETBANK = "E"; // E:网银
	public static final String INIT_SIDE_TELBANK = "T"; // T:电话银行
	public static final String INIT_SIDE_MOBILEBANK = "M"; // M:手机银行
	public static final String INIT_SIDE_SECU = "S"; // S:券商
	public static final String INIT_SIDE_TERMINAL = "C"; // C:自助终端
	public static final String INIT_SIDE_COBANK = "A"; // A:合作行
	public static final String INIT_SIDE_PERSONBANK = "F"; // F:平安个人网银

	/* 存管证件类型常量 */
	public static final String PUBLIC_ID_TYPE = "PUBLIC.idtype";
	public static final String ID_TYPE_OTHERS = "0"; // 0-其它个人证件，900-其他企业证件
	public static final String ID_TYPE_PERSON_SFZ = "1"; // 1-身份证
	public static final String ID_TYPE_PERSON_JRJGZ = "2"; // 2-军人军官证
	public static final String ID_TYPE_PERSON_GATJMTXZ = "3"; // 3-港澳台居民通行证
	public static final String ID_TYPE_PERSON_ZGHZ = "4"; // 4-中国护照
	public static final String ID_TYPE_PERSON_WJJGZ = "8"; // 8-武警警官证
	public static final String ID_TYPE_PERSON_LSSFZ = "9"; // 9-临时身份证
	public static final String ID_TYPE_PERSON_HKP = "11"; // 11-户口薄
	public static final String ID_TYPE_PERSON_ZGJMQTZJ = "12"; // 12-中国居民其它证件
	public static final String ID_TYPE_PERSON_JRSBZ = "13"; // 13-军人士兵证
	public static final String ID_TYPE_PERSON_JRWZGBZ = "14"; // 14-军人文职干部证
	public static final String ID_TYPE_PERSON_JRQTZJ = "15"; // 15-军人其它证件
	public static final String ID_TYPE_PERSON_WJSBZ = "16"; // 16-武警士兵证
	public static final String ID_TYPE_PERSON_WJWZGBZ = "17"; // 17-武警文职干部证
	public static final String ID_TYPE_PERSON_WJQTZJ = "18"; // 18-武警其它证件
	public static final String ID_TYPE_PERSON_WGGMQTZJ = "20"; // 20-外国公民其它证件
	public static final String ID_TYPE_COMPANY_TYSHXYDM = "51"; // 989-统一社会信用代码
	public static final String ID_TYPE_COMPANY_YYZZ = "51"; // 996-营业执照
	public static final String ID_TYPE_COMPANY_ZZJGDMZ = "52"; // 995-组织机构代码证
	public static final String ID_TYPE_COMPANY_JRJGXKZ = "80"; // 902-金融机构许可证

	/* 特殊券商代码 */
	public static final String SECU_GUANGDAZQ = "10200000"; // 光大证券
	public static final String SECU_GUANGDAXY = "10209999"; // 光大信用
	public static final String SECU_GUANGFAZQ = "10230000"; // 广发证券
	public static final String SECU_GUANGFAXY = "10239999"; // 广发信用
	public static final String SECU_GUOTAIJAZQ = "10270000"; // 国泰君安证券
	public static final String SECU_GUOTAIJAXY = "10279999"; // 国泰君安信用
	public static final String SECU_ZHAOSHANGZQ = "10280000"; // 招商证券
	public static final String SECU_ZHAOSHANGXY = "10289999"; // 招商信用
	public static final String SECU_LIANHEZQ = "10540000";// 联合证券，券商信息表无记录
	public static final String SECU_PINGANZQ = "10620000";// 平安证券
	public static final String SECU_PINGANXY = "10629999";// 平安信用
	public static final String SECU_SHENYINWGZQ = "10720000"; // 申银万国证券
	public static final String SECU_WUKUANGZQ = "10730000"; // 五矿证券
	public static final String SECU_ZHONGXINZQ = "11020000"; // 中信证券
	public static final String SECU_ZHONGXINJTZQ = "12710000"; // 中信金通
	public static final String SECU_MINZUZQ = "13130000"; // 民族证券
	public static final String SECU_YINHEZQ = "13690000"; // 银河证券
	public static final String SECU_GUOXINZQ = "10290000"; // 国信证券
	public static final String SECU_GUOXINXY = "10299999"; // 国信信用
	public static final String SECU_ZHONGJINZQ = "10990000"; // 中金证券

	public static final String PUBLIC_SIGN_TYPE = "PUBLIC.signtype"; // 签约类型
	public static final String SIGN_TYPE_PRE = "1"; // 预指定
	public static final String SIGN_TYPE_ONE = "2"; // 一步式
	public static final String SIGN_TYPE_ACTIVE = "3"; // 激活银行预约

	/* 转账摘要码 */
	public static final String STATEMENT_NO_B2S = "TPDM01"; // 银转证摘要码
	public static final String STATEMENT_NO_S2B = "TPDM02"; // 证转银摘要码
	public static final String STATEMENT_NO = "TP0755"; // 摘要码

	/* 借贷标志 */
	public static final String DEBIT_FLAG = "D"; // 借
	public static final String CREDIT_FLAG = "C"; // 贷

	/*
	 * 预指定、一步式签约、激活银行预约 交易码常量设置
	 */
	public static final String PRE_SIGN_SZT = "11002"; // 预指定深证通交易码
	public static final String PRE_SIGN_ZL = "6021"; // 预指定直联交易码
	public static final String ONE_SIGN_SZT = "11001"; // 一步式深证通交易码
	public static final String ONE_SIGN_ZL = "6025"; // 一步式直联交易码
	public static final String ACTIVE_SIGN_SZT_PA = "11013"; // 激活银行预约深证通交易码(平安)
	public static final String ACTIVE_SIGN_SZT_HT = "31002"; // 激活银行预约深证通交易码(华泰联合)
	public static final String ACTIVE_SIGN_ZL = "6028"; // 激活银行预约直联交易码
	public static final String ACTIVE_SIGN_SZT_OTHER = "11003"; // 激活银行预约深证通交易码


	/* 三方存管交易代码 */
	public static final String SF_TX_CODE_SECU_PRE_SIGN = "6021"; // 券商预指定
	public static final String SF_TX_CODE_SIGN = "6022"; // 券商激活银行预指定、银行签约
	public static final String SF_TX_CODE_SECU_DEL_SIGN = "6023"; // 券商撤销签约
	public static final String SF_TX_CODE_CHANGE_ACC = "6024"; // 变更银行结算账号
	public static final String SF_TX_CODE_SECU_ONE_SIGN = "6025"; // 券商一步式签约
	public static final String SF_TX_CODE_SECU_UPD_CUST_INFO = "6026"; // 变更客户重要身份信息
	public static final String SF_TX_CODE_BANK_SIGN = "6029"; // 银行预约开户
	public static final String SF_TX_CODE_BANK_CANCEL = "6030"; // 银行预约撤销
	public static final String SF_TX_CODE_BANK_S2B = "6031"; // 银行证转银
	public static final String SF_TX_CODE_BANK_B2S = "6032"; // 银行银转证
	public static final String SF_TX_CODE_SECU_S2B = "6041"; // 券商证转银
	public static final String SF_TX_CODE_SECU_B2S = "6042"; // 券商银转证
	public static final String SF_TX_CODE_SECU_INTEREST = "6045"; // 单户结息
	public static final String SF_TX_CODE_QRY_SECU_BAL = "6052"; // 查询券商余额

	/* 业务类型 */
	public static final String BUSI_TYPE_B2S = "01"; // 银转证
	public static final String BUSI_TYPE_S2B = "02"; // 证转银
	public static final String BUSI_TYPE_REV = "03"; // 冲正(红字)
	public static final String BUSI_TYPE_INTEREST = "05"; // 单户结息
	public static final String BUSI_TYPE_SECU_PRE_SIGN = "21"; // 券商预指定
	public static final String BUSI_TYPE_SIGN = "22"; // 签约（券商一步式签约、银行签约、券商激活银行预指定）
	public static final String BUSI_TYPE_BANK_SIGN = "23"; // 银行预开户
	public static final String BUSI_TYPE_DEL_SIGN = "24"; // 撤销签约关系
	public static final String BUSI_TYPE_CHANGE_ACC = "25"; // 变更结算帐号
	public static final String BUSI_TYPE_SECU_UPD_CUST_INFO = "26"; // 券商变更客户身份信息
	public static final String BUSI_TYPE_BANK_CANCEL = "27"; // 银行预约撤销
	public static final String BUSI_TYPE_QRY_SECU_BAL = "52"; // 查询券商余额

	/**
	 * 非上下文定义常量
	 */
	/* 报文成功代码 */
	public final static String RESPCODE_SUCCCODE = "ST0000";
	
	public final static String RESPCODE_SUCCMSG = "交易成功";//esb 成功信息
	
	public final static String RESPCODE_SUCCCODE_ESB = "000000";//esb 成功码
	public final static String RESPCODE_SUCCCODE_COBANK = "SF0000";//合作行成功码
	public final static String RESPCODE_SUCCCODE_ZLSECU = "M0000";//直联券商成功码
	public final static String RESPCODE_SUCCCODE_SZTSECU = "0000";//深证通成功码
	
	/* 报文失败代码 */
	public final static String RESPCODE_FAILCODE_ESB = "999999";//esb 失败码
	public final static String RESPCODE_FAILCODE_COBANK = "SF9999";//合作行失败码
	public final static String RESPCODE_FAILCODE_ZLSECU = "E9998";//直联券商失败码
	public final static String RESPCODE_FAILCODE_SZTSECU = "9999";//深证通码
	/*报文超时代码*/
	public final static String RESPCODE_TIMEOUT_ESB = "330135";//esb 超时码
	public final static String RESPCODE_TIMEOUT_AE0666_ESB = "AE0666";//esb 超时码
	public final static String RESPCODE_TIMEOUT_COBANK = "YY6005";//合作行超时码
	public final static String RESPCODE_TIMEOUT_ZLSECU = "330135";//直联券商超时码
	public final static String RESPCODE_TIMEOUT_SZTSECU = "330135";//深证通超时码
	
	/* 客户编号 */
	public static final String DEFAULT_USER_ID = "0110000000000000"; // 银行渠道方发起交易使用
	public static final String DEFAULT_USER_ID_COBANK = "0110800000000000"; // 合作行发起交易（除建立/确认存管签约关系外）使用
	public static final String DEFAULT_USER_ID_COBANK_SIGN = "0110900000000000"; // 合作行发起交易（建立/确认存管签约关系）使用
	
	/* 柜员号 */
	public static final String DEFAULT_COUNTER_ID = "EB001"; // 默认柜员号
	
	/* 发起方交易流水号长度 */
	public static final int SEQ_ID_LEN_8 = 8;

	/*分页参数*/
	public static final String SF_PERPAGE_NUM_BANK="BANK";//银行端每页记录数设置
	public static final String SF_PERPAGE_NUM_COBANK="COBANK";//合作行端每页记录数设置
	
	/*合作行是否启用*/
	public static final String SF_COBANK_STATUS_YES="0";//合作行启用
	public static final String SF_COBANK_STATUS_NO="1";//合作行停用
	
	public static final String DFT_ICOLL_NAME = "DFT_ICOLL_NAME";//默认iColl名称
	
	/* 冲正交易返回码 */
	public static final String RESPCODE_ESB_BEEN_REVERSED = "AE0002";//主机冲正返回已冲正
	public static final String RESPCODE_ESB_REVERSESEQ_NO_EXIST = "AE0003"; //主机冲正返回流水不存在
	
	// ISC参数
	public static final String MAIN_TOPIC_AGREEMENT_SYNC = "40";			//签解约协议主题  MAIN_TOPIC ： 40
	public static final String SUB_TOPIC_AGREEMENT_SYNC = "40088";			//签解约协议子主题 SUB_TOPIC ： 40088
	public static final String SERVICE_CODE_AGREEMENT_SYNC = "10001";		//签解约协议场景码 SERVICE_CODE ：10001

	public static final String MAIN_TOPIC_SEND_ACOOUNT_NUM = "40";			//发送开户数消息主题 MAIN_TOPIC ： 40
	public static final String SUB_TOPIC_SEND_ACOOUNT_NUM = "40004";		//发送开户数消息子主题 SUB_TOPIC ： 40004
	public static final String SERVICE_SEND_ACOOUNT_NUM = "000004";			//发送开户数消息场景码 SERVICE_CODE ：000004
	
	/*
	 * 预警日志类型
	 */
	public static final String STANS_ALERT_LOG_OVERTIME_SEC = "10";         //券商端发起超时预警
	public static final String STANS_ALERT_LOG_OVERTIME_BANK = "20";        //银行端发起超时预警
	public static final String STANS_ALERT_LOG_OVERTIME_COBANK = "30";      //合作行端发起超时预警
	
	public static final String STANS_ALERT_LOG_OVERLOAD_SEC = "11";         //券商端发起限流量预警
	public static final String STANS_ALERT_LOG_OVERLOAD_BANK = "21";        //银行端发起限流量预警
	public static final String STANS_ALERT_LOG_OVERLOAD_COBANK = "31";      //合作行端发起限流量预警
	
	public static final String HOLIDAY_NO = "0";//否
	public static final String HOLIDAY_YES = "1";//是
	
}
