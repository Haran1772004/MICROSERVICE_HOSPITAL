package com.hospital.auth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A tiny fake of hospital-service for tests. It records every request and answers with a
 * status and body that the test can set.
 */
final class FakeHospitalService {

    /** One request received by the fake. */
    record Call(String method, String path, String secret, String body) {
    }

    private final HttpServer server;
    private final List<Call> calls = new CopyOnWriteArrayList<>();
    private volatile int responseStatus = 201;
    private volatile String responseBody = "";

    FakeHospitalService() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            calls.add(new Call(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("X-Internal-Secret"), body));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
            exchange.close();
        });
        server.start();
    }

    int port() {
        return server.getAddress().getPort();
    }

    List<Call> calls() {
        return calls;
    }

    void reset() {
        calls.clear();
        responseStatus = 201;
        responseBody = "";
    }

    void answerWith(int status, String body) {
        this.responseStatus = status;
        this.responseBody = body;
    }

    void stop() {
        server.stop(0);
    }
}
