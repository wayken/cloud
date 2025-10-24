package cloud.apposs.rest;

import cloud.apposs.guard.Guard;
import cloud.apposs.guard.ResourceToken;
import cloud.apposs.ioc.BeanFactory;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.IoSubscriber;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Action;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.rest.interceptor.HandlerInterceptor;
import cloud.apposs.rest.interceptor.HandlerInterceptorSupport;
import cloud.apposs.rest.listener.ApplicationListener;
import cloud.apposs.rest.listener.ApplicationListenerSupport;
import cloud.apposs.rest.listener.HandlerListener;
import cloud.apposs.rest.listener.HandlerListenerSupport;
import cloud.apposs.rest.parameter.ParameterResolver;
import cloud.apposs.rest.plugin.Plugin;
import cloud.apposs.rest.plugin.PluginSupport;
import cloud.apposs.rest.view.NoViewResolverFoundException;
import cloud.apposs.rest.view.ViewResolver;
import cloud.apposs.rest.view.ViewResolverSupport;
import cloud.apposs.threadx.ThreadPool;
import cloud.apposs.threadx.ThreadPoolFactory;
import cloud.apposs.util.StrUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MVC组件框架
 */
public final class Restful<R, P> {
    /**
     * 框架全局配置
     */
    private final RestConfig config;

    /**
     * IOC容器
     */
    private final BeanFactory beanFactory;

    /**
     * 插件服务管理
     */
    private final PluginSupport pluginSupport;

    /**
     * 框架监听服务管理
     */
    private final ApplicationListenerSupport applicationListenerSupport;

    /**
     * Hanlder业务服务监听管理
     */
    private final HandlerListenerSupport<R, P> handlerListenerSupport;

    /**
     * 拦截器管理
     */
    private final HandlerInterceptorSupport<R, P> handlerInterceptorSupport;

    /**
     * 处理器映射
     */
    private final HandlerRouter<R, P> handlerRouter;

    /**
     * 视图渲染器
     */
    private final ViewResolverSupport<R, P> viewResolverSupport;

    /**
     * 方法调用辅助类
     */
    private final HandlerInvocation<R, P> handlerInvocation;

    /**
     * 异常解析器，可根据不同的异常实现不同的错误码提示
     */
    private WebExceptionResolver<R, P> webExceptionResolver;

    /**
     * 线程池，主要用于Handler执行CPU密集操作需要，
     * 注意Handler要使用线程池需要通过{@link cloud.apposs.rest.annotation.Executor}注解开启
     */
    private ThreadPool executor;

    public Restful(RestConfig config) {
        this.config = config;
        this.beanFactory = new BeanFactory();
        this.pluginSupport = new PluginSupport();
        this.applicationListenerSupport = new ApplicationListenerSupport();
        this.handlerRouter = new HandlerRouter<R, P>(config);
        this.handlerListenerSupport = new HandlerListenerSupport<R, P>();
        this.handlerInterceptorSupport = new HandlerInterceptorSupport<R, P>();
        this.viewResolverSupport = new ViewResolverSupport<R, P>();
        this.handlerInvocation = new HandlerInvocation<R, P>();
    }

