package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * React执行异常时重试机制，主要应用于是网络请求异常时的重试逻辑，
 * 底层的实现原理是通过Handler回调，判断是否有返回新的{@link React}继续下一轮数据流调用，
 * 而返回的{@link React}内部则由业务自己定义是否异步休眠一段时间(参考OkHttp.HttpRetry)再返回继续下一轮执行
 */
public class OnSubscribeRetry<T> implements OnSubscribe<T> {
    private final React<T> source;

    private final IoFunction<Throwable, ? extends React<T>> handler;

    public OnSubscribeRetry(React<T> source, IoFunction<Throwable, ? extends React<T>> handler) {
        this.source = source;
        this.handler = handler;
    }

    @Override
    public void call(SafeIoSubscriber<? super T> t) throws Exception {
        RetrySubscriber<T> parent = new RetrySubscriber<T>(t, handler);
        source.subscribe(parent).start();
    }

    static class RetrySubscriber<T> implements IoSubscriber<T> {
        private final IoSubscriber<? super T> actual;

        private final IoFunction<Throwable, ? extends React<T>> handler;

        /**
         * 重试标志保护，OnSubscribeRetry对数据流进行封装重试是仅针对当前封装的对象，不能嵌套产生异常时重试
         */
        private boolean success = false;

        RetrySubscriber(IoSubscriber<? super T> actual, IoFunction<Throwable, ? extends React<T>> handler) {
            this.actual = actual;
            this.handler = handler;
        }

        @Override
        public void onNext(T value) throws Exception {
            // 进入此逻辑则代表当前OnSubscribeRetry封装的数据流是正常的，标记为成功，
            // 其他任务业务抛出的异常均不做重试处理，避免当前数据流本身正常又不断重试
            success = true;
            actual.onNext(value);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }

        @Override
        public void onError(Throwable cause) {
            if (success) {
                // 当前数据流响应是正常的，不需要重试，直接调用下一个OnSubscribe进行错误处理即可
                actual.onError(cause);
                return;
            }
            try {
                // 判断业务是否生成新的React响应流
                React<T> source = handler.call(cause);
                if (source == null) {
                    // 返回为空，代表实现类在重试方法判断超过一定次数返回后null，不再重试了
                    actual.onError(cause);
                } else {
                    // 出错继续触发数据响应重试
                    source.subscribe(this).start();
                }
            } catch (Exception ignore) {
            }
        }
    }
}
