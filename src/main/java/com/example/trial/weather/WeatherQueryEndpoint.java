package com.example.trial.weather;

import javax.ws.rs.core.Response;

/**
 * The query only API for the Weather Server App.
 * This API is made available to the public Internet.
 * @author code test adminsitrator
 */
public interface WeatherQueryEndpoint {

    /**
     * Retrieve health and status information for the the query API.
     * Returns information about the number of data points currently held in
     * memory, and the frequency of requests for each IATA code and radius.
     *
     * The frequency of IATA requests is defined as the number of times this
     * element was requested (a call to #weather(String,String) method with this
     * IATA code) over the total number of requests.
     * Frequency[IATA_CODE] = #weather([IATA_CODE], ..) / #weather
     * Where:
     * - #weather means the amount of weather method calls
     * - #weather([IATA_CODE], ..) means the amount of weather method calls for
     * a particular IATA code
     *
     * Let's suppose there where 100 total calls. 10 of them for BOS and none
     * for EWS, then:
     * Frequency[BOS] = 0.1 = 10 / 100
     * Frequency[EWS] = 0.0 = 0  / 100
     *
     * The frequency of RADIUS requests is presented as an histogram of the
     * recorded method calls rounding the distance to the nearest integer
     * (it means 0.9 will be counted as 1km radius together with 1.0 calls)
     * below 1000 km.
     * It is delivered as an array where each position contains the
     * corresponding km calls information (starting from 0)
     * Frequency[RADIUS] = [#weather(.., 0), #weather(.., 1), #weather(.., 2)..]
     * - #weather(.., [RADIUS]) means the amount of weather method calls for
     * a particular radius
     *
     * Let's suppose there where 100 total calls. 50 of them for radius 0,
     * 20 for radius 1, 3 for radius 2, then the expected result histogram is:
     * Frequency[RADIUS] = [50, 20, 3, ...]
     *
     * Notes:
     * - All the never requested radius (i.e no call for this radius was made)
     * will be cero
     * - The histogram will be as large as the maximum radius requested, up to a
     * maximum of 1001 places (0 .. 1000km)
     * TODO: IMPORTANT: To be confirmed, it was deduced from the implementation
     * as the most logical thing it might be doing. 1000 was extracted from the
     * code as possible reasonable upper bound
     *
     * @return a JSON formatted dictionary with health information.
     */
    String ping();

    /**
     * Retrieve the most up to date atmospheric information from the given
     * airport and other airports in the given radius.
     * @param iata the three letter airport code
     * @param radius the radius, in km, from which to collect weather data
     * @return an HTTP Response and a list of
     * {@link com.example.trial.weather.domain.AtmosphericInformation}
     * from the requested airport and airports in the given radius
     */
    Response weather(String iata, String radius);

}
