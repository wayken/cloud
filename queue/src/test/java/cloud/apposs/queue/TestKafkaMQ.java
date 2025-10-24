package cloud.apposs.queue;

import cloud.apposs.queue.kafka.KafkaMessageConsumer;
import cloud.apposs.queue.kafka.KafkaMessageProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestKafkaMQ {
    private IMQProducer producer;

    private IMQConsumer consumer;

    private static final String TOPIC = "mymqtopic1";
    private static final String GROUP = "myconsumergroup1";

    @Before
    public void before() {
        MQConfig config = new MQConfig();
        config.setBrokerServers("192.168.0.31:9092");
        producer = new KafkaMessageProducer(config);
        consumer = new KafkaMessageConsumer(config, GROUP);
    }

    @Test
    public void testProduce() throws Exception {
        producer.send(TOPIC, "mykey3",  "myvalue3");
    }

    @Test
    public void testConsumer() throws Exception {
        CountDownLatch latch = new CountDownLatch(10);
        consumer.subscribe(TOPIC, new IMQListener() {
            @Override
            public void onConsumeMessage(MQRecord record) {
                latch.countDown();
                System.out.println(record.topic() + ":" + record.key() + ":" + record.value());
            }
        }).start();
        latch.await();
    }

    @After
    public void after() {
        producer.shutdown();
        consumer.shutdown();
    }
}
