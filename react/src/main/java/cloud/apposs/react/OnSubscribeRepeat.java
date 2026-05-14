package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * 重复执行流，将整个上游序列重复订阅执行 count 次，
 * 注意在React.XXX.repeat，之前的所有流方法都会重复执行N次
 */
public class OnSubscribeRepeat<T> implements OnSubscribe<T> {
    private final React<T> source;

    /**
     * 重复执行次数
     */
    private final int count;

    public OnSubscribeRepeat(React<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public void call(IoSubscriber<? super T> subscriber) throws Exception {
        for (int i = 0; i < count; i++) {
            if (subscriber.isUnsubscribed()) {
                return;
            }
            // 每次重新订阅上游，将事件转发给下游
            RepeatSubscriber<T> parent = new RepeatSubscriber<>(subscriber);
            source.subscribe(parent).start();
            // 如果下游在本轮中取消了订阅，立即停止
            if (subscriber.isUnsubscribed()) {
                return;
            }
        }
        // 所有轮次完成后才触发 onCompleted
        if (!subscriber.isUnsubscribed()) {
            subscriber.onCompleted();
        }
    }

    private static class RepeatSubscriber<T> extends SafeIoSubscriber<T> {
        public RepeatSubscriber(IoSubscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override
        public void onCompleted() {
            // 每轮结束不向下游发送 onCompleted，由外层循环统一控制
        }

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
        }
    }
}
