package common.action.dataoper;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.sql.SQLParameterCollection;
import com.ecc.emp.log.EMPLog;
import common.action.db.SqlExecAction;
import common.services.QueryCfgDefine;
import common.services.SQLDefine;
import common.util.SFUtil;

import core.jdbc.sql.SQLParameter;

/**
 * EMP 交易步骤扩展<br>
 * Extends class EMPAction<br>
 * 查询分页模板
 * @date on 2009年11月24日18时00分13秒<br>
 * @autor hq <br>
 * @emp:name FBSPageQueryTemplateAction
 * @emp:catalog extendedElements
 * @emp:states 0=成功;-1=异常;
 * @emp:document
 */
public class PageQueryTemplateAction extends EMPAction {

	/* 业务逻辑操作单元的执行入口 */

	private String pageQueryDefine;
	
	private String orderSql;

	public String getOrderSql() {
		return orderSql;
	}

	public void setOrderSql(String orderSql) {
		this.orderSql = orderSql;
	}

	public void setPageQueryDefine(String pageQueryDefine) {
		this.pageQueryDefine = pageQueryDefine;
	}

	/* 业务逻辑操作单元的执行入口 */
	public String execute(Context context) throws EMPException {

		String recordSize = null;
		SQLDefine define = (SQLDefine) context
				.getService(SFUtil.getContextValueInAction(context,pageQueryDefine));

		// 查询总记录数
		recordSize = this.querySqlCount(context, define);
		if (recordSize == null || recordSize == "0") {
//			throw new FBSException("P0000S100", "记录不存在！");
			return "2";
		}
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0, "数据结果的大小:" + recordSize);
		//执行分页
		this.querySql(context, define, recordSize);
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0, "分页语句执行成功");
		return "0";
	}

	/**
	 * 执行分页sql到context中
	 * 
	 * @param context
	 *            emp上下文
	 * @param sqlWhere
	 *            sql条件
	 * @param recordSize
	 *            记录的总条数
	 * @param execService
	 *            执行sql的服务
	 * @param connection
	 *            数据连接对象
	 * */

	public void querySql(Context context, SQLDefine define,
			String recordSize) throws EMPException {
		QueryCfgDefine aQueryCfgDefine = null;
		String orderBySql = SFUtil.getContextValueInAction(context,orderSql);
		if (orderBySql == null || orderBySql.equals("")) {
			if (define instanceof QueryCfgDefine) {
				aQueryCfgDefine = (QueryCfgDefine)define;
				if(aQueryCfgDefine.getOrderBySql() == null || aQueryCfgDefine.getOrderBySql().equals("")){
					throw new EMPException("order by undefined!");
				}
				orderBySql = aQueryCfgDefine.getOrderBySql();
			}
		} 
		//if(define.getExcelName() != null && !define.getExcelName().equals("")){
	//		context.setDataValue("_FBS_EXCEL_TEMPLET_NAME", define.getExcelName());
	//	}
		
		QueryCfgDefine tmpDefine = makeQueryCfgDefine(define);
		String tempSql = "select * from (select tt.*, ROWNUM as rowno from ( ("
				+ define.getSQLStr() + ")" + (orderBySql == null ? "" : orderBySql)
				+ ") tt  where rownum <= #endNo# ) t where t.rowno > #startNo#";
		tempSql = getPageSQL(context, tempSql, Integer.parseInt(recordSize)); //动态拼接分页sql
		tmpDefine.setSQLStr(tempSql);
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0, "查询执行的sql:"+tmpDefine.getSQLStr());
		
		SqlExecAction.execute(context, tmpDefine);
	}

	/**
	 * 查询数据条数
	 * 
	 * @param context
	 *            emp上下文对象
	 * @param sqlWhere
	 *            条件参数
	 * @param execService
	 *            执行sql的服务
	 * @param connection
	 *            数据连接对象
	 * */
	private String querySqlCount(Context context, SQLDefine define)
			throws EMPException {
		SQLDefine tmpSqlDefine = null;
		QueryCfgDefine aQueryCfgDefine = null;
		String recordSizeStr = "0";
		
		//导出excel 模板数据定义
		if (define instanceof QueryCfgDefine) {
			aQueryCfgDefine = (QueryCfgDefine)define;
			if(aQueryCfgDefine.getExcelName() != null && !aQueryCfgDefine.getExcelName().equals("")){
				context.setDataValue("_FBS_EXCEL_TEMPLET_NAME", aQueryCfgDefine.getExcelName());
			}
		}

		tmpSqlDefine = makeCountSqlDefine(define);
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0, "查询数量执行的sql:"	+ tmpSqlDefine.getSQLStr());

		SqlExecAction.execute(context, tmpSqlDefine);
		recordSizeStr = (String) context.getDataValue("recordSize");
		if (recordSizeStr.equals("0")) {
			return "0";
		}

		return recordSizeStr;
	}

	
	/**
	 * 创建一个新的CountSqlDefine
	 * @param SFSqlDefine
	 * @return
	 * @throws EMPException
	 */
	private SQLDefine makeCountSqlDefine(SQLDefine SFSqlDefine)
			throws EMPException {
		String sqlStr = SFSqlDefine.getSQLStr();
		sqlStr = "select count(*) from (" + sqlStr + ")";

		SQLDefine tempSqlDefine = new SQLDefine();
		tempSqlDefine.setName(SFSqlDefine.getName() + "_C");
		tempSqlDefine.setSqlType("select");
		tempSqlDefine.setSQLStr(sqlStr);
		tempSqlDefine.setSqlHint(SFSqlDefine.getSqlHint());
		tempSqlDefine.setParaBind(SFSqlDefine.isParaBind());
		tempSqlDefine.setInput((SQLParameterCollection) SFSqlDefine
				.getInParamaters());
		SQLParameterCollection tempOutput = new SQLParameterCollection();
		SQLParameter tempParam = new SQLParameter();
		tempParam.setIdx(1);
		tempParam.setDataName("recordSize");
		tempOutput.add(tempParam);
		tempSqlDefine.setOutput(tempOutput);
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0,
				"创建CountSqlDefine成功");
		return tempSqlDefine;
	}
	
	/**
	 * 创建一个新的FBSQueryCfgDefine
	 * @param SQLDefine
	 * @return
	 * @throws EMPException
	 */
	private QueryCfgDefine makeQueryCfgDefine(SQLDefine define)
			throws EMPException {

		QueryCfgDefine tempSqlDefine = new QueryCfgDefine();
		tempSqlDefine.setName(define.getName());
		tempSqlDefine.setSqlType("select");
		tempSqlDefine.setInput((SQLParameterCollection) define
				.getInParamaters());
		tempSqlDefine.setICollName(define.getICollName());
		tempSqlDefine.setOutput((SQLParameterCollection) define.getOutParamaters());
		EMPLog.log("FBSPageQueryTemplateAction", EMPLog.DEBUG, 0,
				"创建FBSQueryCfgDefine成功");
		return tempSqlDefine;
	}

	/**
	 * 得到最终分页语句
	 * 
	 * @param context
	 *            emp上下文对象
	 * @param sql
	 * @param recordSize
	 *            条件的记录数
	 * */
	public String getPageSQL(Context context, String sql, int recordSize)
			throws EMPException {

		String curSql = sql;
		int begin = 0;
		int end = 0;
		String maxLineStr = (String) context.getDataValue("maxLine");
		String targetPageStr = (String) context.getDataValue("targetPage");
		int maxLine = 10;
		if (maxLineStr != null)
			maxLine = Integer.parseInt(maxLineStr);
		else
			context.setDataValue("maxLine", "10");
		int targetPage = 1;
		if (targetPageStr != null) {
			targetPage = Integer.parseInt(targetPageStr);
			context.setDataValue("targetPage", targetPage);
		} else {
			context.setDataValue("targetPage", "1");
		}

		begin = maxLine * (targetPage - 1);
		if (maxLine * targetPage > recordSize)
			end = recordSize;
		else {
			end = maxLine * targetPage;
		}
		curSql = curSql.replaceAll("#startNo#", String.valueOf(begin));
		curSql = curSql.replaceAll("#endNo#", String.valueOf(end));
		return curSql;
	}

}
