package cloud.apposs.webx.sample.action;

import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.IoFunction;
import cloud.apposs.react.React;
import cloud.apposs.rest.FileStream;
import cloud.apposs.rest.annotation.Action;
import cloud.apposs.rest.annotation.Model;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.Variable;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Param;
import cloud.apposs.util.SseEmitter;
import cloud.apposs.webx.interceptor.limit.LimitRate;
import cloud.apposs.webx.sample.bean.MyBean;
import cloud.apposs.webx.upload.MultiFormRequest;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Action
public class UserAction {
    private OkHttp okHttp;

    public UserAction() throws IOException {
    }

    @Autowired
    public void setOkHttp(OkHttp okHttp) {
        this.okHttp = okHttp;
    }

    @Request("/")
    public React<String> root() {
        return index();
    }

    @Request(value = "/index", method = Request.Method.GET)
    public React<String> index() {
        return React.just("forward:index");
    }

    @Request("/login")
    public React<String> login(HttpServletRequest request) {
        return React.just("Login Html");
    }

    /**
     * http://127.0.0.1:8880/search?xxx=xxx
     */
    @Request(value = "/search", method = Request.Method.GET)
    public React<String> search(HttpServletRequest request, HttpServletResponse response) {
        String output = "";
        System.out.println(request.getRequestURI());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            output += "header - " + key + ":" + value + "<br>";
        }
        return React.just("Search Info:" + output);
    }

    /**
     * http://127.0.0.1:8090/product/110/mengniu
     */
    @Request("/product/{id}/{name}")
    public React<String> product(@Variable("id") int id, @Variable("name") String name) {
        return React.just("Product Info:" + id + "-" + name);
    }

    @Request("/myform")
    public React<String> myform() {
        return React.just("forward:myform");
    }

    @Request(value = "/xssfile", method = Request.Method.POST)
    public React<String> xssfile(HttpServletRequest request) {
        return React.just("forward:xssfile");
    }

    /**
     * http://127.0.0.1:8880/apr/xiaomitv
     * 基于React的响应式异步输出，底层判断如果返回值是React直接异步执行网络响应
     */
    @Request("/apr/{name}")
    public React<String> aproduct(@Variable("name") String name) {
        return React.from("Hello，Reactor-" + name);
    }

    @Request("/exp")
    public React<Param> exp(Param info) {
        throw new IllegalStateException();
    }

    @Request("/capt")
    public React<Param> captcha(Param info) {
        return React.just(info);
    }

    @Request("/userinfo")
    public React<Param> userinfo(Param info) {
        return React.just(info);
    }

    @LimitRate(threshold = 2)
    @Request("/limit")
    public React<String> limitRate() {
        return React.just("Limit Rate Ip");
    }

    /**
     * curl -i -X POST -H "Content-type:application/json"
     * -d '{"field1":"value1","field5":[{"name":"MySubName1","id":1},{"name":"MySubName2","id":2}],"field4":{"sub2":"MyName1","sub3":{"name":"MySubName2","id":2},"sub1":"MyTitle1"},"field3":10,"field2":"value2"}'
     * http://localhost:8090/body
     * 将请求json转换为对象模型
     */
    @Request("/body")
    public React<String> abody(@Model MyBean bean) {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                HttpServletRequest request = bean.getRequest();
                Enumeration<String> headers = request.getHeaderNames();
                StringBuilder response = new StringBuilder();
                response.append("Hello，Reactor-" + bean + ";flow:" + bean.getFlow() + "\r\n");
                while (headers.hasMoreElements()) {
                    String key = headers.nextElement();
                    String value = request.getHeader(key);
                    response.append(key + ":" + value + "\r\n");
                }
                return response.toString();
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
                    return SseEmitter.builder().data("Hello SseEmitter " + aLong);
                }
            }
        });
    }

    @Request(value = "/psse")
    public React<?> proxySse() throws Exception {
        OkRequest ioRequest = OkRequest.builder().url("127.0.0.1:8880/openai").sse(true);
        return okHttp.execute(ioRequest).map(response -> {
            if (!response.isSseResponse()) {
                return response.getContent();
            }
            return SseEmitter.builder(response.getStream(), response.isCompleted());
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
                File downloadFile = new File("F:\\Downloads\\CentOS-6.6-x86_64-bin-DVD1.iso");
                String filename = new String(downloadFile.getName().getBytes("utf-8"), "ISO8859-1");
                return FileStream.create(downloadFile)
                        .putHeader("content-disposition", "attachment;filename=" + filename);
            }
        });
    }

    /**
     * 模拟服务器端的先请求外部文件，再供客户端下载
     */
    @Request("/io/buffer3")
    public React<FileStream> iobuffer3() throws Exception {
        String url = "https://1.s131i.faiusr.com/2/AIMBCAAQAhgAIKHk5-oFKJbbjuMHMNIGOK4D.jpg";
        return okHttp.execute(url).map(new IoFunction<OkResponse, FileStream>() {
            @Override
            public FileStream call(OkResponse httpAnswer) throws Exception {
                return FileStream.create(MediaType.IMAGE_PNG, httpAnswer.getBuffer());
            }
        });
    }

    /**
     * 客户端文件上传并存储到服务端
     * curl -v -F "app=1" -F "filecomment=TXT FILE" -F "yum.txt=@/tmp/article.txt" 127.0.0.1:8090/upload/file1
     */
    @Request.Post("/upload/file1")
    public React<String> fileUpload(MultiFormRequest request) throws Exception {
        return React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                Map<String, FileItem> files = request.getFiles();
                for (Map.Entry<String, FileItem> entry : files.entrySet()) {
                    String filename = entry.getKey();
                    FileItem fileItem = entry.getValue();
                    fileItem.write(new File("C://upload/" + filename));
                }
                return "upload ok";
            }
        });
    }
}
