package cloud.apposs.react;

/**
 * 数据流处理的订阅接口，负责控制数据流速和取消（流量契约侧）
 */
public interface IoSubscription {
    /**
     * 是否已取消订阅，如果已取消订阅则所有处理器都不再处理数据流中的数据
     *
     * @return 是否已取消订阅
     */
    boolean isUnsubscribed();

    /**
     * 取消订阅，所有处理器都不再处理数据流中的数据
     */
    void unsubscribe() throws Exception;

    /**
     * 注册取消订阅监听器，当{@link #unsubscribe()}被调用时触发
     *
     * @param onUnsubscribe 取消订阅时的回调
     */
    default void addOnUnsubscribe(IoRunnable onUnsubscribe) throws Exception {
    }
}
