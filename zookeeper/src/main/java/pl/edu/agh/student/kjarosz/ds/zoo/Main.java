package pl.edu.agh.student.kjarosz.ds.zoo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Kamil Jarosz
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Properties props;
        if (args.length == 0) {
            props = System.getProperties();
        } else if (args.length == 1) {
            props = new Properties();
            try (InputStream fis = Files.newInputStream(Paths.get(args[0]))) {
                props.load(fis);
            }
        } else {
            System.err.println("Invalid number of arguments");
            System.exit(1);
            return;
        }

        try (Application app = new Application(props)) {
            app.run();
        }
    }
}
