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
    public void call(SafeIoSubscriber<? super T> t) throws Exception {
        ReduceSubscriber<T> parent = new ReduceSubscriber<T>(t, reducer);
        source.subscribe(parent).start();
    }

    static final class ReduceSubscriber<T> implements IoSubscriber<T> {
        private static final Object EMPTY = new Object();

        private final IoSubscriber<? super T> actual;

        private final IoReduce<T, T, T> reducer;

        private T value;

        private boolean done;

        @SuppressWarnings("unchecked")
        ReduceSubscriber(IoSubscriber<? super T> actual, IoReduce<T, T, T> reducer) {
            this.actual = actual;
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
                    actual.onNext((T)o);
                    actual.onCompleted();
                } catch (Exception e) {
                    actual.onError(e);
                }
            } else {
                actual.onError(new NoSuchElementException());
            }
        }

        @Override
        public void onError(Throwable cause) {
            if (!done) {
                done = true;
                actual.onError(cause);
            }
        }
    }
}
