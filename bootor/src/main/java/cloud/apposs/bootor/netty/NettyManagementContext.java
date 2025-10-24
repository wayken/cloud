package cloud.apposs.bootor.netty;

import cloud.apposs.bootor.ApplicationContext;
import cloud.apposs.bootor.BootorConfig;
import cloud.apposs.bootor.management.ManagementRestful;
import cloud.apposs.logger.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 服务管理接口，负责处理来自管理端的请求，包括健康检查、Prometheus监控、网关静态文件上传等
 */
public class NettyManagementContext {
    private final BootorConfig config;

    private final ManagementRestful restful;

    public NettyManagementContext(ApplicationContext context, BootorConfig config) throws Exception {
        this.config = config;
        this.restful = new ManagementRestful(context, config);
    }

    public ApplicationHandler newApplicationHandler() {
        return new ApplicationHandler();
    }

    public class ApplicationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception {
            NettyHttpRequest request = new NettyHttpRequest(context.channel().remoteAddress(), fullHttpRequest);
            NettyHttpResponse response = new NettyHttpResponse(context);
            // 解析请求参数
            NettyUtil.parseRequestParameter(fullHttpRequest, request, config);
            // 处理请求
            restful.route(request, response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            Logger.error(cause, "Server Management Transport From %s Internal Error by %s,", context.channel().remoteAddress(), cause.getMessage());
            context.close();
        }
    }
}
