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
	public void call(final SafeIoSubscriber<? super T> t) throws Exception {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				source.subscribe(t).start();
			}
		});
	}
}
