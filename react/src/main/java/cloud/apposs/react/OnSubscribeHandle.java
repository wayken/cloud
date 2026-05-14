package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;
import cloud.apposs.util.StandardResult;

/**
 * 对数据进行加工处理，返回{@link StandardResult}，主要应用于业务微服务开发
 */
public class OnSubscribeHandle<T> implements OnSubscribe<StandardResult> {
    private final React<T> source;

    private final IoFunction<? super T, StandardResult> predicate;

    public OnSubscribeHandle(React<T> source, IoFunction<? super T, StandardResult> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public void call(IoSubscriber<? super StandardResult> subscriber) throws Exception {
        StandardResultSubscriber<T> parent = new StandardResultSubscriber<T>(subscriber, predicate);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    public static final class StandardResultException extends Exception {
        private static final long serialVersionUID = -6099542992767550343L;

        private final StandardResult result;

        public StandardResultException(StandardResult result) {
            super(String.format("Unexpected StandardResult '%d: %s'",
                    result.getErrno().value(), result.getErrno().description()));
            this.result = result;
        }

        public StandardResult getResult() {
            return result;
        }
    }

    private static final class StandardResultSubscriber<T> extends IoSubscriber<T> {
        private final IoSubscriber<? super StandardResult> subscriber;

        private final IoFunction<? super T, StandardResult> predicate;

        public StandardResultSubscriber(IoSubscriber<? super StandardResult> subscriber,
                                        IoFunction<? super T, StandardResult> predicate) {
            this.subscriber = subscriber;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) throws Exception {
            StandardResult result;
            if (t instanceof ISkip) {
                result = ((ISkip) t).result();
            } else {
                result = predicate.call(t);
            }
            if (result.isSuccess()) {
                subscriber.onNext(result);
            } else {
                subscriber.onError(new StandardResultException(result));
            }
        }

        @Override
        public void onCompleted() {
            subscriber.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
        }
    }

    public static interface ISkip {
        StandardResult result();
    }
}
