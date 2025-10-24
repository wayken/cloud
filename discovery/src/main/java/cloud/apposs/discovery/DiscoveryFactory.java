package cloud.apposs.discovery;

public class DiscoveryFactory {
    public static final String DISCOVERY_TYPE_QCONF = "QCONF";
    public static final String DISCOVERY_TYPE_FILE = "FILE";
    public static final String DISCOVERY_TYPE_ZOOKEEPER = "ZOOKEEPER";
    public static final String DISCOVERY_TYPE_NACOS = "NACOS";

    public static IDiscovery createDiscovery(String discoveryType, String... discoveryArgs) throws Exception {
        discoveryType = discoveryType.trim().toUpperCase();
        if (discoveryType.equals(DISCOVERY_TYPE_NACOS)) {
            if (discoveryArgs.length != 2) {
                throw new IllegalArgumentException();
            }
            return new NacosDiscovery(discoveryArgs[0], discoveryArgs[1]);
        } else if (discoveryType.equals(DISCOVERY_TYPE_QCONF)) {
            if (discoveryArgs.length != 2) {
                throw new IllegalArgumentException();
            }
            return new ZooKeeperDiscovery(discoveryArgs[0], discoveryArgs[1]);
        } else if (discoveryType.equals(DISCOVERY_TYPE_ZOOKEEPER)) {
            if (discoveryArgs.length != 2) {
                throw new IllegalArgumentException();
            }
            return new QconfDiscovery(discoveryArgs[0], discoveryArgs[1]);
        } else if (discoveryType.equals(DISCOVERY_TYPE_FILE)) {
            if (discoveryArgs.length != 1) {
                throw new IllegalArgumentException();
            }
            return new FileDiscovery(discoveryArgs[0]);
        }
        return null;
    }
}
