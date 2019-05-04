package module.trans.bankchl2sf;

import module.bean.BankSignData;
import module.dao.BankSignDataDao;
import module.trans.Page;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.DateUtil;
import common.util.SFConst;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * (银行渠道发起) 预约查询
 * 交易码 : 200304
 * @author ex_kjkfb_zhumingtao
 *
 */
public class T200304 extends TranBase {	
	
	/**
	 * 初始化BIZ参数
	 * @param context
	 * @throws SFException
	 */
	public void initialize(Context context) throws SFException{

	}
	
	@Override
	public void doHandle(Context context) throws SFException {
		SFLogger.info( context, String.format("qryBankSignData()开始") );
		qryBankSignData(context);
		SFLogger.info( context, String.format("qryBankSignData()结束") );
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
		String startDate = SFUtil.getReqDataValue(context, "START_DATE");	//开始日期
		String endDate = SFUtil.getReqDataValue(context, "END_DATE");		//结束日期
		if(SFUtil.isEmpty(startDate)){
			SFUtil.chkCond(context, "ST4012", "查询日期不符");
		}
		if(SFUtil.isEmpty(endDate)){
			endDate = DateUtil.getMacDate();
		}
		int days = DateUtil.getBetweenDays(DateUtil.formatToHyphenDate(startDate), DateUtil.formatToHyphenDate(endDate));			
		if (startDate.length()!=8 || endDate.length()!=8) {
			SFUtil.chkCond(context, "ST4369", "查询日期长度只能为8位");
		}
		if(days>90){
			SFUtil.chkCond(context, "ST4012", "查询日期跨度不能超过90天");			
		}	
	    SFLogger.info(context,String.format("chkStart()结束"));
	}

	@Override
	protected void chkEnd(Context context) throws SFException {
		// TODO Auto-generated method stub

	}
	
	private void qryBankSignData(Context context) throws SFException{
		String idType = SFUtil.getReqDataValue(context, "ID_TYPE");			//原证件类型
		String sfIdType = SFUtil.getDataValue(context, SFConst.PUBLIC_ID_TYPE);//三方证件类型
		String invIdCode = SFUtil.getReqDataValue(context, "INV_ID_CODE");	//证件号码	
		//String initSide = SFUtil.getReqDataValue(context, "INIT_SIDE");	//渠道
		String startDate = SFUtil.getReqDataValue(context, "START_DATE");	//开始日期
		String endDate = SFUtil.getReqDataValue(context, "END_DATE");		//结束日期
		String strPageNum = SFUtil.getReqDataValue(context, "PAGE_NUM");		//查询页数	
		String pageSize = SFUtil.getDataValue( context, "APP_HEAD.PER_PAGE_NUM" );//每页显示条数
		String invIdCode18Card19 = null;	//15位转18位证件号码
		String invIdCode18Card20 = null;	//15位转18位证件号码
		if((idType.equals(SFConst.ID_TYPE_PERSON_SFZ)||idType.equals(SFConst.ID_TYPE_PERSON_LSSFZ))&&
				SFUtil.isNotEmpty(invIdCode)&&invIdCode.length()==15){
			invIdCode18Card19 = BizUtil.converTo18Card19(invIdCode);	//15位身份证号码转换为18位
			invIdCode18Card20 = BizUtil.converTo18Card20(invIdCode);	//15位身份证号码转换为18位	
		}
		if(SFUtil.isEmpty(startDate)&&SFUtil.isEmpty(endDate)&&(startDate).equals(endDate)){
			SFUtil.chkCond(context, "ST4012", "查询日期不符");
		}

		int pageNum=0;
		try {
			//默认页号从1开始
			if(SFUtil.isNotEmpty(strPageNum)){
				pageNum=Integer.parseInt(strPageNum);
			}else{
				pageNum = 1;
			}
			Page<BankSignData> page=new Page<BankSignData>(SFConst.SF_PERPAGE_NUM_BANK,pageNum);
			if(SFUtil.isNotEmpty(pageSize)){
				page.setPageSize( Integer.valueOf( pageSize ) );
			}
			BankSignDataDao bankSignDataDao = new BankSignDataDao();
			page = bankSignDataDao.qryBankSignDataBespeak(context, tranConnection, startDate, endDate, sfIdType, invIdCode,invIdCode18Card19,invIdCode18Card20, page);
			if(page.getPageData()==null||page.getPageData().size()==0){
				SFUtil.chkCond(context,"ST4069",String.format("查询无记录"));
			}
			IndexedCollection indexColl = SFUtil.getDataElement(context, "200304_O_ICOLL");
			for (BankSignData bankSignData : page.getPageData()) {	
				//组装返回数据
				KeyedCollection keyColl = new KeyedCollection();
				SFUtil.addDataField(context,keyColl, "ACCT_ID", bankSignData.getAcctId());
				SFUtil.addDataField(context,keyColl, "BOOK_SERIAL_NO", bankSignData.getBookNo());
				SFUtil.addDataField(context,keyColl, "SEC_COMP_CODE", bankSignData.getSecCompCode());
				SFUtil.addDataField(context,keyColl, "SEC_COMP_NAME", bankSignData.getSecCompName());
				SFUtil.addDataField(context,keyColl, "TX_DATE", bankSignData.getTxDate());
				SFUtil.addDataField(context,keyColl, "OPEN_DATE", bankSignData.getOpenDate());
				SFUtil.addDataField(context,keyColl, "DEL_DATE", bankSignData.getDelDate());
				SFUtil.addDataField(context,keyColl, "SIGN_FLAG", bankSignData.getSignFlag());
				indexColl.add(keyColl);
			}
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context,"ST4895",String.format("交易失败",e.getMessage()));
		}				
	}		

}
