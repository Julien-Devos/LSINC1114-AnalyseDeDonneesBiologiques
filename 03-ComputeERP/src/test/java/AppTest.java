import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import be.uclouvain.HttpToolbox;
import be.uclouvain.MockHttpExchange;
import be.uclouvain.EDFTimeSeries;

import java.io.IOException;
import java.net.URISyntaxException;

@Grade
@Allow("all")  // Allows the use of "java.lang.Thread" and "java.lang.ClassLoader" for dcm4che/HttpToolbox
public class AppTest {
    @Test
    @Grade(value = 1)
    public void testNotEpoched() throws IOException, URISyntaxException {
        AppLauncher app = new AppLauncher();
        
        final byte[] edf = HttpToolbox.readResource("/n170_erp.edf");
        assertEquals(400, MockHttpExchange.executeMultipartUploadAsStatusCode(app, "/compute-erp", "data", edf));
    }

    @Test
    @Grade(value = 3)
    public void testErp() throws IOException, URISyntaxException {
        final AppLauncher app = new AppLauncher();
        
        final byte[] epochsFile = HttpToolbox.readResource("/n170_epochs.edf");
        final JSONObject computedErp = HttpToolbox.parseJsonObject(
            MockHttpExchange.executeMultipartUploadAsBytes(app, "/compute-erp", "data", epochsFile));

        final EDFTimeSeries expectedErp = new EDFTimeSeries(HttpToolbox.readResource("/n170_erp.edf"));
        
        assertEquals(36, expectedErp.getNumberOfChannels());
        assertEquals(36, computedErp.length());

        for (int i = 0; i < expectedErp.getNumberOfChannels(); i++) {
            final String label = expectedErp.getChannel(i).getLabel();
            final JSONArray a = computedErp.getJSONArray(label);
            assertEquals(expectedErp.getNumberOfSamples(i), a.length());

            final EDFTimeSeries.Channel channel = expectedErp.getChannel(i);
            final float sampleDuration = 1.0f / expectedErp.getSamplingFrequency(i);

            for (int j = 0; j < a.length(); j++) {
                System.out.println("j: "+j);
                final int digitalValue = expectedErp.getDigitalValue(i, j);
                final float physicalValue = channel.getPhysicalValue(digitalValue);

                float threshold = 0.01f;
                if (label.equals("EDF Annotations")) {
                    threshold = 0.1f;
                }
                    
                assertEquals(sampleDuration * (float) j, a.getJSONObject(j).getFloat("x"), 0.0001f);
                assertEquals(physicalValue, a.getJSONObject(j).getFloat("y"), threshold);
            }
        }
    }
}
