package cloud.apposs.bootor.netty;

import cloud.apposs.bootor.BootorConfig;
import cloud.apposs.util.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NettyUtil {
    /**
     * 解析 HTTP 请求参数
     *
     * @param  fullHttpRequest 完整的 HTTP 请求
     * @param  request         NettyHttpRequest 对象
     * @param  config          服务配置
     * @return 解析后的 NettyHttpRequest 对象
     */
    public static void parseRequestParameter(FullHttpRequest fullHttpRequest, NettyHttpRequest request, BootorConfig config) throws Exception {
        if (fullHttpRequest.method() == HttpMethod.GET) {
            // URL 参数数据传递
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            Map<String, String> parameters = request.getParameters();
            for(Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                parameters.put(entry.getKey(),entry.getValue().get(0));
            }
        }
        String contentType = request.getHeader("content-type", true);
        if (MediaType.APPLICATION_FORM_URLENCODED.match(contentType)) {
            // POST URL 表单数据提交
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
            Map<String, String> parameters = request.getParameters();
            for (InterfaceHttpData data : parmList) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    parameters.put(attribute.getName(), attribute.getValue());
                }
            }
            return;
        }
        if (MediaType.APPLICATION_JSON.match(contentType)) {
            // JSON 表单数据提交
            Param param = JsonUtil.parseJsonParam(fullHttpRequest.content().toString(CharsetUtil.UTF_8));
            if (param != null) {
                request.getParam().putAll(param);
            }
            return;
        }
        if (MediaType.MULTIPART_FORM_DATA.match(contentType)) {
            // POST FORM 表单数据提交f
            HttpPostRequestDecoder decoder = null;
            try {
                decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(true), fullHttpRequest);
                List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
                Map<String, String> parameters = request.getParameters();
                for (InterfaceHttpData data : parmList) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        // 解析文件上传
                        FileUpload fileUpload = (FileUpload) data;
                        // 文件上传未完成，继续等待
                        if (!fileUpload.isCompleted()) {
                            continue;
                        }
                        // 开始解析文件上传数据
                        if (fileUpload.isInMemory()) {
                            String field = fileUpload.getName();
                            String fileName = fileUpload.getFilename();
                            FileBuffer buffer = FileBuffer.wrap(fileName, fileUpload.get());
                            request.getFiles().put(field, buffer);
                        } else {
                            String field = fileUpload.getName();
                            String fileName = fileUpload.getFilename();
                            File file = File.createTempFile(CachedFileStream.DEFAULT_TMP_FILE_PREFIX,
                                    CachedFileStream.DEFAULT_TMP_FILE_SUFFIX, new File(config.getTempDir()));
                            fileUpload.renameTo(file);
                            FileBuffer buffer = FileBuffer.wrap(fileName, file);
                            request.getFiles().put(field, buffer);
                        }
                    } else {
                        // 解析普通表单
                        Attribute attribute = (Attribute) data;
                        parameters.put(attribute.getName(), attribute.getValue());
                    }
                }
            } finally {
                if (decoder != null) {
                    decoder.destroy();
                }
            }
        }
    }
}
