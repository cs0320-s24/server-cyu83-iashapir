package testing;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.junit.jupiter.api.Test;
import server.BroadbandHandler;
import server.DataSource;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.crypto.Data;

//import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * An INTEGRATION TEST differs from a UNIT TEST in that it's testing
 * a combination of code units and their combined behavior.
 *
 * Test our API server: send real web requests to our server as it is
 * running. Note that for these, we prefer to avoid sending many
 * real API requests to the NWS, and use "mocking" to avoid it.
 * (There are many other reasons to use mock data here. What are they?)
 *
 * In short, there are two new techniques demonstrated here:
 * writing tests that send fake API requests; and
 * testing with mock data / mock objects.
 */

public class TestBroadbandHandler {

  // Helping Moshi serialize Json responses; see the gearup for more info.
  // NOTE WELL: THE TYPES GIVEN HERE WOULD VARY ANYTIME THE RESPONSE TYPE VARIES
  // We are testing an API that returns Map<String, Object>
  // It would be different if the response was, e.g., List<List<String>>.
  private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;
  private Map<String, Object> responseMap;

  @BeforeAll
  public static void setupOnce() {
    // Pick an arbitrary free port
    Spark.port(0);
    // Eliminate logger spam in console for test suite
   // Logger.getLogger("").setLevel(Level.WARNING); // empty name = root
  }

  @BeforeEach
  public void setup() {
    // Re-initialize parser, state, etc. for every test method

    // Use *MOCKED* data when in this test environment.
    // Notice that the WeatherHandler code doesn't need to care whether it has
    // "real" data or "fake" data. Good separation of concerns enables better testing.
    DataSource mockedSource = new MockedCensusDataSource("test");
    Spark.get("/broadband", new BroadbandHandler(mockedSource));
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);

  }

 // @AfterEach
  public void tearDown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/broadband");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * The "throws" clause doesn't matter below -- JUnit will fail if an
   *     exception is thrown that hasn't been declared as a parameter to @Test.
   *
   * @param apiCall the call string, including endpoint
   *                (Note: this would be better if it had more structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send a request yet)
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    // The request body contains a Json object
    clientConnection.setRequestProperty("Content-Type", "application/json");
    // We're expecting a Json object in the response body
    clientConnection.setRequestProperty("Accept", "application/json");

    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testWeatherRequestSuccess() throws IOException {
    /////////// LOAD DATASOURCE ///////////
    // Set up the request, make the request
    HttpURLConnection loadConnection = tryRequest("broadband?" + toOurServerParams("California", "Los Angeles"));
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("success", responseBody.get("type"));

    // Mocked data: correct temp? We know what it is, because we mocked.
    assertEquals(
        "test",
        responseBody.get("broadband data"));
    // Notice we had to do something strange above, because the map is
    // from String to *Object*. Awkward testing caused by poor API design...

    loadConnection.disconnect();
  }

  private String toOurServerParams(String state, String county){
    return "state=" + state + "&county=" + county;
  }

  /**
   * Helper to make working with a large test suite easier: if an error, print more info.
   * @param body
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if(body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body.toString());
    }
  }








}
