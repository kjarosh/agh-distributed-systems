package pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeServiceGrpc;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeSubscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kamil Jarosz
 */
public class ExchangeServiceImpl extends ExchangeServiceGrpc.ExchangeServiceImplBase {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void monitorCurrencies(ExchangeSubscription subscription, StreamObserver<ExchangeResponse> responseObserver) {
        executorService.submit(new ExchangeProvider(subscription, responseObserver));
    }
}
