package cloud.apposs.react;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 响应式数据定时器，底层采用的是异步线程定时任务
 */
public class OnSubscribeTimerPeriodically implements React.OnSubscribe<Long> {
    final long initialDelay;
    final long period;
    final TimeUnit unit;
    final ScheduledExecutorService scheduler;

    public OnSubscribeTimerPeriodically(long initialDelay, long period, TimeUnit unit, ScheduledExecutorService scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    public void call(final SafeIoSubscriber<? super Long> child) {
        scheduler.scheduleAtFixedRate(new Runnable() {
            long counter;
            @Override
            public void run() {
                try {
                    child.onNext(counter++);
                } catch (Throwable t) {
                    child.onError(t);
                }
            }
        }, initialDelay, period, unit);
    }
}