    /**
     * 服务启动时的初始化，包括：
     * <pre>
     *     1. 初始化IOC容器
     *     2. 初始化参数解析器
     *     3. 初始化视图渲染器
     *     4. 初始化插件服务
     *     5. 初始化容器监听服务
     *     6. 初始化线程池
     *     7. 初始化异常解析器
     *     8. 初始化Handler业务服务
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public void initialize() throws Exception {
        // 先初始化IOC容器，方便外部业务初始化时获取
        String basePackages = config.getBasePackage();
        if (StrUtil.isEmpty(basePackages)) {
            throw new IllegalStateException("base package not setting");
        }
        // 初始化ApplicationContext到IOC容器中，方便业务获取
        // 注意要提前添加到IOC容器，方便IOC容器load组件时直接获取并调用构造方法
        ApplicationContextHolder context = new ApplicationContextHolder(config, beanFactory);
        beanFactory.addBean(context);
        // 判断是否是以cloud.apposs.xxx, com.example.*作为多包扫描
        String[] basePackageSplit = basePackages.split(",");
        String[] basePackageList = new String[basePackageSplit.length];
        for (int i = 0; i < basePackageSplit.length; i++) {
            basePackageList[i] = basePackageSplit[i].trim();
        }
        // 扫描包将各个IOC组件添加进容器中
        beanFactory.load(basePackageList);

        // 初始化参数解析器，包括用户自定义的和系统定义的，
        // 只要有配置basePackage和Component注解均扫描进来
        List<ParameterResolver> parameterResolverList = beanFactory.getBeanHierarchyList(ParameterResolver.class);
        doSortByOrderAnnotation(parameterResolverList);
        for (ParameterResolver parameterResolver : parameterResolverList) {
            addParameterResolver(parameterResolver);
        }

        // 初始化视图渲染器，包括用户自定义的和系统定义的，同上
        List<ViewResolver> viewResolverList = beanFactory.getBeanHierarchyList(ViewResolver.class);
        doSortByOrderAnnotation(viewResolverList);
        for (ViewResolver viewResolver : viewResolverList) {
            addViewResolver(viewResolver.build(config));
        }

        // 初始化插件服务
        List<Plugin> pluginList = beanFactory.getBeanHierarchyList(Plugin.class);
        for (Plugin plugin : pluginList) {
            plugin.initialize(config);
            pluginSupport.addPlugin(plugin);
        }

        // 初始化容器监听服务
        List<ApplicationListener> appListenerList = beanFactory.getBeanHierarchyList(ApplicationListener.class);
        for (ApplicationListener listener : appListenerList) {
            listener.initialize(config);
            applicationListenerSupport.addListener(listener);
        }
        List<HandlerListener> handlerListenerList = beanFactory.getBeanHierarchyList(HandlerListener.class);
        for (HandlerListener listener : handlerListenerList) {
            listener.initialize(config);
            handlerListenerSupport.addListener(listener);
        }

        // 初始化拦截器
        List<HandlerInterceptor> interceptorList = beanFactory.getBeanHierarchyList(HandlerInterceptor.class);
        // 对拦截器进行排序后添加
        doSortByOrderAnnotation(interceptorList);
        for (HandlerInterceptor interceptor : interceptorList) {
            interceptor.initialize(context);
            handlerInterceptorSupport.addInterceptor(interceptor);
        }

        // 初始化异常处理器
        webExceptionResolver = beanFactory.getBeanHierarchy(WebExceptionResolver.class);

        // 初始化线程池
        executor = ThreadPoolFactory.createCachedThreadPool(config.getWorkerCount(), "React-ThreadPool-");

        // 初始化Handler处理器，
        // 只要有配置basePackage和Action、RestAction注解均扫描进来
        List<Class<?>> actionClassList = beanFactory.getClassAnnotationList(Action.class);
        actionClassList.addAll(beanFactory.getClassAnnotationList(RestAction.class));
        for (Class<?> actionClass : actionClassList) {
            // 获取并遍历该Action类中所有的方法
            Method[] actionMethods = actionClass.getMethods();
            for (Method actionMethod : actionMethods) {
                // 处理Action方法
                addHandler(actionClass, actionMethod);
            }
        }
    }

    /**
     * 获取MVC框架IOC容器
     */
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * 添加参数解析器，
     * 负责对{@link cloud.apposs.rest.annotation.Action}中配置的参数进行解析
     */
    public void addParameterResolver(ParameterResolver resolver) {
        handlerRouter.addParameterResolver(resolver);
    }

    /**
     * 添加Action注解对象方法和对应参数解析，
     * 便于之后Http请求时做Url -> Handler映射匹配
     */
    public boolean addHandler(Class<?> actionClass) throws RestException {
        Method[] actionMethods = actionClass.getDeclaredMethods();
        for (Method actionMethod : actionMethods) {
            // 处理Action方法
            handlerRouter.addHandler(actionClass, actionMethod);
        }
        return true;
    }

    public boolean addHandler(Class<?> actionClass, Method actionMethod) throws RestException {
        return handlerRouter.addHandler(actionClass, actionMethod);
    }

    public boolean addHandler(String path, Handler handler) throws RestException {
        return handlerRouter.addHandler(path, handler);
    }

    /**
     * 获取请求对应的{@link Handler}处理器
     */
    public Handler getHandler(IHandlerProcess<R, P> handlerProcess, R request, P response) {
        return handlerRouter.getHandler(handlerProcess, request, response);
    }

    /**
     * 添加视图解析器
     */
    public void addViewResolver(ViewResolver<R, P> viewResolver) {
        viewResolverSupport.addResolver(viewResolver);
    }

    /**
     * 移除视图解析器
     */
    public void removeViewResolver(ViewResolver<R, P> viewResolver) {
        viewResolverSupport.removeResolver(viewResolver);
    }

    /**
     * 添加拦截器
     */
    public void addHandlerInterceptor(HandlerInterceptor<R, P> handlerInterceptor) {
        handlerInterceptorSupport.addInterceptor(handlerInterceptor);
    }

