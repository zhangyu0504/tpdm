package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtCustomerInfo;
import module.bean.AutoBecif;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AutoBecifDao;
import module.trans.TranBase;
import module.trans.sf2cobank.T810021Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;
import common.util.SpecialSecuUtil;

import core.log.SFLogger;

/**
 * A股券商端发起--联机金融类交易
 * 
 * 处理逻辑：
 * 			1.接收券商交易 （<深>Acmt.003.01/11004，<直>6023/612202）
 *			2.检查请求包有效性,检查券商和客户资料、余额等
 *			3.代理客户：调用612321上代理系统撤销签约交易
 *			4.对于我行个人客户，调用D+接口R3042置卡状态字
 *			5.对于我行对公客户，调用D+接口C3048置卡状态字
 *			6.返回券商       （<深>Acmt.004.01/11004，<直>6023/612202）
 * 
 * 撤销签约关系
 * tran code :100101
 * @author 吕超鸿
 *
 */
public class T100101 extends TranBase {

	private String subTxSeqId = null;// 16位服务平台流水号

	private String initSeqId = null;// 14位日志号(前置流水号)

	private String secSeqId = null;// 券商流水号

	private String txSeqId = null; // 8位发起方流水号

	private String chlSeqId = null; // 22位交易流水号

	private String txDate = null;// 券商请求包中的日期

	private String txTime = null;// 交易时间

	private String initSide = null;// 渠道

	private String userId = null;// 客户编号

	private String curCode = null;// 币种

	private String invName = null;// 客户姓名

	private String invIdCode = null;// 客户证件号码

	private String invType = null;// 客户类型

	private String acctId = null;// 银行卡号

	private String capAcct = null;// 资金帐号

	private String secCompCode = null;// 券商编号

	private String hostIdType = null;// 主机类型

	private int secAcctSeq = 0;// 保证金管理账号序列号

	private String secuType = null;// 券商类型，区分直联或深证通

	private String bankId = null;// 合作行号

	private boolean isSuccRetFlag = false;// 标识已撤销签约成功返回

	private DecimalFormat df = new DecimalFormat( "#0.00" );// 数字格式化，保留两位小数

	private SecCompData secu = null;// 券商信息

	private SignAccountData signAccountData = null;// 签约信息

	private AcctJourDao acctJourDao = new AcctJourDao();

