package cloud.apposs.react.actor;

/**
 * 当前锁状态，主要用于锁状态监听
 */
public enum LockStatus {
    PENDING, // 当前锁挂起，等待同一锁的其他任务执行完成再执行当前锁任务
    RUNNING, // 当前锁任务正在执行
    RELEASED // 当前锁任务已经执行完毕
}
