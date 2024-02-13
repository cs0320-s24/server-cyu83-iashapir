package server;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okio.Buffer;
import org.eclipse.jetty.util.IO;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&
 *
 * https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
 */
public class CensusDataSource {

//  private Map<String, Object> stateCodes;
  private List<List<String>> listStateCodes;
  private List<List<String>> countyCodes;


  public String getData(String stateName, String countyName) throws DatasourceException {
      String stateCode = this.findStateCode(stateName);
      String countyCode = this.findCountyCode(stateCode, countyName);

      try {
          // build request to get broadband data from census api for given state and county
          URL requestURL = new URL("https","api.census.gov",
                  "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"+countyCode+"&in=state:"+stateCode);
          HttpURLConnection clientConnection = connect(requestURL);
          Moshi moshi = new Moshi.Builder().build();
          Type listListString = Types.newParameterizedType(List.class, List.class, String.class);
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);

          List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

          clientConnection.disconnect();

          // validity checks for response
          if (body == null || body.size() != 2 || body.get(0).size() != 4) {
              throw new DatasourceException("Malformed response from Census API");
          }

          return body.get(1).get(1); // get broadband data
      } catch (IOException e) {
          throw new DatasourceException(e.getMessage(), e);
      }
  }

  /**
   * Method to find the stateCode given a state name
   * Will first check if hashmap is null (better way??), if so need
   * to query census
   * If not can just refer to state code hashmap
   * Throw all exceptions because we want to handle them in the handler!
   * @return
   */
  public String findStateCode(String stateName) throws DatasourceException{
    System.out.println("find state codes called");
    if(this.listStateCodes==null){
      try{
        this.requestStateCodes();
      }
      catch(IOException e){
        System.out.println("caught IO exception from request");
        throw new DatasourceException(e.getMessage());
      }
    }
    try{
      System.out.println("trying to get stateName from hashmap");
      for (int i = 1; i < listStateCodes.size(); i++) {
          if (listStateCodes.get(i).get(0).equals(stateName)) {
              return listStateCodes.get(i).get(1);
          }
      }
      throw new DatasourceException("State name was not found!");
    }
    /**TODO: figure out a better way to do this, rn all exception caught here?*/
    catch(Exception e){
      throw new DatasourceException(e.getMessage());
    }
  }

    /**
     * Method to find the countyCode given a county name
     * @return
     */
    public String findCountyCode(String stateCode, String countyName) throws DatasourceException{
        if(this.countyCodes==null || !this.countyCodes.get(1).get(2).equals(stateCode)) { // countyCodes saved but not the right state
            try {
                this.requestCountyCodes(stateCode);
            } catch (IOException e) {
                System.out.println("caught IO exception from request");
                throw new DatasourceException(e.getMessage());
            }
        }
        try{
            System.out.println("trying to get stateName from hashmap");
            for (int i = 1; i < countyCodes.size(); i++) {
                String countyAndState = countyCodes.get(i).get(0);
                if (countyAndState.substring(0, countyAndState.indexOf(",")).equals(countyName)) { // parse string "county, state" to just county
                    return countyCodes.get(i).get(2);
                }
            }
            throw new DatasourceException("County name was not found!");
        }
        /**TODO: figure out a better way to do this, rn all exception caught here?*/
        catch(Exception e){
            throw new DatasourceException(e.getMessage());
        }
    }

    /**
     * requests countyCodes from census
     * calls deserialize to turn them into hashmap
     * stores that hashmap as instance variable
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    private void requestCountyCodes(String stateCode) throws IOException, DatasourceException {
        //builds request to census for state code data
        URL requestURL =
                new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"+stateCode);
        HttpURLConnection clientConnection = connect(requestURL);
        Moshi moshi = new Moshi.Builder().build();
        Type listListString = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);
        try {
            List<List<String>> body =
                    adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
            clientConnection.disconnect();
            if (body == null) {
                throw new DatasourceException("Malformed response from Census API");
            }
            //this.stateCodes = body;
            System.out.println(body);
            this.countyCodes = body;
        } catch (Exception e) { //changed to Exception for debugging
            System.out.println(e.getMessage());
        }
    }

  /**
   * requests stateCodes from census
   * calls deserialize to turn them into hashmap
   * stores that hashmap as instance variable
   * @return
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException
   */
  private void requestStateCodes() throws IOException, DatasourceException {
      System.out.println("request state codes called");
      //builds request to census for state code data
      URL requestURL =
          new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientConnection = connect(requestURL);
      System.out.println("connected to client");
      Moshi moshi = new Moshi.Builder().build();
      System.out.println("built moshi");
      Type listListString = Types.newParameterizedType(List.class, List.class, String.class);
      System.out.println("making the type");
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);
      System.out.println("made adapter");
      try {
        List<List<String>> body =
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        System.out.println("got body of request using adapter");
        clientConnection.disconnect();
        System.out.println("disconnected from client");
        if (body == null) {
          System.out.println("body was null");
          throw new DatasourceException("Malformed response from Census API");
        }
        //this.stateCodes = body;
        System.out.println(body);
        this.listStateCodes = body;
      } catch (Exception e) { //changed to Exception for debugging
        System.out.println(e.getMessage());
      }
//      System.out.println("got body of request using adapter");
//      clientConnection.disconnect();
//      System.out.println("disconnected from client");
//      if (body == null) {
//        System.out.println("body was null");
//        throw new DatasourceException("Malformed response from Census API");
//      }
//      this.stateCodes = body;
//    }
    }

  /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(! (urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }

  /**
   * method to deserialize state codes from json string--not used currently
   * @param jsonStateCodes
   * @return
   * @throws IOException
   */
  private HashMap<String, String> deserializeStateCodes(String jsonStateCodes) throws IOException {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<HashMap<String,String>> adapter = moshi.adapter(
          Types.newParameterizedType(HashMap.class, String.class, String.class));
      return adapter.fromJson(jsonStateCodes);
  }



  /***********RECORDS FOR DATA STORAGE*****************/
  public record CodeStorage(String stateCode, String countyCode){}
}