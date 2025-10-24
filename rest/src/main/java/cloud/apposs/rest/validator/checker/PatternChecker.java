package cloud.apposs.rest.validator.checker;

import cloud.apposs.rest.validator.IChecker;
import cloud.apposs.util.StrUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.regex.Matcher;

public class PatternChecker implements IChecker {
    @Override
    public Object check(Field field, Annotation annotation, Object value) {
        Pattern anno = (Pattern) annotation;
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

        String[] regexes = anno.regex();
        boolean xor = anno.xor();
        for (int i = 0; i < regexes.length; i++) {
            String regex = regexes[i];
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value.toString());
            boolean matched = matcher.matches();
            if (xor) {
                if (matched) {
                    return value;
                }
                if (i == regexes.length - 1) {
                    throw new IllegalArgumentException("parameter " + field.getName() + " unmatch for pattern " + regex);
                }
            } else {
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("parameter " + field.getName() + " unmatch for pattern " + regex);
                }
            }
        }

        return value;
    }
}
