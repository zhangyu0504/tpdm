package core.jdbc.sql;

import java.sql.Types;

/**
*
* <b>功能描述：</b><br>
* SQL语句输入输出数据项定义。<br>
* 
* <b>变量说明:</b><br>
*	dataName--SQL语句中的表列对应的Context数据名称<br>
*	dataType--数据类型，支持的类型有：CHAR,VARCHAR,LONGVARCHAR,INTEGER,DECIMAL等<<br>
*	paramIdx--数据项索引值<br>
*
* 
*   @创建时间 2001-03-02
*   @author  ZhongMingChang
*   @modifier GaoLin 2006-10-23
*   
*/

public class SQLParameter {
	public String dataName;

	public int dataType = Types.VARCHAR;

	public int paramIdx = 1; //1 based paramIdx

	public boolean trim = true;

	private boolean paraBind = true;

	public boolean isParaBind() {
		return paraBind;
	}

	public void setParaBind(boolean paraBind) {
		this.paraBind = paraBind;
	}

	public void setDataName(String value) {
		dataName = value;
	}

	public void setIdx(int idx) {
		paramIdx = idx;
	}

	public void setTrim(boolean trim) {
		this.trim = trim;
	}
	
	public void setDataType(String typeStr) {
		if ("CHAR".equals(typeStr) || "CHARACTER".equals(typeStr))
			dataType = Types.CHAR;
		else if ("VARCHAR".equals(typeStr))
			dataType = Types.VARCHAR;
		else if ("LONGVARCHAR".equals(typeStr) || "VARCHAR2".equals(typeStr))
			dataType = Types.LONGVARCHAR;

		else if ("DECIMAL".equals(typeStr))
			dataType = Types.DECIMAL;
		else if ("NUMERIC".equals(typeStr))
			dataType = Types.NUMERIC;

		else if ("INT".equals(typeStr) || "INTEGER".equals(typeStr))
			dataType = Types.INTEGER;
		else if ("TINYINT".equals(typeStr))
			dataType = Types.TINYINT;
		else if ("SMALLINT".equals(typeStr))
			dataType = Types.SMALLINT;
		else if ("BIGINT".equals(typeStr))
			dataType = Types.BIGINT;

		else if ("BIT".equals(typeStr))
			dataType = Types.BIT;
		else if ("BOOLEAN".equals(typeStr))
			dataType = Types.BOOLEAN;

		else if ("REAL".equals(typeStr))
			dataType = Types.REAL;
		else if ("FLOAT".equals(typeStr))
			dataType = Types.FLOAT;
		else if ("DOUBLE".equals(typeStr))
			dataType = Types.DOUBLE;

		else if ("BINARY".equals(typeStr))
			dataType = Types.BINARY;
		else if ("VARBINARY".equals(typeStr))
			dataType = Types.VARBINARY;
		else if ("LONGVARBINARY".equals(typeStr))
			dataType = Types.LONGVARBINARY;
		
		else if ("DATE".equals(typeStr))
			dataType = Types.DATE;
		else if ("TIME".equals(typeStr))
			dataType = Types.TIME;
		else if ("TIMESTAMP".equals(typeStr))
			dataType = Types.TIMESTAMP;

	}

	public void setFieldIndex(String idxStr) {
		try {
			paramIdx = Integer.parseInt(idxStr);
		} catch (Exception e) {

		}
	}
	
	/**
	 * 对数据类型进行检查
	 * @param value
	 * @return
	 */
	public boolean validateType(Object value){
		if(value==null)
			return true;
		try{
			String valueStr=value.toString();
			if(this.dataType==Types.DECIMAL||this.dataType==Types.INTEGER||this.dataType==Types.DOUBLE||this.dataType==Types.SMALLINT||this.dataType==Types.REAL||this.dataType==Types.FLOAT){
				try{
					Double.valueOf(valueStr);
				}catch(Exception e){
					return false;
				}
			}else if(this.dataType==Types.BOOLEAN){
				try{
					Boolean.valueOf(valueStr);
				}catch(Exception e){
					return false;
				}
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
}
