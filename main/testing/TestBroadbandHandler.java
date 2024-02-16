package testing;

import com.google.common.cache.CacheStats;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.junit.jupiter.api.Test;
import server.BroadbandHandler;
import censusAPI.CachingCensusDataSource;
import censusAPI.CensusDataSource;
import censusAPI.DataSource;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


/**
 * Test the broadband handler by creating a mock datasource to test success responses
 * and a real datasource to test that inputting invalid states/counties/parameters will
 * give correct error responses from our API
 */
public class TestBroadbandHandler {

  private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * set the port
   */
  @BeforeAll
  public static void setupOnce() {
    // Pick an arbitrary free port
    Spark.port(3232);
    // Eliminate logger spam in console for test suite
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root
  }

  /**
   * Setup the mockHandler if we're using it for a given test
   */
  public void setupMockedHandler() {
    //initialize mockedSource
    DataSource mockedSource = new MockedCensusDataSource("test");
    Spark.get("/broadband", new BroadbandHandler(mockedSource));
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  /**
   * setup a real handler if we're using it for a given test
   */
  public void setupRealHandler(){
    DataSource realSource = new CensusDataSource();
    Spark.get("/broadband", new BroadbandHandler(realSource));
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }


  /**
   * Gracefully stop Spark listening on both endpoints and ensure server stopped
   */
  @AfterEach
  public void tearDown() {
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
  private HttpURLConnection tryRequest(String apiCall) throws IOException{
    // Configure the connection (but don't actually send a request yet)
      URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
      HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
      // The request body contains a Json object
      clientConnection.setRequestProperty("Content-Type", "application/json");
      // We're expecting a Json object in the response body
      clientConnection.setRequestProperty("Accept", "application/json");

      clientConnection.connect();
      System.out.println("returning client connection");
      return clientConnection;

  }

  /**
   * Use our mock handler to test success when inputting state and county parameters
   * @throws IOException
   */
  @Test
  public void testStateCountyRequestSuccess() throws IOException {
    setupMockedHandler();
    /////////// LOAD DATASOURCE ///////////
    // Set up the request, make the request
    HttpURLConnection loadConnection = tryRequest("broadband?" + toOurServerParams("California", "FakeCounty"));
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    System.out.println(responseBody);
    assertEquals("success", responseBody.get("result"));
    assertEquals("test", responseBody.get("broadband data"));
    loadConnection.disconnect();
  }

  /**
   * Use our mock handler to test error response due to missing parameters
   * @throws IOException
   */
  @Test
  public void testStateCountyRequestBadParams() throws IOException{
    setupMockedHandler();
    HttpURLConnection loadConnection = tryRequest("broadband?state=California");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    System.out.println(responseBody);
    assertEquals("error", responseBody.get("result"));
    assertEquals("missing_parameter", responseBody.get("error_type"));
    assertEquals("California", responseBody.get("state name"));
    loadConnection.disconnect();
  }

  /**
   * Using real handler to test InvalidArgumentExceptions thrown due to bad State/County names
   * Pass in illegal argument (county = chicken)
   * Test that handler works as it should
   * @throws IOException
   */
  @Test
  public void testCountyInvalidArg() throws IOException{
    setupRealHandler();
    HttpURLConnection loadConnection = tryRequest("broadband?state=California&county=Chicken");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    System.out.println(responseBody);
    assertEquals("error", responseBody.get("result"));
    assertEquals("County: 'Chicken' not valid.", responseBody.get("type"));
    assertEquals("California", responseBody.get("state name"));
    assertEquals("Chicken", responseBody.get("county name"));
    loadConnection.disconnect();
  }

  /**
   * Using real handler to test InvalidArgumentExceptions thrown due to bad State/County names
   * Pass in illegal argument (State = chicken)
   * Test that handler works as it should
   * @throws IOException
   */
  @Test
  public void testStateInvalidArg() throws IOException{
    setupRealHandler();
    HttpURLConnection loadConnection = tryRequest("broadband?state=Chicken&county=Alameda");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    System.out.println(responseBody);
    assertEquals("error", responseBody.get("result"));
    assertEquals("State: 'Chicken' not valid.", responseBody.get("type"));
    assertEquals("Chicken", responseBody.get("state name"));
    assertEquals("Alameda", responseBody.get("county name"));
    loadConnection.disconnect();
  }

  /**
   * Test that the cache Integration functioning properly
   * @throws IOException
   */
  @Test
  public void testCachingSource() throws IOException{
    //setup stuff (so we have datasource in here)
    MockedCensusDataSource mockedSource = new MockedCensusDataSource("fakeResult");
    CachingCensusDataSource cachingSource = new CachingCensusDataSource(mockedSource, 10, 4);
    Spark.get("/broadband", new BroadbandHandler(cachingSource));
    Spark.awaitInitialization(); // don't continue until the server is listening
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);

    HttpURLConnection loadConnection = tryRequest("broadband?state=California&county=fakeCounty");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    // Get the expected response: a success
    Map<String, Object> responseBody = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(responseBody);
    System.out.println(responseBody);

    //test expected values for ENTRY 1
    assertEquals("success", responseBody.get("result"));
    assertEquals("fakeResult", responseBody.get("broadband data"));
    CacheStats stats = cachingSource.getStats();
    assertTrue(stats.hitCount()==0);

    //query something different
    //loadConnection = tryRequest("broadband?state=Maryland&county=fakeCounty");
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + "broadband?state=Maryland&county=fakeCounty");
    requestURL.openConnection();

    //test expected values for ENTRY 2
    assertEquals("success", responseBody.get("result"));
    assertEquals("fakeResult", responseBody.get("broadband data"));
    stats = cachingSource.getStats();
    assertTrue(stats.hitCount()==0);

    //query the same thing as before
    //loadConnection = tryRequest("broadband?state=California&county=fakeCounty");

    requestURL = new URL("http://localhost:" + Spark.port() + "/broadband?state=California&county=fakeCounty");
    requestURL.openConnection();

    //test expected values for ENTRY 3
    assertEquals("success", responseBody.get("result"));
    assertEquals("fakeResult", responseBody.get("broadband data"));

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
