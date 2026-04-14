package cloud.apposs.okhttp;

import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.MediaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
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

    // 数据响应是否结束，在SSE中，数据响应过程中一直是false，直到SSE结束的时候才为true，其他响应中为true
    private boolean completed = true;

    // 当前数据响应存储的一些状态值，可以在数据响应过程中存储一些解析状态值，供后续服务使用
    private final Map<Object, Object> attributes = new HashMap<Object, Object>(1);

    public OkResponse(URI url, int status, Map<String, String> headers, CachedFileStream buffer) {
        this.url = url;
        this.status = status;
        this.headers = headers;
        this.buffer = buffer;
    }

    /**
     * 构造一个指定内容的响应，内容类型默认为text/plain
     *
     * @param  content 响应内容
     * @return 响应对象
     */
    public static OkResponse ofContent(String content) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.TEXT_PLAIN.toString());
        return new OkResponse(null, HttpStatus.HTTP_STATUS_200.getCode(), headers, CachedFileStream.wrap(content.getBytes()));
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Object getAttribute(Object key) {
        return getAttribute(key, null);
    }

    public Object getAttribute(Object key, Object defaultVal) {
        Object attr = attributes.get(key);
        if (attr == null && defaultVal != null) {
            attr = defaultVal;
            attributes.put(key, attr);
        }
        return attr;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public Object setAttribute(Object key, Object value) {
        return attributes.put(key, value);
    }

    public boolean removeAttribute(Object key) {
        return attributes.remove(key) != null;
    }
}
