package cloud.apposs.cachex.jdbc.operator;

import java.util.List;

public class InBuilder implements ConditionBuilder {
    @Override
    public Object valueBuild(Object value) {
        return value;
    }

    @Override
    public String operationBuild(String key, Object value) {
        if (List.class.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            List<Object> inList = (List<Object>) value;
            int inSize = inList.size();
            StringBuilder inStr = new StringBuilder();
            for (int i = 0; i < inSize; i++) {
                inStr.append("?");
                if (i < inSize - 1) {
                    inStr.append(" , ");
                }
            }
            return "(" + key + " IN (" + inStr + "))";
        } else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            StringBuilder inStr = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                inStr.append("?");
                if (i < array.length - 1) {
                    inStr.append(" , ");
                }
            }
            return "(" + key + " IN (" + inStr + "))";
        } else {
            return "false";
        }
    }
}
