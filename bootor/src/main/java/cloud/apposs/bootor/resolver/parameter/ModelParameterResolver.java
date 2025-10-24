package cloud.apposs.bootor.resolver.parameter;

import cloud.apposs.bootor.BootorConstants;
import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.parameter.BodyParameterResolver;
import cloud.apposs.rest.parameter.Parameter;
import cloud.apposs.util.Param;

import java.util.Map;

@Component
public class ModelParameterResolver extends BodyParameterResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    @SuppressWarnings("unchecked")
    public Param getParameterValues(Parameter parameter, BootorHttpRequest request, BootorHttpResponse response) {
        Param param = new Param();
        param.putAll(request.getParameters());
        param.putAll(request.getParam());

        // 把请求属性列表打进Model对象，例如BootorConstants.REQUEST_PARAMETRIC_FLOW流水号，方便在进行HTTP请求时也把流水号带上
        // 把请求流水号打进Model对象，方便在进行HTTP请求时也把流水号带上
        Map<Object, Object> attributes = request.getAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String name = entry.getKey().toString();
            Object value = entry.getValue();
            param.put(name, value);
        }
        // 把HttpRequest和HttpResponse也打进Model对象中
        param.put(BootorConstants.REQUEST_PARAMETRIC_REQUEST, request);
        param.put(BootorConstants.REQUEST_PARAMETRIC_RESPONSE, response);

        Map<String, String> uriVariables = (Map) request.getAttribute(BootorConstants.REQUEST_ATTRIBUTE_VARIABLES);
        if (uriVariables != null) {
            param.putAll(uriVariables);
        }
        return param;
    }
}
