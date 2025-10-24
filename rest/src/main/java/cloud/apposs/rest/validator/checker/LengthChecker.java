package cloud.apposs.rest.validator.checker;

import cloud.apposs.rest.validator.IChecker;
import cloud.apposs.util.StrUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.spec.InvalidParameterSpecException;

public class LengthChecker implements IChecker {
    @Override
    public Object check(Field field, Annotation annotation, Object value) throws Exception {
        Length anno = (Length) annotation;
        if (!anno.require() && value == null) {
            return value;
        }

        if (value == null) {
            // 输出异常信息
            if (StrUtil.isEmpty(anno.message())) {
                throw new IllegalArgumentException("require parameter " + field.getName());
            } else {
                throw new IllegalArgumentException(anno.message());
            }
        }

        String valueStr = value.toString();
        if (anno.trim()) {
            valueStr = valueStr.trim();
        }
        int length = valueStr.length();
        int min = anno.min();
        int max = anno.max();
        if (length < min || length > max) {
            throw new IllegalArgumentException("require parameter " + field.getName() +
                    " must be greater than " + min + " and less than " + max);
        }
        return valueStr;
    }
}
