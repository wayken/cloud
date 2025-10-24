package cloud.apposs.cache;

import cloud.apposs.cache.CacheConfig.JvmConfig;
import cloud.apposs.cache.jvm.Element;
import cloud.apposs.cache.jvm.JvmCache;
import cloud.apposs.cache.jvm.JvmCacheListenerAdapter;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * -Xms100M -Xmx100M -Xmn10M -XX:+PrintGCDateStamps -XX:+PrintGCDetails
 */
public class TestJvmCache {
    @Test
    public void testPutString() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            cache.put(key, "MyValue");
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutStringBatch() {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        List<String> keys = new LinkedList<String>();
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < 10000; i++) {
            String key = "MyKey" + i;
            keys.add(key);
            values.add("MyValue" + i);
        }
        long start = System.currentTimeMillis();
        cache.put(keys, values);
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutObjectBatch() {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
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
    public void testGetStringListBatch() {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
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
    public void testPutObject() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 200000; i++) {
            String key = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            cache.getObject(key, Product.class, schema);
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPutObject2() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        int i = 0;
        while (true) {
            String key = "MyKey" + i++;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            cache.getObject(key, Product.class, schema);
            System.out.println(cache);
        }
    }

    /**
     * 测试当缓存超过指定条数时的回收策略
     */
    @Test
    public void testPutObject3() throws Exception {
        CacheConfig config = new CacheConfig();
        JvmConfig jvmConfig = config.getJvmConfig();
        jvmConfig.setMaxElements(100);
        JvmCache cache = new JvmCache(config);
        cache.addListener(new MyListener());
        for (int i = 0; i < 200; i++) {
            String key = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            cache.getObject(key, Product.class, schema);
        }
        System.out.println(cache.size());
    }

    /**
     * 测试当缓存超过指定内存容量时的回收策略
     */
    @Test
    public void testPutObject4() throws Exception {
        CacheConfig config = new CacheConfig();
        JvmConfig jvmConfig = config.getJvmConfig();
//		jvmConfig.setEvictionPolicy(null);
//		jvmConfig.setConcurrencyLevel(256);
        jvmConfig.setMaxMemory(1024 * 1024 * 30);
        JvmCache cache = new JvmCache(config);
        cache.addListener(new MyListener());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 400000; i++) {
            String key = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            cache.getObject(key, Product.class, schema);
            System.out.println(cache);
        }
        System.out.println("batch execute4:" + (System.currentTimeMillis() - start));
    }

