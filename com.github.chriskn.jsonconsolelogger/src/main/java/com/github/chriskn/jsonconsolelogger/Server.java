package com.github.chriskn.jsonconsolelogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends Thread {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    ServerSocket serverSocket;

    public Server(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            logger.error("Could not listen on port: " + port, e);
            throw e;
        }
    }

    @Override
    public void run() {
        logger.info("Waiting for a new connection on port " + serverSocket.getLocalPort()); 
        while (!serverSocket.isClosed()) {
            try {
                final Socket socket = serverSocket.accept();
                new SocketHandler(socket).run();
            } catch (RejectedExecutionException e) {
                logger.warn("Execution rejected", e);
            } catch (IOException e) {
                logger.warn("Failed to accept a new connection: server socket was closed.", e);
            }
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("IOException occurred while closing server socket:", e);
        } finally {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("InterruptedException occurred while shutting down server executor.", e);
            }
        }
        interrupt();
        logger.debug("Server was stopped");
    }
}
