package cloud.apposs.balance;

import java.util.List;

/**
 * 负载均衡服务，
 * 对于该服务来说，其下管理的永远只是后端Peer节点，不关心其他任何细节
 */
public interface ILoadBalancer {
    /**
     * 添加后端服务以实现多后端负载均衡，核心业务接口
     */
    void addPeers(Peer... peers);

    void addPeers(List<Peer> peers);

    /**
     * 更新后端节点，同时也会根据{@link IPing}策略来判断节点是否存活
     */
    void updatePeers(List<Peer> peers);

    /**
     * 通过负载均衡算法选择其中一台后端服务，核心业务接口
     */
    Peer choosePeer(Object key);

    /**
     * 获取可用的后端服务列表
     */
    List<Peer> getReachablePeer();

    /**
     * 获取所有的后端服务列表
     */
    List<Peer> getAllPeers();

    /**
     * 标记后端服务为不可用
     */
    void markPeerDown(Peer peer);

    /**
     * 检查后端服务是否可用
     */
    boolean isPeerAlive(Peer peer);

    /**
     * 设置负载均衡策略
     */
    void setRule(IRule rule);

    /**
     * 设置线路存活检测服务
     */
    void setPing(IPing rule);

    /**
     * 添加节点监听，包括节点添加和移除监听，
     * 便于业务自定义节点监听情况进行报警等操作
     */
    boolean addPeerListener(PeerListener peerListener);

    /**
     * 关闭负载均衡服务
     */
    void shutdown();
}
