package cloud.apposs.react;

import java.util.*;

public class IoSubscriptionList implements IoSubscription {
    private volatile boolean unsubscribed;

    private List<IoSubscription> subscriptions;

    public IoSubscriptionList() {
    }

    public IoSubscriptionList(IoSubscription... subscriptions) {
        this.subscriptions = new LinkedList<IoSubscription>(Arrays.asList(subscriptions));
    }

    public IoSubscriptionList(IoSubscription subscription) {
        this.subscriptions = new LinkedList<IoSubscription>();
        this.subscriptions.add(subscription);
    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed;
    }

    public void add(IoSubscription subscription) {
        if (subscription.isUnsubscribed()) {
            return;
        }
        if (!unsubscribed) {
            synchronized (this) {
                if (!unsubscribed) {
                    List<IoSubscription> subscriptions = this.subscriptions;
                    if (subscriptions == null) {
                        subscriptions = new LinkedList<IoSubscription>();
                        this.subscriptions = subscriptions;
                    }
                    subscriptions.add(subscription);
                    return;
                }
            }
        }
        subscription.unsubscribe();
    }

    public void remove(IoSubscription subscription) {
        if (!unsubscribed) {
            boolean unsubscribe;
            synchronized (this) {
                List<IoSubscription> subs = subscriptions;
                if (unsubscribed || subs == null) {
                    return;
                }
                unsubscribe = subs.remove(subscription);
            }
            if (unsubscribe) {
                subscription.unsubscribe();
            }
        }
    }

    @Override
    public void unsubscribe() {
        if (!unsubscribed) {
            List<IoSubscription> subs;
            synchronized (this) {
                if (unsubscribed) {
                    return;
                }
                unsubscribed = true;
                subs = subscriptions;
                subscriptions = null;
            }
            unsubscribeFromAll(subs);
        }
    }

    private static void unsubscribeFromAll(Collection<IoSubscription> subscriptions) {
        if (subscriptions == null) {
            return;
        }
        List<Throwable> causes = null;
        for (IoSubscription subscription : subscriptions) {
            try {
                subscription.unsubscribe();
            } catch (Throwable e) {
                if (causes == null) {
                    causes = new ArrayList<Throwable>();
                }
                causes.add(e);
            }
        }
    }
}
