package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * 基于数组的数据发送器
 */
public class OnSubscribeFromArray<T> implements OnSubscribe<T> {
	private final T[] value;

	public OnSubscribeFromArray(T[] value) {
		this.value = value;
	}

	@Override
	public void call(IoSubscriber<? super T> subscriber) throws Exception {
		for (int i = 0; i < value.length; i++) {
			if (subscriber.isUnsubscribed()) {
				return;
			}
			subscriber.onNext(value[i]);
		}
		if (!subscriber.isUnsubscribed()) {
			subscriber.onCompleted();
		}
	}
}
