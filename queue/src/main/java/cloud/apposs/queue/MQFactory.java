package cloud.apposs.queue;

import cloud.apposs.queue.kafka.KafkaMessageConsumer;
import cloud.apposs.queue.kafka.KafkaMessageProducer;
import cloud.apposs.queue.rocketmq.RocketMessageConsumer;
import cloud.apposs.queue.rocketmq.RocketMessageProducer;

public class MQFactory {
    /**
     * 获取消息队列生产者服务，每个业务必须全局单单例
     *
     * @param type   消息队列类型，详见{@link MQConstant}
     * @param config 全局配置
     * @return 消息队列生产者服务
     */
    public static IMQProducer getMQProducer(int type, MQConfig config, String group) throws Exception {
        if (type == MQConstant.MQ_TYPE_KAFKA) {
            return new KafkaMessageProducer(config);
        } else if (type == MQConstant.MQ_TYPE_ROCKETMQ) {
            return new RocketMessageProducer(config, group);
        }
        return null;
    }

    /**
     * 获取消息队列消费者服务，每个业务必须全局单单例
     *
     * @param type   消息队列类型，详见{@link MQConstant}
     * @param config 全局配置
     * @param group  消费组群
     * @return 消息队列生产者服务
     */
    public static IMQConsumer getMQConsumer(int type, MQConfig config, String group) {
        if (type == MQConstant.MQ_TYPE_KAFKA) {
            return new KafkaMessageConsumer(config, group);
        } else if (type == MQConstant.MQ_TYPE_ROCKETMQ) {
            return new RocketMessageConsumer(config, group);
        }
        return null;
    }
}
