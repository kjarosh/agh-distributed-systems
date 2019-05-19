package pl.edu.agh.student.kjarosz.ds.bookstore;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.OrderResponse;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchResponse;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingComplete;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingPart;

/**
 * @author Kamil Jarosz
 */
public class BookstoreClientActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(BookstoreClientActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderResponse.class, response -> logger.info("Order response: " + response))
                .match(SearchResponse.class, response -> logger.info("Search response: " + response))
                .match(StreamingPart.class, part -> logger.info("Streaming part: " + part))
                .match(StreamingComplete.class, complete -> logger.info("Streaming complete: " + complete))
                .match(Object.class, obj -> logger.info("Unknown: " + obj))
                .build();
    }
}
