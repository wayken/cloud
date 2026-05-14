package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;
import cloud.apposs.util.Errno;

public final class OnSubscribeMatch<T> implements OnSubscribe<T> {
	private final React<T> source;

	final IoFunction<? super T, Errno> predicate;

    public OnSubscribeMatch(React<T> source, IoFunction<? super T, Errno> predicate) {
        this.source = source;
        this.predicate = predicate;
    }
	
	@Override
	public void call(IoSubscriber<? super T> subscriber) throws Exception {
		MatchSubscriber<T> parent = new MatchSubscriber<T>(subscriber, predicate);
		subscriber.add(parent);
		source.subscribe(parent).start();
	}
	
	public static final class MatchException extends Exception {
		private static final long serialVersionUID = -8099544492758850343L;

		private Errno errno;
		
		public MatchException(Errno errno) {
			this.errno = errno;
		}
		
		public MatchException(String message, Errno errno) {
			super(message);
			this.errno = errno;
		}
		
		public Errno errno() {
			return errno;
		}
	}
	
	private static final class MatchSubscriber<T> extends SafeIoSubscriber<T> {
        private final IoFunction<? super T, Errno> predicate;

        public MatchSubscriber(IoSubscriber<? super T> subscriber, IoFunction<? super T, Errno> predicate) {
            super(subscriber);
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) throws Exception {
            Errno result = predicate.call(t);
            if (result == Errno.OK) {
            	subscriber.onNext(t);
            } else {
            	subscriber.onError(new MatchException(result));
            }
        }
    }
}
