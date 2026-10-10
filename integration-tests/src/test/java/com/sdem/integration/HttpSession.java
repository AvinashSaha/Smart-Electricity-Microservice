package com.sdem.integration;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** A tiny browser: keeps cookies, does not follow redirects, and fills in CSRF tokens. */
final class HttpSession {

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    private final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final String baseUrl;

    HttpSession(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    HttpResponse<String> get(String path) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build());
    }

    HttpResponse<String> postJson(String path, String json) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build());
    }

    /** Submits a form found on {@code pagePath}, adding that page's CSRF token. */
    HttpResponse<String> submitForm(String pagePath, String actionPath, Map<String, String> fields) {
        String page = get(pagePath).body();
        Matcher m = CSRF.matcher(page);
        if (!m.find()) {
            throw new IllegalStateException("No CSRF token on " + pagePath);
        }
        String body = fields.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&")) + "&_csrf=" + enc(m.group(1));
        return send(HttpRequest.newBuilder(URI.create(baseUrl + actionPath))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    HttpResponse<String> login(String username, String password) {
        return submitForm("/login", "/login", Map.of("username", username, "password", password));
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
