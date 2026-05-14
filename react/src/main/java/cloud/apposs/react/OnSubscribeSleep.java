package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 响应式休眠，底层采用的是异步线程定时任务，到达时间后再执行以达到异步休眠的效果
 */
public class OnSubscribeSleep<T> implements OnSubscribe<T> {
    private final React<T> source;

    private final ScheduledExecutorService scheduler;

    private final long sleepTime;

    public OnSubscribeSleep(React<T> source, ScheduledExecutorService scheduler, long sleepTime) {
        this.source = source;
        this.scheduler = scheduler;
        this.sleepTime = sleepTime;
    }

    @Override
    public void call(IoSubscriber<? super T> subscriber) throws Exception {
        SleepSubscriber<T> parent = new SleepSubscriber<T>(subscriber);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    private class SleepSubscriber<T> extends SafeIoSubscriber<T> {
        SleepSubscriber(IoSubscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override
        public void onNext(T value) throws Exception {
            scheduler.schedule(() -> {
                try {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    subscriber.onNext(value);
                } catch(Throwable t) {
                    onError(t);
                }
            }, sleepTime, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onCompleted() {
            scheduler.schedule(() -> {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }, sleepTime, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onError(Throwable cause) {
            scheduler.schedule(() -> {
                try {
                    subscriber.onError(cause);
                } catch(Throwable t) {
                    onError(t);
                }
            }, sleepTime, TimeUnit.MILLISECONDS);
        }
    }
}
