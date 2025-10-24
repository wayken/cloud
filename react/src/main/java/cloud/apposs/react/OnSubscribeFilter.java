package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * 数据过滤操作，只处理过滤通过的数据
 */
public final class OnSubscribeFilter<T> implements OnSubscribe<T> {
	private final React<T> source;

	private final IoFunction<? super T, Boolean> predicate;

    public OnSubscribeFilter(React<T> source, IoFunction<? super T, Boolean> predicate) {
        this.source = source;
        this.predicate = predicate;
    }
	
	@Override
	public void call(SafeIoSubscriber<? super T> t) throws Exception {
		FilterSubscriber<T> parent = new FilterSubscriber<T>(t, predicate);
		source.subscribe(parent).start();
	}
	
	static final class FilterSubscriber<T> implements IoSubscriber<T> {
        final IoSubscriber<? super T> actual;

        final IoFunction<? super T, Boolean> predicate;

        public FilterSubscriber(IoSubscriber<? super T> actual, IoFunction<? super T, Boolean> predicate) {
            this.actual = actual;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) throws Exception {
            boolean result = predicate.call(t);
            if (result) {
                actual.onNext(t);
            }
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }
    }
}
