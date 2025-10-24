package cloud.apposs.cachex;

import cloud.apposs.cachex.database.*;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCacheXH {
    public static final String SQLITE_URL = "jdbc:sqlite://C:/files.tdb";
    public static final String TABLE = "files";

    public static final int KEY_AID = 854;
    public static final String KEY_FILEID = "AD0IzYzEBxACGAAguZqM0gUoyv-tDzDWAzjAAg";
    public static final String KEY_FILEID2 = "AD0I34TEBxACGAAgiLON0gUooOC3ogUw5gE4ggE";

    // 表字段
    public static final String FIELD_AID = "aid";
    public static final String FIELD_FILEID = "fileId";
    public static final String FIELD_SIZE = "size";
    // 表元信息
    public static final ProtoSchema FILES_SCHEMA = ProtoSchema.mapSchema();

    static {
        FILES_SCHEMA.addKey(FIELD_AID, Integer.class);
        FILES_SCHEMA.addKey(FIELD_FILEID, String.class);
        FILES_SCHEMA.addKey(FIELD_SIZE, Integer.class);
    }

    private ParamCacheX<MyCacheKey> cachex;

    @Before
    public void before() throws Exception {
        CacheXConfig config = new CacheXConfig();
        config.setDevelop(true);
        config.getDbConfig().setJdbcUrl(SQLITE_URL);
        config.getDbConfig().setDialect(CacheXConstants.DIALECT_SQLITE);
        config.getDbConfig().setUsername("root");
        config.getDbConfig().setPassword("root");
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
        Metadata metadata = new Metadata(TABLE, "UTF8");
        metadata.addColumn(FIELD_AID, Metadata.COLUMN_TYPE_INT, 11, true);
        metadata.addColumn(FIELD_FILEID, Metadata.COLUMN_TYPE_STRING, 50, true, null);
        metadata.addColumn(FIELD_SIZE, Metadata.COLUMN_TYPE_INT, 11);
        if (!cachex.getTemplate().exist(metadata)) {
            cachex.getTemplate().create(metadata, false);
        }
    }

    @Test
    public void testHPut() throws Exception {
        Param data = new Param();
        data.setInt(FIELD_AID, KEY_AID);
        data.setString(FIELD_FILEID, KEY_FILEID);
        data.setInt(FIELD_SIZE, 1589600);
        // 因为AID是靠数据库递增，所以 一开始并不知道数值
        MyCacheKey key = new MyCacheKey(854);
        cachex.hput(key, KEY_FILEID, data, FILES_SCHEMA);

        Param data2 = new Param();
        data2.setInt(FIELD_AID, KEY_AID);
        data2.setString(FIELD_FILEID, KEY_FILEID2);
        data2.setInt(FIELD_SIZE, 1589600);
        // 因为AID是靠数据库递增，所以 一开始并不知道数值
        cachex.hput(key, KEY_FILEID2, data2, FILES_SCHEMA);

        System.out.println(cachex.hget(key, KEY_FILEID, FILES_SCHEMA));
    }

    @Test
    public void testHGet() throws Exception {
        MyCacheKey key = new MyCacheKey(KEY_AID);
        cachex.hget(key, KEY_FILEID, FILES_SCHEMA);
        System.out.println(cachex.hget(key, KEY_FILEID, FILES_SCHEMA));
    }

    @Test
    public void testHGetAll() throws Exception {
        MyCacheKey key = new MyCacheKey(KEY_AID);
        System.out.println(cachex.hgetAll(key, FILES_SCHEMA));
        System.out.println(cachex.hgetAll(key, FILES_SCHEMA));
    }

    @Test
    public void testDelete() throws Exception {
        MyCacheKey key = new MyCacheKey(KEY_AID);
        cachex.hdelete(key, KEY_FILEID);
        cachex.hget(key, KEY_FILEID, FILES_SCHEMA);
        System.out.println(cachex.hget(key, KEY_FILEID, FILES_SCHEMA));
    }

    @Test
    public void testDeleteAll() throws Exception {
        MyCacheKey key = new MyCacheKey(KEY_AID);
        cachex.hdelete(key, KEY_FILEID, KEY_FILEID2);
        System.out.println(cachex.hgetAll(key, FILES_SCHEMA));
    }

    @Test
    public void testUpdate() throws Exception {
        MyCacheKey key = new MyCacheKey(KEY_AID);
        Param value = new Param();
        value.setInt("size", 1899991225);
        cachex.hupdate(key, KEY_FILEID, value, FILES_SCHEMA);
        System.out.println(cachex.hget(key, KEY_FILEID, FILES_SCHEMA));
    }

    static class MyCacheKey extends AbstractCacheKey {
        private static final String CACHE_KEY_PREFIX = "site";

        private final int aid;

        public MyCacheKey(int aid) {
            this.aid = aid;
        }

        public int getAid() {
            return aid;
        }

        @Override
        public String getCacheKey() {
            return CACHE_KEY_PREFIX + "-" + aid;
        }
    }

    static class ParamCacheLoader extends CacheLoaderAdapter<MyCacheKey, Param> {
        @Override
        public void initialize(CacheX<MyCacheKey, Param> cachex) {
            System.out.println("CacheX Init");
        }

        @Override
        public int hadd(MyCacheKey key, Object field, Param value,
                        ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
            return template.insert(TABLE, new Entity(value), idRef);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Table<Param> hload(MyCacheKey key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            // 查询数据加载到缓存中
            Query query = new Query();
            query.where(FIELD_AID, Where.EQ, key.getAid());
            Table infoList = template.query(TABLE, query);
            return infoList;
        }

        @Override
        public int hdelete(MyCacheKey key, Object field, DBTemplate template, Object... args) throws Exception {
            // 简单匹配的AID和FILEID
            Where where = new Where(FIELD_AID, Where.EQ, key.getAid());
            where.and(FIELD_FILEID, Where.EQ, field);
            return template.delete(TABLE, where);
        }

        @Override
        public int hdelete(MyCacheKey key, Object[] fields, DBTemplate template, Object... args) throws Exception {
            // 简单匹配的AID和FILEID
            Where where = new Where(FIELD_AID, Where.EQ, key.getAid());
            where.and(FIELD_FILEID, Where.IN, fields);
            return template.delete(TABLE, where);
        }

        @Override
        public int hupdate(MyCacheKey key, Object field, Param value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
            // 简单更新实体数据
            Updater updater = new Updater();
            updater.addAll(value);
            updater.where(FIELD_AID, Where.EQ, key.getAid());
            updater.where().and(FIELD_FILEID, Where.EQ, field);
            return template.update(TABLE, updater);
        }

        @Override
        public String getField(Param info) {
            return info.getString(FIELD_FILEID);
        }
    }
}
