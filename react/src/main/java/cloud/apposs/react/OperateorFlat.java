package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.Iterator;

/**
 * 需要进行前置条件判断的多个React串行执行，
 * 下一个串行执行的React流会根据上一个执行的任务来判断是否执行，如果上一个流执行不符合结果则解决False失败处理，
 * 主要业务有：
 * 1. 订单下单，多个子系统串行执行，依赖上一任务的执行结果来决定是否执行下一任务
 * 2. 多子任务定时执行，下一个任务依赖上一个任务的执行结果
 */
public class OperateorFlat<T> implements OnSubscribe<Boolean> {
    private final Iterable<? extends React<? extends T>> sequences;

    /**
     * 具体执行是否继续执行根据此方法进行判断
     */
    private final IoFunction<? super T, Boolean> predicate;

    public OperateorFlat(Iterable<? extends React<? extends T>> sequences, IoFunction<? super T, Boolean> predicate) {
        if (sequences == null || !sequences.iterator().hasNext()) {
            throw new IllegalArgumentException("sequences");
        }
        this.sequences = sequences;
        this.predicate = predicate;
    }

    @Override
    public void call(IoSubscriber<? super Boolean> subscriber) throws Exception {
        Iterator<? extends React<? extends T>> iterator = sequences.iterator();
        FlatSubscriber<T> parent = new FlatSubscriber<T>(subscriber, iterator, predicate);
        subscriber.add(parent);
        React<? extends T> source = iterator.next();
        source.subscribe(parent).start();
    }

    private static class FlatSubscriber<T> extends IoSubscriber<T> {
        final IoSubscriber<? super Boolean> subscriber;

        final Iterator<? extends React<? extends T>> sequences;

        final IoFunction<? super T, Boolean> predicate;

        public FlatSubscriber(IoSubscriber<? super Boolean> subscriber, Iterator<? extends React<? extends T>> sequences, IoFunction<? super T, Boolean> predicate) {
            super(subscriber);
            this.subscriber = subscriber;
            this.sequences = sequences;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T value) throws Exception {
            boolean success = predicate.call(value);
            if (success) {
                // 该次请求验证成功，获取下一个请求继续执行
                if (sequences.hasNext()) {
                    React<? extends T> source = sequences.next();
                    source.subscribe(this).start();
                } else {
                    try {
                        // 已经没有下一个请求了，直接调用业务实现接口返回
                        subscriber.onNext(true);
                    } finally {
                        subscriber.onCompleted();
                    }
                }
            } else {
                try {
                    // 该次请求验证失败，直接调用业务实现接口返回
                    subscriber.onNext(false);
                } finally {
                    subscriber.onCompleted();
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            try {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        }
    }
}
