package com.salesmanager.shop.store.controller.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class SubprocessRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubprocessRunner.class);

	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;

	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }

	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	} 

    public static void info(String msg) {
        LOGGER.info("[PROCESS OUTPUT] " + msg);
    }   


    @Override
    public void run(String... args) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    Path currentRelativePath = Paths.get("");
                    String s = currentRelativePath.toAbsolutePath().toString();
                    LOGGER.info("Current absolute path is: " + s);
                    // For Go service uncomment:
                    // File workdir = new File(new File(s).getParentFile(), "gosrvc");
                    // For Python service uncomment:
                    File workdir = new File(new File(s).getParentFile(), "pysrvc");
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.directory(workdir);
                    // For Go service uncomment:
                    // builder.command("go", "run", ".")
                    // For Python service uncomment:
                    builder.command("python3", "main.py");                    
                    Process process = builder.start();
                    StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), SubprocessRunner::info);
                    Executors.newSingleThreadExecutor().submit(streamGobbler);
                    int exitCode = process.waitFor();
                    LOGGER.info("Runner exiting");
                } catch (Throwable thrown) {
                    thrown.printStackTrace(System.err);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();        
    }
}
