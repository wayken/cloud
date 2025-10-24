package cloud.apposs.cachex.mongodb;

import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.cachex.DBTemplate;
import cloud.apposs.cachex.database.*;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.StrUtil;
import cloud.apposs.util.Table;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MongoTemplate implements DBTemplate {
    private final MongoClient mongoClient;

    private final MongoDatabase database;

    public MongoTemplate(CacheXConfig.DbConfig configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Database configuration is null");
        }
        if (configuration.getDatabaseName() == null) {
            throw new IllegalArgumentException("Database name is null");
        }
        String uri = configuration.getJdbcUrl();
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(configuration.getDatabaseName());
    }

    @Override
    public Entity select(String table, String primary, Object identity, ProtoSchema schema, Query query) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("Table name is empty");
        }
        MongoCollection<Document> collection = database.getCollection(table);
        // 如果没有涉及分析和联表查询则用普通查询，否则用聚合查询
        // 1. 普通查询更轻量，执行路径短，适合高频实时查询
        // 2. 聚合查询功能更强，但执行链条长，性能较差，适合分析类查询
        if (query == null || (query.groupBy() == null && query.joins().isEmpty())) {
            FindIterable<Document> findable = handleDocumentFind(collection, primary, identity, schema, query);
            Document document = findable.first();
            if (document == null) {
                return null;
            }
            return handleDocumentToEntity(document, primary);
        }
        AggregateIterable<Document> aggregate = handleDocumentAggregate(collection, primary, identity, schema, query);
        Document document = aggregate.first();
        if (document == null) {
            return null;
        }
        return handleDocumentToEntity(document, primary);
    }

    @Override
    public Table<Entity> query(String table, String primary, ProtoSchema schema, Query query) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("Table name is empty");
        }
        MongoCollection<Document> collection = database.getCollection(table);
        // 如果没有涉及分析和联表查询则用普通查询，否则用聚合查询
        // 1. 普通查询更轻量，执行路径短，适合高频实时查询
        // 2. 聚合查询功能更强，但执行链条长，性能较差，适合分析类查询
        if (query == null || (query.groupBy() == null && query.joins().isEmpty())) {
            FindIterable<Document> findable = handleDocumentFind(collection, primary, null, schema, query);
            MongoCursor<Document> cursor = findable.iterator();
            Table<Entity> dataList = Table.builder();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Entity entity = handleDocumentToEntity(document, primary);
                dataList.add(entity);
            }
            return dataList;
        }
        AggregateIterable<Document> aggregate = handleDocumentAggregate(collection, primary, null, schema, query);
        MongoCursor<Document> cursor = aggregate.iterator();
        Table<Entity> dataList = Table.builder();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            Entity entity = handleDocumentToEntity(document, primary);
            dataList.add(entity);
        }
        return dataList;
    }

    @Override
    public int update(String table, Entity entity, ProtoSchema schema) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心更新全部就GG了
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }
        Bson filters = handleWhereToFilters(entity.getPrimary(), entity.getIdentity(), null);
        Document document = handleEntityToDocument(entity);
        if (document.isEmpty()) {
            return 0;
        }
        // 移除主键，主键不允许更新
        document.remove(entity.getPrimary());
        if (document.isEmpty()) {
            return 0;
        }

        Bson updates = Updates.combine();
        for (Map.Entry<String, Object> data : document.entrySet()) {
            updates = Updates.combine(updates, Updates.set(data.getKey(), data.getValue()));
        }
        MongoCollection<Document> collection = database.getCollection(table);
        UpdateResult result = collection.updateOne(filters, updates);
        return (int) result.getModifiedCount();
    }

    @Override
    public int update(String table, List<Entity> entities, ProtoSchema schema) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        List<WriteModel<Document>> bulkOperations = new ArrayList<>();
        for (Entity entity : entities) {
            // Entity必须要有主键来做WHERE范围判断，不然一不小心更新全部就GG了
            if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
                throw new IllegalArgumentException("Entity Has No Identity");
            }
            Bson filters = handleWhereToFilters(entity.getPrimary(), entity.getIdentity(), null);
            Document document = handleEntityToDocument(entity);
            if (document.isEmpty()) {
                continue;
            }
            // 移除主键，主键不允许更新
            document.remove(entity.getPrimary());
            if (document.isEmpty()) {
                continue;
            }

            Bson updates = Updates.combine();
            for (Map.Entry<String, Object> data : document.entrySet()) {
                updates = Updates.combine(updates, Updates.set(data.getKey(), data.getValue()));
            }
            UpdateOneModel<Document> updateOneModel = new UpdateOneModel<>(filters, updates);
            bulkOperations.add(updateOneModel);
        }
        if (bulkOperations.isEmpty()) {
            return 0;
        }
        MongoCollection<Document> collection = database.getCollection(table);
        BulkWriteResult result = collection.bulkWrite(bulkOperations);
        return result.getModifiedCount();
    }

    @Override
    public int update(String table, Updater updater) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (updater == null) {
            throw new IllegalArgumentException("updater");
        }
        Bson filters = handleWhereToFilters(null, null, updater.where());
        Document document = handleUpdaterToDocument(updater);
        if (document.isEmpty()) {
            return 0;
        }

        Bson updates = Updates.combine();
        for (Map.Entry<String, Object> data : document.entrySet()) {
            updates = Updates.combine(updates, Updates.set(data.getKey(), data.getValue()));
        }
        MongoCollection<Document> collection = database.getCollection(table);
        UpdateResult result = collection.updateMany(filters, updates);
        return (int) result.getModifiedCount();
    }

    @Override
    public int delete(String table, Entity entity) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }
        Bson filters = handleWhereToFilters(entity.getPrimary(), entity.getIdentity(), null);
        MongoCollection<Document> collection = database.getCollection(table);
        DeleteResult result = collection.deleteMany(filters);
        return (int) result.getDeletedCount();
    }

    @Override
    public int delete(String table, String primary, Object identity) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (StrUtil.isEmpty(primary) || identity == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }
        Bson filters = handleWhereToFilters(primary, identity, null);
        MongoCollection<Document> collection = database.getCollection(table);
        DeleteResult result = collection.deleteMany(filters);
        return (int) result.getDeletedCount();
    }

    @Override
    public int delete(String table, List<Entity> entities) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("entities");
        }
        String primary = entities.get(0).getPrimary();
        for (Entity entity : entities) {
            if (StrUtil.isEmpty(entity.getPrimary()) || !primary.equals(entity.getPrimary())) {
                throw new IllegalArgumentException("Entity Primary No The Same");
            }
            if (entity.getIdentity() == null) {
                throw new IllegalArgumentException("Entity Has No Identity");
            }
        }
        List<ObjectId> ids = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity.getIdentity() instanceof ObjectId) {
                ids.add((ObjectId) entity.getIdentity());
            } else if (entity.getIdentity() instanceof String) {
                ids.add(new ObjectId((String) entity.getIdentity()));
            } else {
                throw new IllegalArgumentException("Entity Identity Type Error");
            }
        }
        Bson filters = Filters.in(primary, ids);
        MongoCollection<Document> collection = database.getCollection(table);
        DeleteResult result = collection.deleteMany(filters);
        return (int) result.getDeletedCount();
    }

    @Override
    public int delete(String table, Where where) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (where == null || where.isEmpty()) {
            throw new IllegalArgumentException("'Where parameter required");
        }

        Bson filters = handleWhereToFilters(null, null, where);
        MongoCollection<Document> collection = database.getCollection(table);
        DeleteResult result = collection.deleteMany(filters);
        return (int) result.getDeletedCount();
    }

    @Override
    public int insert(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("Table name is empty");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity is null");
        }
        Document document = handleEntityToDocument(entity);
        InsertOneResult result = database.getCollection(table).insertOne(document);
        if (result.getInsertedId() != null && idRef != null) {
            idRef.value(result.getInsertedId().asObjectId().getValue());
        }
        return 1;
    }

    @Override
    public int insert(String table, List<Entity> entities, ProtoSchema schema, List<Object> idList) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("Table name is empty");
        }
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("Entities is empty");
        }
        List<Document> documents = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            Document document = handleEntityToDocument(entity);
            documents.add(document);
        }
        InsertManyResult result = database.getCollection(table).insertMany(documents);
        if (result.getInsertedIds() != null && idList != null) {
            for (Map.Entry<Integer, BsonValue> entry : result.getInsertedIds().entrySet()) {
                idList.add(entry.getValue().asObjectId().getValue());
            }
        }
        return documents.size();
    }

    @Override
    public int replace(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心更新全部就GG了
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }
        Bson filters = handleWhereToFilters(entity.getPrimary(), entity.getIdentity(), null);
        Document document = handleEntityToDocument(entity);
        ReplaceOptions options = new ReplaceOptions();
        options.upsert(true);
        UpdateResult result = database.getCollection(table).replaceOne(filters, document, options);
        if (result.getUpsertedId() != null && idRef != null) {
            idRef.value(result.getUpsertedId().asObjectId().getValue());
        }
        return (int) result.getModifiedCount() + (result.getUpsertedId() != null ? 1 : 0);
    }

    @Override
    public boolean create(Metadata metadata, boolean dropIfExist) throws Exception {
        if (dropIfExist) {
            database.getCollection(metadata.getTable()).drop();
        }
        database.createCollection(metadata.getTable());
        return true;
    }

    @Override
    public boolean exist(Metadata metadata) throws Exception {
        ListCollectionNamesIterable collections = database.listCollectionNames();
        for (String name : collections) {
            if (name.equals(metadata.getTable())) {
                return true;
            }
        }
        return false;
    }

    private FindIterable<Document> handleDocumentFind(MongoCollection<Document> collection, String primary, Object identity, ProtoSchema schema, Query query) {
        Bson filters = handleWhereToFilters(primary, identity, query != null ? query.where() : null);
        FindIterable<Document> findable = collection.find(filters);
        String field = Query.DEFAULT_FIELDS;
        if (query != null) {
            field = query.field();
        }
        // 字段过滤
        if (!StrUtil.isEmpty(field) && !field.equals(Query.DEFAULT_FIELDS)) {
            String[] fieldList = field.split(",");
            Bson projection = Projections.fields();
            for (String f : fieldList) {
                projection = Projections.fields(projection, Projections.include(f));
            }
            findable.projection(projection);
        }
        // 排序
        if (query != null && query.orderBy() != null) {
            OrderBy orderBy = query.orderBy();
            if (orderBy.isDesc()) {
                findable.sort(Sorts.descending(orderBy.getField()));
            } else {
                findable.sort(Sorts.ascending(orderBy.getField()));
            }
        }
        // 分页查询
        if (query != null && query.pager() != null) {
            Pager pager = query.pager();
            int skip = (pager.getStart() - 1) * pager.getLimit();
            findable.skip(skip).limit(pager.getLimit());
        }
        return findable;
    }

    private AggregateIterable<Document> handleDocumentAggregate(MongoCollection<Document> collection, String primary, Object identity, ProtoSchema schema, Query query) {
        Bson filters = handleWhereToFilters(primary, identity, query.where());
        List<Bson> pipelines = new ArrayList<>();
        pipelines.add(Aggregates.match(filters));
        String field = Query.DEFAULT_FIELDS;
        if (query != null) {
            field = query.field();
        }
        // 字段过滤
        if (!StrUtil.isEmpty(field) && !field.equals(Query.DEFAULT_FIELDS)) {
            String[] fieldList = field.split(",");
            Bson projection = Projections.fields();
            for (String f : fieldList) {
                projection = Projections.fields(projection, Projections.include(f));
            }
            pipelines.add(Aggregates.project(projection));
        }
        // 表连接查询
        if (query != null && query.joins() != null && !query.joins().isEmpty()) {
            throw new UnsupportedOperationException("MongoDB does not support join queries");
        }
        // 排序
        if (query != null && query.orderBy() != null) {
            OrderBy orderBy = query.orderBy();
            if (orderBy.isDesc()) {
                pipelines.add(Aggregates.sort(Sorts.descending(orderBy.getField())));
            } else {
                pipelines.add(Aggregates.sort(Sorts.ascending(orderBy.getField())));
            }
        }
        // 分组查询
        if (query != null && query.groupBy() != null) {
            GroupBy groupBy = query.groupBy();
            BsonField bsonField = new BsonField(groupBy.getField(), new Document("$first", "$" + groupBy.getField()));
            pipelines.add(Aggregates.group("$" + groupBy.getField(), bsonField));
        }
        // 分页查询
        if (query != null && query.pager() != null) {
            Pager pager = query.pager();
            int skip = (pager.getStart() - 1) * pager.getLimit();
            pipelines.add(Aggregates.skip(skip));
            pipelines.add(Aggregates.limit(pager.getLimit()));
        }
        return collection.aggregate(pipelines);
    }

    private Bson handleWhereToFilters(String primary, Object identity, Where where) {
        Bson filters = Filters.empty();
        // 主键查询和条件查询互斥，因为主键查询就表示已经唯一值
        if (!StrUtil.isEmpty(primary) && identity != null) {
            return Filters.eq(primary, identity);
        }
        if (where == null || where.isEmpty()) {
            return filters;
        }
        // 处理查询条件拼装
        List<Where.Condition> conditionList = where.getConditionList();
        for (Where.Condition condition : conditionList) {
            String key = condition.getKey();
            String operation = condition.getOperation();
            Object value = condition.getValue();
            Where whereNext = condition.getWhere();
            if (whereNext != null && !whereNext.isEmpty()) {
                Bson subFilters = handleWhereToFilters(primary, identity, whereNext);
                if (subFilters != null) {
                    filters = Filters.and(filters, subFilters);
                }
                continue;
            }
            if ("=".equals(operation)) {
                filters = Filters.and(filters, Filters.eq(key, value));
            } else if (">".equals(operation)) {
                filters = Filters.and(filters, Filters.gt(key, value));
            } else if ("<".equals(operation)) {
                filters = Filters.and(filters, Filters.lt(key, value));
            } else if (">=".equals(operation)) {
                filters = Filters.and(filters, Filters.gte(key, value));
            } else if ("<=".equals(operation)) {
                filters = Filters.and(filters, Filters.lte(key, value));
            } else if ("!=".equals(operation) || "<>".equals(operation)) {
                filters = Filters.and(filters, Filters.ne(key, value));
            } else if ("in".equalsIgnoreCase(operation)) {
                if (List.class.isAssignableFrom(value.getClass())) {
                    filters = Filters.and(filters, Filters.in(key, (List<?>) value));
                } else if (value.getClass().isArray()) {
                    filters = Filters.and(filters, Filters.in(key, (Object[]) value));
                } else {
                    throw new IllegalArgumentException("The value of 'in' operation must be a List or Array.");
                }
            } else if ("not in".equalsIgnoreCase(operation)) {
                if (List.class.isAssignableFrom(value.getClass())) {
                    filters = Filters.and(filters, Filters.nin(key, (List<?>) value));
                } else if (value.getClass().isArray()) {
                    filters = Filters.and(filters, Filters.nin(key, (Object[]) value));
                } else {
                    throw new IllegalArgumentException("The value of 'not in' operation must be a List or Array.");
                }
            } else if ("like".equalsIgnoreCase(operation) && value instanceof String) {
                String pattern = ((String) value).replace("%", ".*");
                filters = Filters.and(filters, Filters.regex(key, pattern));
            } else {
                throw new IllegalArgumentException("Unsupported operation: " + operation);
            }
        }
        return filters;
    }

    private Document handleEntityToDocument(Entity entity) {
        Document document = new Document();
        for (Map.Entry<String, Object> dataEntry : entity.entrySet()) {
            String field = dataEntry.getKey();
            Object value = dataEntry.getValue();
            document.append(field, value);
        }
        return document;
    }

    private Entity handleDocumentToEntity(Document document, String primary) {
        if (document == null) {
            return null;
        }
        Entity entity = new Entity(primary);
        for (Map.Entry<String, Object> dataEntry : document.entrySet()) {
            String field = dataEntry.getKey();
            Object value = dataEntry.getValue();
            entity.put(field, value);
        }
        if (!StrUtil.isEmpty(primary)) {
            entity.setIdentity(entity.getObject(primary));
        }
        return entity;
    }

    private Document handleUpdaterToDocument(Updater updater) {
        Document document = new Document();
        if (updater == null || updater.isEmpty()) {
            return document;
        }
        List<Updater.Data> dataList = updater.getDataList();
        for (Updater.Data data : dataList) {
            String key = data.getKey();
            Object value = data.getValue();
            document.append(key, value);
        }
        return document;
    }

    @Override
    public void shutdown() {
        if (Objects.nonNull(mongoClient)) {
            mongoClient.close();
        }
    }
}
