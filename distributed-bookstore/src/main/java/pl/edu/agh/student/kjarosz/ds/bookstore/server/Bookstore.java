package pl.edu.agh.student.kjarosz.ds.bookstore.server;

import akka.util.Timeout;
import scala.concurrent.duration.Duration;

/**
 * @author Kamil Jarosz
 */
public class Bookstore {
    public static final int SEARCH_SERVICES = 3;
    public static final long ORDER_SERVICES = 3;
    public static final long STREAMING_SERVICES = 10;

    public static final String DATABASE1 = "database_a.txt";
    public static final String DATABASE2 = "database_b.txt";
    public static final String ORDER_DATABASE = "orders.txt";

    public static final Timeout DATABASE_TIMEOUT = new Timeout(Duration.create(5, "seconds"));
    public static final Timeout SEARCH_TIMEOUT = new Timeout(Duration.create(5, "seconds"));
}
