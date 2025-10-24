package cloud.apposs.queue.kafka;

import cloud.apposs.queue.IMQConsumer;
import cloud.apposs.queue.IMQListener;
import cloud.apposs.queue.MQConfig;
import cloud.apposs.queue.MQRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaMessageConsumer implements IMQConsumer {
    private KafkaConsumer<String, String> consumer;

    private AtomicBoolean started = new AtomicBoolean(false);

    private IMQListener listener;

    /**
     * 消息队列构造函数
     *
     * @param config 配置项
     * @param group  消费组，同一个消费组内是点对点模式，多个不同消费是发布订阅模式，
     *               具体参考：https://juejin.cn/post/7205882403949035580
     */
    public KafkaMessageConsumer(MQConfig config, String group) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getBrokerServers());
        // 设置消费组ID，同一个消费组下对TOPIC的消费是互斥的
        // 同时也看TOPIC下有多少个分区，如果有2个分区，起多个同组消费者也不会提升对队列的消费速度
        props.put("group.id", group);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 1000);
        props.put("auto.offset.reset", config.getOffset());
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<String, String>(props);
    }

    @Override
    public IMQConsumer subscribe(String topic, IMQListener listener) {
        this.listener = listener;
        consumer.subscribe(Collections.singleton(topic));
        return this;
    }

    @Override
    public void start() {
        if (started.getAndSet(true)) {
            return;
        }
        new Thread(this::consumer).start();
    }

    private void consumer() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record: records) {
                    listener.onConsumeMessage(new MQRecord(record.topic(), record.key(), record.value(), record.timestamp()));
                }
            }
        } finally {
            consumer.close();
        }
    }

    @Override
    public void shutdown() {
        // 唤醒线程关闭消费者
        // 因为当前关闭方法和消费不在同一个线程中，consumer是非线程安全，只能唤醒，在另外的线程中关闭消费者
        consumer.wakeup();
    }
}
