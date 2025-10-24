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
	public void call(final SafeIoSubscriber<? super R> t) throws Exception {
		RequestSubscriber<T, R> parent = new RequestSubscriber<T, R>(t, transformer);
        source.subscribe(parent).start();
	}
	
	static final class RequestSubscriber<T, R> implements IoSubscriber<T> {
        private final IoSubscriber<? super R> actual;

        private final IoFunction<? super T, ? extends React<? extends R>> mapper;

        public RequestSubscriber(IoSubscriber<? super R> actual,
        		IoFunction<? super T, ? extends React<? extends R>> transformer) {
            this.actual = actual;
            this.mapper = transformer;
        }

        @Override
        public void onNext(T t) throws Exception {
            React<? extends R> result = mapper.call(t);
        	result.subscribe(actual).start();
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
            actual.onCompleted();
        }

        @Override
        public void onCompleted() {
        }
    }
}
