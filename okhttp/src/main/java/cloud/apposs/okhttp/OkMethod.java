package cloud.apposs.okhttp;

import java.util.HashMap;
import java.util.Map;

public enum OkMethod {
	GET, POST, HEAD, PUT, DELETE;

	private static final Map<String, OkMethod> methodMap = new HashMap<String, OkMethod>();

	static {
	    methodMap.put("GET", GET);
        methodMap.put("POST", POST);
        methodMap.put("PUT", PUT);
        methodMap.put("DELETE", DELETE);
        methodMap.put("HEAD", HEAD);
    }

    public static OkMethod valuesOf(String method) {
	    return methodMap.get(method);
    }
}
