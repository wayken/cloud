package cloud.apposs.rest.parameter;

import cloud.apposs.util.SysUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * 请求参数解析管理器
 */
public final class ParameterResolverSupport<R, P> {
	/** 参数解析器 */
    private final List<ParameterResolver<R, P>> parameterResolvers = new LinkedList<ParameterResolver<R, P>>();
    
    public ParameterResolver<R, P> getParameterResolver(Parameter parameter) {
    	for (ParameterResolver<R, P> parameterResolver : parameterResolvers) {
			if (parameterResolver.supportsParameter(parameter)) {
				return parameterResolver;
			}
		}
		return null;
	}
    
    public List<ParameterResolver<R, P>> getParameterResolverList(Parameter parameter) {
    	List<ParameterResolver<R, P>> parameterResolverList = new LinkedList<ParameterResolver<R, P>>();
    	for (ParameterResolver<R, P> parameterResolver : parameterResolvers) {
			if (parameterResolver.supportsParameter(parameter)) {
				parameterResolverList.add(parameterResolver);
			}
		}
		if (parameterResolverList.isEmpty()) {
			for (ParameterResolver<R, P> parameterResolver : parameterResolvers) {
				if (parameterResolver.supportsParameter(parameter)) {
					parameterResolverList.add(parameterResolver);
				}
			}
		}
		return parameterResolverList;
	}
    
    public void addParameterResolver(ParameterResolver<R, P> resolver) {
    	SysUtil.checkNotNull(resolver, "resolver");
		parameterResolvers.add(resolver);
	}

	public int getParameterResolverSize() {
		return parameterResolvers.size();
	}
}
