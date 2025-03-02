import be.uclouvain.HttpToolbox;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 **/
public class App {
    
    /**
     * This method takes as argument a POST request to the REST API,
     * whose body contains a JSON object formatted as follows:
     *
     *   {
     *      "celsius": 20
     *   }
     *
     * The method must convert the degrees Celsius both to degrees
     * Fahrenheit and to Kelvins, and must send its response with a
     * body that contains a JSON object formatted as follows:
     *
     *   {
     *      "fahrenheit": 68,
     *      "kelvin": 293.15
     *   }
     *
     * All the values are floating-point numbers. An "IOException"
     * exception must be thrown in the case of a badly formatted
     * request.
     *
     * Make sure to use the global helper functions that are provided
     * by the "be.uclouvain.HttpToolbox" class!
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/convert-celsius -d '{"celsius":41}'
     *   {
     *     "kelvin": 314.15,
     *     "fahrenheit": 105.8
     *   }
     *
     * @param exchange The context of this REST API call.
     **/
    public static void convertCelsius(HttpExchange exchange) throws IOException {
        try {

            JSONObject postRequest = HttpToolbox.getRequestBodyAsJsonObject(exchange);

            float celsius;

            try {
                celsius = Float.parseFloat(postRequest.get("celsius").toString());
            } catch (NumberFormatException e) {
                throw new IOException();
            }

            float fahrenheit = (celsius * 9/5) + 32;

            double kelvin = celsius + 273.15;

            JSONObject response = new JSONObject();
            response.put("fahrenheit", fahrenheit);
            response.put("kelvin", kelvin);

            HttpToolbox.sendResponse(exchange, response);

        } catch (JSONException e) {
            throw new IOException();
        }
    }


    /**
     * This method takes as argument a POST request to the REST API,
     * whose body contains a JSON object (dictionary) containing
     * exactly two among the following fields: "voltage",
     * "resistance", "current", and "power". Here is such a valid
     * body:
     *
     *   {
     *      "current": 6,
     *      "resistance": 5
     *   }
     *
     * The method must compute the two missing values using the
     * "V=R*I" (Ohm's law) and "P=I*V" (electric power dissipated in
     * resistive circuits), and must send these two missing values
     * as a JSON object. Here is the body of the response for the
     * example above:
     *
     *   {
     *      "power": 100,
     *      "voltage": 30
     *   }
     *
     * All the values are floating-point numbers. An "IOException"
     * exception must be thrown in the case of a badly formatted
     * request.
     *
     * Make sure to use the global helper functions that are provided
     * by the "be.uclouvain.HttpToolbox" class!
     *
     * Sample command-line session using the "curl" tool:
     *
     *   $ curl http://localhost:8000/compute-electricity -d '{"power":50,"voltage":40}'
     *   {
     *     "current": 1.25,
     *     "resistance": 32
     *   }
     *
     * @param exchange The context of this REST API call.
     **/
    public static void computeElectricity(HttpExchange exchange) throws IOException {
        try {

            JSONObject postRequest = HttpToolbox.getRequestBodyAsJsonObject(exchange);

            float voltage = 0;
            float resistance = 0;
            float current = 0;
            float power = 0;

            Iterator<String> keys = postRequest.keys();

            if (postRequest.length() != 2) {
                throw new IOException();
            }

            while(keys.hasNext()) {
                String key = keys.next();
                switch (key) {

                    case "voltage":
                        try {
                            voltage = Float.parseFloat(postRequest.get(key).toString());
                        } catch (NumberFormatException e) {
                            throw new IOException();
                        }
                        break;

                    case "resistance":
                        try {
                            resistance = Float.parseFloat(postRequest.get(key).toString());
                        } catch (NumberFormatException e) {
                            throw new IOException();
                        }
                        break;

                    case "current":
                        try {
                            current = Float.parseFloat(postRequest.get(key).toString());
                        } catch (NumberFormatException e) {
                            throw new IOException();
                        }
                        break;

                    case "power":
                        try {
                            power = Float.parseFloat(postRequest.get(key).toString());
                        } catch (NumberFormatException e) {
                            throw new IOException();
                        }
                        break;

                }
            }

            JSONObject response = new JSONObject();

            if (voltage == 0) {

                voltage = getVoltage(resistance, current, power);

                response.put("voltage", voltage);

            } if (resistance == 0) {

                resistance = getResistance(voltage, current, power);

                response.put("resistance", resistance);

            } if (current == 0) {

                current = getCurrent(voltage, resistance, power);

                response.put("current", current);

            } if (power == 0) {

                power = getPower(current, voltage);

                response.put("power", power);

            }

            HttpToolbox.sendResponse(exchange, response);

        } catch (JSONException e) {
            throw new IOException();
        }
    }

    private static float getVoltage(float resistance, float current, float power) {

        if (resistance != 0 && current != 0) {
            return resistance * current;
        } else if (current != 0 && power != 0){
            return power / current;
        }  else {
            return (float) Math.sqrt(power * resistance);
        }
    }

    private static float getResistance(float voltage, float current, float power) {

        if (current == 0) {
            current = getCurrent(voltage, 0, power);
        }

        return voltage / current;

    }

    private static float getCurrent(float voltage, float resistance, float power) {

        if (voltage != 0 && resistance != 0) {
            return voltage / resistance;
        } else if (voltage != 0){
            return power / voltage;
        } else if (resistance != 0) {
            return (float) Math.sqrt(power / resistance);
        }

        return 0;

    }

    private static float getPower(float current, float voltage) {

        return current * voltage;

    }

}
