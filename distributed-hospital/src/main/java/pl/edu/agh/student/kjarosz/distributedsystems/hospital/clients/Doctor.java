package pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.Hospital;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author Kamil Jarosz
 */
public class Doctor extends DefaultConsumer {
    private final String doctorId = UUID.randomUUID().toString();
    private final String name;

    public Doctor(String name, ConnectionFactory factory) throws IOException, TimeoutException {
        super(factory.newConnection().createChannel());
        this.name = name;
        Channel channel = super.getChannel();

        Hospital.declareExchange(channel);
        Hospital.declareInfoExchange(channel);

        String queueName = "doctor." + doctorId;
        channel.queueDeclare(queueName, false, false, true, null);
        channel.queueBind(queueName, Hospital.EXCHANGE_NAME, "doctor." + doctorId);
        channel.basicConsume(queueName, true, this);

        String infoQueue = channel.queueDeclare().getQueue();
        channel.queueBind(infoQueue, Hospital.INFO_EXCHANGE_NAME, "info");
        channel.basicConsume(infoQueue, true, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        if (envelope.getRoutingKey().equals("info")) {
            System.out.println("[Doctor " + name + "] Info: " + new String(body, StandardCharsets.UTF_8));
            return;
        }

        String message = new String(body, StandardCharsets.UTF_8);
        String[] parts = message.split(":");
        String patientName = parts[0];
        String examType = parts[1];
        System.out.println("[Doctor " + name + "] Received a result for: " + patientName + ", " + examType);
    }

    public void requestExamination(String patientName, String examType) throws IOException {
        getChannel().basicPublish(Hospital.EXCHANGE_NAME, "examination." + examType, null,
                (doctorId + ":" + patientName).getBytes(StandardCharsets.UTF_8));
    }
}
