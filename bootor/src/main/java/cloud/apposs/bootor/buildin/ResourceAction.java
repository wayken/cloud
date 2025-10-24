package cloud.apposs.bootor.buildin;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.rest.FileStream;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.CharsetUtil;
import cloud.apposs.util.MediaType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;

/**
 * 静态资源文件访问处理器，用于处理静态资源文件的访问请求，
 * 业务可以继承该类并实现对应的资源文件访问处理逻辑，添加{@link cloud.apposs.ioc.annotation.Component}注解后由框架自动加载，
 * 参考<a href="https://zhuanlan.zhihu.com/p/643951607?utm_id=0">Spring Boot静态资源访问</a>
 */
public class ResourceAction {
    /**
     * 获取配置的静态资源文件路径，由数组形式返回支持的资源文件路径列表，业务可以根据实际情况进行配置
     *
     * @return 支持的资源文件路径列表
     */
    public String[] getStaticPath() {
        return new String[] {
            "classpath:/static/",
            "classpath:/public/",
            "classpath:/dist/",
        };
    }

    /**
     * 当直接根路径/访问时，默认访问的首页文件，默认为index.html
     */
    public String getDefaultPage() {
        return "/index.html";
    }

    /**
     * 当文件不存在时，是否返回默认页面，如index.html，为空则表示不返回默认页面
     */
    public boolean isReturnDefaultPage() {
        return true;
    }

    /**
     * 解析静态资源文件路径，返回资源文件数据
     */
    @Request.Read(value = "/**")
    public React<FileStream> handleResource(BootorHttpRequest request) {
        return React.emitter(() -> {
            String path = WebUtil.getRequestPath(request);
            if (path == null) {
                throw new IllegalArgumentException("Required request path for url '" + request.getUri() + "' is not set");
            }
            String defaultPage = getDefaultPage();
            if (path.equals("/")) {
                path = defaultPage;
            }
            // 如果文件路径中包含中文，需要进行URL解码
            if (path.contains("%")) {
                path = URLDecoder.decode(path, CharsetUtil.UTF_8.name());
            }
            // 获取资源文件，包装成FileStream返回
            FileStream fileStream = handleMatchedFileLoad(path);
            if (fileStream != null) {
                return fileStream;
            }
            // 如果文件不存在，则返回默认页面
            if (isReturnDefaultPage()) {
                fileStream = handleMatchedFileLoad(defaultPage);
                if (fileStream != null) {
                    return fileStream;
                }
            }
            // 如果文件不存在，则抛出异常
            throw new FileNotFoundException("Resource file not found for path '" + path + "'");
        });
    }

    private FileStream handleMatchedFileLoad(String path) throws Exception {
        // 获取静态资源文件路径
        String[] staticPaths = getStaticPath();
        String fileExtension = WebUtil.getFileExtension(path);
        MediaType mediaType = MediaType.getMediaTypeByFileExtension(fileExtension);
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        for (String staticPath : staticPaths) {
            // 如果staticPath是以classpath:开头，则表示是ClassPath下的资源文件，否则是文件系统资源文件
            if (staticPath.startsWith("classpath:")) {
                staticPath = staticPath.substring("classpath:".length());
                path = staticPath + path;
                InputStream resource = getClass().getClassLoader().getResourceAsStream(path);
                if (Logger.isTraceEnabled()) {
                    Logger.trace("Try to load resource file for path '" + path + "' of resource " + resource);
                }
                if (resource != null) {
                    CachedFileStream cachedFileStream = CachedFileStream.wrap(resource);
                    return FileStream.create(mediaType, cachedFileStream);
                }
            } else {
                String filePath = staticPath + path;
                File file = new File(filePath);
                if (Logger.isTraceEnabled()) {
                    Logger.trace("Try to load disk file for path '" + path + "' of file " + file);
                }
                if (file.exists()) {
                    return FileStream.create(mediaType, file);
                }
            }
        }
        return null;
    }
}
