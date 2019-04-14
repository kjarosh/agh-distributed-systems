package pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
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
public class Technician extends DefaultConsumer {
    private final String name;

    public Technician(String name, ConnectionFactory factory, String... examTypes) throws IOException, TimeoutException {
        super(factory.newConnection().createChannel());
        this.name = name;
        Channel channel = super.getChannel();

        Hospital.declareExchange(channel);
        Hospital.declareInfoExchange(channel);

        for (String examType : examTypes) {
            String queueName = "examination." + examType;
            channel.queueDeclare(queueName, false, false, true, null);
            channel.queueBind(queueName, Hospital.EXCHANGE_NAME, "examination." + examType);
            channel.basicConsume(queueName, true, this);
        }

        String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, Hospital.INFO_EXCHANGE_NAME, "info");
        channel.basicConsume(infoQueue, true, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (envelope.getRoutingKey().equals("info")) {
            System.out.println("[Technician " + name + "] Info: " + new String(body, StandardCharsets.UTF_8));
            return;
        }

        String examType = envelope.getRoutingKey().substring("examination.".length());
        String[] parts = new String(body, StandardCharsets.UTF_8).split(":");
        String doctorId = parts[0];
        String patientName = parts[1];
        System.out.println("[Technician " + name + "] Received an examination request: " + examType + ", " + patientName);

        getChannel().basicPublish(Hospital.EXCHANGE_NAME, "doctor." + doctorId, null,
                (patientName + ":" + examType + ":done").getBytes(StandardCharsets.UTF_8));
    }
}
