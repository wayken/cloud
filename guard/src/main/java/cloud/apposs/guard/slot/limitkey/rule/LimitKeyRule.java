package cloud.apposs.guard.slot.limitkey.rule;

import cloud.apposs.guard.slot.Rule;
import cloud.apposs.guard.slot.limitkey.LimitKeyMetric;

/**
 * 限制关键字流控规则
 * 支持资源 QPS 限流搭载关键字 QPS 热点限流
 */
public class LimitKeyRule extends Rule {
    /**
     * 限制关键字 QPS 阈值
     */
    private double threshold;

    /**
     * 是否走QPS限流，如果为false则只走总请求次数限流，两者的区别如下：
     * 1. QPS限流：并发请求数，无论如何配置滑动窗口大小都不受影响，比如1秒内最多允许100次请求，超过则限流
     * 2. 总请求次数限流：时间窗口总请求数，比如1分钟内最多允许1000次请求，超过则限流，滑动窗口大小决定了统计的时间范围，详见{@link LimitKeyMetric}
     */
    private boolean usingQpsLimit = true;

    public LimitKeyRule() {
    }

    public LimitKeyRule(boolean usingQpsLimit) {
        this.usingQpsLimit = usingQpsLimit;
    }

    public boolean passCheck(LimitKeyMetric metric, Object limitKey, int token) {
        if (usingQpsLimit) {
            return passRateLimitCheck(metric, limitKey, token);
        } else {
            return passTotalLimitCheck(metric, limitKey, token);
        }
    }

    private boolean passRateLimitCheck(LimitKeyMetric metric, Object limitKey, int token) {
        double curQps = metric.getLimitKeyQps(limitKey);
        return curQps + token <= threshold;
    }

    private boolean passTotalLimitCheck(LimitKeyMetric metric, Object limitKey, int token) {
        double curQps = metric.getLimitKeySum(limitKey);
        return curQps + token <= threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
