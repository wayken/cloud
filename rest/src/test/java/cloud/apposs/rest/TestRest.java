package cloud.apposs.rest;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.sample.UserAction;
import cloud.apposs.rest.view.AbstractViewResolver;
import org.junit.Test;

import java.util.Map;

public class TestRest {
    @Test
    public void testRequstGet() throws Exception {
        RestConfig restConfig = new RestConfig(TestRest.class);
        restConfig.setBasePackage("cloud.apposs.rest.sample");
        Restful<String , String> rest = new Restful<String , String>(restConfig);
        rest.initialize();
        rest.addViewResolver(new MyViewResolver());

        IHandlerProcess<String, String> handlerProcess = new MyHandlerProcess();
        Handler handler = rest.getHandler(handlerProcess, null, null);
        Object result = rest.invokeHandler(handler, new UserAction(), handlerProcess, null, null);
        rest.renderView(result, null, null);
    }

    @Test
    public void testRestRender() throws Exception {
        RestConfig restConfig = new RestConfig(TestRest.class);
        restConfig.setBasePackage("cloud.apposs.rest.sample");
        Restful<String , String> rest = new Restful<String , String>(restConfig);
        rest.initialize();
        rest.addViewResolver(new MyViewResolver());

        rest.renderView(new MyHandlerProcess(), null, null);
    }

    static class MyHandlerProcess implements IHandlerProcess<String, String> {
        @Override
        public String getRequestMethod(String request, String response) {
            return "GET";
        }

        @Override
        public String getRequestPath(String request, String response) {
            return "/";
        }

        @Override
        public String getRequestHost(String request, String response) {
            return null;
        }

        @Override
        public void processVariable(String request, String response, Map<String, String> variables) {
        }

        @Override
        public void processHandler(String request, String response, Handler handler) {
        }

        @Override
        public IGuardProcess<String, String> getGuardProcess() {
            return null;
        }

        @Override
        public void markAsync(String request, String response) {
        }
    }

    @Component
    public static class MyViewResolver extends AbstractViewResolver<String, String> {
        @Override
        public boolean supports(String request, String response, Object result) {
            return true;
        }

        @Override
        public void render(String request, String response, Object result, boolean flush) throws Exception {
            System.out.println(result);
        }
    }
}
