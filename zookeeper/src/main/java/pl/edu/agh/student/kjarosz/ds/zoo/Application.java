package pl.edu.agh.student.kjarosz.ds.zoo;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Phaser;

/**
 * @author Kamil Jarosz
 */
public class Application implements AutoCloseable {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    private ZooKeeper zk;
    private Properties config;

    public Application(Properties config) {
        this.config = config;
        logger.debug("Using properties:");
        config.forEach((k, v) -> logger.debug(" " + k + "=" + v));
    }

    public void run() throws IOException, InterruptedException {
        connect();


        Thread.sleep(100000);
    }

    private void connect() throws IOException {
        Phaser connectionPhaser = new Phaser(2);
        String connectString = config.getProperty("zk.connection");
        logger.info("Connecting to " + connectString);
        zk = new ZooKeeper(connectString, 2000, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectionPhaser.arriveAndAwaitAdvance();
            }

            logger.debug("Received watched event: " + event + ", state: " + event.getState() + ", path: " + event.getPath());
        });
        connectionPhaser.arriveAndAwaitAdvance();
        logger.info("Connected to " + connectString);
    }

    @Override
    public void close() throws InterruptedException {
        if (zk != null) zk.close();
    }
}
