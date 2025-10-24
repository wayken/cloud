package cloud.apposs.rest.parameter;

import cloud.apposs.rest.RestException;
import cloud.apposs.rest.annotation.Variable;

import java.lang.annotation.Annotation;

public class ParameterResolverUnsupportedException extends RestException {
    private static final long serialVersionUID = -1697457096176021189L;

    private final Parameter parameter;

    public ParameterResolverUnsupportedException(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(64);
        message.append("No ParameterResolver matched for parameter '").append(parameter.getType().getName());
        message.append("' of index ").append(parameter.getIndex());
        message.append(" in method '").append(parameter.getMethod().getName()).append("'");
        return message.toString();
    }
}
