package pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeChange;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeSubscription;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
class ExchangeProvider extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeProvider.class);

    private final Random random = new Random();
    private final ExchangeSubscription subscription;

    private final StreamObserver<ExchangeResponse> responseObserver;
    private final Map<String, Double> rates = new ConcurrentHashMap<>();

    ExchangeProvider(ExchangeSubscription subscription, StreamObserver<ExchangeResponse> responseObserver) {
        this.subscription = subscription;
        this.responseObserver = responseObserver;
    }

    @Override
    public void run() {
        logger.info("Starting exchange provider");

        responseObserver.onNext(generateInitialResponse());

        while (!Thread.interrupted()) {
            String baseCurrency = subscription.getBaseCurrency();
            String currency = randomCurrency();
            responseObserver.onNext(ExchangeResponse.newBuilder()
                    .addChanges(generateChange(baseCurrency, currency))
                    .build());

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                break;
            }
        }

        responseObserver.onCompleted();
    }

    private String randomCurrency() {
        int index = random.nextInt(subscription.getMonitoredCurrenciesCount());
        return subscription.getMonitoredCurrencies(index);
    }

    private ExchangeResponse generateInitialResponse() {
        return ExchangeResponse.newBuilder()
                .addAllChanges(subscription.getMonitoredCurrenciesList().stream()
                        .map(c -> generateChange(subscription.getBaseCurrency(), c))::iterator)
                .build();
    }

    private ExchangeChange generateChange(String baseCurrency, String currency) {
        logger.info("Currency changed: " + currency);

        double exchangeRate;

        if (baseCurrency.equals(currency)) {
            exchangeRate = 1;
        } else {
            double lastRate = rates.computeIfAbsent(currency, i -> 1d / (random.nextDouble() + 0.5d));
            exchangeRate = lastRate + (random.nextDouble() - 0.5d) / 10;
        }

        return ExchangeChange.newBuilder()
                .setChangedCurrency(currency)
                .setExchangeRate(exchangeRate)
                .build();
    }
}
