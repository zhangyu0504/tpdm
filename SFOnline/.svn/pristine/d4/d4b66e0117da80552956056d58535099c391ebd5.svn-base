package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctDetail;
import module.bean.AcctJour;
import module.bean.AgtCustomerInfo;
import module.bean.AgtTranList;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.Reverse;
import module.bean.SecCompData;
import module.bean.SecTruAcct;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ProductInfoCache;
import module.cache.SecTruAcctCache;
import module.communication.CoBankClientBase;
import module.communication.ESBClientBase;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AgtTranListDao;
import module.dao.ReverseDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.TransferClient;
import module.trans.sf2cobank.T810022Client;
import module.trans.sf2cobank.T810024Client;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
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
 * 交易名称：（券商端发起）银转证 
 * 
 * 处理流程：
 *  		1.接收券商交易（<深>Trf.001.01/12001，<直>6042/612206）
 *			2.检查请求包有效性,检查券商信息,客户信息有效性等
 *			3.代理：调用612322转发代理平台 非代理：调用G1001转账（客户帐到4031529）、异步上主机（4031529到券商汇总账户）
 *			4.返回券商  （<深>Trf.002.01/12001，<直>6042/612206）"
 *
 * 交易码：100200
 * @author 许张鸿达
 * 
 */

public class T100200 extends TranBase {

	private AcctJour acctJour = new AcctJour();

	private AcctDetail acctDetail = new AcctDetail();

	private SignAccountData signAccountData = null;

	private AcctJourDao acctJourDao = new AcctJourDao();

	private InvestData investData = null;

	private LocalInfo localInfo = null;

	private ProductInfo productInfo = null;

	private SecTruAcct secTruAcct = null;

	private SecCompData secCompData = null;

	private AgtCustomerInfo agtCustomerInfo = null;

	private String initSeqId = null;

	private String subTxSeqId = null;

	private String chlSeqId = null;

	private String secCompCode = null;

	private String capAcct = null;

	private String secuType = null;

	private String secSeqId = null;

	private String acctId = null;

	private String truAcctId = null;

	private String txDate = null;

	private BigDecimal txAmount = new BigDecimal( 0.00 );

	private DecimalFormat df = new DecimalFormat( "#0.00" );// 数字格式化，保留两位小数

