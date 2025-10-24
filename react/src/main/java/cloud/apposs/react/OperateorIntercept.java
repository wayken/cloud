package cloud.apposs.react;

import java.util.Iterator;

/**
 * 基于异步的拦截器设计，通过多个sequences拦截器逐个判断返回结果，
 * 当返回结果不符合预期时则直接调用actual.onError(cause)抛出异常由业务逻辑实现
 */
public class OperateorIntercept<T, R> implements React.OnSubscribe<R> {
    /**
     * 异步拦截器集合
     */
    private final Iterable<? extends React<? extends T>> sequences;

    /**
     * 过滤函数，拦截器对应的调用结果(可以为任何数据类型)会通过此过滤函数判断，
     * 如果判断拦截器拦截结果不符合预期则返回对应的True/False
     */
    private final IoFunction<? super T, IResult> predicate;

    /**
     * 拦截器全部执行通过后的React数据生成，即最终的业务调用
     */
    private final IoEmitter<React<? extends R>> emitter;

    public OperateorIntercept(Iterable<? extends React<? extends T>> sequences,
              IoFunction<? super T, IResult> predicate, IoEmitter<React<? extends R>> emitter) {
        this.sequences = sequences;
        this.predicate = predicate;
        this.emitter = emitter;
    }

    @Override
    public void call(SafeIoSubscriber<? super R> t) throws Exception {
        Iterator<? extends React<? extends T>> iterator = sequences.iterator();
        // 拦截器为空则直接调用业务逻辑
        if (!iterator.hasNext()) {
            React<? extends R> source = emitter.call();
            source.subscribe(t).start();
            return;
        }
        InterceptSubscriber<T, R> subscriber = new InterceptSubscriber<T, R>(t, iterator, predicate, emitter);
        React<? extends T> source = iterator.next();
        source.subscribe(subscriber).start();
    }

    private static class InterceptSubscriber<T, R> implements IoSubscriber<T> {
        private final IoSubscriber<? super R> actual;

        private final Iterator<? extends React<? extends T>> sequences;

        private final IoFunction<? super T, IResult> predicate;

        private final IoEmitter<React<? extends R>> emitter;

        private InterceptSubscriber(IoSubscriber<? super R> actual, Iterator<? extends React<? extends T>> sequences,
                    IoFunction<? super T, IResult> predicate, IoEmitter<React<? extends R>> emitter) {
            this.actual = actual;
            this.sequences = sequences;
            this.predicate = predicate;
            this.emitter = emitter;
        }

        @Override
        public void onNext(T value) throws Exception {
            // 若判断不符合拦截需求，返回false，不执行接下来的拦截器列表和业务逻辑
            IResult result = predicate.call(value);
            if (result.success) {
                if (sequences.hasNext()) {
                    // 拦截器链式过滤
                    React<? extends T> source = sequences.next();
                    source.subscribe(this).start();
                } else {
                    // 全部拦截器都通过，调用业务逻辑
                    try {
                        React<? extends R> source = emitter.call();
                        source.subscribe(actual).start();
                    } catch (Exception e) {
                        this.onError(e);
                    }
                }
            } else {
                if (!result.skip) {
                    Throwable cause = result.cause == null ? new IllegalAccessException() : result.cause;
                    this.onError(cause);
                }
            }
        }

        @Override
        public void onError(Throwable cause) {
            actual.onError(cause);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }
    }

    public static class IResult {
        // 拦截器执行结果
        private boolean success;

        // 当触发拦截器过滤时，如果结果为false，是否不执行异常抛出，直接跳过
        private boolean skip;

        // 自定义拦截器失败时抛出的异常
        private Throwable cause;

        public static final IResult SUCCESS = new IResult(true, false, null);
        public static final IResult SKIP = new IResult(false, true, null);
        public static final IResult FAILURE = new IResult(false, false, null);

        public IResult(boolean success, boolean skip, Throwable cause) {
            this.success = success;
            this.skip = skip;
            this.cause = cause;
        }

        public static IResult build(boolean success, boolean skip) {
            return build(success, skip, null);
        }

        public static IResult build(boolean success, boolean skip, Throwable cause) {
            return new IResult(success, skip, cause);
        }

        public boolean success() {
            return success;
        }

        public boolean skip() {
            return skip;
        }

        public Throwable cause() {
            return cause;
        }
    }
}
