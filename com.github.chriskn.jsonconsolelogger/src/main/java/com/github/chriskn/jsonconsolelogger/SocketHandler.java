package com.github.chriskn.jsonconsolelogger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SocketHandler implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());
    private final Socket socket;
    private SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy HH:mm:ss:SSS");

    public SocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream inputStream = null;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                int length = inputStream.readInt();
                byte[] data = new byte[length];
                inputStream.read(data);
                String dataString = new String(data, "UTF-8");
                print(new JSONObject(dataString));
            }
        } catch (EOFException e) {
            // stream ended, all good
        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    socket.close();
                } catch (IOException e) {
                    logger.error("Error while closing socket", e);
                }
            }
        }
    }

    private void print(JSONObject event) {
        String timestamp = date.format(new Date(event.getLong("time")));
        String severity = event.getString("severity");
        String message = event.getString("message");
        String line = "NA"; 
        if (event.has("exception")) {
            JSONObject exception = event.getJSONObject("exception"); 
            if (exception.has("stacktrace") && exception.getJSONArray("stacktrace").length() > 0){
                JSONArray stack = exception.getJSONArray("stacktrace"); 
                line = ((JSONObject)stack.get(0)).getString("line");
                printEvent(timestamp, severity, message, line);
                System.out.println(stacktraceToString(stack));
                return; 
            }
        }         
        printEvent(timestamp, severity, message, line);
    }

    private void printEvent(String timestamp, String severity, String message, String line) {
        System.out.printf("%s\t%s\t%s:\t%s\n", timestamp, line, severity, message);
    }
    
    private String stacktraceToString(JSONArray stack){
        StringBuilder builder = new StringBuilder(); 
        int lentgh = stack.length(); 
        for (int i = 0; i < lentgh; i++) {
            JSONObject jsonStacktraceElement = (JSONObject) stack.get(i);
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

}