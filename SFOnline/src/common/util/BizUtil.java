package common.util;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCustomerInfo;
import module.bean.AutoBecif;
import module.bean.BankCodeInfo;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.Param;
import module.bean.SecCompData;
import module.bean.ServStatus;
import module.bean.SignAccountData;
import module.bean.Trans;
import module.cache.ParamCache;
import module.cache.TransCache;
import module.dao.AcctJourDao;
import module.dao.AgtAgentInfoDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AutoBecifDao;
import module.dao.BankCodeInfoDao;
import module.dao.ServStatusDao;
import module.dao.SignAccountDataDao;
import module.trans.sf2bankchl.QryBalClient;
import module.trans.sf2bankchl.QryCardAttrClient;
import module.trans.sf2bankchl.QryCardLevelClient;
import module.trans.sf2bankchl.QryCardTypeClient;
import module.trans.sf2bankchl.QryKeyInvestinfoClient;
import module.trans.sf2bankchl.SetCardStatusWordClient;
import module.trans.sf2cobank.T810026Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.sql.dao.DaoBase;

import core.log.SFLogger;

public class BizUtil {

	/**
	 * 获取交易配置日志级别
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getLogLevel( Context context ) throws SFException {
		String txcode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );
		return getLogLevel( txcode );
	}

	/**
	 * 获取交易配置日志级别
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getLogLevel( String txcode ) throws SFException {
		Trans trans = TransCache.getValue( txcode );
		return trans != null ? trans.getLogLevel() : null;
	}

	/**********************20171214流水改造start****************************************/

	/**
	 * 公共方法： 获取数据库序列号
	 * 获取左补0的定长字符串,目前主要用于生成流水号
	 * @param context
	 * @param length 长度
	 * @param sqeName 序列名称
	 * @return
	 * @throws SFException
	 */
	public static String genSeqId( Context context, int length, String sqeName ) throws SFException {
		DaoBase dao = new DaoBase();
		Connection tranConnection = null;
		String seqId = null;

		try {
			tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			// 查询序列表当前序列号sqeName,不够length位左补0
			String sql = "SELECT LPAD(" + sqeName + ".NEXTVAL ," + length + ",'0') TRCNO FROM DUAL";
			Map<String, Object> result = dao.qryMap( context, tranConnection, sql );

			if( result != null ) {
				seqId = ( String )result.get( "TRCNO" );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
		return seqId;
	}

	/**
	 * 公共方法： 生成定长ID（系统初始流水号14位）
	 * 生成14位的流水号，可用于监听中的logid，或者交易流水表中的acctdealid等字段
		格式：yymmdd+genSeqId（）
	 * @return
	 * @throws SFException
	 */
	public static String getInitSeqId( Context context ) throws SFException {
		return DateUtil.getDateShort() + genSeqId( context, 8, "TRCNO" );// 6位日期+8位seqId
	}

	/**
	 * 生成subtxseqid流水号16位
	 * 仅用于Trdacctjour/Trdacctdetail/trdbanksigndata的subtxseqid,
	 * 格式：getInitSeqId()+2位机器标识（01~06，默认00）
	 * @return
	 * @throws SFException
	 */
	public static String getSubTxSeqId( String initSeqId ) throws SFException {
		String macCode = SFUtil.getSysProperty( "APP_CODE" );
		if( SFUtil.isEmpty( macCode ) || macCode.length() != 2 ) {
			macCode = "00";
		}
		return initSeqId + macCode;// 14位系统流水号+2位机器标识（01~06，默认00）
	}

	/**
	 * 公共方法： 生成22位ESB流水号
	 * 以getSubTxSeqId作为输入，输出22位的数据，格式：429696+getSubTxSeqId
	 * 格式：429696+getSubTxSeqId()
	 * @param initSeqId
	 *            初始流水号
	 * @return esbNo
	 */
	public static String getChlSeqId( Context context, String subTxSeqId ) throws SFException {
		String chlSeqId = SFConst.SYS_SYSID + subTxSeqId; // 6位系统ID+16位流水
		SFUtil.chkCond( context, chlSeqId.length() != 22, "ST4098", String.format( "生成的ESB流水号[%s]长度有误", chlSeqId ) );
		return chlSeqId;
	}

	/**
	 * 公共方法： 初始化生成预约号
	 * 8位系统日期+7位序列号
	 * @param seqName
	 * @return
	 * @throws SFException
	 */
	public static String genBookId( Context context ) throws SFException {
		return DateUtil.getMacDate() + genSeqId( context, 7, "SEQ_BOOKNO" );// yyyyMMdd+7位seqId
	}

	/**
	 * 公共方法： 生成投资人管理帐号
	 * 8888+16位的序号+1位校验值
	 * @return
	 * @throws SFException
	 */
	public static String genSecAcctId( Context context ) throws SFException {
		String seqId = "8888" + genSeqId( context, 16, "SEQ_SECACCT" );// 8888+16位的序号
		String secAcctId = seqId + genVerifyChar( context, seqId );// 8888+16位的序号+1位校验值
		return secAcctId;
	}

	/**
	 * 生成校验位
	 * @param context
	 * @param verifiedStr
	 * @return
	 * @throws SFException
	 */
	public static int genVerifyChar( Context context, String verifiedStr ) throws SFException {
		int i = 0;
		int j = 0;
		int c = 0;
		try {
			if( SFUtil.isNotEmpty( verifiedStr ) ) {
				for( j = 2; c < verifiedStr.length() - 1; c++ ) {
					if( j * ( verifiedStr.charAt( c ) - '0' ) < 10 ) {
						i = i + j * ( verifiedStr.charAt( c ) - '0' );
					} else {
						i = i + j * ( verifiedStr.charAt( c ) - '0' ) - 10 + 1;
					}
					if( --j == 0 ) {
						j = 2;
					}
				}
				c = i % 10;
				if( c != 0 ) {
					c = 10 - c;
				}
			}
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, String.format( "生成校验位 genVerifyChar:[%s]", c % 10 ) );
		return c % 10;
	}

	/**********************20171214流水改造end****************************************/

	/**
	 * 公共方法： 生成发起方交易流水号 （8位券商流水号，左补0）
	 * 
	 * @param seqId
	 * @return txSeqId
	 * 
	 */
	public static String getTxSeqId( int len, String seqId ) {

		int idxSeqId = ( seqId.length() - len ) < 0 ? 0 : ( seqId.length() - len );

		String txSeqId = seqId.substring( idxSeqId );

		// 长度不足，左补0
		while( txSeqId.length() < len ) {
			txSeqId = "0" + txSeqId;
		}

		return txSeqId;

	}

	/**
	 * 公共方法： 生成发起方交易流水号 （8位券商流水号，左补0）
	 * 
	 * @param seqId
	 * @return txSeqId
	 * 
	 */
	public static String getTxSeqId( String seqId ) {

		String txSeqId = getTxSeqId( 8, seqId.trim() );
		return txSeqId;

	}

	/**
	 * 根据交易码判断银证交易类型
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String getTranType( Context context ) throws SFException {
		String tranType = null;
		String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 获取交易码
		if( "100200".equals( txCode ) || "200200".equals( txCode ) || "300200".equals( txCode ) ) {// 银转证交易
			tranType = SFConst.BUSI_TYPE_B2S;
		} else if( "100201".equals( txCode ) || "200201".equals( txCode ) || "300201".equals( txCode ) ) {// 证转银交易
			tranType = SFConst.BUSI_TYPE_S2B;
		}
		return tranType;
	}

	/**
	 * 公共方法： 生成对账单号
	 * 
	 * @return
	 * @throws SFException
	 */
	public static String getStatmentId( Context context ) throws SFException {
		String statmentId = null;
		statmentId = SFConst.STATEMENT_NO + DateUtil.getMacDate();
		return statmentId;
	}

	/**
	 * 检查钞汇标识是否合法 add by lch
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static String chkCashRemitFlag( Context context, String cashRemitFlag ) throws SFException {

		if( SFUtil.isEmpty( cashRemitFlag ) ) {// 如果钞汇标识为空,则默认为钞
			cashRemitFlag = SFConst.CASH_FLAG;
		} else if( !SFConst.REMIT_FLAG.equals( cashRemitFlag ) && !SFConst.CASH_FLAG.equals( cashRemitFlag ) ) {
			SFUtil.chkCond( context, "ST5717", "钞汇标识非法" );
		}
		return cashRemitFlag;
	}

	/**
	 * 获取当前工作模式 0：正常交易时间段 1：清算时间段 2：节假日时间段 3：其他724时间段
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void setWorkMode( Context context, Connection connection ) throws SFException {
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// 营业时间信息
			String txDate = DateUtil.getMacDate();// 系统交易日期
			String bankDate = localInfo.getBankDate();// 营业日期
			String lastBankDate = localInfo.getLastBankDate();// 前一营业日期
			String workDate = localInfo.getWorkdate();// 交易日期

			ServStatusDao servStatusDao = new ServStatusDao();
			// 有记录则为平台服务停止，没记录则为启动
			ServStatus servStatus = servStatusDao.qryServStatus( context, connection, bankDate );

			if( txDate.equals( bankDate ) && txDate.equals( workDate ) && servStatus == null ) {// 0-正常交易时间段
				SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_NORMAL );
			} else {// 724时间段
				if( !bankDate.equals( workDate ) ) {// 1-清算时间段
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724CLEAR );
				} else if( txDate.compareTo( lastBankDate ) > 0 && txDate.compareTo( bankDate ) < 0 ) {// 2-节假日时间段
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724HOLIDAY );
				} else {// 3-724其他时间段
					SFUtil.setDataValue( context, SFConst.PUBLIC_WORKMODE, SFConst.WORKMODE_724OTHER );
				}
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( e );
		}

	}

	/**
	 * 主机证件类型转换系统证件类型
	 * 
	 * @param idCode
	 *            证件类型（主机证件类型/系统证件类型）
	 * @param type
	 * @return
	 */
	private static String convHostAndSF4IdType( Context context, String idType, String type ) throws SFException {
		String newTypeId = "";
		if( SFUtil.isNotEmpty( idType ) ) {
			idType = idType.trim(); // 对证件类型进行去空格
		}
		String idName = getIdTypeName( context, type, idType );
		Param param = null;
		SFLogger.info( context, String.format( "主机证件类型转换系统证件类型idCode=[%s],type=[%s],证件类型[%s]", idType, type, idName ) );
		// 主机证件类型转系统证件类型
		if( SFUtil.isNotEmpty( idType ) && "HOSTTOSF".equals( type.trim() ) ) {
			param = ParamCache.getValue( "SF_HOST2SF_ID_TYPE", idType );
			if( param != null ) {
				newTypeId = param.getValue();
			}
		} else if( SFUtil.isNotEmpty( idType ) && "SFTOHOST".equals( type.trim() ) ) {// 系统证件类型转主机证件类型+
			param = ParamCache.getValue( "SF_SF2HOST_ID_TYPE", idType );
			if( param != null ) {
				if( "0".equals( idType ) ) {
					String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// 客户类型
					newTypeId = SFConst.INV_TYPE_CORP.equals( invType ) ? "900" : "0";// 0-其它个人证件,900-其他企业证件
				} else if( "51".equals( idType ) ) {
					String chlIdType = SFUtil.getReqDataValue( context, "ID_TYPE" );// 渠道过来的证件类型，没送则为null
					newTypeId = "989".equals( chlIdType ) ? chlIdType : "996";// 996-营业执照,989-统一社会信用代码
																				// 优先使用996
				} else {
					newTypeId = param.getValue();
				}
			}
		} else if( SFUtil.isNotEmpty( idType ) && "COBANKTOSF".equals( type.trim() ) ) {// 合作行证件类型转系统证件类型
			param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", idType );
			if( param != null ) {
				newTypeId = param.getValue();
			}
		} else if( SFUtil.isNotEmpty( idType ) && "SFTOCOBANK".equals( type.trim() ) ) {// 系统证件类型转合作行证件类型
			if( "3".equals( idType ) ) {// 如果证件类型为港澳台类型
				String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
				if( SFUtil.isEmpty( idCode ) ) {// 如果证件号码为空，则从Context取出客户对象
					InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
					if( investData != null ) {
						idCode = investData.getInvIdCode();
					}
					if( SFUtil.isEmpty( idCode ) ) {
						idCode = SFUtil.getDataValue( context, "INV_ID_CODE" );
					}
				}
				SFLogger.info( context, String.format( "系统证件类型转换合作行证件类型，证件号码为[%s]", idCode ) );

				if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// 判断idCode第一位为H或者M则是港澳台类型
					return "16";// 返回16(港澳台居民通行证深证通类型)
				} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// 判断idCode首位是否为数字
					return "17";
				}

			} else {
				param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			}
		} else {// 不是正确的请求类型直接返回空字符串
			return "";
		}
		SFLogger.info( context, String.format( "主机证件类型转换系统证件类型成功，方法返回：[%s],证件类型[%s]", newTypeId, idName ) );
		return newTypeId;
	}

