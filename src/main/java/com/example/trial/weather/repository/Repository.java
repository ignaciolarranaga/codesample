package com.example.trial.weather.repository;

import com.example.trial.weather.exceptions.AirportNotFoundExcepition;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.exceptions.InvalidDataPointException;
import java.util.Collection;
import java.util.Set;

/**
 * This interface represents the a repository for the service information.
 * It defines all the operation that need to be supported in order to hold the
 * service information.
 * IMPORTANT: The repository operations need to be thread safe, as it is
 * accessed in paralllel.
 * @author ignaciolarranaga@gmail.com
 */
public interface Repository {

    // Airport mateinance related methods

    /**
     * Adds an airport to the repository.
     * @param airport The airport to be added
     */
    void addAirport(AirportData airport);

    /**
     * Deletes an airport from the repository.
     * @param iata The iata code to do the delete
     * @throws AirportNotFoundExcepition If the airport was not found
     */
    void removeAirport(String iata) throws AirportNotFoundExcepition;

    /**
     * @param iata The iata code to do the search
     * @return The airport identified by the iata code or null if not exists
     */
    AirportData getAirport(String iata);

    /**
     * @param iata The iata code to do the search
     * @return true or false depending if the airport is defined or not
     */
    boolean containsAirport(String iata);

    /**
     * @return All the defined airports
     */
    Set<AirportData> getAirports();

    /**
     * Returns a set of all the current airport codes.
     * TODO: Analyze if this method is required (or removed in favor of #getAirports())
     * @return All the current airport codes stored
     */
    Set<String> getAirportCodes();

    // AtmosphericInformation related methods

    /**
     * @param iata The iata code to do the search
     * @param radius (optional) Indicates the radius to get atmospheric
     * information. If null then only iata specific atmospheric information is
     * returned
     * @return A collection of The atmospheric information for the specified
     * iata code or within the specified radius if provided
     * @throws AirportNotFoundExcepition If the given airport is not found
     */
    Collection<AtmosphericInformation> getAtmosphericInformation(
        String iata, Double radius) throws AirportNotFoundExcepition;

    /**
     * @return All the current atmospheric information
     */
    Collection<AtmosphericInformation> getAllAtmosphericInformation();

    /**
     * This method updates the current atmospheric information.
     * @param iata The iata code of the airport to do the update
     * @param type The data point type to do the update
     * @param dataPoint The actual data point to update
     * @throws InvalidDataPointException If the information contained in the
     * data point is not invalid
     * @throws AirportNotFoundExcepition If the airport is not found
     */
    void updateAtmosphericInformation(String iata, DataPoint.Type type,
        DataPoint dataPoint)
        throws InvalidDataPointException, AirportNotFoundExcepition;

    // Statistics related methods

    /**
     * @return The current usage statistics of the repository
     */
    RepositoryUsageStatistics getUsageStatistics();

    /**
     * This method resets the repository information.
     */
    void reset();

}
