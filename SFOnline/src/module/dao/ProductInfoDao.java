package module.dao;

import java.sql.Connection;

import module.bean.ProductInfo;

import com.ecc.emp.core.Context;
import common.exception.SFException;
import common.sql.dao.DaoBase;
import common.util.SFUtil;

/**
 * 产品信息Dao
 * @author 吕超鸿
 *
 */
public class ProductInfoDao extends DaoBase {
	/**
	 * 查询产品信息
	 * @param context
	 * @param connection
	 * @param bean
	 * @return
	 * @throws SFException
	 */
	public ProductInfo qryProductInfoByDepId(Context context,Connection connection,String secCompCode) throws SFException{
		ProductInfo productInfo = null;
		try{
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("SELECT O.SECCOMPCODE,O.USERID,O.SECCOMPNAME,O.PRODUCTTYPE,O.PRODUCTNAME,O.CURCODE,O.CURNAME,O.PERMITFLAG,O.FIRMCODE,O.TRUACCTID,");
			buffer.append(" O.TRUOPNDEPID,O.SECSELFACCTID,O.SELFOPNDEPID,O.SECCORACCTID,O.COROPNDEPID,O.TPDMFLAG,T.BranchId  AS openBranchId");
			buffer.append(" FROM TRDPRODUCTINFO O,TRDBANKUNIT T");
			buffer.append(" WHERE O.TRUOPNDEPID = T.DEPID AND O.SECCOMPCODE=? AND PRODUCTTYPE='03' AND CURCODE='RMB'");
			
			productInfo = super.qry(context, connection, buffer.toString(), ProductInfo.class, secCompCode);
		} catch (SFException e){
			throw e;
		} catch (Exception e ){
			SFUtil.chkCond(context, "ST4895", e.getMessage());
		}finally{
			if(productInfo!=null){
				productInfo.resetChangedFlag();
			}
		}
		 return productInfo;
	}
}
