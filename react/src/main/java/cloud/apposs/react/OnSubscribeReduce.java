package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.NoSuchElementException;

public final class OnSubscribeReduce<T> implements OnSubscribe<T> {
    private final React<T> source;

    private final IoReduce<T, T, T> reducer;

    public OnSubscribeReduce(React<T> source, IoReduce<T, T, T> reducer) {
        this.source = source;
        this.reducer = reducer;
    }

    @Override
    public void call(IoSubscriber<? super T> subscriber) throws Exception {
        ReduceSubscriber<T> parent = new ReduceSubscriber<T>(subscriber, reducer);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    private static final class ReduceSubscriber<T> extends SafeIoSubscriber<T> {
        private static final Object EMPTY = new Object();

        private final IoReduce<T, T, T> reducer;

        private T value;

        private boolean done;

        @SuppressWarnings("unchecked")
        ReduceSubscriber(IoSubscriber<? super T> subscriber, IoReduce<T, T, T> reducer) {
            super(subscriber);
            this.reducer = reducer;
            this.value = (T) EMPTY;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onNext(T value) throws Exception {
            if (done) {
                return;
            }
            Object o = this.value;
            if (o == EMPTY) {
                this.value = value;
            } else {
                this.value = reducer.call((T)o, value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCompleted() {
            if (done) {
                return;
            }
            done = true;
            Object o = this.value;
            if (o != EMPTY) {
                try {
                    subscriber.onNext((T)o);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            } else {
                subscriber.onError(new NoSuchElementException());
            }
        }

        @Override
        public void onError(Throwable cause) {
            if (!done) {
                done = true;
                subscriber.onError(cause);
            }
        }
    }
}
