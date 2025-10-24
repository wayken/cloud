package cloud.apposs.bootor.netty;

import cloud.apposs.bootor.*;
import cloud.apposs.bootor.filter.FilterChain;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.IGuardProcess;
import cloud.apposs.rest.IHandlerProcess;
import cloud.apposs.rest.Restful;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

public class ApplicationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final BootorConfig config;

    private final NettyApplicationContext context;

    public ApplicationHandler(BootorConfig config, NettyApplicationContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception {
        NettyHttpRequest request = new NettyHttpRequest(context.channel().remoteAddress(), fullHttpRequest);
        NettyHttpResponse response = new NettyHttpResponse(context);
        // 解析请求参数
        NettyUtil.parseRequestParameter(fullHttpRequest, request, config);
        // 处理请求过滤链过滤
        FilterChain filterChain = this.context.getFilterChain();
        boolean success = filterChain.filter(request, response);
        if (!success) {
            return;
        }
        // 处理请求
        Restful<BootorHttpRequest, BootorHttpResponse> restful = this.context.getRestful();
        restful.renderView(new BootorHandlerProcess(), request, response);
    }

    private class BootorHandlerProcess implements IHandlerProcess<BootorHttpRequest, BootorHttpResponse> {
        @Override
        public String getRequestMethod(BootorHttpRequest request, BootorHttpResponse response) {
            return request.getMethod();
        }

        @Override
        public String getRequestPath(BootorHttpRequest request, BootorHttpResponse response) {
            return WebUtil.getRequestPath(request);
        }

        @Override
        public String getRequestHost(BootorHttpRequest request, BootorHttpResponse response) {
            return request.getRemoteHost();
        }

        @Override
        public void processVariable(BootorHttpRequest request, BootorHttpResponse response, Map<String, String> variables) {
            if (variables != null) {
                request.setAttribute(BootorConstants.REQUEST_ATTRIBUTE_VARIABLES, variables);
            }
        }

        @Override
        public void processHandler(BootorHttpRequest request, BootorHttpResponse response, Handler handler) {
            String produces = handler.getProduces();
            if (produces != null && !produces.isEmpty()) {
                response.setContentType(produces);
            }
        }

        @Override
        public IGuardProcess<BootorHttpRequest, BootorHttpResponse> getGuardProcess() {
            return context.getGuard();
        }

        @Override
        public void markAsync(BootorHttpRequest request, BootorHttpResponse response) {
            // do nothing
        }
    }
}
