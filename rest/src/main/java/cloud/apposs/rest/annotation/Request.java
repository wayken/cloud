package cloud.apposs.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求定义
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {
    /** 请求的匹配路径列表 */
    String[] value() default {};

    /** 请求的匹配Method方法列表，默认为所有方法匹配 */
    Method[] method() default {};

    /** 请求的匹配主机，默认是所有host匹配 */
    String host() default "*";

    /** 指定返回值类型 */
    String produces() default "";

    public enum Method {
        GET, HEAD, POST, PUT, DELETE, READ, OPTIONS
    }

    /**
     * 定义 GET 请求，主要应用于数据获取业务
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.GET)
    public @interface Get {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 GET 请求和POST进行HTTP数据提交，主要应用于数据获取业务，
     * 之所以新增这么一个方法，主要是为了针对WriteCmd注释统一将GET/READ定义为读指令，其他的均写指令，
     * 使用场景示例：
     * 1、当业务需要进行数据库数据迁移时，
     * 可以先将服务指向从库，并设置微服务和从库为只读模式，参数详见readOnly，这样可以保证数据在迁移的过程中客户依然可以访问网站
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.READ)
    public @interface Read {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 POST 请求，主要应用于数据存储业务
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.POST)
    public @interface Post {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 PUT 请求，主要应用于数据更新业务
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.PUT)
    public @interface Put {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 DELETE 请求，主要应用于数据删除业务
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.DELETE)
    public @interface Delete {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 HEAD 请求
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.HEAD)
    public @interface Head {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }

    /**
     * 定义 OPTIONS 请求
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Request(method = Method.OPTIONS)
    public @interface Options {
        /** 请求的匹配路径 */
        String[] value();

        /** 请求的匹配主机，默认是所有host匹配 */
        String host() default "*";

        /** 指定返回值类型 */
        String produces() default "";
    }
}
