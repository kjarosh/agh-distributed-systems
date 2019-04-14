package pl.edu.agh.student.kjarosz.distributedsystems.hospital.cli;

import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Administrator;
import pl.edu.agh.student.kjarosz.distributedsystems.hospital.clients.Technician;

import java.util.Scanner;

/**
 * @author Kamil Jarosz
 */
public class TechnicianMain {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Scanner s = new Scanner(System.in);
        System.out.print("Name: ");
        String name = s.nextLine();
        System.out.print("Exam 1: ");
        String exam1 = s.nextLine();
        System.out.print("Exam 2: ");
        String exam2 = s.nextLine();
        new Technician(name, factory, exam1, exam2);
    }
}
