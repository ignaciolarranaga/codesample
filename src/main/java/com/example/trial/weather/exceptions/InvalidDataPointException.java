package com.example.trial.weather.exceptions;

import com.example.trial.weather.domain.DataPoint;

/**
 * This exception represents that a DataPoint is not valid.
 * @see DataPoint
 * @author ignaciolarranaga@gmail.com
 */
public class InvalidDataPointException extends WeatherException {

    /** The data point associate to the exception. */
    private final DataPoint dataPoint;

    public InvalidDataPointException(String reason, DataPoint dataPoint) {
        super(reason);

        this.dataPoint = dataPoint;
    }

    public final DataPoint getDataPoint() {
        return dataPoint;
    }

}
