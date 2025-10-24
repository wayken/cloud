package cloud.apposs.webx.management.netty;

import cloud.apposs.util.Encoder;
import cloud.apposs.util.FileBuffer;
import cloud.apposs.util.Param;
import cloud.apposs.util.StrUtil;
import cloud.apposs.webx.management.WebXHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyHttpRequest implements WebXHttpRequest {
    /**
     * 远程请求IP，即客户端IP
     */
    private final SocketAddress remoteAddr;

    /**
     * 请求路径
     */
    private final URI uri;

    /**
     * 请求HEADER
     */
    private final HttpHeaders headers;

    /**
     * 请求方法
     */
    private final HttpMethod method;

    /**
     * 表单字段数据，支持GET/POST/FORM-URL/FORM-DATA-FIELD
     */
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * 表单文件上传字段，数据结构为[FileName:FileItem]
     */
    private final Map<String, FileBuffer> files = new HashMap<String, FileBuffer>();

    /**
     * 当前会话请求存储的一些状态值
     */
    private final Map<Object, Object> attributes = new ConcurrentHashMap<Object, Object>(1);

    /**
     * 表单JOSN数据，
     * application/json 类型的请求比较特殊，数据是一个JSON对象，所以无法单纯用parameters来储存
     */
    private final Param param = new Param();

    private final String protocol;
    private String remoteHost;

    public NettyHttpRequest(SocketAddress remoteAddr, FullHttpRequest request) {
        this.remoteAddr = remoteAddr;
        this.uri = URI.create(request.uri());
        this.headers = request.headers();
        this.method = request.method();
        this.protocol = request.protocolVersion().protocolName();
    }

    @Override
    public SocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String getMethod() {
        return method.name();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : headers.entries()) {
            headerMap.put(entry.getKey(), entry.getValue());
        }
        return headerMap;
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String getHeader(String key, boolean ignoreCase) {
        if (ignoreCase) {
            for (String k : headers.names()) {
                if (k.equalsIgnoreCase(key)) {
                    return headers.get(k);
                }
            }
        }
        return headers.get(key);
    }

    @Override
    public boolean isHeaderContains(String key) {
        return isHeaderContains(key, false);
    }

    @Override
    public boolean isHeaderContains(String key, boolean ignoreCase) {
        if (ignoreCase) {
            for (String k : headers.names()) {
                if (k.equalsIgnoreCase(key)) {
                    return true;
                }
            }
            return false;
        } else {
            return headers.contains(key);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value != null) {
            return Encoder.decodeUrl(value);
        }
        return value;
    }

    @Override
    public Param getParam() {
        return param;
    }

    @Override
    public Map<String, FileBuffer> getFiles() {
        return files;
    }

    @Override
    public FileBuffer getFile(String key) {
        return files.get(key);
    }

    @Override
    public String getRemoteHost() {
        if (StrUtil.isEmpty(remoteHost)) {
            remoteHost = getHeader("host", true);
        }
        return remoteHost;
    }

    @Override
    public Object getAttribute(Object key) {
        return getAttribute(key, null);
    }

    @Override
    public Object getAttribute(Object key, Object defaultVal) {
        Object attr = attributes.get(key);
        if (attr == null && defaultVal != null) {
            attr = defaultVal;
            attributes.put(key, attr);
        }
        return attr;
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Object setAttribute(Object key, Object value) {
        return attributes.put(key, value);
    }

    @Override
    public boolean hasAttribute(Object key) {
        return false;
    }
}
