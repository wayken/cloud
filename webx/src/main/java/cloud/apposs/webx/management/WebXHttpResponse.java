package cloud.apposs.webx.management;

import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.SseEmitter;

import java.io.File;
import java.io.IOException;

public interface WebXHttpResponse {
    /**
     * 获取响应状态码
     */
    String getStatus();

    /**
     * 添加响应头部数据
     */
    void putHeader(String key, String value);

    /**
     * 设置响应状态码
     */
    void setStatus(HttpStatus status);

    /**
     * 获取响应Content-Type
     */
    String getContentType();

    /**
     * 设置响应Content-Type
     */
    void setContentType(String contentType);

    /**
     * 响应字符串
     *
     * @param content 响应字符串
     * @param flush 是否主动触发发送事件
     */
    void write(String content, boolean flush) throws IOException;

    /**
     * 响应字节数据
     *
     * @param content 响应字节数据
     * @param flush 是否主动触发发送事件
     */
    void write(byte[] content, boolean flush) throws IOException;

    /**
     * 响应SSE事件数据
     *
     * @param content 响应SSE事件数据
     * @param flush 是否主动触发发送事件
     */
    void write(SseEmitter content, boolean flush) throws IOException;

    /**
     * 媒体文件响应输出，采用数据零拷贝输出到网络
     *
     * @param file 本地硬盘文件
     * @param flush 是否主动触发发送事件
     */
    void write(File file, boolean flush) throws IOException;

    /**
     * 立即刷新缓冲区数据到网络
     */
    void flush() throws IOException;
}
