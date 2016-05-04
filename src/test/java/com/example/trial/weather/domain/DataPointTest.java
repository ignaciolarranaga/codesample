package com.example.trial.weather.domain;

import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.exceptions.InvalidDataPointException;
import com.example.trial.weather.test.util.TestUtilities;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class DataPointTest {

    @Test
    public void testSampleDataPoints() throws Exception {
        // All those validations are expected to succeed (ie. not raise exception)
        TestUtilities.CLOUD_COVER_SAMPLE_DATA_POINT.validate(DataPoint.Type.CLOUD_COVER);
        TestUtilities.HUMIDITY_SAMPLE_DATA_POINT.validate(DataPoint.Type.HUMIDITY);
        TestUtilities.PRECIPITATION_SAMPLE_DATA_POINT.validate(DataPoint.Type.PRECIPITATION);
        TestUtilities.PRESSURE_SAMPLE_DATA_POINT.validate(DataPoint.Type.PRESSURE);
        TestUtilities.TEMPERATURE_SAMPLE_DATA_POINT.validate(DataPoint.Type.TEMPERATURE);
        TestUtilities.WIND_SAMPLE_DATA_POINT.validate(DataPoint.Type.WIND);
    }

    /**
     * Restrictions count > 0 (without measures it might not be data point)
     * @see DataPoint
     */
    @Test
    public void testDataPointCount() throws Exception {
        // All those validations should pass
        new DataPoint(10, 20, 30, 0, 1).validate(DataPoint.Type.WIND);
        new DataPoint(10, 20, 30, 10, 16).validate(DataPoint.Type.WIND);

        try {
            new DataPoint(10, 20, 30, 0, 0).validate(DataPoint.Type.WIND);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * WIND data point. Restrictions mean >= 0
     * @see DataPoint
     */
    @Test
    public void testWindDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(10, 20, 30, 0, 6).validate(DataPoint.Type.WIND);
        new DataPoint(10, 20, 30, 10, 6).validate(DataPoint.Type.WIND);

        try {
            new DataPoint(10, 20, 30, -1, 6).validate(DataPoint.Type.WIND);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * PRESSURE data point. Restrictions mean >= 650 && mean < 800
     * @see DataPoint
     */
    @Test
    public void testPressureDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(690, 695, 710, 650, 2).validate(DataPoint.Type.PRESSURE);
        new DataPoint(690, 695, 710, 700, 2).validate(DataPoint.Type.PRESSURE);
        new DataPoint(690, 695, 710, 799, 2).validate(DataPoint.Type.PRESSURE);

        try {
            new DataPoint(690, 695, 710, 649, 2).validate(DataPoint.Type.PRESSURE);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }

        try {
            new DataPoint(690, 695, 710, 800, 2).validate(DataPoint.Type.PRESSURE);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * PRECIPITATION data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    @Test
    public void testPrecipitationDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(10, 50, 60, 0, 3).validate(DataPoint.Type.PRECIPITATION);
        new DataPoint(10, 50, 60, 50, 3).validate(DataPoint.Type.PRECIPITATION);
        new DataPoint(10, 50, 60, 99, 3).validate(DataPoint.Type.PRECIPITATION);

        try {
            new DataPoint(10, 50, 60, -1, 3).validate(DataPoint.Type.PRECIPITATION);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }

        try {
            new DataPoint(10, 50, 60, 100, 3).validate(DataPoint.Type.PRECIPITATION);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * TEMPERATURE data point. Restrictions mean >= -50 && mean < 100
     * @see DataPoint
     */
    @Test
    public void testTemperatureDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(-1, 15, 30, -50, 4).validate(DataPoint.Type.TEMPERATURE);
        new DataPoint(-1, 15, 30, 20, 4).validate(DataPoint.Type.TEMPERATURE);
        new DataPoint(-1, 15, 30, 99, 4).validate(DataPoint.Type.TEMPERATURE);

        try {
            new DataPoint(-1, 15, 30, -51, 4).validate(DataPoint.Type.TEMPERATURE);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }

        try {
            new DataPoint(-1, 15, 30, 100, 4).validate(DataPoint.Type.TEMPERATURE);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * CLOUD COVER data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    @Test
    public void testCloudCoverDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(10, 25, 30, 0, 5).validate(DataPoint.Type.CLOUD_COVER);
        new DataPoint(10, 25, 30, 50, 5).validate(DataPoint.Type.CLOUD_COVER);
        new DataPoint(10, 25, 30, 99, 5).validate(DataPoint.Type.CLOUD_COVER);

        try {
            new DataPoint(10, 25, 30, -1, 5).validate(DataPoint.Type.CLOUD_COVER);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }

        try {
            new DataPoint(10, 25, 30, 100, 5).validate(DataPoint.Type.CLOUD_COVER);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

    /**
     * HUMIDITY data point. Restrictions mean >= 0 && mean < 100
     * @see DataPoint
     */
    @Test
    public void testHumidityDataPoint() throws Exception {
        // All those validations should pass
        new DataPoint(10, 20, 30, 0, 6).validate(DataPoint.Type.HUMIDITY);
        new DataPoint(10, 20, 30, 27, 6).validate(DataPoint.Type.HUMIDITY);
        new DataPoint(10, 20, 30, 99, 6).validate(DataPoint.Type.HUMIDITY);

        try {
            new DataPoint(10, 20, 30, -1, 6).validate(DataPoint.Type.HUMIDITY);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }

        try {
            new DataPoint(10, 20, 30, 100, 6).validate(DataPoint.Type.HUMIDITY);
            fail("It should raise the exception because the data point is invalid");
        } catch (InvalidDataPointException ex) {
            assertTrue("It should raise the exception", ex != null);
        }
    }

}
