package com.example.trial.weather.domain;

import com.example.trial.weather.exceptions.InvalidDataPointException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Encapsulates sensor information for a particular location.
 * @author code test administrator
 */
public class AtmosphericInformation {

    /** Base prime used for hash code calculation. */
    private static final int BASE_HASH_CODE_PRIME_NUMBER = 5;

    /** Second prime used for hash code calculation. */
    private static final int SECOND_HASH_CODE_PRIME_NUMBER = 59;

    /** Hash code shift factor. */
    private static final int HASH_CODE_SHIFT = 32;

    /**
     * Temperature in degrees Celsius.
     */
    private DataPoint temperature;

    /**
     * Wind speed in km/h.
     */
    private DataPoint wind;

    /**
     * Humidity in percent.
     */
    private DataPoint humidity;

    /**
     * Precipitation in cm.
     */
    private DataPoint precipitation;

    /**
     * Pressure in mmHg.
     */
    private DataPoint pressure;

    /** 
     * Cloud cover percent from 0 - 100 (integer).
     */
    private DataPoint cloudCover;

    /**
     * The last time this data was updated, in milliseconds since UTC epoch.
     */
    private long lastUpdateTime;

    /**
     * @return The number of not null data points hold by this object
     */
    @JsonIgnore
    public int getNotNullDataPointCount() {
        return (temperature != null ? 1 : 0) +
                (wind != null ? 1 : 0) +
                (humidity != null ? 1 : 0) +
                (precipitation != null ? 1 : 0) +
                (pressure != null ? 1 : 0) +
                (cloudCover != null ? 1 : 0);
    }

    /**
     * Update atmospheric information with the given data point for the given
     * point type.
     * @param type the data point type
     * @param dataPoint the actual data point
     * @throws InvalidDataPointException If the data point is not valid
     */
    public void update(DataPoint.Type type, DataPoint dataPoint)
        throws InvalidDataPointException {

        // Validate the data point
        dataPoint.validate(type);

        switch (type) {
            case WIND:
                this.wind = dataPoint;
                break;
            case TEMPERATURE:
                temperature = dataPoint;
                break;
            case HUMIDITY:
                humidity = dataPoint;
                break;
            case PRESSURE:
                pressure = dataPoint;
                break;
            case CLOUD_COVER:
                cloudCover = dataPoint;
                break;
            case PRECIPITATION:
                precipitation = dataPoint;
                break;
            default:
                throw new RuntimeException("Unexpected DataPointType " + type);
        }

        // Updating the last update mark
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this,
            ToStringStyle.NO_CLASS_NAME_STYLE);
    }

	@Override
	public int hashCode() {
        // The hash code generation is based on arbitrary different prime
        // numbers like 5 and 59 as shown in the following lines
		int hash = BASE_HASH_CODE_PRIME_NUMBER;
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.temperature);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.wind);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.humidity);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.precipitation);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.pressure);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.cloudCover);
		hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + (int) (this.lastUpdateTime ^ (this.lastUpdateTime >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final AtmosphericInformation other = (AtmosphericInformation) obj;
        return this.lastUpdateTime == other.lastUpdateTime &&
                this.equalsButLastUpdateTime(obj);
	}

    /**
     * @param obj Another AtmosphericInformation to compare to
     * @return true if the objects are equals but lastUpdateTime (it means,
     * even lastUpdateTime's are not equal)
     */
	public boolean equalsButLastUpdateTime(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final AtmosphericInformation other = (AtmosphericInformation) obj;
        return Objects.equals(this.temperature, other.temperature)
                && Objects.equals(this.wind, other.wind)
                && Objects.equals(this.humidity, other.humidity)
                && Objects.equals(this.precipitation, other.precipitation)
                && Objects.equals(this.pressure, other.pressure)
                && Objects.equals(this.cloudCover, other.cloudCover);
	}

	// Getters and Setters

	public DataPoint getTemperature() {
		return temperature;
	}

	public void setTemperature(DataPoint temperature) {
		this.temperature = temperature;
	}

	public DataPoint getWind() {
		return wind;
	}

	public void setWind(DataPoint wind) {
		this.wind = wind;
	}

	public DataPoint getHumidity() {
		return humidity;
	}

	public void setHumidity(DataPoint humidity) {
		this.humidity = humidity;
	}

	public DataPoint getPrecipitation() {
		return precipitation;
	}

	public void setPrecipitation(DataPoint precipitation) {
		this.precipitation = precipitation;
	}

	public DataPoint getPressure() {
		return pressure;
	}

	public void setPressure(DataPoint pressure) {
		this.pressure = pressure;
	}

	public DataPoint getCloudCover() {
		return cloudCover;
	}

	public void setCloudCover(DataPoint cloudCover) {
		this.cloudCover = cloudCover;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
    
}
