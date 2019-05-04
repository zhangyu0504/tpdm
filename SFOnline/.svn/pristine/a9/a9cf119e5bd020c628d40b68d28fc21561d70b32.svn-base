package module.trans.bankchl2sf;

import java.util.List;


import module.bean.SecCompData;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 查询三方券商列表
 * 交易码 : 200303
 * @author ex_kjkfb_zhumingtao
 *
 */

public class T200303 extends TranBase {

	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{

	}
	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info( context, String.format("qrySecCompData()开始") );
		qrySecCompData(context);	//查询三方券商列表				
		SFLogger.info( context, String.format("qrySecCompData()结束") );
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}
	
	private void qrySecCompData(Context context) throws SFException{
		String secCompType = SFUtil.getReqDataValue(context, "SEC_COMP_TYPE");	
		if (!"0".equals(secCompType)&&!"1".equals(secCompType)&&!"2".equals(secCompType)&&!"3".equals(secCompType)&&!"4".equals(secCompType)){
			SFUtil.chkCond(context,"ST5707",String.format("交易要素[SecCompType]非法"));
		}
	    
		//String channel = SFUtil.getReqDataValue(context, "INIT_SIDE");					
		try {				
			List<SecCompData> list  = secCompDataDao.qrySecCompDataBySecCompType(context, tranConnection, secCompType);
			if(list==null||list.size()==0){			
				SFUtil.chkCond(context,"ST4069",String.format("查询无记录"));
			}
			IndexedCollection indexColl = SFUtil.getDataElement(context, "200303_O_ICOLL");
			for (SecCompData secCompData : list) {	
				//组装返回数据
				KeyedCollection keyColl = new KeyedCollection();
				SFUtil.addDataField(context,keyColl, "SEC_COMP_CODE", secCompData.getSecCompCode());
				SFUtil.addDataField(context,keyColl, "SEC_COMP_NAME", secCompData.getSecCompName());
				SFUtil.addDataField(context,keyColl, "PROTOCOL", secCompData.getProtocol());
				SFUtil.addDataField(context,keyColl, "SEC_BRCH_ID_MODE", secCompData.getSecBrchIdMode());
				indexColl.add(keyColl);
			}
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}			
	}	

}
