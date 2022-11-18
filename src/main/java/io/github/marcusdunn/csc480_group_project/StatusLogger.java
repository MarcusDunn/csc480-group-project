package io.github.marcusdunn.csc480_group_project;


import java.util.logging.Logger;

public class StatusLogger implements Runnable {
    private static final Logger logger = Logger.getLogger(StatusLogger.class.getName());

    private final Runnable inner;
    private final String name;

    public StatusLogger(Runnable inner, String name) {
        this.inner = inner;
        this.name = name;
    }

    @Override
    public void run() {
        final var thread = new Thread(inner, name);
        final var seconds = 15;
        thread.start();
        while (thread.isAlive()) {
            logger.info(() -> thread.getName() + " is still being analyzed");
            try {
                //noinspection BusyWait - this is exactly what we want to do
                Thread.sleep(1000 * seconds);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