    /**
     * 移除拦截器
     */
    public void removeHandlerInterceptor(HandlerInterceptor<R, P> handlerInterceptor) {
        handlerInterceptorSupport.removeInterceptor(handlerInterceptor);
    }

    public <T> T getBean(Class<T> beanClass) {
        return beanFactory.getBean(beanClass);
    }

    /**
     * 调用对应的Handler方法进行业务逻辑处理，
     * 如果Handler有注解写指令则进行服务迁移判断，
     * 如果Handler有注解熔断则进行熔断判断逻辑以保护系统负载，
     */
    public Object invokeHandler(Handler handler, Object target,
                IHandlerProcess<R, P> handlerProcess, R request, P response) throws Exception {
        if (handler.isWriteCmd() && config.isReadonly()) {
            throw new ReadOnlyException(handlerProcess.getRequestPath(request, response));
        }

        if (!handler.isGuard()) {
            return handlerInvocation.invoke(handler, target, request, response);
        }

        // 如果注解了限流熔断则进入限流判断逻辑
        ResourceToken token = null;
        try {
            IGuardProcess<R, P> guardProcess = handlerProcess.getGuardProcess();
            String resouce = handler.getResource();
            Object limitKey = guardProcess == null ? null : guardProcess.getGuardKey(resouce, request, response);
            token = Guard.entry(resouce, limitKey);
            return handlerInvocation.invoke(handler, target, request, response);
        } catch (Throwable t) {
            // 业务异常埋点以做熔断
            Guard.trace(token, t);
            throw t;
        } finally {
            if (token != null) {
                token.exit();
            }
        }
    }

    public void renderView(Object result, R request, P response) throws Exception {
        renderView(result, request, response, false);
    }

    /**
     * 根据业务逻辑请求结果渲染视图
     *
     * @param flush 是否为异步数据响应
     */
    public void renderView(Object result, R request, P response, boolean flush) throws Exception {
        ViewResolver<R, P> viewResolver = viewResolverSupport.getViewResolver(request, response, result);
        if (viewResolver == null) {
            throw new NoViewResolverFoundException();
        }
        viewResolver.render(request, response, result, flush);
    }

    /**
     * 根据业务逻辑请求结果渲染视图
     */
    public void renderView(IHandlerProcess<R, P> handlerProcess, R request, P response) {
        try {
            // 容器一类的要做些操作才能走异步响应
            handlerProcess.markAsync(request, response);

            // 获取URL对应的Handler#method映射并对Handler进行前置处理
            Handler handler = getHandler(handlerProcess, request, response);
            if (handler == null) {
                String requestMethod = handlerProcess.getRequestMethod(request, response);
                String requestPath = handlerProcess.getRequestPath(request, response);
                throw new NoHandlerFoundException(requestMethod, requestPath);
            }
            if (Logger.isDebugEnabled()) {
                Logger.debug("Restful Render View Handler Matched: %s", handler);
            }
            handlerProcess.processHandler(request, response, handler);

            // 创建异步拦截器
            handlerListenerSupport.handlerStart(request, response, handler);
            List<HandlerInterceptor<R, P>> interceptorList = handlerInterceptorSupport.getInterceptorList();
            List<React<Boolean>> preInterceptorList = new LinkedList<React<Boolean>>();
            for (HandlerInterceptor<R, P> interceptor : interceptorList) {
                preInterceptorList.add(interceptor.preHandle(request, response, handler));
            }
            // 基于React的拦截器+Handler响应式输出，
            // 框架采用纯异步执行，注意对应拦截器和Handler如果是网络则用底层异步IO(OkHttp/IoWhois等)，
            // 如果是CPU密集的计算操作则需要添加React.subscribeOn方法，用线程池异步执行
            // 如果是网络请求的服务，则基于底层EPoll模型的OkHttp在发送网络请求时就是纯异步，可以应付大量并发请求连接
            ThreadPool executor = this.executor;

            React<?> react = React.intercept(preInterceptorList, (IoEmitter<React<?>>) () -> {
                // 采用全异步模式，任何业务注解实现都需要返回React，
                // 因为在框架启动的时候便已经判断返回值是否为React，详见{HandlerRouter.addHandler}，所以不用再返回值判断了，提升点性能
                Object target = beanFactory.getBean(handler.getClazz());
                React<?> source = (React<?>) invokeHandler(handler, target, handlerProcess, request, response);
                // 业务异步调用返回的React异步框架必须是非空的才能保证流式运行
                // 只有React里面的方法包装才允许返回空，例如返回React<Void>
                if (source == null) {
                    throw new NullPointerException("Illegal Handler ReturnType For HTTP '"
                            + handler.getMethod() + "' Request With URI '" + handler.getPath() + "'");
                }
                return source;
            });
            // 如果Handler有注解Executor，则用线程池再异步执行，在线程池内执行包括拦截器和Handler
            // 如果Handler自己有线程池隔离则不用开启注解，在Handler内自己执行React.subscribeOn(executor)即可
            if (handler.isExecutor()) {
                react = react.subscribeOn(executor);
            }
            react.subscribe(new ReactSubcriber(handler, request, response)).start();
        } catch (Throwable cause) {
            handleProcessError(request, response, null, cause);
        }
    }

