package cloud.apposs.rest.interceptor;

import cloud.apposs.react.React;
import cloud.apposs.rest.ApplicationContextHolder;
import cloud.apposs.rest.Handler;

public class HandlerInterceptorAdapter<R, P> implements HandlerInterceptor<R, P> {
	@Override
	public void initialize(ApplicationContextHolder context) {
	}

	@Override
	public React<Boolean> preHandle(R request, P response, Handler handler) throws Exception {
		return React.just(true);
	}

	@Override
	public void postHandler(R request, P response, Handler handler, Object value) throws Exception {
	}

	@Override
	public void afterCompletion(R request, P response, Handler handler, Object result, Throwable throwable) {
	}

	@Override
	public void destory() {
	}
}
