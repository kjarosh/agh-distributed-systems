package pl.edu.agh.student.kjarosz.ds.zoo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeCreated;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;

/**
 * @author Kamil Jarosz
 */
public class ZooWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ZooWatcher.class);

    private final ZooKeeper zk;
    private final String watchedPath;

    private final List<Runnable> creationListeners = new ArrayList<>();
    private final List<Runnable> deletionListeners = new ArrayList<>();
    private final List<Consumer<List<String>>> listingListeners = new ArrayList<>();

    private AtomicBoolean stopped = new AtomicBoolean(false);

    public ZooWatcher(ZooKeeper zk, String watchedPath) {
        this.zk = zk;
        this.watchedPath = watchedPath;
    }

    public void addCreationListener(Runnable listener) {
        creationListeners.add(listener);
    }

    public void addDeletionListener(Runnable listener) {
        deletionListeners.add(listener);
    }

    public void addListingListener(Consumer<List<String>> listener) {
        listingListeners.add(listener);
    }

    public void startWatchingCreation() {
        try {
            boolean exists = zk.exists(watchedPath, event -> {
                if (event.getType() == NodeCreated) {
                    created();
                }
            }) != null;

            if (exists) created();
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startWatchingDeletion() {
        try {
            boolean exists = zk.exists(watchedPath, event -> {
                if (event.getType() == NodeDeleted) {
                    deleted();
                }
            }) != null;

            if (!exists) deleted();
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifyListingChangedAndWatch() {
        try {
            List<String> children = zk.getChildren(watchedPath, event -> {
                if (event.getType() == NodeChildrenChanged && !stopped.get()) {
                    notifyListingChangedAndWatch();
                }
            });

            listingChanged(children);
        } catch (KeeperException.NoNodeException e) {
            // node has been deleted
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void listingChanged(List<String> children) {
        if (stopped.get()) return;

        logger.info("Listing changed");
        listingListeners.forEach(c -> c.accept(children));
    }

    private void deleted() {
        if (stopped.get()) return;

        startWatchingCreation();
        logger.info("Path created");
        deletionListeners.forEach(Runnable::run);
    }

    private void created() {
        if (stopped.get()) return;

        startWatchingDeletion();
        notifyListingChangedAndWatch();
        logger.info("Path deleted");
        creationListeners.forEach(Runnable::run);
    }

    public void stop() {
        stopped.set(true);
    }
}
