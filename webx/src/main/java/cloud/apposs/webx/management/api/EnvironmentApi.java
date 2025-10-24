package cloud.apposs.webx.management.api;

import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;

import java.util.Properties;

@RestAction
public class EnvironmentApi {
    @Request.Read("/environment")
    public React<String> environment() {
        return React.emitter(() -> {
            Properties properties = System.getProperties();
            StringBuilder infomation = new StringBuilder();
            properties.forEach((key, value) -> {
                infomation.append(key).append(" = ").append(value).append("\n");
            });
            return infomation.toString();
        });
    }
}
