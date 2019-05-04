package common.sql.bean;

public class SQLStruct {
	private String sql = null;
	
	private Object[] values = null;

	public SQLStruct(String sql, Object[] values) {
		this.sql = sql;
		this.values = values;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}
	
	
}
