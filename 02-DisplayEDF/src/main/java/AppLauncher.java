import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class AppLauncher implements HttpHandler {
    private final App app = new App();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new AppLauncher());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().normalize().toString();

        switch (uri) {
            case "/":
                HttpToolbox.sendRedirection(exchange, "index.html");
                break;
            case "/index.html":
                System.out.println("Page loaded");
                HttpToolbox.serveStaticResource(exchange, "text/html", "/index.html");
                break;
            case "/app.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/app.js");
                break;
            case "/BestRendering.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/BestRendering.js");
                break;
            case "/chart.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/chart.js");
                break;
            case "/axios.min.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/axios.min.js");
                break;
            case "/axios.min.map":
                HttpToolbox.serveStaticResource(exchange, "application/octet-stream", "/axios.min.map");
                break;
            case "/hammer.min.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/hammer.min.js");
                break;
            case "/hammer.min.js.map":
                HttpToolbox.serveStaticResource(exchange, "application/octet-stream", "/hammer.min.map");
                break;
            case "/chartjs-plugin-zoom.min.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/chartjs-plugin-zoom.min.js");
                break;

            case "/clear":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    app.postClear(exchange);
                }
                break;
            case "/upload":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    app.postUpload(exchange);
                }
                break;
            case "/channels":
                if (HttpToolbox.protectGetRequest(exchange)) {
                    app.getChannels(exchange);
                }
                break;
            default:
                if (uri.startsWith("/samples")) {
                    if (HttpToolbox.protectGetRequest(exchange)) {
                        Map<String, String> arguments = HttpToolbox.parseGetArguments(uri);
                        app.getSamples(exchange, arguments);
                    }
                } else {
                    HttpToolbox.sendNotFound(exchange);
                }
                break;
        }
    }
}
