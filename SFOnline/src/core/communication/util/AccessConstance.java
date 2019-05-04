package core.communication.util;

/**
 * PBank Constance variable define
 * 
 * @author PBank
 * @since 1.0 2010-01-04
 * 
 */
public class AccessConstance {	
	//PBankEMPFlow中Flow的嵌套层数
	public static String EMPFLOW_NESTING_NO = "_emp_Flow_Nesting_No";
	public static String PBank_FINALHANDLERSERVICENAME = "PBankFinalHandler";
	// PBank 模块名称设定
	public static String PBank_INITIALIZER = "PBank_INITIALIZER";
	//PBank与ESB对接时，为保存报文信息，将报文内容存入Context中。
	public static String PBank_ESB_INOUTDTONAME = "_PBank_ESB_INOUTDTO";
	
	/*
	 * 报文格式
	 */
	public static final String TCCSIA = "_TCCSIA";

	public static final String TCCSOA = "_TCCSOA";

	/* 合作行报文 */
	public final static String A_REQUEST_HEAD = "A_REQUEST_HEAD";
	public final static String AA_REQUEST_HEAD = "AA_REQUEST_HEAD";
	public final static String A_RESPONSE_HEAD = "A_RESPONSE_HEAD";
	public final static String AA_RESPONSE_HEAD = "AA_RESPONSE_HEAD";
	
	

	/* 直联报文 */
	public final static String B_REQUEST_HEAD = "B_REQUEST_HEAD";

	public final static String B_RESPONSE_HEAD = "B_RESPONSE_HEAD";

	public final static String ZLSECU_REQUEST_HEAD = "ZLSECU_REQUEST_HEAD";

	public final static String ZLSECU_RESPONSE_HEAD = "ZLSECU_RESPONSE_HEAD";

	/* 深证通报文 */
	public final static String C_REQUEST_HEAD = "C_REQUEST_HEAD";

	public final static String C_RESPONSE_HEAD = "C_RESPONSE_HEAD";

	// 存管系统内部处理冲正时，保存在Context中的主机访问结果列表
	public static final String SF_HOST_ACCESS_LIST = "hostAccessList";
	// 三方存管系统与ESB对接时，为保存报文信息，将报文内容存入Context中。
	public static final String SF_ESB_INOUTDTONAME = "SF_ESB_INOUTDTO";

	/* SZT超时参数 */
	public static final String SZT_TIMEOUT = "TIME_OUT";
}
