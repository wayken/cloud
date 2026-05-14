package cloud.apposs.react;

/**
 * 异步请求数据后的订阅消费者，负责将请求到的数据进行逻辑处理并发送，由业务方实现业务处理逻辑
 */
public abstract class IoSubscriber<T> implements IoObserver<T>, IoSubscription {
	// 是否已取消订阅，默认未取消订阅
	protected volatile boolean unsubscribed = false;

	// 上层订阅实例封装，如果上层订阅实例不为空，则所有取消订阅的操作都由上层订阅实例进行处理
	protected final IoSubscriber<?> subscriber;

	private final IoSubscriptionList subscriptions;

	protected IoSubscriber() {
		this(null, true);
	}

	protected IoSubscriber(IoSubscriber<?> subscriber) {
		this(subscriber, true);
	}

	protected IoSubscriber(IoSubscriber<?> subscriber, boolean shareSubscriptions) {
		this.subscriber = subscriber;
		this.subscriptions = shareSubscriptions && subscriber != null ? subscriber.subscriptions : new IoSubscriptionList();
	}

	public final void add(IoSubscription subscription) {
		subscriptions.add(subscription);
	}

	@Override
	public void onNext(T value) throws Exception {
	}

	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable cause) {
	}

	@Override
	public boolean isUnsubscribed() {
		return subscriptions.isUnsubscribed();
	}

	@Override
	public void unsubscribe() {
		subscriptions.unsubscribe();
	}
}
