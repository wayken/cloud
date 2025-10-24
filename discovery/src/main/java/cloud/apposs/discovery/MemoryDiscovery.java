package cloud.apposs.discovery;

import cloud.apposs.balance.BaseLoadBalancer;
import cloud.apposs.balance.ILoadBalancer;
import cloud.apposs.balance.LbConfig;
import cloud.apposs.balance.Peer;

import java.util.List;
import java.util.Map;

/**
 * 基于内存的服务发现负载均衡
 */
public class MemoryDiscovery extends AbstractDiscovery {
    private final Map<String, List<Peer>> peers;

    public MemoryDiscovery(Map<String, List<Peer>> peers) {
        this(false, peers);
    }

    /**
     * 内存构造时直接初始化
     *
     * @param peers 服务列表
     */
    public MemoryDiscovery(boolean autoPolling, Map<String, List<Peer>> peers) {
        super(autoPolling);
        this.peers = peers;
        for (String serviceId : peers.keySet()) {
            List<Peer> peerList = peers.get(serviceId);
            LbConfig lbConfig = new LbConfig();
            lbConfig.setAutoPing(false);
            ILoadBalancer balancer = new BaseLoadBalancer(lbConfig);
            balancer.addPeers(peerList);
            addBalancer(serviceId, balancer);
        }
    }

    @Override
    public Map<String, List<Peer>> handlePeersLoad() {
        return this.peers;
    }
}
