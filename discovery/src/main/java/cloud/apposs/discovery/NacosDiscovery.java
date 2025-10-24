package cloud.apposs.discovery;

import cloud.apposs.balance.Peer;
import cloud.apposs.util.CharsetUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 基于Nacos的服务发现负载均衡
 */
public class NacosDiscovery extends AbstractDiscovery {
    private final String groupName;

    private final NamingService namingService;

    private final Charset charset;

    public NacosDiscovery(String servers, String groupName) throws Exception {
        this(null, servers, groupName, CharsetUtil.UTF_8);
    }

    public NacosDiscovery(String serviceId, String servers, String groupName) throws Exception {
        this(serviceId, servers, groupName, CharsetUtil.UTF_8);
    }

    public NacosDiscovery(String serviceId, String servers, String groupName, Charset charset) throws Exception {
        super(serviceId, true);
        this.groupName = groupName;
        Properties properties = new Properties();
        properties.setProperty("serverAddr", servers);
        this.namingService = NacosFactory.createNamingService(properties);
        this.charset = charset;
    }

    @Override
    public Map<String, List<Peer>> handlePeersLoad() throws Exception {
        if (serviceId != null) {
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            handlPeerInit(serviceId, result);
            return result;
        } else {
            int pageNo = 1;
            int pageSize = 1000;
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            while (true) {
                ListView<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, groupName);
                if (serviceList == null) {
                    break;
                }
                List<String> serviceIdList = serviceList.getData();
                if (serviceIdList == null || serviceIdList.isEmpty()) {
                    break;
                }
                for (String serviceId : serviceIdList) {
                    handlPeerInit(serviceId, result);
                }
                pageNo++;
            }
            return result;
        }
    }

    private void handlPeerInit(String serviceId, Map<String, List<Peer>> result) throws Exception {
        List<Instance> instances = namingService.getAllInstances(serviceId, groupName);
        if (instances == null || instances.isEmpty()) {
            return;
        }
        List<Peer> peerList = new ArrayList<>();
        for (Instance instance : instances) {
            Peer peer = new Peer(instance.getIp(), instance.getPort());
            if (instance.getMetadata() != null) {
                for (Map.Entry<String, String> entry : instance.getMetadata().entrySet()) {
                    peer.addMetadata(entry.getKey(), entry.getValue());
                }
            }
            peerList.add(peer);
        }
        result.put(serviceId, peerList);
    }

    @Override
    public synchronized boolean shutdown() {
        if (super.shutdown()) {
            try {
                namingService.shutDown();
            } catch (Exception ignore) {
            }
        }
        return true;
    }
}
