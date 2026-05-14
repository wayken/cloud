package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.concurrent.Executor;

public class OperatorSubscribeOn<T> implements OnSubscribe<T> {
	private final React<T> source;
	
	private final Executor executor;
	
	public OperatorSubscribeOn(React<T> source, Executor executor) {
		this.source = source;
		this.executor = executor;
	}
	
	@Override
	public void call(IoSubscriber<? super T> subscriber) throws Exception {
		executor.execute(() -> {
			if (subscriber.isUnsubscribed()) {
				return;
			}
			source.subscribe(subscriber).start();
		});
	}
}