	protected void initialize( Context context ) throws SFException {

		initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14位前置流水号
		subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16位服务平台流水号
		chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // 获取22位流水号;

		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ); // 券商代码
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Trf00101" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "[券商代码]不允许为空" ) );
			secCompData = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secCompData ), "ST5711", String.format( "券商信息不存在" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secCompData.getSztFlag() );// 将券商类型放入上下文中
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secCompData );

			// 券商类型
			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );
			SFUtil.chkCond( context, SFUtil.isEmpty( secuType ), "ST5701", String.format( "[券商类型]不能为空" ) );

			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );

				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = ( String )SFUtil.getDataValue( context, refKcoll, "Ref" );

				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				acctId = ( String )bkAcctKcoll.getDataValue( "ACCT_ID" );// 银行账号

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				String invName = ( String )custKcoll.getDataValue( "INV_NAME" );// 客户名称
				String idType = ( String )custKcoll.getDataValue( "ID_TYPE_SZT" );// 证件类型
				String invIdCode = ( String )custKcoll.getDataValue( "INV_ID_CODE" );// 证件号码
				String invTypeSZT = ( String )custKcoll.getDataValue( "INV_TYPE_SZT" );// 客户类型
				capAcct = ( String )scAcctKcoll.getDataValue( "CAP_ACCT" );// 券商端客户资金台账号
				String curCode = ( String )kColl.getDataValue( "CUR_CODE" );// 币种
				String txAmount = df.format( new BigDecimal( SFUtil.objectToString( kColl.getDataValue( "TX_AMOUNT" ) ) ) );// 金额
				String invType = BizUtil.convSZT2SFInvType( context, invTypeSZT );

				// 组装券商通用上下文
				KeyedCollection keyColl = new KeyedCollection( "100200_I" );
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// 券商流水号
				SFUtil.addDataField( context, keyColl, "CUR_CODE", curCode );// 币种
				SFUtil.addDataField( context, keyColl, "INV_NAME", invName );// 客户名称
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// 证件类型
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", invIdCode );// 证件号码
				SFUtil.addDataField( context, keyColl, "INV_TYPE", invType );// 客户类型
				SFUtil.addDataField( context, keyColl, "ACCT_ID", acctId );// 发展卡卡号/对公账号
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", capAcct );// 券商端资金台账号
				SFUtil.addDataField( context, keyColl, "TX_AMOUNT", txAmount );// 金额
				SFUtil.addDataElement( context, keyColl );

				// 组返回报文
				KeyedCollection retKColl = SFUtil.getDataElement( context, "Trf00201" );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, retKColl, "ScAcct" );
				KeyedCollection acctSvcrRetKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, acctSvcrRetKcoll, "SEC_COMP_CODE", secCompCode );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, retKColl, "CUR_CODE", curCode );
				SFUtil.setDataValue( context, retKColl, "TX_AMOUNT", txAmount );

				// 组<Rst>组件
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, retKColl, "Rst" );

				SFUtil.setDataValue( context, rstKcoll, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setDataValue( context, rstKcoll, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );

			} else {
				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );
				acctId = SFUtil.getReqDataValue( context, "ACCT_ID" );
				capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );

				// 组装券商通用上下文
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100200_I" );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );

				// 组返回报文
				SFUtil.setResDataValue( context, "SEC_ACCT", SFUtil.getReqDataValue( context, "SEC_ACCT" ) );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );
				SFUtil.setResDataValue( context, "ACCT_ID", acctId );
				SFUtil.setResDataValue( context, "CAP_ACCT", SFUtil.getReqDataValue( context, "CAP_ACCT" ) );
				SFUtil.setResDataValue( context, "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );
				SFUtil.setResDataValue( context, "TX_AMOUNT", SFUtil.getReqDataValue( context, "TX_AMOUNT" ) );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", SFConst.CASH_FLAG );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

	}

	public void doHandle( Context context ) throws SFException {

		addAcctJour( context ); // 记录交易流水表

		doHost( context ); // 我行客户

		doSecu( context ); // 赋值返回券商

	}

	protected void chkStart( Context context ) throws SFException {
		txAmount = new BigDecimal( AmtUtil.conv2SecuDivAmount( context, SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );
		// 交易金额不能小于0
		SFUtil.chkCond( context, SFUtil.object2Double( txAmount ) <= 0, "ST5333", String.format( "交易金额不能小于等于零" ) );
		AmtUtil.chkMaxAmount( context, SFUtil.object2Double( txAmount ) );// 检查交易金额
		AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "TX_AMOUNT" ) ) );// 检查金额小数位有效性

		String invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // 客户类型
		String invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // 证件号码
		String invName = SFUtil.getReqDataValue( context, "INV_NAME" ); // 客户名称
		String curCode = SFUtil.getReqDataValue( context, "CUR_CODE" ); // 币种
		String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // 转换前客户证件类型
		acctId = SFUtil.getReqDataValue( context, "ACCT_ID" ); // 发展卡卡号/对公账号

		// 检查关键字段是否为空
		SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "券商编号不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "证件类型不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "证件号码不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "投资者名称不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "客户类型不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "币别不能为空" ) );
		SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "券商流水号不能为空" ) );
		SFUtil.chkCond( context, ( 10 < secSeqId.length() ), "ST4430", String.format( "券商流水号长度不能超过10位" ) );
	}

	protected void chkEnd( Context context ) throws SFException {
		secCompData = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU );
		signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );
		investData = SFUtil.getDataValue( context, SFConst.PUBLIC_INVEST_DATA );
		localInfo = SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO );
		txDate = localInfo.getWorkdate();

		SFUtil.chkCond( context, ( null == signAccountData ), "ST5720", String.format( "签约信息不存在" ) );
		SFUtil.chkCond( context, ( null == investData ), "ST4392", "投资人信息不存在" );

		// 五矿、银河证券Rever节点需要回送brchId机构号，先添加到context中
		SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.isNotEmpty( signAccountData.getFlags() ) ? signAccountData.getFlags() : " " );

		SFUtil.chkCond( context, !SFConst.SIGN_FLAG_SIGN.equals( signAccountData.getSignFlag() ), "ST5591", String.format( "客户当前状态[%s]不允许办理此交易!", signAccountData.getSignFlag() ) );

		SFUtil.chkCond( context, SFUtil.isEmpty( secCompData.getSecCompCode() ), "ST5705", String.format( "券商代码错误" ) );

		// 检查券商是否允许对公客户做银转证交易
		SFUtil.chkCond( context, ( SFConst.INV_TYPE_CORP.equals( signAccountData.getInvType() ) ) && ( !"1".equals( secCompData.getJGZZFlag1() ) ), "ST4396", String.format( "不允许券商发起对公客户银转证交易" ) );

		// 非正常交易时间段不允许合作行客户发起交易
		SFUtil.chkCond( context, ( !SFConst.WORKMODE_NORMAL.equals( ( String )SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE ) ) ) && ( SFConst.INIT_SIDE_COBANK.equals( signAccountData.getInitSide() ) ), "ST4525", String.format( "非正常交易时间段不允许合作行客户发起交易" ) );
	}

	private void addAcctJour( Context context ) throws SFException {
		SFLogger.info( context, "addAcctJour()开始" );
		SFLogger.info( context, "新增交易流水" );

		String cashRemitFlag = ( SFConst.REMIT_FLAG.equals( ( String )SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ) ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG ); // 钞汇标志
		acctJour.setTxDate( txDate );
		acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
		acctJour.setUserId( secCompData.getUserId() );
		acctJour.setTxSeqId( BizUtil.getTxSeqId( secSeqId ) );
		acctJour.setSubTxSeqId( subTxSeqId );
		acctJour.setSecSeqId( secSeqId );
		acctJour.setInvType( signAccountData.getInvType() );
		acctJour.setInvName( signAccountData.getInvName() );
		acctJour.setIdType( investData.getIdType() );
		acctJour.setInvIdCode( investData.getInvIdCode() );
		acctJour.setSecAcct( signAccountData.getSecAcct() );
		acctJour.setSecAcctSeq( signAccountData.getSecAcctSeq() );
		acctJour.setSecCompCode( signAccountData.getSecCompCode() );
		acctJour.setCapAcct( signAccountData.getCapAcct() );
		acctJour.setAcctId( signAccountData.getAcctId() );
		acctJour.setSavAcct( signAccountData.getSavAcct() );
		acctJour.setOpenDepId( signAccountData.getOpenDepId() );
		acctJour.setOpenBranchId( signAccountData.getOpenBranchId() );
		acctJour.setCurCode( ( String )SFUtil.getReqDataValue( context, "CUR_CODE" ) );
		acctJour.setDcFlag( SFConst.CREDIT_FLAG );
		acctJour.setTxAmount( txAmount );
		acctJour.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
		acctJour.setAbst( ( SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "1003" : "2003" ) );
		acctJour.setAbstractStr( "券商发起银转证" );
		acctJour.setJourFlag( "30" );
		acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_B2S );
		acctJour.setBusiType( SFConst.BUSI_TYPE_B2S );
		acctJour.setTxTime( DateUtil.getMacTime() );
		acctJour.setDepId( signAccountData.getOpenDepId() );
		acctJour.setBranchId( signAccountData.getBranchId() );
		acctJour.setUnitTellerId( "EB001" );
		acctJour.setCashRemitFlag( cashRemitFlag );
		acctJour.setAcctDealId( initSeqId );
		acctJour.setPreSeqId( initSeqId );
		acctJour.setAcctDealDate( DateUtil.getMacDate() );
		acctJour.setProductType( "03" );
		acctJour.setColFlag( "0" );
		acctJour.setMemo( "券商发起银转证" );
		acctJour.setTranSeqId( chlSeqId );
		acctJour.setBusiSeqId( chlSeqId );

		try {
			DBHandler.beginTransaction( context, tranConnection );
			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
			DBHandler.commitTransaction( context, tranConnection );
		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "新增交易流水失败" );
		}
		SFLogger.info( context, "addAcctJour()结束" );
	}

	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()开始" );
		try {
			productInfo = ProductInfoCache.getValue( secCompCode );// 根据券商代码获取券商产品缓存对象
			secTruAcct = SecTruAcctCache.getValue( secCompCode );// 根据券商代码获取券商托管账户缓存对象
			BankUnit bankUnitOutCounter = BankUnitCache.getValue( productInfo.getTruOpnDepId() );// 根据开户网点号获取机构缓存对象
			BankUnit bankUnitInCounter = BankUnitCache.getValue( signAccountData.getOpenBranchId() );// 根据开户网点号获取机构缓存对象
			truAcctId = productInfo.getTruAcctId();

			String retFlag = null;
			String retCode = null;
			String retMsg = null;
			String outDepName = bankUnitOutCounter != null ? bankUnitOutCounter.getDepName() : "";
			String inDepName = bankUnitInCounter != null ? bankUnitInCounter.getDepName() : "";
			if( SFConst.INIT_SIDE_COBANK.equals( signAccountData.getInitSide() ) ) {
				SFUtil.setDataValue( context, SFConst.PUBLIC_INIT_SIDE, SFConst.INIT_SIDE_COBANK );
				retFlag = cobankB2STransfer( context );
				retCode = SFUtil.getDataValue( context, "RESP_CODE" );// 响应码
				retMsg = SFUtil.getDataValue( context, "RESP_MSG" );// 响应信息
			} else {
				SFLogger.info( context, String.format( "上主机记帐,客户帐号[%s]->券商帐号[%s]", acctId, truAcctId ) );
				// 组交易请求包
				Map<String, Object> hostMap = new HashMap<String, Object>();
				hostMap.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
				hostMap.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
				hostMap.put( "OUT_ACCT_NO", acctId );// 转出账号OUT_ACCT_NO
				hostMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );// 转出账户用途OUT_ACCT_USAGE
				// hostMap.put( "OUT_BRANCH_ID", signAccountData.getOpenBranchId() );// 转出行号OUT_BRANCH_ID
				hostMap.put( "OUT_COUNTER_ACCT_NO", truAcctId );// 转出对方行账号OUT_COUNTER_ACCT_NO
				hostMap.put( "OUT_COUNTER_CLIENT_NAME", secTruAcct.getAcctName() );// 转出对方行客户名称OUT_COUNTER_CLIENT_NAME
				hostMap.put( "OUT_COUNTER_BRANCH_NAME", outDepName );// 转出对方行行名OUT_COUNTER_BRANCH_NAME
				hostMap.put( "TX_AMOUNT", txAmount );// 金额AMT
				hostMap.put( "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );// 币种CCY
				hostMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );// 对账单号STATEMENT_NO
				hostMap.put( "IN_ACCT_NO", truAcctId );// 转入账号IN_ACCT_NO
				hostMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );// 转入账户用途IN_ACCT_USAGE
				hostMap.put( "IN_TOTAL_DETAIL_FLAG", "1" );// 转入汇总明细标志IN_TOTAL_DETAIL_FLAG
				// hostMap.put( "IN_BRANCH_ID", bankUnitOutCounter.getBranchId() );// 转入行号IN_BRANCH_ID
				hostMap.put( "IN_COUNTER_ACCT_NO", acctId );// 转入对方行账号IN_COUNTER_ACCT_NO
				hostMap.put( "IN_COUNTER_BRANCH_NAME", inDepName );// 转入对方行分行名称IN_COUNTER_BRANCH_NAME
				hostMap.put( "TRAN_TYPE", SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "B" : "" );// 交易类型TRAN_TYPE
				hostMap.put( "TRADER_TYPE_CODE", SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "SFCG" : "" );// 商户类型代码TRADER_TYPE_CODE

				ESBClientBase esbClient = new TransferClient();
				Context msgContext = esbClient.send( context, hostMap );// 发送报文

				retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );

				IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
				KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
				retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );
				retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息
			}

			if( SFConst.RET_SUCCESS.equals( retFlag ) ) { // 成功
				acctJour.setJourFlag( "00" );
				acctJour.setRespCode( "000000" );
				acctJour.setRespMsg( "上主机转帐成功" );
				SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, SFConst.RESPCODE_SUCCCODE );
				SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, SFConst.RESPCODE_SUCCMSG );
			} else if( SFConst.RET_FAILURE.equals( retFlag ) ) { // 失败
				acctJour.setJourFlag( "20" );
				acctJour.setRespCode( retCode );
				acctJour.setRespMsg( String.format( "上主机转帐失败[%s]", retMsg ) );
				SFUtil.setDataValue( context, SFConst.CTX_ERRCODE, "ST4376" );
				SFUtil.setDataValue( context, SFConst.CTX_ERRMSG, retMsg );
			} else if( SFConst.RET_OVERTIME.equals( retFlag ) ) { // 超时/异常
				acctJour.setJourFlag( "30" );
				acctJour.setRespCode( "E9999" );
				acctJour.setRespMsg( String.format( "主机交易异常[%s]", retMsg ) );
				SFUtil.setDataValue( context, SFConst.PUBLIC_IS_RET_RESP, SFConst.RET_RESP_NO );
			}

			DBHandler.beginTransaction( context, tranConnection );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour ); // 更新交易流水

			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {
				// 新增交易明细
				addAcctDetail( context );

				// 查询TRDSignAccountData锁定记录
				SignAccountData signAccountData = signAccountDataDao.signAccountDataLock( context, tranConnection, secCompCode, capAcct );
				SFUtil.chkCond( context, ( null == signAccountData ), "ST5800", "客户签约账号信息锁表失败!" );

				// 更新签约信息表
				signAccountDataDao.updSignAccountData( context, tranConnection, secCompCode, capAcct, txAmount );

			}

			DBHandler.commitTransaction( context, tranConnection );

			SFUtil.chkCond( context, SFConst.RET_FAILURE.equals( retFlag ), "ST4376", retMsg );

		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "通用主机划款失败" ) );
		}

		SFLogger.info( context, "doHost()结束" );
	}

	private void addAcctDetail( Context context ) throws SFException {
		SFLogger.info( context, "addAcctDetail()开始" );
		SFLogger.info( context, "新增交易明细" );

		String cashRemitFlag = ( SFConst.REMIT_FLAG.equals( ( String )SFUtil.getReqDataValue( context, "CASH_REMIT_FLAG" ) ) ? SFConst.REMIT_FLAG : SFConst.CASH_FLAG ); // 钞汇标志

		acctDetail.setTxDate( txDate );
		acctDetail.setInitSide( SFConst.INIT_SIDE_SECU );
		acctDetail.setUserId( secCompData.getUserId() );
		acctDetail.setTxSeqId( BizUtil.getTxSeqId( secSeqId ) );
		acctDetail.setSubTxSeqId( subTxSeqId );
		acctDetail.setSecSeqId( secSeqId );
		acctDetail.setInvType( signAccountData.getInvType() );
		acctDetail.setSecAcct( signAccountData.getSecAcct() );
		acctDetail.setSecAcctSeq( signAccountData.getSecAcctSeq() );
		acctDetail.setAcctId( signAccountData.getAcctId() );
		acctDetail.setOpenDepId( signAccountData.getOpenDepId() );
		acctDetail.setOpenBranchId( signAccountData.getOpenBranchId() );
		acctDetail.setSecCompCode( signAccountData.getSecCompCode() );
		acctDetail.setCapAcct( signAccountData.getCapAcct() );
		acctDetail.setCurCode( ( String )SFUtil.getReqDataValue( context, "CUR_CODE" ) );
		acctDetail.setDcFlag( SFConst.CREDIT_FLAG );
		acctDetail.setTxAmount( txAmount );
		acctDetail.setAcctBal( signAccountData.getAcctBal().add( txAmount ) );
		acctDetail.setAbst( ( SFConst.INV_TYPE_RETAIL.equals( signAccountData.getInvType() ) ? "1003" : "2003" ) );
		acctDetail.setAbstractStr( "券商发起银转证" );
		acctDetail.setJourFlag( "00" );
		acctDetail.setTxCode( SFConst.SF_TX_CODE_SECU_B2S );
		acctDetail.setBusiType( SFConst.BUSI_TYPE_B2S );
		acctDetail.setTxTime( DateUtil.getMacTime() );
		acctDetail.setDepId( signAccountData.getOpenDepId() );
		acctDetail.setBranchId( signAccountData.getBranchId() );
		acctDetail.setUnitTellerId( "EB001" );
		acctDetail.setCashRemitFlag( cashRemitFlag );
		acctDetail.setAcctDealId( initSeqId );
		acctDetail.setPreSeqId( initSeqId );
		acctDetail.setAcctDealDate( DateUtil.getMacDate() );
		acctDetail.setColFlag( "0" );
		acctDetail.setMemo( "券商发起银转证" );
		acctDetail.setTranSeqId( chlSeqId );
		acctDetail.setBusiSeqId( chlSeqId );

		try {
			AcctDetailDao acctDetailDao = new AcctDetailDao();
			acctDetailDao.saveAcctDetail( context, tranConnection, acctDetail );
		} catch( SFException e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			DBHandler.rollBackTransaction( context, tranConnection );
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "新增交易明细失败" );
		}

		SFLogger.info( context, "addAcctDetail()结束" );
	}

	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()开始" );
		try {
			if( SFConst.SECU_ZL.equals( secuType ) ) {
				SFUtil.setResDataValue( context, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setResDataValue( context, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "SEC_ACCT", signAccountData.getSecAcct() );
				SFUtil.setResDataValue( context, "CASH_REMIT_FLAG", SFConst.CASH_FLAG );

			} else {
				KeyedCollection kColl = SFUtil.getDataElement( context, "Trf00201" );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", SFUtil.getReqDataValue( context, "CAP_ACCT" ) );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", SFUtil.getReqDataValue( context, "SEC_COMP_CODE" ) );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", SFUtil.getReqDataValue( context, "CUR_CODE" ) );
				SFUtil.setDataValue( context, kColl, "TX_AMOUNT", df.format( txAmount ) );

				// 组<Rst>组件
				KeyedCollection rstKcoll = SFUtil.getDataElement( context, kColl, "Rst" );

				SFUtil.setDataValue( context, rstKcoll, "RESP_CODE", SFUtil.getDataValue( context, SFConst.CTX_ERRCODE ) );
				SFUtil.setDataValue( context, rstKcoll, "RESP_MSG", SFUtil.getDataValue( context, SFConst.CTX_ERRMSG ) );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}

		SFLogger.info( context, "doSecu()结束" );
	}

	private String cobankB2STransfer( Context context ) throws SFException {
		SFLogger.info( context, "银转证上合作行划款开始" );
		SFLogger.info( context, "cobankB2STransfer()开始" );

		/**
		 * 1.预记合作行交易流水
		 */
		AddAgtTranList( context );

		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// 证券资金帐号
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// 银行帐号
		String frontLogNo = SFUtil.getDataValue( context, "FRONT_LOG_NO" );// 前置流水号
		String bankId = agtCustomerInfo.getBankId();// 合作行号
		String tranResult = "MA1111";// 预定义交易结果
		String thirdTime = DateUtil.getMacDateTimeShort();// 获取日期yyyyMMddHHmmss
		String warnFlag = agtCustomerInfo.getAgtAgentInfo().getWarnFlag();// 资金警告标识
		String warnMoney = agtCustomerInfo.getAgtAgentInfo().getWarnMoney();// 资金预警金额

		String retFlag = null;
		Context msgContext = null;
		Map<String, Object> msgMap = null;
		CoBankClientBase coBankClient = null;
		TransferClient transferClient = new TransferClient();
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		ReverseDao reverseDao = new ReverseDao();
		AgtTranList agtTranList = new AgtTranList();
		try {

			/**
			 * 2.合作行资金划转
			 */
			msgMap = new HashMap<String, Object>();
			msgMap.put( "ACCT_ID", acctNo );// 银行帐号account_no
			msgMap.put( "SEC_COMP_CODE", secCompCode );// 券商代码SecCode
			msgMap.put( "CAP_ACCT", stkAcct );// 证券资金台账号CapAcct
			msgMap.put( "TX_AMOUNT", AmtUtil.conv2CoBankMulAmount( context, txAmount ) );// 划转金额exch_bal
			msgMap.put( "TRADE_TYPE", "0" );// 资金类型 银转证 传固定值 0
			msgMap.put( "BANK_ID", bankId );// 机构号
			coBankClient = new T810022Client();
			msgContext = coBankClient.send( context, msgMap, bankId );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			if( !SFConst.RET_SUCCESS.equals( retFlag ) ) {// 超时或失败
				String retMsg = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPMSG" );// 接收合作行返回错误信息
				String retCode = SFUtil.getDataValue( msgContext, "A_RESPONSE_HEAD.RESPCODE" );// 接收合作行返回的错误码
				if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// 上合作行资金划款超时
					// 超时冲正合作行
					Reverse reverse = new Reverse();
					reverse.setChannel( "COBANK" );
					reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
					reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
					reverse.setTxDate( DateUtil.getMacDate() );
					reverse.setSubTxSeqId( subTxSeqId );
					reverse.setType( "0" );
					reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
					reverse.setSceneCode( "13" );
					reverse.setReverseSeq( "20" );
					reverse.setReverseNum( 0 );
					reverse.setStatus( "0" );
					reverse.setMemo( "合作行银转证合作行冲正" );// 备注各自交易自行添加
					reverseDao.saveReverse( context, tranConnection, reverse );
					SFLogger.error( context, "去合作行资金划款交易超时" );
					tranResult = "MA0003";
				}
				if( SFConst.RET_FAILURE.equals( retFlag ) ) {// 上合作行资金划款失败
					SFLogger.error( context, retMsg );
					tranResult = "777779";
				}

				DBHandler.beginTransaction( context, tranConnection );// 开启事务2
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// 提交事务2
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", String.format( "去合作行资金划款交易出错[%s]", retMsg ) );

				return retFlag;

			}
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// 成功
				KeyedCollection kColl = SFUtil.getDataElement( msgContext, "810022_O" );
				String agtLogNo = SFUtil.getDataValue( context, kColl, "AGENT_LOG_NO" );// 取出合作行流水号
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setAgentLogNo( agtLogNo );
				DBHandler.beginTransaction( context, tranConnection );// 开启事务3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// 更新合作行流水
				DBHandler.commitTransaction( context, tranConnection );// 提交事务3
			}

			/**
			 * 3.上主机通用记账
			 */
			SFLogger.info( context, "调用上主机通用记账开始" );
			msgMap = new HashMap<String, Object>();
			msgMap.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msgMap.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msgMap.put( "OUT_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "OUT_BRANCH_ID", agtCustomerInfo.getAgtAgentInfo().getBranchCode() );
			msgMap.put( "OUT_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "IN_TOTAL_DETAIL_FLAG", "1" );
			msgMap.put( "OUT_COUNTER_ACCT_NO", truAcctId );
			msgMap.put( "IN_ACCT_NO", truAcctId );
			msgMap.put( "IN_COUNTER_ACCT_NO", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			msgMap.put( "TX_AMOUNT", txAmount );
			msgMap.put( "CUR_CODE", SFConst.CUR_CODE_RMB );
			msgMap.put( "IN_ACCT_USAGE", SFConst.STATEMENT_NO_B2S );
			msgMap.put( "STATEMENT_NO", BizUtil.getStatmentId( context ) );
			msgContext = transferClient.send( context, msgMap );
			retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			IndexedCollection iColl = SFUtil.getDataElement( msgContext, "RET" );
			KeyedCollection kColl = ( KeyedCollection )iColl.getElementAt( 0 );// 获取响应数组
			String retCode = SFUtil.getDataValue( msgContext, kColl, "RET_CODE" );// 响应码
			String retMsg = SFUtil.getDataValue( msgContext, kColl, "RET_MSG" );// 响应信息
			if( SFConst.RET_OVERTIME.equals( retFlag ) ) {// 超时

				SFLogger.info( context, "通用主机记账交易超时" );
				// 合作行成功，主机划款超时，先冲正合作行
				Reverse reverse = new Reverse();
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "13" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "券商银转证合作行划款冲正" );// 备注各自交易自行添加
				reverseDao.saveReverse( context, tranConnection, reverse );// 新增冲正信息
				DBHandler.commitTransaction( context, tranConnection );// 提交事务

				// 合作行成功，主机划款超时，再冲正主机
				reverse = new Reverse();
				reverse.setChannel( "HOST" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "14" );
				reverse.setReverseSeq( "10" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "券商银转证上主机记账冲正" );// 备注各自交易自行添加
				reverseDao.saveReverse( context, tranConnection, reverse );// 新增冲正信息
				DBHandler.commitTransaction( context, tranConnection );// 提交事务

				/*
				 * 更新交易结果
				 */
				SFLogger.info( context, "合作行成功、主机划款超时，冲正主机后更新交易结果" );
				tranResult = "MA0003";
				DBHandler.beginTransaction( context, tranConnection );// 开启事务1
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// 提交事务1
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", "上主机记账超时" );

				return SFConst.RET_OVERTIME;
			}
			if( SFConst.RET_FAILURE.equals( retFlag ) ) {// 失败

				SFLogger.info( context, "通用主机记账交易返回失败" );

				// 合作行成功，主机划款失败，冲正合作行
				Reverse reverse = new Reverse();
				reverse.setChannel( "COBANK" );
				reverse.setInitside( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_INIT_SIDE ) ) );
				reverse.setSeqId( BizUtil.getSubTxSeqId( BizUtil.getInitSeqId( context ) ) );
				reverse.setTxDate( DateUtil.getMacDate() );
				reverse.setSubTxSeqId( subTxSeqId );
				reverse.setType( "0" );
				reverse.setTxCode( SFUtil.objectToString( SFUtil.getDataValue( context, SFConst.PUBLIC_TX_CODE ) ) );
				reverse.setSceneCode( "13" );
				reverse.setReverseSeq( "20" );
				reverse.setReverseNum( 0 );
				reverse.setStatus( "0" );
				reverse.setMemo( "券商银转证合作行划款冲正" );// 备注各自交易自行添加
				reverseDao.saveReverse( context, tranConnection, reverse );// 新增冲正信息
				DBHandler.commitTransaction( context, tranConnection );// 提交事务

				/*
				 * 更新交易结果
				 */
				SFLogger.info( context, "合作行成功、主机划款失败，冲正合作行后更新交易结果" );
				tranResult = "ST4895";
				String accountDate = SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" );// 取系统会计日期
				agtTranList.setFrontLogNo( frontLogNo );
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				if( SFUtil.isNotEmpty( accountDate ) ) {
					agtTranList.setHostDate( accountDate );
				}
				DBHandler.beginTransaction( context, tranConnection );// 开启事务3
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// 提交事务3
				SFUtil.addDataField( context, "RESP_CODE", retCode );
				SFUtil.addDataField( context, "RESP_MSG", String.format( "上主机记帐失败[%s]", retMsg ) );
				return SFConst.RET_FAILURE;
			}
			if( SFConst.RET_SUCCESS.equals( retFlag ) ) {// 成功
				SFLogger.info( context, "通用主机记账上主机成功后的处理" );

				/*
				 * 更新交易结果
				 */
				SFLogger.info( context, "合作行成功、主机划款成功，更新交易结果" );
				tranResult = "ST0000";
				agtTranList.setTranResult( tranResult );
				agtTranList.setCompleteTime( thirdTime );
				agtTranList.setHostLogNo( "" );
				agtTranList.setHostDate( SFUtil.objectToString( SFUtil.getDataValue( msgContext, "APP_HEAD.ACCOUNT_DATE" ) ) );
				agtTranList.setReserve( "" );
				agtTranList.setVoucherNo( "" );
				agtTranList.setFrontLogNo( frontLogNo );
				DBHandler.beginTransaction( context, tranConnection );// 开启事务2
				agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );
				DBHandler.commitTransaction( context, tranConnection );// 提交事务2

			}

			/**
			 * 4.银转证预警
			 */
			double bankBalance = 0.0;
			double warnBalance = 0.0;
			SFLogger.info( context, "银转证预警" );
			String bankBal = SFUtil.getDataValue( msgContext, "MSG_O.AVAIL_BALANCE" );// 取出可用余额
			if( SFUtil.isNotEmpty( bankBal ) ) {
				bankBalance = Double.parseDouble( bankBal );
			}
			if( SFUtil.isNotEmpty( warnMoney ) ) {
				warnBalance = Double.parseDouble( warnMoney );
			}
			String sumLim = "000000000000000";
			String useLim = "000000000000000";
			String limBal = "000000000000000";
			if( ( "1".equals( warnFlag ) && bankBalance < warnBalance ) || ( "2".equals( warnFlag ) && Integer.parseInt( limBal ) < warnBalance ) ) {
				SFLogger.info( context, "上合作行资金预警-开始" );
				// 构建请求报文
				msgMap = new HashMap<String, Object>();
				msgMap.put( "BANK_ACCT", agtCustomerInfo.getAgtAgentInfo().getBankAcct() );// 联网行账号BankAcct
				msgMap.put( "ACCT_BAL", AmtUtil.conv2CoBankMulAmount( context, bankBal ) );// 账号余额BankBal,需要乘以100
				msgMap.put( "TOTAL_LIMIT", sumLim );// 总额度SumLim
				msgMap.put( "USED_LIMIT", useLim );// 已用额度UseLim
				msgMap.put( "AVAIL_LIMIT", limBal );// 可用额度LimBal
				msgMap.put( "WARN_FLAG", warnFlag );// 预警类型WarnFlag
				msgMap.put( "WARN_MONEY", warnMoney );// 预警金额WarnMoney
				coBankClient = new T810024Client();
				coBankClient.send( context, msgMap, bankId );
				SFLogger.info( context, "上合作行资金预警-结束" );
			}
			SFUtil.addDataField( context, "RESP_CODE", "ST0000" );
			SFUtil.addDataField( context, "RESP_MSG", "银转证上合作行划款成功" );
			SFLogger.info( context, "银转证上合作行划款结束" );
			SFLogger.info( context, "cobankB2STransfer()结束" );

		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.addDataField( context, "RESP_CODE", "ST4895" );
			SFUtil.addDataField( context, "RESP_MSG", "银转证上合作行划款处理失败" );
			return SFConst.RET_OVERTIME;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "银转证上合作行划款处理失败" ) );
		}
		return SFConst.RET_SUCCESS;
	}

	/**
	 * 预记合作行交易流水
	 * 
	 * @param
	 * @throws SFException
	 */
	private void AddAgtTranList( Context context ) throws SFException {
		String tranAmount = AmtUtil.conv2CoBankMulAmount( context, txAmount );// 转账金额
		String secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );// 券商代码
		String stkAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" );// 证券资金帐号
		String acctNo = SFUtil.getReqDataValue( context, "ACCT_ID" );// 银行帐号
		String compAcct = truAcctId;// 汇总帐号
		String frontLogNo = BizUtil.getInitSeqId( context );// 再生成一个流水
		SFUtil.addDataField( context, "FRONT_LOG_NO", frontLogNo );// 把后生成的流水存入上下文中,因为frontLogNo是主键
		String voidFlag = "0";
		String hostCheck = "0";
		String tranResult = "MA1111";
		String tranDate = DateUtil.getMacDate();// 获取日期 yyyyMMdd，上主机日期用机器日期
		String thirdTime = DateUtil.getMacDateTimeShort();// 获取日期yyyyMMddHHmmss
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		AgtTranList agtTranList = new AgtTranList();

		try {
			AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();
			agtCustomerInfo = agtCustomerInfoDao.qryAgtCustomerInfoAndAgentInfo( context, tranConnection, secCompCode, stkAcct, acctNo );
			SFUtil.chkCond( context, agtCustomerInfo == null, "ST4377", "该客户不存在" );

			DBHandler.beginTransaction( context, tranConnection );// 开启事务1
			agtTranList.setTranFunc( "812322" );
			agtTranList.setBusinessType( "MS999" );
			agtTranList.setTranType( "0" );
			agtTranList.setPicsLogNo( initSeqId );
			agtTranList.setFrontLogNo( frontLogNo );
			agtTranList.setAcctNo( acctNo );
			agtTranList.setStkCode( secCompCode );
			agtTranList.setStkAcct( stkAcct );
			agtTranList.setCompAcct( compAcct );
			agtTranList.setCcyCode( "RMB" );
			agtTranList.setTranAmount( BizUtil.getCobankTranAmount( context, tranAmount ) );
			agtTranList.setVoidFlag( voidFlag );
			agtTranList.setHostCheck( hostCheck );
			// --add by songshimin date 2018-04-17 10:26
			agtTranList.setInsertTime( thirdTime );
			// Remark 因为对账文件shell脚本查询的是insertTime，故此在此插入insertTime的值
			agtTranList.setThirdTime( thirdTime );
			agtTranList.setTradeDate( tranDate );
			agtTranList.setTranResult( tranResult );
			agtTranList.setBankId( agtCustomerInfo.getBankId() );
			agtTranList.setOpenBranch( agtCustomerInfo.getOpenBranch() );
			agtTranList.setBankAcct( agtCustomerInfo.getAgtAgentInfo().getBankAcct() );
			agtTranListDao.saveAgtTranList( context, tranConnection, agtTranList );// 预计上主机流水
			DBHandler.commitTransaction( context, tranConnection );// 提交事务1
		} catch( SFException e ) {
			SFLogger.error( context, e.getMessage() );
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", String.format( "预记合作行交易流水失败" ) );
		}
	}

}
