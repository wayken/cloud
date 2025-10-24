package cloud.apposs.queue;

import cloud.apposs.queue.rocketmq.RocketMessageConsumer;
import cloud.apposs.queue.rocketmq.RocketMessageProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestRocketMQ {
    private IMQProducer producer;

    private IMQConsumer consumer;

    private static final String TOPIC = "mymqtopic1";
    private static final String GROUP = "mqgroup1";

    @Before
    public void before() throws Exception {
        MQConfig config = new MQConfig();
        config.setNameSrvAddr("192.168.4.11:9876");
        producer = new RocketMessageProducer(config, GROUP);
        consumer = new RocketMessageConsumer(config, GROUP);
    }

    @Test
    public void testProduce() throws Exception {
        int initial = 24;
        for (int i = 0; i < 2; i++) {
            producer.send(TOPIC, "mykey3",  "myvalue" + (initial + i));
        }
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
