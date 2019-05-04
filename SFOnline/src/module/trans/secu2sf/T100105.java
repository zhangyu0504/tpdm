package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import module.bean.AcctDetail;
import module.bean.AcctJour;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.ParamCache;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.AmtUtil;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * A股券商端发起--联机金融类交易
 * 
 * 
 * 处理逻辑：
 * 
 * 			1.接收券商交易 （<深>Trf.007.01/12006，<直>6045/612208）
 *			2.检查请求包有效性,检查券商和客户资料、余额等
 *			3.更新相关数据库
 *			4.返回券商         （<深>Trf.008.01/12006，<直>6045/612208）
 * 
 * 
 * 销户结息
 * tran code :100105
 * @author 吕超鸿
 *
 */
public class T100105 extends TranBase {

	private String subTxSeqId = null;// 16位服务平台流水号

	private String initSeqId = null;// 14位日志号(前置流水号)

	private String secSeqId = null;// 券商流水号

	private String txSeqId = null; // 8位发起方流水号

	private String chlSeqId = null; // 22位交易流水号

	private String txDate = null;// 交易日期

	private String txTime = null;// 交易时间

	private String invType = null;// 客户类型

	private String cashRemitFlag = null;// 钞汇标识

	private BigDecimal txAmount = new BigDecimal( 0.00 );// 交易金额

	private BigDecimal amountTax = new BigDecimal( 0.00 );// 利息

	private String curCode = null;// 币种

	private String clrAccrlType = null;// 结息类型

	private String invName = null;// 客户姓名

	private String acctId = null;// 银行帐号

	private String secCompCode = null;// 券商代码

	private String capAcct = null;// 资金帐号

	private String userId = null;// 客户编号

	private String hostIdType = null;// 三方证件类型

	private int secAcctSeq = 0;// 保证金管理账号序列号

	private String secAcct = null;// 保证金管理账号

	private String secuType = null;// 券商类型

	private boolean succResFlag = false;// 正确流程特殊返回标识

	private SecCompData secu = null;// 券商信息

	private SignAccountData signAccountData = null;// 签约信息

	private AcctJourDao acctJourDao = new AcctJourDao();// 流水Dao实例化

