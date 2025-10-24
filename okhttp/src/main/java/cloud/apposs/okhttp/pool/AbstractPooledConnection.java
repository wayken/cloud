package cloud.apposs.okhttp.pool;

import cloud.apposs.logger.Logger;
import cloud.apposs.okhttp.HttpBuilder;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractPooledConnection implements IPooledConnection {
    /**
     * 连接池大小，注意这里表示连接池可以缓存的最大连接数，超过这个数则会进行不回收直接关闭
     */
    protected final int size;

    protected final HttpBuilder builder;

    /**
     * 连接池，以远程地址作为Key，连接队列作为Value
     */
    protected final Map<PoolKey, Queue<ReactIoConnection>> pool;

    /** 连接池同步锁 */
    protected final ReentrantLock mainLock = new ReentrantLock();

    public AbstractPooledConnection(int size, HttpBuilder builder) {
        this.size = size;
        this.builder = builder;
        this.pool = new ConcurrentHashMap<PoolKey, Queue<ReactIoConnection>>(size);
    }

    @Override
    public ReactIoConnection acquire(PoolKey key) throws Exception {
        Queue<ReactIoConnection> queue = pool.get(key);
        if (queue == null) {
            mainLock.lock();
            try {
                queue = pool.get(key);
                if (queue == null) {
                    queue = new ConcurrentLinkedQueue<ReactIoConnection>();
                    pool.put(key, queue);
                }
            } finally {
                mainLock.unlock();
            }
        }
        ReactIoConnection connection = queue.poll();
        if (connection == null) {
            connection = handleCreateConnection();
        }
        return connection;
    }

    @Override
    public void release(ReactIoConnection connection) {
        PoolKey key = connection.getPoolKey();
        Queue<ReactIoConnection> queue = pool.get(key);
        if (queue == null) {
            mainLock.lock();
            try {
                queue = pool.get(key);
                if (queue == null) {
                    queue = new ConcurrentLinkedQueue<ReactIoConnection>();
                    pool.put(key, queue);
                }
            } finally {
                mainLock.unlock();
            }
        }
        // 连接池已满则关闭连接不再放入连接池
        if (queue.size() >= size) {
            connection.close();
            Logger.warn("Connection pool is full, close the connection: " + connection);
            return;
        }
        queue.offer(connection);
    }

    @Override
    public void remove(ReactIoConnection connection) {
        PoolKey key = connection.getPoolKey();
        Queue<ReactIoConnection> queue = pool.get(key);
        if (queue != null) {
            mainLock.lock();
            try {
                queue.remove(connection);
            } finally {
                mainLock.unlock();
            }
        }
    }

    protected abstract ReactIoConnection handleCreateConnection() throws Exception;
}
