package cloud.apposs.discovery;

import cloud.apposs.balance.Peer;
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.StrUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 基于配置文件读取的服务发现负载均衡，配置文件格式如下：
 * <pre>
 * {
 *     "proxy_socks": [
 *         {
 *             "ip": "xx.xx.xx.xx",
 *             "port": xxx
 *         },
 *         {
 *             "ip": "xx.xx.xx.xx",
 *             "port": xxx
 *         }
 *     ],
 *     "proxy_domain": [
 *         {
 *             "ip": "xx.xx.xx.xx",
 *             "port": xxx
 *         }
 *     ]
 * }
 * </pre>
 */
public class FileDiscovery extends AbstractDiscovery {
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String METADATA = "metadata";

    private final File cachefile;

    /** 文件修改时间 ，主要用于判断文件配置是否更新 */
    private long lastModified = 0;

    public FileDiscovery(String filename) throws IOException {
        File config = new File(filename);
        if (!config.exists()) {
            throw new FileNotFoundException(filename);
        }
        this.cachefile = config;
    }

    @Override
    public Map<String, List<Peer>> handlePeersLoad() {
        long fileLastModified = cachefile.lastModified();
        if (fileLastModified == lastModified) {
            return null;
        }
        String json = FileUtil.readString(cachefile);
        if (StrUtil.isEmpty(json)) {
            return null;
        }
        Param config = JsonUtil.parseJsonParam(json);
        if (config == null) {
            return null;
        }
        lastModified = fileLastModified;
        Map<String, List<Peer>> result = new HashMap<String, List<Peer>>();
        for (String serviceId : config.keySet()) {
            List<Param> addressList = config.getList(serviceId);
            List<Peer> peerList = new LinkedList<Peer>();
            for (Param addressInfo : addressList) {
                String instanceHost = addressInfo.getString(IP);
                int instancePort = addressInfo.getInt(PORT);
                Peer peer = new Peer(instanceHost, instancePort);
                peer.addMetadata(addressInfo.getParam(METADATA));
                peerList.add(peer);
            }
            result.put(serviceId, peerList);
        }
        return result;
    }
}
