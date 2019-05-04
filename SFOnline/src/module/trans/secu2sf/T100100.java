package module.trans.secu2sf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctAdmDetail;
import module.bean.AcctJour;
import module.bean.AgtAgentInfo;
import module.bean.AgtCardBinInfo;
import module.bean.AgtCustomerInfo;
import module.bean.AllyData;
import module.bean.BankSignData;
import module.bean.BankUnit;
import module.bean.InvestData;
import module.bean.LocalInfo;
import module.bean.ProductInfo;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.BankUnitCache;
import module.cache.ParamCache;
import module.dao.AcctAdmDetailDao;
import module.dao.AcctJourDao;
import module.dao.AgtAgentInfoDao;
import module.dao.AgtCardBinInfoDao;
import module.dao.AgtCustomerInfoDao;
import module.dao.AllyDataDao;
import module.dao.BankSignDataDao;
import module.dao.CardBinInfoDao;
import module.dao.ProductInfoDao;
import module.trans.TranBase;
import module.trans.sf2bankchl.QryBalClient;

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
import common.util.SpecialSecuUtil;

import core.log.SFLogger;

/**
 * A股券商端发起--联机金融类交易
 * T100100分为三个子交易：
 * 
 * 1. 预指定/ 2. 一步式签约/ 3. 激活银行预约((银行端预约，券商端确认))
 * 
 * 处理逻辑：
 *          预指定：
 *                  1.接收券商交易 （<深>Acmt.001.01/11002，<直>6021/612201）
 *					2.检查请求包有效性,检查券商和客户资料等
 *					3.返回券商 （<深>Acmt.002.01/11002，<直>6021/612201）
 *					
 *			一步式签约：
 *					1.接收券商交易 （<深>Acmt.001.01/11001，<直>6025/612213）
 *					2.检查请求包有效性,检查券商和客户资料等
 *					3.对公客户不允许做一步式签约
 *					4.银证联名卡判断：调用卡管BCARD93432(卡片类型查询)和BCARD93439(卡片等级查询)交易
 *					5.调用M7030查询客户信息，调用R3036查询卡状态字、客户号，调用R3034查询卡余额开户网点信息
 *					6、代理：调用612326上代理系统校验一步式签约客户信息
 *					7.调用R3042置卡状态字
 *					8.返回券商 （<深>Acmt.002.01/11001，<直>6025/612213）
 *
 *			激活预约：
 *					1.接收券商交易 （<深>Acmt.001.01/11013（平安）31002（华泰联合），<直>6028/612212）
 *					2.检查请求包有效性
 *					3.对公客户不允许办理此交易
 *					4. 对于我行个人客户，调用D+接口R3036查询账户状态
 *					5. 对于我行个人客户，调用D+接口R3042置账户状态字
 *					6.返回券商 （<深>Acmt.002.01/11013（平安）31002（华泰联合），<直>6028/612212）
 *
 * tran code :100100
 * @author 吕超鸿
 */
public class T100100 extends TranBase {

	private String subTxSeqId = null;// 16位服务平台流水号

	private String initSeqId = null;// 14位日志号(前置流水号)

	private String secSeqId = null;// 券商流水号

	private String txSeqId = null; // 8位发起方流水号

	private String chlSeqId = null; // 22位交易流水号

	private String txDate = null;// 券商请求包中的日期

	private String txTime = null;// 获取时间戳HHMMSSttt

	private String hostIdType = null;// 新主机证件类型

	private String secAcct = null;// 生成投资人管理帐号

	private String userId = null;// 客户编号

	private String signType = null;// 签约类型，区分预指定、一步式、激活预约

	private String secuType = null;// 券商类型，区分直联、深证通

	private String secCompCode = null;// 券商代码

	private String acctId = null;// 银行卡号

	private String initSide = null;// 渠道

	private int secAcctSeq = 0;// 保证金管理账号序列号

	private String acctChldNum = null;// 账户顺序号

	private String dbSignFlag = null; // 已有签约表中签约标识

	private String invType = null;// 客户类型

	private String invIdCode = null;// 客户证件号码

	private String capAcct = null;// 资金帐号

	private String invName = null;// 客户姓名

	private String curCode = null;// 币种

	private String workMode = null;// 工作模式

	private String unitTellerId = null;// 操作员编号

	private InvestData investData = null;// 投资人信息

	private SecCompData secu = null;// 券商信息

	private SignAccountData signAccountData = null;// 签约信息

	private ProductInfo productInfo = null;// 产品信息

	private boolean succResFlag = false;// 正确流程特殊返回标识

	private DecimalFormat df = new DecimalFormat( "#0.00" );// 数字格式化，保留两位小数

	private CardBinInfoDao cardBinInfoDao = new CardBinInfoDao();

	private AgtCardBinInfoDao agtCardBinInfoDao = new AgtCardBinInfoDao();

	private BankSignDataDao bankSignDataDao = new BankSignDataDao();

	private AgtCustomerInfoDao agtCustomerInfoDao = new AgtCustomerInfoDao();

