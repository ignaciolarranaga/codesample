package com.example.trial.weather.exceptions;

import com.example.trial.weather.domain.AirportData;

/**
 * This exception represents that a AirportData is not valid.
 * @see com.example.trial.weather.domain.DataPoint
 * @author ignaciolarranaga@gmail.com
 */
public class InvalidAirportDataException extends WeatherException {

    /** The airport data associate to the exception. */
    private final AirportData airportData;

    public InvalidAirportDataException(String reason, AirportData airportData) {
        super(reason);

        this.airportData = airportData;
    }

    public InvalidAirportDataException(String reason, AirportData airportData,
        Throwable cause) {
        super(reason, cause);

        this.airportData = airportData;
    }

    public final AirportData getAirportData() {
        return airportData;
    }

}
