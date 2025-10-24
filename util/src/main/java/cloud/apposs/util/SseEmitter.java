package cloud.apposs.util;

/**
 * 服务端推送数据封装，用于服务端主动推送数据到客户端
 */
public final class SseEmitter {
    private final StringBuilder data = new StringBuilder();

    // 当前数据传输是否结束
    private boolean done = false;

    public static SseEmitter builder() {
        return new SseEmitter();
    }

    public static SseEmitter builder(String text) {
        return builder(text, false);
    }

    public static SseEmitter builder(String text, boolean done) {
        return new SseEmitter().done(done).append(text);
    }

    public SseEmitter id(String id) {
        append("id:").append(id).append("\n");
        return this;
    }


    public SseEmitter name(String name) {
        append("event:").append(name).append("\n");
        return this;
    }

    public SseEmitter comment(String comment) {
        append(":").append(comment).append("\n");
        return this;
    }

    public SseEmitter data(Object object) {
        append("data:").append(object.toString()).append("\n");
        return this;
    }

    private SseEmitter append(String text) {
        data.append(text);
        return this;
    }

    public SseEmitter done(boolean done) {
        return done(done, false);
    }

    public SseEmitter done(boolean done, boolean append) {
        this.done = done;
        if (append) {
            append("[Done]\n");
        }
        return this;
    }

    public boolean isDone() {
        return done;
    }

    public String build() {
        return data.toString();
    }
}
