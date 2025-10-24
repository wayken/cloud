package cloud.apposs.util;

import java.util.List;
import java.util.Map;

/**
 * 数据结果集处理工具
 */
public final class ResultSets {
    /**
     * 从结果集中获取指定Key数据并添加到新的集合里
     *
     * @param result 原始结果集
     * @param key    要获取的结果集Key
     */
    @SuppressWarnings("unchecked")
    public static <T> Table<T> assign(Table<Param> result, String key) {
        Table<T> response = Table.builder();
        if (result != null) {
            for (Param param : result) {
                T value = (T) param.getObject(key);
                if (value != null && !response.contains(value)) {
                    response.add(value);
                }
            }
        }
        return response;
    }

    /**
     * 从结果集中寻找指定Key的值是否满足指定的Value
     * @param result 原始结果集
     * @param key    要获取的结果集Key
     * @param value  匹配的值
     */
    public static Param filter(Table<Param> result, String key, Object value) {
        for (Param param : result) {
            Object pvalue = param.getObject(key);
            if (value.equals(pvalue)) {
                return param;
            }
        }
        return null;
    }

    public static boolean contains(Table<Param> result, String key, Object value) {
        for (Param param : result) {
            Object pvalue = param.getObject(key);
            if (value.equals(pvalue)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(Param value) {
        return value == null || value.isEmpty();
    }

    public static boolean isEmpty(Table<?> value) {
        return value == null || value.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    public static boolean isEmpty(List<?> value) {
        return value == null || value.isEmpty();
    }
}
