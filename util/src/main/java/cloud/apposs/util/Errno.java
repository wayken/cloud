package cloud.apposs.util;

/**
 * 项目错误码，
 * 参考：
 * https://mp.weixin.qq.com/s/1vIB17X_LM4isIx80LzrPg
 * https://www.kancloud.cn/onebase/ob/484204
 * https://blog.csdn.net/u013457167/article/details/79196306
 * 错误码规范：
 * 一共 7 位，分成三段
 * 第一段，1 位，类型
 * 0 - 系统级别异常
 * 1 - 业务级别异常
 * 第二段，3 位，模块
 * 001 - 用户模块
 * 002 - 商品模块
 * 003 - 订单模块
 * 004 - 支付模块
 * 第三段，3 位，错误码
 * 不限制规则，一般建议每个模块自增
 * 示例：
 * <pre>
 *     0: 操作成功
 *     0000002: 系统服务为只读
 *     1001000: 登录密码错误
 *     1001001: 密码修改失败
 * </pre>
 * 项目开发规范：
 * 1、在项目开发中注意先文档规范各业务错误码，方便前端、后端根据不同的错误输出不同的错误提示，优化客户体验，加快问题排查效率
 */
public class Errno {
    /** 成功 */
    public static final Errno OK = new Errno(0, "success");
    /** 通用错误 */
    public static final Errno ERROR = new Errno(1, "error");
    /** 参数错误 */
    public static final Errno EARGUMENT = new Errno(2, "argument error");
    /** 数据已存在 */
    public static final Errno EALREADY_EXISTS = new Errno(3, "already exists error");
    /** 服务为只读，一般为服务在线迁移中 */
    public static final Errno EREADONLY = new Errno(4, "readonly error");
    /** 请求不存在，一般为请求URL+METHOD未匹配 */
    public static final Errno ENO_HANDLER = new Errno(5, "request not found");
    /** 数据不存在，一般为请求数据不存在 */
    public static final Errno ENOT_FOUND = new Errno(6, "data not found");
    /** 不支持的操作，一般为实现类默认抛出不支持异常 */
    public static final Errno EUNSUPPORTED_OPERATION = new Errno(7, "unsupported operation");
    /** 触发了Guard熔断降级等机制给限制请求，业务可通过此错误码做前端产品功能降级 */
    public static final Errno EBLOCK = new Errno(8, "request blocked");
    /** 拦截器拦截了请求 */
    public static final Errno EACCESS_DENIED = new Errno(9, "access denied");
    /** 数据库执行异常 */
    public static final Errno SQL_ERROR = new Errno(10, "sql error");
    /** 文件不存在异常 */
    public static final Errno EFILE_NOT_FOUND = new Errno(11, "file not found");

    /**
     * 响应错误码
     */
    private final int value;

    /**
     * 错误描述
     */
    private final String description;

    public Errno(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int value() {
        return value;
    }

    public String description() {
        return description;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Errno)) {
            return false;
        }
        Errno errno = (Errno) obj;
        return value == errno.value;
    }

    @Override
    public String toString() {
        return "Errno{" +
            "value=" + value +
            ", description='" + description + '\'' +
            '}';
    }
}
