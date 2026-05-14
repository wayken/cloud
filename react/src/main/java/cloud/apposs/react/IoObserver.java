package cloud.apposs.react;

/**
 * 数据流处理的观察者接口，负责接收和处理数据（业务逻辑侧）
 */
public interface IoObserver<T> {
    /**
     * 数据流处理成功时的回调，接收请求到的数据进行处理
     *
     * @param value 数据流数据
     */
    void onNext(T value) throws Exception;

    /**
     * 数据流处理结束时的回调，
     * 注意如果不同的IoSubcriber实现类在{@link #onNext(Object)}、{@link #onError(Throwable)}内实现{actual.onCompleted()}时，
     * 那{@link #onCompleted()}则不需要实现，避免上层主动调用{@link #onCompleted()}导致该方法的主要逻辑失效
     */
    void onCompleted();

    /**
     * 数据流处理链中出现的异常均由此类捕捉并处理
     */
    void onError(Throwable cause);
}
