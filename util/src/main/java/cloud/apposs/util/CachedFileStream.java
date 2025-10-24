package cloud.apposs.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;

/**
 * 缓存文件流，主要存储网络上传的文件数据，原理实现如下：
 * <pre>
 *  1. 判断存储的字节码是否超过最大字节数，小于则存储于内存，大于则存储于文件
 *  2. 存内存服务于小文件网络传输，存储临时文件服务于大文件网络传输，减少JVM内存压力
 * </pre>
 */
public final class CachedFileStream implements AutoCloseable {
    public static final int DEFAULT_THRESHOLD = Integer.MAX_VALUE;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final String DEFAULT_TMP_FILE_PREFIX = "cached_";
    public static final String DEFAULT_TMP_FILE_SUFFIX = "";

    /**
     * 写入文件的最大字节阀值，当写入的字节超过该阀值则存储于文件
     */
    private final int threshold;

    /**
     * 是否存储于内存中
     */
    private volatile boolean inMemory = true;

    /**
     * 字节流
     */
    private OutputStream stream;

    /**
     * 数据存储文件
     */
    private File file;
    /**
     * 临时文件存储相关配置
     */
    private final String prefix;
    private final String suffix;
    private final File tempDir;

    /**
     * 写入的字节数
     */
    private long written;

    public CachedFileStream() {
        this(DEFAULT_THRESHOLD, null, null, null);
    }

    /**
     * 将字节数组包装成CachedFileStream，此时字节数组会存储于内存中
     */
    public static CachedFileStream wrap(byte[] buffer) throws IOException {
        CachedFileStream cachedFileStream = new CachedFileStream(
                DEFAULT_THRESHOLD, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, null);
        cachedFileStream.write(buffer, 0, buffer.length);
        return cachedFileStream;
    }

    /**
     * 将ByteBuffer包装成CachedFileStream，此时字节数组会存储于内存中
     */
    public static CachedFileStream wrap(ByteBuffer buffer) throws IOException {
        CachedFileStream cachedFileStream = new CachedFileStream(
                DEFAULT_THRESHOLD, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, null);
        cachedFileStream.write(buffer);
        return cachedFileStream;
    }

    /**
     * 将文件包装成CachedFileStream，此时文件会存储于文件中
     */
    public static CachedFileStream wrap(File buffer) throws IOException {
        FileOutputStream fos = new FileOutputStream(buffer, true);
        CachedFileStream cachedFileStream = new CachedFileStream(
                DEFAULT_THRESHOLD, fos, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, null);
        cachedFileStream.inMemory = false;
        cachedFileStream.file = buffer;
        cachedFileStream.written += buffer.length();
        return cachedFileStream;
    }

    public static CachedFileStream wrap(InputStream buffer) throws IOException {
        CachedFileStream cachedFileStream = new CachedFileStream(
                DEFAULT_THRESHOLD, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, null);
        cachedFileStream.write(buffer);
        return cachedFileStream;
    }

    public CachedFileStream(File tempDir) {
        this(DEFAULT_THRESHOLD, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, tempDir);
    }

    public CachedFileStream(int threshold, File tempDir) {
        this(threshold, DEFAULT_TMP_FILE_PREFIX, DEFAULT_TMP_FILE_SUFFIX, tempDir);
    }

    public CachedFileStream(int threshold, String prefix, File tempDir) {
        this(threshold, prefix, DEFAULT_TMP_FILE_SUFFIX, tempDir);
    }

    public CachedFileStream(int threshold, String prefix, String suffix, File tempDir) {
        this(threshold, new ByteArrayOutputStream(), prefix, suffix, tempDir);
    }

    public CachedFileStream(int threshold, OutputStream stream, String prefix, String suffix, File tempDir) {
        this.threshold = threshold;
        this.stream = stream;
        this.prefix = prefix;
        this.suffix = suffix;
        this.tempDir = tempDir;
    }

    public File getFile() {
        return file;
    }

    public boolean isThresholdExceeded() {
        return written > threshold;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    public byte[] getRawData() {
        if (isInMemory()) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
            return baos.toByteArray();
        }
        return null;
    }

    public File getRawFile() {
        if (!isInMemory()) {
            return file;
        }
        return null;
    }

    public long write(int b) throws IOException {
        checkThreshold(1);
        stream.write(b);
        written++;
        return 1;
    }

    public long write(String str) throws IOException {
        SysUtil.checkNotNull(str, "str");
        return write(str.getBytes());
    }

    public long write(String str, String charset) throws IOException {
        SysUtil.checkNotNull(str, "str");
        return write(str.getBytes(charset));
    }

    public long write(byte b[]) throws IOException {
        checkThreshold(b.length);
        stream.write(b);
        written += b.length;
        return b.length;
    }

    public long write(byte b[], int off, int len) throws IOException {
        checkThreshold(len);
        stream.write(b, off, len);
        written += len;
        return len;
    }

    public long write(ByteBuffer src) throws IOException {
        return write(src, src.position(), src.limit());
    }

    public long write(ByteBuffer src, int off, int len) throws IOException {
        checkThreshold(len);
        for (int i = off; i < len; i++) {
            stream.write(src.get(i));
        }
        written += len;
        return len;
    }

