package cloud.apposs.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Json Key-Value结构容器，对Map的进一步封装，提供
 * <pre>
 *     1. 不同数据类型的获取
 *     2. JSON数据的解析
 *     3. JSON数据输出的HTML编码，避免XSS攻击
 * </pre>
 * 注意是默认非线程安全，如果需要线程安全需要传递SYNC参数
 */
public class Param implements Map<String, Object> {
    public static final String DATE_PATTERN_DEFAULT = "yyyy-MM-dd";

    private final Map<String, Object> datas;

    public static Param builder() {
        return new Param(false, null);
    }

    public static Param builder(boolean sync, Param datas) {
        return new Param(sync, datas);
    }

    public static Param builder(String key, Object value) {
        return builder().setObject(key, value);
    }

    public static Param builder(Map<String, Object> value) {
        return new Param(false, value);
    }

    public static Param builder(String primary, List<Param> values) {
        return Param.builder(primary, values, false);
    }

    /**
     * 将Param集合列表映射成统一的PrimaryKey->List<Param>方便快速通过索引KEY查找对应数据集合
     *
     * @param primary 要建立的索引KEY
     * @param values 原始数据列表
     */
    public static Param builder(String primary, List<Param> values, boolean sync) {
        Param datas = new Param(sync);
        for (Param value : values) {
            Object key = value.getObject(primary);
            if (key == null) {
                throw new IllegalArgumentException("No Match Key '" + primary + "' in Data List");
            }
            datas.put(key.toString(), value);
        }
        return datas;
    }