	private AcctJourDao acctJourDao = new AcctJourDao();

	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize( Context context ) throws SFException {
		KeyedCollection kColl = null;
		KeyedCollection scAcctKcoll = null;
		KeyedCollection acctSvcrKcoll = null;

		try {

			initSeqId = SFUtil.getDataValue( context, SFConst.PUBLIC_LOG_ID );// 14位前置流水号
			subTxSeqId = BizUtil.getSubTxSeqId( initSeqId );// 16位服务平台流水号
			chlSeqId = BizUtil.getChlSeqId( context, subTxSeqId ); // 获取22位流水号;

			// 先以直联方式获取，取不到以深证通方式获取券商代码
			secCompCode = SFUtil.getReqDataValue( context, "SEC_COMP_CODE" );
			if( SFUtil.isEmpty( secCompCode ) ) {
				kColl = SFUtil.getDataElement( context, "Acmt00101" );
				scAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				acctSvcrKcoll = SFUtil.getDataElement( context, scAcctKcoll, "AcctSvcr" );
				secCompCode = SFUtil.getDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE" );// 券商编号
			}

			SFUtil.chkCond( context, SFUtil.isEmpty( secCompCode ), "ST5711", String.format( "此券商信息不存在" ) );
			secu = secCompDataDao.qrySecCompData( context, tranConnection, secCompCode );
			SFUtil.chkCond( context, ( null == secu ), "ST5711", String.format( "此券商信息不存在" ) );

			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU_TYPE, secu.getSztFlag() );// 将券商类型放入上下文中
			SFUtil.setDataValue( context, SFConst.PUBLIC_SECU, secu );

			// 预指定标识存储context中，用于标识交易类型（SECU_TRANS_TYPE = 1:券商预指定 2：一歩式签约 3：激活银行预约）
			signType = getsignTypeByTxCode( context );

			secuType = SFUtil.getDataValue( context, SFConst.PUBLIC_SECU_TYPE );// 券商类型

			SFUtil.chkCond( context, ( SFUtil.isEmpty( secuType ) ), "ST5701", String.format( "[券商类型]不能为空" ) );
			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式

				secSeqId = SFUtil.getReqDataValue( context, "SEC_SEQ_ID" );// 券商流水号
				unitTellerId = SFUtil.getDataValue( context, "ZLSECU_REQUEST_HEAD.TELLERID" );// 操作员编号

				// 组装券商通用上下文
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_I" );
				SFUtil.setDataValue( context, keyColl, "BEGIN_BAL", AmtUtil.conv2SecuDivAmount( context, null != SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ? SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) : "0.00" ) );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", SFUtil.getReqDataValue( context, "ID_TYPE_ZL" ) );
				if( !keyColl.containsKey( "INTER_FLAG" ) ) {
					SFUtil.addDataField( context, keyColl, "INTER_FLAG", null );
				}
				SFUtil.addDataField( context, keyColl, "SEX", null );
				SFUtil.addDataField( context, keyColl, "NATIONALITY", null );

			} else if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式

				KeyedCollection msgHdrKcoll = SFUtil.getDataElement( context, "MsgHdr" );
				KeyedCollection senderKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Sender" );
				KeyedCollection refKcoll = SFUtil.getDataElement( context, msgHdrKcoll, "Ref" );
				secSeqId = SFUtil.getDataValue( context, refKcoll, "Ref" );// 券商流水号

				KeyedCollection custKcoll = SFUtil.getDataElement( context, kColl, "Cust" );
				KeyedCollection agtKcoll = SFUtil.getDataElement( context, kColl, "Agt" );
				KeyedCollection bkAcctKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );

				String idType = SFUtil.getDataValue( context, custKcoll, "ID_TYPE_SZT" );// 证件类型
				String invTypeSZT = SFUtil.getDataValue( context, custKcoll, "INV_TYPE_SZT" );// 客户类型
				String sex = SFUtil.getDataValue( context, custKcoll, "SEX" );// 性别
				String nationality = SFUtil.getDataValue( context, custKcoll, "NATIONALITY" );// 国籍
				String beginBal = ( null != SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) ? SFUtil.objectToString( SFUtil.getDataValue( context, scBalKcoll, "BEGIN_BAL" ) ) : "0.00";

				// 组装券商通用上下文
				KeyedCollection keyColl = new KeyedCollection( "100100_I" );

				SFUtil.addDataField( context, keyColl, "CUR_CODE", SFUtil.getDataValue( context, kColl, "CUR_CODE" ) );// 币种
				SFUtil.addDataField( context, keyColl, "INV_NAME", SFUtil.getDataValue( context, custKcoll, "INV_NAME" ) );// 客户名称
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", SFUtil.getDataValue( context, custKcoll, "INV_ID_CODE" ) );// 客户类型
				SFUtil.addDataField( context, keyColl, "ID_TYPE", idType );// 证件类型
				SFUtil.addDataField( context, keyColl, "INV_TYPE", BizUtil.convSZT2SFInvType( context, invTypeSZT ) );// idType_SZT 转换客户类型为 1：个人 2：机构
				SFUtil.addDataField( context, keyColl, "TRN_ID_CODE", SFUtil.getDataValue( context, agtKcoll, "TRN_ID_CODE" ) );
				SFUtil.addDataField( context, keyColl, "ACCT_ID", SFUtil.getDataValue( context, bkAcctKcoll, "ACCT_ID" ) );// 发展卡卡号/对公账号
				SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.addDataField( context, keyColl, "CAP_ACCT", SFUtil.getDataValue( context, scAcctKcoll, "CAP_ACCT" ) );// 券商端资金台账号
				SFUtil.addDataField( context, keyColl, "SEC_SEQ_ID", secSeqId );// 券商流水号
				SFUtil.addDataField( context, keyColl, "LEGAL_NAME", SFUtil.getDataValue( context, kColl, "LEGAL_NAME" ) );
				SFUtil.addDataField( context, keyColl, "LEGAL_ID_TYPE", SFUtil.getDataValue( context, kColl, "LEGAL_ID_TYPE" ) );
				SFUtil.addDataField( context, keyColl, "SEX", sex );// 根据客户类型设置性别
				SFUtil.addDataField( context, keyColl, "NATIONALITY", nationality );// 设置国籍
				SFUtil.addDataField( context, keyColl, "ADDR", SFUtil.getDataValue( context, custKcoll, "ADDR" ) );
				SFUtil.addDataField( context, keyColl, "ZIP", SFUtil.getDataValue( context, custKcoll, "ZIP" ) );
				SFUtil.addDataField( context, keyColl, "EMAIL_ADDR", SFUtil.getDataValue( context, custKcoll, "EMAIL_ADDR" ) );
				SFUtil.addDataField( context, keyColl, "FAX", SFUtil.getDataValue( context, custKcoll, "FAX" ) );
				SFUtil.addDataField( context, keyColl, "MOBILE", SFUtil.getDataValue( context, custKcoll, "MOBILE" ) );
				SFUtil.addDataField( context, keyColl, "PHONE", SFUtil.getDataValue( context, custKcoll, "PHONE" ) );
				SFUtil.addDataField( context, keyColl, "TRN_NAME", SFUtil.getDataValue( context, agtKcoll, "TRN_NAME" ) );
				SFUtil.addDataField( context, keyColl, "TRN_ID_TYPE", SFUtil.getDataValue( context, agtKcoll, "TRN_ID_TYPE" ) );
				SFUtil.addDataField( context, keyColl, "BEGIN_BAL", df.format( new BigDecimal( beginBal ) ) );
				SFUtil.addDataField( context, keyColl, "INIT_SIDE", SFConst.INIT_SIDE_SECU );// 券商渠道 送S
				SFUtil.addDataField( context, keyColl, "INTER_FLAG", BizUtil.convInterFlag( idType ) );// 转换境内外标志 0 和 1
				SFUtil.addDataField( context, "SEC_BRCH_ID", SFUtil.getDataValue( context, senderKcoll, "BrchId" ) );// 分支机构标识

				if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
					/* 平安证券新增推荐人信息 和预约号 */
					String bookNo = null;// 预约号
					if( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) {
						KeyedCollection bookRefKcoll = SFUtil.getDataElement( context, kColl, "BookRef" );
						bookNo = SFUtil.getDataValue( context, bookRefKcoll, "Ref" );

						KeyedCollection recomKcoll = SFUtil.getDataElement( context, kColl, "Recom" );
						String cusMagNo = SFUtil.getDataValue( context, recomKcoll, "RemmCode" );// 平安证券新增推荐人信息
						SFUtil.addDataField( context, keyColl, "CUS_MAG_NO", cusMagNo );
					} else {
						bookNo = SFUtil.getDataValue( context, kColl, "UEAppntID" );
					}
					SFUtil.addDataField( context, keyColl, "BOOK_NO", bookNo );
				}

				SFUtil.addDataElement( context, keyColl );
			}
			// 默认客户类型设置
			SFUtil.setReqDataValue( context, "INV_TYPE", SFUtil.isEmpty( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) ? SFConst.INV_TYPE_RETAIL : ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );

			// 默认性别设置
			SFUtil.setReqDataValue( context, "SEX", BizUtil.convSZT2SFSex( context, ( String )SFUtil.getReqDataValue( context, "SEX" ), invType ) );

			// 默认境内外标识设置
			SFUtil.setReqDataValue( context, "INTER_FLAG", SFUtil.isEmpty( SFUtil.getReqDataValue( context, "INTER_FLAG" ) ) ? SFConst.INTER_FLAG_DOMESTIC : ( String )SFUtil.getReqDataValue( context, "INTER_FLAG" ) );

			// 默认国籍设置CHN
			SFUtil.setReqDataValue( context, "NATIONALITY", BizUtil.convSZT2SFNationality( context, ( String )SFUtil.getReqDataValue( context, "NATIONALITY" ) ) );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	public void doHandle( Context context ) throws SFException {

		// 一歩式签约&激活银行预约
		if( !SFConst.SIGN_TYPE_PRE.equals( signType ) ) {

			// 上主机，查询卡类型和卡等级、卡账户信息、置卡状态
			doHost( context );
		}

		// 一步式已经成功直接返回券商成功
		if( succResFlag && SFConst.SIGN_TYPE_ONE.equals( signType ) ) {
			return;
		}

		// 建立投资人信息，插入流水，写入签约记录，写入账户管理交易明细
		addPublicInfo( context );

		// 预指定已经成功直接返回券商成功
		if( succResFlag && SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
			return;
		}

		// 组包返回券商
		doSecu( context );

		// 将成功签约的数据插入到表TRDAUTOBECIF中，后续轮询发协议到BECIF
		addAutoBecif( context );
	}

	public void addPublicInfo( Context context ) throws SFException {
		SFLogger.info( context, "addPublicInfo()开始" );

		try {

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // 券商预指定

				DBHandler.beginTransaction( context, tranConnection ); // 开启事务1

				// 若客户信息为空,则插入客户信息表
				if( null == investData ) {
					addInvestData( context );
				}

				// 保存交易流水
				addAcctJour( context );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务1

				/* 检查签约关系 */
				chkSignRalation( context );

				// 成功预指定直接返回
				if( succResFlag ) {
					return;
				}

				DBHandler.beginTransaction( context, tranConnection ); // 开启事务3

				/* 写入签约记录，在券商预指定时确定客户签约模式 */
				addSignAccountData( context );// 置签约标志SignFlag：3-券商预指定

				/* 写入账户管理交易明细 */
				addAcctAdmDetail( context );

				// 更改流水状态标识
				updAcctJourJourFlag( context );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务3

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // 一歩式签约

				DBHandler.beginTransaction( context, tranConnection );// 开启事务3

				/* 插入签约记录TRDSignAccountData，状态字标志SignFlag为 '1-已设置'，签约状态为：'1-签约处理中' */
				addSignAccountData( context );
				if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {
					// 如果是合作行，查询agt_customerinfo中是否有status = 4的客户，如果有就直接，需要先删除再插入
					AgtCustomerInfo customerInfo = agtCustomerInfoDao.qryAgtCustomerInfoByStatus( context, tranConnection, capAcct, secCompCode, SFConst.SIGN_FLAG_CANCEL );
					if( null != customerInfo ) {
						agtCustomerInfoDao.delAgtCustomerInfo( context, tranConnection, capAcct, secCompCode );
					}
					// 插入合作行客户信息
					addAgtCustomerInfo( context );

					// 更新trdsignaccountdata修改initside标志为A
					updSignAccountDataInitSide( context, SFConst.INIT_SIDE_COBANK );

				}

				// 写入账户管理交易明细,插入TRDAcctAdmDetail
				addAcctAdmDetail( context );

				// 更改流水状态标识
				updAcctJourJourFlag( context );

				// 更新签约余额表trdsignaccountdata的lmcard的状态值cardLevel
				String cardLevel = SFUtil.getReqDataValue( context, "CARD_LEVEL" );
				if( SFUtil.isNotEmpty( cardLevel ) ) {
					updSignAccountDataLmCard( context, cardLevel );
				}

				DBHandler.commitTransaction( context, tranConnection );// 提交事务3

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // 激活银行预约

				DBHandler.beginTransaction( context, tranConnection ); // 开启事务1

				// 若客户信息为空,则插入客户信息表
				if( null == investData ) {
					addInvestData( context );
				}

				// 记录交易流水
				addAcctJour( context );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务1

				DBHandler.beginTransaction( context, tranConnection );// 开启事务2

				// 将TrdBankSignData表该数据写入TRDSignAccountData表
				addSignAccountData( context );

				addBankSignData( context );

				// 写入账户管理交易明细
				addAcctAdmDetail( context );

				// 银证联名卡预约激活成功后，需要将trdbanksigndata的lmcard的值更新过去
				if( SFConst.SECU_PINGANZQ.equals( secCompCode ) ) {
					BankSignData bankSignData = bankSignDataDao.qryBankSignData( context, tranConnection, secCompCode, acctId );
					if( null != bankSignData ) {
						// 需要更新联名卡
						if( SFUtil.isNotEmpty( bankSignData.getLmCard() ) ) {
							updSignAccountDataLmCard( context, bankSignData.getLmCard() );
						}
						// 需要更新email字段
						if( SFUtil.isNotEmpty( bankSignData.getEmail() ) ) {
							updInvestDataEmail( context, bankSignData.getEmail() );
						}
					}
				}

				// 券商激活成功之后将银行预约记录移至历史表中
				bankSignDataDao.migrateBankSignDataToHistory( context, tranConnection, acctId, secCompCode );

				bankSignDataDao.delBankSignData( context, tranConnection, acctId, secCompCode );

				// 更改流水状态标识
				updAcctJourJourFlag( context );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务2

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "addPublicInfo()结束" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		SFLogger.info( context, "doHost()开始" );

		try {

			if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

				// 上主机-一歩式签约
				doHostOfOneSign( context );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {

				// 上主机-激活银行预约
				doHostOfActive( context );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHost()结束" );
	}

	/**
	 * 
	 * 一步式签约：上卡管判断是否是联名卡   银证联名卡判断 & 卡等级查询 
	 * @param context
	 * @throws SFException
	 */
	private void doHostOfOneSign( Context context ) throws SFException {
		SFLogger.info( context, "doHostOfOneSign()开始" );
		Context msgContext = null;// 接收响应

		String resultFlag = "1";// 是否第三方存管系统卡校验位，0-是，1-否

		try {

			// 组发送报文map参数
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msg.put( "ACCT_ID", acctId );// 账号ACCT_NO
			msg.put( "CUR_CODE", curCode );// 币种CCY
			msg.put( "INV_NAME", invName );// 账户名称ACCT_NAME
			msg.put( "SEC_COMP_CODE", secCompCode );// 券商代码

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			if( acctId.startsWith( "621626" ) || acctId.startsWith( "623058" ) ) {// 平安卡

				// 调用公共查询联名卡和卡等级方法返回报文
				msgContext = BizUtil.qryCardTypeClient( context, msg );
				SFLogger.info( context, String.format( "查询主机数据返回卡等级 [%s]", SFUtil.getDataValue( msgContext, "CARD_LEVEL" ) ) );
				SFUtil.addDataField( context, keyIColl, "CARD_LEVEL", SFUtil.getDataValue( msgContext, "CARD_LEVEL" ) );

			}

			if( SFConst.INIT_SIDE_SECU.equals( initSide ) ) { // 我行

				SFLogger.info( context, "上主机查询客户信息开始" );

				// 上主机查询个人客户信息 验证卡号的证件类型、证件号码、户名
				BizUtil.chkKeyInvestInfoClient( context, msg );

				SFLogger.info( context, "上主机查询客户信息结束" );

				SFLogger.info( context, "上主机查询卡状态开始" );

				// 上主机查询卡状态，判断卡是否挂失（正式、口头）
				msgContext = BizUtil.qryCardAttrClient( context, msg );

				// 获取主机返回状态字
				resultFlag = SFUtil.getDataValue( msgContext, "RESULT_FLAG" );

				String ecifNo = SFUtil.getDataValue( msgContext, "MSG_O.BECIF_NO" );// 取客户ECIF号
				SFUtil.chkCond( msgContext, SFUtil.isEmpty( ecifNo ), "ST5602", String.format( "上主机获取[客户号]失败" ) );
				SFLogger.info( msgContext, String.format( "客户ECIF号[%s]", ecifNo ) );
				SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );// 客户ECIF号放入context

				SFLogger.info( context, "上主机查询卡状态结束" );

				SFLogger.info( context, "上主机查询卡主帐号开始" );

				// 上主机查询卡主帐号,取新卡号的开户网点和主帐号
				qryHostAcctIdAndBranchId( context );

				SFLogger.info( context, "上主机查询卡主帐号结束" );

			} else if( SFConst.INIT_SIDE_COBANK.equals( initSide ) ) {// 新增一步式签约代理银行卡信息的校验

				SFLogger.info( context, "上合作行一步式签约代理银行卡信息校验开始" );

				// 合作行一步式签约代理银行卡信息校验
				chkKeyInvestorByCoBank( context );

				SFLogger.info( context, "上合作行一步式签约代理银行卡信息校验结束" );

			}

			DBHandler.beginTransaction( context, tranConnection );// 开启事务1

			// 若客户信息为空,则插入客户信息表
			if( null == investData ) {
				addInvestData( context );
			}

			// 记录交易流水
			addAcctJour( context );

			DBHandler.commitTransaction( context, tranConnection ); // 提交事务1

			// 检查签约关系
			chkSignRalation( context );

			// 成功直接返回券商
			if( succResFlag ) {
				return;
			}

			// 调用FCR 31号接口，该卡无50状态时，且是我行卡才需上FCR置状态
			if( !"0".equals( resultFlag ) && SFConst.INIT_SIDE_SECU.equals( initSide ) ) {

				// 调用上FCR增加卡状态字方法
				BizUtil.setCardStatusWord( context, acctId, "A", null );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHostOfOneSign()结束" );
	}

	/**
	 * 
	 * 上主机查询卡主帐号,取新卡号的开户网点和主帐号
	 * @param context
	 * @throws SFException
	 */
	private void qryHostAcctIdAndBranchId( Context context ) throws SFException {
		SFLogger.info( context, "qryHostAcctIdAndBranchId()开始" );
		KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
		KeyedCollection kColl = null;
		try {
			// 组发送报文map参数
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msg.put( "ACCT_ID", acctId );// 账号ACCT_NO
			msg.put( "CUR_CODE", curCode );// 币种CCY
			msg.put( "INV_NAME", invName );// 账户名称ACCT_NAME

			QryBalClient qryBalClient = new QryBalClient();
			Context msgContext = qryBalClient.send( context, msg );

			String retFlag = SFUtil.getDataValue( msgContext, SFConst.PUBLIC_RET_FLAG );
			SFUtil.chkCond( context, !SFConst.RET_SUCCESS.equals( retFlag ), "ST5601", String.format( "上主机[查询卡状态]失败" ) );

			if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {// 个人
				IndexedCollection iColl1 = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
				kColl = ( KeyedCollection )iColl1.getElementAt( 0 );// 获取响应数组
				String hostAcctId = SFUtil.getDataValue( msgContext, kColl, "ACCT_ID" );// 卡主帐号 CustNo参考接口字段定义值

				SFLogger.info( context, String.format( "取卡下主帐号Account:[%s]", hostAcctId ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( hostAcctId ), "ST5602", "上主机获取[卡主账号]失败" );
				SFUtil.addDataField( context, keyIColl, "SAV_ACCT", hostAcctId );

				String pbcAcctType = SFUtil.getDataValue( msgContext, kColl, "PBC_ACCT_TYPE" );// 取人行账户分类
				SFLogger.info( context, String.format( "取人行账户分类pbcAcctType:[%s]", pbcAcctType ) );
				SFUtil.chkCond( context, SFUtil.isEmpty( pbcAcctType ), "ST5602", "上主机获取[人行账户分类]失败" );

				if( "2".equals( pbcAcctType ) ) {// 二类账户
					SFLogger.info( context, String.format( "该券商已进入二类账户校验，SecCompCode:[%s]", secCompCode ) );
					// 没查到允许二类账户签约记录，拦截报错
					SFUtil.chkCond( context, !"1".equals( secu.getIIAcctFlag() ), "ST5421", String.format( "该券商不允许办理[二类账户签约]" ) );
					SFLogger.info( context, String.format( "该券商二类账户校验通过" ) );
				}
			}

			// 修订开户网点号从AppHead获取错误改成R3034-TRAN_LIST_ARRAY节点获取 edit by lch 20180515
			IndexedCollection tranListIColl = SFUtil.getDataElement( msgContext, "MSG_O_ICOLL" );
			KeyedCollection trankColl = ( KeyedCollection )tranListIColl.getElementAt( 0 );// 获取响应数组
			String openDepId = SFUtil.getDataValue( msgContext, trankColl, "OPEN_DEP_ID" );// 开户网点号
			SFLogger.info( context, String.format( "取卡开户网点号[%s]", openDepId ) );
			SFUtil.chkCond( context, SFUtil.isEmpty( openDepId ), "ST5602", String.format( "上主机获取[账号开户网点]失败" ) );

			SFUtil.setDataValue( context, keyIColl, "OPEN_DEP_ID", openDepId.trim() );// 开户网点号
			SFUtil.setDataValue( context, keyIColl, "DEP_ID", openDepId.trim() );// 操作网点号

			BankUnit bankUnit = BankUnitCache.getValue( openDepId );// 根据开户网点号获取机构缓存对象
			SFUtil.chkCond( context, ( null == bankUnit ), "ST5801", String.format( "查询[开户网点号所属分行]失败" ) );
			SFLogger.info( context, String.format( "开户网点号所属分行[%s]", bankUnit.getBranchId() ) );
			SFUtil.setDataValue( context, keyIColl, "OPEN_BRANCH_ID", bankUnit.getBranchId() );// 开户网点号所属分行
			SFUtil.setDataValue( context, keyIColl, "BRANCH_ID", bankUnit.getBranchId() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "qryHostAcctIdAndBranchId()结束" );
	}

	/**
	 * 
	 * 合作行系统查询客户信息
	 * @param context
	 * @throws SFException
	 */
	private void chkKeyInvestorByCoBank( Context context ) throws SFException {
		SFLogger.info( context, "chkKeyInvestorByCoBank()开始" );
		try {
			AgtAgentInfoDao agtAgentInfoDao = new AgtAgentInfoDao();
			AgtAgentInfo agtAgentInfo = agtAgentInfoDao.qryAgtAgentInfoAndCardBinInfo( context, tranConnection, acctId );
			SFUtil.chkCond( context, ( null == agtAgentInfo ), "ST5799", String.format( "资金账号[%s]券商代码[%s]客户账户[%s]一步式签约校验客户信息的交易,该合作行没有开通一步式签约，直接返回失败", capAcct, secCompCode, acctId ) );
			String bankId = agtAgentInfo.getBankId();
			String coBankBranchId = agtAgentInfo.getBranchCode();

			// 组合作行系统查询客户信息报文
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "SEC_COMP_CODE", secCompCode );// 券商代码SecCode
			msg.put( "CAP_ACCT", capAcct );// 证券资金台账号CapAcct
			msg.put( "INV_NAME", invName );// 客户名称InvName
			msg.put( "ID_TYPE", hostIdType );// 证件类型IdType
			msg.put( "INV_ID_CODE", invIdCode );// 证件号码InvIdCode
			msg.put( "ACCT_ID", acctId );// 银行账号account_no
			msg.put( "CUR_CODE", curCode );// 币种CurCode
			msg.put( "REMARK", "一步式签约" );// 备注Memo

			// 公共方法上合作行校验一步式签约客户信息--方法内已处理异常和失败
			Context msgContext = BizUtil.chkKeyInvestorBycoBankClient( context, tranConnection, msg, bankId );

			// 上合作行返回成功
			String coBankStatus = SFUtil.getDataValue( msgContext, "810026_O.STATUS" );// 账户状态Status
			String agtBankId = SFUtil.getDataValue( msgContext, "810026_O.REMARK" );// 备注Memo

			SFUtil.chkCond( context, ( "1".equals( coBankStatus ) ), "ST5719", String.format( "代理客户银行卡状态异常不允许办理三管签约" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( coBankBranchId ) ), "ST5614", String.format( "取卡开户网点号失败" ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			SFUtil.setDataValue( context, keyIColl, "OPEN_DEP_ID", coBankBranchId );
			SFUtil.setDataValue( context, keyIColl, "DEP_ID", coBankBranchId );

			// 合作行返回合作银行号 agtBankId 无合作行agtBankId沿用bankId
			if( SFUtil.isNotEmpty( agtBankId ) ) {
				SFLogger.info( context, String.format( "合作行返回合作银行号为[%s]", agtBankId ) );
				AgtCardBinInfo agtCardBinInfo = agtCardBinInfoDao.qryAgtCardBinInfo( context, tranConnection, agtBankId, acctId );
				if( null == agtCardBinInfo ) {
					agtBankId = bankId;
				}
			} else {
				agtBankId = bankId;
			}

			// 将卡号赋值到签约表savacct字段
			SFUtil.addDataField( context, keyIColl, "SAV_ACCT", acctId );// 主机返回资金帐号填充上下文
			SFUtil.addDataField( context, keyIColl, "BANK_ID", agtBankId );// 主机返回银行号填充上下文

			/* 对港澳台居民往来大陆通行证进行处理 */

			// 通过开户网点号找到开户分行号
			BankUnit bankUnit = BankUnitCache.getValue( coBankBranchId );// 根据开户网点号获取机构缓存对象
			SFUtil.chkCond( context, ( null == bankUnit ), "ST5801", String.format( "查询[开户网点号所属分行]失败" ) );
			SFLogger.info( context, String.format( "开户网点号所属分行[%s]", bankUnit.getBranchId() ) );
			SFUtil.setDataValue( context, keyIColl, "OPEN_BRANCH_ID", bankUnit.getBranchId() );// 开户网点号所属分行
			SFUtil.setDataValue( context, keyIColl, "BRANCH_ID", bankUnit.getBranchId() );

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkKeyInvestorByCoBank()结束" );
	}

	/**
	 * 
	 * 激活银行预约上主机查询卡状态，增加卡状态字方法
	 * @param context
	 * @throws SFException
	 */
	private void doHostOfActive( Context context ) throws SFException {
		SFLogger.info( context, "doHostOfActive()开始" );
		try {

			// 组发送报文map参数
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put( "BIZ_SEQ_NO", chlSeqId );// 业务流水号
			msg.put( "CONSUMER_SEQ_NO", chlSeqId );// 交易流水号
			msg.put( "ACCT_ID", acctId );// 账号ACCT_NO
			msg.put( "CUR_CODE", curCode );// 币种CCY
			msg.put( "INV_NAME", invName );// 账户名称ACCT_NAME

			// 上主机查询卡状态，判断卡是否挂失（正式、口头）
			Context msgContext = BizUtil.qryCardAttrClient( context, msg );

			// 获取主机返回状态字
			String resultFlag = SFUtil.getDataValue( msgContext, "RESULT_FLAG" );

			String ecifNo = SFUtil.getDataValue( msgContext, "MSG_O.BECIF_NO" );// 取客户ECIF号
			SFUtil.chkCond( msgContext, SFUtil.isEmpty( ecifNo ), "ST5602", String.format( "上主机获取[客户号]失败" ) );
			SFLogger.info( msgContext, String.format( "客户ECIF号ECIF_NO:[%s]", ecifNo ) );

			KeyedCollection keyIColl = SFUtil.getDataElement( context, "100100_I" );
			SFUtil.addDataField( context, keyIColl, "ECIF_NO", ecifNo );// 客户ECIF号放入context

			// 调用增加卡状态字方法
			if( !"0".equals( resultFlag ) ) {
				BizUtil.setCardStatusWord( context, acctId, "A", null );
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			e.printStackTrace();
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doHostOfActive()结束" );
	}

	/**
	 * 检查签约关系
	 *  1：预指定 2 ：一步式签约
	 *
	 * @param context
	 * @throws SFException
	 */
	private void chkSignRalation( Context context ) throws SFException {
		SFLogger.info( context, "chkSignRalation()开始" );

		try {

			String dbAcctId = null;
			/* 检查签约关系 */
			SignAccountData signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );

			// 无签约关系记录
			if( signAccountData == null || SFUtil.isEmpty( signAccountData.getSignFlag() ) ) {
				dbSignFlag = "-";
			} else {
				// 数据库中银行帐号
				dbAcctId = signAccountData.getAcctId();
				// 数据库中签约标识
				dbSignFlag = signAccountData.getSignFlag();
				// 保证金管理帐号不一致
				if( !secAcct.equals( signAccountData.getSecAcct() ) && ( !SFConst.SIGN_FLAG_CANCEL.equals( dbSignFlag ) || "-".equals( dbSignFlag ) ) ) {
					dbSignFlag = "*";
				}
			}

			// 签约处理中、签约确认处理中将券商的卡号赋值给dbAcctId
			if( SFUtil.isNotEmpty( acctId ) && SFUtil.isEmpty( dbAcctId ) && ( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) ) ) {
				dbAcctId = acctId;
			}

			// 签约成功但dbAcctId和acctId银行卡不一样
			if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) && !dbAcctId.equals( acctId ) ) {
				dbSignFlag = "*";
			}

			// 签约信息不符
			SFUtil.chkCond( context, ( "*".equals( dbSignFlag ) ), "ST5720", String.format( "签约关系已存在,且不符" ) );

			// 预指定：0、1、5、6、9签约标识不允许办理
			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) || SFConst.SIGN_FLAG_BANK_PRE.equals( dbSignFlag ) || SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_PRE_IN_PROCESS.equals( dbSignFlag ) ) {
					SFUtil.chkCond( context, "ST5590", String.format( "客户当前状态不允许办理预指定" ) );
				} else if( SFConst.SIGN_FLAG_SECU_PRE.equals( dbSignFlag ) ) {
					// 置成功状态字
					succResFlag = true;
					// 组包返回券商
					doSecu( context );
					return;
				}
				// 一步式：1、3、5、6、9签约标识不允许办理
			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {
				if( SFConst.SIGN_FLAG_SIGN_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_SECU_PRE.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CONFIRM_IN_PROCESS.equals( dbSignFlag ) || SFConst.SIGN_FLAG_CANCEL_PRE_IN_PROCESS.equals( dbSignFlag ) ) {
					SFUtil.chkCond( context, "ST5590", String.format( "客户当前状态不允许办理券商一步式签约" ) );
				} else if( SFConst.SIGN_FLAG_SIGN.equals( dbSignFlag ) ) {
					// 置成功状态字
					succResFlag = true;
					// 组包返回券商
					doSecu( context );
					return;
				}
			}

			/* 已撤销签约关系, 将撤销记录移到 TRDDesSignData 表中 */
			if( SFConst.SIGN_FLAG_CANCEL.equals( dbSignFlag ) || "-".equals( dbSignFlag ) ) {

				/* 余额不为0,客户当前状态不允许办理预指定 */
				SFUtil.chkCond( context, ( null != signAccountData && SFUtil.object2Double( signAccountData.getAcctBal() ) > 0 ), "ST5331", String.format( "[余额不为0]，不允许办理此交易" ) );

				/* 国泰君安,当天撤销后不允许再预指定或签约 */
				if( SFConst.SECU_GUOTAIJAZQ.equals( secCompCode ) || SFConst.SECU_GUOTAIJAXY.equals( secCompCode ) ) {
					SignAccountData signAccountData2 = signAccountDataDao.qrySignAccountDataByCloseDate( context, tranConnection, secCompCode, capAcct, txDate );

					SFUtil.chkCond( context, ( null != signAccountData2 ), "ST5541", String.format( "签约状态为[撤销签约]，国泰君安客户当天撤销后不允许办理此交易" ) );
				}

				DBHandler.beginTransaction( context, tranConnection ); // 开启事务2

				/* 插入TRDDesSignData */
				signAccountDataDao.migrateSignAccountData( context, tranConnection, secCompCode, capAcct );

				/* 删除TRDSignAccountData */
				signAccountDataDao.delSignAccountData( context, tranConnection, secCompCode, capAcct );

				DBHandler.commitTransaction( context, tranConnection ); // 提交事务2
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "chkSignRalation()结束" );
	}

	/**
	 * 根据接口交易码判断交易类型
	 * @param txCode
	 * @return
	 */
	public String getsignTypeByTxCode( Context context ) throws SFException {
		SFLogger.info( context, "getsignTypeByTxCode()开始" );

		String txCode = SFUtil.getDataValue( context, SFConst.PUBLIC_SERVER_ID );// 请求接口号
		if( SFConst.PRE_SIGN_SZT.equals( txCode ) || SFConst.PRE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_PRE;
		} else if( SFConst.ONE_SIGN_SZT.equals( txCode ) || SFConst.ONE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_ONE;
		} else if( SFConst.ACTIVE_SIGN_SZT_PA.equals( txCode ) || SFConst.ACTIVE_SIGN_SZT_OTHER.equals( txCode ) || SFConst.ACTIVE_SIGN_SZT_HT.equals( txCode ) || SFConst.ACTIVE_SIGN_ZL.equals( txCode ) ) {
			signType = SFConst.SIGN_TYPE_ACTIVE;
		}

		SFLogger.info( context, "getsignTypeByTxCode()结束" );
		return signType;
	}

	/**
	 * 检查BIN,区分我行还是合作行客户
	 * @param context
	 * @param acctId
	 * @param sql
	 * @return
	 * @throws SFException
	 */
	private void chkCardBin( Context context, String acctId ) throws SFException {
		SFLogger.info( context, "chkCardBin()开始" );
		if( SFUtil.isNotEmpty( acctId ) ) {
			// 查询我行卡bin数
			long sfCardBinCount = cardBinInfoDao.qryCardBinInfoCount( context, tranConnection, acctId );
			if( sfCardBinCount > 0 ) {
				initSide = SFConst.INIT_SIDE_SECU;
			} else {
				// 查询合作行卡bin数
				long coBankCardBinCount = agtCardBinInfoDao.qryCardBinInfoCount( context, tranConnection, acctId );
				if( coBankCardBinCount > 0 ) {
					initSide = SFConst.INIT_SIDE_COBANK;
				}
			}
		}
		SFLogger.info( context, "chkCardBin()结束" );
	}

	/**
	 * 
	 * 签约关系不存在，生存管理帐号序号
	 * 获取最大secAcctSeq
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private int getSecAcctSeq( Context context ) throws SFException {
		SignAccountData signAccountData = signAccountDataDao.qrySignAccountDataMaxSeqBySecAcct( context, tranConnection, secAcct );
		secAcctSeq = signAccountData.getSecAcctSeq() + 1;
		SFLogger.info( context, String.format( "生成secAcctSeq=%s", secAcctSeq ) );
		return secAcctSeq;
	}

	/**
	 * 建立投资人信息 
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addInvestData( Context context ) throws SFException {
		try {

			InvestData investData = new InvestData();
			investData.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			investData.setSecAcct( secAcct );
			investData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			investData.setIdType( hostIdType );
			investData.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			investData.setTrnName( ( String )SFUtil.getReqDataValue( context, "TRN_NAME" ) );
			investData.setTrnIdCode( ( String )SFUtil.getReqDataValue( context, "TRN_ID_CODE" ) );
			investData.setNationality( ( String )SFUtil.getReqDataValue( context, "NATIONALITY" ) );
			investData.setSex( ( String )SFUtil.getReqDataValue( context, "SEX" ) );
			investData.setTrnIdType( ( String )SFUtil.getReqDataValue( context, "TRN_ID_TYPE" ) );
			investData.setTrnPhone( ( String )SFUtil.getReqDataValue( context, "TRN_PHONE" ) );
			investData.setAddr( ( String )SFUtil.getReqDataValue( context, "ADDR" ) );
			investData.setZip( ( String )SFUtil.getReqDataValue( context, "ZIP" ) );
			investData.setPhone( ( String )SFUtil.getReqDataValue( context, "PHONE" ) );
			investData.setMobile( ( String )SFUtil.getReqDataValue( context, "MOBILE" ) );
			investData.setFax( ( String )SFUtil.getReqDataValue( context, "FAX" ) );
			investData.setInterFlag( ( String )SFUtil.getReqDataValue( context, "INTER_FLAG" ) );
			investData.setEmailAddr( ( String )SFUtil.getReqDataValue( context, "EMAIL_ADDR" ) );
			investData.setTrnMobile( ( String )SFUtil.getReqDataValue( context, "TRN_MOBILE" ) );
			investData.setLegalName( ( String )SFUtil.getReqDataValue( context, "LEGAL_NAME" ) );
			investData.setLegalIdCode( ( String )SFUtil.getReqDataValue( context, "LEGAL_ID_TYPE" ) );
			investData.setMemo( "" );

			if( SFConst.SIGN_TYPE_ONE.equals( signType ) || SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // 一歩式签约&激活预约
				if( SFUtil.isNotEmpty( secAcct ) && SFConst.INIT_SIDE_SECU.equals( initSide ) ) { // 我行卡才更新BECIF号
					investData.setBecifNo( ( String )SFUtil.getReqDataValue( context, "ECIF_NO" ) );
				}
			}
			investDataDao.saveInvestData( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入投资人失败" );
		}
	}

	/**
	 * 更新投资人Email
	 * @param context
	 * @param email
	 * @return
	 * @throws SFException
	 */
	private void updInvestDataEmail( Context context, String email ) throws SFException {
		try {
			InvestData investData = new InvestData();
			investData.setEmailAddr( email );
			investData.setSecAcct( secAcct );

			investDataDao.saveInvestData( context, tranConnection, investData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新投资人失败" );
		}
	}

	/**
	 * 插入流水
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAcctJour( Context context ) throws SFException {
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setSubTxSeqId( subTxSeqId );
			acctJour.setTxDate( txDate );
			acctJour.setUserId( userId );
			acctJour.setInitSide( SFConst.INIT_SIDE_SECU );
			acctJour.setTxSeqId( txSeqId );
			acctJour.setSecSeqId( secSeqId );
			acctJour.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			acctJour.setIdType( hostIdType );
			acctJour.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctJour.setSecAcct( secAcct );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setSecCompCode( secCompCode );
			acctJour.setCapAcct( capAcct );
			acctJour.setAcctId( acctId );
			acctJour.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			acctJour.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			acctJour.setCurCode( curCode );
			acctJour.setDcFlag( SFConst.CREDIT_FLAG );
			acctJour.setAcctBal( new BigDecimal( 0.00 ) );
			acctJour.setAbst( "" );
			acctJour.setJourFlag( "33" );
			acctJour.setTxTime( txTime );
			acctJour.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
			acctJour.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );
			acctJour.setUnitTellerId( unitTellerId );
			acctJour.setCashRemitFlag( SFConst.CASH_FLAG );
			acctJour.setAcctDealId( initSeqId );
			acctJour.setProductType( "03" );
			acctJour.setColFlag( "0" );
			acctJour.setMemo( "" );
			acctJour.setTranSeqId( chlSeqId );
			acctJour.setBusiSeqId( chlSeqId );
			acctJour.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // 券商预指定
				acctJour.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctJour.setAbstractStr( "券商端预指定" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_PRE_SIGN );
				acctJour.setBusiType( "21" );
			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // 一歩式签约
				acctJour.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctJour.setAbstractStr( "券商一步式签约" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SECU_ONE_SIGN );
				acctJour.setBusiType( "22" );
			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // 激活银行预约
				acctJour.setTxAmount( new BigDecimal( 0.00 ) );
				acctJour.setAbstractStr( "激活银行预指定" );
				acctJour.setTxCode( SFConst.SF_TX_CODE_SIGN );
				acctJour.setBusiType( "22" );
			}

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入交易流水失败" );
		}
	}

	/**
	 * 更新流水状态标识
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void updAcctJourJourFlag( Context context ) throws SFException {
		try {
			AcctJour acctJour = new AcctJour();
			acctJour.setJourFlag( "00" );
			acctJour.setSecAcctSeq( secAcctSeq );
			acctJour.setTxDate( txDate );
			acctJour.setSubTxSeqId( subTxSeqId );

			acctJourDao.saveAcctJour( context, tranConnection, acctJour );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新交易流水失败" );
		}
	}

	/**
	 * 
	 *
	 * 插入账户管理交易明细
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAcctAdmDetail( Context context ) throws SFException {
		try {
			AcctAdmDetail acctAdmDetail = new AcctAdmDetail();
			acctAdmDetail.setTxDate( txDate );
			acctAdmDetail.setInitSide( SFConst.INIT_SIDE_SECU );
			acctAdmDetail.setUserId( userId );
			acctAdmDetail.setTxSeqId( txSeqId );
			acctAdmDetail.setSecSeqId( secSeqId );
			acctAdmDetail.setSubTxSeqId( subTxSeqId );
			acctAdmDetail.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			acctAdmDetail.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			acctAdmDetail.setInvIdCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			acctAdmDetail.setSecAcct( secAcct );
			acctAdmDetail.setSecAcctSeq( secAcctSeq );
			acctAdmDetail.setSecCompCode( secCompCode );
			acctAdmDetail.setCapAcct( capAcct );
			acctAdmDetail.setAcctId( acctId );
			acctAdmDetail.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			acctAdmDetail.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			acctAdmDetail.setOldAcctId( acctId );
			acctAdmDetail.setCurCode( curCode );
			acctAdmDetail.setDcFlag( SFConst.CREDIT_FLAG );
			acctAdmDetail.setAbStract( " " );
			acctAdmDetail.setJourFlag( "00" );
			acctAdmDetail.setTxTime( txTime );
			acctAdmDetail.setUnitTellerId( unitTellerId );
			acctAdmDetail.setCashRemitFlag( SFConst.CASH_FLAG );
			acctAdmDetail.setCusMagNo( ( String )SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) );
			acctAdmDetail.setAcctDealId( initSeqId );// 14位日志号
			acctAdmDetail.setColFlag( "0" );
			acctAdmDetail.setMemo( "" );

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // 券商预指定
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctAdmDetail.setAbstractStr( "券商端预指定" );
				acctAdmDetail.setSignFlag( dbSignFlag );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SECU_PRE );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_PRE_SIGN );
				acctAdmDetail.setBusiType( "21" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // 一歩式签约
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
				acctAdmDetail.setAbstractStr( "券商一步式签约" );
				acctAdmDetail.setSignFlag( dbSignFlag );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SIGN );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SECU_ONE_SIGN );
				acctAdmDetail.setBusiType( "22" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // 激活银行预约
				acctAdmDetail.setIdType( hostIdType );
				acctAdmDetail.setTxAmount( new BigDecimal( 0.00 ) );
				acctAdmDetail.setAbstractStr( "券商激活银行预指定" );
				acctAdmDetail.setSignFlag( SFConst.SIGN_FLAG_BANK_PRE );
				acctAdmDetail.setnSignFlag( SFConst.SIGN_FLAG_SIGN );
				acctAdmDetail.setTxCode( SFConst.SF_TX_CODE_SIGN );
				acctAdmDetail.setBusiType( "22" );
				acctAdmDetail.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
				acctAdmDetail.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );

			}
			AcctAdmDetailDao acctAdmDetailDao = new AcctAdmDetailDao();
			acctAdmDetailDao.saveAcctAdmDetail( context, tranConnection, acctAdmDetail );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入账户管理明细失败" );
		}
	}

	/**
	 * 插入签约账户余额
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addSignAccountData( Context context ) throws SFException {
		try {

			SignAccountData signAccountData = new SignAccountData();
			signAccountData.setInitSide( SFConst.INIT_SIDE_SECU );
			signAccountData.setSecAcct( secAcct );
			signAccountData.setSecAcctSeq( secAcctSeq );
			signAccountData.setSecCompCode( secCompCode );
			signAccountData.setProductType( "03" );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setCurCode( curCode );
			signAccountData.setCashRemitFlag( SFConst.CASH_FLAG );
			signAccountData.setShsthCode( "" );
			signAccountData.setSzsthCode( "" );
			signAccountData.setAcctId( acctId );
			signAccountData.setSavAcct( ( String )SFUtil.getReqDataValue( context, "SAV_ACCT" ) );
			signAccountData.setOpenDepId( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			signAccountData.setOpenBranchId( ( String )SFUtil.getReqDataValue( context, "OPEN_BRANCH_ID" ) );
			signAccountData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			signAccountData.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			signAccountData.setDepId( ( String )SFUtil.getReqDataValue( context, "DEP_ID" ) );
			signAccountData.setBranchId( ( String )SFUtil.getReqDataValue( context, "BRANCH_ID" ) );
			signAccountData.setDesDepId( "" );
			signAccountData.setDesBranchId( "" );
			signAccountData.setUnitTellerId( unitTellerId );
			signAccountData.setDesUnitTellerId( "" );
			signAccountData.setCusMagno( ( String )SFUtil.getReqDataValue( context, "CUS_MAG_NO" ) );
			signAccountData.setOpenDate( ( String )SFUtil.getReqDataValue( context, "MAX_OPEN_DATE" ) );
			signAccountData.setPreTxDate( "19000101" );
			signAccountData.setBeginBal( new BigDecimal( 0.00 ) );
			signAccountData.setAcctBal( new BigDecimal( 0.00 ) );
			signAccountData.setIsMailBill( "0" );
			signAccountData.setMailDate( "" );
			signAccountData.setFlags( SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );// 营业部代码

			if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) { // 券商预指定
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SECU_PRE );//
				signAccountData.setOpenDate( txDate );//
				signAccountData.setSignMode( "1" );//

			} else if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) { // 一歩式签约

				signAccountData.setAcctChldNum( "00" );
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
				signAccountData.setStatFlag( "1" );
				signAccountData.setActiveFlag( "1" );
				signAccountData.setOpenDate( txDate );
				signAccountData.setSignDate( txDate );
				signAccountData.setSignMode( "3" );

			} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) { // 激活银行预约

				signAccountData.setAcctChldNum( acctChldNum );// 账户顺序号
				signAccountData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
				signAccountData.setStatFlag( "1" );
				signAccountData.setOpenDate( ( String )SFUtil.getReqDataValue( context, "MAX_TX_DATE" ) );
				signAccountData.setSignDate( txDate );
				signAccountData.setSignMode( "2" );
				signAccountData.setChannel( ( String )SFUtil.getReqDataValue( context, "CHANNEL" ) );
			}
			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入签约记录失败" );
		}
	}

	/**
	 * 更新签约账户余额联名卡信息
	 * @param context
	 * @param cardLv
	 * @return
	 * @throws SFException
	 */
	private void updSignAccountDataLmCard( Context context, String cardLv ) throws SFException {

		try {
			SignAccountData signAccountData = signAccountDataDao.qrySignAccountData( context, tranConnection, capAcct, secCompCode, false );
			signAccountData.setLmCard( cardLv );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新签约账户联名卡信息失败" );
		}
	}

	/**
	 * 更新签约账户余额发起方
	 * @param context
	 * @param initSide
	 * @return
	 * @throws SFException
	 */
	private void updSignAccountDataInitSide( Context context, String initSide ) throws SFException {
		try {

			SignAccountData signAccountData = new SignAccountData();
			signAccountData.setInitSide( initSide );
			signAccountData.setSecAcct( secAcct );
			signAccountData.setSecAcctSeq( secAcctSeq );
			signAccountData.setCapAcct( capAcct );
			signAccountData.setSecCompCode( secCompCode );

			signAccountDataDao.saveSignAccountData( context, tranConnection, signAccountData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "更新签约账户发起方信息失败" );
		}
	}

	/**
	 * 插入银行预指定信息
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addBankSignData( Context context ) throws SFException {
		try {
			BankSignData bankSignData = new BankSignData();
			bankSignData.setSignFlag( SFConst.SIGN_FLAG_SIGN );
			bankSignData.setCapAcct( capAcct );
			bankSignData.setOpenDate( txDate );
			bankSignData.setOpenTime( txTime );
			bankSignData.setSubTxSeqId2( subTxSeqId );
			bankSignData.setSecCompCode( secCompCode );
			bankSignData.setAcctId( acctId );
			bankSignData.setCurCode( curCode );
			bankSignData.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			bankSignData.setTxDate( ( String )SFUtil.getReqDataValue( context, "MAX_TX_DATE" ) );
			bankSignData.setTxTime( txTime );

			bankSignDataDao.saveBankSignData( context, tranConnection, bankSignData );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入银行预指定信息失败" );
		}
	}

	/**
	 * 更新联网行客户信息
	 * @param context
	 * @return
	 * @throws SFException
	 */
	private void addAgtCustomerInfo( Context context ) throws SFException {
		try {
			AgtCustomerInfo agtCustomerInfo = new AgtCustomerInfo();
			agtCustomerInfo.setInvType( ( String )SFUtil.getReqDataValue( context, "INV_TYPE" ) );
			agtCustomerInfo.setAcctNo( acctId );
			agtCustomerInfo.setStkAcct( capAcct );
			agtCustomerInfo.setBankId( ( String )SFUtil.getReqDataValue( context, "BANK_ID" ) );// 有合作行返回合作银行号使用agtBankId,本行使用bankId
			agtCustomerInfo.setOpenBranch( ( String )SFUtil.getReqDataValue( context, "OPEN_DEP_ID" ) );
			agtCustomerInfo.setStkCode( secCompCode );
			agtCustomerInfo.setCurCode( curCode );
			agtCustomerInfo.setInvName( ( String )SFUtil.getReqDataValue( context, "INV_NAME" ) );
			agtCustomerInfo.setIdType( ( String )SFUtil.getReqDataValue( context, "ID_TYPE" ) );// 合作行取证件类型赋值
			agtCustomerInfo.setInvidCode( ( String )SFUtil.getReqDataValue( context, "INV_ID_CODE" ) );
			agtCustomerInfo.setOpenDate( txDate );
			agtCustomerInfo.setStatus( "0" );
			agtCustomerInfo.setMemo( "一步式签约" );

			agtCustomerInfoDao.saveAgtCustomerInfo( context, tranConnection, agtCustomerInfo );
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
			SFUtil.chkCond( context, "ST4895", "插入联网行客户信息失败" );
		}
	}

	@Override
	public void doSecu( Context context ) throws SFException {
		SFLogger.info( context, "doSecu()开始" );
		try {

			if( SFConst.SECU_ZL.equals( secuType ) ) { // 直联模式
				SFUtil.setResDataValue( context, "SEC_ACCT", secAcct );
				SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
				SFUtil.setResDataValue( context, "CAP_ACCT", capAcct );
				SFUtil.setResDataValue( context, "CUR_CODE", curCode );
				SFUtil.setResDataValue( context, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setResDataValue( context, "SEC_SEQ_ID", ( String )SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );

				// 券商预指定&一歩式签约
				if( SFConst.SIGN_TYPE_PRE.equals( signType ) || SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

					SFUtil.setResDataValue( context, "BEGIN_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );

					// 激活银行预约
				} else if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {

					SFUtil.setResDataValue( context, "ACCT_ID", acctId );

				}
			}

			if( SFConst.SECU_SZT.equals( secuType ) ) { // 深证通模式

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00201" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// 组<BkAcct>组件
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

				// 组<ScBal>组件
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				SFUtil.setDataValue( context, scBalKcoll, "BEGIN_BAL", SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );

			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
		SFLogger.info( context, "doSecu()结束" );
	}

	/**
	 * 重写父类前置检查
	 */
	@Override
	protected void chkStart( Context context ) throws SFException {

		try {

			invType = SFUtil.getReqDataValue( context, "INV_TYPE" ); // 客户类型
			invIdCode = SFUtil.getReqDataValue( context, "INV_ID_CODE" ); // 证件号码
			acctId = SFUtil.getReqDataValue( context, "ACCT_ID" ); // 发展卡卡号/对公账号
			capAcct = SFUtil.getReqDataValue( context, "CAP_ACCT" ); // 券商端资金台账号
			invName = SFUtil.getReqDataValue( context, "INV_NAME" ); // 客户名称
			curCode = SFUtil.getReqDataValue( context, "CUR_CODE" ); // 币种
			txSeqId = BizUtil.getTxSeqId( secSeqId.trim() );// 生成发起方流水号
			String idType = SFUtil.getReqDataValue( context, "ID_TYPE" ); // 转换前客户证件类型

			// 先预定义返回报文-交易异常退出时返回 --开始
			if( SFConst.SECU_ZL.equals( secuType ) ) {// 直联模式
				KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_O" );
				SFUtil.setDataValue( context, keyColl, "CUR_CODE", curCode );// 币种
				SFUtil.setDataValue( context, keyColl, "SEC_COMP_CODE", secCompCode );// 券商代码
				SFUtil.setDataValue( context, keyColl, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, keyColl, "ACCT_ID", acctId );
				SFUtil.setDataValue( context, keyColl, "SEC_ACCT", secAcct );
				SFUtil.setDataValue( context, keyColl, "SUB_TX_SEQ_ID", subTxSeqId );
				SFUtil.setDataValue( context, keyColl, "SEC_SEQ_ID", SFUtil.getReqDataValue( context, "SEC_SEQ_ID" ) );
				SFUtil.setDataValue( context, keyColl, "BEGIN_BAL", String.valueOf( AmtUtil.conv2SecuMulAmount( context, SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) ) );
			}
			// 深证通模式
			if( SFConst.SECU_SZT.equals( secuType ) ) {

				KeyedCollection kColl = SFUtil.getDataElement( context, "Acmt00201" );
				SFUtil.setDataValue( context, kColl, "CUR_CODE", curCode );

				// 组<BkAcct>组件
				KeyedCollection bkAccyKcoll = SFUtil.getDataElement( context, kColl, "BkAcct" );
				SFUtil.setDataValue( context, bkAccyKcoll, "ACCT_ID", acctId );

				// 组<ScAcct>组件
				KeyedCollection secAcctKcoll = SFUtil.getDataElement( context, kColl, "ScAcct" );
				KeyedCollection acctSvcrKcoll = SFUtil.getDataElement( context, secAcctKcoll, "AcctSvcr" );

				SFUtil.setDataValue( context, secAcctKcoll, "CAP_ACCT", capAcct );
				SFUtil.setDataValue( context, acctSvcrKcoll, "SEC_COMP_CODE", secCompCode );

				// 组<ScBal>组件
				KeyedCollection scBalKcoll = SFUtil.getDataElement( context, kColl, "ScBal" );
				SFUtil.setDataValue( context, scBalKcoll, "BEGIN_BAL", SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );
			}
			// 检查关键字段是否为空
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secCompCode ) ), "ST4496", String.format( "券商编号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( capAcct ) ), "ST4388", String.format( "券商端客户资金台账号不能为空" ) );
			SFUtil.chkCond( context, ( !SFConst.SIGN_TYPE_PRE.equals( signType ) && SFUtil.isEmpty( acctId ) ), "ST4388", String.format( "银行账号不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( idType ) ), "ST4385", String.format( "证件类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invIdCode ) ), "ST4386", String.format( "证件号码不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invName ) ), "ST4377", String.format( "投资者名称不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( invType ) ), "ST4390", String.format( "客户类型不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( curCode ) ), "ST4439", String.format( "币别不能为空" ) );
			SFUtil.chkCond( context, ( SFUtil.isEmpty( secSeqId ) ), "ST4430", String.format( "券商流水号不能为空" ) );

			// 检查金额小数位有效性
			AmtUtil.chkAmtValid( context, SFUtil.objectToString( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) );

			// 增加判断证件类型与客户类型的关系，证件类型为10的，客户类型只能为1；证件类型为51、52的，客户类型只能为2
			if( SFConst.INV_TYPE_CORP.equals( invType ) && "10".equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_TYSHXYDM.equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_YYZZ.equals( idType ) || SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_COMPANY_ZZJGDMZ.equals( idType ) ) {
				SFUtil.chkCond( context, "ST4386", String.format( "证件类型不符" ) );
			}

			// 银河证券、五矿证券须校验券商营业部信息
			SpecialSecuUtil.chkSecBrchIdBySecCompCode( context, secCompCode, SFUtil.objectToString( SFUtil.getDataValue( context, "SEC_BRCH_ID" ) ) );

			// 证件类型20、21不允许做预指定和一步式交易
			SFUtil.chkCond( context, ( SFConst.ID_TYPE_PERSON_WGGMQTZJ.equals( idType ) || "21".equals( idType ) ) && !SFConst.SIGN_TYPE_ACTIVE.equals( signType ), "ST5100", String.format( "证件类型不符" ) );

			// 一歩式签约
			if( SFConst.SIGN_TYPE_ONE.equals( signType ) ) {

				// 机构户不允许做此交易
				SFUtil.chkCond( context, ( SFConst.INV_TYPE_CORP.equals( invType ) ), "ST5422", String.format( "[对公客户]不允许办理此交易" ) );

				// 检查卡BIN
				chkCardBin( context, acctId );

				// 不是我行和合作行卡不能一步式签约
				SFUtil.chkCond( context, ( SFUtil.isEmpty( initSide ) ), "ST5103", String.format( "该卡不能做此交易" ) );

			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		long bankSignCount = 0;
		long sfCapAcctNum = 0;
		String invIdCode18Card19 = null; // 15位转18位证件号码,20世纪
		String invIdCode18Card20 = null;
		try {

			// 检查是否存在重复流水
			BizUtil.chkRepeatAcctJour( context, tranConnection );

			KeyedCollection keyColl = SFUtil.getDataElement( context, "100100_I" );
			userId = secu.getUserId();
			txDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// 交易日期
			txTime = DateUtil.getMacTime();// 取物理时间
			hostIdType = SFUtil.getDataValue( context, SFConst.PUBLIC_ID_TYPE );// 券商证件类型转换为主机证件类型
			workMode = SFUtil.getDataValue( context, SFConst.PUBLIC_WORKMODE );

			// 合作行个人客户只能正常交易时间一步式签约
			SFUtil.chkCond( context, ( SFConst.INIT_SIDE_COBANK.equals( initSide ) && !SFConst.WORKMODE_NORMAL.equals( workMode ) ), "ST5104", String.format( "非正常交易时间段该卡不能做此交易" ) );

			investData = investDataDao.qryInvestData( context, tranConnection, hostIdType, invIdCode );
			if( null != investData ) {
				secAcct = investData.getSecAcct();
				// 预指定 &一步式 检查属于银行与制定的资金帐号只要未激活都不允许做与制定交易
				if( !SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
					AllyDataDao allyDataDao = new AllyDataDao();
					AllyData allyData = allyDataDao.qryAllyDataByCapAcct( context, tranConnection, secCompCode, capAcct );
					long bankCount = bankSignDataDao.qryBankSignDataChkInfoBySignFlag( context, tranConnection, capAcct, secCompCode );

					// 该资金账号处于银行预指定状态或属于银行预指定资金账号，不允许做券商预指定交易
					SFUtil.chkCond( context, ( allyData != null && SFConst.SIGN_FLAG_BANK_PRE.equals( allyData.getUseFlag() ) || bankCount != 0 ), "ST4525", String.format( "银行预指定资金账号不允许做券商预指定交易" ) );

					// 统计该客户的资金账号签约数，最多同时签约n(参数配置)个资金账号
					long signCount = signAccountDataDao.qrySignAccountDataTotalCountBySecAcct( context, tranConnection, secAcct );

					// 核对证件类型、证件号码，身份证号15/18位兼容
					if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) && invIdCode.length() == 15 ) {
						invIdCode18Card19 = BizUtil.converTo18Card19( invIdCode ); // 15位转18位证件号码,20世纪
						invIdCode18Card20 = BizUtil.converTo18Card20( invIdCode ); // 15位转18位证件号码，21世纪
					}
					bankSignCount = bankSignDataDao.qryBankSignDataCountByInvIdCode( context, tranConnection, hostIdType, invIdCode, invIdCode18Card19, invIdCode18Card20 );

					if( SFConst.INV_TYPE_RETAIL.equals( invType ) ) {
						sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_RETAIL", "CAPACCT_NUM" ).getValue() );
						SFUtil.chkCond( context, ( SFUtil.add( bankSignCount, signCount ) > sfCapAcctNum ), "ST5100", String.format( "个人客户最多可以同时签约[%s]个资金账号", sfCapAcctNum ) );
					} else {
						sfCapAcctNum = Long.valueOf( ParamCache.getValue( "SF_CORP", "CAPACCT_NUM" ).getValue() );
						SFUtil.chkCond( context, ( signCount > sfCapAcctNum ), "ST5100", String.format( "对公客户最多可以同时签约[%s]个资金账号", sfCapAcctNum ) );
					}
				}

			} else {
				secAcct = BizUtil.genSecAcctId( context );// 生成投资人管理帐号
				SFUtil.chkCond( context, SFUtil.isEmpty( secAcct ), "ST5711", "调用[生成投资人管理账号]失败" );
			}
			secAcctSeq = getSecAcctSeq( context );

			// 激活银行预约
			if( SFConst.SIGN_TYPE_ACTIVE.equals( signType ) ) {
				// 检查TRDSignAccountData表券商资金账号是否已存在
				signAccountData = SFUtil.getDataValue( context, SFConst.PUBLIC_SIGN_ACCOUNT_DATA );// 签约信息
				if( null != signAccountData ) {
					String signFlag = signAccountData.getSignFlag();
					SFUtil.chkCond( context, ( !SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5590", String.format( "当前签约状态不允许办理此业务" ) );
					SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL.equals( signFlag ) ), "ST5541", String.format( "签约状态为[撤销签约]，不允许做券商激活银行预指定" ) );
				}

				// 深证通金额必须为0
				SFUtil.chkCond( context, ( SFConst.SECU_SZT.equals( secuType ) && SFUtil.object2Double( SFUtil.getReqDataValue( context, "BEGIN_BAL" ) ) != 0 ), "ST4433", String.format( "金额必须为零" ) );
				String bookNo = SFUtil.getReqDataValue( context, "BOOK_NO" );// 预约号

				// 检查当前券商是否允许办理激活银行预指定交易
				SFUtil.chkCond( context, ( !"1".equals( secu.getBankPreSignFlag() ) ), "ST5421", String.format( "该券商不允许办理[银行预指定交易]" ) );

				// 预约号不能为空
				SFUtil.chkCond( context, ( "1".equals( secu.getCheckBookNoFlag() ) && SFUtil.isEmpty( bookNo ) ), "ST5701", String.format( "[预约流水号]不能为空" ) );

				// 检查投资人信息是否与银行预指定信息一致
				// 因联合的预约号可能相同，所以要找最后一条记录
				BankSignData bankSignData = bankSignDataDao.qryBankSignDataMaxTxDate( context, tranConnection, invName, secCompCode, acctId, curCode );
				SFUtil.chkCond( context, ( null == bankSignData ), "ST5712", String.format( "客户类型不符" ) );
				SFUtil.chkCond( context, ( SFUtil.isEmpty( bankSignData.getTxDate() ) || SFUtil.isEmpty( bankSignData.getTxTime() ) ), "ST5042", String.format( "[客户信息]不符或无[银行预指定]信息" ) );
				SFUtil.addDataField( context, keyColl, "MAX_TX_DATE", bankSignData.getTxDate() );
				SFUtil.addDataField( context, keyColl, "CHANNEL", bankSignData.getChannel() );
				SFUtil.setDataValue( context, keyColl, "OPEN_DEP_ID", bankSignData.getOpenDepId() );
				SFUtil.setDataValue( context, keyColl, "OPEN_BRANCH_ID", bankSignData.getOpenBranchId() );
				SFUtil.setDataValue( context, keyColl, "DEP_ID", bankSignData.getDepId() );
				SFUtil.setDataValue( context, keyColl, "BRANCH_ID", bankSignData.getBranchId() );
				// 账户顺序号
				acctChldNum = bankSignData.getAcctChldNum();

				// 校验客户类型
				SFUtil.chkCond( context, ( !invType.equals( bankSignData.getInvType() ) ), "ST4385", String.format( "客户类型不符" ) );

				// 按照主机证件类型进行比较
				SFUtil.chkCond( context, ( !hostIdType.equals( bankSignData.getIdType() ) ), "ST4385", String.format( "证件类型不符" ) );

				// 核对预约号
				if( "1".equals( secu.getCheckBookNoFlag() ) ) {
					SFUtil.chkCond( context, ( !bookNo.equals( bankSignData.getBookNo() ) ), "ST4513", String.format( "预约流水信息不符" ) );
				}
				// 核对广发的资金账号
				SpecialSecuUtil.chkCapAcctBySecCompCode( context, secCompCode, capAcct, bankSignData.getCapAcct() );

				// 核对证件类型、证件号码，身份证号15/18位兼容
				String bankInvIdCode = bankSignData.getInvIdCode();
				if( SFConst.INV_TYPE_RETAIL.equals( invType ) && SFConst.ID_TYPE_PERSON_SFZ.equals( hostIdType ) ) {
					BizUtil.chkRetailInvIdCode( context, invIdCode, bankInvIdCode );
				}
				String bankSignFlag = bankSignData.getSignFlag();
				SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_SIGN.equals( bankSignFlag ) ), SFConst.RESPCODE_SUCCCODE, String.format( "该银行预指定信息已激活成功" ) );
				SFUtil.chkCond( context, ( SFConst.SIGN_FLAG_CANCEL_PRE.equals( bankSignFlag ) ), "ST5581", String.format( "签约状态为[预约已撤销]，不允许办理此交易" ) );
				SFUtil.chkCond( context, ( ( SFConst.SIGN_FLAG_CANCEL_IN_PROCESS.equals( bankSignFlag ) ) || ( SFConst.SIGN_FLAG_BANK_PRE_IN_PROCESS.equals( bankSignFlag ) ) ), "ST5591", String.format( "签约状态不正常" ) );

			} else if( SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				ProductInfoDao productInfoDao = new ProductInfoDao();
				productInfo = productInfoDao.qryProductInfoByDepId( context, tranConnection, secCompCode );
				if( null != productInfo ) {
					SFUtil.setDataValue( context, keyColl, "OPEN_DEP_ID", productInfo.getTruOpnDepId() );
					SFUtil.setDataValue( context, keyColl, "OPEN_BRANCH_ID", productInfo.getOpenBranchId() );
					SFUtil.setDataValue( context, keyColl, "DEP_ID", productInfo.getTruOpnDepId() );
					SFUtil.setDataValue( context, keyColl, "BRANCH_ID", productInfo.getOpenBranchId() );
				}
			}

		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	/**
	 * 
	 * 一歩式签约&激活银行预约:
	 * 将成功签约的数据插入到表TRDAUTOBECIF中，后续轮询发协议到BECIF
	 * @param context
	 * @throws SFException
	 */
	private void addAutoBecif( Context context ) throws SFException {

		try {
			if( !SFConst.SIGN_TYPE_PRE.equals( signType ) ) {
				Map<String, Object> becifMap = new HashMap<String, Object>();
				becifMap.put( "ECIF_NO", SFUtil.objectToString( SFUtil.getReqDataValue( context, "ECIF_NO" ) ) );
				becifMap.put( "TX_DATE", txDate );
				becifMap.put( "TX_TIME", txTime );
				becifMap.put( "INV_TYPE", invType );
				becifMap.put( "SUB_TX_SEQ_ID", subTxSeqId );
				becifMap.put( "USER_ID", userId );
				becifMap.put( "SEC_COMP_CODE", secCompCode );
				becifMap.put( "ACCT_ID", acctId );
				becifMap.put( "INIT_SIDE", initSide );

				BizUtil.addAutoBecif( context, tranConnection, becifMap );
			}

		} catch( Exception e ) {
			SFLogger.error( context, e.getMessage() );
		}
	}
}
