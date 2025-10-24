package cloud.apposs.cachex.database;

import cloud.apposs.util.Param;

/**
 * 数据实体类，代表数据库中的一行数据，
 * 以Key->Value形式保存数据
 */
public class Entity extends Param {
    /**
     * 数据主键
     */
    private Object identity;

    /**
     * 数据主键字段名称
     */
    private String primary;

    public Entity() {
        this(null, null, false);
    }

    public Entity(String primary) {
        this(primary, null, false);
    }

    public Entity(String primary, Object identity) {
        this(primary, identity, false);
    }

    public Entity(String primary, Object identity, boolean sync) {
        super(sync);
        this.primary = primary;
        this.identity = identity;
        put(primary, identity);
    }

    public Entity(Param value) {
        super(value);
    }

    public static Entity builder() {
        return new Entity();
    }

    public static Entity builder(String primary, Object identity) {
        return new Entity(primary, identity);
    }

    public Object getIdentity() {
        return identity;
    }

    public void setIdentity(Object identity) {
        this.identity = identity;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public void setPrimaryValue(String primary, Object identity) {
        this.primary = primary;
        this.identity = identity;
    }

    public Param getDatas() {
        return this;
    }
}
