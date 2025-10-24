package cloud.apposs.rest.listener.httplog;

import cloud.apposs.logger.Logger;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.NoHandlerFoundException;
import cloud.apposs.rest.RestConfig;
import cloud.apposs.rest.listener.HandlerListener;
import cloud.apposs.rest.listener.httplog.variable.AbstractVariableParser;

public abstract class HandlerLogListener<R, P> implements HandlerListener<R, P> {
    protected boolean loggable = false;

    /**
     * 日志解析器
     */
    protected AbstractVariableParser<R, P> parser;

    @Override
    public void initialize(RestConfig config) {
        this.loggable = config.isHttpLogEnable();
    }

    @Override
    public void handlerStart(R request, P response, Handler handler) {
        if (!loggable) return;
        this.setStartTime(request, response, handler);
    }

    @Override
    public void handlerComplete(R request, P response, Handler handler, Object result, Throwable cause) {
        if (!loggable || cause instanceof NoHandlerFoundException) {
            return;
        }
        if (cause != null) {
            Logger.error(cause, parser.parse(request, response, handler, cause));
        } else {
            Logger.info(parser.parse(request, response, handler, cause));
        }
    }

    public abstract void setStartTime(R request, P response, Handler handler);
}
