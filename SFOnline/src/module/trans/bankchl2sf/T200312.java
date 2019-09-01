package module.trans.bankchl2sf;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.util.SFConst;
import common.util.SFUtil;
import module.bean.AcctDetail;
import module.bean.AcctJour;
import module.bean.LocalInfo;
import module.dao.AcctDetailDao;
import module.dao.AcctJourDao;
import module.trans.TranBase;

 
public class T200312 extends TranBase {
	protected void initialize(Context context) throws SFException {
	}

	public void doHandle(Context context) throws SFException {
		
		String txDate = null;
		String bankDate = null;
		String workDate = null;
		String tranSeqNo = null;
		String subTxSeqId = null;
		String status = null;
		String busiType = null;
		String jourFlag = null;
		String detailFlag = null;

		bankDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getBankDate();
		workDate = ( ( LocalInfo )SFUtil.getDataValue( context, SFConst.PUBLIC_LOCAL_INFO ) ).getWorkdate();
		tranSeqNo = (String) SFUtil.getReqDataValue(context, "BUSI_SEQ_NO");

		AcctJourDao acctJourDao = new AcctJourDao();
		AcctJour acctJour = new AcctJour();
		acctJour = acctJourDao.qryAcctJourByChlTranSeqId(context, tranConnection, bankDate, workDate, tranSeqNo);

		SFUtil.chkCond(context, acctJour == null, "ST5800", String.format("²éÑ¯ÎÞ¼ÇÂ¼"));
		
		txDate = acctJour.getTxDate();
		subTxSeqId = acctJour.getSubTxSeqId();
		busiType = acctJour.getBusiType();
		jourFlag = acctJour.getJourFlag();

		AcctDetailDao acctdetailDao = new AcctDetailDao();
		AcctDetail acctDetail = new AcctDetail();
		acctDetail = acctdetailDao.qryAcctDetailByFlag(context, tranConnection, txDate, subTxSeqId, acctJour.getYbStatus());

		detailFlag = acctDetail != null ? acctDetail.getJourFlag() : "  ";

		if ("02".equals(busiType) && "00".equals(detailFlag) && "30".equals(jourFlag)){
			status = "X";
		}
		else if ("0".equals(String.valueOf(detailFlag.charAt(0)))){
			status = "S";
		}
		else{
			status = "F";
		}
		
		SFUtil.setResDataValue(context, "TRAN_DATE", acctJour.getTxDate());
		SFUtil.setResDataValue(context, "BUSI_SEQ_NO", acctJour.getChlSeqId());
		SFUtil.setResDataValue(context, "TRAN_SEQ_NO", acctJour.getChlTranSeqId());
		SFUtil.setResDataValue(context, "STATUS", status);
		SFUtil.setResDataValue(context, "ERR_CODE", acctJour.getRespCode());
		SFUtil.setResDataValue(context, "ERR_MSG", acctJour.getRespMsg());
	}

	public void doHost(Context context) throws SFException {
	}

	public void doSecu(Context context) throws SFException {
	}

	protected void chkStart(Context context) throws SFException {
	}

	protected void chkEnd(Context context) throws SFException {
	}
}