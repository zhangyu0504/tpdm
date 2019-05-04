package common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * ������������
 * ����������ǰBEAN�����Ƿ��������ݿ����
 * ����������ݿ����,��������
 * ��Ӧ�����ݿ���е�����,Ĭ��Ϊ��ǰ�����������������
 * @author hey
 *
 */
@Target(ElementType.METHOD)
public @interface ColumnName {
	 /**
     * �����ֶα�ʶ
     */
    public boolean updateFlag() default false;
	/**
     * ���ݱ�����ע�⣬Ĭ��ֵΪ������
     * @return
     */
    public String name() default "";
    
}
