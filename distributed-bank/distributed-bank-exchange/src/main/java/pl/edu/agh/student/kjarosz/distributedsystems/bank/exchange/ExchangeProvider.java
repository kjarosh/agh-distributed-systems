package pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeSubscription;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kamil Jarosz
 */
class ExchangeProvider extends Thread implements StreamObserver<ExchangeSubscription> {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeProvider.class);

    private final Random random = new Random();

    private StreamObserver<ExchangeResponse> responseObserver;

    private ConcurrentSkipListSet<String> subscribed = new ConcurrentSkipListSet<>();
    private AtomicReference<String> baseCurrency = new AtomicReference<>();

    ExchangeProvider(StreamObserver<ExchangeResponse> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            String baseCurrency = this.baseCurrency.get();
            String currency = randomCurrency();
            responseObserver.onNext(generateResponse(baseCurrency, currency));
        }

        responseObserver.onCompleted();
    }

    @Override
    public void onNext(ExchangeSubscription subscription) {
        if (subscription.getCancel()) {
            subscribed.removeAll(subscription.getMonitoredCurrenciesList());
        } else {
            subscribed.addAll(subscription.getMonitoredCurrenciesList());
        }

        String newBaseCurrency = subscription.getBaseCurrency();
        if (!newBaseCurrency.isEmpty()) {
            baseCurrency.set(newBaseCurrency);
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.error("Client-side error occurred", t);
        Thread.currentThread().interrupt();
    }

    @Override
    public void onCompleted() {
        logger.debug("A client finished subscription");
        Thread.currentThread().interrupt();
    }

    private String randomCurrency() {
        int index = random.nextInt(subscribed.size());
        Iterator<String> iter = subscribed.iterator();
        for (int i = 0; i < index; ++i) {
            iter.next();
        }
        return iter.next();
    }

    private ExchangeResponse generateResponse(String baseCurrency, String currency) {
        double exchangeRate = 0;
        return ExchangeResponse.newBuilder()
                .setBaseCurrency(baseCurrency)
                .setChangedCurrency(currency)
                .setExchangeRate(exchangeRate)
                .build();
    }
}
