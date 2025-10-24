package cloud.apposs.okhttp;

import cloud.apposs.balance.Peer;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.discovery.MemoryDiscovery;
import cloud.apposs.react.IoSubscriber;
import cloud.apposs.util.Proxy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TestOkHttpPool {
    private OkHttp okHttp;
    private IDiscovery discovery;

    @Before
    public void before() throws Exception {
        Map<String, List<Peer>> peers = new HashMap<String, List<Peer>>();
        List<Peer> peerList = new LinkedList<Peer>();
        peerList.add(new Peer("1.2.3.4", 12345));
        peers.put("sid1", peerList);
        discovery = new MemoryDiscovery(peers);
        okHttp = HttpBuilder.builder().discovery(discovery).poolConnections(10)
                .ioMode(HttpBuilder.IO_MODE_NETTY).retryCount(3).retrySleepTime(2000).build();
    }

    /**
     * 测试异步HTTP GET请求
     */
    @Test
    public void testHttpExecuteGet() throws Exception {
        String site = "http://127.0.0.1:8880/";
        CountDownLatch latch = new CountDownLatch(1);
        okHttp.execute(site)
        .subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getUrl());
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
        CountDownLatch latch2 = new CountDownLatch(1);
        okHttp.execute(site)
        .subscribe(new IoSubscriber<OkResponse>() {
            @Override
            public void onNext(OkResponse value) throws Exception {
                System.out.println(value.getUrl());
                latch2.countDown();
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
                latch2.countDown();
            }
        }).start();
        latch2.await();
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
