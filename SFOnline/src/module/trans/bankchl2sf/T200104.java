package module.trans.bankchl2sf;

import java.util.List;

import module.bean.InvestData;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 账户信息修改
 * 交易码：200104
 * @author 张钰
 *
 */
public class T200104 extends TranBase {

	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{
		
	}
	@Override
	public void doHandle(Context context) throws SFException {
		//修改客户信息
		SFLogger.info(context, String.format("updInvestData()开始"));
		updInvestData(context);
		SFLogger.info(context, String.format("updInvestData()结束"));
	}

	private void updInvestData(Context context) throws SFException {
		//String initSide = SFUtil.getReqDataValue(context, "INIT_SIDE");//渠道
		String invType = SFUtil.getReqDataValue(context, "INV_TYPE");//客户类型
		String acctId = SFUtil.getReqDataValue(context, "ACCT_ID");//卡号
		String capAcct = SFUtil.getReqDataValue(context, "CAP_ACCT");//资金帐号
		String emailAddr = SFUtil.getReqDataValue(context, "EMAIL_ADDR");//Email
		String mobile = SFUtil.getReqDataValue(context, "MOBILE");//手机号码
		String fax = SFUtil.getReqDataValue(context, "FAX");//传真
		String tel = SFUtil.getReqDataValue(context, "PHONE");//联系电话
		String zip = SFUtil.getReqDataValue(context, "ZIP");//邮编
		String legalName = SFUtil.getReqDataValue(context, "LEGAL_NAME");//法人代表姓名
		String remark = SFUtil.getReqDataValue(context, "REMARK");//备注
		String addRess = SFUtil.getReqDataValue(context, "ADDR");//地址
		try {
			DBHandler.beginTransaction(context, tranConnection);//开启事务
			/*String address = null;
		    String trnName = null;
		    String trnIdType = null;
		    String trnIdCode = null;
		    String trnPhone = null;
		    String trnMobile = null;*/
			/*if (SFConst.CTX_PUBLIC_INIT_SIDE_ABBANK.equals(initSide)) {
				address = SFUtil.getReqDataValue(context, "Address1");
				address+=SFUtil.getReqDataValue(context, "Address2");
				address+=SFUtil.getReqDataValue(context, "Address3");
				address+=SFUtil.getReqDataValue(context, "Address4");
				if (SFConst.CTX_PUBLIC_INV_TYPE_CORP.equals(invType)){
					//经办人信息
					trnName = SFUtil.getReqDataValue(context, "TrnName1");
					trnName += SFUtil.getReqDataValue(context, "TrnName2");
					trnIdType =  SFUtil.getReqDataValue(context, "TRN_ID_TYPE");//授权代理人证件类型
					trnIdCode =  SFUtil.getReqDataValue(context, "TRN_ID_CODE");//授权代理人证件号码
					trnPhone =  SFUtil.getReqDataValue(context, "TrnPhone");
					trnMobile =  SFUtil.getReqDataValue(context, "TrnMobile");
				}
			} else {
				address = SFUtil.getReqDataValue(context, "Address1");
				if (SFConst.CTX_PUBLIC_INV_TYPE_CORP.equals(invType)){
					trnName = SFUtil.getReqDataValue(context, "TRN_NAME");//授权代理人姓名
				}
			}*/

	   	   InvestData investData = new InvestData();
	   	   /*investData.setTrnName(trnName);
	   	   investData.setTrnIdType(trnIdType);
	   	   investData.setTrnIdCode(trnIdCode);
	   	   investData.setTrnPhone(trnPhone);
	   	   investData.setTrnMobile(trnMobile);
	   	   investData.setAddr(address);*/
	   	   investData.setAddr(addRess);
	   	   investData.setInvType(invType);
	   	   investData.setZip(zip);
	   	   investData.setPhone(tel);
	   	   investData.setFax(fax);
	   	   investData.setMobile(mobile);
	   	   investData.setEmailAddr(emailAddr);
	   	   investDataDao.updInvestDataByAcctId(context, tranConnection, investData, acctId);
	   	   DBHandler.commitTransaction(context, tranConnection);
	   	  
	   	   // 交易成功，组返回报文
	   	   SFUtil.setResDataValue(context, "ACCT_ID", acctId);
	   	   SFUtil.setResDataValue(context, "CAP_ACCT", capAcct);
	   	   SFUtil.setResDataValue(context, "PHONE", tel);
	   	   SFUtil.setResDataValue(context, "MOBILE", mobile);
	   	   SFUtil.setResDataValue(context, "FAX", fax);
	   	   SFUtil.setResDataValue(context, "ZIP", zip);
	   	   SFUtil.setResDataValue(context, "EMAIL_ADDR", emailAddr);
	   	   SFUtil.setResDataValue(context, "ADDR", "");
	   	   SFUtil.setResDataValue(context, "TRN_NAME", "");
	   	   SFUtil.setResDataValue(context, "LEGAL_NAME", legalName);
	   	   SFUtil.setResDataValue(context, "REMARK", remark);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败%s",e.getMessage()));
		}
	
	}
	@Override
	public void doHost(Context context) throws SFException {

	}


	@Override
	public void doSecu(Context context) throws SFException {

	}


	@Override
	protected void chkStart(Context context) throws SFException {

	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		List<SignAccountData> signAccountDataList = signAccountDataDao.qrySignAccountDataListBySignFlag(context, tranConnection,(String)SFUtil.getReqDataValue(context, "ACCT_ID"));
   	   	SFUtil.chkCond(context, signAccountDataList == null||signAccountDataList.size() == 0, "ST5720",String.format("客户签约信息不存在"));
   	   	String secCompCode = signAccountDataList.get(0).getSecCompCode();
   	   	SecCompData secCompData = secCompDataDao.qrySecCompData(context,tranConnection, secCompCode);
   	   	SFUtil.setDataValue(context, SFConst.PUBLIC_SECU,secCompData); // 在上下文中存入券商对象
   	   //检查券商在该时间段不允许交易
   	//   SFUtil.chkCond(context,!SecuService.chkSecu724(context, tranConnection),"ST4014", String.format("券商在该时间段不允许交易"));
	}

}
