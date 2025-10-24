package cloud.apposs.cache;

import cloud.apposs.cache.jvm.JvmCache;
import cloud.apposs.cache.redis.RedisCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestCacheIncr {
    public static final String HOST = "192.168.7.41";
    public static final int PORT = 6030;
    public static final int TYPE_CACHE = 0;

    private Cache cache = null;
    private String key = "LimitIp";

    @Before
    public void before() {
        if (TYPE_CACHE == 0) {
            CacheConfig config = new CacheConfig();
            cache = new JvmCache(config);
        } else {
            CacheConfig config = new CacheConfig();
            CacheConfig.RedisConfig redisConfig = config.getRedisConfig();
            redisConfig.addServer(new CacheConfig.RedisConfig.RedisServer(HOST, PORT));
            redisConfig.setCacheType(CacheConfig.RedisConfig.REDIS_CACHE_SINGLE);
            cache = new RedisCache(config);
        }
    }

    @Test
    public void testIncr() throws Exception {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new IncrTask(latch));
            t.setName("simulate-incr-Task");
            t.start();
        }
        latch.await();
        long value = cache.incr(key);
        System.out.println(value);
        Assert.assertEquals(10001, value);
    }

    /**
     * 模拟多个线程同时调用incr方法，测试原子性
     */
    class IncrTask implements Runnable {
        private CountDownLatch latch;

        public IncrTask(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            int total = 100;
            for (int i = 0; i < total; i++) {
                cache.incr(key);
                Random random = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(50));
                } catch (InterruptedException e) {
                }
            }
            latch.countDown();
        }
    }
}
