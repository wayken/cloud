package cloud.apposs.discovery;

import cloud.apposs.balance.Peer;
import cloud.apposs.registry.ServiceInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestDiscovery {
    private static final String DIR = System.getProperty("user.dir") + "/target/classes/";
    private static final String ZOOKEEPER_SERVER = "172.17.2.39:2081";
    private static final String NACOS_SERVER = "192.168.5.38:8848";
    private static final String GROUP_NAME = "SERVICE_RPC";
    private static final int MODE_ZOOKEEPER = 0;
    private static final int MODE_QCONF = 1;
    private static final int MODE_FILE = 2;
    private static final int MODE_MEMORY = 3;
    private static final int MODE_NACOS = 4;

    private IDiscovery discovery;

    @Before
    public void before() throws Exception {
        int mode = 4;
        if (mode == MODE_ZOOKEEPER) {
            discovery = new ZooKeeperDiscovery(ZOOKEEPER_SERVER, "/service/providers");
        } else if (mode == MODE_QCONF) {
            discovery = new QconfDiscovery("center", "/service/providers");
        } else if (mode == MODE_FILE) {
            discovery = new FileDiscovery(DIR + "proxy.conf");
        } else if (mode == MODE_MEMORY) {
            Map<String, List<Peer>> peers = new HashMap<String, List<Peer>>();
            List<Peer> peerList = new LinkedList<Peer>();
            peerList.add(new Peer("127.0.0.1", 12001));
            peerList.add(new Peer("127.0.0.2", 12002));
            peers.put("sid1", peerList);
            discovery = new MemoryDiscovery(peers);
        } else if (mode == MODE_NACOS) {
            discovery = new NacosDiscovery(NACOS_SERVER, GROUP_NAME);
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        discovery.start();
    }

    @Test
    public void testDiscovery() throws Exception {
        ServiceInstance instance1 = discovery.choose("sid1", 854);
        ServiceInstance instance2 = discovery.choose("sid1", 854);
        ServiceInstance instance3 = discovery.choose("sid2", 854);

        Assert.assertNotNull(instance1);
        Assert.assertNotNull(instance2);
        System.out.println(instance1);
        System.out.println(instance3);
    }

    @After
    public void after() {
        if (discovery != null) {
            discovery.shutdown();
        }
    }
}
