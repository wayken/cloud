package cloud.apposs.queue;

/**
 * 消息队列生产者，全局单例
 */
public interface IMQProducer {
    MQRecord send(String topic, String content) throws Exception;

    /**
     * 同步发送消息
     *
     * @param topic   消息主题
     * @param key     消息KEY
     * @param content 消息内容
     * @return 响应结果
     */
    MQRecord send(String topic, String key, String content) throws Exception;

    void send(String topic, String content, IMQCallback callback) throws Exception;

    /**
     * 异步发送消息
     *
     * @param topic   消息主题
     * @param key     消息KEY
     * @param content 消息内容
     * @param callback 消息处理回调
     * @return 响应结果
     */
    void send(String topic, String key, String content, IMQCallback callback) throws Exception;

    /**
     * 关闭消息生产服务
     */
    void shutdown();
}
