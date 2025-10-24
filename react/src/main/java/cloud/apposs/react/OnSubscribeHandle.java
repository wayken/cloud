package cloud.apposs.react;

import cloud.apposs.util.StandardResult;

/**
 * 对数据进行加工处理，返回{@link StandardResult}，主要应用于业务微服务开发
 */
public class OnSubscribeHandle<T> implements React.OnSubscribe<StandardResult> {
    private final React<T> source;

    private final IoFunction<? super T, StandardResult> predicate;

    public OnSubscribeHandle(React<T> source, IoFunction<? super T, StandardResult> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public void call(SafeIoSubscriber<? super StandardResult> subscriber) throws Exception {
        StandardResultSubscriber<T> parent = new StandardResultSubscriber<T>(subscriber, predicate);
        source.subscribe(parent).start();
    }

    public static final class StandardResultException extends Exception {
        private static final long serialVersionUID = -6099542992767550343L;

        private final StandardResult result;

        public StandardResultException(StandardResult result) {
            super(String.format("Unexpected StandardResult '%d: %s'",
                    result.getErrno().value(), result.getErrno().description()));
            this.result = result;
        }

        public StandardResult getResult() {
            return result;
        }
    }

    static final class StandardResultSubscriber<T> implements IoSubscriber<T> {
        final IoSubscriber<? super StandardResult> actual;

        final IoFunction<? super T, StandardResult> predicate;

        public StandardResultSubscriber(IoSubscriber<? super StandardResult> actual,
                IoFunction<? super T, StandardResult> predicate) {
            this.actual = actual;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) throws Exception {
            StandardResult result;
            if (t instanceof ISkip) {
                // 判断上层业务是否直接返回ISkip接口实现类，如果是则不调用predicate回调函数
                // 之所以设计此逻辑在于上层业务有可能判断某些条件下是不需要Handler处理下一层的，则直接跳过
                // 如：获取文档信息时需要再通过handle请求文档对应的会员信息
                // 如果文档里面没会员则不需要下层回调函数调用会员获取信息
                result = ((ISkip) t).result();
            } else {
                // 普通结果实例按普通逻辑处理
                result = predicate.call(t);
            }
            if (result.isSuccess()) {
                actual.onNext(result);
            } else {
                actual.onError(new StandardResultException(result));
            }
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }
    }

    public static interface ISkip {
        StandardResult result();
    }
}
