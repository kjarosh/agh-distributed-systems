package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(BankServer.class);

    private static int managementPort = 9091;
    private static int servicesPort = 9092;

    private static AccountRepository accountRepository = new AccountRepository();
    private static ExchangeRateService exchangeRateService;

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", "exchange-host", true, "exchange host");
        options.addOption("p", "exchange-port", true, "exchange port");
        options.addOption("m", "mport", true, "management port");
        options.addOption("s", "sport", true, "services port");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String exchangeHost = cmd.getOptionValue("exchange-host", "localhost");
        int exchangePort = Integer.parseInt(cmd.getOptionValue("exchange-port", "9093"));
        exchangeRateService = new ExchangeRateService(exchangeHost, exchangePort);
        managementPort = Integer.parseInt(cmd.getOptionValue("mport", "9091"));
        servicesPort = Integer.parseInt(cmd.getOptionValue("sport", "9092"));

        logger.info("Using exchange on " + exchangeHost + ":" + exchangePort);
        logger.info("Management port " + managementPort);
        logger.info("Services port " + servicesPort);

        new Thread(BankServer::runAccountManagement).start();
        new Thread(BankServer::runAccountServices).start();
    }

    private static void runAccountServices() {
        try {
            PremiumAccount.Processor<PremiumAccountHandler> processor1 =
                    new PremiumAccount.Processor<>(new PremiumAccountHandler(accountRepository, exchangeRateService));

            TServerTransport serverTransport = new TServerSocket(servicesPort);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport)
                    .protocolFactory(protocolFactory)
                    .processor(processor1));

            System.out.println("Starting account services");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runAccountManagement() {
        try {
            AccountManagement.Processor<AccountManagementHandler> processor1 =
                    new AccountManagement.Processor<>(new AccountManagementHandler(accountRepository));

            TServerTransport serverTransport = new TServerSocket(managementPort);
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport)
                    .protocolFactory(protocolFactory)
                    .processor(processor1));

            System.out.println("Starting account management");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
