package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

/**
 * 基于数组的数据发送器
 */
public class OnSubscribeFromArray<T> implements OnSubscribe<T> {
	private final T[] array;

	public OnSubscribeFromArray(T[] array) {
        this.array = array;
    }

	@Override
	public void call(IoSubscriber<? super T> t) throws Exception {
	    try {
            for (int i = 0; i < array.length; i++) {
                t.onNext(array[i]);
            }
	    } finally{
	        t.onCompleted();
        }
	}
}
