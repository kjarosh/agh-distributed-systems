package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingComplete;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingPart;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class StreamingServiceActor extends AbstractActor {

    private final Logger logger = LoggerFactory.getLogger(getSelf().toString());
    private BookstoreContext context;

    public StreamingServiceActor(BookstoreContext context) {
        this.context = context;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamingRequest.class, request -> {
                    Materializer materializer = ActorMaterializer.create(getContext().system());
                    InputStream resourceAsStream = StreamingServiceActor.class.getClassLoader()
                            .getResourceAsStream("streaming/" + request.getTitle());
                    if (resourceAsStream == null) {
                        logger.info("Unavailable title for streaming: " + request.getTitle());
                        getSender().tell(new StreamingComplete(false), getSelf());
                        return;
                    }

                    List<String> lines;
                    try (InputStream resource = resourceAsStream) {
                        lines = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.toList());
                    }

                    Source<StreamingPart, NotUsed> source = Source.fromPublisher(subscriber -> {
                        lines.stream()
                                .map(StreamingPart::new)
                                .peek(p -> {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .forEach(subscriber::onNext);
                        subscriber.onComplete();
                    });

                    Sink<StreamingPart, NotUsed> sinkPrint = Sink.actorRef(getSender(), new StreamingComplete(true));
                    logger.info("Starting streaming");
                    source.runWith(sinkPrint, materializer);
                })
                .build();
    }
}
