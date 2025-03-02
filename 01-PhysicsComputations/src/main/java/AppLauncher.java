import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class AppLauncher implements HttpHandler {
    public static void main(String[] args) throws IOException {
        AppLauncher launcher = new AppLauncher();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", launcher);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().normalize().toString();

        switch (uri) {
            case "/":
                HttpToolbox.sendRedirection(exchange, "index.html");
                return;

            case "/index.html":
                HttpToolbox.serveStaticResource(exchange, "text/html", "/index.html");
                System.out.println("page loaded");
                return;

            case "/app.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/app.js");
                return;

            case "/axios.min.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/axios.min.js");
                return;

            case "/axios.min.map":
                HttpToolbox.serveStaticResource(exchange, "application/octet-stream", "/axios.min.map");
                return;

            case "/convert-celsius":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    App.convertCelsius(exchange);
                }
                return;

            case "/compute-electricity":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    App.computeElectricity(exchange);
                }
                return;

            default:
                HttpToolbox.sendNotFound(exchange);
        }
    }
}
