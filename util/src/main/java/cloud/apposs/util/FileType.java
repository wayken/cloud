package cloud.apposs.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型操作，主要是结合{@link MediaType}对网络传输文件进行文件类型解析封装
 */
public class FileType {
    public static final int FILE_TYPE_UNKNOW = 0;
    public static final int FILE_TYPE_FOLDER = 1;
    public static final int FILE_TYPE_JPG = 2;
    public static final int FILE_TYPE_JPEG = 3;
    public static final int FILE_TYPE_PNG = 4;
    public static final int FILE_TYPE_GIF = 5;
    public static final int FILE_TYPE_TXT = 6;
    public static final int FILE_TYPE_MPEG = 7;
    public static final int FILE_TYPE_EXCEL = 8;
    public static final int FILE_TYPE_EXCELX = 9;
    public static final int FILE_TYPE_WORD = 10;
    public static final int FILE_TYPE_WORDX = 11;
    public static final int FILE_TYPE_PPT = 12;
    public static final int FILE_TYPE_PPTX = 13;
    public static final int FILE_TYPE_MINDMAP = 14;
    public static final int FILE_TYPE_PROTYPE = 15;
    public static final int FILE_TYPE_MODULENOTE = 16;
    public static final int FILE_TYPE_DRAW = 17;
    public static final int FILE_TYPE_MARKDOWN = 18;

    private static final Map<Integer, MediaType> fileTypeMapping = new HashMap<Integer, MediaType>();
    static {
        fileTypeMapping.put(FILE_TYPE_JPG, MediaType.IMAGE_JPG);
        fileTypeMapping.put(FILE_TYPE_JPEG, MediaType.IMAGE_JPEG);
        fileTypeMapping.put(FILE_TYPE_TXT, MediaType.TEXT_PLAIN);
        fileTypeMapping.put(FILE_TYPE_GIF, MediaType.IMAGE_GIF);
        fileTypeMapping.put(FILE_TYPE_PNG, MediaType.IMAGE_PNG);
        fileTypeMapping.put(FILE_TYPE_MPEG, MediaType.AUDIO_MPEG);
    }

    /**
     * 根据文件类型获取文件类型对象
     */
    public static MediaType getMediaType(int fileType) {
        return fileTypeMapping.get(fileType);
    }

    /**
     * 根据文件类型获取文件类型对象
     */
    public static int getFileType(String contentType) {
        if (StrUtil.isEmpty(contentType)) {
            return FILE_TYPE_UNKNOW;
        }
        for (Integer fileType : fileTypeMapping.keySet()) {
            MediaType mediaType = fileTypeMapping.get(fileType);
            if (mediaType.getType().equalsIgnoreCase(contentType)) {
                return fileType;
            }
        }
        return FILE_TYPE_UNKNOW;
    }

    /**
     * 判断文件类型是否为图片类型
     */
    public static boolean isFolderType(int fileType) {
        return fileType == FILE_TYPE_FOLDER;
    }

    /**
     * 判断文件类型是否为图片类型
     */
    public static boolean isIamgeType(int fileType) {
        if (fileType == FILE_TYPE_JPG || fileType == FILE_TYPE_JPEG
                || fileType == FILE_TYPE_PNG || fileType == FILE_TYPE_GIF) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件后缀
     */
    public static String getFileSuffix(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if ((pos < 0) || (pos >= name.length())) {
            return "";
        }
        return name.substring(pos + 1);
    }

    /**
     * 获取文件的后缀
     */
    public static String getFileSuffix(String filePath) {
        if (StrUtil.isEmpty(filePath)) {
            throw new IllegalArgumentException("file");
        }
        String name = getFileName(filePath);
        int pos = name.lastIndexOf('.');
        if ((pos < 0) || (pos >= name.length())) {
            return "";
        }
        return name.substring(pos + 1);
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String filePath) {
        if (StrUtil.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath");
        }
        String name = filePath.replace('\\', '/');
        int pos = name.lastIndexOf('/');
        if (pos < 0) {
            return name;
        }
        return name.substring(pos + 1);
    }
}
