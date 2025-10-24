package cloud.apposs.bootor.sample.action;

import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@RestAction
public class MeterRegistryAction {
    private final Counter visitCounter;

    public MeterRegistryAction(MeterRegistry registry) {
        visitCounter = Counter.builder("visit_counter")
                .description("Number of visits to the site")
                .register(registry);
    }

    @Request.Read("/visit")
    public React<String> visit() {
        return React.emitter(() -> {
            visitCounter.increment();
            return "Hello World!";
        });
    }
}
