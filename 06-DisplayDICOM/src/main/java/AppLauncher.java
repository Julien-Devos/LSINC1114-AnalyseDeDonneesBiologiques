import be.uclouvain.DicomImage;
import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class AppLauncher implements HttpHandler {
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
                HttpToolbox.serveStaticResource(exchange, "text/html", "/index.html");
                break;
            case "/app.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/app.js");
                break;
            case "/BestRendering.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/BestRendering.js");
                break;
            case "/render-dicom":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    byte[] body = HttpToolbox.getMultipartFile(exchange, "data");
                    App.renderDicom(exchange, DicomImage.createFromBytes(body));
                }
                break;
            case "/parse-tags":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    byte[] body = HttpToolbox.getMultipartFile(exchange, "data");
                    App.parseTags(exchange, DicomImage.createFromBytes(body));
                }
                break;
            default:
                HttpToolbox.sendNotFound(exchange);
        }
    }
}
