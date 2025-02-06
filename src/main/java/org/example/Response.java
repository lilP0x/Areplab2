package org.example;

import java.io.OutputStream;

public class Response {
    private final OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void send(String message) {
        try {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + message.length() + "\r\n\r\n" +
                    message;
            output.write(response.getBytes());
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
