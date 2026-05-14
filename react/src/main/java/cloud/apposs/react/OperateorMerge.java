package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 合并多个请求结果，
 * 一般用于同时请求多个网络连接场景，
 * 无论成功或者失败当所有请求都结束后最后都会调用{@link IoSubscriber#onCompleted()}方法
 */
public class OperateorMerge<T> implements OnSubscribe<T> {
	private final React<? extends T>[] sequences;
	
	public OperateorMerge(React<? extends T>[] sequences) {
		if (sequences == null || sequences.length <= 0) {
			throw new IllegalArgumentException("sequences");
		}
		this.sequences = sequences;
	}
	
	@SuppressWarnings("unchecked")
	public OperateorMerge(List<? extends React<? extends T>> sequences) {
		if (sequences == null || sequences.size() <= 0) {
			throw new IllegalArgumentException("sequences");
		}
		this.sequences = new React[sequences.size()];
		sequences.toArray(this.sequences);
	}

	@Override
	public void call(IoSubscriber<? super T> subscriber) throws Exception {
		MergeSubscriber<T> parent = new MergeSubscriber<T>(subscriber, sequences.length);
		subscriber.add(parent);
		for (int i = 0; i < sequences.length; i++) {
			if (subscriber.isUnsubscribed()) {
				return;
			}
			React<? extends T> react = sequences[i];
            react.subscribe(parent).start();
        }
	}
	
	private static final class MergeSubscriber<T> extends SafeIoSubscriber<T> {
		private final int total;
		
		private final AtomicInteger index;
		
        public MergeSubscriber(IoSubscriber<? super T> subscriber, int total) {
            super(subscriber);
            this.total = total;
            this.index = new AtomicInteger(0);
        }
		
		@Override
		public void onNext(T value) {
			try {
				subscriber.onNext(value);
				index.incrementAndGet();
			} catch(Throwable t) {
				onError(t);
			} finally {
				if (index.get() >= total) {
					subscriber.onCompleted();
				}
			}
		}
		
		@Override
		public void onError(Throwable t) {
			try {
				index.incrementAndGet();
				subscriber.onError(t);
			} finally {
				if (index.get() >= total) {
					subscriber.onCompleted();
				}
			}
		}
	}
}
