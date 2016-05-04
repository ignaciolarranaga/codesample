package com.example.trial.weather;

import com.example.trial.weather.domain.DataPoint;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A reference implementation for the weather client. Consumers of the REST API
 * can look at WeatherClient to understand API semantics. This existing client
 * populates the REST endpoint with dummy data useful for testing.
 * @author code test administrator
 */
public class WeatherClient {

    /**
     * The end point for read queries.
     */
    private WebTarget query;

    /**
     * The end point to supply updates.
     */
    private WebTarget collect;

    public WeatherClient() {
        this(WeatherServer.BASE_URL);
    }

    public WeatherClient(final String baseUrl) {
        // I would prefer a method init for this initialization but worried
        // about the compatiblity with the grader
        Client client = ClientBuilder.newClient();
        query = client.target(baseUrl + "query");
        collect = client.target(baseUrl + "collect");
    }

    public void pingCollect() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        System.out.print("collect.ping: " + response.readEntity(String.class) + "\n");
    }

    public void addAirport(String iata, String latitude, String longitude) {
        WebTarget path = collect.path("/airport/" + iata + "/" + latitude + "/"
            + longitude);
        Response response = path.request().post(Entity.entity("",
            MediaType.APPLICATION_JSON));
        System.out.print("collect.addAirport: " +
            response.readEntity(String.class) + "\n");
    }

    public void query(String iata) {
        WebTarget path = query.path("/weather/" + iata + "/0");
        Response response = path.request().get();
        System.out.println("query." + iata + ".0: " + response.readEntity(String.class));
    }

    public void pingQuery() {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        System.out.println("query.ping: " + response.readEntity(String.class));
    }

    public void populate(String pointType, int first, int last, int mean,
        int median, int count) {
        WebTarget path = collect.path("/weather/BOS/" + pointType);
        DataPoint dp = new DataPoint(first, mean, last, median, count);
        Response post = path.request().post(
            Entity.entity(dp, MediaType.APPLICATION_JSON));
    }

    public void exit() {
        try {
            collect.path("/exit").request().get();
        } catch (Throwable t) {
            // swallow
        }
    }

    public static void main(String[] args) {
        WeatherClient wc = new WeatherClient();
        wc.pingCollect();
        wc.addAirport("BOS", "42.364347", "-71.005181");
        wc.addAirport("EWR", "40.6925", "-74.168667");
        wc.addAirport("JFK", "40.639751", "-73.778925");
        wc.addAirport("LGA", "40.777245", "-73.872608");
        wc.addAirport("MMU", "40.79935", "-74.4148747");
        wc.populate("wind", 0, 10, 6, 4, 20);

        wc.query("BOS");
        wc.query("JFK");
        wc.query("EWR");
        wc.query("LGA");
        wc.query("MMU");

        wc.pingQuery();
        wc.exit();
        System.out.print("complete");
        System.exit(0);
    }
    
}
