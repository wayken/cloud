package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

public final class OnSubscribeMap<T, R> implements OnSubscribe<R> {
    private final React<T> source;

    private final IoFunction<? super T, ? extends R> transformer;

    public OnSubscribeMap(React<T> source, IoFunction<? super T, ? extends R> transformer) {
        this.source = source;
        this.transformer = transformer;
    }

    @Override
    public void call(final IoSubscriber<? super R> subscriber) throws Exception {
        MapSubscriber<T, R> parent = new MapSubscriber<T, R>(subscriber, transformer);
        subscriber.add(parent);
        source.subscribe(parent).start();
    }

    private static final class MapSubscriber<T, R> extends IoSubscriber<T> {
        private final IoSubscriber<? super R> subscriber;

        private final IoFunction<? super T, ? extends R> mapper;

        public MapSubscriber(IoSubscriber<? super R> subscriber, IoFunction<? super T, ? extends R> mapper) {
            super(subscriber);
            this.subscriber = subscriber;
            this.mapper = mapper;
        }

        @Override
        public void onNext(T t) throws Exception {
            R result = mapper.call(t);
            subscriber.onNext(result);
        }

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
        }

        @Override
        public void onCompleted() {
            subscriber.onCompleted();
        }
    }
}
