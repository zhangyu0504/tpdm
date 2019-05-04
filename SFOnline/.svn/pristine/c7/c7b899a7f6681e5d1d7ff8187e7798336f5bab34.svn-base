package module.trans.bankchl2sf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import module.bean.AcctJour;
import module.bean.AllyData;
import module.bean.BankSignData;
import module.bean.LocalInfo;
import module.bean.SecCompData;
import module.dao.AcctJourDao;
import module.dao.AllyDataDao;
import module.dao.BankSignDataDao;
import module.trans.TranBase;
import module.trans.sf2secu.B2SRevocationClient;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 预约撤销 交易码:200121
 * 
 * 
 * 
 */
public class T200121 extends TranBase {

	private AcctJour acctJour = null;
	private AllyData allyData = null;
	private BankSignDataDao bankSignDataDao = new BankSignDataDao();
	private AcctJourDao acctJourDao = new AcctJourDao();

	@Override
	public void doHandle(Context context) throws SFException {
		try {
			// 开始发券商
			doSecu(context);

		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("交易失败", e.getMessage()));
		}
	}

	@Override
	public void doHost(Context context) throws SFException {

	}

	@Override
	public void doSecu(Context context) throws SFException {
		SFLogger.info(context, "doSecu()开始");

		String secSeqId = "";
		String ProductType = "03";
		String invName = SFUtil.getReqDataValue(context, "INV_NAME");// 客户名称
		String idType = SFUtil.getReqDataValue(context, "ID_TYPE");// 证件类型
		String idCode = SFUtil.getReqDataValue(context, "INV_ID_CODE");// 证件ID
		String invType = SFUtil.getReqDataValue(context, "INV_TYPE");// 客户类型
		String acctId = SFUtil.getReqDataValue(context, "ACCT_ID");// 银行帐号
		String bookNo = SFUtil.getReqDataValue(context, "BOOK_SERIAL_NO");// 预约顺序号
		String secCompCode = SFUtil.getReqDataValue(context, "SEC_COMP_CODE");// 券商代码
		String InitSide = SFUtil.getReqDataValue(context, "INIT_SIDE");// 发起渠道
		String tranDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();// 交易日期DateUtil.getMacDate();// 交易日期
		String tranTime = DateUtil.getMacTime();// 交易时间

		String frontLogNo = SFUtil.getDataValue(context, SFConst.PUBLIC_LOG_ID);// 14位初始流水号
		String subTxSeqId = BizUtil.getSubTxSeqId(frontLogNo);//16位subTxSeqId流水，initSeqId+机器码
		String newFrontLogNo = BizUtil.getChlSeqId(context,subTxSeqId);// 22位ESB流水号
		String tempBizSeq = SFUtil.getDataValue(context, "APP_HEAD.BIZ_SEQ_NO");//业务流水号
		int count = 0;

		SecCompData secCompData = SFUtil.getDataValue(context,
				SFConst.PUBLIC_SECU);// 券商信息
		idType = SFUtil.getDataValue(context, SFConst.PUBLIC_ID_TYPE);// 转换后的证件类型

		try {

			SFLogger.info(context, "券商是否可以做银行预指定业务开始开始");
			SFUtil.chkCond(context,!"1".equals(secCompData.getBankPreSignFlag()), "ST5422",	"该券商不允许办理[银行预指定交易]");
			SFLogger.info(context, "券商是否可以做银行预指定业务开始结束");

			SFLogger.info(context, "检查是否联名卡不能做此交易开始");
			AllyDataDao allyDataDao = new AllyDataDao();
			allyData = allyDataDao.qryAllyData(context, tranConnection, acctId);
			if (null != allyData) {
				//cardType = "2";
				SFUtil.chkCond(context, "ST5120", "银证联名卡不能做此交易");
			} else {
				//cardType = "1";
			}
			SFLogger.info(context, "检查是否联名卡不能做此交易结束");

			SFLogger.info(context, "检查TrdBankSignData表预约记录是否存在开始");
			BankSignData bankSignData = bankSignDataDao.qryBankSignDataCheck(context, tranConnection, secCompData.getSecCompCode(),acctId, bookNo);
			SFUtil.chkCond(context, SFUtil.isEmpty(bankSignData), "ST5803","查询[预约信息]，无记录");
			SFLogger.info(context, "检查TrdBankSignData表预约记录是否存在结束");
			String signFalg = bankSignData.getSignFlag();
			SFLogger.info(context, "插入流水表开始");
			DBHandler.beginTransaction(context, tranConnection);// 开启事务
			// 初始化变量
			acctJour = new AcctJour();
			acctJour.setTxDate(tranDate);
			acctJour.setUserId(SFConst.DEFAULT_USER_ID);
			acctJour.setInitSide(InitSide);
			acctJour.setTxSeqId(BizUtil.getTxSeqId(frontLogNo));
			acctJour.setSecSeqId(secSeqId);
			acctJour.setSubTxSeqId(subTxSeqId);
			acctJour.setInvType(invType);
			acctJour.setInvName(invName);
			acctJour.setIdType(idType);
			acctJour.setInvIdCode(idCode);
			acctJour.setSecAcct("888888888888888888888");
			acctJour.setSecAcctSeq(0);
			acctJour.setSecCompCode(secCompCode);
			acctJour.setCapAcct("");
			acctJour.setAcctId(acctId);
			acctJour.setSavAcct("");
			acctJour.setOpenDepId("");
			acctJour.setOpenBranchId("");
			acctJour.setCurCode("RMB");
			acctJour.setDcFlag("C");
			acctJour.setTxAmount(new BigDecimal(0.00));
			acctJour.setAcctBal(new BigDecimal(0.00));
			acctJour.setAbst("");
			acctJour.setAbstractStr("银行预约撤销");
			acctJour.setJourFlag("33");
			acctJour.setTxCode(SFConst.SF_TX_CODE_BANK_CANCEL);
			acctJour.setBusiType(SFConst.BUSI_TYPE_BANK_CANCEL);
			acctJour.setTxTime(tranTime);
			acctJour.setAbnDealTimes(0);
			acctJour.setDepId("");
			acctJour.setBranchId("");
			acctJour.setUnitTellerId("");
			acctJour.setCashRemitFlag(SFConst.REMIT_FLAG);
			acctJour.setAcctDealId(frontLogNo);
			acctJour.setProductType(ProductType);
			acctJour.setColFlag("0");
			acctJour.setMemo(bookNo);
			acctJour.setTranSeqId(newFrontLogNo);
			acctJour.setBusiSeqId(tempBizSeq);

			acctJourDao.saveAcctJour(context, tranConnection, acctJour);
			DBHandler.commitTransaction(context, tranConnection);// 提交事务

			SFLogger.info(context, "插入流水表结束");

			SFLogger.info(context, "发券商开始");
			DBHandler.beginTransaction(context, tranConnection);// 开启事务
			if ("1".equals(secCompData.getBankPreDesMode())) {
				// 修改TrdBankSignData表状态
				bankSignData = new BankSignData();
				bankSignData.setDelDate(tranDate);
				bankSignData.setDelTime(tranTime);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				count = bankSignDataDao.updBankSignDataBySignFlag(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895", String.format("修改数据库操作失败[%s]", "TRDBANKSIGNDATA"));

				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				// 预约撤销时将记录移至银行预指定历史信息表
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				bankSignDataDao.migrateBankSignDataToHistory(context, tranConnection, acctId, secCompCode);
				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				// 删除银行预指定信息表
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				bankSignData = new BankSignData();
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				bankSignDataDao.delBankSignDataByBookNo(context,tranConnection, acctId, secCompCode, bookNo);
				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				// 普通卡实时发券商处理
			} else {
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				// 修改TrdBankSignData表状态
				bankSignData = new BankSignData();
				bankSignData.setDelDate(tranDate);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				count = bankSignDataDao.updBankSignDataSignFlagToSendMSG(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895",String.format("修改数据库操作失败[%s]", "TRDBANKSIGNDATA"));

				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				String retFlag = null;
				Context secuContext = null;

				if (SFConst.SECU_SZT.equals(secCompData.getSztFlag())) {
					// 深证通
					// 发券商参数map
					Map<String, Object> secuMap = new HashMap<String, Object>();
					secuMap.put("BIZ_SEQ_NO", subTxSeqId);
					secuMap.put("INV_NAME", invName);
					secuMap.put("ID_TYPE", idType);
					secuMap.put("TRN_ID_CODE", idCode);
					secuMap.put("ACCT_ID", acctId);
					secuMap.put("CAP_ACCT", "");
					secuMap.put("SEC_COMP_TYPE", "");
					secuMap.put("SEC_COMP_CODE", secCompCode);
					secuMap.put("SEC_BRCH_ID", "");
					secuMap.put("BEGIN_BAL", "");
					secuMap.put("CUR_CODE", "RMB");
					secuMap.put("LEGAL_NAME", "");
					secuMap.put("LEGAL_ID_TYPE", "");

					B2SRevocationClient secuClient = new B2SRevocationClient();
					secuContext = secuClient.send(context, secuMap);

					retFlag = SFUtil.getDataValue(secuContext,SFConst.PUBLIC_RET_FLAG);

				} else {
					// 暂无直联模式券商，暂不处理，直接报错
					SFUtil.chkCond(context, "5003", "预约撤销暂无直联模式券商,银行处理失败！");
				}

				// 发送券商失败后处理
				if (!SFConst.RET_SUCCESS.equals(retFlag)) {
					SFLogger.info(context, "发送券商失败后处理-开始");
					// 修改TrdBankSignData表状态
					DBHandler.beginTransaction(context, tranConnection);// 开启事务bankSignData
					//String signFlag = bankSignData.getSignFlag();
					bankSignData = new BankSignData();
					bankSignData.setDelDate(tranDate);
					bankSignData.setSubTxSeqId3(subTxSeqId);
					bankSignData.setAcctId(acctId);
					bankSignData.setBookNo(bookNo);
					bankSignData.setSignFlag(signFalg);
					bankSignData.setDelTime("");
					bankSignData.setSecCompCode(secCompData.getSecCompCode());
					count = bankSignDataDao.updBankSignDataBySignFlagToRollback(context,tranConnection, bankSignData);
					SFUtil.chkCond(context, count <= 0, "ST4895", String.format("发送券商失败,更新[%s]表状态失败", "TRDBANKSIGNDATA"));
					DBHandler.commitTransaction(context, tranConnection);// 提交事务

					// 更新流水表
					DBHandler.beginTransaction(context, tranConnection);// 开启事务
					count = acctJourDao.updAcctJourDataByJourFlag(context,tranConnection, "02", secSeqId, "发送券商失败", tranDate,subTxSeqId);
					SFUtil.chkCond(context, count <= 0, "ST4895",String.format("发送券商失败,更新[%s]表状态失败", "TRDACCTJOUR"));
					DBHandler.commitTransaction(context, tranConnection);// 提交事务
					SFLogger.info(context, "发送券商失败后处理-结束");
				}
			}

			DBHandler.beginTransaction(context, tranConnection);// 开启事务
			acctJour = new AcctJour();
			acctJour.setSecSeqId(secSeqId);
			acctJour.setTxDate(tranDate);
			acctJour.setSubTxSeqId(subTxSeqId);
			count = acctJourDao.updAcctJourDataByJourFlag(context,tranConnection, "00", secSeqId, null, tranDate, subTxSeqId);
			SFUtil.chkCond(context, count <= 0, "ST4895",String.format("发送券商成功,更新[%s]表状态失败", "TRDACCTJOUR"));
			DBHandler.commitTransaction(context, tranConnection);// 提交事务

			// 实时发券商的后续处理
			if ("2".equals(secCompData.getBankPreDesMode())) {
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				bankSignData = new BankSignData();
				bankSignData.setSignFlag("8");
				bankSignData.setDelTime(tranTime);
				bankSignData.setAcctId(acctId);
				bankSignData.setSecCompCode(secCompCode);
				bankSignData.setBookNo(bookNo);
				bankSignData.setDelDate(tranDate);
				bankSignData.setSubTxSeqId3(subTxSeqId);
				count = bankSignDataDao.updBankSignDataBySignFlagToRollback(context, tranConnection, bankSignData);
				SFUtil.chkCond(context, count <= 0, "ST4895",String.format("发券商成功后,更新[%s]表失败", "TRDBANKSIGNDATA"));
				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				// 预约撤销时将记录移至银行预指定历史信息表
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				bankSignDataDao.migrateBankSignDataToHistory(context,tranConnection, acctId, secCompCode);
				DBHandler.commitTransaction(context, tranConnection);// 提交事务

				// 删除银行预指定信息表
				DBHandler.beginTransaction(context, tranConnection);// 开启事务
				bankSignData = new BankSignData();
				bankSignData.setAcctId(acctId);
				bankSignData.setBookNo(bookNo);
				bankSignData.setSecCompCode(secCompData.getSecCompCode());
				bankSignDataDao.delBankSignDataByBookNo(context,tranConnection, acctId, secCompCode, bookNo);
				DBHandler.commitTransaction(context, tranConnection);// 提交事务
			}
			SFLogger.info(context, "发券商结束");
			// 组返回券商报文
			SFUtil.setResDataValue( context, "ACCT_ID", acctId );
			SFUtil.setResDataValue( context, "SEC_COMP_CODE", secCompCode );
			SFUtil.setResDataValue( context, "BOOK_SERIAL_NO", bookNo );
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("交易失败", e.getMessage()));
		}

		SFLogger.info(context, "doSecu()结束");
	}

	@Override
	protected void chkStart(Context context) throws SFException {

	}

	@Override
	protected void chkEnd(Context context) throws SFException {

	}

	@Override
	protected void initialize(Context context) throws SFException {

	}

}
