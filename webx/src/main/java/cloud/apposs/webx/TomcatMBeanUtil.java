package cloud.apposs.webx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public final class TomcatMBeanUtil {
    public static int getTomcatPort() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("Catalina:type=Connector,port=*");
            for (ObjectName obj : mbs.queryNames(name, null)) {
                String protocol = (String) mbs.getAttribute(obj, "protocol");
                if (protocol.toLowerCase().contains("http")) {
                    return (Integer) mbs.getAttribute(obj, "port");
                }
            }
        } catch (Exception ignore) {
        }
        return -1;
    }
}
