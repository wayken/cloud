package cloud.apposs.util;

/**
 * 空对象包装，主要用于框架对于特殊NULL场景下的分支处理，目前应用的场景如下：
 * 1、在Updater封装类中会进行字段NULL更新
 * 2、在Json Param解析中如果有null值则默认替换为NULL对象并在解析成字符串时替换为null
 */
public final class Null {
    private Null() {}

    public static Null builder() {
        return new Null();
    }
}
