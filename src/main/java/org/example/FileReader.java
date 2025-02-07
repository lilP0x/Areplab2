package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class FileReader {
    private static String staticFolder = "src/main/resources";  

    private static final Map<String, BiConsumer<Request, Response>> getRoutes = new HashMap<>();
    private static final Map<String, BiConsumer<Request, Response>> postRoutes = new HashMap<>();

    public FileReader() {
        getRoutes.put("/hello", (req, res) -> {
            String name = req.getQueryParam("name");
            if (name == null) {
                name = "World";
            }
            res.send("Hello, " + name + "!");
        });
    }

    public static void staticfiles(String path) {
        staticFolder = path;
    }

    public void handleRequest(ServerSocket socket, Socket clientSocket) throws Exception {
        OutputStream out = clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        boolean isFirstLine = true;
        String fullPath = "";
        String method = "";
        StringBuilder requestBody = new StringBuilder();
        boolean isPost = false;

        while ((inputLine = in.readLine()) != null) {
            if (isFirstLine) {
                String[] requestParts = inputLine.split(" ");
                method = requestParts[0];
                fullPath = requestParts[1];
                isFirstLine = false;

                if (method.equals("POST")) {
                    isPost = true;
                }
            }

            if (isPost && inputLine.isEmpty()) {
                while (in.ready()) {
                    requestBody.append((char) in.read());
                }
                break;
            }

            System.out.println("Received: " + inputLine);
            if (!in.ready()) {
                break;
            }
        }

        Request request = new Request(method, fullPath, requestBody.toString());
        Response response = new Response(out);

        String path = request.getPath();
        if (method.equals("GET") && getRoutes.containsKey(path)) {
            getRoutes.get(path).accept(request, response);
        } else if (method.equals("POST") && postRoutes.containsKey(path)) {
            postRoutes.get(path).accept(request, response);
        } else {
            serveFile(path.substring(1), out);
        }

        out.close();
        in.close();
        clientSocket.close();
    }

    private static void serveFile(String filePath, OutputStream output) throws IOException {
        Path file = Paths.get(staticFolder, filePath);
        boolean isError = false;

        if (!Files.exists(file) || Files.isDirectory(file)) {
            file = Paths.get(staticFolder, "400badrequest.html");
            isError = true;
        }

        byte[] fileBytes = Files.readAllBytes(file);
        String contentType = getContentType(filePath);

        PrintWriter writer = new PrintWriter(output, true);
        if (isError) {
            writer.println("HTTP/1.1 400 Bad Request");
            contentType = "text/html";
        } else {
            writer.println("HTTP/1.1 200 OK");
        }

        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + fileBytes.length);
        writer.println();
        output.write(fileBytes);
        output.flush();
    }

    private static String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        return "application/octet-stream";
    }
}
