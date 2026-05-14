package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;
import cloud.apposs.util.Errno;
import cloud.apposs.util.StandardResult;

/**
 * 当响应式中有错误结果或者异常时的回滚操作
 */
public class OnSubscribeRollback<T> implements OnSubscribe<StandardResult> {
    private final React<T> source;

    final IoFunction<StandardResult, StandardResult> rollback;

    public OnSubscribeRollback(React<T> source, IoFunction<StandardResult, StandardResult> rollback) {
        this.source = source;
        this.rollback = rollback;
    }

    @Override
    public void call(IoSubscriber<? super StandardResult> subscriber) throws Exception {
        RollbackSubscriber<T> parent = new RollbackSubscriber<T>(subscriber, rollback);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    private static final class RollbackSubscriber<T> extends IoSubscriber<T> {
        private final IoSubscriber<? super StandardResult> subscriber;

        final IoFunction<StandardResult, StandardResult> rollback;

        public RollbackSubscriber(IoSubscriber<? super StandardResult> subscriber, IoFunction<StandardResult, StandardResult> rollback) {
            super(subscriber);
            this.subscriber = subscriber;
            this.rollback = rollback;
        }

        @Override
        public void onNext(T value) throws Exception {
            if (value instanceof StandardResult) {
                StandardResult result = (StandardResult) value;
                if (result.isError()) {
                    // 将处理结果为失败时调用回滚操作
                    subscriber.onNext(rollback.call(result));
                } else {
                    subscriber.onNext(result);
                }
            } else {
                // 非StandardResult则进行包装再传递
                subscriber.onNext(StandardResult.success(value));
            }
        }

        @Override
        public void onError(Throwable cause) {
            try {
                // 异常也作为错误结果处理
                StandardResult result = StandardResult.error(Errno.ERROR, cause);
                subscriber.onNext(rollback.call(result));
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }
    }
}
