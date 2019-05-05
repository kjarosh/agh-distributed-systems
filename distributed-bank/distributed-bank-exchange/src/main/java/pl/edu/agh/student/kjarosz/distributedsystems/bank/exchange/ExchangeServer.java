package pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
public class ExchangeServer {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeServer.class);

    private final int port;
    private final Server server;

    public ExchangeServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new ExchangeServiceImpl())
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Options options = new Options();
        options.addOption("port", true, "port");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        ExchangeServer server = new ExchangeServer(Integer.parseInt(cmd.getOptionValue("port", "9093")));
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            ExchangeServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
