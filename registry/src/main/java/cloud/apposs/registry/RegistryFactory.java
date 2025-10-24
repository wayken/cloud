package cloud.apposs.registry;

import cloud.apposs.util.FileUtil;
import cloud.apposs.util.StrUtil;

public final class RegistryFactory {
    public static final String REGISTRY_TYPE_FILE = "FILE";
    public static final String REGISTRY_TYPE_ZOOKEEPER = "ZOOKEEPER";
    public static final String REGISTRY_TYPE_CURATOR = "CURATOR";
    public static final String REGISTRY_TYPE_NACOS = "NACOS";

    public static IRegistry createRegistry(String registryType, String... args) throws Exception {
        if (StrUtil.isEmpty(registryType)) {
            return null;
        }
        registryType = registryType.trim().toUpperCase();
        if (registryType.equals(REGISTRY_TYPE_NACOS)) {
            String servers = args[0];
            String groupName = args.length > 1 ? args[1] : NacosRegistry.DEFAULT_GROUP_NAME;
            return new NacosRegistry(servers, groupName);
        } else if (registryType.equals(REGISTRY_TYPE_CURATOR)) {
            String zkServer = args[0];
            String path = args.length > 1 ? args[1] : IRegistry.DEFAULT_REGISTRY_ROOT_PATH;
            return new CuratorRegistry(zkServer, path);
        } else if (registryType.equals(REGISTRY_TYPE_ZOOKEEPER)) {
            String zkServer = args[0];
            String path = args.length > 1 ? args[1] : IRegistry.DEFAULT_REGISTRY_ROOT_PATH;
            return new ZookeeperRegistry(zkServer, path);
        } else if (registryType.equals(REGISTRY_TYPE_FILE)) {
            String file = args[0];
            FileUtil.create(file);
            return new FileRegistry(file);
        }
        return null;
    }
}