	/**
	 * 主机转三方
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convHost2SF4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "HOSTTOSF" );
	}

	/**
	 * 三方转主机
	 * 
	 * @param context
	 * @param idCode
	 * @return
	 * @throws SFException
	 */
	public static String convSF2Host4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "SFTOHOST" );
	}

	/**
	 * 合作行转三方
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convCoBank2SF4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "COBANKTOSF" );
	}

	/**
	 * 三方转合作行
	 * 
	 * @param context
	 * @param idCode
	 * @param type
	 * @return
	 */
	public static String convSF2CoBank4IdType( Context context, String idType ) throws SFException {
		return convHostAndSF4IdType( context, idType, "SFTOCOBANK" );
	}

	/**
	 * 券商证件类型转存管证件类型
	 * @param idCode
	 *            证件号码
	 * @param context
	 * @return 证件类型
	 */
	private static String convSecuAndSF4IdType( Context context, String idType, String turnType ) throws SFException {
		SFLogger.info( context, String.format( "convSecuAndSF4IdType()券商证件类型转存管证件类型开始" ) );
		String newTypeId = "";
		Param param = null;
		String idName = getIdTypeName( context, turnType, idType );
		SFLogger.info( context, String.format( "券商证件类型[%s]转存管证件类型[%s]", idType, idName ) );
		Object sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// 从上下文中获取深证通标识
		if( "SECU2SF".equals( turnType ) ) {// 券商转三方存管证件类型
			if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( idType ) && !"70".equals( idType ) ) ) {// 深证通证件类型并且不是港澳台类型
				param = ParamCache.getValue( "SF_SZT2SF_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( idType ) && !"17".equals( idType ) ) ) {// 直联证件类型不是港澳台类型
				param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", idType );
				if( param != null ) {
					newTypeId = param.getValue();
				}
			} else {// 港澳台类型
				return "3";
			}
		} else if( "SF2SECU".equals( turnType ) ) {// 三方存管转券商证件类型
			if( SFConst.SECU_SZT.equals( sztFlag ) ) {// 深证通证件类型
				if( "3".equals( idType ) ) {// 如果证件类型为港澳台类型
					String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
					if( SFUtil.isEmpty( idCode ) ) {// 如果证件号码为空，则从Context取出客户对象
						InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
						if( investData != null ) {
							idCode = investData.getInvIdCode();
						}
					}
					SFLogger.info( context, String.format( "系统证件类型转换券商件类型，证件号码为[%s]", idCode ) );
					if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// 判断idCode第一位为H或者M则是港澳台类型
						return "60";// 返回60(港澳台居民通行证深证通类型)
					} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// 判断idCode首位是否为数字
						return "70";
					}
				} else {
					param = ParamCache.getValue( "SF_SF2SZT_ID_TYPE", idType );
					if( param != null ) {
						newTypeId = param.getValue();
					}
				}

			} else {// 直连证件类型
				if( "3".equals( idType ) ) {// 如果证件类型为港澳台类型
					String idCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
					if( SFUtil.isEmpty( idCode ) ) {// 如果证件号码为空，则从Context取出客户对象
						InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
						if( investData != null ) {
							idCode = investData.getInvIdCode();
						}
					}
					SFLogger.info( context, String.format( "系统证件类型转换券商件类型，证件号码为[%s]", idCode ) );
					if( SFUtil.isNotEmpty( idCode ) && ( 'H' == ( idCode.charAt( 0 ) ) || 'M' == ( idCode.charAt( 0 ) ) ) ) {// 判断idCode第一位为H或者M则是港澳台类型
						return "16";// 返回16(港澳台居民通行证深证通类型)
					} else if( SFUtil.isNotEmpty( idCode ) && SFUtil.isNum( SFUtil.objectToString( idCode.charAt( 0 ) ) ) ) {// 判断idCode首位是否为数字
						return "17";
					}
				} else {
					param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", idType );
					if( param != null ) {
						newTypeId = param.getValue();
					}
				}

			}
		}
		SFLogger.info( context, String.format( "convSecuAndSF4IdType()券商证件类型转存管证件类型结束，返回:[%s],证件类型[%s]", newTypeId, idName ) );
		return newTypeId;
	}

	/**
	 * 券商转三方
	 * 
	 * @param context
	 * @param idCode
	 * @param idType
	 * @return
	 * @throws SFException
	 */
	public static String convSecu2SF4IdType( Context context, String idType ) throws SFException {
		String newIdType = convSecuAndSF4IdType( context, idType, "SECU2SF" );
		return newIdType;
	}

	/**
	 * 三方转券商
	 * 
	 * @param context
	 * @param idCode
	 * @param idType
	 * @return
	 * @throws SFException
	 */
	public static String convSF2Secu4IdType( Context context, String idType ) throws SFException {
		String newIdType = convSecuAndSF4IdType( context, idType, "SF2SECU" );
		return newIdType;
	}

	/**
	 * 
	 *〈方法功能描述〉获取证件类型中文说明
	 * @param context
	 * @param type 证件类型
	 * @param id  证件类型值
	 * @return
	 * @throws SFException
	 */
	private static String getIdTypeName( Context context, String type, String id ) throws SFException {
		String idName = null;
		try {
			String sztFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			Param param = null;
			if( SFUtil.isEmpty( type ) )
				return idName;
			if( "SECU2SF".equals( type ) ) {
				if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( id ) && !"70".equals( id ) ) ) {// 深证通
					param = ParamCache.getValue( "SF_SZT2SF_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( id ) && !"17".equals( id ) ) ) {// 直联
					param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}

				} else {
					return "港澳台证件类型";
				}
			} else if( "SF2SECU".equals( type ) ) {
				if( SFConst.SECU_SZT.equals( sztFlag ) && ( !"60".equals( id ) && !"70".equals( id ) ) ) {// 深证通
					param = ParamCache.getValue( "SF_SF2SZT_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else if( SFConst.SECU_ZL.equals( sztFlag ) && ( !"16".equals( id ) && !"17".equals( id ) ) ) {// 直联
					param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", id );
					if( param != null ) {
						idName = param.getName();
					}
				} else {
					return "港澳台证件类型";
				}
			} else if( "HOSTTOSF".equals( type ) ) {
				param = ParamCache.getValue( "SF_HOST2SF_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "SFTOHOST".equals( type ) ) {
				param = ParamCache.getValue( "SF_SF2HOST_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "COBANKTOSF".equals( type ) ) {
				param = ParamCache.getValue( "SF_ZL2SF_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			} else if( "SFTOCOBANK".equals( type ) ) {
				param = ParamCache.getValue( "SF_SF2ZL_ID_TYPE", id );
				if( param != null ) {
					idName = param.getName();
				}
			}
		} catch( Exception e ) {
			SFLogger.error( context, String.format( "获取证件类型中文描述出错,[%s]", e ) );
		}
		return idName;
	}


	/**
	 * 检查当天是否发生过转账交易
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkTransfer( Context context ) throws SFException {
		SFLogger.info( context, "chkTransfer()开始" );
		try {
			Object tranCode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 从上下文中获取交易码
			Connection tranConnection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			AcctJour acctJour = new AcctJourDao().qryAcctJourByTxCode( context, tranConnection );
			if( "300102".equals( tranCode ) || "200102".equals( tranCode ) || "100102".equals( tranCode ) ) {
				SFUtil.chkCond( context, acctJour != null, "ST5775", "当日发生过签约或转账交易,不允许变更结算帐号" );
			} else if( "100101".equals( tranCode ) ) {
				SFUtil.chkCond( context, acctJour != null, "ST5775", "当日发生过签约或转账交易,不允许撤销签约关系" );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkTransfer()结束" );
	}

	/**
	 * 将字符数组转换成数字数组
	 * 
	 * @param ca
	 *            字符数组
	 * @return 数字数组
	 */
	public static int[] converCharToInt( char[] ca ) {
		int len = ca.length;
		int[] iArr = new int[ len ];
		try {
			for( int i = 0; i < len; i++ ) {
				iArr[ i ] = Integer.parseInt( String.valueOf( ca[ i ] ) );
			}
		} catch( NumberFormatException e ) {
			e.printStackTrace();
		}
		return iArr;
	}

	/**
	 * 数字验证
	 * 
	 * @param val
	 * @return 提取的数字。
	 */
	public static boolean isNum( String val ) {
		return val == null || "".equals( val ) ? false : val.matches( "^[0-9]*$" );
	}

	/** 每位加权因子 */
	public static final int power[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
	 * 
	 * @param iArr
	 * @return 身份证编码。
	 */
	public static int getPowerSum( int[] iArr ) {
		int iSum = 0;
		if( power.length == iArr.length ) {
			for( int i = 0; i < iArr.length; i++ ) {
				for( int j = 0; j < power.length; j++ ) {
					if( i == j ) {
						iSum = iSum + iArr[ i ] * power[ j ];
					}
				}
			}
		}
		return iSum;
	}

	/**
	 * 将power和值与11取模获得余数进行校验码判断
	 * 
	 * @param iSum
	 * @return 校验位
	 */
	public static String getCheckCode18( int iSum ) {
		String sCode = "";
		switch( iSum % 11 ) {
		case 10:
			sCode = "2";
			break;
		case 9:
			sCode = "3";
			break;
		case 8:
			sCode = "4";
			break;
		case 7:
			sCode = "5";
			break;
		case 6:
			sCode = "6";
			break;
		case 5:
			sCode = "7";
			break;
		case 4:
			sCode = "8";
			break;
		case 3:
			sCode = "9";
			break;
		case 2:
			sCode = "X";
			break;
		case 1:
			sCode = "0";
			break;
		case 0:
			sCode = "1";
			break;
		}
		return sCode;
	}
	
	/**
	 * 将18位身份证号码转换为15位
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo15( String invIdCode ) throws SFException {
		String invIdCode15 = invIdCode.substring(0, 6)+invIdCode.substring(8, 17); // 获取首6位+获取8-17位=组成15位身份证号码		
		return invIdCode15;
	}
	
	
	/**
	 * 将15位身份证号码转换为18位（19**）
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo18Card19( String invIdCode ) throws SFException {
		return conver15CardTo18( invIdCode, "19" );
	}

	/**
	 * 将15位身份证号码转换为18位（20**）
	 * 
	 * @param invIdCode
	 * @return
	 * @throws SFException
	 */
	public static String converTo18Card20( String invIdCode ) throws SFException {
		return conver15CardTo18( invIdCode, "20" );
	}

	/**
	 * 将15位身份证号码转换为18位
	 * 
	 * @param idCard
	 *            15位身份编码
	 * @return 18位身份编码
	 */
	private static String conver15CardTo18( String invIdCode, String preYear ) throws SFException {
		String idCard18 = null;
		if( SFUtil.isEmpty( invIdCode ) || invIdCode.length() != 15 ) {
			return null;
		}
		try {
			if( BizUtil.isNum( invIdCode ) ) {
				// 获取出生年月日
				String birthday = invIdCode.substring( 6, 12 );
				Date birthDate;
				birthDate = DateUtil.strToDate( "yyyyMMdd", preYear + birthday );
				Calendar cal = Calendar.getInstance();
				if( birthDate != null )
					cal.setTime( birthDate );
				// 获取出生年(完全表现形式,如：2010)
				String sYear = String.valueOf( cal.get( Calendar.YEAR ) );
				idCard18 = invIdCode.substring( 0, 6 ) + sYear + invIdCode.substring( 8 );
				// 转换字符数组
				char[] cArr = idCard18.toCharArray();
				if( cArr != null ) {
					int[] iCard = BizUtil.converCharToInt( cArr );
					int iSum17 = BizUtil.getPowerSum( iCard );
					// 获取校验位
					String sVal = BizUtil.getCheckCode18( iSum17 );
					if( sVal.length() > 0 ) {
						idCard18 += sVal;
					} else {
						return null;
					}
				}
			}
		} catch( Exception e ) {
			throw new SFException( "ST5799", "将15位身份证号码转换为18位失败！", e );
		}
		return idCard18;
	}

	/**
	 * 根据证件类型区分境内外标志
	 * 
	 * @param IdType
	 * @return
	 */
	public static String convInterFlag( String idType ) {
		return ( "4".equals( idType ) || "3".equals( idType ) || "19".equals( idType ) || "20".equals( idType ) ) ? SFConst.INTER_FLAG_DOMESTIC : SFConst.INTER_FLAG_ABROAD;
	}

	/**
	 * 0/20/21不允许做此交易
	 * 
	 * @param IdType
	 * @return
	 * @throws SFException
	 */
	public static void chkIdType( Context context, String idType, String invType ) throws SFException {
		if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_OTHERS.equals( idType ) ) {
			// 个人客户0证件类型不允许做此交易
			SFUtil.chkCond( context, "ST5100", String.format( "该证件类型不允许做此业务ID_TYPE=[%s]", idType ) );
		} else if( SFConst.INV_TYPE_CORP.equals( invType ) && ( SFConst.ID_TYPE_PERSON_WGGMQTZJ.equals( idType ) || "21".equals( idType ) ) ) {
			// 对公客户20/21证件类型不允许做此交易
			SFUtil.chkCond( context, "ST5100", String.format( "该证件类型不允许做此业务ID_TYPE=[%s]", idType ) );
		}

	}

	/**
	 * 银行端交易交易币种校验
	 * 
	 * @param context
	 * @param initSide
	 *            渠道
	 * @param curCode
	 *            币种
	 * @return
	 * @throws SFException
	 */
	public static String convCurCode( Context context, String curCode ) throws SFException {
		String initSide = SFUtil.getDataValue( context, "CTX_PUBLIC_INIT_SIDE" );// 获取渠道
		if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) && SFUtil.isEmpty( curCode ) ) {
			curCode = SFConst.CUR_CODE_RMB;
		}
		SFUtil.chkCond( context, !SFConst.CUR_CODE_RMB.equals( curCode ), "ST5100", String.format( "币种错误！CUR_CODE=[%s]", curCode ) );
		return curCode;
	}

	/**
	 * 银行渠道转加密券商资金密码
	 * 
	 * @param context
	 * @param initSide
	 *            渠道
	 * @param chnPwd
	 *            资金密码
	 * @return
	 * @throws SFException
	 */
	public static String convBankChLEncryptPwd( Context context, String secCompCode, String initSide, String invType, String chnPwd ) throws SFException {
		String txtPwd = null;// 解密后明文
		String eptPwd = null;// 转加密后密文
		try {
			/*
			 * 获取券商密钥
			 */
			String secretKey = ParamCache.getValue2( "SEC_ENCRYPT", secCompCode );

			// 不采用加密方式进行传送的渠道
			String channelStr = ParamCache.getValue2( "SF_SYS", "UNENCRYPTED_CHL" );

			/*
			 * ****************** 解密渠道密码*****************
			 */
			if( channelStr.indexOf( initSide ) == -1 && "1".equals( invType ) ) {// 采用加密方式进行传送的渠道
				// 对个人客户网银渠道的资金密码解密成明文，其它渠道均为明文不需转成明文
				txtPwd = DESUtil.decode( chnPwd, SFConst.EB_ENCRYPT_KEY );
				if( txtPwd != null && txtPwd.length() > 6 ) {
					txtPwd = txtPwd.substring( 0, 6 );
				}
			} else {
				// 除网银发来的密文，其它渠道均为明文，不需转成明文
				txtPwd = chnPwd;
			}
			/*
			 * *****************加密券商密码*****************
			 */
			// 密钥的ASCILL转换成十六进制
			String hKey = DESUtil.bytesToHex( secretKey.getBytes() );
			hKey = DESUtil.formatString( hKey, 16 );
			// 明文ASCILL转换成十六进制
			String hData = DESUtil.bytesToHex( txtPwd.getBytes() );
			// String hKey=DESUtil.bytesToHex(secretKey.getBytes());
			// 格式化字符串，不足部份补0
			hData = DESUtil.formatString( hData, 16 );
			// hData=hData+"0030";
			// 加密
			eptPwd = DESUtil.encode( hData, hKey );
			SFUtil.chkCond( context, SFUtil.isEmpty( eptPwd ), "ST4223", "加密券商资金密码失败！" );
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( "ST4223", e.getMessage(), e );
		}
		return eptPwd;

	}

	/**
	 * 合作行端交易转加密券商资金密码
	 * 
	 * @param context
	 * @param initSide
	 *            渠道
	 * @param chnPwd
	 *            资金密码
	 * @return
	 * @throws SFException
	 */
	public static String convCobankEncryptPwd( Context context, String secCompCode, AgtAgentInfo agtInfo, String chnPwd ) throws SFException {
		String txtPwd = null;// 解密后明文
		String eptPwd = null;// 转加密后密文
		try {
			if( agtInfo == null ) {
				throw new SFException( "ST4223", "证券资金帐号密码加密失败，请与银行技术人员联系！" );
			}

			/*
			 * ****************** 解密渠道密码*****************
			 */
			if( SFUtil.isNotEmpty( agtInfo.getPinkey() ) && chnPwd.length() > 6 ) {
				// 对网银渠道的资金密码解密成明文，其它渠道均为明文不需转成明文
				txtPwd = DESUtil.decode( chnPwd, agtInfo.getPinkey() );
				if( txtPwd != null && txtPwd.length() > 6 ) {
					txtPwd = txtPwd.substring( 0, 6 );
				}
			} else {
				// 除网银发来的密文，其它渠道均为明文，不需转成明文
				txtPwd = chnPwd;
			}

			/*
			 * *****************加密券商密码*****************
			 */
			// 获取券商密钥
			String secretKey = ParamCache.getValue2( "SEC_ENCRYPT", secCompCode );
			// 密钥的ASCILL转换成十六进制
			String hKey = DESUtil.bytesToHex( secretKey.getBytes() );
			hKey = DESUtil.formatString( hKey, 16 );
			// 明文ASCILL转换成十六进制
			String hData = DESUtil.bytesToHex( txtPwd.getBytes() );
			// String hKey=DESUtil.bytesToHex(secretKey.getBytes());
			// 格式化字符串，不足部份补0
			hData = DESUtil.formatString( hData, 16 );
			// hData=hData+"0030";
			// 加密
			eptPwd = DESUtil.encode( hData, hKey );
			SFUtil.chkCond( context, SFUtil.isEmpty( eptPwd ), "ST4223", "加密券商资金密码失败！" );
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			throw new SFException( "ST4223", e.getMessage(), e );
		}
		return eptPwd;

	}

	/**
	 * 客户类型转换：深证通转三方
	 * 
	 * @param context
	 * @param invTypeSZT
	 * @return
	 * @throws SFException
	 */
	public static String convSZT2SFInvType( Context context, String invTypeSZT ) throws SFException {
		String invType = null;
		if( SFConst.INV_TYPE_CORP_SZT.equals( invTypeSZT ) ) {
			invType = SFConst.INV_TYPE_CORP;
		} else {
			invType = SFConst.INV_TYPE_RETAIL;
		}
		return invType;
	}

	/**
	 * 客户类型转换：三方转深证通
	 * 
	 * @param context
	 * @param invTypeSZT
	 * @return
	 * @throws SFException
	 */
	public static String convSF2SZTInvType( Context context, String invType ) throws SFException {
		String invTypeSZT = null;
		if( SFConst.INV_TYPE_CORP.equals( invType ) ) {
			invTypeSZT = SFConst.INV_TYPE_CORP_SZT;
		} else {
			invTypeSZT = SFConst.INV_TYPE_RETAIL_SZT;
		}
		return invTypeSZT;
	}

	/**
	 * 
	 * 通过客户类型设置默认性别
	 * 
	 * @param context
	 * @param sex
	 * @param invType
	 * @return
	 * @throws SFException
	 */
	public static String convSZT2SFSex( Context context, String sex, String invType ) throws SFException {
		if( SFUtil.isEmpty( sex ) ) {
			if( "1".equals( invType ) ) {
				sex = "M";
			} else {
				sex = "-";
			}
		}
		return sex;
	}

	public static String convSZT2SFNationality( Context context, String nationality ) throws SFException {
		if( SFUtil.isEmpty( nationality ) ) {
			nationality = "CHN";
		}
		return nationality;
	}

	/**
	 * 上主机查询校验卡状态
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryCardAttrClient( Context context, Map<String, Object> msg ) throws SFException {

		Context msgContext = null;
		try {
			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// 账号ACCT_NO
			QryCardAttrClient qryCardAttrClient = new QryCardAttrClient();
			msgContext = qryCardAttrClient.send( context, tmpMsg );

			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			// String retCode = SFUtil.getDataValue(msgContext, kColl,
			// "RET_CODE");
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息

			// 主机返回失败或异常,交易退出
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "上主机[查询卡状态]失败,主机返回[%s]", retMsg ) );

			// 卡状态校验
			String resultFlag = "1";// 是否第三方存管系统卡校验位，0-是，1-否
			String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// 客户类型
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// 个人
				String retStat = SFUtil.getDataValue( msgContext, "SYS_HEAD.RET_STATUS" );
				if( "S".equals( retStat ) ) {
					// 校验卡挂失状态
					IndexedCollection certArrayIColl = SFUtil.getDataElement( msgContext, "MSG_O_CERT_ARRAY" );
					for( int i = 0; certArrayIColl != null && i < certArrayIColl.size(); i++ ) {
						String certType = SFUtil.getDataValue( msgContext, ( KeyedCollection )certArrayIColl.getElementAt( i ), "CERT_TYPE" );// 凭证类型
						if( "000".equals( certType ) ) {// 000-类型为卡
							certArrayIColl = SFUtil.getDataElement( msgContext, "CERT_PROPERTY_ARRAY" );
							String certStatus = SFUtil.getDataValue( msgContext, ( KeyedCollection )certArrayIColl.getElementAt( i ), "CERT_STATUS" );// 凭证状态
							// 73-银行卡口头挂失;74-银行卡书面挂失
							SFUtil.chkCond( context, "73".equals( certStatus ) || "74".equals( certStatus ), "ST5100", String.format( "卡状态异常，卡已挂失！" ) );
							// 71.司法冻结
							SFUtil.chkCond( context, "71".equals( certStatus ), "ST5101", String.format( "银行帐号状态不正常[账户已被司法冻结]，不能开通第三方存管业务" ) );
							// 判断一账通卡是否过期
							SFUtil.chkCond( context, "90".equals( certStatus ), "ST5101", String.format( "银行帐号状态不正常[一账通卡已过期]，不能开通第三方存管业务" ) );
						}
					}
					// 校验个贷放款账户
					IndexedCollection acctArrayIColl = SFUtil.getDataElement( msgContext, "ACCT_PROPERTY_ARRAY" );
					for( int i = 0; acctArrayIColl != null && i < acctArrayIColl.size(); i++ ) {
						String acctStatus = SFUtil.getDataValue( msgContext, ( KeyedCollection )acctArrayIColl.getElementAt( i ), "ACCT_STATUS" );// 账户状态
						// 校验个贷放款账户 51.信贷资金监管账户(公司)
						SFUtil.chkCond( context, "51".equals( acctStatus ), "ST5102", String.format( "卡下有账号为[贷款账号]，不能开通第三方存管业务" ) );
						// 校验三方存管状态字 50:第三方存管
						if( "50".equals( acctStatus ) ) {
							resultFlag = "0";// 是三方存管帐号
						}
					}
				}
			}
			SFUtil.addDataField( msgContext, "RESULT_FLAG", resultFlag );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryCardAttrClient()失败%s", e.getMessage() ) );
		}

		return msgContext;
	}

	/**
	 * 上卡管查询卡类型和等级
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryCardTypeClient( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "上主机查询是否联名卡开始" );
		String cardType = null;// 卡类型
		String cardlevel = null;// 卡等级
		String lmcard = null;// 是否是联名卡 1:是
		Context msgContext = null;
		try {

			/**
			 * 上卡管判断是否是联名卡
			 */
			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// 账号ACCT_NO
			QryCardTypeClient qcTypeClient = new QryCardTypeClient();

			msgContext = qcTypeClient.send( context, tmpMsg ); // 发送报文
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			String respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息
			String respCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// 响应码
			String retstat = SFUtil.getDataValue( msgContext, "SYS_HEAD.RET_STATUS" );

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST4377", String.format( "上主机查询是否联名卡失败,主机返回[%s]", respMsg ) );
			SFUtil.chkCond( context, !SFConst.INIT_SIDE_SECU.equals( retstat ) || !SFConst.DEFAULT_TRXCODE.equals( respCode ), "ST4377", String.format( "上主机查询是否联名卡失败,主机返回[%s]", respMsg ) );

			// 当retstat为"S"且响应码为000000取卡类型
			IndexedCollection cardMsgiColl = SFUtil.getDataElement( msgContext, "MSG_O.CARD_MSG_ARRAY" );
			SFUtil.chkCond( context, ( null == cardMsgiColl || cardMsgiColl.size() <= 0 ), "ST4403", String.format( "上主机查询卡片类型出错" ) );

			// 匹配银证联名卡的卡类型，一旦符合就立马退出,020,200,300,400分别银证普卡的等级，094，095，096，097为银证IC卡
			for( int i = 0; i < cardMsgiColl.size(); i++ ) {
				cardType = SFUtil.getDataValue( msgContext, ( KeyedCollection )cardMsgiColl.getElementAt( i ), "CARD_TYPE" );
				if( "040".equals( cardType ) || "200".equals( cardType ) || "300".equals( cardType ) || "400".equals( cardType ) || "094".equals( cardType ) || "095".equals( cardType ) || "096".equals( cardType ) || "097".equals( cardType ) ) {
					lmcard = "1";
					break;
				}
			}

			// 判断该联名卡是否是要签约平安证券，如果不是则直接退出报错
			if( "1".equals( lmcard ) ) {
				SFLogger.info( context, String.format( "此卡cardno[%s]为联名卡", msg.get( "ACCT_ID" ) ) );
				SFUtil.chkCond( context, ( !SFConst.SECU_PINGANZQ.equals( msg.get( "SEC_COMP_CODE" ) ) && !SFConst.SECU_PINGANXY.equals( msg.get( "SEC_COMP_CODE" ) ) ), "ST5111", String.format( "银证联名卡不能签约非平安证券" ) );

				/**
				 * 联名卡签约上卡管判断卡等级开始
				 */
				tmpMsg = new HashMap<String, Object>();
				tmpMsg.put( "ACCT_ID", msg.get( "ACCT_ID" ) );// 账号ACCT_NO
				QryCardLevelClient qcLeveClient = new QryCardLevelClient();
				msgContext = qcLeveClient.send( context, tmpMsg ); // 发送报文

				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
				IndexedCollection retiColl = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection ikColl = ( KeyedCollection )retiColl.getElementAt( 0 );// 获取响应数组
				String retMsg = SFUtil.getDataValue( msgContext, ikColl, "RET_MSG" );// 响应信息

				// 判断查询是否成功
				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST4377", String.format( "上主机查询联名卡等级失败,主机返回[%s]", retMsg ) );
				// 联名卡等级
				cardlevel = SFUtil.getDataValue( msgContext, "MSG_O.CARD_LEVEL" );
				SFLogger.info( context, String.format( "联名卡签约平安证券卡片等级为cardlv[%s]", cardlevel ) );
			}
			SFUtil.addDataField( msgContext, "CARD_LEVEL", cardlevel );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "上主机查询卡类型和等级失败[%s]", e.getMessage() ) );
		}
		return msgContext;
	}

	/**
	 * 上主机查询卡主帐号开始
	 * 
	 * @param context
	 * @param msg
	 * @return
	 * @throws SFException
	 */
	public static Context qryBalClient( Context context ) throws SFException {
		Context msgContext = null;
		try {
			String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// 账号ACCT_NO
			String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// 币种CCY
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 账户名称ACCT_NAME
			String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码

			Map<String, Object> tmpMsg = new HashMap<String, Object>();
			tmpMsg.put( "ACCT_ID", acctId );
			tmpMsg.put( "CUR_CODE", curCode );
			tmpMsg.put( "INV_NAME", invName );
			QryBalClient qryBalClient = new QryBalClient();
			msgContext = qryBalClient.send( context, tmpMsg ); // 发送报文
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			String respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5602", String.format( "上主机获取[卡主账号]失败，主机返回[%s]", respMsg ) );

			String savAcct = null;
			String branchId = null;
			String invType = SFUtil.getDataValue( context, SFConst.PUBLIC_INV_TYPE );// 客户类型
			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// 个人
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
				kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// 获取响应数组
				savAcct = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// 卡主帐号
				SFLogger.info( context, String.format( "取卡下主帐号[%s]", savAcct ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( savAcct ), "ST5602", String.format( "上主机获取[卡主账号]失败" ) );

				String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// 取人行账户分类
				SFLogger.info( context, String.format( "取人行账户分类[%s]", pbcAcctType ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5602", String.format( "上主机获取[人行账户分类]失败" ) );

				if( "2".equals( pbcAcctType ) ) {// 二类账户
					SFLogger.info( context, String.format( "该券商已进入二类账户校验[%s]", secCompCode ) );
					// 没查到允许二类账户签约记录，拦截报错
					SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// 获取券商信息
					SFUtil.chkCond( context, !"1".equals( secCompData.getIIAcctFlag() ), "ST5421", String.format( "该券商不允许办理[二类账户签约]" ) );
					SFLogger.info( context, String.format( "该券商二类账户校验通过" ) );
				}
				branchId = SFUtil.getDataValue( msgContext, kColl, "OPEN_DEP_ID" );// 开户网点号
				SFLogger.info( context, String.format( "取卡开户网点号[%s]", branchId ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( branchId ), "ST5602", String.format( "上主机获取[账号开户网点]失败" ) );

			}

			SFUtil.addDataField( msgContext, "SAV_ACCT", savAcct );
			SFUtil.addDataField( msgContext, "OPEN_DEP_ID", branchId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryCardAttrClient()失败%s", e.getMessage() ) );
		}
		return msgContext;
	}

	/**
	 * 发送BECIF协议
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static void addAutoBecif( Context context, Connection tranConnection, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "addAutoBecif()开始" );
		try {
			// 发送BECIF协议
			String openDepId = null;// 推荐网点号
			String secCompCode = SFUtil.objectToString( msg.get( "SEC_COMP_CODE" ) );// 券商代码
			String acctId = SFUtil.objectToString( msg.get( "ACCT_ID" ) );// 银行帐号
			String initSide = SFUtil.objectToString( msg.get( "INIT_SIDE" ) );// 渠道
			String becifNo = SFUtil.objectToString( msg.get( "ECIF_NO" ) );
			String invType = SFUtil.objectToString( msg.get( "INV_TYPE" ) );
			String counterID = SFUtil.objectToString( msg.get( "USER_ID" ) );
			String txDate = SFUtil.objectToString( msg.get( "TX_DATE" ) );
			String txTime = SFUtil.objectToString( msg.get( "TX_TIME" ) );

			SFLogger.info( context, String.format( "becifNo:[%s]", becifNo ) );

			AutoBecifDao autoBecifDao = new AutoBecifDao();
			SignAccountDataDao signAccountDataDao = new SignAccountDataDao();
			SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// 获取券商信息
			DBHandler.beginTransaction( context, tranConnection );// 开启事务

			String subType = null;
			String tpdmFlag = secCompData.getTpdmFlag();
			/* 判断是普通证券还是融资融券 */
			if( SFConst.TPDM_FLAG_NORMAL.equals( tpdmFlag ) ) {// 普通证券
				subType = "R81";
			} else if( SFConst.TPDM_FLAG_MARGIN.equals( tpdmFlag ) ) {// 融资融券
				subType = "R83";
			}
			// 拼接协议号 券商代码（8位）-协议小类（R81 为A股、R82为B股 、R83为融资融券）-卡号
			String agreementNo = secCompCode + "-" + subType + "-" + acctId;

			// 查询卡号对应成功签约该券商的个数，如果只有一个，即是刚刚签约的这一条。需要发协议到BECIF
			List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, acctId, secCompCode );
			if( signAccountDataList != null && signAccountDataList.size() == 1 && !SFUtil.isEmpty( subType ) && SFUtil.isNotEmpty( becifNo ) && SFConst.INV_TYPE_RETAIL.equals( invType ) ) {

				if( !SFConst.INIT_SIDE_ABBANK.equals( initSide ) ) {// 非柜面的电子渠道来的交易，操作柜员置为EB001
					counterID = "EB001";
					openDepId = "9998";
				}

				// 拼接协议号 券商代码（8位）-协议小类（R81 为A股、R82为B股 、R83为融资融券）-卡号
				AutoBecif autoBecif = new AutoBecif();
				autoBecif.setTxTime( txTime );
				autoBecif.setStatus( "0" );
				autoBecif.setAgreementNo( agreementNo );
				autoBecif.setBecifNo( becifNo );
				autoBecif.setAgreementType( "R8" );
				autoBecif.setAgreementSubType( subType );
				autoBecif.setAgreementStatus( "1" );
				autoBecif.setProductNo( acctId );
				autoBecif.setOpenDate( txDate );
				autoBecif.setCloseDate( "" );
				autoBecif.setDeptNo( openDepId );
				autoBecif.setUserId( counterID );
				autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );
				autoBecif.setTxDate( txDate );
				autoBecif.setSubTxSeqId( SFUtil.objectToString( msg.get( "SUB_TX_SEQ_ID" ) ) );
				autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务
			}
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "插入轮询发协议到BECIF数据失败" ) );
		}
		SFLogger.info( context, "addAutoBecif()结束" );
	}

	/**
	 * 
	 * 〈方法功能描述〉 上主机验证客户信息 - 区分个人和机构客户
	 * 
	 * @param context
	 * @param msg
	 * @throws SFException
	 */
	public static void chkKeyInvestInfoClient( Context context, Map<String, Object> msg ) throws SFException {
		SFLogger.info( context, "chkKeyInvestInfoClient()开始" );
		try {

			QryKeyInvestinfoClient qryKeyInvestInfoClient = new QryKeyInvestinfoClient();// 客户信息查询
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 客户名称
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// 客户类型
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// 证件号码
			String hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// 券商证件类型转换为主机证件类型
			String txcode = SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE );// 私有交易码
			SFLogger.info( context, String.format( "证件类型转换为主机证件类型 [%s]", hostIdType ) );

			// 主机返回证件类型
			String hostRetIdType = null;
			// 主机返回证件号码
			String hostInvIdCode = null;

			// 上主机查询客户信息
			Context msgContext = qryKeyInvestInfoClient.send( context, msg );

			// 响应报文返回标识
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5603", String.format( "上主机查询客户信息失败[%s]", retMsg ) );

			/* 主机返回客户类型、客户编码、客户名称 */
			KeyedCollection oKeyColl = ( KeyedCollection )msgContext.getDataElement( "MSG_O" );
			String hostInvName = SFUtil.getDataValue( msgContext, oKeyColl, "INV_NAME" );

			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// 个人客户

				hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "ID_TYPE" );
				hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "INV_ID_CODE" );

				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户证件类型 [%s]", hostRetIdType ) );
				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户名称 [%s]", hostInvName ) );
				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户证件号码 [%s]", hostInvIdCode ) );

				// 校验证件类型&客户名称
				SFUtil.chkCond( context, ( !hostIdType.equals( hostRetIdType ) ), "ST4090", String.format( "客户证件信息不符" ) );
				SFUtil.chkCond( context, ( !invName.equals( hostInvName ) ), "ST4531", String.format( "客户名称不符" ) );

				if( SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {

					// 校验证件号码 增加15,18位转换 兼容
					chkRetailInvIdCode( context, invIdCode, hostInvIdCode );
				}
			} else { // 机构客户

				SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "上主机[查询客户资料]失败" ) );

				hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "GLOBAL_TYPE" );
				hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "GLOBAL_ID" );
				String finFlag = SFUtil.getDataValue( msgContext, oKeyColl, "FIN_FLAG" );

				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户证件类型 [%s]", hostRetIdType ) );
				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户名称 [%s]", hostInvName ) );
				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取客户证件号码 [%s]", hostInvIdCode ) );
				SFLogger.info( context, String.format( "查询主机数据返回客户卡号获取同业标识 [%s]", finFlag ) );

				// 同业账号从OTHER_PROVE_FILE_TYPE取证件类型，OTHER_PROVE_FILE_NO取证件号码
				if( "I".equals( finFlag ) ) {
					hostRetIdType = SFUtil.getDataValue( msgContext, oKeyColl, "OTHER_PROVE_FILE_TYPE" );
					hostInvIdCode = SFUtil.getDataValue( msgContext, oKeyColl, "OTHER_PROVE_FILE_NO" );
				}
				if( "73".equals( hostRetIdType ) ) {
					hostRetIdType = "51";
				}

				// 主机证件类型转三方证件类型
				hostRetIdType = BizUtil.convHost2SF4IdType( msgContext, hostRetIdType );

				// 20180202-新增对公客户同步客户信息需要添加判断券商托管证件类型和号码一致性
				if( "100104".equals( txcode ) ) {

					// 调用C3011查询券商托管证件类型&号码
					QryCardAttrClient qryCardAttrClient = new QryCardAttrClient();
					msgContext = qryCardAttrClient.send( context, msg );

					retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
					iColl = SFUtil.getDataElement( msgContext, "RET" );
					kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
					retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息

					// 主机返回失败或异常,交易退出
					SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "上主机[查询卡状态]失败,主机返回[%s]", retMsg ) );

					// 主机成功获取券商托管证件类型&号码
					String trustIdType = SFUtil.getDataValue( msgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_TYPE" );// 券商托管证件类型
					String trustInvCodeId = SFUtil.getDataValue( msgContext, "MSG_O.STOCKJOBBER_TRUST_GLOBAL_NO" );// 券商托管证件号码

					SFLogger.info( context, String.format( "查询主机数据返回券商托管证件类型 [%s]", trustIdType ) );
					SFLogger.info( context, String.format( "查询主机数据返回券商托管证件号码 [%s]", trustInvCodeId ) );

					SFUtil.chkCond( context, ( ( !hostIdType.equals( BizUtil.convHost2SF4IdType( context, trustIdType ) ) || !invIdCode.equals( trustInvCodeId ) ) && ( !hostIdType.equals( hostRetIdType ) || !invIdCode.equals( hostInvIdCode ) ) ), "ST5040", String.format( "[客户信息]不符，客户必须先到银行更新资料" ) );
				} else {
					SFUtil.chkCond( context, ( !hostIdType.equals( hostRetIdType ) || !invIdCode.equals( hostInvIdCode ) ), "ST5040", String.format( "[客户信息]不符，客户必须先到银行更新资料" ) );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestInfoClient()失败%s", e.getMessage() ) );
		}
		SFLogger.info( context, "chkKeyInvestInfoClient()结束" );

	}

	/**
	 * 
	 *  上合作行查询客户信息
	 * 
	 * @param context
	 * @param tranConnection
	 * @param coBankMsg
	 * @param bankId
	 * @throws SFException
	 */
	public static Context qryKeyInvestorBycoBankClient( Context context, Connection tranConnection, String capAcct, String secCompCode ) throws SFException {
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()开始" );
		Context msgContext = null;
		try {
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 客户名称
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// 客户类型
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// 证件号码
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );// 证件类型

			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			AgtCustomerInfo agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
			SFUtil.chkCond( context, ( null == agtCustomerInfo ), "ST4090", String.format( "上合作行查询客户资料失败" ) );

			String cobankInvName = agtCustomerInfo.getInvName();
			String cobankIdType = agtCustomerInfo.getIdType();
			String cobankInvIdCode = agtCustomerInfo.getInvidCode();

			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户证件类型 [%s]", cobankIdType ) );
			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户名称 [%s]", cobankInvName ) );
			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户证件号码 [%s]", cobankInvIdCode ) );

			// 主机证件类型转换为合作行证件类型
			SFUtil.chkCond( context, ( !invName.equals( cobankInvName ) || ( !cobankIdType.equals( idType ) ) ), "ST4090", String.format( "客户身份信息核对不符" ) );

			// 校验证件类型是否一致
			if( !invIdCode.equals( cobankInvIdCode ) ) {
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( idType ) ) {

					// 校验证件号码 增加15,18位转换 兼容
					chkRetailInvIdCode( context, invIdCode, cobankInvIdCode );
				} else {
					SFUtil.chkCond( context, "ST5040", String.format( "客户证件号码与合作行系统不一致,客户必须先到银行进行更新资料" ) );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestorBycoBankClient()失败%s", e.getMessage() ) );
		}
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()结束" );
		return msgContext;
	}

	/**
	 * 
	 * 〈方法功能描述〉 上合作行验证客户信息
	 * 612326 上合作行校验一步式签约客户信息
	 * 
	 * @param context
	 * @param tranConnection
	 * @param coBankMsg
	 * @param bankId
	 * @throws SFException
	 */
	public static Context chkKeyInvestorBycoBankClient( Context context, Connection tranConnection, Map<String, Object> coBankMsg, String bankId ) throws SFException {
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()开始" );
		Context msgContext = null;
		try {
			String invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 客户名称
			String invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// 客户类型
			String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );// 证件号码
			String hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// 券商证件类型转换为主机证件类型

			// tranToHost 612326 上合作行校验一步式签约客户信息
			T810026Client qryKeyInvestorBycoBankClient = new T810026Client();// 合作行返回账户信息以及账户状态
			msgContext = qryKeyInvestorBycoBankClient.send( context, coBankMsg, bankId );

			// 返回报文信息
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			String respCode = SFUtil.getDataValue( msgContext, "A_REQUEST_HEAD.RESPCODE" );// 返回报文获取返回码&返回信息

			// 上合作行明确返回失败
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {
				// 根据错误码从数据库中找出对应的错误信息
				BankCodeInfoDao bankCodeInfoDao = new BankCodeInfoDao();
				BankCodeInfo bankCodeInfo = bankCodeInfoDao.qryBankCodeInfo( context, tranConnection, respCode );
				if( null != bankCodeInfo ) {
					SFUtil.chkCond( context, ( !SFConst.RET_SUCCESS.equals( retFlag ) ), "ST4091", String.format( bankCodeInfo.getMsg() ) );
				} else {
					SFLogger.info( context, "数据库中未保存此主机错误码,请增加" );
				}
			}

			// 上合作行超时或异常
			SFUtil.chkCond( context, ( SFConst.RET_OVERTIME.equals( retFlag ) ), "ST5799", String.format( "银行处理失败" ) );

			// 上合作行返回成功
			KeyedCollection oKeyCol = SFUtil.getDataElement( msgContext, "810026_O" );
			String coBankInvName = SFUtil.getDataValue( msgContext, oKeyCol, "INV_NAME" );// 银行端客户名称InvName
			String coBankHostIdType = SFUtil.getDataValue( msgContext, oKeyCol, "ID_TYPE" );// 银行端证件类型IdType
			String coBankInvIdCode = SFUtil.getDataValue( msgContext, oKeyCol, "INV_ID_CODE" );// 银行端证件号码InvIdCode

			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户证件类型 [%s]", coBankHostIdType ) );
			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户名称 [%s]", coBankInvName ) );
			SFLogger.info( context, String.format( "查询合作行返回客户卡号获取客户证件号码 [%s]", coBankInvIdCode ) );

			// 主机证件类型转换为合作行证件类型
			String dlHostIdType = BizUtil.convSF2CoBank4IdType( context, hostIdType );
			SFUtil.chkCond( context, ( !invName.equals( coBankInvName ) ), "ST4531", String.format( "客户名称不符" ) );
			SFUtil.chkCond( context, ( !dlHostIdType.equals( coBankHostIdType ) ), "ST4090", String.format( "客户证件信息不符" ) );

			// 校验证件类型是否一致
			if( !coBankInvIdCode.equals( invIdCode ) ) {
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {
					// 校验证件号码 增加15,18位转换 兼容
					chkRetailInvIdCode( context, invIdCode, coBankInvIdCode );
				} else {
					SFUtil.chkCond( context, "ST5040", String.format( "客户证件号码与合作行系统不一致,客户必须先到银行进行更新资料" ) );
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "qryKeyInvestorBycoBankClient()失败%s", e.getMessage() ) );
		}
		SFLogger.info( context, "qryKeyInvestorBycoBankClient()结束" );
		return msgContext;
	}

	/**
	 * 校验存管个人客户证件号码和主机或合作行返回证件号码
	 * 证件类型转换 增加15,18位兼容
	 * 
	 * @param context：上下文
	 * @param invIdCode：存管证件号码
	 * @param hostInvIdCode：主机或合作行返回证件号码
	 * @throws SFException：三方异常
	 */
	public static void chkRetailInvIdCode( Context context, String invIdCode, String hostInvIdCode ) throws SFException {
		SFLogger.info( context, "chkRetailInvIdCode()开始" );
		String invIdCode18Card19 = null; // 15位转18位证件号码，20世纪
		String invIdCode18Card20 = null; // 15位转18位证件号码，21世纪
		String hostInvIdCode18Card19 = null; // 主机或合作行返回证件号码15位转18位证件号码，20世纪
		String hostInvIdCode18Card20 = null; // 主机或合作行返回证件号码15位转18位证件号码，21世纪
		try {

			// 场景1：两者都是18位不用转换 2. 三方15位，主机18位 3.三方18位，主机15位 4.主机和三方都是15位
			if( SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 18 && SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 18 ) {
				SFUtil.chkCond( context, ( !hostInvIdCode.equals( invIdCode ) ), "ST4090", String.format( "客户身份信息核对不符" ) );
			}
			// 场景2：三方15位，主机15位
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 15 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 15 ) {
				SFUtil.chkCond( context, ( !hostInvIdCode.equals( invIdCode ) ), "ST4090", String.format( "客户身份信息核对不符" ) );
			}

			// 场景3：三方15位，主机18位[三方证件号码15位转18位后与主机返回18位证件号码比较]
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 15 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 18 ) {
				invIdCode18Card19 = BizUtil.converTo18Card19( invIdCode );
				invIdCode18Card20 = BizUtil.converTo18Card20( invIdCode );
				if( ( SFUtil.isNotEmpty( invIdCode18Card19 ) && SFUtil.isNotEmpty( invIdCode18Card20 ) ) && !invIdCode18Card19.equals( hostInvIdCode ) && ( !invIdCode18Card20.equals( hostInvIdCode ) ) ) {
					SFUtil.chkCond( context, "ST4090", String.format( "客户身份信息核对不符" ) );
				}
			}

			// 场景4：三方18位，主机15位[主机返回证件号码15位转18位后与三方证件号码比较]
			if( SFUtil.isNotEmpty( invIdCode ) && invIdCode.length() == 18 && SFUtil.isNotEmpty( hostInvIdCode ) && hostInvIdCode.length() == 15 ) {
				hostInvIdCode18Card19 = BizUtil.converTo18Card19( hostInvIdCode );
				hostInvIdCode18Card20 = BizUtil.converTo18Card20( hostInvIdCode );
				if( SFUtil.isNotEmpty( hostInvIdCode18Card19 ) && SFUtil.isNotEmpty( hostInvIdCode18Card20 ) && !invIdCode.equals( hostInvIdCode18Card19 ) && ( !invIdCode.equals( hostInvIdCode18Card20 ) ) ) {
					SFUtil.chkCond( context, "ST4090", String.format( "客户身份信息核对不符" ) );
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "chkRetailInvIdCode()失败%s", e.getMessage() ) );
		}
		SFLogger.info( context, "chkRetailInvIdCode()结束" );
	}

	/**
	 * 〈方法功能描述〉 上主机置卡状态字
	 * 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public static Context setCardStatusWord( Context context, String acctId, String funcCode, String openDepId ) throws SFException {
		SFLogger.info( context, "上主机维护卡状态字开始" );
		Context msgContext = null;
		String retFlag = null;
		String respMsg = null;
		String respCode = null;
		try {
			// 发展卡卡号/对公账号
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", BizUtil.getChlSeqId( context, getSubTxSeqId( BizUtil.getInitSeqId( context ) ) ) );// 上主机22位流水SYS_HEAD.CONSUMER_SEQ_NO字段
			msg.put( "ACCT_ID", acctId );// 账号ACCT_NO
			msg.put( "FUNCTION_CODE", funcCode );// 功能码 A-建立
			if( SFUtil.isNotEmpty( openDepId ) ) {
				msg.put( "BRANCH_ID", openDepId );// 开户网点
			}
			SetCardStatusWordClient setCardStatusWordClient = new SetCardStatusWordClient();// 设置卡状态
			msgContext = setCardStatusWordClient.send( context, msg );

			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// 获取响应数组

				respCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );
				respMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );

				// 已存在或者不存在[第三方托管账户]状态，返回券商置卡状态成功
				if( ( respCode.endsWith( "ME4022" ) && "A".equals( funcCode ) ) || ( respCode.endsWith( "ME4021" ) && "D".equals( funcCode ) ) ) {
					retFlag = SFConst.RET_SUCCESS;
				}
			}

			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", respMsg );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "上主机维护卡状态字结束" );
		return msgContext;
	}

	/**
	 * 设置券商营业部编码，针对银河证券、五矿证券
	 * 
	 * @param secCompCode
	 * @param map
	 * @param secBrchId
	 */
	public static void setSecBrchId( String secCompCode, Map<String, Object> map, String secBrchId ) {
		if( SFConst.SECU_YINHEZQ.equals( secCompCode ) || SFConst.SECU_WUKUANGZQ.equals( secCompCode ) ) {
			map.put( "SEC_BRCH_ID", secBrchId );
		}
	}

	/**
	 * 调整中信金通为中信证券
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void setZhongXinSecuCompCode( Context context ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码
		if( SFConst.SECU_ZHONGXINJTZQ.equals( secCompCode ) ) {// 调整12710000券商代码为11020000
			SFUtil.setReqDataValue( context, "SEC_COMP_CODE", SFConst.SECU_ZHONGXINZQ );
		}
	}

	/**
	 * 
	 * 检查重复流水
	 * 
	 * @param context
	 * @throws SFException
	 */
	public static void chkRepeatAcctJour( Context context, Connection tranConnection ) throws SFException {
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码
		String acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// 银行卡号
		String secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// 券商流水号
		String txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// 交易日期
		AcctJour acctJour = new AcctJourDao().qryAcctJourByJourFlag( context, tranConnection, secSeqId, secCompCode, acctId, "00", txDate );
		SFUtil.chkCond( context, ( null != acctJour ), "ST5704", String.format( "[客户流水号]重复" ) );
	}

	/**
	 * 是否设置缓存参数默认值判断
	 * 
	 * @param type
	 *            参数类型
	 * @return
	 */
	public static String getDefaultParam( String type ) {
		String[] arr = new String[] { "SEC_APP_ID", "SEC_INTEREST_FILE", "SEC_CLEARING_FILE", "SEC_CLOSE_ACCOUNT_FILE", "SEC_OPEN_ACCT_ABNORMAL_FILE", "SEC_INTEREST_FILE_REC", "SEC_CLEARING_FILE_REC", "SEC_CLOSE_ACCOUNT_FILE_REC", "SEC_OPEN_ACCT_ABNORMAL_FILE_REC", "SEC_TRANSFER_FILE_REC", "SEC_CHECKING_FILE_REC", "SEC_INTEREST_FILE_FMT", "SEC_CLEARING_FILE_FMT", "SEC_OPEN_ACCT_ABNORMAL_FILE_FMT", "SEC_TRANSFER_FILE_FMT", "SEC_CHECKING_FILE_FMT", "SEC_INTEREST_TAX", "SEC_ACCT_BAL_CHK" };
		for( int i = 0; i < arr.length; i++ ) {
			if( type.equals( arr[ i ] ) ) {
				return "DEFAULT";
			}
		}
		return null;
	}

	/**
	 * 路径处理
	 * 
	 * @param context
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String processPath( Context context, String path ) throws Exception {
		try {
			SFUtil.chkCond( context, path == null, "ST4589", "路径为空，请检查参数配置" );
			if( !( path.endsWith( "\\" ) || path.endsWith( "/" ) ) )
				path = path + "/";
		} catch( Exception e ) {
			throw e;
		}
		return path;
	}

	/**
	 * 获取日志路径
	 * 
	 * @return
	 */
	public final static String getLogPath() {
		// log输出路径
		String logPath = "/logs";
		String sLogPath = SFUtil.getSysProperty( "LOG_PATH" );// ParamCache.getValue2( "SF_LOG", "SYS_LOG_PATH" );
		if( SFUtil.isNotEmpty( sLogPath ) ) {
			logPath = sLogPath;
		}
		return logPath;
	}

	/**
	 * 合作行MAC校验
	 * 
	 * @param context
	 * @param msgSource
	 * @throws SFException
	 */
	public static void chkCoBankMac( Context context, String msgSource ) throws SFException {
		SFLogger.debug( context, String.format( "合作行MAC校验:msgSource【%s】", msgSource ) );
		Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );// 从上下文中取出合作行
		if( agentInfo == null ) {
			/*
			 * 从报文头中获取合作行信息
			 */
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {// 非行E通报文从请求头里取出合作行号
				bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 从头信息取出BankId
			} else {
				bankNo = SFUtil.getDataValue( context, "BANK_ID" );// 从头信息取出BankId
			}
			if( SFUtil.isEmpty( bankNo ) ) {
				SFLogger.error( context, "报文头中获取合作行信息失败！" );
				throw new SFException( "ST4528", "报文头中获取合作行信息失败！" );
			}
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		if( agentInfo == null ) {
			SFLogger.error( context, "上下文中获取合作行信息失败！" );
			throw new SFException( "ST4528", "上下文中获取合作行信息失败！" );
		}
		/*
		 * 无需MAC校验
		 */
		if( "0".equals( agentInfo.getMacFlag() ) ) {
			return;
		}
		/*
		 * MAC校验
		 */
		try {
			boolean retFlag = MacUtil.checkMac( agentInfo.getMackey(), msgSource );
			if( !retFlag ) {
				SFLogger.error( context, "MAC校验错！" );
				throw new SFException( "ST4528", "MAC校验错！" );
			}
		} catch( Exception e ) {
			SFLogger.error( context, "MAC校验错！" );
			throw new SFException( "ST4528", "MAC校验错！" );
		}

	}

	/**
	 * 生成合作行MAC校验码
	 * 
	 * @param context
	 * @param msgSource
	 * @throws SFException
	 */
	public static String genCoBankMac( Context context, String msgSource ) throws SFException {
		if( SFUtil.isEmpty( msgSource ) || msgSource.length() > 3008 ) {
			SFLogger.error( context, String.format( "输入报文长度[%d]大于定义的变量source长度[3000]!", msgSource.length() ) );
			throw new SFException( "ST4406", String.format( "输入报文长度[%d]大于定义的变量source长度[3000]!", msgSource.length() ) );
		}
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );// 从上下文中取出合作行

		/**
		 * 如果从上下文中未取到合作行信息，再进行查询
		 */
		if( agentInfo == null ) {
			/*
			 * 从报文头中获取合作行信息
			 */
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = null;
			if( !context.containsKey( "A_RESPONSE_HEAD.CHCICSCODE" ) ) {// 非行E通报文从请求头里取出合作行号
				bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 从头信息取出BankId
			} else {
				bankNo = SFUtil.getDataValue( context, "BANK_ID" );// 从头信息取出BankId
			}
			if( SFUtil.isEmpty( bankNo ) ) {
				SFLogger.error( context, "报文头中获取合作行信息失败！" );
				throw new SFException( "ST4528", "报文头中获取合作行信息失败！" );
			}
			Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		if( agentInfo == null ) {
			SFLogger.error( context, "上下文中获取合作行信息失败！" );
			throw new SFException( "ST4406", "上下文中获取合作行信息失败！" );
		}
		// 无需MAC校验
		// if ("0".equals(agentInfo.getMacFlag())) {//临时注释，原C不许分是否需要校验
		// return "";
		// }
		String retMac = "";
		// MAC加密
		try {
			retMac = MacUtil.generateMac8( agentInfo.getMackey(), msgSource );
		} catch( Exception e ) {
			SFLogger.error( context, "MAC校验错！" );
			throw new SFException( "ST4406", "MAC校验错！" );
		}
		SFLogger.debug( context, String.format( "合作行MAC校验码加密:msgSource【%s】,MAC【%s】", msgSource, retMac ) );
		return retMac;
	}

	public static int getSourceLength( Context context, int length ) throws SFException {
		AgtAgentInfo agentInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_AGENTINFO );
		if( agentInfo == null ) {
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			String bankNo = SFUtil.getDataValue( context, "A_REQUEST_HEAD.BANKNO" );// 从头信息取出BankId
			Connection connection = SFUtil.getDataValue( context, SFConst.PUBLIC_TRAN_CONNECTION );
			agentInfo = agtAgentInfoDao.qryAgtAgentInfo( context, connection, bankNo );
		}
		/*
		 * 无需MAC校验
		 */
		if( "0".equals( agentInfo.getMacFlag() ) ) {
			return length;
		}

		return length + 8;
	}

	/**
	 * 获取合作行左补0后的金额
	 *〈方法功能描述〉
	 * @param context
	 * @param tranAmount
	 * @return
	 * @throws SFException
	 */
	public static String getCobankTranAmount( Context context, String tranAmount ) {
		return SFUtil.fixChar( tranAmount, 13, '0', "left" );
	}

	/**
	 * 当异常响应报文超长时获取异常响应报文正确长度文本
	 *〈方法功能描述〉
	 * @param context 
	 * @param retMsg 异常信息字节数组
	 * @param len  需要截取的长度
	 * @return
	 */
	public static String getResponseMsg( Context context, String retMsg, int len ) {
		int subCount = 0;
		byte[] msg = retMsg.getBytes();
		for( int i = 0; i < len; i++ ) {
			if( msg[ i ] < 0 ) {
				subCount++;
			}
		}
		if( subCount % 2 == 0 ) {
			len = subCount;
		} else {
			len -= 1;
		}
		return new String( msg, 0, len );
	}
	
	
	/**
	 * 截取银行渠道流水号
	 *〈方法功能描述〉
	 * @param context
	 * @param chlTranSeq 渠道送来的流水号
	 * @param initSide   渠道
	 * @param invType    客户类型
	 * @return
	 */
	public static String getBankSeqid( Context context,String chlTranSeq, String initSide, String invType) {
		if (SFConst.INV_TYPE_RETAIL.equals(invType)&&chlTranSeq!=null){//个人
			if (chlTranSeq.length()>21){
				chlTranSeq = chlTranSeq.substring(14, 22);
			}else if (chlTranSeq.length()>14){
				chlTranSeq = chlTranSeq.substring(14);
			} else {
				chlTranSeq = "";
			}
		}else {//对公
			if(!"B".equals(initSide)&&chlTranSeq!=null){
				if (chlTranSeq.length()>23){
					chlTranSeq = chlTranSeq.substring(4, 24);
				}else if (chlTranSeq.length()>4){
					chlTranSeq = chlTranSeq.substring(4);
				} else {
					chlTranSeq = "";
				}
			} else {
				chlTranSeq = "";
			}
		}
		return chlTranSeq;
	}
	

	/**
	 * 获取应用编码
	 * @param context
	 * @return
	 */
	public static String getMacCode( Context context ) {
		return SFUtil.getSysProperty( "APP_CODE" );
	}

	/**
	 * 获取应用属性
	 * @param context
	 * @return
	 */
	public static String getMacType( Context context ) {
		return SFUtil.getSysProperty( "APP_TYPE" );
	}
	
	
	/**
	 * 获取Param缓存对象Value值
	 * @param context
	 * @param type
	 * @param id
	 * @return
	 */
	public static String getParamValue( Context context,String type ,String id ) {
		String value = null;
		try {
			value = ParamCache.getValue2(type,id);
		} catch (SFException e) {
			SFLogger.error( context, "获取缓存出错！" );
		}
		return value;
	}
	
	/**
	 * 
	 *获取Param缓存对象Value1值
	 * @param context
	 * @return
	 */
	public static String getParamValue1( Context context,String type,String id ) {
		String value1 = null;
		try {
			Param param=ParamCache.getValue(type,id);
			if (param!=null){
				value1 = param.getValue1();
			}
		} catch (SFException e) {
			SFLogger.error( context, "获取缓存出错！" );
		}
		return value1;
	}

	
	/**
	 * 按券商截取错误信息
	 * 直连券商截取100位字符
	 * 深证通券商截取50位字符
	 * 
	 * @param context
	 * @param errMsg
	 * @return
	 */
	public static String returnSecuErrMsg( Context context, String errMsg ) throws SFException {
		try {
			String secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// 券商类型
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "券商类型不能为空[%s]", secuType ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式
				errMsg = SFUtil.getSubString( errMsg, 100 );
			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式
				errMsg = SFUtil.getSubString( errMsg, 50 );
			} else {
				errMsg = SFUtil.getSubString( errMsg, 80 );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		return errMsg;
	}
	
	/**
	 * 返回券商、合作行渠道时错误信息截取60字符
	 * @param context
	 * @param errMsg	错误信息
	 * @return
	 */
	public static String returnErrMsg( Context context, String type, String errMsg) throws SFException {
		try {
			int len = 60;
			if ("SZT".equals(type)||"ZL".equals(type)||"COBANK".equals(type)){
				String value = BizUtil.getParamValue(context, type, "SF_ERRMSG_CUT_OUT");
				if (value!=null){
					len = Integer.parseInt(value);
				}
			}				
			errMsg = SFUtil.getSubString( errMsg, len );
		}  catch( Exception e ) {
			SFLogger.error(context,e);
		}
		return errMsg;
	}
}