package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

public final class OnSubscribeRequest<T, R> implements OnSubscribe<R> {
	private final React<T> source;
	
	private final IoFunction<? super T, ? extends React<? extends R>> transformer;
	
	public OnSubscribeRequest(React<T> source, IoFunction<? super T, ? extends React<? extends R>> func) {
        this.source = source;
        this.transformer = func;
    }
	
	@Override
	public void call(IoSubscriber<? super R> subscriber) throws Exception {
		RequestSubscriber<T, R> parent = new RequestSubscriber<T, R>(subscriber, transformer);
        subscriber.add(parent);
        source.subscribe(parent).start();
	}
	
	private static final class RequestSubscriber<T, R> extends IoSubscriber<T> {
        private final IoSubscriber<? super R> subscriber;

        private final IoFunction<? super T, ? extends React<? extends R>> mapper;

        public RequestSubscriber(IoSubscriber<? super R> subscriber,
        		IoFunction<? super T, ? extends React<? extends R>> transformer) {
            super(subscriber);
            this.subscriber = subscriber;
            this.mapper = transformer;
        }

        @Override
        public void onNext(T t) throws Exception {
            React<? extends R> result = mapper.call(t);
            if (subscriber.isUnsubscribed()) {
                return;
            }
        	result.subscribe(subscriber).start();
        }
    }
}
