package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchRequest;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchResponse;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Kamil Jarosz
 */
public class DatabaseAccessActor extends AbstractActor {

    private final BookstoreContext context;
    private final String database;

    public DatabaseAccessActor(BookstoreContext context, String database) {
        this.context = context;
        this.database = database;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, request -> {
                    ActorRef sender = getSender();
                    ActorRef self = getSelf();
                    String title = request.getTitle();
                    context.getExecutionContext().execute(() -> {
                        try (Scanner scanner = new Scanner(getInputStream())) {
                            String[] line = scanner.nextLine().split(":");
                            String dbTitle = line[0];
                            String dbPrice = line[1];

                            if (dbTitle.equals(title)) {
                                sender.tell(new SearchResponse(title, true, dbPrice), self);
                                return;
                            }
                        }

                        sender.tell(new SearchResponse(title, false, null), self);
                    });
                })
                .build();
    }

    private InputStream getInputStream() {
        return DatabaseAccessActor.class.getClassLoader().getResourceAsStream(database);
    }
}
