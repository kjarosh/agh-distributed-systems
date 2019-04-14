package pl.edu.agh.student.kjarosz.distributedsystems.hospital.cli;

import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Administrator;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Doctor;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Technician;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Kamil Jarosz
 */
public class AdminMain {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Scanner s = new Scanner(System.in);
        System.out.print("Name: ");
        String name = s.nextLine();
        Administrator admin = new Administrator(name, factory);

        while (!Thread.interrupted()) {
            System.out.print("> ");
            String message = s.nextLine().trim();
            admin.sendInfo(message);
        }
    }
}
