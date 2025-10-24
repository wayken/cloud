package cloud.apposs.react;

import cloud.apposs.react.actor.Actor;
import cloud.apposs.react.actor.ActorLock;
import cloud.apposs.util.Errno;
import cloud.apposs.util.StandardResult;
import cloud.apposs.util.SysUtil;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 数据响应式编程
 * 采用RXJAVA类似的响应式开发思维，代码不多，但很绕，核心原理介绍如下
 * <pre>
 *     1. {@link React#create(OnSubscribe)} 负责产生数据流供各个 OnSucribeXXX 进行数据过滤处理
 *     2. {@link React#subscribe(IoSubscriber)} 中 {@link IoSubscriber} 由业务实现对数据流实现最终业务处理
 *     3. OnSucribeXXX 核心是负责对 {@link IoSubscriber} 进行重包装，即对数据流进行包装处理
 * </pre>
 */
public class React<T> {
    public interface OnSubscribe<T> extends IoAction<SafeIoSubscriber<? super T>> {
    }

    protected final OnSubscribe<T> onSubscribe;

    /**
     * 监听数据的订阅者
     */
    private SafeIoSubscriber<? super T> subscriber;

    public React(OnSubscribe<T> f) {
        this.onSubscribe = f;
    }

    public static <T> React<T> create(final OnSubscribe<T> f) {
        return new React<T>(f);
    }

    /**
     * 将数组转换为 React 对象并产生数据流
     */
    @SuppressWarnings("unchecked")
    public static <T> React<T> just(T value) {
        return create(new OnSubscribeFromArray<T>((T[]) new Object[] { value }));
    }

    /**
     * 将数组转换为 React 对象并产生数据流
     */
    @SuppressWarnings("unchecked")
    public static <T> React<T> from(T... array) {
        return create(new OnSubscribeFromArray<T>(array));
    }

    /**
     * 将迭代器转换为 React 对象并产生数据流
     */
    public static <T> React<T> from(Iterable<? extends T> iterable) {
        return create(new OnSubscribeFromIterable<T>(iterable));
    }

    /**
     * 合并多个请求并行调用，
     * 调用结果会逐个触发{@link IoSubscriber#onNext(Object)}进行处理，
     * 所有请求结束后，无论成功还是失败，最终调用一次{@link IoSubscriber#onCompleted()}
     * 适用场景：批量请求URL并逐个进行解析，如批量CDN请求文件等
     */
    @SuppressWarnings("unchecked")
    public static <T> React<T> merge(React<? extends T>... sequences) {
        return create(new OperateorMerge<T>(sequences));
    }

    public static <T> React<T> merge(List<? extends React<? extends T>> sequences) {
        return create(new OperateorMerge<T>(sequences));
    }

    @SuppressWarnings("unchecked")
    public static <T> React<List<T>> mergeList(React<? extends T>... sequences) {
        return create(new OperateorMergeList<T>(sequences, true));
    }

    /**
     * 合并多个请求并行调用，
     * 调用结果会合并到List集合中并触发{@link IoSubscriber#onNext(Object)}进行处理
     * 适用场景：需要通过批量请求汇总数据，默认设置跳过错误以此保证批量请求中对端微服务可能宕机依然不影响当前服务
     *
     * @param sequences 数据集
     * @param skipError 某条链处理异常时是否依然继续处理，不触发onError事件，默认为跳过错误
     */
    public static <T> React<List<T>> mergeList(React<? extends T>[] sequences, boolean skipError) {
        return create(new OperateorMergeList<T>(sequences, skipError));
    }

    public static <T> React<List<T>> mergeList(List<React<T>> sequences) {
        return create(new OperateorMergeList<T>(sequences, true));
    }

    public static <T> React<List<T>> mergeList(List<React<T>> sequences, boolean skipError) {
        return create(new OperateorMergeList<T>(sequences, skipError));
    }

    /**
     * 返回空的React，
     * 主要应用场景为某些业务在特定条件下可能不发送链式请求，直接返回空响应数据，直接交给下一条链处理
     */
    public static <T> React<T> blank() {
        return create(subscriber -> subscriber.onNext(null));
    }

    /**
     * 需要进行前置条件判断的多个React串行执行，
     * 下一个串行执行的React流会根据上一个执行的任务来判断是否执行，如果上一个流执行不符合结果则解决False失败处理，
     * 主要业务有：
     * 1. 订单下单，多个子系统串行执行，依赖上一任务的执行结果来决定是否执行下一任务
     * 2. 多子任务定时执行，下一个任务依赖上一个任务的执行结果
     */
    public static <T> React<Boolean> flat(Iterable<? extends React<? extends T>> sequences,
                                          IoFunction<? super T, Boolean> predicate) {
        return create(new OperateorFlat<T>(sequences, predicate));
    }

    /**
     * React集合拦截器，基于异步的拦截器设计
     *
     * @param sequences 异步拦截器集合
     * @param predicate 过滤函数，
     *                  拦截器对应的调用结果会通过此过滤函数判断，如果判断拦截器拦截结果不符合预期则可抛出异常不让拦截器继续执行
     * @param emitter 拦截器全部执行通过后的React数据生成，即最终的业务调用
     */
    public static <T, R> React<R> intercept(Iterable<? extends React<? extends T>> sequences,
                                            IoFunction<? super T, OperateorIntercept.IResult> predicate, IoEmitter<React<? extends R>> emitter) {
        return create(new OperateorIntercept<T, R>(sequences, predicate, emitter));
    }

    public static <R> React<R> intercept(Iterable<? extends React<? extends Boolean>> sequences,
                                         IoEmitter<React<? extends R>> emitter) {
        return create(new OperateorIntercept<Boolean, R>(sequences, success -> {
            if (success) {
                return OperateorIntercept.IResult.SUCCESS;
            }
            return OperateorIntercept.IResult.FAILURE;
        }, emitter));
    }

    /**
     * 创建异步锁
     */
    public static <T> React<T> lock(ActorLock key, Actor actor, IoEmitter<? extends T> func) {
        return create(new OnSubscribeLock<T>(key, actor, new OnSubscribeEmitter<T>(func)));
    }

    public static <T> React<T> lock(ActorLock key, Actor actor, OnSubscribe<T> f) {
        return create(new OnSubscribeLock<T>(key, actor, f));
    }

    /**
     * 创建异步IO数据流
     */
    public static <T> React<T> emitter(IoEmitter<? extends T> func) {
        return create(new OnSubscribeEmitter<T>(func));
    }

    /**
     * 创建异步请求
     */
    public final <R> React<R> request(IoFunction<? super T, ? extends React<? extends R>> func) {
        return create(new OnSubscribeRequest<T, R>(this, func));
    }

    /**
     * 数据变换操作，将一个数据类型转换成另外一个数据类型
     */
    public final <R> React<R> map(IoFunction<? super T, ? extends R> func) {
        return create(new OnSubscribeMap<T, R>(this, func));
    }

    /**
     * 数据聚合操作，将多个数据聚合成一个数据流
     */
    public final  React<T> reduce(IoReduce<T, T, T> func) {
        return create(new OnSubscribeReduce<>(this, func));
    }

    /**
     * 数据过滤操作，只处理过滤通过的数据
     */
    public final React<T> filter(IoFunction<? super T, Boolean> predicate) {
        return create(new OnSubscribeFilter<T>(this, predicate));
    }

    /**
     * 数据匹配操作，匹配的数据进行正常流程处理，不匹配的则进入异常流程处理
     */
    public final React<T> match(IoFunction<? super T, Errno> predicate) {
        return create(new OnSubscribeMatch<T>(this, predicate));
    }

    /**
     * React执行异常时重试机制，主要应用于是网络请求异常时的重试逻辑
     */
    public final React<T> retry(IoFunction<Throwable, ? extends React<T>> handler) {
        return create(new OnSubscribeRetry<T>(this, handler));
    }

    /**
     * 基于{@link StandardResult}的数据响应码操作，
     * 响应码为成功的数据进行正常流程处理，响应码为失败的则进入异常流程处理
     */
    public final React<StandardResult> handle(IoFunction<? super T, StandardResult> predicate) {
        return create(new OnSubscribeHandle<T>(this, predicate));
    }

    /**
     * 处理结果失败时的回滚操作，一般用于业务数据库操作失败时的异步回滚
     */
    public final React<StandardResult> rollbackIfError(IoFunction<StandardResult, StandardResult> predicate) {
        return create(new OnSubscribeRollback<T>(this, predicate));
    }

    /**
     * 让业务逻辑在单独的线程池中异步执行，该方法只作用调用链其下的方法
     * 注意该方法要放在调用{@link #subscribe(IoSubscriber)}之前
     */
    public final React<T> executOn(Executor executor) {
        return create(new OperatorExecutOn<T>(onSubscribe, executor));
    }

    /**
     * 让业务逻辑在单独的定时任务里睡眠一段时间，该方法只作用调用链其下的方法
     * 注意该方法要放在调用{@link #subscribe(IoSubscriber)}之前
     *
     * @param scheduler 底层定时任务，之所以暴露此参数就是在业务层保证底层业务只用一个单例，提升性能
     * @param sleepTime 休眠时间，单位为毫秒
     */
    public final React<T> sleep(ScheduledExecutorService scheduler, long sleepTime) {
        return create(new OnSubscribeSleep<T>(this, scheduler, sleepTime));
    }

    /**
     * 对某一个Observable重复产生多次结果
     */
    public final React<T> repeat(int count) {
        return create(new OnSubscribeRepeat<T>(this, count));
    }

    public static final React<Long> interval(ScheduledExecutorService scheduler, long interval, TimeUnit unit) {
        return interval(scheduler, interval, interval, unit);
    }

    /**
     * 按指定的时间间隔发出递增的序列号
     *
     * @param scheduler    底层定时任务，之所以暴露此参数就是在业务层保证底层业务只用一个单例，提升性能
     * @param initialDelay 第一次发出序列号的延迟时间
     * @param period       发出序列号的时间间隔
     * @param unit         时间间隔的单位
     */
    public static final React<Long> interval(ScheduledExecutorService scheduler, long initialDelay, long period, TimeUnit unit) {
        return create(new OnSubscribeTimerPeriodically(initialDelay, period, unit, scheduler));
    }

    /**
     * 让业务逻辑在单独的线程池中异步执行，该方法作用调用链其上下的方法，
     * 注意该方法要放在调用{@link #subscribe(IoSubscriber)}之前，
     * 并且对OnSubscribeIo无效，因为OnSubscribeIo是有EventLoop异步网络线程去执行的
     */
    public final React<T> subscribeOn(Executor executor) {
        return create(new OperatorSubscribeOn<T>(this, executor));
    }

    /**
     * 请求异步请求的输出流传递给{@link IoSubscriber}回调处理
     */
    @SuppressWarnings("unchecked")
    public React<T> subscribe(IoSubscriber<? super T> subscriber) {
        if (!(subscriber instanceof SafeIoSubscriber<?>)) {
            this.subscriber = new SafeIoSubscriber<T>(subscriber);
        } else {
            this.subscriber = (SafeIoSubscriber<? super T>) subscriber;
        }
        return this;
    }

    public React<T> subscribe(IoAction<? super T> onNext) {
        SysUtil.checkNotNull(onNext, "onNext");

        IoAction<Throwable> onError = IoActionSubscriber.UNSUPPORTED_ACTION;
        IoAction<Void> onComplete = IoActionSubscriber.EMPTY_ACTION;
        return subscribe(new IoActionSubscriber<T>(onNext, onError, onComplete));
    }

    public React<T> subscribe(IoAction<? super T> onNext, final IoAction<Throwable> onError) {
        SysUtil.checkNotNull(onNext, "onNext");
        SysUtil.checkNotNull(onError, "onError");

        IoAction<Void> onComplete = IoActionSubscriber.EMPTY_ACTION;
        return subscribe(new IoActionSubscriber<T>(onNext, onError, onComplete));
    }

    public React<T> subscribe(IoAction<? super T> onNext, IoAction<Throwable> onError, IoAction<Void> onComplete) {
        SysUtil.checkNotNull(onNext, "onNext");
        SysUtil.checkNotNull(onError, "onError");
        SysUtil.checkNotNull(onComplete, "onComplete");

        return subscribe(new IoActionSubscriber<T>(onNext, onError, onComplete));
    }

    /**
     * 开始启动异步数据响应处理
     */
    public void start() {
        try {
            onSubscribe.call(subscriber);
        } catch (Throwable t) {
            subscriber.onError(t);
        }
    }
}
