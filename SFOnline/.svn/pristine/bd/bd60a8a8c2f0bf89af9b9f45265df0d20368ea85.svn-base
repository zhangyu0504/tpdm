package common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 对象属性描述
 * 用于描述当前BEAN对象是否用于数据库操作
 * 如果用于数据库操作,必须申明
 * 对应的数据库表中的列名,默认为当前类对象名称属性名称
 * @author hey
 *
 */
@Target(ElementType.METHOD)
public @interface ColumnName {
	 /**
     * 更新字段标识
     */
    public boolean updateFlag() default false;
	/**
     * 数据表名称注解，默认值为类名称
     * @return
     */
    public String name() default "";
    
}
