package cloud.apposs.cachex.jdbc.operator;

public class LikeBuilder implements ConditionBuilder {
    @Override
    public Object valueBuild(Object value) {
        return value;
    }

    @Override
    public String operationBuild(String key, Object value) {
        return "(" + key + " LIKE ?)";
    }
}
