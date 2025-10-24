package cloud.apposs.okhttp;

import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.MediaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * HTTP请求响应
 */
public class OkResponse {
    private final URI url;

    private final int status;

    private final Map<String, String> headers;

    // 响应内容缓冲区，可能是内存数据，也可能是文件流
    private final CachedFileStream buffer;

    // SSE响应数据流
    private String stream;

    // 数据响应是否结束，在SSE中，数据响应过程中一直是false，直到SSE结束的时候才为true，其他响应中为true
    private boolean completed = true;

    public OkResponse(URI url, int status, Map<String, String> headers, CachedFileStream buffer) {
        this.url = url;
        this.status = status;
        this.headers = headers;
        this.buffer = buffer;
    }

    public URI getUrl() {
        return url;
    }

    public String getPath() {
        return url.getPath();
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return getHeader(key, false);
    }

    public String getHeader(String key, boolean ignoreCase) {
        if (ignoreCase) {
            for (String k : headers.keySet()) {
                if (k.equalsIgnoreCase(key)) {
                    return headers.get(k);
                }
            }
        }
        return headers.get(key);
    }

    /**
     * 获取响应内容，注意，每次调用此方法都会重新解析响应内容，因此建议只调用一次
     */
    public String getContent() throws IOException {
        return getContent(CharsetUtil.UTF_8);
    }

    /**
     * 获取响应内容，注意，每次调用此方法都会重新解析响应内容，因此建议只调用一次
     *
     * @param charset 内容编码
     */
    public String getContent(Charset charset) throws IOException {
        return new String(buffer.array(), charset);
    }

    public byte[] getBytes() throws IOException {
        return buffer.array();
    }

    public CachedFileStream getBuffer() throws IOException {
        return buffer;
    }

    /**
     * 判断当前响应是否是流式响应
     */
    public boolean isSseResponse() {
        String contentType = getHeader("Content-Type", true);
        return contentType != null && contentType.startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
