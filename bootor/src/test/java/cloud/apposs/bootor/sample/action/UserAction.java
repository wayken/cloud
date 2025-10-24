package cloud.apposs.bootor.sample.action;

import cloud.apposs.bootor.BootorConfig;
import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.sample.bean.MyBean;
import cloud.apposs.ioc.Initializable;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.IoFunction;
import cloud.apposs.react.IoReduce;
import cloud.apposs.react.React;
import cloud.apposs.rest.FileStream;
import cloud.apposs.rest.annotation.*;
import cloud.apposs.util.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestAction
public class UserAction implements Initializable {
    private BootorConfig config;

    private OkHttp okHttp;

    /** 构造器注入配置 */
    @Autowired
    public UserAction(BootorConfig config) throws IOException {
        this.config = config;
    }

    /** 方法注入HTTP组件 */
    @Autowired
    public void setOkHttp(OkHttp okHttp) {
        this.okHttp = okHttp;
    }

    @Override
    public void initialize() {
        System.out.println("UserAction initialize");
    }

    @Request("/")
    public React<String> root() {
        return React.just("Hello Index Html");
    }

    @Request.Get(value = "/show")
    public React<String> show() {
        return React.just("HTTP GET SHOW");
    }

    /**
     * http://127.0.0.1:8880/usr?id=1&name=qun
     */
    @Request(value = "/usr", method = Request.Method.GET)
    public React<String> usr(@Variable("id") int id, @Variable("name") String name) {
        return React.just("Usr Info:" + id + "-" + name);
    }

    /**
     * http://127.0.0.1:8880/search?xxx=xxx
     */
    @Request(value = "/search", method = Request.Method.GET)
    public React<String> search(BootorHttpRequest request, BootorHttpResponse response) {
        Map<String, String> headers = request.getHeaders();
        StringBuilder output = new StringBuilder();
        for (String header: headers.keySet()) {
            output.append(header).append(" : ").append(headers.get(header)).append("<br>");
        }
        return React.just("Search Info:" + output);
    }

    /**
     * http://127.0.0.1:8880/product/110/mengniu
     */
    @Request("/product/{id}/{name}")
    public React<String> product(@Variable("id") int id, @Variable("name") String name) {
        return React.just("Product Info:" + id + "-" + name);
    }

    /**
     * curl http://127.0.0.1:8880/search/12/television
     */
    @Request("/search/{id}/{name}")
    public React<String> search(Param info) {
        return React.just("Search Info2:" + info.getInt("id") + "-" + info.getString("name"));
    }

    /**
     * curl -v -d "id=16&name=myproduct" http://192.168.0.104:8880/pr
     */
    @Request(value = "/pr", method = Request.Method.POST)
    public React<String> pdetail(Param info) {
        return React.just("Product Detail:" + info.getString("name"));
    }

    /**
     * curl -v -d "id=16&name=myproduct2" http://192.168.0.104:8880/pr/18
     */
    @Request(value = "/pr/{id}", method = Request.Method.POST)
    public React<String> pdetail(@Variable("id") int id, Param info) {
        return React.just("Product Detail:" + id + "-" + info.getString("name"));
    }

    /**
     * 测试host+path的匹配
     */
    @Request(value = "/", host = "www.mydomain.com")
    public React<String> domain() {
        return React.just("Hello Index Html From Mydomain");
    }

    /**
     * http://127.0.0.1:8880/config
     */
    @Request("/config")
    public React<String> config() {
        return React.just(config.getBasePackage());
    }

    /**
     * http://127.0.0.1:8880/read?id=100&name=window
     */
    @Request.Read("/read")
    public React<Param> read(Param info) {
        return React.just(info);
    }

    /**
     * 配置请求熔断，该resource_flow必须在配置文件在配置对应的熔断规则
     */
    @GuardCmd("flow_qps")
    @Request("/guard")
    public React<String> guard(@Variable("name") String name) {
        return React.just("Guard In Info:" + name);
    }

    /**
     * http://127.0.0.1:8880/exp
     */
    @Request("/exp")
    public React<String> exception() {
        throw new IllegalArgumentException("require parameter");
    }

    @WriteCmd
    @Request("/write")
    public React<String> writeProcess() {
        return React.just("Content Write Html");
    }

