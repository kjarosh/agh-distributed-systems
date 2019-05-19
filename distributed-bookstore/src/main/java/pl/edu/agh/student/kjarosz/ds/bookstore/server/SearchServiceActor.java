package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchRequest;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchResponse;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.util.Try;

/**
 * @author Kamil Jarosz
 */
public class SearchServiceActor extends AbstractActor {

    private final BookstoreContext context;

    public SearchServiceActor(BookstoreContext context) {
        this.context = context;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, request -> {
                    ExecutionContext executionContext = context.getExecutionContext();
                    Future<SearchResponse> result1Future = Patterns.ask(context.getDatabaseAccessActor1(), request, Bookstore.DATABASE_TIMEOUT)
                            .map(r -> (SearchResponse) r, executionContext);
                    Future<SearchResponse> result2Future = Patterns.ask(context.getDatabaseAccessActor2(), request, Bookstore.DATABASE_TIMEOUT)
                            .map(r -> (SearchResponse) r, executionContext);

                    ActorRef sender = getSender();
                    result1Future.onComplete(result1 -> handleResult(request, result1, result2Future, sender), executionContext);
                })
                .build();
    }

    private Void handleResult(SearchRequest request, Try<SearchResponse> result, Future<SearchResponse> fallback, ActorRef sender) {
        if (result.isSuccess() && result.get().isExists()) {
            sender.tell(result.get(), getSelf());
        } else if (fallback != null) {
            fallback.onComplete(result2 -> handleResult(request, result2, null, sender), context.getExecutionContext());
        } else {
            sender.tell(new SearchResponse(request.getTitle(), false, null), getSelf());
        }

        return null;
    }
}
