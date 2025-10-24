package cloud.apposs.util;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP状态码，参考：
 * https://www.cnblogs.com/carl10086/p/6185095.html
 */
public final class HttpStatus {
    public static final HttpStatus HTTP_STATUS_200 = newStatus(200, "OK");
    public static final HttpStatus HTTP_STATUS_204 = newStatus(204, "No Content");

    public static final HttpStatus HTTP_STATUS_301 = newStatus(301, "Moved Permanently");
    public static final HttpStatus HTTP_STATUS_302 = newStatus(302, "Found");
    public static final HttpStatus HTTP_STATUS_304 = newStatus(304, "Not Modified");

    public static final HttpStatus HTTP_STATUS_404 = newStatus(404, "Not Found");
    public static final HttpStatus HTTP_STATUS_400 = newStatus(400, "Bad Request");
    public static final HttpStatus HTTP_STATUS_401 = newStatus(401, "Unauthorized");
    public static final HttpStatus HTTP_STATUS_403 = newStatus(403, "Forbidden");
    public static final HttpStatus HTTP_STATUS_429 = newStatus(429, "Too Many Requests");

    public static final HttpStatus HTTP_STATUS_500 = newStatus(500, "Internal Server Error");
    public static final HttpStatus HTTP_STATUS_501 = newStatus(501, "Not Implemented");
    public static final HttpStatus HTTP_STATUS_502 = newStatus(502, "Bad Gateway");

    private final int code;

    private final String description;

    private static final Map<Integer, HttpStatus> status = new HashMap<Integer, HttpStatus>();
    static {
        status.put(HTTP_STATUS_200.getCode(), HTTP_STATUS_200);
        status.put(HTTP_STATUS_204.getCode(), HTTP_STATUS_204);

        status.put(HTTP_STATUS_301.getCode(), HTTP_STATUS_301);
        status.put(HTTP_STATUS_302.getCode(), HTTP_STATUS_302);
        status.put(HTTP_STATUS_304.getCode(), HTTP_STATUS_304);

        status.put(HTTP_STATUS_404.getCode(), HTTP_STATUS_404);
        status.put(HTTP_STATUS_400.getCode(), HTTP_STATUS_400);
        status.put(HTTP_STATUS_403.getCode(), HTTP_STATUS_403);
        status.put(HTTP_STATUS_429.getCode(), HTTP_STATUS_429);

        status.put(HTTP_STATUS_500.getCode(), HTTP_STATUS_500);
        status.put(HTTP_STATUS_501.getCode(), HTTP_STATUS_501);
        status.put(HTTP_STATUS_502.getCode(), HTTP_STATUS_502);
    }

    private HttpStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private static HttpStatus newStatus(int code, String description) {
        return new HttpStatus(code, description);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static HttpStatus valuesOf(int code) {
        HttpStatus value = status.get(code);
        return value != null ? value : newStatus(code, "Unknown");
    }
}