    /**
     * 从源Param中提取指定的KEY列表，构建新的Param对象
     *
     * @param source 源Param对象
     * @param keys 要提取的KEY列表
     * @return 新的Param对象
     */
    public static Param builder(Param source, String... keys) {
        Param datas = new Param(false);
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                datas.put(key, value);
            }
        }
        return datas;
    }

    public Param() {
        this(false, null);
    }

    public Param(boolean sync) {
        this(sync, null);
    }

    public Param(Param datas) {
        this(false, datas);
    }

    public Param(Map<String, Object> datas) {
        this(false, datas);
    }

    public Param(boolean sync, Param datas) {
        if (sync) {
            this.datas = new ConcurrentHashMap<String, Object>();
        } else {
            this.datas = new HashMap<String, Object>();
        }
        if (datas != null) {
            this.datas.putAll(datas);
        }
    }

    public Param(boolean sync, Map<String, Object> datas) {
        if (sync) {
            this.datas = new ConcurrentHashMap<String, Object>();
        } else {
            this.datas = new HashMap<String, Object>();
        }
        if (datas != null) {
            this.datas.putAll(datas);
        }
    }

    @Override
    public Object get(Object key) {
        return datas.get(key);
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseBoolean((String) value);
        }
        return (Boolean) value;
    }

    public Param setBoolean(String key, Boolean value) {
        put(key, value);
        return this;
    }

    public Byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    public Byte getByte(String key, byte defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseByte((String) value);
        } else if (value instanceof Integer) {
            return ((Integer) value).byteValue();
        } else if (value instanceof Long) {
            return ((Long) value).byteValue();
        }
        return (Byte) value;
    }

    public Param setBoolean(String key, byte value) {
        put(key, value);
        return this;
    }

    public Integer getInt(String key) {
        return getInt(key, null);
    }

    public Integer getInt(String key, Integer defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseInt((String) value);
        }
        return (Integer) value;
    }

    public Param setInt(String key, Integer value) {
        put(key, value);
        return this;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);
        if (!(value instanceof String)) {
            return defaultValue;
        }
        if (StrUtil.isEmpty((String) value) && defaultValue != null) {
            return defaultValue;
        }
        return (String) value;
    }

    public String getHtmlString(String key) {
        return getHtmlString(key, null);
    }

    /**
     * 获取经过Html编码的字符串，如果是在要写在HTML页面上的，务必使用此函数，而不要使用getString
     */
    public String getHtmlString(String key, String defaultValue) {
        String value = getString(key, defaultValue);
        if (value == null) {
            return null;
        }
        return Encoder.encodeHtml(value);
    }

    public Param setString(String key, String value) {
        put(key, value);
        return this;
    }

    public RichText getRichText(String key) {
        return getRichText(key, null);
    }

    public RichText getRichText(String key, RichText defaultValue) {
        Object value = get(key);
        if (!(value instanceof RichText)) {
            return null;
        }
        if (StrUtil.isEmpty(value.toString())) {
            return defaultValue;
        }
        return (RichText) value;
    }

    public Param setRichText(String key, RichText value) {
        put(key, value);
        return this;
    }

    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Long getLong(String key, Long defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String || value instanceof Integer) {
            return Parser.parseLong(value.toString());
        }
        return (Long) value;
    }

    public Param setLong(String key, Long value) {
        put(key, value);
        return this;
    }

    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    public Double getDouble(String key, Double defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseDouble((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public Param setDouble(String key, Double value) {
        put(key, value);
        return this;
    }

    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    public Float getFloat(String key, Float defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseFloat((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }

    public Param setFloat(String key, Float value) {
        put(key, value);
        return this;
    }

    public Short getShort(String key) {
        return getShort(key, null);
    }

    public Short getShort(String key, Short defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return Parser.parseShort((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return defaultValue;
    }

    public Param setShort(String key, Short value) {
        put(key, value);
        return this;
    }

    public byte[] getBytes(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return (byte[]) value;
    }

    public Param setBytes(String key, byte[] value) {
        put(key, value);
        return this;
    }

    public BigDecimal getBigDecimal(String key) {
        return getBigDecimal(key, null);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return defaultValue;
    }

    public Param setBigDecimal(String key, BigDecimal value) {
        put(key, value);
        return this;
    }

    public Map<String, Object> getMap(String key) {
        return getMap(key, null);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key, Param defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Map<String, Object>) value;
    }

    public Map<String, Object> getMapWithoutNull(String key) {
        return getMapWithoutNull(key, false);
    }

    public Map<String, Object> getMapWithoutNull(String key, boolean sync) {
        Param value = getParam(key);
        if (value == null) {
            return new Param(sync);
        }
        return value;
    }

    public Param setMap(String key, Map<String, Object> value) {
        put(key, value);
        return this;
    }

    public Param getParam(String key) {
        return getParam(key, null);
    }

    public Param getParam(String key, Param defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Param) value;
    }

    public Param getParamWithoutNull(String key) {
        return getParamWithoutNull(key, false);
    }

    public Param getParamWithoutNull(String key, boolean sync) {
        Param value = getParam(key);
        if (value == null) {
            return new Param(sync);
        }
        return value;
    }

    public Param setParam(String key, Param value) {
        put(key, value);
        return this;
    }

    public Param setParam(String key, Map<String, Object> value) {
        put(key, value);
        return this;
    }

    public <T> Table<T> getTable(String key) {
        return getTable(key, null);
    }

    public <T> Table<T> getTableWithoutNull(String key) {
        return getTableWithoutNull(key, false);
    }

    public <T> Table<T> getTableWithoutNull(String key, boolean sync) {
        Table<T> value = getTable(key);
        if (value == null) {
            return new Table<T>(sync);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> Table<T> getTable(String key, Table<T> defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Table<T>) value;
    }

    public <T> Param setTable(String key, Table<T> value) {
        put(key, value);
        return this;
    }

    public <T> List<T> getList(String key) {
        return getList(key, null);
    }

    public <T> List<T> getListWithoutNull(String key) {
        List<T> value = getList(key);
        if (value == null) {
            return new ArrayList<T>();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, List<T> defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return (List<T>) value;
    }

    public <T> Param setList(String key, List<T> value) {
        put(key, value);
        return this;
    }

    public ByteBuffer getBuffer(String key) {
        return getBuffer(key, null);
    }

    public ByteBuffer getBuffer(String key, ByteBuffer defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof byte[]) {
            return ByteBuffer.wrap((byte[]) value);
        }
        return (ByteBuffer) value;
    }

    public Param setBuffer(String key, ByteBuffer value) {
        put(key, value);
        return this;
    }

    public Calendar getCalendar(String key) {
        return getCalendar(key, null);
    }

    public Calendar getCalendar(String key, Calendar defaultValue) {
        return getCalendar(key, DATE_PATTERN_DEFAULT, defaultValue);
    }

    public Calendar getCalendar(String key, String format, Calendar defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            Calendar newValue = Calendar.getInstance();
            newValue.setTimeInMillis(getLong(key));
            return newValue;
        }
        if (value instanceof String) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                Date date = dateFormat.parse((String) value);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            } catch (ParseException e) {
                return defaultValue;
            }
        }
        return (Calendar) value;
    }

    public Param setCalendar(String key, Calendar value) {
        put(key, value);
        return this;
    }

    public Date getDate(String key, Date defaultValue) {
        return getDate(key, DATE_PATTERN_DEFAULT, defaultValue);
    }

    public Date getDate(String key, String format, Date defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return new Date(getLong(key));
        }
        if (value instanceof String) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse((String) value);
            } catch (ParseException e) {
                return defaultValue;
            }
        }
        return (Date) value;
    }

    public Param setDate(String key, Date value) {
        put(key, value);
        return this;
    }

    public Object getObject(String key) {
        return getObject(key, null);
    }

    public Object getObject(String key, Object defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Param setObject(String key, Object value) {
        put(key, value);
        return this;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            return null;
        }
        return datas.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        datas.putAll(m);
    }

    @Override
    public Collection<Object> values() {
        return datas.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return datas.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return datas.isEmpty();
    }

    @Override
    public int size() {
        return datas.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return datas.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return datas.containsValue(value);
    }

    @Override
    public Set<String> keySet() {
        return datas.keySet();
    }

    @Override
    public Object remove(Object key) {
        return datas.remove(key);
    }

    /**
     * 把param的key数据赋值到当前数据集上，如果有相同字段的则替换
     * 注意：替换的值如果是对象则是对象的引用，外部修改的是该对象的地址引用而非克隆后的数值
     *
     * @return 替换成功返回true
     */
    public boolean assign(Param param, String key) {
        if (param == null) {
            return false;
        }
        Object value = param.getObject(key);
        if (value == null) {
            return false;
        }

        setObject(key, value);
        return true;
    }

    /**
     * 把param的key数据赋值到当前数据集上，如果有相同字段的则用指定key替换
     * 注意：替换的值如果是对象则是对象的引用，外部修改的是该对象的地址引用而非克隆后的数值
     *
     * @return 替换成功返回true
     */
    public boolean assign(Param param, String key, String newKey) {
        if (param == null) {
            return false;
        }
        Object value = param.getObject(key);
        if (value == null) {
            return false;
        }

        setObject(newKey, value);
        return true;
    }

    /**
     * 把param的key数据赋值到当前数据集上，如果有相同字段的则替换
     * 注意：替换的值如果是对象则是对象的引用，外部修改的是该对象的地址引用而非克隆后的数值
     *
     * @return 替换成功返回true
     */
    public boolean assign(Param param, String... keys) {
        if (param == null || keys == null || keys.length == 0) {
            return false;
        }
        boolean result = false;
        for (String key : keys) {
            Object value = param.getObject(key);
            if (value != null) {
                setObject(key, value);
                result = true;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return toJson(false, 0, null, false);
    }

    public String toJson() {
        return toJson(false, 0, null, false);
    }

    public String toJson(boolean format) {
        return toJson(format, 0, null, false);
    }

    public String toJson(boolean format, String line) {
        return toJson(format, 0, line, false);
    }

    public String toHtmlJson(){
        return toJson(false, 0, null, true);
    }

    public String toJson(boolean format, int tab, String line, boolean encode) {
        return toJson(format, tab, line, encode, false);
    }

    /**
     * 将Param输出成Json格式
     *
     * @param format 是否格式化输出
     * @param tab    制表符缩进
     * @param line   换行，Linux是\n，Windows是\r\n
     * @param encode 是否HTML JSON内容，免客户端利用xss攻击在输出时变成HTML标签给黑客利用来攻击
     * @param ignoreNull 如果解析值为空是否不赋值对应Key -> Value，默认为不忽略
     */
    public String toJson(boolean format, int tab, String line, boolean encode, boolean ignoreNull) {
        StringBuilder info = new StringBuilder(512);
        if (!format) {
            info.append("{");
            int count = 0, total = datas.size();
            for (Entry<String, Object> entry : datas.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String jsonValue = JsonUtil.toJson(value, format, tab, line, encode);
                if (jsonValue != null || !ignoreNull) {
                    info.append("\"").append(key).append("\":").append(jsonValue);
                    if (++count < total) {
                        info.append(",");
                    }
                }
            }
            info.append("}");
        } else {
            if (StrUtil.isEmpty(line)) {
                line = "\n";
            }
            String tab1 = "";
            for (int t = 0; t < tab; t++) {
                tab1 += "  ";
            }
            String tab2 = tab1 + "  ";

            info.append(tab1).append("{").append(line);
            int count = 0, total = datas.size();
            for (Entry<String, Object> entry : datas.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String jsonValue = JsonUtil.toJson(value, format, tab + 1, line, encode);
                if (jsonValue != null || !ignoreNull) {
                    info.append(tab2).append("\"").append(key).append("\": ").append(jsonValue);
                    if (++count < total) {
                        info.append(",").append(line);
                    } else {
                        info.append(line);
                    }
                }
            }
            info.append(tab1).append("}");
        }
        return info.toString();
    }

    @Override
    public void clear() {
        datas.clear();
    }
}
