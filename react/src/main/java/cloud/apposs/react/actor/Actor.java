package cloud.apposs.react.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 无锁化编程，将用到锁的地方均采用队列串行执行的方式来实际以实现无锁化编程，
 * 底层实现原理：
 * <pre>
 * 1、根据lockKey区分不同的子业务锁
 * 2、不同的子业务锁扔到对应的队列中
 * 3、定时线程将子业务队列的请求一一取出并执行以实现子业务的串行化执行
 * </pre>
 * 注意：
 * <pre>
 * 1、因为同一把ActorLock锁底层是由{@link #locks}维护的，
 * 所以当非同一把ActorLock锁的请求越多时，Actor占用的内存越大，直到同一把ActorLock锁的所有请求结束
 * </pre>
 */
public final class Actor {
    public static final String DEFAULT_ACTOR_THREAD_PREFIX = "Actor-Worker-";
    public static final int DEFAULT_ACTOR_THREAD_NUM = Runtime.getRuntime().availableProcessors();

    private volatile boolean shutdown = false;

    private final BlockingQueue<TaskLock> taskPool = new LinkedBlockingQueue<TaskLock>();
    /**
     * 不同ActorLock锁的队列维护，
     * 保证同一把ActorLock锁在执行任务时先通过此locks.contains(key)来判断是否已经有同一把ActorLock锁在执行
     */
    private final Map<ActorLock, TaskLock> locks = new ConcurrentHashMap<ActorLock, TaskLock>();

    private final List<Worker> workerList;

    private static final TaskLock EXIT_SIGNAL = new TaskLock();

    private static final List<ActorListener> listenerList = new CopyOnWriteArrayList<ActorListener>();

    public Actor() {
        this(DEFAULT_ACTOR_THREAD_NUM, false, DEFAULT_ACTOR_THREAD_PREFIX);
    }

    public Actor(String threadNamePrefix) {
        this(DEFAULT_ACTOR_THREAD_NUM, false, threadNamePrefix);
    }

    public Actor(int poolSize) {
        this(poolSize, false, DEFAULT_ACTOR_THREAD_PREFIX);
    }

    public Actor(int poolSize, String threadNamePrefix) {
        this(poolSize, false, threadNamePrefix);
    }

    public Actor(int poolSize, boolean daemon) {
        this(poolSize, daemon, DEFAULT_ACTOR_THREAD_PREFIX);
    }

    /**
     * 创建异步锁框架
     * @param poolSize 执行异步任务的线程池，
     *                 主要利于在多个不同的{@link ActorLock}进行锁资源争抢时线程池可以同时执行不同锁的任务执行
     * @param daemon 是否为守护线程
     * @param threadNamePrefix 线程名称前缀，方便进行jstack调试
     */
    public Actor(int poolSize, boolean daemon, String threadNamePrefix) {
        workerList = new ArrayList<Worker>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            Thread thread = new Thread(worker);
            thread.setDaemon(daemon);
            thread.setName(threadNamePrefix + i);
            workerList.add(worker);
            thread.start();
        }
    }

    public static ActorLock createLock(Object key) {
        return new ActorLock(key);
    }

    /**
     * 开始执行异步串行锁
     *
     * @param key 该业务的子业务锁，可以为AID或者AID+CMD等来保持同一子业务串行执行，
     *            注意该key如果是一个自定义对象，为了保定locks这个map能够找到类型相同的key，该key对象要实现hashCode和equals方法
     * @param task 异步锁拿到之后执行的回调方法
     */
    public synchronized void lock(ActorLock key, ActorTask task) {
        TaskLock lock = locks.get(key);
        if (lock == null) {
            lock = new TaskLock(key, this, task);
            locks.put(key, lock);
        } else {
            lock.offer(task);
        }
        key.setLock(lock);
        taskPool.offer(lock);
    }

    public void addListener(ActorListener listener) {
        listenerList.add(listener);
    }

    public synchronized void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;

        for (Worker worker : workerList) {
            worker.shutdown();
            taskPool.offer(EXIT_SIGNAL);
        }
    }

    private void removeLock(ActorLock lock) {
        locks.remove(lock);
    }

    private void addTask(TaskLock lock) {
        taskPool.offer(lock);
    }

    private static final class ExitTask implements ActorTask {
        @Override
        public ActorLock getLockKey() {
            return null;
        }

        @Override
        public void run() {
        }
    }

    final class Worker extends Thread {
        private volatile boolean running = false;

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    TaskLock lock = taskPool.take();
                    if (lock == null) {
                        continue;
                    }
                    if (lock == EXIT_SIGNAL) {
                        break;
                    }
                    ActorTask task = lock.acquire();
                    if (task != null) {
                        task.run();
                    }
                } catch (Throwable cause) {
                    cause.printStackTrace();
                    continue;
                }
            }
        }

        public synchronized void shutdown() {
            running = false;
        }
    }

    /**
     * 任务锁，同一把ActorLock锁执行的TaskLosk任务只会有一个在执行，
     * 在TaskLock执行完成之后会再判断同一把ActorLock锁是否还会其他任务在等待，有则取出执行，否则从{@link #locks}移动该锁任务
     */
    public static final class TaskLock {
        private ActorLock key;

        private Actor actor;

        private Queue<ActorTask> pendPool;

        private volatile boolean isRunning = false;

        private TaskLock() {
        }

        private TaskLock(ActorLock key, Actor actor, ActorTask task) {
            this.key = key;
            this.actor = actor;
            this.pendPool = new ConcurrentLinkedQueue<ActorTask>();
            this.pendPool.offer(task);
        }

        public void offer(ActorTask task) {
            this.pendPool.offer(task);
        }

        /**
         * 获取锁资源执行任务，
         * 如果当前队列中有在执行的任务则放入等等队列中等待上一个任务执行完成并释放锁资源
         */
        public synchronized ActorTask acquire() {
            if (!isRunning) {
                isRunning = true;
                ActorTask task = pendPool.poll();
                // 当前锁队列还没被线可执行线程，可以直接执行
                for (int i = 0; i < listenerList.size(); i++) {
                    ActorListener listener = listenerList.get(i);
                    listener.onActorStatusChange(key, LockStatus.RUNNING);
                }
                return task;
            }
            // 已经有一个任务在执行了
            // 等待锁队列中的任务执行完成之后才将未执行的任务从等待队列中取出执行
            for (int i = 0; i < listenerList.size(); i++) {
                ActorListener listener = listenerList.get(i);
                listener.onActorStatusChange(key, LockStatus.PENDING);
            }
            return null;
        }

        /**
         * 任务执行完成，释放锁资源，由异步任务手动执行，
         * 释放锁后会从任务等待队列获取先进来的任务再执行
         */
        public synchronized boolean release() {
            isRunning = false;
            boolean complete = false;
            for (int i = 0; i < listenerList.size(); i++) {
                ActorListener listener = listenerList.get(i);
                listener.onActorStatusChange(key, LockStatus.RELEASED);
            }
            if (pendPool.isEmpty()) {
                actor.removeLock(key);
                complete = true;
            } else {
                // 任务执行完毕，把缓冲队列中未执行的任务重新添加到执行队列中
                // 注意，队列是先进先出，所以未执行的任务可能要比其他任务要慢执行
                actor.addTask(this);
            }
            return complete;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TaskLock)) {
                return false;
            }
            return key.equals(((TaskLock) obj).key);
        }

        @Override
        public String toString() {
            return key.toString();
        }
    }
}
