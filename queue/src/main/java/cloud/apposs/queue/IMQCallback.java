package cloud.apposs.queue;

/**
 * 消息发送回调函数
 */
public interface IMQCallback {
    void onSuccess(MQRecord result);

    void onException(Throwable cause);
}
