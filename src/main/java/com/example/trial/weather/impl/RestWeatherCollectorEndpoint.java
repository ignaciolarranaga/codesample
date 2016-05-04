package com.example.trial.weather.impl;

import com.example.trial.weather.WeatherCollectorEndpoint;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.domain.DataPoint;
import com.example.trial.weather.exceptions.InvalidAirportDataException;
import com.example.trial.weather.exceptions.AirportNotFoundExcepition;
import com.example.trial.weather.exceptions.InvalidDataPointException;
import com.example.trial.weather.exceptions.InvalidIATAException;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A REST implementation of the WeatherCollector API.
 * Accessible only to airport weather collection sites via secure VPN.
 * @author code test administrator
 */
@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {

    private static final Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());

	/**
	 * Predefined responses (there is no real necessity of build a new response
     * on each call, so handful responses can be pre-built).
	 */
	private static final Response OK_RESPONSE = Response.status(Response.Status.OK).build();
    private static final Response NOT_FOUND_RESPONSE = Response.status(Response.Status.NOT_FOUND).build();

    /**
     * Shared gson
     * Verified it is thread safe:
     * @link: http://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
     */
    private static final Gson gson = new Gson();

    /**
     * @see WeatherCollectorEndpoint#ping()
     * @return An OK response with 1 every time
     */
    @Override
    @GET @Path("/ping")
    public Response ping() {
        // Remember: @return 1 if the endpoint is alive functioning, 0 otherwise
        // That's whay the entity is 1
        final Response response = Response.status(Response.Status.OK).entity(1).build();
        return response;
    }

    /**
     * This method basically takes the parameters, performs some validations and
     * delegates to the repository if the information is reasonably coherent.
     * @param iata The iata code
     * @param pointType The point type to be updated
     * @param dataPointString The string JSON representation of the data point,
     * @return An OK response if successfully updates the atmospheric,
     * NOT_FOUND for not existent airports or data points, and BAD_REQUEST for
     * invalid data points
     * @see WeatherCollectorEndpoint#updateWeather(String, String, String)
     */
    @Override
    @POST @Path("/weather/{iata}/{pointType}")
    public Response updateWeather(@PathParam("iata") String iata,
                                  @PathParam("pointType") String pointType,
                                  String dataPointString) {
        // Validations
        try {
            AirportData.validateIATA(iata);
        } catch (InvalidIATAException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage()).build();
        }

        Repository repository = RepositoryFactory.getInstance();

        DataPoint dataPoint = gson.fromJson(dataPointString, DataPoint.class);

        try {
            // There was a typo error on the DataPoint constants,
            // we can not lost compatibility so the constant can not be updated
            // without the proper handling, which is the following code:
            if (pointType.equals("humidty")) {
                pointType = "humidity";
            } else if (pointType.equals("cloudcover")) {
                // Improved the constant to separate words so this adjustment
                // needs to be made before calling the: DataPoint.Type.valueOf
                pointType = "cloud_cover";
            }

            repository.updateAtmosphericInformation(iata,
                DataPoint.Type.valueOf(pointType.toUpperCase()), dataPoint);
        } catch (IllegalArgumentException ex) {
            final String message = "The data point type: " + pointType
                + " does not exists.";
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.FINEST, "Exception associated: ", ex);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(message).build();
        } catch (InvalidDataPointException ex) {
            final String message = "The data point provided was invalid (BLA): "
                + ex.getDataPoint() + ": " + ex.getMessage();
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.FINEST, "Exception associated: ", ex);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(message).build();
        } catch (AirportNotFoundExcepition ex) {
            final String message = "The airport: " + iata + " does not exists.";
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.FINEST, "Exception associated: ", ex);
            return Response.status(Response.Status.NOT_FOUND).entity(message)
                .build();
        }

        return OK_RESPONSE;
    }

    /**
     * Delegates the call to the repository to get the list of all the codes.
     * @return A response with a list of airport codes
     * @see WeatherCollectorEndpoint#getAirports
     */
    @Override
    @GET @Path("/airports") @Produces(MediaType.APPLICATION_JSON)
    public Response getAirports() {
        Repository repository = RepositoryFactory.getInstance();
        return Response.status(Response.Status.OK)
            .entity(repository.getAirportCodes()).build();
    }

    /**
     * Delegates the call to the repositories to get the airport information.
     * @return A response with a list of airport codes
     * @see WeatherCollectorEndpoint#getAirports
     */
    @Override
    @GET @Path("/airport/{iata}") @Produces(MediaType.APPLICATION_JSON)
    public Response getAirport(@PathParam("iata") String iata) {
        // Validations
        try {
            AirportData.validateIATA(iata);
        } catch (InvalidIATAException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage()).build();
        }

        Repository repository = RepositoryFactory.getInstance();
        AirportData ad = repository.getAirport(iata);
        if (ad != null) {
            return Response.status(Response.Status.OK).entity(ad).build();
        } else {
            String message = "The iata code: " + iata + " was not found.";
            LOGGER.log(Level.WARNING, message);
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();
        }
    }

    /**
     * This method delegates the calls on the #addAiport(String, String, String).
     * @param airports The list of AirportData to be added
     * @return OK if all went OK or BAD_REQUEST if some error occurs in the
     * underlying calls to #addAiport(String, String, String)
     * @see WeatherCollectorEndpoint#addAirports(List<AirportData>)
     */
    @Override
    @POST @Path("/airports") @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAirports(List<AirportData> airports) {
        List errors = new ArrayList();
        for (AirportData airport : airports) {
            // Calling add airport for individual airports and building an
            // unified response
            Response airportResponse = addAirport(airport.getIata(),
                String.valueOf(airport.getLatitude()),
                String.valueOf(airport.getLongitude()));
            if (airportResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                // Changing the response to be an error response
                errors.add(airportResponse.getEntity());
            }
        }

        if (errors.isEmpty()) {
            return OK_RESPONSE;
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).build();
        }
    }

    /**
     * This method do some validations and delegate the call to the repository.
     * @param iata The iata code of the airport to be added
     * @param latitude The latitude of the airport to be added
     * @param longitude The longitude of the airport to be added
     * @return OK if all the validations and the delegation succeed or
     * BAD_REQUEST for invalid IATA codes, CONFLICT for already existing
     * airports or BAD_REQUEST for invalid airport data.
     * @see WeatherCollectorEndpoint#addAirport(String, String, String)
     */
    @Override
    @POST @Path("/airport/{iata}/{lat}/{long}")
    public Response addAirport(@PathParam("iata") String iata,
                        @PathParam("lat") String latitude,
                        @PathParam("long") String longitude) {
        Repository repository = RepositoryFactory.getInstance();

        // Initial Validations
        try {
            AirportData.validateIATA(iata);
        } catch (InvalidIATAException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage()).build();
        }
        if (repository.containsAirport(iata)) {
            LOGGER.log(Level.WARNING,
                "Invalid IATA code received on addAirport operation: {0}", iata);
            final Response response = Response
                .status(Response.Status.CONFLICT)
                .entity("The iata airport: '" + iata + "' is already defined.")
                .build();
            return response;
        }

        try {
            AirportData airport = new AirportData(iata,
                Double.valueOf(latitude), Double.valueOf(longitude));
            airport.validate();

            repository.addAirport(airport);

            return OK_RESPONSE;
        } catch (InvalidAirportDataException ex) {
            LOGGER.log(Level.WARNING,
                "Invalid airport data received on addAirport operation: {0}",
                ex.getAirportData());
            final Response response = Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage())
                .build();
            return response;
        }
    }

    /**
     * This method do some validations and delegate the call to the repository.
     * @param iata The airport iata code to be deleted
     * @return OK if the airport is successfully removed or BAD_REQUEST if the
     * IATA validation fails, or NOT_FOUND if the airport does not exists.
     * @see WeatherCollectorEndpoint#deleteAirport(String)
     */
    @Override
    @DELETE @Path("/airport/{iata}")
    public Response deleteAirport(@PathParam("iata") String iata) {
        Repository repository = RepositoryFactory.getInstance();

        // Validations
        try {
            AirportData.validateIATA(iata);
        } catch (InvalidIATAException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage()).build();
        }

        try {
            repository.removeAirport(iata);
            return OK_RESPONSE;
        } catch (AirportNotFoundExcepition ex) {
            LOGGER.log(Level.WARNING, "Deleting unknown airport: {0}", ex.getIata());
            LOGGER.log(Level.FINEST, "Exception associated: ", ex);
            return NOT_FOUND_RESPONSE;
        }
    }

    /**
     * This method terminates the execution by calling System.exit(0).
     * IMPORTANT: Use with extreme caution.
     * @return noContent, but does not matter because the process is finished
     * before make the return.
     * @see WeatherCollectorEndpoint#exit
     */
    @Override
    @GET @Path("/exit")
    public Response exit() {
        //TODO: This is not appropiate but will be keep the API contract
        System.exit(0);
        return Response.noContent().build();
    }

}
