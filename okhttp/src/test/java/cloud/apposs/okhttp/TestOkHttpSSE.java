package cloud.apposs.okhttp;

import cloud.apposs.react.IoSubscriber;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestOkHttpSSE {
    private static final String SK_KEY = "sk-yfteolckczsrrcvfcldkrofpvxbuaxdlnfqxygvtuhewvxxw";

    private OkHttp okHttp;

    @Before
    public void before() throws Exception {
        okHttp = HttpBuilder.builder().poolConnections(10)
                .ioMode(HttpBuilder.IO_MODE_NETTY).retryCount(3).retrySleepTime(2000).build();
    }

    /**
     * CURL测试
     * <pre>
     *  curl -k https://api.siliconflow.cn/v1/chat/completions \
     *   -H "Content-Type: application/json" \
     *   -H "Authorization: Bearer sk-xxx" \
     *   -d '{
     *     "model": "Qwen/Qwen2.5-7B-Instruct",
     *     "messages": [
     *       {"role": "user", "content": "你好"}
     *     ]
     *   }'
     * </pre>
     */
    @Test
    public void testHttpExecuteGetAIContentBlock() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String site = "https://api.siliconflow.cn/v1/chat/completions";
        Table<Param> messages = Table.builder();
        messages.add(
                Param.builder("role", "user").setString("content", "你好")
        );
        FormEntity formEntity = FormEntity.builder(FormEntity.FORM_ENCTYPE_JSON)
                .add("stream", false)
                .add("model", "Qwen/Qwen2.5-7B-Instruct")
                .add("messages", messages)
                .add("top_p", 0.9)
                .add("temperature", 0.5);
        OkRequest request = OkRequest.builder().url(site).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SK_KEY)
                .post(formEntity);
        okHttp.execute(request).subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getContent());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
            }

            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testHttpExecuteGetAIContentSSE() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String site = "https://api.siliconflow.cn/v1/chat/completions";
        Table<Param> messages = Table.builder();
        messages.add(
                Param.builder("role", "user").setString("content", "用java实现一个http服务")
        );
        FormEntity formEntity = FormEntity.builder(FormEntity.FORM_ENCTYPE_JSON)
                .add("stream", true)
                .add("model", "Qwen/Qwen2.5-7B-Instruct")
                .add("messages", messages)
                .add("top_p", 0.9)
                .add("temperature", 0.5);
        OkRequest request = OkRequest.builder().url(site).sse(true).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SK_KEY)
                .post(formEntity);
        okHttp.execute(request).subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getStream());
                if (value.isCompleted()) {
                    latch.countDown();
                }
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
            }

            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }
}
