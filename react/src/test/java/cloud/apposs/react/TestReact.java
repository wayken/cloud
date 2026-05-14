package cloud.apposs.react;

import cloud.apposs.react.actor.Actor;
import cloud.apposs.react.actor.ActorLock;
import cloud.apposs.util.Errno;
import cloud.apposs.util.Pair;
import cloud.apposs.util.StandardResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class TestReact {
    @Test
    public void testReactSimpleExecutor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Executor executor = getNamedExecutor("ExecutorOnThread");
        React.emitter(() -> {
            System.err.println("----------------- 1." + Thread.currentThread() + " ----------------");
            return "This is From Executor-" + Thread.currentThread().getName();
        }).subscribeOn(executor)
        .subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) throws Exception {
                System.err.println("----------------- 2." + Thread.currentThread() + " ----------------");
                System.out.println(value);
                Thread.sleep(2000);
                System.out.println("sleep in");
            }
            @Override
            public void onCompleted() {
                System.err.println("----------------- 3." + Thread.currentThread() + " ----------------");
                System.out.println("complete");
                latch.countDown();
                ((ExecutorService) executor).shutdown();
            }
            @Override
            public void onError(Throwable cause) {
            }
        }).start();
        latch.await();
    }

    /**
     * 模拟数据先经过拦截器，最终调用业务的情况
     */
    @Test
    public void testReactIntercept() throws Exception {
        React<String> inter1 = React.just("one");
        React<String> inter2 = React.just("two");
        React<String> inter3 = React.just("three");
        List<React<String>> interList = new LinkedList<React<String>>();
        interList.add(inter1);
        interList.add(inter2);
        interList.add(inter3);
        React.intercept(interList, s -> {
            System.out.println("interceptor: " + s);
            // 可以在第二个拦截器拦截并抛出异常，流程就不会执行
            if (s.equals("two")) {
                return OperateorIntercept.IResult.FAILURE;
            }
            // 可以在第三个拦截器拦截并跳过并且不会抛出异常，且下面流程就不会执行
            if (s.equals("three")) {
                return OperateorIntercept.IResult.SKIP;
            }
            return OperateorIntercept.IResult.SUCCESS;
        }, () -> {
            System.out.println("just in");
            return React.just(1);
        }).subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) throws Exception {
                System.out.println("all execute result: " + value);
            }
            @Override
            public void onCompleted() {
                System.out.println("complete");
            }
            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace();
            }
        }).start();
    }

    public static void testReactEmitter() {
        React.create((React.OnSubscribe<String>) t -> {
            t.onNext("This is a Emitter String");
            t.onCompleted();
        }).map(t -> {
            System.out.println("recv str " + t);
            return t.hashCode();
        }).match(t -> Errno.OK)
        .subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) {
                System.out.println(value);
            }

            @Override
            public void onCompleted() {
                System.out.println("execute complete");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void testReactFrom() throws Exception {
        React.from(1, 2, 3)
        .map(t -> "AA:" + t)
        .subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) {
                System.out.println(value);
            }
            @Override
            public void onCompleted() {
                System.out.println("execute complete");
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void testReactIterator() throws Exception {
        List<Pair<Integer, String>> dataList = new LinkedList<Pair<Integer, String>>();
        dataList.add(Pair.build(1, "one"));
        dataList.add(Pair.build(2, "two"));
        dataList.add(Pair.build(3, "three"));
        React.from(dataList)
        .map(t -> "AA:" + t.key() + "-" + t.value())
        .subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) {
                System.out.println(value);
            }

            @Override
            public void onCompleted() {
                System.out.println("execute complete");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void testReactReduce() throws Exception {
        React.from(1, 2, 3)
        .reduce((v1, v2) -> v1 + v2).subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) {
                System.out.println(value);
            }

            @Override
            public void onCompleted() {
                System.out.println("execute complete");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void testReactRetry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        React.create((React.OnSubscribe<Integer>) t -> t.onNext(1001 / 0))
        .retry(throwable -> React.from(1, 12, 14)
        .sleep(scheduler, 1200)).subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) {
                System.out.println(value);
            }
            @Override
            public void onCompleted() {
                System.out.println("execute complete");
                latch.countDown();
            }
            @Override
            public void onError(Throwable e) {
                System.out.println("execute error");
                e.printStackTrace();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactSleep() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long startTime = System.currentTimeMillis();
        React.create((React.OnSubscribe<String>) t -> {
            t.onNext("This is a Emitter String");
            t.onCompleted();
        }).sleep(scheduler, 200)
        .map(s -> {
            System.out.println("map in " + s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
            return s;
        })
        .sleep(scheduler, 1200)
        .subscribe(s -> {
            System.out.println(s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
            latch.countDown();
        }).start();
        latch.await();
    }

    @Test
    public void testReactRepeat() throws Exception {
        long startTime = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(1);
        React.from("AA", "BB")
        .repeat(2)
        .map(s -> {
            System.out.println("map in " + s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
            return s;
        })
        .subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) {
                System.out.println(value);
            }
            @Override
            public void onCompleted() {
                latch.countDown();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactInterval() throws Exception {
        CountDownLatch latch = new CountDownLatch(10);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        React.interval(scheduler, 1000, TimeUnit.MILLISECONDS).map(aLong -> {
            latch.countDown();
            return "AA:" + aLong;
        }).subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) {
                System.out.println(value);
            }
            @Override
            public void onCompleted() {
                System.out.println("execute complete");
                latch.countDown();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactLock() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Actor actor = new Actor();
        long startTime = System.currentTimeMillis();
        Object aid = 854;
        ActorLock lock = Actor.createLock(aid);
        React.lock(lock, actor, (React.OnSubscribe<String>) t -> {
            System.out.println("Foo in 1");
            t.onNext("This is a Emitter Lock String");
        }).map(s -> {
            System.out.println("Foo in 2");
            return s + ": On Map";
        }).subscribe(s -> {
            System.out.println(s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
            latch.countDown();
        }).start();
        latch.await();
    }

    @Test
    public void testThreadReactLock() throws Exception {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        Actor actor = new Actor(2);
        // 第一个用户
        ActorLock lockKey = Actor.createLock(854);
        List<Thread> orderTheadList = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            orderTheadList.add(new MyThread(i, lockKey, actor, latch));
        }
        for (int i = 0; i < orderTheadList.size(); i++) {
            orderTheadList.get(i).start();
            Thread.sleep(100);
        }
        latch.await();
    }

    public static void testReactSubscribeOn() {
        long start = System.currentTimeMillis();
        final Executor executor = getNamedExecutor("ExecutorOnThread");
        React.create((React.OnSubscribe<String>) t -> {
            System.err.println("----------------- Emitter " + Thread.currentThread() + " ----------------");
            t.onNext("This is a Emitter String");
            t.onCompleted();
        })
        .map(t -> {
            System.err.println("----------------- Map " + Thread.currentThread() + " ----------------");
            return "AA:" + t;
        })
        .subscribeOn(executor)
        .subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) {
                System.err.println("----------------- OnSubcriber " + Thread.currentThread() + " ----------------");
                System.out.println("subscribe recv from:" + value);
                ((ExecutorService) executor).shutdown();
            }
            @Override
            public void onCompleted() {
                System.out.println("execute complete");
                ((ExecutorService) executor).shutdown();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                ((ExecutorService) executor).shutdown();
            }
        }).start();
        long exeTime = System.currentTimeMillis() - start;
        System.err.println("----------------- " + Thread.currentThread() + " execute time:" + exeTime + " ----------------");
    }

    /**
     * 测试故意返回StandardResult.error时的异常抛出
     */
    @Test
    public void testReactStandardResultWithError() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        React.create((React.OnSubscribe<String>) t -> {
            t.onNext("This is a Emitter String");
            t.onCompleted();
        }).handle(s -> StandardResult.error(Errno.ERROR)).subscribe(new IoSubscriber<StandardResult>() {
            @Override
            public void onNext(StandardResult value) throws Exception {
                System.out.println(value.toJson());
            }
            @Override
            public void onCompleted() {
                System.out.println("----------------- Request Complete -----------------");
                latch.countDown();
            }
            @Override
            public void onError(Throwable cause) {
                System.err.println("Exception Caught");
                cause.printStackTrace();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactRollback() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        React.create((React.OnSubscribe<StandardResult>) t1 -> {
//                int i = 1 / 0;
//                t.onNext(StandardResult.success("Hello result"));
            t1.onNext(StandardResult.error(Errno.ERROR));
            t1.onCompleted();
        }).rollbackIfError(result -> {
            // 只有当返回结果为StandardResult.isError或者上游抛出异常时才会触发rollback回滚操作
            // 回滚操作结束后再返回另外包装的结果
            System.out.println("Fail In: " + result);
            return StandardResult.success("Rollback In");
        }).subscribe(new IoSubscriber<StandardResult>() {
            @Override
            public void onNext(StandardResult value) throws Exception {
                System.out.println(value.toJson());
            }
            @Override
            public void onCompleted() {
                System.out.println("----------------- Request Complete -----------------");
                latch.countDown();
            }
            @Override
            public void onError(Throwable cause) {
                System.err.println("Exception Caught");
                cause.printStackTrace();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactFilter() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        React.create((React.OnSubscribe<String>) t -> {
            t.onNext("This is a Emitter String");
            t.onCompleted();
        }).filter(s -> {
            if (s.isEmpty()) {
                return false;
            }
            return true;
        }).map(s -> "maped " + s).subscribe(new IoSubscriber<String>() {
            @Override
            public void onNext(String value) throws Exception {
                System.out.println(value);
                latch.countDown();
            }
            @Override
            public void onError(Throwable cause) {
                System.err.println("Exception Caught");
                cause.printStackTrace();
                latch.countDown();
            }
            @Override
            public void onCompleted() {
                System.out.println("----------------- Request Complete -----------------");
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    // 测试loop循环：handler返回null时终止循环，最终值传递给下游，模拟计数器从0累加到5后停止
    @Test
    public void testReactLoopUntilNull() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        React.create((React.OnSubscribe<Integer>) t -> {
            t.onNext(0);
            t.onCompleted();
        }).loop(value -> {
            System.out.println("loop value: " + value);
            if (value >= 5) {
                // 返回null终止循环
                return null;
            }
            // 返回新的React继续下一轮循环
            final int next = value + 1;
            return React.create(t -> {
                t.onNext(next);
            });
        }).subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) throws Exception {
                System.out.println("loop final value: " + value);
            }
            @Override
            public void onCompleted() {
                System.out.println("----------------- Loop Complete -----------------");
                latch.countDown();
            }
            @Override
            public void onError(Throwable cause) {
                System.err.println("Exception Caught");
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    // 测试loop循环：模拟分页查询，每次返回一页数据，直到最后一页返回null终止循环
    @Test
    public void testReactLoopPaging() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final int pageSize = 3;
        final List<String> allData = new ArrayList<String>();
        // 模拟总共10条数据，分页拉取
        final int totalRows = 10;
        React.create((React.OnSubscribe<List<String>>) t -> {
            // 第一页从offset=0开始
            List<String> page = fetchPage(0, pageSize, totalRows);
            t.onNext(page);
            t.onCompleted();
        }).loop(new IoFunction<List<String>, React<List<String>>>() {
            private int offset = pageSize;
            @Override
            public React<List<String>> call(List<String> page) {
                allData.addAll(page);
                System.out.println("fetched page: " + page);
                if (page.size() < pageSize) {
                    // 最后一页，终止循环
                    return null;
                }
                final int currentOffset = offset;
                offset += pageSize;
                return React.create(t -> {
                    List<String> nextPage = fetchPage(currentOffset, pageSize, totalRows);
                    t.onNext(nextPage);
                    t.onCompleted();
                });
            }
        }).subscribe(new IoSubscriber<List<String>>() {
            @Override
            public void onNext(List<String> value) {
                allData.addAll(value);
                System.out.println("all data size: " + allData.size());
            }
            @Override
            public void onCompleted() {
                System.out.println("----------------- Paging Loop Complete -----------------");
                latch.countDown();
            }
            @Override
            public void onError(Throwable cause) {
                System.err.println("Exception Caught");
                cause.printStackTrace();
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testUnsubscribeSynchronous() {
        AtomicInteger received = new AtomicInteger(0);
        IoSubscription subscription = React.from(1, 2, 3, 4, 5, 6)
        .map(i -> {
            System.out.println("Processing: " + i);
            return i;
        })
        .repeat(4)
        .subscribe(new IoSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) throws Exception {
                received.incrementAndGet();
                System.out.println("onNext: " + value);
                if (value == 3) {
                    // 收到 3 后立即取消订阅
                    unsubscribe();
                }
            }
            @Override
            public void onError(Throwable e) {
                System.err.println("不应该触发 onError");
            }
            @Override
            public void onCompleted() {
                // 取消订阅后不会到达这里
                System.out.println("onCompleted");
            }
        }).start();
        // 只收到 1、2、3，之后被取消
        System.out.println("Total received: " + received.get() + ", unsubscribed: " + subscription.isUnsubscribed());
    }

    @Test
    public void testUnsubscribeAsynchronous() throws Exception {
        AtomicInteger received = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // interval 每 100ms 发射一个数字
        IoSubscription subscription = React.interval(scheduler, 100, TimeUnit.MILLISECONDS)
        .map((n) -> {
            System.out.println("map: " + n);
            return n;
        })
        .subscribeOn(scheduler)
        .subscribe(new IoSubscriber<Long>() {
            @Override
            public void onNext(Long value) {
                received.incrementAndGet();
                System.out.println("async onNext: " + value);
            }
        }).start();
        // 让它跑 350ms，期间大约发射 3 个元素
        Thread.sleep(350);
        subscription.unsubscribe();
        int countAfterUnsub = received.get();
        Thread.sleep(300); // 再等 300ms，确认没有新元素到达
        System.out.println("共收到 " + countAfterUnsub + " 个元素" + ", unsubscribed: " + subscription.isUnsubscribed());
        Thread.sleep(30000);
    }

    @Test
    public void testUnsubscribeWithListener() throws Exception {
        AtomicInteger received = new AtomicInteger(0);
        AtomicInteger unsubscribeCalled = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        IoSubscription subscription = React.interval(scheduler, 100, TimeUnit.MILLISECONDS)
        .map(aLong -> {
            System.out.println("map: " + aLong);
            return aLong;
        })
        .filter(aLong -> {
            System.out.println("filter: " + aLong);
            return true;
        })
        .subscribe(new IoSubscriber<Long>() {
            @Override
            public void onNext(Long value) {
                received.incrementAndGet();
                System.out.println("onNext: " + value);
            }
        }).start();
        // 注册 onUnsubscribe 监听
        subscription.addOnUnsubscribe(() -> {
            unsubscribeCalled.incrementAndGet();
            System.out.println("onUnsubscribe triggered! received=" + received.get());
        });
        Thread.sleep(350);
        subscription.unsubscribe();
        System.out.println("unsubscribeCalled=" + unsubscribeCalled.get()
                + ", isUnsubscribed=" + subscription.isUnsubscribed());
        scheduler.shutdown();
    }

    private static List<String> fetchPage(int offset, int pageSize, int total) {
        List<String> page = new ArrayList<String>();
        for (int i = offset; i < Math.min(offset + pageSize, total); i++) {
            page.add("row-" + i);
        }
        return page;
    }

    // 用指定的名称新建一个线程
    public static Executor getNamedExecutor(final String name) {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, name);
            }
        });
    }

    private static class MyThread extends Thread {
        private int index;

        private ActorLock lockKey;

        private Actor actor;

        private CountDownLatch latch;

        public MyThread(int index, ActorLock lockKey, Actor actor, CountDownLatch latch) {
            this.index = index;
            this.lockKey = lockKey;
            this.actor = actor;
            this.latch = latch;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public void run() {
            Random random = new Random();
            int time = random.nextInt(2000);
            // 模拟EventLoop多线程下React响应式编程
            React.lock(lockKey, actor, (React.OnSubscribe<String>) t -> {
                // 模拟即使有请求进来，但耗时比较久，其他同lockkey的请求也是要阻塞保证串行执行
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                }
                // 模拟HTTP请求结束会调用此方法触发数据的发送
                t.onNext("This is a Emitter Lock String");
            }).subscribe(s -> {
                // 输出时会按顺序输出0:MsgXX,1:MsgXX，即使同lockkey队列方法中执行的耗时不同，依然也是顺序执行
                System.out.println(index + ";Msg=" + s + ";LockKey=" + lockKey + ";SleepTime=" + time);
                latch.countDown();
            }).start();
        }
        @Override
        public String toString() {
            return String.valueOf(this.index);
        }
    }
}