    @Request(value = "/produce", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public React<String> produce() {
        return React.just("Content Produce Markdown Content");
    }

    @Request("/srt")
    public React<String> srt() {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                return "Hello StandardResult";
            }
        });
    }

    @Request(value = "/rsse")
    public React<SseEmitter> rawSse() throws IOException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        return React.interval(scheduler, 1000, TimeUnit.MILLISECONDS).map(new IoFunction<Long, SseEmitter>() {
            @Override
            public SseEmitter call(Long aLong) throws Exception {
                if (aLong > 10) {
                    scheduler.shutdown();
                    return SseEmitter.builder().done(true);
                } else {
                    System.out.println("Send sse data " + aLong);
                    return SseEmitter.builder().data("Hello SseEmitter " + aLong);
                }
            }
        });
    }

    @Request("/executeon")
    @Executor
    public React<String> executorOn() {
        return React.just("Hello Index Html Executor In " + Thread.currentThread());
    }

    @Request("/spider2")
    public React<String> spider3() throws Exception {
        return okHttp.execute("https://www.qq.com").map(new IoFunction<OkResponse, String>() {
            @Override
            public String call(OkResponse httpAnswer) throws Exception {
                return httpAnswer.getContent();
            }
        });
    }

    @Request("/proxy1")
    public React<String> proxy1() throws Exception {
        OkRequest request = OkRequest.builder()
                .proxyMode(Proxy.Type.SOCKS)
                .serviceId("socks_proxy")
                .url("http://myip.ipip.net/");
        return okHttp.execute(request).map(new IoFunction<OkResponse, String>() {
            @Override
            public String call(OkResponse httpAnswer) throws Exception {
                return httpAnswer.getContent();
            }
        });
    }

    /**
     * 代理转发
     * http://192.168.4.5:8880/proxypass
     */
    @Request("/proxypass")
    public React<String> proxypass() throws Exception {
        OkRequest request = OkRequest.builder()
                .url("http://192.168.4.40/");
        return okHttp.execute(request).map(new IoFunction<OkResponse, String>() {
            @Override
            public String call(OkResponse httpAnswer) throws Exception {
                return httpAnswer.getContent();
            }
        });
    }

    /**
     * curl -i -X POST -H "Content-type:application/json" \
     * -d '{"field1":"value1","field5":[{"name":"MySubName1","id":1},{"name":"MySubName2","id":2}],"field4":{"sub2":"MyName1","sub3":{"name":"MySubName2","id":2},"sub1":"MyTitle1"},"field3":10,"field2":"value2"}'\
     * http://localhost:8880/body
     * 将请求json转换为对象模型
     */
    @Request("/body")
    public React<String> abody(@Model MyBean bean) {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                BootorHttpRequest request = bean.getRequest();
                Map<String, String> headers = request.getHeaders();
                StringBuilder response = new StringBuilder();
                response.append("Hello，Reactor-" + bean + ";flow:" + bean.getFlow() + "\r\n");
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    response.append(key + ":" + value + "\r\n");
                }
                return response.toString();
            }
        });
    }

    /**
     * http://127.0.0.1:8880/apr/xiaomitv
     * 基于React的响应式异步输出，底层判断如果返回值是React直接异步执行网络响应
     */
    @Request("/apr/{name}")
    public React<String> aproduct(@Variable("name") String name) {
        return React.from("Hello，Reactor-" + name);
    }

    @Request("/apr2/{name}")
    public React<String> aproduct2(@Variable("name") String name) {
        return React.from("Hello，Reactor1-" + name, "Hello, Reactor2-" + name)
            .map(new IoFunction<String, String>() {
                @Override
                public String call(String s) throws Exception {
                    return s + " From Map";
                }
            }).reduce(new IoReduce<String, String, String>() {
                @Override
                public String call(String s, String s2) throws Exception {
                    return s + "::" + s2;
                }
            });
    }

    /**
     * 模拟服务器端的文件图片展现
     */
    @Request("/io/buffer1")
    public React<FileStream> iobuffer() {
        return React.emitter(new IoEmitter<FileStream>() {
            @Override
            public FileStream call() throws Exception {
                return FileStream.create(MediaType.IMAGE_PNG, "C://12.jpg");
            }
        });
    }

    /**
     * 模拟客户端请求服务器端的文件下载，采用数据零拷贝进行数据传输，对JVM内存没有压力，
     * HTTP协议实现中HEADER返回contetype-type为stream，BODY为文件二进制流
     */
    @Request("/io/buffer2")
    public React<FileStream> iobuffer2() {
        return React.emitter(new IoEmitter<FileStream>() {
            @Override
            public FileStream call() throws Exception {
                File downloadFile = new File("E:\\Download\\System\\CentOS-8.2.2004-x86_64-minimal.iso");
                return FileStream.create(downloadFile)
                        .putHeader("content-disposition", "attachment;filename=" + downloadFile.getName());
            }
        });
    }

    /**
     * 模拟服务器端的先请求外部文件，再供客户端下载
     */
    @Request("/io/buffer3")
    public React<FileStream> iobuffer3() throws Exception {
        String url = "https://aisearch.cdn.bcebos.com/homepage/dashboard/ai_picture_create/1230041354_0_final.jpg";
        return okHttp.execute(url).map(new IoFunction<OkResponse, FileStream>() {
            @Override
            public FileStream call(OkResponse httpAnswer) throws Exception {
                return FileStream.create(MediaType.IMAGE_PNG, httpAnswer.getBuffer());
            }
        });
    }

    /**
     * 客户端文件上传并存储到服务端
     * curl -F "userid=1" -F "filecomment=NormalFile" -F "yum.txt=@/tmp/yum_save_tx.2020-09-20.13-54.hLsN2N.yumtx" 192.168.1.8:8880/upload/file1
     */
    @Request.Post("/upload/file1")
    public React<String> fileUpload(BootorHttpRequest request) throws Exception {
        Map<String, FileBuffer> files = request.getFiles();
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                for (Map.Entry<String, FileBuffer> entry : files.entrySet()) {
                    FileBuffer value = entry.getValue();
                    System.out.println(value.size());
//                    System.out.println(new String(value.array()));
                }
                return "upload ok " + files.size();
            }
        });
    }
}
