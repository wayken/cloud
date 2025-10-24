package cloud.apposs.webx.interceptor.limit;

import cloud.apposs.cache.Cache;
import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cache.jvm.JvmCache;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.ApplicationContextHolder;
import cloud.apposs.rest.Handler;
import cloud.apposs.webx.interceptor.WebXInterceptorAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 业务限流拦截器
 * 若请求的LimitKey在缓存中统计中超过访问次数上限，则拉黑一段时间
 */
@Component
public class LimitRateInterceptor extends WebXInterceptorAdaptor {
    private ApplicationContextHolder context;

    private volatile Cache cache;

    private static final String BLACK_PREFIX = "LimitBlack:";

    @Override
    public void initialize(ApplicationContextHolder context) {
        this.context = context;
    }

    @Override
    public React<Boolean> preHandle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 判断Action类或者方法没有配置Limit限流注解，没有则直接跳过
                LimitRate limitAnnotation = handler.getAnnotation(LimitRate.class);
                if (limitAnnotation == null) {
                    limitAnnotation = handler.getClazz().getAnnotation(LimitRate.class);
                }
                if (limitAnnotation == null) return true;
                ILimitRate ilimit = context.getBean(limitAnnotation.ilimit());
                // 没有对应的限流策略实现类也跳出
                if (ilimit == null) return true;
                // 初次请求时才加载缓存，避免业务没有注解限流，但缓存一直开着占用资源
                if (cache == null) {
                    synchronized (Cache.class) {
                        if (cache == null) {
                            CacheConfig config = new CacheConfig();
                            config.getJvmConfig().setExpirationTime(limitAnnotation.measurement() * 1000);
                            config.getJvmConfig().setExpirationTimeRandom(false);
                            cache = new JvmCache(config);
                        }
                    }
                }
                // 判断是否已经在黑名单中
                String limitKey = ilimit.getLimitKey(request);
                if (cache.exists(BLACK_PREFIX + limitKey)) {
                    return false;
                }
                // 判断请求的LimitKey是否在缓存中超过访问次数上限
                int threshold = limitAnnotation.threshold();
                if (cache.incr(limitKey) > threshold) {
                    // 加入黑名单
                    int forbidden = limitAnnotation.forbidden();
                    if (forbidden > 0) {
                        cache.put(BLACK_PREFIX + limitKey, 1);
                        cache.expire(BLACK_PREFIX, forbidden);
                    }
                    throw new LimitRateException(limitKey);
                }
                return true;
            }
        });
    }
}
