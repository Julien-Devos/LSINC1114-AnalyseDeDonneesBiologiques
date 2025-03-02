import be.uclouvain.DicomImage;
import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.commons.math3.linear.RealMatrix;

public class AppLauncher implements HttpHandler {
    private RealMatrix pixelData;
    
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
            case "/axios.min.js":
                HttpToolbox.serveStaticResource(exchange, "application/javascript", "/axios.min.js");
                break;
            case "/axios.min.map":
                HttpToolbox.serveStaticResource(exchange, "application/octet-stream", "/axios.min.map");
                break;
            case "/upload-source":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    byte[] body = HttpToolbox.getMultipartFile(exchange, "data");
                    synchronized (this) {
                        pixelData = DicomImage.createFromBytes(body).getFloatPixelData();
                    }
                    HttpToolbox.sendResponse(exchange, "text/plain", "");
                }
                break;
            case "/render-source":
                if (HttpToolbox.protectGetRequest(exchange)) {
                    synchronized (this) {
                        if (pixelData == null) {
                            HttpToolbox.sendInternalServerError(exchange);
                        } else {
                            DicomImage.sendImageToJavaScript(exchange, App.rangeNormalization(pixelData));
                        }
                    }
                }
                break;
            case "/render-sobel-x":
                if (HttpToolbox.protectGetRequest(exchange)) {
                    synchronized (this) {
                        if (pixelData == null) {
                            HttpToolbox.sendInternalServerError(exchange);
                        } else {
                            RealMatrix sobel = App.sobelX(pixelData);
                            DicomImage.sendImageToJavaScript(exchange, App.rangeNormalization(sobel));
                        }
                    }
                }
                break;
            case "/render-sobel-y":
                if (HttpToolbox.protectGetRequest(exchange)) {
                    synchronized (this) {
                        if (pixelData == null) {
                            HttpToolbox.sendInternalServerError(exchange);
                        } else {
                            RealMatrix sobel = App.sobelY(pixelData);
                            DicomImage.sendImageToJavaScript(exchange, App.rangeNormalization(sobel));
                        }
                    }
                }
                break;
            case "/render-sobel-magnitude":
                if (HttpToolbox.protectGetRequest(exchange)) {
                    synchronized (this) {
                        if (pixelData == null) {
                            HttpToolbox.sendInternalServerError(exchange);
                        } else {
                            RealMatrix sobel = App.sobelMagnitude(pixelData);
                            DicomImage.sendImageToJavaScript(exchange, App.rangeNormalization(sobel));
                        }
                    }
                }
                break;
            default:
                HttpToolbox.sendNotFound(exchange);
        }
    }
}
