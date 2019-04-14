package pl.edu.agh.student.kjarosz.distributedsystems.hospital.cli;

import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Administrator;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Doctor;

import java.util.Scanner;

/**
 * @author Kamil Jarosz
 */
public class DoctorMain {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Scanner s = new Scanner(System.in);
        System.out.print("Name: ");
        String name = s.nextLine();
        Doctor doc = new Doctor(name, factory);

        while (!Thread.interrupted()) {
            System.out.print("> ");
            String[] message = s.nextLine().trim().split("\\s+");
            String patientName = message[0];
            String examType = message[1];
            doc.requestExamination(patientName, examType);
        }
    }
}
