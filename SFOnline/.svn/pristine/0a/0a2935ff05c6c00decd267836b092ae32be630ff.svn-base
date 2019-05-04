package common.action.dataoper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.InvalidArgumentException;
import com.ecc.emp.data.ObjectNotFoundException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.sql.SQLParameterCollection;
import com.ecc.emp.log.EMPLog;
import common.action.db.SqlExecAction;
import common.exception.SFException;
import common.services.SQLDefine;
import common.services.SqlExecService;

/**
 * PageQueryAction.java<br>
 * EMP 交易步骤扩展<br>
 * Extends class EMPAction<br>
 * @autor <br>
 * @emp:name PageQueryAction
 * @emp:catalog extendedElements
 * @emp:states 0=成功;-1=异常;
 * @emp:document
 */
public class SFPageQueryParamAction extends EMPAction {

	/* 业务逻辑操作单元的执行入口 */
	private String pageSqlPrefix = "SELECT * FROM ( SELECT temp.*, rownum as aRowNumber FROM("; // 拼接SQL语句的前半部分
	private String pageSqlPostfix = " )  temp  where  rownum<= "; // 拼接sql
																	// 语句的后半部分
	private String pageQuerySql = ""; // 执行分页的语句
	private String pageCountSql = "";// 查询最大条数的语句
	private SQLDefine pageExecDefine = null;
	private SQLDefine pageExecCountDefine = null;
	private List sqlDefineRef; // 总记录
	private String orderByStr = ""; // 排序的SQL
	private String paramStr; // 动态参数的sql
	private String groupByStr = "";
	private String paramOrderByStr = ""; // 动态排序条件

