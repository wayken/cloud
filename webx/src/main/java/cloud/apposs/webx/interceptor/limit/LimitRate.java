package cloud.apposs.webx.interceptor.limit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务限流注解，基于此注解的方法或类均会进行流速校验
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitRate {
    /**
     * 计量单位，单位为秒，默认1分钟
     */
    int measurement() default 60;

    /**
     * 限流阀值，一个计量单位内允许访问的次数
     */
    int threshold() default 10;

    /**
     * 超限后需要拦截的时长，单位为秒，默认 0 表示不拦截；大于0 的数值表示将限流Key纳入黑名单的时长
     */
    int forbidden() default 0;

    /**
     * 限流策略，可由业务自己实现不同的限流策略
     */
    Class<? extends ILimitRate> ilimit() default DefaultLimitRate.class;
}
