package com.example.trial.weather.exceptions;

/**
 * This exception represents an invalid IATA code detected.
 * @author ignaciolarranaga@gmail.com
 */
public class InvalidIATAException extends WeatherException {

    /** The iata code associate to the exception. */
    private final String iata;

    public InvalidIATAException(String reason, String iata) {
        super(reason);
        
        this.iata = iata;
    }

    public final String getIata() {
        return iata;
    }

}
