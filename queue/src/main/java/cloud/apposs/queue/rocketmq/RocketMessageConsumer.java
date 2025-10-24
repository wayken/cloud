package cloud.apposs.queue.rocketmq;

import cloud.apposs.queue.IMQConsumer;
import cloud.apposs.queue.IMQListener;
import cloud.apposs.queue.MQConfig;
import cloud.apposs.queue.MQRecord;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RocketMessageConsumer implements IMQConsumer {
    private DefaultMQPushConsumer consumer;

    private final MQConfig config;

    private AtomicBoolean started = new AtomicBoolean(false);

    public RocketMessageConsumer(MQConfig config, String group) {
        this.consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(config.getNameSrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        this.config = config;
    }

    @Override
    public IMQConsumer subscribe(String topic, IMQListener listener) throws Exception {
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    listener.onConsumeMessage(new MQRecord(topic, msg.getKeys(),
                            new String(msg.getBody(), Charset.forName(config.getChartset())), msg.getBornTimestamp()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.subscribe(topic, "*");
        return this;
    }

    @Override
    public void start() throws Exception {
        if (started.getAndSet(true)) {
            return;
        }
        consumer.start();
    }

    @Override
    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }
}
