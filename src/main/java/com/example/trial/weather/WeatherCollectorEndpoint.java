package com.example.trial.weather;

import com.example.trial.weather.domain.AirportData;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * The interface shared to airport weather collection systems.
 * @author code test administartor
 */
public interface WeatherCollectorEndpoint {

    /**
     * A liveliness check for the collection endpoint.
     * @return 1 if the endpoint is alive functioning, 0 otherwise
     */
    Response ping();

    /**
     * Update the airports atmospheric information for a particular pointType
     * with json formatted data point information.
     * @param iata the 3 letter airport code
     * @param pointType the type of point being updated, refer to
     * {@link com.example.trial.weather.domain.DataPoint.Type} for details
     * @param dataPoint a json dict containing mean, first, second, third
     * and count keys
     * @return HTTP Response code
     */
    Response updateWeather(String iata, String pointType, String dataPoint);

    /**
     * Return a list of known airports as a json formatted list.
     * @return HTTP Response code and a json formatted list of IATA codes
     */
    Response getAirports();

    /**
     * Retrieve airport data, including latitude and longitude for a particular
     * airport.
     * @param iata the 3 letter airport code
     * @return an HTTP Response with a json representation of
     * {@link com.example.trial.weather.domain.AirportData}
     */
    Response getAirport(String iata);

    /**
     * Add a new airport to the known airport list.
     * @param iata the 3 letter airport code of the new airport
     * @param latitude the airport's latitude in degrees as a string [-90, 90]
     * @param longitude the airport's longitude in degrees as a string [-180, 180]
     * @return HTTP Response code for the add operation
     */
    Response addAirport(String iata, String latitude, String longitude);


    /**
     * Adds a list of airports to the known airports list.
     * @param airports The list of airports to be added
     * @return HTTP Response code for the add operation
     */
    Response addAirports(List<AirportData> airports);

    /**
     * Removes an airport from the known airport list.
     * @param iata the 3 letter airport code
     * @return HTTP Response code for the delete operation
     */
    Response deleteAirport(String iata);

    /**
     * Exits the process finalizing the server.
     * Please use carefully, if possible I would remove it from the API.
     * @return A meaning less response because the process was finalized
     */
    Response exit();

}
