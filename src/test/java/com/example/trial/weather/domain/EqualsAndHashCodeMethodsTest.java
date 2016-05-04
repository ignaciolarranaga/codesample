package com.example.trial.weather.domain;

import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.domain.AirportData;
import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class EqualsAndHashCodeMethodsTest {
    
    @Test
    public void validateAirportData() {
		PodamFactory factory = new PodamFactoryImpl();

        // Creating 2 samples from on same equivalence class (i.e. if we base only on the id they are 2 equals objects)
		AirportData eq11 = factory.manufacturePojo(AirportData.class);
        AirportData eq12 = new AirportData();
        BeanUtils.copyProperties(eq11, eq12);
		// Create 2 samples from ANOTHER equivalence class (another 2 equal objects but differnt from the previous)
		AirportData eq21 = factory.manufacturePojo(AirportData.class);
        AirportData eq22 = new AirportData();
        BeanUtils.copyProperties(eq21, eq22);

		new EqualsTester()
			// The 2 samples of the same equivalence class have to be equals
			.addEqualityGroup(eq11, eq12)
            // Those too but different from the previous equivalence class
			.addEqualityGroup(eq21, eq22)
			.testEquals();
    }

	@Test
    public void validateAtmosphericInformation() {
		PodamFactory factory = new PodamFactoryImpl();

        // Creating 2 samples from on same equivalence class (i.e. if we base only on the id they are 2 equals objects)
		AtmosphericInformation eq11 = factory.manufacturePojo(AtmosphericInformation.class);
        AtmosphericInformation eq12 = new AtmosphericInformation();
        BeanUtils.copyProperties(eq11, eq12);
		// Create 2 samples from ANOTHER equivalence class (another 2 equal objects but differnt from the previous)
		AtmosphericInformation eq21 = factory.manufacturePojo(AtmosphericInformation.class);
        AtmosphericInformation eq22 = new AtmosphericInformation();
        BeanUtils.copyProperties(eq21, eq22);

		new EqualsTester()
            // The 2 samples of the same equivalence class have to be equals
			.addEqualityGroup(eq11, eq12)
            // Those too but different from the previous equivalence class
			.addEqualityGroup(eq21, eq22)
			.testEquals();
    }

	@Test
    public void validateDataPoint() {
		PodamFactory factory = new PodamFactoryImpl();

        // Creating 2 samples from on same equivalence class (i.e. if we base only on the id they are 2 equals objects)
		DataPoint eq11 = factory.manufacturePojo(DataPoint.class);
        DataPoint eq12 = new DataPoint();
        BeanUtils.copyProperties(eq11, eq12);
		// Create 2 samples from ANOTHER equivalence class (another 2 equal objects but differnt from the previous)
		DataPoint eq21 = factory.manufacturePojo(DataPoint.class);
        DataPoint eq22 = new DataPoint();
        BeanUtils.copyProperties(eq21, eq22);

		new EqualsTester()
			// The 2 samples of the same equivalence class have to be equals
			.addEqualityGroup(eq11, eq12)
            // Those too but different from the previous equivalence class
			.addEqualityGroup(eq21, eq22)
			.testEquals();
    }

}