package cloud.apposs.webx.management;

import cloud.apposs.util.FileBuffer;
import cloud.apposs.util.Param;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Map;

public interface WebXHttpRequest {
    /**
     * 获取远程请求IP，即客户端IP
     */
    SocketAddress getRemoteAddr();

    /**
     * 获取请求路径
     */
    URI getUri();

    /**
     * 获取请求方法
     */
    String getMethod();

    /**
     * 获取HTTP请求协议
     */
    String getProtocol();

    /**
     * 获取请求Header信息列表，注意请求的HEADER数据列表修改是无效，只提供只读功能
     */
    Map<String, String> getHeaders();

    /**
     * 通过Header Key获取请求Header Value
     */
    String getHeader(String key);

    /**
     * 通过Header Key获取请求Header Value，是否忽略key大小写
     */
    String getHeader(String key, boolean ignoreCase);

    /**
     * 判断请求Header是否包含指定Key
     */
    boolean isHeaderContains(String key);

    /**
     * 判断请求Header是否包含指定Key，是否忽略key大小写
     */
    boolean isHeaderContains(String key, boolean ignoreCase);

    /**
     * 获取请求参数，包括GET/POST请求传递的数据
     */
    Map<String, String> getParameters();

    /**
     * 获取指定参数Key获取请求参数值
     */
    String getParameter(String key);

    /**
     * 表单JOSN数据
     */
    Param getParam();

    /**
     * 获取表单上传文件列表数据
     */
    Map<String, FileBuffer> getFiles();

    /**
     * 获取表单文件
     */
    FileBuffer getFile(String key);

    /**
     * 获取HTTP请求HOST
     */
    String getRemoteHost();

    /**
     * 获取当前请求会话存储的一些状态值
     */
    Object getAttribute(Object key);

    Object getAttribute(Object key, Object defaultVal);

    /**
     * 获取当前请求会话存储数据列表
     */
    Map<Object, Object> getAttributes();

    /**
     * 设置当前请求会话存储的键值数据
     */
    Object setAttribute(Object key, Object value);

    /**
     * 判断当前请求会话存储数据是否包含指定Key
     */
    boolean hasAttribute(Object key);
}
