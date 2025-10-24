package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

import java.net.InetSocketAddress;

/**
 * 请求远程地址，对应参数：$remote_addr
 */
public class RemoteAddressVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        InetSocketAddress address = (InetSocketAddress) request.getRemoteAddr();
        return address.getHostString();
    }
}
