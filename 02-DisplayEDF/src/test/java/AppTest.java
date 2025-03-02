import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import be.uclouvain.HttpToolbox;
import be.uclouvain.MockHttpExchange;

import java.io.IOException;
import java.net.URISyntaxException;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    @Test
    @Grade(value = 1)
    public void testEmpty() throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/BestRendering.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/app.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/axios.min.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/axios.min.map"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/chart.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/chartjs-plugin-zoom.min.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/hammer.min.js"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/hammer.min.js.map"));
        assertEquals(200, MockHttpExchange.executeGetAsStatusCode(app, "/index.html"));
        assertEquals(302, MockHttpExchange.executeGetAsStatusCode(app, "/"));
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/nope"));
        assertEquals(405, MockHttpExchange.executeGetAsStatusCode(app, "/clear"));
        assertEquals(405, MockHttpExchange.executeGetAsStatusCode(app, "/upload"));

        assertEquals(400, MockHttpExchange.executeGetAsStatusCode(app, "/samples"));
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/channels"));
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/samples?channel=10"));
    }

    @Test
    @Grade(value = 1)
    public void testUpload() throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();

        assertEquals(0, MockHttpExchange.executePostAsBytes(app, "/clear", new byte[0]).length);
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/channels"));

        final byte[] edf = HttpToolbox.readResource("/eeglab_data.edf");
        assertEquals(0, MockHttpExchange.executeMultipartUploadAsBytes(app, "/upload", "data", edf).length);

        JSONObject channels = MockHttpExchange.executeGetAsJsonObject(app, "/channels");
        assertEquals(33, channels.length());

        assertEquals(0, MockHttpExchange.executeMultipartUploadAsBytes(app, "/upload", "data", edf).length);
        channels = MockHttpExchange.executeGetAsJsonObject(app, "/channels");
        assertEquals(33, channels.length());

        assertEquals(0, MockHttpExchange.executePostAsBytes(app, "/clear", new byte[0]).length);
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/channels"));
    }
    
    @Test
    @Grade(value = 1)
    public void testSamples() throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();
        
        final byte[] edf = HttpToolbox.readResource("/eeglab_data.edf");
        MockHttpExchange.executeMultipartUploadAsBytes(app, "/upload", "data", edf);

        JSONObject channels = MockHttpExchange.executeGetAsJsonObject(app, "/channels");
        assertEquals(33, channels.length());
        assertEquals(0, channels.getInt("FPz"));
        assertEquals(1, channels.getInt("EOG1"));
        assertEquals(30, channels.getInt("Oz"));
        assertEquals(31, channels.getInt("O2"));
        assertEquals(32, channels.getInt("EDF Annotations"));

        assertEquals(400, MockHttpExchange.executeGetAsStatusCode(app, "/samples?channel=nope"));
        assertEquals(404, MockHttpExchange.executeGetAsStatusCode(app, "/samples?channel=33"));

        {
            JSONArray f3 = new JSONArray(MockHttpExchange.executeGetAsString(app, "/samples?channel=2"));
            assertEquals(30592, f3.length());
            assertEquals(0, f3.getJSONObject(0).getDouble("x"), 0.00001);
            assertEquals(-26.771448, f3.getJSONObject(0).getDouble("y"), 0.00001);
            assertEquals(238.9921875, f3.getJSONObject(30591).getDouble("x"), 0.00001);
            assertEquals(0.012035, f3.getJSONObject(30591).getDouble("y"), 0.0001);
        }

        {
            JSONArray o2 = new JSONArray(MockHttpExchange.executeGetAsString(app, "/samples?channel=31"));
            assertEquals(30592, o2.length());
            assertEquals(0, o2.getJSONObject(0).getDouble("x"), 0.00001);
            assertEquals(-9.496239, o2.getJSONObject(0).getDouble("y"), 0.00001);
            assertEquals(238.9921875, o2.getJSONObject(30591).getDouble("x"), 0.00001);
            assertEquals(0.012035, o2.getJSONObject(30591).getDouble("y"), 0.0001);
        }    
    }
}
