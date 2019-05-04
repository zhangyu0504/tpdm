package module.service;

import java.sql.Connection;

import module.bean.Holiday;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.SecCompData;
import module.bean.SecNoServTime;
import module.bean.SecServStatus;
import module.bean.ServStatus;
import module.cache.ProductInfoCache;
import module.dao.HolidayDao;
import module.dao.HolidayDateDao;
import module.dao.SecNoServTimeDao;
import module.dao.ServStatusDao;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

public class SecuService {

	/**
	 * 检查系统状态
	 * 
	 * @param context
	 * @throws SFException
	 * 
	 */
	public static void chkSubCenterStatus( Context context ) throws SFException {
		SFLogger.info( context, "chkSubCenterStatus()开始" );
		SFLogger.info( context, "检查系统状态" );
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
			SFUtil.chkCond( context, !"1".equals( localInfo.getSubCenterStatus() ), "ST5700", "当前时间不允许交易" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSubCenterStatus()结束" );
	}

	/**
	 * 检查当前券商是否允许进行该币种交易
	 * 
	 * @param secComCode
	 *            券商代码
	 * @param productType
	 *            产品种类
	 * @param curCode
	 *            币种
	 */
	public static void chkCurCode( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkCurCode()开始" );
		SFLogger.info( context, "检查当前券商是否允许进行该币种交易" );
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 从上下文取出券商代码
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// 取出币种
		try {
			SFUtil.chkCond( context,  SFUtil.isNotEmpty( curCode ) && !curCode.equals(SFConst.CUR_CODE_RMB) , "ST4074", "该券商不支持此币种业务" );
			ProductInfo productInfo = ProductInfoCache.getValue(secCompCode);// 根据券商代码获取券商产品缓存对象
			SFUtil.chkCond( context, productInfo == null || !"1".equals(productInfo.getPermitFlag() ), "ST4074", "该券商不支持此币种业务" );
			SFLogger.info( context, "chkCurCode()结束" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 检查724状态
	 * 
	 * @param context
	 *            上下文对象
	 * @param secCompCode
	 *            券商代码
	 * @throws SFException
	 * @return 1-开通了增值服务并且是服务时间
	 */
	public static boolean chkSecu724( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, String.format( "chkSecu724()开始" ) );
		SFLogger.info( context, "检查券商724状态" );
		boolean chkFlag = true;
		String secuTxDate = null;// 券商上送交易日期
		String allDayFlag = null;// 增值服务标识
		String adBeginTime = null;// 增值服务开始时间
		String adEndTime = null;// 增值服务结束时间
		String txDate = DateUtil.getMacDate();// 获取日期
		String txTime = DateUtil.getMacTime();// 获取时分秒
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// 营业时间信息
		String workDate = localInfo.getWorkdate();// 交易日期
		String bankDate = localInfo.getBankDate();// 营业日期
		SecNoServTime secNoSerTime = null;
		SecNoServTimeDao secNoServTimeDao = new SecNoServTimeDao();
		String secCompCode = null;
		try {
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "此券商信息不存在" ) );
			secCompCode = secCompData.getSecCompCode();
			allDayFlag = secCompData.getAllDayFlag();
			adBeginTime = secCompData.getADBeginTime();
			adEndTime = secCompData.getADENDTIME();

			if( SFUtil.isNotEmpty( allDayFlag ) && !"0".equals( allDayFlag ) ) {
				if( "1".equals( allDayFlag ) ) {// 表明开通了7*24小时
					secNoSerTime = secNoServTimeDao.qrySecNoServTimeByBetweenTime( context, connection, secCompCode, txDate, txTime, txTime );
					if( secNoSerTime != null ) {
						chkFlag = false;
					}

					// 修订724券商在存管切日但券商未切日情况下导致两边系统时间不一致银证交易未校验时间一致性 edit by lch 20180525 11:00 start
					String secuType = secCompData.getSztFlag();
					if( SFUtil.isNotEmpty( secuType ) && SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式
						secuTxDate = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TXDATE" );// ZL券商交易日期
					} else if( SFUtil.isNotEmpty( secuType ) && SFConst.SECU_SZT.equals( secuType ) ) {
						secuTxDate = SFUtil.getDataValue( context, "MsgHdr.Date" );// SZT券商交易日期
					}
					if( SFUtil.isNotEmpty( secuTxDate ) && !secuTxDate.equals( workDate ) ) {
						chkFlag = false;
					}
					// 修订724券商在存管切日但券商未切日情况下导致两边系统时间不一致银证交易未校验时间一致性 edit by lch 20180525 11:00 end

				} else if( "2".equals( allDayFlag ) ) {// 表明开通了7*8
					if( txDate.equals( bankDate ) && workDate.equals( bankDate ) ) {
						chkFlag = false;
					}
					if( !txDate.equals( bankDate ) && !workDate.equals( bankDate ) ) {/* 节假日 */
						if( txTime.compareTo( adBeginTime ) < 0 && txTime.compareTo( adEndTime ) > 0 ) {
							chkFlag = false;
						}
						secNoSerTime = secNoServTimeDao.qrySecNoServTimeByBetweenTime( context, connection, secCompCode, txDate, txTime, txTime );
						if( secNoSerTime != null ) {
							chkFlag = false;
						}
					}
				}
			} else {// 未开通增值业务
				chkFlag = false;
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "chkSecu724()结束" );
		return chkFlag;
	}

	/**
	 * 检查券商假日无忧标志
	 * 
	 * @param context
	 * @param secCompCode
	 * @return 0：正常三方存管交易日 ;1：假日无忧
	 */
	public static boolean chkSecuHoliday( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuHoliday()开始" );
		SFLogger.info( context, "检查券商假日无忧标识" );
		boolean chkFlag = false;
		String holidayFlag = null;
		String startTime = null;
		String endTime = null;// 初始化假日无忧标志等字段
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		String txTime = DateUtil.getMacTime();
		String tranDate = DateUtil.getMacDate();
		Holiday holiday = new HolidayDao().qryHoliday( context, connection,tranDate );
		try {
			String secCompCode = null;
			if( secCompData != null ) {
				secCompCode = secCompData.getSecCompCode();// 获取券商代码
				holidayFlag = secCompData.getHolidayFlag();
				startTime = secCompData.getStartTime();
				endTime = secCompData.getEndTime();
			}

			if( "1".equals( holidayFlag ) && holiday != null && "1".equals(holiday.getHoliDayFlag())) {// 有开通假日无忧判断是否在服务时间内
				if( ( txTime.compareTo( endTime ) < 0 && txTime.compareTo( startTime ) > 0 ) && new HolidayDateDao().qryHolidayData( context, connection, secCompCode, tranDate ) == null ) {
					chkFlag = true;
				}
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuHoliday()结束" );
		return chkFlag;
	}

	/**
	 * 检查服务停启状态
	 * 
	 * @param context
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static boolean chkSecuStatus( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuStatus()开始" );
		SFLogger.info( context, "检查服务停启状态" );
		boolean chkFlag = true;
		try {
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
			ServStatus servStatus = new ServStatusDao().qryServStatus( context, connection, localInfo.getBankDate() );
			if( servStatus != null ) {
				chkFlag = false;
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuStatus()结束" );
		return chkFlag;
	}

	/**
	 * 检查账户类服务标志
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuAcctFlag( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "chkSecuAcctFlag()开始" );
		SFLogger.info( context, "检查账户类服务标志" );
		try {
			SecServStatus secSerStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secSerStatus == null || "0".equals( secSerStatus.getAcctServFlag() ), "ST4371", "未开启账户类服务" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "chkSecuAcctFlag()结束" );
	}

	/**
	 * 检查单户结息服务标志
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuAccrualFlag( Context context ) throws SFException {
		SFLogger.info( context, "chkAccrualFlag()开始" );
		SFLogger.info( context, "检查单户结息服务标志" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getIntServFlag() ), "ST4371", "未开启单户结息服务" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkAccrualFlag()结束" );
	}

	/**
	 * 检查是否允许银行发起银转证
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuBankChlB2S( Context context ) throws SFException {
		SFLogger.info( context, "chkBankChlB2S()开始" );
		SFLogger.info( context, "检查是否允许银行发起银转证" );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		SFUtil.chkCond( context, secCompData == null, "ST5705", "券商代码为空" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag1() ), "ST4371", "不允许银行发起银转证" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkBankChlB2S()结束" );

	}

	/**
	 * 检查是否允许银行发起证转银
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuBankChlS2B( Context context ) throws SFException {
		SFLogger.info( context, "chkBankChlS2B()开始" );
		SFLogger.info( context, "检查是否允许银行发起证转银" );
		SecCompData secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		SFUtil.chkCond( context, secCompData == null, "ST5705", "券商代码为空" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag2() ), "ST4371", "不允许银行发起证转银" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkBankChlS2B()结束" );

	}

	/**
	 * 检查是否允许券商发起银转证
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuChlB2S( Context context ) throws SFException {
		SFLogger.info( context, "chkSecuChlB2S()开始" );
		SFLogger.info( context, "检查是否允许券商发起银转证" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag3() ), "ST4371", "不允许券商发起银转证" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuChlB2S()结束" );

	}

	/**
	 * 检查是否允许券商发起证转银
	 * 
	 * @param context
	 * @param secCompCode
	 *            券商代码
	 * @return 0-停止 1-启动
	 * @throws SFException
	 */
	public static void chkSecuChlS2B( Context context ) throws SFException {
		SFLogger.info( context, "chkSecuChlS2B()开始" );
		SFLogger.info( context, "检查是否允许券商发起证转银" );
		try {
			SecServStatus secServStatus = SFUtil.getDataValue( context, SFConst.PUBLIC_SEC_SERV_STATUS );
			SFUtil.chkCond( context, secServStatus == null || "0".equals( secServStatus.getTransServFlag4() ), "ST4371", "不允许券商发起证转银" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSecuChlS2B()结束" );
	}

}
