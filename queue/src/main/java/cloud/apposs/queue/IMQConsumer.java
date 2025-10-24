package cloud.apposs.queue;

/**
 * 消息队列消费者，全局单例
 */
public interface IMQConsumer {
    /**
     * 订阅主题
     *
     * @param topic    主题
     * @param listener 消息订阅监听
     */
    IMQConsumer subscribe(String topic, IMQListener listener) throws Exception;

    /**
     * 订阅主题之后开始监听消息生产
     */
    void start() throws Exception;

    /**
     * 关闭消息生产服务
     */
    void shutdown();
}
