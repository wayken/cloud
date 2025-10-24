package cloud.apposs.guard.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 滑动窗口控制器
 */
public abstract class SlideWindowControl<T extends DataBucket> {
    /**
     * 时间窗口内的数据桶，根据时间和样本数计算出来
     */
    private final long windowLengthInMs;

    /**
     * 统计窗口时间间距，单位毫秒
     * 默认为 1 秒，即每秒QPS的统计超过阈值则触发限流，在这个时间窗口内超过阈值都会限流
     * 如果要改成分钟级别则设置为 60 * 1000
     * 如果要改成小时级别则设置为 60 * 60 * 1000
     * 如果要改成天级别则设置为 24 * 60 * 60 * 1000
     */
    private final long intervalInMs;

    /**
     * 滑动窗口内的数据桶数量，在这个时间窗口内会统计有多少个数据量
     */
    private final AtomicReferenceArray<WindowWrap<T>> samples;

    private final Lock updateLock;

    /**
     * 构造滑动窗口控制器
     *
     * @param sampleCount  滑动窗口内的数据桶数量
     * @param intervalInMs 统计窗口时间间距，单位毫秒，即多少时间内统计一次，默认是 1 秒
     */
    public SlideWindowControl(int sampleCount, long intervalInMs) {
        this.intervalInMs = intervalInMs;
        this.windowLengthInMs = intervalInMs / sampleCount;
        this.samples = new AtomicReferenceArray<WindowWrap<T>>(sampleCount);
        this.updateLock = new ReentrantLock();
    }

    /**
     * 获取当前时间窗口
     */
    public WindowWrap<T> currentWindow() {
        long now = System.currentTimeMillis();
        int idx = getIdx(now);
        long curWindowStart = getCurWindowStart(now);
        WindowWrap<T> window = null;
        while (window == null) {
            window = samples.get(idx);
            if (window == null) {
                // set new window
                WindowWrap<T> newWindow = new WindowWrap<T>(curWindowStart, newEmptyBucket());
                if (!samples.compareAndSet(idx, null, newWindow)) {
                    Thread.yield();
                }
            } else if (window.getWindowStart() != curWindowStart) {
                if (updateLock.tryLock()) {
                    try {
                        // 重置窗口以开始下一个数据收集周期
                        window.resetTo(curWindowStart);
                        window.value().reset();
                    } finally {
                        updateLock.unlock();
                    }
                } else {
                    Thread.yield();
                }
            }
        }
        return window;
    }

    public WindowWrap<T> getPreviousWindow() {
        return getPreviousWindow(System.currentTimeMillis());
    }

    public WindowWrap<T> getPreviousWindow(long time) {
        long timeId = (time - windowLengthInMs) / windowLengthInMs;
        int idx = (int)(timeId % samples.length());
        time = time - windowLengthInMs;
        WindowWrap<T> wrap = samples.get(idx);
        if (wrap == null || isWindowDeprecated(wrap)) {
            return null;
        }
        if (wrap.getWindowStart() + windowLengthInMs < (time)) {
            return null;
        }
        return wrap;
    }

    /**
     * 初始化一个新的数据桶
     */
    protected abstract T newEmptyBucket();

    /**
     * 获取所有有效窗口
     */
    public List<T> windows() {
        List<T> windows = new ArrayList<T>();
        for (int i = 0; i < samples.length(); i++) {
            WindowWrap<T> window = samples.get(i);
            if (window != null && !isWindowDeprecated(window)) {
                windows.add(window.value());
            }
        }
        return windows;
    }

    /**
     * 窗口是否过期(窗口开始时间超过了设定的间隔时间则为过期)
     */
    private boolean isWindowDeprecated(WindowWrap<T> window) {
        long now = System.currentTimeMillis();
        return (now - window.getWindowStart()) > intervalInMs;
    }

    /**
     * 获取时间窗口开始时间
     */
    private long getCurWindowStart(long now) {
        return now - (now % windowLengthInMs);
    }

    /**
     * 根据时间获取数组下标
     */
    private int getIdx(long time) {
        long timeId = time / windowLengthInMs;
        return (int) (timeId % samples.length());
    }

    public long getIntervalInSec() {
        return intervalInMs / 1000;
    }
}
