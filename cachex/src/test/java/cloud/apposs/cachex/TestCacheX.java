package cloud.apposs.cachex;

import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Metadata;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCacheX {
    public static final String SQLITE_URL = "jdbc:sqlite://C:/user.tdb";
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/teambeit?useUnicode=true&characterEncoding=UTF8&connectTimeout=1000&socketTimeout=3000";
    public static final String MONGODB_URL = "mongodb://root:root@192.168.5.67:27017/teambeit";

    public static final String SWITCH_DIALOG = "mongodb";

    //	public static final String HOST = "172.17.1.225";
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 3306;

    public static final String REDIS_HOST = "172.17.35.166";
    public static final int REDIS_PORT = 6031;

    // 表字段
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CLASS = "class";
    // 表元信息
    public static final ProtoSchema USER_SCHEMA = ProtoSchema.mapSchema();

    static {
        USER_SCHEMA.addKey(FIELD_ID, Integer.class);
        USER_SCHEMA.addKey(FIELD_NAME, String.class);
        USER_SCHEMA.addKey(FIELD_CLASS, Integer.class);
    }

    private ParamCacheX<MyCacheKey> cachex;

    @Before
    public void before() throws Exception {
        CacheXConfig config = new CacheXConfig();
        config.setDevelop(true);
        if (SWITCH_DIALOG.equals("mysql")) {
            config.getDbConfig().setJdbcUrl(MYSQL_URL);
            config.getDbConfig().setDialect(CacheXConstants.DIALECT_MYSQL);
        } else if (SWITCH_DIALOG.equals("sqlite")) {
            config.getDbConfig().setJdbcUrl(SQLITE_URL);
            config.getDbConfig().setDialect(CacheXConstants.DIALECT_SQLITE);
        } else if (SWITCH_DIALOG.equals("mongodb")) {
            config.getDbConfig().setDatabaseName("teambeit");
            config.getDbConfig().setJdbcUrl(MONGODB_URL);
            config.getDbConfig().setDialect(CacheXConstants.DIALECT_MONGODB);
        }

        config.getDbConfig().setUsername("root");
        config.getDbConfig().setPassword("root");

//		config.setCache(Cache.CACHE_REDIS);
//		CacheXConfig.RedisConfig redisConfig = config.getRedisConfig();
//		redisConfig.addServer(new CacheXConfig.RedisConfig.RedisServer(REDIS_HOST, REDIS_PORT));
//		redisConfig.setCacheType(CacheXConfig.RedisConfig.REDIS_CACHE_SINGLE);

        config.setWriteBehind(false);
        ParamCacheLoader loader = new ParamCacheLoader();
        cachex = new ParamCacheX<MyCacheKey>(config, loader);
    }

    @After
    public void after() throws Exception {
        cachex.shutdown();
    }

    @Test
    public void testCreate() throws Exception {
        Metadata metadata = new Metadata("user", "UTF8");
        metadata.addPrimaryColumn(FIELD_ID, Metadata.COLUMN_TYPE_INT, 11);
        metadata.addColumn(FIELD_NAME, Metadata.COLUMN_TYPE_STRING, 50, true, null);
        metadata.addColumn(FIELD_CLASS, Metadata.COLUMN_TYPE_INT, 11);
        if (!cachex.getTemplate().exist(metadata)) {
            cachex.getTemplate().create(metadata, false);
        }
    }

    @Test
    public void testPut() throws Exception {
        Param data = new Param();
        data.setString(FIELD_NAME, "John");
        data.setInt(FIELD_CLASS, 101);
        // 因为AID是靠数据库递增，所以 一开始并不知道数值
        MyCacheKey key = new MyCacheKey();
        cachex.put(key, data, USER_SCHEMA);
        System.out.println(cachex.get(key, USER_SCHEMA));
    }

    @Test
    public void testPutBatch() throws Exception {
        List<Param> values = new ArrayList<Param>();
        Param data1 = new Param();
        data1.setString(FIELD_NAME, "Donald");
        data1.setInt(FIELD_CLASS, 101);
        values.add(data1);
        Param data2 = new Param();
        data2.setString(FIELD_NAME, "Lily");
        data2.setInt(FIELD_CLASS, 101);
        values.add(data2);
        // 因为AID是靠数据库递增，所以 一开始并不知道数值
        List<MyCacheKey> keys = new ArrayList<MyCacheKey>();
        for (int i = 0; i < values.size(); i++) {
            keys.add(new MyCacheKey());
        }
        int count = cachex.put(keys, values, USER_SCHEMA);
        System.out.println(count);
    }

    @Test
    public void testGet() throws Exception {
        MyCacheKey key = new MyCacheKey(1);
        cachex.get(key, USER_SCHEMA);
        System.out.println(cachex.get(key, USER_SCHEMA));
    }

    @Test
    public void testSelect() throws Exception {
        MyCacheKey.QueryKey key = new MyCacheKey.QueryKey("select");
        Query query = Query.builder();
        query.where(FIELD_CLASS, Where.EQ, 101);
        Param data = cachex.select(key, query, USER_SCHEMA);
        Param data2 = cachex.select(key, query, USER_SCHEMA);
        System.out.println(data);
        System.out.println(data2);
    }

    @Test
    public void testQuery() throws Exception {
        MyCacheKey.QueryKey key = new MyCacheKey.QueryKey("search1");
        Query query = Query.builder();
        query.where(FIELD_CLASS, Where.EQ, 101);
        List<Param> dataList = cachex.query(key, query, USER_SCHEMA, true);
        List<Param> dataList2 = cachex.query(key, query, USER_SCHEMA, true);
        System.out.println(dataList);
        System.out.println(dataList2);
    }

    /**
     * 测试通过遍历来批量加载数据
     */
    @Test
    public void testGetBatch() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MyCacheKey key = new MyCacheKey(1);
            cachex.get(key, USER_SCHEMA);
            MyCacheKey key2 = new MyCacheKey(2);
            cachex.get(key2, USER_SCHEMA);
            MyCacheKey key3 = new MyCacheKey(3);
            cachex.get(key3, USER_SCHEMA);
        }
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    /**
     * 测试通过将查询封装成一个集合来批量加载数据
     */
    @Test
    public void testGetBatch2() throws Exception {
        long start = System.currentTimeMillis();
        List<MyCacheKey> keys = new ArrayList<MyCacheKey>();
        keys.add(new MyCacheKey(1));
        keys.add(new MyCacheKey(2));
        keys.add(new MyCacheKey(3));
        for (int i = 0; i < 10000; i++) {
            cachex.get(keys, USER_SCHEMA);
        }
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testQueryIn() throws Exception {
        long start = System.currentTimeMillis();
        Query query = Query.builder();
        Integer[] inlist = new Integer[2];
        inlist[0] = 1;
        inlist[1] = 2;
        query.where("id", Where.IN, inlist);
        for (int i = 0; i < 5000; i++) {
            cachex.query(NoCacheKey.getInstance(), query, USER_SCHEMA);
        }
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testDelete() throws Exception {
        MyCacheKey key = new MyCacheKey(1);
        cachex.delete(key);
        System.out.println(cachex.get(key, USER_SCHEMA));
    }

    @Test
    public void testDeleteBatch() throws Exception {
        List<MyCacheKey> keys = new ArrayList<MyCacheKey>();
        keys.add(new MyCacheKey(10));
        keys.add(new MyCacheKey(11));
        int count = cachex.delete(keys);
        Assert.assertTrue(count == 2);
    }

    @Test
    public void testDeleteWhere() throws Exception {
        Where where = Where.builder("name", Where.EQ, "Wayken100");
        int count = cachex.delete(NoCacheKey.getInstance(), where);
        Assert.assertTrue(count == 1 || count == 0);
    }

    @Test
    public void testUpdate() throws Exception {
        MyCacheKey key = new MyCacheKey(2);
        Param value = new Param();
        value.setString("name", "WayKen100");
        cachex.update(key, value, USER_SCHEMA);
        System.out.println(cachex.get(key, USER_SCHEMA));
    }

    @Test
    public void testGetArgs() throws Exception {
        MyCacheKey key = new MyCacheKey(1);
        int productId = 10081;
        cachex.get(key, USER_SCHEMA, productId);
        System.out.println(cachex.get(key, USER_SCHEMA));
    }

    static class MyCacheKey extends AbstractCacheKey<Integer> {
        private static final String CACHE_KEY_PREFIX = "site-";

        public MyCacheKey() {
            super(CACHE_KEY_PREFIX);
        }

        public MyCacheKey(int aid) {
            super(aid, CACHE_KEY_PREFIX);
        }

        static class QueryKey extends AbstractCacheKey<String> {
            private final String key;

            public QueryKey(String key) {
                this.key = key;
            }

            @Override
            public String getCacheKey() {
                return key;
            }
        }
    }

    static class ParamCacheLoader extends CacheLoaderAdapter<MyCacheKey, Param> {
        private static final String TABLE = "user";

        @Override
        public int add(MyCacheKey key, Param value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
            int count = template.insert(TABLE, new Entity(value), idRef);
            return count;
        }

        @Override
        public int add(List<MyCacheKey> keys, List<Param> values, ProtoSchema schema, DBTemplate template, List<Object> idRefs, Object... args) throws Exception {
            List<Entity> datas = new ArrayList<Entity>();
            for (Param value : values) {
                datas.add(new Entity(value));
            }
            int count = template.insert(TABLE, datas, schema, idRefs);
            return count;
        }

        @Override
        public Param load(MyCacheKey key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            if (args != null && args.length > 0) {
                System.out.println("loader recv args:" + args[0]);
            }
            // 查询数据加载到缓存中
            int aid = key.getPrimary();
            Entity info = template.select(TABLE, FIELD_ID, aid);
            if (info == null) {
                return ParamCacheX.DATA_NOT_FOUND;
            }
            return info.getDatas();
        }

        @Override
        public Param select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            return template.select(TABLE, query);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Table<Param> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            Table datas = template.query(TABLE, query);
            return datas;
        }

        @Override
        public int delete(MyCacheKey key, DBTemplate template, Object... args) throws Exception {
            // 简单删除主键
            return template.delete(TABLE, FIELD_ID, key.getPrimary());
        }

        @Override
        public int delete(List<MyCacheKey> keys, DBTemplate template, Object... args) throws Exception {
            List<Entity> idList = new ArrayList<Entity>();
            for (MyCacheKey key : keys) {
                Entity entity = new Entity(FIELD_ID);
                entity.setIdentity(key.getPrimary());
                idList.add(entity);
            }
            return template.delete(TABLE, idList);
        }

        @Override
        public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
            return template.delete(TABLE, where);
        }

        @Override
        public int update(MyCacheKey key, Param value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            // 简单更新实体数据
            Entity entity = new Entity(FIELD_ID);
            entity.setIdentity(key.getPrimary());
            entity.putAll(value);
            return template.update(TABLE, entity);
        }
    }
}