	private DecimalFormat df = new DecimalFormat( "#0.00" );// 数字格式化，保留两位小数

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
				kColl = SFUtil.getDataElement( context, "Trf00701" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// 券商编号
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "此券商信息不存在" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "此券商信息不存在" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// 将券商类型放入上下文中
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// 券商类型
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "券商类型不能为空" ) );
			// 直联模式
			if( SFConst.SECU_ZL.equals( secuType ) ) {

				cashRemitFlag = SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ); // 钞汇标示

				// 组装券商通用上下文
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100105_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

				// 深证通模式
			} else if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );

				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// 券商流水号
				invType = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// 客户类型
				String txAmountStr = ( null != SFUtil.getDataValue( context, kColl, "TX_AMOUNT" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "TX_AMOUNT" ) ) : "0.00";
				String amountTaxStr = ( null != SFUtil.getDataValue( context, kColl, "AMOUNT_TAX" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, kColl, "AMOUNT_TAX" ) ) : "0.00";
				txAmount = new BigDecimal( txAmountStr );// 金额
				amountTax = new BigDecimal( amountTaxStr );// 利息税

				// 组装券商通用上下文
				KeyedCollection keyColl = new KeyedCollection( "100105_I" );

				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", df.format( txAmount ) );// 结息金额
				SFUtil.addDataField( context, keyColl, "AMOUNT_TAX", df.format( amountTax ) );// 利息税
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" ) );// 证件类型
				SFUtil.addDataField( context, keyColl, "INV_TYPE", BizUtil.convSZT2SFInvType( context, invType ) );// 转换客户类型为 1：个人 2：机构
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// 券商端资金台账号
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );//
				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// 币种
				SFUtil.addDataField( context, keyColl, "CLC_ACCRL_TYPE", SFUtil.getDataValue( context, kColl, "CLC_ACCRL_TYPE" ) );// 结息类型
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// 客户名称
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// 发展卡卡号/对公账号
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// 券商流水号

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

		// 记录交易流水,更新签约账户余额表的余额,插入交易明细
		if( !succResFlag ) {
			addPublicInfo( context );
		}

		// 组包返回券商
		doSecu( context );

	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()开始" );

		try {
			DBHandler.beginTransaction( context, tranConnection ); // 开启事务

			/* 记录交易流水 */
			addAcctJour( context );

			/* 更新签约账户余额表的余额 */
			updSignAccountData( context );

			/* 插入交易明细 */
			addAcctDetail( context );

			DBHandler.commitTransaction( context, tranConnection );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()结束" );
	}

	/**
	 * 更新签约账户余额表的余额
	 *〈方法功能描述〉
	 * @param context
	 * @throws SFException
	 */
	public void updSignAccountData( Context context ) throws SFException {
		SFLogger.info( context, "updSignAccountData()开始" );

		try {

			// 查询TRDSignAccountData锁定记录
			SignAccountData signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
			SFUtil.chkCond( context, null == signAccountData, "ST5800", "客户签约账号信息查询失败!" );

			// 更新签约账户余额表的余额
			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setProductType( "03" );
			signAccountData.setPreTxDate( txDate );
			signAccountData.setAcctBal( signAccountData.getAcctBal().add( new BigDecimal( SFUtil.objectToString( txAmount ) ) ) );
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新签约表余额信息失败" );
		}
		SFLogger.info( context, "updSignAccountData()结束" );
	}

	/**
	 * 
	 * 插入交易明细
	 * @param context
	 * @throws SFException
	 */
	public void addAcctDetail( Context context ) throws SFException {
		SFLogger.info( context, "doAddAcctDetail()开始" );
		try {

			AcctDetail acctDetail = new AcctDetail();
			acctDetail.setTxDate( txDate );
			acctDetail.setUserId( userId );
			acctDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctDetail.setTxSeqId( txSeqId );
			acctDetail.setSecSeqId( secSeqId );
			acctDetail.setSubTxSeqId( subTxSeqId );
			acctDetail.setSecAcct( secAcct );
			acctDetail.setSecAcctSeq( secAcctSeq );
			acctDetail.setCapAcct( capAcct );
			acctDetail.setAcctId( acctId );
			acctDetail.setSecCompCode( secCompCode );
			acctDetail.setCurCode( curCode );
			acctDetail.setDcFlag( SFConst.CREDIT_FLAG );
			acctDetail.setTxAmount( txAmount );
			acctDetail.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
			acctDetail.setAbst( "****" );
			acctDetail.setAbstractStr( "券商端销户结息" );
			acctDetail.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctDetail.setJourFlag( "00" );
			acctDetail.setTxCode( SFConst.SF_TX_CODE_SECU_INTEREST );
			acctDetail.setBusiType( "05" );
			acctDetail.setTxTime( txTime );
			acctDetail.setDepId( "" );
			acctDetail.setUnitTellerId( "" );
			acctDetail.setCashRemitFlag( cashRemitFlag );
			acctDetail.setMemo( "" );
			acctDetail.setColFlag( "0" );
			acctDetail.setAcctDealId( "" );
			acctDetail.setAcctHostSeqId( "" );
			acctDetail.setPreSeqId( "" );
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入交易明细信息失败" );
		}
		SFLogger.info( context, "doAddAcctDetail()结束" );
	}

	/**
	 * 记录交易流水
	 */
	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()开始" );
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setTxDate( txDate );// 交易日期
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setSecAcct( secAcct );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.CREDIT_FLAG );
			acctJour.setTxAmount( txAmount );
			acctJour.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
			acctJour.setAbstractStr( "券商端销户结息" );
			acctJour.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctJour.setJourFlag( "00" );
			acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_INTEREST );
			acctJour.setBusiType( "05" );
			acctJour.setTxTime( txTime );// 交易日期
			acctJour.setDepId( "" );
			acctJour.setUnitTellerId( "" );
			acctJour.setCashRemitFlag( cashRemitFlag );
			acctJour.setMemo( "" );
			acctJour.setColFlag( "0" );
			acctJour.setAcctDealId( "" );
			acctJour.setAcctHostSeqId( "" );
			acctJour.setPreSeqId( "" );
			acctJour.setOldInvName( invName );
			acctJour.setOldIdType( hostIdType );
			acctJour.setOldInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctJour.setOldAcctId( acctId );
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

	@Override
	public void doHost( Context context ) throws SFException {

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()开始" );
		try {

			// 交易成功-组包返回券商
			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式

				// 国泰证券 或 国泰信用 需要回送capAcct、curCode、secSeqId字段
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) || SFConst.SECU_ZHONGJINZQ.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
					SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				}
				// 中金公司需要回送 SecSeqId、AcctId、SecAcct、CapAcct、CurCode、CashRemitFlag、TxAmount、SubTxSeqId字段
				if( SFConst.SECU_ZHONGJINZQ.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "ACCT_ID", acctId );
					SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
					SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", cashRemitFlag );
					SFUtil.setResDataValue( context, "TX_AMOUNT", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.add( signAccountData.getAcctBal(), txAmount ) ) ) );
					SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				}
				// 其他券商只需回送SecSeqId字段
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式组包返回券商

				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00801" );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( SFUtil.add( signAccountData.getAcctBal(), txAmount ) ) );
				SFUtil.setDataValue( context, kColl, "AMOUNT_TAX", df.format( amountTax ) );
				SFUtil.setDataValue( context, kColl, "CLC_ACCRL_TYPE", clrAccrlType );
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

			invName = SFUtil.getReqDataValue( context, "INV_NAME" );// 客户名称
			invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // 客户类型
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );// 发展卡卡号/对公账号
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// 券商端资金台账号
			secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// 券商端流水号
			txAmount = new BigDecimal( AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" );// 币种
			clrAccrlType = SFUtil.getReqDataValue( context, "CLC_ACCRL_TYPE" );// 结息类型

			// 交易异常退出时返回
			if( SFConst.SECU_ZL.equals( secuType ) ) {// 直联模式
				// 国泰证券 或 国泰信用 需要回送capAcct、curCode、secSeqId字段,其他券商只需回送SecSeqId字段
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) ) {
					SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
					SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				}
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", secSeqId );
			}
			// 深证通模式
			if( SFConst.SECU_SZT.equals( secuType ) ) {
				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00801" );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );
				SFUtil.setDataValue( context, kColl, "AMOUNT_TAX", df.format( amountTax ) );
				SFUtil.setDataValue( context, kColl, "CLC_ACCRL_TYPE", clrAccrlType );

				// 检查利息税小数位有效性
				AmtUtil.chkAmtValid( context, df.format( txAmount ) );
				AmtUtil.chkAmtValid( context, df.format( amountTax ) );
			}
			// 检查关键字段是否为空
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "券商编号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "投资者名称不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "客户类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "币别不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "券商流水号不能为空" ) );

			// 检查金额小数位有效性
			AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );

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

			// 五矿、银河证券Rever节点需要回送brchId机构号，先添加到context中
			SFUtil.addDataField( context, "SEC_BRCH_ID", ( null != signAccountData && SFUtil.isNotEmpty( signAccountData.getFlags() ) ) ? signAccountData.getFlags() : " " );

			// 检查签约关系,此处删去了pics上主机验证客户身份证
			String signFlag = signAccountData.getSignFlag();// 签约信息
			SFUtil.chkCond( context, ( SFUtil.isEmpty( signFlag ) ), "ST5720", String.format( "签约信息不存在" ) );

			if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( signFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( signFlag ) ) {
				signFlag = "*";
			}
			SFUtil.chkCond( context, ( "*".equals( signFlag ) ), "ST5720", String.format( "签约信息不符" ) );
			SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5720", String.format( "已撤销签约" ) );

			secAcctSeq = signAccountData.getSecAcctSeq();
			secAcct = signAccountData.getSecAcct();// 保证金管理账号
			userId = secu.getUserId();// 客户编号
			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// 券商证件类型转换为主机证件类型
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// 交易日期
			txTime = DateUtil.getMacTime();// 取物理时间

			// 查询券商利息税处理方式 从数据字典获取
			String secInterestTax = ParamCache.getValue2( "SEC_INTEREST_TAX", secCompCode );
			secInterestTax = SFUtil.isEmpty( secInterestTax ) ? "0" : secInterestTax;
			// 利息 深证通时 利息等于金额txAmount,直联未送利息税字段
			if( "1".equals( secInterestTax ) ) {
				txAmount = txAmount.subtract( amountTax ).abs();// 取绝对值
			}
			SFLogger.info( context, String.format( "真正的结息金额TxAmount:[%s]", df.format( txAmount ) ) );

			// 检查当天是否有结息
			context.addDataField( "TRAN_DATE", txDate );// 添加交易日期
			AcctJour acctJour = acctJourDao.qryAcctJourByTxCode( context, tranConnection );
			if( SFUtil.isNotEmpty( acctJour ) ) {
				SFUtil.chkCond( context, ( txAmount.compareTo( acctJour.getTxAmount() ) != 0 ), "ST5721", String.format( "当日[已结息]，不允许重复办理" ) );
				// 组包返回券商成功
				if( txAmount.compareTo( acctJour.getTxAmount() ) == 0 ) {
					succResFlag = true;
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}
}