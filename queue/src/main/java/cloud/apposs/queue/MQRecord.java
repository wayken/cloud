package cloud.apposs.queue;

/**
 * 消息队列生产者/消费者返回结果集
 */
public class MQRecord {
    private final String topic;

    private final String key;

    private final String value;

    private final long timestamp;

    public MQRecord(String topic, String key, String value, long timestamp) {
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String topic() {
        return topic;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public long timestamp() {
        return timestamp;
    }
}
