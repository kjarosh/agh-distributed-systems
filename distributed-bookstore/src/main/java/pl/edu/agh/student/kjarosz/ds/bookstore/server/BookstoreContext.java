package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.BalancingRoutingLogic;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import scala.concurrent.ExecutionContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class BookstoreContext {

    private final ExecutionContext executionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool());
    private final ActorRef bookstore;
    private final Router searchServiceRouter;
    private final Router orderServiceRouter;
    private final Router streamingServiceRouter;
    private final ActorRef databaseAccessActor1;
    private final ActorRef databaseAccessActor2;
    private final ActorRef orderDatabaseAccessActor;

    public BookstoreContext(ActorRef bookstore, ActorContext context) {
        this.bookstore = bookstore;

        List<Routee> searchServiceRoutees = Stream.generate(() -> newSearchServiceActor(context))
                .limit(Bookstore.SEARCH_SERVICES)
                .map(ActorRefRoutee::new)
                .collect(Collectors.toList());
        this.searchServiceRouter = new Router(new RoundRobinRoutingLogic(), searchServiceRoutees);

        List<Routee> orderServiceRoutees = Stream.generate(() -> newOrderServiceActor(context))
                .limit(Bookstore.ORDER_SERVICES)
                .map(ActorRefRoutee::new)
                .collect(Collectors.toList());
        this.orderServiceRouter = new Router(new RoundRobinRoutingLogic(), orderServiceRoutees);

        List<Routee> streamingServiceRoutees = Stream.generate(() -> newStreamingServiceActor(context))
                .limit(Bookstore.STREAMING_SERVICES)
                .map(ActorRefRoutee::new)
                .collect(Collectors.toList());
        this.streamingServiceRouter = new Router(new RoundRobinRoutingLogic(), streamingServiceRoutees);

        this.databaseAccessActor1 = newDatabaseAccessActor(context, Bookstore.DATABASE1);
        this.databaseAccessActor2 = newDatabaseAccessActor(context, Bookstore.DATABASE2);
        this.orderDatabaseAccessActor = newOrderDatabaseAccessActor(context);
    }

    private ActorRef newStreamingServiceActor(ActorContext context) {
        return context.actorOf(Props.create(StreamingServiceActor.class, this), "streaming-service-" + UUID.randomUUID());
    }

    private ActorRef newOrderServiceActor(ActorContext context) {
        return context.actorOf(Props.create(OrderServiceActor.class, this), "order-service-" + UUID.randomUUID());
    }

    private ActorRef newSearchServiceActor(ActorContext context) {
        return context.actorOf(Props.create(SearchServiceActor.class, this), "search-service-" + UUID.randomUUID());
    }

    private ActorRef newDatabaseAccessActor(ActorContext context, String database) {
        return context.actorOf(Props.create(DatabaseAccessActor.class, this, database), "db-access-" + UUID.randomUUID());
    }

    private ActorRef newOrderDatabaseAccessActor(ActorContext context) {
        return context.actorOf(Props.create(OrderDatabaseAccessActor.class, this, Bookstore.ORDER_DATABASE), "order-db-access");
    }

    public Router getSearchServiceRouter() {
        return searchServiceRouter;
    }

    public ActorRef getDatabaseAccessActor1() {
        return databaseAccessActor1;
    }

    public ActorRef getDatabaseAccessActor2() {
        return databaseAccessActor2;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public Router getOrderServiceRouter() {
        return orderServiceRouter;
    }

    public ActorRef getOrderDatabaseAccessActor() {
        return orderDatabaseAccessActor;
    }

    public ActorRef getBookstore() {
        return bookstore;
    }

    public Router getStreamingServiceRouter() {
        return streamingServiceRouter;
    }
}
