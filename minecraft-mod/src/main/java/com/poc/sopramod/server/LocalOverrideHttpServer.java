package com.poc.sopramod.server;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.EventRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class LocalOverrideHttpServer {
    private HttpServer httpServer;

    public void start() {
        if (httpServer != null) {
            return;
        }
        try {
            httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 3010), 0);
            httpServer.createContext("/force-event", new ForceEventHandler());
            httpServer.createContext("/events", new EventsHandler());
            httpServer.createContext("/health", exchange -> writeJson(exchange, 200, "{\"ok\":true}"));
            httpServer.setExecutor(Executors.newSingleThreadExecutor());
            httpServer.start();
            Sopramod.LOGGER.info("Local override HTTP server started on http://127.0.0.1:3010");
        } catch (IOException ex) {
            Sopramod.LOGGER.error("Failed to start local override HTTP server", ex);
        }
    }

    private static final class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
                return;
            }

            StringBuilder json = new StringBuilder("{\"events\":[");
            boolean first = true;
            for (var holder : EventRegistry.EVENTS.listElements().toList()) {
                String id = EventRegistry.getEventId(holder.value()).identifier().getPath();
                if (!first) {
                    json.append(',');
                }
                json.append('"').append(id).append('"');
                first = false;
            }
            json.append("]}");
            writeJson(exchange, 200, json.toString());
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
            Sopramod.LOGGER.info("Local override HTTP server stopped.");
        }
    }

    private static final class ForceEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
                return;
            }

            URI uri = exchange.getRequestURI();
            Map<String, String> query = parseQuery(uri.getRawQuery());
            String event = query.getOrDefault("event", "").trim();
            String by = query.getOrDefault("by", "killer").trim();
            if (by.isBlank()) {
                by = "killer";
            }
            final String triggeredBy = by;
            if (event.isBlank()) {
                writeJson(exchange, 400, "{\"ok\":false,\"error\":\"missing_event\"}");
                return;
            }

            if (Sopramod.getInstance().eventHandler == null) {
                writeJson(exchange, 409, "{\"ok\":false,\"error\":\"event_handler_not_ready\"}");
                return;
            }

            try {
                CompletableFuture<ServerEventHandler.ForcedEventResult> resultFuture = new CompletableFuture<>();
                Sopramod.getInstance().eventHandler.server.execute(() ->
                    resultFuture.complete(Sopramod.getInstance().eventHandler.forceEventNow(event, triggeredBy))
                );
                ServerEventHandler.ForcedEventResult result = resultFuture.get(3, TimeUnit.SECONDS);

                if (result.ok()) {
                    writeJson(exchange, 200, "{\"ok\":true,\"event\":\"" + escapeJson(result.eventId()) + "\",\"label\":\"" + escapeJson(result.label()) + "\"}");
                } else {
                    writeJson(exchange, 422, "{\"ok\":false,\"error\":\"" + escapeJson(result.code()) + "\",\"event\":\"" + escapeJson(result.eventId()) + "\"}");
                }
            } catch (Exception ex) {
                writeJson(exchange, 500, "{\"ok\":false,\"error\":\"force_event_failed\",\"details\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }

        private static Map<String, String> parseQuery(String rawQuery) {
            Map<String, String> result = new HashMap<>();
            if (rawQuery == null || rawQuery.isBlank()) {
                return result;
            }
            String[] pairs = rawQuery.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, value);
            }
            return result;
        }
    }

    private static void writeJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
