package cloud.apposs.cachex.jdbc;

import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.cachex.DBTemplate;
import cloud.apposs.cachex.database.*;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

import java.util.List;

public class JDBCTemplate implements DBTemplate {
    private DbPool pool;

    public JDBCTemplate(CacheXConfig.DbConfig config) throws Exception {
        this.pool = new DbPool(config);
    }

    @Override
    public Entity select(String table, String primary, Object identity, ProtoSchema schema, Query query) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.select(table, primary, identity, schema, query);
        } finally {
            dao.close();
        }
    }

    @Override
    public Table<Entity> query(String table, String primary, ProtoSchema schema, Query query) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.query(table, primary, schema, query);
        } finally {
            dao.close();
        }
    }

    @Override
    public int update(String table, Entity entity, ProtoSchema schema) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.update(table, entity, schema);
        } finally {
            dao.close();
        }
    }

    @Override
    public int update(String table, List<Entity> entities, ProtoSchema schema) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.update(table, entities, schema);
        } finally {
            dao.close();
        }
    }

    @Override
    public int update(String table, Updater updater) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.update(table, updater);
        } finally {
            dao.close();
        }
    }

    @Override
    public int delete(String table, Entity entity) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.delete(table, entity);
        } finally {
            dao.close();
        }
    }

    @Override
    public int delete(String table, String primary, Object identity) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.delete(table, primary, identity);
        } finally {
            dao.close();
        }
    }

    @Override
    public int delete(String table, List<Entity> entities) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.delete(table, entities);
        } finally {
            dao.close();
        }
    }

    @Override
    public int delete(String table, Where where) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.delete(table, where);
        } finally {
            dao.close();
        }
    }

    @Override
    public int insert(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.insert(table, entity, schema, idRef);
        } finally {
            dao.close();
        }
    }

    @Override
    public int insert(String table, List<Entity> entities, ProtoSchema schema, List<Object> idList) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.insert(table, entities, schema, idList);
        } finally {
            dao.close();
        }
    }

    @Override
    public int replace(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.replace(table, entity, schema, idRef);
        } finally {
            dao.close();
        }
    }

    @Override
    public boolean create(Metadata metadata, boolean dropIfExist) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.create(metadata, dropIfExist);
        } finally {
            dao.close();
        }
    }

    @Override
    public boolean exist(Metadata metadata) throws Exception {
        Dao dao = pool.getDao();
        try {
            return dao.exist(metadata);
        } finally {
            dao.close();
        }
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }
}
