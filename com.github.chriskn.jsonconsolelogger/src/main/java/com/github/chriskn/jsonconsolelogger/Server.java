package com.github.chriskn.jsonconsolelogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends Thread {

	final int port;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public Server(int port) {
		this.port = port;
	}

	/**
	 * Run server forever until break with Ctrl-C.
	 */
	@Override
	public void run() {
		while (true) {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port);
				while (!serverSocket.isClosed()) {
					while (true) {
						Socket socket = null;
						try {
							// logger.debug("Waiting for a new connection on
							// port " + serverSocket.getLocalPort());
							socket = serverSocket.accept();
							new SocketHandler(socket).run();
						} catch (Exception ex) {
							logger.error("Problem raised, will wait for new connection", ex);
						} finally {
							if (socket != null) {
								try {
									socket.close();
								} catch (IOException ex) {
									// ignore
								}
							}
						}
					}
				}
			} catch (IOException ex) {
				logger.error("Could not listen on port: " + port, ex);
			} finally {
				if (serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}
	}
}
