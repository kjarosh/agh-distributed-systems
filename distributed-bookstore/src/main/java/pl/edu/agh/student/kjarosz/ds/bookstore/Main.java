package pl.edu.agh.student.kjarosz.ds.bookstore;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.BookstoreActor;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.Order;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.SearchRequest;
import pl.edu.agh.student.kjarosz.ds.bookstore.server.message.StreamingRequest;

import java.util.Scanner;

/**
 * @author Kamil Jarosz
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("bookstore");
        ActorRef bookstoreActor = system.actorOf(Props.create(BookstoreActor.class), "bookstore");
        ActorRef clientActor = system.actorOf(Props.create(BookstoreClientActor.class), "client");

        runClient(bookstoreActor, clientActor);
    }

    private static void runClient(ActorRef bookstore, ActorRef client) {
        logger.info("Examples:");
        logger.info("search Title1");
        logger.info("order Title1");
        logger.info("stream Title1");

        Scanner s = new Scanner(System.in);
        while (true) {
            String line = s.nextLine();
            String[] args = line.split("\\s+");
            if (args.length != 2) {
                logger.error("Invalid args");
                continue;
            }

            switch (args[0]) {
                case "search":
                    requestSearch(bookstore, client, args[1]);
                    break;
                case "order":
                    requestOrder(bookstore, client, args[1]);
                    break;
                case "stream":
                    requestStreaming(bookstore, client, args[1]);
                    break;
                default:
                    logger.error("Unknown command: " + args[0]);
                    break;
            }
        }
    }

    private static void requestOrder(ActorRef bookstore, ActorRef client, String title) {
        Order order = new Order(title);
        logger.info("Requesting order: " + order);
        bookstore.tell(order, client);
    }

    private static void requestSearch(ActorRef bookstore, ActorRef client, String title) {
        SearchRequest request = new SearchRequest(title);
        logger.info("Requesting search: " + request);
        bookstore.tell(request, client);
    }

    private static void requestStreaming(ActorRef bookstore, ActorRef client, String title) {
        StreamingRequest request = new StreamingRequest(title);
        logger.info("Requesting streaming: " + request);
        bookstore.tell(request, client);
    }
}
