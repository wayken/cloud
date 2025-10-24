package cloud.apposs.cachex;

import cloud.apposs.cachex.jdbc.JDBCTemplate;
import cloud.apposs.cachex.mongodb.MongoTemplate;

public final class JDBCTemplateFactory {
    public static DBTemplate buildDBTemplate(CacheXConfig.DbConfig configuration) throws Exception {
        if (CacheXConfig.DbConfig.isJDBCTemplate(configuration.getDialect())) {
            return new JDBCTemplate(configuration);
        } else if (CacheXConfig.DbConfig.isMongoTemplate(configuration.getDialect())) {
            return new MongoTemplate(configuration);
        } else {
            throw new IllegalArgumentException("Unsupported DBTemplate dialect: " + configuration.getDialect());
        }
    }
}
