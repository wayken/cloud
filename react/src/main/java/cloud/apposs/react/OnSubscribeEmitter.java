package cloud.apposs.react;

public class OnSubscribeEmitter<T> implements React.OnSubscribe<T> {
    private final IoEmitter<? extends T> transformer;

    public OnSubscribeEmitter(IoEmitter<? extends T> transformer) {
        this.transformer = transformer;
    }

    @Override
    public void call(final SafeIoSubscriber<? super T> t) throws Exception {
        T value = transformer.call();
        t.onNext(value);
        t.onCompleted();
    }
}