    /**
     * 处理基于React的异步数据响应，
     * 异步处理结束后会触发该类的处理，最终实现异步处理结束后的视图渲染、事件监听和异常处理
     */
    private class ReactSubcriber implements IoSubscriber<Object> {
        private final Handler handler;

        private final R request;

        private final P response;

        private ReactSubcriber(Handler handler, R request, P response) {
            this.handler = handler;
            this.request = request;
            this.response = response;
        }

        @Override
        public void onNext(Object value) throws Exception {
            ViewResolver<R, P> viewResolver = viewResolverSupport.getViewResolver(request, response, value);
            if (viewResolver == null) {
                throw new NoViewResolverFoundException();
            }
            boolean isViewRenderCompleted = viewResolver.isCompleted(request, response, value);
            if (isViewRenderCompleted) {
                // 触发Handler响应数据前的拦截
                handlerInterceptorSupport.postHandler(request, response, handler, value);
            }
            // 业务有可能返回空或者Void，则代表业务自己定义如何输出响应数据，不由框架来渲染视图
            if (value != null) {
                // 注意此处响应数据渲染视图需要放在最后，
                // 因为request在Action::React.subscribeOn多线程下可能有request数据被清空问题，
                // 具体原因为：
                // 1、当调用renderView进行数据输出时此时由Event_Loop_XXX线程中发送，发送完成后可能请求关闭重置
                // 2、此时此方法中的xxxCompletion是在另外一个线程池执行
                // 3、而此时获取的值则可能是Event_Loop_XXX中请求被重置后的数据了
                viewResolver.render(request, response, value, true);
            }
            // 触发Hanler处理结束的监听和拦截
            if (isViewRenderCompleted) {
                handlerInterceptorSupport.afterCompletion(request, response, handler, value);
                handlerListenerSupport.handlerComplete(request, response, handler, value);
            }
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable cause) {
            handleProcessError(request, response, handler, cause);
        }
    }

    /**
     * 根据Order注解进行列表的排序
     */
    private <T> void doSortByOrderAnnotation(List<T> compareList) {
        Collections.sort(compareList, (object1, object2) -> {
            Order order1 = object1.getClass().getAnnotation(Order.class);
            Order order2 = object2.getClass().getAnnotation(Order.class);
            int order1Value = order1 == null ? 0 : order1.value();
            int order2Value = order2 == null ? 0 : order2.value();
            return order1Value - order2Value;
        });
    }

    /**
     * 处理业务和底层抛出的所有异常
     */
    private void handleProcessError(R request, P response, Handler handler, Throwable cause) {
        // 如果是方法调用中有异常，需要获取的是真正的业务异常
        if (cause instanceof InvocationTargetException) {
            cause = ((InvocationTargetException) cause).getTargetException();
        }
        // 如果有配置统一框架异常处理，则用业务实现的异常解析器解析异常结果并响应输出
        try {
            if (webExceptionResolver == null) {
                throw cause;
            }
            // 调用统一异常接口响应错误信息
            Object result = webExceptionResolver.resolveHandlerException(request, response, cause);
            handlerInterceptorSupport.afterCompletion(request, response, handler, result, cause);
            handlerListenerSupport.handlerComplete(request, response, handler, result, cause);
            // 获取视图解析器进行输出渲染
            renderView(result, request, response, true);
        } catch (Throwable unkonw) {
            // 没有默认的异常处理器则直接终端输出异常
            cause.printStackTrace();
            unkonw.printStackTrace();
        }
    }

    /**
     * 服务退出时的资源销毁和回调
     */
    public void destroy() {
        // 服务销毁
        pluginSupport.destroy();
        applicationListenerSupport.destroy();
        // 拦截器销毁
        handlerInterceptorSupport.destroy();
        // IOC容器实例销毁
        beanFactory.destroy();
        // 销毁线程池
        if (Objects.nonNull(executor)) {
            executor.shutdownNow();
        }
    }
}
