package cloud.apposs.react;

/**
 * {@link IoSubscriber}包装类，保存当次响应会话内部状态，只有系统内部内调用避免暴露太多属性给业务方
 */
public class SafeIoSubscriber<T> extends IoSubscriber<T> {
	protected final IoSubscriber<? super T> subscriber;
	
	public SafeIoSubscriber(IoSubscriber<? super T> subscriber) {
        super(subscriber);
		this.subscriber = subscriber;
    }

	@Override
	public void onCompleted() {
		subscriber.onCompleted();
	}

	@Override
	public void onError(Throwable e) {
		subscriber.onError(e);
	}

	@Override
	public void onNext(T value) throws Exception {
		subscriber.onNext(value);
	}
}
