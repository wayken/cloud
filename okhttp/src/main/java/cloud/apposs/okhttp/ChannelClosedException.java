package cloud.apposs.okhttp;

import java.io.IOException;

public class ChannelClosedException extends IOException {
    public ChannelClosedException(String message) {
        super(message);
    }
}
