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
public class TestScenario {
    private static List<Administrator> admins = new ArrayList<>();
    private static List<Doctor> doctors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        new Technician("Steve", factory, "knee", "hip");
        new Technician("Gary", factory, "knee", "elbow");

        admins.add(new Administrator("Andrew", factory));
        doctors.add(new Doctor("Bob", factory));
        doctors.add(new Doctor("Al", factory));

        Scanner s = new Scanner(System.in);
        System.out.println("example> d 0 knee Brian");
        System.out.println("example> a 0 hello");
        while (!Thread.interrupted()) {
            System.out.print("> ");
            String type = s.next().trim();
            int number = s.nextInt();

            if (type.equals("d")) {
                String examType = s.next().trim();
                String patientName = s.nextLine().trim();

                doctors.get(number).requestExamination(patientName, examType);
            } else if (type.equals("a")) {
                String message = s.nextLine().trim();
                admins.get(number).sendInfo(message);
            } else {
                System.out.println("I don't understand");
            }
        }
    }
}
