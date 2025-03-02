import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import be.uclouvain.MockHttpExchange;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONObject;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    final String[] ELECTRICITY = {"voltage", "resistance", "current", "power"};

    private static void checkIOException(String uri, JSONObject request) throws URISyntaxException {
        try {
            MockHttpExchange.executePostAsJsonObject(new AppLauncher(), uri, request);
            fail();
        } catch (IOException e) {
        }
    }

    @Test
    @Grade(value = 1)
    public void testCelsius() throws IOException, URISyntaxException {
        JSONObject request = new JSONObject();
        request.put("celsius", 15.0f);
        JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/convert-celsius", request);
        assertEquals(59.0f, result.getFloat("fahrenheit"), 0.0001f);
        assertEquals(288.15, result.getFloat("kelvin"), 0.0001f);
    }

    @Test
    @Grade(value = 1)
    public void testBadCelsius() throws URISyntaxException {
        JSONObject request = new JSONObject();
        checkIOException("/convert-celsius", request);

        request.put("celsius", "invalid");
        checkIOException("/convert-celsius", request);
    }

    @Test
    @Grade(value = 1)
    public void testElectricity() throws IOException, URISyntaxException {
        {
            JSONObject request = new JSONObject();
            request.put("voltage", 12);
            request.put("resistance", 18);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(2.0f / 3.0f, result.getFloat("current"), 0.0001f);
            assertEquals(8.0f, result.getFloat("power"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("voltage", 12);
            request.put("resistance", 50);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(0.24f, result.getFloat("current"), 0.0001f);
            assertEquals(2.88f, result.getFloat("power"), 0.0001f);
        }

        {
            JSONObject request = new JSONObject();
            request.put("voltage", 30);
            request.put("resistance", 5);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(6.0f, result.getFloat("current"), 0.0001f);
            assertEquals(180.0f, result.getFloat("power"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("voltage", 30);
            request.put("current", 6);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(5.0f, result.getFloat("resistance"), 0.0001f);
            assertEquals(180.0f, result.getFloat("power"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("voltage", 30);
            request.put("power", 180);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(5.0f, result.getFloat("resistance"), 0.0001f);
            assertEquals(6.0f, result.getFloat("current"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("resistance", 5);
            request.put("current", 6);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(30.0f, result.getFloat("voltage"), 0.0001f);
            assertEquals(180.0f, result.getFloat("power"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("resistance", 5);
            request.put("power", 180);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(30.0f, result.getFloat("voltage"), 0.0001f);
            assertEquals(6.0f, result.getFloat("current"), 0.0001f);
        }
        {
            JSONObject request = new JSONObject();
            request.put("current", 6);
            request.put("power", 180);
            JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
            assertEquals(2, result.keySet().size());
            assertEquals(30.0f, result.getFloat("voltage"), 0.0001f);
            assertEquals(5.0f, result.getFloat("resistance"), 0.0001f);
        }

        for (int i = 0; i < ELECTRICITY.length; i++) {
            for (int j = i + 1; j < ELECTRICITY.length; j++) {
                JSONObject request = new JSONObject();
                request.put(ELECTRICITY[i], 7.0f);
                request.put(ELECTRICITY[j], 11.0f);
                JSONObject result = MockHttpExchange.executePostAsJsonObject(new AppLauncher(), "/compute-electricity", request);
                for (int k = 0; k < ELECTRICITY.length; k++) {
                    if (k != i && k != j) {
                        request.put(ELECTRICITY[k], result.getFloat(ELECTRICITY[k]));
                    }
                }
                assertEquals(request.getFloat("voltage"), request.getFloat("resistance") * request.getFloat("current"), 0.0001f);
                assertEquals(request.getFloat("power"), request.getFloat("current") * request.getFloat("voltage"), 0.0001f);
            }
        }
    }

    @Test
    @Grade(value = 1)
    public void testBadElectricity() throws URISyntaxException {
        {
            JSONObject request = new JSONObject();
            checkIOException("/compute-electricity", request);
        }

        {
            JSONObject request = new JSONObject();
            for (String s : ELECTRICITY) {
                request.put(s, 1);
            }
            checkIOException("/compute-electricity", request);
        }

        for (String s : ELECTRICITY) {
            JSONObject request = new JSONObject();
            request.put(s, 1);
            checkIOException("/compute-electricity", request);
        }

        for (String s : ELECTRICITY) {
            JSONObject request = new JSONObject();
            for (String t : ELECTRICITY) {
                request.put(t, 1);
            }
            request.remove(s);
            checkIOException("/compute-electricity", request);
        }

        for (int i = 0; i < ELECTRICITY.length; i++) {
            for (int j = i + 1; j < ELECTRICITY.length; j++) {
                {
                    JSONObject request = new JSONObject();
                    request.put(ELECTRICITY[i], "nope");
                    request.put(ELECTRICITY[j], 1);
                    checkIOException("/compute-electricity", request);
                }
                {
                    JSONObject request = new JSONObject();
                    request.put(ELECTRICITY[i], 1);
                    request.put(ELECTRICITY[j], "nope");
                    checkIOException("/compute-electricity", request);
                }
            }
        }
    }
}
