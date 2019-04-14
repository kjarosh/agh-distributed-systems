package pl.edu.agh.student.kjarosz.distributedsystems.hospital;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
public class Hospital {
    public static final String EXCHANGE_NAME = "hospital";
    public static final String INFO_EXCHANGE_NAME = "info";

    public static void declareExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    }

    public static void declareInfoExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(INFO_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    }
}
