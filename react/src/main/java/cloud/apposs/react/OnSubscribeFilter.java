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
	public void call(IoSubscriber<? super T> subscriber) throws Exception {
		FilterSubscriber<T> parent = new FilterSubscriber<T>(subscriber, predicate);
        subscriber.add(parent);
		source.subscribe(parent).start();
	}
	
	private static final class FilterSubscriber<T> extends SafeIoSubscriber<T> {
        private final IoFunction<? super T, Boolean> predicate;

        public FilterSubscriber(IoSubscriber<? super T> subscriber, IoFunction<? super T, Boolean> predicate) {
            super(subscriber);
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) throws Exception {
            boolean result = predicate.call(t);
            if (result) {
                subscriber.onNext(t);
            }
        }
    }
}
