package cloud.apposs.okhttp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP FORM表单数据请求
 */
public final class FormEntity {
    // 基于HTTP GET 数据提交
    public static final int FORM_ENCTYPE_GET = 0;
    // 基于HTTP Form 表单数据提交
    public static final int FORM_ENCTYPE_URLENCODE = 1;
    // 基于HTTP POST Form Data的表单数据提交
    public static final int FORM_ENCTYPE_FORMDATA = 2;
    // 基于HTTP POST JSON的表单数据提交
    public static final int FORM_ENCTYPE_JSON = 3;

    private int formEncrypt = FORM_ENCTYPE_URLENCODE;

    /**
     * 参数列表
     */
    private final Map<String, Object> parameters = new HashMap<String, Object>();

    public static FormEntity builder() {
        return new FormEntity(FORM_ENCTYPE_GET, "utf-8");
    }

    public static FormEntity builder(int formEnctype) {
        return new FormEntity(formEnctype, "utf-8");
    }

    public static FormEntity builder(int formEnctype, String charset) {
        return new FormEntity(formEnctype, charset);
    }

    private FormEntity(int formEnctype, String charset) {
        this.formEncrypt = formEnctype;
    }

    public FormEntity add(String name, Object value) throws IOException {
        parameters.put(name, value);
        return this;
    }

    public int getFormEncrypt() {
        return formEncrypt;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
