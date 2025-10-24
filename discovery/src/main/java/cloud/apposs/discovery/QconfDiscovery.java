package cloud.apposs.discovery;

import cloud.apposs.balance.Peer;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import net.qihoo.qconf.Qconf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 基于QConf的服务发现负载均衡
 */
public class QconfDiscovery extends AbstractDiscovery {
    private final String path;

    private final String environment;

    public QconfDiscovery(String environment, String path) {
        this.environment = environment;
        this.path = path;
    }

    @Override
    public Map<String, List<Peer>> handlePeersLoad() throws Exception {
        if (serviceId != null) {
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            String serviceIdPath = path + "/" + serviceId;
            handlPeerInit(serviceIdPath, result);
            return result;
        } else {
            Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
            List<String> serviceIdList = Qconf.getBatchKeys(path, environment);
            // 更新存在的服务节点数据
            for (String serviceId : serviceIdList) {
                String serviceIdPath = path + "/" + serviceId;
                handlPeerInit(serviceIdPath, result);
            }
            return result;
        }
    }

    private void handlPeerInit(String serviceId, Map<String, List<Peer>> result) throws Exception {
        List<String> addressList = Qconf.getBatchKeys(serviceId, environment);
        List<Peer> peerList = new LinkedList<Peer>();
        for (String address : addressList) {
            String addressPath = serviceId + "/" + address;
            String addressStr = Qconf.getConf(addressPath, environment);
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
}
