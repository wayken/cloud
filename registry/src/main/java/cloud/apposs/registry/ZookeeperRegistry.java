package cloud.apposs.registry;

import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.SysUtil;
import org.I0Itec.zkclient.ZkClient;

import java.nio.charset.Charset;

/**
 * 服务实例注册到zookeeper中，实现分布式的服务注册中心，采用zkclient zookeeper客户端实现，
 * zookeeper数据结构如下：
 * <pre>/service/providers/{serviceid}/{host:port}/{ServiceInstance}</pre>
 * 参考：
 * <pre>
 * https://blog.csdn.net/jiaowo_ccc/article/details/87976422
 * https://www.cnblogs.com/bearduncle/p/8602554.html
 * </pre>
 */
public class ZookeeperRegistry implements IRegistry {
    private static final int ZK_CONNECT_TIMEOUT = 10000;
    private static final int ZK_SESSION_TIMEOUT = 60000;

    private final ZkClient zkClient;

    // 服务注册路径前缀
    private final String path;

    private final Charset charset;

    public ZookeeperRegistry(String zkServers) {
        this(zkServers, IRegistry.DEFAULT_REGISTRY_ROOT_PATH, ZK_CONNECT_TIMEOUT, ZK_SESSION_TIMEOUT, CharsetUtil.UTF_8);
    }

    public ZookeeperRegistry(String zkServers, String path) {
        this(zkServers, path, ZK_CONNECT_TIMEOUT, ZK_SESSION_TIMEOUT, CharsetUtil.UTF_8);
    }

    public ZookeeperRegistry(String zkServers, int connectTimeout, int sessionTimeout) {
        this(zkServers, IRegistry.DEFAULT_REGISTRY_ROOT_PATH, connectTimeout, sessionTimeout, CharsetUtil.UTF_8);
    }

    public ZookeeperRegistry(String zkServers, Charset charset) {
        this(zkServers, IRegistry.DEFAULT_REGISTRY_ROOT_PATH, ZK_CONNECT_TIMEOUT, ZK_SESSION_TIMEOUT, charset);
    }

    public ZookeeperRegistry(String zkServers, String path, int connectTimeout, int sessionTimeout, Charset charset) {
        this.zkClient = new ZkClient(zkServers, connectTimeout, sessionTimeout);
        this.path = path;
        this.charset = charset;
    }

    @Override
    public boolean registInstance(ServiceInstance serviceInstance) {
        SysUtil.checkNotNull(serviceInstance, "serviceInstance");

        // 根节点不存在时先创建根节点（持久节点）
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }
        // 创建服务节点（持久节点）
        String serviceId = serviceInstance.getId();
        String servicePath = path + "/" + serviceId;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
        }
        // 会话地址节点存在则不创建，避免重复创建
        String addressPath = servicePath + "/" + serviceInstance.getPath();
        if (zkClient.exists(addressPath)) {
            return false;
        }
        // 创建会话地址节点，连接断开时zookeeper也会自动删除该临时节点
        String serviceValue = serviceInstance.getValue();
        zkClient.createEphemeral(addressPath, serviceValue.getBytes(charset));
        return true;
    }

    @Override
    public boolean deregistInstance(ServiceInstance serviceInstance) {
        SysUtil.checkNotNull(serviceInstance, "serviceInstance");

        // 根节点不存在
        if (!zkClient.exists(path)) {
            return false;
        }
        // 服务节点不存在
        String serviceId = serviceInstance.getId();
        String servicePath = path + "/" + serviceId;
        if (!zkClient.exists(servicePath)) {
            return false;
        }
        // 删除指定节点，示例/registry/{serviceid}/{host:port}
        String addressPath = servicePath + "/" + serviceInstance.getPath();
        if (!zkClient.exists(addressPath)) {
            return false;
        }
        return zkClient.delete(addressPath);
    }

    @Override
    public void release() {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
