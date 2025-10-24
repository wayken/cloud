package cloud.apposs.bootor.filter;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;

/**
 * 过滤器接口，用于对请求进行过滤处理，和{@link cloud.apposs.rest.interceptor.HandlerInterceptor}的区别在于，
 * <pre>
 *     1、过滤器是在请求的最前面进行处理，而拦截器是在请求的最后面进行处理，所以过滤器可以对请求进行预处理，例如进行CORS处理等
 *     2、过滤器不会处理Handler的执行，而拦截器会处理Handler的执行
 *     3、过滤器可以对所有请求进行拦截，而拦截器只能对Handler的执行进行拦截，所以如果没有找到匹配的Handler，那么拦截器也不会执行，而过滤器会执行
 * </pre>
 */
public interface IFilter {
    /**
     * 过滤器处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @return 返回true表示继续执行后续的过滤器，返回false表示停止执行后续的过滤器
     * @throws Exception
     */
    boolean filter(BootorHttpRequest request, BootorHttpResponse response) throws Exception;
}
