package cloud.apposs.react;

import cloud.apposs.react.actor.Actor;
import cloud.apposs.react.actor.ActorLock;
import cloud.apposs.util.Errno;
import cloud.apposs.util.Pair;
import cloud.apposs.util.StandardResult;
import cloud.apposs.util.Table;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@SuppressWarnings("unchecked")
public class TestReact {
    @Test
    public void testReactSimpleExecutor() throws Exception {
        final Executor executor = getNamedExecutor("ExecutorOnThread");

        CountDownLatch latch = new CountDownLatch(1);
        React.emitter(new IoEmitter<String>() {
            @Override
            public String call() throws Exception {
                System.err.println("----------------- 1." + Thread.currentThread() + " ----------------");
                return "This is From Executor-" + Thread.currentThread().getName();
            }
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
        React.intercept(interList, new IoFunction<String, OperateorIntercept.IResult>() {
            @Override
            public OperateorIntercept.IResult call(String s) throws Exception {
                System.out.println("interceptor: " + s);
                // 可以在第二个拦截器拦截并抛出异常，流程就不会执行
//                if (s.equals("two")) {
//                    return OperateorIntercept.IResult.FAILURE;
//                }
                // 可以在第三个拦截器拦截并跳过并且不会抛出异常，且下面流程就不会执行
//                if (s.equals("three")) {
//                    return OperateorIntercept.IResult.SKIP;
//                }
                return OperateorIntercept.IResult.SUCCESS;
            }
        }, new IoEmitter<React<? extends Integer>>() {
            @Override
            public React<Integer> call() throws Exception {
                System.out.println("just in");
                return React.just(1);
            }
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
        React.create(new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                t.onNext("This is a Emitter String");
                t.onCompleted();
            }
        }).map(new IoFunction<String, Integer>() {
            @Override
            public Integer call(String t) {
                System.out.println("recv str " + t);
                return t.hashCode();
            }
        }).match(new IoFunction<Integer, Errno>() {
            @Override
            public Errno call(Integer t) throws Exception {
                return Errno.OK;
            }
        }).subscribe(new IoSubscriber<Integer>() {
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
                .map(new IoFunction<Integer, String>() {
                    @Override
                    public String call(Integer t) {
                        return "AA:" + t;
                    }
                }).subscribe(new IoSubscriber<String>() {
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
        .map(new IoFunction<Pair<Integer, String>, String>() {
            @Override
            public String call(Pair<Integer, String> t) throws Exception {
                return "AA:" + t.key() + "-" + t.value();
            }
        }).subscribe(new IoSubscriber<String>() {
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
        .reduce(new IoReduce<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        }).subscribe(new IoSubscriber<Integer>() {
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
        React.create(new React.OnSubscribe<Integer>() {
            @Override
            public void call(SafeIoSubscriber<? super Integer> t) throws Exception {
                t.onNext(1001 / 0);
            }
        }).retry(new IoFunction<Throwable, React<Integer>>() {
            @Override
            public React<Integer> call(Throwable throwable) throws Exception {
                return React.from(1, 12, 14).sleep(scheduler, 1200);
            }
        }).subscribe(new IoSubscriber<Integer>() {
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
        React.create(new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                t.onNext("This is a Emitter String");
                t.onCompleted();
            }
        }).sleep(scheduler, 200)
        .map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                System.out.println("map in " + s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
                return s;
            }
        })
        .sleep(scheduler, 1200)
        .subscribe(new IoAction<String>() {
            @Override
            public void call(String s) throws Exception {
                System.out.println(s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
                latch.countDown();
            }
        }).start();
        latch.await();
    }

    @Test
    public void testReactRepeat() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();
        React.from("AA", "BB")
        .repeat(2)
        .map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                System.out.println("map in " + s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
                return s;
            }
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
        React.interval(scheduler, 1000, TimeUnit.MILLISECONDS).map(new IoFunction<Long, String>() {
            @Override
            public String call(Long aLong) throws Exception {
                latch.countDown();
                return "AA:" + aLong;
            }
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
        React.lock(lock, actor, new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                System.out.println("Foo in 1");
                t.onNext("This is a Emitter Lock String");
            }
        }).map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                System.out.println("Foo in 2");
                return s + ": On Map";
            }
        }).subscribe(new IoAction<String>() {
            @Override
            public void call(String s) throws Exception {
                System.out.println(s + ", Cost Time:" + (System.currentTimeMillis() - startTime));
                latch.countDown();
            }
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

    static class MyThread extends Thread {
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
            React.lock(lockKey, actor, new React.OnSubscribe<String>() {
                @Override
                public void call(SafeIoSubscriber<? super String> t) throws Exception {
                    // 模拟即使有请求进来，但耗时比较久，其他同lockkey的请求也是要阻塞保证串行执行
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                    }
                    // 模拟HTTP请求结束会调用此方法触发数据的发送
                    t.onNext("This is a Emitter Lock String");
                }
            }).subscribe(new IoAction<String>() {
                @Override
                public void call(String s) throws Exception {
                    // 输出时会按顺序输出0:MsgXX,1:MsgXX，即使同lockkey队列方法中执行的耗时不同，依然也是顺序执行
                    System.out.println(index + ";Msg=" + s + ";LockKey=" + lockKey + ";SleepTime=" + time);
                    latch.countDown();
                }
            }).start();
        }

        @Override
        public String toString() {
            return String.valueOf(this.index);
        }
    }

    public static void testReactSubscribeOn() {
        long start = System.currentTimeMillis();

        final Executor executor = getNamedExecutor("ExecutorOnThread");
        React.create(new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                System.err.println("----------------- Emitter " + Thread.currentThread() + " ----------------");
                t.onNext("This is a Emitter String");
                t.onCompleted();
            }
        })
        .map(new IoFunction<String, String>() {
            @Override
            public String call(String t) {
                System.err.println("----------------- Map " + Thread.currentThread() + " ----------------");
                return "AA:" + t;
            }
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
        React.create(new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                t.onNext("This is a Emitter String");
                t.onCompleted();
            }
        }).handle(new IoFunction<String, StandardResult>() {
            @Override
            public StandardResult call(String s) throws Exception {
                return StandardResult.error(Errno.ERROR);
            }
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
    public void testReactRollback() throws Exception {
        Table t = new Table();
        final CountDownLatch latch = new CountDownLatch(1);
        React.create(new React.OnSubscribe<StandardResult>() {
            @Override
            public void call(SafeIoSubscriber<? super StandardResult> t) throws Exception {
//                int i = 1 / 0;
//                t.onNext(StandardResult.success("Hello result"));
                t.onNext(StandardResult.error(Errno.ERROR));
                t.onCompleted();
            }
        }).rollbackIfError(new IoFunction<StandardResult, StandardResult>() {
            @Override
            public StandardResult call(StandardResult result) throws Exception {
                // 只有当返回结果为StandardResult.isError或者上游抛出异常时才会触发rollback回滚操作
                // 回滚操作结束后再返回另外包装的结果
                System.out.println("Fail In: " + result);
                return StandardResult.success("Rollback In");
            }
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
        React.create(new React.OnSubscribe<String>() {
            @Override
            public void call(SafeIoSubscriber<? super String> t) throws Exception {
                t.onNext("This is a Emitter String");
                t.onCompleted();
            }
        }).filter(new IoFunction<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                if (s.isEmpty()) {
                    return false;
                }
                return true;
            }
        }).map(new IoFunction<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return "maped " + s;
            }
        }).subscribe(new IoSubscriber<String>() {
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

    /**
     * 用指定的名称新建一个线程
     */
    public static Executor getNamedExecutor(final String name) {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, name);
            }
        });
    }
}
