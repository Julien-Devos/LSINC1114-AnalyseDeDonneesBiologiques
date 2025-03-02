import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;
import be.uclouvain.Signal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class AppLauncher implements HttpHandler {
    private EDFTimeSeries timeSeries;
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new AppLauncher());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static JSONArray signalToJson(Signal signal,
                                          double samplingTime) {
        JSONArray jsonSignal = new JSONArray();
        for (int i = 0; i < signal.getLength(); i++) {
            JSONObject item = new JSONObject();
            item.put("x", (double) i * samplingTime);
            item.put("y", signal.getValue(i).getReal());
            jsonSignal.put(item);
        }

        return jsonSignal;
    }

    private static JSONArray channelToJson(EDFTimeSeries timeSeries,
                                           int channelIndex) throws IOException {
        final int numberOfSamples = timeSeries.getNumberOfSamples(channelIndex);
        final double samplingTime = 1.0 / (double) timeSeries.getSamplingFrequency(channelIndex);
        final EDFTimeSeries.Channel channel = timeSeries.getChannel(channelIndex);
        
        JSONArray jsonSignal = new JSONArray();
        for (int i = 0; i < numberOfSamples; i++) {
            JSONObject item = new JSONObject();
            item.put("x", (double) i * samplingTime);
            item.put("y", channel.getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)));
            jsonSignal.put(item);
        }

        return jsonSignal;
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
                            String label = timeSeries.getChannel(i).getLabel();
                            if (!label.equals("EDF Annotations")) {
                                channels.put(label, i);
                            }
                        }
                    }
                    
                    HttpToolbox.sendResponse(exchange, channels);
                }
                break;

            case "/filter":
                if (HttpToolbox.protectPostRequest(exchange)) {
                    try {
                        final JSONObject request = HttpToolbox.getRequestBodyAsJsonObject(exchange);
                        final int channelIndex = request.getInt("channel");
                        final boolean hasLowpass = request.getBoolean("hasLowpass");
                        final boolean hasHighpass = request.getBoolean("hasHighpass");
                        final double lowpassCutoff = request.getDouble("lowpass");
                        final double highpassCutoff = request.getDouble("highpass");

                        if (timeSeries == null ||
                            channelIndex >= timeSeries.getNumberOfChannels()) {
                            HttpToolbox.sendNotFound(exchange);
                        } else {
                            final double samplingTime = 1.0 / timeSeries.getSamplingFrequency(channelIndex);

                            Signal filtered;
                            
                            synchronized (this) {
                                filtered = App.filter(timeSeries, channelIndex, hasHighpass, highpassCutoff, hasLowpass, lowpassCutoff);
                            }
                            
                            JSONObject response = new JSONObject();
                            response.put("source", channelToJson(timeSeries, channelIndex));
                            response.put("filtered", signalToJson(filtered, samplingTime));
                            HttpToolbox.sendResponse(exchange, response);
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
