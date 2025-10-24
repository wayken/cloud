package cloud.apposs.discovery;

import cloud.apposs.balance.*;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.util.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractDiscovery implements IDiscovery {
    /**
     * 服务注册ID:负载均衡映射
     */
    protected Map<String, ILoadBalancer> balances;

    /**
     * 节点监听，包括节点添加和移除监听
     */
    protected List<PeerListener> peerListeners = new CopyOnWriteArrayList<PeerListener>();

    /**
     * 服务主动发现轮询器，负责定时检查服务注册ID对应的实例列表是否有更新
     */
    protected PollingServiceDiscovery polling;

    protected AtomicBoolean active = new AtomicBoolean(false);

    /**
     * 负载均衡规则，可定义每个服务注册ID不同的负载均衡模式
     */
    protected final Map<String, IRule> rules = new HashMap<String, IRule>();

    /**
     * 是否定时检查服务是否可用，可定义每个服务注册ID不同的存活检测模式
     */
    protected final Map<String, IPing> pings = new HashMap<String, IPing>();

    protected final String serviceId;

    public AbstractDiscovery() {
        this(null, true);
    }

    public AbstractDiscovery(boolean autoPolling) {
        this(null, autoPolling);
    }

    public AbstractDiscovery(String serviceId, boolean autoPolling) {
        this.balances = new ConcurrentHashMap<String, ILoadBalancer>();
        if (autoPolling) {
            this.polling = new PollingServiceDiscovery();
        }
        this.serviceId = serviceId;
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        return choose(serviceId, "default");
    }

    @Override
    public ServiceInstance choose(String serviceId, Object key) {
        ILoadBalancer balancer = balances.get(serviceId);
        if (balancer == null) {
            return null;
        }
        Peer peer = balancer.choosePeer(key);
        if (peer == null) {
            return null;
        }
        return new ServiceInstance(peer.getId(), peer.getHost(), peer.getPort(), peer.getUrl(), peer.getMetadata());
    }

    @Override
    public void setRule(String serviceId, IRule rule) {
        if (StrUtil.isEmpty(serviceId) || rule == null) {
            throw new IllegalArgumentException();
        }
        rules.put(serviceId, rule);
        ILoadBalancer balancer = balances.get(serviceId);
        if (balancer != null) {
            balancer.setRule(rule);
        }
    }

    @Override
    public void setPing(String serviceId, IPing ping) {
        if (StrUtil.isEmpty(serviceId) || ping == null) {
            throw new IllegalArgumentException();
        }
        pings.put(serviceId, ping);
        ILoadBalancer balancer = balances.get(serviceId);
        if (balancer != null) {
            balancer.setPing(ping);
        }
    }

    /**
     * 添加负载均衡器到发现服务，同时也添加发现服务中业务自定义的PeerListener，便于监听节点存活并报警
     */
    public void addBalancer(String serviceId, ILoadBalancer balancer) {
        for (PeerListener listener : peerListeners) {
            balancer.addPeerListener(listener);
        }
        balances.put(serviceId, balancer);
    }

    @Override
    public boolean addPeerListener(PeerListener peerListener) {
        return peerListeners.add(peerListener);
    }

    public void updateLoadBalances() throws Exception {
        Map<String, List<Peer>> servicePeerList = handlePeersLoad();
        if (servicePeerList == null) {
            return;
        }
        for (Map.Entry<String, List<Peer>> entry : servicePeerList.entrySet()) {
            String serviceId = entry.getKey();
            List<Peer> peerList = entry.getValue();
            ILoadBalancer balancer = balances.get(serviceId);
            if (balancer == null) {
                balancer = new BaseLoadBalancer();
                IRule rule = rules.get(serviceId);
                if (rule != null) {
                    balancer.setRule(rule);
                }
                IPing ping = pings.get(serviceId);
                if (ping != null) {
                    balancer.setPing(ping);
                }
                addBalancer(serviceId, balancer);
            }
            balancer.updatePeers(peerList);
        }
        // 踢除已经不存在的服务节点
        for (String serviceId : balances.keySet()) {
            if (!servicePeerList.containsKey(serviceId)) {
                balances.remove(serviceId).shutdown();
            }
        }
    }

    /**
     * 获取服务节点列表，由各个服务发现实现提供，注意：
     * <pre>
     *     1. 当{@link #serviceId}为null时，
     *     则代表是客户端请求未知ServiceId，需要返回所有服务节点列表，一般用于服务发现场景
     *     2. 当{@link #serviceId}不为null时，
     *     则代表是服务端请求指定ServiceId，需要返回指定ServiceId的服务节点列表，一般用于网关路由配置serviceId场景
     * </pre>
     */
    public abstract Map<String, List<Peer>> handlePeersLoad() throws Exception;

    @Override
    public synchronized void start() throws Exception {
        if (!active.compareAndSet(false, true)) {
            return;
        }
        updateLoadBalances();
        if (polling != null) {
            polling.start();
        }
    }

    @Override
    public synchronized boolean shutdown() {
        if (!active.get()) {
            return false;
        }
        if (polling != null) {
            polling.shutdown();
        }
        for (ILoadBalancer balancer : balances.values()) {
            balancer.shutdown();
        }
        return active.compareAndSet(true, false);
    }

    /**
     * 定期检测是否有新注册服务节点
     */
    private class PollingServiceDiscovery extends Thread {
        private static final String THREAD_NAME = "Thread-PollingServiceDiscovery";
        private static final int DEFAULT_INTERVAL = 30 * 1000;

        private int interval = DEFAULT_INTERVAL;

        public PollingServiceDiscovery() {
            this.setName(THREAD_NAME);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (active.get()) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
                try {
                    updateLoadBalances();
                } catch (Exception e) {
                }
            }
        }

        public void shutdown() {
            active.compareAndSet(true, false);
        }
    }
}
