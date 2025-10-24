package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

import java.net.InetSocketAddress;

/**
 * 请求远程端口，对应参数：$remote_port
 */
public class RemotePortVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        InetSocketAddress address = (InetSocketAddress) request.getRemoteAddr();
        return String.valueOf(address.getPort());
    }
}
