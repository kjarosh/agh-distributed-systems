package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.Order;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.OrderComplete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Kamil Jarosz
 */
public class OrderDatabaseAccessActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(OrderDatabaseAccessActor.class);

    private final BookstoreContext context;
    private final String database;

    public OrderDatabaseAccessActor(BookstoreContext context, String database) {
        this.context = context;
        this.database = database;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Order.class, request -> {
                    Path databasePath = Paths.get(database);
                    if (!Files.exists(databasePath, LinkOption.NOFOLLOW_LINKS))
                        Files.createFile(databasePath);

                    try {
                        Files.writeString(databasePath, request.getTitle() + "\n", StandardOpenOption.APPEND);
                        getSender().tell(new OrderComplete(true), getSelf());
                    } catch (IOException e) {
                        logger.error("Cannot write an order", e);
                        getSender().tell(new OrderComplete(false), getSelf());
                    }
                })
                .build();
    }
}
