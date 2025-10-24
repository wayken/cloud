package cloud.apposs.okhttp.netty;

import cloud.apposs.okhttp.ChannelClosedException;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.okhttp.RemoteSocketAddress;
import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Proxy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP数据异步处理器
 */
public class HttpClientProcessor extends ChannelDuplexHandler {
    /** 当前连接是否已经建立 */
    private boolean connected;

    /**
     * 是否开启保持连接，当开启连接池时，此字段则为true，否则在数据接收完成之后马上连接资源
     */
    private final boolean keepAlive;

    /**
     * 响应式连接，从线程池中取出，
     * 因为每次请求时Connection均会重新设置Request参数，所以是支持同一远程服务发送不同参数
     */
    private final NettyIoConnection connection;

    public HttpClientProcessor(NettyIoConnection connection, boolean keepAlive) {
        this.connection = connection;
        this.keepAlive = keepAlive;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                SocketAddress localAddress, ChannelPromise promise) throws Exception {
        // 判断是否是改成走Proxy Pass请求转发
        RemoteSocketAddress proxyAddress = connection.getRemoteAddress();
        boolean isProxyPass = proxyAddress.proxy() == Proxy.Type.PROXYPASS;
        if (isProxyPass) {
            // 采用代理的直接请求过去即可，如果不是用代理则代表使用的是服务发现，需要替换成真实的服务发现实例
            remoteAddress = proxyAddress.address();
        }
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        connected = true;
        // 已经建立连接，设置连接句柄上下文
        connection.setContext(context);
        // 发送请求数据
        NettyUtil.sendRequest(connection, context);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        boolean isSseTransfer = false;
        try {
            if (message instanceof FullHttpResponse) {
                final OkRequest request = connection.getRequest();
                final FullHttpResponse httpResponse = (FullHttpResponse) message;
                final Map<String, String> headers = new HashMap<String, String>();
                for (Map.Entry<String, String> entry : httpResponse.headers().entries()) {
                    headers.put(entry.getKey(), entry.getValue());
                }
                IoConnectionSubscriber subscriber = (IoConnectionSubscriber) request.getAttribute(NettyIoConnection.CONTEXT_SUBSCRIBE);
                OkResponse response = new OkResponse(request.uri(),
                        httpResponse.status().code(), headers, CachedFileStream.wrap(httpResponse.content().nioBuffer()));
                subscriber.onNext(response);
                return;
            }
            // 在SSE分段响应下，第一次收到 HttpResponse（仅包含响应头），之后不断收到 HttpContent（含体内容）
            if (message instanceof HttpResponse) {
                final OkRequest request = connection.getRequest();
                HttpResponse httpResponse = (HttpResponse) message;
                final Map<String, String> headers = new HashMap<String, String>();
                for (Map.Entry<String, String> entry : httpResponse.headers().entries()) {
                    headers.put(entry.getKey(), entry.getValue());
                }
                String contentType = httpResponse.headers().get("Content-Type");
                isSseTransfer = contentType != null && contentType.toLowerCase().startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
                OkResponse response = new OkResponse(request.uri(), httpResponse.status().code(), headers, null);
                response.setCompleted(false);
                request.setAttribute(NettyIoConnection.CONTEXT_RESPONSE, response);
                return;
            }
            if (message instanceof HttpContent) {
                HttpContent content = (HttpContent) message;
                ByteBuf buffer = content.content();
                String chunk = buffer.toString(connection.getBuilder().charset());
                final OkRequest request = connection.getRequest();
                IoConnectionSubscriber subscriber = (IoConnectionSubscriber) request.getAttribute(NettyIoConnection.CONTEXT_SUBSCRIBE);
                OkResponse response = (OkResponse) request.getAttribute(NettyIoConnection.CONTEXT_RESPONSE);
                if (response == null) {
                    subscriber.onError(new IllegalStateException("remote address '" + request.url() + "' data error"));
                    return;
                }
                String contentType = response.getHeader("Content-Type", true);
                isSseTransfer = contentType != null && contentType.toLowerCase().startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
                if (content instanceof LastHttpContent) {
                    isSseTransfer = false;
                    response.setCompleted(true);
                }
                response.setStream(chunk);
                subscriber.onNext(response);
                return;
            }
        } finally {
            ReferenceCountUtil.release(message);
            if (!keepAlive && !isSseTransfer) {
                connected = false;
                context.close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        if (!connected) {
            // 主动释放的连接不做逻辑处理
            return;
        }
        final OkRequest request = connection.getRequest();
        // 该代码主要服务响应式异步调用
        IoConnectionSubscriber subscriber = (IoConnectionSubscriber) request.getAttribute(NettyIoConnection.CONTEXT_SUBSCRIBE);
        subscriber.onError(new ChannelClosedException("remote address '" + connection.getRemoteAddress().address() + "' colsed"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final OkRequest request = connection.getRequest();
        IoConnectionSubscriber subscriber = (IoConnectionSubscriber) request.getAttribute(NettyIoConnection.CONTEXT_SUBSCRIBE);
        subscriber.onError(cause);
    }
}
