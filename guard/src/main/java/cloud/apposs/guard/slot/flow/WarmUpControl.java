package cloud.apposs.guard.slot.flow;

import cloud.apposs.guard.node.Node;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 冷启动策略控制器
 */
public class WarmUpControl implements TrafficShapingControl {
    private double count;
    private int coldFactor;
    private int warningToken = 0;
    private int maxToken;
    private double slope;

    private AtomicLong storedTokens = new AtomicLong(0);
    private AtomicLong lastFilledTime = new AtomicLong(0);

    public WarmUpControl(double count, int warmUpPeriodInMic) {
        this(count, warmUpPeriodInMic, 3);
    }

    public WarmUpControl(double count, int warmUpPeriodInSec, int coldFactor) {
        if (coldFactor <= 1) {
            throw new IllegalArgumentException("Cold factor should be larger than 1");
        }
        this.count = count;
        this.coldFactor = coldFactor;
        warningToken = (int)(warmUpPeriodInSec * count) / (coldFactor - 1);
        maxToken = warningToken + (int)(2 * warmUpPeriodInSec * count / (1.0 + coldFactor));
        slope = (coldFactor - 1.0) / count / (maxToken - warningToken);
    }

    @Override
    public boolean canPass(Node node, int token) {
        long passQps = node.passQps();
        long previousQps = node.previousPassQps();
        syncToken(previousQps);
        // 开始计算它的斜率
        // 如果进入了警戒线，开始调整他的qps
        long restToken = storedTokens.get();
        if (restToken >= warningToken) {
            long aboveToken = restToken - warningToken;
            // 消耗的速度要比warning快，但是要比慢
            // current interval = restToken*slope+1/count
            double warningQps = Math.nextUp(1.0 / (aboveToken * slope + 1.0 / count));
            if (passQps + token <= warningQps) {
                return true;
            }
        } else {
            if (passQps + token <= count) {
                return true;
            }
        }
        return false;
    }

    private void syncToken(long passQps) {
        long currentTime = System.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        long oldLastFillTime = lastFilledTime.get();
        if (currentTime <= oldLastFillTime) {
            return;
        }
        long oldValue = storedTokens.get();
        long newValue = coolDownTokens(currentTime, passQps);

        if (storedTokens.compareAndSet(oldValue, newValue)) {
            long currentValue = storedTokens.addAndGet(0 - passQps);
            if (currentValue < 0) {
                storedTokens.set(0L);
            }
            lastFilledTime.set(currentTime);
        }
    }

    private long coolDownTokens(long currentTime, long passQps) {
        long oldValue = storedTokens.get();
        long newValue = oldValue;
        // 添加令牌的判断前提条件:
        // 当令牌的消耗程度远远低于警戒线的时候
        if (oldValue < warningToken) {
            newValue = (long)(oldValue + (currentTime - lastFilledTime.get()) * count / 1000);
        } else if (oldValue > warningToken) {
            if (passQps < (int)count / coldFactor) {
                newValue = (long)(oldValue + (currentTime - lastFilledTime.get()) * count / 1000);
            }
        }
        return Math.min(newValue, maxToken);
    }
}
