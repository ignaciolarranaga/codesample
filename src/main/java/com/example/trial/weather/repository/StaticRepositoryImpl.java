package com.example.trial.weather.repository;

import com.example.trial.weather.exceptions.AirportNotFoundExcepition;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.exceptions.InvalidDataPointException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of the repository using static fields and
 * synchronized methods to be thread safe.
 * @author ignaciolarranaga@gmail.com
 */
public class StaticRepositoryImpl implements Repository {

    /**
     * The atmospheric information for each airport.
     */
    private static Map<String, AtmosphericInformation> atmosphericInformation = new HashMap<>();

    /**
     * The airports information.
     */
    private static Set<AirportData> airports = new HashSet<>();

    /**
     * This is a map containing the counters for all the IATA calls received.
     */
    private static Map<String, Integer> iataCallCounters = new HashMap<>();

    /**
     * This is a counter on how many times an specific radius was requested on
     * a weather method call (i.e 1.0=>10, 1.5=>5, etc.).
     * @see RestWeatherQueryEndpoint#weather(String, String)
     */
    private static Map<Integer, Integer> radiusCallCounters = new HashMap<>();

    /**
     * The total number of query method calls.
     * @see #getAtmosphericInformation(String, Double)
     */
    private static int totalCallCount = 0;

    /**
     * Adds an airport to the container.
     * @param airport The airport to be added
     * @see Repository#addAirport(AirportData)
     */
    @Override
    public synchronized void addAirport(AirportData airport) {
        airports.add(airport);
    }

    /**
     * Removes an airport from the container or throws AirportNotFoundExcepition
     * if it is not found.
     * @param iata The iata code of the airport to be removed
     * @see Repository#removeAirport(String)
     * @throws AirportNotFoundExcepition If not defined in the airports container
     */
    @Override
    public synchronized void removeAirport(String iata) throws AirportNotFoundExcepition {
        for (Iterator<AirportData> it = airports.iterator(); it.hasNext();) {
            AirportData data = it.next();
            if (data.getIata().equals(iata)) {
                it.remove();
                return;
            }
        }

        throw new AirportNotFoundExcepition("The airport " + iata +
            "was not found", iata);
    }

    /**
     * @param iata The iata code of the airport to do the search
     * @return An airport from the container of null if not found
     * @see Repository#getAirport(String)
     */
    @Override
    public synchronized AirportData getAirport(String iata) {
        return airports.stream()
            .filter(ap -> ap.getIata().equals(iata))
            .findFirst().orElse(null);
    }

    /**
     * @param iata The iata code of the airport to do the search
     * @return true if the airport with this IATA code is found on the container
     * @see Repository#containsAirport(String)
     */
    @Override
    public synchronized boolean containsAirport(String iata) {
        return airports.stream()
            .map(a -> a.getIata())
            .anyMatch(c -> c.equals(iata));
    }

    /**
     * @return A copy of the current airports container
     * @see Repository#getAirports()
     */
    @Override
    public synchronized Set<AirportData> getAirports() {
        return new HashSet<>(airports);
    }

    /**
     * @return A set of all the current airport IATA codes
     * @see Repository#getAirportCodes() 
     */
    @Override
    public synchronized Set<String> getAirportCodes() {
        return airports.stream()
            .map(a -> a.getIata())
            .collect(Collectors.toSet());
    }

    /**
     * Filters the container for the specific data and performs the query.
     * This method also counts a call for the usage statistics.
     * @param iata The iata code of the airport to search
     * @param radius The radius around the airport.
     * @return A collection of all the airports
     * @throws AirportNotFoundExcepition If the airport is not already in the repository
     * @see Repository#getAtmosphericInformation(String, Double)
     */
    @Override
    public synchronized Collection<AtmosphericInformation> getAtmosphericInformation(
        String iata, Double radius) throws AirportNotFoundExcepition {
        AirportData airport = getAirport(iata);
        
        if (airport == null) {
            throw new AirportNotFoundExcepition("The airport " + iata +
                "was not found", iata);
        }

        increaseCallCounters(iata, radius);

        return airports.stream()
            // Filtering only airports within the indicated radious
            .filter(other -> airport.distance(other) <=
                // Null raidous means 0, i.e only the same airport
                (radius != null ? radius : 0.0))
            // Filtering only airports with data
            .filter(other ->
                atmosphericInformation.containsKey(other.getIata()))
            // Obtaining the data
            .map(other -> atmosphericInformation.get(other.getIata()))
            .collect(Collectors.toList());
    }

