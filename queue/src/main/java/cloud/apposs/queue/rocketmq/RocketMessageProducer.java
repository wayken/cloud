package cloud.apposs.queue.rocketmq;

import cloud.apposs.queue.IMQCallback;
import cloud.apposs.queue.IMQProducer;
import cloud.apposs.queue.MQConfig;
import cloud.apposs.queue.MQRecord;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.Charset;

public class RocketMessageProducer implements IMQProducer {
    private static final String DEFAULT_TAG = "default";

    private DefaultMQProducer producer;

    private final MQConfig config;

    public RocketMessageProducer(MQConfig config, String group) throws Exception {
        this.config = config;
        this.producer = new DefaultMQProducer(group);
        this.producer.setNamesrvAddr(config.getNameSrvAddr());
        this.producer.start();
    }

    @Override
    public MQRecord send(String topic, String content) throws Exception {
        return send(topic, DEFAULT_TAG, content);
    }

    @Override
    public MQRecord send(String topic, String key, String content) throws Exception {
        Message message = new Message(topic, key, content.getBytes(config.getChartset()));
        SendResult result = producer.send(message);
        return new MQRecord(topic, key, content, System.currentTimeMillis());
    }

    @Override
    public void send(String topic, String content, IMQCallback callback) throws Exception {
        send(topic, DEFAULT_TAG, content, callback);
    }

    @Override
    public void send(String topic, String key, String content, IMQCallback callback) throws Exception {
        Message message = new Message(topic, key, content.getBytes(Charset.forName(config.getChartset())));
        producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                callback.onSuccess(new MQRecord(topic, key, content, System.currentTimeMillis()));
            }
            @Override
            public void onException(Throwable cause) {
                callback.onException(cause);
            }
        });
    }

    @Override
    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
