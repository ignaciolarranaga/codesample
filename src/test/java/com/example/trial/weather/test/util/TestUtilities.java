package com.example.trial.weather.test.util;

import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * This class contains several utilities used in the tests
 * @author ignaciolarranaga@gmail.com
 */
public class TestUtilities {

    // Sample data points
    /**
     * Sample WIND data point. Restrictions mean >= 0
     * @see DataPoint
     */
    public static final DataPoint WIND_SAMPLE_DATA_POINT = new DataPoint(10, 22, 30, 20, 1);

    /**
     * Sample PRESSURE data point. Restrictions mean >= 650 && mean < 800
     * @see DataPoint
     */
    public static final DataPoint PRESSURE_SAMPLE_DATA_POINT = new DataPoint(690, 695, 710, 700, 2);

    /**
     * Sample PRECIPITATION data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    public static final DataPoint PRECIPITATION_SAMPLE_DATA_POINT = new DataPoint(10, 50, 60, 55, 3);

    /**
     * Sample TEMPERATURE data point. Restrictions mean >= -50 && mean < 100
     * @see DataPoint
     */
    public static final DataPoint TEMPERATURE_SAMPLE_DATA_POINT = new DataPoint(-1, 15, 30, 20, 4);

    /**
     * Sample CLOUD COVER data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    public static final DataPoint CLOUD_COVER_SAMPLE_DATA_POINT = new DataPoint(10, 25, 30, 25, 5);

    /**
     * Sample HUMIDITY data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    public static final DataPoint HUMIDITY_SAMPLE_DATA_POINT = new DataPoint(10, 20, 30, 27, 6);

    /**
     * This method updates the lastUpdateTime of an object.
     * The last update time is produced on the server, so it is not know by
     * the caller.
     * @param ai The AtmosphericInformation to be updated
     * @param list The list of results to get the actual lastUpdateTime
     */
    public static void adjustLastUpdateTime(AtmosphericInformation ai,
        List<AtmosphericInformation> list) {
        for (AtmosphericInformation element : list) {
            if (element.equalsButLastUpdateTime(ai)) {
                ai.setLastUpdateTime(element.getLastUpdateTime());
            }
        }
    }

    /**
     * Validates that the expected radius are received for each position
     * @param expectedFrequencies A map between IATA codes and frequencies
     * @param result The result received for validation
     */
    public static void validateRadius(int[] expectedRadius, JsonArray result) {
        assertThat("The result has the expected size", result.size(),
            is(expectedRadius.length));
        
        for (int i = 0; i < expectedRadius.length; i++) {
            assertThat("The result radius are identical in each single position",
                result.get(i).getAsInt(), is(expectedRadius[i]));
        }
    }

    /**
     * Validates that the expected frequencies are received for each IATA code
     * specified.
     * @param expectedFrequencies A map between IATA codes and frequencies
     * @param result The result received for validation
     */
    public static void validateFrequencies(Map<String, Double> expectedFrequencies,
        JsonObject result) {
        assertThat("Validating that we receive all the expected frequencies",
            result.entrySet().size(), is(expectedFrequencies.keySet().size()));
        for (String iata : expectedFrequencies.keySet()) {
            assertThat("Validating that the expected frequency is received",
                result.get(iata).getAsDouble(), is(expectedFrequencies.get(iata)));
        }
    }

}
