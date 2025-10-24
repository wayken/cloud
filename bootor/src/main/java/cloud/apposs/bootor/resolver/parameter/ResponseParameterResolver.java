package cloud.apposs.bootor.resolver.parameter;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.parameter.Parameter;
import cloud.apposs.rest.parameter.ParameterResolver;

@Component
public class ResponseParameterResolver implements ParameterResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public boolean supportsParameter(Parameter parameter) {
        return BootorHttpResponse.class.isAssignableFrom(parameter.getType());
    }

    @Override
    public Object resolveArgument(Parameter parameter, BootorHttpRequest request, BootorHttpResponse response) throws Exception {
        return response;
    }
}
