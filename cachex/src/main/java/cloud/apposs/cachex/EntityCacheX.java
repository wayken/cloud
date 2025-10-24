package cloud.apposs.cachex;

import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于数据类型为{@link Entity}的数据服务
 */
public class EntityCacheX<K extends CacheKey> extends AbstractCacheX<K, Entity> {
    /**
     * 数据库查询不存在时的缓存，用于将不存在(NOT FOUND)的数据进行缓存避免数据一直查询
     */
    public static final Entity DATA_NOT_FOUND = new Entity();

    public EntityCacheX(CacheXConfig config, CacheLoader<K, Entity> loader) throws Exception {
        super(config, loader);
    }

    public EntityCacheX(CacheXConfig config, CacheLoader<K, Entity> loader, int lockLength) throws Exception {
        super(config, loader, lockLength);
    }

    /**
     * 对查询的Schema进行再封装，减少业务关心太多细节
     * {@inheritDoc}
     */
    @Override
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception {
        return super.query(key, query, ProtoSchema.listSchema(Param.class, schema), args);
    }

    @Override
    public boolean doPut(String key, Entity value, ProtoSchema schema, Object... args) {
        Param fieldData = value.getDatas();
        return cache.put(key, fieldData, schema);
    }

    @Override
    protected boolean doPutList(String key, List<Entity> values, ProtoSchema schema, Object... args) {
        List<Param> fileDatas = new ArrayList<Param>(values.size());
        for (Entity value : values) {
            fileDatas.add(value.getDatas());
        }
        return cache.put(key, fileDatas, schema);
    }

    @Override
    protected boolean doPutList(List<String> keys, List<Entity> values, ProtoSchema schema, Object... args) {
        List<ProtoBuf> pvalues = new ArrayList<ProtoBuf>(values.size());
        for (Entity value : values) {
            pvalues.add(ProtoBuf.wrap(value, schema));
        }
        return cache.put(keys, pvalues);
    }

    @Override
    public Entity doGet(String key, ProtoSchema schema, Object... args) {
        Param data = cache.getParam(key, schema);
        if (data == null) {
            return null;
        }
        return new Entity(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> doGetList(String key, ProtoSchema schema, Object... args) {
        Table<Param> values = (Table<Param>) cache.getTable(key, schema);
        Table<Entity> entities = new Table<Entity>();
        for (Param value : values) {
            entities.add(new Entity(value));
        }
        return entities;
    }

    @Override
    public Table<Entity> doGetList(List<String> keys, ProtoSchema schema, Object... args) {
        List<ProtoBuf> bufferList = cache.getBufferList(keys);
        Table<Entity> values = new Table<Entity>();
        for (ProtoBuf protoBuf : bufferList) {
            if (protoBuf == null) {
                values.add(null);
            } else {
                values.add(new Entity(protoBuf.getParam(schema)));
            }
        }
        return values;
    }

    @Override
    public boolean checkExist(Entity value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public boolean doHputAll(String key, List<Entity> value, ProtoSchema schema, Object... args) {
        Charset charset = config.getChrset();
        Map<byte[], byte[]> infoList = new HashMap<byte[], byte[]>();
        for (Entity entity : value) {
            String mapKey = loader.getField(entity);
            if (mapKey == null) {
                throw new IllegalStateException("CacheLoader get field null error");
            }
            Param data = entity.getDatas();
            ProtoBuf buffer = ProtoBuf.wrap(data, schema);
            infoList.put(mapKey.getBytes(charset), buffer.array());
        }
        return cache.hmput(key, infoList);
    }

    @Override
    public boolean doHput(String key, Object field, Entity value, ProtoSchema schema, Object... args) {
        Param data = value.getDatas();
        return cache.hput(key, field.toString(), data, schema);
    }

    @Override
    public boolean checkHExist(Entity value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public Entity doHget(String key, Object field, ProtoSchema schema, Object... args) {
        Param data = cache.hgetParam(key, field.toString(), schema);
        return new Entity(data);
    }

    @Override
    public Table<Entity> doHgetAll(String key, ProtoSchema schema, Object... args) {
        Table<Param> dataList = cache.hgetParamTable(key, schema);
        if (dataList == null) {
            return null;
        }
        Table<Entity> entityList = new Table<Entity>();
        for (Param data : dataList) {
            entityList.add(new Entity(data));
        }
        return entityList;
    }
}
