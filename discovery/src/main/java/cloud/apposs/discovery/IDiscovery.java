package cloud.apposs.discovery;

import cloud.apposs.balance.IPing;
import cloud.apposs.balance.IRule;
import cloud.apposs.balance.PeerListener;
import cloud.apposs.registry.ServiceInstance;

/**
 * 服务发现接口组件，负责维护serviceId:ILoadBalancer的映射关系
 */
public interface IDiscovery {
    /**
     * 选举负载均衡服务节点
     *
     * @param serviceId 服务注册ID
     */
    ServiceInstance choose(String serviceId);

    /**
     * 选举负载均衡服务节点
     *
     * @param serviceId 服务注册ID
     * @param key 负载KEY，例如AID等
     */
    ServiceInstance choose(String serviceId, Object key);

    /**
     * 设置负载均衡模式
     */
    void setRule(String serviceId, IRule rule);

    /**
     * 设置存活检测策略
     */
    void setPing(String serviceId, IPing ping);

    /**
     * 添加节点监听，包括节点添加和移除监听，
     * 便于业务自定义节点监听情况进行报警等操作
     */
    boolean addPeerListener(PeerListener peerListener);

    void start() throws Exception;

    boolean shutdown();
}
