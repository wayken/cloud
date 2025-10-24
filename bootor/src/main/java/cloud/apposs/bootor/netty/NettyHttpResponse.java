package cloud.apposs.bootor.netty;

import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.SseEmitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class NettyHttpResponse implements BootorHttpResponse {
    /**
     * 网络句柄
     */
    private final ChannelHandlerContext context;

    /**
     * 响应头部
     */
    private final HttpHeaders headers = new DefaultHttpHeaders();

    /**
     * HTTP版本协议
     */
    private HttpVersion version = HttpVersion.HTTP_1_1;

    /**
     * 响应状态码
     */
    private HttpResponseStatus status = HttpResponseStatus.OK;

    /**
     * 响应编码
     */
    private Charset charset = CharsetUtil.UTF_8;

    /**
     * 是否已经写入响应头，主要应用于SSE响应场景
     */
    private boolean isHeaderWritten = false;

    public NettyHttpResponse(ChannelHandlerContext context) {
        this.context = context;
    }

    @Override
    public String getStatus() {
        return String.valueOf(status.code());
    }

    @Override
    public void putHeader(String key, String value) {
        headers.add(key, value);
    }

    @Override
    public void setStatus(HttpStatus status) {
        this.status = HttpResponseStatus.valueOf(status.getCode());
    }

    @Override
    public String getContentType() {
        return headers.get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public void setContentType(String contentType) {
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    @Override
    public void write(String content, boolean flush) {
        ByteBuf buffer = Unpooled.copiedBuffer(content, charset);
        int bufferLength = buffer.readableBytes();
        DefaultHttpResponse response = new DefaultFullHttpResponse(version, status, buffer, headers, new DefaultHttpHeaders());
        HttpUtil.setContentLength(response, bufferLength);
        doWrite(response, flush);
    }

    @Override
    public void write(byte[] content, boolean flush) throws IOException {
        ByteBuf buffer = Unpooled.copiedBuffer(content);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(version, status, buffer, headers, new DefaultHttpHeaders());
        HttpUtil.setContentLength(response, content.length);
        doWrite(response, flush);
    }

    @Override
    public void write(SseEmitter content, boolean flush) throws IOException {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        if (!isHeaderWritten) {
            isHeaderWritten = true;
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_EVENT_STREAM + "; charset=" + charset);
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
            response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            context.write(response);
        }
        ByteBuf message = Unpooled.copiedBuffer(content.build(), charset);
        if (flush) {
            context.writeAndFlush(new DefaultHttpContent(message));
            if (content.isDone()) {
                context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            context.write(new DefaultHttpContent(message));
            if (content.isDone()) {
                context.write(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void write(File file, boolean flush) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long fileLength = raf.length();
        HttpResponse response = new DefaultHttpResponse(version, status, headers);
        HttpUtil.setContentLength(response, fileLength);
        HttpUtil.setKeepAlive(response, true);
        context.write(response);
        FileRegion fileRegion = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
        context.write(fileRegion, context.newProgressivePromise());
        if (flush) {
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        } else {
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }
    }

    private void doWrite(Object content, boolean flush) {
        if (flush) {
            context.writeAndFlush(content).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    // 判断当前请求是否有keep-alive标志
                    if (!HttpUtil.isKeepAlive((FullHttpResponse) content)) {
                        // 如果没有keep-alive标志，则关闭连接
                        context.close();
                    }
                }
            });
        } else {
            context.write(content);
        }
    }

    @Override
    public void flush() throws IOException {
        context.flush();
    }
}
