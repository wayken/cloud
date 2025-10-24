package cloud.apposs.webx.management.api;

import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.Param;
import cloud.apposs.util.StandardResult;

@RestAction
public class HealthyApi {
    @Request.Read("/healthy")
    public React<StandardResult> healthy() {
        return React.emitter(() -> {
            Param response = Param.builder("status", "ok");
            return StandardResult.success(response);
        });
    }
}
