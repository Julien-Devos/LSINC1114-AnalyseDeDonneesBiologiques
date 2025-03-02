import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.json.JSONException;
import org.json.JSONObject;

public class AppLauncher implements HttpHandler {
    private EDFTimeSeries timeSeries;
    
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

            case "/upload":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    JSONObject channels = new JSONObject();
                    
                    synchronized(this) {
                        byte[] body = HttpToolbox.getMultipartFile(exchange, "data");
                        timeSeries = new EDFTimeSeries(body);

                        for (int i = 0; i < timeSeries.getNumberOfChannels(); i++) {
                            channels.put(timeSeries.getChannel(i).getLabel(), i);
                        }
                    }
                    
                    HttpToolbox.sendResponse(exchange, channels);
                }
                break;

            case "/compute-power-spectrum":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    try {
                        JSONObject request = HttpToolbox.getRequestBodyAsJsonObject(exchange);
                        int channelIndex = request.getInt("channel");

                        synchronized (this) {
                            App.computePowerSpectrum(exchange, timeSeries, channelIndex);
                        }
                    } catch (JSONException | IOException e) {
                        HttpToolbox.sendBadRequest(exchange);
                    }
                }
                break;
                
            default:
                HttpToolbox.sendNotFound(exchange);
                break;
        }
    }
}