    /**
     * 测试当缓存过期时间随机
     */
    @Test
    public void testPutObject5() throws Exception {
        CacheConfig config = new CacheConfig();
        JvmConfig jvmConfig = config.getJvmConfig();
        jvmConfig.setExpirationTimeRandom(true);
        jvmConfig.setExpirationTimeRandomMin(10 * 1000);
        jvmConfig.setExpirationTimeRandomMax(30 * 1000);
        JvmCache cache = new JvmCache(config);
        cache.addListener(new MyListener());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String key = "MyKey" + i;
            Product value = new Product(i, "MyProduct");
            ProtoSchema schema = ProtoSchema.getSchema(Product.class);
            cache.put(key, value, schema);
            cache.getObject(key, Product.class, schema);
//			System.out.println(cache);
        }
        System.out.println("batch execute4:" + (System.currentTimeMillis() - start));
    }

    /**
     * 测试List递归添加Scheme，示例结构如下
     * <pre>
     *   [
     *     {
     *         id: 1,
     *         name: "product1",
     *         chileren: [
     *          id: 11,
     *          name: "product11",
     *          chileren: [
     *              id: 111,
     *              name: "product111"
     *          ]
     *         ]
     *     },
     *     {
     *         id: 2,
     *         name: "product2"
     *     }
     *   ]
     * </pre>
     */
    @Test
    public void testPutObject6() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        String key = "MyKey";
        ProtoSchema schema = ProtoSchema.mapSchema();
        schema.addKey("id", Integer.class);
        schema.addKey("name", String.class);
        schema.addKey("children", List.class, ProtoSchema.listSchema(Param.class, schema));
        List<Param> dataList = new ArrayList<Param>();
        Param data1 = Param.builder("id", 1).setString("name", "product1");
        Param data2 = Param.builder("id", 2).setString("name", "product2");
        // 一级目录
        List<Param> children1 = new ArrayList<Param>();
        Param data11 = Param.builder("id", 11).setString("name", "product11");
        Param data12 = Param.builder("id", 12).setString("name", "product12");
        children1.add(data11);
        children1.add(data12);
        data1.setList("children", children1);
        // 二级目录
        List<Param> children11 = new ArrayList<Param>();
        Param data111 = Param.builder("id", 111).setString("name", "product111");
        Param data112 = Param.builder("id", 112).setString("name", "product112");
        children11.add(data111);
        children11.add(data112);
        data11.setList("children", children11);
        dataList.add(data1);
        dataList.add(data2);
        cache.put(key, dataList, ProtoSchema.listSchema(Param.class, schema));
        System.out.println(cache.getList(key, ProtoSchema.listSchema(Param.class, schema)));
    }

    @Test
    public void testPutObject7() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        String key = "MyKey";
        ProtoSchema schema = ProtoSchema.mapSchema();
        schema.addKey("id", Integer.class);
        schema.addKey("name", String.class);
        schema.addKey("children", Table.class, ProtoSchema.listSchema(Param.class, schema));
        List<Param> dataList = new ArrayList<Param>();
        Param data1 = Param.builder("id", 1).setString("name", "product1");
        Param data2 = Param.builder("id", 2).setString("name", "product2");
        // 一级目录
        Table<Param> children1 = Table.builder();
        Param data11 = Param.builder("id", 11).setString("name", "product11");
        Param data12 = Param.builder("id", 12).setString("name", "product12");
        children1.add(data11);
        children1.add(data12);
        data1.setTable("children", children1);
        // 二级目录
        Table<Param> children11 = Table.builder();
        Param data111 = Param.builder("id", 111).setString("name", "product111");
        Param data112 = Param.builder("id", 112).setString("name", "product112");
        children11.add(data111);
        children11.add(data112);
        data11.setTable("children", children11);
        dataList.add(data1);
        dataList.add(data2);
        cache.put(key, dataList, ProtoSchema.listSchema(Param.class, schema));
        System.out.println(cache.getList(key, ProtoSchema.listSchema(Param.class, schema)));
    }

    @Test
    public void testRemove() {
        CacheConfig config = new CacheConfig();
        JvmConfig jvmConfig = config.getJvmConfig();
        jvmConfig.setExpirationTimeRandom(true);
        jvmConfig.setExpirationTimeRandomMin(10 * 1000);
        jvmConfig.setExpirationTimeRandomMax(30 * 1000);
        JvmCache cache = new JvmCache(config);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            String key = "MyKey" + i;
            String value = "MyValue" + i;
            cache.put(key, value);
//			System.out.println(cache.getString(key));
        }
        for (int i = 0; i < 10; i++) {
            String key = "MyKey" + i;
            cache.remove(key);
//            System.out.println(cache.size());
        }
        Assert.assertTrue(cache.size() == 0);
        System.out.println("batch execute: " + (System.currentTimeMillis() - start));
    }

    /**
     * 测试二级缓存存储
     */
    @Test
    public void testHPutString() throws Exception {
        long start = System.currentTimeMillis();
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        String key = "MyKey";
        for (int i = 0; i < 10000; i++) {
            String field = "MyKey" + i;
            cache.hput(key, field, "MyValue");
            cache.hgetString(key, field);
        }
        System.out.println(cache.size());
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testHPutObject() throws Exception {
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        long start = System.currentTimeMillis();
        String key = "MyKey";
        for (int i = 0; i < 100000; i++) {
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
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
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

    @Test
    public void testHPutObjectMaxMemory() throws Exception {
        CacheConfig config = new CacheConfig();
        JvmConfig jvmConfig = config.getJvmConfig();
        jvmConfig.setMaxMemory(1024 * 1024 * 30);
        JvmCache cache = new JvmCache(config);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 5000; i++) {
            String key = "MyKey" + i;
            for (int j = 0; j < 100; j++) {
                String field = "MyKey" + j;
                Product value = new Product(j, "MyProduct");
                ProtoSchema schema = ProtoSchema.getSchema(Product.class);
                cache.hput(key, field, value, schema);
                cache.hgetObject(key, field, Product.class, schema);
            }
        }
        System.out.println(cache);
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testHRemove() {
        long start = System.currentTimeMillis();
        CacheConfig config = new CacheConfig();
        Cache cache = new JvmCache(config);
        String key = "MyKey";
        for (int i = 0; i < 10; i++) {
            String field = "MyField" + i;
            String value = "MyValue" + i;
            cache.hput(key, field, value);
//            System.out.println(cache.hgetString(key, field));
        }
        for (int i = 0; i < 10; i++) {
            String field = "MyField" + i;
            cache.remove(key, field);
        }
        Assert.assertTrue(cache.size() == 0);
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
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

    public static class MyListener extends JvmCacheListenerAdapter {
        private int count = 0;

        @Override
        public void cacheEvicted(String key, Element value) {
//			System.out.println("cache:" + key + " evict" + count++);
        }

        public int getCount() {
            return count;
        }
    }
}
