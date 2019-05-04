package module.trans.cobank2sf;

import java.util.List;

import module.bean.InvestData;
import module.bean.SecCompData;
import module.bean.SignAccountData;
import module.cache.ParamCache;
import module.trans.TranBase;

import com.ecc.emp.core.Context;
import com.ecc.emp.data.IndexedCollection;
import com.ecc.emp.data.KeyedCollection;
import common.exception.SFException;
import common.util.BizUtil;
import common.util.SFUtil;

import core.log.SFLogger;

/**
 * 此类为合作行发起查询签约关系
 * @author ex_kjkfb_songshimin
 * 交易码:300300
 */
public class T300300 extends TranBase {

	@Override
	protected void initialize( Context context ) throws SFException {}

	@Override
	public void doHandle( Context context ) throws SFException {
		// 查询签约关系
		SFLogger.info( context, String.format( "qrySignAccountData()开始" ) );
		qrySignAccountData( context );
		SFLogger.info( context, String.format( "qrySignAccountData()结束" ) );
	}

	/**
	 * 查询签约关系
	 * @param context
	 * @throws SFException
	 */
	private void qrySignAccountData( Context context ) throws SFException {
		String searchFlag = SFUtil.getReqDataValue( context, "SEARCH_FLAG" );
		String searchNum = SFUtil.getReqDataValue( context, "SEARCH_NUM" );
		String idType = "1";
		List<SignAccountData> signAccountList = null;
		try {
			if( "1".equals( searchFlag ) ) {// 身份证号索引查询
				InvestData investData = investDataDao.qryInvestData( context, tranConnection, idType, searchNum );
				SFUtil.chkCond( context, investData == null, "ST4049", String.format( "客户[%s]身份信息不存在", searchNum ) );
				String secAcct = investData.getSecAcct();
				signAccountList = signAccountDataDao.qrySignAcctountDataListBySecAcct( context, tranConnection, secAcct );
				SFUtil.chkCond( context, signAccountList.size() == 0, "ST4785", String.format( "签约关系不存在" ) );
				buildData( context, signAccountList, searchFlag, investData );
			} else if( "2".equals( searchFlag ) || "3".equals( searchFlag ) || "4".equals( searchFlag ) ) {// 2-证券资金帐号索引查询; 3-签约银行帐号索引查询 4-其他证件号码索引查询
				signAccountList = signAccountDataDao.qrySignAccountDataListBySearchFlag( context, tranConnection, searchNum, searchFlag );
				SFUtil.chkCond( context, signAccountList.size() == 0, "ST4785", String.format( "签约关系不存在" ) );
				buildData( context, signAccountList, searchFlag, null );
			}
		} catch( SFException e ) {
			throw e;
		} catch( Exception e ) {
			SFUtil.chkCond( context, "ST4895", e.getMessage() );
		}
	}

	private void buildData( Context context, List<SignAccountData> signAccountDataList, String searchFlag, InvestData investData ) throws SFException {
		IndexedCollection indexColl = SFUtil.getDataElement( context, "300300_O_ICOLL" );
		indexColl.clear();
		for( SignAccountData sign : signAccountDataList ) {// 循环遍历集合
			KeyedCollection keyColl = new KeyedCollection();
			if( "1".equals( searchFlag ) ) {// 身份证号索引查询
				SecCompData secCompData = secCompDataDao.qrySecCompData( context, tranConnection, sign.getSecCompCode() );// 根据券商码查询券商名称
				SFUtil.addDataField( context, keyColl, "INV_NAME", investData.getInvName() );
				SFUtil.addDataField( context, keyColl, "INV_TYPE", investData.getInvType() );
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", investData.getInvIdCode() );
				SFUtil.addDataField( context, keyColl, "PHONE", investData.getPhone() );
				SFUtil.addDataField( context, keyColl, "MOBILE", investData.getMobile() );
				SFUtil.addDataField( context, keyColl, "ADDR", investData.getAddr() );
				SFUtil.addDataField( context, keyColl, "ZIP", investData.getZip() );
				SFUtil.addDataField( context, keyColl, "EMAIL_ADDR", investData.getEmailAddr() );
				SFUtil.addDataField( context, keyColl, "ID_TYPE", BizUtil.convSF2CoBank4IdType( context, "1" ) );
				SFUtil.addDataField( context, keyColl, "SEC_COMP_NAME", secCompData != null ? secCompData.getSecCompName() : "" );
			} else if( "2".equals( searchFlag ) || "3".equals( searchFlag ) || "4".equals( searchFlag ) ) {// 2-证券资金帐号索引查询; 3-签约银行帐号索引查询 4-其他证件号码索引查询
				SFUtil.addDataField( context, keyColl, "SEC_COMP_NAME", sign.getSecCompData().getSecCompName() );
				SFUtil.addDataField( context, keyColl, "INV_NAME", sign.getInvestData().getInvName() );
				if( !context.containsKey( "INV_ID_CODE" ) ) {
					SFUtil.addDataField( context, "INV_ID_CODE", sign.getInvestData().getInvIdCode() );
				} else {
					SFUtil.setDataValue( context, "INV_ID_CODE", sign.getInvestData().getInvIdCode() );
				}
				SFUtil.addDataField( context, keyColl, "ID_TYPE", BizUtil.convSF2CoBank4IdType( context, SFUtil.objectToString( sign.getInvestData().getIdType() ) ) );
				SFUtil.addDataField( context, keyColl, "INV_TYPE", sign.getInvestData().getInvType() );
				SFUtil.addDataField( context, keyColl, "INV_ID_CODE", sign.getInvestData().getInvIdCode() );
				SFUtil.addDataField( context, keyColl, "PHONE", sign.getInvestData().getPhone() );
				SFUtil.addDataField( context, keyColl, "MOBILE", sign.getInvestData().getMobile() );
				SFUtil.addDataField( context, keyColl, "ADDR", sign.getInvestData().getAddr() );
				SFUtil.addDataField( context, keyColl, "ZIP", sign.getInvestData().getZip() );
				SFUtil.addDataField( context, keyColl, "EMAIL_ADDR", sign.getInvestData().getEmailAddr() );
			}
			/**
			 * 组返回节点参数
			 */
			SFUtil.addDataField( context, keyColl, "ACCT_ID", sign.getAcctId() );
			SFUtil.addDataField( context, keyColl, "CUR_CODE", sign.getCurCode() );
			SFUtil.addDataField( context, keyColl, "CAP_ACCT", sign.getCapAcct() );
			SFUtil.addDataField( context, keyColl, "SEC_COMP_CODE", sign.getSecCompCode() );
			SFUtil.addDataField( context, keyColl, "SIGN_FLAG", ParamCache.getValue2( "SF_SIGN_FLAG_DESC", sign.getSignFlag() ) );// 根据签约标识返回签约标识中文意义
			indexColl.add( keyColl );
		}
		// SFUtil.setResDataValue( context, "MAC", "520D3647" );
	}

	@Override
	public void doHost( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSecu( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkStart( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void chkEnd( Context context ) throws SFException {
		// TODO Auto-generated method stub

	}

}