    /**
     * @return A collection of all the atmospheric information
     * @see Repository#getAllAtmosphericInformation() 
     */
    @Override
    public synchronized Collection<AtmosphericInformation> getAllAtmosphericInformation() {
        return atmosphericInformation.values();
    }

    /**
     * This method performs validations and delegates to the corresponding
     * atmospheric information object.
     * @param iata The iata code of the airport to update the information
     * @param type The type of data point
     * @param dataPoint The data point to be added
     * @throws InvalidDataPointException If the
     * AtmosphericInformation#update(DataPoint.Type, DataPoint) throws it
     * @throws AirportNotFoundExcepition If the getAirport method does not found it
     * @see Repository#updateAtmosphericInformation(String, DataPoint.Type, DataPoint)
     */
    @Override
    public synchronized void updateAtmosphericInformation(String iata,
        DataPoint.Type type, DataPoint dataPoint)
        throws InvalidDataPointException, AirportNotFoundExcepition {
        if (getAirport(iata) == null) {
            throw new AirportNotFoundExcepition("The airport " + iata +
                "was not found", iata);
        }

        if (! atmosphericInformation.containsKey(iata)) {
            atmosphericInformation.put(iata, new AtmosphericInformation());
        }

        atmosphericInformation.get(iata).update(type, dataPoint);
    }

    /**
     * This method build and returns the current usage statics from the counters
     * that this object holds.
     * @return An statistics object just built for this call
     * @see Repository#getUsageStatistics() 
     */
    @Override
    public synchronized RepositoryUsageStatistics getUsageStatistics() {
        // 24 hour threshold for the statistics
        final long threshold = System.currentTimeMillis() - 86400000;
        int datasize = atmosphericInformation.values().stream()
            // Filtering older information
            .filter(ai -> ai.getLastUpdateTime() > threshold)
            // Counting the number of data points per AtmosphericInformation
            .mapToInt(ai -> ai.getNotNullDataPointCount())
            .sum();

        Map<String, Double> freq = airports.stream()
        // Mapping each IATA counter to its frequency dividing by the total
        // number of weather method calls
        .collect(Collectors.toMap(ad -> ad.getIata(),
            ad -> totalCallCount == 0 ? 0 :
               (double) iataCallCounters.getOrDefault(ad.getIata(), 0) /
                        totalCallCount));

        // Calculating the radius histogram
        int[] histogram = new int[0];
        Map<Integer,Integer> radiusCallCount = radiusCallCounters;
        if (! radiusCallCount.isEmpty()) {
            int maximumRadius = radiusCallCount.keySet().stream()
                .max(Integer::compare).orElse(0);
            histogram = new int[maximumRadius + 1];
            for (Map.Entry<Integer,Integer> entry : radiusCallCount.entrySet()) {
                histogram[entry.getKey()] = entry.getValue();
            };
        }

        return new RepositoryUsageStatistics(datasize, freq, histogram);
    }

    /**
     * Resets all the variables. Mainly intended for testing.
     * @see Repository#reset() 
     */
    public synchronized void reset() {
        airports.clear();
        atmosphericInformation.clear();

        // Counters
        totalCallCount = 0;
        radiusCallCounters.clear();
        iataCallCounters.clear();
    }

    private void increaseCallCounters(String iata, Double radius) {
        totalCallCount++;

        // Icreasing the iata call counter
        if (iata != null) {
            iataCallCounters.put(iata, iataCallCounters.getOrDefault(iata, 0) + 1);
        }

        // Increasing the radius call counter
        if (radius == null) {
            // Null radius is equivalent to 0
            radius = 0.0;
        }

        // The radius is counted on the nearest integer, but below 1000
        // 1000 is a supposition on the maximum "interesting" radious
        // derived from the original code
        int nearestIntegerRadius = (int) Math.round(Math.min(radius, 1000));

        radiusCallCounters.put(nearestIntegerRadius,
            radiusCallCounters.getOrDefault(nearestIntegerRadius, 0) + 1);
    }

}
