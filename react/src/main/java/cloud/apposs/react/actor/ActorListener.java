package cloud.apposs.react.actor;

public interface ActorListener {
    void onActorStatusChange(ActorLock lock, LockStatus status);
}
