package com.example.trial.weather.domain;

import com.example.trial.weather.exceptions.InvalidDataPointException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A collected point, including some information about the range of collected
 * values (first, second & third quartiles, mean and count of observations).
 * @author code test administrator
 */
public class DataPoint {

    /** Base prime used for hash code calculation. */
    private static final int BASE_HASH_CODE_PRIME_NUMBER = 3;

    /** Second prime used for hash code calculation. */
    private static final int SECOND_HASH_CODE_PRIME_NUMBER = 97;

    /** Hash code shift factor. */
    private static final int HASH_CODE_SHIFT = 32;

    /** Minimum value for the mean of the data point. */
    private static final int WIND_MINIMUM_MEAN_VALUE = 0;

    /** Minimum value for the mean of the data point. */
    private static final int PRECIPITATION_MINIMUM_MEAN_VALUE = 0;
    /** Maximum value (including the value) for the mean of the data point. */
    private static final int PRECIPITATION_MAXIMUM_MEAN_INCLUDED_VALUE = 100;

    /** Minimum value for the mean of the data point. */
    private static final int PRESSURE_MINIMUM_MEAN_VALUE = 650;
    /** Maximum value (including the value) for the mean of the data point. */
    private static final int PRESSURE_MAXIMUM_MEAN_INCLUDED_VALUE = 800;

    /** Minimum value for the mean of the data point. */
    private static final int CLOUD_COVER_MINIMUM_MEAN_VALUE = 0;
    /** Maximum value (including the value) for the mean of the data point. */
    private static final int CLOUD_COVER_MAXIMUM_MEAN_INCLUDED_VALUE = 100;

    /** Minimum value for the mean of the data point. */
    private static final int HUMIDITY_MINIMUM_MEAN_VALUE = 0;
    /** Maximum value (including the value) for the mean of the data point. */
    private static final int HUMIDITY_MAXIMUM_MEAN_INCLUDED_VALUE = 100;

    /** Minimum value for the mean of the data point. */
    private static final int TEMPERATURE_MINIMUM_MEAN_VALUE = -50;
    /** Maximum value (including the value) for the mean of the data point. */
    private static final int TEMPERATURE_MAXIMUM_MEAN_INCLUDED_VALUE = 100;

    /**
     * The various types of data points we can collect.
     */
    public enum Type {
        WIND, TEMPERATURE, HUMIDITY, PRESSURE, CLOUD_COVER, PRECIPITATION
    }

    /**
     * 1st quartile -- useful as a lower bound.
     */
    private int first;

    /**
     * 2nd quartile -- median value.
     */
    private int second;

    /**
     * 3rd quartile value -- less noisy upper value.
     */
    private int third;

    /**
     * The mean of the observations.
     */
    private double mean;

     /**
      * The total number of measurements. It should be greater than 0
      */
    private int count;

    public DataPoint() {
    }

    public DataPoint(int first, int second, int third, double mean, int count) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.mean = mean;
        this.count = count;
    }

    /**
     * This method validates the DataPoint (it values) considering the type of
     * measurement it represents. For example a wind measure can not be negative
     * IMPORTANT: Only the specified restrictions were enforced, but it might
     * be other that are not tested.
     * @param type The type of measure to perform the validation
     * @throws InvalidDataPointException If the data point is not valid
     */
    public void validate(Type type) throws InvalidDataPointException {
        //TODO: Investigate if there are more restrictions to apply

        // Validate count
        if (count <= 0) {
            throw new InvalidDataPointException("The count value of the data "
                + "point is incorrect (it should be greater than 0). "
                + "Count: " + count, this);
        }

        switch (type) {
            case WIND:
                if (mean < WIND_MINIMUM_MEAN_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be greather or equals to 0. "
                        + "Mean: " + mean, this);
                }
                break;
            case TEMPERATURE:
                if (mean < TEMPERATURE_MINIMUM_MEAN_VALUE
                    || mean >= TEMPERATURE_MAXIMUM_MEAN_INCLUDED_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be between [-50 & 100)). "
                        + "Mean: " + mean, this);
                }
                break;
            case HUMIDITY:
                if (mean < HUMIDITY_MINIMUM_MEAN_VALUE
                    || mean >= HUMIDITY_MAXIMUM_MEAN_INCLUDED_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be between [0 & 100)). "
                        + "Mean: " + mean, this);
                }
                break;
            case PRESSURE:
                if (mean < PRESSURE_MINIMUM_MEAN_VALUE
                    || mean >= PRESSURE_MAXIMUM_MEAN_INCLUDED_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be between [650 & 800)). "
                        + "Mean: " + mean, this);
                }
                break;
            case CLOUD_COVER:
                if (mean < CLOUD_COVER_MINIMUM_MEAN_VALUE
                    || mean >= CLOUD_COVER_MAXIMUM_MEAN_INCLUDED_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be between [0 & 100)). "
                        + "Mean: " + mean, this);
                }
                break;
            case PRECIPITATION:
                if (mean < PRECIPITATION_MINIMUM_MEAN_VALUE
                    || mean >= PRECIPITATION_MAXIMUM_MEAN_INCLUDED_VALUE) {
                    throw new InvalidDataPointException("The data point is "
                        + "invalid, the mean should be between [0 & 100)). "
                        + "Mean: " + mean, this);
                }
                break;
            default:
                throw new RuntimeException("Unexpected DataPoint type " + type);
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this,
            ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    @Override
    public int hashCode() {
        // The hash code generation is based on arbitrary different prime
        // numbers like 3 and 97 as shown in the following lines
        int hash = BASE_HASH_CODE_PRIME_NUMBER;
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + this.first;
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + this.second;
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + this.third;
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash
            + (int) (Double.doubleToLongBits(this.mean)
            ^ (Double.doubleToLongBits(this.mean) >>> HASH_CODE_SHIFT));
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + this.count;
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

        final DataPoint other = (DataPoint) obj;
        return this.first == other.first
                && this.second == other.second
                && this.third == other.third
                && this.mean == other.mean
                && this.count == other.count;
    }

    // Getters and Setters

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}