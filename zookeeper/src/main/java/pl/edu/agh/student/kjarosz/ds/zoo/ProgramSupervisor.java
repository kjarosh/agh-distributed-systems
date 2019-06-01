package pl.edu.agh.student.kjarosz.ds.zoo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Kamil Jarosz
 */
public class ProgramSupervisor {
    private static final Logger logger = LoggerFactory.getLogger(ProgramSupervisor.class);

    private final String program;

    private Process process;

    public ProgramSupervisor(String program) {
        this.program = program;
    }

    public void ensureRunning() {
        try {
            if (process != null) {
                return;
            }

            process = Runtime.getRuntime().exec(program);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ensureStopped() {
        if (process == null) {
            return;
        }

        process.destroy();
        try {
            process.onExit().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("While waiting for child process to complete", e);
        } catch (TimeoutException ignored) {

        }

        if (process.isAlive()) {
            process.destroyForcibly();
        }
        process = null;
    }
}