	@Override
	protected void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {
			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14位前置流水号
			subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16位服务平台流水号
			chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // 获取22位流水号;

			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Acmt00301" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// 券商编号
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "此券商信息不存在" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "此券商信息不存在" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// 将券商类型放入上下文中
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			// 券商类型
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "券商类型不能为空[%s]", secuType ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式
				String unitTellerId = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TELLERID" );// 操作员编号
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// 券商流水号

				// 组装券商通用上下文
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100101_I" );
				// 撤销时余额
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
				SFUtil.addDataField( context, keyColl, "UNIT_TELLER_ID", unitTellerId );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection senderKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Sender" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// 券商流水号

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				String invType = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// 客户类型

				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );

				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				String beginBal = ( null != SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) : "0.00";

				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );

				// idType_SZT 转换客户类型为 1：个人 2：机构
				invType = BizUtil.convSZT2SFInvType( context, invType );

				// 客户类型默认为1：个人
				invType = SFUtil.isEmpty( invType ) ? SFConst.INV_TYPE_RETAIL : invType;

				// 组装券商通用上下文
				KeyedCollection keyColl = new KeyedCollection( "100101_I" );

				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// 客户类型
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", df.format( new BigDecimal( beginBal ) ) );// 结息金额
				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// 币种
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// 客户名称
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" ) );// 证件类型
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );// 客户类型
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// 发展卡卡号/对公账号
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// 券商端资金台账号
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// 券商流水号
				SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.getDataValue( context, senderKcoll, "BrchId" ) );// 分支机构标识

				SFUtil.addDataElement( context, keyColl );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// 已撤销签约的直接返回券商成功，不走后续撤销流程
		if( isSuccRetFlag ) {
			doSecu( context );
			return;
		}
		// 记录交易流水,写入账户管理交易明细,更新签约账户信息,更新流水状态标识，成功签约的数据插入到表TRDAUTOBECIF
		addPublicInfo( context );

		// 若AcctId为空,则为只是券商预指定,不须上主机撤销指定,否则撤销成功,则上卡管取消卡状态字
		if( SFUtil.isNotEmpty( acctId ) ) {
			doHost( context );
		}

		// 组包返回券商
		doSecu( context );

		// 将成功签约的数据插入到表TRDAUTOBECIF中，后续轮询发协议到BECIF
		if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
			addAutoBecif( context );
		}
	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()开始" );

		try {

			DBHandler.beginTransaction( context, tranConnection );// 开启事务 1

			// 记录交易流水
			addAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // 提交事务1

			DBHandler.beginTransaction( context, tranConnection ); // 开启事务 2

			// 写入账户管理交易明细
			addAcctAdmDetail( context );

			// 更新签约信息
			updSignAccountData( context );

			// 更改流水状态标志
			updAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // 提交事务2

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()结束" );
	}

	/**
	 * 记录交易流水
	 */
	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()开始" );
		try {

			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setInvType( invType );
			acctJour.setInvName( invName );
			acctJour.setIdType( hostIdType );
			acctJour.setInvIdCode( invIdCode );
			acctJour.setSecAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
			acctJour.setSecAcctSeq( 0 );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setOpenDepId( signAccountData.getOpenDepId() );
			acctJour.setOpenBranchId( signAccountData.getOpenDepId() );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.DEBIT_FLAG );
			acctJour.setTxAmount( new BigDecimal( 0.00 ) );
			acctJour.setAcctBal( new BigDecimal( 0.00 ) );
			acctJour.setAbst( "" );
			acctJour.setAbstractStr( "券商端撤销签约" );
			acctJour.setJourFlag( "33" );
			acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_DEL_SIGN );
			acctJour.setBusiType( SFConst.BUSI_TYPE_DEL_SIGN );
			acctJour.setTxTime( txTime );
			acctJour.setDepId( signAccountData.getDepId() );
			acctJour.setBranchId( signAccountData.getBranchId() );
			acctJour.setUnitTellerId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "UNIT_TELLER_ID" ) ) );
			acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
			acctJour.setAcctDealId( initSeqId );// 取14位日志号
			acctJour.setProductType( "03" );
			acctJour.setColFlag( "0" );
			acctJour.setMemo( "" );
			acctJour.setTranSeqId( chlSeqId );
			acctJour.setBusiSeqId( chlSeqId );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入交易流水失败" );
		}
		SFLogger.info( context, "addAcctJour()结束" );
	}

	private void updAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "updAcctJour()开始" );
		try {

			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setJourFlag( "00" );
			acctJour.setSavAcct( signAccountData.getSavAcct() );
			acctJour.setDepId( signAccountData.getOpenDepId() );
			acctJour.setBranchId( signAccountData.getOpenDepId() );
			acctJour.setOpenDepId( signAccountData.getOpenDepId() );
			acctJour.setOpenBranchId( signAccountData.getOpenDepId() );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新交易流水失败" );
		}
		SFLogger.info( context, "updAcctJour()结束" );
	}

	/**
	 *	更新签约账户余额表的余额
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public void updSignAccountData( Context context ) throws SFException {
		SFLogger.info( context, "updSignAccountData()开始" );
		try {

			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setProductType( "03" );
			signAccountData.setDesDepId( signAccountData.getDepId() );
			signAccountData.setDesBranchId( signAccountData.getBranchId() );
			signAccountData.setCloseDate( txDate );
			signAccountData.setSignFlag( SFConst.SIGN_FLAG_CANCEL );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新签约账户余额表的余额失败" );
		}
		SFLogger.info( context, "updSignAccountData()结束" );
	}

	/**
	 * 
	 * 写交易明细
	 * @param context
	 * @return
	 * @throws SFException
	 */
	public void addAcctAdmDetail( Context context ) throws SFException {
		SFLogger.info( context, "addAcctAdmDetail()开始" );
		try {

			AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
			acctAdmDetail.setTxDate( txDate );
			acctAdmDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctAdmDetail.setUserId( userId );
			acctAdmDetail.setTxSeqId( txSeqId );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetail.setInvType( invType );
			acctAdmDetail.setInvName( invName );
			acctAdmDetail.setIdType( hostIdType );
			acctAdmDetail.setInvIdCode( invIdCode );
			acctAdmDetail.setSecAcct( SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
			acctAdmDetail.setSecAcctSeq( secAcctSeq );
			acctAdmDetail.setSecCompCode( secCompCode );
			acctAdmDetail.setCapAcct( capAcct );
			acctAdmDetail.setAcctId( acctId );
			acctAdmDetail.setOpenDepId( signAccountData.getOpenDepId() );
			acctAdmDetail.setOpenBranchId( signAccountData.getOpenBranchId() );
			acctAdmDetail.setOldAcctId( acctId );
			acctAdmDetail.setCurCode( curCode );
			acctAdmDetail.setDcFlag( SFConst.DEBIT_FLAG );
			acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
			acctAdmDetail.setAbStract( " " );
			acctAdmDetail.setAbstractStr( "券商端撤销签约" );
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setSignFlag( signAccountData.getSignFlag() );
			acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_CANCEL );
			acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_DEL_SIGN );
			acctAdmDetail.setBusiType( SFConst.BUSI_TYPE_DEL_SIGN );
			acctAdmDetail.setTxTime( txTime );
			acctAdmDetail.setDepId( signAccountData.getOpenDepId() );
			acctAdmDetail.setBranchId( signAccountData.getOpenBranchId() );
			acctAdmDetail.setUnitTellerId( SFUtil.objectToString( SFUtil.getReqDataValue( context, "UNIT_TELLER_ID" ) ) );
			acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
			acctAdmDetail.setCusMagNo( SFUtil.objectToString( SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) ) );
			acctAdmDetail.setAcctDealId( initSeqId );// 14位流水号
			acctAdmDetail.setColFlag( "0" );
			acctAdmDetail.setMemo( "" );

			AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入账户管理明细表失败" );
		}
		SFLogger.info( context, "addAcctAdmDetail()结束" );
	}

	/**
	 * 更新合作行客户信息
	 * @param context
	 * @throws SFException
	 */
	public void doCoBankCancleSign( Context context ) throws SFException {
		SFLogger.info( context, "doCoBankCancleSign()开始" );
		try {
			// 合作行客户检查
			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			AgtCustomerInfo agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoByAcctNo( context, tranConnection, capAcct, secCompCode, acctId );
			if( null == agtCustomerInfo ) {
				SFLogger.info( context, String.format( "不存在该合作行客户" ) );
				return;
			}

			bankId = agtCustomerInfo.getBankId();
			String memo = agtCustomerInfo.getMemo();
			if( SFUtil.isNotEmpty( memo ) && memo.trim().length() > 1007 ) {
				memo = memo.substring( 1007 ) + "-D:" + DateUtil.getMacDateTimeShort();
			} else {
				memo = memo + "-D:" + DateUtil.getMacDateTimeShort();
			}
			SFLogger.info( context, String.format( "合作行号bankId [%s],备注memo [%s]", bankId, memo ) );
			DBHandler.beginTransaction( context, tranConnection ); // 开启事务
			agtCustomerInfoDao.updAgtCustomerInfoByAcctNo( context, tranConnection, capAcct, secCompCode, acctId, "4", memo );
			DBHandler.commitTransaction( context, tranConnection ); // 提交事务

			// 构建请求报文
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "SEC_COMP_CODE", secCompCode );// 券商代码SecCode
			msg.put( "CAP_ACCT", capAcct );// 证券资金台账号CapAcct
			msg.put( "INV_NAME", invName );// 客户名称InvName
			msg.put( "ID_TYPE", hostIdType );// 发给代理的其实就是直联的证件类型
			msg.put( "INV_ID_CODE", invIdCode );// 证件号码InvIdCode
			msg.put( "ACCT_ID", acctId );// 银行账号account_no
			msg.put( "CUR_CODE", curCode );// 币种CurCode
			msg.put( "REMARK", "" );// 备注Memo

			// 发送报文
			T810021Client coBankProcess = new T810021Client();
			Context msgContext = coBankProcess.send( context, msg, bankId );

			// 返回报文信息
			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

			/* 无视代理平台返回 */
			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {
				SFLogger.info( context, String.format( "撤销签约成功，上合作行取消状态字失败[%s]", retFlag ) );
			}

		} catch( SFException e ) {
			SFLogger.info( context, String.format( e.getMessage() ) );
		} catch( Exception e ) {
			SFLogger.info( context, String.format( e.getMessage() ) );
		}
		SFLogger.info( context, "doCoBankCancleSign()结束" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()开始" );

		// 上合作行撤销签约关系
		if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {
			SFLogger.info( context, "上合作行撤销签约关系开始" );

			this.doCoBankCancleSign( context );

			SFLogger.info( context, "上合作行撤销签约关系结束" );

		} else if( SFConst.INIT_SIDE_SECU.equals( initSide ) ) {

			// 该账号已无签约关系，上卡管取消状态字
			List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListBySignFlag( context, tranConnection, acctId );
			if( null == signAccountDataList || signAccountDataList.size() == 0 ) {
				try {
					// 券商发起撤销签约上卡管不管成功失败或异常不影响后续流程
					BizUtil.setCardStatusWord( context, acctId, "D", signAccountData.getOpenDepId() );
				} catch( Exception e ) {
					SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, null );
					SFLogger.error( context, String.format( "撤销签约成功，上卡管取消状态字失败但不影响交易流程!" ) );
				}
			}
		}
		// 如果上合作行和主机，取消撤销签约关系超时和明确失败都属于失败处理，放入取消卡状态字表，晚上清算时继续取消
		SFLogger.info( context, "doHost()结束" );
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()开始" );
		try {

			// 交易成功-组包返回券商
			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式

				SFUtil.setResDataValue( context, "SEC_ACCT", SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_ACCT" ) ) );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", SFUtil.objectToString( SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) ) );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式组包返回券商

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00401" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// 组<BkAcct>组件
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

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
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );
			invName = SFUtil.getReqDataValue( context, "INV_NAME" );
			invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" );
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );
			invType = SFUtil.getReqDataValue( context, "INV_TYPE" );
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // 转换前客户证件类型

			// 先预定义返回报文-交易异常退出时返回 --开始
			if( SFConst.SECU_ZL.equals( secuType ) ) {// 直联模式
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100101_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// 币种
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
			}
			// 深证通模式
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00401" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// 组<BkAcct>组件
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
			}

			// 检查关键字段是否为空
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "券商编号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "证件类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "证件号码不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "投资者名称不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "客户类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "币别不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "券商流水号不能为空" ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		try {

			// 检查是否存在重复流水
			BizUtil.chkRepeatAcctJour( context, tranConnection );

			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// 生成发起方流水号
			signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );// 签约信息
			SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "签约信息不存在" ) );
			InvestData investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
			SFUtil.chkCond( context, ( null == investData ), "ST4392", "投资人信息不存在" );
			initSide = signAccountData.getInitSide();

			// 银河证券、五矿证券须校验券商营业部信息
			SpecialSecuUtil.chkSecBrchIdBySecCompCode( context, secCompCode, SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100101_I" );
			String secAcct = signAccountData.getSecAcct();
			String ecifNo = investData.getBecifNo();// 获取becifNo
			SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );
			SFUtil.addDataField( context, keyIColl, "SEC_ACCT", secAcct );

			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// 券商证件类型转换为主机证件类型
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// 交易日期
			txTime = DateUtil.getMacTime();// 取物理时间
			userId = secu.getUserId();

			/***************************************************
			 * 公共检查： 检查券商服务状态 *
			 *  		  检查当前券商是否允许办理该币种交易 * 
			 *  		  当前券商是否允许办理账户类交易 * 
			 *  		  检查投资人身份
			 **************************************************/

			String signFlag = signAccountData.getSignFlag();// 签约信息
			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			if( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ) {// 已撤销签约
				isSuccRetFlag = true;
				return;
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "签约信息不符" ) );

			SFLogger.info( context, "检查当天是否发生过转账交易，发生过则不允许撤销签约关系开始" );
			// 添加交易日期
			context.addDataField( "TRAN_DATE", txDate );
			// 检查当天是否发生过转账交易，发生过则不允许撤销签约关系
			BizUtil.chkTransfer( context );
			SFLogger.info( context, "检查当天是否发生过转账交易，发生过则不允许撤销签约关系结束" );

			// 检查余额是否一致
			BigDecimal txAmount = new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );
			if( txAmount.compareTo( signAccountData.getBeginBal() ) != 0 || txAmount.compareTo( signAccountData.getAcctBal() ) != 0 ) {
				SFUtil.chkCond( context, "ST5332", String.format( "[交易金额与余额不一致]，不允许办理此交易" ) );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 
	 * 撤销签约的数据插入到表TRDAUTOBECIF中，后续轮询发协议到BECIF
	 * @param context
	 * @throws SFException
	 */
	private void addAutoBecif( Context context ) throws SFException {
		try {
			String subType = null;
			String tpdmFlag = secu.getTpdmFlag();
			String ecifNo = SFUtil.getReqDataValue( context, "ECIF_NO" );
			List<SignAccountData> signAccountDataList = null;

			/* 判断是普通证券还是融资融券 */
			if( SFConst.TPDM_FLAG_NORMAL.equals( tpdmFlag ) ) {// 普通证券
				subType = "R81";
			} else if( SFConst.TPDM_FLAG_MARGIN.equals( tpdmFlag ) ) {// 融资融券
				subType = "R83";
			}
			// 拼接协议号 券商代码（8位）-协议小类（R81 为A股、R82为B股 、R83为融资融券）-卡号
			String agreementNo = secCompCode + "-" + subType + "-" + acctId;
			SFLogger.info( context, String.format( "ecifNo:[%s],userId:[%s],agreementNo：[%s]", ecifNo, userId, agreementNo ) );
			// 查询卡号对应成功签约该券商的个数，如果为0，则需要发消息到BECIF接触协议
			signAccountDataList = signAccountDataDao.qrySignAccountDataListByAcctId( context, tranConnection, acctId, secCompCode );
			if( ( signAccountDataList == null || signAccountDataList.size() == 0 ) && SFUtil.isNotEmpty( subType ) && SFUtil.isNotEmpty( ecifNo ) && SFUtil.isNotEmpty( acctId ) ) {

				// 拼接协议号 券商代码（8位）-协议小类（R81 为A股、R82为B股 、R83为融资融券）-卡号
				AutoBecif autoBecif = new AutoBecif();
				autoBecif.setTxTime( txTime );
				autoBecif.setStatus( "0" );
				autoBecif.setAgreementNo( agreementNo );
				autoBecif.setBecifNo( ecifNo );
				autoBecif.setAgreementType( "R8" );
				autoBecif.setAgreementSubType( subType );
				autoBecif.setAgreementStatus( "2" );
				autoBecif.setProductNo( acctId );
				autoBecif.setOpenDate( "" );
				autoBecif.setCloseDate( txDate );
				autoBecif.setDeptNo( "9998" );
				autoBecif.setUserId( "EB001" );
				autoBecif.setBusinessSeriesCD( SFConst.SYS_SYSID );
				autoBecif.setTxDate( txDate );
				autoBecif.setSubTxSeqId( subTxSeqId );

				DBHandler.beginTransaction( context, tranConnection );// 开启事务
				AutoBecifDao autoBecifDao = new AutoBecifDao();
				autoBecifDao.saveAutoBecif( context, tranConnection, autoBecif );
				DBHandler.commitTransaction( context, tranConnection ); // 提交事务
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
	}
}