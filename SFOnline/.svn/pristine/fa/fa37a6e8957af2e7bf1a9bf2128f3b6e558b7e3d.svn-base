package common.action.dataoper;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.flow.EMPAction;
import com.ecc.emp.jdbc.ConnectionManager;
import com.ecc.emp.jdbc.RecordNotFoundException;
import com.ecc.emp.jdbc.sql.SQLParameterCollection;
import com.ecc.emp.log.EMPLog;
import common.services.SQLDefine;
import common.services.SqlExecService;
import common.util.SFConst;

/**
 * PageQueryAction.java<br>
 * EMP 交易步骤扩展<br>
 * Extends class EMPAction<br>
 * @autor      <br>

 * @emp:name PageQueryAction
 * @emp:catalog extendedElements
 * @emp:states 0=成功;-1=异常;
 * @emp:document 
 */
public class SFPageQueryAction extends EMPAction {
 
	/*业务逻辑操作单元的执行入口*/
	private String pageSqlPrefix="SELECT * FROM (";   //拼接SQL语句的前半部分 
	private String rowNumberSqlFix=", rownum as aRowNumber ";  //基于Oracle 里面的rownum
	private String pageSqlPostfix=" )  temp  where ";    //拼接sql 语句的后半部分 
	private String dataSourceName="datasource"; //数据源
	private String sqlServiceName="sqlservice";  //执行sql语句的服务
	private String pageQuerySql="" ; //执行分页的语句
	private String pageCountSql="";//查询最大条数的语句
	private SQLDefine pageExecDefine=null;	 
	private List sqlDefineRef;  //总记录
    
	/*业务逻辑操作单元的执行入口*/
	public String execute(Context context) throws EMPException
	{
		
		String recordSize=null;  //总记录数
		DataSource dataSource =null;  //数据源
		SqlExecService execService =null;  //执行sql语句的服务 。 网数据集合里赋值数据
		
		Connection connection =null; //数据库连接
		if(this.dataSourceName !=null && this.dataSourceName.length()!=0 )
		{
			dataSource = (DataSource)context.getService((String)context.getDataValue(SFConst.SERVICE_DATASOURCE));
			
			if(dataSource==null)
			{
				throw new EMPException("dataSource named \"" + dataSourceName+"\"is not found in JDBCSQLExecAction:" + this.toString());
			}
		}
		//判断执行sql语句的服务是否为空  
	   if(this.sqlServiceName!=null && this.sqlServiceName.length()!=0)
		{
			execService = (SqlExecService)context.getService((String) context.getDataValue(SFConst.SERVICE_SQL));
			if(execService==null)
			{
				throw new EMPException("JDBCSQLExecService not set for JDBCSQLAction:"+ this.toString());
			}
		} 
		
		
		// String recordSizeStr = (String)context.getDataValue("recordSize");
	    //	recordSize = "100";   //
		//通过emp平台的连接管理器。获得连接对象
		connection = ConnectionManager.getConnection(dataSource);
		
		
		
		
		
	     String recordSizeStr = (String)context.getDataValue("recordSize");  //如果是第二次进入 数据里会总记录数
	   try
	    {
	      if (recordSizeStr == null) {
 
	    	    if(this.sqlDefineRef!=null)
	    	    {
	    	       for (int i = 0; i < this.sqlDefineRef.size(); ++i) {
	                     String sqlId = (String)this.sqlDefineRef.get(i);
	                     SQLDefine aDefine = (SQLDefine)context.getService(sqlId);
	                      
	                     if (aDefine == null) {
	                        // throw new EMPException("JDBCSQLDefine [" + sqlId + "] not valid!");
	                       }
	                 execService.executeSQLDef(aDefine, context, connection);  //查询数据的总条数
	                }
	    	    }else
	    	    {
	    	    	SQLDefine aDefine = (SQLDefine)context.getService(this.pageCountSql);
	    
	    	    	execService.executeSQLDef(aDefine, context, connection);  //查询数据的总条数
	    	    }

	        recordSizeStr = (String)context.getDataValue("recordSize");
	        if(null==recordSizeStr  || recordSizeStr.equals("0"))
	        {
	        	return "2";
	        }
	       EMPLog.log("PageQueryAction",EMPLog.DEBUG, 0, "数据集合的数量:"+recordSizeStr);
	      }
	     }catch(RecordNotFoundException e)
	     {
	    	 return "2";
	     }
	      recordSize = recordSizeStr;
	      
		
		
		
		
		
		
		//将获得的sql 封装成sql定义的数据对象。
	      SQLDefine define = (SQLDefine)context.getService(this.pageQuerySql);
		
		 if(this.pageExecDefine==null)
		 {
			 SQLDefine tempsqlDefine = new SQLDefine();
			 tempsqlDefine.setErrorCode(define.getErrorCode()); //错误码
			 tempsqlDefine.setErrorMessage(define.getErrorMessage()); //错误消息
			 tempsqlDefine.setICollName(define.getICollName());//数据集合
			 tempsqlDefine.setInput((SQLParameterCollection)define.getInParamaters());  //输入参数集合
			 tempsqlDefine.setMaxRecords(define.getMaxRecords());//最大记录数据
			 tempsqlDefine.setOutput((SQLParameterCollection)define.getOutParamaters());//获得输出参数
			 tempsqlDefine.setSqlType("select");  //执行的sql语句的类型
			 tempsqlDefine.setSqlHint(define.getSqlHint());  //执行的sql语句的类型
			 tempsqlDefine.setParaBind(define.isParaBind());
			 this.pageExecDefine = tempsqlDefine;
		 }
		 String tempSql= define.getSQLStr().toUpperCase();//将sql语句转换成大写
		 int index = tempSql.indexOf(" FROM");  //获得第一个from所在的位置
		 tempSql = this.pageSqlPrefix + tempSql.substring(0, index) + this.rowNumberSqlFix + tempSql.substring(index) + this.pageSqlPostfix;
	
	    
		 
		   try {
			    tempSql = getPageIndex(context, tempSql, Integer.parseInt(recordSize));
				// System.out.println("组装成的SQL语句"+tempSql);
		       } catch (Exception e) {
			      e.printStackTrace();
		       } 
		 
		 this.pageExecDefine.setSQLStr(tempSql);
		 execService.executeSQLDef(pageExecDefine, context, connection);
		 if (connection != null)
		 {
		 ConnectionManager.releaseConnection(dataSource, connection);
		 }
		 return "0";
	}
	 public String getPageIndex(Context context, String sql, int recordSize)
		throws Exception {
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
	      curSql = curSql + "aRowNumber>" + begin + " AND aRowNumber<=" + end;
	      return curSql;
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
	public void setDataSourceName(String newDataSourceName) {
		dataSourceName = newDataSourceName;
	}
	public String getDataSourceName() {
		return dataSourceName;
	}


 /**
  * @emp:name 执行SQL语句的服务
  * @emp:desc 
  * @emp:mustSet true
  * @emp:attrType string
  * @emp:isAttribute true
  * @emp:valueList 
 */
	public void setSqlServiceName(String newSqlServiceName) {
		sqlServiceName = newSqlServiceName;
	}
	public String getSqlServiceName() {
		return sqlServiceName;
	}
	

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
}
