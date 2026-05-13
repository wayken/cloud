package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

public class OnSubscribeEmitter<T> implements OnSubscribe<T> {
    private final IoEmitter<? extends T> transformer;

    public OnSubscribeEmitter(IoEmitter<? extends T> transformer) {
        this.transformer = transformer;
    }

    @Override
    public void call(IoSubscriber<? super T> t) throws Exception {
        T value = transformer.call();
        t.onNext(value);
        t.onCompleted();
    }
}