	/* 业务逻辑操作单元的执行入口 */
	public String execute(Context context) throws EMPException {

		String recordSize = null; // 总记录数
		SqlExecService execService = null; // 执行sql语句的服务 。 网数据集合里赋值数据
		Connection connection = null; // 数据库连接

		// 动态参数解决
		String sqlWhere = "";
		if (this.paramStr != null && this.paramStr.length() > 0) {
			List paramList = new ArrayList();
			List childList = new ArrayList();
			Object paramobject = this.paramStr;
			String param = paramobject.toString();

			String paramStr[] = param.split(";");

			for (int i = 0; i < paramStr.length; i++) {
				String childStr[] = paramStr[i].split(":");
				if (childStr.length == 1) {
					childList.add(childStr[0].toString());
				}
				if (childStr.length == 4) {
					paramList.add(childStr);
				}
				if (childStr.length == 3) {
					paramList.add(childStr);
				}
			}

			sqlWhere = makeCondition(paramList, childList, context);
			if (groupByStr != null && groupByStr.length() > 0) {
				sqlWhere += "  " + groupByStr;
			}

		}
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "组装生成的条件:"
				+ sqlWhere);
		// 动态参数解决完毕
		recordSize = this.makeSqlCount(context, sqlWhere, execService,
				connection);
		if (recordSize == null || recordSize == "0") {
			throw new SFException("P0000S100", "记录不存在！");
			// return "2";
		}
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "数据结果的大小:"
				+ recordSize);

		// 执行排序 ，如果配置动态排序，将不会执行默认的排序条件。如果动态排序为空，则执行默认排序

		if (paramOrderByStr != null && paramOrderByStr.length() > 0) {

			sqlWhere += " " + getFields(paramOrderByStr, context, "", "") + " ";

		} else if (orderByStr != null && orderByStr.length() > 0) {
			sqlWhere += " order by " + orderByStr;
		}
		this.makeSql(context, sqlWhere, recordSize, execService, connection);

		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "分页语句执行成功");
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

	private void makeSql(Context context, String sqlWhere, String recordSize,
			SqlExecService execService, Connection connection)
			throws EMPException {

		SQLDefine define = (SQLDefine) context
				.getService(this.pageQuerySql);
		if (this.pageExecDefine == null) {
			this.pageExecDefine = this.makeFbsSqlDefine(define);
		}
		String tempSql = define.getSQLStr() + sqlWhere;
		try {
			ArrayList<Integer> indexList = (ArrayList<Integer>) getPageIndex(
					context, tempSql, Integer.parseInt(recordSize));
			tempSql = this.pageSqlPrefix + tempSql + this.pageSqlPostfix + " "
					+ indexList.get(1) + " ) t where t.aRowNumber >"
					+ indexList.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.pageExecDefine.setSQLStr(tempSql);
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "查询执行的sql"
				+ pageExecDefine.getSQLStr());
		SqlExecAction.execute(context, pageExecDefine);
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
	private String makeSqlCount(Context context, String sqlWhere,
			SqlExecService execService, Connection connection)
			throws EMPException {

		String recordSizeStr = "0";
		SQLDefine aDefine = (SQLDefine) context
				.getService(this.pageCountSql);
		if (this.pageExecCountDefine == null) {
			this.pageExecCountDefine = this.makeFbsSqlDefine(aDefine);
		}
		pageExecCountDefine.setSQLStr(aDefine.getSQLStr() + sqlWhere);
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "查询数量执行的sql"
				+ pageExecCountDefine.getSQLStr());
		SqlExecAction.execute(context, pageExecCountDefine);
		recordSizeStr = (String) context.getDataValue("recordSize");
		if (null == recordSizeStr || recordSizeStr.equals("0")) {
			return null;
		}

		return recordSizeStr;
	}

	/**
	 * 创建一个新的FBSSqlDefine
	 * 
	 * @param SFSqlDefine
	 *            一个FbsSqlDefine 对象
	 * 
	 * */
	private SQLDefine makeFbsSqlDefine(SQLDefine SFSqlDefine) {

		try {
			SQLDefine tempsqlDefine = new SQLDefine();
			tempsqlDefine.setName(SFSqlDefine.getName());
			tempsqlDefine.setErrorCode(SFSqlDefine.getErrorCode()); // 错误码
			tempsqlDefine.setErrorMessage(SFSqlDefine.getErrorMessage()); // 错误消息
			tempsqlDefine.setICollName(SFSqlDefine.getICollName());// 数据集合
			tempsqlDefine.setInput((SQLParameterCollection) SFSqlDefine
					.getInParamaters()); // 输入参数集合
			tempsqlDefine.setMaxRecords(SFSqlDefine.getMaxRecords());// 最大记录数据
			tempsqlDefine.setOutput((SQLParameterCollection) SFSqlDefine
					.getOutParamaters());// 获得输出参数
			tempsqlDefine.setSqlType("select"); // 执行的sql语句的类型
			tempsqlDefine.setSqlHint(SFSqlDefine.getSqlHint());
			tempsqlDefine.setParaBind(SFSqlDefine.isParaBind());
			EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0,
					"创建FbsSqlDefine成功");
			return tempsqlDefine;
		} catch (Exception ce) {
			EMPLog.log("FBSPageQueryParamAction", EMPLog.ERROR, 0,
					"创建FbsSqlDefine时候出错", ce);
		}
		return null;
	}

	/**
	 * 动态添加网点sql 条件
	 * 
	 * @param 当前sql条件
	 * */
	// public String makeBranchNo(String sqlWhere,Context context){
	//		
	// if(sqlWhere!="" && sqlWhere!=null && sqlWhere.length()>=0){
	// sqlWhere += " and ";
	// }else{
	// sqlWhere +=" where ";
	// }
	// //String branch_no
	// =FBSPublicFunction.getContextValueInAction(context,"_FBS_USER_BRNO");
	// try {
	// sqlWhere
	// +="  branch_no in (select * from TABLE(fbsweb.getsubbranch('"+this.getSubordinateBranchNoOpen()+"','"+this.getSubordinateBranchNoOpen()+"','"+context.getDataValue("_FBS_USER_BRNO").toString()+"'))) ";
	//			
	// } catch (ObjectNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (InvalidArgumentException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return sqlWhere;
	// }
	/**
	 * 动态拼接sql条件
	 * 
	 * @param paramList
	 *            存储；分隔的条件
	 * @author childList 存储连接条件
	 * 
	 * */
	private String makeCondition(List paramList, List childList, Context context) {
		// 1是加% 和 ‘ 号 2是加% 不加’号 3 是加‘不加% 1=both 2=right

		String returnStr = " where ";
		String sqlWhereTemp = "";

		List conditionList = new ArrayList();
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "进入条件判断");
		for (int i = 0; i < paramList.size(); i++) {
			List stringList = new ArrayList();
			String param[] = (String[]) paramList.get(i);
			// *******************************
			String paramValue = "";
			EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0,
					"param[0].toString()" + param[2].toString());
			if (param[2].toString().indexOf("$") >= 0) {
				paramValue = (String) this.getFields(param[2].toString(),
						context, "", "");// context.getDataValue();
				EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0,
						"paramValue@@@@@@@@" + paramValue);
				if (paramValue != null && paramValue.length() != 0) {

					if (param[1].compareToIgnoreCase("Like") == 0) {
						String tempStr = "";
						if (param[3].compareToIgnoreCase("both") == 0) {
							sqlWhereTemp = " "
									+ param[0]
									+ " "
									+ param[1]
									+ " "
									+ (String) this.getFields(param[2]
											.toString(), context, "'%", "%'");
						} else if (param[3].compareToIgnoreCase("right") == 0) {
							sqlWhereTemp = " "
									+ param[0]
									+ " "
									+ param[1]
									+ " "
									+ (String) this.getFields(param[2]
											.toString(), context, "'", "%'");
						} else if (param[3].compareToIgnoreCase("left") == 0) {
							sqlWhereTemp = " "
									+ param[0]
									+ " "
									+ param[1]
									+ " "
									+ (String) this.getFields(param[2]
											.toString(), context, "'%", "'");
						}

					} else {
						if (param[2].toString().startsWith("$")) {
							sqlWhereTemp = " " + param[0] + " " + param[1]
									+ " " + "'" + paramValue + "'";
						} else {
							sqlWhereTemp = " " + param[0] + " " + param[1]
									+ " " + " " + paramValue + " ";
						}
						if (param.length == 4) {
							if (param[3].compareToIgnoreCase("ymd") == 0) {
								sqlWhereTemp = " "
										+ param[0]
										+ " "
										+ param[1]
										+ " "
										+ (String) this.getFields(param[2]
												.toString(), context,
												"to_date('", "','yyyymmdd')");
							} else if (param[3].compareToIgnoreCase("hms") == 0) {
								sqlWhereTemp = " "
										+ param[0]
										+ " "
										+ param[1]
										+ " "
										+ (String) this.getFields(param[2]
												.toString(), context,
												"to_date('",
												"','yyyymmdd hh24:mi:ss')");
							}
						}
					}
					stringList.add(sqlWhereTemp);
					stringList.add(i);
					conditionList.add(stringList);

				}
				// *****************
			} else {

				paramValue = param[2].toString();
				sqlWhereTemp = " " + param[0] + " " + param[1] + " " + ""
						+ paramValue + "";

				stringList.add(sqlWhereTemp);
				stringList.add(i);
				conditionList.add(stringList);

				EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0,
						"paramValue  " + paramValue);

			}

		}
		// 判断条件是否为空 。 如果为空返回空
		if (conditionList.size() == 0) {
			returnStr = "";
		}

		for (int z = 0; z < conditionList.size(); z++) {
			List conditionChildList = (ArrayList) conditionList.get(z);
			returnStr += conditionChildList.get(0);
			if ((z + 1) < conditionList.size()) {
				returnStr += "  "
						+ childList.get(Integer.parseInt(conditionChildList
								.get(1).toString()));
			}
		}
		return returnStr;
	}

	/**
	 * 动态拼接分页的条件
	 * 
	 * @param context
	 *            emp上下文对象
	 * @param sql
	 * @param recordSize
	 *            条件的记录数
	 * */
	private List getPageIndex(Context context, String sql, int recordSize)
			throws Exception {
		List<Integer> indexList = new ArrayList<Integer>();
		// String curSql = sql;
		int begin = 0;
		int end = 0;
		String maxLineStr = (String) context.getDataValue("maxLine");
		String targetPageStr = (String) context.getDataValue("targetPage");
		int maxLine = 10;
		if (maxLineStr != null) {
			maxLine = Integer.parseInt(maxLineStr);
			context.setDataValue("maxLine", maxLine);
		} else {
			context.setDataValue("maxLine", "10");
		}

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
		indexList.add(begin);
		indexList.add(end);
		return indexList;
	}

	private String transactionType;

	/**
	 * @emp:name 事务类型
	 * @emp:desc
	 * @emp:mustSet true
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList TRX_REQUIRED=应用全局事务;TRX_REQUIRE_NEW=创建独有事务;
	 */
	public void setTransactionType(String newTransactionType) {
		transactionType = newTransactionType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * @emp:name 数据源
	 * @emp:desc
	 * @emp:mustSet true
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	// public void setDataSourceName(String newDataSourceName) {
	// dataSourceName = newDataSourceName;
	// }
	// public String getDataSourceName() {
	// return dataSourceName;
	// }

	/**
	 * @emp:name 执行SQL语句的服务
	 * @emp:desc
	 * @emp:mustSet true
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	// public void setSqlServiceName(String newSqlServiceName) {
	// sqlServiceName = newSqlServiceName;
	// }
	// public String getSqlServiceName() {
	// return sqlServiceName;
	// }
	//	

	/**
	 * @emp:name 分页的SQL语句
	 * @emp:desc
	 * @emp:mustSet true
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	public void setPageQuerySql(String newPageQuerySql) {
		pageQuerySql = newPageQuerySql;
	}

	public String getPageQuerySql() {
		return pageQuerySql;
	}

	/**
	 * @emp:name 查询集合数SQL
	 * @emp:desc
	 * @emp:mustSet true
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	public void setPageCountSql(String newPageCountSql) {
		pageCountSql = newPageCountSql;
	}

	public String getPageCountSql() {
		return pageCountSql;
	}

	/**
	 * @emp:name
	 * @emp:desc
	 * @emp:mustSet false
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	public String getParamStr() {
		return paramStr;
	}

	public void setParamStr(String paramStr) {
		this.paramStr = paramStr;
	}

	/**
	 * @emp:name 分页的排序条件
	 * @emp:desc
	 * @emp:mustSet false
	 * @emp:attrType string
	 * @emp:isAttribute true
	 * @emp:valueList
	 */
	public String getOrderByStr() {
		return orderByStr;
	}

	public void setOrderByStr(String orderByStr) {
		this.orderByStr = orderByStr;
	}

	private String getParamOrderByStr() {
		return paramOrderByStr;
	}

	public void setParamOrderByStr(String paramOrderByStr) {
		this.paramOrderByStr = paramOrderByStr;
	}

	private String getGroupByStr() {
		return groupByStr;
	}

	public void setGroupByStr(String groupByStr) {
		this.groupByStr = groupByStr;
	}

	/**
	 * 执行表达式,将表达式里面的动态变量,替换成context中相应的值
	 * 
	 * @param expree
	 *            需要执行的表达式
	 * @param context
	 *            数据上下文
	 * @param startExp
	 *            表达式前缀
	 * @param endExp
	 *            表示式后缀
	 * 
	 * */
	private String getFields(String expree, Context context, String startExp,
			String endExp) {
		String returnStr = "";
		StringBuffer sb = new StringBuffer(expree);
		List<String> feilds = new ArrayList<String>();

		while (true) {
			int sidx = sb.indexOf("$(");

			if (sidx < 0) {
				break;
			}
			int eidx = sb.indexOf(")", sidx + 2);
			feilds.add(sb.substring(sidx + 2, eidx));
			sb = new StringBuffer(sb.substring(eidx));
		}

		for (int i = 0; i < feilds.size(); i++) {

			try {
				if (context.getDataValue(feilds.get(i).toString()) == null
						|| context.getDataValue(feilds.get(i).toString())
								.toString().length() == 0) {
					expree = "";
				} else {
					String temp = context
							.getDataValue(feilds.get(i).toString()).toString();
					if (context.getDataValue(feilds.get(i).toString())
							.toString().indexOf("'") == -1) {
						expree = expree.replace("$(" + feilds.get(i) + ")",
								startExp + "" + temp + "" + endExp);
					} else {
						expree = expree.replace("$(" + feilds.get(i) + ")",
								startExp + "" + temp.replaceAll("'", "''") + ""
										+ endExp);
					}
				}

			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				e.printStackTrace();
			}
		}
		EMPLog.log("FBSPageQueryParamAction", EMPLog.DEBUG, 0, "returnStr"
				+ returnStr);
		return expree;
	}
}
