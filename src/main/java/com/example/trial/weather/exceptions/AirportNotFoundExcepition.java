package com.example.trial.weather.exceptions;

/**
 * Indicates that an airport could not be found.
 * @author ignaciolarranaga@gmail.com
 */
public class AirportNotFoundExcepition extends WeatherException {

    /** The iata code associate to the exception. */
    private final String iata;

    public AirportNotFoundExcepition(String reason, String iata) {
        super(reason);
        
        this.iata = iata;
    }

    public final String getIata() {
        return iata;
    }

}
