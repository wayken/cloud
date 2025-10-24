package cloud.apposs.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Html流媒体类型，主要是对网络传输文件类型进行封装
 */
public class MediaType implements Serializable {
    private static final long serialVersionUID = 2069937152339670231L;

    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String APPLICATION_XML_VALUE = "application/xml";
    public static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream";
    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";
    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";
    public static final String TEXT_PLAIN_VALUE = "text/plain";
    public static final String TEXT_HTML_VALUE = "text/html";
    public static final String TEXT_XML_VALUE = "text/xml";
    public static final String TEXT_MARKDOWN_VALUE = "text/markdown";
    public static final String APPLICATION_PDF_VALUE = "application/pdf";
    public static final String APPLICATION_ZIP_VALUE = "application/zip";
    public static final String APPLICATION_JAVA_VALUE = "application/java";
    public static final String TEXT_JAVASCRIPT_VALUE = "text/javascript";
    public static final String TEXT_JAVASSOURCE_VALUE = "text/x-java-source";
    public static final String TEXT_CSS_VALUE = "text/css";
    public static final String IMAGE_GIF_VALUE = "image/gif";
    public static final String IMAGE_JPG_VALUE = "image/jpg";
    public static final String IMAGE_JPEG_VALUE = "image/jpeg";
    public static final String IMAGE_PNG_VALUE = "image/png";
    public static final String IMAGE_SVG_VALUE = "image/svg+xml";
    public static final String AUDIO_MPEG_VALUE = "audio/mpeg";
    public static final String APPLICATION_ALL_VALUE = "*/*";

    public static final MediaType APPLICATION_JSON = valueOf(APPLICATION_JSON_VALUE);
    public static final MediaType APPLICATION_XML = valueOf(APPLICATION_XML_VALUE);
    public static final MediaType APPLICATION_OCTET_STREAM = valueOf(APPLICATION_OCTET_STREAM_VALUE);
    public static final MediaType APPLICATION_FORM_URLENCODED = valueOf(APPLICATION_FORM_URLENCODED_VALUE);
    public static final MediaType APPLICATION_PDF = valueOf(APPLICATION_PDF_VALUE);
    public static final MediaType APPLICATION_ZIP = valueOf(APPLICATION_ZIP_VALUE);
    public static final MediaType APPLICATION_JAVA = valueOf(APPLICATION_JAVA_VALUE);
    public static final MediaType IMAGE_GIF = valueOf(IMAGE_GIF_VALUE);
    public static final MediaType IMAGE_JPG = valueOf(IMAGE_JPG_VALUE);
    public static final MediaType IMAGE_JPEG = valueOf(IMAGE_JPEG_VALUE);
    public static final MediaType IMAGE_PNG = valueOf(IMAGE_PNG_VALUE);
    public static final MediaType IMAGE_SVG = valueOf(IMAGE_SVG_VALUE);
    public static final MediaType MULTIPART_FORM_DATA = valueOf(MULTIPART_FORM_DATA_VALUE);
    public static final MediaType TEXT_HTML = valueOf(TEXT_HTML_VALUE);
    public static final MediaType TEXT_PLAIN = valueOf(TEXT_PLAIN_VALUE);
    public static final MediaType TEXT_CSS = valueOf(TEXT_CSS_VALUE);
    public static final MediaType TEXT_JAVASCRIPT = valueOf(TEXT_JAVASCRIPT_VALUE);
    public static final MediaType TEXT_JAVASSOURCE = valueOf(TEXT_JAVASSOURCE_VALUE);
    public static final MediaType TEXT_XML = valueOf(TEXT_XML_VALUE);
    public static final MediaType TEXT_MARKDOWN = valueOf(TEXT_MARKDOWN_VALUE);
    public static final MediaType TEXT_EVENT_STREAM = valueOf(TEXT_EVENT_STREAM_VALUE);
    public static final MediaType AUDIO_MPEG = valueOf(AUDIO_MPEG_VALUE);
    public static final MediaType APPLICATION_ALL = valueOf(APPLICATION_ALL_VALUE);

    private static final Map<String, MediaType> MEDIA_TYPE_FILE_EXTENSION_MAP = new HashMap<String, MediaType>();
    static {
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("json", APPLICATION_JSON);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("pdf", APPLICATION_PDF);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("zip", APPLICATION_ZIP);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("class", APPLICATION_JAVA);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("gif", IMAGE_GIF);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("jpg", IMAGE_JPG);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("jpeg", IMAGE_JPEG);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("png", IMAGE_PNG);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("svg", IMAGE_SVG);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("html", TEXT_HTML);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("txt", TEXT_PLAIN);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("css", TEXT_CSS);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("js", TEXT_JAVASCRIPT);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("java", TEXT_JAVASSOURCE);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("xml", TEXT_XML);
        MEDIA_TYPE_FILE_EXTENSION_MAP.put("mp3", AUDIO_MPEG);
    }

    private final String type;

    private MediaType(String type) {
        this.type = type;
    }

    public static MediaType valueOf(String value) {
        return new MediaType(value);
    }

    public String getType() {
        return type;
    }

    /**
     * 判断媒体类型是否匹配
     *
     * @param  type 媒体类型字符串
     * @return 如果匹配则返回true，否则返回false
     */
    public boolean match(String type) {
        if (type == null) {
            return false;
        }
        return type.toLowerCase().startsWith(this.type);
    }

    /**
     * 根据媒体类型字符串获取对应的媒体类型
     *
     * @param  type 媒体类型字符串
     * @return 返回对应的媒体类型，如果没有找到则返回null
     */
    public static MediaType getMediaType(String type) {
        if (type == null) {
            return null;
        }
        for (MediaType mediaType : MEDIA_TYPE_FILE_EXTENSION_MAP.values()) {
            if (mediaType.match(type)) {
                return mediaType;
            }
        }
        return null;
    }

    /**
     * 根据文件扩展名获取对应的媒体类型
     *
     * @param  fileExtension 文件扩展名
     * @return 返回对应的媒体类型，如果没有找到则返回null
     */
    public static MediaType getMediaTypeByFileExtension(String fileExtension) {
        if (fileExtension == null) {
            return null;
        }
        return MEDIA_TYPE_FILE_EXTENSION_MAP.get(fileExtension.toLowerCase());
    }

    public String value() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
