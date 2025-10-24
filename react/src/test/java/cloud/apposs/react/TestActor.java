package cloud.apposs.react;

import cloud.apposs.react.actor.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class TestActor {
    @Test
    public void testActorThread() throws Exception {
        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount * 2);
        Actor actor = new Actor(4, false);
        actor.addListener(new ActorListener() {
            @Override
            public void onActorStatusChange(ActorLock lock, LockStatus status) {
                System.out.println(lock + " is " +status);
            }
        });
        // 第一个用户
        ActorLock lockKey = Actor.createLock(854);
        List<Thread> orderTheadList = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            orderTheadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    // 模拟多线程获取锁请求进来
                    actor.lock(lockKey, new MyTask(index, lockKey, latch));
                }
            }));
        }
        for (int i = 0; i < orderTheadList.size(); i++) {
            orderTheadList.get(i).start();
            Thread.sleep(10);
        }

        // 第二个用户
        ActorLock lockKey2 = Actor.createLock(855);
        List<Thread> orderTheadList2 = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            orderTheadList2.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    // 模拟多线程获取锁请求进来
                    actor.lock(lockKey2, new MyTask(index, lockKey2, latch));
                }
            }));
        }
        for (int i = 0; i < orderTheadList2.size(); i++) {
            orderTheadList2.get(i).start();
            Thread.sleep(10);
        }

        latch.await();
    }

    @Test
    public void testActorThreadCustomKey() throws Exception {
        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        Actor actor = new Actor(4, false);
        actor.addListener(new ActorListener() {
            @Override
            public void onActorStatusChange(ActorLock lock, LockStatus status) {
                System.out.println(lock + " is " + status);
            }
        });
        List<Thread> orderTheadList = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            ActorLock lockKey = Actor.createLock(new MyActorKey(854, i));
            final int index = i;
            orderTheadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    // 模拟多线程获取锁请求进来
                    actor.lock(lockKey, new MyTask(index, lockKey, latch));
                }
            }));
        }
        for (int i = 0; i < orderTheadList.size(); i++) {
            orderTheadList.get(i).start();
            Thread.sleep(100);
        }

        latch.await();
    }

    static class MyActorKey {
        private Integer key;

        private int index;

        public MyActorKey(int key, int index) {
            this.key = key;
            this.index = index;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MyActorKey)) {
                return false;
            }
            return key.equals(((MyActorKey) obj).key);
        }

        @Override
        public String toString() {
            return "lockkey:" + key + "-" + index;
        }
    }

    static class MyTask implements ActorTask {
        private final int index;

        private final ActorLock lock;

        private final CountDownLatch latch;

        MyTask(int index, ActorLock lock, CountDownLatch latch) {
            this.index = index;
            this.lock = lock;
            this.latch = latch;
        }

        @Override
        public ActorLock getLockKey() {
            return lock;
        }

        @Override
        public void run() {
            Random random = new Random();
            try {
                int time = random.nextInt(2000);
                Thread.sleep(time);
                System.out.println(index + ";LockKey=" + getLockKey() + ";Sleep Time=" + time);
                latch.countDown();
            } catch (InterruptedException e) {
            }
            lock.unlock();
        }
    }
}
