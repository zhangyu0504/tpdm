package module.trans.cobank2sf;

import java.util.List;

import module.bean.AgtAgentBranch;
import module.dao.AgtAgentBranchDao;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;

import common.exception.SFException;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * （行E通管理台发起)查询下级机构信息
 * tran code :300311
 * @author ex_kjkfb_zhumingtao
 *
 */
public class T300311 extends TranBase {

	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{

	}	
	
	@Override
	public void doHandle(Context context) throws SFException {		
		SFLogger.info( context, String.format("qryAgtAgentBranch()开始") );
		qryAgtAgentBranch(context);	//查询下级机构信息
		SFLogger.info( context, String.format("qryAgtAgentBranch()结束") );
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
		String bankId = SFUtil.getReqDataValue(context, "BANK_ID");		//合作行号BankId
		String branchId = SFUtil.getReqDataValue(context, "BRANCH_ID");	//分行号BranchId
		try {
			if(SFUtil.isNotEmpty(bankId)){				
				AgtAgentBranchDao agtAgentBranchDao = new AgtAgentBranchDao();
				List<AgtAgentBranch> agtAgentBranchlist = agtAgentBranchDao.qryAgtAgentBranchByBranchId(context, tranConnection,bankId,branchId);
				SFUtil.chkCond(context, agtAgentBranchlist==null || agtAgentBranchlist.size()==0, "ST4895", "下属代理机构不存在");
			}
			SFLogger.info(context,String.format("chkStart()结束"));
		}catch (SFException e) {
			throw e;
		} catch (Exception e) {
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}	
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}
	private void qryAgtAgentBranch(Context context) throws SFException{
		String bankId = SFUtil.getReqDataValue(context, "BANK_ID");		//合作行号BankId
		String branchId = SFUtil.getReqDataValue(context, "BRANCH_ID");	//分行号BranchId		
		try {		
			AgtAgentBranchDao agtAgentBranchDao = new AgtAgentBranchDao();
			List<AgtAgentBranch> agtAgentBranchlist = agtAgentBranchDao.qryAgtAgentBranchByBranchId(context, tranConnection,bankId,branchId);
			if(agtAgentBranchlist==null || agtAgentBranchlist.size()==0){		
				return;
			}
			IndexedCollection indexColl = SFUtil.getDataElement(context, "300311_O_ICOLL");
			for(AgtAgentBranch agtAgentBranch : agtAgentBranchlist){
				//组装返回数据
				KeyedCollection keyColl = new KeyedCollection();
				SFUtil.addDataField(context,keyColl, "BANK_ID", agtAgentBranch.getBankId());
				SFUtil.addDataField(context,keyColl, "BRANCH_ID", agtAgentBranch.getBranchId());
				SFUtil.addDataField(context,keyColl, "BRANCH_NAME", agtAgentBranch.getBranchName());
				SFUtil.addDataField(context,keyColl, "FATHER_BRANCH", agtAgentBranch.getFatherBranch());
				SFUtil.addDataField(context,keyColl, "STATUS", agtAgentBranch.getStatus());
				SFUtil.addDataField(context,keyColl, "OPEN_DATE", agtAgentBranch.getOpenDate());		
				indexColl.add(keyColl);
			}
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}		
	}	
	
}
