package testing;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import csv.CSVDataSource;
import csv.CSVFailureResponse;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.*;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spark.Spark.after;

public class TestCSVHandler {
	private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
	private JsonAdapter<Map<String, Object>> adapter;
	@BeforeAll
	public static void setup_before_everything() {
		Spark.port(3232); // Set the Spark port number
		// Changing the JDK *ROOT* logger's level (not global) will block messages
		//   (assuming using JDK, not Log4J)
		Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
	}
	@BeforeEach
	public void setup() {
		CSVDataSource csvDatasource = new CSVDataSource();

		// restart the entire Spark server for every test
		Spark.get("loadcsv", new LoadCSVHandler(csvDatasource));
		Spark.get("viewcsv", new ViewCSVHandler(csvDatasource));
		Spark.get("searchcsv", new SearchCSVHandler(csvDatasource));
		Spark.init();
		Spark.awaitInitialization(); // don't continue until the server is listening

		Moshi moshi = new Moshi.Builder().build();
		adapter = moshi.adapter(mapStringObject);
	}

	@AfterEach
	public void teardown() {
		// Gracefully stop Spark listening on both endpoints after each test
		Spark.unmap("loadcsv");
		Spark.unmap("viewcsv");
		Spark.unmap("searchcsv");
		Spark.awaitStop(); // don't proceed until the server is stopped
	}

	@Test
	public void testNoParamsError() throws IOException {
		// check that server does not crash even when no params are given
		HttpURLConnection loadConnection = tryRequest("loadcsv");
		assertEquals(200, loadConnection.getResponseCode());

		HttpURLConnection searchConnection = tryRequest("searchcsv");
		assertEquals(200, searchConnection.getResponseCode());

		Map<String, Object> loadResponseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
		assertEquals("error", loadResponseBody.get("response_type"));

		Map<String, Object> searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		loadConnection.disconnect();
		searchConnection.disconnect();
	}

	@Test
	public void testBeforeAfterLoading() throws IOException {
		// check that server does not crash even when no params are given
		HttpURLConnection viewConnection = tryRequest("viewcsv");
		assertEquals(200, viewConnection.getResponseCode());
		Map<String, Object> viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));

		HttpURLConnection searchConnection = tryRequest("searchcsv");
		assertEquals(200, searchConnection.getResponseCode());
		Map<String, Object> searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		// now load a csv to view
		viewConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeader=yes");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("success", viewResponseBody.get("response_type"));

		viewConnection = tryRequest("viewcsv");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("success", viewResponseBody.get("response_type"));

		searchConnection = tryRequest("searchcsv?searchTerm=2");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("success", searchResponseBody.get("response_type"));
		searchConnection = tryRequest("searchcsv?searchTerm=2&column=0&colIsString=no");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("success", searchResponseBody.get("response_type"));

		viewConnection.disconnect();
		searchConnection.disconnect();
	}

	@Test
	public void testInvalidSearchInput() throws IOException {
		// now load a csv to view
		HttpURLConnection viewConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeader=yes");

		// missing a param
		HttpURLConnection searchConnection = tryRequest("searchcsv?searchTerm=2&column=0");
		assertEquals(200, searchConnection.getResponseCode());
		Map<String, Object> searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		searchConnection = tryRequest("searchcsv?searchTerm=2&colIsString=no");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		// colIsString not "yes or no"
		searchConnection = tryRequest("searchcsv?searchTerm=2&column=0&colIsString=bananas");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		// column index out of bounds/does not exist
		searchConnection = tryRequest("searchcsv?searchTerm=2&column=20&colIsString=no");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		searchConnection = tryRequest("searchcsv?searchTerm=2&column=bananas&colIsString=yes");
		assertEquals(200, searchConnection.getResponseCode());
		searchResponseBody = adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
		assertEquals("error", searchResponseBody.get("response_type"));

		viewConnection.disconnect();
		searchConnection.disconnect();
	}

	@Test
	public void testInvalidLoadInput() throws IOException {
		// file does not exist
		HttpURLConnection viewConnection = tryRequest("loadcsv?filepath=bananas&hasHeader=yes");
		assertEquals(200, viewConnection.getResponseCode());
		Map<String, Object> viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));

		// no access to file
		viewConnection = tryRequest("loadcsv?filepath=stars/../stars/stardata.csv&hasHeader=yes");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));

		// hasHeader not provided
		viewConnection = tryRequest("loadcsv?filepath=stars/../stars/stardata.csv");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));

		// file not provided
		viewConnection = tryRequest("loadcsv?hasHeader=yes");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));

		// hasHeader not 'yes' or 'no'
		viewConnection = tryRequest("loadcsv?filepath=stars/../stars/stardata.csv&hasHeader=bananas");
		assertEquals(200, viewConnection.getResponseCode());
		viewResponseBody = adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
		assertEquals("error", viewResponseBody.get("response_type"));
	}


	/**
	 * Helper to make working with a large test suite easier: if an error, print more info.
	 * @param body
	 */
	private void showDetailsIfError(Map<String, Object> body) {
		if(body.containsKey("response_type") && "error".equals(body.get("response_type"))) {
			System.out.println(body.toString());
		}
	}

	/**
	 * Helper to start a connection to a specific API endpoint/params
	 *
	 * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
	 *     structure!)
	 * @return the connection for the given URL, just after connecting
	 * @throws IOException if the connection fails for some reason
	 */
	private static HttpURLConnection tryRequest(String apiCall) throws IOException {
		// Configure the connection (but don't actually send the request yet)
		URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
		HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

		// The default method is "GET", which is what we're using here.
		// If we were using "POST", we'd need to say so.
		clientConnection.setRequestMethod("GET");

		clientConnection.connect();
		return clientConnection;
	}
}
