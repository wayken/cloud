package cloud.apposs.discovery;

import cloud.apposs.balance.Peer;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 基于ZooKeeper的服务发现负载均衡
 */
public class ZooKeeperDiscovery extends AbstractDiscovery {
    private static final int DEAFAULT_ZK_CONNECT_TIMEOUT = 10 * 1000;
    private static final int DEAFAULT_ZK_SESSION_TIMEOUT = 20 * 1000;

    private final String path;

    private final CuratorFramework zkClient;

    private final Charset charset;

    public ZooKeeperDiscovery(String zkServers, String path) {
        this(null, zkServers, DEAFAULT_ZK_CONNECT_TIMEOUT, DEAFAULT_ZK_SESSION_TIMEOUT, path, CharsetUtil.UTF_8);
    }

    public ZooKeeperDiscovery(String serviceId, String zkServers, String path) {
        this(serviceId, zkServers, DEAFAULT_ZK_CONNECT_TIMEOUT, DEAFAULT_ZK_SESSION_TIMEOUT, path, CharsetUtil.UTF_8);
    }

    public ZooKeeperDiscovery(String serviceId, String zkServers, int connectTimeout, int sessionTimeout, String path, Charset charset) {
        super(serviceId, true);
        this.path = path;
        this.charset = charset;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectTimeout)
                .retryPolicy(retryPolicy)
                .build();
        this.zkClient.start();
    }

    @Override
    public Map<String, List<Peer>> handlePeersLoad() throws Exception {
        if (zkClient.checkExists().forPath(path) == null) {
            return null;
        }
        if (serviceId != null) {
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            String serviceIdPath = path + "/" + serviceId;
            handlPeerInit(serviceIdPath, result);
            return result;
        } else {
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            List<String> serviceIdList = zkClient.getChildren().forPath(path);
            // 更新存在的服务节点数据
            for (String serviceId : serviceIdList) {
                String serviceIdPath = path + "/" + serviceId;
                handlPeerInit(serviceIdPath, result);
            }
            return result;
        }
    }

    private void handlPeerInit(String serviceId, Map<String, List<Peer>> result) throws Exception {
        List<String> addressList = zkClient.getChildren().forPath(serviceId);
        List<Peer> peerList = new LinkedList<Peer>();
        for (String address : addressList) {
            String addressPath = serviceId + "/" + address;
            String addressStr = new String(zkClient.getData().forPath(addressPath), charset);
            Param addressInfo = JsonUtil.parseJsonParam(addressStr);
            String instanceId = addressInfo.getString(ServiceInstance.Name.ID);
            String instanceHost = addressInfo.getString(ServiceInstance.Name.HOST);
            int instancePort = addressInfo.getInt(ServiceInstance.Name.PORT);
            String instanceUrl = addressInfo.getString(ServiceInstance.Name.URL);
            Peer peer = new Peer(instanceId, instanceHost, instancePort, instanceUrl);
            peer.addMetadata(addressInfo.getParam(ServiceInstance.Name.METADATA));
            peerList.add(peer);
        }
        result.put(serviceId, peerList);
    }

    @Override
    public synchronized boolean shutdown() {
        if (super.shutdown()) {
            zkClient.close();
        }
        return false;
    }
}
