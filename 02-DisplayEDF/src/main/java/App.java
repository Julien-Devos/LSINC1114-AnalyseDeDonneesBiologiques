import com.sun.net.httpserver.HttpExchange;

import be.uclouvain.EDFTimeSeries;
import be.uclouvain.HttpToolbox;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF file to be used in the context of this project:
 * "eeglab_data.edf".
 **/
public class App {
    private EDFTimeSeries timeSeries;  // Current EDF file

    /**
     * This POST route in the REST API will discard the current EDF
     * file. It must answer with an empty text response.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/clear -d ''
     *
     **/
    public synchronized void postClear(HttpExchange exchange) throws IOException {

        timeSeries = null;

        HttpToolbox.sendResponse(exchange, "", "");

    }

    /**
     * This POST route in the REST API uploads and parses the EDF file
     * that is provided in the body of the request. The method must
     * answer with an empty text response. You can use
     * "HttpToolbox.getMultipartFile()" to retrieve the bytes of the
     * EDF file, its name will be "data" (cf. "app.js").
     *
     * Sample command-line session using the "curl" tool (where
     * "eeglab_data.edf" corresponds to some EDF file in the current
     * directory):
     *
     *   $ curl http://localhost:8000/upload -F data=@eeglab_data.edf
     *
     **/
    public synchronized void postUpload(HttpExchange exchange) throws IOException {


        byte[] edfFileBytes = HttpToolbox.getMultipartFile(exchange, "data");

        timeSeries = new EDFTimeSeries(edfFileBytes);

        HttpToolbox.sendResponse(exchange, "", "");

    }

    /**
     * This GET route in the REST API returns a JSON dictionary that
     * maps the labels of the channels/electrodes to their index in
     * the current EDF file.
     *
     * If no EDF file is currently uploaded, the method must answer
     * with a 404 "Not Found" HTTP status.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/channels
     *   {
     *     "O1": 29,
     *     "O2": 31,
     *     [...]
     *     "FPz": 0,
     *     "Fz": 3
     *   }
     *
     **/
    public synchronized void getChannels(HttpExchange exchange) throws IOException {

        if (timeSeries == null) {
            HttpToolbox.sendNotFound(exchange);
            return;
        }

        JSONObject response = new JSONObject();

        for (int i=0; i<timeSeries.getNumberOfChannels(); i++) {

            response.put(timeSeries.getChannel(i).getLabel(), i);
        }

        HttpToolbox.sendResponse(exchange, response);
    }

    /**
     * This GET route in the REST API returns all the samples that are
     * stored in one channel/electrode of interest, in the current EDF
     * file. The channel of interest is contained in the GET argument
     * "channel" provided in the URI (to help you, in this exercise,
     * the GET arguments of the URI are already parsed in the
     * "arguments" parameter of the method).
     *
     * The function must answer with a JSON array, each element of it
     * being a JSON dictionary with two fields: "x" indicates the
     * timecode of the sample (expressed in seconds), and "y"
     * indicates the value of the sample (expressed in physical
     * values).
     *
     * If the "channel" GET argument is absent or incorrectly
     * formatted (i.e. not an integer), the method must answer with a
     * 400 "Bad Request" HTTP status.
     *
     * If no EDF file is currently uploaded, or if the index "channel"
     * is non-existent in the currently uploaded EDF file, the method
     * must answer with a 404 "Not Found" HTTP status.
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/samples?channel=25
     *   [
     *     {
     *       "x": 0,
     *       "y": -13.808136
     *     },
     *     {
     *       "x": 0.0078125,
     *       "y": -4.907959
     *     },
     *     [...]
     *     {
     *       "x": 238.99219,
     *       "y": 0.012023926
     *     }
     *   ]
     *
     **/
    public synchronized void getSamples(HttpExchange exchange,
                                        Map<String, String> arguments) throws IOException {

        int channelIndex;

        try {
            channelIndex = Integer.parseInt(arguments.get("channel"));
        } catch (NumberFormatException e) {
            HttpToolbox.sendBadRequest(exchange);
            return;
        }

        if (timeSeries == null || ! (arguments.containsKey("channel")) || channelIndex >
                timeSeries.getNumberOfChannels() -1 ) {
            HttpToolbox.sendNotFound(exchange);
            return;
        }

        EDFTimeSeries.Channel currentChannel = timeSeries.getChannel(channelIndex);


        JSONArray array = new JSONArray();


        System.out.println("max: "+timeSeries.getNumberOfSamples(channelIndex));
        for (int i=0; i<timeSeries.getNumberOfSamples(channelIndex); i++) {
            JSONObject dictionary = new JSONObject();

            dictionary.put("x", i / timeSeries.getSamplingFrequency(channelIndex));

            dictionary.put("y", currentChannel.getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)));

            array.put(dictionary);
        }

        HttpToolbox.sendResponse(exchange, array);
    }
}
