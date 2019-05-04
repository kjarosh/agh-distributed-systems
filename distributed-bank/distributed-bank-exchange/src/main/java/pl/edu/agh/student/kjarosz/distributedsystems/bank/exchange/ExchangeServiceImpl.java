package pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeServiceGrpc;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.exchange.api.ExchangeSubscription;

/**
 * @author Kamil Jarosz
 */
public class ExchangeServiceImpl extends ExchangeServiceGrpc.ExchangeServiceImplBase {
    @Override
    public StreamObserver<ExchangeSubscription> monitorCurrencies(StreamObserver<ExchangeResponse> responseObserver) {
        return new ExchangeProvider(responseObserver);
    }
}
