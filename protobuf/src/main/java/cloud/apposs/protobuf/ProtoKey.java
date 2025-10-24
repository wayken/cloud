package cloud.apposs.protobuf;

/**
 * 协议Key包装，字节码内存表现如下：
 * <pre>
 * +-------+------+
 * |  key  | type |
 * +-------+------+
 * | 00001 | 001  |
 * +-------+------+
 * </pre>
 */
public final class ProtoKey {
    /**
     * 协议类型存储位数
     */
    public static final int TAG_TYPE_BITS = 3;
    /**
     * 协议类型存储解码掩码
     */
    public static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

    /**
     * 协议Key值，存在于字节前5位
     */
    private byte key;

    /**
     * 协议字段值类型，存在于字节后3位
     */
    private byte type;

    public ProtoKey(byte value) {
        // 前5位存储的是key值/序号
        // 加上& 0xff主要目的是将value转换为byte二进制再进行移位操作，避免进行移位时高位都被补1
        // 参考：https://www.cnblogs.com/del88/p/15839209.html
        this.key = (byte) ((value & 0xff) >>> TAG_TYPE_BITS);
        // 后三位存储的是传输类型
        this.type = (byte) (value & TAG_TYPE_MASK);
    }

    /**
     * 获取协议Key值，存在于字节前5位
     */
    public byte getKey() {
        return key;
    }

    /**
     * 协议字段值类型，存在于字节后3位
     */
    public byte getType() {
        return type;
    }
}
