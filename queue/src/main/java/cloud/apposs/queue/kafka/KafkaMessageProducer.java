package cloud.apposs.queue.kafka;

import cloud.apposs.queue.IMQCallback;
import cloud.apposs.queue.IMQProducer;
import cloud.apposs.queue.MQConfig;
import cloud.apposs.queue.MQRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka消息生产服务，全局单例
 */
public class KafkaMessageProducer implements IMQProducer {
    private KafkaProducer<String, String> producer;

    public KafkaMessageProducer(MQConfig config) {
        Properties properties = new Properties();
        // 指定 Broker
        properties.put("bootstrap.servers", config.getBrokerServers());
        // 将 key 的 Java 对象转成字节数组
        properties.put("key.serializer", StringSerializer.class.getName());
        // 将 value 的 Java 对象转成字节数组
        properties.put("value.serializer", StringSerializer.class.getName());
        // 消息至少成功发给一个副本后才返回成功
        properties.put("acks", config.getAcks());
        // 消息发送超时时间
        properties.put("max.block.ms", config.getMaxBlockTime());
        // 消息重试 5 次
        properties.put("retries", config.getRetries());
        this.producer = new KafkaProducer<String,String>(properties);
    }

    @Override
    public MQRecord send(String topic, String content) throws Exception {
        return send(topic, null, content);
    }

    @Override
    public MQRecord send(String topic, String key, String value) throws Exception {
        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(topic, key, value));
        RecordMetadata result = future.get();
        return new MQRecord(result.topic(), key, value, result.timestamp());
    }

    @Override
    public void send(String topic, String content, IMQCallback callback) {
        send(topic, null, content, callback);
    }

    @Override
    public void send(String topic, String key, String value, IMQCallback callback) {
        producer.send(new ProducerRecord<String, String>(topic, key, value), new Callback() {
            @Override
            public void onCompletion(RecordMetadata result, Exception exception) {
                if (exception != null) {
                    callback.onException(exception);
                } else {
                    callback.onSuccess(new MQRecord(result.topic(), key, value, result.timestamp()));
                }
            }
        });
    }

    @Override
    public void shutdown() {
        if (producer != null) {
            producer.close();
        }
    }
}
