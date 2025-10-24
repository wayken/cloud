package cloud.apposs.bootor;

import cloud.apposs.okhttp.FormEntity;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.rest.parameter.Parametric;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Proxy;
import cloud.apposs.util.SseEmitter;

import java.io.File;
import java.io.IOException;

public final class WebUtil {
    /**
     * 获取请求路径
     */
    public static String getRequestPath(BootorHttpRequest request) {
        return request.getUri().getPath();
    }

    /**
     * 301永久跳转，对于google等有效，可以把页面权重转移
     */
    public static void sendRedirect301(BootorHttpResponse response, String url) {
        response.putHeader("Location", url);
        response.setStatus(HttpStatus.HTTP_STATUS_301);
    }

    /**
     * 302临时跳转
     */
    public static void sendRedirect302(BootorHttpResponse response, String url) {
        response.putHeader("Location", url);
        response.setStatus(HttpStatus.HTTP_STATUS_302);
    }

    /**
     * 字符串响应输出
     */
    public static void response(BootorHttpResponse response, MediaType contentType, String content) throws IOException {
        response(response, contentType, BootorConstants.DEFAULT_CHARSET, content, false);
    }

    /**
     * 字符串响应输出
     */
    public static void response(BootorHttpResponse response, MediaType contentType,
                                String content, boolean flush) throws IOException {
        response(response, contentType, BootorConstants.DEFAULT_CHARSET, content, flush);
    }

    /**
     * 字符串响应输出
     */
    public static void response(BootorHttpResponse response, MediaType contentType,
                                String charset, String content, boolean flush) throws IOException {
        if (response.getContentType() == null) {
            response.setContentType(contentType.getType() + "; charset=" + charset);
        }
        response.write(content, flush);
    }

    /**
     * 字节码响应输出
     */
    public static void response(BootorHttpResponse response, MediaType contentType,
                                String charset, byte[] content, boolean flush) throws IOException {
        if (response.getContentType() == null) {
            response.setContentType(contentType.getType() + "; charset=" + charset);
        }
        response.write(content, flush);
    }

    public static void response(BootorHttpResponse response, String charset, SseEmitter content, boolean flush) throws IOException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE + "; charset=" + charset);
        response.write(content, flush);
    }

    /**
     * 流媒体文件响应输出，采用数据零拷贝输出到网络
     */
    public static void response(BootorHttpResponse response, MediaType contentType,
                                String charset, File file, boolean flush) throws IOException {
        if (response.getContentType() == null) {
            response.setContentType(contentType.getType() + "; charset=" + charset);
        }
        response.write(file, flush);
    }

    /**
     * 根据Model对象构造请求表单
     *
     * @param  parametric Model对象
     * @return 请求表单
     */
    public static FormEntity buildFormEntity(Parametric parametric) throws IOException {
        return buildFormEntity(parametric, FormEntity.FORM_ENCTYPE_JSON);
    }

    /**
     * 根据Model对象构造请求表单
     *
     * @param  parametric Model对象
     * @param  formEnctype 表单类型，默认为JSON表单类型
     * @return 请求表单
     */
    public static FormEntity buildFormEntity(Parametric parametric, int formEnctype) throws IOException {
        if (parametric == null) {
            throw new IllegalArgumentException("parametric");
        }
        FormEntity formEntity = FormEntity.builder(formEnctype);
        formEntity.add(BootorConstants.REQUEST_PARAMETER_FLOW, parametric.getFlow());
        return formEntity;
    }

    public static FormEntity buildFormEntity(long flow) throws IOException {
        return buildFormEntity(flow, FormEntity.FORM_ENCTYPE_JSON);
    }

    /**
     * 构造带流水号请求的请求表单，
     * 主要服务于是非Parametric参数接口的业务
     *
     * @param  flow 流水号
     * @param  formEnctype 表单类型，默认为JSON表单类型
     * @return 请求表单
     */
    public static FormEntity buildFormEntity(long flow, int formEnctype) throws IOException {
        FormEntity formEntity = FormEntity.builder(formEnctype);
        formEntity.add(BootorConstants.REQUEST_PARAMETER_FLOW, flow);
        return formEntity;
    }

    /**
     * 根据Model对象构造请求体
     *
     * @param  serviceId 服务注册实例ID
     * @param  url 请求URL
     * @return 请求体
     */
    public static OkRequest buildIORequest(String serviceId, String url) {
        return OkRequest.builder().serviceId(serviceId).proxyMode(Proxy.Type.SERVICE).url(serviceId + url);
    }

    /**
     * 获取文件扩展名
     *
     * @param  filePath 文件路径
     * @return 文件扩展名
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null) {
            return null;
        }
        int index = filePath.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return filePath.substring(index + 1);
    }
}
