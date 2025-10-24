package cloud.apposs.queue;

public class MQConfig {
    private String chartset = "UTF-8";

    /** Broker服务地址列表，填写多个用","隔开 */
    private String brokerServers;

    /**
     * acks表示所有需同步返回确认的节点数
     * all或者‑1表示分区全部备份节点均需响应，可靠性最高，但吞吐量会相对降低
     * 1表示只需分区leader节点响应，0表示无需等待服务端响应
     * 大部分业务建议配置1，风控或安全建议配置0
     */
    private String acks = "1";

    /**
     * 消息重试次数，
     * 当出现网络的瞬时抖动时，消息发送可能会失败，
     * 此时配置了 retries > 0 的 Producer 能够自动重试消息发送，避免消息丢失
     * 注意：如果配置重试请保证消费端具有业务上幂等
     */
    private int retries = 5;

    /**
     * 该配置控制 KafkaProducer.send() 和 KafkaProducer.partitionsFor() 将阻塞多长时间，默认为60S
     */
    private int maxBlockTime = 60000;

    /**
     * 从何处开始消费，latest 表示消费最新消息，earliest 表示从头开始消费，none表示抛出异常，默认latest
     */
    private String offset = "latest";

    public String getChartset() {
        return chartset;
    }

    public void setChartset(String chartset) {
        this.chartset = chartset;
    }

    /**
     * RocketMQ对应Name Server
     */
    private String nameSrvAddr;

    public String getBrokerServers() {
        return brokerServers;
    }

    public void setBrokerServers(String brokerServers) {
        this.brokerServers = brokerServers;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getMaxBlockTime() {
        return maxBlockTime;
    }

    public void setMaxBlockTime(int maxBlockTime) {
        this.maxBlockTime = maxBlockTime;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getNameSrvAddr() {
        return nameSrvAddr;
    }

    public void setNameSrvAddr(String nameSrvAddr) {
        this.nameSrvAddr = nameSrvAddr;
    }
}
