package com.example.trial.weather.impl;

import com.example.trial.weather.domain.AtmosphericInformation;
import com.example.trial.weather.WeatherQueryEndpoint;
import com.example.trial.weather.domain.AirportData;
import com.example.trial.weather.exceptions.AirportNotFoundExcepition;
import com.example.trial.weather.exceptions.InvalidIATAException;
import com.example.trial.weather.repository.Repository;
import com.example.trial.weather.repository.RepositoryFactory;
import com.example.trial.weather.repository.RepositoryUsageStatistics;
import com.google.gson.Gson;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the implementation of the Query endpoint, mainly just do validations
 * and delegates to the repository.
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

    private static final Logger LOGGER = Logger.getLogger(RestWeatherQueryEndpoint.class.getName());

    /**
     * Shared gson.
     */
    private static final Gson gson = new Gson();

    /**
     * This method gets the data from the repository and creates the result.
     * @see WeatherQueryEndpoint#ping()
     * @return A JSon formated Map with the required information
     */
    @Override
    @GET @Path("/ping")
    public String ping() {
        Map<String, Object> result = new HashMap<>();

        Repository repository = RepositoryFactory.getInstance();
        RepositoryUsageStatistics statistics = repository.getUsageStatistics();

        result.put("datasize", statistics.getDataPointCount());
        result.put("iata_freq", statistics.getIataCallFrecuencies());
        result.put("radius_freq", statistics.getRadiusCallsHistogram());

        return gson.toJson(result);
    }

    /**
     * This method performs validations and delegates the call to the repository.
     * @see WeatherQueryEndpoint#weather(String, String)
     * @param iata The iata code to get the information
     * @param radiusString The radius in km to look for alternatives
     * @return A Response containing a List of AtmosphericInformation objects
     */
    @Override
    @GET @Path("/weather/{iata}/{radius}") @Produces(MediaType.APPLICATION_JSON)
    public Response weather(@PathParam("iata") String iata,
                            @PathParam("radius") String radiusString) {
        try {
            Repository repository = RepositoryFactory.getInstance();

            // Validations
            AirportData.validateIATA(iata);
            //TODO: Improve radius validation

            double radius = radiusString == null ||
                radiusString.trim().isEmpty() ? 0 :
                    Double.valueOf(radiusString);

            Collection<AtmosphericInformation> result =
                repository.getAtmosphericInformation(iata,
                    radius == 0 ? null : radius);

            // We do this for compatibility with the old API implementation
            if (result.isEmpty()) {
                result.add(new AtmosphericInformation());
            }

            return Response.status(Response.Status.OK).entity(result).build();
        } catch (InvalidIATAException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ex.getMessage()).build();
        } catch (AirportNotFoundExcepition ex) {
            final String message = "The airport " + ex.getIata() +
                " for the requested where information was not found.";
            LOGGER.log(Level.WARNING, message);
            return Response.status(Response.Status.NOT_FOUND).entity(message)
                .build();
        } catch (NumberFormatException ex) {
            final String message = "There radius given for the weather "
                + " was not able to be parsed on the API call.";
            LOGGER.warning(message);
            LOGGER.log(Level.FINEST, "Exception associated: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).entity(message)
                .build();
        }
    }

}
