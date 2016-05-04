package com.example.trial.weather.exceptions;

/**
 * The base exception for all the system exceptions.
 * @author ignaciolarranaga@gmail.com
 */
public class WeatherException extends Exception {

    public WeatherException(String reason) {
        super(reason);
    }

    public WeatherException(String reason, Throwable cause) {
        super(reason, cause);
    }

}
