package cloud.apposs.bootor.sample.action;

import cloud.apposs.ioc.annotation.Prototype;
import cloud.apposs.react.IoFunction;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Action;
import cloud.apposs.rest.annotation.Executor;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.WriteCmd;

@Action
@Prototype
@WriteCmd
public class PayAction {
    @Request.Read({"/pay", "/give"})
    public React<String> pay() {
        return React.just("I am pay in " + Thread.currentThread());
    }

    @Request.Read("/pay2")
    public React<String> pay2() {
        return React.just("I am pay2 in " + this.toString());
    }

    @Executor
    @Request.Read("/pay3")
    public React<String> pay3() {
        return React.just("Hello").map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s + " in " + Thread.currentThread();
            }
        });
    }

    @Request.Read("/pay4")
    public React<String> pay4() {
        return React.just("Hello2").map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s + " in " + Thread.currentThread();
            }
        });
    }

    /**
     * 测试拦截器返回false
     */
    @Request.Read("/pay5")
    public React<String> pay5() {
        return React.just("Hello3").map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s + " in " + Thread.currentThread();
            }
        });
    }
}
