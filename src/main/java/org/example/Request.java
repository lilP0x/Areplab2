package org.example;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final String body;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request(String method, String fullPath, String body) {
        this.method = method;
        this.body = body;

        try {
            URI uri = new URI(fullPath);
            this.path = uri.getPath();

            if (uri.getQuery() != null) {
                String[] params = uri.getQuery().split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(
                            URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing URL", e);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }
}
