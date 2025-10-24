package cloud.apposs.cachex;

import cloud.apposs.cachex.CacheXConfig.DbConfig;
import cloud.apposs.cachex.database.*;
import cloud.apposs.cachex.jdbc.*;
import cloud.apposs.util.Convertor;
import cloud.apposs.util.Null;
import cloud.apposs.util.Ref;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestJdbcDao {
    public static final String TABLE = "user";
    public static final String DIALECT = CacheXConstants.DIALECT_MYSQL;
    //	public static final String DIALECT = SqlBuilder.DIALECT_SQLITE;
    public static final String MYSQL_URL =
            "jdbc:mysql://192.168.0.102:9091/product?useUnicode=true&characterEncoding=UTF8";
    public static final String SQLITE_URL = "jdbc:sqlite://C:/world0.tdb";
    public static String JDBC_URL = MYSQL_URL;

    static {
        if (DIALECT.equals(CacheXConstants.DIALECT_MYSQL)) {
            JDBC_URL = MYSQL_URL;
        } else if (DIALECT.equals(CacheXConstants.DIALECT_SQLITE)) {
            JDBC_URL = SQLITE_URL;
        }
    }

    private DbPool pool;
    private Dao dao;

    @Before
    public void before() throws Exception {
        DbConfig dbCfg = new DbConfig();
        dbCfg.setJdbcUrl(DIALECT);
        dbCfg.setJdbcUrl(JDBC_URL);
        dbCfg.setUsername("root");
        dbCfg.setPassword("root");
        dbCfg.setDebuggable(true);
        dbCfg.setTestConnectionOnCheckout(false);
        dbCfg.setPoolOperationWatch(false);
        pool = new DbPool(dbCfg);
        dao = pool.getDao();
    }

    @After
    public void after() throws Exception {
        if (dao != null) {
            dao.close();
        }
        if (pool != null) {
            pool.shutdown();
        }
    }

    @Test
    public void testExists() throws Exception {
        Metadata metadata = new Metadata("user");
        TestCase.assertTrue(dao.exist(metadata));
    }

    @Test
    public void testCreate() throws Exception {
        Metadata metadata = new Metadata("user", "UTF8");
        metadata.addColumn("id", Metadata.COLUMN_TYPE_INT, 11, true, true, null);
        metadata.addColumn("name", Metadata.COLUMN_TYPE_STRING, 50, false, null);
        metadata.addColumn("class", Metadata.COLUMN_TYPE_INT, 11);
        metadata.addColumn("pwd", Metadata.COLUMN_TYPE_BINARY, 16);
        TestCase.assertTrue(dao.create(metadata, true));

        metadata = new Metadata("product", "UTF8");
        metadata.addColumn("id", Metadata.COLUMN_TYPE_INT, 11, true, true, null);
        metadata.addColumn("uid", Metadata.COLUMN_TYPE_INT, 11);
        metadata.addColumn("name", Metadata.COLUMN_TYPE_STRING, 50, true, null);
        TestCase.assertTrue(dao.create(metadata, true));
    }

    @Test
    public void testSelect() throws Exception {
        Entity datas = dao.select(TABLE);
        String pwd = Convertor.bytes2hex(datas.getBytes("pwd"));
        Assert.assertNotNull(pwd);
    }

    @Test
    public void testQuery() throws Exception {
        long start = System.currentTimeMillis();
        List<Entity> datas = dao.query(TABLE);
        System.out.println(datas);
        System.out.println("Execute Time:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testQueryLike() throws Exception {
        Query query = new Query();
        query.where("name", Where.LK, "way");
        List<Entity> datas = dao.query(TABLE, query);
        System.out.println(datas);
    }

    @Test
    public void testQueryNotNull() throws Exception {
        Query query = new Query();
        query.where("name", Where.ISNOT, Null.builder());
        List<Entity> datas = dao.query(TABLE, query);
        System.out.println(datas);
    }

    @Test
    public void testQueryBatch() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Query query = new Query().limit(1, 5);
            dao.query(TABLE, query);
        }
        System.out.println("batch execute:" + (System.currentTimeMillis() - start));
    }

    /**
     * 测试表查询查询，
     * product插入语句
     * insert into product (uid, name) values (1, "iphone");
     * insert into product (uid, name) values (1, "xiaomi");
     * insert into product (uid, name) values (1, "huawei");
     * @throws Exception
     */
    @Test
    public void testJoin() throws Exception {
        ColumnLabel uname = ColumnLabel.create("user", "name", "uname");
        ColumnLabel pname = ColumnLabel.create("product", "name", "pname");
        Query query = new Query(uname, pname);
        ColumnLabel userId = ColumnLabel.create("user", "id");
        ColumnLabel puid = ColumnLabel.create("product", "uid");
        query.join("product").on(userId, Where.EQ, puid).and(userId, Where.EQ, 1);
        List<Entity> datas = dao.query(TABLE, query);
        System.out.println(datas);
    }

    @Test
    public void testUpdate() throws Exception {
        Entity entity = dao.select(TABLE, "id");
        entity.setString("name", "wayken");
        int count = dao.update(TABLE, entity);
        TestCase.assertTrue(count > 0);
    }

    @Test
    public void testUpdateBatch() throws Exception {
        Entity entity0 = new Entity("id");
        entity0.setIdentity(1);
        entity0.setString("name", "wayken0");
        Entity entity1 = new Entity("id");
        entity1.setIdentity(2);
        entity1.setString("name", "wayken1");
        List<Entity> entities = new LinkedList<Entity>();
        entities.add(entity0);
        entities.add(entity1);
        int count = dao.update(TABLE, entities);
        TestCase.assertTrue(entities.size() == count);
    }

    @Test
    public void testUpdater() throws Exception {
        Updater u = new Updater("name", "qun");
        u.add("class", 111);
        u.where("id", Where.EQ, 1);
        int count = dao.update(TABLE, u);
        System.out.println(count);
    }

    @Test
    public void testUpdaterNull() throws Exception {
        Updater u = new Updater("name", Null.builder());
        u.add("class", 111);
        u.where("id", Where.EQ, 1);
        int count = dao.update(TABLE, u);
        System.out.println(count);
    }

    @Test
    public void testInsert() throws Exception {
        Entity e = new Entity("id");
        e.setInt("class", 100);
        e.setString("name", "way100");
        e.setBytes("pwd", Convertor.hex2bytes("872be7378d2e5c4b747f2547144c6dc5"));
        Ref<Object> idRef = new Ref<Object>();
        int count = dao.insert(TABLE, e, idRef);
        TestCase.assertTrue(count > 0);
    }

    @Test
    public void testInsertBatch() throws Exception {
        Entity e0 = new Entity("id");
        e0.setInt("class", 101);
        e0.setString("name", "way101");
        Entity e1 = new Entity("id");
        e1.setInt("class", 102);
        e1.setString("name", "way102");
        List<Entity> all = new LinkedList<Entity>();
        all.add(e0);
        all.add(e1);
        List<Object> idList = new LinkedList<Object>();
        int count = dao.insert(TABLE, all, idList);
        TestCase.assertTrue(all.size() == count);
    }

    @Test
    public void testReplace() throws Exception {
        Entity e = new Entity("id", 1);
        e.setInt("class", 101);
        e.setString("name", "way102");
        Ref<Object> idRef = new Ref<Object>();
        int count = dao.replace(TABLE, e, idRef);
        System.out.println(idRef.value());
        System.out.println(count);
        TestCase.assertTrue(count > 0);
    }

    @Test
    public void testDelete() throws Exception {
        Entity e = new Entity("id");
        e.setIdentity(4);
        int count = dao.delete(TABLE, e);
        TestCase.assertTrue(count > 0);
    }

    @Test
    public void testDeleteWhere() throws Exception {
        Where where = new Where("id", Where.EQ, 17);
        int count = dao.delete(TABLE, where);
        System.out.println(count);
    }

    @Test
    public void testDeleteBatch() throws Exception {
        Entity e0 = new Entity("id");
        e0.setIdentity(11);
        Entity e1 = new Entity("id");
        e1.setIdentity(12);
        List<Entity> all = new LinkedList<Entity>();
        all.add(e0);
        all.add(e1);
        int count = dao.delete(TABLE, all);
        System.out.println(count);
    }

    /**
     * 测试事务提交与回滚
     */
    @Test
    public void testTransaction() throws Exception {
        Entity e = new Entity("id");
        e.setIdentity(3);
        e.setString("name", "wayken");
        try {
            dao.setAutoCommit(false);
            int count = dao.update(TABLE, e);
            int i = 12 / 0;
            dao.commit();
        } catch (Exception e1) {
            dao.rollback();
        }
    }
}
