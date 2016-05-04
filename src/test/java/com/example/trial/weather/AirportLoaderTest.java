package com.example.trial.weather;

import com.example.trial.weather.AirportLoader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class AirportLoaderTest {

    // Sample lines for testing
    private static String SAMPLE_FILE_LINE_BOS     = "1,\"General Edward Lawrence Logan Intl\",\"Boston\",\"United States\",\"BOS\",\"KBOS\",42.364347,-71.005181,19,-5,\"A\"";
    private static String MISSING_FIELD_FILE_LINE  =   "\"General Edward Lawrence Logan Intl\",\"Boston\",\"United States\",\"BOS\",\"KBOS\",42.364347,-71.005181,19,-5,\"A\"";
    private static String INCORRECT_IATA_FILE_LINE = "1,\"General Edward Lawrence Logan Intl\",\"Boston\",\"United States\",\"BS\",\"KBOS\",42.364347,-71.005181,19,-5,\"A\"";

    @Test
    public void testSampleOneByOneCase() throws Exception {
        Map<String, Object> context = prepareContext();
        AirportLoader loader = (AirportLoader) context.get("loader");

        Reader reader = new StringReader(SAMPLE_FILE_LINE_BOS);
        loader.uploadOneByOne(reader);

        WebTarget collect = (WebTarget) context.get("collect");
        verify(collect, times(1)).path("/airport/BOS/42.364347/-71.005181");
    }

    @Test
    public void testIncorrectlyFormatedFileLinesIgnored() throws Exception {
        Map<String, Object> context = prepareContext();
        AirportLoader loader = (AirportLoader) context.get("loader");

        // It has 1 field less
        Reader reader = new StringReader(MISSING_FIELD_FILE_LINE);
        loader.uploadOneByOne(reader);
        
        WebTarget collect = (WebTarget) context.get("collect");
        verify(collect, never()).path(any(String.class));
    }

    @Test
    public void testOnlyIncorrectlyFormatedFileLinesIgnored() throws Exception {
        Map<String, Object> context = prepareContext();
        AirportLoader loader = (AirportLoader) context.get("loader");

        Reader reader = new StringReader(SAMPLE_FILE_LINE_BOS + "\n"
            + MISSING_FIELD_FILE_LINE);
        loader.uploadOneByOne(reader);

        WebTarget collect = (WebTarget) context.get("collect");
        verify(collect, times(1)).path("/airport/BOS/42.364347/-71.005181");
    }

    @Test
    public void testIncorrectAirportFileLinesReportingError() throws Exception {
        Map<String, Object> context = prepareContext();
        AirportLoader loader = (AirportLoader) context.get("loader");

        Reader reader = new StringReader(INCORRECT_IATA_FILE_LINE);
        loader.uploadOneByOne(reader);

        WebTarget collect = (WebTarget) context.get("collect");
        verify(collect, times(1)).path("/airport/BS/42.364347/-71.005181");

        Response response = (Response) context.get("response");
        when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * This method prepares the context for a successful execution, that can be
     * later altered in order to create different scenarios.
     * A context is a collection of all the intermediate objects mocked in order
     * to execute a call
     * @return The context prepared
     */
    private Map<String, Object> prepareContext() {
        Map<String, Object> context = new HashMap<>();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        context.put("response", response);

        Builder builder = mock(Builder.class);
        when(builder.post(null, Response.class)).thenReturn(response);
        context.put("builder", builder);

        WebTarget path = mock(WebTarget.class);
        when(path.request()).thenReturn(builder);
        context.put("path", path);

        WebTarget collect = mock(WebTarget.class);
        when(collect.path(any(String.class))).thenReturn(path);
        context.put("collect", collect);

        // Class used to inhibit the client initialization
        class AirportLoaderMock extends AirportLoader {
            public AirportLoaderMock(Client client) {
                this.client = client;
            }
            public void init(String serverBaseUrl) {
            }
        }

        Client client = mock(Client.class);
        when(client.target(any(String.class))).thenReturn(collect);
        AirportLoader loader = new AirportLoaderMock(client);
        context.put("loader", loader);

        return context;
    }

}
