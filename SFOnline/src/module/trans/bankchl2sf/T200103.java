package module.trans.bankchl2sf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.bean.SignAccountData;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DBHandler;
import common.util.BizUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 建立/取消卡状态字
 *  交易码：200103
 * @author 张钰
 * 
 */
public class T200103 extends TranBase {

	private String acctId = null;//卡号
	private String functionCode = null;//功能码
	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException {

	}

	@Override
	public void doHandle(Context context) throws SFException {
		//开始上主机
		SFLogger.info(context, String.format("doHost()开始"));
		doHost(context);
		SFLogger.info(context, String.format("doHost()结束"));
	}

	@Override
	public void doHost(Context context) throws SFException {
		try {
			String branchId = null;//开户网点号
			DBHandler.beginTransaction(context, tranConnection);// 开启事务
			if( SFConst.INV_TYPE_CORP.equals( SFUtil.getReqDataValue( context, "INV_TYPE" ) ) ) {//对公客户通过C3011接口查询客户信息
				/**************************************************************************
				 *                    上主机查询卡状态开始
				 ***************************************************************************/
				Map<String,Object> msg=new HashMap<String,Object>();
				msg.put("ACCT_ID",acctId);//账号ACCT_NO
				Context msgContext = BizUtil.qryCardAttrClient(context, msg);
				branchId = SFUtil.getDataValue(msgContext,"MSG_O.OPEN_DEP_ID");//开户网点号
				/**********************上主机查询卡状态字结束********************************/
			}
			// 上主机设置状态字
			BizUtil.setCardStatusWord(context,acctId,"O".equals(functionCode) ? "A" : "D",branchId);//功能码 A-建立
			//主机返回成功
			if ("O".equals(functionCode)){//建立卡状态字
				signAccountDataDao.updSignAccountDataByAcctId(context,tranConnection, acctId, "1");
			} else {//取消卡状态字
				signAccountDataDao.updSignAccountDataByAcctId(context,tranConnection, acctId, "0");
			}
			DBHandler.commitTransaction(context, tranConnection);
			
			// 交易成功，组返回报文
			//if(SFConst.INV_TYPE_RETAIL.equals(SFUtil.getReqDataValue( context, "INV_TYPE" ))){
				SFUtil.setResDataValue(context, "ACCT_ID", acctId);//对公零售都需要返回
			//}
			SFUtil.setResDataValue(context, "FUNCTION_CODE", "O".equals(functionCode)?"建立":"取消");
		} catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context, "ST4895",String.format("交易失败%s", e.getMessage()));
		} 

	}

	@Override
	public void doSecu(Context context) throws SFException {

	}

	@Override
	protected void chkStart(Context context) throws SFException {
		functionCode = SFUtil.getReqDataValue(context, "FUNCTION_CODE");// 功能码
		// 功能码合法性检查
		SFUtil.chkCond(context,!"O".equals(functionCode) && !"F".equals(functionCode),"ST5702", String.format("[功能码]非法"));
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		acctId = SFUtil.getReqDataValue(context, "ACCT_ID");// 卡号
		//设置卡状态字检查
		List<SignAccountData> signList = signAccountDataDao.qrySignAccountDataListByAcctId(context, tranConnection,acctId, "");
		if ("O".equals(functionCode)) {
			//未建立签约关系, 不能建立卡状态字
			SFUtil.chkCond(context,signList.size() == 0, "ST5611",String.format("上主机建立卡状态字失败[卡/账户尚未建立签约关系]"));
		} else {
			//已经建立签约关系, 不能取消卡状态字
			SFUtil.chkCond(context,signList.size() > 0, "ST5611",String.format("上主机取消卡状态字失败[卡/账户已经建立签约关系]"));
		}
	}

}
