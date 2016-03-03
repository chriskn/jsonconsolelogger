package com.github.chriskn.jsonconsolelogger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Socket socket;
	private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");

	public SocketHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		DataInputStream inputStream = null;
		String jsonDataAsString = null;
		try {
			inputStream = new DataInputStream(socket.getInputStream());
			int length = inputStream.readInt();
			jsonDataAsString = slurp(inputStream, length);
			// logger.debug("dataString" + dataString);
			final JSONObject json = new JSONObject(jsonDataAsString);
			printConsoleMessage(json);
		} catch (EOFException ex) {
			// stream ended, all good
		} catch (JSONException ex) {
			logger.error("Error while processing json data:\n" + jsonDataAsString);
		} catch (Exception ex) {
			logger.error("Error while processing data:\n" + jsonDataAsString, ex);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Error while closing inpuStream", e);
				}
				try {
					socket.close();
				} catch (IOException e) {
					logger.error("Error while closing socket", e);
				}
			}
		}
	}

	private String slurp(final InputStream is, final int bufferSize) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(is, "UTF-8")) {
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		} catch (UnsupportedEncodingException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out.toString();
	}

	private void printConsoleMessage(final JSONObject event) {
		final String timestamp = date.format(new Date(event.getLong("time")));
		final String severity = event.getString("severity");
		final String message = event.getString("message");
		String line = "NA";
		dumpToConsole("%s\t%s\t%s:\t%s\n", timestamp, line, severity, message);
		if (event.has("exception")) {
			JSONObject exception = event.getJSONObject("exception");
			if (exception.has("stacktrace") && exception.getJSONArray("stacktrace").length() > 0) {
				JSONArray stack = exception.getJSONArray("stacktrace");
				line = ((JSONObject) stack.get(0)).getString("line");
				dumpToConsole(stacktraceToString(stack));
			}
		}
	}

	private String stacktraceToString(final JSONArray stack) {
		final StringBuilder builder = new StringBuilder();
		final int length = stack.length();
		for (int i = 0; i < length; i++) {
			final JSONObject jsonStacktraceElement = (JSONObject) stack.get(i);
			builder.append("\tat ");
			builder.append(jsonStacktraceElement.getString("class"));
			builder.append(".");
			builder.append(jsonStacktraceElement.getString("method"));
			builder.append("(");
			builder.append(jsonStacktraceElement.getString("file"));
			builder.append(":");
			builder.append(jsonStacktraceElement.getString("line"));
			builder.append(")");
			builder.append("\n");
		}
		return builder.toString();
	}

	private void dumpToConsole(final String msg) {
		System.out.println(msg);
	}

	private void dumpToConsole(final String pattern, final Object... args) {
		System.out.printf(pattern, args);
	}

}