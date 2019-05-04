package module.trans.bankchl2sf;

import java.util.List;

import module.bean.BankSignData;
import module.bean.InvestData;
import module.bean.SignAccountData;
import module.dao.BankSignDataDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 一户通查询签约关系 交易码 : 200306
 * 
 * @author ex_kjkfb_zhumingtao
 * 
 */
public class T200306 extends TranBase {

	/**
	 * 初始化BIZ参数
	 * 
	 * @param context
	 * @throws SFException
	 */

	private String acctId = null; // 卡号CARD_NO
	private String idType = null; // 转换后的证件类型
	private String invIdCode = null; // 证件号码

	private String convInvIdCode1 = null; // 转换后证件号码，转换前15位，转换后为18位20实际证件号码；转换前18位，转换后为15位证件号码；
	private String convInvIdCode2 = null; // 转换后证件号码，转换前15位，转换后为18位20实际证件号码；转换前18位，无转换，为空值；

	public void initialize(Context context) throws SFException {

		acctId = SFUtil.getReqDataValue(context, "ACCT_ID");// 卡号CARD_NO
		idType = SFUtil.getDataValue(context, SFConst.PUBLIC_ID_TYPE);// 转换后的证件类型
		invIdCode = SFUtil.getReqDataValue(context, "INV_ID_CODE"); // 证件号码

		if (SFUtil.isNotEmpty(invIdCode)
				&& (SFConst.ID_TYPE_PERSON_SFZ.equals(idType) || SFConst.ID_TYPE_PERSON_LSSFZ
						.equals(idType))) {
			if (invIdCode.length() == 15) {
				convInvIdCode1 = BizUtil.converTo18Card19(invIdCode); // 15位身份证号码转换为18位
				convInvIdCode2 = BizUtil.converTo18Card20(invIdCode); // 15位身份证号码转换为18位
			}
			if (invIdCode.length() == 18) {
				convInvIdCode1 = BizUtil.converTo15(invIdCode); // 18位身份证号码转换为15位
				convInvIdCode2 = "";
			}
		}
	}

	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info(context, String.format("qrySignAccountData()开始"));
		qrySignAccountData(context); // 查询一户通签约关系
		SFLogger.info(context, String.format("qrySignAccountData()结束"));
	}

	@Override
	public void doHost(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart(Context context) throws SFException {
		SFLogger.info(context, String.format("chkStart()开始"));

		SignAccountData signAccountData = new SignAccountData();
		InvestData InvestData = new InvestData();

		try {

			if (SFUtil.isNotEmpty(acctId)) {
				signAccountData = signAccountDataDao
						.qrySignAccountDataByAcctId(context, tranConnection,
								acctId);
				SFUtil.chkCond(context, signAccountData == null, "ST5720",
						"该客户未签约第三方存管业务");
			} else {
				SFUtil.chkCond(context, InvestData == null, "ST5720",
						"该客户未签约第三方存管业务");
			}
			SFLogger.info(context, String.format("chkStart()结束"));
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",
					String.format("交易失败", e.getMessage()));
		}
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	private void qrySignAccountData(Context context) throws SFException {

		BankSignData bankSignData = new BankSignData();
		try {

			List<SignAccountData> signList = signAccountDataDao
					.qrySignAccountDataAndSecInv(context, tranConnection,
							acctId, idType, invIdCode, convInvIdCode1,
							convInvIdCode2);
			if (signList == null) {
				SFUtil.chkCond(context, "ST4069", String.format("查询无记录"));
			}
			IndexedCollection indexColl = SFUtil.getDataElement(context,
					"200306_O_ICOLL");
			for (SignAccountData signAccountData : signList) {
				// 组装返回数据
				KeyedCollection keyColl = new KeyedCollection();

				if (SFConst.SIGN_FLAG_BANK_PRE.equals(signAccountData
						.getSignMode())) {
					// 模式为2-银行预约、券商激活模式，则需要查询银行预约关系表TRDBankSignData的银行预制定日期(TxDate)+激活日期(opendate)
					bankSignData.setAcctId(acctId);
					bankSignData.setIdType(idType);
					bankSignData.setInvIdCode(invIdCode);
					BankSignDataDao bankSignDataDao = new BankSignDataDao();
					bankSignData = bankSignDataDao.qryBankSignDataByAcctId(
							context, tranConnection, acctId, idType, invIdCode);
					SFUtil.addDataField(context, keyColl, "TX_DATE",
							bankSignData.getTxDate());
					SFUtil.addDataField(context, keyColl, "OPEN_DATE",
							bankSignData.getOpenDate());
				}
				SFUtil.addDataField(context, keyColl, "ACCT_ID",
						signAccountData.getAcctId());
				SFUtil.addDataField(context, keyColl, "INV_NAME",
						signAccountData.getInvName());
				SFUtil.addDataField(context, keyColl, "CUR_CODE",
						signAccountData.getCurCode());
				SFUtil.addDataField(context, keyColl, "ID_TYPE", BizUtil
						.convSF2Host4IdType(context, signAccountData
								.getInvestData().getIdType()));
				SFUtil.addDataField(context, keyColl, "INV_ID_CODE",
						signAccountData.getInvestData().getInvIdCode());
				SFUtil.addDataField(context, keyColl, "CAP_ACCT",
						signAccountData.getCapAcct());
				SFUtil.addDataField(context, keyColl, "INV_TYPE",
						signAccountData.getInvType());
				SFUtil.addDataField(context, keyColl, "SEC_COMP_CODE",
						signAccountData.getSecCompData().getSecCompCode());
				SFUtil.addDataField(context, keyColl, "SEC_COMP_NAME",
						signAccountData.getSecCompData().getSecCompName());
				SFUtil.addDataField(context, keyColl, "SIGN_FLAG",
						signAccountData.getSignFlag());
				SFUtil.addDataField(context, keyColl, "CHANNEL",
						signAccountData.getChannel());
				SFUtil.addDataField(context, keyColl, "CUS_MAG_NO",
						signAccountData.getCusMagno());
				SFUtil.addDataField(context, keyColl, "DEP_ID",
						signAccountData.getDepId());
				SFUtil.addDataField(context, keyColl, "OPEN_DATE",
						signAccountData.getOpenDate());
				SFUtil.addDataField(context, keyColl, "SIGN_DATE",
						signAccountData.getSignDate());
				SFUtil.addDataField(context, keyColl, "BEGIN_BAL",
						signAccountData.getBeginBal());
				SFUtil.addDataField(context, keyColl, "ACCT_BAL",
						signAccountData.getAcctBal());
				indexColl.add(keyColl);
			}
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",
					String.format("交易失败", e.getMessage()));
		}
	}
}
