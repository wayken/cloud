package cloud.apposs.cache;

import cloud.apposs.cache.CacheConfig.RedisConfig;
import cloud.apposs.cache.CacheConfig.RedisConfig.RedisServer;
import cloud.apposs.cache.redis.RedisCache;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestRedisCache {
    public static final String HOST = "192.168.0.102";
    public static final int PORT = 6030;

    private RedisCache cache = null;

    @Before
    public void before() {
        CacheConfig config = new CacheConfig();
        RedisConfig redisConfig = config.getRedisConfig();
        redisConfig.addServer(new RedisServer(HOST, PORT));
        redisConfig.setCacheType(RedisConfig.REDIS_CACHE_SINGLE);
        cache = new RedisCache(config);
    }

    @After
    public void after() {
        cache.shutdown();
    }

    @Test
    public void testPutString() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            cache.put(key, "MyValue" + i);
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutStringBatch() {
        long start = System.currentTimeMillis();
        List<String> keys = new LinkedList<String>();
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            keys.add(key);
            values.add("MyValue" + i);
        }
        cache.put(keys, values);
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutObjectBatch() {
        List<String> keys = new LinkedList<String>();
        List<ProtoBuf> values = new LinkedList<ProtoBuf>();
        ProtoSchema schema = ProtoSchema.getSchema(Product.class);
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            keys.add(key);
            Product object = new Product(i, "MyProduct");
            ProtoBuf value = ProtoBuf.wrap(object, schema);
            values.add(value);
        }
        long start = System.currentTimeMillis();
        cache.put(keys, values, false);
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testGetString() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            String value = cache.getString(key);
        }
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testGetStringListBatch() {
        long start = System.currentTimeMillis();
        List<String> keys = new LinkedList<String>();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKeys" + i;
            keys.add(key);
        }
        List<String> values = cache.getStringList(keys);
        System.out.println(values.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testGetObjectListBatch() {
        long start = System.currentTimeMillis();
        List<String> keys = new LinkedList<String>();
        for (int i = 0; i < 10; i++) {
            String key = "MyKey" + i;
            keys.add(key);
        }
        List<ProtoBuf> values = cache.getBufferList(keys);
        ProtoSchema schema = ProtoSchema.getSchema(Product.class);
        for (ProtoBuf value : values) {
            System.out.println(value.getObject(Product.class, schema));
        }
        System.out.println(values.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutObject() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            String key = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            System.out.println(cache.getObject(key, Product.class, schema));
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    /**
     * 测试二级缓存存储
     */
    @Test
    public void testHPutString() throws Exception {
        long start = System.currentTimeMillis();
        String key = "MyKey";
        for (int i = 0; i < 100; i++) {
            String field = "MyKey" + i;
            cache.hput(key, field, "MyValue");
            System.out.println(cache.hgetString(key, field));
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testHGetObject() throws Exception {
        String key = "MyKey";
        cache.expire(key, 60000);
        List<Product> products = cache.hgetObjectList(key, Product.class, ProtoSchema.getSchema(Product.class));
        System.out.println(products);
    }

    @Test
    public void testHPutObject() throws Exception {
        long start = System.currentTimeMillis();
        String key = "MyKey";
        for (int i = 0; i < 100; i++) {
            String field = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.hput(key, field, value, schema);
            cache.hgetObject(key, field, Product.class, schema);
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
        List<Product> products = cache.hgetObjectList(key, Product.class, ProtoSchema.getSchema(Product.class));
        System.out.println(products.size());
    }

    @Test
    public void testHPutParam() throws Exception {
        String key = "MyKey";
        ProtoSchema schema = ProtoSchema.mapSchema();
        schema.addKey("id", Integer.class);
        schema.addKey("name", String.class);
        for (int i = 0; i < 100; i++) {
            String field = "MyKey" + i;
            Param data = Param.builder("id", i).setString("name", "product" + i);
            data.setInt(key + i, i);
            cache.hput(key, field, data, schema);
        }
        List<Param> result = cache.hgetParamList(key, schema);
        List<String> keys = cache.hgetKeyList(key);
        System.out.println(keys.size());
        System.out.println(result.size());
        Assert.assertTrue(keys.size() == result.size());
        cache.remove(key, keys);
        System.out.println(cache.hgetParam(key, "MyKey10", schema));
    }

    public static class Product {
        private int id;

        private String name;

        public Product() {
        }

        public Product(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "id:" + id + ";name:" + name;
        }
    }
}
