package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.AbstractActor;
import akka.pattern.Patterns;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.Order;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.OrderComplete;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.OrderResponse;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchRequest;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * @author Kamil Jarosz
 */
public class OrderServiceActor extends AbstractActor {

    private final BookstoreContext context;

    public OrderServiceActor(BookstoreContext context) {
        this.context = context;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Order.class, order -> {
                    Future<Object> future = Patterns.ask(context.getBookstore(), new SearchRequest(order.getTitle()), Bookstore.SEARCH_TIMEOUT);
                    SearchResponse searchResponse = (SearchResponse) Await.result(future, Bookstore.SEARCH_TIMEOUT.duration());

                    if (!searchResponse.isExists()) {
                        tellOrderFailed(order);
                        return;
                    }

                    Future<Object> future2 = Patterns.ask(context.getOrderDatabaseAccessActor(), order, Bookstore.DATABASE_TIMEOUT);
                    OrderComplete orderComplete = (OrderComplete) Await.result(future2, Bookstore.DATABASE_TIMEOUT.duration());

                    if (orderComplete.isSuccess()) {
                        getSender().tell(new OrderResponse(order.getTitle(), true, searchResponse.getPrice()), getSelf());
                    } else {
                        tellOrderFailed(order);
                    }
                })
                .build();
    }

    private void tellOrderFailed(Order order) {
        getSender().tell(new OrderResponse(order.getTitle(), false, null), getSelf());
    }
}
