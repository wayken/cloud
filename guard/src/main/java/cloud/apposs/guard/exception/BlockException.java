package cloud.apposs.guard.exception;

/**
 * 阻断时抛出的阻断异常
 */
public class BlockException extends Exception {
    private String resource;

    public BlockException(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public static boolean isBlockException(Throwable t) {
        if (null == t) {
            return false;
        }
        int counter = 0;
        for(Throwable cause = t; cause != null && counter++ < 10; cause = cause.getCause()) {
            if (cause instanceof BlockException) {
                return true;
            }
            if (cause.getMessage() != null && cause.getMessage().startsWith("BlockException")) {
                return true;
            }
        }
        return false;
    }
}
