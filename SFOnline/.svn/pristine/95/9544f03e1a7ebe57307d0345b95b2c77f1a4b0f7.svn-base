package module.trans.secu2sf;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.BankCodeInfo;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.ProductInfoCache;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.dao.BankCodeInfoDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.ReverseClient;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 
 * (券商端发起交易)证转银冲正
 *	交易码 : 100203
 *
 *	证转银冲正流程:
 *
 *				1.接收券商交易 （<深>Trf.003.01/12004，<直>6044/612205）
 *				2.检查请求包有效性,检查券商和客户资料,检查原交易是否存在和状态等
 *				3.合作行客户：不支持冲正      我行客户：调用G1010转账（客户帐到券商汇总账户）
 *				4.返回券商 （<深>Trf.004.01/12004，<直>6044/612205）
 *
 * tran code :100203
 * @author 吕超鸿
 */
public class T100203 extends TranBase {

	private String subTxSeqId = null;// 16位服务平台流水号

	private String initSeqId = null;// 14位日志号(前置流水号)

	private String secSeqId = null;// 券商流水号

	private String txSeqId = null; // 8位发起方流水号

	private String chlSeqId = null; // 22位交易流水号

	private String tranDate = null;// 交易日期

	private String secCompCode = null;// 券商代码

	private String capAcct = null;// 资金帐号

	private String invName = null;// 客户名称

	private String curCode = null;// 币种

	private String acctId = null;// 银行帐号

	private String invType = null;// 客户类型

	private String invIdCode = null;// 证件ID

	private String cashRemitFlag = null;// 钞汇标识

	private String orgTxSeqId = null;// 原发起方流水号

	private String signFlag = null;// 签约标识

	private String secutype = null;// 券商类型

	private String secAcct = null;// 保证金管理帐号

	private BigDecimal txAmount = new BigDecimal( 0.00 );// 交易金额

	private boolean succResFlag = false;// 正确流程特殊返回标识

	private SignAccountData signAccountData = null;// 签约信息

	private AcctJour oldAcctJour = null;// 旧流水信息

	private SecCompData secCompData = null;// 券商信息

	private AcctJourDao acctJourDao = new AcctJourDao();

	private DecimalFormat df = new DecimalFormat( "#0.00" );// 数字格式化，保留两位小数
	
	@Override
	protected void initialize( Context context ) throws SFException {
		try {

			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14位前置流水号
			subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16位服务平台流水号
			chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // 获取22位流水号;

			KeyedCollection secCompCodekColl = SFUtil.getDataElement( context, "Trf00301" );
			KeyedCollection scAcctKcoll = null;
			if( null != secCompCodekColl ) {
				scAcctKcoll = SFUtil.getDataElement( context, secCompCodekColl, "ScAcct" );
			}

			// 获取券商代码
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				if( null != acctSvcrKcoll ) {
					secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// 券商编号
				}
			}
			// 获取资金帐号
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
			if( SFUtil.isEmpty( capAcct ) ) {
				capAcct = SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" );// 券商编号
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5701", String.format( "券商代码不存在" ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( capAcct ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );

			SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "券商信息不存在" ) );

			signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "签约信息不存在" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// 将券商类型放入上下文中
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData );
			SFUtil.setDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA, signAccountData );// 将签约信息型放入上下文中

			// 券商类型
			secutype = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secutype ), "ST5701", String.format( "券商类型不能为空" ) );

			// 深证通模式
			if( SFConst.SECU_SZT.equals( secutype ) ) {
				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				tranDate = SFUtil.getDataValue( context, msgHdrKcoll, "Date" );// 交易日期

				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00301" );
				curCode = SFUtil.getDataValue( context, kColl, "CUR_CODE" );// 币种
				txAmount = new BigDecimal( SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "TX_AMOUNT" ) ) );// 金额

				KeyedCollection BkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				acctId = SFUtil.getDataValue( context, BkAcctKcoll, "ACCT_ID" );

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				String idType = SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" );
				invType = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );

				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// 券商流水号

				KeyedCollection cnRefKcoll = SFUtil.getDataElement( context, kColl, "CnRef" );
				// 原交易流水号
				orgTxSeqId = BizUtil.getTxSeqId( SFUtil.objectToString( SFUtil.getDataValue( context, cnRefKcoll, "ORG_TX_SEQ_ID" ) ).trim() );

				// 转换客户类型为 1：个人 2：机构
				invType = BizUtil.convSZT2SFInvType( context, invType );

				// 设置默认币种
				curCode = SFUtil.isEmpty( curCode ) ? SFConst.CUR_CODE_RMB : curCode;// 币种

				// 组装券商通用上下文
				KeyedCollection keyColl = new KeyedCollection( "100203_I" );
				SFUtil.addDataField( context, keyColl, "ACCT_ID", acctId );// 发展卡卡号/对公账号
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", capAcct );// 券商代码
				SFUtil.addDataField( context, keyColl, "CUR_CODE", curCode );// 交易币种
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// 证件类型
				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", txAmount );// 交易金额
				SFUtil.addDataField( context, keyColl, "TX_DATE", tranDate );// 交易日期
				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// 客户类型
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// 客户名称
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );// 客户类型
				SFUtil.addDataElement( context, keyColl );

				// 直联模式
			} else {
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// 生成发（券商）起方流水号
				orgTxSeqId = BizUtil.getTxSeqId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SUB_TX_SEQ_ID" ) ) );// 原交易流水号
				txAmount = new BigDecimal( AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) )  );// 直联金额不带小数点
				cashRemitFlag = SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" );// 钞汇标识
				curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// 币种
				capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// 资金帐号
				secAcct = SFUtil.getReqDataValue( context, "SEC_ACCT" );// 保证金管理账号
				secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码
				invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 客户名称
				invType = SFUtil.getReqDataValue( context, "INV_TYPE" );// 客户类型
				acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// 银行帐号

				// 组装券商通用上下文
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100203_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );// 证件类型
			}

			// 如果钞汇标识为空,则默认为钞
			cashRemitFlag = SFUtil.isEmpty( cashRemitFlag ) ? SFConst.CASH_FLAG : cashRemitFlag;
			SFUtil.chkCond( context, ( !SFConst.CASH_FLAG.equals( cashRemitFlag ) && !SFConst.REMIT_FLAG.equals( cashRemitFlag ) ), "ST5717", String.format( "钞汇标识非法[%s]", cashRemitFlag ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// 已经冲正成功直接返回券商成功
		if( succResFlag ) {
			return;
		}

		// 将此笔流水置为冲正流水
		updAcctJourFlag( context, tranConnection );

		// 上主机证转银冲正
		doHost( context );

		// 发券商
		doSecu( context );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()开始" );

		try {
			// 我行客户冲正，合作行客户不支持冲正
			SFUtil.chkCond( context, SFConst.INIT_SIDE_COBANK.equals( signAccountData.getInitSide() ), "ST5753", String.format( "合作行客户不支持证转银冲正" ) );

			SFLogger.info( context, "发送G1010证转银冲正交易开始" );

			/**
			 * 联机通用冲销:G1010
			 */
			doHostReverse( context );

			SFLogger.info( context, "发送G1010证转银冲正交易结束" );

			SFLogger.info( context, "券商端发起证转银冲正纪录数据库开始" );

			/**
			 * 上主机后银转证冲正数据库更新操作
			 */
			updPublicInfo( context );

			SFLogger.info( context, "券商端发起证转银冲正纪录数据库结束" );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 
	 * 流水置为冲正流水
	 * @param context
	 * @param connection
	 * @throws SFException
	 */
	private void updAcctJourFlag( Context context, Connection connection ) throws SFException {
		try {
			DBHandler.beginTransaction( context, tranConnection );// 开启事务

			// 置冲正流水jourFlag = "30"
			acctJourDao.updAcctJourByReverseJourFlag( context, tranConnection, txSeqId, "30", tranDate, secCompData.getUserId(), orgTxSeqId );

			DBHandler.commitTransaction( context, tranConnection );// 提交事务

		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	/**
	 * 
	 * 我行客户上主机 G1010 冲正
	 * @param context
	 * @throws SFException
	 */
	private void doHostReverse( Context context ) throws SFException {
		SFLogger.info( context, "doHostReverse()开始" );

		Map<String, Object> msg = null;
		String retCode = null;
		String retMsg = null;
		String retFlag = null;

		try {

			// 联机通用冲销:G1010 构建请求报文
			msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msg.put( "OLD_BUSS_SEQ_NO", oldAcctJour.getBusiSeqId() );// 原业务流水号OLD_BUSS_SEQ_NO
			msg.put( "OLD_TRAN_SEQ_NO", oldAcctJour.getTranSeqId() );// 原交易流水号OLD_TRAN_SEQ_NO
			msg.put( "REASON", "证转银冲正" );// 原因REASON
			msg.put( "FLAG", "0" );

			ReverseClient reverseClient = new ReverseClient();
			Context msgContext = reverseClient.send( context, msg );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// 返回码
			retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息

			SFUtil.addDataField( context, SFConst.PUBLIC_RET_FLAG, retFlag );
			SFUtil.addDataField( context, "RESP_CODE", retCode );
			SFUtil.addDataField( context, "RESP_MSG", retMsg );

			SFLogger.info( context, String.format( "发送主机冲正交易结束，返回结果：ret[%s],respCode[%s],respMsg[%s]", retFlag, retCode, retMsg ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHostReverse()结束" );
	}

	/**
	 * 
	 * 上主机后更新数据库操作
	 * @param context
	 * @throws SFException
	 */
	public void updPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "updPublicInfo() 开始" );
		String retCode = null;
		String retMsg = null;
		String retFlag = null;

		try {
			// 重新获取返回码
			retFlag = SFUtil.getDataValue( context, SFConst.PUBLIC_RET_FLAG );

			// 重新获取响应码&响应信息
			retCode = SFUtil.getDataValue( context, "RESP_CODE" );
			retMsg = SFUtil.getDataValue( context, "RESP_MSG" );

			DBHandler.beginTransaction( context, tranConnection );// 开启事务

			SFUtil.chkCond( context, SFConst.RET_OVERTIME.equals( retFlag ), "ST5699", String.format( "券商证转银冲正交易主机超时或异常" ) );

			// 上D+冲正成功
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {

				// 更新 TRDAcctJour 冲正成功标志
				oldAcctJour.setJourFlag( "44" );
				oldAcctJour.setRespCode( "000000" );
				oldAcctJour.setRespMsg( "证转银冲正成功" );
				acctJourDao.saveAcctJour( context, tranConnection, oldAcctJour );

				// 更新 TRDAcctDetail 冲正成功标志
				updAcctDetail( context, "44" );

				// 更新TRDSignAccountData
				updSignAccountData( context, tranConnection );

			} else {
				// 冲正异常，失败处理
				BankCodeInfo bankCodeInfo = new BankCodeInfoDao().qryBankCodeInfo( context, tranConnection, retCode );
				if( null != bankCodeInfo ) {
					retMsg = bankCodeInfo.getMsg();
				} else {
					SFLogger.info( context, "数据库中未保存此主机错误码,请增加" );
				}
				oldAcctJour.setJourFlag( "00" );
				oldAcctJour.setRespCode( retCode );
				oldAcctJour.setRespMsg( "证转银冲正失败:" + retMsg );
				acctJourDao.saveAcctJour( context, tranConnection, oldAcctJour );

			}
			DBHandler.commitTransaction( context, tranConnection );// 开始事务

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "updPublicInfo() 结束" );
	}

	/**
	 * 
	 *
	 * 更新账户管理交易明细
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void updAcctDetail( Context context, String jourFlag ) throws SFException {
		SFLogger.info( context, "更新转账交易明细开始" );
		try {
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			acctDetailDao.updAcctDetailByReverseJourFlag( context, tranConnection, jourFlag, tranDate, secCompData.getUserId(), orgTxSeqId );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", String.format( "updAcctAdmDetail()失败%s", e.getMessage() ) );
		}
		SFLogger.info( context, "更新转账交易明细结束" );
	}

	/**
	 * 
	 * 更新签约表中余额
	 * @param context
	 * @param connection
	 * @param acctBal
	 * @throws SFException
	 */
	private void updSignAccountData( Context context, Connection connection ) throws SFException {
		SFLogger.info( context, "updSignAccountData()开始" );

		try {
			// 查询TRDSignAccountData锁定记录
			SignAccountData signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5800", "客户签约账号信息锁表失败!" );

			// 更新签约信息表
			signAccountDataDao.updSignAccountData( context, tranConnection, secCompCode, capAcct, txAmount );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, String.format( "UpdateBalFailure[%s]",e.getMessage() ) );
			SFUtil.chkCond( context, "ST4895", "更新客户签约账户余额失败" );
		}
		SFLogger.info( context, "updSignAccountData()结束" );
	}

	/**
	 * 发送券商
	 */
	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()开始" );
		try {
			if( SFConst.SECU_SZT.equals( secutype ) ) {// 深证通

				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00401" );

				KeyedCollection refKcoll = SFUtil.getDataElement( context, kColl, "Ref" );
				SFUtil.setDataValue( context, refKcoll, "SUB_TX_SEQ_ID", subTxSeqId );

				KeyedCollection rltdRefKcoll = SFUtil.getDataElement( context, kColl, "RltdRef" );
				SFUtil.setDataValue( context, rltdRefKcoll, "SEC_SEQ_ID", secSeqId );

				KeyedCollection cnRefKcoll = SFUtil.getDataElement( context, kColl, "CnRef" );
				SFUtil.setDataValue( context, cnRefKcoll, "ORG_TX_SEQ_ID", orgTxSeqId );

				KeyedCollection scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, scAcctKcoll, "CAP_ACCT", capAcct );

				KeyedCollection pwdKcoll = SFUtil.getDataElement( context, scAcctKcoll, "Pwd" );
				SFUtil.setDataValue( context, pwdKcoll, "CAP_ACCT_PWD", "" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );

			} else if( SFConst.SECU_ZL.equals( secutype ) ) {

				SFUtil.setResDataValue( context, "RESP_CODE", "SF0000" );
				SFUtil.setResDataValue( context, "RESP_MSG", "券商端发起证转银冲正成功" );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", oldAcctJour != null ? ( oldAcctJour.getSubTxSeqId() != null ? oldAcctJour.getSubTxSeqId() : "" ) : "" );
				SFUtil.setResDataValue( context, "ACCT_ID", acctId );
				SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", cashRemitFlag );
				SFUtil.setResDataValue( context, "TX_AMOUNT", SFUtil.objectToString( AmtUtil.conv2SecuMulAmount( context, txAmount ) ) );
				SFUtil.setResDataValue( context, "NEW_SUB_TX_SEQ_ID", subTxSeqId );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doSecu()结束" );
	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		try {
			// 转换前客户证件类型
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" );
			invName = SFUtil.getReqDataValue( context, "INV_NAME" );
			invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// 生成发起方流水号

			// 先预定义返回报文-交易异常退出时返回
			if( SFConst.SECU_ZL.equals( secutype ) ) {// 直联模式
				SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", cashRemitFlag );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );
				SFUtil.setResDataValue( context, "ACCT_ID", acctId );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "TX_AMOUNT", String.valueOf( AmtUtil.conv2SecuMulAmount( context, txAmount ) ) );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", orgTxSeqId );
				SFUtil.setResDataValue( context, "NEW_SUB_TX_SEQ_ID", subTxSeqId );
			}
			// 深证通模式
			if( SFConst.SECU_SZT.equals( secutype ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00401" );

				KeyedCollection refKcoll = SFUtil.getDataElement( context, kColl, "Ref" );
				SFUtil.setDataValue( context, refKcoll, "SUB_TX_SEQ_ID", subTxSeqId );

				KeyedCollection rltdRefKcoll = SFUtil.getDataElement( context, kColl, "RltdRef" );
				SFUtil.setDataValue( context, rltdRefKcoll, "SEC_SEQ_ID", secSeqId );

				KeyedCollection cnRefKcoll = SFUtil.getDataElement( context, kColl, "CnRef" );
				SFUtil.setDataValue( context, cnRefKcoll, "ORG_TX_SEQ_ID", orgTxSeqId );

				KeyedCollection scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, scAcctKcoll, "CAP_ACCT", capAcct );

				KeyedCollection pwdKcoll = SFUtil.getDataElement( context, scAcctKcoll, "Pwd" );
				SFUtil.setDataValue( context, pwdKcoll, "CAP_ACCT_PWD", "" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );
			}

			// 检查关键字段是否为空
			SFUtil.chkCond( context, ( SFUtil.isEmpty( orgTxSeqId ) ), "ST4479", String.format( "冲正原交易流水不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "券商编号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "证件类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "证件号码不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "投资者名称不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "客户类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "币别不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "券商流水号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( acctId ) ), "ST4092", String.format( "账号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.object2Double( txAmount ) <= 0 ), "ST4479", String.format( "交易金额不能小于等于零" ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {

			secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );// 券商信息
			LocalInfo localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );// 营业时间信息
			tranDate = localInfo.getWorkdate();// 交易日期
			
			// 五矿、银河证券Rever节点需要回送brchId机构号，先添加到context中
			SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " " );

			/**
			 * 检查签约关系
			 */
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "签约信息不存在" ) );
			signFlag = signAccountData.getSignFlag();
			SFUtil.chkCond( context, ( SFUtil.isEmpty( signFlag ) ), "ST5720", String.format( "签约信息不存在" ) );
			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "签约信息不符" ) );
			SFUtil.chkCond( context, ( !SFConst.SIGN_FLAG_SIGN.equals( signFlag ) ), "ST5590", String.format( "当前签约状态不允许办理查询交易" ) );

			/**
			 * 检查当前券商是否允许办理该币种交易 
			 */
			ProductInfo productInfo = ProductInfoCache.getValue( secCompCode );// 根据券商代码获取券商产品缓存对象
			SFUtil.chkCond( context, productInfo == null, "ST4074", String.format( "该券商未开通此币种的业务" ) );
			SFUtil.chkCond( context, ( !"1".equals( productInfo.getPermitFlag() ) ), "ST4074", String.format( "不允许此券商办理该币种转账业务" ) );

			/**
			 * 检查原流水数据
			 */
			SFLogger.info( context, "检查原流水数据开始" );
			oldAcctJour = acctJourDao.qryAcctJour( context, tranConnection, tranDate, secCompData.getUserId(), orgTxSeqId );
			// 若没找到原交易,返回券商冲正成功;若原流水失败,则返回券商冲正成功
			if( null == oldAcctJour || ( null != oldAcctJour && "2".equals( String.valueOf( oldAcctJour.getJourFlag().charAt( 0 ) ) ) ) ) {
				succResFlag = true;
				doSecu( context );// 组包返回券商
				return;
			}
			if( !"1004".equals( oldAcctJour.getAbst() ) && !"2004".equals( oldAcctJour.getAbst() ) ) {
				SFUtil.chkCond( context, "ST5753", String.format( "该流水转帐类型不能被冲正" ) );
			}
			if( !curCode.equals( oldAcctJour.getCurCode() ) ) {
				SFUtil.chkCond( context, "ST4428", String.format( "冲正币种与原交易不符" ) );
			}
			if( Math.abs( SFUtil.sub( oldAcctJour.getTxAmount(), txAmount ) ) > 0.000001 ) {
				SFUtil.chkCond( context, "ST4483", String.format( "冲正资金与原交易不符" ) );
			}
			if( "44".equals( oldAcctJour.getJourFlag() ) ) {
				SFUtil.chkCond( context, "ST4502", String.format( "原流水已冲正(冲正交易)" ) );
			}
			if( !"00".equals( oldAcctJour.getJourFlag() ) ) {
				SFUtil.chkCond( context, "ST5752", String.format( "原交易失败或异常中,不允许冲正" ) );
			}
			if( SFUtil.isNotEmpty( oldAcctJour.getRevTxSeqId() ) ) {
				SFUtil.chkCond( context, "ST4502", String.format( "原流水已冲正(冲正交易)" ) );
			}
			if( SFUtil.isEmpty( oldAcctJour.getPreSeqId() ) ) {
				SFUtil.chkCond( context, "ST5753", String.format( "此笔交易的原前置流水号为空,无法进行冲正" ) );
			}
			SFLogger.info( context, "检查原流水数据结束" );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}
