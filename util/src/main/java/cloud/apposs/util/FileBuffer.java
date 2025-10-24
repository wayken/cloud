package cloud.apposs.util;

import java.io.File;
import java.io.IOException;

/**
 * 文件字节包装类，目前用于以下场景
 * 1. Form表单提交
 * 2. Form表单文件上传后的解析存储，底层会持续读取HTTP上传的FORM表单文件，当超过一定上传文件大小时会自动改成临时文件存储以减少JVM内存压力
 */
public class FileBuffer {
    /**
     * 传输的表单文件名称
     */
    private final String name;

    /**
     * 传输字节数据，为了减少JVM内存和数据零拷贝传输，小数据在内存，大数据在硬盘文件
     */
    private final CachedFileStream buffer;

    public static FileBuffer wrap(String name, byte[] data) throws IOException {
        CachedFileStream fileStream = CachedFileStream.wrap(data);
        return new FileBuffer(name, fileStream);
    }

    public static FileBuffer wrap(String name, File data) throws IOException {
        CachedFileStream fileStream = CachedFileStream.wrap(data);
        return new FileBuffer(name, fileStream);
    }

    public FileBuffer(String name) {
        this(name, new CachedFileStream());
    }

    public FileBuffer(String name, CachedFileStream buffer) {
        this.name = name;
        this.buffer = buffer;
    }

    public FileBuffer(String filename, int threshold, File tempDir) {
        this.name = filename;
        this.buffer = new CachedFileStream(threshold, tempDir);
    }

    public String getName() {
        return name;
    }

    public long size() {
        return buffer.size();
    }

    public byte[] getRawData() {
        return buffer.getRawData();
    }

    public byte[] array() throws IOException {
        return buffer.array();
    }

    /**
     * 添加文件数据
     */
    public void write(byte[] data) throws IOException {
        buffer.write(data);
    }

    public void write(byte[] data, int offset, int length) throws IOException {
        buffer.write(data, offset, length);
    }

    /**
     * 将表单文件数据零拷贝到指定文件
     */
    public long transfer(File dst) throws IOException {
        return buffer.transfer(dst);
    }

    /**
     * 将表单文件数据重命名
     */
    public long rename(File dst) throws IOException {
        return buffer.rename(dst);
    }

    public void delete() throws IOException {
        buffer.close();
    }
}
