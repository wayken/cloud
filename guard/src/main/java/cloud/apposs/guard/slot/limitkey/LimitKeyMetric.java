package cloud.apposs.guard.slot.limitkey;

import cloud.apposs.guard.GuardConstants;

/**
 * 限制关键字数据托管
 */
public class LimitKeyMetric {
    /**
     * 滑动数组统计调用次数
     */
    private LimitKeyMetricControl control;

    public LimitKeyMetric() {
        this(GuardConstants.DEFAULT_WINDOW_SAMPLE_SIZE, GuardConstants.DEFAULT_WINDOW_INTERVAL_IN_MS);
    }

    public LimitKeyMetric(long intervalInMs) {
        this(GuardConstants.DEFAULT_WINDOW_SAMPLE_SIZE, intervalInMs);
    }

    /**
     * 构造限制关键字流控器
     *
     * @param sampleSize   滑动窗口大小
     * @param intervalInMs 滑动窗口时间间隔
     */
    public LimitKeyMetric(int sampleSize, long intervalInMs) {
        this.control = new LimitKeyMetricControl(sampleSize, intervalInMs);
    }

    public double getLimitKeySum(Object limitKey) {
        return control.getKeySum(limitKey);
    }

    public double getLimitKeyQps(Object limitKey) {
        return control.getKeyQps(limitKey);
    }

    public void addPass(Object limitKey, int token) {
        if (limitKey == null) {
            return;
        }
        control.addKey(limitKey, token);
    }
}
