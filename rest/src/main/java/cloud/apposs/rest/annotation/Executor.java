package cloud.apposs.rest.annotation;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.IHandlerProcess;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Action类线程池注解，当注解指定类或方法时对应的Handler会执行放到线程池中执行，
 * 具体参考{@link cloud.apposs.rest.Restful#renderView(IHandlerProcess, Object, Object)}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Executor {
}
