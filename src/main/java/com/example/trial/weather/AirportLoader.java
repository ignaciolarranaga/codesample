package com.example.trial.weather;

import com.example.trial.weather.domain.AirportData;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A simple airport loader which reads a file from disk and sends entries to
 * the web service.
 *
 * IMPORTANT: The file structure was corrected based on the sample
 * The expected structure of the read input is as follows:
 *
 * Header   | Description
 * ---------|------------
 * Row      | Row number (GUESSED)
 * Name     | Airport name (GUESSED)
 * City     | Main city served by airport. May be spelled differently from name.
 * Country  | Country or territory where airport is located.
 * IATA/FAA | 3-letter FAA code or IATA code (blank or "" if not assigned)
 * ICAO     | 4-letter ICAO code (blank or "" if not assigned)
 * Latitude | Decimal degrees, up to 6 significant digits. Negative is South, positive is North.
 * Longitude| Decimal degrees, up to 6 significant digits. Negative is West, positive is East.
 * Altitude | In feet
 * Timezone | Hours offset from UTC. Fractional hours are expressed as decimals. (e.g. India is 5.5)
 * DST      | One of E (Europe), A (US/Canada), S (South America), O (Australia), Z (New Zealand), N (None) or U (Unknown)
 *
 * @author code test administrator
 */
public class AirportLoader implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(AirportLoader.class.getName());

    /**
     * This is the default batch size used for uploads.
     */
    private static final int DEFAULT_BATCH_SIZE = 500;

    /**
     * Expected input file columns count.
     */
    private static final int FILE_EXPECTED_COLUMN_COUNT = 11;

    /**
     * Column indexes of the file (starting from 0).
     */
    private static final int IATA_CODE_FILE_COLUMN_INDEX = 4;
    private static final int LONGITUDE_FILE_COLUMN_INDEX = 7;
    private static final int LATITUDE_FILE_COLUMN_INDEX = 6;

    /**
     * This is the base URL used by the autoloader.
     */
    private final String serverBaseUrl;

    /**
     * The client used to access the server.
     */
    protected Client client;

    /**
     * Default constructor, assumes the BASE_URL to be the one defined in
     * WeatherServer.
     * @see WeatherServer
     */
    public AirportLoader() {
        this(WeatherServer.BASE_URL);
    }

    /**
     * @param serverBaseUrl Is the base address for the server
     */
    public AirportLoader(final String serverBaseUrl) {
        // I would prefer a method init for this initialization but worried
        // about the compatiblity with the grader
        this.serverBaseUrl = serverBaseUrl;

        client = ClientBuilder.newClient();
    }

    /**
     * This method closes the airport loader releasing all the resources.
     */
    @Override
    public final void close() {
        client.close();
    }

    /**
     * This method uploads the airports information (obtained from reader) to
     * the server, using single add operations (i.e. the old API signature).
     * The data has to be formatted as expected (check the class comments for
     * details). The loader will ignore/log failed rows.
     * @param reader The reader that is going to be used to access the data
     * @throws IOException If an error accessing the data occurs
     */
    public final void uploadOneByOne(final Reader reader) throws IOException {
        WebTarget collect = client.target(serverBaseUrl + "collect");

        CSVReader csvReader = new CSVReader(reader);
        String[] nextLine;
        for (int i = 0; (nextLine = csvReader.readNext()) != null; i++) {
            // Validations
            if (nextLine.length != FILE_EXPECTED_COLUMN_COUNT) {
                LOGGER.log(Level.SEVERE, "Ignoring line {0} because it doesn't "
                    + "match the structure (does not have the specified amount "
                    + "of fields).", i);
                continue;
            }

            final String iata = nextLine[IATA_CODE_FILE_COLUMN_INDEX];
            final String latitude = nextLine[LATITUDE_FILE_COLUMN_INDEX];
            final String longitude = nextLine[LONGITUDE_FILE_COLUMN_INDEX];

            WebTarget path = collect.path("/airport/" + iata + "/" + latitude + "/" + longitude);

            Response response = null;
            try {
                response = path.request().post(null, Response.class);
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    LOGGER.log(Level.SEVERE,
                        "There was an error adding the airport {0}: {1}",
                        new Object[]{ iata, response.readEntity(String.class) });
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }

        LOGGER.log(Level.INFO, "Loading process finished, {0} lines readed.",
            csvReader.getLinesRead());

        if (csvReader.getLinesRead() != csvReader.getRecordsRead()) {
            LOGGER.log(Level.WARNING, "{0} lines were not able to be readed.",
                csvReader.getLinesRead() - csvReader.getRecordsRead());
        }
    }

    /**
     * This method does the same operation than #uploadOneByOne but in batch.
     * A new API method was added for this purpose not interfering with the old
     * API signature.
     * The data has to be formatted as expected (check the class comments for
     * details). The loader will ignore/log failed rows.
     * @param reader The reader that is going to be used to access the data
     * @param batchSize The size of the chunk to be uploaded
     * @throws IOException If an error accessing the data occurs
     */
    public final void uploadInBatch(final Reader reader, final int batchSize)
        throws IOException {
        CSVReader csvReader = new CSVReader(reader);
        String[] nextLine;
        List<AirportData> queue = new ArrayList<>();
        for (int i = 0; (nextLine = csvReader.readNext()) != null; i++) {
            // Validations
            if (nextLine.length != FILE_EXPECTED_COLUMN_COUNT) {
                LOGGER.log(Level.SEVERE, "Ignoring line {0} because it doesn't "
                    + "match the structure (does not have the specified amount "
                    + "of fields).", i);
                continue;
            }

            final String iata = nextLine[IATA_CODE_FILE_COLUMN_INDEX];
            final String latitude = nextLine[LATITUDE_FILE_COLUMN_INDEX];
            final String longitude = nextLine[LONGITUDE_FILE_COLUMN_INDEX];

            queue.add(new AirportData(iata, Double.valueOf(latitude),
                Double.valueOf(longitude)));

            // Uploading each batchSize elements
            if ((i + 1) % batchSize == 0) {
                uploadBatch(queue);
                queue.clear();
            }
        }

        // Uploading the possible remaining elements
        if (!queue.isEmpty()) {
            uploadBatch(queue);
        }

        LOGGER.log(Level.INFO, "Loading process finished, {0} lines processed.",
            csvReader.getLinesRead());

        if (csvReader.getLinesRead() != csvReader.getRecordsRead()) {
            LOGGER.log(Level.WARNING, "{0} lines were not able to be readed.",
                csvReader.getLinesRead() - csvReader.getRecordsRead());
        }
    }

    /**
     * This method uploads a batch of a airports to the server.
     * @param batch The batch to be uploaded
     */
    private void uploadBatch(final List<AirportData> batch) {
        WebTarget collect = client.target(serverBaseUrl + "collect");
        WebTarget path = collect.path("/airports/");

        Response response = null;
        try {
            response = path.request().post(
                Entity.entity(batch, MediaType.APPLICATION_JSON));
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LOGGER.log(Level.SEVERE,
                    "There was an error adding the airports: {0}",
                    response.readEntity(String.class));
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public static void main(String args[]) throws IOException{
        File airportDataFile = new File(args[0]);
        if (!airportDataFile.exists() || airportDataFile.length() == 0) {
            LOGGER.log(Level.SEVERE, "{0} is not a valid input",
                airportDataFile);
            System.exit(1);
        }

        try (AirportLoader loader = new AirportLoader()) {
            try (Reader reader = new FileReader(airportDataFile)) {
                loader.uploadInBatch(reader, DEFAULT_BATCH_SIZE);
            }
        }

        System.exit(0);
    }

}
