package cloud.apposs.react;

/**
 * IoSubscriber的适配器类，提供默认实现，业务方可根据需要选择性重写方法
 */
public class IoSubscripberAdapter<T> implements IoSubscriber<T> {
    @Override
    public void onNext(T value) throws Exception {
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable cause) {
    }
}
