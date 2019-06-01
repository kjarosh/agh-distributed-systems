package pl.edu.agh.student.kjarosz.ds.zoo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Phaser;

/**
 * @author Kamil Jarosz
 */
public class Application implements AutoCloseable {
    public static final String WATCHED_PATH = "/z";
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private final Properties config;

    private final ZooKeeper zk;
    private final ZooWatcher watcher;
    private final ProgramSupervisor programSupervisor;

    public Application(Properties config) throws IOException {
        this.config = config;
        logger.debug("Using properties:");
        config.forEach((k, v) -> logger.debug(" " + k + "=" + v));
        this.programSupervisor = new ProgramSupervisor(config.getProperty("zk.application"));
        this.zk = connect();
        this.watcher = new ZooWatcher(zk, WATCHED_PATH);
        this.watcher.addCreationListener(programSupervisor::ensureRunning);
        this.watcher.addDeletionListener(programSupervisor::ensureStopped);
        this.watcher.addListingListener(children -> {
            System.out.println("Children:");
            children.forEach(child -> System.out.println("  /z/" + child));
        });
    }

    public void run() {
        watcher.startWatchingCreation();

        Scanner s = new Scanner(System.in);
        while (!Thread.interrupted()) {
            try {
                String command = s.nextLine().trim();
                switch (command) {
                    case "ls":
                        printStructure(WATCHED_PATH);
                        break;

                    case "exit":
                    case "quit":
                    case "logout":
                        logger.info("Exit requested");
                        return;

                    default:
                        System.out.println("Unknown command: " + command);
                }
            } catch (InterruptedException e) {
                return;
            }
        }

        logger.info("Interrupted");
    }

    private void printStructure(String path) throws InterruptedException {
        try {
            for (String child : zk.getChildren(path, false)) {
                String childPath = path + "/" + child;
                System.out.println(childPath);
                printStructure(childPath);
            }
        } catch (KeeperException ignored) {
        }
    }

    private ZooKeeper connect() throws IOException {
        Phaser connectionPhaser = new Phaser(2);
        String connectString = config.getProperty("zk.connection");
        logger.info("Connecting to " + connectString);
        ZooKeeper zk = new ZooKeeper(connectString, 2000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectionPhaser.arriveAndAwaitAdvance();
            }

            logger.debug("Received watched event: " + event + ", state: " + event.getState() + ", path: " + event.getPath());
        });
        connectionPhaser.arriveAndAwaitAdvance();
        logger.info("Connected to " + connectString);
        return zk;
    }

    @Override
    public void close() throws InterruptedException {
        if (zk != null) zk.close();

        if (watcher != null) watcher.stop();

        if (programSupervisor != null) programSupervisor.ensureStopped();
    }
}
