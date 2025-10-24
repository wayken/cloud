package cloud.apposs.bootor.resolver.parameter;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.util.StrUtil;

@Component
public class VariableLongParameterResolver extends AbstractVariableParameterResolver {
    @Override
    public boolean isParameterTypeSupports(Class<?> parameterType) {
        // 判断是否为Long基本类型或者包装类型
        return Long.TYPE.toString().equals(parameterType.toString()) || Long.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object castParameterValue(String parameterValue) {
        if (!StrUtil.isEmpty(parameterValue)) {
            try {
                return Long.parseLong(parameterValue);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
