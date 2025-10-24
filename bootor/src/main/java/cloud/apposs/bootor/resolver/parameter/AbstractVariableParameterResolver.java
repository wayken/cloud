package cloud.apposs.bootor.resolver.parameter;

import cloud.apposs.bootor.BootorConstants;
import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.annotation.Variable;
import cloud.apposs.rest.parameter.Parameter;
import cloud.apposs.rest.parameter.VariableParameterResolver;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractVariableParameterResolver extends VariableParameterResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getParameterVariables(Parameter parameter, BootorHttpRequest request, BootorHttpResponse response) {
        Map<String, String> variables = (Map) request.getAttribute(BootorConstants.REQUEST_ATTRIBUTE_VARIABLES);
        // 先通过RESTFUL来获取参数，没有则用GET方式来获取参数
        if (variables != null) {
            return variables;
        }
        // 通过Request来获取参数，GET和POST都可用
        Variable variable = (Variable) parameter.getAnnotation();
        String parameterName = variable.value();
        String parameterValue = request.getParameter(parameterName);
        if (parameterValue == null) {
            return null;
        }
        variables = new HashMap<String, String>();
        variables.put(parameterName, parameterValue);
        return variables;
    }
}
