package module.trans.cobank2sf;

import module.bean.AgtTranList;
import module.dao.AgtTranListDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * （行E通管理台发起) 查询流水处理情况交易
 * 交易码 : 300312
 * @author ex_kjkfb_zhumingtao
 *
 */
public class T300312 extends TranBase {
	
	@Override
	protected void initialize(Context context) throws SFException {

	}
	
	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info( context, String.format("qryAgtTranList()开始") );
		qryAgtTranList(context);	//查询流水处理情况
		SFLogger.info( context, String.format("qryAgtTranList()结束") );
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
		SFLogger.info(context,String.format("chkStart()开始"));
		String queryLogNo = SFUtil.getReqDataValue(context, "PICS_LOG_NO");	//流水号	
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		try{
			AgtTranList agtTranList = agtTranListDao.qryAgtTranListBylogNo(context, tranConnection, queryLogNo);
			SFUtil.chkCond(context, agtTranList==null, "ST4069", "查询该流水号对应记录不存在");	
			SFLogger.info(context,String.format("chkStart()结束"));		
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}
		
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}

	private void qryAgtTranList(Context context) throws SFException{
		String queryLogNo = SFUtil.getReqDataValue(context, "PICS_LOG_NO");	//流水号	
		AgtTranListDao agtTranListDao = new AgtTranListDao();
		try {
			AgtTranList agtTranList = agtTranListDao.qryAgtTranListBylogNo(context, tranConnection, queryLogNo);
			if(agtTranList==null){	
				return;
			}	
			//组装返回数据
			SFUtil.setResDataValue(context, "BANK_ID",  agtTranList.getBankId());
			SFUtil.setResDataValue(context, "OPEN_BRANCH", agtTranList.getOpenBranch());
			SFUtil.setResDataValue(context, "TRAN_TYPE", agtTranList.getTranType());
			SFUtil.setResDataValue(context, "PICS_LOG_NO", agtTranList.getPicsLogNo());
			SFUtil.setResDataValue(context, "FRONT_LOG_NO", agtTranList.getFrontLogNo());
			SFUtil.setResDataValue(context, "AGENT_LOG_NO", agtTranList.getAgentLogNo());
			SFUtil.setResDataValue(context, "ACCT_ID", agtTranList.getAcctNo());
			SFUtil.setResDataValue(context, "SEC_COMP_CODE", agtTranList.getStkCode());
			SFUtil.setResDataValue(context, "CAP_ACCT", agtTranList.getStkAcct());
			SFUtil.setResDataValue(context, "BANK_ACCT", agtTranList.getBankAcct());
			SFUtil.setResDataValue(context, "COMP_ACCT", agtTranList.getCompAcct());
			SFUtil.setResDataValue(context, "CUR_CODE", agtTranList.getCcyCode());
			SFUtil.setResDataValue(context, "TX_AMOUNT", agtTranList.getTranAmount());
			SFUtil.setResDataValue(context, "THIRD_TIME", agtTranList.getThirdTime());
			SFUtil.setResDataValue(context, "VOID_FLAG", agtTranList.getVoidFlag());
			SFUtil.setResDataValue(context, "TRAN_RESULT", agtTranList.getTranResult());
			SFUtil.setResDataValue(context, "HOST_CHECK", agtTranList.getHostCheck());
			SFUtil.setResDataValue(context, "HOST_CHECK",  agtTranList.getHostLogNo());
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}			
	}	
}
