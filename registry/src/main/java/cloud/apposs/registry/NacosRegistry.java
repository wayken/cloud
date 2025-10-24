package cloud.apposs.registry;

import cloud.apposs.util.SysUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 服务实例注册到Nacos中，数据结构如下：
 * <pre>
 *     | Service Name | Group Name           | Instance          |
 *     +--------------+----------------------+-------------------+
 *     | ServiceId    | RPC/WEBSOCKET/TOMCAT | {ServiceInstance} |
 * </pre>
 * 当Noacos服务宕机重启时，服务发现会自动恢复注册
 */
public class NacosRegistry implements IRegistry {
    public static final String DEFAULT_GROUP_NAME = "SERVICE";

    private final NamingService namingService;

    private final String groupName;

    public NacosRegistry(String servers) throws Exception {
        this(servers, DEFAULT_GROUP_NAME);
    }

    public NacosRegistry(String servers, String groupName) throws Exception {
        if (groupName != null && !groupName.isEmpty()) {
            this.groupName = groupName;
        } else {
            this.groupName = DEFAULT_GROUP_NAME;
        }
        Properties properties = new Properties();
        properties.setProperty("serverAddr", servers);
        this.namingService = NacosFactory.createNamingService(properties);
    }

    @Override
    public boolean registInstance(ServiceInstance serviceInstance) throws Exception {
        SysUtil.checkNotNull(serviceInstance, "serviceInstance");
        Instance instance = new Instance();
        instance.setIp(serviceInstance.getHost());
        instance.setPort(serviceInstance.getPort());
        if (serviceInstance.getMetadata() != null) {
            Map<String, String> metadata = new HashMap<>(serviceInstance.getMetadata().size());
            for (Map.Entry<String, Object> entry : serviceInstance.getMetadata().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    metadata.put(entry.getKey(), entry.getValue().toString());
                }
            }
            instance.setMetadata(metadata);
        }
        namingService.registerInstance(serviceInstance.getId(), groupName, instance);
        return true;
    }

    @Override
    public boolean deregistInstance(ServiceInstance serviceInstance) throws Exception {
        SysUtil.checkNotNull(serviceInstance, "serviceInstance");
        namingService.deregisterInstance(serviceInstance.getId(), groupName, serviceInstance.getHost(), serviceInstance.getPort());
        return true;
    }

    @Override
    public void release() {
        if (namingService == null) {
            return;
        }
        try {
            namingService.shutDown();
        } catch (Exception ignore) {
        }
    }
}
