package cloud.apposs.bootor.sample.action;

import cloud.apposs.bootor.BootorConfig;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.IoFunction;
import cloud.apposs.react.React;
import cloud.apposs.rest.ApplicationContextHolder;
import cloud.apposs.rest.WebExceptionResolver;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;

@RestAction
public class ProductAction {
    private final BootorConfig config;

    private final OkHttp okHttp;

    private final ApplicationContextHolder context;

    /**
     * Action构造方法参数注入
     */
    @Autowired
    public ProductAction(BootorConfig config, OkHttp okHttp, ApplicationContextHolder context) {
        this.config = config;
        this.okHttp = okHttp;
        this.context = context;
    }

    @Request.Read("/product/config")
    public React<String> getConfig() {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                return config.toString();
            }
        });
    }

    @Request.Read("/product/baidu")
    public React<String> getBaidu() throws Exception {
        return okHttp.execute("https://www.baidu.com")
            .map(new IoFunction<OkResponse, String>() {
                @Override
                public String call(OkResponse httpAnswer) throws Exception {
                    return httpAnswer.getContent();
                }
            });
    }

    @Request.Read("/product/resolver")
    public React<String> getResolver() {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                return context.getBeanHierarchy(WebExceptionResolver.class).toString();
            }
        });
    }
}
