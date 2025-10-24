package cloud.apposs.okhttp.netty;

import cloud.apposs.okhttp.*;
import cloud.apposs.okhttp.pool.ReactIoConnection;
import cloud.apposs.react.SafeIoSubscriber;
import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.Param;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.StringJoiner;

public final class NettyUtil {
    /**
     * 发送HTTP请求，主要在以下场景下会调用：
     * <pre>
     *     1. 如果是首次从连接池中获取连接则会调用
     *     2. 如果连接池中已经建立了连接则在触发响应式请求时会调用
     * </pre>
     * 详见{@link NettyIoConnection#call(SafeIoSubscriber)}
     */
    public static void sendRequest(ReactIoConnection connection, ChannelHandlerContext context) throws Exception {
        // 封装HTTP请求参数
        final OkRequest request = connection.getRequest();
        final HttpBuilder builder = connection.getBuilder();
        final URI uri = request.uri();
        // 生成FullHttpRequest,URI中的参数也需要加入到请求头中
        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(request.method().name()),
                uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery()));
        httpRequest.headers().set(HttpHeaderNames.HOST, WebUtil.getHost(request.uri()))
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            httpRequest.headers().set(entry.getKey(), entry.getValue());
        }
        if (!httpRequest.headers().contains(HttpHeaderNames.USER_AGENT)) {
            httpRequest.headers().set(HttpHeaderNames.USER_AGENT, OkHttpConstants.DEFAULT_USER_AGENT);
        }
        if (!httpRequest.headers().contains(HttpHeaderNames.ACCEPT)) {
            httpRequest.headers().set(HttpHeaderNames.ACCEPT, OkHttpConstants.DEFAULT_ACCEPT_ALL);
        }
        final FormEntity formEntity = request.formEntity();
        if (formEntity != null) {
            if (formEntity.getFormEncrypt() == FormEntity.FORM_ENCTYPE_GET) {
                String charset = builder.charset().name();
                StringJoiner encoder = new StringJoiner("&");
                for (Map.Entry<String, Object> parameter : formEntity.getParameters().entrySet()) {
                    encoder.add(URLEncoder.encode(parameter.getKey(), charset) + "=" + URLEncoder.encode(parameter.getValue().toString(), charset));
                }
                String parameter = uri.getRawQuery();
                if (parameter == null) {
                    parameter = encoder.toString();
                } else {
                    parameter += "&" + encoder.toString();
                }
                if (parameter.isEmpty()) {
                    httpRequest.setUri(uri.getRawPath());
                } else {
                    httpRequest.setUri(uri.getRawPath() + "?" + parameter);
                }
            } else if (formEntity.getFormEncrypt() == FormEntity.FORM_ENCTYPE_URLENCODE) {
                httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
                String charset = builder.charset().name();
                StringJoiner encoder = new StringJoiner("&");
                for (Map.Entry<String, Object> parameter : formEntity.getParameters().entrySet()) {
                    encoder.add(URLEncoder.encode(parameter.getKey(), charset) + "=" + URLEncoder.encode(parameter.getValue().toString(), charset));
                }
                ByteBuf content = Unpooled.copiedBuffer(encoder.toString(), CharsetUtil.UTF_8);
                httpRequest.content().clear().writeBytes(content);
            } else if (formEntity.getFormEncrypt() == FormEntity.FORM_ENCTYPE_JSON) {
                Param parameters = new Param();
                parameters.putAll(formEntity.getParameters());
                httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                String value = parameters.toJson(false, 0, null, false, true);
                httpRequest.content().writeBytes(value.getBytes(builder.charset()));
            }
        }
        httpRequest.headers().add(HttpHeaderNames.CONTENT_LENGTH, httpRequest.content().readableBytes());
        // 发送HTTP请求
        context.writeAndFlush(httpRequest);
    }
}