    public long write(File src) throws IOException {
        long len = src.length();
        checkThreshold(len);
        FileInputStream fis = null;
        try {
            int n;
            long count = 0;
            fis = new FileInputStream(src);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while ((n = fis.read(buffer)) != -1) {
                stream.write(buffer, 0, n);
                count += n;
            }
            written += count;
            return count;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public long write(InputStream src) throws IOException {
        long len = src.available();
        checkThreshold(len);
        int n;
        long count = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while ((n = src.read(buffer)) != -1) {
            stream.write(buffer, 0, n);
            count += n;
        }
        written += count;
        return count;
    }

    /**
     * 获取缓存文件流字节大小
     */
    public long size() {
        if (isInMemory()) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
            return baos.size();
        }
        return file.length();
    }

    /**
     * 将当前字节流拷贝到目标字节流
     */
    public long transfer(OutputStream out) throws IOException {
        if (isInMemory()) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
            int count = baos.size();
            baos.writeTo(out);
            return count;
        } else {
            FileInputStream fis = null;
            try {
                int n;
                long count = 0;
                fis = new FileInputStream(file);
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                while ((n = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                    count += n;
                }
                return count;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public long transfer(File dstFile) throws IOException {
        return transfer(dstFile, true);
    }

    /**
     * 将当前字节流零拷贝到目标文件
     *
     * @param  dstFile 目标文件
     * @param  checkExists 是否检查目标文件是否存在，避免原有文件被覆盖，默认为true
     * @return 成功写入文件的字节数
     */
    public long transfer(File dstFile, boolean checkExists) throws IOException {
        if (dstFile == null) {
            throw new IllegalArgumentException("dstFile");
        }
        if (checkExists && dstFile.exists()) {
            throw new FileAlreadyExistsException(dstFile.getPath());
        }

        if (isInMemory()) {
            FileOutputStream fos = new FileOutputStream(dstFile);
            try {
                ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
                int count = baos.size();
                baos.writeTo(fos);
                return count;
            } finally {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        } else {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel fisChannel = null;
            FileChannel fosChannel = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(dstFile);
                fisChannel = fis.getChannel();
                fosChannel = fos.getChannel();

                long trans = 0;
                long offset = 0;
                long total = fisChannel.size();
                long max = DEFAULT_BUFFER_SIZE * 1024;
                while (trans < total) {
                    // 文件之间的传输也要配置一次传输缓冲，避免一开始传输量过大也会导致OOM
                    long remain = total - trans;
                    long needTrans = max > remain ? remain : max;
                    long length = fisChannel.transferTo(offset, needTrans, fosChannel);
                    trans += length;
                    offset += length;
                }
                return trans;
            } finally {
                if (fisChannel != null) {
                    try {
                        fisChannel.close();
                    } catch (IOException ignore) {
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignore) {
                    }
                }
                if (fosChannel != null) {
                    try {
                        fosChannel.close();
                    } catch (IOException ignore) {
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public long rename(File dstFile) throws IOException {
        return rename(dstFile, true);
    }

    /**
     * 将当前字节流重命名到目标文件中，相当于是移动字节文件
     *
     * @param  dstFile 目标文件
     * @param  checkExists 是否检查目标文件是否存在，避免原有文件被覆盖，默认为true
     * @return 成功写入文件的字节数
     */
    public long rename(File dstFile, boolean checkExists) throws IOException {
        if (dstFile == null) {
            throw new IllegalArgumentException("dstFile");
        }
        if (checkExists && dstFile.exists()) {
            throw new FileAlreadyExistsException(dstFile.getPath());
        }

        if (isInMemory()) {
            FileOutputStream fos = new FileOutputStream(dstFile);
            try {
                ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
                int count = baos.size();
                baos.writeTo(fos);
                return count;
            } finally {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        } else {
            if (file.renameTo(dstFile)) {
                return file.length();
            }
            return -1;
        }
    }

    /**
     * 将当前字节流拷贝到目标字节流
     */
    public byte[] array() throws IOException {
        long length = size();
        if (length > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException();
        }
        return array(0, (int) length);
    }

    /**
     * 将当前字节流拷贝到目标字节流
     */
    public byte[] array(int offset, int length) throws IOException {
        if (isInMemory()) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
            int count = baos.size();
            length = (count - offset) > length ? length : (count - offset);
            byte[] buffer = new byte[length];
            ByteArrayInputStream bais = null;
            try {
                bais = new ByteArrayInputStream(baos.toByteArray());
                bais.read(buffer, offset, length);
                return buffer;
            } finally {
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        } else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                length = (file.length() - offset) > length ? length : (int) (file.length() - offset);
                byte[] buffer = new byte[length];
                fis.read(buffer, offset, length);
                return buffer;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public void flush() throws IOException {
        stream.flush();
    }

    /**
     * 重置缓冲状态，以应对同一个实例的数据重复读取写入
     */
    public void reset() throws IOException {
        if (isInMemory()) {
            ((ByteArrayOutputStream) stream).reset();
        } else {
            stream = new FileOutputStream(file);
        }
    }

    @Override
    public void close() {
        try {
            flush();
        } catch (IOException ignored) {
        }
        try {
            stream.close();
        } catch (IOException ignored) {
        }
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void checkThreshold(long count) throws IOException {
        if (inMemory && written + count > threshold) {
            inMemory = false;
            if (prefix != null) {
                file = File.createTempFile(prefix, suffix, tempDir);
            }
            final FileOutputStream fos = new FileOutputStream(file);
            try {
                ((ByteArrayOutputStream) stream).writeTo(fos);
            } catch (IOException e) {
                fos.close();
                throw e;
            }
            stream = fos;
        }
    }
}
