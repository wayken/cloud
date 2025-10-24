package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;
import cloud.apposs.react.actor.Actor;
import cloud.apposs.react.actor.ActorLock;
import cloud.apposs.react.actor.ActorTask;

/**
 * 锁的响应式，
 * 底层会在数据执行结束之后自动调用{@link ActorLock#unlock()}来自动释放锁资源
 */
public class OnSubscribeLock<T> implements OnSubscribe<T> {
    private final ActorLock key;

    private final Actor actor;

    private final OnSubscribe<T> subscribe;

    public OnSubscribeLock(ActorLock key, Actor actor, OnSubscribe<T> subscribe) {
        this.key = key;
        this.actor = actor;
        this.subscribe = subscribe;
    }

    @Override
    public void call(final SafeIoSubscriber<? super T> subscriber) throws Exception {
        actor.lock(key, new ActorTask() {
            @Override
            public ActorLock getLockKey() {
                return key;
            }

            @Override
            public void run() {
                LockSubscriber<T> lockSubscriber = null;
                try {
                    lockSubscriber = new LockSubscriber<>(subscriber, key);
                    subscribe.call(lockSubscriber);
                } catch (Throwable t) {
                    lockSubscriber.onError(t);
                }
            }
        });
    }

    /**
     * 对Subscriber进行再包装，保证当所有数据流处理结束时自动释放锁
     */
    static final class LockSubscriber<T> extends SafeIoSubscriber<T> {
        private final ActorLock key;

        public LockSubscriber(IoSubscriber<? super T> actual, ActorLock key) {
            super(actual);
            this.key = key;
        }

        @Override
        public void onNext(T t) throws Exception {
            try {
                actual.onNext(t);
            } finally {
                key.unlock();
            }
        }

        @Override
        public void onError(Throwable e) {
            try {
                actual.onError(e);
            } finally {
                key.unlock();
            }
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }
    }
}
