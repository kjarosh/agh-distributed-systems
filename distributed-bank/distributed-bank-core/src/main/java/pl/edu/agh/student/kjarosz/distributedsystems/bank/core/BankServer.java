package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountManagement;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.PremiumAccount;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
public class BankServer {
    public static final Set<String> SUPPORTED_CURRENCIES = new HashSet<>(Arrays.asList(
            "PLN", "USD", "CHF", "JPY"));

    private static final int PORT_ACCOUNT_MANAGEMENT = 9091;
    private static final int PORT_ACCOUNT_SERVICES = 9092;

    private static AccountRepository accountRepository = new AccountRepository();
    private static ExchangeRateService exchangeRateService = new ExchangeRateService("localhost", 9093);

    public static void main(String[] args) {
        new Thread(BankServer::runAccountManagement).start();
        new Thread(BankServer::runAccountServices).start();
    }

    private static void runAccountServices() {
        try {
            PremiumAccount.Processor<PremiumAccountHandler> processor1 =
                    new PremiumAccount.Processor<>(new PremiumAccountHandler(accountRepository, exchangeRateService));

            TServerTransport serverTransport = new TServerSocket(PORT_ACCOUNT_SERVICES);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(processor1));

            System.out.println("Starting account management");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runAccountManagement() {
        try {
            AccountManagement.Processor<AccountManagementHandler> processor1 =
                    new AccountManagement.Processor<>(new AccountManagementHandler(accountRepository));

            TServerTransport serverTransport = new TServerSocket(PORT_ACCOUNT_MANAGEMENT);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(processor1));

            System.out.println("Starting the simple server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
