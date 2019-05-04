package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeChange;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeServiceGrpc;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeSubscription;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class ExchangeRateService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ManagedChannel channel;
    private final Map<String, Double> currencies = new ConcurrentHashMap<>();
    private final Lock currenciesLock = new ReentrantLock();
    private final Condition noCurrency = currenciesLock.newCondition();

    public ExchangeRateService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        ExchangeServiceGrpc.ExchangeServiceStub exchangeServiceStub = ExchangeServiceGrpc.newStub(channel);
        ExchangeSubscription subscription = ExchangeSubscription.newBuilder()
                .setBaseCurrency("PLN")
                .addAllMonitoredCurrencies(BankServer.SUPPORTED_CURRENCIES)
                .build();
        exchangeServiceStub.monitorCurrencies(subscription, new StreamObserver<ExchangeResponse>() {
            @Override
            public void onNext(ExchangeResponse response) {
                currenciesLock.lock();
                try {
                    logger.info("Received currency rates: " + response.getChangesList()
                            .stream()
                            .map(ExchangeChange::getChangedCurrency)
                            .collect(Collectors.toList()));
                    for (ExchangeChange change : response.getChangesList()) {
                        currencies.put(change.getChangedCurrency(), change.getExchangeRate());
                    }
                    noCurrency.signalAll();
                } finally {
                    currenciesLock.unlock();
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error while streaming exchange rates", t);
            }

            @Override
            public void onCompleted() {
                logger.error("Streaming exchange rates completed abruptly");
            }
        });
    }

    public OptionalDouble getExchangeRate(String currency) {
        currenciesLock.lock();
        try {
            Double value;
            Date deadline = Date.from(Instant.now().plusSeconds(10));
            boolean stillWaiting = true;
            while ((value = currencies.get(currency)) == null) {
                if (!stillWaiting)
                    return OptionalDouble.empty();
                logger.info("Currency " + currency + " currently not available, waiting");
                stillWaiting = noCurrency.awaitUntil(deadline);
            }

            return OptionalDouble.of(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return OptionalDouble.empty();
        } finally {
            currenciesLock.unlock();
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
