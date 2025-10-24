package cloud.apposs.bootor.sample.action;

import cloud.apposs.react.IoFunction;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.MediaType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestAction
public class SseAction {
    /**
     * curl -v http://192.168.5.36:8880/sse/interval
     */
    @Request.Get(value = "/sse/interval", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public React<String> sseInterval() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        return React.interval(scheduler, 1000, TimeUnit.MILLISECONDS)
        .map(new IoFunction<Long, String>() {
            @Override
            public String call(Long value) {
                return "->" + value;
            }
        });
    }
}
