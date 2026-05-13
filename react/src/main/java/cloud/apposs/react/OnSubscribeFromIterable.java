package cloud.apposs.react;

import cloud.apposs.react.React.OnSubscribe;

import java.util.Iterator;

/**
 * 基于迭代器的数据发送器
 */
public class OnSubscribeFromIterable<T> implements OnSubscribe<T> {
    private final Iterable<? extends T> iterable;

    public OnSubscribeFromIterable(Iterable<? extends T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public void call(IoSubscriber<? super T> t) throws Exception {
        try {
            Iterator<? extends T> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                T value = iterator.next();
                t.onNext(value);
            }
        } finally{
            t.onCompleted();
        }
    }
}
