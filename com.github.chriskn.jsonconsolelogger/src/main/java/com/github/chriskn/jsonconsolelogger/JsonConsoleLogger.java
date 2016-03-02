package com.github.chriskn.jsonconsolelogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start the JsonConsoleLogger.
 */
public class JsonConsoleLogger {

	// package scope to be access from tests
	final static int RC_OK = 0;
	final static int RC_USAGE = 1;

	private final Logger logger = LoggerFactory.getLogger(JsonConsoleLogger.class);

	public static void main(String[] args) {
		JsonConsoleLogger consoleLogger = new JsonConsoleLogger();
		int rc = consoleLogger.doMain(args);
		if (rc == RC_USAGE) {
			System.out.println("Usage: JsonConsoleLogger [-h] {port}");
			System.out.println("  if port is missing port has a default of 4444");
		}
		// only call system exit here from static main
		System.exit(rc);
	}

	public int doMain(String[] args) {
		// Parse port, default is 4444
		int port = 4444;
		// Parse for "-h" argument
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (("-h".equalsIgnoreCase(args[i])) || ("--help".equalsIgnoreCase(args[i]))) {
					return RC_USAGE;
				}
			}
			if (args.length > 0) {
				try {
					port = parsePort(args[0]);
				} catch (Exception ex) {
					// error: port can not be parsed or is invalid
					return RC_USAGE;
				}
			}
		}
		try {
			logger.info("Server will be started on port " + port);
			Thread server = createServer(port);
			server.start();
			// wait until server has been stopped
			server.join();
		} catch (Exception ex) {
			logger.error("Server stopped with problem", ex);
		}
		return RC_OK;
	}

	/**
	 * Factory method to allow different server mocks in test cases.
	 */
	protected Thread createServer(int port) throws Exception {
		return new Server(port);
	}

	/**
	 * Parse the port and raise an exception in case port can not be parsed or
	 * is negative.
	 */
	private int parsePort(String portAsString) throws Exception {
		int port = -1;
		try {
			port = Integer.parseInt(portAsString);
		} catch (Exception ex) {
			throw new Exception("Invalid port number: '" + portAsString + "' ", ex);
		}
		if ((port < 0) || (port > 65535)) {
			throw new IllegalArgumentException("Invalid port number, must be >0 and <65535'" + portAsString + "' ");
		}
		return port;
	}

}
