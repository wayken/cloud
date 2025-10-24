package cloud.apposs.util;

/**
 * 富文本封装类，目前主要用于{@link Param}/{@link Table}进行html编码时直接裸码编码
 */
public class RichText {
    private final String value;

    public RichText(String value) {
        this.value = value;
    }

    public static RichText builder(String value) {
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        return new RichText(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
