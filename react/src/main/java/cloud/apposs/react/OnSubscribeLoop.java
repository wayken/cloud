package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * React循环执行机制，主要应用于需要根据上一轮结果决定是否继续下一轮执行的场景，
 * 底层实现原理是通过Handler回调，判断是否有返回新的{@link React}继续下一轮数据流调用，
 * 返回null则终止循环，返回新的{@link React}则继续下一轮循环
 */
public class OnSubscribeLoop<T> implements OnSubscribe<T> {
    private final React<T> source;

    private final IoFunction<T, ? extends React<T>> handler;

    public OnSubscribeLoop(React<T> source, IoFunction<T, ? extends React<T>> handler) {
        this.source = source;
        this.handler = handler;
    }

    @Override
    public void call(IoSubscriber<? super T> subscriber) throws Exception {
        LoopSubscriber<T> parent = new LoopSubscriber<T>(subscriber, handler);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    private static class LoopSubscriber<T> extends SafeIoSubscriber<T> {
        private final IoFunction<T, ? extends React<T>> handler;

        LoopSubscriber(IoSubscriber<? super T> subscriber, IoFunction<T, ? extends React<T>> handler) {
            super(subscriber);
            this.handler = handler;
        }

        @Override
        public void onNext(T value) throws Exception {
            try {
                // 调用handler判断是否需要继续下一轮循环
                React<T> next = handler.call(value);
                if (next == null) {
                    // 返回null，终止循环，将当前值传递给下游并完成
                    subscriber.onNext(value);
                    subscriber.onCompleted();
                } else {
                    // 返回新的React，继续下一轮循环
                    next.subscribe(this).start();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }
    }
}
