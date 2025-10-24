package cloud.apposs.queue;

import java.util.EventListener;

public interface IMQListener extends EventListener {
    void onConsumeMessage(MQRecord record);
}
