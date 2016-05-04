package com.example.trial.weather.repository;

import java.util.Map;

/**
 * This class represents the repository usage statistics.
 * This object was specifically designed to match the statistics requirements
 * of the WeatherQueryEndpoint#ping() method.
 * This a Data Transfer Object (DTO)
 * @see com.example.trial.weather.WeatherQueryEndpoint
 * @author ignaciolarranaga@gmail.com
 */
public class RepositoryUsageStatistics {

    /**
     * The number of data points currently held in memory.
     */
    private final int dataPointCount;

    /**
     * The frequency of IATA requests is defined as the number of times this
     * element was requested (a call to #weather(String,String) method with this
     * IATA code) over the total number of requests.
     * Frequency[IATA_CODE] = #weather([IATA_CODE], ..) / #weather
     *
     * Where:
     * - #weather means the amount of weather method calls
     * - #weather([IATA_CODE], ..) means the amount of weather method calls for
     * a particular IATA code
     *
     * Let's suppose there where 100 total calls. 10 of them for BOS and none
     * for EWS, then:
     * Frequency[BOS] = 0.1 = 10 / 100
     * Frequency[EWS] = 0.0 = 0  / 100
     */
    private final Map<String,Double> iataCallFrecuencies;

    /**
     * The RADIUS requests histogram is an histogram of the recorded method
     * calls rounding the distance to the nearest integer (it means 0.9 will be
     * counted as 1km radius together with 1.0 calls) below 1000 km.
     *
     * It is modeled as an array where each position contains the
     * corresponding km calls information (starting from 0)
     * Frequency[RADIUS] = [#weather(.., 0), #weather(.., 1), #weather(.., 2)..]
     * - #weather(.., [RADIUS]) means the amount of weather method calls for
     * a particular radius
     *
     * Let's suppose there where 100 total calls. 50 of them for radius 0,
     * 20 for radius 1, 3 for radius 2, then the expected result histogram is:
     * Frequency[RADIUS] = [50, 20, 3, ...]
     */
    private final int[] radiusCallsHistogram;

    public RepositoryUsageStatistics(int dataPointCount,
        Map<String,Double> iataCallFrecuencies, int[] radiusCallsHistogram) {
        this.dataPointCount = dataPointCount;
        this.iataCallFrecuencies = iataCallFrecuencies;
        this.radiusCallsHistogram = radiusCallsHistogram;
    }

    public final int getDataPointCount() {
        return dataPointCount;
    }

    public final Map<String,Double> getIataCallFrecuencies() {
        return iataCallFrecuencies;
    }

    public final int[] getRadiusCallsHistogram() {
        return radiusCallsHistogram;
    }

}
