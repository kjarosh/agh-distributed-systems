package pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.Hospital;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author Kamil Jarosz
 */
public class Administrator extends DefaultConsumer {
    private final String name;

    public Administrator(String name, ConnectionFactory factory) throws IOException, TimeoutException {
        super(factory.newConnection().createChannel());
        this.name = name;
        Channel channel = super.getChannel();

        Hospital.declareExchange(channel);
        Hospital.declareInfoExchange(channel);

        String queueName = "admin";
        channel.queueDeclare(queueName, false, false, true, null);
        channel.queueBind(queueName, "hospital", "#");
        channel.basicConsume(queueName, true, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        String message = new String(body, StandardCharsets.UTF_8);
        System.out.println("[Admin " + name + "] Received a message: " + message);
    }

    public void sendInfo(String message) throws IOException {
        getChannel().basicPublish(Hospital.INFO_EXCHANGE_NAME, "info", null,
                message.getBytes(StandardCharsets.UTF_8));
    }
}
