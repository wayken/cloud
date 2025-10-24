package cloud.apposs.configure;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Value {
    String value();

    /**
     * 是否自动发现配置文件，如果为true则会自动发现配置文件并更新配置
     */
    boolean updatable() default false;
}
