package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 合并多个请求结果，
 * 一般用于同时请求多个网络连接场景，
 * 无论成功或者失败当所有请求都结束后最后才会合并所有结果并只调用一次{@link IoSubscriber#onNext(Object)}方法
 */
public class OperateorMergeList<T> implements OnSubscribe<List<T>> {
    private final React<? extends T>[] sequences;

    private final boolean skipError;

    public OperateorMergeList(React<? extends T>[] sequences, boolean skipError) {
        if (sequences == null || sequences.length <= 0) {
            throw new IllegalArgumentException("sequences");
        }
        this.sequences = sequences;
        this.skipError = skipError;
    }

    @SuppressWarnings("unchecked")
    public OperateorMergeList(List<React<T>> sequences, boolean skipError) {
        if (sequences == null || sequences.size() <= 0) {
            throw new IllegalArgumentException("sequences");
        }
        this.sequences = new React[sequences.size()];
        sequences.toArray(this.sequences);
        this.skipError = skipError;
    }

    @Override
    public void call(SafeIoSubscriber<? super List<T>> t) throws Exception {
        MergeListSubscriber<T> subscriber = new MergeListSubscriber<T>(t, sequences.length, skipError);
        for (int i = 0; i < sequences.length; i++) {
            React<? extends T> react = sequences[i];
            react.subscribe(subscriber).start();
        }
    }

    static final class MergeListSubscriber<T> implements IoSubscriber<T> {
        private final IoSubscriber<? super List<T>> actual;

        private final int total;

        private final AtomicInteger index;

        private final List<T> valueList;

        private final boolean skipError;

        public MergeListSubscriber(SafeIoSubscriber<? super List<T>> t, int total, boolean skipError) {
            this.actual = t;
            this.total = total;
            this.index = new AtomicInteger(0);
            this.valueList = new CopyOnWriteArrayList<T>();
            this.skipError = skipError;
        }

        @Override
        public void onNext(T value) throws Exception {
            try {
                valueList.add(value);
                // 注意此处需要保持原子性，
                // 即直接index.incrementAndGet() >= total而非index.incrementAndGet()后再通过index.get() >= total来判断
                // 因为后者会导致多个异步请求结束后因为先触发index.incrementAndGet()会递增，
                // 如果刚好多个请求都在同一时间递增了，那么在下一步index.get() >= total发现都满足条件了，会导致多次onNext触发
                if (index.incrementAndGet() >= total) {
                    actual.onNext(valueList);
                    actual.onCompleted();
                }
            } catch (Throwable t) {
                actual.onError(t);
            }
        }

        @Override
        public void onCompleted() {
            if (index.get() >= total) {
                actual.onCompleted();
            }
        }

        @Override
        public void onError(Throwable t) {
            try {
                if (!skipError) {
                    actual.onError(t);
                } else if (index.incrementAndGet() >= total) {
                    actual.onNext(valueList);
                    actual.onCompleted();
                }
            } catch (Throwable e) {
                actual.onError(t);
            }
        }
    }
}
