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
    public void call(IoSubscriber<? super List<T>> subscriber) throws Exception {
        MergeListSubscriber<T> parent = new MergeListSubscriber<T>(subscriber, sequences.length, skipError);
        subscriber.add(parent);
        for (int i = 0; i < sequences.length; i++) {
            React<? extends T> react = sequences[i];
            if (subscriber.isUnsubscribed()) {
                return;
            }
            react.subscribe(parent).start();
        }
    }

    private static final class MergeListSubscriber<T> extends IoSubscriber<T> {
        private final IoSubscriber<? super List<T>> subscriber;

        private final int total;

        private final AtomicInteger index;

        private final List<T> valueList;

        private final boolean skipError;

        public MergeListSubscriber(IoSubscriber<? super List<T>> subscriber, int total, boolean skipError) {
            super(subscriber);
            this.subscriber = subscriber;
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
                    subscriber.onNext(valueList);
                    subscriber.onCompleted();
                }
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        }

        @Override
        public void onCompleted() {
            if (index.get() >= total) {
                subscriber.onCompleted();
            }
        }

        @Override
        public void onError(Throwable t) {
            try {
                if (!skipError) {
                    subscriber.onError(t);
                } else if (index.incrementAndGet() >= total) {
                    subscriber.onNext(valueList);
                    subscriber.onCompleted();
                }
            } catch (Throwable e) {
                subscriber.onError(t);
            }
        }
    }
}
