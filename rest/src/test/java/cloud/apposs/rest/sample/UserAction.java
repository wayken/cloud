package cloud.apposs.rest.sample;

import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Action;
import cloud.apposs.rest.annotation.Request;

@Action
public class UserAction {
    @Request("/")
    public React<String> root() {
        return React.just("Hello Index Html");
    }
}
