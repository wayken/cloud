package cloud.apposs.rest.validator.checker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被注释的元素必须是手机号码
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mobile {
    /**
     * 是否强制需要此值
     */
    boolean require() default true;

    /**
     * 错误消息输出
     */
    String message() default "";
}
