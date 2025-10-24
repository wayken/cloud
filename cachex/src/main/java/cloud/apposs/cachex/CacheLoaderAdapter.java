package cloud.apposs.cachex;

import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

import java.util.List;

public class CacheLoaderAdapter<K extends CacheKey, V> implements CacheLoader<K, V> {
    @Override
    public void initialize(CacheX<K, V> cachex) {
        // Do Nothing
    }

    @Override
    public int add(K key, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        throw new UnsupportedOperationException("Not implemented method add(K key, V value, ...) of CacheLoader");
    }

    @Override
    public int add(List<K> keys, List<V> values, ProtoSchema schema, DBTemplate template, List<Object> idRefs, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method add(List<K> keys, List<V> values, ...) of CacheLoader");
    }

    @Override
    public V load(K key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method load(K key, ProtoSchema schema, ...) of CacheLoader");
    }

    @Override
    public V select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method select(CacheKey<?> key, Query query, ...) of CacheLoader");
    }

    @Override
    public Table<V> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method query(CacheKey<?> key, Query query, ...) of CacheLoader");
    }

    @Override
    public int replace(K key, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method replace(K key, V value, ProtoSchema schema, ...) of CacheLoader");
    }

    @Override
    public int delete(K key, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException("Not implemented method delete(K key, Dao dao, ...) of CacheLoader");
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method delete(CacheKey<?> key, Where where, ...) of CacheLoader");
    }

    @Override
    public int delete(List<K> keys, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method delete(List<K> keys, Dao dao, ...) of CacheLoader");
    }

    @Override
    public int delete(List<CacheKey<?>> keys, Where where, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method delete(List<CacheKey<?>> keys, Where where, ...) of CacheLoader");
    }

    @Override
    public int update(K key, V value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method update(K key, V value, ...) of CacheLoader");
    }

    @Override
    public int update(List<K> keys, List<V> values, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method update(List<K> keys, List<V> values, ...) of CacheLoader");
    }

    @Override
    public int update(CacheKey<?> key, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method update(CacheKey<?> key, Updater updater, ...) of CacheLoader");
    }

    @Override
    public int update(List<CacheKey<?>> keys, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method update(List<CacheKey<?>> keys, Updater updater, ...) of CacheLoader");
    }

    @Override
    public int hadd(K key, Object field, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method hadd(K key, Object field, V value, ...) of CacheLoader");
    }

    @Override
    public List<V> hload(K key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method hload(K key, ProtoSchema schema, ...) of CacheLoader");
    }

    @Override
    public int hdelete(K key, Object field, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method hdelete(K key, Object field, ...) of CacheLoader");
    }

    @Override
    public int hdelete(K key, Object[] fields, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method hdelete(K key, Object[] fields, ...) of CacheLoader");
    }

    @Override
    public int hupdate(K key, Object field, V value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        throw new UnsupportedOperationException(
                "Not implemented method hupdate(K key, Object field, V value, ...) of CacheLoader");
    }

    @Override
    public String getField(V info) {
        throw new UnsupportedOperationException("Not implemented method getField(V info) of CacheLoader");
    }
}
