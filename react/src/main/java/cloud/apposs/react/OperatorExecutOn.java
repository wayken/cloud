package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.concurrent.Executor;

public class OperatorExecutOn<T> implements OnSubscribe<T> {
	private final OnSubscribe<T> parent;
	
	private final Executor executor;
	
	public OperatorExecutOn(OnSubscribe<T> parent, Executor executor) {
		this.parent = parent;
		this.executor = executor;
	}
	
	@Override
	public void call(final IoSubscriber<? super T> subscriber) throws Exception {
		IoSubscriber<? super T> is = new ExecutorOnSubscriber<T>(subscriber, executor);
		subscriber.add(is);
		parent.call(new SafeIoSubscriber<T>(is));
	}
	
	private static final class ExecutorOnSubscriber<T> extends SafeIoSubscriber<T> {
		private final Executor executor;
		
		public ExecutorOnSubscriber(IoSubscriber<? super T> subscriber, Executor executor) {
			super(subscriber);
			this.executor = executor;
		}
		
		@Override
		public void onNext(final T value) throws Exception {
			executor.execute(new Runnable() {
    			@Override
    			public void run() {
    				try {
						subscriber.onNext(value);
					} catch (Exception e) {
						subscriber.onError(e);
					}
    			}
    		});
		}

		@Override
		public void onCompleted() {
			executor.execute(() -> {
				subscriber.onCompleted();
			});
		}

		@Override
		public void onError(final Throwable t) {
			executor.execute(() -> {
				subscriber.onError(t);
			});
		}
	}
}
