package cloud.apposs.okhttp;

import cloud.apposs.balance.Peer;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.discovery.MemoryDiscovery;
import cloud.apposs.react.*;
import cloud.apposs.util.Proxy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TestOkHttp {
    private static final String COMMON_URL = "https://www.baidu.com";
    private static final String BUS_URL = "http://localhost:8880/product/config";
    private IDiscovery discovery;
    private OkHttp okHttp;

    @Before
    public void before() throws Exception {
        Map<String, List<Peer>> peers = new HashMap<String, List<Peer>>();
        List<Peer> peerList = new LinkedList<Peer>();
        peerList.add(new Peer("1.2.3.4", 12345));
        peers.put("sid1", peerList);
        discovery = new MemoryDiscovery(peers);
        okHttp = HttpBuilder.builder().discovery(discovery).ioMode(HttpBuilder.IO_MODE_NETTY).retryCount(3).retrySleepTime(2000).build();
    }

    /**
     * 测试异步HTTP GET请求
     */
    @Test
    public void testHttpExecuteGet() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        okHttp.execute("https://www.baidu.com/")
        .subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getPath());
                System.out.println(value.getContent());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    /**
     * 测试异步HTTP POST请求
     */
    @Test
    public void testHttpExecutePost() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        FormEntity formEntity = FormEntity.builder()
                .add("name", "xiaomitv")
                .add("price", 99.9);
        OkRequest request = OkRequest.builder()
                .url("http://www.baidu.com")
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
            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    /**
     * 测试异步HTTP GET请求
     */
    @Test
    public void testHttpExecuteRetry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        okHttp.execute(BUS_URL).subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getContent());
                latch.countDown();
            }
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testHttpExecuteRetry2() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                return "Hello String";
            }
        }).request(new IoFunction<String, React<OkResponse>>() {
            @Override
            public React<OkResponse> call(String s) throws Exception {
                return okHttp.execute("http://www.baidu.com");
            }
        }).request(new IoFunction<OkResponse, React<OkResponse>>() {
            @Override
            public React<OkResponse> call(OkResponse s) throws Exception {
                return okHttp.execute("http://www.qq.com");
            }
        }).subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                int i = 1 / 0;
                System.out.println(value.getContent());
                latch.countDown();
            }
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    /**
     * 测试异步HTTP SOCKS代理请求
     */
    @Test
    public void testHttpProxyRetry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        OkRequest request = OkRequest.builder().url("https://www.google.com").proxyMode(Proxy.Type.SOCKS).serviceId("sid1");
        okHttp.execute(request).subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getContent());
                latch.countDown();
            }
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    @After
    public void after() {
        okHttp.close();
    }
}
