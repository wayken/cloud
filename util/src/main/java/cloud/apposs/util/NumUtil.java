package cloud.apposs.util;

import java.nio.ByteBuffer;

/**
 * 数值计算工具
 */
public final class NumUtil {
    /**
     * byte数组中取int数值，
     * 本方法适用于(低位在前，高位在后)的顺序，和{@link #intToBytes(int)}配套使用
     *
     * @param value byte数组
     */
    public static int bytesToInt(byte[] value) {
        return value[3] & 0xFF | (value[2] & 0xFF) << 8 | (value[1] & 0xFF) << 16 | (value[0] & 0xFF) << 24;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，
     * 本方法适用于(低位在前，高位在后)的顺序，和{@link #bytesToInt(byte[])}配套使用
     *
     * @param value 要转换的int值
     */
    public static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) ((value >> 24) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) (value & 0xFF)
        };
    }

    /**
     * byte数组中取long数值，
     * 本方法适用于(低位在前，高位在后)的顺序，和{@link #longToBytes(long)}配套使用
     *
     * @param value byte数组
     */
    public static long bytesToLong(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(value, 0, value.length);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * 将int数值转换为占四个字节的byte数组，
     * 和{@link #bytesToLong(byte[])}配套使用
     *
     * @param value 要转换的值
     */
    public static byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, value);
        return buffer.array();
    }
}
