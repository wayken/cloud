package cloud.apposs.cachex.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import cloud.apposs.util.Param;
import cloud.apposs.protobuf.ProtoField;

/**
 * SQL UPDATE更新
 */
public class Updater {
    public static final String UPDATE_LAND = "&";    // logic and:&
    public static final String UPDATE_LOR = "|";    // logic or:|
    public static final String UPDATE_INC = "+";    // increase
    public static final String UPDATE_DEC = "-";    // decrease
    public static final String UPDATE_MUL = "*";    // multiply

    protected final Where where = new Where();

    protected final List<Data> dataList = new LinkedList<Data>();

    public static Updater builder() {
        return new Updater();
    }

    public static Updater builder(String key, Object value) {
        return new Updater(key, value);
    }

    public static Updater builder(String key, String operation, Object value) {
        return new Updater(key, operation, value);
    }

    public static Updater builder(Param data) {
        return new Updater(data);
    }

    /**
     * 创建更新
     */
    public Updater() {
    }

    /**
     * 创建更新
     *
     * @param key   更新的字段
     * @param value 更新的值
     */
    public Updater(String key, Object value) {
        this(key, null, value);
    }

    /**
     * 创建更新
     *
     * @param key       更新的字段
     * @param operation 更新操作，可以为{@link Updater#UPDATE_LAND}、&、+等
     * @param value     更新的值
     */
    public Updater(String key, String operation, Object value) {
        add(key, operation, value);
    }

    /**
     * 创建更新
     *
     * @param data 更新的字段数据，可以传递{@link Entity}
     */
    public Updater(Param data) {
        addAll(data);
    }

    /**
     * 添加更新，
     * 禁止要更新的值传递为null，如果需要指定更新为null将value改成{@link cloud.apposs.util.Null}对象，否则不会添加更新
     *
     * @param key   更新的字段
     * @param value 更新的值
     */
    public Updater add(String key, Object value) {
        return add(key, null, value);
    }

    /**
     * 添加更新
     * 禁止要更新的值传递为null，如果需要指定更新为null将value改成{@link cloud.apposs.util.Null}对象，否则不会添加更新
     *
     * @param key   更新的字段
     * @param value 更新的值
     * @param codec 更新值对应的ProtoBuf编码解码器，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     */
    public Updater add(String key, Object value, ProtoField<?> codec) {
        return add(key, null, value, codec);
    }

    /**
     * 添加更新，
     * 禁止要更新的值传递为null，如果需要指定更新为null将value改成{@link cloud.apposs.util.Null}对象，否则不会添加更新
     *
     * @param key       更新的字段
     * @param operation 更新操作，可以为{@link Updater#UPDATE_LAND}、&、+等
     * @param value     更新的值
     */
    public Updater add(String key, String operation, Object value) {
        if (value != null) {
            dataList.add(new Data(key, operation, value, null));
        }
        return this;
    }

    /**
     * 添加更新
     *
     * @param key       更新的字段
     * @param operation 更新操作，可以为{@link Updater#UPDATE_LAND}、&、+等
     * @param value     更新的值
     * @param codec     更新值对应的ProtoBuf编码解码器，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     */
    public Updater add(String key, String operation, Object value, ProtoField<?> codec) {
        if (value != null) {
            dataList.add(new Data(key, operation, value, codec));
        }
        return this;
    }

    public Updater addAll(Param data) {
        for (Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                dataList.add(new Data(entry.getKey(), null, entry.getValue(), null));
            }
        }
        return this;
    }

    public Where where() {
        return where;
    }

    public Where where(Where where) {
        for (Where.Condition condition : where.getConditionList()) {
            this.where.add(condition);
        }
        return this.where;
    }

    /**
     * Where与查询
     *
     * @param key       查询字段
     * @param operation 查询操作，可以为=、>=、<=等操作
     * @param value     查询的值
     */
    public Where where(String key, String operation, Object value) {
        return where.and(key, operation, value);
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public boolean isEmpty() {
        return dataList.isEmpty();
    }

    /**
     * 更新字段封装
     */
    public static class Data {
        /**
         * 更新字段
         */
        private final String key;

        /**
         * 更新操作，等于/异或，可为空，默认为等于
         */
        private final String operation;

        /**
         * 更新的值
         */
        private final Object value;

        /**
         * 要更新字段数值编码解码器，主要服务于NOSQL二进制
         */
        private final ProtoField<?> codec;

        public Data(String key, String operation, Object value, ProtoField<?> codec) {
            this.key = key;
            this.operation = operation;
            this.value = value;
            this.codec = codec;
        }

        public String getKey() {
            return key;
        }

        public String getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }

        public ProtoField<?> getCodec() {
            return codec;
        }
    }
}
