package com.example.trial.weather.domain;

import com.example.trial.weather.exceptions.InvalidAirportDataException;
import com.example.trial.weather.exceptions.InvalidIATAException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The class defines the airport information.
 * @author code test administrator
 */
public class AirportData {

    /**
     * Earth radius in KM, used to calculate the distance between airports.
     */
    private static final double EARTH_RADIUS_IN_KM = 6372.8;

    /** Maximum allowed longitude. */
    private static final int MAXIMUM_LONGITUDE = 180;

    /** Minimum allowed longitude. */
    private static final int MINIMUM_LONGITUDE = -180;

    /** Maximum allowed latitude. */
    private static final int MAXIMUM_LATITUDE = 90;

    /** Minimum allowed latitude. */
    private static final int MINIMUM_LATITUDE = -90;

    /** Base prime used for hash code calculation. */
    private static final int BASE_HASH_CODE_PRIME_NUMBER = 7;

    /** Second prime used for hash code calculation. */
    private static final int SECOND_HASH_CODE_PRIME_NUMBER = 89;

    /** Hash code shift factor. */
    private static final int HASH_CODE_SHIFT = 32;

    /**
     * The expected size of an IATA code.
     * @see https://en.wikipedia.org/wiki/International_Air_Transport_Association_airport_code
     */
    private static final int EXPECTED_IATA_CODE_SIZE = 3;

    /**
     * This is the expected patter of the IATA codes
     */
    private static final String EXPECTED_IATA_PATTERN = "[A-Z]{3}";

    /**
     * The three letter IATA code.
     * @see https://en.wikipedia.org/wiki/International_Air_Transport_Association_airport_code
     */
    private String iata;

    /**
     * Latitude value in degrees.
     * Valid values are in range [-90,90]
     * @see https://en.wikipedia.org/wiki/Latitude
     */
    private double latitude;

    /**
     * Longitude value in degrees.
     * Valid values are in range [-180,180]
     * @see https://en.wikipedia.org/wiki/Longitude
     */
    private double longitude;

    public AirportData() {
    }

    public AirportData(String iata, double latitude, double longitude) {
        this.iata = iata;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * This method validates the coherence of the airport data (iata codes to
     * 'seems' valid, latitude and longitude valid values).
     * IMPORTANT: IATA codes are ONLY validated in structure not against the
     * actual table of valid values
     * @throws InvalidAirportDataException If the information is not valid
     */
    public void validate() throws InvalidAirportDataException {
        try {
            validateIATA(this.iata);
        } catch (InvalidIATAException ex) {
            throw new InvalidAirportDataException("The iata code is invalid", this, ex);
        }

        if (latitude < MINIMUM_LATITUDE || latitude > MAXIMUM_LATITUDE) {
            throw new InvalidAirportDataException("The latitude value " +
                latitude + " is invalid.", this);
        }

        if (longitude < MINIMUM_LONGITUDE || longitude > MAXIMUM_LONGITUDE) {
            throw new InvalidAirportDataException("The longitude value " +
                longitude + " is invalid.", this);
        }
    }

    /**
     * This method validates a IATA code.
     * @param iata The iata to be validated
     * @throws InvalidIATAException If the IATA code is invalid
     */
    public static void validateIATA(String iata) throws InvalidIATAException {
        if (iata == null) {
            throw new InvalidIATAException("The iata code can not be null.", iata);
        }

        if (iata.length() != EXPECTED_IATA_CODE_SIZE) {
            throw new InvalidIATAException("The iata code lenght is "
                + "not valid, it has " + iata.length() + " characters.", iata);
        }

        if (!iata.matches(EXPECTED_IATA_PATTERN)) {
            throw new InvalidIATAException("The iata code: " + iata + " is invalid.", iata);
        }
    }

    /**
     * This uses the ‘haversine’ formula to calculate the great-circle distance
     * between two points – that is, the shortest distance over the earth’s
     * surface.
     * @see http://andrew.hedges.name/experiments/haversine/
     * @see http://www.movable-type.co.uk/scripts/latlong.html
     * @see https://en.wikipedia.org/wiki/Haversine_formula
     * @param other Is the other airport to calculate the distance to
     * @return The Haversine distance from this airport to the other
     */
    public double distance(AirportData other) {
        double deltaLat = Math.toRadians(other.latitude - latitude);
        double deltaLon = Math.toRadians(other.longitude - longitude);
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.pow(Math.sin(deltaLon / 2), 2)
                * Math.cos(latitude) * Math.cos(other.latitude);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_IN_KM * c;
    }

	@Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this,
            ToStringStyle.NO_CLASS_NAME_STYLE);
    }

	@Override
	public int hashCode() {
        // The hash code generation is based on arbitrary different prime
        // numbers like 7 and 89 as shown in the following lines
        int hash = BASE_HASH_CODE_PRIME_NUMBER;
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash + Objects.hashCode(this.iata);
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash
            + (int) (Double.doubleToLongBits(this.latitude)
            ^ (Double.doubleToLongBits(this.latitude) >>> HASH_CODE_SHIFT));
        hash = SECOND_HASH_CODE_PRIME_NUMBER * hash
            + (int) (Double.doubleToLongBits(this.longitude)
            ^ (Double.doubleToLongBits(this.longitude) >>> HASH_CODE_SHIFT));
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

		final AirportData other = (AirportData) obj;
        return this.latitude == other.latitude
                && this.longitude == other.longitude
                && Objects.equals(this.iata, other.iata);
	}

	// Getters and Setters

	public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
