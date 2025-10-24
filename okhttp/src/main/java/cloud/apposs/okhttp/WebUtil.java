package cloud.apposs.okhttp;

import java.net.URI;

public final class WebUtil {
    public static String getHost(URI url) {
        String host = url.getHost();
        if (host == null) {
            host = url.getAuthority();
        }
        return host;
    }
}
