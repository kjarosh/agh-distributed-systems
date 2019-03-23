package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import java.util.Scanner;

public class Main {
    private final DistributedMap map;

    public static void main(String[] args) throws Exception {
        try (DistributedMap map = new DistributedMap("distributed-map", "230.100.200.123")) {
            new Main(map).run();
        }
    }

    private Main(DistributedMap map) {
        this.map = map;
    }

    private void run() {
        Scanner s = new Scanner(System.in);
        while (!Thread.interrupted()) {
            System.out.print("> ");
            System.out.flush();
            String line = s.nextLine();
            if (line.isEmpty()) continue;
            String[] cmdline = line.split("\\s+");

            interpret(cmdline);
            System.err.flush();
        }
    }

    private void interpret(String[] cmdline) {
        switch (cmdline[0]) {
            case "print":
                System.out.println(map.toString());
                break;

            case "put":
                if (cmdline.length != 3) {
                    System.err.println("Wrong number of arguments");
                } else {
                    String key = cmdline[1];
                    Integer value;
                    try {
                        value = Integer.parseInt(cmdline[2]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number");
                        return;
                    }

                    map.put(key, value);
                }
                break;

            case "get":
                if (cmdline.length != 2) {
                    System.err.println("Wrong number of arguments");
                } else {
                    String key = cmdline[1];

                    System.out.println(map.get(key));
                }
                break;

            default:
                System.err.println("Unknown command: " + cmdline[0]);
                break;
        }
    }
}
