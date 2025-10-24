package cloud.apposs.webx.resolver.parameter;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.util.StrUtil;

@Component
public class VariableIntegerParameterResolver extends AbstractVariableParameterResolver {
    @Override
    public boolean isParameterTypeSupports(Class<?> parameterType) {
        // 判断是否为Int基本类型或者包装类型
        return Integer.TYPE.toString().equals(parameterType.toString()) || Integer.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object castParameterValue(String parameterValue) {
        if (!StrUtil.isEmpty(parameterValue)) {
            try {
                return Integer.parseInt(parameterValue);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
