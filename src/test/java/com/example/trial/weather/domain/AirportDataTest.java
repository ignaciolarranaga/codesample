package com.example.trial.weather.domain;

import com.example.trial.weather.domain.AirportData;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class AirportDataTest {

    private static final AirportData BOS_AIRPORT_DATA = new AirportData("BOS", 42.364347, -71.005181);
    private static final AirportData EWR_AIRPORT_DATA = new AirportData("EWR", 40.6925, -74.168667);
    private static final AirportData JFK_AIRPORT_DATA = new AirportData("JFK", 40.639751, -73.778925);

    @Test
    public void testDistanceBetweenBOSAndEWR() {
        // Checked at: http://andrew.hedges.name/experiments/haversine/
        // But the distance seems to be in miles not KMs, any way this does not
        // affect the results, it is just a matter of units
        assertThat("Validating the distance calculation between BOS and EWR",
            BOS_AIRPORT_DATA.distance(EWR_AIRPORT_DATA), is(200.87299713488193d));
    }

    @Test
    public void testDistanceBetweenEWRAndBOS() {
        assertThat("Validating the distance calculation between BOS and EWR",
            EWR_AIRPORT_DATA.distance(BOS_AIRPORT_DATA), is(200.87299713488193d));
    }

    @Test
    public void testDistanceBetweenEWRAndJFK() {
        assertThat("Validating the distance calculation between EWR and JFK",
            EWR_AIRPORT_DATA.distance(JFK_AIRPORT_DATA), is(43.07676410761436));
    }

}
