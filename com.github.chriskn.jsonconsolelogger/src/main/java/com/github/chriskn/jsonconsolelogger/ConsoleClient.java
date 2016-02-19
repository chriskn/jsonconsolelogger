package com.github.chriskn.jsonconsolelogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleClient {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleClient.class);

    public static void main(String[] args) throws Exception {
        int port = -1; 
        try {
            port = parsePort(args[0]);
        } catch (Exception e) {
            logger.error("Startup error", e);
            logger.info(help());
            System.exit(-1);
        }
        Server server = new Server(port);
        server.start();
    }

    private static int parsePort(String arg) throws Exception {
        int port = -1;
        try {
            port = Integer.parseInt(arg);
        } catch (Exception e) {
            logger.info(help());
            throw e;
        }
        if (port < 0) {
            throw new IllegalArgumentException(
                    "Port value should be a positive number. Current incoming port: " + port);
        }
        return port;
    }

    private static String help() {
        return "first param must be the port number as positive integer";
    }

}
