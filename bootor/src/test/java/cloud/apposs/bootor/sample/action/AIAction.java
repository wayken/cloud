package cloud.apposs.bootor.sample.action;

import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.okhttp.FormEntity;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.Param;
import cloud.apposs.util.SseEmitter;
import cloud.apposs.util.Table;

import java.io.IOException;

@RestAction
public class AIAction {
    public static final String URL = "https://api.siliconflow.cn/v1/chat/completions";
    public static final String TOKEN = "sk-...";

    private OkHttp okHttp;

    /** 构造器注入配置 */
    @Autowired
    public AIAction(OkHttp okHttp) throws IOException {
        this.okHttp = okHttp;
    }

    @Request(value = "/openai")
    public React<SseEmitter> openai() throws Exception {
        Param message = Param.builder("role", "user")
                .setString("content", "Hello!");
        Table<Param> messages = Table.builder();
        messages.add(message);
        FormEntity formEntity = FormEntity.builder(FormEntity.FORM_ENCTYPE_JSON)
                .add("stream", true)
                .add("model", "Qwen/Qwen2.5-7B-Instruct")
                .add("messages", messages);
        OkRequest request = OkRequest.builder().url(URL).sse(true).post(formEntity);
        request.header("Authorization", "Bearer " + TOKEN);
        return okHttp.execute(request).map((OkResponse result) -> {
            String data = result.getStream();
            System.out.println("SSE Data: " + data);
            SseEmitter emitter = SseEmitter.builder(data);
            if (result.isCompleted()) {
                emitter.done(true, false);
            }
            return emitter;
        });
    }
}
