package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.Order;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchRequest;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingRequest;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.restart;

/**
 * @author Kamil Jarosz
 */
public class BookstoreActor extends AbstractActor {

    private static final SupervisorStrategy strategy = new OneForOneStrategy(10,
            Duration.create("1 minute"), DeciderBuilder.matchAny(o -> restart()).build());

    private final BookstoreContext context = new BookstoreContext(getSelf(), getContext());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, request -> context.getSearchServiceRouter().route(request, getSender()))
                .match(Order.class, request -> context.getOrderServiceRouter().route(request, getSender()))
                .match(StreamingRequest.class, request -> context.getStreamingServiceRouter().route(request, getSender()))
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
